package hlaa.duelbot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import hlaa.duelbot.KnowledgeBase;
import hlaa.duelbot.utils.Inventory;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class CombatBehavior extends BaseBehavior {

    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class WeaponPref {
        @Getter
        @NonNull
        private double priorityMean;
        @Getter
        @NonNull
        private double priorityStd;
        @Getter
        @NonNull
        private ItemType weapon;
        @Getter
        @NonNull
        private boolean primaryMode;
        @Getter
        private double priority = 1.0;
    }
    public static final List<WeaponPref> WEAPON_PREFS = new LinkedList<>();
    static {
        //TODO make sure priorities are correct
        WEAPON_PREFS.add(new WeaponPref(400.0, 100.0, UT2004ItemType.FLAK_CANNON, true));
        WEAPON_PREFS.add(new WeaponPref(180.0, 60.0, UT2004ItemType.FLAK_CANNON, false, 0.7));
        WEAPON_PREFS.add(new WeaponPref(600, 150, UT2004ItemType.MINIGUN, true));
        WEAPON_PREFS.add(new WeaponPref(1100, 200, UT2004ItemType.MINIGUN, false));
        WEAPON_PREFS.add(new WeaponPref(1500, 300, UT2004ItemType.SHOCK_RIFLE, true));
        WEAPON_PREFS.add(new WeaponPref(1400, 200, UT2004ItemType.LIGHTNING_GUN, true));
        WEAPON_PREFS.add(new WeaponPref(1400, 200, UT2004ItemType.SNIPER_RIFLE, true));
        WEAPON_PREFS.add(new WeaponPref(1700, 200, UT2004ItemType.LIGHTNING_GUN, true));
        WEAPON_PREFS.add(new WeaponPref(1700, 200, UT2004ItemType.SNIPER_RIFLE, true));
        WEAPON_PREFS.add(new WeaponPref(2500, 500, UT2004ItemType.LIGHTNING_GUN, true));
        WEAPON_PREFS.add(new WeaponPref(2500, 500, UT2004ItemType.SNIPER_RIFLE, true));
        WEAPON_PREFS.add(new WeaponPref(250, 40, UT2004ItemType.ROCKET_LAUNCHER, true));
        WEAPON_PREFS.add(new WeaponPref(400, 400, UT2004ItemType.ASSAULT_RIFLE, true, 0.6));
        WEAPON_PREFS.add(new WeaponPref(150, 100, UT2004ItemType.BIO_RIFLE, true, 0.8));
        WEAPON_PREFS.add(new WeaponPref(400, 250, UT2004ItemType.LINK_GUN, true, 0.8));
        //Inventory.showDistributions(WEAPON_PREFS);
    }

    public CombatBehavior(UT2004BotModuleController bot) {
        super(bot);
    }
    public CombatBehavior(UT2004BotModuleController bot, double priority) {
        super(bot, priority);
    }
    public CombatBehavior(UT2004BotModuleController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public CombatBehavior(UT2004BotModuleController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    @Override
    public boolean isFiring() {
        if(_bot.getPlayers().getVisiblePlayers().size() == 0){
            _bot.getShoot().stopShooting();
        }

        return _bot.getPlayers().getVisiblePlayers().size() > 0;
    }

    @Override
    public void execute() {
        Location myLocation = _bot.getInfo().getLocation();
        Player opponent = _bot.getPlayers().getNearestVisiblePlayer();
        Location opponentLocation = opponent.getLocation();
        double playerDistance = myLocation.getDistance(opponentLocation);
        _bot.getLog().info("See player " + opponent.getName() + " " + playerDistance + " away");
        WeaponPref pref = Inventory.bestWeapon(_bot.getWeaponry(), WEAPON_PREFS, playerDistance);
        _bot.getLog().info("Decided for " + pref.getWeapon().getName() + " using " + (pref.isPrimaryMode() ? "primary" : "secondary"));

        _bot.getShoot().shoot(
                _bot.getWeaponry().getWeapon(pref.getWeapon()),
                pref.isPrimaryMode(),
                opponent
        );
    }

    @Override
    public void terminate() {
        _bot.getShoot().stopShooting();
    }
}
