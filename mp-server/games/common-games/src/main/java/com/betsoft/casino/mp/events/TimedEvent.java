package com.betsoft.casino.mp.events;

import com.dgphoenix.casino.common.exception.CommonException;

import java.util.Date;

/**
 * User: flsh
 * Date: 11.11.17.
 */
public abstract class TimedEvent implements ITimedEvent {
    private long startTime;
    private long originTime;
    private long time;
    private long elapsed;

    private boolean periodical;
    private boolean stopped;
    private boolean paused;

    private IEventManager manager;

    public TimedEvent(long time, boolean periodical) {
        this.periodical = periodical;
        this.stopped = true;
        this.paused = false;
        this.time = time;
    }

    public void startTimer()
            throws CommonException {
        synchronized (this) {
            assertRegistered();
            assertNotStarted();

            startTime = System.currentTimeMillis();
            originTime = startTime;
            elapsed = 0l;
            stopped = false;
            paused = false;
        }
        manager.notify(time);
    }

    public synchronized void stopTimer()
            throws CommonException {
        assertRegistered();
        stopped = true;
        paused = false;
        elapsed = 0l;
        startTime = 0l;
        originTime = 0l;
    }

    public synchronized void pauseTimer()
            throws CommonException {
        assertRegistered();
        paused = true;
        elapsed = elapsed + System.currentTimeMillis() - startTime;
    }

    public void continueTimer()
            throws CommonException {
        synchronized (this) {
            assertRegistered();
            if (!isPaused()) {
                throw new CommonException("Can't continue not paused timer");
            }
            paused = false;
            startTime = System.currentTimeMillis();
        }
        manager.notify(time - elapsed);
    }

    public synchronized boolean isStopped() {
        return stopped;
    }

    public synchronized boolean isPaused() {
        return paused;
    }

    public void setTime(long time)
            throws CommonException {
        assertNotStarted();
        if (time <= 0) {
            throw new CommonException("Can't set negative or zero time");
        }
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public synchronized long getElapsed(long sinceTime) {
        if (isPaused() || isStopped()) {
            return elapsed;
        } else {
            return elapsed + (sinceTime - startTime);
        }
    }

    public long getElapsed() {
        return getElapsed(System.currentTimeMillis());
    }

    public void setPeriodical(boolean periodical)
            throws CommonException {
        assertNotStarted();
        this.periodical = periodical;
    }

    public boolean isPeriodical() {
        return periodical;
    }

    public void setManager(IEventManager manager) {
        this.manager = manager;
    }

    public boolean isRegistered() {
        return manager != null;
    }

    private void assertRegistered()
            throws CommonException {
        if (manager == null) {
            throw new CommonException("Register this event first");
        }
    }

    private void assertNotStarted()
            throws CommonException {
        if (!isStopped()) {
            throw new CommonException("Can't change timer on started event");
        }
    }

    public void restart() throws CommonException {
        stopTimer();
        startTimer();
    }

    public String toString() {
        return "[" + getClass().getSimpleName()
                + " time=" + time
                + ", elapsed=" + getElapsed()
                + ", startTime=" + new Date(startTime)
                + ", originTime=" + new Date(originTime)
                + ", paused=" + paused
                + ", stopped=" + stopped + "]";
    }

}
