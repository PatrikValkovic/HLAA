package vip.bot.utils;

import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import java.util.HashMap;
import java.util.Map;

public class WeaponPickingValues {
    public static final Map<ItemType, Double> WEAPON_WORTH = new HashMap<>();

    static {
        WEAPON_WORTH.put(UT2004ItemType.ASSAULT_RIFLE, 10.0);
        WEAPON_WORTH.put(UT2004ItemType.BIO_RIFLE, 40.0);
        WEAPON_WORTH.put(UT2004ItemType.LINK_GUN, 80.0);
        WEAPON_WORTH.put(UT2004ItemType.FLAK_CANNON, 180.0);
        WEAPON_WORTH.put(UT2004ItemType.MINIGUN, 120.0);
        WEAPON_WORTH.put(UT2004ItemType.SHOCK_RIFLE, 120.0);
        WEAPON_WORTH.put(UT2004ItemType.LIGHTNING_GUN, 200.0);
        WEAPON_WORTH.put(UT2004ItemType.SNIPER_RIFLE, 200.0);
        WEAPON_WORTH.put(UT2004ItemType.ROCKET_LAUNCHER, 90.0);
        WEAPON_WORTH.put(UT2004ItemType.U_DAMAGE_PACK, 125.0);
        WEAPON_WORTH.put(UT2004ItemType.SUPER_SHIELD_PACK, 90.0);
        WEAPON_WORTH.put(UT2004ItemType.SHIELD_PACK, 75.0);
        WEAPON_WORTH.put(UT2004ItemType.SUPER_HEALTH_PACK, 50.0);
        WEAPON_WORTH.put(UT2004ItemType.HEALTH_PACK, 25.0);
        WEAPON_WORTH.put(UT2004ItemType.MINI_HEALTH_PACK, 5.0);
        WEAPON_WORTH.put(UT2004ItemType.ADRENALINE_PACK, 1.0);

        WEAPON_WORTH.put(UT2004ItemType.ROCKET_LAUNCHER_AMMO, 9.0);
        WEAPON_WORTH.put(UT2004ItemType.MINIGUN_AMMO, 12.0);
        WEAPON_WORTH.put(UT2004ItemType.ASSAULT_RIFLE_AMMO, 1.0);
        WEAPON_WORTH.put(UT2004ItemType.LINK_GUN_AMMO, 8.0);
        WEAPON_WORTH.put(UT2004ItemType.LIGHTNING_GUN_AMMO, 20.0);
        WEAPON_WORTH.put(UT2004ItemType.SHOCK_RIFLE_AMMO, 12.0);
        WEAPON_WORTH.put(UT2004ItemType.SNIPER_RIFLE_AMMO, 20.0);
        WEAPON_WORTH.put(UT2004ItemType.FLAK_CANNON_AMMO, 18.0);
        WEAPON_WORTH.put(UT2004ItemType.BIO_RIFLE_AMMO, 4.0);
        WEAPON_WORTH.put(UT2004ItemType.SHIELD_GUN_AMMO, 0.5);
        WEAPON_WORTH.put(UT2004ItemType.REDEEMER_AMMO, 0.0);
        WEAPON_WORTH.put(UT2004ItemType.ION_PAINTER_AMMO, 0.0);
        WEAPON_WORTH.put(UT2004ItemType.ONS_MINE_LAYER_AMMO, 0.0);
        WEAPON_WORTH.put(UT2004ItemType.ONS_GRENADE_LAUNCHER_AMMO, 0.0);
        WEAPON_WORTH.put(UT2004ItemType.ONS_AVRIL_AMMO, 0.0);
    }
}
