package com.betsoft.casino.mp.common.testmodel;

import com.dgphoenix.casino.common.util.ISequencer;
import com.dgphoenix.casino.common.util.ISequencerPersister;

/**
 * User: flsh
 * Date: 20.02.2020.
 */
public class StubSequencerPersister implements ISequencerPersister {
    @Override
    public ISequencer getOrCreateSequencer(String name) {
        return new ISequencer() {
            long id;

            @Override
            public void init() {
                id = 0;
            }

            @Override
            public long getValue() {
                return id;
            }

            @Override
            public void setValue(long l) {
                id = l;
            }

            @Override
            public long getAndIncrement() {
                return id++;
            }

            @Override
            public String getName() {
                return "Test";
            }

            @Override
            public void setName(String s) {

            }

            @Override
            public void shutdownAllocator() {

            }
        };
    }

    @Override
    public ISequencer importSequencer(ISequencer seq) {
        return null;
    }
}
