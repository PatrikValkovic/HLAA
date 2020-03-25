package hlaa.duelbot.behavior;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BehaviorManager {

    private List<IBehavior> _behaviors = new LinkedList<>();
    private List<IBehavior> _previouslyActivated = new LinkedList<>();

    public BehaviorManager addBehavior(IBehavior behavior){
        this._behaviors.add(behavior);
        return this;
    }

    public void execute() {
        List<IBehavior> nowActivated = this._behaviors.stream()
                       .filter(IBehavior::isFiring)
                       .sorted(Comparator.comparingDouble(b -> -b.priority()))
                       .collect(Collectors.toList());
        _previouslyActivated.removeAll(nowActivated);
        _previouslyActivated.forEach(IBehavior::terminate);

        _previouslyActivated = nowActivated;
        _previouslyActivated.forEach(IBehavior::execute);
    }
}
