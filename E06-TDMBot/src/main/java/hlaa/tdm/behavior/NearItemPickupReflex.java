package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import hlaa.tdm.utils.Inventory;
import hlaa.tdm.utils.Navigation;
import java.util.Comparator;
import java.util.Optional;

public class NearItemPickupReflex extends BaseReflex {

    private static final double CLOSE_ITEM = 250.0;

    public NearItemPickupReflex(UT2004BotModuleController bot) {
        super(bot);
    }

    @Override
    public boolean triggered() {
        return _bot.getItems()
                   .getSpawnedItems()
                   .values()
                   .stream()
                   .anyMatch(i -> Navigation.directDistance(_bot, i.getLocation()) < CLOSE_ITEM);
    }

    @Override
    public void execute() {
        Optional<Item> closest = _bot.getItems()
                                     .getSpawnedItems()
                                     .values()
                                     .stream()
                                     .filter(i -> Inventory.shouldPickup(_bot.getWeaponry(), _bot.getInfo(), i.getType()))
                                     .min(Comparator.comparingDouble(
                                             i -> Navigation.directDistance(_bot, i.getLocation())
                                     ));

        if (closest.isPresent()) {
            ILocated current = _bot.getNMNav().getCurrentTarget();
            _bot.getNMNav().navigate(closest.get().getLocation());
            _bot.getNMNav().setContinueTo(current);
        }
    }
}
