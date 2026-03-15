package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.Cql;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.Row;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.currency.CurrencyRate;
import com.abs.casino.common.currency.ICurrencyRateChangedListener;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.StreamUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cdouble;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 22.03.13
 */
public class CassandraCurrencyRatesPersister extends AbstractCassandraPersister<String, String> {
    private static final List<ICurrencyRateChangedListener> changedListeners = new ArrayList<>();

    private static final String COLUMN_FAMILY = "CurrencyRatesCF";
    private static final String SOURCE_FIELD = "SOURCE";
    private static final String DEST_FIELD = "DEST";
    private static final String RATE_FIELD = "CRATE";
    private static final String UPDATE_DATE_FIELD = "UPDATE_DATE";

    private static final Logger LOG = LogManager.getLogger(CassandraCurrencyRatesPersister.class);

    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY,
            Arrays.asList(
                    new ColumnDefinition(SOURCE_FIELD, text(), false, false, true),
                    new ColumnDefinition(DEST_FIELD, text(), false, false, true),
                    new ColumnDefinition(RATE_FIELD, cdouble(), false, false, false),
                    new ColumnDefinition(UPDATE_DATE_FIELD, bigint(), false, false, false)
            ), SOURCE_FIELD, DEST_FIELD);

    private CassandraCurrencyRatesPersister() {
        super();
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    public void registerListener(ICurrencyRateChangedListener listener) {
        changedListeners.add(listener);
    }

    public void createOrUpdate(CurrencyRate currencyRate) {
        com.abs.casino.cassandra.persist.engine.Statement query = com.abs.casino.cassandra.persist.engine.Statement.of(getInsertQuery()
                .value(SOURCE_FIELD, currencyRate.getSourceCurrency())
                .value(DEST_FIELD, currencyRate.getDestinationCurrency())
                .value(RATE_FIELD, currencyRate.getRate())
                .value(UPDATE_DATE_FIELD, currencyRate.getUpdateDate()));
        execute(query, "create");
        Pair<String, String> pair = new Pair<>(currencyRate.getSourceCurrency(), currencyRate.getDestinationCurrency());
        for (ICurrencyRateChangedListener listener : changedListeners) {
            listener.notify(pair);
        }
    }

    public CurrencyRate getCurrencyRate(String source, String target) {
        com.abs.casino.cassandra.persist.engine.Statement query = com.abs.casino.cassandra.persist.engine.Statement.of(getSelectColumnsQuery(RATE_FIELD, UPDATE_DATE_FIELD)
                .where(Cql.eq(SOURCE_FIELD, source))
                .and(Cql.eq(DEST_FIELD, target)));
        Row row = executeWrapped(query, "getRate").one();
        CurrencyRate result = null;
        if (row != null && !row.isNull(RATE_FIELD)) {
            double rate = row.getDouble(RATE_FIELD);
            long updateDate = row.getLong(UPDATE_DATE_FIELD);
            result = new CurrencyRate(source, target, rate, updateDate);
        }
        LOG.debug("getRate: source={}, target={}, result={}", source, target, result);
        return result;
    }

    public Collection<CurrencyRate> getRates() {
        com.abs.casino.cassandra.persist.engine.Statement query = com.abs.casino.cassandra.persist.engine.Statement.of(getSelectColumnsQuery(SOURCE_FIELD, DEST_FIELD, RATE_FIELD, UPDATE_DATE_FIELD));
        return StreamUtils.asStream(executeWrapped(query, "getRates"))
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
                    long updateDate = row.getLong(UPDATE_DATE_FIELD);
                    return new CurrencyRate(source, target, rate, updateDate);
                })
                .collect(Collectors.toSet());
    }

    public void delete(String sourceCurrency, String destCurrency) {
        deleteItem(eq(SOURCE_FIELD, sourceCurrency), eq(DEST_FIELD, destCurrency));
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

}
