package hlaa.tdm;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;
import hlaa.tdm.knowledge.ItemDistancesKnowledge;
import hlaa.tdm.knowledge.ItemSpawnKnowledge;
import hlaa.tdm.knowledge.OthersPickingKnowledge;
import hlaa.tdm.utils.DeltaCounter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.Matrix2D;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation;

public class KnowledgeBase {
    private static final double SPEED = 340.0;
    private static final double PRECOMPUTE_STEP = 0.25;
    private static final double ZERO_NAVPOINTS_IN_DISTANCE = 150.0;
    private static final double MC_PROB_MULTIPLIER = 0.12;

    private final UT2004BotTCController _bot;
    private final DeltaCounter _delta = new DeltaCounter();

    private final ItemSpawnKnowledge _itemSpawn;
    private final ItemDistancesKnowledge _itemDistances;
    private final OthersPickingKnowledge _othersPicking;

    private SparseMatrix _movementMarkovChain;
    private Matrix2D _positionEstimation;
    private Map<Integer, UnrealId> _indexToNavpoin;
    private Map<UnrealId, Integer> _navpointToIndex;


    public KnowledgeBase(UT2004BotTCController bot) {
        this._bot = bot;
        _itemSpawn = new ItemSpawnKnowledge(bot);
        _itemDistances = new ItemDistancesKnowledge(bot);
        _othersPicking = new OthersPickingKnowledge(bot, _itemSpawn);

        createMarkovChain();
        createPositionMatrix();
        //_positionEstimation.showGUI();

    }

    private void createMarkovChain() {
        int nNavpoints = _bot.getNavPoints().getNavPoints().size();
        _movementMarkovChain = SparseMatrix.Factory.zeros(nNavpoints, nNavpoints);
        _indexToNavpoin = new HashMap<>(nNavpoints * 3, 0.6f);
        _navpointToIndex = new HashMap<>(nNavpoints * 3, 0.6f);

        int currentIndex = 0;
        for (UnrealId navPointsIds : _bot.getNavPoints().getNavPoints().keySet()) {
            _indexToNavpoin.put(currentIndex, navPointsIds);
            _navpointToIndex.put(navPointsIds, currentIndex);
            currentIndex++;
        }

        _bot.getNavPoints()
            .getNavPoints()
            .values()
            .forEach(point -> {
                int nNeighbors = point.getOutgoingEdges().size();
                point.getOutgoingEdges()
                     .values()
                     .stream()
                     .map(link -> link.getToNavPoint() == point ? link.getFromNavPoint() : link.getToNavPoint())
                     .forEach(neigbor -> {
                         double distance = neigbor.getLocation().getDistance(point.getLocation());
                         double valueToStore = MC_PROB_MULTIPLIER * PRECOMPUTE_STEP * distance / SPEED / (double) (nNeighbors + 1);
                         _movementMarkovChain.setAsFloat(
                                 (float) valueToStore,
                                 _navpointToIndex.get(point.getId()),
                                 _navpointToIndex.get(neigbor.getId())
                         );
                     });
            });
        Matrix sums = _movementMarkovChain.sum(Calculation.Ret.NEW, 1, false);
        for (int i = 0; i < nNavpoints; i++) {
            _movementMarkovChain.setAsFloat(Math.max(0.0f, 1.0f - sums.getAsFloat(i, 0)), i, i);
        }

        // normalize so rows sum to 1
        Matrix rowSums = _movementMarkovChain.sum(Calculation.Ret.NEW, 1, false);
        SparseMatrix multiplyMatrix = SparseMatrix.Factory.zeros(nNavpoints, nNavpoints);
        for (int i = 0; i < nNavpoints; i++)
            multiplyMatrix.setAsFloat(1.0f / rowSums.getAsFloat(i, 0), i, i);
        _movementMarkovChain.mtimes(Calculation.Ret.ORIG, false, multiplyMatrix);

        // check
        for (int r = 0; r < nNavpoints; r++) {
            double s = 0;
            for (int c = 0; c < nNavpoints; c++)
                s += _movementMarkovChain.getAsDouble(r, c);
            assert Math.abs(s - 1.0) < 1e-25;
        }
    }

