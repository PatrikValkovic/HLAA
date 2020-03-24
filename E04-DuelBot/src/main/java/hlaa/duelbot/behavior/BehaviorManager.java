package hlaa.duelbot.behavior;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class BehaviorManager {

    private List<IBehavior> behaviors = new LinkedList<>();

    public BehaviorManager addBehavior(IBehavior behavior){
        this.behaviors.add(behavior);
        return this;
    }

    public void execute() {
        this.behaviors.stream()
                      .filter(IBehavior::isFiring)
                      .sorted(Comparator.comparingDouble(IBehavior::priority))
                      .forEach(IBehavior::execute);
    }
}
