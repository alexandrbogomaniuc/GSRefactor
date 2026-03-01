package com.abs.casino.common.util;

import com.abs.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 19.03.13
 */
public interface IIntegerSequencerPersister {
    IIntegerSequencer getOrCreateSequencer(String name) throws CommonException;
    IIntegerSequencer importSequencer(IIntegerSequencer seq) throws CommonException;
}
