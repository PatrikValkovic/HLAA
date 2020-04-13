package hlaa.tdm.knowledge;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import hlaa.tdm.utils.Navigation;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

public class OthersPickingKnowledge {

    @AllArgsConstructor
    private static class Helper {
        double distance;
        Item item;
        UnrealId bot;
    }

    private final UT2004BotTCController _bot;
    private final ItemSpawnKnowledge _spawn;
    private final Map<UnrealId, Helper> _picking = new HashMap<>();

    public OthersPickingKnowledge(UT2004BotTCController bot, ItemSpawnKnowledge spawn) {
        _bot = bot;
        _spawn = spawn;
    }

    public void reset(){
        _picking.clear();
    }

    public void otherGoingToPick(Item what, double distance, UnrealId player) {
        _picking.put(player, new Helper(distance, what, player));
    }

    public List<Item> itemsToPick() {
        Map<UnrealId, Double> others = new HashMap<>();
        _picking.values()
                .stream()
                .filter(i -> !i.bot.equals(_bot.getInfo().getId()))
                .sorted(Comparator.comparingDouble(i -> i.distance))
                .forEach(i -> {
                    if(i.item == null)
                        return;
                    if(!others.containsKey(i.item.getId()) || others.get(i.item.getId()) > i.distance)
                        others.put(i.item.getId(), i.distance);
                });

        return _spawn.getSpawnedItems()
                     .stream()
                     .filter(i ->
                             !others.containsKey(i.getId()) ||
                             Navigation.distanceBetween(_bot.getNMNav(), _bot.getInfo(), i) < others.get(i.getId())
                     )
                     .collect(Collectors.toList());
    }
}
