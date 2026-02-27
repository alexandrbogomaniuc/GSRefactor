package com.dgphoenix.casino.cassandra.persist.engine;

import com.abs.casino.cassandra.persist.engine.ColumnIteratorCallback;
import com.abs.casino.cassandra.persist.engine.FakeNotAppliedResultSet;
import com.dgphoenix.casino.common.util.StreamUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.util.UnsafeUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

/**
 * User: Grien
 * Date: 25.12.2012 19:42
 */
public abstract class AbstractCassandraPersister<KEY, COLUMN> implements ICassandraPersister {
    public static final String VERSION_FIELD = "v";

    protected static final String KEY = "key";
    protected static final int MAX_WRITE_ATEMPTS_COUNT = 100;
    protected static final String SERIALIZED_COLUMN_NAME = "scn";
    protected static final String JSON_COLUMN_NAME = "jcn";
    protected static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.wrap(new byte[]{0});
    protected static final int IN_CLAUSE_SIZE = 1000;

    private com.datastax.driver.core.Session session;
    private com.datastax.driver.core.ConsistencyLevel readConsistency;
    private com.datastax.driver.core.ConsistencyLevel writeConsistency;
    private com.datastax.driver.core.ConsistencyLevel serialConsistency;
    private int ttl = 0;//load from config

    protected boolean initialized;

    protected AbstractCassandraPersister() {

    }

    @Override
    public void init() {
        this.initialized = true;
    }

    @Override
    public void setConsistencyLevels(com.datastax.driver.core.ConsistencyLevel readConsistency, com.datastax.driver.core.ConsistencyLevel writeConsistency, com.datastax.driver.core.ConsistencyLevel serialConsistency) {
        if (!serialConsistency.isSerial()) {
            throw new IllegalArgumentException("Supplied consistency level is not serial: " + serialConsistency);
        }
        this.readConsistency = readConsistency;
        this.writeConsistency = writeConsistency;
        this.serialConsistency = serialConsistency;
    }

    protected String getKeyColumnName() {
        return KEY;
    }

    @Override
    public final void createTable(com.datastax.driver.core.Session session, TableDefinition tableDefinition) {
        tableDefinition.defaultTimeToLive(getTtl());
        com.datastax.driver.core.schemabuilder.SchemaStatement createTable = tableDefinition.getCreateTableStatement();
        getLog().info("createTable: create table statement: {}", createTable);
        session.execute(createTable);
        Collection<com.datastax.driver.core.schemabuilder.SchemaStatement> createIndexes = tableDefinition.getCreateIndexStatements().values();
        for (com.datastax.driver.core.schemabuilder.SchemaStatement createIndex : createIndexes) {
            getLog().info("createTable: create index statement: {}", createIndex);
            session.execute(createIndex);
        }
    }

    protected List<com.datastax.driver.core.Statement> getOrCreateStatements(Map<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap) {
        return statementsMap.computeIfAbsent(getSession(), session -> new LinkedList<>());
    }

    @Override
    public void updateTable(com.datastax.driver.core.Session session, TableDefinition tableDefinition, com.datastax.driver.core.TableMetadata tableMetadata) {
        getLog().debug("updateTable: tableMetadata={}", tableMetadata);
        for (ColumnDefinition columnDefinition : tableDefinition.getColumns()) {
            String columnName = columnDefinition.getName();
            com.datastax.driver.core.ColumnMetadata existColumnMetadata = tableMetadata.getColumn(columnName);
            if (existColumnMetadata == null) {
                addColumn(session, tableMetadata.getName(), columnName, columnDefinition);
            }
            if (columnDefinition.isIndexed()) {
                String indexName = tableDefinition.getIndexName(columnName);
                boolean indexIsNotCreated = existColumnMetadata == null || indexDoesNotExist(tableMetadata, indexName);
                if (indexIsNotCreated) {
                    createIndex(session, tableDefinition, columnName);
                }
            }
        }
    }

    private boolean indexDoesNotExist(com.datastax.driver.core.TableMetadata tableMetadata, String indexName) {
        return tableMetadata.getIndex(indexName) == null
                && tableMetadata.getIndex(getQuotedName(indexName)) == null;
    }

    private String getQuotedName(String name) {
        return "\"" + name + "\"";
    }

    private void addColumn(com.datastax.driver.core.Session session, String tableName, String columnName, ColumnDefinition columnDefinition) {
        com.datastax.driver.core.DataType type = columnDefinition.getType();
        com.datastax.driver.core.schemabuilder.SchemaStatement addColumn = com.datastax.driver.core.schemabuilder.SchemaBuilder.alterTable(tableName).addColumn(columnName).type(type);
        getLog().info("updateTable: add column statement: {}", addColumn);
        session.execute(addColumn);
    }

    private void createIndex(com.datastax.driver.core.Session session, TableDefinition tableDefinition, String columnName) {
        com.datastax.driver.core.schemabuilder.SchemaStatement createIndex = tableDefinition.getCreateIndexStatements().get(columnName);
        getLog().info("updateTable: create index statement: {}", createIndex);
        session.execute(createIndex);
    }

