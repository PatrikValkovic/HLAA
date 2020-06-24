package vip.bot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import vip.bot.KnowledgeBase;

public class PairBehavior extends BaseBehavior {

    public PairBehavior(UT2004BotVIPController bot) {
        super(bot);
    }
    public PairBehavior(UT2004BotVIPController bot, double priority) {
        super(bot, priority);
    }
    public PairBehavior(UT2004BotVIPController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public PairBehavior(UT2004BotVIPController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    @Override
    public boolean isFiring() {
        return true;
    }

    @Override
    public void execute() {
        Location closest = _knowledge.getAlliesPositionsKnowledge().closestAlly(_bot.getInfo());

        _bot.getNMNav().navigate(closest);
    }

    @Override
    public void terminate() {
        _bot.getNMNav().stopNavigation();
    }
}
