package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;
import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.StreamUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User: flsh
 * Date: 22.03.13
 */
public class CassandraCurrencyRatesConfigPersister extends AbstractCassandraPersister<String, String> {
    private static final Logger LOG = LogManager.getLogger(CassandraCurrencyRatesConfigPersister.class);

    private static final String COLUMN_FAMILY = "CurrencyRatesConfigCF";
    private static final String CURRENCY_NAME = "CURRENCY_NAME";
    private static final String CURRENCY_FORMULA = "CURRENCY_PARAM";
    private static final String CURRENCY_TARGET = "CURRENCY_TARGET";
    private static final String UPDATE_PERIOD = "UPDATE_PERIOD";

    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY,
            Arrays.asList(
                    new ColumnDefinition(CURRENCY_NAME, DataType.text(), false, false, true),
                    new ColumnDefinition(CURRENCY_FORMULA, DataType.text(), false, false, false),
                    new ColumnDefinition(CURRENCY_TARGET, DataType.text(), false, false, false),
                    new ColumnDefinition(UPDATE_PERIOD, DataType.bigint(), false, false, false) //ALTER TABLE CurrencyRatesConfigCF ADD UPDATE_PERIOD bigint;
            ), CURRENCY_NAME);

    private CassandraCurrencyRatesConfigPersister() {
        super();
    }

    @Override
    protected String getKeyColumnName() {
        return CURRENCY_NAME;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    public void persist(String currency, String target, String formula) {
        com.datastax.driver.core.Statement query = getInsertQuery()
                .value(CURRENCY_NAME, currency)
                .value(CURRENCY_FORMULA, formula)
                .value(CURRENCY_TARGET, target);
        execute(query, "create");
    }

    public void persist(String currency, long updatePeriod) {
        com.datastax.driver.core.Statement query = getInsertQuery()
                .value(CURRENCY_NAME, currency)
                .value(UPDATE_PERIOD, updatePeriod);
        execute(query, "create custom update period");
    }

    public Map<String, Pair<String, String>> getCalculatedCurrenciesConfig() {
        com.datastax.driver.core.Statement select = getSelectColumnsQuery(CURRENCY_NAME, CURRENCY_FORMULA, CURRENCY_TARGET);
        return StreamUtils.asStream(execute(select, "getCalculatedCurrenciesConfig"))
                .filter(row -> !StringUtils.isTrimmedEmpty(row.getString(CURRENCY_FORMULA)))
                .collect(Collectors.toMap(
                        row -> row.getString(CURRENCY_NAME),
                        row -> new Pair<>(row.getString(CURRENCY_FORMULA), row.getString(CURRENCY_TARGET))
                ));
    }

    public Long getUpdatePeriod(String currency) {
        Row row = getAsRow(currency, UPDATE_PERIOD);
        return row != null && !row.isNull(UPDATE_PERIOD) ? row.getLong(UPDATE_PERIOD) : null;
    }

    public void delete(String currencyName) {
        deleteItem(eq(CURRENCY_NAME, currencyName));
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

}
