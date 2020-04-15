package hlaa.tdm.behavior;

import cz.cuni.amis.pathfinding.alg.astar.AStarResult;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.utils.CoverIPFMavView;
import hlaa.tdm.utils.Inventory;
import hlaa.tdm.utils.Navigation;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static hlaa.tdm.utils.DrawingColors.MEDKIT_PATH;

public class MedkitBehavior extends BaseBehavior {
    private static final boolean DRAW = true;
    private final int EVERY_NAVPOINT = 4;
    private final CoverIPFMavView _mapview;

    public MedkitBehavior(UT2004BotTCController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
        _mapview = new CoverIPFMavView(knowledge, _bot.getVisibility());
    }

    public MedkitBehavior(UT2004BotTCController bot, double priority, KnowledgeBase knowledge) {
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

    private boolean executeTerminationCondition(Item item) {
        try {
            return _bot.getNMNav().isNavigating() &&
                    _bot.getNMNav().getPathExecutor().getPathTo() != null &&
                    _bot.getNMNav().getPathExecutor().getPathTo().getLocation().equals(item.getLocation());
        }
        catch(Exception ignore){}
        return false;
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

        if (this.executeTerminationCondition(toPickup) && !DRAW)
            return;

        // find path
        AStarResult<NavPoint> path = _bot.getAStar().findPath(
                myNavpoint,
                toPickup.getNavPoint(),
                _mapview
        );

        //transform it to path
        List<NavPoint> p = Navigation.getPath(path);
        if(p == null){
           _bot.getLog().warning("No path returned from medkit");
                return;
        }

        // use only some navpoints
        List<ILocated> navigateThrough = IntStream.range(1, p.size())
                                                  .filter(i -> i % EVERY_NAVPOINT == 0)
                                                  .mapToObj(p::get)
                                                  .collect(Collectors.toCollection(ArrayList::new));
        navigateThrough.add(toPickup); // include last navpoint

        // find the real path
        IPathFuture<ILocated> realPath = Navigation.pathThrough(_bot.getNMNav(), navigateThrough, _bot.getInfo());

        //draw path
        if (true) {
            List<ILocated> tmp = realPath.get();
            for (int i = 1; i < tmp.size(); i++){
                _bot.getDraw().drawLine(MEDKIT_PATH, tmp.get(i-1), tmp.get(i));
            }
        }

        if(this.executeTerminationCondition(toPickup))
            return;

        // navigate
        _bot.getNMNav().getPathExecutor().followPath(realPath);
    }

    @Override
    public void terminate() {
        _bot.getNavigation().stopNavigation();
    }
}
