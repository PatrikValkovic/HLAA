package vip.bot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.UT2004AcceleratedPathExecutor;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import vip.bot.utils.DrawingColors;
import java.util.List;
import static vip.bot.utils.DrawingColors.NAVIGATION_PATH;

public class DodgeReflex extends BaseReflex {

    private static final double MIN_DODGE_DISTANCE = 500.0;
    private static final double MAX_HEIGHT_DIFFERENCE = 40.0;

    public DodgeReflex(UT2004BotVIPController bot) {
        super(bot);
    }

    @Override
    public boolean triggered() {
        return _bot.getNMNav() != null &&
                _bot.getNMNav().getCurrentPathDirect() != null &&
                _bot.getNMNav().getCurrentPathDirect().size() > 0;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void execute() {
        //disable for this one fucking place that always cause it to jump away, as the bot probably dont know how to aim.....
        if(_bot.getInfo().getLocation().getDistance(new Location(1000,1500,-200)) < 300)
            return;

        try {
            UT2004AcceleratedPathExecutor executor = (UT2004AcceleratedPathExecutor) _bot.getNMNav().getPathExecutor();
            List path = executor.getPath();
            if(path == null)
                return;

            int index = Math.max(executor.getPathElementIndex(), 0);
            if (index >= path.size()) {
                return;
            }

            ILocated dodgeTo = (ILocated)path.get(index);

            if (DrawingColors.DRAW) {
                _bot.getDraw().drawCube(DrawingColors.NEXT_NAVIGATION_POINT, dodgeTo, 15.0);
                //_bot.getLog().info("Height difference " + Math.abs(dodgeTo.getLocation().z - _bot.getInfo().getLocation().z));

                ILocated lastLoc = _bot.getInfo().getLocation();
                for (ILocated l : _bot.getNMNav().getCurrentPathDirect()) {
                    _bot.getDraw().drawLine(NAVIGATION_PATH, lastLoc, l);
                    lastLoc = l;
                }
            }

            double heightDistance = Math.abs(dodgeTo.getLocation().z - _bot.getInfo().getLocation().z);
            double distance = _bot.getInfo().getLocation().getDistance(dodgeTo.getLocation());

            if (distance > MIN_DODGE_DISTANCE && heightDistance < MAX_HEIGHT_DIFFERENCE) {
                //_bot.getLog().info("Dodging to " + dodgeTo.getLocation());
                _bot.getMove().dodgeTo(dodgeTo, true);
            }
        }
        catch(ClassCastException e){
            _bot.getLog().warning("Bot is not using UT2004AcceleratedPathExecutor", e);
        }
    }
}
