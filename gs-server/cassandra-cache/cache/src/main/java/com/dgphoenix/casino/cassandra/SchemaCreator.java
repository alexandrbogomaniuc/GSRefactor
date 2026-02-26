package com.dgphoenix.casino.cassandra;

import com.dgphoenix.casino.cassandra.config.ClusterConfig;
import com.dgphoenix.casino.cassandra.persist.engine.ICassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.configuration.ConfigHelper;
import com.dgphoenix.casino.common.exception.CommonException;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 16.09.16
 */
public class SchemaCreator {

    private static final Logger LOG = LogManager.getLogger(SchemaCreator.class);
    private final ClusterConfig config;
    private final String cqlFilename;

    public SchemaCreator(ClusterConfig config, String cqlFilename) {
        checkNotNull(config, "com.datastax.driver.core.Cluster config should be not null");
        this.config = config;
        this.cqlFilename = cqlFilename;
    }

    public void createSchema(com.datastax.driver.core.Session session, List<ICassandraPersister> persisters) {
        String keyspaceName = config.getKeySpace();
        LOG.warn("Begin schema creation for: {}", keyspaceName);
        Map<String, Object> replicationOptions;
        String strategyClass = config.getReplicationStrategyClass();
        if (strategyClass.contains("NetworkTopologyStrategy")) {
            replicationOptions = ImmutableMap.<String, Object>builder()
                    .put("class", strategyClass)
                    .putAll(config.getDataCenterReplicationFactor())
                    .build();
        } else {
            replicationOptions = ImmutableMap.<String, Object>builder()
                    .put("class", strategyClass)
                    .put("replication_factor", config.getReplicationFactor())
                    .build();
        }
        com.datastax.driver.core.schemabuilder.KeyspaceOptions createKeyspace = com.datastax.driver.core.schemabuilder.SchemaBuilder.createKeyspace(keyspaceName).ifNotExists()
                .with()
                .replication(replicationOptions);
        LOG.info("Creating keyspace...");
        session.execute(createKeyspace);
        LOG.info("Switching to keyspace: {}", keyspaceName);
        session.execute("USE " + keyspaceName);

        for (ICassandraPersister persister : persisters) {
            LOG.info("Creating schema: {}", persister);
            for (TableDefinition tableDefinition : persister.getAllTableDefinitions()) {
                persister.createTable(session, tableDefinition);
            }
        }
        executeCQL(session, loadAdditionalCQL());
        LOG.warn("Complete schema creation for: {}", keyspaceName);
    }

    public void updateSchema(com.datastax.driver.core.KeyspaceMetadata keyspaceMetadata, com.datastax.driver.core.Session session, List<ICassandraPersister> persisters) {
        String keyspaceName = config.getKeySpace();
        LOG.warn("Begin schema update for: {}", keyspaceName);
        session.execute("USE " + keyspaceName);
        for (ICassandraPersister persister : persisters) {
            LOG.info("Updating schema: {}", persister);
            for (TableDefinition tableDefinition : persister.getAllTableDefinitions()) {
                com.datastax.driver.core.TableMetadata existTableMetadata = keyspaceMetadata.getTable(tableDefinition.getTableName());
                if (existTableMetadata == null) {
                    persister.createTable(session, tableDefinition);
                } else {
                    persister.updateTable(session, tableDefinition, existTableMetadata);
                }
            }
        }
        executeCQL(session, loadAdditionalCQL());
        LOG.warn("Complete schema update for: {}", keyspaceName);
    }

    private String loadAdditionalCQL() {
        try {
            File file = ConfigurationUtils.fileFromURL(ConfigurationUtils.locate(cqlFilename));
            if (file != null && file.exists()) {
                return ConfigHelper.asString(file);
            }
        } catch (CommonException e) {
            LOG.debug("Can't load cql file (it's correct): {}, reason: {}", cqlFilename, e.getMessage());
        }
        return null;
    }

    private void executeCQL(com.datastax.driver.core.Session session, String cql) {
        if (isBlank(cql)) {
            return;
        }
        LOG.warn("Start executing CQL script");
        String[] queries = cql.split("\\r?\\n|\\r"); //One line = One query
        for (String query : queries) {
            if (isNotBlank(query)) {
                try {
                    com.datastax.driver.core.ResultSet resultSet = session.execute(query);
                    LOG.warn("Execute query: {}\nresult: {}", query, resultSet);
                } catch (Throwable t) {
                    LOG.error("Can't execute CQL:" + query, t);
                    throw t;
                }
            }
        }
        LOG.warn("CQL script executed");
    }
}
