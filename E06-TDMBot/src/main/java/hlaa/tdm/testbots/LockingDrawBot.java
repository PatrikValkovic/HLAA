package hlaa.tdm.testbots;

import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.behavior.BehaviorManager;
import hlaa.tdm.behavior.LockingBehavior;
import hlaa.tdm.utils.DrawingColors;
import hlaa.tdm.utils.LockLocations;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * TDM BOT TEMPLATE CLASS
 * Version: 0.0.1
 */
@AgentScoped
public class LockingDrawBot extends UT2004BotTCController<UT2004Bot> {

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
    	levelGeometryModule.setAutoLoad(false);
    	log.setLevel(Level.ALL);
    }


	@Override
    public Initialize getInitializeCommand() {
    	// IT IS FORBIDDEN BY COMPETITION RULES TO CHANGE DESIRED SKILL TO DIFFERENT NUMBER THAN 6
    	// IT IS FORBIDDEN BY COMPETITION RULES TO ALTER ANYTHING EXCEPT NAME & SKIN VIA INITIALIZE COMMAND
		// Change the name of your bot, e.g., Jakub Gemrot would rewrite this to: targetName = "JakubGemrot"
		int botInstance = BOT_COUNT.getAndIncrement();
		String targetName = LockingDrawBot.class.getSimpleName() + botInstance;
		int targetTeam = AgentInfo.TEAM_RED;
        return new Initialize().setName(targetName)
							   .setSkin(UT2004Skins.SKINS[0])
							   .setTeam(targetTeam)
							   .setDesiredSkill(6);
    }




    // ==============
    // MAIN BOT LOGIC
    // ==============

    /**
     * Method that is executed only once before the first {@link LockingDrawBot#logic()}
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

			//getDraw().clearAll();
			for (List<Location> location : LockLocations.getLockLocations(getGame())) {
				for(int i=0;i<location.size();i++){
					List<ILocated> l = getNMNav().getPathPlanner()
												 .computePath(location.get(i), location.get((i + 1) % location.size()))
												 .get();
					for(int j=1;j<l.size();j++)
						getDraw().drawLine(DrawingColors.LOCK_LOCATION, l.get(j-1), l.get(j));
				}
			}
		}
		catch (Exception e) {
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
    	new UT2004BotRunner(LockingDrawBot.class, LockingDrawBot.class.getSimpleName())
				.setMain(true)
				.setLogLevel(Level.WARNING)
				.startAgents(1);
    }

}
