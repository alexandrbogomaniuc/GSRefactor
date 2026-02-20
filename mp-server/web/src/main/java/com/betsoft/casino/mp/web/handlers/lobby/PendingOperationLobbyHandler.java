package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.model.IRoomPlayerInfo;
import com.betsoft.casino.mp.model.MoneyType;
import com.betsoft.casino.mp.model.PaymentTransactionStatus;
import com.betsoft.casino.mp.payment.AddWinPendingOperation;
import com.betsoft.casino.mp.payment.IPendingOperation;
import com.betsoft.casino.mp.payment.PendingOperationType;
import com.betsoft.casino.mp.service.IPendingOperationService;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.transport.CheckPendingOperationStatus;
import com.betsoft.casino.mp.transport.PendingOperationStatus;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.service.SocketService;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * Handler for pending operations of players in Lobby
 */
@Component
public class PendingOperationLobbyHandler extends MessageHandler<CheckPendingOperationStatus, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(PendingOperationLobbyHandler.class);
    private SocketService socketService;
    private RoomPlayerInfoService playerInfoService;
    private IPendingOperationService pendingOperationService;

    public PendingOperationLobbyHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                                      SocketService socketService, RoomPlayerInfoService playerInfoService, IPendingOperationService pendingOperationService) {
        super(gson, lobbySessionService, lobbyManager);
        this.socketService = socketService;
        this.playerInfoService = playerInfoService;
        this.pendingOperationService = pendingOperationService;
    }

    /**
     * Handle CheckPendingOperationStatus message from client. Send to client current status of pending operations.
     * @param session web socket session
     * @param message CheckPendingOperationStatus message from client
     * @param client lobby socket client
     */
    @Override
    public void handle(WebSocketSession session, CheckPendingOperationStatus message, ILobbySocketClient client) {
        long accountId = client.getAccountId();
        if (client.getAccountId() == null) {
            sendErrorMessage(client, ErrorCodes.NOT_LOGGED_IN, "CheckPendingOperationStatus failed: account not found",
                    message.getRid());
            return;
        }
        if (MoneyType.FREE.equals(client.getMoneyType())) {
            client.sendMessage(new PendingOperationStatus(getCurrentTime(), message.getRid(), false), message);
            return;
        }

        boolean pending = false;
        PaymentTransactionStatus status = getPendingWinStatus(client);
        getLog().debug("PendingOperationHandler, accountId: {}, status: {}",
                client.getAccountId(), status);
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
                    //it's possible to remove second try catch after removing using forceUnlock in game
            }
            if (status == PaymentTransactionStatus.FAILED) {
                sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR, "Pending operation failed",
                        message.getRid());
                return;
            }
        }
        client.sendMessage(new PendingOperationStatus(getCurrentTime(), message.getRid(), pending), message);
    }

    /**
     * Checks pending win status  for player
     * @param client lobby socket client
     * @return {@code PaymentTransactionStatus} return actual status (STARTED|PENDING|APPROVED|FAILED)
     */
    protected PaymentTransactionStatus getPendingWinStatus(ILobbySocketClient client) {
        long accountId = client.getAccountId();
        long roundId = -1;
        long gameSessionId = -1;
        long roomId = -1;
        IPendingOperation operation = pendingOperationService.get(accountId);
        if (operation != null && operation.getOperationType() == PendingOperationType.BUY_IN) {
            return PaymentTransactionStatus.PENDING;
        }
        if (operation != null && operation.getOperationType() == PendingOperationType.ADD_WIN) {
            AddWinPendingOperation addWinPendingOperation = (AddWinPendingOperation) operation;
            roundId = addWinPendingOperation.getRoundId();
            gameSessionId = addWinPendingOperation.getGameSessionId();
            roomId = addWinPendingOperation.getRoomId();
        } else {
            IRoomPlayerInfo playerInfo = playerInfoService.get(accountId);
            if (playerInfo != null) {
                roundId = playerInfo.getExternalRoundId();
                gameSessionId = playerInfo.getGameSessionId();
                roomId = playerInfo.getRoomId();
            }
        }
        return socketService.getPaymentOperationStatus(client.getAccountId(), roomId, roundId, client.getSessionId(), gameSessionId,
                client.getGameType().getGameId(), client.getBankId(), null, -1);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

}
