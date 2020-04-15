package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.utils.DrawingColors;
import hlaa.tdm.utils.Inventory;
import hlaa.tdm.utils.Navigation;
import hlaa.tdm.utils.WeaponPrefs;
import static hlaa.tdm.utils.DrawingColors.PURSUE_VISIBLE_NAVPOINT;

public class PursueBehavior extends BaseBehavior {

    public PursueBehavior(UT2004BotTCController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public PursueBehavior(UT2004BotTCController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    @Override
    public boolean isFiring() {
        return true;
    }

    @Override
    public void execute() {
        // get point to navigate
        NavPoint p = _knowledge.getEnemyPositionsKnowledge().getPointWithMaxProb();
        while(!Navigation.canReachNavpoint(_bot.getNMNav(), _bot.getInfo().getLocation(), p.getLocation())) {
            _knowledge.getEnemyPositionsKnowledge().updateNavpoint(p, 0.0f);
            p = _knowledge.getEnemyPositionsKnowledge().getPointWithMaxProb();
        }

        //System.out.println("Bot location " + _bot.getInfo().getLocation());
        _bot.getLog().info("Pursue to point " + p.getLocation());
        _bot.getNMNav().navigate(p);

        //draw point
        if(DrawingColors.DRAW){
            _bot.getDraw().drawLine(
                    DrawingColors.PURSUE_NAVPOINT,
                    _bot.getInfo().getLocation(),
                    p.getLocation()
            );
        }

        //look at the point if visible
        if(Navigation.canSeeNavpoint(_bot, p)){
            _bot.getNavigation().setFocus(p);
            if(DrawingColors.DRAW){
                _bot.getDraw().drawLine(PURSUE_VISIBLE_NAVPOINT, _bot.getInfo().getLocation(), p.getLocation().add(new Location(0,0,10)));
            }
        }
        else {
            _bot.getNavigation().setFocus(null);
        }

        //set weapon
        WeaponPrefs.WeaponPref pref =
                Inventory.bestWeaponForDistance(_bot.getWeaponry(), WeaponPrefs.WEAPON_PREFS, Navigation.directDistance(_bot, p));
        _bot.getWeaponry().changeWeapon(pref.getWeapon());
    }
}
