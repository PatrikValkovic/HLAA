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
    private static final double SPEED = 300.0;
    private static final double PRECOMPUTE_STEP = 0.2;

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
        Matrix sums = _movementMarkovChain.sum(Calculation.Ret.NEW, 0, false);
        for (int i = 0; i < nNavpoints; i++) {
            _movementMarkovChain.setAsFloat(Math.max(0.0f, 1.0f - sums.getAsFloat(0, i)), i, i);
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

        // update seen navpoints
        _bot.getNavPoints().getVisibleNavPoints()
            .keySet()
            .forEach(point -> {
                _positionEstimation.setAsFloat(0.0f, 0, _navpointToIndex.get(point));
            });
        double sum = _positionEstimation.getValueSum();
        _positionEstimation.divide(sum);

        // update estimator
        int ticks = (int)Math.round(delta / PRECOMPUTE_STEP);
        _positionEstimation.mtimes(
                Calculation.Ret.ORIG,
                false,
                _movementMarkovChain.power(Calculation.Ret.LINK, ticks)
        );

        // update estimator if see player
        _bot.getPlayers().getVisiblePlayers()
            .values()
            .forEach(player -> {
                NavPoint playerNavpoint
                        = _bot.getNavPoints()
                              .getNavPoints()
                              .values()
                              .stream()
                              .sorted(Comparator.comparingDouble(p -> p.getLocation().getDistance(player.getLocation())))
                              .findFirst()
                              .get();
                _positionEstimation.fill(Calculation.Ret.ORIG, 0.0f);
                _positionEstimation.setAsFloat(1.0f, 0, _navpointToIndex.get(playerNavpoint.getId()));
            });
        _positionEstimation.divide(Calculation.Ret.ORIG, false, 1.0 / _positionEstimation.getValueSum());
    }
}
