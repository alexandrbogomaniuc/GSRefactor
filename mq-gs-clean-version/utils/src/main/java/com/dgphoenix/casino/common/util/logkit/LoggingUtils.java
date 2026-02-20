package com.dgphoenix.casino.common.util.logkit;

/**
 * User: Grien
 * Date: 13.01.2015 16:33
 */
public class LoggingUtils {

    private LoggingUtils() {
    }

    public static void initializeGameLog() {
        GameLog.getInstance().initialize(new GameLogger());
    }

}
