package com.abs.casino.cassandra.persist;

import com.abs.casino.cassandra.persist.engine.AbstractCassandraPersister;
import com.abs.casino.cassandra.persist.engine.ColumnDefinition;
import com.abs.casino.cassandra.persist.engine.Cql;
import com.abs.casino.cassandra.persist.engine.TableDefinition;
import com.abs.casino.cassandra.persist.engine.configuration.CompactionStrategy;
import com.abs.casino.cassandra.persist.engine.configuration.Compression;
import com.abs.casino.common.cache.data.payment.IWallet;
import com.abs.casino.common.cache.data.session.ClientType;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.gs.managers.payment.wallet.CommonGameWallet;
import com.abs.casino.gs.managers.payment.wallet.CommonWallet;
import com.abs.casino.gs.managers.payment.wallet.IWalletPersister;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.bigint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cboolean;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cdouble;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.cint;
import static com.abs.casino.cassandra.persist.engine.CassandraDataTypes.text;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.Select;
import com.abs.casino.cassandra.persist.engine.SchemaCql;






/**
 * User: flsh
 * Date: 6/28/12
 */
public class CassandraCommonGameWalletPersister extends AbstractCassandraPersister<String, String>
        implements IWalletPersister {
    private static final Logger LOG = LogManager.getLogger(CassandraCommonGameWalletPersister.class);
    public static final String COMMON_GAME_WALLET_CF = "CGW_CF";
    private static final String ACCOUNT_ID_FIELD = "accountId";
    private static final String GAME_ID_FIELD = "gameId";
    private static final String ROUND_ID_FIELD = "roundId";
    private static final String GAME_SESSION_ID_FIELD = "gameSessionId";
    private static final String WIN_AMOUNT_FIELD = "winAmount";
    private static final String BET_AMOUNT_FIELD = "betAmount";
    private static final String LAST_NEGATIVE_BET_FIELD = "lnBet";
    private static final String NEGATIVE_BET_FIELD = "nBet";
    private static final String NEW_ROUND_FIELD = "newRound";
    private static final String ROUND_FINISHED_FIELD = "finRound";
    private static final String ADDITIONAL_ROUND_INFO = "addRoundInfo";
    private static final String JP_CONTRIBUTION = "jpContribution";
    private static final String JP_WIN = "jpWin";
    private static final String TEMP_TOKEN = "tempToken";
    private static final String CLIENT_TYPE = "clientType";

    //ReadTimeouts under high load hint: alter table cgw_cf with speculative_retry = 'ALWAYS';
    private static final TableDefinition TABLE = new TableDefinition(COMMON_GAME_WALLET_CF,
            Arrays.asList(
                    //key is accountId_gameId
                    new ColumnDefinition(ACCOUNT_ID_FIELD, bigint(), false, false, true),
                    new ColumnDefinition(GAME_ID_FIELD, cint(), false, false, true),
                    new ColumnDefinition(ROUND_ID_FIELD, bigint(), false, false, false),
                    new ColumnDefinition(GAME_SESSION_ID_FIELD, bigint(), false, false, false),
                    new ColumnDefinition(WIN_AMOUNT_FIELD, bigint(), false, false, false),
                    new ColumnDefinition(BET_AMOUNT_FIELD, bigint(), false, false, false),
                    new ColumnDefinition(LAST_NEGATIVE_BET_FIELD, bigint(), false, false, false),
                    new ColumnDefinition(NEGATIVE_BET_FIELD, bigint(), false, false, false),
                    new ColumnDefinition(NEW_ROUND_FIELD, cboolean(), false, false, false),
                    new ColumnDefinition(ROUND_FINISHED_FIELD, cboolean(), false, false, false),
                    new ColumnDefinition(ADDITIONAL_ROUND_INFO, text(), false, false, false),
                    new ColumnDefinition(JP_CONTRIBUTION, cdouble(), false, false, false),
                    new ColumnDefinition(JP_WIN, bigint(), false, false, false),
                    new ColumnDefinition(TEMP_TOKEN, text(), false, false, false),
                    new ColumnDefinition(CLIENT_TYPE, text(), false, false, false)
            ), ACCOUNT_ID_FIELD)
            .compaction(CompactionStrategy.LEVELED)
            .gcGraceSeconds(TimeUnit.DAYS.toSeconds(1))
            .compression(Compression.NONE)
            .speculativeRetry(SchemaCql.always());

    private CassandraCommonGameWalletPersister() {
    }

    public void persistCommonWallet(CommonWallet wallet) {
        IWallet oldWallet = getWallet(wallet.getAccountId());
        Batch batch = Cql.batch();
        //first remove not existing in actual wallet
        Set<Integer> oldGameWallets = oldWallet.getWalletGamesIds();
        for (Integer gameId : oldGameWallets) {
            CommonGameWallet gameWallet = wallet.getGameWallet(gameId);
            if (gameWallet == null) {
                Delete query = Cql.delete().from(getMainColumnFamilyName());
                query.where(eq(ACCOUNT_ID_FIELD, wallet.getAccountId())).and(eq(GAME_ID_FIELD, gameId));
                batch.add(query);
            }
        }
        //second add actual
        Collection<CommonGameWallet> newGameWallets = wallet.getCommonGameWallets();
        for (CommonGameWallet gameWallet : newGameWallets) {
            batch.add(getInsertQuery(wallet.getAccountId(), gameWallet));
        }
        execute(batch, "persistCommonWallet");
    }

    private Insert getInsertQuery(long accountId, CommonGameWallet gameWallet) {
        ClientType clientType = gameWallet.getClientType();
        LOG.debug("createOrUpdate: accountId={}, gameId={}, roundId={}, bet={}, win={}, negativeBet={}, isRoundFinished={}, clientType={}",
                accountId, gameWallet.getGameId(), gameWallet.getRoundId(), gameWallet.getBetAmount(), gameWallet.getWinAmount(),
                gameWallet.getNegativeBet(), gameWallet.isRoundFinished(), clientType);
        Insert query = getInsertQuery();
        query.value(ACCOUNT_ID_FIELD, accountId);
        query.value(GAME_ID_FIELD, gameWallet.getGameId());
        query.value(ROUND_ID_FIELD, gameWallet.getRoundId() == null ? 0 : gameWallet.getRoundId());
        query.value(GAME_SESSION_ID_FIELD, gameWallet.getGameSessionId() == null ? 0 : gameWallet.getGameSessionId());
        query.value(WIN_AMOUNT_FIELD, gameWallet.getWinAmount());
        query.value(BET_AMOUNT_FIELD, gameWallet.getBetAmount());
        query.value(LAST_NEGATIVE_BET_FIELD, gameWallet.getLastNegativeBet() == null ? 0 :
                gameWallet.getLastNegativeBet());
        query.value(NEGATIVE_BET_FIELD, gameWallet.getNegativeBet());
        query.value(NEW_ROUND_FIELD, gameWallet.isNewRound());
        query.value(ROUND_FINISHED_FIELD, gameWallet.isRoundFinished());
        query.value(ADDITIONAL_ROUND_INFO, gameWallet.getAdditionalRoundInfo());
        query.value(JP_CONTRIBUTION, gameWallet.getJpContribution());
        query.value(JP_WIN, gameWallet.getJpWin());
        query.value(TEMP_TOKEN, gameWallet.getTempToken());
        query.value(CLIENT_TYPE, clientType == null ? null : clientType.toString());
        return query;
    }

    public void createOrUpdate(long accountId, CommonGameWallet gameWallet) {
        execute(getInsertQuery(accountId, gameWallet), "createOrUpdate");
    }

    @Override
    public TableDefinition getMainTableDefinition() {
        return TABLE;
    }

    public CommonGameWallet getById(long accountId, int gameId) {
        Select query = Cql.select().from(getMainColumnFamilyName());
        query.where().and(eq(ACCOUNT_ID_FIELD, accountId)).and(eq(GAME_ID_FIELD, gameId));
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "getById");
        com.abs.casino.cassandra.persist.engine.Row row = resultSet.one();
        return row == null ? null : extractFromResult(row);
    }

    @Override
    public IWallet getWallet(long accountId) {
        Select query = Cql.select().from(getMainColumnFamilyName());
        query.where().and(eq(ACCOUNT_ID_FIELD, accountId));
        com.abs.casino.cassandra.persist.engine.ResultSet resultSet = executeWrapped(query, "getWallet", 2);
        CommonWallet wallet = new CommonWallet(accountId);
        for (com.abs.casino.cassandra.persist.engine.Row row : resultSet) {
            CommonGameWallet gameWallet = extractFromResult(row);
            wallet.addGameWallet(gameWallet);
        }
        return wallet;
    }

    @Override
    public void removeWallet(long accountId) {
        LOG.debug("removeWallet: {}", accountId);
        Delete query = Cql.delete().from(getMainColumnFamilyName());
        query.where(eq(ACCOUNT_ID_FIELD, accountId));
        execute(query, "removeWallet");
    }

    @Override
    public void removeGameWallet(long accountId, int gameId) {
        LOG.debug("removeGameWallet: {}, gameId={}", accountId, gameId);
        Delete query = Cql.delete().from(getMainColumnFamilyName());
        query.where(eq(ACCOUNT_ID_FIELD, accountId)).and(eq(GAME_ID_FIELD, gameId));
        execute(query, "removeGameWallet");
    }

    private CommonGameWallet extractFromResult(com.abs.casino.cassandra.persist.engine.Row result) {
        long roundId = result.getLong(ROUND_ID_FIELD);
        long gameSessionId = result.getLong(GAME_SESSION_ID_FIELD);
        long winAmount = result.getLong(WIN_AMOUNT_FIELD);
        long betAmount = result.getLong(BET_AMOUNT_FIELD);
        long lastNegativeBet = result.getLong(LAST_NEGATIVE_BET_FIELD);
        long negativeBet = result.getLong(NEGATIVE_BET_FIELD);
        int gameId = result.getInt(GAME_ID_FIELD);
        boolean newRound = result.getBool(NEW_ROUND_FIELD);
        boolean roundFinished = result.getBool(ROUND_FINISHED_FIELD);
        String additionalRoundInfo = result.getString(ADDITIONAL_ROUND_INFO);
        double jpContribution = result.getDouble(JP_CONTRIBUTION);
        long jpWin = result.getLong(JP_WIN);
        String tempToken = result.getString(TEMP_TOKEN);
        String strClientType = result.getString(CLIENT_TYPE);
        CommonGameWallet commonGameWallet = new CommonGameWallet(gameId, gameSessionId);
        commonGameWallet.setRoundId(roundId == 0 ? null : roundId);
        commonGameWallet.setWinAmount(winAmount);
        commonGameWallet.setBetAmount(betAmount);
        commonGameWallet.setLastNegativeBet(lastNegativeBet == 0 ? null : lastNegativeBet);
        commonGameWallet.setNegativeBet(negativeBet);
        commonGameWallet.setNewRound(newRound);
        commonGameWallet.setRoundFinished(roundFinished);
        commonGameWallet.setTempToken(tempToken);
        commonGameWallet.setAdditionalRoundInfo(additionalRoundInfo);
        commonGameWallet.setJpContribution(jpContribution);
        commonGameWallet.setJpWin(jpWin);
        commonGameWallet.setClientType(StringUtils.isTrimmedEmpty(strClientType) ? null : ClientType.valueOf(strClientType));
        return commonGameWallet;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
