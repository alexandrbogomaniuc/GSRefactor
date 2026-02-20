package com.dgphoenix.casino.common.util;

import java.util.Map;

/**
 * User: flsh
 * Date: 19.03.13
 */
public interface ILongIdGenerator {

    long getNext(Class klass);

    long getNext(String sequencerName);

    Map<String, ISequencer> getAllObjects();

    void init(ISequencerPersister persister);

    void shutdownBlockAllocators();
}
