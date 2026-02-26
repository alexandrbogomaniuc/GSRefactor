package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.persist.StreamPersister;
import com.dgphoenix.casino.common.persist.TableProcessor;
import com.dgphoenix.casino.common.util.IIntegerSequencer;
import com.dgphoenix.casino.common.util.IIntegerSequencerPersister;
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
public class CassandraIntSequencerPersister extends AbstractCassandraPersister<String, String>
        implements IIntegerSequencerPersister, StreamPersister<String, IIntegerSequencer> {
    public static final String SEQUENCER_CF = "IntSequencerCF";
    public static final String VALUE_COLUMN_NAME = "V";
    private static final Logger LOG = LogManager.getLogger(CassandraIntSequencerPersister.class);
    private static final TableDefinition TABLE = new TableDefinition(SEQUENCER_CF,
            Arrays.asList(
                    //key is sequencer name
                    new ColumnDefinition(KEY, DataType.text(), false, false, true),
                    new ColumnDefinition(VALUE_COLUMN_NAME, DataType.cint(), false, false, false)
            ),
            Collections.singletonList(KEY));

    private CassandraIntSequencerPersister() {
    }

    @Override
    public IIntegerSequencer importSequencer(IIntegerSequencer seq) throws CommonException {
        return getOrCreateSequencer(seq.getName(), seq.getValue());
    }

    @Override
    public IIntegerSequencer getOrCreateSequencer(String name) throws CommonException {
        return getOrCreateSequencer(name, getStartValue());
    }

    private IIntegerSequencer getOrCreateSequencer(String name, int startValue) {
        long now = System.currentTimeMillis();
        CassandraIntegerSequencer sequencer;
        try {
            Integer currentValue = getCurrentValue(name);
            if (currentValue == null) {
                sequencer = new CassandraIntegerSequencer(name, startValue);
            } else {
                sequencer = new CassandraIntegerSequencer(name, currentValue);
            }
            //reserve block
            persist(sequencer, currentValue, sequencer.getBase() + CassandraIntegerSequencer.BLOCK);
        } catch (Exception e) {
            LOG.error("unexpected error", e);
            return null;
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics("CassandraIntSequencerPersister: getOrCreateSequencer",
                    System.currentTimeMillis() - now);
        }
        return sequencer;
    }

    public Integer getCurrentValue(String sequencerName) {
        com.datastax.driver.core.Statement query = QueryBuilder.select(VALUE_COLUMN_NAME).
                from(getMainColumnFamilyName()).where(eq("KEY", sequencerName)).limit(1);
        com.datastax.driver.core.ResultSet rows = execute(query, "getCurrentValue");
        com.datastax.driver.core.Row row = rows.one();
        return row == null || row.isNull(VALUE_COLUMN_NAME) ? null : row.getInt(VALUE_COLUMN_NAME);
    }

    //return start value for reserved block
    public int allocateNextBlock(String name, int block) {
        int baseValue;
        long now = System.currentTimeMillis();
        try {
            Integer currentValue = getCurrentValue(name);
            baseValue = Preconditions.checkNotNull(currentValue, "Cannot allocate next block, sequencer not found: " + name +
                    ", column is null");
            int attemptsCount = 0;
            boolean success = false;
            while (!success) {
                //reserve block
                com.datastax.driver.core.Statement updateQuery = getUpdateQuery()
                        .where(getSimpleKeyClause(name))
                        .with(QueryBuilder.set(VALUE_COLUMN_NAME, baseValue + block))
                        .onlyIf(eq(VALUE_COLUMN_NAME, currentValue));
                com.datastax.driver.core.ResultSet resultSet = executeWithCheckTimeout(updateQuery, "allocateNextBlock");
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

    private void persist(CassandraIntegerSequencer seq, Integer currentValue, int newValue) {
        boolean success = false;
        Integer newCurrentValue = currentValue;
        int newDesiredValue = newValue;
        if (currentValue == null) {
            getLog().info("persist: add new sequencer: " + seq + ", newValue=" + newValue);
            com.datastax.driver.core.ResultSet resultSet = executeWithCheckTimeout(
                    addInsertion(seq.getName(), VALUE_COLUMN_NAME, newValue).ifNotExists(),
                    "persist[insert]");
            if (!resultSet.wasApplied()) {
                getLog().warn("Insert failed, sequencer already exist: seq={}, newValue={}", seq, newValue);
                newCurrentValue = 0;
                com.datastax.driver.core.Row row = resultSet.one();
                if (row != null) {
                    newCurrentValue = row.getInt(VALUE_COLUMN_NAME);
                }
                if (newCurrentValue <= 0) {
                    newCurrentValue = getCurrentValue(seq.getName());
                }
                newDesiredValue = newCurrentValue + CassandraIntegerSequencer.BLOCK;
            } else {
                success = true;
            }
        }
        if (!success) {
            int attemptsCount = 0;
            while (!success) {
                com.datastax.driver.core.Statement query = getUpdateQuery(seq.getName())
                        .with(QueryBuilder.set(VALUE_COLUMN_NAME, newDesiredValue))
                        .onlyIf(eq(VALUE_COLUMN_NAME, newCurrentValue));
                com.datastax.driver.core.ResultSet resultSet = executeWithCheckTimeout(query, "persist[update]");
                success = resultSet.wasApplied();
                if (success) {
                    seq.setBase(newDesiredValue);
                } else {
                    attemptsCount++;
                    LOG.error("Concurrent modification: expected={}, attemptsCount={}", newCurrentValue, attemptsCount);
                    if (attemptsCount >= MAX_WRITE_ATEMPTS_COUNT) {
                        throw new ConcurrentModificationException("Cannot allocate new block exceeded max " +
                                "attempts count, seq: " + seq.getName());
                    } else {
                        newCurrentValue = 0;
                        com.datastax.driver.core.Row row = resultSet.one();
                        if (row != null) {
                            newCurrentValue = row.getInt(VALUE_COLUMN_NAME);
                        }
                        if (newCurrentValue <= 0) {
                            newCurrentValue = getCurrentValue(seq.getName());
                        }
                        newDesiredValue = newCurrentValue + CassandraIntegerSequencer.BLOCK;
                    }
                    newCurrentValue = 0;
                    com.datastax.driver.core.Row row = resultSet.one();
                    if (row != null) {
                        newCurrentValue = row.getInt(VALUE_COLUMN_NAME);
                    }
                    if (newCurrentValue <= 0) {
                        newCurrentValue = getCurrentValue(seq.getName());
                    }
                    newDesiredValue = newCurrentValue + CassandraIntegerSequencer.BLOCK;
                }
            }
        }
    }

    private int getStartValue() {
        int startValue = 1;
        try {
            String property = System.getProperty("ID_INT_GENERATOR_START_VALUE");
            if (!StringUtils.isTrimmedEmpty(property)) {
                startValue = Integer.parseInt(property);
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
    public void processAll(TableProcessor<Pair<String, IIntegerSequencer>> tableProcessor) throws IOException {
        Iterator<com.datastax.driver.core.Row> iterator = getAll();
        while (iterator.hasNext()) {
            com.datastax.driver.core.Row row = iterator.next();
            String name = row.getString(KEY);
            int value = row.getInt(VALUE_COLUMN_NAME);
            CassandraIntegerSequencer sequencer = new CassandraIntegerSequencer(name, value);
            tableProcessor.process(new Pair<>(name, sequencer));
        }
    }

    @Override
    public void processByCondition(TableProcessor<Pair<String, IIntegerSequencer>> tableProcessor, String conditionName,
                                   Object... conditionValues)
            throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
