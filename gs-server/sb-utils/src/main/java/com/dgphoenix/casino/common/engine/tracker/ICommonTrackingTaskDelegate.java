package com.abs.casino.common.engine.tracker;

import com.abs.casino.common.exception.CommonException;
import com.esotericsoftware.kryo.KryoSerializable;
import org.apache.log4j.Logger;

/**
 * User: Grien
 * Date: 02.04.2014 12:30
 */
public interface ICommonTrackingTaskDelegate extends KryoSerializable {
    void process(String key, AbstractCommonTracker tracker) throws CommonException;

    long getTaskSleepTimeout() throws CommonException;

    abstract Logger getLog();
}