package com.dgphoenix.casino.common.util.logkit;

import org.apache.log4j.Logger;

/**
 * User: Grien
 * Date: 12.01.2015 18:03
 */
public interface IGameLogger {
    final static String DEFAULT_LOGGER = "game_common";
    final static String DEFAULT_ERROR_LOGGER = "game_common_error";
    final static String JACKPOT_LOGGER = "jackpot";

    Logger log(String loggerName, boolean isFreeMode);

    void info(String logName, String message, IContextProvider provider);

    void warn(Logger logger, String message, IContextProvider provider);

    void warn(String logName, String message, IContextProvider provider);

    void debug(Logger logger, String message, IContextProvider provider);

    void debug(String loggerName, String message, IContextProvider provider);

    void error(Logger logger, String message, IContextProvider provider);

    void error(String loggerName, String message, IContextProvider provider);

    void error(Logger logger, String message, IContextProvider provider, Throwable ex);

    void error(String logger, String message, IContextProvider provider, Throwable ex);

    void fatal(Logger logger, String message, IContextProvider provider);

    void fatal(String logger, String message, IContextProvider provider);

    void fatal(Logger logger, String message, IContextProvider provider, Throwable ex);

    void fatal(String logger, String message, IContextProvider provider, Throwable ex);

    public static interface IContextProvider {
        String getContextInfo();
        boolean isFreeMode();
    }
}