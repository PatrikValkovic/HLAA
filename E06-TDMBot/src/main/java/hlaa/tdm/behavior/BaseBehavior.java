package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import hlaa.tdm.KnowledgeBase;

public abstract class BaseBehavior implements IBehavior {

    protected final UT2004BotModuleController _bot;
    protected final KnowledgeBase _knowledge;
    protected double _priority;

    public BaseBehavior(UT2004BotModuleController bot) {
        this(bot, 0.0);
    }
    public BaseBehavior(UT2004BotModuleController bot, double priority) {
        this(bot, priority, null);
    }
    public BaseBehavior(UT2004BotModuleController bot, KnowledgeBase knowledge) {
        this(bot, 0.0, knowledge);
    }
    public BaseBehavior(UT2004BotModuleController bot, double priority, KnowledgeBase knowledge){
        _bot = bot;
        _priority = priority;
        _knowledge = knowledge;
    }

    @Override
    public double priority() {
        return _priority;
    }
}
