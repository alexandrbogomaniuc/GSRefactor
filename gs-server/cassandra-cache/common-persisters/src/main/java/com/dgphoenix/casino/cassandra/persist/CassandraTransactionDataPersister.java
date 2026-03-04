package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.abs.casino.common.cache.CacheKeyInfo;
import com.abs.casino.common.cache.IDistributedCache;
import com.abs.casino.common.cache.LoadBalancerCache;
import com.abs.casino.common.cache.data.account.AccountInfo;
import com.abs.casino.common.cache.data.session.GameSession;
import com.abs.casino.common.cache.data.session.SessionInfo;
import com.abs.casino.common.lock.ChangeLockListener;
import com.abs.casino.common.lock.LockingInfo;
import com.abs.casino.common.lock.ServerLockInfo;
import com.abs.casino.common.transactiondata.ITransactionData;
import com.abs.casino.common.transactiondata.ITransactionDataProcessor;
import com.abs.casino.common.transactiondata.OnlineSessionInfo;
import com.abs.casino.common.transactiondata.TrackingInfo;
import com.abs.casino.common.transactiondata.TrackingState;
import com.abs.casino.common.transactiondata.TrackingStatus;
import com.abs.casino.common.transactiondata.TransactionData;
import com.abs.casino.common.transactiondata.TransactionDataFactory;
import com.abs.casino.common.transactiondata.TransactionDataInvalidatedListener;
import com.abs.casino.common.transactiondata.ITransactionDataPersister;
import com.abs.casino.common.transactiondata.storeddate.StoredItem;
import com.abs.casino.common.transactiondata.storeddate.StoredItemType;
import com.abs.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.NtpTimeProvider;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.string.StringIdGenerator;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.statistics.StatisticsManager;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.blob;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;

/**
 * User: flsh
 * Date: 7/4/12
 */
