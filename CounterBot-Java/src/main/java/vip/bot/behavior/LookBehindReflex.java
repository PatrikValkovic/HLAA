package vip.bot.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import cz.cuni.amis.utils.Cooldown;
import java.util.concurrent.TimeUnit;

public class LookBehindReflex extends BaseReflex {

    private static final Cooldown _lookBehindCooldown = new Cooldown(15, TimeUnit.SECONDS);
    private static Location _toLook = null;

    public LookBehindReflex(UT2004BotVIPController bot) {
        super(bot);
    }

    @Override
    public boolean triggered() {
        return _bot.getPlayers().getVisiblePlayers().size() == 0 && (_lookBehindCooldown.isCool() || _toLook != null);
    }

    @Override
    public void execute() {
        if(_lookBehindCooldown.tryUse()) {
            _bot.getLog().info("Look behind");
            _toLook = _bot.getInfo().getLocation().sub(_bot.getInfo().getRotation().toLocation().scale(1000));
        }
        _bot.getNavigation().setFocus(_toLook);
        if(_bot.getInfo().isFacing(_toLook, 35)){
            _toLook = null;
            _bot.getNavigation().setFocus(null);
        }
    }
}
