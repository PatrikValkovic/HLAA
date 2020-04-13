package hlaa.tdm.behavior;

import cz.cuni.amis.pathfinding.alg.astar.AStarResult;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.utils.CoverIPFMavView;
import hlaa.tdm.utils.Inventory;
import hlaa.tdm.utils.Navigation;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class MedkitBehavior extends BaseBehavior {

    private static final double MAX_PATH_DIFF = 150.0;
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
        return _bot.getInfo().getHealth() < 100 && _knowledge.getItemSpawnedKnowledge()
                                                             .getSpawnedItems()
                                                             .stream()
                                                             .anyMatch(i -> Inventory.isHealth(i.getType()));
    }

    private void getRidOfPoint(List<NavPoint> p, int index1, int index2){
        if (p.size() >= Math.max(index1, index2)+1) {
            double distanceToBoth = Navigation.directDistance(_bot, p.get(index1)) + Navigation.directDistance(_bot, p.get(index2));
            double distanceBetween = Navigation.directDistance(p.get(index1), p.get(index2));
            if (distanceToBoth < distanceBetween + MAX_PATH_DIFF) {
                p.remove(index1);
                //System.out.println("Getting rid at index " + index1 + " of " + p.get(index1));
            }
        }
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
        Item toPickup = _knowledge.getItemSpawnedKnowledge()
                                  .getSpawnedItems()
                                  .stream()
                                  .filter(i -> Navigation.canReachNavpoint(_bot.getNMNav(), _bot.getInfo().getLocation(), i.getLocation()))
                                  .min(Comparator.comparingDouble(i ->
                                          i.getLocation().getDistance(_bot.getInfo().getLocation())
                                  )).orElse(null);
        if (toPickup == null)
            return;


        AStarResult<NavPoint> path = _bot.getAStar().findPath(
                myNavpoint,
                toPickup.getNavPoint(),
                _mapview
        );

        //get rid of position if passed
        List<NavPoint> p = Navigation.getPath(path);
        if(p == null)
            return;
        getRidOfPoint(p, 0, 1);
        getRidOfPoint(p, 1, 2);
        getRidOfPoint(p, 0, 1);

        //draw path
        if (true) {
            for (int i = 0; i < p.size() - 1; i++){
                _bot.getDraw().drawLine(Color.ORANGE, p.get(i).getLocation(), p.get(i + 1).getLocation());
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
