package com.abs.casino.web.api.newgames;

import com.dgphoenix.casino.account.AccountManager;
import com.abs.casino.actions.enter.game.routing.GameplayOrchestratorRoutingBridge;
import com.abs.casino.actions.enter.game.routing.HistoryServiceRoutingBridge;
import com.abs.casino.actions.enter.game.routing.ProtocolAdapterRoutingBridge;
import com.abs.casino.actions.enter.game.routing.WalletAdapterRoutingBridge;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationStatus;
import com.dgphoenix.casino.common.cache.data.payment.WalletOperationType;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.WalletException;
import com.dgphoenix.casino.common.util.DigitFormatter;
import com.dgphoenix.casino.common.util.IdGenerator;
import com.dgphoenix.casino.common.util.NumberUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonGameWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWallet;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletWagerResult;
import com.dgphoenix.casino.gs.managers.payment.wallet.IWalletProtocolManager;
import com.dgphoenix.casino.gs.managers.payment.wallet.WalletProtocolFactory;
import com.dgphoenix.casino.gs.managers.payment.wallet.v2.ICommonWalletClient;
import com.dgphoenix.casino.gs.persistance.PlayerSessionPersister;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Internal API used by new-games runtime.
 * This servlet intentionally serves non-.do endpoints.
 */
