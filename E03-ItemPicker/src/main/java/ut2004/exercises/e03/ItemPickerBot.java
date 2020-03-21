package ut2004.exercises.e03;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GlobalChat;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ItemPickedUp;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import ut2004.exercises.e03.comm.TCItemPicked;
import ut2004.exercises.e03.comm.TCPursuit;

/**
 * EXERCISE 03
 * -----------
 * <p>
 * Your task is to pick all interesting items.
 * <p>
 * Interesting items are:
 * -- weapons
 * -- shields
 * -- armors
 * <p>
 * Target maps, where to test your squad are:
 * -- DM-1on1-Albatross
 * -- DM-1on1-Roughinery-FPS
 * -- DM-Rankin-FE
 * <p>
 * To start scenario:
 * 1. start either of startGamebotsTDMServer-DM-1on1-Albatross.bat, startGamebotsTDMServer-DM-1on1-Roughinery-FPS.bat, startGamebotsTDMServer-DM-Rankin-FE.bat
 * 2. start team communication view running {@link TCServerStarter#main(String[])}.
 * 3. start your squad
 * 4. use ItemPickerChecker methods to query the state of your run
 * <p>
 * Behavior tips:
 * 1. be sure not to repick item you have already picked
 * 2. be sure not to repick item some other bot has already picked (use {@link #tcClient} for that)
 * 3. do not try to pick items you are unable to, check by {@link Items#isPickable(Item)}
 * 4. be sure not to start before {@link ItemPickerChecker#isRunning()}
 * 5. you may terminate your bot as soon as {@link ItemPickerChecker#isVictory()}.
 * <p>
 * WATCH OUT!
 * 1. All your bots can be run from the same JVM (for debugging purposes), but they must not communicate via STATICs!
 * 2. If you want to test your bots in separate JVMs, switch {@link #RUN_STANDALONE} to TRUE and start this file 3x.
 *
 * @author Jakub Gemrot aka Jimmy aka Kefik
 */
@AgentScoped
public class ItemPickerBot extends UT2004BotTCController {

    private static final AtomicInteger INSTANCE = new AtomicInteger(1);
    private static final Set<ItemType.Category> INTERESTED = new HashSet<ItemType.Category>() {{
        add(ItemType.Category.ARMOR);
        add(ItemType.Category.WEAPON);
        add(ItemType.Category.SHIELD);
    }};
    private static final double TIMEOUT_CONSTANT = 1.3;

    private TabooSet<UnrealId> _picked;
    private int _instance;
    private Map<UnrealId, Double> _otherPursuing = new HashMap<>(6, 0.6f);

    /**
     * Here we can modify initializing command for our bot, e.g., sets its name or skin.
     *
     * @return instance of {@link Initialize}
     */
    @Override
    public Initialize getInitializeCommand() {
        _instance = INSTANCE.getAndIncrement();
        return new Initialize().setName("PickerBot-" + _instance).setSkin(UT2004Skins.getSkin());
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
    }

    /**
     * This method is called only once, right before actual logic() method is called for the first time.
     * At this point you have {@link Self} i.e., this.info fully initialized.
     */
    @Override
    public void beforeFirstLogic() {
        // REGISTER TO ITEM PICKER CHECKER
        try {
            Thread.sleep(200 * _instance);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        ItemPickerChecker.register(info.getId());
        _picked = new TabooSet<>(bot);
    }

    /**
     * THIS BOT has picked an item!
     *
     * @param event
     */
    @EventListener(eventClass = ItemPickedUp.class)
    public void itemPickedUp(ItemPickedUp event) {
        log.info("I picked " + event.getId());
        if (ItemPickerChecker.itemPicked(info.getId(), items.getItem(event.getId()))) {
            // AN ITEM HAD BEEN PICKED + ACKNOWLEDGED BY ItemPickerChecker
            tcClient.sendToAllOthers(new TCItemPicked(info.getId(), event.getId()));
            _picked.add(event.getId(), TIMEOUT_CONSTANT * items.getItemTimeToSpawn(items.getItem(event.getId())));
        } else {
            // should not happen... but if you encounter this, just wait with the bot a cycle and report item picked again
            // log.severe("SHOULD NOT BE HAPPENING! ItemPickerChecker refused our item!");
        }
    }

    /**
     * Someone else picked an item!
     *
     * @param event
     */
    @EventListener(eventClass = TCItemPicked.class)
    public void tcItemPicked(TCItemPicked event) {
        if (RUN_STANDALONE) {
            ItemPickerChecker.itemPicked(event.getWho(), items.getItem(event.getWhat()));
            _picked.add(event.getWhat(), TIMEOUT_CONSTANT * items.getItemTimeToSpawn(items.getItem(event.getWhat())));
        }
    }

    /**
     * Someone else is pursuing the item
     *
     * @param event
     */
    @EventListener(eventClass = TCPursuit.class)
    public void tcPursuit(TCPursuit event) {
        _otherPursuing.put(event.getWhat(), event.getDistance());
    }


    /**
     * Main method called 4 times / second. Do your action-selection here.
     */
    @Override
    public void logic() throws PogamutException {

        if (!tcClient.isConnected()) {
            log.warning("TeamComm not running!");
            return;
        }

        if (!ItemPickerChecker.isRunning()) {
            if (RUN_STANDALONE) {
                for (UnrealId connectedBot : tcClient.getConnectedAllBots()) {
                    ItemPickerChecker.register(connectedBot);
                }
            }
            return;
        }
        if (ItemPickerChecker.isVictory()) {
            log.info("I won");
            System.exit(0);
        }

        Map<Item, Double> itemsToPick
                = items.getSpawnedItems()
                       .values()
                       .stream()
                       .filter(i -> items.isPickable(i))
                       .filter(i -> INTERESTED.contains(i.getType().getCategory()))
                       .filter(i -> !_picked.contains(i.getId()))
                       .map(i -> new AbstractMap.SimpleEntry<>(
                               i,
                               navMeshModule.getAStarPathPlanner().getDistance(bot.getLocation(), i.getLocation())
                       ))
                       .filter(entry ->
                               !_otherPursuing.containsKey(entry.getKey().getId()) ||
                                       _otherPursuing.get(entry.getKey().getId()) > entry.getValue()
                       )
                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (itemsToPick.size() == 0) {
            navigation.stopNavigation();
        } else {
            Map.Entry<Item, Double> entry = itemsToPick.entrySet()
                       .stream()
                       .sorted(Comparator.comparingDouble(Map.Entry::getValue))
                       .findFirst()
                       .get();
            Item i = entry.getKey();
            double dist = entry.getValue();
            navigation.navigate(i.getLocation());
            tcClient.sendToAllOthers(new TCPursuit(i.getId(), dist));
            //log.info("Pursuing " + i.getId());
        }

    }

    /**
     * To run ItemPickerBots in standalone mode - switch this to TRUE and run this file 3 times.
     */
    public static boolean RUN_STANDALONE = false;

    /**
     * This method is called when the bot is started either from IDE or from command line.
     *
     * @param args
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String args[]) throws PogamutException {
        new UT2004BotRunner(      // class that wrapps logic for bots executions, suitable to run single bot in single JVM
                ItemPickerBot.class,  // which UT2004BotController it should instantiate
                "PickerBot"       // what name the runner should be using
        ).setMain(true)           // tells runner that is is executed inside MAIN method, thus it may block the thread and watch whether agent/s are correctly executed
         .startAgents(RUN_STANDALONE ? 1 : ItemPickerChecker.ITEM_PICKER_BOTS); // tells the runner to start N agent
    }
}
