package ut2004.exercises.e02;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Configuration;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.*;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import ut2004.exercises.e02.valkovic.*;

/**
 * EXERCISE 02
 * -----------
 * <p>
 * Implement a WolfBot(s) that will be able to catch all the sheeps as fast as possible!
 * <p>
 * No shooting allowed, no speed reconfiguration allowed.
 * <p>
 * Just use {@link AdvancedLocomotion#moveTo(cz.cuni.amis.pogamut.base3d.worldview.object.ILocated)},
 * {@link AdvancedLocomotion#strafeTo(cz.cuni.amis.pogamut.base3d.worldview.object.ILocated, cz.cuni.amis.pogamut.base3d.worldview.object.ILocated)}
 * {@link AdvancedLocomotion#jump()} and {@link AdvancedLocomotion#dodge(cz.cuni.amis.pogamut.base3d.worldview.object.Location, boolean)}
 * and alikes to move your bot!
 * <p>
 * To start scenario:
 * 1. blend directory ut2004 with you UT2004 installation
 * 2. start DM-TagMap using startGamebotsDMServer-DM-TagMap.bat
 * 3. start SheepBot
 * -- it will launch 12 agents (Sheeps) into the game
 * 4. start WolfBot
 * 5. one of your WolfBot has to say "start" to start the match or "restart" to re/start the match
 * <p>
 * Behavior tips:
 * 1. in this exercise, you can implement the communication using statics, i.e., both your Wolfs are running
 * within the same JVM - make use of that - and watch out for race-conditions (synchronized(MUTEX){ ... } your critical stuff)
 * 2. first, you have to check that both your wolfs are kicking and you should issue "start" message
 * 3. do not start playing before that ;) ... check {@link Utils#gameRunning} whether the game is running
 * 4. you catch the sheep by bumping to it (getting near to it...)
 * 5. count how many sheeps are still alive (via implementing PlayerKilled listener correctly) to know when to restart the match!
 * -- how fast can you take them out all?
 *
 * @author Jakub Gemrot aka Jimmy aka Kefik
 */
@SuppressWarnings("rawtypes")
@AgentScoped
public class WolfBot extends UT2004BotModuleController {

    private static final int SPEED = 270;
    private static final int PLAN_AHEAD = 8;
    private static final int SIMULATE = 32;
    private static final int SCAN_ROTATION = 60;
    private static final int TRIGGER_DISTANCE = 400;
    private static final int AT_POINT_DISTANCE = 100;
    private static AtomicInteger INSTANCES_COUNT = new AtomicInteger(0);

    private FPSCounter _fps = new FPSCounter();
    private int _state = 0;
    private NavigationModule _navModule;
    private InitialScan _initialScan = new InitialScan(SCAN_ROTATION);
    private Map<String, Location> _lastKnownLocations = new HashMap<>(30, 0.6f);
    private final Object _lastKNowLocationsMutex = new Object();
    private PlanningThread _planningThreadInstance;
    private Thread _planningThreadExecutor;
    private int _alive = 12;
    private Instant _roundStart = Instant.now();
    private Instant _roundEnd;

    /**
     * Here we can modify initializing command for our bot, e.g., sets its name or skin.
     *
     * @return instance of {@link Initialize}
     */
    @Override
    public Initialize getInitializeCommand() {
        int instance = INSTANCES_COUNT.getAndIncrement();
        return new Initialize().setName("WolfBot-" + instance).setSkin(UT2004Skins.getSkin());
    }

