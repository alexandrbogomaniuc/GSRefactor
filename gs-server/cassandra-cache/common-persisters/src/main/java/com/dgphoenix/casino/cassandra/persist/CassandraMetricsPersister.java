package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.cassandra.persist.engine.configuration.Caching;
import com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.abs.casino.cassandra.persist.engine.configuration.Compression;
import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.common.util.system.Metric;
import com.abs.casino.common.util.system.MetricStat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Created by quant on 19.11.15.
 */
public class CassandraMetricsPersister extends AbstractCassandraPersister<Integer, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraMetricsPersister.class);
    public static final String METRICS_CF = "MetricsCF";
    public static final String LAST_STAT_TIME_CF = "LastStatTimeCF";
    public static final String METRICS_STAT_CF = "MetricsStatCF";
    public static final String METRIC_ID_FIELD = "MetricId";
    public static final String GAME_SERVER_ID_FIELD = "GameServerId";
    public static final String LOG_TIME_FIELD = "LogTime";
    public static final String METRIC_VALUE_FIELD = "MetricValue";
    public static final String STAT_TIME_FIELD = "StatTime";
    public static final String AVERAGE_VALUE_FIELD = "AverageValue";
    public static final String MIN_VALUE_TIME_FIELD = "MinValueTime";
    public static final String MIN_VALUE_FIELD = "MinValue";
    public static final String MAX_VALUE_TIME_FIELD = "MaxValueTime";
    public static final String MAX_VALUE_FIELD = "MaxValue";
    public static final String LAST_STAT_TIME_FIELD = "LastStatTime";

    private static final TableDefinition METRICS_TABLE = new TableDefinition(METRICS_CF,
            Arrays.asList(
                    new ColumnDefinition(METRIC_ID_FIELD, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(GAME_SERVER_ID_FIELD, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(LOG_TIME_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(METRIC_VALUE_FIELD, com.datastax.driver.core.DataType.bigint())
            ), METRIC_ID_FIELD, GAME_SERVER_ID_FIELD)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.NONE);


    private static final TableDefinition METRICS_STAT_TABLE = new TableDefinition(METRICS_STAT_CF,
            Arrays.asList(
                    new ColumnDefinition(METRIC_ID_FIELD, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(GAME_SERVER_ID_FIELD, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(STAT_TIME_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(AVERAGE_VALUE_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(MIN_VALUE_TIME_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(MIN_VALUE_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(MAX_VALUE_TIME_FIELD, com.datastax.driver.core.DataType.bigint()),
                    new ColumnDefinition(MAX_VALUE_FIELD, com.datastax.driver.core.DataType.bigint())
            ), METRIC_ID_FIELD, GAME_SERVER_ID_FIELD)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.NONE);

    private static final TableDefinition LAST_STAT_TIME_TABLE = new TableDefinition(LAST_STAT_TIME_CF,
            Arrays.asList(
                    new ColumnDefinition(METRIC_ID_FIELD, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(GAME_SERVER_ID_FIELD, com.datastax.driver.core.DataType.cint(), false, false, true),
                    new ColumnDefinition(LAST_STAT_TIME_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, false)
            ), METRIC_ID_FIELD, GAME_SERVER_ID_FIELD)
            .caching(Caching.NONE)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.NONE);

    private CassandraMetricsPersister() {
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return METRICS_TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(METRICS_TABLE, METRICS_STAT_TABLE, LAST_STAT_TIME_TABLE);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    protected String getKeyColumnName() {
        return METRIC_ID_FIELD;
    }

    public void persist(Map<Metric, Pair<Long, Long>> metrics, int gameServerId) {
        if (metrics.isEmpty()) {
            return;
        }
        for (Map.Entry<Metric, Pair<Long, Long>> entry : metrics.entrySet()) {
            Metric metric = entry.getKey();
            Pair<Long, Long> pair = entry.getValue();
            long logTime = pair.getKey();
            long metricValue = pair.getValue();
            com.datastax.driver.core.querybuilder.Insert query = getInsertQuery();
            query.value(METRIC_ID_FIELD, metric.ordinal());
            query.value(GAME_SERVER_ID_FIELD, gameServerId);
            query.value(LOG_TIME_FIELD, logTime);
            query.value(METRIC_VALUE_FIELD, metricValue);
            executeAsync(query, "persist");
        }
    }

    public void persistStat(int gameServerId) {
        com.datastax.driver.core.querybuilder.Batch batch = com.datastax.driver.core.querybuilder.QueryBuilder.unloggedBatch();
        for (Metric metric : Metric.values()) {
            persistStat(batch, metric, gameServerId);
        }
        execute(batch, "persistStat in batch");
    }

    private void persistStat(com.datastax.driver.core.querybuilder.Batch batch, Metric metric, int gameServerId) {
        Calendar start = Calendar.getInstance();
        long lastStatTime = getLastStatTime(metric, gameServerId);
        long nextStatTime = lastStatTime + TimeUnit.HOURS.toMillis(1);
        if (lastStatTime == 0) {
            nextStatTime = getFirstLogTime(metric, gameServerId);
        }

        start.setTimeInMillis(nextStatTime);
        start.set(Calendar.MINUTE, start.getActualMinimum(Calendar.MINUTE));
        start.set(Calendar.SECOND, start.getActualMinimum(Calendar.SECOND));
        start.set(Calendar.MILLISECOND, start.getActualMinimum(Calendar.MILLISECOND));

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(System.currentTimeMillis());
        end.set(Calendar.MINUTE, end.getActualMinimum(Calendar.MINUTE));
        end.set(Calendar.SECOND, end.getActualMinimum(Calendar.SECOND));
        end.set(Calendar.MILLISECOND, end.getActualMinimum(Calendar.MILLISECOND));

        while (start.before(end)) {
            long hourStart = start.getTimeInMillis();
            start.add(Calendar.HOUR_OF_DAY, 1);
            long hourEnd = start.getTimeInMillis();

            com.datastax.driver.core.ResultSet resultSet = queryMetricValues(metric, gameServerId, hourStart, hourEnd);
            if (!resultSet.isExhausted()) {
                long resultCount = 0;

                long minValue = Long.MAX_VALUE;
                long minValueTime = 0;
                long maxValue = Long.MIN_VALUE;
                long maxValueTime = 0;
                BigInteger averageValue = BigInteger.valueOf(0);

                for (com.datastax.driver.core.Row row : resultSet) {
                    long value = row.getLong(METRIC_VALUE_FIELD);
                    long time = row.getLong(LOG_TIME_FIELD);
                    averageValue = averageValue.add(BigInteger.valueOf(value));
                    if (value < minValue) {
                        minValue = value;
                        minValueTime = time;
                    }
                    if (value > maxValue) {
                        maxValue = value;
                        maxValueTime = time;
                    }
                    ++resultCount;
                }
                persistStat(batch, metric, gameServerId, hourStart, averageValue.divide(BigInteger.valueOf(resultCount)).longValue(),
                        minValueTime, minValue, maxValueTime, maxValue);
            }
        }
    }

    private void persistStat(com.datastax.driver.core.querybuilder.Batch batch, Metric metric, int gameServerId, long startTime, long averageValue,
                             long minValueTime, long minValue, long maxValueTime, long maxValue) {
        com.datastax.driver.core.querybuilder.Insert query = getInsertQuery(METRICS_STAT_TABLE, getTtl());
        query.value(METRIC_ID_FIELD, metric.ordinal());
        query.value(GAME_SERVER_ID_FIELD, gameServerId);
        query.value(STAT_TIME_FIELD, startTime);
        query.value(AVERAGE_VALUE_FIELD, averageValue);
        query.value(MIN_VALUE_TIME_FIELD, minValueTime);
        query.value(MIN_VALUE_FIELD, minValue);
        query.value(MAX_VALUE_TIME_FIELD, maxValueTime);
        query.value(MAX_VALUE_FIELD, maxValue);

        batch.add(query);

        com.datastax.driver.core.querybuilder.Update update = com.datastax.driver.core.querybuilder.QueryBuilder.update(LAST_STAT_TIME_TABLE.getTableName());
        update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(LAST_STAT_TIME_FIELD, startTime)).
                where(eq(METRIC_ID_FIELD, metric.ordinal())).
                and(eq(GAME_SERVER_ID_FIELD, gameServerId));

        batch.add(update);
    }

    private long getLastStatTime(Metric metric, int gameServerId) {
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().column(LAST_STAT_TIME_FIELD).from(LAST_STAT_TIME_CF);
        query.where(eq(METRIC_ID_FIELD, metric.ordinal())).
                and(eq(GAME_SERVER_ID_FIELD, gameServerId)).limit(1);
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getLastStatTime");
        if (resultSet.isExhausted()) {
            getLog().warn("Metric stat data is empty for metric={}, gameServerId={}", metric, gameServerId);
            return 0;
        }
        return resultSet.one().getLong(LAST_STAT_TIME_FIELD);
    }

    private long getFirstLogTime(Metric metric, int gameServerId) {
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().column(LOG_TIME_FIELD).from(getMainColumnFamilyName());
        query.where(eq(METRIC_ID_FIELD, metric.ordinal())).
                and(eq(GAME_SERVER_ID_FIELD, gameServerId)).limit(1);
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getFirstLogTime");
        if (resultSet.isExhausted()) {
            getLog().warn("Metric stat data is empty for metric={}, gameServerId={}", metric, gameServerId);
            return System.currentTimeMillis();
        }
        return resultSet.one().getLong(LOG_TIME_FIELD);
    }

    private com.datastax.driver.core.ResultSet queryMetricValues(Metric metric, int gameServerId, long startTime, long endTime) {
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().
                column(LOG_TIME_FIELD).
                column(METRIC_VALUE_FIELD).
                from(getMainColumnFamilyName());
        query.where(eq(METRIC_ID_FIELD, metric.ordinal())).
                and(eq(GAME_SERVER_ID_FIELD, gameServerId)).
                and(com.datastax.driver.core.querybuilder.QueryBuilder.gte(LOG_TIME_FIELD, startTime)).
                and(com.datastax.driver.core.querybuilder.QueryBuilder.lte(LOG_TIME_FIELD, endTime));
        return execute(query, "queryMetricValues");
    }

    public List<Pair<Long, Long>> getMetricValues(Metric metric, int gameServerId, long startTime, long endTime) {
        com.datastax.driver.core.ResultSet resultSet = queryMetricValues(metric, gameServerId, startTime, endTime);
        List<Pair<Long, Long>> values = new ArrayList<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            values.add(new Pair<>(row.getLong(LOG_TIME_FIELD), row.getLong(METRIC_VALUE_FIELD)));
        }
        return values;
    }

    public List<MetricStat> getMetricStatValues(Metric metric, int gameServerId, long startTime, long endTime) {
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().
                column(STAT_TIME_FIELD).
                column(AVERAGE_VALUE_FIELD).
                column(MIN_VALUE_TIME_FIELD).
                column(MIN_VALUE_FIELD).
                column(MAX_VALUE_TIME_FIELD).
                column(MAX_VALUE_FIELD).
                from(METRICS_STAT_CF);
        query.where(eq(METRIC_ID_FIELD, metric.ordinal())).
                and(eq(GAME_SERVER_ID_FIELD, gameServerId)).
                and(com.datastax.driver.core.querybuilder.QueryBuilder.gte(STAT_TIME_FIELD, startTime)).
                and(com.datastax.driver.core.querybuilder.QueryBuilder.lte(STAT_TIME_FIELD, endTime));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getMetricValues");
        List<MetricStat> values = new ArrayList<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            MetricStat metricStat = new MetricStat();
            metricStat.setStatTime(row.getLong(STAT_TIME_FIELD));
            metricStat.setAverageValue(row.getLong(AVERAGE_VALUE_FIELD));
            metricStat.setMinValueTime(row.getLong(MIN_VALUE_TIME_FIELD));
            metricStat.setMinValue(row.getLong(MIN_VALUE_FIELD));
            metricStat.setMaxValueTime(row.getLong(MAX_VALUE_TIME_FIELD));
            metricStat.setMaxValue(row.getLong(MAX_VALUE_FIELD));
            values.add(metricStat);
        }
        return values;
    }
}
