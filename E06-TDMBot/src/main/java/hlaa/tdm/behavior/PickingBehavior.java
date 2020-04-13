package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.messages.TCGoingToPick;
import hlaa.tdm.utils.Inventory;
import hlaa.tdm.utils.Navigation;
import java.util.*;
import java.util.stream.Collectors;

public class PickingBehavior extends BaseBehavior {

    private static final double MAX_PATH_DIFF = 1.1;
    private static final double POWER_BASE = 0.999;
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
        WEAPON_WORTH.put(UT2004ItemType.ROCKET_LAUNCHER, 90.0);
        WEAPON_WORTH.put(UT2004ItemType.U_DAMAGE_PACK, 125.0);
        WEAPON_WORTH.put(UT2004ItemType.SUPER_SHIELD_PACK, 90.0);
        WEAPON_WORTH.put(UT2004ItemType.SHIELD_PACK, 75.0);
        WEAPON_WORTH.put(UT2004ItemType.SUPER_HEALTH_PACK, 50.0);
        WEAPON_WORTH.put(UT2004ItemType.HEALTH_PACK, 25.0);
        WEAPON_WORTH.put(UT2004ItemType.MINI_HEALTH_PACK, 5.0);
        WEAPON_WORTH.put(UT2004ItemType.ADRENALINE_PACK, 1.0);

        WEAPON_WORTH.put(UT2004ItemType.ROCKET_LAUNCHER_AMMO, 9.0);
        WEAPON_WORTH.put(UT2004ItemType.MINIGUN_AMMO, 12.0);
        WEAPON_WORTH.put(UT2004ItemType.ASSAULT_RIFLE_AMMO, 1.0);
        WEAPON_WORTH.put(UT2004ItemType.LINK_GUN_AMMO, 8.0);
        WEAPON_WORTH.put(UT2004ItemType.LIGHTNING_GUN_AMMO, 20.0);
        WEAPON_WORTH.put(UT2004ItemType.SHOCK_RIFLE_AMMO, 12.0);
        WEAPON_WORTH.put(UT2004ItemType.SNIPER_RIFLE_AMMO, 20.0);
        WEAPON_WORTH.put(UT2004ItemType.FLAK_CANNON_AMMO, 18.0);
        WEAPON_WORTH.put(UT2004ItemType.BIO_RIFLE_AMMO, 4.0);
        WEAPON_WORTH.put(UT2004ItemType.SHIELD_GUN_AMMO, 0.5);
        WEAPON_WORTH.put(UT2004ItemType.REDEEMER_AMMO, 0.0);
        WEAPON_WORTH.put(UT2004ItemType.ION_PAINTER_AMMO, 0.0);
        WEAPON_WORTH.put(UT2004ItemType.ONS_MINE_LAYER_AMMO, 0.0);
        WEAPON_WORTH.put(UT2004ItemType.ONS_GRENADE_LAUNCHER_AMMO, 0.0);
        WEAPON_WORTH.put(UT2004ItemType.ONS_AVRIL_AMMO, 0.0);
    }

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
                                                            )) * WEAPON_WORTH.get(i.getType())
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
