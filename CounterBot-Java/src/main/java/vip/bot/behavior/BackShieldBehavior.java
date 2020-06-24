package vip.bot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import vip.bot.KnowledgeBase;
import vip.bot.utils.Navigation;

public class BackShieldBehavior extends BaseBehavior {

    public BackShieldBehavior(UT2004BotVIPController bot) {
        super(bot);
    }
    public BackShieldBehavior(UT2004BotVIPController bot, double priority) {
        super(bot, priority);
    }
    public BackShieldBehavior(UT2004BotVIPController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public BackShieldBehavior(UT2004BotVIPController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    @Override
    public boolean isFiring() {
        return true;
    }

    @Override
    public void terminate() {
        _knowledge.getGuardKnowledge().stop_guarding(_bot.getInfo().getId(), true);
    }

    @Override
    public void execute() {
        _knowledge.getGuardKnowledge().start_guarding(_bot.getInfo().getId(), true);

        Location guard_location = _knowledge.getVipKnowledge().getLocation();
        Location focus = _knowledge.getVipKnowledge().getLocation().sub(_knowledge.getVipKnowledge().getDirection());

        _bot.getNMNav().navigate(guard_location);
        _bot.getNMNav().setFocus(focus);
    }
}
