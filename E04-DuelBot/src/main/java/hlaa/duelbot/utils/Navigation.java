package hlaa.duelbot.utils;

import cz.cuni.amis.pathfinding.alg.astar.AStarResult;
import cz.cuni.amis.pogamut.base.agent.navigation.impl.PathFuture;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.levelGeometry.LevelGeometry;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.levelGeometry.RayCastResult;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class Navigation {

    private static final double RAY_LIFT = 50.0;

    public static double distanceBetween(NavMeshNavigation nav, Location from, Location to) {
        return nav.getPathPlanner().getDistance(from, to);
    }

    public static double pathComposition(NavMeshNavigation nav, Location from, Location through, Location to){
        return distanceBetween(nav, from, through) + distanceBetween(nav, through, to);
    }

    public static boolean isVisible(LevelGeometry g, Location from, Location to){
        if(g == null || !g.isLoaded())
            return false;

        double distanceBetween = from.getDistance(to);
        RayCastResult ray = g.rayCast(from, to);
        double rayDistance = ray.isHit() ? ray.hitDistance : Double.POSITIVE_INFINITY;
        return distanceBetween < rayDistance;
    }

    public static boolean canSee(LevelGeometry g, Location from, Location to){
        return isVisible(g, from, to);
    }

    public static boolean canSeeNavpoint(UT2004BotModuleController bot, NavPoint point){
        bot.getDraw().drawLine(Color.GREEN, bot.getInfo().getLocation(), point.getLocation().add(new Location(0.0, 0.0, RAY_LIFT)));
        if(bot.getLevelGeometry() == null || !bot.getLevelGeometry().isLoaded())
            return false;

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

    public static void navigateThrough(NavMeshNavigation nav, List<? extends ILocated> points){
        PathFuture<ILocated> pathFuture = new PathFuture<>(points.get(0), points.get(points.size()-1));
        pathFuture.setResult(points.stream().map(n -> (ILocated)n.getLocation()).collect(Collectors.toList()));
        nav.getPathExecutor().followPath(pathFuture);
    }

    public static double directDistance(ILocated first, ILocated second){
        return first.getLocation().getDistance(second.getLocation());
    }

    public static double directDistance(UT2004BotModuleController first, ILocated second){
        return directDistance(first.getInfo(), second);
    }

    public static double directDistance(ILocated first, UT2004BotModuleController second){
        return directDistance(second.getInfo(), first);
    }

    public static List<NavPoint> getPath(AStarResult<NavPoint> path){
        if (!path.isSuccess())
            return null;

        Stack<NavPoint> tempPath = new Stack<>();
        Set<NavPoint> processed = new HashSet<>(16, 0.6f);
        tempPath.push(path.goalNode);

        NavPoint node = path.goalNode;
        while (node != path.startNode){
            node = path.getPreviousNode(node);
            if (node == null)
                return null; // path doesn't exist
            if(processed.contains(node))
                break;

            tempPath.push(node);
            processed.add(node);
        }

        List<NavPoint> finalPath = new ArrayList<NavPoint>(tempPath.size());

        while (!tempPath.empty()){
            finalPath.add(tempPath.pop());
        }

        return finalPath;
    }

    public static NavPoint getClosestNavpoint(UT2004BotModuleController bot, NavPoints navpoints){
        return getClosestNavpoint(bot.getInfo().getLocation(), navpoints);
    }

    public static NavPoint getClosestNavpoint(ILocated from, NavPoints navpoints){
        return navpoints.getNavPoints()
                .values()
                .stream()
                .min(Comparator.comparingDouble(p -> directDistance(p, from)))
                .get();
    }

}
