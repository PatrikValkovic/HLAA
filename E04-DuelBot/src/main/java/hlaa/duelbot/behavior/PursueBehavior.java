package hlaa.duelbot.behavior;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import hlaa.duelbot.KnowledgeBase;
import hlaa.duelbot.utils.Inventory;
import hlaa.duelbot.utils.Navigation;
import hlaa.duelbot.utils.WeaponPrefs;

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
        NavPoint p = _knowledge.getPointWithMaxProb();
        while(!Navigation.canReachNavpoint(_bot.getNMNav(), _bot.getInfo().getLocation(), p.getLocation())) {
            _knowledge.updateNavpoint(p, 0.0f);
            p = _knowledge.getPointWithMaxProb();
        }

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

        //set weapon
        CombatBehavior.WeaponPref pref =
                Inventory.bestWeapon(_bot.getWeaponry(), WeaponPrefs.WEAPON_PREFS, Navigation.directDistance(_bot, p));
        _bot.getWeaponry().changeWeapon(pref.getWeapon());
    }
}
