package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import hlaa.tdm.KnowledgeBase;
import hlaa.tdm.utils.Navigation;

public class FollowingBehaviour extends BaseBehavior {

    public FollowingBehaviour(UT2004BotModuleController bot) {
        super(bot);
    }

    public FollowingBehaviour(UT2004BotModuleController bot, double priority) {
        super(bot, priority);
    }

    public FollowingBehaviour(UT2004BotModuleController bot, KnowledgeBase knowledge) {
        super(bot, knowledge);
    }

    public FollowingBehaviour(UT2004BotModuleController bot, double priority, KnowledgeBase knowledge) {
        super(bot, priority, knowledge);
    }

    @Override
    public boolean isFiring() {
        return _bot.getPlayers().canSeePlayers() && Navigation.directDistance(_bot, _bot.getPlayers().getNearestVisiblePlayer().getLocation()) > 200;
    }

    @Override
    public void execute() {
        _bot.getNMNav().navigate(_bot.getPlayers().getNearestVisiblePlayer().getLocation());
    }

    @Override
    public void terminate() {
        _bot.getNMNav().stopNavigation();
    }
}
