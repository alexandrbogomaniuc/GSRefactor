package com.betsoft.casino.mp.events;

import com.dgphoenix.casino.common.exception.CommonException;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 11.11.17.
 */
public interface IEvent extends Serializable {
    void setManager(IEventManager manager);

    boolean isRegistered();

    void occur() throws CommonException;
}
