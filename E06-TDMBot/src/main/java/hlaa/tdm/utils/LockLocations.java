package hlaa.tdm.utils;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import java.util.ArrayList;
import java.util.List;

public class LockLocations {

    public static List<List<Location>> getLockLocations(Game game) {
        if (game.getMapName().contains("Roughinery")) {
            return getRoughineryLockLocations();
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

    //TODO rest of maps


}
