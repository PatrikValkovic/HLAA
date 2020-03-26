package hlaa.duelbot.utils;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;


public class SpawnItemHelper {

    public SpawnItemHelper(@NonNull Location location, @NonNull double spawnTime, @NonNull Item item) {
        this.location = location;
        this.spawnTime = spawnTime * 1000.0;
        this.item = item;
        lastUnseen = Instant.now().minus((long)this.spawnTime, ChronoUnit.MILLIS);
        spawnProb = 1;
    }

    @Getter
    @NonNull
    Location location;

    @Getter
    @NonNull
    double spawnTime;

    @Getter
    @NonNull
    Item item;

    @Setter
    @Getter
    Instant lastUnseen;

    @Setter
    @Getter
    double spawnProb;

    public double getCurrentSpawnProb(){
        return Math.min(1.0, spawnProb + (double)Duration.between(lastUnseen, Instant.now()).toMillis() / spawnTime);
    }
}
