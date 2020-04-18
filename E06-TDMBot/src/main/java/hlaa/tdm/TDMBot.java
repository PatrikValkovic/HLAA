package hlaa.tdm;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.*;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import hlaa.tdm.behavior.*;
import hlaa.tdm.messages.*;
import hlaa.tdm.utils.MapTweaks;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * TDM BOT TEMPLATE CLASS
 * Version: 0.0.1
 */
@AgentScoped
public class TDMBot extends UT2004BotTCController<UT2004Bot> {
	
	/**
	 * TRUE => rebinds NAVMESH+NAVIGATION GRAPH; useful when you add new map tweak into {@link MapTweaks}.
	 */
	public static final boolean UPDATE_NAVMESH = true;
	/**
	 * If true, all bots will enter RED team... 
	 */
	public static final boolean START_BOTS_IN_SINGLE_TEAM = true;
	/**
	 * How many bots we have started so far; used to split bots into teams.
	 */
	private static AtomicInteger BOT_COUNT = new AtomicInteger(0);
	/**
	 * How many bots have entered RED team.
	 */
	private static AtomicInteger BOT_COUNT_RED_TEAM = new AtomicInteger(0);
	/**
	 * How many bots have entered BLUE team.
	 */
	private static AtomicInteger BOT_COUNT_BLUE_TEAM = new AtomicInteger(0);
	/**
	 * 0-based; note that during the tournament all your bots will have botInstance == 0!
	 */
	private int botInstance = 0;



	private KnowledgeBase _knowledge;
	private BehaviorManager _behavior;


	
    // =============
    // BOT LIFECYCLE
    // =============

    /**
     * This is a place where you should use map tweaks, i.e., patch original Navigation Graph that comes from UT2004.
     */
    @Override
    public void mapInfoObtained() {
    	// See {@link MapTweaks} for details; add tweaks in there if required.
    	MapTweaks.tweak(navBuilder);    	
    	if (botInstance == 0) navMeshModule.setReloadNavMesh(UPDATE_NAVMESH);    	
    }

	@Override
    public Initialize getInitializeCommand() {
    	// IT IS FORBIDDEN BY COMPETITION RULES TO CHANGE DESIRED SKILL TO DIFFERENT NUMBER THAN 6
    	// IT IS FORBIDDEN BY COMPETITION RULES TO ALTER ANYTHING EXCEPT NAME & SKIN VIA INITIALIZE COMMAND
		// Change the name of your bot, e.g., Jakub Gemrot would rewrite this to: targetName = "JakubGemrot"
		String targetName = "PatrikValkovic";
		int botTeamInstance;
		botInstance = BOT_COUNT.getAndIncrement();
		
		int targetTeam = AgentInfo.TEAM_RED;
		if (!START_BOTS_IN_SINGLE_TEAM) {
			targetTeam = botInstance % 2 == 0 ? AgentInfo.TEAM_RED : AgentInfo.TEAM_BLUE;
		}
		switch (targetTeam) {
		case AgentInfo.TEAM_RED: 
			botTeamInstance = BOT_COUNT_RED_TEAM.getAndIncrement();  
			targetName += "-RED-" + botTeamInstance; 
			break;
		case AgentInfo.TEAM_BLUE: 
			botTeamInstance = BOT_COUNT_BLUE_TEAM.getAndIncrement(); 
			targetName += "-BLUE-" + botTeamInstance;
			break;
		}		
        return new Initialize().setName(targetName)
							   .setSkin(targetTeam == AgentInfo.TEAM_RED ? UT2004Skins.SKINS[0] : UT2004Skins.SKINS[UT2004Skins.SKINS.length-1])
							   .setTeam(targetTeam)
							   .setDesiredSkill(6);
    }

    /**
     * Bot has been initialized inside GameBots2004 (Unreal Tournament 2004) and is about to enter the play
     * (it does not have the body materialized yet).
     *  
     * @param gameInfo
     * @param currentConfig
     * @param init
     */
    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
    	// INITIALIZE TABOO SETS, if you have them, HERE
    }

    // ==========================
    // EVENT LISTENERS / HANDLERS
    // ==========================

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

	@EventListener(eventClass = ItemPickedUp.class)
	public void itemPickedUp(ItemPickedUp event){
		if(_knowledge != null)
			_knowledge.getItemSpawnedKnowledge().pickedUp(event.getId());
		getTCClient().sendToTeam(new TCDontSeeItem(event.getId()));
	}

	@EventListener(eventClass = TCSeeItem.class)
	public void seeItem(TCSeeItem event){
		if(_knowledge != null)
			_knowledge.getItemSpawnedKnowledge().seeItem(event.getItemId());
	}

	@EventListener(eventClass = TCDontSeeItem.class)
	public void dontSeeItem(TCDontSeeItem event){
		if(_knowledge != null)
			_knowledge.getItemSpawnedKnowledge().dontSeeItem(event.getItemId());
	}

	@EventListener(eventClass = TCGoingToPick.class)
	public void goingToPick(TCGoingToPick event){
		if(_knowledge != null)
			_knowledge.getOtherPickingKnowledge().otherGoingToPick(
					getItems().getItem(event.getItemId()),
					event.getDistance(),
					event.getBotId()
			);
	}

	@EventListener(eventClass = TCAllyLocation.class)
	public void allyLocation(TCAllyLocation event){
		if(_knowledge != null)
			_knowledge.getAlliesPositionsKnowledge().setPosition(event.getPlayerId(), event.getLocation());
	}

	@EventListener(eventClass = TCEnemyLocation.class)
	public void enemyLocation(TCEnemyLocation event){
		if(_knowledge != null)
			_knowledge.getEnemyPositionsKnowledge().updatePlayer(event.getPlayerId(), event.getLocation());
	}

	@EventListener(eventClass = TCWantToLock.class)
	public void wantToLock(TCWantToLock event){
		if(_knowledge != null)
			_knowledge.getLockingKnowledge().wantToLock(event.getPlayerId(), event.getRegion());
	}

	@EventListener(eventClass = TCSeeEnemy.class)
	public void allySeeEnemy(TCSeeEnemy event){
		if(_knowledge != null)
			_knowledge.getFirepowerConcentrationKnowledge().teamSeePlayer(event.getAllyId(), event.getEnemyId());
	}

	@EventListener(eventClass = TCDontSeeEnemy.class)
	public void allyDontSeeEnemy(TCDontSeeEnemy event){
		if(_knowledge != null)
			_knowledge.getFirepowerConcentrationKnowledge().teamDontSeePlayer(event.getAllyId(), event.getEnemyId());
	}

    // ==============
    // MAIN BOT LOGIC
    // ==============
    
    /**
     * Method that is executed only once before the first {@link TDMBot#logic()} 
     */
    @SuppressWarnings("unused")
	@Override
    public void beforeFirstLogic() {
		_knowledge = new KnowledgeBase(this);
		_behavior = new BehaviorManager(this);
		_behavior
				.addBehavior(
						new ReflexBehavior(this, 100.0)
								.addReflex(new DodgeReflex(this))
								.addReflex(new LookBehindReflex(this))
								.addReflex(new NearItemPickupReflex(this))
								.addReflex(new RocketAvoidanceReflex(this))
				).addProvider(
						new TeamMainDecisions(this, _knowledge)
		).addBehavior(
				new LookAroundBehavior(this, -20)
		);
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
    	new UT2004BotRunner(TDMBot.class, "TDMBot").setMain(true)
												   .setLogLevel(Level.WARNING)
												   .startAgents(1);
    }
    
}
