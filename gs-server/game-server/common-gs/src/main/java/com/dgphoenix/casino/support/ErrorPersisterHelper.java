package com.dgphoenix.casino.support;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraHttpCallInfoPersister;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.payment.IWallet;
import com.dgphoenix.casino.common.transport.TObject;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringIdGenerator;
import com.dgphoenix.casino.common.util.support.AdditionalInfoAttribute;
import com.dgphoenix.casino.common.util.support.ExceptionInfo;
import com.dgphoenix.casino.common.util.support.HttpCallInfo;
import com.dgphoenix.casino.common.util.web.HttpRequestContextHolder;
import com.dgphoenix.casino.common.util.web.WebTools;
import com.dgphoenix.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.kafka.dto.RoundInfoResultDto;
import com.abs.casino.promo.tournaments.messages.Error;
import com.dgphoenix.casino.system.configuration.GameServerConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import static com.dgphoenix.casino.cassandra.persist.CassandraHttpCallInfoPersister.composeKey;
import static com.dgphoenix.casino.common.util.support.AdditionalInfoAttribute.*;
import static com.dgphoenix.casino.common.web.BaseAction.*;
import static java.lang.System.currentTimeMillis;
import static java.util.function.Function.identity;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 30.01.2020
 */
public class ErrorPersisterHelper {

    private static final Logger LOG = LogManager.getLogger(ErrorPersisterHelper.class);

    private final HttpRequestContextHolder httpRequestContext;
    private final GameServerConfiguration gameServerConfiguration;
    private final CassandraHttpCallInfoPersister httpCallInfoPersister;

    public ErrorPersisterHelper(GameServerConfiguration gameServerConfiguration, CassandraPersistenceManager persistenceManager) {
        httpRequestContext = HttpRequestContextHolder.getRequestContext();
        this.gameServerConfiguration = gameServerConfiguration;
        httpCallInfoPersister = persistenceManager.getPersister(CassandraHttpCallInfoPersister.class);
    }

    public void persistBaseServletError(String sessionId, IDBLink dbLink, Exception exception, long exceptionTime) {
        if (!httpRequestContext.isInitialized()) {
            logNotInitializedError("persistBaseServletError", sessionId);
            return;
        }

        HttpCallInfo httpCallInfo = httpRequestContext.getHttpCallInfo();
        setGameServerId(httpCallInfo);
        setExceptionInfo(httpCallInfo, exception, exceptionTime);

        httpCallInfo.addAdditionalInfo(ACCOUNT_ID, dbLink.getAccountId())
                .addAdditionalInfo(GAME_ID, dbLink.getGameId())
                .addAdditionalInfo(GAME_SESSION_ID, dbLink.getGameSessionId())
                .addAdditionalInfo(SESSION_ID, sessionId);
        Long walletTransactionId = dbLink.getWalletTransactionId();
        if (walletTransactionId != null) {
                httpCallInfo.addAdditionalInfo(TRANSACTION_ID, walletTransactionId);
        }
        Function<AccountInfo, String> toExternalId = accountInfo ->
                composeKey(dbLink.getBankId(), accountInfo.getExternalId());
        String roundId = getRoundId(dbLink, httpCallInfo);
        String token = getRequestParameterValue(httpCallInfo, TOKEN);
        String transactionId = getTransactionId(dbLink, httpCallInfo);

        addIfPresent(dbLink.getAccount(), toExternalId, httpCallInfo, EXTERNAL_ID);
        addIfPresent(dbLink.getWallet(), IWallet::toString, httpCallInfo, WALLET_STATE);
        addIfPresent(roundId, identity(), httpCallInfo, ROUND_ID);
        addIfPresent(token, identity(), httpCallInfo, TOKEN);
        addIfPresent(transactionId, identity(), httpCallInfo, TRANSACTION_ID);

        persist(httpCallInfo);
        logSuccessfulPersistInfo("baseServletError", sessionId);
    }

