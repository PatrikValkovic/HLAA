package vip.bot.knowledge;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.pathfollowing.NavMeshNavigation;
import vip.bot.utils.LockLocations;
import vip.bot.utils.Navigation;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class LockingKnowledge {

    private static final double VALID_FOR_MS = 600;

    @AllArgsConstructor
    private static class Helper {
        @Getter
        UnrealId player;
        @Getter
        int region;
        @Getter
        Instant when;
    }

    private final AllyPositionsKnowledge _allies;
    private final Map<UnrealId, Helper> _locked = new HashMap<>();

    public LockingKnowledge(AllyPositionsKnowledge allies) {
        _allies = allies;
    }


    public void update(){
        // get invalid entries
        Set<UnrealId> invalid = _locked.entrySet()
                .stream()
                .filter(entry -> Duration.between(entry.getValue().getWhen(), Instant.now()).toMillis() > VALID_FOR_MS)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // remove them
        invalid.forEach(_locked::remove);
    }

    public void reset() {
        _locked.clear();
    }

    public void wantToLock(UnrealId player, int region){
        _locked.put(player, new Helper(player, region, Instant.now()));
    }

    public int getLockLocation(AgentInfo info, Game game, NavMeshNavigation nav) {
        List<List<Location>> locations = LockLocations.getLockLocations(game);

        Map<UnrealId, Helper> toConsider = new HashMap<>(_locked);
        toConsider.remove(info.getId());
        Set<Integer> locked = toConsider.values()
                                        .stream()
                                        .map(h -> h.region)
                                        .collect(Collectors.toSet());

        int toLock = IntStream.range(0, locations.size())
                              .filter(index -> !locked.contains(index))
                              .mapToObj(i -> i)
                              .min(Comparator.comparingDouble(index -> {
                                  Location closest = Navigation.getClosest(nav, info, locations.get(index));
                                  return Navigation.distanceBetween(nav, info, closest);
                              }))
                              .get();

        return toLock;
    }
}
