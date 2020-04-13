package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.utils.Navigation;
import java.util.Comparator;

public class RetreatBehavior extends BaseBehavior {

    private static final double EXSCAPE_POINTS_IN_RADIUS = 600.0;
    private boolean _alreadyJumped = false;

    public RetreatBehavior(UT2004BotModuleController bot) {
        super(bot);
    }
    public RetreatBehavior(UT2004BotModuleController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public RetreatBehavior(UT2004BotModuleController bot, double priority) {
        super(bot, priority);
    }

    @Override
    public boolean isFiring() {
        return _bot.getPlayers().getVisiblePlayers().size() > 0;
    }

    @Override
    public void execute() {
        Player opponent = _bot.getPlayers().getNearestVisiblePlayer();
        Location opponentLocation = opponent.getLocation();

        Comparator<NavPoint> coverPointsComparator = Comparator.comparingDouble(point ->
                Math.log(opponentLocation.getDistance(point.getLocation()))
                        /
                        (_bot.getInfo().getLocation().getDistance(point.getLocation()))
        );

        NavPoint toGo = null;
        if (_bot.getLevelGeometry() != null && _bot.getLevelGeometry().isLoaded()) {
            //_bot.getLog().info("Going to use level geometry to escape");
            // find closest non visible nav point
            toGo = _bot.getNavPoints()
                       .getNavPoints()
                       .values()
                       .stream()
                       .filter(point -> !Navigation.isVisible(_bot.getLevelGeometry(), opponentLocation, point.getLocation()))
                       .max(coverPointsComparator)
                       .get();
        }
        if (toGo == null && _bot.getVisibility().isInitialized()) { // use visibility matrix
            //_bot.getLog().info("Going to use visibility to escape");
            toGo = _bot.getVisibility()
                       .getCoverNavPointsFrom(opponentLocation)
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
                    .filter(p -> p.getLocation().getDistance(_bot.getInfo().getLocation()) < EXSCAPE_POINTS_IN_RADIUS)
                    .max(Comparator.comparingDouble(point ->
                        point.getLocation().getDistance(opponentLocation)
                    )).get();
        }

        // use shield
        _bot.getShoot().shoot(_bot.getWeaponry().getWeapon(UT2004ItemType.SHIELD_GUN), false, opponentLocation);
        _bot.getNavigation().setFocus(opponentLocation);

        // go awway
        if(!_alreadyJumped) {
            _bot.getMove().dodge(toGo.getLocation().sub(_bot.getInfo().getLocation()), true);
            _alreadyJumped = true;
        }
        else
            _bot.getNMNav().navigate(toGo);
    }

    @Override
    public void terminate() {
        _bot.getShoot().stopShooting();
        _bot.getNavigation().setFocus(null);
        _alreadyJumped = false;
    }
}
