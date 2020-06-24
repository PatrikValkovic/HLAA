package vip.bot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import cz.cuni.amis.utils.Cooldown;
import vip.bot.KnowledgeBase;
import vip.bot.utils.Inventory;
import vip.bot.utils.Navigation;
import vip.bot.utils.WeaponPrefs;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RetreatBehavior extends BaseBehavior {

    private static final double EXSCAPE_POINTS_IN_RADIUS = 600.0;
    private final Cooldown  _jumpCooldown = new Cooldown(6000);

    public RetreatBehavior(UT2004BotVIPController bot) {
        super(bot);
    }
    public RetreatBehavior(UT2004BotVIPController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public RetreatBehavior(UT2004BotVIPController bot, double priority) {
        super(bot, priority);
    }

    @Override
    public boolean isFiring() {
        return _bot.getPlayers().getVisibleEnemies().size() > 0;
    }

    @Override
    public void execute() {
        Player nearestEnemy = _bot.getPlayers().getNearestVisibleEnemy();
        List<Player> opponents = new ArrayList<>(_bot.getPlayers().getVisibleEnemies().values());
        List<Location> opponentLocations = opponents.stream().map(Player::getLocation).collect(Collectors.toList());
        Location myLoc = _bot.getInfo().getLocation();
        NavPoint myNavpoint = Navigation.getClosestNavpoint(_bot, _bot.getNavPoints());

        Comparator<NavPoint> coverPointsComparator = Comparator.comparingDouble(point ->
                Math.log(opponentLocations.stream()
                                .mapToDouble(l -> l.getDistance(point.getLocation()))
                                .sum() + 1.05)
        );

        NavPoint toGo = null;
        if (_bot.getLevelGeometry() != null && _bot.getLevelGeometry().isLoaded()) {
            //_bot.getLog().info("Going to use level geometry to escape");
            // find closest non visible nav point
            toGo = _bot.getNavPoints()
                       .getNavPoints()
                       .values()
                       .stream()
                       .filter(point ->
                               opponentLocations.stream()
                                                .noneMatch(l -> Navigation.isVisible(_bot.getLevelGeometry(), l, point.getLocation()))
                       )
                       .max(coverPointsComparator)
                       .get();
        }
        if (toGo == null && _bot.getVisibility().isInitialized()) { // use visibility matrix
            //_bot.getLog().info("Going to use visibility to escape");
            toGo = _bot.getVisibility()
                       .getCoverNavPointsFromN(opponentLocations.toArray(new Location[0]))
                       .stream()
                       .max(coverPointsComparator)
                       .get();
        }
        if(toGo == null) { // just go away
            //_bot.getLog().info("Going to use only navpoints to escape");
            toGo = _bot.getNavPoints()
                    .getNavPoints()
                    .values()
                    .stream()
                    .filter(p -> Navigation.directDistance(_bot.getInfo(), p) < EXSCAPE_POINTS_IN_RADIUS)
                    .max(coverPointsComparator)
                    .get();
        }

        // use shield if available
        _bot.getNavigation().setFocus(_bot.getPlayers().getNearestVisibleEnemy());
        if(_bot.getWeaponry().getSecondaryWeaponAmmo(UT2004ItemType.SHIELD_GUN) > 25){
            _bot.getShoot().shoot(_bot.getWeaponry().getWeapon(UT2004ItemType.SHIELD_GUN), false, nearestEnemy);
        }
        // else use rocket launcher
        else if(_bot.getWeaponry().hasWeapon(UT2004ItemType.ROCKET_LAUNCHER) && _bot.getWeaponry().hasWeaponAmmo(UT2004ItemType.ROCKET_LAUNCHER)){
            _bot.getShoot().shoot(_bot.getWeaponry().getWeapon(UT2004ItemType.ROCKET_LAUNCHER), true, nearestEnemy);
        }
        // else any weapon
        else {
            WeaponPrefs.WeaponPref pref = Inventory.bestWeaponForDistance(_bot.getWeaponry(), WeaponPrefs.WEAPON_PREFS, Navigation.directDistance(_bot, nearestEnemy));
            _bot.getShoot().shoot(_bot.getWeaponry().getWeapon(pref.getWeapon()), true, nearestEnemy);
        }

        // go away
        if(_jumpCooldown.tryUse()) {
            _bot.getMove().dodge(toGo.getLocation().sub(_bot.getInfo().getLocation()), true);
        }
        else
            _bot.getNMNav().navigate(toGo);
    }

    @Override
    public void terminate() {
        _bot.getShoot().stopShooting();
        _bot.getNavigation().setFocus(null);
    }
}
