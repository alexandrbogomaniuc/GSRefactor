package com.betsoft.casino.bots.requests;

import org.apache.logging.log4j.Logger;

/**
 * User: flsh
 * Date: 18.09.18.
 */
public abstract class AbstractBotRequest implements IBotRequest {
    private final long requestStartTime = System.currentTimeMillis();
    private final Logger logger;

    protected AbstractBotRequest(Logger logger) {
        this.logger = logger;
    }

    @Override
    public long getRequestStartTime() {
        return requestStartTime;
    }

    public Logger getLogger() {
        return logger;
    }
}
