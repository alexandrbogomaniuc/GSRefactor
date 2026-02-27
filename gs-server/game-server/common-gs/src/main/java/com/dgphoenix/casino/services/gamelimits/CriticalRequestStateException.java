package com.abs.casino.services.gamelimits;

/**
 * @author <a href="mailto:dader@dgphoenix.com">Timur Shaymardanov</a>
 * @since 11.02.2020
 */
public class CriticalRequestStateException extends RuntimeException {

    public CriticalRequestStateException(String message, Object... args) {
        super(String.format(message, args));
    }
}
