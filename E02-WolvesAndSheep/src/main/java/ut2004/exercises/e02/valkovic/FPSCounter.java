package ut2004.exercises.e02.valkovic;

import java.time.Duration;
import java.time.Instant;

public class FPSCounter {
    private Instant lastTick = Instant.now();
    private double delta = 0.0;

    public void tick() {
        Instant n = Instant.now();
        delta = Duration.between(lastTick, n).toMillis() / 1000.0;
        lastTick = n;
    }

    public double getDelta() {
        return delta;
    }
}
