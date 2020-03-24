package hlaa.duelbot.utils;

import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Inventory {

    public static Set<ItemType> getUsableWeapons(Weaponry weaponry){
        return weaponry.getWeapons()
                .values()
                .stream()
                .filter(w -> weaponry.getAmmo(w.getType()) > 0)
                .map(Weapon::getType)
                .collect(Collectors.toSet());
    }

    public static boolean hasWeapon(Weaponry weaponry, ItemType type){
        return weaponry.hasWeapon(type);
    }

    public static boolean hasDistanceWeapon(Weaponry weaponry){
        return !Collections.disjoint(
                Inventory.getUsableWeapons(weaponry),
                new HashSet<ItemType>(){{
                    add(UT2004ItemType.LIGHTNING_GUN);
                    add(UT2004ItemType.SNIPER_RIFLE);
                    add(UT2004ItemType.SHOCK_RIFLE);
                }}
        );
    }

    public static boolean hasMidrangeWeapon(Weaponry weaponry){
        return !Collections.disjoint(
                Inventory.getUsableWeapons(weaponry),
                new HashSet<ItemType>(){{
                    add(UT2004ItemType.MINIGUN);
                    add(UT2004ItemType.LINK_GUN);
                    add(UT2004ItemType.ROCKET_LAUNCHER);
                }}
        );
    }

    public static boolean hasShortrangeWeapon(Weaponry weaponry){
        return !Collections.disjoint(
                Inventory.getUsableWeapons(weaponry),
                new HashSet<ItemType>(){{
                    add(UT2004ItemType.FLAK_CANNON);
                    add(UT2004ItemType.BIO_RIFLE);
                }}
        );
    }

}
