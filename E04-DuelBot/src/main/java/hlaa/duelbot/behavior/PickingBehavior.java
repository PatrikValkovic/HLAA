package hlaa.duelbot.behavior;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import hlaa.duelbot.KnowledgeBase;
import hlaa.duelbot.utils.Inventory;
import hlaa.duelbot.utils.Navigation;
import java.util.*;
import java.util.stream.Collectors;

public class PickingBehavior extends BaseBehavior {

    private static final double MAX_PATH_DIFF = 1.1;
    private static final Map<ItemType, Double> WEAPON_WORTH = new HashMap<>();

    static {
        WEAPON_WORTH.put(UT2004ItemType.ASSAULT_RIFLE, 10.0);
        WEAPON_WORTH.put(UT2004ItemType.BIO_RIFLE, 40.0);
        WEAPON_WORTH.put(UT2004ItemType.LINK_GUN, 80.0);
        WEAPON_WORTH.put(UT2004ItemType.FLAK_CANNON, 180.0);
        WEAPON_WORTH.put(UT2004ItemType.MINIGUN, 120.0);
        WEAPON_WORTH.put(UT2004ItemType.SHOCK_RIFLE, 120.0);
        WEAPON_WORTH.put(UT2004ItemType.LIGHTNING_GUN, 200.0);
        WEAPON_WORTH.put(UT2004ItemType.SNIPER_RIFLE, 200.0);
        WEAPON_WORTH.put(UT2004ItemType.ROCKET_LAUNCHER, 100.0);
        WEAPON_WORTH.put(UT2004ItemType.U_DAMAGE_PACK, 125.0);
        WEAPON_WORTH.put(UT2004ItemType.SUPER_SHIELD_PACK, 75.0);
    }


    public PickingBehavior(UT2004BotModuleController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    @Override
    public boolean isFiring() {
        return true;
    }

    @Override
    public void execute() {
        // get items to consider
        List<Item> toConsider = _bot.getItems()
                                    .getSpawnedItems()
                                    .values()
                                    .stream()
                                    .filter(item -> WEAPON_WORTH.containsKey(item.getType()))
                                    .filter(item -> !Inventory.hasWeapon(_bot.getWeaponry(), item.getType()))
                                    .collect(Collectors.toList());
        // first the target item
        // TODO different priorities to short/mid/long range weapons based on the inventory?
        Optional<Item> toPickup = toConsider.stream().min(Comparator.comparingDouble(
                i -> Math.sqrt(
                        Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo().getLocation(), i.getLocation())
                ) * WEAPON_WORTH.get(i.getType())
        ));

        if (!toPickup.isPresent()) {
            return;
        }

        //_bot.getLog().info("Main item to pick: " + toPickup.get().getType().getName());

        // get items on the way
        //TODO distances between items can be computed before
        double pathLength = Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo().getLocation(), toPickup.get().getLocation());
        Item currentTarget = toPickup.get();
        boolean targetChanged = true;
        while (targetChanged) {
            targetChanged = false;
            Item finalCurrentTarget = currentTarget;
            double finalPathLength = pathLength;
            Optional<Item> newTarget = toConsider.stream().filter(item ->
                Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo().getLocation(), item.getLocation()) < finalPathLength
            ).filter(item ->
                    Navigation.pathComposition(_bot.getNMNav(), _bot.getInfo().getLocation(), item.getLocation(), finalCurrentTarget.getLocation()) < finalPathLength * MAX_PATH_DIFF
            ).min(Comparator.comparingDouble(item -> Navigation.pathComposition(
                    _bot.getNMNav(), _bot.getInfo().getLocation(), item.getLocation(), finalCurrentTarget.getLocation()
            )));

            if (newTarget.isPresent()) {
                pathLength = Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo().getLocation(), newTarget.get().getLocation());
                currentTarget = newTarget.get();
                targetChanged = true;
            }
        }

        // navigate to item
        //_bot.getLog().info("Going to pick " + currentTarget.getType().getName());
        _bot.getNMNav().navigate(currentTarget.getLocation());
    }

    @Override
    public double priority() {
        return 10.0f;
    }
}