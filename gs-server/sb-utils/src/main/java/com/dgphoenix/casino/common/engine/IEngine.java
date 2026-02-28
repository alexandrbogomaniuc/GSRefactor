package com.abs.casino.common.engine;

import com.abs.casino.common.util.Controllable;

/**
 * Created
 * Date: 24.11.2008
 * Time: 15:35:24
 */
public interface IEngine extends Controllable {
    void pause();
    void resume();
    boolean isPaused();
    void setSleepInterval(long interval);
    long getSleepInterval();
}
