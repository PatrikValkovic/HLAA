package vip.bot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import vip.bot.KnowledgeBase;
import vip.bot.messages.TCWantToLock;
import vip.bot.utils.LockLocations;
import vip.bot.utils.Navigation;
import java.util.List;

public class LockingBehavior extends BaseBehavior {

    private static final double REACHED_DISTANCE = 150;

    public LockingBehavior(UT2004BotVIPController bot) {
        super(bot);
    }
    public LockingBehavior(UT2004BotVIPController bot, double priority) {
        super(bot, priority);
    }
    public LockingBehavior(UT2004BotVIPController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }
    public LockingBehavior(UT2004BotVIPController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    private int _lockLocation = -1;
    private int _currentTarget = -1;

    @Override
    public boolean isFiring() {
        return true;
    }

    @Override
    public void execute() {

        // get lock location
        int lockLocationIndex = _knowledge.getLockingKnowledge().getLockLocation(_bot.getInfo(), _bot.getGame(), _bot.getNMNav());
        List<Location> lockLocationPoints = LockLocations.getLockLocations(_bot.getGame()).get(lockLocationIndex);

        // check if should change
        if(_lockLocation != lockLocationIndex){
            _lockLocation = lockLocationIndex;
            Location closest = Navigation.getClosest(_bot.getNMNav(), _bot.getInfo(), lockLocationPoints);
            _currentTarget = lockLocationPoints.indexOf(closest);
        }

        // inform rest
        _bot.getTCClient().sendToTeam(new TCWantToLock(_bot.getInfo().getId(), _lockLocation));

        // reached target location
        if(Navigation.directDistance(_bot.getInfo(), lockLocationPoints.get(_currentTarget)) < REACHED_DISTANCE){
            _currentTarget = (_currentTarget + 1) % lockLocationPoints.size();
        }

        // navigate to location
        _bot.getNMNav().navigate(lockLocationPoints.get(_currentTarget));
    }

    @Override
    public void terminate() {
        _lockLocation = -1;
        _currentTarget = -1;
        _bot.getNavigation().stopNavigation();
    }
}
