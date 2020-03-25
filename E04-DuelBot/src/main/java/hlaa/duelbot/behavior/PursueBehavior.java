package hlaa.duelbot.behavior;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import hlaa.duelbot.KnowledgeBase;
import hlaa.duelbot.utils.Navigation;

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
        // get point to navigate
        NavPoint p;
        do {
            p = _knowledge.getPointWithMaxProb();
            _knowledge.updateNavpoint(p, 0.0f);
        } while(!Navigation.canReachNavpoint(_bot.getNMNav(), _bot.getInfo().getLocation(), p.getLocation()));

        //System.out.println("Bot location " + _bot.getInfo().getLocation());
        _bot.getLog().info("Pursue to point " + p.getLocation());
        _bot.getNMNav().navigate(p);

        //look at the point if visible
        if(Navigation.canSeeNavpoint(_bot, p)){
            _bot.getNavigation().setFocus(p);
        }
        else {
            _bot.getNavigation().setFocus(null);
        }
    }

    @Override
    public double priority() {
        return 10.0;
    }
}
