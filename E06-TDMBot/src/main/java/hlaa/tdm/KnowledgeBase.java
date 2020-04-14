package hlaa.tdm;

import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import hlaa.tdm.knowledge.*;

public class KnowledgeBase {

    private final UT2004BotTCController _bot;

    private final ItemSpawnKnowledge _itemSpawn;
    private final ItemDistancesKnowledge _itemDistances;
    private final OthersPickingKnowledge _othersPicking;
    private final AllyPositionsKnowledge _alliesPosition;
    private final EnemyPositionKnowledge _enemyPosition;


    public KnowledgeBase(UT2004BotTCController bot) {
        this._bot = bot;
        _itemSpawn = new ItemSpawnKnowledge(bot);
        _itemDistances = new ItemDistancesKnowledge(bot);
        _othersPicking = new OthersPickingKnowledge(bot, _itemSpawn);
        _alliesPosition = new AllyPositionsKnowledge(bot);
        _enemyPosition = new EnemyPositionKnowledge(bot);
    }

    public void updateKnowledge() {
        _bot.getDraw().clearAll();
        _itemSpawn.update();
        _alliesPosition.update();
        _enemyPosition.update();
    }

    public void reset() {
        _itemSpawn.reset();
        _othersPicking.reset();
        _alliesPosition.reset();
        _enemyPosition.reset();
    }

    public ItemSpawnKnowledge getItemSpawnedKnowledge(){
        return _itemSpawn;
    }

    public ItemDistancesKnowledge getItemDistancesKnowledge() {
        return _itemDistances;
    }

    public OthersPickingKnowledge getOtherPickingKnowledge() {
        return _othersPicking;
    }

    public AllyPositionsKnowledge getAlliesPositionsKnowledge(){
        return _alliesPosition;
    }

    public EnemyPositionKnowledge getEnemyPositionsKnowledge() {
        return _enemyPosition;
    }

}
