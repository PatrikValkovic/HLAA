package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.messages.TCGoingToPick;
import hlaa.tdm.utils.Inventory;
import hlaa.tdm.utils.Navigation;
import hlaa.tdm.utils.WeaponPickingValues;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PickingBehavior extends BaseBehavior {

    private static final double MAX_PATH_DIFF = 1.1;
    private static final double POWER_BASE = 0.999;

    public PickingBehavior(UT2004BotTCController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public PickingBehavior(UT2004BotTCController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    @Override
    public boolean isFiring() {
        return itemsToPickup().size() > 0;
    }

    private List<Item> itemsToPickup() {
        return _knowledge.getOtherPickingKnowledge()
                         .itemsToPick()
                         .stream()
                         .filter(item -> Inventory.shouldPickup(_bot.getWeaponry(), _bot.getInfo(), item.getType()))
                         .filter(item ->
                                 Navigation.canReachNavpoint(_bot.getNMNav(), _bot.getInfo().getLocation(), item.getLocation())
                         ).collect(Collectors.toList());
    }

    @Override
    public void execute() {
        // get items to consider
        List<Item> toConsider = itemsToPickup();

        // first the target item
        Optional<Item> toPickup = toConsider.stream()
                                            .max(Comparator.comparingDouble(
                                                    i -> Math.pow(
                                                            POWER_BASE, Navigation.distanceBetween(
                                                                    _bot.getNMNav(),
                                                                    _bot.getInfo(),
                                                                    i
                                                            )) * WeaponPickingValues.WEAPON_WORTH.get(i.getType())
                                            ));

        if (!toPickup.isPresent()) {
            return;
        }

        _bot.getLog().info("Main item to pick: " + toPickup.get().getType().getName());


        // get items on the way
        double pathLength = Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo(), toPickup.get());
        Item currentTarget = toPickup.get();
        boolean targetChanged = true;
        while (targetChanged) {
            targetChanged = false;
            Item finalCurrentTarget = currentTarget;
            double finalPathLength = pathLength;

            Optional<Item> newTarget = toConsider.stream()
                                                 .filter(item ->
                                                         Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo(), item) < finalPathLength
                                                 )
                                                 .filter(item ->
                                                         Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo(), item.getLocation()) + _knowledge.getItemDistancesKnowledge().getDistanceBetween(item, finalCurrentTarget) < finalPathLength * MAX_PATH_DIFF
                                                 )
                                                 .min(Comparator.comparingDouble(item ->
                                                         Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo(), item.getLocation()) + _knowledge.getItemDistancesKnowledge().getDistanceBetween(item, finalCurrentTarget)
                                                 ));

            if (newTarget.isPresent()) {
                pathLength = Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo(), newTarget.get());
                currentTarget = newTarget.get();
                targetChanged = true;
            }
        }

        // navigate to item
        _bot.getTCClient().sendToTeam(new TCGoingToPick(toPickup.get().getId(), pathLength, _bot.getInfo().getId()));
        _bot.getNMNav().navigate(toPickup.get());
    }

    @Override
    public void terminate() {
        _bot.getNMNav().stopNavigation();
    }
}
