package com.betsoft.casino.mp.events;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.logkit.ThreadLog;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: flsh
 * Date: 11.11.17.
 */
public class TimedEventManager extends Thread implements IEventManager {
    private Collection<ITimedEvent> events;
    private long wakeUpTime;
    private long timeout;
    private boolean shutdown;
    private final long tableId;

    public TimedEventManager(long tableId) {
        this.events = new ArrayList<ITimedEvent>();
        this.timeout = 0l;
        this.wakeUpTime = 0l;
        setDaemon(true);
        this.tableId = tableId;
        setName("TimedEventManager_" + tableId);
    }

    public long getTableId() {
        return tableId;
    }

    public synchronized void add(IEvent event) {
        ThreadLog.debug("Add event " + event);
        event.setManager(this);
        events.add((ITimedEvent) event);
    }

    public synchronized void remove(IEvent event) {
        ThreadLog.debug("Remove event " + event);
        if (events.remove((ITimedEvent) event)) {
            event.setManager(null);
        }
    }

    public synchronized void shutdown() {
        shutdown = true;
        notify();
    }

    public synchronized void notify(long waitingPeriod) {
        long now = System.currentTimeMillis();
        long newWakeUp = now + waitingPeriod;
        if (newWakeUp < wakeUpTime || wakeUpTime <= 0l) {
            timeout = waitingPeriod;
            notify();
        }
    }

    public void run() {
        try {
            while (!shutdown) {
                ITimedEvent event = getNextEvent();
                if (!event.isPaused() && !event.isStopped()) {
                    try {
                        event.stopTimer();
                        try {
                            event.occur();
                        } catch (Exception e) {
                            ThreadLog.error("Unable to process event", e);
                        } finally {
                            if (event.isPeriodical() &&
                                    event.isRegistered()) {
                                event.startTimer();
                            }
                        }
                    } catch (CommonException e) {
                        ThreadLog.error("Unable to handle event", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            ThreadLog.info("interrupted");
        }
    }

    private synchronized ITimedEvent getNextEvent()
            throws InterruptedException {
        timeout = 0l;
        wakeUpTime = 0l;
        while (true) {
            checkShutdown();
            long originTime = System.currentTimeMillis();
            if (timeout <= 0) {
                for (Object event1 : events) {
                    final ITimedEvent event = (ITimedEvent) event1;
                    //noinspection SynchronizationOnLocalVariableOrMethodParameter
                    synchronized (event) {
                        if (!event.isPaused() && !event.isStopped()) {
                            long elapsed = event.getElapsed(originTime);
                            long timeout = event.getTime() - elapsed;
                            if (timeout <= 0) {
                                return event;
                            } else if (timeout < this.timeout ||
                                    this.timeout == 0l) {
                                this.timeout = timeout;
                            }
                        }
                    }
                }
            }
            if (timeout == 0l) {
                wakeUpTime = 0l;
                //log.trace("Sleeping for infinity");
                wait();
            } else {
                long now = System.currentTimeMillis();
                timeout = timeout - (now - originTime);
                if (timeout > 0) {
                    wakeUpTime = now + timeout;
                    long tmp = timeout;
                    timeout = 0l;
                    //log.trace("Sleeping for " + tmp);
                    wait(tmp);
                }
            }
        }
    }

    private void checkShutdown()
            throws InterruptedException {
        if (shutdown) {
            throw new InterruptedException("Self interruption");
        }
    }

}
