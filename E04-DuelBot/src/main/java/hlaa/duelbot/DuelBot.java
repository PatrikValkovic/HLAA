package hlaa.duelbot;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.*;
import hlaa.duelbot.behavior.*;
import hlaa.duelbot.utils.LogitMeasure;
import java.util.logging.Level;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;

@AgentScoped
public class DuelBot extends UT2004BotModuleController {

    private BehaviorManager _behaviorManager;
    private KnowledgeBase _knowledge;
    private LogitMeasure _measure = new LogitMeasure();

    /**
     * Here we can modify initializing command for our bot, e.g., sets its name or skin.
     *
     * @return instance of {@link Initialize}
     */
    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName("DuelBot" + (this.hashCode() % 32)).setSkin(UT2004Skins.getRandomSkin()).setDesiredSkill(6);
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        bot.getLogger().getCategory("Yylex").setLevel(Level.OFF);
        _knowledge = new KnowledgeBase(this);
        ReflexBehavior reflex = new ReflexBehavior(this, 1000.0);
        reflex.addReflex(new DodgeReflex(this))
              .addReflex(new LookBehindReflex(this))
              .addReflex(new RocketAvoidanceReflex(this));
        _behaviorManager = new BehaviorManager(log);
        _behaviorManager
                .addBehavior(
                        new ReflexBehavior(this, 1000.0)
                                .addReflex(new DodgeReflex(this))
                                .addReflex(new LookBehindReflex(this))
                                .addReflex(new NearItemPickupReflex(this))
                                .addReflex(new RocketAvoidanceReflex(this))
                ).addProvider(
                        new MainDecisions(this, _knowledge)
                ).addBehavior(
                    new LookAroundBehavior(this, -10)
                );
    }

    @Override
    public void beforeFirstLogic() {
    }

    // ====================
    // BOT MIND MAIN METHOD
    // ====================

    @Override
    public void logic() throws PogamutException {
        // FOLLOWS THE BOT'S LOGIC
        _measure.start();
        _knowledge.updateKnowledge();
        _behaviorManager.execute();
        _measure.end(true, 100, log);
    }

    // ==============
    // EVENT HANDLERS
    // ==============

    /**
     * YOUR bot has just been killed.
     */
    @Override
    public void botKilled(BotKilled event) {
        log.info("I was KILLED!");
        navigation.stopNavigation();
        shoot.stopShooting();
        // RESET YOUR MEMORY VARIABLES HERE
    }

    /**
     * Some other BOT has just been damaged by someone (may be even by you).
     * @param event
     */
    @EventListener(eventClass = PlayerDamaged.class)
    public void playerDamaged(PlayerDamaged event) {
        if (event.getId().equals(this.getInfo().getId())) {
            this.getLog().warning("I have been damaged");
        }
    }

    /**
     * Some other BOT has just been killed by someone (may be even by you).
     * @param event
     */
    @EventListener(eventClass = PlayerKilled.class)
    public void playerKilled(PlayerKilled event) {
        if(event.getId() != info.getId()){
            _knowledge.resetProbabilities();
        }
    }

    /**
     * Somebody send a message
     * @param event
     */
    @EventListener(eventClass = GlobalChat.class)
    public void globalChat(GlobalChat event) {
        if(event.getText().equals("reset"))
            this.getBot().respawn();
    }

    // ===========
    // MAIN METHOD
    // ===========

    public static void main(String args[]) throws PogamutException {
        new UT2004BotRunner(     // class that wrapps logic for bots executions, suitable to run single bot in single JVM
                DuelBot.class,   // which UT2004BotController it should instantiate
                "DuelBot"        // what name the runner should be using
        ).setMain(true)          // tells runner that is is executed inside MAIN method, thus it may block the thread and watch whether agent/s are correctly executed
         .startAgents(2);        // tells the runner to start 2 agents
    }
}
