package hlaa.tdm;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.utils.Heatup;
import hlaa.tdm.behavior.*;
import hlaa.tdm.utils.Inventory;
import hlaa.tdm.utils.Navigation;
import hlaa.tdm.utils.WeaponPrefs;

public class MainDecisions implements IBehaviorProvider {

    private final UT2004BotModuleController _bot;
    private final KnowledgeBase _knowledge;

    private final BehaviorManager _combat;
    private final MedkitBehavior _medkit;
    private final PickingBehavior _picking;
    private final PursueBehavior _pursue;
    private final RetreatBehavior _retreat;
    private final Heatup _pursuitHeatup = new Heatup(6000);

    public MainDecisions(UT2004BotModuleController bot, KnowledgeBase knowledge){
        _bot = bot;
        _knowledge = knowledge;

        _combat = new BehaviorManager(_bot.getLog())
                .addBehavior(new CombatBehavior(_bot, 100, knowledge))
                .addBehavior(new CombatMovementBehaviour(_bot, knowledge));
        _medkit = new MedkitBehavior(_bot, _knowledge);
        _picking = new PickingBehavior(_bot, _knowledge);
        _pursue = new PursueBehavior(_bot, _knowledge);
        _retreat = new RetreatBehavior(_bot, _knowledge);
    }

    @Override
    public IBehavior get() {
        if(_bot.getPlayers().getNearestVisiblePlayer() != null){
            _pursuitHeatup.heat();
            if(_bot.getInfo().getHealth() < 20)
                return _retreat;
            if(Inventory.getWeaponStrengthForDistance(
                    _bot.getWeaponry(),
                    WeaponPrefs.WEAPON_PREFS,
                    Navigation.directDistance(_bot, _bot.getPlayers().getNearestVisiblePlayer().getLocation())) < 0.0025)
                return _retreat;
            return _combat;
        }
        else { // dont see player
            if(_bot.getInfo().hasUDamage())
                return _pursue;
            if(_bot.getInfo().getHealth() < 40)
                return _medkit;
            if(Inventory.hasAllRangeWeapon(_bot.getWeaponry()) && _pursuitHeatup.isHot())
                return _pursue;
            if(_picking.itemsTopickup().size() > 0)
                return _picking;
            return _pursue;
        }
    }
}
