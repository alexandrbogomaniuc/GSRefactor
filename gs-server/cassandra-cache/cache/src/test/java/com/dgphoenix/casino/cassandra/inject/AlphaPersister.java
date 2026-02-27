package com.abs.casino.cassandra.inject;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 08.08.2022
 */
public class AlphaPersister extends AbstractPersister {

    private BravoPersister bravoPersister;
    private CharliePersister charliePersister;

    private void setBravoPersister(BravoPersister bravoPersister) {
        this.bravoPersister = bravoPersister;
    }

    private void setCharliePersister(CharliePersister charliePersister) {
        this.charliePersister = charliePersister;
    }
}
