package hlaa.duelbot.utils;

import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import hlaa.duelbot.behavior.CombatBehavior;
import java.util.*;
import java.util.stream.Collectors;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

public class Inventory {

    public static Set<ItemType> getUsableWeapons(Weaponry weaponry) {
        return weaponry.getWeapons()
                       .values()
                       .stream()
                       .filter(w -> weaponry.getAmmo(w.getType()) > 0)
                       .map(Weapon::getType)
                       .collect(Collectors.toSet());
    }

    public static boolean needItem(Weaponry weaponry, AgentInfo info, ItemType item) {
        return needWeapon(weaponry, item) ||
                needHealthPack(info, item) ||
                needShieldPack(info, item) ||
                UT2004ItemType.U_DAMAGE_PACK.equals(item);
    }

    public static boolean needWeapon(Weaponry weaponry, ItemType type) {
        return isWeapon(type) &&
                (!weaponry.hasWeapon(type) ||
                        weaponry.getPrimaryWeaponAmmo(type) < weaponry.getMaxAmmo(weaponry.getPrimaryWeaponAmmoType(type)) ||
                        weaponry.getSecondaryWeaponAmmo(type) < weaponry.getMaxAmmo(weaponry.getSecondaryWeaponAmmoType(type))
                );
    }

    public static boolean needHealthPack(AgentInfo info, ItemType type) {
        return isHealth(type) &&
                (UT2004ItemType.HEALTH_PACK.equals(type) && info.getHealth() < 100) ||
                (UT2004ItemType.MINI_HEALTH_PACK.equals(type) && info.getHealth() < 199) ||
                (UT2004ItemType.SUPER_HEALTH_PACK.equals(type) && info.getHealth() < 100); //TODO not sure
    }

    public static boolean needShieldPack(AgentInfo info, ItemType type) {
        return isShield(type) && info.getArmor() < 150;
        //return UT2004ItemType.Category.SHIELD.getTypes().contains(type) &&
        //        info.getArmor() < 150;
    }

    public static boolean hasLongRangeWeapon(Weaponry weaponry) {
        return !Collections.disjoint(
                Inventory.getUsableWeapons(weaponry),
                new HashSet<ItemType>() {{
                    add(UT2004ItemType.LIGHTNING_GUN);
                    add(UT2004ItemType.SNIPER_RIFLE);
                    add(UT2004ItemType.SHOCK_RIFLE);
                }}
        );
    }

    public static boolean hasMidRangeWeapon(Weaponry weaponry) {
        return !Collections.disjoint(
                Inventory.getUsableWeapons(weaponry),
                new HashSet<ItemType>() {{
                    add(UT2004ItemType.MINIGUN);
                    add(UT2004ItemType.LINK_GUN);
                    add(UT2004ItemType.ROCKET_LAUNCHER);
                }}
        );
    }

    public static boolean hasShortRangeWeapon(Weaponry weaponry) {
        return !Collections.disjoint(
                Inventory.getUsableWeapons(weaponry),
                new HashSet<ItemType>() {{
                    add(UT2004ItemType.ROCKET_LAUNCHER);
                    add(UT2004ItemType.MINIGUN);
                    add(UT2004ItemType.FLAK_CANNON);
                    add(UT2004ItemType.BIO_RIFLE);
                    add(UT2004ItemType.LINK_GUN);
                }}
        );
    }

    public static boolean hasAllRangeWeapon(Weaponry weaponry) {
        return hasShortRangeWeapon(weaponry) && hasMidRangeWeapon(weaponry) && hasLongRangeWeapon(weaponry);
    }

    public static double normalDistribution(double mean, double std, double val) {
        return 1.0 / (std * Math.sqrt(2 * Math.PI)) * Math.exp(-0.5 * Math.pow((val - mean) / std, 2.0));
    }

    public static void showDistributions(List<CombatBehavior.WeaponPref> prefs) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        for (CombatBehavior.WeaponPref pref : prefs) {
            TimeSeries series = new TimeSeries(pref.getWeapon().getName());
            for (int i = 0; i < 3000; i++) {
                series.add(
                        new Second(i % 60, i / 60, 1 + i / 3600, 1, 1, 2000),
                        normalDistribution(pref.getPriorityMean(), pref.getPriorityStd(), i) * pref.getPriority()
                );
            }
            dataset.addSeries(series);
        }
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Weapon priorities distributions",
                "distance", "priority",
                dataset
        );
        ChartPanel panel = new ChartPanel(chart, false);
        ApplicationFrame f = new ApplicationFrame("Plot");
        f.setSize(1200, 960);
        f.add(panel);
        f.setVisible(true);
    }

    public static double getWeaponStrengthForDistance(Weaponry weaponry, List<CombatBehavior.WeaponPref> weaponPrefs, double distance) {
        return weaponPrefs.stream()
                          .filter(w -> canUseWeapon(weaponry, w.getWeapon()))
                          .mapToDouble(w -> normalDistribution(w.getPriorityMean(), w.getPriorityStd(), distance) * w.getPriority())
                          .max()
                          .orElse(0.0);
    }

    public static CombatBehavior.WeaponPref bestWeapon(Weaponry available, List<CombatBehavior.WeaponPref> weaponPrefs, double distance) {
        return bestWeapon(available, weaponPrefs, distance, new HashSet<>());
    }

    public static CombatBehavior.WeaponPref bestWeapon(Weaponry available, List<CombatBehavior.WeaponPref> weaponPrefs, double distance, Set<ItemType> except) {
        return weaponPrefs.stream()
                          .filter(i -> available.hasWeapon(i.getWeapon()))
                          .filter(i -> !except.contains(i.getWeapon()))
                          .max(Comparator.comparingDouble(
                                  w -> normalDistribution(w.getPriorityMean(), w.getPriorityStd(), distance) * w.getPriority()
                          )).get();
    }

    public static boolean canUseWeapon(Weaponry weaponry, ItemType type) {
        return UT2004ItemType.Category.WEAPON.getTypes().contains(type) &&
                weaponry.hasWeapon(type) && weaponry.getAmmo(type) >= 5;
    }

    public static boolean isWeapon(ItemType type) {
        return UT2004ItemType.Category.WEAPON.getTypes().contains(type);
    }

    public static boolean isHealth(ItemType type) {
        return UT2004ItemType.Category.HEALTH.getTypes().contains(type);
    }

    public static boolean isShield(ItemType type) {
        return UT2004ItemType.SHIELD_PACK.equals(type) || UT2004ItemType.SUPER_SHIELD_PACK.equals(type);
    }

    public static boolean isUDamage(ItemType type) {
        return UT2004ItemType.U_DAMAGE_PACK.equals(type);
    }

    public static boolean needUDamage(AgentInfo info) {
        return true;
    }

    public static boolean isAmmo(ItemType type) {
        return UT2004ItemType.Category.AMMO.getTypes().contains(type);
    }

    public static boolean needAmmo(Weaponry weaponry, ItemType type) {
        int currentAmmo = weaponry.getAmmo(type);
        int maxAmmo = weaponry.getMaxAmmo(type);
        return currentAmmo < maxAmmo;
    }

    public static boolean shouldPickup(Weaponry weaponry, AgentInfo info, ItemType type) {
        return (isWeapon(type) && needWeapon(weaponry, type)) ||
                (isHealth(type) && needHealthPack(info, type)) ||
                (isShield(type) && needShieldPack(info, type)) ||
                (isUDamage(type) && needUDamage(info)) ||
                (isAmmo(type) && needAmmo(weaponry, type));
    }
}
