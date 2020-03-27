package hlaa.duelbot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshClearanceComputer;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import hlaa.duelbot.KnowledgeBase;
import hlaa.duelbot.utils.Inventory;
import hlaa.duelbot.utils.Navigation;
import java.awt.*;
import math.geom2d.Vector2D;

public class CombatMovementBehaviour extends BaseBehavior {

    public CombatMovementBehaviour(UT2004BotModuleController bot) {
        super(bot);
    }
    public CombatMovementBehaviour(UT2004BotModuleController bot, double priority) {
        super(bot, priority);
    }
    public CombatMovementBehaviour(UT2004BotModuleController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public CombatMovementBehaviour(UT2004BotModuleController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    @Override
    public boolean isFiring() {
        return _bot.getPlayers().canSeePlayers();
    }

    @Override
    public void execute() {
        Player enemy = _bot.getPlayers().getNearestVisiblePlayer();
        if(enemy == null)
            return;

        double distance = Navigation.directDistance(_bot, enemy);
        double optimal = Inventory.getOptimalDistance(_bot.getWeaponry(), distance, 500.0);

        // define directions
        Vector2D[] direction = new Vector2D[]{
                new Vector2D(1, 0),
                new Vector2D(0, 1),
                new Vector2D(-1, 0),
                new Vector2D(0, -1),
                new Vector2D(1, 1).getNormalizedVector(),
                new Vector2D(-1, 1).getNormalizedVector(),
                new Vector2D(1, -1).getNormalizedVector(),
                new Vector2D(-1, -1).getNormalizedVector()
        };

        // decide direction
        double bestUtility = Double.NEGATIVE_INFINITY;
        Location toLoc = _bot.getInfo().getLocation();
        NavMeshClearanceComputer clearanceComputer = _bot.getNavMeshModule().getClearanceComputer();
        for (Vector2D escapeDirection : direction) {
            NavMeshClearanceComputer.ClearanceLimit limit = clearanceComputer.findEdge(_bot.getInfo().getLocation(), escapeDirection);
            double utility = 0;
            utility = Math.abs(Navigation.directDistance(enemy, limit.getLocation()) - optimal);
            utility = Math.min(utility, 300.0);

            if (utility > bestUtility) {
                bestUtility = utility;
                toLoc = new Location(escapeDirection.getX(), escapeDirection.getY(), 0.0f);
                toLoc = _bot.getInfo().getLocation().add(toLoc.getNormalized().scale(10.0));
            }
        }

        _bot.getLog().info("Ideal distance from player is " + toLoc);
        _bot.getDraw().drawLine(Color.WHITE, _bot.getInfo(), toLoc);
        _bot.getMove().dodge(_bot.getInfo().getLocation().sub(toLoc), false);
    }
}
