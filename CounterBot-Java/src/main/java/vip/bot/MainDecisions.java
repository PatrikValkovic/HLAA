package vip.bot;

import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import vip.bot.behavior.*;

public class MainDecisions implements IBehaviorProvider {

    private final UT2004BotVIPController _bot;
    private final KnowledgeBase _knowledge;

    private final GuardingBehavior _guard;
    private final CombatBehavior _combat;
    private final BackShieldBehavior _backshield;

    public MainDecisions(UT2004BotVIPController bot, KnowledgeBase knowledge){
        _bot = bot;
        _knowledge = knowledge;

        _guard = new GuardingBehavior(bot, knowledge);
        _combat = new CombatBehavior(bot, knowledge);
        _backshield = new BackShieldBehavior(bot, knowledge);
    }

    @Override
    public IBehavior get() {
        if(!_knowledge.getVipKnowledge().isRunning())
            return null;

        if(_bot.getPlayers().canSeeEnemies()){
            return _combat;
        }
        if(_knowledge.getGuardKnowledge().is_back_shield()){
            return _backshield;
        }

        return _guard;
    }
}
