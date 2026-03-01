package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.ExtendedAccountInfoPersister;


public final class ExtendedAccountInfoPersisterInstanceHolder {
    private static ExtendedAccountInfoPersister persister;

    private ExtendedAccountInfoPersisterInstanceHolder() {
    }

    public static ExtendedAccountInfoPersister getPersister() {
        return persister;
    }

    public static void setPersister(ExtendedAccountInfoPersister persister) {
        ExtendedAccountInfoPersisterInstanceHolder.persister = persister;
    }
}
