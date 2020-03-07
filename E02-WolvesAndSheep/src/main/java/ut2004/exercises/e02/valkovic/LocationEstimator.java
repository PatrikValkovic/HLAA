package ut2004.exercises.e02.valkovic;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.StreamSupport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class LocationEstimator {

    private static final int DONT_SEE_MS_DELETE = 400;
    private static final double IDEAL_WOLF_DISTANCE = 600;
    private static final double WOLF_DISTANCE_WEIGHT = 0.8;
    private static final double SHEEP_DISTANCE_WEIGHTS = 1.0;

    @NoArgsConstructor
    @AllArgsConstructor
    private static class Observation {
        public Observation(String name, Location loc) {
            this.name = name;
            this.location = loc;
            this.observed = Instant.now();
        }

        @Getter
        private String name;
        @Getter
        private Instant observed;
        @Getter
        private Location location;
    }

    private final int SPEED;
    private final int PLAN_AHEAD;

    private Location _myLocation;
    private Location _wolfLocation;
    private Map<String, Observation> _observations = new HashMap<>(30, 0.6f);
    private Set<Location> _predictedSheepLocations = new HashSet<>(30, 0.6f);

    public LocationEstimator(int speed, int planAhead) {
        SPEED = speed;
        PLAN_AHEAD = planAhead;
    }

    public void addPlayer(String name, Location location, String currenName) {
        if (name.equals(currenName)) {
            _myLocation = location;
            return;
        }
        if (name.contains("WolfBot")) {
            _wolfLocation = location;
            return;
        }
        _observations.put(name, new Observation(name, location));
    }

    public void estimateLocations() {
        if (_myLocation == null || _wolfLocation == null) {
            return;
        }

        Instant to = Instant.now();
        _predictedSheepLocations.clear();
        _observations.entrySet()
                     .removeIf(p -> Duration.between(p.getValue().observed, to).toMillis() > DONT_SEE_MS_DELETE);
        _observations.forEach((name, observation) -> {
            double timeSpan = (double) Duration.between(observation.observed, to).toMillis() / 1000.0;
            Location from = _myLocation.getDistance(observation.location) < _wolfLocation.getDistance(observation.location) ?
                    _myLocation : _wolfLocation;
            Location movementDirection = observation.location.sub(from);
            double powerBy = (double) SPEED * timeSpan;
            Location finalMovement = movementDirection.getNormalized().scale(powerBy);
            _predictedSheepLocations.add(
                    observation.location.add(finalMovement)
            );
        });
        return;
    }

    private double pointLineDistance(Location point, Location pointOnLine, Location lineDirection) {
        Location secondPoint = pointOnLine.add(lineDirection);
        return (point.sub(pointOnLine).cross(point.sub(secondPoint))).getLength() / (secondPoint.sub(pointOnLine)).getLength();
    }

    public Location findNextNav(Iterable<NavPoint> points, int planAhead) {
        double maxDistance = (double) SPEED * (double) planAhead;
        return StreamSupport.stream(points.spliterator(), false)
                            .filter(p -> p.getLocation().getDistance(_myLocation) < maxDistance)
                            .min(Comparator.comparingDouble(p -> optFitness(p.getLocation())))
                            .get()
                            .getLocation();

    }

    public double optFitness(Location point) {
        double sheepSums = _predictedSheepLocations.stream()
                                                   .mapToDouble(l -> l.getDistance(point))
                                                   //.mapToDouble(p -> _wolfLocation.sub(point).cross(p.sub(point)).getLength() * 0.5)
                                                   .sum();
        double wolfDistance = Math.pow(Math.abs(_wolfLocation.getDistance(point) - IDEAL_WOLF_DISTANCE), 1.0);
        return sheepSums * SHEEP_DISTANCE_WEIGHTS + wolfDistance * WOLF_DISTANCE_WEIGHT;
    }

}
