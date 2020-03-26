package hlaa.duelbot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.utils.Cooldown;
import hlaa.duelbot.KnowledgeBase;
import hlaa.duelbot.utils.Inventory;
import hlaa.duelbot.utils.Navigation;
import java.util.*;
import java.util.stream.Collectors;

public class PickingBehavior extends BaseBehavior {

    private static final double MAX_PATH_DIFF = 1.1;
    private static final Map<ItemType, Double> WEAPON_WORTH = new HashMap<>();
    private static final double ROCKET_MINUS = 50.0;
    private static final double ROCKET_COVER_DISTANCE_MIN = 300.0;
    private static final double ROCKET_COVER_DISTANCE_MAX = 700.0;
    private final Cooldown _useRocketCoverFire = new Cooldown(4000);

    static {
        WEAPON_WORTH.put(UT2004ItemType.ASSAULT_RIFLE, 10.0);
        WEAPON_WORTH.put(UT2004ItemType.BIO_RIFLE, 40.0);
        WEAPON_WORTH.put(UT2004ItemType.LINK_GUN, 80.0);
        WEAPON_WORTH.put(UT2004ItemType.FLAK_CANNON, 180.0);
        WEAPON_WORTH.put(UT2004ItemType.MINIGUN, 120.0);
        WEAPON_WORTH.put(UT2004ItemType.SHOCK_RIFLE, 120.0);
        WEAPON_WORTH.put(UT2004ItemType.LIGHTNING_GUN, 200.0);
        WEAPON_WORTH.put(UT2004ItemType.SNIPER_RIFLE, 200.0);
        WEAPON_WORTH.put(UT2004ItemType.ROCKET_LAUNCHER, 1000.0);
        WEAPON_WORTH.put(UT2004ItemType.U_DAMAGE_PACK, 125.0);
        WEAPON_WORTH.put(UT2004ItemType.SUPER_SHIELD_PACK, 90.0);
        WEAPON_WORTH.put(UT2004ItemType.SHIELD_PACK, 75.0);
        WEAPON_WORTH.put(UT2004ItemType.SUPER_HEALTH_PACK, 50.0);
        WEAPON_WORTH.put(UT2004ItemType.HEALTH_PACK, 25.0);
        WEAPON_WORTH.put(UT2004ItemType.MINI_HEALTH_PACK, 5.0);
        WEAPON_WORTH.put(UT2004ItemType.ADRENALINE_PACK, 1.0);
    }

    public PickingBehavior(UT2004BotModuleController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
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
                                    .filter(item -> Inventory.needItem(_bot.getWeaponry(), _bot.getInfo(), item.getType()))
                                    .filter(item ->
                                        Navigation.canReachNavpoint(_bot.getNMNav(), _bot.getInfo().getLocation(), item.getLocation())
                                    ).collect(Collectors.toList());
        // first the target item
        Optional<Item> toPickup = toConsider.stream().max(Comparator.comparingDouble(
                i -> 1.0 / Math.sqrt(
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
            ).max(Comparator.comparingDouble(item -> Navigation.pathComposition(
                    _bot.getNMNav(), _bot.getInfo().getLocation(), item.getLocation(), finalCurrentTarget.getLocation()
            )));

            if (newTarget.isPresent()) {
                pathLength = Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo().getLocation(), newTarget.get().getLocation());
                currentTarget = newTarget.get();
                targetChanged = true;
            }
        }

        // navigate to item
        //_bot.getLog().info("Going to pick " + currentTarget.getType().getName() + " at " + currentTarget.getLocation());
        _bot.getNMNav().navigate(currentTarget.getLocation());

        // cover fire
        //System.out.println("Distance: " + Navigation.directDistance(_bot, currentTarget.getLocation()));
        //System.out.println("Can use: " + Inventory.canUseWeapon(_bot.getWeaponry(), UT2004ItemType.ROCKET_LAUNCHER));
        //System.out.println("Calldown: " + _useRocketCoverFire.getRemainingTime());
        if(Inventory.canUseWeapon(_bot.getWeaponry(), UT2004ItemType.ROCKET_LAUNCHER) &&
                _useRocketCoverFire.isCool() &&
                Navigation.directDistance(_bot, currentTarget.getLocation()) < ROCKET_COVER_DISTANCE_MAX &&
                Navigation.directDistance(_bot, currentTarget.getLocation()) > ROCKET_COVER_DISTANCE_MIN &&
                Navigation.canSee(_bot.getLevelGeometry(), _bot.getInfo().getLocation(), currentTarget.getLocation())
        ){
            //System.out.println("I shoot");
            _useRocketCoverFire.use();
            _bot.getShoot().shoot(
                    _bot.getWeaponry().getWeapon(UT2004ItemType.ROCKET_LAUNCHER),
                    true,
                    currentTarget.getLocation().sub(new Location(0,0,ROCKET_MINUS))
            );
        }
        else {
            _bot.getShoot().stopShooting();
        }
    }

    @Override
    public void terminate() {
        _bot.getNMNav().stopNavigation();
    }
}