@SuppressWarnings("rawtypes")
@CacheKeyInfo(description = "transactionData.accountId")
public class CassandraTransactionDataPersister extends AbstractCassandraPersister<String, String>
        implements ITransactionDataPersister, ChangeLockListener, IDistributedCache<String, ITransactionData> {
    public static final String TRANSACTION_DATA_CF = "trdata_cf";
    public static final String ACCOUNT_FIELD = "a";
    public static final String PLAYER_SESSION_FIELD = "ps";
    public static final String GAME_SESSION_FIELD = "gs";
    public static final String LAST_HAND_FIELD = "lh";
    public static final String WALLET_FIELD = "w";
    public static final String PAYMENT_TRANSACTION_FIELD = "pt";
    public static final String BONUS_FIELD = "bonus";
    public static final String FRBONUS_FIELD = "frbonus";
    public static final String FRBWIN_FIELD = "frbwin";
    public static final String FRBNOTIFY_FIELD = "frbn";
    public static final String PROMO_MEMBERS_FIELD = "pmembers";
    public static final String LAST_BET_FIELD = "lb";
    public static final String LAST_UPDATE_ID_FIELD = "lui";
    public static final String LAST_WRITE_TIME_ALIAS = "wtlui";
    public static final String TRACKING_INFO = "track";
    //FB only fields
    public static final String EXTRA_ACCOUNT_INFO = "ssea";
    public static final String GAME_ACHIEVEMENT = "ssga";
    public static final String PLAYER_ACTIVITY = "sspa";

    public static final String TRANSACTION_TRACKING_CF = "trdata_tracking_cf";
    public static final String SERVER_ID = "si";
    public static final String TRACKING_STATUS = "ts";
    private static final com.datastax.driver.core.ProtocolVersion SERIALIZE_PROTOCOL_VERSION = com.datastax.driver.core.ProtocolVersion.NEWEST_SUPPORTED;
    protected static final Cache<String, ITransactionData> cached = CacheBuilder.
            newBuilder().
            initialCapacity(100).
            maximumSize(5000).
            expireAfterAccess(10, TimeUnit.MINUTES).
            recordStats().
            concurrencyLevel(8).
            build();
    private static final Logger LOG = LogManager.getLogger(CassandraTransactionDataPersister.class);
    private static final TableDefinition TABLE = new TableDefinition(TRANSACTION_DATA_CF,
            Arrays.asList(
                    new ColumnDefinition(KEY, text(), false, false, true),
                    new ColumnDefinition(ACCOUNT_FIELD, blob()),
                    new ColumnDefinition(PLAYER_SESSION_FIELD, blob()),
                    new ColumnDefinition(GAME_SESSION_FIELD, blob()),
                    new ColumnDefinition(LAST_HAND_FIELD, blob()),
                    new ColumnDefinition(WALLET_FIELD, blob()),
                    new ColumnDefinition(LAST_BET_FIELD, blob()),
                    new ColumnDefinition(BONUS_FIELD, blob()),
                    new ColumnDefinition(FRBONUS_FIELD, blob()),
                    new ColumnDefinition(FRBWIN_FIELD, blob()),
                    new ColumnDefinition(FRBNOTIFY_FIELD, blob()),
                    new ColumnDefinition(LAST_UPDATE_ID_FIELD, text()),
                    new ColumnDefinition(VERSION_FIELD, bigint()),
                    new ColumnDefinition(TRACKING_INFO, text()),
                    new ColumnDefinition(EXTRA_ACCOUNT_INFO, blob()),
                    new ColumnDefinition(GAME_ACHIEVEMENT, blob()),
                    new ColumnDefinition(PLAYER_ACTIVITY, blob()),
                    new ColumnDefinition(PAYMENT_TRANSACTION_FIELD, blob()),
                    new ColumnDefinition(PROMO_MEMBERS_FIELD, blob())
            ), KEY)
            .compaction(CompactionStrategy.getLeveled(true, TimeUnit.HOURS.toSeconds(1)))
            .gcGraceSeconds(TimeUnit.HOURS.toSeconds(4));

    private static final TableDefinition TRACKING_TABLE = new TableDefinition(TRANSACTION_TRACKING_CF,
            Arrays.asList(
                    new ColumnDefinition(TRACKING_STATUS, cint(), false, false, true),
                    new ColumnDefinition(SERVER_ID, cint(), false, false, true),
                    new ColumnDefinition(KEY, text(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, text())
            ), TRACKING_STATUS)
            .compaction(CompactionStrategy.getLeveled(true, TimeUnit.HOURS.toSeconds(1)))
            .gcGraceSeconds(TimeUnit.HOURS.toSeconds(4));


    private final EnumMap<StoredItemType, IStoredDataProcessor> storedDataProcessors = new EnumMap<>(StoredItemType.class);
    private final List<TransactionDataInvalidatedListener> invalidatedListeners = new ArrayList<>();

    private CassandraAccountInfoPersister accountInfoPersister;

    static {
        StatisticsManager.getInstance()
                .registerStatisticsGetter("CassandraTransactionDataPersister cache size",
                        () -> "size=" + cached.size() + ", stats=" + cached.stats());
    }

    private int gameServerId = -1;

    private CassandraTransactionDataPersister() {
        super();
    }

    @SuppressWarnings("unused")
    private void setAccountInfoPersister(CassandraAccountInfoPersister accountInfoPersister) {
        this.accountInfoPersister = accountInfoPersister;
    }

    public void registerStoredDataProcessors(StoredItemType type, IStoredDataProcessor processor) {
        storedDataProcessors.put(type, processor);
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public List<TableDefinition> getAllTableDefinitions() {
        return Arrays.asList(TABLE, TRACKING_TABLE);
    }

    @Override
    public int getGameServerId() {
        return gameServerId;
    }

    @Override
    public void setGameServerId(int gameServerId) {
        if (gameServerId < 0) {
            throw new RuntimeException("gameServerId must be positive");
        }
        if (this.gameServerId == -1) {
            this.gameServerId = gameServerId;
        } else {
            throw new RuntimeException("gameServer value cannot be changed, current=" + this.gameServerId +
                    ", new =" + gameServerId);
        }
    }

    public void registerInvalidationListener(TransactionDataInvalidatedListener listener) {
        invalidatedListeners.add(listener);
    }

    @Override
    public List<OnlineSessionInfo> getOnlineSessionInfos(Integer gameServerId, Integer bankId,
                                                         boolean withEmptyGameSession) {
        Map<String, TrackingState> map = getByTrackingStatusAndGameServer(TrackingStatus.ONLINE, gameServerId);
        Set<String> keys = map.keySet();
        List<String> listKeys = new ArrayList<>(keys);
        if (bankId != null) {//by bankId possible make filter only for keys, because key cointain bankId
            Iterator<String> keysIterator = listKeys.iterator();
            while (keysIterator.hasNext()) {
                String key = keysIterator.next();
                Pair<Integer, String> pair = null;
                try {
                    pair = StringIdGenerator.extractBankAndExternalUserIdFromUserHash(key);
                } catch (Throwable t) {
                    LOG.error("Can't parse key={}", key, t);
                }
                if (pair == null || !pair.getKey().equals(bankId)) {
                    keysIterator.remove();
                }
            }
        }

        int stepSize = 1000;
        int size = listKeys.size();
        int fromIndex, toIndex = 0;
        List<OnlineSessionInfo> result = new ArrayList<>(size);
        do {
            fromIndex = toIndex;
            toIndex = fromIndex + stepSize;
            if (toIndex > size) {
                toIndex = size;
            }
            List<String> stepKeys = listKeys.subList(fromIndex, toIndex);
            String[] columns;
            {
                short c = 5;
                if (gameServerId == null) {
                    c--;
                }
                if (bankId != null) {
                    c--;
                }
                columns = new String[c];
                c = 0;
                columns[c++] = ACCOUNT_FIELD;
                columns[c++] = PLAYER_SESSION_FIELD;
                columns[c++] = GAME_SESSION_FIELD;
                if (gameServerId != null) {
                    columns[c++] = TRACKING_INFO;
                }
                if (bankId == null) {
                    columns[c++] = KEY;
                }
            }

            Iterator<com.datastax.driver.core.Row> iterator = execute(
                    getSelectColumnsQuery(columns).where(com.datastax.driver.core.querybuilder.QueryBuilder.in(KEY, stepKeys)),
                    "getOnlineSessionInfos").iterator();
            if (!iterator.hasNext()) {
                continue;
            }
            while (iterator.hasNext()) {
                com.datastax.driver.core.Row row = iterator.next();
                Integer curBankId;
                if (bankId != null) {
                    curBankId = bankId;
                } else {
                    String key = row.getString(KEY);
                    try {
                        curBankId = StringIdGenerator.extractBankAndExternalUserIdFromUserHash(key).getKey();
                    } catch (Throwable t) {
                        LOG.error("Can't parse key=" + key, t);
                        continue;
                    }
                }

                if (gameServerId != null) {
                    String lastTrackingKey = deserializeField(curBankId, row.getBytesUnsafe(TRACKING_INFO),
                            TRACKING_INFO);
                    TrackingState state = TransactionData.getTrackingStateFromString(lastTrackingKey);
                    if (state == null || !gameServerId.equals(state.getGameServerId())) {
                        continue;
                    }
                }

                SessionInfo sessionInfo = deserializeField(curBankId, row.getBytesUnsafe(PLAYER_SESSION_FIELD),
                        PLAYER_SESSION_FIELD);
                if (sessionInfo == null) {
                    continue;
                }
                GameSession gameSession = deserializeField(curBankId, row.getBytesUnsafe(GAME_SESSION_FIELD),
                        GAME_SESSION_FIELD);
                if (gameSession == null && !withEmptyGameSession) {
                    continue;
                }
                AccountInfo accountInfo = deserializeField(curBankId, row.getBytesUnsafe(ACCOUNT_FIELD), ACCOUNT_FIELD);
                result.add(new OnlineSessionInfo(accountInfo, sessionInfo, gameSession));
            }
        } while (toIndex < size);
        return result;
    }

    @Override
    public void persistPlayerBet(ITransactionData data) {
        StoredItemType type = StoredItemType.PLAYER_BET;
        if (data.getAtomicallyStoredData() == null || data.getAtomicallyStoredData().isEmpty()
                || data.getAtomicallyStoredData().get(type) == null) {
            return;
        }

        StoredItem playerBet = data.getAtomicallyStoredData().get(type);
        LOG.debug("persistPlayerBet: {}", playerBet);

        Pair<Pair<ByteBuffer, com.datastax.driver.core.Statement>, com.datastax.driver.core.Statement> trackingInfoChanges = null;
        try {
            trackingInfoChanges = updateTrackingInfo(data);
            Pair<ByteBuffer, com.datastax.driver.core.Statement> before = trackingInfoChanges.getKey();
            if (before != null) {
                com.datastax.driver.core.ResultSet result = execute(before.getValue(), "persistPlayerBet before");
                if (!result.wasApplied()) {
                    LOG.error("persistJackpotWin before statement ({}) return not success result", before);
                    assertAppliedByVersion(result, data.getVersion());
                }
            }
        } finally {
            if (trackingInfoChanges != null && trackingInfoChanges.getKey() != null && trackingInfoChanges.getKey().getKey() != null) {
                TRACKING_TABLE.releaseBuffer(trackingInfoChanges.getKey().getKey());
            }
        }

        com.datastax.driver.core.Statement after = trackingInfoChanges.getValue();

        Map<StoredItemType, StoredItem> storedItems = data.getAtomicallyStoredData();
        if (CollectionUtils.isEmpty(storedItems)) {
            LOG.warn("persistPlayerBet: storedItems is empty for lockId={}", data.getLockId());
        }

        HashMap<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap = new HashMap<>();
        List<ByteBuffer> byteBuffers = new LinkedList<>();

        try {
            IStoredDataProcessor processor = storedDataProcessors.get(type);
            if (processor != null) {
                processor.process(playerBet, statementsMap, byteBuffers);
            } else {
                LOG.error("persistPlayerBet Unknown processor for type={}", type);
            }
            execute(statementsMap, "persistPlayerBet").getKey();
        } finally {
            if (storedItems != null) {
                storedItems.remove(type);
            }
            for (ByteBuffer byteBuffer : byteBuffers) {
                releaseBuffer(byteBuffer);
            }
        }

        if (after != null) {
            com.datastax.driver.core.ResultSet result = execute(after, "persistPlayerBet after");
            if (!result.wasApplied()) {
                LOG.error("persistJackpotWin after statement ({}) return not success result", after);
                assertAppliedByVersion(result, data.getVersion());
            }
        }

    }

    protected Map<String, TrackingState> getByTrackingStatusAndGameServer(TrackingStatus trackingStatus,
                                                                          Integer gameServerId) {
        Iterator<Pair<String, Pair<TrackingState, TrackingInfo>>> iterator =
                getTrackingInfo(trackingStatus, gameServerId).iterator();
        Map<String, TrackingState> map = new HashMap<>();
        while (iterator.hasNext()) {
            Pair<String, Pair<TrackingState, TrackingInfo>> pair = iterator.next();
            map.put(pair.getKey(), pair.getValue().getKey());
        }
        return map;
    }

    public Iterable<Pair<String, Pair<TrackingState, TrackingInfo>>> getTrackingInfo(TrackingStatus trackingStatus,
                                                                                     Integer gameServerId) {
        com.datastax.driver.core.querybuilder.Select select = getSelectColumnsQuery(TRACKING_TABLE, SERVER_ID, TRACKING_STATUS, KEY, SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
        select.where(eq(TRACKING_STATUS, trackingStatus.ordinal()));
        if (gameServerId != null) {
            select.where(eq(SERVER_ID, gameServerId));
        }
        final TrackingStatus[] trackingStatuses = TrackingStatus.values();
        return executeAndGetAsIterableSkipNull(select, "getByTrackingStatusAndGameServer",
                row -> {
                    int aServerId = row.getInt(SERVER_ID);
                    int aTrackingStatus = row.getInt(TRACKING_STATUS);
                    String lockId = row.getString(KEY);
                    String json = row.getString(JSON_COLUMN_NAME);
                    TrackingInfo info = TRACKING_TABLE.deserializeFromJson(json, TrackingInfo.class);

                    if (info == null) {
                        ByteBuffer value = row.getBytes(SERIALIZED_COLUMN_NAME);//onlyAsMarker
                        info = TRACKING_TABLE.deserializeFrom(value, TrackingInfo.class);
                    }
                    if (info == null) {
                        return null;
                    }
                    return new Pair<>(lockId, new Pair<>(
                            new TrackingState(aServerId, trackingStatuses[aTrackingStatus]), info));
                });
    }

    public boolean limitOfTrackingInfoByStatusIsExceededForAllGameServers(TrackingStatus status, int limit) {
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().countAll().from(TRANSACTION_TRACKING_CF).
                where().and(eq(TRACKING_STATUS, status.ordinal())).limit(limit + 1);

        com.datastax.driver.core.ResultSet resultSet = execute(query, "getAllTrackInfoCountByStatus");
        com.datastax.driver.core.Row row = resultSet.one();

        long count = row.getLong("count");
        return count > limit;
    }

    public boolean limitOfTrackingInfoByStatusIsExceededForGameServer(TrackingStatus status, Integer serverId, int limit) {
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().countAll().from(TRANSACTION_TRACKING_CF).
                where().and(eq(TRACKING_STATUS, status.ordinal())).and(eq(SERVER_ID, serverId)).limit(limit + 1);

        com.datastax.driver.core.ResultSet resultSet = execute(query, "getAllTrackInfoCountByStatusAndGameServer");
        com.datastax.driver.core.Row row = resultSet.one();

        long count = row.getLong("count");
        return count > limit;
    }


    public void processTransactions(TrackingStatus trackingStatus, Integer gameServerId,
                                    ITransactionDataProcessor processor) {
        long now = System.currentTimeMillis();
        Iterator<Pair<String, Pair<TrackingState, TrackingInfo>>> iterator =
                getTrackingInfo(trackingStatus, gameServerId).iterator();
        while (iterator.hasNext()) {
            if (!processor.isStopProcessing()) {
                Pair<String, Pair<TrackingState, TrackingInfo>> pair = iterator.next();
                String lockId = pair.getKey();
                TrackingState state = pair.getValue().getKey();
                TrackingInfo trackingInfo = pair.getValue().getValue();
                ITransactionData data = cached.getIfPresent(lockId);
                processor.process(lockId, state, trackingInfo, data);
            } else {
                LOG.info("processTransactions: processing stopped for status={}, gameServer={}",
                        trackingStatus, gameServerId);
                break;
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                " processTransactions", System.currentTimeMillis() - now);
    }

    public void processTransactions(Integer gameServerId, ITransactionDataProcessor processor) {
        long now = System.currentTimeMillis();
        com.datastax.driver.core.querybuilder.Select select = getSelectColumnsQuery(KEY);
        if (gameServerId != null) {
            select.where(eq(SERVER_ID, gameServerId));
        }
        Iterable<String> result = executeAndGetAsIterableSkipNull(
                select,
                "processTransactions",
                row -> {
                    String lockId = row.getString(KEY);
                    if (StringUtils.isTrimmedEmpty(lockId)) {
                        return null;
                    }
                    return lockId;
                }
        );
        for (String lockId : result) {
            if (!processor.isStopProcessing()) {
                ITransactionData data = cached.getIfPresent(lockId);
                processor.process(lockId, null, null, data);
            } else {
                LOG.info("processTransactions: processing stopped for lockId={}, gameServer={}", lockId, gameServerId);
                break;
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                " processTransactions", System.currentTimeMillis() - now);
    }

    private long getTimeStamp(ITransactionData data) {
        long currentTime = NtpTimeProvider.getInstance().getTimeMicroseconds();
        if (currentTime > data.getWriteTime()) {
            return currentTime;
        } else {
            LOG.warn("Found possible lost write, need correction, currentTime={}, td.writeTime={}, key={}",
                    currentTime, data.getWriteTime(), data.getLockId());
            return data.getWriteTime() + 1;
        }
    }

    /**
     * When game played in guest mode all transaction data saved only in local cache
     * In others modes transaction data will be saved in cassandra
     */
    @Override
    public void persist(ITransactionData data) {
        long now = System.currentTimeMillis();
        String lockId = data.getLockId();
        Pair<Pair<ByteBuffer, com.datastax.driver.core.Statement>, com.datastax.driver.core.Statement> trackingInfoChanges = null;
        Map<String, ByteBuffer> savedData = null;
        try {
            trackingInfoChanges = updateTrackingInfo(data);
            long time = NtpTimeProvider.getInstance().getTimeMicroseconds();
            data.setLastUpdateInfo(gameServerId + "_" + Thread.currentThread().getName() + "_" + time);
            savedData = TransactionDataFactory.getInstance().getStoredData(data);
            long timeStamp = getTimeStamp(data);
            com.datastax.driver.core.querybuilder.Insert insert = getInsertQuery(null).value(getKeyColumnName(), lockId);
            for (Map.Entry<String, ByteBuffer> e : savedData.entrySet()) {
                insert.value(e.getKey(), e.getValue());
            }
            Pair<ByteBuffer, com.datastax.driver.core.Statement> before = trackingInfoChanges.getKey();
            com.datastax.driver.core.Statement after = trackingInfoChanges.getValue();
            boolean success = processInTransactionItems(data, insert, before, after, "persist");
            if (!success) {
                cached.invalidate(lockId);
                ITransactionData changed = getFromDB(lockId);
                LOG.error("Concurrent modification: lockId={}, saved={}, changed={}", lockId, data, changed);
                throw new ConcurrentModificationException("lockId: " + lockId);
            } else {
                data.setWriteTime(timeStamp);
            }
        } finally {
            if (savedData != null) {
                for (ByteBuffer byteBuffer : savedData.values()) {
                    releaseBuffer(byteBuffer);
                }
            }
            if (trackingInfoChanges != null && trackingInfoChanges.getKey() != null &&
                    trackingInfoChanges.getKey().getKey() != null) {
                TRACKING_TABLE.releaseBuffer(trackingInfoChanges.getKey().getKey());
            }
        }

        data.incrementVersion();

        cached.put(lockId, data);
        if (LOG.isDebugEnabled()) {
            LOG.debug("persist(" + lockId + "): " + data);
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " persist",
                System.currentTimeMillis() - now);
    }

    protected boolean processInTransactionItems(ITransactionData data,
                                                com.datastax.driver.core.RegularStatement changeTransactionData,
                                                Pair<ByteBuffer, com.datastax.driver.core.Statement> before,
                                                com.datastax.driver.core.Statement after,
                                                String callerClassMethodIdentification) {
        Map<StoredItemType, StoredItem> storedItems = data.getAtomicallyStoredData();
        if (CollectionUtils.isEmpty(storedItems)) {
            LOG.warn("processInTransactionItems: storedItems is empty for lockId={}", data.getLockId());
        }
        HashMap<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap = new HashMap<>();
        List<ByteBuffer> byteBuffers = new LinkedList<>();
        try {
            boolean success = true;
            if (!CollectionUtils.isEmpty(storedItems)) {
                List<StoredItemType> types = new ArrayList<>(storedItems.keySet());
                Collections.sort(types);
                for (StoredItemType type : types) {
                    StoredItem item = storedItems.get(type);
                    if (item != null) {
                        IStoredDataProcessor processor = storedDataProcessors.get(type);
                        if (processor != null) {
                            processor.process(item, statementsMap, byteBuffers);
                        } else {
                            LOG.error("Unknown processor for type={}", type);
                        }
                    }
                }
                success = execute(statementsMap, callerClassMethodIdentification + " internal").getKey();
            }
            if (success) {
                if (before != null) {
                    success = execute(before.getValue(), callerClassMethodIdentification).wasApplied();
                    if (!success) {
                        LOG.error("before statement ({}) return not success result", before);
                        return false;
                    }
                }
                com.datastax.driver.core.ResultSet resultSet = execute(changeTransactionData, callerClassMethodIdentification);
                if (resultSet.wasApplied()) {
                    if (after != null) {
                        success = execute(after, callerClassMethodIdentification).wasApplied();
                        if (!success) {
                            LOG.error("after statement ({}) return not success result", after);
                            return false;
                        }
                    }
                    return true;
                }
            } else {
                LOG.error("Error on process atomic transaction items");
            }
            return false;
        } finally {
            if (storedItems != null) {
                storedItems.clear();
            }
            for (ByteBuffer byteBuffer : byteBuffers) {
                releaseBuffer(byteBuffer);
            }
        }

    }

    protected Pair<Boolean, Map<com.datastax.driver.core.Session, List<com.datastax.driver.core.ResultSet>>> execute(Map<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap,
                                                                   String callerClassMethodIdentification) {
        long now = System.currentTimeMillis();
        try {
            List<com.datastax.driver.core.Statement> strongConsistencyItems = statementsMap.remove(getSession());
            Map<com.datastax.driver.core.Session, List<com.datastax.driver.core.ResultSet>> resultSets = new HashMap<>(statementsMap.size());
            for (Map.Entry<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> entry : statementsMap.entrySet()) {
                Pair<Boolean, List<com.datastax.driver.core.ResultSet>> ksResult = executeSingleBatch(entry.getKey(), entry.getValue(),
                        callerClassMethodIdentification);
                resultSets.put(entry.getKey(), ksResult.getValue());
                if (!Boolean.TRUE.equals(ksResult.getKey())) {
                    return new Pair<>(false, resultSets);
                }
            }
            if (strongConsistencyItems != null) {
                Pair<Boolean, List<com.datastax.driver.core.ResultSet>> ksResult = executeSingleBatch(getSession(), strongConsistencyItems,
                        callerClassMethodIdentification);
                resultSets.put(getSession(), ksResult.getValue());
                if (!Boolean.TRUE.equals(ksResult.getKey())) {
                    return new Pair<>(false, resultSets);
                }
            }
            return new Pair<>(true, resultSets);
        } finally {
            StatisticsManager.getInstance().updateRequestStatistics(
                    getClass().getSimpleName() + " execute " + callerClassMethodIdentification,
                    System.currentTimeMillis() - now);
        }
    }

    private Pair<Boolean, List<com.datastax.driver.core.ResultSet>> executeSingleBatch(com.datastax.driver.core.Session session, List<com.datastax.driver.core.Statement> statements,
                                                              String callerClassMethodIdentification) {
        String keySpace = (com.abs.casino.cassandra.persist.engine.Session.class.isInstance(session) ?
                ((com.abs.casino.cassandra.persist.engine.Session) session).getKeySpace() :
                "unknown");
        List<com.datastax.driver.core.ResultSet> resultSetsByKs = new ArrayList<>(statements.size());
        for (com.datastax.driver.core.Statement statement : statements) {
            com.datastax.driver.core.ResultSet resultSet = execute(session, statement, callerClassMethodIdentification +
                    "::keySpace=" + keySpace);
            resultSetsByKs.add(resultSet);
            if (!resultSet.wasApplied()) {
                try {
                    getLog().error("com.datastax.driver.core.querybuilder.Batch was not applied. Keyspace={}; statement={}", keySpace, statement);
                } catch (Throwable t) {
                    getLog().error("execute: failed log cql", t);
                }
                return new Pair<>(false, resultSetsByKs);
            }
        }
        return new Pair<>(true, resultSetsByKs);
    }

    @Override
    public void persistBonus(ITransactionData data) {
        long now = System.currentTimeMillis();
        Pair<Pair<ByteBuffer, com.datastax.driver.core.Statement>, com.datastax.driver.core.Statement> trackingInfoChanges = null;
        try {
            trackingInfoChanges = updateTrackingInfo(data);
            Pair<ByteBuffer, com.datastax.driver.core.Statement> before = trackingInfoChanges.getKey();
            if (before != null) {
                com.datastax.driver.core.ResultSet result = execute(before.getValue(), "persistBonus before");
                if (!result.wasApplied()) {
                    LOG.error("persistBonus before statement ({}}) return not success result", before);
                    assertAppliedByVersion(result, data.getVersion());
                }
            }
        } finally {
            if (trackingInfoChanges != null && trackingInfoChanges.getKey() != null && trackingInfoChanges.getKey().getKey() != null) {
                TRACKING_TABLE.releaseBuffer(trackingInfoChanges.getKey().getKey());
            }
        }

        com.datastax.driver.core.Statement after = trackingInfoChanges.getValue();
        long clock = getTimeStamp(data);
        data.setLastUpdateInfo(gameServerId + "_" + Thread.currentThread().getName() + "_" + clock);
        com.datastax.driver.core.querybuilder.Update update = getUpdateQuery(data.getLockId());
        update.using(com.datastax.driver.core.querybuilder.QueryBuilder.timestamp(clock));
        ByteBuffer byteBuffer = TABLE.serializeToBytes(data.getBonus());
        ByteBuffer luBuffer = null;
        try {
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(BONUS_FIELD, byteBuffer));
            if (data.isTrackingStateChanged()) {
                update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(TRACKING_INFO, serializeField(data.getBankId(),
                        TRACKING_INFO, data.getTrackingStateAsString())));
            }
            luBuffer = com.datastax.driver.core.TypeCodec.ascii().serialize(data.getLastUpdateInfo(), SERIALIZE_PROTOCOL_VERSION);
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(LAST_UPDATE_ID_FIELD, luBuffer));
            //update.onlyIf(com.datastax.driver.core.querybuilder.QueryBuilder.eq(VERSION_FIELD, data.getVersion()));
            com.datastax.driver.core.ResultSet result = execute(update, "persistBonus");
/*
            if (LOG.isDebugEnabled()) {
                LOG.debug("persistBonus " + data.getBonus());
            }
*/
            assertAppliedByVersion(result, data.getVersion());
            data.setWriteTime(clock);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " persistBonus",
                    System.currentTimeMillis() - now);
        } finally {
            releaseBuffer(byteBuffer);
            if (luBuffer != null) {
                releaseBuffer(luBuffer);
            }
        }
        if (after != null) {
            com.datastax.driver.core.ResultSet result = execute(after, "persistBonus after");
            if (!result.wasApplied()) {
                LOG.error("persistBonus after statement ({}) return not success result", after);
                assertAppliedByVersion(result, data.getVersion());
            }
        }
    }

    @Override
    public void persistFrBonus(ITransactionData data) {
        long now = System.currentTimeMillis();
        Pair<Pair<ByteBuffer, com.datastax.driver.core.Statement>, com.datastax.driver.core.Statement> trackingInfoChanges = null;
        try {
            trackingInfoChanges = updateTrackingInfo(data);
            Pair<ByteBuffer, com.datastax.driver.core.Statement> before = trackingInfoChanges.getKey();
            if (before != null) {
                com.datastax.driver.core.ResultSet result = execute(before.getValue(), "persistFrBonus before");
                if (!result.wasApplied()) {
                    LOG.error("persistFrBonus before statement ({}) return not success result", before);
                    assertAppliedByVersion(result, data.getVersion());
                }
            }
        } finally {
            if (trackingInfoChanges != null && trackingInfoChanges.getKey() != null && trackingInfoChanges.getKey().getKey() != null) {
                TRACKING_TABLE.releaseBuffer(trackingInfoChanges.getKey().getKey());
            }
        }

        com.datastax.driver.core.Statement after = trackingInfoChanges.getValue();
        long clock = getTimeStamp(data);
        data.setLastUpdateInfo(gameServerId + "_" + Thread.currentThread().getName() + "_" + clock);

        com.datastax.driver.core.querybuilder.Update update = getUpdateQuery(data.getLockId());
        update.using(com.datastax.driver.core.querybuilder.QueryBuilder.timestamp(clock));
        ByteBuffer byteBuffer = TABLE.serializeToBytes(data.getFrBonus());
        ByteBuffer luBuffer = null;
        try {
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(FRBONUS_FIELD, byteBuffer));
            if (data.isTrackingStateChanged()) {
                update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(TRACKING_INFO, serializeField(data.getBankId(), TRACKING_INFO,
                        data.getTrackingStateAsString())));
            }
            luBuffer = com.datastax.driver.core.TypeCodec.ascii().serialize(data.getLastUpdateInfo(), SERIALIZE_PROTOCOL_VERSION);
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(LAST_UPDATE_ID_FIELD, luBuffer));
            com.datastax.driver.core.ResultSet result = execute(update, "persistFrBonus");
            if (LOG.isDebugEnabled()) {
                LOG.debug("persistFrBonus " + data.getFrBonus());
            }
            assertAppliedByVersion(result, data.getVersion());
            data.setWriteTime(clock);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " persistFrBonus",
                    System.currentTimeMillis() - now);
        } finally {
            releaseBuffer(byteBuffer);
            if (luBuffer != null) {
                releaseBuffer(luBuffer);
            }
        }
        if (after != null) {
            com.datastax.driver.core.ResultSet result = execute(after, "persistFrBonus after");
            if (!result.wasApplied()) {
                LOG.error("persistFrBonus after statement ({}) return not success result", after);
                assertAppliedByVersion(result, data.getVersion());
            }
        }
    }

    @Override
    public void persistFrbWin(ITransactionData data) {
        long now = System.currentTimeMillis();
        Pair<Pair<ByteBuffer, com.datastax.driver.core.Statement>, com.datastax.driver.core.Statement> trackingInfoChanges = null;
        try {
            trackingInfoChanges = updateTrackingInfo(data);
            Pair<ByteBuffer, com.datastax.driver.core.Statement> before = trackingInfoChanges.getKey();
            if (before != null) {
                com.datastax.driver.core.ResultSet result = execute(before.getValue(), "persistFrbWin before");
                if (!result.wasApplied()) {
                    LOG.error("persistFrbWin before statement ({}) return not success result", before);
                    assertAppliedByVersion(result, data.getVersion());
                }
            }
        } finally {
            if (trackingInfoChanges != null && trackingInfoChanges.getKey() != null && trackingInfoChanges.getKey().getKey() != null) {
                TRACKING_TABLE.releaseBuffer(trackingInfoChanges.getKey().getKey());
            }
        }

        com.datastax.driver.core.Statement after = trackingInfoChanges.getValue();
        long clock = getTimeStamp(data);
        data.setLastUpdateInfo(gameServerId + "_" + Thread.currentThread().getName() + "_" + clock);


        com.datastax.driver.core.querybuilder.Update update = getUpdateQuery(data.getLockId());
        update.using(com.datastax.driver.core.querybuilder.QueryBuilder.timestamp(clock));
        ByteBuffer byteBuffer = TABLE.serializeToBytes(data.getFrbWin());
        ByteBuffer luBuffer = null;
        try {
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(FRBWIN_FIELD, byteBuffer));
            if (data.isTrackingStateChanged()) {
                update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(TRACKING_INFO, serializeField(data.getBankId(), TRACKING_INFO,
                        data.getTrackingStateAsString())));
            }
            luBuffer = com.datastax.driver.core.TypeCodec.ascii().serialize(data.getLastUpdateInfo(), SERIALIZE_PROTOCOL_VERSION);
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(LAST_UPDATE_ID_FIELD, luBuffer));
            com.datastax.driver.core.ResultSet result = execute(update, "persistFrbWin");
            if (LOG.isDebugEnabled()) {
                LOG.debug("persistFrbWin " + data.getFrbWin());
            }
            assertAppliedByVersion(result, data.getVersion());
            data.setWriteTime(clock);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " persistFrbWin",
                    System.currentTimeMillis() - now);
        } finally {
            releaseBuffer(byteBuffer);
            if (luBuffer != null) {
                releaseBuffer(luBuffer);
            }
        }
        if (after != null) {
            com.datastax.driver.core.ResultSet result = execute(after, "persistFrbWin after");
            if (!result.wasApplied()) {
                LOG.error("persistFrbWin after statement ({}) return not success result", after);
                assertAppliedByVersion(result, data.getVersion());
            }
        }
    }

    @Override
    public void persistFrbNotification(ITransactionData data) {
        long now = System.currentTimeMillis();
        Pair<Pair<ByteBuffer, com.datastax.driver.core.Statement>, com.datastax.driver.core.Statement> trackingInfoChanges = null;
        try {
            trackingInfoChanges = updateTrackingInfo(data);
            Pair<ByteBuffer, com.datastax.driver.core.Statement> before = trackingInfoChanges.getKey();
            if (before != null) {
                com.datastax.driver.core.ResultSet result = execute(before.getValue(), "persistFrbNotification before");
                if (!result.wasApplied()) {
                    LOG.error("persistFrbNotification before statement ({}) return not success result", before);
                    assertAppliedByVersion(result, data.getVersion());
                }
            }
        } finally {
            if (trackingInfoChanges != null && trackingInfoChanges.getKey() != null && trackingInfoChanges.getKey().getKey() != null) {
                TRACKING_TABLE.releaseBuffer(trackingInfoChanges.getKey().getKey());
            }
        }

        com.datastax.driver.core.Statement after = trackingInfoChanges.getValue();
        long clock = getTimeStamp(data);
        data.setLastUpdateInfo(gameServerId + "_" + Thread.currentThread().getName() + "_" + clock);

        com.datastax.driver.core.querybuilder.Update update = getUpdateQuery(data.getLockId());
        update.using(com.datastax.driver.core.querybuilder.QueryBuilder.timestamp(clock));
        ByteBuffer byteBuffer = TABLE.serializeToBytes(data.getFrbNotification());
        ByteBuffer luBuffer = null;
        try {
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(FRBNOTIFY_FIELD, byteBuffer));
            if (data.isTrackingStateChanged()) {
                update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(TRACKING_INFO, serializeField(data.getBankId(), TRACKING_INFO,
                        data.getTrackingStateAsString())));
            }
            luBuffer = com.datastax.driver.core.TypeCodec.ascii().serialize(data.getLastUpdateInfo(), SERIALIZE_PROTOCOL_VERSION);
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(LAST_UPDATE_ID_FIELD, luBuffer));
            com.datastax.driver.core.ResultSet result = execute(update, "persistFrbNotification");
            if (LOG.isDebugEnabled()) {
                LOG.debug("persistFrbNotification " + data.getFrbNotification());
            }
            assertAppliedByVersion(result, data.getVersion());
            data.setWriteTime(clock);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " persistFrbNotification",
                    System.currentTimeMillis() - now);
        } finally {
            releaseBuffer(byteBuffer);
            if (luBuffer != null) {
                releaseBuffer(luBuffer);
            }
        }
        if (after != null) {
            com.datastax.driver.core.ResultSet result = execute(after, "persistFrbNotification after");
            if (!result.wasApplied()) {
                LOG.error("persistFrbNotification after statement ({}) return not success result", after);
                assertAppliedByVersion(result, data.getVersion());
            }
        }
    }

    @Override
    public void persistAccount(ITransactionData data) {
        long now = System.currentTimeMillis();

        long clock = getTimeStamp(data);
        data.setLastUpdateInfo(gameServerId + "_" + Thread.currentThread().getName() + "_" + clock);

        com.datastax.driver.core.querybuilder.Update update = getUpdateQuery(data.getLockId());
        update.using(com.datastax.driver.core.querybuilder.QueryBuilder.timestamp(clock));
        ByteBuffer accountByteBuffer = TABLE.serializeToBytes(data.getAccount());
        ByteBuffer luBuffer = null;
        try {
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(ACCOUNT_FIELD, accountByteBuffer));
            luBuffer = com.datastax.driver.core.TypeCodec.ascii().serialize(data.getLastUpdateInfo(), SERIALIZE_PROTOCOL_VERSION);
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(LAST_UPDATE_ID_FIELD, luBuffer));
            com.datastax.driver.core.ResultSet result = execute(update, "persistAccount");
            boolean wasApplied = result.wasApplied();
            if (!wasApplied) {
                LOG.error("persistAccount new : {}, was not applied", data.getPaymentTransaction());
                throw new RuntimeException("Persist AccountInfo failed, result was not applied");
            } else {
                data.setWriteTime(clock);
            }

            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    " persistAccount", System.currentTimeMillis() - now);
        } finally {
            releaseBuffer(accountByteBuffer);
            if (luBuffer != null) {
                releaseBuffer(luBuffer);
            }
        }
    }


    @Override
    public void persistPaymentTransaction(ITransactionData data, boolean saveAccount) {
        long now = System.currentTimeMillis();
        Pair<Pair<ByteBuffer, com.datastax.driver.core.Statement>, com.datastax.driver.core.Statement> trackingInfoChanges = null;
        try {
            trackingInfoChanges = updateTrackingInfo(data);
            Pair<ByteBuffer, com.datastax.driver.core.Statement> before = trackingInfoChanges.getKey();
            if (before != null) {
                com.datastax.driver.core.ResultSet result = execute(before.getValue(), "persistWallet before");
                if (!result.wasApplied()) {
                    LOG.error("persistPaymentTransaction before statement ({}) return not success result", before);
                    assertAppliedByVersion(result, data.getVersion());
                }
            }
        } finally {
            if (trackingInfoChanges != null && trackingInfoChanges.getKey() != null && trackingInfoChanges.getKey().getKey() != null) {
                TRACKING_TABLE.releaseBuffer(trackingInfoChanges.getKey().getKey());
            }
        }

        com.datastax.driver.core.Statement after = trackingInfoChanges.getValue();
        long clock = getTimeStamp(data);
        data.setLastUpdateInfo(gameServerId + "_" + Thread.currentThread().getName() + "_" + clock);

        com.datastax.driver.core.querybuilder.Update update = getUpdateQuery(data.getLockId());
        update.using(com.datastax.driver.core.querybuilder.QueryBuilder.timestamp(clock));
        ByteBuffer paymentByteBuffer = TABLE.serializeToBytes(data.getPaymentTransaction());
        ByteBuffer accountByteBuffer = saveAccount && data.getAccount() != null ?
                TABLE.serializeToBytes(data.getAccount()) : null;
        ByteBuffer luBuffer = null;
        try {
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(PAYMENT_TRANSACTION_FIELD, paymentByteBuffer));
            if (accountByteBuffer != null) {
                update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(ACCOUNT_FIELD, accountByteBuffer));
            }
            if (data.isTrackingStateChanged()) {
                update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(TRACKING_INFO, serializeField(data.getBankId(),
                        TRACKING_INFO, data.getTrackingStateAsString())));
            }
            luBuffer = com.datastax.driver.core.TypeCodec.ascii().serialize(data.getLastUpdateInfo(), SERIALIZE_PROTOCOL_VERSION);
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(LAST_UPDATE_ID_FIELD, luBuffer));
            com.datastax.driver.core.ResultSet result = execute(update, "persistPaymentTransaction");
            boolean wasApplied = result.wasApplied();
            if (!wasApplied) {
                LOG.error("persistPaymentTransaction new : {}, was not applied", data.getPaymentTransaction());
                throw new RuntimeException("Persist PaymentTransaction failed, result was not applied");
            } else {
                data.setWriteTime(clock);
            }

            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    " persistPaymentTransaction", System.currentTimeMillis() - now);
        } finally {
            releaseBuffer(paymentByteBuffer);
            if (accountByteBuffer != null) {
                releaseBuffer(accountByteBuffer);
            }
            if (luBuffer != null) {
                releaseBuffer(luBuffer);
            }
        }
        if (after != null) {
            com.datastax.driver.core.ResultSet result = execute(after, "persistPaymentTransaction after");
            if (!result.wasApplied()) {
                LOG.error("persistPaymentTransaction after statement ({}) return not success result", after);
                assertAppliedByVersion(result, data.getVersion());
            }
        }
    }

    @Override
    public void persistWallet(ITransactionData data) {
        long now = System.currentTimeMillis();
        Pair<Pair<ByteBuffer, com.datastax.driver.core.Statement>, com.datastax.driver.core.Statement> trackingInfoChanges = null;
        try {
            trackingInfoChanges = updateTrackingInfo(data);
            Pair<ByteBuffer, com.datastax.driver.core.Statement> before = trackingInfoChanges.getKey();
            if (before != null) {
                com.datastax.driver.core.ResultSet result = execute(before.getValue(), "persistWallet before");
                if (!result.wasApplied()) {
                    LOG.error("persistWallet before statement ({}) return not success result", before);
                    assertAppliedByVersion(result, data.getVersion());
                }
            }
        } finally {
            if (trackingInfoChanges != null && trackingInfoChanges.getKey() != null && trackingInfoChanges.getKey().getKey() != null) {
                TRACKING_TABLE.releaseBuffer(trackingInfoChanges.getKey().getKey());
            }
        }

        com.datastax.driver.core.Statement after = trackingInfoChanges.getValue();

        long clock = getTimeStamp(data);
        data.setLastUpdateInfo(gameServerId + "_" + Thread.currentThread().getName() + "_" + clock);

        com.datastax.driver.core.querybuilder.Update update = getUpdateQuery(data.getLockId());
        update.using(com.datastax.driver.core.querybuilder.QueryBuilder.timestamp(clock));
        ByteBuffer byteBuffer = TABLE.serializeWithClassToBytes(data.getWallet());
        ByteBuffer luBuffer = null;
        try {
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(WALLET_FIELD, byteBuffer));
            if (data.isTrackingStateChanged()) {
                update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(TRACKING_INFO, serializeField(data.getBankId(), TRACKING_INFO,
                        data.getTrackingStateAsString())));
            }
            luBuffer = com.datastax.driver.core.TypeCodec.ascii().serialize(data.getLastUpdateInfo(), SERIALIZE_PROTOCOL_VERSION);
            update.with(com.datastax.driver.core.querybuilder.QueryBuilder.set(LAST_UPDATE_ID_FIELD, luBuffer));
            com.datastax.driver.core.ResultSet result = execute(update, "persistWallet");
            boolean wasApplied = result.wasApplied();
            if (!wasApplied) {
                LOG.error("persistWallet new : {}, was not applied", data.getWallet());
                throw new RuntimeException("Persist wallet failed, result was not applied");
            } else {
                data.setWriteTime(clock);
            }

            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " persistWallet",
                    System.currentTimeMillis() - now);
        } finally {
            releaseBuffer(byteBuffer);
            if (luBuffer != null) {
                releaseBuffer(luBuffer);
            }
        }
        if (after != null) {
            com.datastax.driver.core.ResultSet result = execute(after, "persistWallet after");
            if (!result.wasApplied()) {
                LOG.error("persistWallet after statement ({}) return not success result", after);
                assertAppliedByVersion(result, data.getVersion());
            }
        }
    }

    public ITransactionData getFromCache(String lockId) {
        return cached.getIfPresent(lockId);
    }

    //don't use this method
    public ITransactionData getWithoutLockerCheck(String lockId) {
        long now = System.currentTimeMillis();
        ITransactionData data = cached.getIfPresent(lockId);
        if (data != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getWithoutLockerCheck[{}] from cache", lockId);
            }
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    " getWithoutLockerCheck [hit]", System.currentTimeMillis() - now);
        } else {
            data = getFromDB(lockId);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                    " getWithoutLockerCheck", System.currentTimeMillis() - now);
        }
        return data;
    }

    @Override
    public ITransactionData get(LockingInfo lockInfo) {
        long now = System.currentTimeMillis();
        String lockId = lockInfo.getLockId();
        ITransactionData data = cached.getIfPresent(lockId);
        if (data != null) {
            ServerLockInfo serverLockInfo = lockInfo.getServerLockInfo();
            boolean cacheExpiredDueToNewLock = serverLockInfo.getTimeStamp() * 1000 > data.getWriteTime();
            if (LOG.isDebugEnabled()) {
                LOG.debug("get[{}] from cache, expired={}", lockId, cacheExpiredDueToNewLock);
            }
            if (cacheExpiredDueToNewLock) {
                StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                        " get [expired on new lock]", System.currentTimeMillis() - now, lockId);
            }
            if (serverLockInfo.getLastLockerServerId() != gameServerId || cacheExpiredDueToNewLock) {
                notifyInvalidateListeners(data);
                cached.invalidate(lockId);
                data = getFromDB(lockId);
                if (data != null) {
                    cached.put(lockId, data);
                }
                StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() +
                        " get [locker mismatch]", System.currentTimeMillis() - now);
            } else {
                StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " get [hit]",
                        System.currentTimeMillis() - now);
            }
        } else {
            data = getFromDB(lockId);
            if (data != null) {
                cached.put(lockId, data);
            }
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " get",
                    System.currentTimeMillis() - now);
        }
        return data;
    }

    public Map<String, ByteBuffer> getByColumns(String lockId, String... columns) {
        long now = System.currentTimeMillis();

        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select(columns).from(getMainColumnFamilyName());
        query.where(eq(getKeyColumnName(), lockId)).limit(1);
        com.datastax.driver.core.ResultSet resultSet = execute(query, "CassandraTransactionDataPersister: getByColumns");
        com.datastax.driver.core.Row row = resultSet.one();
        if (row == null) {
            return Collections.emptyMap();
        }
        com.datastax.driver.core.ColumnDefinitions definitions = row.getColumnDefinitions();
        Map<String, ByteBuffer> map = new HashMap<>(definitions.size());
        for (com.datastax.driver.core.ColumnDefinitions.Definition definition : definitions) {
            com.datastax.driver.core.DataType dataType = definition.getType();
            ByteBuffer bytes;
            if (blob().equals(dataType)) {
                bytes = row.getBytes(definition.getName());
            } else {
                bytes = row.getBytesUnsafe(definition.getName());
            }
            if (bytes != null) {
                map.put(definition.getName(), bytes);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + " getByColumns",
                System.currentTimeMillis() - now);
        return map;
    }

    public Iterable<Pair<String, GameSession>> getGameSessions(int bankId) {
        return getColumns(GAME_SESSION_FIELD, GameSession.class, bankId);
    }

    public <T> Iterable<Pair<String, T>> getColumns(final String column, final Class<? extends T> valueType) {
        return executeAndGetAsIterableSkipNull(
                getSelectColumnsQuery(KEY, column),
                "getGameSessions",
                row -> {
                    String lockId = row.getString(KEY);
                    if (StringUtils.isTrimmedEmpty(lockId)) {
                        return null;
                    }
                    Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserIdFromUserHash(lockId);
                    Integer bankId = pair.getKey();
                    T value = deserializeField(bankId, row.getBytes(column), column);
                    return value == null ? null : new Pair<>(lockId, value);
                });
    }

    public <T> Iterable<Pair<String, T>> getColumns(final String column, final Class<? extends T> valueType, final int bankId) {
        return executeAndGetAsIterableSkipNull(
                getSelectColumnsQuery(KEY, column),
                "getGameSessions",
                row -> {
                    String lockId = row.getString(KEY);
                    if (StringUtils.isTrimmedEmpty(lockId)) {
                        return null;
                    }
                    Pair<Integer, String> pair = StringIdGenerator.extractBankAndExternalUserIdFromUserHash(lockId);
                    if (!pair.getKey().equals(bankId)) {
                        return null;
                    }
                    T value = deserializeField(bankId, row.getBytesUnsafe(column), column);
                    return value == null ? null : new Pair<>(lockId, value);
                });
    }


    public Map<String, Map<String, Object>> getByKeysColumns(Set<String> lockIds, Pair<String, Class>... columns) {
        String[] columnsNames = new String[columns.length + 1];
        for (int i = 0; i < columns.length; i++) {
            Pair<String, Class> pair = columns[i];
            columnsNames[i] = pair.getKey();
        }
        columnsNames[columns.length] = KEY;
        com.datastax.driver.core.querybuilder.Select query = getSelectColumnsQuery(columnsNames);
        query.where(com.datastax.driver.core.querybuilder.QueryBuilder.in(KEY, lockIds.toArray()));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getByKeysColumns");
        Map<String, Map<String, Object>> result = new HashMap<>();
        for (com.datastax.driver.core.Row row : resultSet) {
            Map<String, Object> cur = new HashMap<>();
            for (Pair<String, Class> column : columns) {
                ByteBuffer bytes = row.getBytes(column.getKey());
                Object o = bytes == null ? null : TABLE.deserializeFrom(bytes, column.getValue());
                if (o != null) {
                    cur.put(column.getKey(), o);
                }
            }
            if (!cur.isEmpty()) {
                result.put(row.getString(KEY), cur);
            }
        }
        return result;
    }

    public Pair<TrackingState, TrackingInfo> getTrackingInfo(String lockId) {
        Set<Integer> servers = LoadBalancerCache.getInstance().getServers();
        Pair<TrackingState, TrackingInfo> result = null;
        com.datastax.driver.core.querybuilder.Clause keyClause = eq(KEY, lockId);
        com.datastax.driver.core.querybuilder.Clause statusClause = com.datastax.driver.core.querybuilder.QueryBuilder.in(TRACKING_STATUS, 0, 1, 2, 3);
        for (Integer serverId : servers) {
            com.datastax.driver.core.querybuilder.Select select = getSelectColumnsQuery(TRACKING_TABLE, SERVER_ID, TRACKING_STATUS, KEY,
                    SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME);
            select.where(statusClause).and(eq(SERVER_ID, serverId)).and(keyClause);
            final TrackingStatus[] trackingStatuses = TrackingStatus.values();
            com.datastax.driver.core.ResultSet rows = execute(select, "getTrackingInfo");
            for (com.datastax.driver.core.Row row : rows) {
                int aServerId = row.getInt(SERVER_ID);
                int trackingStatus = row.getInt(TRACKING_STATUS);
                String json = row.getString(JSON_COLUMN_NAME);
                TrackingInfo info = TRACKING_TABLE.deserializeFromJson(json, TrackingInfo.class);
                if (info == null) {
                    ByteBuffer value = row.getBytes(SERIALIZED_COLUMN_NAME);
                    info = TRACKING_TABLE.deserializeFrom(value, TrackingInfo.class);
                }
                Pair<TrackingState, TrackingInfo> pair = new Pair<>(new TrackingState(aServerId,
                        trackingStatuses[trackingStatus]), info);
                if (result == null) {
                    result = pair;
                } else {
                    LOG.error("getTrackingInfo: duplicate tracking info, result={}, pair={}", result, pair);
                }
            }
        }

        return result;
    }

    public List<ITransactionData> getTransactionsWithoutTracking() {
        List<ITransactionData> result = new ArrayList<>();
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().
                column(KEY).
                column(ACCOUNT_FIELD).
                column(PLAYER_SESSION_FIELD).
                column(GAME_SESSION_FIELD).
                column(LAST_HAND_FIELD).
                column(WALLET_FIELD).
                column(LAST_BET_FIELD).
                column(BONUS_FIELD).
                column(FRBONUS_FIELD).
                column(FRBWIN_FIELD).
                column(FRBNOTIFY_FIELD).
                column(LAST_UPDATE_ID_FIELD).
                column(VERSION_FIELD).
                column(TRACKING_INFO).
                column(EXTRA_ACCOUNT_INFO).
                column(GAME_ACHIEVEMENT).
                column(PLAYER_ACTIVITY).
                column(PAYMENT_TRANSACTION_FIELD).
                writeTime(LAST_UPDATE_ID_FIELD).as(LAST_WRITE_TIME_ALIAS).
                from(getMainColumnFamilyName());

        com.datastax.driver.core.ResultSet resultSet = execute(query, "getTransactionsWithoutTracking");

        Iterator<com.datastax.driver.core.Row> it = resultSet.iterator();
        while (it.hasNext()) {
            com.datastax.driver.core.Row row = it.next();
            String lockId = row.getString(KEY);
            ITransactionData data = convertTransactionData(lockId, row);
            if (data == null) {
                continue;
            }
            Pair<TrackingState, TrackingInfo> trackingInfoPair = getTrackingInfo(data.getLockId());
            if (trackingInfoPair == null) {
                result.add(data);
            } else {
                TrackingState state = trackingInfoPair.getKey();
                TrackingInfo trackingInfo = trackingInfoPair.getValue();
                if (state == null || trackingInfo == null) {
                    LOG.error("getTransactionsWithoutTracking: bad tracking info, " +
                            "lockId={}, trackingInfoPair={}", data.getLockId(), trackingInfoPair);
                } else {
                    TrackingState cachedTrackingState = data.getTrackingState();
                    boolean stateDifferent = !state.equals(cachedTrackingState);
                    TrackingInfo cachedTrackingInfo = data.getTrackingInfo();
                    boolean trackingInfoDifferent = !trackingInfo.equals(cachedTrackingInfo);
                    if (stateDifferent || trackingInfoDifferent) {
                        LOG.error("getTransactionsWithoutTracking: states mismatch, lockId = {}, trackingInfoPair = {}, " +
                                        "cachedTrackingState={}, cachedTrackingInfo={}", data.getLockId(), trackingInfoPair,
                                cachedTrackingState, cachedTrackingInfo);
                    }
                }
            }
        }
        return result;
    }

    //made public. dirty get, don't use this method in usual situations
    public ITransactionData getFromDB(String lockId) {
        com.datastax.driver.core.querybuilder.Select query = com.datastax.driver.core.querybuilder.QueryBuilder.select().
                column(KEY).
                column(ACCOUNT_FIELD).
                column(PLAYER_SESSION_FIELD).
                column(GAME_SESSION_FIELD).
                column(LAST_HAND_FIELD).
                column(WALLET_FIELD).
                column(LAST_BET_FIELD).
                column(BONUS_FIELD).
                column(FRBONUS_FIELD).
                column(FRBWIN_FIELD).
                column(FRBNOTIFY_FIELD).
                column(LAST_UPDATE_ID_FIELD).
                column(VERSION_FIELD).
                column(TRACKING_INFO).
                column(EXTRA_ACCOUNT_INFO).
                column(GAME_ACHIEVEMENT).
                column(PLAYER_ACTIVITY).
                column(PAYMENT_TRANSACTION_FIELD).
                column(PROMO_MEMBERS_FIELD).
                writeTime(LAST_UPDATE_ID_FIELD).as(LAST_WRITE_TIME_ALIAS).
                from(getMainColumnFamilyName()).
                where(eq(getKeyColumnName(), lockId)).
                limit(1);

        com.datastax.driver.core.ResultSet resultSet = execute(query, "getFromDB");
        com.datastax.driver.core.Row row = resultSet.one();
        return convertTransactionData(lockId, row);
    }

    private ITransactionData convertTransactionData(String lockId, com.datastax.driver.core.Row row) {
        if (row == null) {
            return null;
        }
        com.datastax.driver.core.ColumnDefinitions definitions = row.getColumnDefinitions();
        Map<String, ByteBuffer> map = new HashMap<>(definitions.size());
        for (com.datastax.driver.core.ColumnDefinitions.Definition definition : definitions) {
            com.datastax.driver.core.DataType dataType = definition.getType();
            ByteBuffer bytes;
            if (blob().equals(dataType)) {
                bytes = row.getBytes(definition.getName());
            } else {
                bytes = row.getBytesUnsafe(definition.getName());
            }
            if (bytes != null) {
                map.put(definition.getName(), bytes);
            }
        }
        if (CollectionUtils.isEmpty(map)) {
            return null;
        }
        ITransactionData transactionData = TransactionDataFactory.getInstance().createTransactionData(lockId, map, gameServerId);
        long writeTime = row.getLong(LAST_WRITE_TIME_ALIAS);
        transactionData.setWriteTime(writeTime);
        return transactionData;
    }

    protected <T> T deserializeField(int bankId, ByteBuffer buffer, String field) {
        return TransactionDataFactory.getInstance().getTransactionDataManager(bankId).getHelper(field).deserialize(buffer);
    }

    protected ByteBuffer serializeField(int bankId, String fieldName, Object value) {
        return TransactionDataFactory.getInstance().getTransactionDataManager(bankId).getHelper(fieldName).serialize(value);
    }

    @Override
    public boolean delete(String lockId) {
        LOG.debug("delete: lockId={}", lockId);
        com.datastax.driver.core.Statement delete = com.datastax.driver.core.querybuilder.QueryBuilder.delete().
                from(getMainColumnFamilyName()).
                where(getSimpleKeyClause(lockId));
        com.datastax.driver.core.ResultSet resultSet = execute(delete, "delete");
        notifyInvalidateListeners(cached.getIfPresent(lockId));
        cached.invalidate(lockId);
        return resultSet.wasApplied();
    }

    @Override
    public boolean delete(ITransactionData data) {
        String lockId = data.getLockId();
        LOG.debug("delete: id={}, lockId={}", data.getId(), lockId);
        if (data.getAccount() != null) {
            accountInfoPersister.persist(data.getAccount());
        }
        com.datastax.driver.core.RegularStatement delete = com.datastax.driver.core.querybuilder.QueryBuilder.delete().
                from(getMainColumnFamilyName()).
                where(getSimpleKeyClause(lockId));
        com.datastax.driver.core.Statement deleteTrackingInfo = getDeleteTrackingInfo(data, data.getTrackingState());
        boolean success = processInTransactionItems(data, delete, null, deleteTrackingInfo, "persist");
        notifyInvalidateListeners(data);
        cached.invalidate(lockId);
        return success;
    }

    private Pair<Pair<ByteBuffer, com.datastax.driver.core.Statement>, com.datastax.driver.core.Statement> updateTrackingInfo(ITransactionData data) {
        TrackingState prevTrackingState = data.getTrackingState();
        data.updateTrackingState(gameServerId);

        boolean stateChanged = data.isTrackingStateChanged();
        Pair<ByteBuffer, com.datastax.driver.core.Statement> before = null;
        try {
            before = stateChanged ? getPersistTrackingInfo(data) : null;
            com.datastax.driver.core.Statement after = stateChanged ? getDeleteTrackingInfo(data, prevTrackingState) : null;
            return new Pair<>(before, after);
        } catch (Throwable t) {
            if (before != null && before.getKey() != null) {
                TRACKING_TABLE.releaseBuffer(before.getKey());
            }
            throw t;
        }
    }

    public Pair<ByteBuffer, com.datastax.driver.core.Statement> getPersistTrackingInfo(ITransactionData data) {
        TrackingInfo trackingInfo = data.getTrackingInfo();
        String json = TRACKING_TABLE.serializeToJson(trackingInfo);
        ByteBuffer byteBuffer = TRACKING_TABLE.serializeToBytes(trackingInfo);
        try {
            TrackingState trackingState = data.getTrackingState();
            return new Pair<>(byteBuffer, getInsertQuery(TRACKING_TABLE, null).
                    value(SERVER_ID, trackingState.getGameServerId()).
                    value(TRACKING_STATUS, trackingState.getStatus().ordinal()).
                    value(KEY, data.getLockId()).
                    value(SERIALIZED_COLUMN_NAME, byteBuffer).
                    value(JSON_COLUMN_NAME, json));
        } catch (Throwable t) {
            TRACKING_TABLE.releaseBuffer(byteBuffer);
            throw t;
        }
    }

    private com.datastax.driver.core.Statement getDeleteTrackingInfo(ITransactionData data, TrackingState trackingState) {
        if (trackingState == null) {
            return null;
        }
        return addItemDeletion(TRACKING_TABLE.getTableName(),
                eq(SERVER_ID, trackingState.getGameServerId()),
                eq(TRACKING_STATUS, trackingState.getStatus().ordinal()),
                eq(KEY, data.getLockId())
        );
    }

    public void deleteTrackingInfo(ITransactionData data, TrackingState trackingState) {
        execute(getDeleteTrackingInfo(data, trackingState), "deleteTrackingInfo");
    }

    @Override
    public void invalidate(ServerLockInfo lockInfo) {
        notifyInvalidateListeners(lockInfo);
        cached.invalidate(lockInfo.getLockId());
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    public void lockChanged(ServerLockInfo lockInfo) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("lockChanged: {}", lockInfo);
        }
        if (lockInfo.getLastLockerServerId() != getGameServerId()) {
            LOG.info("lockChanged, found lastLocker mismatch, invalidate cache: {}; lastLockerServerId: {}",
                    lockInfo.getLockId(), lockInfo.getLastLockerServerId());
            notifyInvalidateListeners(lockInfo);
            cached.invalidate(lockInfo.getLockId());
        }
    }

    private void notifyInvalidateListeners(ServerLockInfo lockInfo) {
        String lockId = lockInfo.getLockId();
        notifyInvalidateListeners(cached.getIfPresent(lockId));
    }

    private void notifyInvalidateListeners(ITransactionData data) {
        if (data != null) {
            for (TransactionDataInvalidatedListener listener : invalidatedListeners) {
                listener.invalidate(data);
            }
        }
    }

    @Override
    public ITransactionData getObject(String id) {
        String lockId = id;
        try {
            long accountId = Long.parseLong(id);
            AccountInfo accountInfo = accountInfoPersister.getById(accountId);
            if (accountInfo != null) {
                lockId = accountInfo.getLockId();
            }
        } catch (NumberFormatException e) {
            //nop, id may be lockId
        }
        return getFromDB(lockId);
    }

    @Override
    public Map<String, ITransactionData> getAllObjects() {
        return cached.asMap();
    }

    @Override
    public String getAdditionalInfo() {
        return "";
    }

    @Override
    public String printDebug() {
        return "";
    }

    public long getCacheSize() {
        return cached.size();
    }
}
