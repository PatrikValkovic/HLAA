package vip.bot;

import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004TCClient;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import vip.bot.knowledge.*;
import vip.bot.utils.DrawingColors;

public class KnowledgeBase {

    private final UT2004BotVIPController _bot;

    private final ItemSpawnKnowledge _itemSpawn;
    private final ItemDistancesKnowledge _itemDistances;
    private final OthersPickingKnowledge _othersPicking;
    private final AllyPositionsKnowledge _alliesPosition;
    private final EnemyPositionKnowledge _enemyPosition;
    private final FirepowerConcentrationKnowledge _firepower;
    private final LockingKnowledge _locking;
    private final VipKnowledge _vip;
    private final GuardingKnowledge _guard;


    public KnowledgeBase(UT2004BotVIPController bot, UT2004TCClient client) {
        this._bot = bot;
        _itemSpawn = new ItemSpawnKnowledge(bot, client);
        _itemDistances = new ItemDistancesKnowledge(bot);
        _othersPicking = new OthersPickingKnowledge(bot, _itemSpawn);
        _alliesPosition = new AllyPositionsKnowledge(bot, client);
        _enemyPosition = new EnemyPositionKnowledge(bot, client);
        _firepower = new FirepowerConcentrationKnowledge(bot, client);
        _locking = new LockingKnowledge(_alliesPosition);
        _vip = new VipKnowledge(bot);
        _guard = new GuardingKnowledge(bot, this);
    }

    public void updateKnowledge() {
        if(DrawingColors.DRAW) _bot.getDraw().clearAll();
        _itemSpawn.update();
        _alliesPosition.update();
        _enemyPosition.update();
        _firepower.update();
        _locking.update();
    }

    public void reset() {
        _itemSpawn.reset();
        _othersPicking.reset();
        _alliesPosition.reset();
        _enemyPosition.reset();
        _firepower.reset();
        _locking.reset();
        _vip.reset();
        _guard.reset();
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

    public FirepowerConcentrationKnowledge getFirepowerConcentrationKnowledge() {
        return _firepower;
    }

    public LockingKnowledge getLockingKnowledge() {
        return _locking;
    }

    public VipKnowledge getVipKnowledge(){
        return _vip;
    }

    public GuardingKnowledge getGuardKnowledge() {
        return _guard;
    }

}
