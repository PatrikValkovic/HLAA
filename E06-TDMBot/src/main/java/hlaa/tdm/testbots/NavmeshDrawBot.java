package hlaa.tdm.testbots;

import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.behavior.BehaviorManager;
import hlaa.tdm.behavior.LookAroundBehavior;
import hlaa.tdm.behavior.PickingBehavior;
import hlaa.tdm.utils.MapTweaks;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * TDM BOT TEMPLATE CLASS
 * Version: 0.0.1
 */
@AgentScoped
public class NavmeshDrawBot extends UT2004BotTCController<UT2004Bot> {

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
    }


	@Override
    public Initialize getInitializeCommand() {
		String targetName = NavmeshDrawBot.class.getSimpleName();
        return new Initialize().setName(targetName)
							   .setSkin(UT2004Skins.SKINS[0])
							   .setTeam(0)
							   .setDesiredSkill(6);
    }

	@Override
	public void mapInfoObtained() {
		MapTweaks.tweak(navBuilder);
		navMeshModule.setReloadNavMesh(true);
	}

	// ==============
    // MAIN BOT LOGIC
    // ==============
    
    /**
     * Method that is executed only once before the first {@link NavmeshDrawBot#logic()}
     */
    @SuppressWarnings("unused")
	@Override
    public void beforeFirstLogic() {
		navMeshModule.getNavMeshDraw().draw(true, true);
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
    	new UT2004BotRunner(NavmeshDrawBot.class, NavmeshDrawBot.class.getSimpleName())
				.setMain(true)
				.setLogLevel(Level.INFO)
				.startAgents(1);
    }
    
}
