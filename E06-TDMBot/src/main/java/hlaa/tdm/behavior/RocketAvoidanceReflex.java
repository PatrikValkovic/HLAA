package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import hlaa.tdm.utils.Navigation;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import math.geom2d.Vector2D;

public class RocketAvoidanceReflex extends BaseReflex {

    public RocketAvoidanceReflex(UT2004BotTCController bot) {
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
            double d = -botLocation.dot(myPlaneNorm); // plane norm d
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
                    new Vector2D(botIntersectionDirection.x, botIntersectionDirection.y),
                    new Vector2D(-botIntersectionDirection.x, -botIntersectionDirection.y),
                    new Vector2D(-botIntersectionDirection.y, botIntersectionDirection.x),
                    new Vector2D(botIntersectionDirection.y, -botIntersectionDirection.x),

                    new Vector2D(
                            botIntersectionDirection.x - botIntersectionDirection.y,
                            botIntersectionDirection.y + botIntersectionDirection.x
                    ),
                    new Vector2D(
                            -botIntersectionDirection.x - botIntersectionDirection.y,
                            -botIntersectionDirection.y + botIntersectionDirection.x
                    ),
                    new Vector2D(
                            botIntersectionDirection.x + botIntersectionDirection.y,
                            botIntersectionDirection.y - botIntersectionDirection.x
                    ),
                    new Vector2D(
                            -botIntersectionDirection.x + botIntersectionDirection.y,
                            -botIntersectionDirection.y - botIntersectionDirection.x
                    ),
            };

            // compute best escape direction
            Vector2D escapeDirection =
                    Stream.of(escapeDirections)
                          .max(Comparator.comparingDouble(direction -> {
                              double escapeDistance = Navigation.navMeshRayCastDistance(_bot.getNavMeshModule(), _bot.getInfo(), direction);
                              escapeDistance = Math.min(escapeDistance, 400.0);
                              Location escapeResult = _bot.getInfo().getLocation().add(
                                      new Location(direction.getX(), direction.getY(), 0).getNormalized().scale(escapeDistance)
                              );
                              double utility = escapeResult.getDistance(rocketIntersection) +
                                      Math.sqrt(projectileOrigin.getDistance(escapeResult));
                              System.out.println("Escape " + direction + " with utility " + utility);
                              return utility;
                          }))
                          .get();
            Location toLocation = Navigation.navMeshRayCast(_bot.getNavMeshModule(), _bot.getInfo(), escapeDirection);


            // make actions
            _bot.getMove().turnTo(projectileOrigin);
            _bot.getMove().dodge(toLocation.sub(botLocation), true);
        }
        catch (Exception e){
            _bot.getLog().severe("Error avoiding rocket", e);
        }
    }
}
