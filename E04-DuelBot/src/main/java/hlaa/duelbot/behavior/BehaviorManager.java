package hlaa.duelbot.behavior;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BehaviorManager {

    private List<IBehaviorProvider> _behaviors = new LinkedList<>();
    private List<IBehavior> _previouslyActivated = new LinkedList<>();
    private final Logger log;

    public BehaviorManager(Logger log) {
        this.log = log;
    }

    public BehaviorManager addBehavior(IBehavior behavior) {
        return addProvider(new DefaultBehaviorProvider(behavior));
    }

    public BehaviorManager addProvider(IBehaviorProvider provider) {
        _behaviors.add(provider);
        return this;
    }

    public void execute() {
        List<IBehavior> nowActivated = this._behaviors.stream()
                                                      .map(IBehaviorProvider::get)
                                                      .filter(IBehavior::isFiring)
                                                      .sorted(Comparator.comparingDouble(IBehavior::priority))
                                                      .collect(Collectors.toList());
        _previouslyActivated.removeAll(nowActivated);
        _previouslyActivated.forEach(IBehavior::terminate);

        log.info(String.format("Now activated {%s}",
                nowActivated.stream().map(i -> i.getClass().getSimpleName()).collect(Collectors.joining(","))
        ));
        _previouslyActivated = nowActivated;
        _previouslyActivated.forEach(IBehavior::execute);
    }
}
