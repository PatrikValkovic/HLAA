package hlaa.duelbot.behavior;

public interface IBehavior {

    boolean isFiring();

    double priority();

    void execute();
}
