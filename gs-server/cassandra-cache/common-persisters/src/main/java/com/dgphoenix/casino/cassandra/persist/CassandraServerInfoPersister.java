package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.LoadBalancerCache;
import com.abs.casino.common.cache.data.server.IServerInfoInternalProvider;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * User: flsh
 * Date: 8/10/12
 */
public class CassandraServerInfoPersister extends AbstractLongDistributedConfigEntryPersister<ServerInfo>
        implements IServerInfoInternalProvider {

    private static final String SERVER_INFO_CF = "SrvInfoCF";
    private static final String VOTE_MASTER_CF = "VoteSrvMasterCF";
    private static final Logger LOG = LogManager.getLogger(CassandraServerInfoPersister.class);
    private static final String GS_ID_FIELD = "GsId";

    private static final TableDefinition VOTE_MASTER_TABLE = new TableDefinition(VOTE_MASTER_CF,
            Arrays.asList(
                    new ColumnDefinition(KEY, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(GS_ID_FIELD, com.datastax.driver.core.DataType.cint(), false, false, false)
            ), Collections.singletonList(KEY));

    private CassandraServerInfoPersister() {
        super();
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(getMainTableDefinition(), VOTE_MASTER_TABLE);
    }


    @Override
    public String getMainColumnFamilyName() {
        return SERVER_INFO_CF;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public Map<Long, ServerInfo> getAllServers() {
        Map<Long, ServerInfo> map = loadAllAsMap(ServerInfo.class);
        if (map == null) {
            return new HashMap<>();
        }
        return map;
    }

    public void persist(ServerInfo serverInfo) {
        //LOG.info("serverInfo " + serverInfo);
        String json = getMainTableDefinition().serializeToJson(serverInfo);
        ByteBuffer byteBuffer = getMainTableDefinition().serializeToBytes(serverInfo);
        try {
            com.datastax.driver.core.querybuilder.Insert query = addInsertion(serverInfo.getId(), SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(JSON_COLUMN_NAME, json);
            execute(query, "persist", com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE);
        } finally {
            releaseBuffer(byteBuffer);
        }
    }

    @Override
    public int loadAll() {
        //not load to cache, periodicaly load by GameServer.ServerInfoUpdaterTask
        return 0;
    }

    @Override
    public void saveAll() {
        //nop
    }

    @Override
    public ServerInfo get(String id) {
        return get(id, ServerInfo.class);
    }

    @Override
    public LoadBalancerCache getCache() {
        return LoadBalancerCache.getInstance();
    }

}