    private void createPositionMatrix() {
        int nNavpoints = _bot.getNavPoints().getNavPoints().size();
        _positionEstimation = DenseMatrix.Factory.zeros(1, nNavpoints);
        for (int i = 0; i < nNavpoints; i++)
            _positionEstimation.setAsFloat(1.0f / (float) nNavpoints, 0, i);
    }

    public void updateKnowledge() {
        float delta = _delta.getDelta();
        _bot.getDraw().clearAll();

        // update estimator
        _positionEstimation.mtimes(
                Calculation.Ret.ORIG,
                false,
                _movementMarkovChain
        );

        // update seen navpoints
        _bot.getNavPoints().getVisibleNavPoints()
            .keySet()
            .forEach(point -> {
                _positionEstimation.setAsFloat(0.0000000001f, 0, _navpointToIndex.get(point));
                //System.out.println("Setting 0 to " + _bot.getNavPoints().getNavPoint(point).getLocation());
            });
        // and navpoints pretty close
        _bot.getNavPoints().getNavPoints()
            .values()
            .stream()
            .filter(p -> p.getLocation().getDistance(_bot.getInfo().getLocation()) < ZERO_NAVPOINTS_IN_DISTANCE)
            .forEach(point -> {
                _positionEstimation.setAsFloat(0.0000000001f, 0, _navpointToIndex.get(point.getId()));
                //System.out.println("Setting 0 to nearby " + point.getLocation());
            });

        // update estimator if see player
        _bot.getPlayers().getVisiblePlayers()
            .values()
            .forEach(player -> {
                NavPoint playerNavpoint
                        = _bot.getNavPoints()
                              .getNavPoints()
                              .values()
                              .stream().min(Comparator.comparingDouble(
                                p -> p.getLocation().getDistance(player.getLocation()))
                        ).get();
                _positionEstimation.fill(Calculation.Ret.ORIG, 0.0000000001f);
                _positionEstimation.setAsFloat(1.0f, 0, _navpointToIndex.get(playerNavpoint.getId()));
                //System.out.println("Setting to 1 because see player " + playerNavpoint.getLocation());
            });
        float positionSum = _positionEstimation.sum(Calculation.Ret.NEW, 1, false).getAsFloat(0, 0);
        _positionEstimation.divide(Calculation.Ret.ORIG, false, positionSum);

        // check
        /*{
            double s = 0;
            for(int i=0;i<_bot.getNavPoints().getNavPoints().size();i++)
                s += _positionEstimation.getAsDouble(0, i);
            assert Math.abs(s - 1.0) < 1e-25;
        }*/

        _itemSpawn.update();
    }

    public Matrix2D getPositionEstimation() {
        return _positionEstimation;
    }

    public NavPoint getPointWithMaxProb() {
        long[] maximumCoords = _positionEstimation.getCoordinatesOfMaximum();
        //System.out.println("Maximum prob at position " + Arrays.toString(maximumCoords));
        //System.out.println("Maximum value is " + _positionEstimation.getAsFloat(maximumCoords));
        UnrealId idOfMax = _indexToNavpoin.get((int) maximumCoords[1]);
        return _bot.getNavPoints().getNavPoint(idOfMax);
    }

    public void updateNavpoint(NavPoint point, float newValue) {
        // update
        _positionEstimation.setAsFloat(Math.max(newValue, 0.0000000001f), 0, _navpointToIndex.get(point.getId()));

        // normalize to 1
        float positionSum = _positionEstimation.sum(Calculation.Ret.NEW, 1, false).getAsFloat(0, 0);
        _positionEstimation.divide(Calculation.Ret.ORIG, false, positionSum);

        // check
        /*{
            double s = 0;
            for(int i=0;i<_bot.getNavPoints().getNavPoints().size();i++)
                s += _positionEstimation.getAsDouble(0, i);
            assert Math.abs(s - 1.0) < 1e-25;
        }*/
    }

    public double getProbAtNavpoint(NavPoint p) {
        return _positionEstimation.getAsDouble(0, _navpointToIndex.get(p.getId()));
    }

    public double getMaxProb() {
        return getProbAtNavpoint(getPointWithMaxProb());
    }

    public void resetProbabilities() {
        _positionEstimation.fill(Calculation.Ret.NEW, 1.0 / (double) _positionEstimation.getSize(1));
    }

    public void reset() {
        _itemSpawn.reset();
        _othersPicking.reset();
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


}
