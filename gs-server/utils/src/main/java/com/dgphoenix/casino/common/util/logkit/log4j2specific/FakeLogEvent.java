package com.abs.casino.common.util.logkit.log4j2specific;

import org.apache.logging.log4j.core.AbstractLogEvent;

import java.util.Map;

/**
 * User: Grien
 * Date: 24.11.2014 15:45
 */
public class FakeLogEvent extends AbstractLogEvent {
    private final Map<String, String> contextMap;

    public FakeLogEvent(Map<String, String> contextMap) {
        this.contextMap = contextMap;
    }

    @Override
    public Map<String, String> getContextMap() {
        return contextMap;
    }
}
