package com.dgphoenix.casino.common.util;

/**
 * Created
 * Date: 19.11.2008
 * Time: 17:37:07
 */
public interface Controllable {
    boolean startup();

    boolean isStarted();

    boolean isRunning();

    boolean shutdown();
}
