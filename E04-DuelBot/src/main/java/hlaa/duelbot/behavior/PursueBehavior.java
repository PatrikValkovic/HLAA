package hlaa.duelbot.behavior;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import hlaa.duelbot.KnowledgeBase;

public class PursueBehavior extends BaseBehavior {

    public PursueBehavior(UT2004BotModuleController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public PursueBehavior(UT2004BotModuleController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    @Override
    public boolean isFiring() {
        return true;
    }

    @Override
    public void execute() {
        NavPoint p = _knowledge.getPointWithMaxProb();
        //System.out.println("Bot location " + _bot.getInfo().getLocation());
        _bot.getLog().info("Pursue to point " + p.getLocation());
        _bot.getNMNav().navigate(p);
    }

    @Override
    public double priority() {
        return 10.0;
    }
}
