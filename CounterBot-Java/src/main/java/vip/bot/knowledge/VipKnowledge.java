package vip.bot.knowledge;

import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.Velocity;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import lombok.Getter;
import lombok.Setter;
import vip.tc.msgs.TCPlayerUpdate;

public class VipKnowledge {

    private final UT2004BotVIPController _bot;
    private TCPlayerUpdate _player;
    @Getter
    @Setter
    private ILocated nextLocation;

    public VipKnowledge(UT2004BotVIPController bot) {
        _bot = bot;
    }

    public boolean isRunning() {
        return _bot.getVip().isRoundRunning();
    }

    public void reset() {
        _player = null;
    }

    public NavPoint safeNavpoint() {
        return _bot.getVip().getVIPSafeAreaNavPoint();
    }


    public void updatePlayer(TCPlayerUpdate event){
        _player = event;
    }

    public Location getLocation(){
        if(_player != null){
            return _player.location;
        }
        return new Location(0,0,0);
    }

    public Velocity getDirection() {
        if(_player != null) {
            return _player.velocity;
        }
        return new Velocity(0,0,0);
    }

    public Location getLocationInterpolation(int in_ms){
        return this.getLocation().add(this.getDirection().scale((double)in_ms / 1000.0));
    }
}
