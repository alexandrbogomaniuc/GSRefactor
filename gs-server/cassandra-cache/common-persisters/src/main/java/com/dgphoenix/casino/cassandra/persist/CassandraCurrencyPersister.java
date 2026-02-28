package com.dgphoenix.casino.cassandra.persist;

import com.abs.casino.common.cache.AbstractDistributedCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.persist.StreamPersister;
import com.abs.casino.common.persist.TableProcessor;
import com.dgphoenix.casino.common.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * User: flsh
 * Date: 4/11/12
 */
public class CassandraCurrencyPersister extends AbstractStringDistributedConfigEntryPersister<Currency>
        implements ICachePersister<String, Currency>, StreamPersister<String, Currency> {
    private static final String CURRENCY_CF = "CurrencyCF";
    private static final Logger LOG = LogManager.getLogger(CassandraCurrencyPersister.class);

    private CassandraCurrencyPersister() {
        super();
    }

    @Override
    public Currency get(String id) {
        return get(id, Currency.class);
    }

    @Override
    public void persist(Currency currency) {
        String key = currency.getCode();
        persist(key, currency);
    }

    public boolean delete(String s) {
        return super.deleteWithCheck(s);
    }

    @Override
    public AbstractDistributedCache getCache() {
        return CurrencyCache.getInstance();
    }

    @Override
    public int loadAll() {
        final Map<String, Currency> infos = loadAllAsMap(Currency.class);
        if (infos == null) {
            return 0;
        }
        int count = 0;
        for (Currency info : infos.values()) {
            put(info);
            count++;
        }
        LOG.info("loadAll: count=" + count + ", CurrencyCache.getInstance().size()=" +
                CurrencyCache.getInstance().size());
        return CurrencyCache.getInstance().size();
    }

    @Override
    public String getMainColumnFamilyName() {
        return CURRENCY_CF;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public void processAll(TableProcessor<Pair<String, Currency>> tableProcessor) throws IOException {
        Iterator<com.datastax.driver.core.Row> iterator = getAll();
        while (iterator.hasNext()) {
            com.datastax.driver.core.Row row = iterator.next();
            processRow(row, tableProcessor);
        }
    }

    private void processRow(com.datastax.driver.core.Row row, TableProcessor<Pair<String, Currency>> tableProcessor) throws IOException {
        String key = row.getString(KEY);
        Currency value = _getTableDefinition().deserializeFromJson(row.getString(JSON_COLUMN_NAME), Currency.class);

        if (value == null) {
            value = _getTableDefinition().deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), Currency.class);
        }
        if (value != null) {
            tableProcessor.process(new Pair<>(key, value));
        }
    }

    @Override
    public void processByCondition(TableProcessor<Pair<String, Currency>> tableProcessor, String conditionName, Object... conditionValues) throws IOException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
