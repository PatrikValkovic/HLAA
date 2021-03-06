package hlaa.tdm.knowledge;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import hlaa.tdm.messages.TCDontSeeItem;
import hlaa.tdm.messages.TCSeeItem;
import hlaa.tdm.utils.DrawingColors;
import hlaa.tdm.utils.Navigation;
import hlaa.tdm.utils.SpawnItemHelper;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import static hlaa.tdm.utils.DrawingColors.SPAWN_ITEM_DONTSEE;
import static hlaa.tdm.utils.DrawingColors.SPAWN_ITEM_SEE;

public class ItemSpawnKnowledge {
    private final UT2004BotTCController _bot;
    private final Map<UnrealId, SpawnItemHelper> _spawnedItems;

    public ItemSpawnKnowledge(UT2004BotTCController _bot) {
        this._bot = _bot;

        _spawnedItems = _bot.getItems()
                            .getAllItems()
                            .values()
                            .stream()
                            .map(item -> {
                                double timeToSpawn = _bot.getItems().getItemRespawnTime(item);
                                Location spawnLocation = item.getLocation();
                                return new SpawnItemHelper(spawnLocation, timeToSpawn, item);
                            }).collect(Collectors.toMap(i -> i.getItem().getId(), i -> i));
    }

    public void update(){
        // update positions
        if (_bot.getLevelGeometry() != null && _bot.getLevelGeometry().isLoaded()) {
            // get items that should the bot see
            Set<SpawnItemHelper> shouldSeeItems = _spawnedItems.values()
                                                               .stream()
                                                               .filter(item -> _bot.getInfo().isFacing(item.getLocation(), 35))
                                                               .filter(item -> Navigation.canSee(_bot.getLevelGeometry(), _bot.getInfo().getLocation(), item.getLocation()))
                                                               .collect(Collectors.toSet());

            Set<SpawnItemHelper> seeItems = _bot.getItems()
                                                .getVisibleItems()
                                                .keySet()
                                                .stream()
                                                .map(_spawnedItems::get)
                                                .filter(Objects::nonNull)
                                                .collect(Collectors.toSet());

            // create set of items that the bot dont see
            Set<SpawnItemHelper> dontSeeItems = new HashSet<>(shouldSeeItems);
            dontSeeItems.removeAll(seeItems);

            // draw lines to spawned (pink) and not spawned (purple) items.
            if(DrawingColors.DRAW){
                seeItems.forEach(item -> _bot.getDraw().drawLine(SPAWN_ITEM_SEE, _bot.getInfo(), item.getLocation()));
                dontSeeItems.forEach(item -> _bot.getDraw().drawLine(SPAWN_ITEM_DONTSEE, _bot.getInfo(), item.getLocation()));
            }

            dontSeeItems.forEach(helper -> {
                this.dontSeeItem(helper.getItem().getId());
                _bot.getTCClient().sendToTeam(new TCDontSeeItem(helper.getItem().getId()));
            });

            seeItems.forEach(helper -> {
                this.seeItem(helper.getItem().getId());
                _bot.getTCClient().sendToTeam(new TCSeeItem(helper.getItem().getId()));
            });
        }
    }

    public void reset(){
        _spawnedItems.forEach((key, helper) -> {
            helper.setLastUnseen(Instant.now());
            helper.setSpawnProb(0);
        });
    }

    public List<Item> getSpawnedItems() {
        if(_bot.getLevelGeometry() != null && _bot.getLevelGeometry().isLoaded()) {
            return _spawnedItems.values()
                                .stream()
                                .filter(i -> i.getCurrentSpawnProb() > 0.9)
                                .map(SpawnItemHelper::getItem)
                                .collect(Collectors.toList());
        }
        else {
            return new ArrayList<>(_bot.getItems()
                                       .getSpawnedItems()
                                       .values());

        }
    }

    public void pickedUp(UnrealId id) {
        SpawnItemHelper helper = _spawnedItems.get(id);
        if (helper != null) {
            helper.setLastSeen(Instant.now().minus(1, ChronoUnit.MILLIS));
            helper.setLastUnseen(Instant.now());
            helper.setSpawnProb(0);
        }
    }

    public void seeItem(UnrealId id){
        if(_spawnedItems.containsKey(id))
            _spawnedItems.get(id).setLastSeen(Instant.now());
    }

    public void dontSeeItem(UnrealId id){
        if(_spawnedItems.containsKey(id))
            _spawnedItems.get(id).setLastUnseen(Instant.now());
    }

}
