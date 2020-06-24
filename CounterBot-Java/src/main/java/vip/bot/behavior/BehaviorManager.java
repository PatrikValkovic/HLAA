package vip.bot.behavior;

import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BehaviorManager implements IBehavior {

    private List<IBehaviorProvider> _behaviors = new LinkedList<>();
    private List<IBehavior> _previouslyActivated = new LinkedList<>();
    private final UT2004BotVIPController _bot;

    public BehaviorManager(UT2004BotVIPController bot) {
        this._bot = bot;
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
                                                      .filter(Objects::nonNull)
                                                      .filter(IBehavior::isFiring)
                                                      .sorted(Comparator.comparingDouble(IBehavior::priority))
                                                      .collect(Collectors.toList());
        _previouslyActivated.removeAll(nowActivated);
        _previouslyActivated.forEach(IBehavior::terminate);

        // show active states
        if (_bot != null) {
            String states = nowActivated.stream().map(i -> i.getClass().getSimpleName()).collect(Collectors.joining(","));
            _bot.getBot().getBotName().setInfo(states);
            _bot.getLog().info("Active states: " + states);
        }

        // run activated
        _previouslyActivated = nowActivated;
        _previouslyActivated.forEach(IBehavior::execute);
    }


    @Override
    public boolean isFiring() {
        return _behaviors.stream()
                         .map(IBehaviorProvider::get)
                         .anyMatch(IBehavior::isFiring);
    }

    @Override
    public double priority() {
        return _behaviors.stream()
                         .map(IBehaviorProvider::get)
                         .mapToDouble(IBehavior::priority)
                         .average()
                         .orElse(0);
    }

    @Override
    public void terminate() {
        _previouslyActivated.forEach(IBehavior::terminate);
        _previouslyActivated.clear();
    }
}
