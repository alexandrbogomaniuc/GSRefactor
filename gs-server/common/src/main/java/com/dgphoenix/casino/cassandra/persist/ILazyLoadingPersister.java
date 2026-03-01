package com.abs.casino.cassandra.persist;

import java.util.Map;

/**
 * Created by grien on 06.03.15.
 */
public interface ILazyLoadingPersister<KEY, ENTRY> {
    void persist(KEY key, ENTRY entry);

    void delete(KEY key, ENTRY entry);

    ENTRY get(KEY key);

    Map<KEY, ENTRY> getAllAsMap();

    Map<KEY, ENTRY> getAsMap(Integer bankId);
}
