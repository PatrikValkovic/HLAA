package hlaa.tdm.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class LogitMeasure {

    private Instant _start;

    public void start(){
        _start = Instant.now();
    }

    public void end(boolean print){
        end(print, 0);
    }

    public void end(boolean print, int printTime){
        end(print, printTime, null);
    }

    public void end(boolean print, Logger log){
        end(print, 0, log);
    }

    public void end(boolean print, int printTime, Logger log){
        Duration diff = Duration.between(_start, Instant.now());
        if(print && diff.toMillis() > printTime){
            String message = "Logit took " + (double)diff.toMillis() + " ms";
            Consumer<String> callback = log == null ? System.out::println : log::warning;
            callback.accept(message);
        }
    }

}
