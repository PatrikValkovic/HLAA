package ut2004.exercises.e02.valkovic;

public class InitialScan {

    private final int ROTATION_DEGREE;
    private final int TOTAL_ROTATIONS;
    private int _rotations = 0;

    public InitialScan(int rotationDegree){
        ROTATION_DEGREE = rotationDegree;
        TOTAL_ROTATIONS = (int)Math.ceil(360.0f / (float)rotationDegree);
    }

    public int getRotation(){
        return ROTATION_DEGREE;
    }

    public void rotationPerformed(){
        _rotations++;
    }

    public boolean isDone(){
        return _rotations >= TOTAL_ROTATIONS;
    }

}
