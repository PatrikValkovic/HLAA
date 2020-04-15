package hlaa.tdm.utils;

import cz.cuni.amis.pathfinding.alg.astar.AStarResult;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.agent.navigation.impl.PathFuture;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.levelGeometry.LevelGeometry;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.levelGeometry.RayCastResult;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshClearanceComputer;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshModule;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import java.util.*;
import math.geom2d.Vector2D;

public class Navigation {

    private static final double RAY_LIFT = 50.0;

    public static double distanceBetween(NavMeshNavigation nav, ILocated from, ILocated to) {
        return nav.getPathPlanner().getDistance(from, to);
    }

    public static double pathComposition(NavMeshNavigation nav, Location from, Location through, Location to){
        return distanceBetween(nav, from, through) + distanceBetween(nav, through, to);
    }

    public static boolean isVisible(LevelGeometry g, Location from, Location to){
        if(g == null || !g.isLoaded())
            return false;

        double distanceBetween = from.getDistance(to);
        double rayDistance = rayCastDistance(g, from, to);
        return distanceBetween < rayDistance;
    }

    public static RayCastResult rayCast(LevelGeometry g, Location from, Location to){
        if(g == null || !g.isLoaded())
            return null;

        return g.rayCast(from, to);
    }

    public static double rayCastDistance(LevelGeometry g, Location from, Location to){
        if(g == null || !g.isLoaded())
            return Double.POSITIVE_INFINITY;

        RayCastResult ray = rayCast(g, from, to);
        double rayDistance = ray.isHit() ? ray.hitDistance : Double.POSITIVE_INFINITY;
        return rayDistance;
    }

    public static boolean canSee(LevelGeometry g, Location from, Location to){
        return isVisible(g, from, to);
    }

    public static boolean canSeeNavpoint(UT2004BotModuleController bot, NavPoint point){
        if(bot.getLevelGeometry() == null || !bot.getLevelGeometry().isLoaded())
            return false;

        return isVisible(
                bot.getLevelGeometry(),
                bot.getInfo().getLocation(),
                point.getLocation().add(new Location(0.0,0.0, RAY_LIFT))
        );
    }

    public static boolean canReachNavpoint(NavMeshNavigation nav, Location from, Location to){
        return nav.getPathPlanner().computePath(from, to).get() != null;
    }

    public static PathFuture<ILocated> pathThrough(NavMeshNavigation nav, List<? extends ILocated> points, ILocated botLocation){
        nav.getPathExecutor().stop();
        if(points == null || points.size() == 0){
            nav.getLog().warning("Path for navigate through is not valid");
            return new PathFuture<>(botLocation, botLocation);
        }

        List<ILocated> finalPath = new ArrayList<>();
        finalPath.addAll(nav.getPathPlanner().computePath(botLocation, points.get(0)).get());
        for(int i=1;i<points.size();i++){
            IPathFuture<ILocated> path = nav.getPathPlanner().computePath(points.get(i-1), points.get(i));
            finalPath.addAll(path.get());
        }

        PathFuture<ILocated> pathFuture = new PathFuture<>(finalPath.get(0), finalPath.get(finalPath.size()-1));
        pathFuture.setResult(finalPath);
        return pathFuture;
    }

    public static void navigateThrough(NavMeshNavigation nav, List<? extends ILocated> points, ILocated botLocation){

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
        return getClosestNavpoint(bot.getInfo(), navpoints);
    }

    public static NavPoint getClosestNavpoint(ILocated from, NavPoints navpoints){
        return navpoints.getNavPoints()
                .values()
                .stream()
                .min(Comparator.comparingDouble(p -> directDistance(p, from)))
                .get();
    }

    public static double navMeshRayCastDistance(NavMeshModule n, ILocated from, Vector2D direction){
        if(n == null || !n.getNavMesh().isLoaded())
            return Double.POSITIVE_INFINITY;

        return navMeshRayCast(n, from, direction).getDistance(from.getLocation());
    }

    public static Location navMeshRayCast(NavMeshModule n, ILocated from, Vector2D direction){
        if(n == null || !n.getNavMesh().isLoaded())
            return from.getLocation();

        NavMeshClearanceComputer comp = n.getClearanceComputer();
        NavMeshClearanceComputer.ClearanceLimit limit = comp.findEdge(from.getLocation(), direction);
        return limit.getLocation();
    }

    public static Set<Vector2D> eightDirections(Vector2D base) {
        return new HashSet<Vector2D>() {{
            add(new Vector2D(base.getX(), base.getY()));
            add(new Vector2D(-base.getX(), -base.getY()));
            add(new Vector2D(-base.getY(), base.getX()));
            add(new Vector2D(base.getY(), -base.getX()));

            add(new Vector2D(base.getX() - base.getY(), base.getY() + base.getX()));
            add(new Vector2D(-base.getX() - base.getY(), -base.getY() + base.getX()));
            add(new Vector2D(base.getX() + base.getY(), base.getY() - base.getX()));
            add(new Vector2D(-base.getX() + base.getY(), -base.getY() - base.getX()));
        }};
    }

}