    /**
     * Bot is ready to be spawned into the game; configure last minute stuff in here
     *
     * @param gameInfo information about the game type
     * @param config   information about configuration
     * @param init     information about configuration
     */
    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange config, InitedMessage init) {
        // ignore any Yylex whining...
        bot.getLogger().getCategory("Yylex").setLevel(Level.OFF);
        log.setLevel(Level.ALL);
        // act every 100 ms!
        act.act(new Configuration().setVisionTime(0.1).setSyncNavPointsOff(true));
    }

    /**
     * This method is called only once, right before actual logic() method is called for the first time.
     */
    @Override
    public void beforeFirstLogic() {
        // enable manual spawning
        act.act(new Configuration().setManualSpawn(true));
        // create markov chain
        _navModule = new NavigationModule(this.navPoints.getNavPoints(), SPEED);
        _navModule.preprocess(SIMULATE, PLAN_AHEAD);
    }

    @Override
    public void botKilled(BotKilled event) {
        if(_planningThreadExecutor != null){
            _planningThreadExecutor.interrupt();
            _planningThreadExecutor = null;
            _planningThreadInstance = null;
        }
        _alive = 12;
        _roundStart = Instant.now();
        _state = 0;
    }



    @EventListener(eventClass=PlayerKilled.class)
    public void playerKilled(PlayerKilled event) {
        _alive--;
    }


    /**
     * Say something through the global channel + log it into the console...
     *
     * @param msg Received message.
     */
    private void sayGlobal(String msg) {
        // Simple way to send msg into the UT2004 chat
        body.getCommunication().sendGlobalTextMessage(msg);
        // And user log as well
        // log.info(msg);
    }

    String _otherWolf = null;
    boolean _otherConfirmed = false;

    @EventListener(eventClass = GlobalChat.class)
    public void chatReceived(GlobalChat msg) {
        Utils.handleMessage(msg);
        if (msg.getText().toLowerCase().contains("restart")) {
            body.getAction().respawn();
            return;
        }
        if (msg.getText().startsWith("Wolf")) {
            _otherWolf = msg.getName();
        }
        if (msg.getText().startsWith("WolfConfirm:") && msg.getText().endsWith(info.getName())) {
            _otherConfirmed = true;
        }
        if(msg.getText().startsWith("Pos:")){
            updateEntityLocation(msg.getName(), parseLocation(msg.getText().substring(4)));
        }
        if(msg.getText().startsWith("Sheep:")){
            String content = msg.getText().substring(6);
            String[] split = content.split(";", 2);
            updateEntityLocation(split[0], parseLocation(split[1]));
        }
    }

    private Location parseLocation(String text){
        String content = text.substring(1, text.length() - 2);
        String[] locations = content.split(";");
        return new Location(
                Double.parseDouble(locations[0]),
                Double.parseDouble(locations[1]),
                Double.parseDouble(locations[2])
        );
    }

    private void updateEntityLocation(String name, Location loc){
        synchronized (_lastKNowLocationsMutex) {
            _lastKnownLocations.put(name, loc);
        }
    }

    /**
     * Main method called 4 times / second. Do your action-selection here.
     */
    @Override
    public void logic() throws PogamutException {
        _fps.tick();
        double delta_time = _fps.getDelta();

        if (!Utils.gameRunning) {
            if (INSTANCES_COUNT.get() == 2)
                    sayGlobal("restart");
            return;
        }

        if(_planningThreadExecutor == null){
            _planningThreadInstance = new PlanningThread(
                    _navModule,
                    _lastKnownLocations,
                    _lastKNowLocationsMutex,
                    log,
                    PLAN_AHEAD
            );
            _planningThreadInstance.get_currentLocation().set(info.getLocation());
            _planningThreadExecutor = new Thread(
                    _planningThreadInstance,
                    info.getName() + "_planningthread"
            );
            _planningThreadExecutor.start();
        }
        _planningThreadInstance.get_currentLocation().set(info.getLocation());


        // tell my position
        sayGlobal("Pos:" + info.getLocation());
        // tell positions of players
        updateEntityLocation(info.getName(), bot.getLocation());
        for(Player sheep : players.getVisiblePlayers().values()){
            if(sheep.getId().getStringId().equals(_otherWolf))
                continue;
            updateEntityLocation(sheep.getName(), sheep.getLocation());
            sayGlobal(String.format(
                    "Sheep:%s;%s",
                    sheep.getName(),
                    sheep.getLocation()
            ));
        }

        log.info("State: " + _state);

        // planning result
        AtomicReference<Location> targetLocation = _navModule.getNavLocation();

        //if(_alive == 0)
        //    _state = 98;

        if (_state == 0) { // find other wolfs
            sayGlobal("Wolf");
            if (_otherWolf != null)
                _state = 1;
        }
        if (_state == 1) { // confirm
            sayGlobal("Wolf");
            sayGlobal("WolfConfirm:" + _otherWolf);
            if (_otherConfirmed)
                _state = 2;
            _planningThreadInstance.get_otherWolf().set(_otherWolf);
        }
        if(_state == 2){ // initial scan
            if(_initialScan.isDone())
                _state = 3;
            move.turnHorizontal(_initialScan.getRotation());
            _initialScan.rotationPerformed();
        }
        if(_state == 3) {// use Markov chain
            if(targetLocation.get() != null) {
                log.info("Using plan");
                navigation.navigate(targetLocation.get());
            }
            Player nearestSheep = null;
            if(players.getVisiblePlayers().size() > 0) {
                nearestSheep = Utils.getNearestSheep(
                        players.getVisiblePlayers().values().toArray(new Player[0]),
                        info.getLocation()
                );
            }
            if(nearestSheep != null && nearestSheep.getLocation().getDistance(info.getLocation()) < TRIGGER_DISTANCE) {
                _state = 4;
            }
            else if (targetLocation.get() == null ||
                    Utils.distanceFrom(info.getLocation(), targetLocation.get()) < AT_POINT_DISTANCE
            ){
                log.info("Not using plan");
                targetLocation.set(null);
                if(nearestSheep != null)
                    move.moveTo(nearestSheep.getLocation());
                else
                    move.turnHorizontal(SCAN_ROTATION);
            }

        }
        if(_state == 4){ // triggered
            Player nearestSheep = Utils.getNearestSheep(
                    players.getVisiblePlayers().values().toArray(new Player[0]),
                    info.getLocation()
            );
            if(nearestSheep == null || Utils.distanceFrom(info.getLocation(), nearestSheep.getLocation()) > TRIGGER_DISTANCE){
                _state = 3;
                return;
            }
            move.turnTo(nearestSheep);
            move.dodge(nearestSheep.getLocation().sub(info.getLocation()), true);
        }
        if (_state == -1) { // old behavior
            if (!players.canSeePlayers() ||
                    _otherWolf.equals(players.getNearestVisiblePlayer().getName())) {
                move.turnHorizontal(30);
            } else {
                navigation.stopNavigation();
                navigation.navigate(players.getNearestVisiblePlayer());
            }
        }
        if(_state == 98) { //win
            _roundEnd = Instant.now();
            _state = 99;
        }
        if(_state == 99) { //win
            sayGlobal("We won in " + Duration.between(_roundEnd, _roundStart).toMillis() / 1000 + " s");
        }
    }

    /**
     * This method is called when the bot is started either from IDE or from command line.
     *
     * @param args Arguments from the command line.
     */
    @SuppressWarnings({"rawtypes"})
    public static void main(String[] args) throws PogamutException {
        new UT2004BotRunner(      // class that wrapps logic for bots executions, suitable to run single bot in single JVM
                WolfBot.class,  // which UT2004BotController it should instantiate
                "WolfBot"       // what name the runner should be using
        ).setMain(true)           // tells runner that is is executed inside MAIN method, thus it may block the thread and watch whether agent/s are correctly executed
         .startAgents(2);         // tells the runner to start 1 agent
    }
}
