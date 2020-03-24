package hlaa.duelbot;

import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import hlaa.duelbot.utils.DeltaCounter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.Matrix2D;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation;

public class KnowledgeBase {
    private static final double SPEED = 2000.0;
    private static final double PRECOMPUTE_STEP = 0.25;
    private static final double ZERO_NAVPOINTS_IN_DISTANCE = 150.0;

    private final UT2004BotModuleController _bot;
    private final DeltaCounter _delta = new DeltaCounter();

    private SparseMatrix _movementMarkovChain;
    private Matrix2D _positionEstimation;
    private Map<Integer, UnrealId> _indexToNavpoin;
    private Map<UnrealId, Integer> _navpointToIndex;

    public KnowledgeBase(UT2004BotModuleController bot) {
        this._bot = bot;
        createMarkovChain();
        createPositionMatrix();
        _positionEstimation.showGUI();
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
                         double valueToStore = PRECOMPUTE_STEP * distance / SPEED / (double) (nNeighbors + 1);
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
        for(int i=0;i<nNavpoints;i++)
            multiplyMatrix.setAsFloat(1.0f / rowSums.getAsFloat(i, 0), i, i);
        _movementMarkovChain.mtimes(Calculation.Ret.ORIG, false, multiplyMatrix);

        // check
        for(int r=0;r<nNavpoints;r++){
            double s = 0;
            for(int c=0;c<nNavpoints;c++)
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
                _positionEstimation.setAsFloat(0.0f, 0, _navpointToIndex.get(point));
                //System.out.println("Setting 0 to " + _bot.getNavPoints().getNavPoint(point).getLocation());
            });
        // and navpoints pretty close
        _bot.getNavPoints().getNavPoints()
            .values()
            .stream()
            .filter(p -> p.getLocation().getDistance(_bot.getInfo().getLocation()) < ZERO_NAVPOINTS_IN_DISTANCE)
            .forEach(point -> {
                _positionEstimation.setAsFloat(0.0f, 0, _navpointToIndex.get(point.getId()));
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
                _positionEstimation.fill(Calculation.Ret.ORIG, 0.0f);
                _positionEstimation.setAsFloat(1.0f, 0, _navpointToIndex.get(playerNavpoint.getId()));
                System.out.println("Setting to 1 because see player " + playerNavpoint.getLocation());
            });
        float positionSum = _positionEstimation.sum(Calculation.Ret.NEW, 1, false).getAsFloat(0,0);
        _positionEstimation.divide(Calculation.Ret.ORIG, false, positionSum);

        // check
        {
            double s = 0;
            for(int i=0;i<_bot.getNavPoints().getNavPoints().size();i++)
                s += _positionEstimation.getAsDouble(0, i);
            assert Math.abs(s - 1.0) < 1e-25;
        }
    }

    public Matrix2D getPositionEstimation() {
        return _positionEstimation;
    }

    public NavPoint getPointWithMaxProb(){
        long[] maximumCoords = _positionEstimation.getCoordinatesOfMaximum();
        //System.out.println("Maximum prob at position " + Arrays.toString(maximumCoords));
        //System.out.println("Maximum value is " + _positionEstimation.getAsFloat(maximumCoords));
        UnrealId idOfMax = _indexToNavpoin.get((int)maximumCoords[1]);
        return _bot.getNavPoints().getNavPoint(idOfMax);
    }
}
