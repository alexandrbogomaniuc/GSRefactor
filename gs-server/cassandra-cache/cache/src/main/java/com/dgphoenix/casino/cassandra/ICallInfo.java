package com.abs.casino.cassandra;

import com.esotericsoftware.kryo.KryoSerializable;

/**
 * User: flsh
 * Date: 01.10.2021.
 */
public interface ICallInfo extends KryoSerializable {
    long getRequestTime();

    long getRequestId();

    String getCallerId();

    String getMethodName();
}
