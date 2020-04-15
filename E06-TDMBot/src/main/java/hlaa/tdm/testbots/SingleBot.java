package hlaa.tdm.testbots;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GlobalChat;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ItemPickedUp;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.MainDecisions;
import hlaa.tdm.behavior.*;
import hlaa.tdm.messages.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * TDM BOT TEMPLATE CLASS
 * Version: 0.0.1
 */
@AgentScoped
public class SingleBot extends UT2004BotTCController<UT2004Bot> {

	private static AtomicInteger BOT_COUNT = new AtomicInteger(0);

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
    	levelGeometryModule.setAutoLoad(true);
    	log.setLevel(Level.ALL);
    }


	@Override
    public Initialize getInitializeCommand() {
    	// IT IS FORBIDDEN BY COMPETITION RULES TO CHANGE DESIRED SKILL TO DIFFERENT NUMBER THAN 6
    	// IT IS FORBIDDEN BY COMPETITION RULES TO ALTER ANYTHING EXCEPT NAME & SKIN VIA INITIALIZE COMMAND
		// Change the name of your bot, e.g., Jakub Gemrot would rewrite this to: targetName = "JakubGemrot"
		int botInstance = BOT_COUNT.getAndIncrement();
		String targetName = SingleBot.class.getSimpleName() + botInstance;
		int targetTeam = botInstance % 2 == 0 ? AgentInfo.TEAM_RED : AgentInfo.TEAM_BLUE;
		String skin = targetTeam == AgentInfo.TEAM_RED ? UT2004Skins.SKINS[0] : UT2004Skins.SKINS[1];
        return new Initialize().setName(targetName)
							   .setSkin(skin)
							   .setTeam(targetTeam)
							   .setDesiredSkill(6);
    }




    // ==============
    // MAIN BOT LOGIC
    // ==============
    
    /**
     * Method that is executed only once before the first {@link SingleBot#logic()}
     */
    @SuppressWarnings("unused")
	@Override
    public void beforeFirstLogic() {
		_knowledge = new KnowledgeBase(this);
		_behavior = new BehaviorManager(this);
		_behavior
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


	@EventListener(eventClass = BotKilled.class)
	public void botKilled(BotKilled event){
    	if(_knowledge != null)
    		_knowledge.reset();
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
    	new UT2004BotRunner(SingleBot.class, SingleBot.class.getSimpleName())
				.setMain(true)
				.setLogLevel(Level.WARNING)
				.startAgents(2);
    }
    
}