    public void persistAddWinError(String sessionId, long gameSessionId, long cents, long returnedBet, long roundId,
                                   long roomId, long accountId, long transactionId, RoundInfoResultDto roundInfoResult, Exception exception) {
        if (!httpRequestContext.isInitialized()) {
            logNotInitializedError("persistAddWinError", sessionId);
            return;
        }

        persistAddWinOrSitOutError(sessionId, gameSessionId, cents, returnedBet, roundId, roomId, accountId, transactionId, roundInfoResult, exception);
        logSuccessfulPersistInfo("addWinError", sessionId);
    }

    public void persistSitOutError(String sessionId, long gameSessionId, long cents, long returnedBet, long roundId,
                                   long roomId, long accountId, long transactionId, RoundInfoResultDto roundInfoResult, Exception exception) {
        if (!httpRequestContext.isInitialized()) {
            logNotInitializedError("persistSitOutError", sessionId);
            return;
        }

        persistAddWinOrSitOutError(sessionId, gameSessionId, cents, returnedBet, roundId, roomId, accountId, transactionId, roundInfoResult, exception);
        logSuccessfulPersistInfo("sitOutError", sessionId);
    }

    public void persistSitInError(String sessionId, long gameId, String mode, String lang, long cents, long bonusId,
                                  long oldGameSessionId, long oldRoundId, long roomId, int betNumber,
                                  long transactionId, Exception exception) {
        if (!httpRequestContext.isInitialized()) {
            logNotInitializedError("persistSitInError", sessionId);
            return;
        }

        HttpCallInfo httpCallInfo = httpRequestContext.getHttpCallInfo();
        addGenericServiceHandlerInfo(httpCallInfo, exception, sessionId);

        httpCallInfo.addAdditionalInfo(BET_NUMBER, betNumber)
                .addAdditionalInfo(BONUS_ID, bonusId)
                .addAdditionalInfo(CENTS, cents)
                .addAdditionalInfo(GAME_ID, gameId)
                .addAdditionalInfo(LANG, lang)
                .addAdditionalInfo(MODE, mode)
                .addAdditionalInfo(GAME_SESSION_ID, oldGameSessionId)
                .addAdditionalInfo(ROUND_ID, oldRoundId)
                .addAdditionalInfo(ROOM_ID, roomId)
                .addAdditionalInfo(SESSION_ID, sessionId);
        if (transactionId > 0) {
            httpCallInfo.addAdditionalInfo(TRANSACTION_ID, transactionId);
        }

        persist(httpCallInfo);
        logSuccessfulPersistInfo("sitInError", sessionId);
    }

    public void persistBuyInError(String sessionId, long cents, long gameSessionId, long roomId, int betNumber,
                                  Exception exception) {
        if (!httpRequestContext.isInitialized()) {
            logNotInitializedError("persistBuyInError", sessionId);
            return;
        }

        HttpCallInfo httpCallInfo = httpRequestContext.getHttpCallInfo();
        addGenericServiceHandlerInfo(httpCallInfo, exception, sessionId);

        httpCallInfo.addAdditionalInfo(BET_NUMBER, betNumber)
                .addAdditionalInfo(CENTS, cents)
                .addAdditionalInfo(GAME_SESSION_ID, gameSessionId)
                .addAdditionalInfo(ROOM_ID, roomId)
                .addAdditionalInfo(SESSION_ID, sessionId);

        persist(httpCallInfo);
        logSuccessfulPersistInfo("buyInError", sessionId);
    }

    public void persistGetDetailedPlayerInfoError(String sessionId, long gameId, String mode, Exception exception) {
        if (!httpRequestContext.isInitialized()) {
            logNotInitializedError("persistGetDetailedPlayerInfoError", sessionId);
            return;
        }

        HttpCallInfo httpCallInfo = httpRequestContext.getHttpCallInfo();
        addGenericServiceHandlerInfo(httpCallInfo, exception, sessionId);

        httpCallInfo.addAdditionalInfo(GAME_ID, gameId)
                .addAdditionalInfo(MODE, mode)
                .addAdditionalInfo(SESSION_ID, sessionId);

        persist(httpCallInfo);
        logSuccessfulPersistInfo("getDetailedPlayerInfoError", sessionId);
    }

