package com.dgphoenix.casino.cassandra.persist;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.cassandra.persist.engine.configuration.Caching;
import com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.abs.casino.cassandra.persist.engine.configuration.Compression;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItem;
import com.dgphoenix.casino.common.transactiondata.storeddate.StoredItemType;
import com.abs.casino.common.transactiondata.storeddate.identifier.PlayerBetStoredInfo;
import com.abs.casino.common.transactiondata.storeddate.identifier.PlayerBetTransferStoredInfo;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.abs.casino.common.util.FastKryoHelper;
import com.abs.casino.common.util.LZ4Compressor;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.abs.casino.gs.managers.bet.PlayerBetPersister;
import com.esotericsoftware.kryo.Serializer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User: flsh
 * Date: 20.10.11
 */
public class CassandraBetPersister extends AbstractCassandraPersister<Long, Long> implements PlayerBetPersister {
    public static final String COLUMN_FAMILY_NAME = "BetCF";
    public static final String GAME_SESSION_ID_FIELD = "SID";
    public static final String BET_ID_FIELD = "BID";
    private static final Logger LOG = LogManager.getLogger(CassandraBetPersister.class);
    private CassandraTempBetPersister tempBetPersister;
    private CassandraRoundGameSessionPersister roundGameSessionPersister;

    private final Serializer betListSerializer = FastKryoHelper.createCollectionSerializer(PlayerBet.class);

    private static final TableDefinition TABLE = new TableDefinition(COLUMN_FAMILY_NAME,
            Arrays.asList(
                    new ColumnDefinition(GAME_SESSION_ID_FIELD, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(SERIALIZED_COLUMN_NAME, com.datastax.driver.core.DataType.blob()),
                    new ColumnDefinition(JSON_COLUMN_NAME, com.datastax.driver.core.DataType.text())
            ), GAME_SESSION_ID_FIELD)
            .caching(Caching.NONE)
            .gcGraceSeconds(0)
            .compaction(CompactionStrategy.LEVELED)
            .compression(Compression.DEFLATE);

    protected final Cache<Long, List<PlayerBet>> cached = CacheBuilder.
            newBuilder().
            initialCapacity(100).
            maximumSize(1024).
            concurrencyLevel(8).
            build();

    @SuppressWarnings("unused")
    private void setTempBetPersister(CassandraTempBetPersister tempBetPersister) {
        LOG.debug("Start setting tempBetPersister: {}", tempBetPersister);
        this.tempBetPersister = tempBetPersister;
        LOG.debug("Set tempBetPersister: {}", this.tempBetPersister);
    }

    @SuppressWarnings("unused")
    private void setRoundGameSessionPersister(CassandraRoundGameSessionPersister roundGameSessionPersister) {
        LOG.debug("Start setting roundGameSessionPersister: {}", roundGameSessionPersister);
        this.roundGameSessionPersister = roundGameSessionPersister;
        LOG.debug("Set roundGameSessionPersister field: {}", this.roundGameSessionPersister);
    }

    protected CassandraBetPersister() {
    }

    public CassandraTempBetPersister getTempBetPersister() {
        return tempBetPersister;
    }

    public CassandraRoundGameSessionPersister getRoundGameSessionPersister() {
        return roundGameSessionPersister;
    }

    public List<PlayerBet> getBets(Long gameSessionId) {
        return getBets(gameSessionId, null, null);
    }

    @Override
    protected String getKeyColumnName() {
        return GAME_SESSION_ID_FIELD;
    }

    public List<PlayerBet> getBets(Long gameSessionId, Integer from, Integer count) {//only from GameHistoryServlet
        return getBets(gameSessionId, from, count, false);
    }

    //only from GameHistoryServlet
    public List<PlayerBet> getBets(Long gameSessionId, Integer from, Integer count, boolean immutableCache) {
        long now = System.currentTimeMillis();
        List<PlayerBet> bets = cached.getIfPresent(gameSessionId);
        if (bets != null) {
            List<PlayerBet> result = choiceBets(bets, from, count);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ": getBetsAndRealSize [hint]",
                    System.currentTimeMillis() - now);
            return result;
        }
        com.datastax.driver.core.Statement query = com.datastax.driver.core.querybuilder.QueryBuilder.select(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .from(getMainColumnFamilyName())
                .where(eq(GAME_SESSION_ID_FIELD, gameSessionId));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getBetsAndRealSize");
        com.datastax.driver.core.Row row = resultSet.one();
        if (row != null) {
            bets = TABLE.deserializeToListJson(row.getString(JSON_COLUMN_NAME), PlayerBet.class);
            if (bets == null) {
                bets = TABLE.deserializeToList(row.getBytes(SERIALIZED_COLUMN_NAME), betListSerializer);
            }
            if (!immutableCache && bets != null && bets.size() > 10) {
                cached.put(gameSessionId, bets);
            }
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ": getBetsAndRealSize",
                    System.currentTimeMillis() - now);
            return choiceBets(bets, from, count);
        }
        getLog().warn("getBetsAndRealSize: not found bets by gameSessionId={}", gameSessionId);
        return Collections.emptyList();
    }