    protected void assertInitialized() {
        checkState(initialized, this.getClass().getCanonicalName() + " Not initialized");
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void initSession(com.datastax.driver.core.Session session) {
        this.session = session;
    }

    @Override
    public void shutdown() {
        assertInitialized();
        initialized = false;
    }

    @Override
    public void setTtl(Integer ttl) {
        if (ttl != null && ttl > 0) {
            this.ttl = ttl;
        }
    }

    @Override
    public Integer getTtl() {
        return ttl;
    }

    protected com.datastax.driver.core.Session getSession() {
        checkState(session != null, "com.datastax.driver.core.Session undefined");
        return session;
    }

    protected static com.datastax.driver.core.querybuilder.Clause eq(String name, Object value) {
        return com.datastax.driver.core.querybuilder.QueryBuilder.eq(name, value);
    }

    protected static com.datastax.driver.core.querybuilder.Assignment set(String name, Object value) {
        return com.datastax.driver.core.querybuilder.QueryBuilder.set(name, value);
    }

    protected com.datastax.driver.core.querybuilder.Select getSelectAllColumnsQuery(TableDefinition tableDef) {
        return com.datastax.driver.core.querybuilder.QueryBuilder.select().all().from(tableDef.getTableName());
    }

    protected com.datastax.driver.core.querybuilder.Select getSelectColumnsQuery(TableDefinition tableDef, String... columns) {
        return com.datastax.driver.core.querybuilder.QueryBuilder.select(columns).from(tableDef.getTableName());
    }

    protected com.datastax.driver.core.querybuilder.Select getSelectAllColumnsQuery() {
        return getSelectAllColumnsQuery(getMainTableDefinition());
    }

    protected com.datastax.driver.core.querybuilder.Select getSelectColumnsQuery(String... columns) {
        return com.datastax.driver.core.querybuilder.QueryBuilder.select(columns).from(getMainColumnFamilyName());
    }

    protected com.datastax.driver.core.querybuilder.Select getDistinctSelectColumnsQuery(String... columns) {
        com.datastax.driver.core.querybuilder.Select.Selection select = com.datastax.driver.core.querybuilder.QueryBuilder.select().distinct();
        for (String column : columns) {
            select.column(column);
        }
        return select.from(getMainColumnFamilyName());
    }

    protected void assertAppliedByVersion(com.datastax.driver.core.ResultSet result, long expectedVersion) {
        if (!result.wasApplied()) {
            com.datastax.driver.core.Row row = result.one();
            throw new RuntimeException("result was not applied: expected version: " + expectedVersion +
                    ", found: " + (row == null || row.isNull(VERSION_FIELD) ? "null" : row.getLong(VERSION_FIELD)));
        }
    }

    protected com.datastax.driver.core.querybuilder.Insert getInsertQuery() {
        return getInsertQuery(getTtl() > 0 ? getTtl() : null);
    }

    protected com.datastax.driver.core.querybuilder.Insert getInsertQuery(Integer ttl) {
        return getInsertQuery(getMainTableDefinition(), ttl);
    }

    protected com.datastax.driver.core.querybuilder.Insert getInsertQuery(TableDefinition tableDef, Integer ttl) {
        com.datastax.driver.core.querybuilder.Insert insert = com.datastax.driver.core.querybuilder.QueryBuilder.insertInto(tableDef.getTableName());
        if (ttl != null) {
            insert.using(com.datastax.driver.core.querybuilder.QueryBuilder.ttl(ttl));
        }
        return insert;
    }

    protected com.datastax.driver.core.querybuilder.Update getUpdateQuery() {
        return com.datastax.driver.core.querybuilder.QueryBuilder.update(getMainTableDefinition().getTableName());
    }

    protected com.datastax.driver.core.querybuilder.Update getUpdateQuery(Integer ttl, com.datastax.driver.core.querybuilder.Clause... clauses) {
        com.datastax.driver.core.querybuilder.Update update = getUpdateQuery();
        com.datastax.driver.core.querybuilder.Update.Where where = update.where();
        for (com.datastax.driver.core.querybuilder.Clause clause : clauses) {
            where.and(clause);
        }
        if (ttl != null) {
            update.using(com.datastax.driver.core.querybuilder.QueryBuilder.ttl(ttl));
        }
        return update;
    }

    protected com.datastax.driver.core.querybuilder.Update getUpdateQuery(com.datastax.driver.core.querybuilder.Clause... clauses) {
        return getUpdateQuery(null, clauses);
    }

    protected com.datastax.driver.core.querybuilder.Update getUpdateQuery(com.datastax.driver.core.querybuilder.Clause clause) {
        return getUpdateQuery(getTtl() > 0 ? getTtl() : null, clause);
    }

    protected com.datastax.driver.core.querybuilder.Update getUpdateQuery(KEY keyValue) {
        return getUpdateQuery(eq(getKeyColumnName(), keyValue));
    }

    protected com.datastax.driver.core.ResultSet executeWithCheckTimeout(com.datastax.driver.core.Statement statement, String callerClassMethodIdentification) {
        long now = System.currentTimeMillis();
        try {
            return execute(this.session, statement, callerClassMethodIdentification, true);
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            if (e instanceof com.datastax.driver.core.exceptions.WriteTimeoutException) {
                com.datastax.driver.core.WriteType writeType = ((com.datastax.driver.core.exceptions.WriteTimeoutException) e).getWriteType();
                if (com.datastax.driver.core.WriteType.CAS.equals(writeType)) {
                    getLog().warn("executeWithCheckTimeout CAS query failed:{}", e.getMessage());
                } else {
                    getLog().error("executeWithCheckTimeout: writeType={}", writeType);
                }
            }
            StatisticsManager.getInstance().updateRequestStatistics(
                    getClass().getSimpleName() + " execution timeout " + callerClassMethodIdentification,
                    System.currentTimeMillis() - now);
            return new FakeNotAppliedResultSet(e);
        }
    }

    protected com.datastax.driver.core.ResultSetFuture executeAsync(com.datastax.driver.core.Statement query, String callerClassMethodIdentification) {
        assertInitialized();
        long now = System.currentTimeMillis();
        com.datastax.driver.core.ResultSetFuture rsFuture = session.executeAsync(query);
        StatisticsManager.getInstance().updateRequestStatistics(
                getClass().getSimpleName() + " executeAsync " + callerClassMethodIdentification,
                System.currentTimeMillis() - now);
        return rsFuture;
    }

    protected com.datastax.driver.core.ResultSet execute(String query, String callerClassMethodIdentification) {
        assertInitialized();
        long now = System.currentTimeMillis();
        com.datastax.driver.core.ResultSet rs;
        try {
            rs = session.execute(query);
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            long duration = System.currentTimeMillis() - now;
            getLog().error("{}:{} execute com.datastax.driver.core.exceptions.QueryExecutionException, duration ms={}",getClass().getSimpleName(),
                    callerClassMethodIdentification, duration);
            StatisticsManager.getInstance().updateRequestStatistics(
                    getClass().getSimpleName() + " execution timeout " + callerClassMethodIdentification,
                    duration);
            throw e;
        }
        StatisticsManager.getInstance().updateRequestStatistics(
                getClass().getSimpleName() + " execute " + callerClassMethodIdentification,
                System.currentTimeMillis() - now);
        return rs;
    }

    protected com.datastax.driver.core.ResultSet execute(String query, String callerClassMethodIdentification, Object... values) {
        assertInitialized();
        long now = System.currentTimeMillis();
        com.datastax.driver.core.ResultSet rs;
        try {
            rs = session.execute(query, values);
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            long duration = System.currentTimeMillis() - now;
            getLog().error("{}:{} execute com.datastax.driver.core.exceptions.QueryExecutionException, duration ms={}",getClass().getSimpleName(),
                    callerClassMethodIdentification, duration);
            StatisticsManager.getInstance().updateRequestStatistics(
                    getClass().getSimpleName() + " execution timeout " + callerClassMethodIdentification,
                    duration);
            throw e;
        }
        StatisticsManager.getInstance().updateRequestStatistics(
                getClass().getSimpleName() + " execute " + callerClassMethodIdentification,
                System.currentTimeMillis() - now);
        return rs;
    }

    protected com.datastax.driver.core.ResultSet execute(com.datastax.driver.core.Statement statement, String callerClassMethodIdentification, com.datastax.driver.core.ConsistencyLevel level) {
        return execute(this.session, statement, callerClassMethodIdentification, level);
    }

    protected com.datastax.driver.core.ResultSet execute(com.datastax.driver.core.querybuilder.Select statement, String callerClassMethodIdentification,
                                int queryReadTimeoutAttempts) {
        com.datastax.driver.core.ResultSet resultSet = null;
        int count = 0;
        while (resultSet == null) {
            try {
                resultSet = execute(this.session, statement, callerClassMethodIdentification, null);
            } catch (com.datastax.driver.core.exceptions.ReadTimeoutException e) {
                if (++count >= queryReadTimeoutAttempts) {
                    throw e;
                }
            }
        }
        return resultSet;
    }

    protected com.datastax.driver.core.ResultSet execute(com.datastax.driver.core.Statement statement, String callerClassMethodIdentification) {
        return execute(this.session, statement, callerClassMethodIdentification, null);
    }

    protected com.datastax.driver.core.ResultSet execute(com.datastax.driver.core.Session session, com.datastax.driver.core.Statement statement, String callerClassMethodIdentification,
                                boolean warnErrors) {
        assertInitialized();
        long now = System.currentTimeMillis();
        com.datastax.driver.core.ResultSet rs;
        try {
            setStatementConsistencyLevels(statement);
            rs = session.execute(statement);
        } catch (Exception e) {
            if (statement instanceof com.datastax.driver.core.querybuilder.BuiltStatement) {
                com.datastax.driver.core.querybuilder.BuiltStatement bStatement = (com.datastax.driver.core.querybuilder.BuiltStatement) statement;
                try { //bStatement.toString may be  failed, need prevent miss original exception
                    if (warnErrors) {
                        if (getLog().isWarnEnabled()) {
                            getLog().warn("execute: failed={}", bStatement, e);
                        }
                    } else {
                        getLog().error("execute: failed={}", bStatement, e);
                    }
                } catch (Exception e1) {
                    if (warnErrors) {
                        if (getLog().isWarnEnabled()) {
                            getLog().warn("execute: failed. can't log cql", e1);
                        }
                    } else {
                        getLog().error("execute: failed. can't log cql", e1);
                    }
                }
            }
            if (e instanceof com.datastax.driver.core.exceptions.QueryExecutionException) {
                long duration = System.currentTimeMillis() - now;
                if (warnErrors) {
                    getLog().warn("{}:{} execute com.datastax.driver.core.exceptions.QueryExecutionException, duration ms={}", getClass().getSimpleName(),
                            callerClassMethodIdentification, duration);
                } else {
                    getLog().error("{}:{} execute com.datastax.driver.core.exceptions.QueryExecutionException, duration ms={}", getClass().getSimpleName(),
                            callerClassMethodIdentification, duration);
                }
                StatisticsManager.getInstance().updateRequestStatistics(
                        getClass().getSimpleName() + " execution timeout " + callerClassMethodIdentification,
                        duration);
            }
            throw e;
        }
        StatisticsManager.getInstance().updateRequestStatistics(
                getClass().getSimpleName() + " execute " + callerClassMethodIdentification,
                System.currentTimeMillis() - now);
        return rs;
    }

    protected com.datastax.driver.core.ResultSet execute(com.datastax.driver.core.Session session, com.datastax.driver.core.Statement statement, String callerClassMethodIdentification) {
        return execute(session, statement, callerClassMethodIdentification, null);
    }

    protected com.datastax.driver.core.ResultSet execute(com.datastax.driver.core.Session session, com.datastax.driver.core.Statement statement, String callerClassMethodIdentification,
                                com.datastax.driver.core.ConsistencyLevel level) {
        assertInitialized();
        long now = System.currentTimeMillis();
        com.datastax.driver.core.ResultSet rs;
        try {
            if (level != null) {
                statement.setConsistencyLevel(level);
            } else {
                //use default
                setStatementConsistencyLevels(statement);
            }
            rs = session.execute(statement);
        } catch (Exception e) {
            if (statement instanceof com.datastax.driver.core.querybuilder.BuiltStatement) {
                com.datastax.driver.core.querybuilder.BuiltStatement bStatement = (com.datastax.driver.core.querybuilder.BuiltStatement) statement;
                try { //bStatement.toString may be  failed, need prevent miss original exception
                    if (com.datastax.driver.core.exceptions.QueryExecutionException.class.isInstance(e)) {
                        getLog().error("execute: failed=" + bStatement + ", error=" + e);
                    } else {
                        getLog().error("execute: failed=" + bStatement, e);
                    }
                } catch (Exception e1) {
                    getLog().error("execute: failed log cql", e1);
                }
            }
            if (com.datastax.driver.core.exceptions.QueryExecutionException.class.isInstance(e)) {
                long duration = System.currentTimeMillis() - now;
                getLog().error(getClass().getSimpleName() + ":" + callerClassMethodIdentification +
                        " execute com.datastax.driver.core.exceptions.QueryExecutionException, duration ms=" + duration);
                StatisticsManager.getInstance().updateRequestStatistics(
                        getClass().getSimpleName() + " execution timeout " + callerClassMethodIdentification,
                        duration);
            }
            throw e;
        }
        StatisticsManager.getInstance().updateRequestStatistics(
                getClass().getSimpleName() + " execute " + callerClassMethodIdentification,
                System.currentTimeMillis() - now);
        return rs;
    }

    protected void setStatementConsistencyLevels(com.datastax.driver.core.Statement statement) {
        if (statement.getConsistencyLevel() == null) {
            setStatementReadWriteConsistencyLevel(statement);
        }
        if (statement.getSerialConsistencyLevel() == null) {
            setStatementSerialConsistencyLevel(statement);
        }
    }

    private void setStatementReadWriteConsistencyLevel(com.datastax.driver.core.Statement statement) {
        if (statement instanceof com.datastax.driver.core.querybuilder.Select || statement instanceof com.datastax.driver.core.querybuilder.Select.Where) {
            statement.setConsistencyLevel(readConsistency);
        } else if (statement instanceof com.datastax.driver.core.querybuilder.Insert
                || statement instanceof com.datastax.driver.core.querybuilder.Update
                || statement instanceof com.datastax.driver.core.querybuilder.Update.Where
                || statement instanceof com.datastax.driver.core.querybuilder.Delete
                || statement instanceof com.datastax.driver.core.querybuilder.Delete.Where) {

            statement.setConsistencyLevel(writeConsistency);
        }
    }

    private void setStatementSerialConsistencyLevel(com.datastax.driver.core.Statement statement) {
        statement.setSerialConsistencyLevel(serialConsistency);
    }

    protected com.datastax.driver.core.ResultSet insert(KEY key, String columnName, Object value) {
        return insert(key, columnName, value, false);
    }

    protected com.datastax.driver.core.ResultSet insert(KEY key, Map<String,Object> columnValues) {
        return insert(key, columnValues, false);
    }

    protected com.datastax.driver.core.ResultSet insert(KEY key, String columnName, Object value, boolean ifNotExist) {
        return insert(key, Collections.singletonMap(columnName, value), ifNotExist);
    }

    protected com.datastax.driver.core.ResultSet insert(KEY key, Map<String,Object> columnValues, boolean ifNotExist) {
        if (columnValues.isEmpty()) {
            throw new IllegalArgumentException("columnValues cannot be empty");
        }
        long now = System.currentTimeMillis();
        com.datastax.driver.core.querybuilder.Insert insert = com.datastax.driver.core.querybuilder.QueryBuilder.insertInto(getMainTableDefinition().getTableName()).value(getKeyColumnName(), key);

        for (Entry<String, Object> columnValue : columnValues.entrySet()) {
            insert = insert.value(columnValue.getKey(), columnValue.getValue());
        }

        if (ifNotExist) { //Cannot provide custom timestamp for conditional updates
            insert.ifNotExists();
        }
        com.datastax.driver.core.ResultSet resultSet;
        try {
            setStatementConsistencyLevels(insert);
            resultSet = session.execute(insert);
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            long duration = System.currentTimeMillis() - now;
            getLog().error(getClass().getSimpleName() + ":insert execute com.datastax.driver.core.exceptions.QueryExecutionException, duration ms=" + duration, e);
            StatisticsManager.getInstance().updateRequestStatistics(
                    getClass().getSimpleName() + " insertion timeout",
                    duration);
            throw e;
        }

        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " insert",
                System.currentTimeMillis() - now);
        return resultSet;
    }

