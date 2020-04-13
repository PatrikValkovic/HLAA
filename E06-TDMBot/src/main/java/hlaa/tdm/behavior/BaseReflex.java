package hlaa.tdm.behavior;

import cz.cuni.amis.pogamut.ut2004.teamcomm.bot.UT2004BotTCController;

public abstract class BaseReflex implements IReflex {

    protected UT2004BotTCController _bot;

    public BaseReflex(UT2004BotTCController bot){
        _bot = bot;
    }

}
