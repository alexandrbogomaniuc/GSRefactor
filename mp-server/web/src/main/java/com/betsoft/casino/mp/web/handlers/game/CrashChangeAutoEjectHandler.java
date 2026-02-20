package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.maxcrashgame.model.AbstractCrashGameRoom;
import com.betsoft.casino.mp.maxcrashgame.model.Seat;
import com.betsoft.casino.mp.model.ICrashBetInfo;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.CrashChangeAutoEject;
import com.betsoft.casino.mp.transport.CrashChangeAutoEjectResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.utils.TObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

/**
 * User: flsh
 * Date: 24.03.2022.
 */
@Component
public class CrashChangeAutoEjectHandler extends AbstractRoomHandler<CrashChangeAutoEject, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(CrashChangeAutoEjectHandler.class);
    protected final LobbySessionService lobbySessionService;

    public CrashChangeAutoEjectHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                       MultiNodeRoomInfoService multiNodeRoomInfoService, RoomPlayerInfoService playerInfoService,
                                       RoomServiceFactory roomServiceFactory, ServerConfigService serverConfigService,
                                       LobbySessionService lobbySessionService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
    }

    @Override
    public void handle(WebSocketSession session, CrashChangeAutoEject message, IGameSocketClient client) {
        if (client.getRoomId() == null || client.getAccountId() == null) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
            return;
        }
        getLog().debug("handle CrashChangeAutoEject message: {}, accountId: {} ", message, client.getAccountId());

        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession == null) {
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }
        if (lobbySession.getActiveFrbSession() != null || lobbySession.getActiveCashBonusSession() != null
                || lobbySession.getTournamentSession() != null) {
            sendErrorMessage(client, ErrorCodes.BUYIN_NOT_ALLOWED, "Not allowed for bonus mode", message.getRid());
            return;
        }
        try {
            AbstractCrashGameRoom room = (AbstractCrashGameRoom) getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room == null) {
                return;
            }
            room.lockSeat(client.getAccountId());
            try {
                Seat seat = room.getSeatByAccountId(client.getAccountId());
                if (seat == null) {
                    sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
                    return;
                }
                ICrashBetInfo crashBet = seat.getCrashBet(message.getBetId());
                double messageMultiplier = message.getMultiplier();
                double roundedMultiplier = BigDecimal.valueOf(messageMultiplier).setScale(2, RoundingMode.HALF_UP).doubleValue();
                if (crashBet == null) {
                    sendErrorMessage(client, ErrorCodes.BET_NOT_FOUND, "Not found", message.getRid());
                    return;
                } else if (!room.isBuyInAllowed(seat)) {
                    sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_CHANGE_BET_LEVEL, "Not allowed at current game state",
                            message.getRid());
                    return;
                } else if (crashBet.isEjected()) {
                    sendErrorMessage(client, ErrorCodes.BET_NOT_FOUND, "Bet is ejected", message.getRid());
                    return;
                } else if (!room.isValidMultiplier(roundedMultiplier)) {
                    LOG.error("Bad multiplier value , multiplier={}, seat={}", roundedMultiplier, seat);
                    sendErrorMessage(client, ErrorCodes.BAD_MULTIPLIER, "Bad multiplier value",
                            message.getRid(), message);
                    return;
                }
                //after fix Bot, remove this check or sendErrorMessage - ErrorCodes.BAD_MULTIPLIER
                if (messageMultiplier - roundedMultiplier != 0.0) {
                    LOG.error("Found suspicious message multiplier, messageMultiplier={}, roundedMultiplier={}",
                            messageMultiplier, roundedMultiplier);
                }
                crashBet.setAutoPlay(true);
                crashBet.setMultiplier(roundedMultiplier);
                crashBet.setAutoPlayMultiplier(crashBet.getMultiplier());
                room.saveSeat(0, seat);
                seat.sendMessage(new CrashChangeAutoEjectResponse(System.currentTimeMillis(), message.getRid(),
                        message.getBetId(), seat.getNickname()), message);
                sendMessageToOtherSeats(room, message, seat);
            } finally {
                room.unlockSeat(client.getAccountId());
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    private void sendMessageToOtherSeats(AbstractCrashGameRoom room, CrashChangeAutoEject message, Seat seat) {
        CrashChangeAutoEjectResponse otherSeatsMessage = new CrashChangeAutoEjectResponse(System.currentTimeMillis(), TObject.SERVER_RID,
                message.getBetId(), seat.getNickname());
        Collection<IGameSocketClient> observers = room.getObservers();
        for (IGameSocketClient observer : observers) {
            if (!observer.isDisconnected() && observer.getAccountId() != seat.getAccountId()) {
                try {
                    observer.sendMessage(otherSeatsMessage);
                } catch (Exception e) {
                    LOG.debug("Cannot send message to otherSeat, accountId={}", observer.getAccountId(), e);
                }
            }
        }
        SendSeatsMessageTask sendSeatsMessageTask = (SendSeatsMessageTask) room.createSendSeatsMessageTask(seat.getAccountId(),
                true, message.getRid(), otherSeatsMessage, true);
        room.executeOnAllMembers(sendSeatsMessageTask);
    }

}
