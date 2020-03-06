package ut2004.exercises.e02.valkovic;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

public class NavigationModule {

    private final int SPEED;
    private final Map<UnrealId, NavPoint> NAV_POINTS;

    private INDArray _markovChain;
    private INDArray _simulatedMarkovChain;
    private INDArray _plannedChain;

    private Map<String, Integer> _idToPos;
    private Map<Integer, NavPoint> _posToPoint;
    private Map<String, INDArray> _locationsEstimators = new HashMap<>(30, 0.6f);

    public NavigationModule(Map<UnrealId, NavPoint> navPoints, int speed) {

        SPEED = speed;
        NAV_POINTS = navPoints;

        int num_navpoints = navPoints.size();
        _idToPos = new HashMap<>(num_navpoints * 2, 0.6f);
        _posToPoint = new HashMap<>(num_navpoints * 2, 0.6f);
        int pos = 0;
        for (NavPoint p : navPoints.values()) {
            _idToPos.put(p.getId().getStringId(), pos);
            _posToPoint.put(pos, p);
            pos++;
        }

        _markovChain = Nd4j.zeros(num_navpoints, num_navpoints);
        navPoints.values()
                 .stream()
                 .flatMap(p -> p.getOutgoingEdges().values().stream())
                 .collect(Collectors.toSet())
                 .forEach(link -> {
                     NavPoint fromNavPoint = link.getFromNavPoint();
                     NavPoint toNavPoint = link.getToNavPoint();
                     int fromPointIndex = _idToPos.get(fromNavPoint.getId().getStringId());
                     int toPointIndex = _idToPos.get(toNavPoint.getId().getStringId());
                     Location fromLoc = fromNavPoint.getLocation();
                     Location toLoc = toNavPoint.getLocation();
                     int fromArcs = fromNavPoint.getOutgoingEdges().size();
                     int toArcs = toNavPoint.getOutgoingEdges().size();
                     float distance = (float) fromLoc.getDistance(toLoc);
                     float probFrom = Math.min((float) speed / distance, 1) / (float) (fromArcs + 1);
                     _markovChain.put(fromPointIndex, toPointIndex, probFrom);
                     float probTo = Math.min((float) speed / distance, 1) / (float) (toArcs + 1);
                     _markovChain.put(toPointIndex, fromPointIndex, probTo);
                 });
        INDArray summed = _markovChain.sum(1);
        for (int i = 0; i < num_navpoints; i++) {
            _markovChain.put(i, i, 1 - summed.getFloat(i));
        }
    }

    public void preprocess(int simulate_steps, int plan_steps) {
        _simulatedMarkovChain = Nd4j.diag(Nd4j.ones(_idToPos.size()));
        for (int i = 0; i < simulate_steps; i++)
            _simulatedMarkovChain = _simulatedMarkovChain.mmul(_markovChain);

        _plannedChain = Nd4j.diag(Nd4j.ones(_idToPos.size()));
        for (int i = 0; i < simulate_steps - plan_steps; i++)
            _plannedChain = _plannedChain.mmul(_markovChain);
    }

    public void updateLocations(Map<String, Location> locations) {
        // perform next step of all entities without new information
        Set<String> notUpdatedLocations = _locationsEstimators.keySet();
        notUpdatedLocations.removeAll(locations.keySet());
        for (String p : notUpdatedLocations) {
            INDArray updated = _locationsEstimators.get(p).mmul(_markovChain);
            _locationsEstimators.put(p, updated);
        }
        // set locations of known entries
        locations.forEach((name, location) -> {
            NavPoint closest = NAV_POINTS.values().stream().min((c1, c2) -> {
                double firstDistance = c1.getLocation().getDistance(location);
                double secondDistance = c2.getLocation().getDistance(location);
                return Double.compare(firstDistance, secondDistance);
            }).get();
            int closestPos = _idToPos.get(closest.getId().getStringId());
            INDArray position = Nd4j.zeros(_markovChain.shape());
            position.put(closestPos, closestPos, 1.0f);
            _locationsEstimators.put(name, position);
        });
    }

    private Stream<NavPoint> getNavPointsAround(Location location, double distance) {
        Random rand = new Random();
        return NAV_POINTS.values()
                         .stream()
                         .filter(p -> p.getLocation().getDistance(location) < distance)
                         .sorted(Comparator.comparingDouble(e -> rand.nextDouble()));
    }

    public void computeNextPoint(Location fromLocation, int ahead, String otherWolf, Logger log) {
        if (otherWolf == null) return;
        // estimate other wolf location
        INDArray otherWolfEstimation = _locationsEstimators.get(otherWolf).mmul(_simulatedMarkovChain);
        // estimate sheep locations
        String[] sheepNames = _locationsEstimators.keySet().stream().filter(s -> s.contains("Sheep")).toArray(String[]::new);
        if (sheepNames.length == 0) return;
        INDArray[] sheepLocations = new INDArray[sheepNames.length];
        for (int i = 0; i < sheepNames.length; i++) {
            sheepLocations[i] = _locationsEstimators.get(sheepNames[i]).mmul(_simulatedMarkovChain).sub(otherWolfEstimation);
            sheepLocations[i] = Transforms.max(sheepLocations[i], 0.0f);
        }

        // consider all the points
        Set<NavPoint> toConsider = getNavPointsAround(fromLocation, SPEED * ahead).collect(Collectors.toSet());
        log.info("Considering " + toConsider.size() + " nav points");
        Map<NavPoint, Float> scores = new HashMap<>(toConsider.size() * 2, 0.6f);
        toConsider.forEach(navPoint -> {
            int navPointPos = _idToPos.get(navPoint.getId().getStringId());
            INDArray expectedPosition = Nd4j.zeros(_markovChain.shape());
            expectedPosition.put(navPointPos, navPointPos, 1.0f);
            INDArray expectCurrentNavpoint = expectedPosition.mmul(_plannedChain);
            float result = (Float) Stream.of(sheepLocations).map(l -> l.sub(expectCurrentNavpoint))
                                         .map(m -> Transforms.max(m, 0.0f))
                                         .reduce(INDArray::add)
                                         .get()
                                         .sum(0, 1)
                                         .element();
            scores.put(navPoint, result);
        });

        // get the best point
        Map.Entry<NavPoint, Float> min = Collections.min(
                scores.entrySet(),
                Map.Entry.comparingByValue()
        );
        log.info("Best entry " + min.getValue());
        _nextLocation.set(min.getKey().getLocation());
    }

    private AtomicReference<Location> _nextLocation = new AtomicReference<>(new Location(0.0, 0.0, 0.0));

    public AtomicReference<Location> getNavLocation() {
        return _nextLocation;
    }

    public void removeEstimationFor(String target, Logger log) {
        if (target.contains("WolfBot"))
            return;
        if (!_locationsEstimators.containsKey(target))
            return;
        _locationsEstimators.remove(target);
        log.info("Forgot " + target);
    }

}
