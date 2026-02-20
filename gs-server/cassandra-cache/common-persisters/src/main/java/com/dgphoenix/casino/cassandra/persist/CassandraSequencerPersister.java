package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.persist.StreamPersister;
import com.dgphoenix.casino.common.persist.TableProcessor;
import com.dgphoenix.casino.common.util.ISequencer;
import com.dgphoenix.casino.common.util.ISequencerPersister;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * User: flsh
 * Date: 4/10/12
 */
public class CassandraSequencerPersister extends AbstractCassandraPersister<String, String>
        implements ISequencerPersister, StreamPersister<String, ISequencer> {
    public static final String SEQUENCER_CF = "SequencerCF";
    public static final String VALUE_COLUMN_NAME = "V";
    private static final Logger LOG = LogManager.getLogger(CassandraSequencerPersister.class);
    private static final TableDefinition TABLE = new TableDefinition(SEQUENCER_CF,
            Arrays.asList(
                    //key is sequencer name
                    new ColumnDefinition(KEY, DataType.text(), false, false, true),
                    new ColumnDefinition(VALUE_COLUMN_NAME, DataType.bigint(), false, false, false)
            ),
            Collections.singletonList(KEY));

    private CassandraSequencerPersister() {
    }

    @Override
    public ISequencer importSequencer(ISequencer seq) throws CommonException {
        return getOrCreateSequencer(seq.getName(), seq.getValue());
    }

    @Override
    public ISequencer getOrCreateSequencer(String name) throws CommonException {
        return getOrCreateSequencer(name, getStartValue());
    }

    private ISequencer getOrCreateSequencer(String name, long startValue) {
        long now = System.currentTimeMillis();
        CassandraSequencer sequencer;
        try {
            Long currentValue = getCurrentValue(name);
            if (currentValue == null) {
                sequencer = new CassandraSequencer(name, startValue);
            } else {
                sequencer = new CassandraSequencer(name, currentValue);
            }
            //reserve block
            persist(sequencer, currentValue, sequencer.getBase() + CassandraSequencer.BLOCK);
        } catch (Exception e) {
            LOG.error("unexpected error", e);
            return null;
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics("CassandraSequencerPersister: getOrCreateSequencer",
                    System.currentTimeMillis() - now);
        }
        return sequencer;
    }

    public Long getCurrentValue(String sequencerName) {
        Select query = QueryBuilder.select(VALUE_COLUMN_NAME).
                from(getMainColumnFamilyName()).where(eq("KEY", sequencerName)).limit(1);
        ResultSet rows = execute(query, "getCurrentValue");
        Row row = rows.one();
        return row == null || row.isNull(VALUE_COLUMN_NAME) ? null : row.getLong(VALUE_COLUMN_NAME);
    }

    //return start value for reserved block
    public long allocateNextBlock(String name, long block) {
        long baseValue;
        long now = System.currentTimeMillis();
        try {
            Long currentValue = getCurrentValue(name);
            baseValue = Preconditions.checkNotNull(currentValue, "Cannot allocate next block, sequencer not found: " + name + ", column is null");
            int attemptsCount = 0;
            boolean success = false;
            while (!success) {
                //reserve block
                Update updateQuery = getUpdateQuery();
                updateQuery.where(getSimpleKeyClause(name)).
                        with(QueryBuilder.set(VALUE_COLUMN_NAME, baseValue + block)).
                        onlyIf(eq(VALUE_COLUMN_NAME, currentValue));
                ResultSet resultSet = executeWithCheckTimeout(updateQuery, "allocateNextBlock");
                success = resultSet.wasApplied();
                if (!success) {
                    attemptsCount++;
                    currentValue = getCurrentValue(name);
                    LOG.error("Concurrent modification: expected={}, attemptsCount={}, currentValue={}", baseValue, attemptsCount, currentValue);
                    if (attemptsCount >= MAX_WRITE_ATEMPTS_COUNT) {
                        throw new CommonException("Cannot allocate new block exceeded max attempts count, seq: " + name);
                    }
                    baseValue = currentValue;
                }
            }
        } catch (Exception e) {
            LOG.error("unexpected error", e);
            throw new IllegalStateException("Cannot allocate next block", e);
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics("CassandraSequencerPersister: getOrCreateSequencer",
                    System.currentTimeMillis() - now);
        }
        return baseValue;
    }

    private void persist(CassandraSequencer seq, Long currentValue, long newValue) {
        boolean success = false;
        Long newCurrentValue = currentValue;
        long newDesiredValue = newValue;

        if (currentValue == null) {
            getLog().info("persist: add new sequencer: " + seq + ", newValue=" + newValue);
            ResultSet resultSet = executeWithCheckTimeout(
                    addInsertion(seq.getName(), VALUE_COLUMN_NAME, newValue).ifNotExists(),
                    "persist[insert]");
            if (!resultSet.wasApplied()) {
                getLog().warn("Insert failed, sequencer already exist: seq=" + seq + ", newValue=" + newValue);
                newCurrentValue = 0l;
                Row row = resultSet.one();
                if (row != null) {
                    newCurrentValue = row.getLong(VALUE_COLUMN_NAME);
                }
                if (newCurrentValue <= 0) {
                    newCurrentValue = getCurrentValue(seq.getName());
                }
                newDesiredValue = newCurrentValue + CassandraSequencer.BLOCK;
            } else {
                success = true;
            }
        }
        if (!success) {
            int attemptsCount = 0;
            while (!success) {
                Update query = getUpdateQuery(seq.getName());
                query.with(QueryBuilder.set(VALUE_COLUMN_NAME, newDesiredValue));
                query.onlyIf(eq(VALUE_COLUMN_NAME, newCurrentValue));
                ResultSet resultSet = executeWithCheckTimeout(query, "persist[update]");

                success = resultSet.wasApplied();
                if (success) {
                    seq.setBase(newDesiredValue);
                } else {
                    attemptsCount++;
                    LOG.error("Concurrent modification: expected={}, attemptsCount={}", newCurrentValue, attemptsCount);
                    if (attemptsCount >= MAX_WRITE_ATEMPTS_COUNT) {
                        throw new ConcurrentModificationException("Cannot allocate new block exceeded max attempts count, seq: " + seq.getName());
                    } else {
                        newCurrentValue = 0L;
                        Row row = resultSet.one();
                        if (row != null) {
                            newCurrentValue = row.getLong(VALUE_COLUMN_NAME);
                        }
                        if (newCurrentValue <= 0) {
                            newCurrentValue = getCurrentValue(seq.getName());
                        }
                        newDesiredValue = newCurrentValue + CassandraSequencer.BLOCK;
                    }
                    newCurrentValue = 0L;
                    Row row = resultSet.one();
                    if (row != null) {
                        newCurrentValue = row.getLong(VALUE_COLUMN_NAME);
                    }
                    if (newCurrentValue <= 0) {
                        newCurrentValue = getCurrentValue(seq.getName());
                    }
                    newDesiredValue = newCurrentValue + CassandraSequencer.BLOCK;
                }
            }
        }
    }

    private long getStartValue() {
        long startValue = 1;
        try {
            String property = System.getProperty("ID_GENERATOR_START_VALUE");
            if (!StringUtils.isTrimmedEmpty(property)) {
                startValue = Long.parseLong(property);
            }
        } catch (Throwable e) {
            LOG.debug("init error:", e);
        }
        return startValue;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public void processAll(TableProcessor<Pair<String, ISequencer>> tableProcessor) throws IOException {
        Iterator<Row> iterator = getAll();
        while (iterator.hasNext()) {
            Row row = iterator.next();
            String name = row.getString(KEY);
            long value = row.getLong(VALUE_COLUMN_NAME);
            CassandraSequencer sequencer = new CassandraSequencer(name, value);
            tableProcessor.process(new Pair<>(name, sequencer));
        }
    }

    @Override
    public void processByCondition(TableProcessor<Pair<String, ISequencer>> tableProcessor, String conditionName, Object... conditionValues) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
