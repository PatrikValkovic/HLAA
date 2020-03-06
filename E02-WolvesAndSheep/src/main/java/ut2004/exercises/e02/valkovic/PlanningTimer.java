package ut2004.exercises.e02.valkovic;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class PlanningTimer {

    private final int PLAN_EVERY;
    private Instant _lastPlanning = Instant.now().minus(1, ChronoUnit.HOURS);

    public PlanningTimer(int planEveryMilliseconds){
        PLAN_EVERY = planEveryMilliseconds;
    }

    public boolean shouldReplan() {
        return Duration.between(_lastPlanning, Instant.now()).toMillis() > PLAN_EVERY;
    }

    public void replan(){
        _lastPlanning = Instant.now();
    }

}
