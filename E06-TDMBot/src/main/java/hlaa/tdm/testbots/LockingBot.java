package hlaa.tdm.testbots;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GlobalChat;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.behavior.BehaviorManager;
import hlaa.tdm.behavior.LockingBehavior;
import hlaa.tdm.messages.TCAllyLocation;
import hlaa.tdm.messages.TCWantToLock;
import hlaa.tdm.utils.MapTweaks;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import static hlaa.tdm.TDMBot.UPDATE_NAVMESH;

/**
 * TDM BOT TEMPLATE CLASS
 * Version: 0.0.1
 */
@AgentScoped
public class LockingBot extends UT2004BotTCController<UT2004Bot> {

	private static AtomicInteger BOT_COUNT = new AtomicInteger(0);
	private int botInstance;

	private KnowledgeBase _knowledge;
	private BehaviorManager _behavior;

	// =============
    // BOT LIFECYCLE
    // =============

    /**
     * Bot's preparation - called before the bot is connected to GB2004 and launched into UT2004.
     */
    @Override
    public void prepareBot(UT2004Bot bot) {
    }
    
    @Override
    protected void initializeModules(UT2004Bot bot) {
    	super.initializeModules(bot);
    	levelGeometryModule.setAutoLoad(false);
    	log.setLevel(Level.ALL);
    }


	@Override
    public Initialize getInitializeCommand() {
    	// IT IS FORBIDDEN BY COMPETITION RULES TO CHANGE DESIRED SKILL TO DIFFERENT NUMBER THAN 6
    	// IT IS FORBIDDEN BY COMPETITION RULES TO ALTER ANYTHING EXCEPT NAME & SKIN VIA INITIALIZE COMMAND
		// Change the name of your bot, e.g., Jakub Gemrot would rewrite this to: targetName = "JakubGemrot"
		botInstance = BOT_COUNT.getAndIncrement();
		String targetName = LockingBot.class.getSimpleName() + botInstance;
		int targetTeam = AgentInfo.TEAM_RED;
        return new Initialize().setName(targetName)
							   .setSkin(UT2004Skins.SKINS[0])
							   .setTeam(targetTeam)
							   .setDesiredSkill(6);
    }

	/**
	 * This is a place where you should use map tweaks, i.e., patch original Navigation Graph that comes from UT2004.
	 */
	@Override
	public void mapInfoObtained() {
		MapTweaks.tweak(navBuilder);
		if (botInstance == 0) navMeshModule.setReloadNavMesh(UPDATE_NAVMESH);
	}



    // ==============
    // MAIN BOT LOGIC
    // ==============
    
    /**
     * Method that is executed only once before the first {@link LockingBot#logic()}
     */
    @SuppressWarnings("unused")
	@Override
    public void beforeFirstLogic() {
		_knowledge = new KnowledgeBase(this);
		_behavior = new BehaviorManager(this)
				.addBehavior(new LockingBehavior(this,  _knowledge));
    }

    /**
     * Main method that controls the bot - makes decisions what to do next. It
     * is called iteratively by Pogamut engine every time a synchronous batch
     * from the environment is received. This is usually 4 times per second.
     * 
     * This is a typical place from where you start coding your bot. Even though bot
     * can be completely EVENT-DRIVEN, the reactive aproach via "ticking" logic()
     * method is more simple / straight-forward.
     */
    @Override
    public void logic() {

    	try {
			_knowledge.updateKnowledge();
			_behavior.execute();
		}
    	catch(Exception e){
    		e.printStackTrace();
		}

    }

	// ======
	// EVENTS
	// ======
	@EventListener(eventClass = GlobalChat.class)
	public void chatReceived(GlobalChat msg) {
    	if(msg.getText().equals("reset"))
    		body.getAction().respawn();
	}

	@EventListener(eventClass = BotKilled.class)
	public void botKilled(BotKilled event){
    	if(_knowledge != null)
    		_knowledge.reset();
	}

	@EventListener(eventClass = TCAllyLocation.class)
	public void allyLocation(TCAllyLocation event){
		if(_knowledge != null)
			_knowledge.getAlliesPositionsKnowledge().setPosition(event.getPlayerId(), event.getLocation());
	}

	@EventListener(eventClass = TCWantToLock.class)
	public void wantToLock(TCWantToLock event){
		if(_knowledge != null)
			_knowledge.getLockingKnowledge().wantToLock(event.getPlayerId(), event.getRegion());
	}



	// ===========
    // MAIN METHOD
    // ===========
    
    /**
     * Main execute method of the program.
     * 
     * @param args
     * @throws PogamutException
     */
    public static void main(String args[]) throws PogamutException {
    	// Starts N agents of the same type at once
    	// WHEN YOU WILL BE SUBMITTING YOUR CODE, MAKE SURE THAT YOU RESET NUMBER OF STARTED AGENTS TO '1' !!!
    	// => during the development, please use {@link Starter_Bots} instead to ensure you will leave "1" in here
    	new UT2004BotRunner(LockingBot.class, LockingBot.class.getSimpleName())
				.setMain(true)
				.setLogLevel(Level.WARNING)
				.startAgents(4);
    }
    
}
