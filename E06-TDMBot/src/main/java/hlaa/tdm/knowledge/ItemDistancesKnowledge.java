package hlaa.tdm.knowledge;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import hlaa.tdm.utils.Navigation;
import java.util.HashMap;
import java.util.Map;

public class ItemDistancesKnowledge {

    private final UT2004BotModuleController _bot;
    private final Map<UnrealId, Map<UnrealId, Double>> _distances = new HashMap<>();

    public ItemDistancesKnowledge(UT2004BotModuleController bot) {
        this._bot = bot;

        computeDistances();
    }

    private void computeDistances(){
        for(Item i : _bot.getItems().getAllItems().values())
            for(Item j : _bot.getItems().getAllItems().values()){
                double distance = Navigation.distanceBetween(_bot.getNMNav(), i, j);
                if(!_distances.containsKey(i.getId()))
                    _distances.put(i.getId(), new HashMap<>());
                _distances.get(i.getId()).put(j.getId(), distance);
            }
    }

    public double getDistanceBetween(Item from, Item to){
        try {
            return _distances.get(from.getId()).get(to.getId());
        }
        catch(NullPointerException e) {
            double distance = Navigation.distanceBetween(_bot.getNMNav(), from, to);
            if(!_distances.containsKey(from.getId()))
                _distances.put(from.getId(), new HashMap<>());
            _distances.get(from.getId()).put(to.getId(), distance);
            return distance;
        }
    }
}
