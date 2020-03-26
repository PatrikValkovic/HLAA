package hlaa.duelbot.testbots;

import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import hlaa.duelbot.KnowledgeBase;
import hlaa.duelbot.behavior.BehaviorManager;
import hlaa.duelbot.behavior.MedkitBehavior;
import java.awt.*;
import java.util.logging.Level;

@AgentScoped
public class DrawNavmeshBot extends UT2004BotModuleController {

    private BehaviorManager _behavior;
    private KnowledgeBase _knowledge;

    /**
     * Here we can modify initializing command for our bot, e.g., sets its name or skin.
     *
     * @return instance of {@link Initialize}
     */
    @Override
    public Initialize getInitializeCommand() {  
    	return new Initialize().setName(this.getClass().getSimpleName()).setSkin("Ophelia").setDesiredSkill(6);
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
    	bot.getLogger().getCategory("Yylex").setLevel(Level.OFF);

    	_knowledge = new KnowledgeBase(this);
    	_behavior = new BehaviorManager();
    }
    
    @Override
    public void beforeFirstLogic() {
        getNavMesh().getPolygons()
                    .forEach(polygon -> {
                        polygon.getEdges().forEach(edge -> {
                            draw.drawLine(Color.WHITE, edge.getSource().getLocation(), edge.getDestination().getLocation());
                        });
                    });
    }
    
    // ====================
    // BOT MIND MAIN METHOD
    // ====================
        
    @Override
    public void logic() throws PogamutException {
    	// FOLLOWS THE BOT'S LOGIC
        _knowledge.updateKnowledge();

        _behavior.execute();

    }
    
    // ===========
    // MAIN METHOD
    // ===========
    
    public static void main(String args[]) throws PogamutException {
        new UT2004BotRunner(     // class that wrapps logic for bots executions, suitable to run single bot in single JVM
                DrawNavmeshBot.class,   // which UT2004BotController it should instantiate
                (new Object(){}).getClass().getEnclosingClass().getSimpleName()        // what name the runner should be using
        ).setMain(true)          // tells runner that is is executed inside MAIN method, thus it may block the thread and watch whether agent/s are correctly executed
         .startAgents(1);        // tells the runner to start 2 agents
    }
}
