package vip.bot.behavior;

public class DefaultBehaviorProvider implements IBehaviorProvider {

    private IBehavior _behavior;

    public DefaultBehaviorProvider(IBehavior _behavior) {
        this._behavior = _behavior;
    }

    @Override
    public IBehavior get() {
        return _behavior;
    }
}
