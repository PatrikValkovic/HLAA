package vip.bot.utils;

import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HandmadeNav {

    public static final double REACHED_DISTANCE = 50;
    public static final int STUCK_MOVES_TO_CONSIDER = 3;
    public static final double STUCK_DISTANCE = 100;

    private final ArrayList<ILocated> _path;
    private final LinkedList<ILocated> _history = new LinkedList<>();
    private int _currentTarget = 0;

    public HandmadeNav(List<ILocated> path){
        _path = new ArrayList<>(path);
    }

    public ILocated getTargetLocation(){
        return _path.get(_path.size()-1);
    }

    public boolean isDone(){
        return _currentTarget >= _path.size();
    }

    public int resultingLength(){
        return _path.size() - _currentTarget - 1;
    }

    public ILocated getCurrentTarget(){
        return _path.get(_currentTarget);
    }
    public ILocated getNextTarget(){
        return _path.get(_currentTarget + 1);
    }

    public void navigate(UT2004BotVIPController bot){

        _history.add(bot.getInfo().getLocation());
        while(_history.size() > STUCK_MOVES_TO_CONSIDER)
            _history.remove(0);
        if(_history.size() == STUCK_MOVES_TO_CONSIDER &&
                Navigation.directDistance(_history.get(0), _history.get(STUCK_MOVES_TO_CONSIDER-1)) < STUCK_DISTANCE){
            bot.getLog().warning("Stuck detected");
            _currentTarget++;
        }

        while(!this.isDone() && Navigation.directDistance(bot.getInfo(), this.getCurrentTarget()) < REACHED_DISTANCE){
            _currentTarget++;
        }

        if(this.isDone())
            return;


        if(this.resultingLength() >= 2) {
            bot.getMove().moveAlong(this.getCurrentTarget(), this.getNextTarget());
        }
        else {
            bot.getMove().moveTo(this.getCurrentTarget());
        }
    }
}
