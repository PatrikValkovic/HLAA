package hlaa.tdm.knowledge;

import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import hlaa.tdm.messages.TCAllyLocation;
import hlaa.tdm.utils.Navigation;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class AllyPositionsKnowledge {

    @AllArgsConstructor
    private static class Helper {
        @Getter
        Player player;
        @Getter
        Location location;
    }

    private final UT2004BotTCController _bot;
    private final Map<UnrealId, Helper> _positions = new HashMap<>();

    public AllyPositionsKnowledge(UT2004BotTCController bot) {
        _bot = bot;
    }

    public void update() {
        _bot.getTCClient().sendToTeam(new TCAllyLocation(_bot.getInfo().getId(), _bot.getInfo().getLocation()));
    }

    public void reset() {
        _positions.clear();
    }

    public void setPosition(UnrealId id, Location location) {
        _positions.put(id, new Helper(_bot.getPlayers().getPlayer(id), location));
    }

    public Location closestAlly(ILocated from) {
        if (_positions.size() == 0)
            return from.getLocation();

        return Navigation.getClosest(
                _bot.getNMNav(),
                from,
                _positions.values().stream().map(Helper::getLocation).collect(Collectors.toList())
        );
    }
}
