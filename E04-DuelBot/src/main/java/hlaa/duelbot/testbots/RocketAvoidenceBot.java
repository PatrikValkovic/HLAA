package hlaa.duelbot.testbots;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.*;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import hlaa.duelbot.behavior.BehaviorManager;
import hlaa.duelbot.behavior.ReflexBehavior;
import hlaa.duelbot.behavior.RocketAvoidanceReflex;
import java.util.logging.Level;

@AgentScoped
public class RocketAvoidenceBot extends UT2004BotModuleController {

    private BehaviorManager _behaviorManager;

    /**
     * Here we can modify initializing command for our bot, e.g., sets its name or skin.
     *
     * @return instance of {@link Initialize}
     */
    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName(this.getClass().getSimpleName()).setSkin(UT2004Skins.getRandomSkin()).setDesiredSkill(6);
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        bot.getLogger().getCategory("Yylex").setLevel(Level.OFF);

        this._behaviorManager = new BehaviorManager(log);
        this._behaviorManager.addBehavior(
                new ReflexBehavior(this, 100.0)
                        .addReflex(new RocketAvoidanceReflex(this))
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
        this._behaviorManager.execute();
    }

    // ==============
    // EVENT HANDLERS
    // ==============

    @ObjectClassEventListener(eventClass = WorldObjectUpdatedEvent.class, objectClass = IncomingProjectile.class)
    public void incomingProjectile(WorldObjectUpdatedEvent<IncomingProjectile> event) {
        //event.getObject().getDirection();
        //event.getObject().getSpeed();
    }

    // ===========
    // MAIN METHOD
    // ===========

    public static void main(String args[]) throws PogamutException {
        new UT2004BotRunner(     // class that wrapps logic for bots executions, suitable to run single bot in single JVM
                RocketAvoidenceBot.class,   // which UT2004BotController it should instantiate
                (new Object() {
                }).getClass().getEnclosingClass().getSimpleName()        // what name the runner should be using
        ).setMain(true)          // tells runner that is is executed inside MAIN method, thus it may block the thread and watch whether agent/s are correctly executed
         .startAgents(1);        // tells the runner to start 2 agents
    }
}
