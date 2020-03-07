package ut2004.exercises.e02;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GlobalChat;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class Utils {

    public static final int CATCH_DISTANCE = 150;

    public static boolean gameRunning = false;

    public static void handleMessage(GlobalChat msg) {
        if (msg.getText().toLowerCase().contains("restart")) gameRunning = true;
        else if (msg.getText().toLowerCase().contains("start")) gameRunning = true;
        else if (msg.getText().toLowerCase().contains("stop")) gameRunning = false;
    }

    public static boolean isSheep(Player player) {
        return player.getName().toLowerCase().contains("sheepbot");
    }

    public static boolean isWolf(Player player) {
        return !isSheep(player);
    }

    public static Player getNearestSheep(Player[] players, Location loc) {
        Optional<Player> r = Stream.of(players)
                                   .filter(Utils::isSheep)
                                   .min(Comparator.comparingDouble(c -> c.getLocation().getDistance(loc)));
		return r.orElse(null);
	}

    public static double distanceFrom(Location loc1, Location loc2) {
        return loc1.getDistance(loc2);
    }


    public static Location parseLocation(String text){
        String content = text.substring(1, text.length() - 2);
        String[] locations = content.split(";");
        return new Location(
                Double.parseDouble(locations[0]),
                Double.parseDouble(locations[1]),
                Double.parseDouble(locations[2])
        );
    }

}