    public void persistStartGameActionError(HttpServletRequest request, Exception exception, long exceptionTime) {
        String supportTicketId = String.valueOf(request.getAttribute(SUPPORT_TICKET_ID_ATTRIBUTE));
        if (!httpRequestContext.isInitialized()) {
            LOG.warn("httpRequestContext not initialized, force create. Request: {}", request);
            httpRequestContext.create();
        }

        HttpCallInfo httpCallInfo = httpRequestContext.getHttpCallInfo();
        httpCallInfo.addAdditionalInfo(SUPPORT_TICKET_ID, supportTicketId);

        setGameServerId(httpCallInfo);
        setExceptionInfo(httpCallInfo, exception, exceptionTime);

        addIfPresent(request, BANK_ID_ATTRIBUTE, httpCallInfo, BANK_ID);
        addIfPresent(request, PARAM_BONUS_ID, httpCallInfo, BONUS_ID);
        addIfPresent(request, GAME_ID_ATTRIBUTE, httpCallInfo, GAME_ID);
        addIfPresent(request, GAMEMODE_ATTRIBUTE, httpCallInfo, MODE);
        addIfPresent(request, LANG_ID_ATTRIBUTE, httpCallInfo, LANG);
        addIfPresent(request, SESSION_ID_ATTRIBUTE, httpCallInfo, SESSION_ID);
        addIfPresent(request, "sessionId", httpCallInfo, SESSION_ID);
        addIfPresent(request, TOKEN_ATTRIBUTE, httpCallInfo, TOKEN);

        persist(httpCallInfo);
        LOG.info("startGameActionError persisted for supportTicketId={}", supportTicketId);
    }

    public void persistTournamentLobbyActionError(String sessionId, String token, Integer bankId, Exception exception) {
        if (!httpRequestContext.isInitialized()) {
            logNotInitializedError("persistTournamentLobbyActionError", sessionId);
            return;
        }

        HttpCallInfo httpCallInfo = httpRequestContext.getHttpCallInfo();
        setGameServerId(httpCallInfo);
        setExceptionInfo(httpCallInfo, exception, System.currentTimeMillis());
        addExternalIdFromSessionId(httpCallInfo, sessionId);

        httpCallInfo.addAdditionalInfo(SESSION_ID, sessionId)
                .addAdditionalInfo(TOKEN, token);
        if (bankId != null) {
            httpCallInfo.addAdditionalInfo(BANK_ID, bankId.longValue());
        }

        persist(httpCallInfo);
        logSuccessfulPersistInfo("tournamentLobbyActionError", sessionId);
    }

    public void persistTournamentError(String sessionId, Error error, TObject message) {
        persistTournamentError(sessionId, error, message, null);
    }

    public void persistTournamentError(String sessionId, Error error, TObject message, Exception exception) {
        LOG.info("Start persisting tournament error for sessionId={}", sessionId);

        HttpCallInfo httpCallInfo = new HttpCallInfo(Thread.currentThread().getName());
        setGameServerId(httpCallInfo);
        setExceptionInfo(httpCallInfo, exception, System.currentTimeMillis());

        httpCallInfo.addAdditionalInfo(SESSION_ID, sessionId).
                addAdditionalInfo("errorTime", new Date(error.getDate()).toString()).
                addAdditionalInfo(error.getClassName(), error.toString()).
                addAdditionalInfo("messageTime", new Date(message.getDate()).toString()).
                addAdditionalInfo(message.getClassName(), message.toString());
        addExternalIdFromSessionId(httpCallInfo, sessionId);

        persist(httpCallInfo);
        logSuccessfulPersistInfo("tournamentError", sessionId);
    }

    private void persistAddWinOrSitOutError(String sessionId, long gameSessionId, long cents, long returnedBet,
                                            long roundId, long roomId, long accountId, long transactionId,
                                            RoundInfoResultDto roundInfoResult, Exception exception) {
        HttpCallInfo httpCallInfo = httpRequestContext.getHttpCallInfo();
        addGenericServiceHandlerInfo(httpCallInfo, exception, sessionId);

        httpCallInfo.addAdditionalInfo(ACCOUNT_ID, accountId)
                .addAdditionalInfo(CENTS, cents)
                .addAdditionalInfo(GAME_SESSION_ID, gameSessionId)
                .addAdditionalInfo(RETURNED_BET, returnedBet)
                .addAdditionalInfo(ROOM_ID, roomId)
                .addAdditionalInfo(ROUND_ID, roundId)
                .addAdditionalInfo(ROUND_INFO_RESULT, roundInfoResult.toString())
                .addAdditionalInfo(SESSION_ID, sessionId);
        if (transactionId > 0) {
            httpCallInfo.addAdditionalInfo(TRANSACTION_ID, transactionId);

        }
        persist(httpCallInfo);
    }

