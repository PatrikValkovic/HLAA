package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import java.util.LinkedList;
import java.util.List;

public class ReflexBehavior extends BaseBehavior {

    private List<IReflex> _reflexes = new LinkedList<>();


    public ReflexBehavior(UT2004BotTCController bot) {
        super(bot, 100.0);
    }
    public ReflexBehavior(UT2004BotTCController bot, double priority) {
        super(bot, priority);
    }


    @Override
    public boolean isFiring() {
        return _reflexes.stream().anyMatch(IReflex::triggered);
    }

    @Override
    public void execute() {
        this._reflexes.stream()
                      .filter(IReflex::triggered)
                      .forEach(IReflex::execute);
    }


    public ReflexBehavior addReflex(IReflex reflex){
        this._reflexes.add(reflex);
        return this;
    }
}
