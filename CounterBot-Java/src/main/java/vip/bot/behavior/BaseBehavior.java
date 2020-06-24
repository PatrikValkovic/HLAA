package vip.bot.behavior;

import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import vip.bot.KnowledgeBase;

public abstract class BaseBehavior implements IBehavior {

    protected final UT2004BotVIPController _bot;
    protected final KnowledgeBase _knowledge;
    protected double _priority;

    public BaseBehavior(UT2004BotVIPController bot) {
        this(bot, 0.0);
    }
    public BaseBehavior(UT2004BotVIPController bot, double priority) {
        this(bot, priority, null);
    }
    public BaseBehavior(UT2004BotVIPController bot, KnowledgeBase knowledge) {
        this(bot, 0.0, knowledge);
    }
    public BaseBehavior(UT2004BotVIPController bot, double priority, KnowledgeBase knowledge){
        _bot = bot;
        _priority = priority;
        _knowledge = knowledge;
    }

    @Override
    public double priority() {
        return _priority;
    }
}
