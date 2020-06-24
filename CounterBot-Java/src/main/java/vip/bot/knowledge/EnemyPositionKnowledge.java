package vip.bot.knowledge;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004TCClient;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.Matrix2D;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.calculation.Calculation;
import vip.bot.messages.TCEnemyLocation;
import vip.bot.utils.DeltaCounter;
import vip.bot.utils.Navigation;

public class EnemyPositionKnowledge {

    private static final double SPEED = 340.0;
    private static final double PRECOMPUTE_STEP = 0.25;
    private static final double ZERO_NAVPOINTS_IN_DISTANCE = 150.0;
    private static final double MC_PROB_MULTIPLIER = 0.12;

    @AllArgsConstructor
    private static class Helper {
        @Getter
        Player player;
        @Getter
        private Matrix2D positionEstimation;
    }

    private final DeltaCounter _delta = new DeltaCounter();
    private final UT2004BotModuleController _bot;
    private final UT2004TCClient _client;

    private SparseMatrix _movementMarkovChain;
    private Map<Integer, UnrealId> _indexToNavpoin;
    private Map<UnrealId, Integer> _navpointToIndex;
    private Map<UnrealId, Helper> _estimations = new HashMap<>();

    public EnemyPositionKnowledge(UT2004BotModuleController bot, UT2004TCClient client) {
        _bot = bot;
        _client = client;

        createMarkovChain();
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

    public void update() {
        float delta = _delta.getDelta();

        // update estimator
        for (Helper helper : _estimations.values())
            helper.getPositionEstimation()
                  .mtimes(
                          Calculation.Ret.ORIG,
                          false,
                          _movementMarkovChain
                  );

        // update seen navpoints
        _bot.getNavPoints().getVisibleNavPoints()
            .keySet()
            .forEach(point -> {
                for (Helper helper : _estimations.values())
                    helper.getPositionEstimation()
                          .setAsFloat(
                                  0.0000000001f,
                                  0,
                                  _navpointToIndex.get(point)
                          );
                //System.out.println("Setting 0 to " + _bot.getNavPoints().getNavPoint(point).getLocation());
            });
        // and navpoints pretty close
        _bot.getNavPoints().getNavPoints()
            .values()
            .stream()
            .filter(p -> p.getLocation().getDistance(_bot.getInfo().getLocation()) < ZERO_NAVPOINTS_IN_DISTANCE)
            .forEach(point -> {
                for (Helper helper : _estimations.values())
                    helper.getPositionEstimation()
                          .setAsFloat(
                                  0.0000000001f,
                                  0,
                                  _navpointToIndex.get(point.getId())
                          );
                //System.out.println("Setting 0 to nearby " + point.getLocation());
            });

        // update estimator if see player
        _bot.getPlayers()
            .getVisibleEnemies()
            .values()
            .forEach(player -> {
                updatePlayer(player.getId(), player.getLocation());
            });

        // make sure it sums to 1
        for (Helper helper : _estimations.values()) {
            float positionSum = helper.getPositionEstimation()
                                      .sum(Calculation.Ret.NEW, 1, false)
                                      .getAsFloat(0, 0);
            helper.getPositionEstimation().divide(Calculation.Ret.ORIG, false, positionSum);
        }

        // notify others about enemy
        _bot.getPlayers()
            .getVisibleEnemies()
            .values()
            .forEach(enemy -> {
                _client.sendToTeam(new TCEnemyLocation(enemy.getId(), enemy.getLocation()));
            });


        // check
        /*{
            double s = 0;
            for(int i=0;i<_bot.getNavPoints().getNavPoints().size();i++)
                s += _positionEstimation.getAsDouble(0, i);
            assert Math.abs(s - 1.0) < 1e-25;
        }*/
    }

    public void reset(){
        _estimations.clear();
    }

    public void updatePlayer(UnrealId playerId, Location location){
        NavPoint playerNavpoint =  Navigation.getClosestNavpoint(location, _bot.getNavPoints());
        if (!_estimations.containsKey(playerId))
            _estimations.put(playerId, new Helper(_bot.getPlayers().getPlayer(playerId), DenseMatrix.Factory.zeros(1, _navpointToIndex.size())));
        Helper h = _estimations.get(playerId);
        updateNavpoint(playerNavpoint, 1.0f, playerId);
        //System.out.println("Setting to 1 because see player " + playerNavpoint.getLocation());
    }

    public void updateNavpoint(NavPoint point, float newValue){
        for (UnrealId id : _estimations.keySet()) {
            updateNavpoint(point, newValue, id);
        }
    }

    public void updateNavpoint(NavPoint point, float newValue, UnrealId forPlayer){
        if(!_estimations.containsKey(forPlayer)){
            _bot.getLog().warning("Player " + forPlayer + " not in estimations");
            return;
        }
        Helper helper = _estimations.get(forPlayer);
        Matrix2D positionEstimation = helper.getPositionEstimation();
        // update
        positionEstimation.setAsFloat(Math.max(newValue, 0.0000000001f), 0, _navpointToIndex.get(point.getId()));

        // normalize to 1
        float positionSum = positionEstimation.sum(Calculation.Ret.NEW, 1, false).getAsFloat(0, 0);
        positionEstimation.divide(Calculation.Ret.ORIG, false, positionSum);

        // check
        /*{
            double s = 0;
            for(int i=0;i<_bot.getNavPoints().getNavPoints().size();i++)
                s += positionEstimation.getAsDouble(0, i);
            assert Math.abs(s - 1.0) < 1e-25;
        }*/
    }


    public NavPoint getPointWithMaxProb() {
        Optional<Helper> optHelper = _estimations.values()
                                                 .stream()
                                                 .max(Comparator.comparingDouble(helper -> helper.positionEstimation.getMaxValue()));

        if(!optHelper.isPresent())
            return _bot.getNavPoints().getRandomNavPoint();

        Helper helper = optHelper.get();
        long[] maximumCoords = helper.positionEstimation.getCoordinatesOfMaximum();
        //System.out.println("Maximum prob at position " + Arrays.toString(maximumCoords));
        //System.out.println("Maximum value is " + _positionEstimation.getAsFloat(maximumCoords));
        UnrealId idOfMax = _indexToNavpoin.get((int) maximumCoords[1]);
        return _bot.getNavPoints().getNavPoint(idOfMax);
    }

    public double getProbAtNavpoint(NavPoint p) {
        int navpointIndex = _navpointToIndex.get(p.getId());
        return _estimations.values()
                .stream()
                .map(helper -> helper.positionEstimation.getAsDouble(0, navpointIndex))
                .max(Comparator.comparingDouble(i -> i))
                .get();
    }

    public double getCumulativeProbAtNavpoint(NavPoint p) {
        int navpointIndex = _navpointToIndex.get(p.getId());
        return _estimations.values()
                           .stream()
                           .mapToDouble(helper -> helper.positionEstimation.getAsDouble(0, navpointIndex))
                           .sum();
    }

    public double getMaxProb() {
        return getProbAtNavpoint(getPointWithMaxProb());
    }

    public void resetProbabilities() {
        int navpoints = _indexToNavpoin.size();
        _estimations.values()
                    .forEach(helper -> {
                        helper.positionEstimation.fill(Calculation.Ret.ORIG, 1.0 / (double)navpoints);
                    });
    }

}
