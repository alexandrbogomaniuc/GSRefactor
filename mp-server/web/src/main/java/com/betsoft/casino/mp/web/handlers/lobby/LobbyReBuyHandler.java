package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.data.persister.TournamentSessionPersister;
import com.betsoft.casino.mp.data.persister.WeaponsPersister;
import com.betsoft.casino.mp.exceptions.BuyInFailedException;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.service.IWeaponService;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.transport.ReBuy;
import com.betsoft.casino.mp.transport.ReBuyResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.service.SocketService;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.betsoft.casino.utils.TInboundObject;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Map;

@Component
public class LobbyReBuyHandler extends MessageHandler<ReBuy, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(LobbyReBuyHandler.class);
    private static final String INSUFFICIENT_BALANCE = "Insufficient balance";
    private static final String REBUY_LIMIT_EXCEEDED = "ReBuy limit exceeded";

    private final SocketService socketService;
    private final RoomPlayerInfoService playerInfoService;
    private final IWeaponService weaponService;
    private final TournamentSessionPersister tournamentSessionPersister;

    public LobbyReBuyHandler(Gson gson, LobbySessionService lobbySessionService, SocketService socketService,
                             LobbyManager lobbyManager, RoomPlayerInfoService playerInfoService,
                             CassandraPersistenceManager cpm) {
        super(gson, lobbySessionService, lobbyManager);
        this.socketService = socketService;
        this.playerInfoService = playerInfoService;
        this.weaponService = cpm.getPersister(WeaponsPersister.class);
        this.tournamentSessionPersister = cpm.getPersister(TournamentSessionPersister.class);
    }

    @Override
    public void handle(WebSocketSession session, ReBuy message, ILobbySocketClient client) {
        Long accountId = client.getAccountId();
        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession == null) {
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }
        TournamentSession tournamentSession = lobbySession.getTournamentSession();
        if (tournamentSession == null) {
            sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "ReBuy is available only in Tournament mode", message.getRid());
            return;
        }
        if (!isReBuyAvailable(tournamentSession)) {
            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, REBUY_LIMIT_EXCEEDED, message.getRid());
            return;
        } else {
            TournamentSession tSession =
                    tournamentSessionPersister.get(tournamentSession.getTournamentId(), tournamentSession.getAccountId());
            if (!isReBuyAvailable(tSession)) {
                sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, REBUY_LIMIT_EXCEEDED, message.getRid());
                return;
            }
        }
        IRoomPlayerInfo playerInfo = playerInfoService.get(accountId);
        if (playerInfo != null && playerInfo.getRoomId() > 0) {
            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Player sitting in the room", message.getRid());
            return;
        }
        try {
            if (hasPendingOperations(accountId, client, message)) {
                return;
            }
            playerInfoService.lock(accountId);
            getLog().debug("handle HS lock: {}", accountId);
            try {
                if (tournamentSession.getBalance() >= getMinStake(lobbySession)) {
                    LOG.error("Player has enough money for shooting, reBuy refused: tournamentSession={}", tournamentSession);
                    sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Player has money for shooting", message.getRid());
                    return;
                }
                if (playerHasSpecialWeapons(tournamentSession, client.getGameType().getGameId())) {
                    LOG.error("Player has special weapons, re-buy refused: tournamentSession={}", tournamentSession);
                    sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Player has special weapons", message.getRid());
                    return;
                }
                long amount = tournamentSession.getReBuyPrice();
                long balance = getRealBalance(client, MoneyType.REAL);
                LOG.info("Balance before ReBuy: {}", balance);
                if (amount > balance) {
                    LOG.error("Not enough money for ReBuy");
                    sendErrorMessage(client, ErrorCodes.NOT_ENOUGH_MONEY, "Not enough money", message.getRid());
                    return;
                }
                try {
                    long currentBalance = tournamentSession.getBalance();
                    BuyInResult buyInResult = socketService.buyIn(client.getServerId(), client.getAccountId(),
                            client.getSessionId(), Money.fromCents(amount), -1,
                            -1, -1, tournamentSession.getTournamentId(), currentBalance, null);
                    tournamentSession.setBalance(buyInResult.getAmount() + currentBalance);
                    tournamentSession.setReBuyCount(tournamentSession.getReBuyCount() + 1);
                    lobbySession.setBalance(tournamentSession.getBalance());
                    lobbySessionService.add(lobbySession);
                    tournamentSessionPersister.persist(tournamentSession);
                    client.sendMessage(new ReBuyResponse(System.currentTimeMillis(), message.getRid(), 0,
                            lobbySession.getBalance(), tournamentSession.getReBuyCount()), message);
                } catch (Exception e) {
                    LOG.error("Failed to perform ReBuy", e);
                    if (e instanceof BuyInFailedException) {
                        BuyInFailedException bfExc = (BuyInFailedException) e;
                        if (bfExc.getErrorCode() > 0) {
                            sendErrorMessage(client,
                                    ErrorCodes.translateGameServerErrorCode(bfExc.getErrorCode()),
                                    "ReBuy failed, reason: " + e.getMessage(), message.getRid());
                        } else if (INSUFFICIENT_BALANCE.equals(bfExc.getMessage())) {
                            sendErrorMessage(client, ErrorCodes.NOT_ENOUGH_MONEY, INSUFFICIENT_BALANCE,
                                    message.getRid());
                        } else if (REBUY_LIMIT_EXCEEDED.equals(bfExc.getMessage())) {
                            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, REBUY_LIMIT_EXCEEDED,
                                    message.getRid());
                        } else {
                            sendErrorMessage(client,
                                    bfExc.isFatal() ? ErrorCodes.BAD_BUYIN : ErrorCodes.NOT_FATAL_BAD_BUYIN,
                                    "ReBuy failed, reason: " + e.getMessage(), message.getRid());
                        }
                    } else {
                        sendErrorMessage(client, ErrorCodes.INTERNAL_ERROR,
                                "ReBuy failed, reason: " + e.getMessage(), message.getRid());
                    }
                }
            } finally {
                playerInfoService.unlock(accountId);
                getLog().debug("handle HS unlock: {}", accountId);
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    private long getRealBalance(ILobbySocketClient client, MoneyType moneyType) throws Exception {
        return socketService.getBalanceSync(client.getServerId(), client.getSessionId(), moneyType.name());
    }

    private boolean isReBuyAvailable(TournamentSession session) {
        return session.isReBuyAllowed() && (session.getReBuyLimit() == -1 || session.getReBuyCount() < session.getReBuyLimit());
    }

    private boolean playerHasSpecialWeapons(TournamentSession session, long gameId) {
        Map<Money, Map<Integer, Integer>> weaponsByStake = weaponService.getAllSpecialModeWeapons(
                session.getTournamentId(), session.getAccountId(), MoneyType.TOURNAMENT.ordinal(), gameId);
        for (Map<Integer, Integer> weapons : weaponsByStake.values()) {
            for (int count : weapons.values()) {
                if (count > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private long getMinStake(LobbySession lobbySession) {
        return lobbySession.getStakes().stream()
                .mapToLong(stake -> stake)
                .min()
                .orElse(Long.MAX_VALUE);
    }

    public boolean hasPendingOperations(long accountId, ILobbySocketClient client, TInboundObject message) {
        int cnt = 20;
        boolean hasPendingOperation = false;

        IRoomPlayerInfo playerInfo = null;
        while (cnt-- > 0) {
            playerInfo = playerInfoService.get(accountId);
            if (playerInfo == null) {
                break;
            }
            hasPendingOperation = playerInfo.isPendingOperation();
            if (!hasPendingOperation) {
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                getLog().error(e.getMessage(), e);
            }
        }

        getLog().debug("cnt:{},  playerInfo:{}, hasPendingOperation: {}, message: {}",
                cnt, playerInfo, hasPendingOperation, message);

        if (playerInfo != null && hasPendingOperation) {
            getLog().error("handle: cannot make sitOut, found payment operation, accountId={}", accountId);
            sendErrorMessage(client, ErrorCodes.FOUND_PENDING_OPERATION, "Found pending operation", message.getRid());
        }
        return hasPendingOperation;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
