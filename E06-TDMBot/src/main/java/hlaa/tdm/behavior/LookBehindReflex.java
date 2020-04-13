package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import hlaa.tdm.utils.DeltaCounter;

public class LookBehindReflex extends BaseReflex {

    private static final double LOOK_BEHIND_EVERY_S = 5.0;
    private final DeltaCounter _deltaCounter = new DeltaCounter();
    private double _delta = 0;

    public LookBehindReflex(UT2004BotModuleController bot) {
        super(bot);
    }

    @Override
    public boolean triggered() {
        _delta += _deltaCounter.getDelta();
        return _bot.getPlayers().getVisiblePlayers().size() == 0 && _delta > LOOK_BEHIND_EVERY_S;
    }

    @Override
    public void execute() {
        _bot.getLog().info("Look behind");
        _bot.getMove().turnHorizontal(180);
        _delta -= LOOK_BEHIND_EVERY_S;
    }
}
