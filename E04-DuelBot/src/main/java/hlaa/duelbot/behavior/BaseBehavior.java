package hlaa.duelbot.behavior;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;

public abstract class BaseBehavior implements IBehavior {

    protected UT2004BotModuleController _bot;
    protected double _priority;

    public BaseBehavior(UT2004BotModuleController bot) {
        this(bot, 0.0);
    }
    public BaseBehavior(UT2004BotModuleController bot, double priority){
        _bot = bot;
        _priority = priority;
    }

    @Override
    public double priority() {
        return _priority;
    }
}
