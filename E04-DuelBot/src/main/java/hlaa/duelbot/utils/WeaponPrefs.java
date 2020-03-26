package hlaa.duelbot.utils;

import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import hlaa.duelbot.behavior.CombatBehavior;
import java.util.LinkedList;
import java.util.List;

public class WeaponPrefs {

    public static final List<CombatBehavior.WeaponPref> WEAPON_PREFS = new LinkedList<>();
    static {
        //TODO make sure priorities are correct
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(400.0, 100.0, UT2004ItemType.FLAK_CANNON, true, 0.6));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(180.0, 60.0, UT2004ItemType.FLAK_CANNON, false, 0.2));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(600, 150, UT2004ItemType.MINIGUN, true, 1.0));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(1100, 200, UT2004ItemType.MINIGUN, false, 1.0));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(1500, 300, UT2004ItemType.SHOCK_RIFLE, true, 1.4));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(1400, 200, UT2004ItemType.LIGHTNING_GUN, true, 1.6));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(1400, 200, UT2004ItemType.SNIPER_RIFLE, true, 1.6));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(1700, 200, UT2004ItemType.LIGHTNING_GUN, true, 1.4));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(1700, 200, UT2004ItemType.SNIPER_RIFLE, true, 1.4));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(2500, 500, UT2004ItemType.LIGHTNING_GUN, true, 2.6));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(2500, 500, UT2004ItemType.SNIPER_RIFLE, true, 2.6));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(250, 80, UT2004ItemType.ROCKET_LAUNCHER, true, 0.4));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(400, 400, UT2004ItemType.ASSAULT_RIFLE, true, 0.4));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(150, 100, UT2004ItemType.BIO_RIFLE, true, 0.35));
        WEAPON_PREFS.add(new CombatBehavior.WeaponPref(400, 250, UT2004ItemType.LINK_GUN, true, 1.1));
        //Inventory.showDistributions(WEAPON_PREFS);
    }

}
