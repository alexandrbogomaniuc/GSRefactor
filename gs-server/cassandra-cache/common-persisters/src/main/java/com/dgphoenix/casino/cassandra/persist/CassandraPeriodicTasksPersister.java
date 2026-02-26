package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.Caching;
import com.dgphoenix.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by vladislav on 4/20/16.
 */
public class CassandraPeriodicTasksPersister extends AbstractCassandraPersister<String, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraPeriodicTasksPersister.class);

    private static final String PERIODIC_TASKS_TABLE_NAME = "PeriodicTasksCF";
    private static final String TASK_KEY = "key";
    private static final String LAST_EXECUTION_TIME = "exTime";
    private static final String TASK_DATA = "data";
    private static final TableDefinition PERIODIC_TASKS_TABLE = new TableDefinition(PERIODIC_TASKS_TABLE_NAME,
            Arrays.asList(
                    new ColumnDefinition(TASK_KEY, com.datastax.driver.core.DataType.text(), false, false, true),
                    new ColumnDefinition(LAST_EXECUTION_TIME, com.datastax.driver.core.DataType.bigint(), false, false, false),
                    new ColumnDefinition(TASK_DATA, com.datastax.driver.core.DataType.blob(), false, false, false)
            ), TASK_KEY)
            .compaction(CompactionStrategy.LEVELED)
            .caching(Caching.NONE);

    private CassandraPeriodicTasksPersister() {
    }

    public Long getLastExecutionTime(String taskKey) {
        com.datastax.driver.core.Statement selectExecutionTime = getSelectColumnsQuery(LAST_EXECUTION_TIME)
                .where(eq(TASK_KEY, taskKey));

        com.datastax.driver.core.Row result = execute(selectExecutionTime, "getLastExecutionTime").one();
        Long lastExecutionTime = null;
        if (result != null) {
            lastExecutionTime = result.getLong(LAST_EXECUTION_TIME);
        }

        return lastExecutionTime;
    }

    public <T> T getTaskData(String taskKey) {
        com.datastax.driver.core.Statement selectExecutionTime = getSelectColumnsQuery(TASK_DATA)
                .where(eq(TASK_KEY, taskKey));

        com.datastax.driver.core.Row result = execute(selectExecutionTime, "getTaskData").one();
        T taskData = null;
        if (result != null) {
            String json = result.getString(JSON_COLUMN_NAME);
            taskData = (T) getMainTableDefinition().deserializeWithClassFromJson(json);

            if (taskData == null) {
                ByteBuffer taskDataAsBytes = result.getBytes(TASK_DATA);
                if (taskDataAsBytes != null) {
                    taskData = getMainTableDefinition().deserializeWithClassFrom(taskDataAsBytes);
                }
            }
        }

        return taskData;
    }

    public void saveLastExecutionTime(String taskKey, long time) {
        com.datastax.driver.core.Statement insert = getInsertQuery()
                .value(TASK_KEY, taskKey)
                .value(LAST_EXECUTION_TIME, time);

        execute(insert, "saveLastExecutionTime");
    }

    public void saveTaskData(String taskKey, Object taskData) {
        ByteBuffer taskDataAsBytes = getMainTableDefinition().serializeWithClassToBytes(taskData);
        String json = getMainTableDefinition().serializeWithClassToJson(taskData);
        com.datastax.driver.core.Statement insert = getInsertQuery()
                .value(TASK_KEY, taskKey)
                .value(TASK_DATA, taskDataAsBytes)
                .value(JSON_COLUMN_NAME, json);

        execute(insert, "saveTaskData");
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return PERIODIC_TASKS_TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
