package com.betsoft.casino.mp.events;

/**
 * User: flsh
 * Date: 11.11.17.
 */
public interface IEventManager {
    void start();

    void shutdown();

    void add(IEvent event);

    void remove(IEvent event);

    void notify(long waitingPeriod);
}
