package com.dgphoenix.casino.cassandra.persist;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import com.dgphoenix.casino.cassandra.IEntityUpdateListener;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.common.cache.AbstractDistributedCache;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bank.Coin;
import com.dgphoenix.casino.common.cache.data.bank.Limit;
import com.dgphoenix.casino.common.cache.data.currency.ICurrency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfo;
import com.dgphoenix.casino.common.cache.data.game.GameVariableType;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.persist.StreamPersister;
import com.dgphoenix.casino.common.persist.TableProcessor;
import com.dgphoenix.casino.common.util.Pair;
import com.google.common.collect.ImmutableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

/**
 * User: flsh
 * Date: 4/11/12
 */
public class CassandraBaseGameInfoPersister extends AbstractStringDistributedConfigEntryPersister<BaseGameInfo>
        implements ICassandraBaseGameInfoPersister<BaseGameInfo>, StreamPersister<String, BaseGameInfo> {
    private static final Logger LOG = LogManager.getLogger(CassandraBaseGameInfoPersister.class);

    public static final String BASE_GAME_INFO_CF = "GameInfoCF";

    private static final String BANK_IDX = "BankIdx";
    private static final String BANK_AND_CUR_IDX = "BankAndCurIdx";

    private static final TableDefinition TABLE = new TableDefinition(
            BASE_GAME_INFO_CF,
            Arrays.asList(
                    new ColumnDefinition(KEY, DataType.text(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, DataType.text()),
                    new ColumnDefinition(BANK_IDX, DataType.text(), false, true, false),
                    new ColumnDefinition(BANK_AND_CUR_IDX, DataType.text(), false, true, false)
            ),
            KEY
    );

    private final List<IEntityUpdateListener<String, BaseGameInfo>> baseGameInfoUpdateListeners = new CopyOnWriteArrayList<>();

    private CassandraBaseGameInfoPersister() {
        super();
    }

    protected String getKey(BaseGameInfo entry) {
        return BaseGameCache.getInstance().composeGameKey(entry);
    }

    //with notify other server use RemoteCallHelper.getInstance().saveAndSendNotification(gameInfo);
    public void save(BaseGameInfo gameInfo) {
        persist(gameInfo);
    }

    @Override
    public void saveAll() {
        Map<String, IBaseGameInfo> gameInfoMap = BaseGameCache.getInstance().getAllObjects();
        for (Map.Entry<String, IBaseGameInfo> entry : gameInfoMap.entrySet()) {
            IBaseGameInfo gameInfo = entry.getValue();
            if (gameInfo instanceof BaseGameInfo) {
                persist(entry.getKey(), (BaseGameInfo) gameInfo);
            } else {
                LOG.warn("saveAll: cannot save: " + gameInfo);
            }
        }
    }

    @Override
    public Set<String> getKeys() {
        com.datastax.driver.core.Statement select = getSelectColumnsQuery(KEY);
        Iterator<com.datastax.driver.core.Row> iterator = execute(select, "getKeys").iterator();
        Set<String> result = new HashSet<>();
        while (iterator.hasNext()) {
            com.datastax.driver.core.Row row = iterator.next();
            result.add(row.getString(KEY));
        }
        return ImmutableSet.copyOf(result);
    }

    @Override
    public List<BaseGameInfo> getByBank(long bankId) {
        com.datastax.driver.core.Statement select = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(BANK_IDX, getBankIdx(bankId)));
        Iterator<com.datastax.driver.core.Row> iterator = execute(select, "getByBank", ConsistencyLevel.LOCAL_ONE).iterator();
        List<BaseGameInfo> result = new ArrayList<>();
        while (iterator.hasNext()) {
            com.datastax.driver.core.Row row = iterator.next();
            result.add(deserialize().apply(row));
        }
        return result;
    }

    @Override
    public List<BaseGameInfo> getByBankAndCurrency(long bankId, ICurrency currency) {
        com.datastax.driver.core.Statement select = getSelectColumnsQuery(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .where(eq(BANK_AND_CUR_IDX, getBankAndCurIdx(bankId, currency.getCode())));
        Iterator<com.datastax.driver.core.Row> iterator = execute(select, "getByBankAndCurrency", ConsistencyLevel.LOCAL_ONE).iterator();
        List<BaseGameInfo> result = new ArrayList<>();
        while (iterator.hasNext()) {
            com.datastax.driver.core.Row row = iterator.next();
            result.add(deserialize().apply(row));
        }
        return result;
    }

    protected Function<com.datastax.driver.core.Row, BaseGameInfo> deserialize() {
        return row -> {
            BaseGameInfo bgi = TABLE.deserializeFromJson(
                    Objects.requireNonNull(row).getString(JSON_COLUMN_NAME), BaseGameInfo.class);
            if (bgi == null) {
                bgi = TABLE.deserializeFrom(
                        Objects.requireNonNull(row).getBytes(SERIALIZED_COLUMN_NAME),
                        BaseGameInfo.class);
            }
            return bgi;
        };
    }

    @Override
    public void persist(BaseGameInfo gameInfo) {
        String key = getKey(gameInfo);
        persist(key, gameInfo);
    }

    @Override
    public void persist(String key, BaseGameInfo gameInfo) {
        LOG.debug("Save: {}", gameInfo);
        BaseGameInfo copy = gameInfo.copy();
        long bankId = copy.getBankId();
        BaseGameInfo defaultGameInfo = BaseGameInfoTemplateCache.getInstance().getDefaultGameInfo(gameInfo.getId());
        if (defaultGameInfo != null) {
            Map<String, String> map = defaultGameInfo.getPropertiesMap();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                //LOG.debug("CassandraPersister:: property: " + copy.getProperty(entry.getKey()));
                String copyProperty = copy.getProperty(entry.getKey());
                if (copyProperty != null && copyProperty.equals(entry.getValue())) {
                    //LOG.debug("CassandraPersister::" + "removed: " + copy.getProperty(entry.getKey()));
                    copy.removeProperty(entry.getKey());
                }
            }
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (copy.getVariableType() == GameVariableType.LIMIT) {
                Limit limit = copy.getLimit();
                Limit bankLimit = bankInfo.getLimit();
                if (limit != null && bankLimit != null && limit.equals(bankLimit)) {
                    copy.setLimit(null);
                }
            } else if (copy.getVariableType() == GameVariableType.COIN) {
                List<Coin> coins = copy.getCoins();
                List<Coin> bankCoins = bankInfo.getCoins();
                if (coins != null && bankCoins != null && coins.equals(bankCoins)) {
                    copy.setCoins(null);
                }
            }
        }

        String json = TABLE.serializeToJson(copy);
        ByteBuffer byteBuffer = TABLE.serializeToBytes(copy);
        try {
            com.datastax.driver.core.Statement insert = getInsertQuery()
                    .value(KEY, key)
                    .value(JSON_COLUMN_NAME, json)
                    .value(SERIALIZED_COLUMN_NAME, byteBuffer)
                    .value(BANK_IDX, getBankIdx(gameInfo.getBankId()))
                    .value(BANK_AND_CUR_IDX, getBankAndCurIdx(gameInfo.getBankId(), gameInfo.getCurrency().getCode()));
            execute(insert, "persist");
        } finally {
            releaseBuffer(byteBuffer);
        }

        notifyBaseGameInfoUpdateListeners(key, copy);
    }

    @Override
    public void refresh(String id) {
        super.refresh(id);
        BaseGameInfo newValue = get(id);
        notifyBaseGameInfoUpdateListeners(id, newValue);
    }

    private void notifyBaseGameInfoUpdateListeners(String id, BaseGameInfo updatedGameInfo) {
        try {
            for (IEntityUpdateListener<String, BaseGameInfo> updateListener : baseGameInfoUpdateListeners) {
                updateListener.notify(id, updatedGameInfo);
            }
        } catch (Exception e) {
            LOG.debug("Can't notify BaseGameInfo update listeners", e);
        }
    }

    @Override
    public Map<String, BaseGameInfo> getAllAsMap() {
        Map<String, BaseGameInfo> result = new HashMap<>();
        Iterator<com.datastax.driver.core.Row> iterator = getAll();
        while (iterator.hasNext()) {
            com.datastax.driver.core.Row row = iterator.next();
            String key = row.getString(KEY);
            BaseGameInfo value = TABLE.deserializeFromJson(row.getString(JSON_COLUMN_NAME), BaseGameInfo.class);
            if (value == null) {
                value = TABLE.deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), BaseGameInfo.class);
            }
            result.put(key, value);
        }
        return result;
    }

    @Override
    public void processAll(TableProcessor<Pair<String, BaseGameInfo>> tableProcessor) throws IOException {
        Iterator<com.datastax.driver.core.Row> iterator = getAll();
        while (iterator.hasNext()) {
            com.datastax.driver.core.Row row = iterator.next();
            processRow(row, tableProcessor);
        }
    }

    @Override
    public void processByCondition(TableProcessor<Pair<String, BaseGameInfo>> tableProcessor, String conditionName,
                                   Object... conditionValues)
            throws IOException {
        if (conditionName.equals("byBank")) {
            Long bankId = (Long) conditionValues[0];
            com.datastax.driver.core.Statement select = getSelectColumnsQuery(KEY, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                    .where(eq(BANK_IDX, getBankIdx(bankId)));
            Iterator<com.datastax.driver.core.Row> iterator = execute(select, "getByBank").iterator();
            while (iterator.hasNext()) {
                com.datastax.driver.core.Row row = iterator.next();
                processRow(row, tableProcessor);
            }
        }
    }

    private void processRow(com.datastax.driver.core.Row row, TableProcessor<Pair<String, BaseGameInfo>> tableProcessor) throws IOException {
        String key = row.getString(KEY);
        BaseGameInfo value = deserialize().apply(row);
        tableProcessor.process(new Pair<>(key, value));
    }

    public void addBaseGameInfoUpdateListener(IEntityUpdateListener<String, BaseGameInfo> updateListener) {
        baseGameInfoUpdateListeners.add(updateListener);
    }

    private String getBankAndCurIdx(long bankId, String currencyCode) {
        return bankId + "_" + currencyCode;
    }

    private String getBankIdx(long bankId) {
        return String.valueOf(bankId);
    }

    @Override
    public BaseGameInfo get(String key) {
        return get(key, BaseGameInfo.class);
    }

    public boolean delete(String key) {
        boolean deleted = super.deleteWithCheck(key);
        notifyBaseGameInfoUpdateListeners(key, null);
        return deleted;
    }

    @Override
    public AbstractDistributedCache<BaseGameInfo> getCache() {
        return BaseGameCache.getInstance();
    }

    @Override
    public int loadAll() {
        return 0;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
