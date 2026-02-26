package com.dgphoenix.casino.promo.persisters;

import com.dgphoenix.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.dgphoenix.casino.cassandra.persist.engine.ColumnDefinition;
import com.dgphoenix.casino.cassandra.persist.engine.TableDefinition;
import com.dgphoenix.casino.promo.win.PromoWin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CassandraPromoWinPersister extends AbstractCassandraPersister<Long, Long> {
    private static final Logger LOG = LogManager.getLogger(CassandraPromoWinPersister.class);
    private static final String TABLE_NAME = "promoWinCF";
    private static final String PROMO_ID = "promoId";
    private static final String TIME_WIN = "timeWin";
    private static final String ACCOUNT_ID = "accountId";
    private static final String GAME_SESSION_ID = "gameSessionId";
    private static final String BANK_ID = "bankId";
    private static final String GAME_ID = "gameId";
    private static final String AMOUNT = "amount";
    private static final String AMOUNT_IN_PLAYER_CURRENCY = "amountInPlayerCurrency";
    private static final String TRANSFER_STATUS = "transferStatus";

    private static final TableDefinition TABLE = new TableDefinition(TABLE_NAME,
            Arrays.asList(
                    new ColumnDefinition(PROMO_ID, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(TIME_WIN, com.datastax.driver.core.DataType.bigint(), false, false, true),
                    new ColumnDefinition(ACCOUNT_ID, com.datastax.driver.core.DataType.bigint(), false, false, false),
                    new ColumnDefinition(GAME_SESSION_ID, com.datastax.driver.core.DataType.bigint(), false, false, false),
                    new ColumnDefinition(BANK_ID, com.datastax.driver.core.DataType.bigint(), false, false, false),
                    new ColumnDefinition(GAME_ID, com.datastax.driver.core.DataType.bigint(), false, false, false),
                    new ColumnDefinition(AMOUNT, com.datastax.driver.core.DataType.bigint(), false, false, false),
                    new ColumnDefinition(AMOUNT_IN_PLAYER_CURRENCY, com.datastax.driver.core.DataType.bigint(), false, false, false),
                    new ColumnDefinition(TRANSFER_STATUS, com.datastax.driver.core.DataType.text(), false, false, false)
            ), PROMO_ID
    );

    public void persist(PromoWin promoWin) {
        execute(getInsertQuery()
                .value(PROMO_ID, promoWin.getPromoId())
                .value(TIME_WIN, promoWin.getTimeWin())
                .value(ACCOUNT_ID, promoWin.getAccountId())
                .value(GAME_SESSION_ID, promoWin.getGameSessionId())
                .value(BANK_ID, promoWin.getBankId())
                .value(GAME_ID, promoWin.getGameId())
                .value(AMOUNT, promoWin.getAmount())
                .value(AMOUNT_IN_PLAYER_CURRENCY, promoWin.getAmountInPlayerCurrency())
                .value(TRANSFER_STATUS, promoWin.getTransferStatus()), "persist");
    }

    public Set<PromoWin> getByPromoId(long promoId) {
        com.datastax.driver.core.ResultSet result = execute(getSelectAllColumnsQuery().where(eq(PROMO_ID, promoId)), "getByPromoId");
        Set<PromoWin> wins = new HashSet<>();
        for (com.datastax.driver.core.Row row : result) {
            wins.add(getPromoWinEntry(row));
        }
        return wins;
    }

    public Set<PromoWin> getAllWins() {
        Iterator<com.datastax.driver.core.Row> rows = getAll();
        Set<PromoWin> wins = new HashSet<>();
        while (rows.hasNext()) {
            com.datastax.driver.core.Row row = rows.next();
            wins.add(getPromoWinEntry(row));
        }
        return wins;
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    private PromoWin getPromoWinEntry(com.datastax.driver.core.Row row) {
        long id = row.getLong(PROMO_ID);
        long timeWin = row.getLong(TIME_WIN);
        long accountId = row.getLong(ACCOUNT_ID);
        long gameSessionId = row.getLong(GAME_SESSION_ID);
        long bankId = row.getLong(BANK_ID);
        long gameId = row.getLong(GAME_ID);
        long amount = row.getLong(AMOUNT);
        long amountInPlayerCurrency = row.getLong(AMOUNT_IN_PLAYER_CURRENCY);
        String transferStatus = row.getString(TRANSFER_STATUS);
        return new PromoWin(id, timeWin, accountId, gameSessionId, bankId, gameId, amount, amountInPlayerCurrency, transferStatus);
    }
}
