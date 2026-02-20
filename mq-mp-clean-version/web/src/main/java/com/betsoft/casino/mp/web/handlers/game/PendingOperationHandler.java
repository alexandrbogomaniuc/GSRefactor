package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.IRoomPlayerInfo;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.MoneyType;
import com.betsoft.casino.mp.model.PaymentTransactionStatus;
import com.betsoft.casino.mp.payment.AddWinPendingOperation;
import com.betsoft.casino.mp.payment.IPendingOperation;
import com.betsoft.casino.mp.payment.PendingOperationType;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.CheckPendingOperationStatus;
import com.betsoft.casino.mp.transport.PendingOperationStatus;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * Handler for pending operations of players
 */
@Component
public class PendingOperationHandler extends AbstractRoomHandler<CheckPendingOperationStatus, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(PendingOperationHandler.class);
    private SocketService socketService;
    protected LobbySessionService lobbySessionService;
    protected IPendingOperationService pendingOperationService;

    public PendingOperationHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                   MultiNodeRoomInfoService multiNodeRoomInfoService, RoomPlayerInfoService playerInfoService,
                                   RoomServiceFactory roomServiceFactory, ServerConfigService serverConfigService, SocketService socketService,
                                   LobbySessionService lobbySessionService, IPendingOperationService pendingOperationService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.socketService = socketService;
        this.lobbySessionService = lobbySessionService;
        this.pendingOperationService = pendingOperationService;
    }

    /**
     * Handle CheckPendingOperationStatus message from client. Send to client current status of pending operations.
     * @param session web socket session
     * @param message CheckPendingOperationStatus message from client
     * @param client game socket client
     */
    @Override
    public void handle(WebSocketSession session, CheckPendingOperationStatus message, IGameSocketClient client) {
        Long accountId = client.getAccountId() != null ? client.getAccountId() : getAccountIdFromSid(message);
        if (accountId == null) {
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }
        boolean pending = false;
        String sessionId = client.getSessionId() != null ? client.getSessionId() : message.getSid();
        LobbySession lobbySession = lobbySessionService.get(sessionId);
        if (MoneyType.FREE.equals(lobbySession.getMoneyType())) {
            client.sendMessage(new PendingOperationStatus(getCurrentTime(), message.getRid(), false), message);
            return;
        }

        PaymentTransactionStatus status = getPendingWinStatus(client, lobbySession);
        getLog().debug("PendingOperationHandler, accountId: {}, status: {}",
                accountId, status);
        if (status != null) {
            pending = status.equals(PaymentTransactionStatus.PENDING) || status.equals(PaymentTransactionStatus.STARTED);
            boolean locked = false;
            try {
                if (!pending) {
                    playerInfoService.lock(accountId);
                    locked = true;
                    getLog().debug("PendingOperationHandler lock: {}", accountId);
                    IRoomPlayerInfo actualPlayerInfo = playerInfoService.get(accountId);
                    if (actualPlayerInfo != null) {
                        actualPlayerInfo.setPendingOperation(false);
                        playerInfoService.put(actualPlayerInfo);
                    }
                    getLog().debug("PendingOperationHandler set pending operation false, accountId: {},", accountId);
                }
            } catch (Exception e) {
                processUnexpectedError(client, message, e);
            } finally {
                if (locked) {
                    playerInfoService.unlock(accountId);
                    getLog().debug("PendingOperationHandler unlock: {}", accountId);
                }
            }
            if (status == PaymentTransactionStatus.FAILED) {
                sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "Pending operation failed",
                        message.getRid());
                return;
            }
        }
        client.sendMessage(new PendingOperationStatus(getCurrentTime(), message.getRid(), pending), message);
    }

    private Long getAccountIdFromSid(CheckPendingOperationStatus message) {
        String sid = message.getSid();
        if (sid == null) {
            return null;
        }
        LobbySession lobbySession = lobbySessionService.get(sid);
        return lobbySession != null ? lobbySession.getAccountId() : null;
    }

    /**
     * Checks pending win status  for player
     * @param client game socket client
     * @return {@code PaymentTransactionStatus} return actual status (STARTED|PENDING|APPROVED|FAILED)
     */
    protected PaymentTransactionStatus getPendingWinStatus(IGameSocketClient client, LobbySession lobbySession) {
        long roundId = -1;
        long gameSessionId = -1;
        IPendingOperation operation = pendingOperationService.get(lobbySession.getAccountId());
        if (operation != null && operation.getOperationType() == PendingOperationType.BUY_IN) {
            getLog().debug("getPendingWinStatus: PaymentTransactionStatus=PENDING, IPendingOperation={} " +
                            "for AccountId={}", operation.getOperationType(), lobbySession.getAccountId());
            return PaymentTransactionStatus.PENDING;
        }
        if (operation != null && operation.getOperationType() == PendingOperationType.ADD_WIN) {
            AddWinPendingOperation addWinPendingOperation = (AddWinPendingOperation) operation;
            roundId = addWinPendingOperation.getRoundId();
            gameSessionId = addWinPendingOperation.getGameSessionId();
        } else if (client.getSeat() != null && client.getSeat().getPlayerInfo() != null) {
            IRoomPlayerInfo playerInfo = client.getSeat().getPlayerInfo();
            roundId = playerInfo.getExternalRoundId();
            gameSessionId = playerInfo.getGameSessionId();
        } else {
            IRoomPlayerInfo playerInfo = playerInfoService.get(lobbySession.getAccountId());
            if (playerInfo != null) {
                roundId = playerInfo.getExternalRoundId();
                gameSessionId = playerInfo.getGameSessionId();
            }
        }

        PaymentTransactionStatus paymentTransactionStatus
                = socketService.getPaymentOperationStatus(lobbySession.getAccountId(), lobbySession.getRoomId(), roundId,
                lobbySession.getSessionId(), gameSessionId,
                lobbySession.getGameId(), lobbySession.getBankId(), null, -1);

        getLog().debug("getPendingWinStatus: PaymentTransactionStatus={}, IPendingOperation={} for " +
                        "AccountId={}, roundId={}, gameSessionId={}",
                paymentTransactionStatus,
                operation != null ? operation.getOperationType() : null,
                lobbySession.getAccountId(), roundId, gameSessionId);

        return paymentTransactionStatus;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
