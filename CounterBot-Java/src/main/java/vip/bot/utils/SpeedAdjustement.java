package vip.bot.utils;

import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.vip.bot.UT2004BotVIPController;

public class SpeedAdjustement {

    /**
     * Speed of the VIP bot.
     */
    public static final double VIPBOT_SPEED = 0.6;
    /**
     * Normal speed of the bot (i.e., one that the bot initially has).
     */
    public static final double NORMAL_SPEED = 1.0;

    /**
     * Changes the speed of the bot to match the speed of VIP
     */
    public static void setVIPSpeed(UT2004BotVIPController<UT2004Bot> bot) {
        bot.getConfig().setSpeedMultiplier(VIPBOT_SPEED);
    }

    /**
     * Changes the speed of the bot back to normal.
     */
    public static void setNormalSpeed(UT2004BotVIPController<UT2004Bot> bot) {
        bot.getConfig().setSpeedMultiplier(NORMAL_SPEED);
    }

    public static boolean isVIPSpeed(UT2004BotVIPController<UT2004Bot> bot) {
        return Math.abs(bot.getConfig().getSpeedMultiplier() - VIPBOT_SPEED) < 0.001;
    }

    public static boolean isNormalSpeed(UT2004BotVIPController<UT2004Bot> bot) {
        return Math.abs(bot.getConfig().getSpeedMultiplier() - NORMAL_SPEED) < 0.001;
    }

}
