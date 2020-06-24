package vip.bot.behavior;

import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;

public abstract class BaseReflex implements IReflex {

    protected UT2004BotVIPController _bot;

    public BaseReflex(UT2004BotVIPController bot){
        _bot = bot;
    }

}
