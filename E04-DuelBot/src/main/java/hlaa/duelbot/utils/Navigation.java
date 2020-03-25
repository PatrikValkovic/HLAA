package hlaa.duelbot.utils;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.levelGeometry.LevelGeometry;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.levelGeometry.RayCastResult;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import java.awt.*;

public class Navigation {

    private static final double RAY_LIFT = 50.0;

    public static double distanceBetween(NavMeshNavigation nav, Location from, Location to) {
        return nav.getPathPlanner().getDistance(from, to);
    }

    public static double pathComposition(NavMeshNavigation nav, Location from, Location through, Location to){
        return distanceBetween(nav, from, through) + distanceBetween(nav, through, to);
    }

    public static boolean isVisible(LevelGeometry g, Location from, Location to){
        double distanceBetween = from.getDistance(to);
        RayCastResult ray = g.rayCast(from, to);
        double rayDistance = ray.isHit() ? ray.hitDistance : Double.POSITIVE_INFINITY;
        return distanceBetween < rayDistance;
    }

    public static boolean canSeeNavpoint(UT2004BotModuleController bot, NavPoint point){
        bot.getDraw().clearAll();
        bot.getDraw().drawLine(Color.GREEN, bot.getInfo().getLocation(), point.getLocation().add(new Location(0.0, 0.0, RAY_LIFT)));
        RayCastResult ray = bot.getLevelGeometry().rayCast(bot.getInfo().getLocation(), point.getLocation().add(new Location(0.0, 0.0, RAY_LIFT)));
        if(ray.isHit()){
            bot.getDraw().drawLine(Color.BLUE, bot.getInfo().getLocation(), ray.hitLocation);
        }
        else {
            bot.getDraw().drawLine(Color.BLUE, bot.getInfo().getLocation(), point.getLocation().add(new Location(0.0, 0.0, RAY_LIFT-10.0)));
        }

        return isVisible(
                bot.getLevelGeometry(),
                bot.getInfo().getLocation(),
                point.getLocation().add(new Location(0.0,0.0, RAY_LIFT))
        );
    }

    public static boolean canReachNavpoint(NavMeshNavigation nav, Location from, Location to){
        return nav.getPathPlanner().computePath(from, to).get() != null;
    }

}
