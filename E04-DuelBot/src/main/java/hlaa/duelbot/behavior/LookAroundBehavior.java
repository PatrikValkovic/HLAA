package hlaa.duelbot.behavior;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;

public class LookAroundBehavior extends BaseBehavior {

    private int _state = 0;

    public LookAroundBehavior(UT2004BotModuleController bot) {
        super(bot);
    }
    public LookAroundBehavior(UT2004BotModuleController bot, double priority) {
        super(bot, priority);
    }

    @Override
    public boolean isFiring() {
        return true;
    }

    @Override
    public void execute() {
        if(_state == 0){ // turn around
            if(_bot.getPlayers().getVisiblePlayers().size() > 0) {
                _state = 1;
            }
            else {
                _bot.getMove().turnHorizontal(30);
            }
        }
        if(_state == 1){ // focus on player
            if(_bot.getPlayers().getVisiblePlayers().size() == 0){
                _state = 0;
                return;
            }
            _bot.getMove().turnTo(_bot.getPlayers().getNearestVisiblePlayer());
        }
    }
}
