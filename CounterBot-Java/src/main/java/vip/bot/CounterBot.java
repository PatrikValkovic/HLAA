package vip.bot;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.*;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import cz.cuni.amis.pogamut.ut2004.vip.protocol.CSBotTeam;
import cz.cuni.amis.pogamut.ut2004.vip.protocol.messages.CSRoundStart;
import cz.cuni.amis.utils.exception.PogamutException;
import java.util.logging.Level;
import vip.bot.behavior.*;
import vip.bot.messages.*;
import vip.tc.msgs.TCPlayerUpdate;
import vip.tc.msgs.TCRunningTo;

public class CounterBot extends UT2004BotVIPController<UT2004Bot> {

	private static int INSTANCE = 0;

	private KnowledgeBase _knowledge;
	private BehaviorManager _behavior;
	
    /**
     * Initialize command of the bot, called during initial handshake, init can
     * set things like name of bot, its skin, skill, team ect.
     *
     * @see Initialize
     * @return
     */
    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName("Neo-" + (++INSTANCE))
							   .setDesiredSkill(6)
							   .setTeam(CSBotTeam.COUNTER_TERRORIST.ut2004Team)
							   .setSkin("neo");
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        super.botInitialized(gameInfo, currentConfig, init);
        log.setLevel(Level.INFO);
        bot.getLogger().getCategory("Yylex").setLevel(Level.OFF);
        
        // INITIALIZE CUSTOM MODULES
 		//tabooItems = new TabooSet<Item>(bot);

 		/*this.getNavigation().addStrongNavigationListener(
 				new FlagListener<NavigationState>() {
 					@Override
 					public void flagChanged(NavigationState changedValue) {
 						switch (changedValue) {
 						case PATH_COMPUTATION_FAILED:
 						case STUCK:
 							if (targetItem != null)
 								tabooItems.add(targetItem, 30);
 							break;
 						case TARGET_REACHED:
 							if (targetItem != null)
 								tabooItems.add(targetItem, 5);
 							break;
 						}
 					}
 				});
 		// TODO handle later
 		*/
    }

    // region events
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
		this.getMove().turnTo(event.getLocation());
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

	@EventListener(eventClass=CSRoundStart.class)
	public void roundStart(CSRoundStart event) {
    	_knowledge.reset();
	}
	
	@EventListener(eventClass=TCRunningTo.class)
	public void vipRunningTo(TCRunningTo event) {
		if (event.id.equals(getVip().getVIPId())) {
			_knowledge.getVipKnowledge().setNextLocation(event.location);
		}
	}
	@EventListener(eventClass= TCPlayerUpdate.class)
	public void vipStatus(TCPlayerUpdate event) {
		if (event.id.equals(getVip().getVIPId())) {
			_knowledge.getVipKnowledge().updatePlayer(event);
		}
	}
	@EventListener(eventClass= TCGuarding.class)
	public void guardingUpdate(TCGuarding event) {
		_knowledge.getGuardKnowledge().update_guarding(event);
	}
	//endregion


    
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

	/**
	 * Method that is executed only once before the first {@link CounterBot#logic()}
	 */
	@SuppressWarnings("unused")
	@Override
	public void beforeFirstLogic() {
		_knowledge = new KnowledgeBase(this, getTCClient());
		_behavior = new BehaviorManager(this);
		_behavior
				.addBehavior(new ReflexBehavior(this, 100)
					.addReflex(new LookBehindReflex(this))
					.addReflex(new NearItemPickupReflex(this))
					.addReflex(new RocketAvoidanceReflex(this))
				)
				.addProvider(
					new MainDecisions(this, _knowledge)
				);
	}


    public static void main(String[] args) throws PogamutException {
        new UT2004BotRunner(CounterBot.class, "CTBot")
				.setMain(true)
				.setLogLevel(Level.WARNING)
				.startAgents(1);
    }
    
}
