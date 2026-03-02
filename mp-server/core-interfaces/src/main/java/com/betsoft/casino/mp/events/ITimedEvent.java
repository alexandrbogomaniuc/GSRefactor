package com.betsoft.casino.mp.events;

import com.abs.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 11.11.17.
 */
public interface ITimedEvent extends IEvent {
    void startTimer() throws CommonException;

    void stopTimer() throws CommonException;

    void pauseTimer() throws CommonException;

    void continueTimer() throws CommonException;

    boolean isStopped();

    boolean isPaused();

    void setTime(long time) throws CommonException;

    long getTime();

    void setPeriodical(boolean bPeriodical) throws CommonException;

    boolean isPeriodical();

    long getElapsed(long sinceTime);

    long getElapsed();
}
