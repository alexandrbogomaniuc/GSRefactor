package com.dgphoenix.casino.cassandra.persist.engine;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.ReadTimeoutException;
import com.datastax.driver.core.exceptions.WriteTimeoutException;
import com.datastax.driver.core.querybuilder.*;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.datastax.driver.core.schemabuilder.SchemaStatement;
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
    private ConsistencyLevel readConsistency;
    private ConsistencyLevel writeConsistency;
    private ConsistencyLevel serialConsistency;
    private int ttl = 0;//load from config

    protected boolean initialized;

    protected AbstractCassandraPersister() {

    }

    @Override
    public void init() {
        this.initialized = true;
    }

    @Override
    public void setConsistencyLevels(ConsistencyLevel readConsistency, ConsistencyLevel writeConsistency, ConsistencyLevel serialConsistency) {
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
        SchemaStatement createTable = tableDefinition.getCreateTableStatement();
        getLog().info("createTable: create table statement: {}", createTable);
        session.execute(createTable);
        Collection<SchemaStatement> createIndexes = tableDefinition.getCreateIndexStatements().values();
        for (SchemaStatement createIndex : createIndexes) {
            getLog().info("createTable: create index statement: {}", createIndex);
            session.execute(createIndex);
        }
    }

    protected List<Statement> getOrCreateStatements(Map<com.datastax.driver.core.Session, List<Statement>> statementsMap) {
        return statementsMap.computeIfAbsent(getSession(), session -> new LinkedList<>());
    }

    @Override
    public void updateTable(com.datastax.driver.core.Session session, TableDefinition tableDefinition, TableMetadata tableMetadata) {
        getLog().debug("updateTable: tableMetadata={}", tableMetadata);
        for (ColumnDefinition columnDefinition : tableDefinition.getColumns()) {
            String columnName = columnDefinition.getName();
            ColumnMetadata existColumnMetadata = tableMetadata.getColumn(columnName);
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

    private boolean indexDoesNotExist(TableMetadata tableMetadata, String indexName) {
        return tableMetadata.getIndex(indexName) == null
                && tableMetadata.getIndex(getQuotedName(indexName)) == null;
    }

    private String getQuotedName(String name) {
        return "\"" + name + "\"";
    }

    private void addColumn(com.datastax.driver.core.Session session, String tableName, String columnName, ColumnDefinition columnDefinition) {
        DataType type = columnDefinition.getType();
        SchemaStatement addColumn = SchemaBuilder.alterTable(tableName).addColumn(columnName).type(type);
        getLog().info("updateTable: add column statement: {}", addColumn);
        session.execute(addColumn);
    }

    private void createIndex(com.datastax.driver.core.Session session, TableDefinition tableDefinition, String columnName) {
        SchemaStatement createIndex = tableDefinition.getCreateIndexStatements().get(columnName);
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

    protected static Clause eq(String name, Object value) {
        return QueryBuilder.eq(name, value);
    }

    protected static Assignment set(String name, Object value) {
        return QueryBuilder.set(name, value);
    }

    protected Select getSelectAllColumnsQuery(TableDefinition tableDef) {
        return QueryBuilder.select().all().from(tableDef.getTableName());
    }

    protected Select getSelectColumnsQuery(TableDefinition tableDef, String... columns) {
        return QueryBuilder.select(columns).from(tableDef.getTableName());
    }

    protected Select getSelectAllColumnsQuery() {
        return getSelectAllColumnsQuery(getMainTableDefinition());
    }

    protected Select getSelectColumnsQuery(String... columns) {
        return QueryBuilder.select(columns).from(getMainColumnFamilyName());
    }

    protected Select getDistinctSelectColumnsQuery(String... columns) {
        Select.Selection select = QueryBuilder.select().distinct();
        for (String column : columns) {
            select.column(column);
        }
        return select.from(getMainColumnFamilyName());
    }

    protected void assertAppliedByVersion(ResultSet result, long expectedVersion) {
        if (!result.wasApplied()) {
            Row row = result.one();
            throw new RuntimeException("result was not applied: expected version: " + expectedVersion +
                    ", found: " + (row == null || row.isNull(VERSION_FIELD) ? "null" : row.getLong(VERSION_FIELD)));
        }
    }

    protected Insert getInsertQuery() {
        return getInsertQuery(getTtl() > 0 ? getTtl() : null);
    }

    protected Insert getInsertQuery(Integer ttl) {
        return getInsertQuery(getMainTableDefinition(), ttl);
    }

    protected Insert getInsertQuery(TableDefinition tableDef, Integer ttl) {
        Insert insert = QueryBuilder.insertInto(tableDef.getTableName());
        if (ttl != null) {
            insert.using(QueryBuilder.ttl(ttl));
        }
        return insert;
    }

    protected Update getUpdateQuery() {
        return QueryBuilder.update(getMainTableDefinition().getTableName());
    }

    protected Update getUpdateQuery(Integer ttl, Clause... clauses) {
        Update update = getUpdateQuery();
        Update.Where where = update.where();
        for (Clause clause : clauses) {
            where.and(clause);
        }
        if (ttl != null) {
            update.using(QueryBuilder.ttl(ttl));
        }
        return update;
    }

    protected Update getUpdateQuery(Clause... clauses) {
        return getUpdateQuery(null, clauses);
    }

    protected Update getUpdateQuery(Clause clause) {
        return getUpdateQuery(getTtl() > 0 ? getTtl() : null, clause);
    }

    protected Update getUpdateQuery(KEY keyValue) {
        return getUpdateQuery(eq(getKeyColumnName(), keyValue));
    }

    protected ResultSet executeWithCheckTimeout(Statement statement, String callerClassMethodIdentification) {
        long now = System.currentTimeMillis();
        try {
            return execute(this.session, statement, callerClassMethodIdentification, true);
        } catch (QueryExecutionException e) {
            if (e instanceof WriteTimeoutException) {
                WriteType writeType = ((WriteTimeoutException) e).getWriteType();
                if (WriteType.CAS.equals(writeType)) {
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

    protected ResultSetFuture executeAsync(Statement query, String callerClassMethodIdentification) {
        assertInitialized();
        long now = System.currentTimeMillis();
        ResultSetFuture rsFuture = session.executeAsync(query);
        StatisticsManager.getInstance().updateRequestStatistics(
                getClass().getSimpleName() + " executeAsync " + callerClassMethodIdentification,
                System.currentTimeMillis() - now);
        return rsFuture;
    }

    protected ResultSet execute(String query, String callerClassMethodIdentification) {
        assertInitialized();
        long now = System.currentTimeMillis();
        ResultSet rs;
        try {
            rs = session.execute(query);
        } catch (QueryExecutionException e) {
            long duration = System.currentTimeMillis() - now;
            getLog().error("{}:{} execute QueryExecutionException, duration ms={}",getClass().getSimpleName(),
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

    protected ResultSet execute(String query, String callerClassMethodIdentification, Object... values) {
        assertInitialized();
        long now = System.currentTimeMillis();
        ResultSet rs;
        try {
            rs = session.execute(query, values);
        } catch (QueryExecutionException e) {
            long duration = System.currentTimeMillis() - now;
            getLog().error("{}:{} execute QueryExecutionException, duration ms={}",getClass().getSimpleName(),
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

    protected ResultSet execute(Statement statement, String callerClassMethodIdentification, ConsistencyLevel level) {
        return execute(this.session, statement, callerClassMethodIdentification, level);
    }

    protected ResultSet execute(Select statement, String callerClassMethodIdentification,
                                int queryReadTimeoutAttempts) {
        ResultSet resultSet = null;
        int count = 0;
        while (resultSet == null) {
            try {
                resultSet = execute(this.session, statement, callerClassMethodIdentification, null);
            } catch (ReadTimeoutException e) {
                if (++count >= queryReadTimeoutAttempts) {
                    throw e;
                }
            }
        }
        return resultSet;
    }

    protected ResultSet execute(Statement statement, String callerClassMethodIdentification) {
        return execute(this.session, statement, callerClassMethodIdentification, null);
    }

    protected ResultSet execute(com.datastax.driver.core.Session session, Statement statement, String callerClassMethodIdentification,
                                boolean warnErrors) {
        assertInitialized();
        long now = System.currentTimeMillis();
        ResultSet rs;
        try {
            setStatementConsistencyLevels(statement);
            rs = session.execute(statement);
        } catch (Exception e) {
            if (statement instanceof BuiltStatement) {
                BuiltStatement bStatement = (BuiltStatement) statement;
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
            if (e instanceof QueryExecutionException) {
                long duration = System.currentTimeMillis() - now;
                if (warnErrors) {
                    getLog().warn("{}:{} execute QueryExecutionException, duration ms={}", getClass().getSimpleName(),
                            callerClassMethodIdentification, duration);
                } else {
                    getLog().error("{}:{} execute QueryExecutionException, duration ms={}", getClass().getSimpleName(),
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

    protected ResultSet execute(com.datastax.driver.core.Session session, Statement statement, String callerClassMethodIdentification) {
        return execute(session, statement, callerClassMethodIdentification, null);
    }

    protected ResultSet execute(com.datastax.driver.core.Session session, Statement statement, String callerClassMethodIdentification,
                                ConsistencyLevel level) {
        assertInitialized();
        long now = System.currentTimeMillis();
        ResultSet rs;
        try {
            if (level != null) {
                statement.setConsistencyLevel(level);
            } else {
                //use default
                setStatementConsistencyLevels(statement);
            }
            rs = session.execute(statement);
        } catch (Exception e) {
            if (statement instanceof BuiltStatement) {
                BuiltStatement bStatement = (BuiltStatement) statement;
                try { //bStatement.toString may be  failed, need prevent miss original exception
                    if (QueryExecutionException.class.isInstance(e)) {
                        getLog().error("execute: failed=" + bStatement + ", error=" + e);
                    } else {
                        getLog().error("execute: failed=" + bStatement, e);
                    }
                } catch (Exception e1) {
                    getLog().error("execute: failed log cql", e1);
                }
            }
            if (QueryExecutionException.class.isInstance(e)) {
                long duration = System.currentTimeMillis() - now;
                getLog().error(getClass().getSimpleName() + ":" + callerClassMethodIdentification +
                        " execute QueryExecutionException, duration ms=" + duration);
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

    protected void setStatementConsistencyLevels(Statement statement) {
        if (statement.getConsistencyLevel() == null) {
            setStatementReadWriteConsistencyLevel(statement);
        }
        if (statement.getSerialConsistencyLevel() == null) {
            setStatementSerialConsistencyLevel(statement);
        }
    }

    private void setStatementReadWriteConsistencyLevel(Statement statement) {
        if (statement instanceof Select || statement instanceof Select.Where) {
            statement.setConsistencyLevel(readConsistency);
        } else if (statement instanceof Insert
                || statement instanceof Update
                || statement instanceof Update.Where
                || statement instanceof Delete
                || statement instanceof Delete.Where) {

            statement.setConsistencyLevel(writeConsistency);
        }
    }

    private void setStatementSerialConsistencyLevel(Statement statement) {
        statement.setSerialConsistencyLevel(serialConsistency);
    }

    protected ResultSet insert(KEY key, String columnName, Object value) {
        return insert(key, columnName, value, false);
    }

    protected ResultSet insert(KEY key, Map<String,Object> columnValues) {
        return insert(key, columnValues, false);
    }

    protected ResultSet insert(KEY key, String columnName, Object value, boolean ifNotExist) {
        return insert(key, Collections.singletonMap(columnName, value), ifNotExist);
    }

    protected ResultSet insert(KEY key, Map<String,Object> columnValues, boolean ifNotExist) {
        if (columnValues.isEmpty()) {
            throw new IllegalArgumentException("columnValues cannot be empty");
        }
        long now = System.currentTimeMillis();
        Insert insert = QueryBuilder.insertInto(getMainTableDefinition().getTableName()).value(getKeyColumnName(), key);

        for (Entry<String, Object> columnValue : columnValues.entrySet()) {
            insert = insert.value(columnValue.getKey(), columnValue.getValue());
        }

        if (ifNotExist) { //Cannot provide custom timestamp for conditional updates
            insert.ifNotExists();
        }
        ResultSet resultSet;
        try {
            setStatementConsistencyLevels(insert);
            resultSet = session.execute(insert);
        } catch (QueryExecutionException e) {
            long duration = System.currentTimeMillis() - now;
            getLog().error(getClass().getSimpleName() + ":insert execute QueryExecutionException, duration ms=" + duration, e);
            StatisticsManager.getInstance().updateRequestStatistics(
                    getClass().getSimpleName() + " insertion timeout",
                    duration);
            throw e;
        }

        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " insert",
                System.currentTimeMillis() - now);
        return resultSet;
    }

    protected ResultSet insert(KEY key, String columnName, Object value, int ttl) {
        long now = System.currentTimeMillis();
        Insert insert = getInsertQuery().value(getKeyColumnName(), key).value(columnName, value);
        insert.using(QueryBuilder.ttl(ttl));
        ResultSet resultSet;
        try {
            resultSet = session.execute(insert);
        } catch (QueryExecutionException e) {
            long duration = System.currentTimeMillis() - now;
            getLog().error(getClass().getSimpleName() + ":insert execute QueryExecutionException, duration ms=" + duration, e);
            StatisticsManager.getInstance().updateRequestStatistics(
                    getClass().getSimpleName() + " insertion timeout",
                    duration);
            throw e;
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " insert",
                System.currentTimeMillis() - now);
        return resultSet;
    }

    protected Insert addInsertion(KEY key, String columnName, Object value) {
        return getInsertQuery().value(getKeyColumnName(), key).value(columnName, value);
    }

    protected Insert addInsertion(KEY key, String columnName, Object value, int ttl) {
        Insert insert = getInsertQuery().value(getKeyColumnName(), key).value(columnName, value);
        insert.using(QueryBuilder.ttl(ttl));
        return insert;
    }

    protected Insert addInsertion(String columnFamily, KEY key, String columnName, Object value, int ttl) {
        Insert insert = QueryBuilder.insertInto(columnFamily).value(getKeyColumnName(), key).value(columnName, value);
        insert.using(QueryBuilder.ttl(ttl));
        return insert;
    }

    protected Delete addItemDeletion(KEY key) {
        return addItemDeletion(getSimpleKeyClause(key));
    }

    protected Delete addItemDeletion(Clause... clauses) {
        return addItemDeletion(getMainColumnFamilyName(), clauses);
    }

    protected Delete addItemDeletion(String tableName, KEY key) {
        return addItemDeletion(tableName, getSimpleKeyClause(key));
    }

    protected Delete addItemDeletion(String tableName, Clause... clauses) {
        assertInitialized();
        Delete query = QueryBuilder.delete().from(tableName);
        Delete.Where where = query.where();
        for (Clause clause : clauses) {
            where.and(clause);
        }
        return query;
    }

    protected Delete addColumnDeletion(KEY key, String column) {
        return addColumnDeletion(new String[]{column}, getSimpleKeyClause(key));
    }

    protected Delete addColumnDeletion(String[] columns, Clause... clauses) {
        return addColumnDeletion(getMainColumnFamilyName(), columns, clauses);
    }

    protected Delete addColumnDeletion(String tableName, KEY key, String column) {
        return addColumnDeletion(tableName, new String[]{column}, getSimpleKeyClause(key));
    }

    protected Delete addColumnDeletion(String tableName, String[] columns, Clause... clauses) {
        assertInitialized();
        Delete query = QueryBuilder.delete(columns).from(tableName);
        Delete.Where where = query.where();
        for (Clause clause : clauses) {
            where.and(clause);
        }
        return query;
    }

    //not rename this method to delete(), need prevent conflict with void delete() from com.hazelcast.core.MapStore
    protected boolean deleteWithCheck(KEY key) {
        ResultSet resultSet = deleteItem(key);
        return resultSet.wasApplied();
    }

    //may be overridden if KEY column have another name
    protected Clause getSimpleKeyClause(KEY key) {
        return eq(getKeyColumnName(), key);
    }

    protected ResultSet deleteItem(KEY key) {
        Delete query = QueryBuilder.delete().from(getMainColumnFamilyName());
        query.where(getSimpleKeyClause(key));
        return execute(query, " deleteItem");
    }

    protected ResultSet deleteItem(Clause... clauses) {
        assertInitialized();
        Delete query = QueryBuilder.delete().from(getMainColumnFamilyName());
        Delete.Where where = query.where();
        for (Clause clause : clauses) {
            where.and(clause);
        }
        return execute(query, " deleteItem");
    }

    protected ResultSet deleteMapItem(String mapColumnName, String itemKey, Clause... clauses) {
        assertInitialized();
        Delete query = QueryBuilder.delete().mapElt(mapColumnName, itemKey).from(getMainColumnFamilyName());
        Delete.Where where = query.where();
        for (Clause clause : clauses) {
            where.and(clause);
        }
        return execute(query, " deleteMapItem");
    }

    protected Batch batch() {
        return QueryBuilder.batch();
    }

    protected ResultSet deleteColumn(String column, Clause... clauses) {
        assertInitialized();
        Delete query = QueryBuilder.delete(column).from(getMainColumnFamilyName());
        Delete.Where where = query.where();
        for (Clause clause : clauses) {
            where.and(clause);
        }
        return execute(query, " deleteColumn");
    }

    protected ResultSet deleteColumn(String[] columns, Clause... clauses) {
        assertInitialized();
        Delete query = QueryBuilder.delete(columns).from(getMainColumnFamilyName());
        Delete.Where where = query.where();
        for (Clause clause : clauses) {
            where.and(clause);
        }
        return execute(query, " deleteColumn");
    }

    protected ResultSet deleteColumn(KEY key, String column) {
        assertInitialized();
        Delete query = QueryBuilder.delete(column).from(getMainColumnFamilyName());
        query.where(getSimpleKeyClause(key));
        return execute(query, " deleteColumn");
    }

    public int size() {
        return (int) count();
    }

    protected long count(TableDefinition tableDef, List<Clause> clauses) {
        return clauses == null ? count(tableDef) : count(tableDef, clauses.toArray(new Clause[clauses.size()]));
    }

    protected long count(List<Clause> clauses) {
        return clauses == null ? count() : count(clauses.toArray(new Clause[clauses.size()]));
    }

    protected long count(TableDefinition tableDef, Clause... where) {
        Select query = QueryBuilder.select().countAll().from(tableDef.getTableName());
        if (where != null) {
            for (Clause clause : where) {
                query.where().and(clause);
            }
        }
        ResultSet resultSet = execute(query, "count");
        Row row = resultSet.one();
        return row == null ? 0 : row.getLong("count");
    }

    protected long count(Clause... where) {
        return count(getMainTableDefinition(), where);
    }

    protected Iterator<Row> getAll() {
        return getAll(null);
    }

    protected Iterator<Row> getAll(Clause clause) {
        long now = System.currentTimeMillis();
        Select query = QueryBuilder.select().all().from(getMainColumnFamilyName());
        if (clause != null) {
            query.where(clause);
        }
        try {
            setStatementConsistencyLevels(query);
            ResultSet execute = session.execute(query);
            Iterator<Row> iterator = execute.iterator();
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getAll", System.currentTimeMillis() - now);
            return iterator;
        } catch (QueryExecutionException e) {
            long duration = System.currentTimeMillis() - now;
            getLog().error(getClass().getSimpleName() + ":getAll execute QueryExecutionException, duration ms=" + duration, e);
            StatisticsManager.getInstance().updateRequestStatistics(
                    getClass().getSimpleName() + " getAll timeout",
                    duration);
            throw e;
        }
    }

    protected Row getByKey(KEY key) {
        Select query = QueryBuilder.select().all().from(getMainColumnFamilyName()).where(eq(getKeyColumnName(), key)).
                limit(1);
        ResultSet resultSet = execute(query, "getByKey");
        return resultSet.one();
    }

    protected ByteBuffer get(Map<String, Object> keys, String columnName) {
        assertInitialized();
        Select query = QueryBuilder.select(columnName).from(getMainColumnFamilyName());
        Select.Where where = null;

        for (Map.Entry<String, Object> entry : keys.entrySet()) {
            if (where == null) {
                where = query.where(eq(entry.getKey(), entry.getValue()));
            } else {
                where.and(eq(entry.getKey(), entry.getValue()));
            }
        }
        query.limit(1);
        ResultSet rows = execute(query, "get");
        ByteBuffer result = null;
        if (rows.iterator().hasNext()) {
            Row row = rows.iterator().next();
            result = row.getBytes(columnName);
        }
        if (result == null) {
            getLog().warn("get: QueryResult is null, key=" + keys);
        }
        return result;
    }

    protected ByteBuffer get(String columnName, Clause... clauses) {
        assertInitialized();
        Select query = QueryBuilder.select(columnName).from(getMainColumnFamilyName()).limit(1);
        for (Clause clause : clauses) {
            query.where(clause);
        }
        ResultSet rows = execute(query, "get");
        ByteBuffer result = null;
        if (rows.iterator().hasNext()) {
            Row row = rows.iterator().next();
            result = row.getBytes(columnName);
        }
        if (result == null) {
            String clausesString = Arrays.toString(clauses);
            getLog().warn("get: QueryResult is null, clauses={}", clausesString);
        }
        return result;
    }

    protected ByteBuffer get(Clause clause, String columnName) {
        return get(columnName, clause);
    }

    protected Row getAsRow(KEY key, String columnName) {
        assertInitialized();
        Select query = QueryBuilder.select(getKeyColumnName(), columnName).from(getMainColumnFamilyName()).
                where(eq(getKeyColumnName(), key)).limit(1);
        ResultSet rows = execute(query, "getAsRow");
        return rows.one();
    }

    protected Long getWriteTime(KEY key) {
        assertInitialized();
        Select query = QueryBuilder.select().
                writeTime(SERIALIZED_COLUMN_NAME).
                writeTime(JSON_COLUMN_NAME).
                from(getMainColumnFamilyName()).
                where(eq(getKeyColumnName(), key)).
                limit(1);
        ResultSet rows = execute(query, "getWriteTime");
        Row row = rows.one();
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
        Select query =
                QueryBuilder.
                        select(columnName).
                        from(getMainColumnFamilyName()).
                        where(eq(getKeyColumnName(), key)).
                        limit(1);
        ResultSet rows = execute(query, "get");
        Row row = rows.one();
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
        Select query =
                QueryBuilder.
                        select(JSON_COLUMN_NAME).
                        from(getMainColumnFamilyName()).
                        where(eq(getKeyColumnName(), key)).
                        limit(1);
        ResultSet rows = execute(query, "get");
        Row row = rows.one();
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
        Select query = QueryBuilder.select().all().from(getMainColumnFamilyName());
        ResultSet resultSet = execute(query, "iterateAllColumnFamily");
        getLog().debug("iterateAllColumnFamily: getAvailableWithoutFetching=" +
                resultSet.getAvailableWithoutFetching());
        int count = 0;
        for (Row row : resultSet) {
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
                                                                             Clause... clauses) {
        return getAsIterableSkipNull(entryColumnNames, callerClassMethodIdentification, readTimeout,
                getDeserializeColumnFunction(entryClass), clauses);
    }

    protected <E extends KryoSerializable> Iterable<E> getAsIterableSkipNull(final String[] entryColumnNames,
                                                                             final Class<E> entryClass,
                                                                             String callerClassMethodIdentification,
                                                                             Clause... clauses) {
        return getAsIterableSkipNull(entryColumnNames, callerClassMethodIdentification,
                getDeserializeColumnFunction(entryClass), clauses);
    }

    protected <E extends KryoSerializable> Iterable<E> executeAndGetAsIterableSkipNull(Statement statement, String callerClassMethodIdentification,
                                                                                       final String entryColumnName, final Class<E> entryClass) {
        return executeAndGetAsIterableSkipNull(statement, callerClassMethodIdentification, getDeserializeColumnFunction(entryClass));
    }

    protected <E extends KryoSerializable> Iterable<E> executeAndGetAsIterable(Statement statement, String callerClassMethodIdentification,
                                                                               final String entryColumnName, final Class<E> entryClass) {
        return executeAndGetAsIterable(statement, callerClassMethodIdentification, getDeserializeColumnFunction(entryClass));
    }

    protected <E extends KryoSerializable> Iterable<E> deserializeIterable(Iterable<Row> iterable, final String entryColumnName, final Class<E> entryClass) {
        return StreamUtils.asStream(iterable)
                .map(getDeserializeColumnFunction(entryClass))
                .collect(Collectors.toList());
    }

    private <E> Function<Row, E> getDeserializeColumnFunction(final Class<E> entryClass) {
        return row -> {
            E obj = getMainTableDefinition().deserializeFromJson(row.getString(JSON_COLUMN_NAME), entryClass);

            if (obj == null) {
                obj = getMainTableDefinition().deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), entryClass);
            }
            return obj;
        };
    }

    protected <E> Iterable<E> getAsIterableSkipNull(final String[] entryColumnNames, String callerClassMethodIdentification,
                                                    Integer readTimeout, Function<Row, E> function, Clause... clauses) {
        Select.Where select = getSelectColumnsQuery(entryColumnNames).where();
        if (readTimeout != null) {
            select.setReadTimeoutMillis(readTimeout);
        }
        for (Clause clause : clauses) {
            select.and(clause);
        }
        return executeAndGetAsIterableSkipNull(select, callerClassMethodIdentification, function);
    }

    protected <E> Iterable<E> getAsIterableSkipNull(final String[] entryColumnNames, String callerClassMethodIdentification,
                                                    Function<Row, E> function, Clause... clauses) {
        return getAsIterableSkipNull(entryColumnNames, callerClassMethodIdentification, null, function, clauses);
    }

    protected <E> Iterable<E> executeAndGetAsIterableSkipNull(Statement statement, String callerClassMethodIdentification,
                                                              Function<Row, E> function) {
        return StreamUtils.asStream(executeAndGetAsIterable(statement, callerClassMethodIdentification, function))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected <E> Iterable<E> executeAndGetAsIterable(Statement statement, String callerClassMethodIdentification,
                                                      Function<Row, E> function) {
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