    private void persist(HttpCallInfo httpCallInfo) {
        try {
            httpCallInfoPersister.persist(httpCallInfo);
        } catch (Exception e) {
            LOG.error("persist: Could not persist httpCallInfo", e);
        }
    }

    private void addGenericServiceHandlerInfo(HttpCallInfo httpCallInfo, Exception exception, String sessionId) {
        setGameServerId(httpCallInfo);
        setExceptionInfo(httpCallInfo, exception, currentTimeMillis());
        addExternalIdFromSessionId(httpCallInfo, sessionId);
    }

    private void addIfPresent(HttpServletRequest request, String requestAttribute, HttpCallInfo httpCallInfo,
                              AdditionalInfoAttribute additionalInfoAttribute) {
        Optional.ofNullable(extractRequestParameterIgnoreCase(request, requestAttribute))
                .ifPresent(value -> httpCallInfo.addAdditionalInfo(additionalInfoAttribute, value));
    }

    private <T> void addIfPresent(T parameter, Function<T, String> mapper, HttpCallInfo httpCallInfo,
                                  AdditionalInfoAttribute attribute) {
        Optional.ofNullable(parameter)
                .map(mapper)
                .ifPresent(value -> httpCallInfo.addAdditionalInfo(attribute, value));
    }

    private String getRequestParameterValue(HttpCallInfo httpCallInfo, AdditionalInfoAttribute attribute) {
        boolean requestInitialized = httpCallInfo.getHttpMessage() != null && httpCallInfo.getHttpMessage().getRequest() != null;
        if (requestInitialized) {
            String request = httpCallInfo.getHttpMessage().getRequest().getRequest();
            return WebTools.getHttpRequestParameterValue(request, attribute.getAttributeName());
        }
        return null;
    }

    private String getRoundId(IDBLink dbLink, HttpCallInfo httpCallInfo) {
        return Optional.ofNullable(dbLink.getRoundId())
                .map(String::valueOf)
                .orElseGet(() -> getRequestParameterValue(httpCallInfo, ROUND_ID));
    }

    private String getTransactionId(IDBLink dbLink, HttpCallInfo httpCallInfo) {
        return Optional.ofNullable(dbLink.getWalletTransactionId())
                .map(String::valueOf)
                .orElseGet(() -> getRequestParameterValue(httpCallInfo, TRANSACTION_ID));
    }

    private void setGameServerId(HttpCallInfo httpCallInfo) {
        httpCallInfo.setGameServerId(gameServerConfiguration.getServerId());
    }

    private void setExceptionInfo(HttpCallInfo httpCallInfo, Exception exception, long exceptionTime) {
        if (httpCallInfo.getExceptionInfo() == null && exception != null) {
            ExceptionInfo exceptionInfo = new ExceptionInfo(exception, exceptionTime);
            httpCallInfo.setExceptionInfo(exceptionInfo);
        }
    }

    private void addExternalIdFromSessionId(HttpCallInfo httpCallInfo, String sessionId) {
        try {
            Pair<Integer, String> bankIdAndExternalId = StringIdGenerator.extractBankAndExternalUserId(sessionId);
            String externalId = composeKey(bankIdAndExternalId.getKey(), bankIdAndExternalId.getValue());
            httpCallInfo.addAdditionalInfo(EXTERNAL_ID, externalId);
        } catch (Exception e) {
            LOG.error("Could not extract externalId from sessionId={}, reason: {}", sessionId, e.getMessage());
        }
    }

    private void logNotInitializedError(String methodName, String sessionId) {
        LOG.error("{}: httpCallInfo not initialized for sessionId={}", methodName, sessionId);
    }

    private void logSuccessfulPersistInfo(String errorName, String sessionId) {
        LOG.info("{} persisted for sessionId={}", errorName, sessionId);
    }
}
