package hlaa.tdm.utils;

import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class WeaponPrefs {

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
        WEAPON_PREFS.add(new WeaponPref(400.0, 300.0, UT2004ItemType.FLAK_CANNON, true, 6));
        WEAPON_PREFS.add(new WeaponPref(180.0, 180.0, UT2004ItemType.FLAK_CANNON, false, 2));
        WEAPON_PREFS.add(new WeaponPref(600, 450, UT2004ItemType.MINIGUN, true, 10));
        WEAPON_PREFS.add(new WeaponPref(1100, 600, UT2004ItemType.MINIGUN, false, 10));
        WEAPON_PREFS.add(new WeaponPref(1500, 900, UT2004ItemType.SHOCK_RIFLE, true, 14));
        WEAPON_PREFS.add(new WeaponPref(1400, 600, UT2004ItemType.LIGHTNING_GUN, true, 16));
        WEAPON_PREFS.add(new WeaponPref(1400, 600, UT2004ItemType.SNIPER_RIFLE, true, 16));
        WEAPON_PREFS.add(new WeaponPref(1700, 600, UT2004ItemType.LIGHTNING_GUN, true, 14));
        WEAPON_PREFS.add(new WeaponPref(1700, 600, UT2004ItemType.SNIPER_RIFLE, true, 14));
        WEAPON_PREFS.add(new WeaponPref(2500, 1500, UT2004ItemType.LIGHTNING_GUN, true, 26));
        WEAPON_PREFS.add(new WeaponPref(2500, 1500, UT2004ItemType.SNIPER_RIFLE, true, 26));
        WEAPON_PREFS.add(new WeaponPref(250, 240, UT2004ItemType.ROCKET_LAUNCHER, true, 4));
        WEAPON_PREFS.add(new WeaponPref(400, 1200, UT2004ItemType.ASSAULT_RIFLE, true, 4));
        WEAPON_PREFS.add(new WeaponPref(150, 300, UT2004ItemType.BIO_RIFLE, true, 3.5));
        WEAPON_PREFS.add(new WeaponPref(400, 750, UT2004ItemType.LINK_GUN, true, 11));
        //Inventory.showDistributions(WEAPON_PREFS);
    }
}
