package com.dgphoenix.casino.cassandra.persist;

import java.util.Map;

/**
 * Created by mic on 12.01.15.
 */
public interface ExtendedAccountInfoPersister {
    String get(long bankId, String externalId, String propertyName);

    Map<String, String> get(long bankId, String externalId);

    void persist(long bankId, String externalId, Map<String, String> properties);

    void persist(long bankId, String externalId, String propertyName, String value);
}
