package ut2004.exercises.e02.valkovic;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import lombok.Getter;

public class PlanningThread implements Runnable {

    private static final int MAX_DONTSEE_EXCLUDE = 2;
    private static final int MIN_PLAN_DELAY = 500;
    private final Map<String, Integer> _seen = new HashMap<>(30, 0.6f);

    private final AtomicBoolean _keepRuning = new AtomicBoolean(true);
    private final NavigationModule _navModule;
    private final Map<String, Location> _lastKnownLocations;
    private final Object _lastKnowLocationsMutex;
    @Getter
    private final AtomicReference<Location> _currentLocation = new AtomicReference<>();
    private final Logger log;
    private final int PLAN_AHEAD;
    @Getter
    private final AtomicReference<String> _otherWolf = new AtomicReference<>();

    public PlanningThread(NavigationModule _navModule, Map<String, Location> lastKnownLocations, Object lastKNowLocationsMutex, Logger log, int plan_ahead) {
        this._navModule = _navModule;
        this._lastKnownLocations = lastKnownLocations;
        this._lastKnowLocationsMutex = lastKNowLocationsMutex;
        this.log = log;
        PLAN_AHEAD = plan_ahead;
    }

    @Override
    public void run() {
        Instant lastExecution = Instant.now().minus(1, ChronoUnit.HOURS);
        while(_keepRuning.get()){
            long timeBetween = Duration.between(lastExecution, Instant.now()).toMillis();
            if(timeBetween < MIN_PLAN_DELAY) {
                try {
                    Thread.sleep(MIN_PLAN_DELAY - timeBetween);
                }
                catch (InterruptedException ignore) {
                }
            }

            lastExecution = Instant.now();
            Map<String, Location> lastKnownLocationsCopy;
            synchronized (_lastKnowLocationsMutex){
                lastKnownLocationsCopy = new HashMap<>(30, 0.6f);
                lastKnownLocationsCopy.putAll(_lastKnownLocations);
                _lastKnownLocations.clear();
            }

            // compute seen
            for(String k : _seen.keySet())
                _seen.put(k, _seen.get(k) + 1);
            for(String k : lastKnownLocationsCopy.keySet())
                _seen.put(k, 0);
            for(String k : _seen.keySet())
                if(_seen.get(k) >= MAX_DONTSEE_EXCLUDE)
                    _navModule.removeEstimationFor(k, log);


            Instant start = Instant.now();
            _navModule.updateLocations(lastKnownLocationsCopy);
            _navModule.computeNextPoint(_currentLocation.get(), PLAN_AHEAD, _otherWolf.get(), log);
            log.info("Planning took " + Duration.between(start, Instant.now()).toMillis() + " ms");
        }
    }

    public void stop() {
        _keepRuning.set(false);
    }
}
