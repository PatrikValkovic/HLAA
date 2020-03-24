package hlaa.duelbot;

import java.util.logging.Level;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ItemPickedUp;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;

@AgentScoped
public class DuelBot extends UT2004BotModuleController {

	private long   lastLogicTime        = -1;
    private long   logicIterationNumber = 0;    

    /**
     * Here we can modify initializing command for our bot, e.g., sets its name or skin.
     *
     * @return instance of {@link Initialize}
     */
    @Override
    public Initialize getInitializeCommand() {  
    	return new Initialize().setName("DuelBot").setSkin(UT2004Skins.getRandomSkin()).setDesiredSkill(6);
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
    	bot.getLogger().getCategory("Yylex").setLevel(Level.OFF);
    }

    @Override
    public void botFirstSpawn(GameInfo gameInfo, ConfigChange config, InitedMessage init, Self self) {
        navigation.addStrongNavigationListener(new FlagListener<NavigationState>() {
			@Override
			public void flagChanged(NavigationState changedValue) {
				navigationStateChanged(changedValue);
			}
        });
    }

    /**
     * The navigation state has changed...
     * @param changedValue
     */
    private void navigationStateChanged(NavigationState changedValue) {
    	switch(changedValue) {
    	case TARGET_REACHED:
    		return;
		case PATH_COMPUTATION_FAILED:
			return;
		case STUCK:
			return;
		}
    }
    
    @Override
    public void beforeFirstLogic() {
    }
    
    // ====================
    // BOT MIND MAIN METHOD
    // ====================
        
    @Override
    public void logic() throws PogamutException {
    	if (lastLogicTime < 0) {
    		lastLogicTime = System.currentTimeMillis();
    		return;
    	}

    	log.info("---LOGIC: " + (++logicIterationNumber) + " / D=" + (System.currentTimeMillis() - lastLogicTime) + "ms ---");
    	lastLogicTime = System.currentTimeMillis();

    	// FOLLOWS THE BOT'S LOGIC
    	
    	// use Bot Name to visualize high-level state of your bot to ease debugging
    	setDebugInfo("BRAIN-DEAD");
    	
    }
    
    // ==============
    // EVENT HANDLERS
    // ==============
    
    /**
     * You have just picked up some item.
     * @param event
     */
    @EventListener(eventClass=ItemPickedUp.class)
    public void itemPickedUp(ItemPickedUp event) {
    	if (info.getSelf() == null) return; // ignore the first equipment...
    	Item pickedUp = items.getItem(event.getId());
    	if (pickedUp == null) return; // ignore unknown items
    }
    
    /**
     * YOUR bot has just been damaged.
     * @param event
     */
    @EventListener(eventClass=BotDamaged.class)
    public void botDamaged(BotDamaged event) {
    }

    /**
     * YOUR bot has just been killed. 
     */
    @Override
    public void botKilled(BotKilled event) {
        sayGlobal("I was KILLED!");
        
        navigation.stopNavigation();
        shoot.stopShooting();
        
        // RESET YOUR MEMORY VARIABLES HERE
    }
    
    /**
     * Some other BOT has just been damaged by someone (may be even by you).
     * @param event
     */
    @EventListener(eventClass=PlayerDamaged.class)
    public void playerDamaged(PlayerDamaged event) {
    }
    
    /**
     * Some other BOT has just been killed by someone (may be even by you).
     * @param event
     */
    @EventListener(eventClass=PlayerKilled.class)
    public void playerKilled(PlayerKilled event) {    	
    }
    
    @ObjectClassEventListener(eventClass=WorldObjectUpdatedEvent.class, objectClass=IncomingProjectile.class)
    public void incomingProjectile(WorldObjectUpdatedEvent<IncomingProjectile> event) {
    	  //event.getObject().getDirection();
    	  //event.getObject().getSpeed();
    }

    // =========
    // UTILITIES
    // =========

    private void setDebugInfo(String info) {
    	bot.getBotName().setInfo(info);
    	log.info(info);
    }

    private void setDebugValue(String tag, String value) {
    	bot.getBotName().setInfo(tag, value);
    	log.info(tag + ": " + value);
    }

    private void sayGlobal(String msg) {
    	// Simple way to send msg into the UT2004 chat
    	body.getCommunication().sendGlobalTextMessage(msg);
    	// And user log as well
    	log.info(msg);
    }
    
    // ===========
    // MAIN METHOD
    // ===========
    
    public static void main(String args[]) throws PogamutException {
        new UT2004BotRunner(     // class that wrapps logic for bots executions, suitable to run single bot in single JVM
                DuelBot.class,   // which UT2004BotController it should instantiate
                "DuelBot"        // what name the runner should be using
        ).setMain(true)          // tells runner that is is executed inside MAIN method, thus it may block the thread and watch whether agent/s are correctly executed
         .startAgents(1);        // tells the runner to start 2 agents
    }
}
