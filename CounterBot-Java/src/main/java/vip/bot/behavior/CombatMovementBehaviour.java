package vip.bot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import vip.bot.KnowledgeBase;
import vip.bot.utils.DrawingColors;
import vip.bot.utils.Inventory;
import vip.bot.utils.Navigation;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import math.geom2d.Vector2D;

public class CombatMovementBehaviour extends BaseBehavior {

    public CombatMovementBehaviour(UT2004BotVIPController bot) {
        super(bot);
    }
    public CombatMovementBehaviour(UT2004BotVIPController bot, double priority) {
        super(bot, priority);
    }
    public CombatMovementBehaviour(UT2004BotVIPController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public CombatMovementBehaviour(UT2004BotVIPController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    @Override
    public boolean isFiring() {
        return _bot.getPlayers().canSeePlayers();
    }

    @Override
    public void execute() {
        Player enemy = _bot.getPlayers().getNearestVisibleEnemy();
        if(enemy == null)
            return;

        double distance = Navigation.directDistance(_bot, enemy);
        double optimal = Inventory.getOptimalDistance(_bot.getWeaponry(), distance, 500.0);
        Location enemyLocation = enemy.getLocation();
        Location directionToEnemy = _bot.getInfo().getLocation().sub(enemyLocation).getNormalized();

        // define directions
        Set<Vector2D> directions = Navigation.eightDirections(new Vector2D(directionToEnemy.x, directionToEnemy.y));

        // decide direction
        Optional<Location> optLocation =
            directions.stream()
                      .map(dir -> {
                          Location collisionLocation = Navigation.navMeshRayCast(_bot.getNavMeshModule(), _bot.getInfo(), dir);
                          Location direction = collisionLocation.sub(_bot.getInfo().getLocation()).getNormalized();
                          double multiplier = Math.min(400, _bot.getInfo().getLocation().getDistance(collisionLocation));
                          Location endingLocation = _bot.getInfo().getLocation().add(direction.scale(multiplier));
                          return endingLocation;
                      })
                      .filter(loc -> Navigation.isVisible(_bot.getLevelGeometry(), _bot.getInfo().getLocation(), loc))
                      .min(Comparator.comparingDouble(loc -> {
                          return Math.abs(Navigation.directDistance(enemy, loc) - optimal);
                      }));

        if(!optLocation.isPresent())
            return;

        Location loc = optLocation.get();
        if (DrawingColors.DRAW) {
            _bot.getLog().info("Combat move to " + loc);
            _bot.getDraw().drawLine(DrawingColors.COMBAT_MOVEMENT, _bot.getInfo(), loc);
            _bot.getMove().dodge(loc.sub(_bot.getInfo().getLocation()), false);
        }
    }
}