    public Pair<Integer, List<PlayerBet>> getBetsAndRealSize(Long gameSessionId, Integer from, Integer count,
                                                             boolean immutableCache) {
        long now = System.currentTimeMillis();
        List<PlayerBet> bets = cached.getIfPresent(gameSessionId);
        if (bets != null) {
            List<PlayerBet> result = choiceBets(bets, from, count);
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ": getBetsAndRealSize [hint]",
                    System.currentTimeMillis() - now);
            return new Pair<>(bets.size(), result);
        }
        com.datastax.driver.core.Statement query = com.datastax.driver.core.querybuilder.QueryBuilder.select(SERIALIZED_COLUMN_NAME, JSON_COLUMN_NAME)
                .from(getMainColumnFamilyName())
                .where(eq(GAME_SESSION_ID_FIELD, gameSessionId));
        com.datastax.driver.core.ResultSet resultSet = execute(query, "getBetsAndRealSize");
        com.datastax.driver.core.Row row = resultSet.one();
        if (row != null) {
            bets = TABLE.deserializeToListJson(row.getString(JSON_COLUMN_NAME), PlayerBet.class);
            if (bets == null) {
                bets = TABLE.deserializeToList(row.getBytes(SERIALIZED_COLUMN_NAME), betListSerializer);
            }
            if (!immutableCache && bets != null && bets.size() > 10) {
                cached.put(gameSessionId, bets);
            }
            StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ": getBetsAndRealSize",
                    System.currentTimeMillis() - now);
            List<PlayerBet> result = choiceBets(bets, from, count);
            assert bets != null;
            return new Pair<>(bets.size(), result);
        }
        getLog().warn("getBetsAndRealSize: not found bets by gameSessionId={}", gameSessionId);
        return new Pair<>(0, Collections.<PlayerBet>emptyList());
    }

    protected List<PlayerBet> choiceBets(List<PlayerBet> bets, Integer from, Integer count) {
        if (CollectionUtils.isEmpty(bets)) {
            return Collections.emptyList();
        } else if (from == null) {
            return Collections.unmodifiableList(bets);
        } else if (from > bets.size()) {
            return Collections.emptyList();
        } else {
            int fromIndex = bets.size() > from ? from : 0;
            int toIndex = bets.size() >= from + count ? from + count : bets.size();
            return Collections.unmodifiableList(bets.subList(fromIndex, toIndex));
        }
    }

    public Pair<Integer, List<PlayerBet>> getBetsAndRealSize(long gameSessionId, int from, int count, final Long roundId) {
        long now = System.currentTimeMillis();
        Pair<Integer, List<PlayerBet>> pair = getBetsAndRealSize(gameSessionId, null, null, false);
        List<PlayerBet> result = pair.getValue().stream()
                .filter(playerBet -> playerBet.getServletData().contains("ROUND_ID=" + roundId)
                        || playerBet.getData().contains("playerRoundId=" + roundId))
                .collect(Collectors.toList());

        //next lines is dirty hack, all bets stored as single record, but VAB Applets need paging
        int start = from > result.size() ? result.size() : from;
        int end = start + count > result.size() ? result.size() : start + count;
        Pair<Integer, List<PlayerBet>> resultPair = new Pair<>(result.size(), result.subList(start, end));
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ": getBetsAndRealSize(range)",
                System.currentTimeMillis() - now);
        return resultPair;
    }


    public int getBetsCount(long gameSessionId) {
        return (int) count(eq(GAME_SESSION_ID_FIELD, gameSessionId));
    }

    public int getRoundsCount(GameSession gameSession, long roundId) {
        long now = System.currentTimeMillis();
        List<PlayerBet> bets = getBets(gameSession.getId());
        int count = 0;
        if (!CollectionUtils.isEmpty(bets)) {
            for (PlayerBet bet : bets) {
                String servletData = bet.getServletData();
                boolean isRoundIdBet = servletData != null && servletData.contains("ROUND_ID=" + roundId);
                boolean isRoundIdBetMQ = bet.getData() != null && bet.getData().contains("playerRoundId=" + roundId);
                if (isRoundIdBet || isRoundIdBetMQ) {
                    count++;
                }
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ": getRoundsCount",
                System.currentTimeMillis() - now);
        return count;
    }

    @Override
    public PlayerBet persist(GameSession gameSession, PlayerBet bet, boolean createNewBet, Long roundId,
                             boolean isSaveGameSidByRound) {
        try {
            put(gameSession, bet);
            if (roundId != null && isSaveGameSidByRound) {
                getRoundGameSessionPersister().persist(roundId, gameSession);
            }
        } catch (Exception e) {
            getLog().error("cannot save bet: " + bet, e);
            throw e;
        }
        gameSession.setCreateNewBet(createNewBet, true);
        return bet;
    }

    @Override
    public void finishGameSession(GameSession gameSession, long endTime, PlayerBet bet) {
        if (bet != null) {
            put(gameSession, bet);
        } else {
            getLog().error("finishGameSession: bet is null, gameSession={}", gameSession);
        }
        long now = System.currentTimeMillis();
        gameSession.finish(endTime, null);
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ": finishGameSession",
                System.currentTimeMillis() - now);
    }

    public List<PlayerBet> getPlayerBets(long gameSessionId, Set<Long> betIds) {
        if (CollectionUtils.isEmpty(betIds)) {
            return Collections.emptyList();
        }
        long now = System.currentTimeMillis();
        List<PlayerBet> bets = getBets(gameSessionId);
        if (CollectionUtils.isEmpty(bets)) {
            return Collections.emptyList();
        }
        List<PlayerBet> result = new ArrayList<>(Math.min(betIds.size(), bets.size()));
        for (PlayerBet playerBet : bets) {
            if (betIds.contains(playerBet.getId())) {
                result.add(playerBet);
            }
        }
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ": getPlayerBets",
                System.currentTimeMillis() - now);
        return result;
    }

    public PlayerBet getPlayerBet(long gameSessionId, long betId) {
        return tempBetPersister.getPlayerBet(gameSessionId, betId);
    }

    @Override
    public PlayerBet getCurrentBet(GameSession gameSession) {
        if (SessionHelper.getInstance().isSessionOpen()) {
            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            GameSession currentGameSession = transactionData.getGameSession();
            if (currentGameSession != null && currentGameSession.getId() == gameSession.getId()) {
                return transactionData.getLastBet();
            }
        }
        return getPlayerBet(gameSession.getId(), gameSession.getLastPlayerBetId());
    }

    @Override
    public void flushGameSessionHistory(GameSession gameSession) {
        SessionHelper.getInstance().getTransactionData().add(StoredItemType.TRANSFER_PLAYER_BET, gameSession.getId(),
                new PlayerBetTransferStoredInfo((int) (gameSession.getLastPlayerBetId() + 1)));
        SessionHelper.getInstance().getTransactionData().setLastBet(null);
    }

    public void prepareToPersistGameSessionBets(Map<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, long gameSessionId,
                                                int maxPlayerBetId, List<ByteBuffer> byteBuffersCollector) {
        long now = System.currentTimeMillis();
        List<com.datastax.driver.core.Statement> statements = getOrCreateStatements(statementsMap);
        Map<Integer, PlayerBet> betsMap = new HashMap<>(100);
        StoredItem<PlayerBet, PlayerBetStoredInfo> localCachedItem = SessionHelper.getInstance().getTransactionData().
                get(StoredItemType.PLAYER_BET);
        if (localCachedItem != null && localCachedItem.getIdentifier().getGameSessionId() == gameSessionId) {
            PlayerBet bet = localCachedItem.getItem();
            int betId = (int) bet.getId();
            if (betId <= maxPlayerBetId) {
                betsMap.put(betId, bet);
            } else {
                getLog().error("!!! Incorrect playerBet with roundId > then max (gameSession.lastPlayerBetId+1). BetId={}, " +
                        "playerBet={}, gameSessionId={}", betId, bet, gameSessionId);
            }
            SessionHelper.getInstance().getTransactionData().getAtomicallyStoredData().
                    remove(StoredItemType.PLAYER_BET);
        }
        com.datastax.driver.core.ResultSet resultSet = tempBetPersister.getResultSetByGameSessionId(gameSessionId);
        Iterator<com.datastax.driver.core.Row> iterator = resultSet.iterator();

        while (iterator.hasNext()) {
            com.datastax.driver.core.Row row = iterator.next();
            if (row != null) {
                int betId = row.getInt(BET_ID_FIELD);
                if (!betsMap.containsKey(betId) && betId <= maxPlayerBetId) {
                    PlayerBet playerBet = tempBetPersister.getMainTableDefinition().
                            deserializeFromJson(row.getString(JSON_COLUMN_NAME),
                                    PlayerBet.class);
                    if (playerBet == null) {
                        playerBet = tempBetPersister.getMainTableDefinition().
                            deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME),
                                    PlayerBet.class);
                    }
                    if (playerBet != null) {
                        betsMap.put(betId, playerBet);
                    }
                } else if (getLog().isDebugEnabled()) {
                    getLog().error("!!! Incorrect playerBet with roundId > then max (gameSession.lastPlayerBetId+1). " +
                            "BetId=" + betId + ", gameSessionId=" + gameSessionId + " playerBet=" +
                            tempBetPersister.getMainTableDefinition().
                                    deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), PlayerBet.class));
                }
            }
        }

        if (betsMap.size() < maxPlayerBetId - 1) {//may be exist already saved bets
            //it's check already saved player bets
            List<PlayerBet> bets = getBets(gameSessionId, null, null, true);
            getLog().error("!!! rounds count=" + betsMap.size() + " but by gameSession must be " + (maxPlayerBetId - 1) +
                    " rounds" + ", gameSessionId=" + gameSessionId +
                    ", entire bets size=" + (bets == null ? "null" : bets.size()));
            if (bets != null) {
                for (PlayerBet playerBet : bets) {
                    if (playerBet != null && !betsMap.containsKey((int) playerBet.getId())) {
                        if (playerBet.getId() <= maxPlayerBetId) {
                            betsMap.put((int) playerBet.getId(), playerBet);
                        } else {
                            getLog().error("!!! Incorrect playerBet with roundId > then max " +
                                    "(gameSession.lastPlayerBetId+1). " +
                                    "BetId=" + playerBet.getId() + " playerBet=" + playerBet);
                        }
                    }
                }
            }
        }

        List<PlayerBet> playerBets = new ArrayList<>(betsMap.values());
        Collections.sort(playerBets);
        ByteBuffer value = FastKryoHelper.serializeToBytes(playerBets, betListSerializer, 512);
        byteBuffersCollector.add(value);
        statements.add(addInsertion(gameSessionId, SERIALIZED_COLUMN_NAME, value).setConsistencyLevel(com.datastax.driver.core.ConsistencyLevel.LOCAL_ONE));
        tempBetPersister.addDeleteStatement(statementsMap, gameSessionId);
        StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ": prepareToPersistBetsList",
                System.currentTimeMillis() - now);
    }

    @Override
    public Map<Long, List<PlayerBet>> getPlayerBets(Map<Long, List<Long>> sessionBetsMap) {
        Map<Long, Set<Long>> required = new HashMap<>(sessionBetsMap.size());
        for (Map.Entry<Long, List<Long>> entry : sessionBetsMap.entrySet()) {
            required.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        Map<Long, Map<Long, PlayerBet>> betsMap = new HashMap<>(sessionBetsMap.size());
        if (SessionHelper.getInstance().isSessionOpen()) {
            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            GameSession gameSession = transactionData.getGameSession();
            Long playedGameSessionId = null;
            if (gameSession != null) {
                playedGameSessionId = gameSession.getId();
            }
            StoredItem<PlayerBet, PlayerBetStoredInfo> localCachedBet = transactionData.get(StoredItemType.PLAYER_BET);
            for (Map.Entry<Long, Set<Long>> entry : required.entrySet()) {
                Long gameSessionId = entry.getKey();
                Set<Long> betIds = entry.getValue();
                if (localCachedBet != null && gameSessionId.equals(localCachedBet.getIdentifier().getGameSessionId())) {
                    long id = localCachedBet.getItem().getId();
                    if (betIds.contains(id)) {
                        addPlayerBet(betsMap, gameSessionId, localCachedBet.getItem());
                        betIds.remove(id);
                    }
                }
                if (playedGameSessionId != null && playedGameSessionId.equals(gameSessionId)) {
                    com.datastax.driver.core.ResultSet resultSet = tempBetPersister.getResultSetByGameSessionIdAndRounds(gameSessionId, betIds);
                    for (com.datastax.driver.core.Row row : resultSet) {
                        PlayerBet playerBet = tempBetPersister.getMainTableDefinition().
                                deserializeFromJson(row.getString(JSON_COLUMN_NAME), PlayerBet.class);
                        if (playerBet == null) {
                            playerBet = tempBetPersister.getMainTableDefinition().
                                deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), PlayerBet.class);
                        }
                        if (playerBet != null) {
                            addPlayerBet(betsMap, gameSessionId, playerBet);
                            betIds.remove(playerBet.getId());
                        }
                    }
                }
            }
        } else {
            for (Map.Entry<Long, Set<Long>> entry : required.entrySet()) {
                Long gameSessionId = entry.getKey();
                Set<Long> betIds = entry.getValue();
                com.datastax.driver.core.ResultSet resultSet = tempBetPersister.getResultSetByGameSessionIdAndRounds(gameSessionId, betIds);
                for (com.datastax.driver.core.Row row : resultSet) {
                    PlayerBet playerBet = tempBetPersister.getMainTableDefinition().
                            deserializeFromJson(row.getString(JSON_COLUMN_NAME), PlayerBet.class);
                    if (playerBet == null) {
                        playerBet = tempBetPersister.getMainTableDefinition().
                            deserializeFrom(row.getBytes(SERIALIZED_COLUMN_NAME), PlayerBet.class);
                    }
                    if (playerBet != null) {
                        addPlayerBet(betsMap, gameSessionId, playerBet);
                        betIds.remove(playerBet.getId());
                    }
                }
            }
        }
        for (Map.Entry<Long, Set<Long>> entry : required.entrySet()) {
            Set<Long> betIds = entry.getValue();
            if (CollectionUtils.isEmpty(betIds)) {
                continue;
            }
            Long gameSessionId = entry.getKey();
            List<PlayerBet> bets = getPlayerBets(gameSessionId, betIds);
            if (!CollectionUtils.isEmpty(bets)) {
                for (PlayerBet playerBet : bets) {
                    addPlayerBet(betsMap, gameSessionId, playerBet);
                    betIds.remove(playerBet.getId());
                }
            }
        }

        Map<Long, List<PlayerBet>> resultMap = new HashMap<>(betsMap.size());
        for (Map.Entry<Long, Map<Long, PlayerBet>> entry : betsMap.entrySet()) {
            List<PlayerBet> playerBets = new ArrayList<>(entry.getValue().values());
            Collections.sort(playerBets);
            resultMap.put(entry.getKey(), playerBets);
        }
        if (getLog().isDebugEnabled()) {//logging not founded betIds
            StringBuilder builder = new StringBuilder();
            boolean first = true;
            for (Map.Entry<Long, Set<Long>> entry : required.entrySet()) {
                if (!CollectionUtils.isEmpty(entry.getValue())) {
                    if (first) {
                        first = false;
                    } else {
                        builder.append("; ");
                    }
                    builder.append("gameSessionId=").append(entry.getKey()).append(" betIds=").append(entry.getValue());
                }
            }
            if (!first) {
                getLog().debug("getPlayerBets. Not found: " + builder);
            }
        }
        return resultMap;
    }

    private void addPlayerBet(Map<Long, Map<Long, PlayerBet>> betsMap, long gameSessionId, PlayerBet bet) {
        Map<Long, PlayerBet> map = betsMap.get(gameSessionId);
        if (map == null) {
            betsMap.put(gameSessionId, map = new HashMap<>());
        }
        map.put(bet.getId(), bet);
    }

    @Override
    public void updateBet(GameSession gameSession, PlayerBet playerBet, int gameStateId, String data,
                          String servletData, long bet, long win, long balance, byte[] archiveAdditionalData) {
        if (playerBet == null) {
            getLog().error("Cannot update bet, bet is null. gameSession=" + gameSession +
                    ", gameStateId=" + gameStateId + ", data=" + data + ", servletData=" + servletData +
                    ", bet=" + bet + ", win=" + win + ", balance=" + balance);
            throw new RuntimeException("Cannot update bet, current bet is null, gameSessionId=" + gameSession.getId());
        }
        playerBet.update(gameStateId, data, servletData, bet, win, balance, archiveAdditionalData);
        put(gameSession, playerBet);
    }

    public void prepareToPersistBet(HashMap<com.datastax.driver.core.Session, List<com.datastax.driver.core.Statement>> statementsMap, long gameSessionId, PlayerBet bet,
                                    List<ByteBuffer> byteBuffersCollector) {
        tempBetPersister.prepareToPersistBet(statementsMap, gameSessionId, bet,
                byteBuffersCollector);
    }

    public void persist(long gameSessionId, List<PlayerBet> bets) {
        if (CollectionUtils.isEmpty(bets)) {
            getLog().warn("persist: empty bets list, gameSessionid={}", gameSessionId);
            return;
        }
        Collections.sort(bets);
        ByteBuffer buffer;
        ByteBuffer uncompressed = FastKryoHelper.serializeToBytes(bets, betListSerializer, 512);
        if (TABLE.isClientCompression()) {
            try {
                buffer = LZ4Compressor.getInstance().compress(uncompressed);
                StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ": persist " + TABLE.getTableName() +
                        " uncompressed", uncompressed.limit());
                StatisticsManager.getInstance().updateRequestStatistics(getClass().getSimpleName() + ": serializeToBytes " + TABLE.getTableName() +
                        " compressed", buffer.limit());
            } finally {
                releaseBuffer(uncompressed);
            }
        } else {
            buffer = uncompressed;
        }
        try {
            String json = tempBetPersister.getMainTableDefinition().serializeToListJson(bets, PlayerBet.class);
            execute(addInsertion(gameSessionId, SERIALIZED_COLUMN_NAME, buffer).value(JSON_COLUMN_NAME, json), "persist list bets");
        } finally {
            releaseBuffer(buffer);
        }
    }

    private void put(GameSession gameSession, PlayerBet bet) {
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        transactionData.setLastBet(bet);
        transactionData.add(StoredItemType.PLAYER_BET, bet, new PlayerBetStoredInfo(gameSession.getId()));
    }

    public void delete(long gameSessionId) {
        super.deleteWithCheck(gameSessionId);
    }

    public void delete(long... gameSessionIds) {
        if (gameSessionIds.length == 0) {
            return;
        }
        com.datastax.driver.core.Statement query =
                com.datastax.driver.core.querybuilder.QueryBuilder.delete().
                        from(getMainColumnFamilyName()).
                        where(com.datastax.driver.core.querybuilder.QueryBuilder.in(GAME_SESSION_ID_FIELD, gameSessionIds));
        execute(query, "delete gameSessions");
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
