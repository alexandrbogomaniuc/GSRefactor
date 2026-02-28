package com.abs.casino.common.util;

import java.util.Map;

/**
 * User: flsh
 * Date: 19.03.13
 */
public interface IIntegerIdGenerator {

    int getNext(Class klass);

    int getNext(String sequencerName);

    Map<String, IIntegerSequencer> getAllObjects();

    void init(IIntegerSequencerPersister persister);

    void shutdownBlockAllocators();
}