    protected com.datastax.driver.core.ResultSet insert(KEY key, String columnName, Object value, int ttl) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.querybuilder.Insert insert = getInsertQuery().value(getKeyColumnName(), key).value(columnName, value);
        insert.using(com.datastax.driver.core.querybuilder.QueryBuilder.ttl(ttl));
        com.datastax.driver.core.ResultSet resultSet;
        try {
            resultSet = session.execute(insert);
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            long duration = System.currentTimeMillis() - now;
            getLog().error(getClass().getSimpleName() + ":insert execute com.datastax.driver.core.exceptions.QueryExecutionException, duration ms=" + duration, e);
            StatisticsManager.getInstance().updateRequestStatistics(
                    getClass().getSimpleName() + " insertion timeout",
                    duration);
            throw e;
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " insert",
                System.currentTimeMillis() - now);
        return resultSet;
    }

    protected com.datastax.driver.core.querybuilder.Insert addInsertion(KEY key, String columnName, Object value) {
        return getInsertQuery().value(getKeyColumnName(), key).value(columnName, value);
    }

    protected com.datastax.driver.core.querybuilder.Insert addInsertion(KEY key, String columnName, Object value, int ttl) {
        com.datastax.driver.core.querybuilder.Insert insert = getInsertQuery().value(getKeyColumnName(), key).value(columnName, value);
        insert.using(com.datastax.driver.core.querybuilder.QueryBuilder.ttl(ttl));
        return insert;
    }

    protected com.datastax.driver.core.querybuilder.Insert addInsertion(String columnFamily, KEY key, String columnName, Object value, int ttl) {
        com.datastax.driver.core.querybuilder.Insert insert = com.datastax.driver.core.querybuilder.QueryBuilder.insertInto(columnFamily).value(getKeyColumnName(), key).value(columnName, value);
        insert.using(com.datastax.driver.core.querybuilder.QueryBuilder.ttl(ttl));
        return insert;
    }

    protected com.datastax.driver.core.querybuilder.Delete addItemDeletion(KEY key) {
        return addItemDeletion(getSimpleKeyClause(key));
    }

    protected com.datastax.driver.core.querybuilder.Delete addItemDeletion(com.datastax.driver.core.querybuilder.Clause... clauses) {
        return addItemDeletion(getMainColumnFamilyName(), clauses);
    }

    protected com.datastax.driver.core.querybuilder.Delete addItemDeletion(String tableName, KEY key) {
        return addItemDeletion(tableName, getSimpleKeyClause(key));
    }

    protected com.datastax.driver.core.querybuilder.Delete addItemDeletion(String tableName, com.datastax.driver.core.querybuilder.Clause... clauses) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Delete query = com.datastax.driver.core.querybuilder.QueryBuilder.delete().from(tableName);
        com.datastax.driver.core.querybuilder.Delete.Where where = query.where();
        for (com.datastax.driver.core.querybuilder.Clause clause : clauses) {
            where.and(clause);
        }
        return query;
    }

    protected com.datastax.driver.core.querybuilder.Delete addColumnDeletion(KEY key, String column) {
        return addColumnDeletion(new String[]{column}, getSimpleKeyClause(key));
    }

    protected com.datastax.driver.core.querybuilder.Delete addColumnDeletion(String[] columns, com.datastax.driver.core.querybuilder.Clause... clauses) {
        return addColumnDeletion(getMainColumnFamilyName(), columns, clauses);
    }

    protected com.datastax.driver.core.querybuilder.Delete addColumnDeletion(String tableName, KEY key, String column) {
        return addColumnDeletion(tableName, new String[]{column}, getSimpleKeyClause(key));
    }

    protected com.datastax.driver.core.querybuilder.Delete addColumnDeletion(String tableName, String[] columns, com.datastax.driver.core.querybuilder.Clause... clauses) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Delete query = com.datastax.driver.core.querybuilder.QueryBuilder.delete(columns).from(tableName);
        com.datastax.driver.core.querybuilder.Delete.Where where = query.where();
        for (com.datastax.driver.core.querybuilder.Clause clause : clauses) {
            where.and(clause);
        }
        return query;
    }

    //not rename this method to delete(), need prevent conflict with void delete() from com.hazelcast.core.MapStore
    protected boolean deleteWithCheck(KEY key) {
        com.datastax.driver.core.ResultSet resultSet = deleteItem(key);
        return resultSet.wasApplied();
    }

    //may be overridden if KEY column have another name
    protected com.datastax.driver.core.querybuilder.Clause getSimpleKeyClause(KEY key) {
        return eq(getKeyColumnName(), key);
    }

    protected com.datastax.driver.core.ResultSet deleteItem(KEY key) {
        com.datastax.driver.core.querybuilder.Delete query = com.datastax.driver.core.querybuilder.QueryBuilder.delete().from(getMainColumnFamilyName());
        query.where(getSimpleKeyClause(key));
        return execute(query, " deleteItem");
    }

    protected com.datastax.driver.core.ResultSet deleteItem(com.datastax.driver.core.querybuilder.Clause... clauses) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Delete query = com.datastax.driver.core.querybuilder.QueryBuilder.delete().from(getMainColumnFamilyName());
        com.datastax.driver.core.querybuilder.Delete.Where where = query.where();
        for (com.datastax.driver.core.querybuilder.Clause clause : clauses) {
            where.and(clause);
        }
        return execute(query, " deleteItem");
    }

    protected com.datastax.driver.core.ResultSet deleteMapItem(String mapColumnName, String itemKey, com.datastax.driver.core.querybuilder.Clause... clauses) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Delete query = com.datastax.driver.core.querybuilder.QueryBuilder.delete().mapElt(mapColumnName, itemKey).from(getMainColumnFamilyName());
        com.datastax.driver.core.querybuilder.Delete.Where where = query.where();
        for (com.datastax.driver.core.querybuilder.Clause clause : clauses) {
            where.and(clause);
        }
        return execute(query, " deleteMapItem");
    }

    protected com.datastax.driver.core.querybuilder.Batch batch() {
        return com.datastax.driver.core.querybuilder.QueryBuilder.batch();
    }

    protected com.datastax.driver.core.ResultSet deleteColumn(String column, com.datastax.driver.core.querybuilder.Clause... clauses) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Delete query = com.datastax.driver.core.querybuilder.QueryBuilder.delete(column).from(getMainColumnFamilyName());
        com.datastax.driver.core.querybuilder.Delete.Where where = query.where();
        for (com.datastax.driver.core.querybuilder.Clause clause : clauses) {
            where.and(clause);
        }
        return execute(query, " deleteColumn");
    }

    protected com.datastax.driver.core.ResultSet deleteColumn(String[] columns, com.datastax.driver.core.querybuilder.Clause... clauses) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Delete query = com.datastax.driver.core.querybuilder.QueryBuilder.delete(columns).from(getMainColumnFamilyName());
        com.datastax.driver.core.querybuilder.Delete.Where where = query.where();
        for (com.datastax.driver.core.querybuilder.Clause clause : clauses) {
            where.and(clause);
        }
        return execute(query, " deleteColumn");
    }

    protected com.datastax.driver.core.ResultSet deleteColumn(KEY key, String column) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Delete query = com.datastax.driver.core.querybuilder.QueryBuilder.delete(column).from(getMainColumnFamilyName());
        query.where(getSimpleKeyClause(key));
        return execute(query, " deleteColumn");
    }

    public int size() {
        return (int) count();
    }

    protected long count(TableDefinition tableDef, List<com.datastax.driver.core.querybuilder.Clause> clauses) {
        return clauses == null ? count(tableDef) : count(tableDef, clauses.toArray(new com.datastax.driver.core.querybuilder.Clause[clauses.size()]));
    }

    protected long count(List<com.datastax.driver.core.querybuilder.Clause> clauses) {
        return clauses == null ? count() : count(clauses.toArray(new com.datastax.driver.core.querybuilder.Clause[clauses.size()]));
    }

    protected long count(TableDefinition tableDef, com.datastax.driver.core.querybuilder.Clause... where) {
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().countAll().from(tableDef.getTableName());
        if (where != null) {
            for (com.datastax.driver.core.querybuilder.Clause clause : where) {
                query.where().and(clause);
            }
        }
        com.datastax.driver.core.ResultSet resultSet = execute(query, "count");
        com.datastax.driver.core.Row row = resultSet.one();
        return row == null ? 0 : row.getLong("count");
    }

    protected long count(com.datastax.driver.core.querybuilder.Clause... where) {
        return count(getMainTableDefinition(), where);
    }

    protected Iterator<com.datastax.driver.core.Row> getAll() {
        return getAll(null);
    }

    protected Iterator<com.datastax.driver.core.Row> getAll(com.datastax.driver.core.querybuilder.Clause clause) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().all().from(getMainColumnFamilyName());
        if (clause != null) {
            query.where(clause);
        }
        try {
            setStatementConsistencyLevels(query);
            com.datastax.driver.core.ResultSet execute = session.execute(query);
            Iterator<com.datastax.driver.core.Row> iterator = execute.iterator();
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getAll", System.currentTimeMillis() - now);
            return iterator;
        } catch (com.datastax.driver.core.exceptions.QueryExecutionException e) {
            long duration = System.currentTimeMillis() - now;
            getLog().error(getClass().getSimpleName() + ":getAll execute com.datastax.driver.core.exceptions.QueryExecutionException, duration ms=" + duration, e);
            StatisticsManager.getInstance().updateRequestStatistics(
                    getClass().getSimpleName() + " getAll timeout",
                    duration);
            throw e;
        }
    }

    protected com.datastax.driver.core.Row getByKey(KEY key) {
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().all().from(getMainColumnFamilyName()).where(eq(getKeyColumnName(), key)).
                limit(1);
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getByKey");
        return resultSet.one();
    }

    protected ByteBuffer get(Map<String, Object> keys, String columnName) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select(columnName).from(getMainColumnFamilyName());
        com.datastax.driver.core.querybuilder.Select.Where where = null;

        for (Map.Entry<String, Object> entry : keys.entrySet()) {
            if (where == null) {
                where = query.where(eq(entry.getKey(), entry.getValue()));
            } else {
                where.and(eq(entry.getKey(), entry.getValue()));
            }
        }
        query.limit(1);
        com.datastax.driver.core.ResultSet rows = execute(query, "get");
        ByteBuffer result = null;
        if (rows.iterator().hasNext()) {
            com.datastax.driver.core.Row row = rows.iterator().next();
            result = row.getBytes(columnName);
        }
        if (result == null) {
            getLog().warn("get: QueryResult is null, key=" + keys);
        }
        return result;
    }

    protected ByteBuffer get(String columnName, com.datastax.driver.core.querybuilder.Clause... clauses) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select(columnName).from(getMainColumnFamilyName()).limit(1);
        for (com.datastax.driver.core.querybuilder.Clause clause : clauses) {
            query.where(clause);
        }
        com.datastax.driver.core.ResultSet rows = execute(query, "get");
        ByteBuffer result = null;
        if (rows.iterator().hasNext()) {
            com.datastax.driver.core.Row row = rows.iterator().next();
            result = row.getBytes(columnName);
        }
        if (result == null) {
            String clausesString = Arrays.toString(clauses);
            getLog().warn("get: QueryResult is null, clauses={}", clausesString);
        }
        return result;
    }

    protected ByteBuffer get(com.datastax.driver.core.querybuilder.Clause clause, String columnName) {
        return get(columnName, clause);
    }

    protected com.datastax.driver.core.Row getAsRow(KEY key, String columnName) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select(getKeyColumnName(), columnName).from(getMainColumnFamilyName()).
                where(eq(getKeyColumnName(), key)).limit(1);
        com.datastax.driver.core.ResultSet rows = execute(query, "getAsRow");
        return rows.one();
    }

    protected Long getWriteTime(KEY key) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().
                writeTime(SERIALIZED_COLUMN_NAME).
                writeTime(JSON_COLUMN_NAME).
                from(getMainColumnFamilyName()).
                where(eq(getKeyColumnName(), key)).
                limit(1);
        com.datastax.driver.core.ResultSet rows = execute(query, "getWriteTime");
        com.datastax.driver.core.Row row = rows.one();
        if (row == null) {
            return null;
        }
        long writeTimeS = row.getLong("writetime(" + SERIALIZED_COLUMN_NAME + ")");
        long writeTimeJ = row.getLong("writetime(" + JSON_COLUMN_NAME + ")");
        long writeTime = Long.max(writeTimeS, writeTimeJ);
        return writeTime <= 0 ? null : writeTime;
    }

    protected ByteBuffer get(KEY key, String columnName) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Select query =
                com.datastax.driver.core.querybuilder.QueryBuilder.
                        select(columnName).
                        from(getMainColumnFamilyName()).
                        where(eq(getKeyColumnName(), key)).
                        limit(1);
        com.datastax.driver.core.ResultSet rows = execute(query, "get");
        com.datastax.driver.core.Row row = rows.one();
        ByteBuffer result = null;
        if (row != null) {
            result = row.getBytes(columnName);
        }
        if (result == null) {
            //getLog().warn("get: QueryResult is null, key=" + key);
        }
        return result;
    }

    protected String getJson(KEY key) {
        assertInitialized();
        com.datastax.driver.core.querybuilder.Select query =
                com.datastax.driver.core.querybuilder.QueryBuilder.
                        select(JSON_COLUMN_NAME).
                        from(getMainColumnFamilyName()).
                        where(eq(getKeyColumnName(), key)).
                        limit(1);
        com.datastax.driver.core.ResultSet rows = execute(query, "get");
        com.datastax.driver.core.Row row = rows.one();
        String result = null;
        if (row != null) {
            result = row.getString(JSON_COLUMN_NAME);
        }
        if (result == null) {
            //getLog().warn("get: QueryResult is null, key=" + key);
        }
        return result;
    }

    protected <T> T get(KEY key, Class<T> tClass) {
        String json = getJson(key);
        T obj = getMainTableDefinition().deserializeFromJson(json, tClass);

        if (obj == null) {
            ByteBuffer bytes = get(key, SERIALIZED_COLUMN_NAME);
            obj = getMainTableDefinition().deserializeFrom(bytes, tClass);
        }

        return obj;
    }

    public void iterateAllColumnFamily(ColumnIteratorCallback callback) {
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().all().from(getMainColumnFamilyName());
        com.datastax.driver.core.ResultSet resultSet = execute(query, "iterateAllColumnFamily");
        getLog().debug("iterateAllColumnFamily: getAvailableWithoutFetching=" +
                resultSet.getAvailableWithoutFetching());
        int count = 0;
        for (com.datastax.driver.core.Row row : resultSet) {
            callback.process(row);
            if (resultSet.getAvailableWithoutFetching() <= 0) {
                getLog().debug("iterateAllColumnFamily: getAvailableWithoutFetching=0, current count={}", count);
            }
        }
        count++;
        getLog().debug("iterateAllColumnFamily: processed count={}", count);
    }

    protected <E extends KryoSerializable> Iterable<E> getAsIterableSkipNull(final String[] entryColumnNames,
                                                                             final Class<E> entryClass,
                                                                             final Integer readTimeout,
                                                                             String callerClassMethodIdentification,
                                                                             com.datastax.driver.core.querybuilder.Clause... clauses) {
        return getAsIterableSkipNull(entryColumnNames, callerClassMethodIdentification, readTimeout,
                getDeserializeColumnFunction(entryClass), clauses);
    }

    protected <E extends KryoSerializable> Iterable<E> getAsIterableSkipNull(final String[] entryColumnNames,
                                                                             final Class<E> entryClass,
                                                                             String callerClassMethodIdentification,
                                                                             com.datastax.driver.core.querybuilder.Clause... clauses) {
        return getAsIterableSkipNull(entryColumnNames, callerClassMethodIdentification,
                getDeserializeColumnFunction(entryClass), clauses);
    }

    protected <E extends KryoSerializable> Iterable<E> executeAndGetAsIterableSkipNull(com.datastax.driver.core.Statement statement, String callerClassMethodIdentification,
                                                                                       final String entryColumnName, final Class<E> entryClass) {
        return executeAndGetAsIterableSkipNull(statement, callerClassMethodIdentification, getDeserializeColumnFunction(entryClass));
    }

    protected <E extends KryoSerializable> Iterable<E> executeAndGetAsIterable(com.datastax.driver.core.Statement statement, String callerClassMethodIdentification,
                                                                               final String entryColumnName, final Class<E> entryClass) {
        return executeAndGetAsIterable(statement, callerClassMethodIdentification, getDeserializeColumnFunction(entryClass));
    }

    protected <E extends KryoSerializable> Iterable<E> deserializeIterable(Iterable<com.datastax.driver.core.Row> iterable, final String entryColumnName, final Class<E> entryClass) {
        return StreamUtils.asStream(iterable)
                .map(getDeserializeColumnFunction(entryClass))
                .collect(Collectors.toList());
    }

    private <E> Function<com.datastax.driver.core.Row, E> getDeserializeColumnFunction(final Class<E> entryClass) {
        return row -> {
            E obj = getMainTableDefinition().deserializeFromJson(row.getString(JSON_COLUMN_NAME), entryClass);

            if (obj == null) {
                obj = getMainTableDefinition().deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), entryClass);
            }
            return obj;
        };
    }

    protected <E> Iterable<E> getAsIterableSkipNull(final String[] entryColumnNames, String callerClassMethodIdentification,
                                                    Integer readTimeout, Function<com.datastax.driver.core.Row, E> function, com.datastax.driver.core.querybuilder.Clause... clauses) {
        com.datastax.driver.core.querybuilder.Select.Where select = getSelectColumnsQuery(entryColumnNames).where();
        if (readTimeout != null) {
            select.setReadTimeoutMillis(readTimeout);
        }
        for (com.datastax.driver.core.querybuilder.Clause clause : clauses) {
            select.and(clause);
        }
        return executeAndGetAsIterableSkipNull(select, callerClassMethodIdentification, function);
    }

    protected <E> Iterable<E> getAsIterableSkipNull(final String[] entryColumnNames, String callerClassMethodIdentification,
                                                    Function<com.datastax.driver.core.Row, E> function, com.datastax.driver.core.querybuilder.Clause... clauses) {
        return getAsIterableSkipNull(entryColumnNames, callerClassMethodIdentification, null, function, clauses);
    }

    protected <E> Iterable<E> executeAndGetAsIterableSkipNull(com.datastax.driver.core.Statement statement, String callerClassMethodIdentification,
                                                              Function<com.datastax.driver.core.Row, E> function) {
        return StreamUtils.asStream(executeAndGetAsIterable(statement, callerClassMethodIdentification, function))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected <E> Iterable<E> executeAndGetAsIterable(com.datastax.driver.core.Statement statement, String callerClassMethodIdentification,
                                                      Function<com.datastax.driver.core.Row, E> function) {
        return StreamUtils.asStream(execute(statement, callerClassMethodIdentification))
                .map(function)
                .collect(Collectors.toList());
    }

    public void releaseBuffer(ByteBuffer buffer) {
        try {
            UnsafeUtil.releaseBuffer(buffer);
        } catch (Throwable ignore) {
        }
    }
}
