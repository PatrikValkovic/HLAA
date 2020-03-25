package hlaa.duelbot.utils;

import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weapon;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
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

    public static Set<ItemType> getUsableWeapons(Weaponry weaponry){
        return weaponry.getWeapons()
                .values()
                .stream()
                .filter(w -> weaponry.getAmmo(w.getType()) > 0)
                .map(Weapon::getType)
                .collect(Collectors.toSet());
    }

    public static boolean hasWeapon(Weaponry weaponry, ItemType type){
        return weaponry.hasWeapon(type) && weaponry.hasAmmo(type);
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

    public static double normalDistribution(double mean, double std, double val){
        return 1.0 / (std * Math.sqrt(2*Math.PI)) * Math.exp(-0.5 * Math.pow((val - mean) / std, 2.0));
    }

    public static void showDistributions(List<CombatBehavior.WeaponPref> prefs){
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        for(CombatBehavior.WeaponPref pref : prefs){
            TimeSeries series = new TimeSeries(pref.getWeapon().getName());
            for(int i=0;i<3000;i++){
                series.add(
                        new Second(i % 60, i / 60,1 + i / 3600,1,1,2000),
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
        f.setSize(1200,960);
        f.add(panel);
        f.setVisible(true);
    }

    public static CombatBehavior.WeaponPref bestWeapon(Weaponry available, List<CombatBehavior.WeaponPref> weaponPrefs, double distance){
        return weaponPrefs.stream()
                          .filter(i -> available.hasWeapon(i.getWeapon()))
                          .max(Comparator.comparingDouble(
                                  w -> normalDistribution(w.getPriorityMean(), w.getPriorityStd(), distance) * w.getPriority()
                          )).get();
    }

}