public class NewGamesInternalApiServlet extends HttpServlet {
    private static final Logger LOG = LogManager.getLogger(NewGamesInternalApiServlet.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String CONTRACT_VERSION = "v1";
    private static final String PATH_SESSION_VALIDATE = "/session/validate";
    private static final String PATH_WALLET_RESERVE = "/wallet/reserve";
    private static final String PATH_WALLET_SETTLE = "/wallet/settle";
    private static final String PATH_HISTORY_WRITE = "/history/write";
    private static final int DEFAULT_GAME_ID = 10;

    private static final String WALLET_GATEWAY_ERROR = "WALLET_GATEWAY_ERROR";
    private static final String WALLET_RESERVE_REJECTED_PREFIX = "WALLET_RESERVE_REJECTED:";
    private static final String WALLET_SETTLE_REJECTED_PREFIX = "WALLET_SETTLE_REJECTED:";

    private static final ConcurrentMap<String, Long> lastRequestCounterBySession = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, ReserveState> reserveByWalletOperationId = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, ReserveState> reserveByIdempotencyKey = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String> settleByIdempotencyKey = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Long> syntheticGameSessionIdBySession = new ConcurrentHashMap<>();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");

        String contractVersion = request.getHeader("X-NGS-Contract");
        if (!StringUtils.isTrimmedEmpty(contractVersion) && !CONTRACT_VERSION.equals(contractVersion)) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "UNSUPPORTED_CONTRACT",
                    "Unsupported contract version: " + contractVersion, false, request, null);
            return;
        }

        String path = normalizePath(request.getPathInfo());
        try {
            if (PATH_SESSION_VALIDATE.equals(path)) {
                handleSessionValidate(request, response);
                return;
            }
            if (PATH_WALLET_RESERVE.equals(path)) {
                handleWalletReserve(request, response);
                return;
            }
            if (PATH_WALLET_SETTLE.equals(path)) {
                handleWalletSettle(request, response);
                return;
            }
            if (PATH_HISTORY_WRITE.equals(path)) {
                handleHistoryWrite(request, response);
                return;
            }
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "UNKNOWN_ENDPOINT",
                    "Unknown New Games internal endpoint: " + path, false, request, null);
        } catch (CommonException e) {
            LOG.warn("NewGamesInternalApiServlet common error: {}", e.getMessage());
            writeMappedCommonError(response, request, e);
        } catch (Exception e) {
            LOG.error("NewGamesInternalApiServlet internal error", e);
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                    "Unexpected GS internal API error", true, request, null);
        }
    }

    private void handleSessionValidate(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SessionValidateRequest body = readRequestBody(request, SessionValidateRequest.class);
        requireNotEmpty(body.sessionId, "sessionId is required");

        SessionContext context = executeForSession(body.sessionId, false, (sessionInfo, accountInfo) -> {
            if (body.bankId != null && body.bankId != accountInfo.getBankId()) {
                throw new CommonException("bankId does not match session account");
            }
            if (!StringUtils.isTrimmedEmpty(body.playerId)) {
                String playerId = body.playerId.trim();
                boolean isExternalMatch = playerId.equals(accountInfo.getExternalId());
                boolean isInternalMatch = playerId.equals(String.valueOf(accountInfo.getId()));
                if (!isExternalMatch && !isInternalMatch) {
                    throw new CommonException("playerId does not match session account");
                }
            }
            return new SessionContext(sessionInfo, accountInfo);
        });

        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("sessionId", context.sessionInfo.getSessionId());
        result.put("playerId", context.accountInfo.getExternalId());
        result.put("bankId", context.accountInfo.getBankId());
        result.put("balance", getCurrentBalance(context.accountInfo));
        result.put("currency", toCurrencyPayload(context.accountInfo.getCurrency()));
        writeJson(response, HttpServletResponse.SC_OK, result);
    }

    private void handleWalletReserve(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WalletReserveRequest body = readRequestBody(request, WalletReserveRequest.class);
        requireNotEmpty(body.sessionId, "sessionId is required");
        requireNotEmpty(body.roundId, "roundId is required");
        requireNotNull(body.requestCounter, "requestCounter is required");
        requireNotNull(body.betAmount, "betAmount is required");
        if (body.betAmount <= 0) {
            throw new CommonException("betAmount must be positive");
        }

        int gameId = body.gameId != null ? body.gameId : DEFAULT_GAME_ID;
        if (gameId <= 0) {
            throw new CommonException("gameId must be positive");
        }

        String idempotencyKey = resolveIdempotencyKey(request, body.sessionId, body.clientOperationId);
        if (idempotencyKey != null) {
            ReserveState existing = reserveByIdempotencyKey.get(idempotencyKey);
            if (existing != null) {
                if (!existing.roundId.equals(body.roundId) || existing.betAmount != body.betAmount.longValue()) {
                    writeError(response, HttpServletResponse.SC_CONFLICT, "IDEMPOTENCY_KEY_REUSE",
                            "Idempotency key was already used with different payload", false, request, null);
                    return;
                }
                writeJson(response, HttpServletResponse.SC_OK,
                        createReserveResponse(existing.walletOperationId, existing.balanceAfterReserve));
                return;
            }
        }

        SessionContext context = executeForSession(body.sessionId, true, (sessionInfo, accountInfo) -> {
            validateRequestCounter(body.sessionId, body.requestCounter);

            long gameSessionId = resolveGameSessionId(body.sessionId, sessionInfo);
            long roundNumericId = toNumericRoundId(body.roundId);
            long operationId = generateUniqueOperationId();
            String walletOperationId = "ngs-" + operationId;

            WalletWagerOutcome outcome = executeWalletWager(sessionInfo, accountInfo, gameId, gameSessionId,
                    roundNumericId, operationId, body.betAmount, 0L, WalletCallType.RESERVE);

            ReserveState state = new ReserveState(walletOperationId, body.sessionId, body.roundId, body.betAmount,
                    gameId, gameSessionId, roundNumericId, outcome.balanceAfterOperation);
            reserveByWalletOperationId.put(walletOperationId, state);
            if (idempotencyKey != null) {
                reserveByIdempotencyKey.put(idempotencyKey, state);
            }
            lastRequestCounterBySession.put(body.sessionId, body.requestCounter.longValue());
            return new SessionContext(sessionInfo, accountInfo, state);
        });

        shadowProtocolWalletNormalize(context.accountInfo.getBankId(), body.sessionId, body.roundId,
                context.reserveState.walletOperationId, body.betAmount, WalletCallType.RESERVE, request);
        shadowWalletAdapterOperation(context.accountInfo.getBankId(), body.sessionId, body.roundId,
                String.valueOf(context.accountInfo.getId()), context.reserveState.walletOperationId,
                context.accountInfo.getCurrency() != null ? context.accountInfo.getCurrency().getCode() : "USD",
                body.betAmount, "reserve", request);
        shadowGameplayFinancialIntent(context.accountInfo.getBankId(), body.sessionId, context.reserveState.gameId,
                context.reserveState.walletOperationId, body.roundId, body.betAmount, WalletCallType.RESERVE,
                context.accountInfo.getCurrency() != null ? context.accountInfo.getCurrency().getCode() : "USD",
                request);

        writeJson(response, HttpServletResponse.SC_OK, createReserveResponse(
                context.reserveState.walletOperationId, getCurrentBalance(context.accountInfo)));
    }

    private void handleWalletSettle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        WalletSettleRequest body = readRequestBody(request, WalletSettleRequest.class);
        requireNotEmpty(body.sessionId, "sessionId is required");
        requireNotEmpty(body.roundId, "roundId is required");
        requireNotEmpty(body.walletOperationId, "walletOperationId is required");
        requireNotNull(body.requestCounter, "requestCounter is required");
        requireNotNull(body.winAmount, "winAmount is required");
        if (body.winAmount < 0) {
            throw new CommonException("winAmount must be >= 0");
        }

        String idempotencyKey = resolveIdempotencyKey(request, body.sessionId, body.clientOperationId);
        if (idempotencyKey != null) {
            String existingWalletOperationId = settleByIdempotencyKey.get(idempotencyKey);
            if (existingWalletOperationId != null) {
                if (!existingWalletOperationId.equals(body.walletOperationId)) {
                    writeError(response, HttpServletResponse.SC_CONFLICT, "IDEMPOTENCY_KEY_REUSE",
                            "Idempotency key was already used with different walletOperationId", false, request, null);
                    return;
                }
                ReserveState state = reserveByWalletOperationId.get(existingWalletOperationId);
                Long balance = state != null ? state.balanceAfterSettle : null;
                writeJson(response, HttpServletResponse.SC_OK, createSettleResponse(balance));
                return;
            }
        }

        SessionContext context = executeForSession(body.sessionId, true, (sessionInfo, accountInfo) -> {
            validateRequestCounter(body.sessionId, body.requestCounter);
            ReserveState reserveState = reserveByWalletOperationId.get(body.walletOperationId);
            if (reserveState == null) {
                throw new CommonException("walletOperationId not found");
            }
            if (!reserveState.sessionId.equals(body.sessionId)) {
                throw new CommonException("walletOperationId does not belong to this session");
            }
            if (!reserveState.roundId.equals(body.roundId)) {
                throw new CommonException("roundId does not match reserved operation");
            }
            if (reserveState.settled) {
                if (idempotencyKey != null) {
                    settleByIdempotencyKey.put(idempotencyKey, body.walletOperationId);
                }
                return new SessionContext(sessionInfo, accountInfo, reserveState);
            }

            int gameId = body.gameId != null ? body.gameId : reserveState.gameId;
            long operationId = extractOperationId(body.walletOperationId);
            WalletWagerOutcome outcome = executeWalletWager(sessionInfo, accountInfo, gameId, reserveState.gameSessionId,
                    reserveState.roundNumericId, operationId, 0L, body.winAmount, WalletCallType.SETTLE);

            reserveState.settled = true;
            reserveState.winAmount = body.winAmount;
            reserveState.settleRoundId = body.roundId;
            reserveState.balanceAfterSettle = outcome.balanceAfterOperation;

            lastRequestCounterBySession.put(body.sessionId, body.requestCounter.longValue());
            if (idempotencyKey != null) {
                settleByIdempotencyKey.put(idempotencyKey, body.walletOperationId);
            }
            return new SessionContext(sessionInfo, accountInfo, reserveState);
        });

        shadowProtocolWalletNormalize(context.accountInfo.getBankId(), body.sessionId, body.roundId,
                body.walletOperationId, body.winAmount, WalletCallType.SETTLE, request);
        shadowWalletAdapterOperation(context.accountInfo.getBankId(), body.sessionId, body.roundId,
                String.valueOf(context.accountInfo.getId()), body.walletOperationId,
                context.accountInfo.getCurrency() != null ? context.accountInfo.getCurrency().getCode() : "USD",
                body.winAmount, "settle", request);
        shadowGameplayFinancialIntent(context.accountInfo.getBankId(), body.sessionId, context.reserveState.gameId,
                body.walletOperationId, body.roundId, body.winAmount, WalletCallType.SETTLE,
                context.accountInfo.getCurrency() != null ? context.accountInfo.getCurrency().getCode() : "USD",
                request);

        writeJson(response, HttpServletResponse.SC_OK, createSettleResponse(getCurrentBalance(context.accountInfo)));
    }

    private void handleHistoryWrite(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HistoryWriteRequest body = readRequestBody(request, HistoryWriteRequest.class);
        requireNotEmpty(body.sessionId, "sessionId is required");
        requireNotEmpty(body.roundId, "roundId is required");
        requireNotEmpty(body.eventType, "eventType is required");

        SessionContext context = executeForSession(body.sessionId, false, (sessionInfo, accountInfo) -> {
            LOG.info("NGS_HISTORY_WRITE sid={}, accountId={}, roundId={}, eventType={}, data={}",
                    body.sessionId, accountInfo.getId(), body.roundId, body.eventType, body.data);
            return new SessionContext(sessionInfo, accountInfo);
        });

        shadowHistoryWrite(context.accountInfo.getBankId(), body.sessionId, body.roundId, body.eventType, request);

        writeJson(response, HttpServletResponse.SC_OK, Collections.singletonMap("ok", true));
    }

    private WalletWagerOutcome executeWalletWager(SessionInfo sessionInfo, AccountInfo accountInfo, long gameId,
                                                  long gameSessionId, long roundNumericId, long operationId,
                                                  long betAmount, long winAmount, WalletCallType callType)
            throws CommonException {
        IWalletProtocolManager protocolManager;
        try {
            protocolManager = WalletProtocolFactory.getInstance().getWalletProtocolManager(accountInfo.getBankId());
        } catch (WalletException e) {
            throw mapWalletException(e, callType);
        }
        if (protocolManager == null || protocolManager.getClient() == null) {
            throw new CommonException(WALLET_GATEWAY_ERROR + ":wallet client is not configured");
        }

        ICommonWalletClient walletClient = protocolManager.getClient();
        BankInfo bankInfo = protocolManager.getBankInfo();

        CommonWallet wallet = new CommonWallet(accountInfo.getId());
        wallet.setServerBalance(getCurrentBalance(accountInfo));
        CommonGameWallet gameWallet = wallet.createGameWallet((int) gameId, gameSessionId, sessionInfo.getClientType());
        gameWallet.setRoundId(roundNumericId);

        long operationAmount = callType == WalletCallType.RESERVE ? betAmount : winAmount;
        WalletOperationType operationType = callType == WalletCallType.RESERVE ?
                WalletOperationType.DEBIT : WalletOperationType.CREDIT;

        CommonWalletOperation operation = new CommonWalletOperation(operationId, accountInfo.getId(), gameSessionId,
                roundNumericId, operationAmount, operationType, WalletOperationStatus.STARTED,
                WalletOperationStatus.STARTED, System.currentTimeMillis(), 0L,
                "NGS internal API " + callType.name().toLowerCase(), null, null,
                0L, null, -1L, -1L);
        operation.setExternalSessionId(sessionInfo.getExternalSessionId());

        String bet = callType == WalletCallType.RESERVE ? prepareWagerAmount(bankInfo, betAmount, operationId) : "";
        String win = callType == WalletCallType.SETTLE ? prepareWagerAmount(bankInfo, winAmount, operationId) : "";
        Boolean isRoundFinished = callType == WalletCallType.SETTLE ? Boolean.TRUE : Boolean.FALSE;

        CommonWalletWagerResult result;
        try {
            result = walletClient.wager(accountInfo.getId(), accountInfo.getExternalId(), bet, win, isRoundFinished,
                    roundNumericId, roundNumericId, gameId, accountInfo.getBankId(), operation, wallet,
                    sessionInfo.getClientType(), accountInfo.getCurrency());
        } catch (WalletException e) {
            throw mapWalletException(e, callType);
        } catch (CommonException e) {
            throw new CommonException(WALLET_GATEWAY_ERROR + ":" + e.getMessage(), e);
        }

        if (!result.isSuccess()) {
            throw mapWagerResultError(result, callType);
        }

        long resolvedBalance = resolveWalletBalance(bankInfo, result.getBalance());
        applyWalletBalance(accountInfo, resolvedBalance);
        return new WalletWagerOutcome(resolvedBalance, result.getExtSystemTransactionId(), result.getResponseCode());
    }

    private CommonException mapWalletException(WalletException exception, WalletCallType callType) {
        Integer numericCode = exception.tryToGetNumericErrorCode();
        String walletCode = numericCode != null ? String.valueOf(numericCode) : exception.getErrorCode();
        if (!StringUtils.isTrimmedEmpty(walletCode)) {
            String prefix = callType == WalletCallType.RESERVE ? WALLET_RESERVE_REJECTED_PREFIX :
                    WALLET_SETTLE_REJECTED_PREFIX;
            return new CommonException(prefix + walletCode + ":" + exception.getMessage(), exception);
        }
        return new CommonException(WALLET_GATEWAY_ERROR + ":" + exception.getMessage(), exception);
    }

    private CommonException mapWagerResultError(CommonWalletWagerResult result, WalletCallType callType) {
        String walletCode = result.getResponseCode();
        if (StringUtils.isTrimmedEmpty(walletCode)) {
            walletCode = "UNKNOWN";
        }
        String walletMessage = result.getErrorMessage();
        if (StringUtils.isTrimmedEmpty(walletMessage)) {
            walletMessage = "wallet rejected request";
        }
        String prefix = callType == WalletCallType.RESERVE ? WALLET_RESERVE_REJECTED_PREFIX :
                WALLET_SETTLE_REJECTED_PREFIX;
        return new CommonException(prefix + walletCode + ":" + walletMessage);
    }

    private long resolveWalletBalance(BankInfo bankInfo, double walletBalance) {
        if (bankInfo != null && bankInfo.isParseLong()) {
            return (long) walletBalance;
        }
        return DigitFormatter.getCentsFromCurrency(walletBalance);
    }

    private String prepareWagerAmount(BankInfo bankInfo, long amount, long operationId) {
        String amountPart = bankInfo != null && bankInfo.isCWSendAmountInDollars() ?
                String.valueOf(NumberUtils.asMoney(amount / 100d)) : String.valueOf(amount);
        return amountPart + "|" + operationId;
    }

    private long resolveGameSessionId(String sessionId, SessionInfo sessionInfo) {
        Long sessionGameSessionId = sessionInfo.getGameSessionId();
        if (sessionGameSessionId != null && sessionGameSessionId > 0) {
            return sessionGameSessionId;
        }
        return syntheticGameSessionIdBySession.computeIfAbsent(sessionId,
                ignored -> IdGenerator.getInstance().getNext(GameSession.class));
    }

    private long toNumericRoundId(String roundId) {
        try {
            BigInteger numeric = new BigInteger(roundId, 16);
            long value = numeric.mod(BigInteger.valueOf(Long.MAX_VALUE)).longValue();
            return value == 0 ? 1L : value;
        } catch (Exception e) {
            long fallback = Integer.toUnsignedLong(roundId.hashCode());
            return fallback == 0 ? 1L : fallback;
        }
    }

    private long extractOperationId(String walletOperationId) {
        if (!StringUtils.isTrimmedEmpty(walletOperationId) && walletOperationId.startsWith("ngs-")) {
            try {
                return Long.parseLong(walletOperationId.substring(4));
            } catch (NumberFormatException ignored) {
                // fallback below
            }
        }
        return generateUniqueOperationId();
    }

    private long generateUniqueOperationId() {
        long timestampPart = System.currentTimeMillis() * 1000L;
        long randomPart = Math.abs(UUID.randomUUID().getLeastSignificantBits() % 1000L);
        long operationId = timestampPart + randomPart;
        if (operationId > 0) {
            return operationId;
        }
        long fallback = Math.abs(UUID.randomUUID().getMostSignificantBits());
        return fallback == 0 ? 1L : fallback;
    }

    private <T> T executeForSession(String sessionId, boolean commit, SessionExecutor<T> executor) throws Exception {
        SessionHelper.getInstance().lock(sessionId);
        try {
            SessionHelper.getInstance().openSession();
            SessionInfo sessionInfo = PlayerSessionPersister.getInstance().getSessionInfo();
            if (sessionInfo == null) {
                throw new CommonException("Session not found");
            }
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(sessionInfo.getAccountId());
            if (accountInfo == null) {
                throw new CommonException("Account not found");
            }
            T result = executor.execute(sessionInfo, accountInfo);
            if (commit) {
                SessionHelper.getInstance().commitTransaction();
            }
            SessionHelper.getInstance().markTransactionCompleted();
            return result;
        } finally {
            SessionHelper.getInstance().clearWithUnlock();
        }
    }

    private void validateRequestCounter(String sessionId, Integer requestCounter) throws CommonException {
        long lastSeenCounter = lastRequestCounterBySession.getOrDefault(sessionId, 0L);
        long expectedCounter = lastSeenCounter + 1;
        if (requestCounter == null || requestCounter.longValue() != expectedCounter) {
            throw new CommonException("INVALID_REQUEST_COUNTER expected=" + expectedCounter + " got=" + requestCounter);
        }
    }

    private void applyWalletBalance(AccountInfo accountInfo, long balance) throws CommonException {
        if (accountInfo.isGuest()) {
            accountInfo.setFreeBalance(balance);
            return;
        }
        accountInfo.setBalance(balance);
    }

    private void shadowProtocolWalletNormalize(long bankId,
                                               String sessionId,
                                               String roundId,
                                               String walletOperationId,
                                               Long amount,
                                               WalletCallType callType,
                                               HttpServletRequest request) {
        try {
            ProtocolAdapterRoutingBridge.RouteDecision routeDecision =
                    ProtocolAdapterRoutingBridge.getInstance().decide(bankId);
            ProtocolAdapterRoutingBridge.getInstance().shadowNormalizeWalletOperation(
                    routeDecision,
                    bankId,
                    sessionId,
                    roundId,
                    walletOperationId,
                    amount == null ? 0L : amount.longValue(),
                    callType == WalletCallType.RESERVE ? "reserve" : "settle",
                    resolveTraceId(request)
            );
        } catch (Exception e) {
            LOG.warn("NGS protocol-adapter wallet shadow failed (ignored): bankId={}, callType={}, reason={}",
                    bankId, callType, e.getMessage());
        }
    }

    private void shadowWalletAdapterOperation(long bankId,
                                              String sessionId,
                                              String roundId,
                                              String accountId,
                                              String walletOperationId,
                                              String currencyCode,
                                              Long amount,
                                              String operationType,
                                              HttpServletRequest request) {
        try {
            WalletAdapterRoutingBridge.RouteDecision routeDecision =
                    WalletAdapterRoutingBridge.getInstance().decide(bankId);
            WalletAdapterRoutingBridge.getInstance().shadowWalletOperation(
                    routeDecision,
                    bankId,
                    sessionId,
                    roundId,
                    accountId,
                    walletOperationId,
                    currencyCode,
                    amount == null ? 0L : amount.longValue(),
                    operationType,
                    resolveTraceId(request)
            );
        } catch (Exception e) {
            LOG.warn("NGS wallet-adapter shadow failed (ignored): bankId={}, operationType={}, reason={}",
                    bankId, operationType, e.getMessage());
        }
    }

    private void shadowGameplayFinancialIntent(long bankId,
                                               String sessionId,
                                               int gameId,
                                               String walletOperationId,
                                               String roundId,
                                               Long amount,
                                               WalletCallType callType,
                                               String currencyCode,
                                               HttpServletRequest request) {
        try {
            GameplayOrchestratorRoutingBridge.RouteDecision routeDecision =
                    GameplayOrchestratorRoutingBridge.getInstance().decide(bankId, false);
            if (callType == WalletCallType.RESERVE) {
                GameplayOrchestratorRoutingBridge.getInstance().shadowWagerIntent(
                        routeDecision,
                        bankId,
                        sessionId,
                        gameId,
                        walletOperationId,
                        roundId,
                        currencyCode,
                        amount == null ? 0L : amount.longValue(),
                        resolveTraceId(request)
                );
            } else {
                GameplayOrchestratorRoutingBridge.getInstance().shadowSettleIntent(
                        routeDecision,
                        bankId,
                        sessionId,
                        gameId,
                        walletOperationId,
                        roundId,
                        currencyCode,
                        amount == null ? 0L : amount.longValue(),
                        resolveTraceId(request)
                );
            }
        } catch (Exception e) {
            LOG.warn("NGS gameplay-orchestrator shadow failed (ignored): bankId={}, callType={}, reason={}",
                    bankId, callType, e.getMessage());
        }
    }

    private void shadowHistoryWrite(long bankId,
                                    String sessionId,
                                    String roundId,
                                    String eventType,
                                    HttpServletRequest request) {
        try {
            HistoryServiceRoutingBridge.RouteDecision routeDecision =
                    HistoryServiceRoutingBridge.getInstance().decide(bankId);
            String operationId = "ngs-history-" + sessionId + "-" + roundId + "-" + eventType;
            HistoryServiceRoutingBridge.getInstance().shadowAppendRecord(
                    routeDecision,
                    bankId,
                    sessionId,
                    roundId,
                    eventType,
                    operationId,
                    resolveTraceId(request)
            );
        } catch (Exception e) {
            LOG.warn("NGS history-service shadow failed (ignored): bankId={}, eventType={}, reason={}",
                    bankId, eventType, e.getMessage());
        }
    }

    private long getCurrentBalance(AccountInfo accountInfo) {
        return accountInfo.isGuest() ? accountInfo.getFreeBalance() : accountInfo.getBalance();
    }

    private Map<String, Object> createReserveResponse(String walletOperationId, Long balance) {
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("walletOperationId", walletOperationId);
        if (balance != null) {
            result.put("balance", balance);
        }
        return result;
    }

    private Map<String, Object> createSettleResponse(Long balance) {
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        if (balance != null) {
            result.put("balance", balance);
        }
        return result;
    }

    private Map<String, Object> toCurrencyPayload(Currency currency) {
        Map<String, Object> payload = new HashMap<>();
        String symbol = currency != null ? currency.getSymbol() : "$";
        payload.put("code", currency != null ? currency.getCode() : "USD");
        payload.put("prefix", symbol == null ? "$" : symbol);
        payload.put("suffix", "");
        payload.put("grouping", ",");
        payload.put("decimal", ".");
        payload.put("precision", 1);
        payload.put("denomination", 1);
        return payload;
    }

    private String resolveIdempotencyKey(HttpServletRequest request, String sessionId, String clientOperationId) {
        String headerKey = request.getHeader("X-Idempotency-Key");
        String key = !StringUtils.isTrimmedEmpty(headerKey) ? headerKey : clientOperationId;
        if (StringUtils.isTrimmedEmpty(key)) {
            return null;
        }
        return sessionId + ":" + key.trim();
    }

    private String normalizePath(String pathInfo) {
        if (pathInfo == null || pathInfo.isEmpty()) {
            return "";
        }
        String path = pathInfo.trim();
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private <T> T readRequestBody(HttpServletRequest request, Class<T> requestClass) throws IOException {
        return MAPPER.readValue(request.getInputStream(), requestClass);
    }

    private void requireNotEmpty(String value, String message) throws CommonException {
        if (StringUtils.isTrimmedEmpty(value)) {
            throw new CommonException(message);
        }
    }

    private void requireNotNull(Object value, String message) throws CommonException {
        if (value == null) {
            throw new CommonException(message);
        }
    }

    private void writeJson(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        MAPPER.writeValue(response.getWriter(), body);
    }

    private void writeError(HttpServletResponse response, int status, String code, String message, boolean retryable,
                            HttpServletRequest request, Map<String, Object> details) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("message", message);
        error.put("retryable", retryable);
        error.put("traceId", resolveTraceId(request));
        error.put("details", details == null ? Collections.emptyMap() : details);

        Map<String, Object> envelope = new HashMap<>();
        envelope.put("error", error);
        writeJson(response, status, envelope);
    }

    private void writeMappedCommonError(HttpServletResponse response, HttpServletRequest request,
                                        CommonException exception) throws IOException {
        String message = exception.getMessage();
        if (StringUtils.isTrimmedEmpty(message)) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST",
                    "Request validation failed", false, request, null);
            return;
        }

        if (message.startsWith("INVALID_REQUEST_COUNTER")) {
            writeError(response, HttpServletResponse.SC_CONFLICT, "INVALID_REQUEST_COUNTER",
                    message, false, request, null);
            return;
        }

        if (message.startsWith(WALLET_RESERVE_REJECTED_PREFIX)) {
            writeWalletRejectedError(response, request, "WALLET_RESERVE_REJECTED",
                    message.substring(WALLET_RESERVE_REJECTED_PREFIX.length()));
            return;
        }

        if (message.startsWith(WALLET_SETTLE_REJECTED_PREFIX)) {
            writeWalletRejectedError(response, request, "WALLET_SETTLE_REJECTED",
                    message.substring(WALLET_SETTLE_REJECTED_PREFIX.length()));
            return;
        }

        if (message.startsWith(WALLET_GATEWAY_ERROR + ":")) {
            String errorMessage = message.substring((WALLET_GATEWAY_ERROR + ":").length());
            writeError(response, HttpServletResponse.SC_BAD_GATEWAY, WALLET_GATEWAY_ERROR,
                    errorMessage, true, request, null);
            return;
        }

        if ("INSUFFICIENT_FUNDS".equals(message)) {
            writeError(response, HttpServletResponse.SC_CONFLICT, "INSUFFICIENT_FUNDS",
                    "Not enough balance for reserve", false, request, null);
            return;
        }

        if ("Session not found".equals(message)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_SESSION",
                    message, false, request, null);
            return;
        }

        if (message.toLowerCase().contains("not found")) {
            writeError(response, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND",
                    message, false, request, null);
            return;
        }

        writeError(response, HttpServletResponse.SC_BAD_REQUEST, "BAD_REQUEST",
                message, false, request, null);
    }

    private void writeWalletRejectedError(HttpServletResponse response, HttpServletRequest request, String code,
                                          String payload) throws IOException {
        String walletCode = "UNKNOWN";
        String walletMessage = "wallet rejected request";
        if (!StringUtils.isTrimmedEmpty(payload)) {
            String[] parts = payload.split(":", 2);
            walletCode = parts[0];
            if (parts.length > 1 && !StringUtils.isTrimmedEmpty(parts[1])) {
                walletMessage = parts[1];
            }
        }
        Map<String, Object> details = new HashMap<>();
        details.put("walletCode", walletCode);
        writeError(response, HttpServletResponse.SC_CONFLICT, code, walletMessage, false, request, details);
    }

    private String resolveTraceId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        if (!StringUtils.isTrimmedEmpty(requestId)) {
            return requestId;
        }
        return UUID.randomUUID().toString();
    }

    private interface SessionExecutor<T> {
        T execute(SessionInfo sessionInfo, AccountInfo accountInfo) throws Exception;
    }

    private enum WalletCallType {
        RESERVE,
        SETTLE
    }

    private static class SessionContext {
        private final SessionInfo sessionInfo;
        private final AccountInfo accountInfo;
        private final ReserveState reserveState;

        private SessionContext(SessionInfo sessionInfo, AccountInfo accountInfo) {
            this(sessionInfo, accountInfo, null);
        }

        private SessionContext(SessionInfo sessionInfo, AccountInfo accountInfo, ReserveState reserveState) {
            this.sessionInfo = sessionInfo;
            this.accountInfo = accountInfo;
            this.reserveState = reserveState;
        }
    }

    private static class WalletWagerOutcome {
        private final long balanceAfterOperation;
        private final String externalTransactionId;
        private final String responseCode;

        private WalletWagerOutcome(long balanceAfterOperation, String externalTransactionId, String responseCode) {
            this.balanceAfterOperation = balanceAfterOperation;
            this.externalTransactionId = externalTransactionId;
            this.responseCode = responseCode;
        }
    }

    private static class ReserveState {
        private final String walletOperationId;
        private final String sessionId;
        private final String roundId;
        private final long betAmount;
        private final int gameId;
        private final long gameSessionId;
        private final long roundNumericId;
        private final Long balanceAfterReserve;
        private volatile boolean settled;
        private volatile Long winAmount;
        private volatile String settleRoundId;
        private volatile Long balanceAfterSettle;

        private ReserveState(String walletOperationId, String sessionId, String roundId, long betAmount, int gameId,
                             long gameSessionId, long roundNumericId, Long balanceAfterReserve) {
            this.walletOperationId = walletOperationId;
            this.sessionId = sessionId;
            this.roundId = roundId;
            this.betAmount = betAmount;
            this.gameId = gameId;
            this.gameSessionId = gameSessionId;
            this.roundNumericId = roundNumericId;
            this.balanceAfterReserve = balanceAfterReserve;
            this.settled = false;
        }
    }

    public static class SessionValidateRequest {
        public String sessionId;
        public Integer bankId;
        public String playerId;
    }

    public static class WalletReserveRequest {
        public String sessionId;
        public String roundId;
        public Integer requestCounter;
        public Long betAmount;
        public String clientOperationId;
        public Integer gameId;
    }

    public static class WalletSettleRequest {
        public String sessionId;
        public String roundId;
        public String walletOperationId;
        public Integer requestCounter;
        public Long winAmount;
        public String clientOperationId;
        public Integer gameId;
    }

    public static class HistoryWriteRequest {
        public String sessionId;
        public String roundId;
        public String eventType;
        public Map<String, Object> data;
    }
}
