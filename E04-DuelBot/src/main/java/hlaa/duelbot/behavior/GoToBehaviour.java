package hlaa.duelbot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import hlaa.duelbot.utils.Navigation;

public class GoToBehaviour extends BaseBehavior {

    private final static double REACH_DISTANCE = 150.0;
    private final Location _where;

    public GoToBehaviour(UT2004BotModuleController bot, Location where) {
        super(bot);
        _where = where;
    }
    public GoToBehaviour(UT2004BotModuleController bot, double priority, Location where) {
        super(bot, priority);
        _where = where;
    }

    @Override
    public boolean isFiring() {
        return Navigation.directDistance(_bot, _where) > REACH_DISTANCE;
    }

    @Override
    public void execute() {
        _bot.getNMNav().navigate(_where);
    }

    @Override
    public void terminate() {
        _bot.getNavigation().stopNavigation();
    }
}
