package com.dgphoenix.casino.common.util.logkit;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringBuilderWriter;

import java.io.PrintWriter;

/**
 * Created by ANGeL
 * Date: Sep 16, 2008
 * Time: 5:35:50 PM
 */
public class LogUtils {
    private LogUtils() {
    }

    public static String markException(String message) {
        return stackTrace("NOT AN EXCEPTION, JUST STACKTRACING. Error message: " + message);
    }

    public static String stackTrace(String message) {
        //noinspection ThrowableInstanceNeverThrown
        return stackTrace(message, new CommonException(message));
    }

    public static String stackTrace(String message, Throwable ex) {

        final StringBuilderWriter sw = new StringBuilderWriter();
        final PrintWriter out = new PrintWriter(sw);

        try {
            if (message != null) {
                out.print(message);
                out.print(": ");
            }
            ex.printStackTrace(out);
            return sw.toString();
        } finally {
            out.close();
        }
    }

    public static String stackTrace(StackTraceElement[] trace) {
        StringBuilder s = new StringBuilder();
        for (StackTraceElement aTrace : trace) {
            s.append("\tat ").append(aTrace).append("\n");
        }
        return s.toString();
    }

    public static String dumpThread(Thread t) {
        if (t == null) {
            return "thread is null";
        }
        StackTraceElement[] stackTrace = t.getStackTrace();
        if (stackTrace.length == 0) {
            return "stacktrace empty";
        }
        return stackTrace(stackTrace);
    }

}
