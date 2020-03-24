package hlaa.duelbot.utils;

import java.time.Duration;
import java.time.Instant;

public class DeltaCounter {

    private Instant _lastExecution;

    public DeltaCounter(){
    }

    public float getDelta() {
        if(_lastExecution == null){
            _lastExecution = Instant.now();
            return 0.0f;
        }
        Instant now = Instant.now();
        float delta = (float)Duration.between(_lastExecution, now).toMillis() / 1000.0f;
        _lastExecution = now;
        return delta;
    }
}
