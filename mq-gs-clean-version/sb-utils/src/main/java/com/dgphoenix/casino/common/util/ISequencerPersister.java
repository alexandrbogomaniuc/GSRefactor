package com.dgphoenix.casino.common.util;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 19.03.13
 */
public interface ISequencerPersister {
    ISequencer getOrCreateSequencer(String name) throws CommonException;
    ISequencer importSequencer(ISequencer seq) throws CommonException;
}
