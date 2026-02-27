package com.abs.casino.common.util.web;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: flsh
 * Date: 9/30/11
 */
public class HttpClientConnectionStatistics implements Serializable {
    private AtomicInteger timeouts = new AtomicInteger();
    private AtomicInteger emptyResponces = new AtomicInteger();
    private AtomicInteger unclassifiedErrors = new AtomicInteger();
    private AtomicInteger error503 = new AtomicInteger();
    private AtomicInteger error500 = new AtomicInteger();
    private AtomicInteger errorUnclassified = new AtomicInteger();
    private AtomicInteger success = new AtomicInteger();
    private AtomicInteger loginErrorByGameSessionsLimit = new AtomicInteger();
    private AtomicInteger longRequests = new AtomicInteger();

    public HttpClientConnectionStatistics() {
    }

    public AtomicInteger getTimeouts() {
        return timeouts;
    }

    public AtomicInteger getEmptyResponces() {
        return emptyResponces;
    }

    public AtomicInteger getUnclassifiedErrors() {
        return unclassifiedErrors;
    }

    public AtomicInteger getError503() {
        return error503;
    }

    public AtomicInteger getError500() {
        return error500;
    }

    public AtomicInteger getErrorUnclassified() {
        return errorUnclassified;
    }

    public AtomicInteger getSuccess() {
        return success;
    }

    public AtomicInteger getLoginErrorByGameSessionsLimit() {
        return loginErrorByGameSessionsLimit;
    }

    public AtomicInteger getLongRequests() {
        return longRequests;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("HttpClientConnectionStatistics");
        sb.append("[timeouts=").append(timeouts.get());
        sb.append(", emptyResponces=").append(emptyResponces.get());
        sb.append(", unclassifiedErrors=").append(unclassifiedErrors.get());
        sb.append(", error503=").append(error503.get());
        sb.append(", error500=").append(error500.get());
        sb.append(", errorUnclassified=").append(errorUnclassified.get());
        sb.append(", success=").append(success.get());
        sb.append(", loginErrorByGameSessionsLimit=").append(loginErrorByGameSessionsLimit.get());
        sb.append(", longRequests=").append(longRequests.get());
        sb.append(']');
        return sb.toString();
    }
}
