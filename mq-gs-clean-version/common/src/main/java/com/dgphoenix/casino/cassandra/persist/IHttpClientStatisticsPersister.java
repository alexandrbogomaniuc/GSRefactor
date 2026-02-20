package com.dgphoenix.casino.cassandra.persist;

/**
 * Created by mic on 02.02.15.
 */
public interface IHttpClientStatisticsPersister {
    void persist(String date, String url, boolean isSuccess, long amount);
}
