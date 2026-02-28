package com.abs.casino.support;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraGameSessionPersister;
import com.dgphoenix.casino.common.cache.data.bet.PlayerBet;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.StreamUtils;
import com.abs.casino.gs.biz.GameHistory;
import com.abs.casino.gs.biz.GameHistoryListEntry;
import com.abs.casino.gs.persistance.bet.PlayerBetPersistenceManager;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.ArrayUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by grien on 06.02.15.
 */
public class CassandraBetHistoryArchiver extends AbstractStorageArchiver<GameSessionHistory, Long> {

    private static final int REMOVE_BATCH_SIZE = 100;
    private static final int DEFAULT_PERIOD = 6;

    private final XStream xstream = new XStream();
    private final CassandraGameSessionPersister gameSessionPersister;
    private final PlayerBetPersistenceManager betPersistenceManager;

    public CassandraBetHistoryArchiver() {
        super();
        registerXmlAliases();
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        gameSessionPersister = persistenceManager.getPersister(CassandraGameSessionPersister.class);
        betPersistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("playerBetPersistenceManager", PlayerBetPersistenceManager.class);
    }

    private void registerXmlAliases() {
        xstream.alias(GameHistory.class.getSimpleName(), GameHistory.class);
        xstream.alias("Entry", GameHistoryListEntry.class);
        xstream.addImplicitCollection(GameHistory.class, "entries");
    }


    @Override
    public String getName() {
        return "cassandra_bet";
    }

    @Override
    protected String getColumnFamilyName() {
        return betPersistenceManager.getMainColumnFamilyName();
    }

    @Override
    protected int getDefaultStartPeriod() {
        return DEFAULT_PERIOD;
    }

    @Override
    protected Iterable<GameSessionHistory> getRecords(Date dayStartDate, Date dayEndDate) throws CommonException {
        return StreamUtils.asStream(gameSessionPersister.getRecordsByDay(dayStartDate))
                .map(this::getRecord)
                .collect(Collectors.toList());
    }

    protected GameSessionHistory getRecord(GameSession gameSession) {
        long gameSessionId = gameSession.getId();
        List<PlayerBet> bets = betPersistenceManager.getBets(gameSessionId, null, null, true);
        List<GameHistoryListEntry> listEntries = bets.stream()
                .map(GameHistoryListEntry::new)
                .collect(Collectors.toList());
        String data = xstream.toXML(new GameHistory(listEntries));
        return new GameSessionHistory(gameSessionId, gameSession.getGameId(), data);
    }

    @Override
    protected void remove(List<Long> needRemoveIdentifiers) {
        List<List<Long>> removeBatches = Lists.partition(needRemoveIdentifiers, REMOVE_BATCH_SIZE);
        for (List<Long> removeBatch : removeBatches) {
            long[] ids = ArrayUtils.toPrimitive(removeBatch.toArray(new Long[removeBatch.size()]));
            betPersistenceManager.delete(ids);
        }
    }

    @Override
    protected void addNeedRemoveIdentifier(GameSessionHistory record, List<Long> needRemoveIdentifiers) {
        needRemoveIdentifiers.add(record.getGameSessionId());
    }
}
