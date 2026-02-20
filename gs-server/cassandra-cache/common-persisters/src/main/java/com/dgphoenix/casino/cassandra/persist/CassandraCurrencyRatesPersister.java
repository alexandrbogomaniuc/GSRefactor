package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.currency.CurrencyRate;
import com.dgphoenix.casino.common.currency.ICurrencyRateChangedListener;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.StreamUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

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
                    new ColumnDefinition(SOURCE_FIELD, DataType.text(), false, false, true),
                    new ColumnDefinition(DEST_FIELD, DataType.text(), false, false, true),
                    new ColumnDefinition(RATE_FIELD, DataType.cdouble(), false, false, false),
                    new ColumnDefinition(UPDATE_DATE_FIELD, DataType.bigint(), false, false, false)
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
        Insert query = getInsertQuery()
                .value(SOURCE_FIELD, currencyRate.getSourceCurrency())
                .value(DEST_FIELD, currencyRate.getDestinationCurrency())
                .value(RATE_FIELD, currencyRate.getRate())
                .value(UPDATE_DATE_FIELD, currencyRate.getUpdateDate());
        execute(query, "create");
        Pair<String, String> pair = new Pair<>(currencyRate.getSourceCurrency(), currencyRate.getDestinationCurrency());
        for (ICurrencyRateChangedListener listener : changedListeners) {
            listener.notify(pair);
        }
    }

    public CurrencyRate getCurrencyRate(String source, String target) {
        Select select = getSelectColumnsQuery(RATE_FIELD, UPDATE_DATE_FIELD);
        select.where().and(QueryBuilder.eq(SOURCE_FIELD, source)).and(QueryBuilder.eq(DEST_FIELD, target));
        Row row = execute(select, "getRate").one();
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
        Select select = getSelectColumnsQuery(SOURCE_FIELD, DEST_FIELD, RATE_FIELD, UPDATE_DATE_FIELD);
        return StreamUtils.asStream(execute(select, "getRates"))
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
