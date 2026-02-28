package com.abs.casino.common.util.logkit;

import org.apache.log4j.Logger;

/**
 * Created by ANGeL
 * Date: Sep 16, 2008
 * Time: 5:11:09 PM
 */
public class ThreadLog {
    private static final Logger LOG = Logger.getLogger(ThreadLog.class);

    protected ThreadLog() {
    }

    public static Logger log() {
        return LOG;
    }

    public static void info(String message) {
        log().info(message);
    }

    public static void info(String message, Throwable ex) {
        if (ex instanceof Error) {
            fatal(message, ex);
        } else {
            log().info(LogUtils.stackTrace(message, ex));
        }
    }

    public static void warn(String message) {
        log().warn(message);
    }

    public static void warn(String message, Throwable ex) {
        log().warn(LogUtils.stackTrace(message, ex));
    }

    public static void debug(String message) {
        if(LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
    }

    public static void debug(String message, Throwable ex) {
        if (ex instanceof Error) {
            fatal(message, ex);
        } else if (LOG.isDebugEnabled()) {
            LOG.debug(LogUtils.stackTrace(message, ex));
        }
    }

    public static void error(String message) {
        log().error(message);
    }

    public static void error(String message, Throwable ex) {
        if (ex instanceof Error) {
            fatal(message, ex);
        } else {
            log().error(LogUtils.stackTrace(message, ex));
        }
    }

    public static void fatal(String message) {
        log().fatal(message);
    }

    public static void fatal(String message, Throwable ex) {
        log().fatal(LogUtils.stackTrace(message, ex));
    }

    public static void markException(String message) {
        log().debug(LogUtils.stackTrace("NOT AN EXCEPTION, JUST STACKTRACING. Error message: " + message));
    }
}
