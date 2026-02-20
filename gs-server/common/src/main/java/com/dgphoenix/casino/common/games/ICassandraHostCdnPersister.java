package com.dgphoenix.casino.common.games;

import java.util.List;

/**
 * Created by inter on 18.08.15.
 */
public interface ICassandraHostCdnPersister {

    List<CdnCheckResult> getCdnByIp(String ip);

    void remove(String ip, String cdn);

    void persist(String ip, String cdn, int time);
}
