package hlaa.duelbot.behavior;

import cz.cuni.amis.pathfinding.alg.astar.AStarResult;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import hlaa.duelbot.KnowledgeBase;
import hlaa.duelbot.utils.CoverIPFMavView;
import hlaa.duelbot.utils.Navigation;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class MedkitBehavior extends BaseBehavior {

    private static final double MAX_PATH_DIFF = 20.0;
    private final CoverIPFMavView _mapview;

    public MedkitBehavior(UT2004BotModuleController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
        _mapview = new CoverIPFMavView(knowledge, _bot.getVisibility());
    }
    public MedkitBehavior(UT2004BotModuleController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
        _mapview = new CoverIPFMavView(knowledge, _bot.getVisibility());
    }

    @Override
    public boolean isFiring() {
        return _bot.getInfo().getHealth() < 100;
    }

    @Override
    public void execute() {
        //my navpoint
        NavPoint myNavpoint = _bot.getNavPoints().getNavPoints()
                .values()
                .stream()
                .min(Comparator.comparingDouble(n -> n.getLocation().getDistance(_bot.getInfo().getLocation())))
                .get();

        //decide what to pickup
        Item toPickup = _bot.getItems()
                            .getSpawnedItems(UT2004ItemType.Category.HEALTH)
                            .values()
                            .stream()
                            .filter(i -> Navigation.canReachNavpoint(_bot.getNMNav(), _bot.getInfo().getLocation(), i.getLocation()))
                            .min(Comparator.comparingDouble(i ->
                                    i.getLocation().getDistance(_bot.getInfo().getLocation())
                            )).get();
        if(toPickup == null)
            return;


        AStarResult<NavPoint> path = _bot.getAStar().findPath(
                myNavpoint,
                toPickup.getNavPoint(),
                _mapview
        );

        //draw path
        if(true){
            _bot.getDraw().clearAll();
            List<NavPoint> p = Navigation.getPath(path);
            for (int i = 0; i < p.size() - 1; i++)
                _bot.getDraw().drawLine(Color.ORANGE, p.get(i).getLocation(), p.get(i + 1).getLocation());
        }

        //get rid of the first point if bot passed it
        List<NavPoint> p = Navigation.getPath(path);
        if(p.size() >= 2){
            double distanceToBoth = Navigation.directDistance(_bot, p.get(0)) + Navigation.directDistance(_bot, p.get(1));
            double distanceBetween = Navigation.directDistance(p.get(0), p.get(1));
            if(distanceToBoth < distanceBetween + MAX_PATH_DIFF){
                p.remove(0);
            }
        }

        // navigate
        Navigation.navigateThrough(_bot.getNMNav(), p);
    }

    @Override
    public void terminate() {
        _bot.getNavigation().stopNavigation();
    }
}
