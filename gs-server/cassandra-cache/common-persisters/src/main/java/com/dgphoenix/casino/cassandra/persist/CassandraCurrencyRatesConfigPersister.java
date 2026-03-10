package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.Row;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.StreamUtils;
import com.abs.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

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
                    new ColumnDefinition(CURRENCY_NAME, text(), false, false, true),
                    new ColumnDefinition(CURRENCY_FORMULA, text(), false, false, false),
                    new ColumnDefinition(CURRENCY_TARGET, text(), false, false, false),
                    new ColumnDefinition(UPDATE_PERIOD, bigint(), false, false, false) //ALTER TABLE CurrencyRatesConfigCF ADD UPDATE_PERIOD bigint;
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
        com.abs.casino.cassandra.persist.engine.Statement query = com.abs.casino.cassandra.persist.engine.Statement.of(getInsertQuery()
                .value(CURRENCY_NAME, currency)
                .value(CURRENCY_FORMULA, formula)
                .value(CURRENCY_TARGET, target));
        execute(query, "create");
    }

    public void persist(String currency, long updatePeriod) {
        com.abs.casino.cassandra.persist.engine.Statement query = com.abs.casino.cassandra.persist.engine.Statement.of(getInsertQuery()
                .value(CURRENCY_NAME, currency)
                .value(UPDATE_PERIOD, updatePeriod));
        execute(query, "create custom update period");
    }

    public Map<String, Pair<String, String>> getCalculatedCurrenciesConfig() {
        com.abs.casino.cassandra.persist.engine.Statement select = com.abs.casino.cassandra.persist.engine.Statement.of(getSelectColumnsQuery(CURRENCY_NAME, CURRENCY_FORMULA, CURRENCY_TARGET));
        return StreamUtils.asStream(executeWrapped(select, "getCalculatedCurrenciesConfig"))
                .filter(row -> !StringUtils.isTrimmedEmpty(row.getString(CURRENCY_FORMULA)))
                .collect(Collectors.toMap(
                        row -> row.getString(CURRENCY_NAME),
                        row -> new Pair<>(row.getString(CURRENCY_FORMULA), row.getString(CURRENCY_TARGET))
                ));
    }

    public Long getUpdatePeriod(String currency) {
        Row row = getAsWrappedRow(currency, UPDATE_PERIOD);
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
