package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.util.StreamUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CassandraCurrencyRatesByDatePersister extends AbstractCassandraPersister<String, String> {
    private static final String COLUMN_FAMILY = "CurrencyRatesByDateCF";
    private static final String SOURCE_FIELD = "SOURCE";
    private static final String DEST_FIELD = "DEST";
    private static final String RATE_FIELD = "CRATE";
    private static final String UPDATE_DATE_FIELD = "UPDATE_DATE";
    private static final long DATE_PRECISION = TimeUnit.SECONDS.toMillis(60);

    private static final Logger LOG = LogManager.getLogger(CassandraCurrencyRatesByDatePersister.class);

    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY,
            Arrays.asList(
                    new ColumnDefinition(SOURCE_FIELD, DataType.text(), false, false, true),
                    new ColumnDefinition(DEST_FIELD, DataType.text(), false, false, true),
                    new ColumnDefinition(UPDATE_DATE_FIELD, DataType.bigint(), false, true, true),
                    new ColumnDefinition(RATE_FIELD, DataType.cdouble(), false, false, false)
                ), SOURCE_FIELD, DEST_FIELD);

    private CassandraCurrencyRatesByDatePersister() {
        super();
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    public void createOrUpdate(CurrencyRate currencyRate) {
        Insert query = getInsertQuery()
            .value(SOURCE_FIELD, currencyRate.getSourceCurrency())
            .value(DEST_FIELD, currencyRate.getDestinationCurrency())
            .value(UPDATE_DATE_FIELD, normalizeDate(currencyRate.getUpdateDate()))
            .value(RATE_FIELD, currencyRate.getRate());
        execute(query, "createOrUpdate");
    }

    public void createOrUpdate(long date, Set<CurrencyRate> currencyRates) {
        Batch batch = batch();
        long normalizedDate = normalizeDate(date);
        for (CurrencyRate currencyRate : currencyRates) {
            Insert query = getInsertQuery();
            query.value(SOURCE_FIELD, currencyRate.getSourceCurrency())
                    .value(DEST_FIELD, currencyRate.getDestinationCurrency())
                    .value(UPDATE_DATE_FIELD, normalizedDate)
                    .value(RATE_FIELD, currencyRate.getRate());
            batch.add(query);
        }
        execute(batch, "createOrUpdateByDate");
    }

    public CurrencyRate getCurrencyRate(long date, String source, String target) {
        long normalizedDate = normalizeDate(date);
        Select select = getSelectColumnsQuery(RATE_FIELD);
        select.where().and(eq(SOURCE_FIELD, source)).and(eq(DEST_FIELD, target)).and(eq(UPDATE_DATE_FIELD, normalizedDate));
        Row row = execute(select, "getCurrencyRate").one();
        CurrencyRate result = null;
        if (row != null && !row.isNull(RATE_FIELD)) {
            double rate = row.getDouble(RATE_FIELD);
            result = new CurrencyRate(source, target, rate, normalizedDate);
        }
        LOG.debug("getCurrencyRate: date={}, source={}, target={}, result={}", date, source, target, result);
        return result;
    }

    public Collection<CurrencyRate> getRates(long date) {
        long normalizedDate = normalizeDate(date);
        Select select = getSelectColumnsQuery(SOURCE_FIELD, DEST_FIELD, RATE_FIELD);
        select.where().and(eq(UPDATE_DATE_FIELD, normalizedDate));
        return StreamUtils.asStream(execute(select, "getRatesByDate"))
                .filter(Objects::nonNull)
                .filter(row -> {
                    if (row.isNull(RATE_FIELD)) {
                        LOG.warn("Undefined rate for exchange {} -> {}", row.getString(SOURCE_FIELD), row.getString(DEST_FIELD));
                        return false;
                    } else {
                        return true;
                    }
                })
                .map(row -> {
                    String source = row.getString(SOURCE_FIELD);
                    String target = row.getString(DEST_FIELD);
                    double rate = row.getDouble(RATE_FIELD);
                    return new CurrencyRate(source, target, rate, normalizedDate);
                })
                .collect(Collectors.toSet());
    }

    public void delete(long date) {
        deleteItem(eq(UPDATE_DATE_FIELD, normalizeDate(date)));
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    public long normalizeDate(long date) {
        return date / DATE_PRECISION * DATE_PRECISION;
    }
}
