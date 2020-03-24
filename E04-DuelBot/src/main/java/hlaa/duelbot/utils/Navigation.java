package hlaa.duelbot.utils;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;

public class Navigation {

    public static double distanceBetween(NavMeshNavigation nav, Location from, Location to) {
        return nav.getPathPlanner().getDistance(from, to);
    }

    public static double pathComposition(NavMeshNavigation nav, Location from, Location through, Location to){
        return distanceBetween(nav, from, through) + distanceBetween(nav, through, to);
    }

}
