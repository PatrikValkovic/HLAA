package vip.bot.utils;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import java.util.ArrayList;
import java.util.List;

public class LockLocations {

    public static List<List<Location>> getLockLocations(Game game) {
        if (game.getMapName().contains("Roughinery")) {
            return getRoughineryLockLocations();
        } else if(game.getMapName().contains("Ironic")) {
            return getIronicLockLocations();
        } else if(game.getMapName().contains("Rankin")) {
            return getRankinLockLocations();
        } else return new ArrayList<>();
    }

    public static List<List<Location>> getRoughineryLockLocations() {
        return new ArrayList<List<Location>>() {{
            add(new ArrayList<Location>() {{
                add(new Location(-870, -1482, -206));
                add(new Location(114, -890, -206));
            }});

            add(new ArrayList<Location>() {{
                add(new Location(892, 889, -206));
                add(new Location(1013, 792, -1006));
                add(new Location(1153, 1437, -718));
            }});

            add(new ArrayList<Location>() {{
                add(new Location(1266, -121, -974));
                add(new Location(873, -1421, -974));
            }});

            add(new ArrayList<Location>() {{
                add(new Location(481, -163, -1230));
                add(new Location(-841, 430, -974));
            }});
        }};
    }

    public static List<List<Location>> getIronicLockLocations() {
        return new ArrayList<List<Location>>() {{
            add(new ArrayList<Location>() {{
                add(new Location(477, -21, -78));
                add(new Location(-368, 363, -78));
            }});

            add(new ArrayList<Location>() {{
                add(new Location(834, 1139, -462));
                add(new Location(-71, 1273, -460));
                add(new Location(-911, 547, -206));
            }});

            add(new ArrayList<Location>() {{
                add(new Location(-91, -1881, -46));
                add(new Location(185, -1329, -462));
                add(new Location(-945, -1241, -206));
            }});

            add(new ArrayList<Location>() {{
                add(new Location(818, -544, -462));
                add(new Location(1208, -1542, -462));
                add(new Location(1056, -1883, -462));
                add(new Location(1468, -966, -366));
            }});
        }};
    }

    public static List<List<Location>> getRankinLockLocations() {
        return new ArrayList<List<Location>>() {{
            add(new ArrayList<Location>() {{
                add(new Location(2026, -2058, 49));
                add(new Location(2758, -2500, 56));
                add(new Location(2326, -1198, 49));
            }});

            add(new ArrayList<Location>() {{
                add(new Location(778, 540, 49));
                add(new Location(-242, -465, 50));
                add(new Location(-71, -823, 49));
            }});

            add(new ArrayList<Location>() {{
                add(new Location(-692, 257, -260));
                add(new Location(-156, 1273, -270));
            }});

            add(new ArrayList<Location>() {{
                add(new Location(3515, -611, -366));
                add(new Location(3200, 17, -366));
                add(new Location(4188, -343, -322));
            }});
        }};
    }

}
