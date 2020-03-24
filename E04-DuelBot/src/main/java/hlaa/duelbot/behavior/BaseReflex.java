package hlaa.duelbot.behavior;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;

public abstract class BaseReflex implements IReflex {

    protected UT2004BotModuleController _bot;

    public BaseReflex(UT2004BotModuleController bot){
        _bot = bot;
    }

}
