package vip.bot.knowledge;

import cz.cuni.amis.pogamut.base.communication.worldview.object.WorldObjectId;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import java.util.*;
import vip.bot.KnowledgeBase;
import vip.bot.messages.TCGuarding;

public class GuardingKnowledge {

    private final UT2004BotVIPController _bot;
    private final KnowledgeBase _knowledge;
    private Set<UnrealId> _guards = new HashSet<>();

    public GuardingKnowledge(UT2004BotVIPController bot, KnowledgeBase knowledge) {
        _bot = bot;
        _knowledge = knowledge;
    }

    public void reset() {
        stop_guarding(_bot.getInfo().getId());
        _guards.clear();
    }


    public void update_guarding(TCGuarding event) {
        if (event.guarding) {
            this.start_guarding(event.playerId);
        }
        else {
            this.stop_guarding(event.playerId);
        }
    }

    public void start_guarding(UnrealId player) {
        this.start_guarding(player, false);
    }

    public void start_guarding(UnrealId player, boolean notifty) {
        _guards.add(player);
        if (notifty) {
            _bot.getTCClient()
                .sendToTeam(new TCGuarding(_bot.getInfo().getId(), true));
        }
    }

    public void stop_guarding(UnrealId player) {
        stop_guarding(player, false);
    }

    public void stop_guarding(UnrealId player, boolean notify) {
        _guards.remove(player);
        if (notify)
            _bot.getTCClient()
                .sendToTeam(new TCGuarding(_bot.getInfo().getId(), false));
    }

    public int numGuards(){
        return _guards.size();
    }

    public Location guard_place() {
        Location future_location = _knowledge.getVipKnowledge().getLocationInterpolation(2000);
        return future_location;
    }

    public boolean needs_to_guard() {
        return true; //_guards.size() < 2;
    }

    public boolean is_back_shield(){
        if(_guards.size() > 0) {
            UnrealId lowest = _guards.stream().min(Comparator.comparing(WorldObjectId::getStringId)).get();
            return _bot.getInfo()
                       .getId()
                       .equals(lowest);
        }
        return false;
    }
}
