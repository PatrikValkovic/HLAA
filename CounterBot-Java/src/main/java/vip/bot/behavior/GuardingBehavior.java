package vip.bot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import cz.cuni.amis.utils.Cooldown;
import java.awt.*;
import vip.bot.KnowledgeBase;
import vip.bot.utils.Navigation;

public class GuardingBehavior extends BaseBehavior {

    public GuardingBehavior(UT2004BotVIPController bot) {
        super(bot);
    }
    public GuardingBehavior(UT2004BotVIPController bot, double priority) {
        super(bot, priority);
    }
    public GuardingBehavior(UT2004BotVIPController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public GuardingBehavior(UT2004BotVIPController bot, double priority, KnowledgeBase knowledge) {
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

        Location guard_location = _knowledge.getGuardKnowledge().guard_place();
        if(Navigation.directDistance(guard_location, _bot) < 600.0){
            _bot.getMove().moveTo(guard_location);
        }
        _bot.getNMNav().navigate(guard_location);
    }
}
