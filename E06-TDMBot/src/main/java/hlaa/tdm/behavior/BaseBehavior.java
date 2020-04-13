package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import hlaa.tdm.KnowledgeBase;

public abstract class BaseBehavior implements IBehavior {

    protected final UT2004BotTCController _bot;
    protected final KnowledgeBase _knowledge;
    protected double _priority;

    public BaseBehavior(UT2004BotTCController bot) {
        this(bot, 0.0);
    }
    public BaseBehavior(UT2004BotTCController bot, double priority) {
        this(bot, priority, null);
    }
    public BaseBehavior(UT2004BotTCController bot, KnowledgeBase knowledge) {
        this(bot, 0.0, knowledge);
    }
    public BaseBehavior(UT2004BotTCController bot, double priority, KnowledgeBase knowledge){
        _bot = bot;
        _priority = priority;
        _knowledge = knowledge;
    }

    @Override
    public double priority() {
        return _priority;
    }
}
