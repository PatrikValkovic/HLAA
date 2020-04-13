package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshClearanceComputer;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import java.util.Optional;
import math.geom2d.Vector2D;

public class RocketAvoidanceReflex extends BaseReflex {

    public RocketAvoidanceReflex(UT2004BotModuleController bot) {
        super(bot);
    }

    @Override
    public boolean triggered() {
        Optional<IncomingProjectile> projectile = _bot.getWorld()
                                                      .getAll(IncomingProjectile.class)
                                                      .values()
                                                      .stream()
                                                      .findFirst();
        return projectile.isPresent();
    }

    @Override
    public void execute() {
        try {
            _bot.getLog().warning("Incoming projectile");
            // get projectile
            IncomingProjectile projectile = _bot.getWorld()
                                                .getAll(IncomingProjectile.class)
                                                .values()
                                                .stream()
                                                .findFirst()
                                                .get();
            // analytics
            Location projectileOrigin = projectile.getOrigin();
            Location projectileDirection = new Location(projectile.getDirection()).getNormalized();
            Location botLocation = _bot.getBot().getLocation();
            Location upVector = new Location(0.0f, 0.0f, 1.0f);
            Location toEnemyVector = projectileOrigin.sub(botLocation).getNormalized();
            Location rightVector = toEnemyVector.cross(upVector).getNormalized();
            Location myPlaneNorm = rightVector.cross(upVector.scale(-1)).getNormalized();
            double d = -botLocation.dot(myPlaneNorm);
            double divider = projectileDirection.dot(myPlaneNorm);
            if (Math.abs(divider) < 10e-12) {
                // ignore
                return;
            }
            double t = (-d - myPlaneNorm.dot(projectileOrigin)) / divider;
            Location rocketIntersection = projectileOrigin.add(projectileDirection.scale(t));
            Location botIntersectionDirection = rocketIntersection.sub(botLocation).getNormalized();


            // decide escape direction
            Vector2D[] escapeDirections = new Vector2D[]{
                    new Vector2D(-botIntersectionDirection.x, -botIntersectionDirection.y),
                    new Vector2D(botIntersectionDirection.x, -botIntersectionDirection.y),
                    new Vector2D(-botIntersectionDirection.x, botIntersectionDirection.y),
                    new Vector2D(botIntersectionDirection.x, botIntersectionDirection.y),
            };
            double bestEscapeUtility = Double.NEGATIVE_INFINITY;
            Location toLoc = botLocation;
            NavMeshClearanceComputer clearanceComputer = _bot.getNavMeshModule().getClearanceComputer();
            for (Vector2D escapeDirection : escapeDirections) {
                NavMeshClearanceComputer.ClearanceLimit limit = clearanceComputer.findEdge(botLocation, escapeDirection);
                double utility = 0;
                utility = limit.getLocation().getDistance(rocketIntersection);
                utility = Math.min(utility, 300.0);

                if (utility > bestEscapeUtility) {
                    bestEscapeUtility = utility;
                    toLoc = new Location(escapeDirection.getX(), escapeDirection.getY(), 0.0f);
                    toLoc = botLocation.add(toLoc.getNormalized().scale(10.0));
                }
            }


            // make actions
            _bot.getMove().turnTo(projectileOrigin);
            _bot.getMove().dodge(toLoc.sub(botLocation), true);
        }
        catch (Exception e){
            _bot.getLog().severe("Error avoiding rocket", e);
        }
    }
}
