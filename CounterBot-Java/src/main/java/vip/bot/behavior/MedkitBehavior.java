package vip.bot.behavior;

import cz.cuni.amis.pathfinding.alg.astar.AStarResult;
import cz.cuni.amis.pogamut.base.agent.navigation.impl.PrecomputedPathFuture;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import vip.bot.KnowledgeBase;
import vip.bot.utils.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static vip.bot.utils.DrawingColors.DRAW;
import static vip.bot.utils.DrawingColors.MEDKIT_PATH;

public class MedkitBehavior extends BaseBehavior {
    private final int EVERY_NAVPOINT = 5;
    private final CoverIPFMavView _mapview;
    private HandmadeNav _navigation;

    public MedkitBehavior(UT2004BotVIPController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
        _mapview = new CoverIPFMavView(knowledge, _bot.getVisibility());
    }

    public MedkitBehavior(UT2004BotVIPController bot, double priority, KnowledgeBase knowledge) {
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

    @Override
    public void execute() {
        //my navpoint
        NavPoint myNavpoint = Navigation.getClosestNavpoint(_bot, _bot.getNavPoints());

        //decide what to pickup
        Item toPickup = _knowledge.getItemSpawnedKnowledge()
                                  .getSpawnedItems()
                                  .stream()
                                  .filter(i -> Inventory.isHealth(i.getType()))
                                  .filter(i -> Navigation.canReachNavpoint(_bot.getNMNav(), _bot.getInfo().getLocation(), i.getLocation()))
                                  .min(Comparator.comparingDouble(i ->
                                          Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo(), i)
                                  )).orElse(null);

        if (toPickup == null)
            return;
        if (skipCondition(toPickup) && !DRAW){
            _navigation.navigate(_bot);
            return;
        }

        // find path
        AStarResult<NavPoint> aStarResult = _bot.getAStar().findPath(
                myNavpoint,
                toPickup.getNavPoint(),
                _mapview
        );

        //transform it to path
        List<NavPoint> aStarPath = Navigation.getPath(aStarResult);
        if(aStarPath == null){
           _bot.getLog().warning("No path returned for medkit");
                return;
        }

        // use only some navpoints
        List<ILocated> navigateThrough = IntStream.range(1, aStarPath.size())
                                                  .filter(i -> i % EVERY_NAVPOINT == 0)
                                                  .mapToObj(aStarPath::get)
                                                  .collect(Collectors.toCollection(ArrayList::new));
        navigateThrough.add(toPickup); // include last navpoint

        // find the real path
        PrecomputedPathFuture<ILocated> realPath = Navigation.pathThrough(_bot.getNMNav(), navigateThrough, _bot.getInfo());

        //draw path
        if (DrawingColors.DRAW) {
            List<ILocated> tmp = realPath.get();
            for (int i = 1; i < tmp.size(); i++){
                _bot.getDraw().drawLine(MEDKIT_PATH, tmp.get(i-1), tmp.get(i));
            }
        }

        if(skipCondition(toPickup)){
            _navigation.navigate(_bot);
            return;
        }

        // navigate
        _navigation = new HandmadeNav(realPath.get());
        _navigation.navigate(_bot);
    }

    private boolean skipCondition(Item toPickup) {
        return _navigation != null && _navigation.getTargetLocation().getLocation().equals(toPickup.getLocation()) && !_navigation.isDone();
    }

    @Override
    public void terminate() {
        _navigation = null;
        _bot.getNavigation().stopNavigation();
    }
}
