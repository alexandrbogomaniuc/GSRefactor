package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.maxcrashgame.model.Seat;
import com.betsoft.casino.mp.model.ICrashBetInfo;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.room.IMultiNodeRoom;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.CrashCancelAutoEject;
import com.betsoft.casino.mp.transport.CrashCancelAutoEjectResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.utils.TObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Collection;

/**
 * User: flsh
 * Date: 09.03.2022.
 * Handler for cancel auto eject
 */
@Component
public class CrashCancelAutoEjectHandler extends AbstractRoomHandler<CrashCancelAutoEject, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(CrashCancelAutoEjectHandler.class);
    protected final LobbySessionService lobbySessionService;

    public CrashCancelAutoEjectHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                                       MultiNodeRoomInfoService multiNodeRoomInfoService, RoomPlayerInfoService playerInfoService,
                                       RoomServiceFactory roomServiceFactory, ServerConfigService serverConfigService,
                                       LobbySessionService lobbySessionService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
    }

    @Override
    public void handle(WebSocketSession session, CrashCancelAutoEject message, IGameSocketClient client) {
        Long accountId = client.getAccountId();
        if (client.getRoomId() == null || accountId == null) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
            return;
        }
        getLog().debug("handle CrashCancelBet message: {}, accountId: {} ", message, accountId);

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
            @SuppressWarnings("rawtypes")
            IMultiNodeRoom room = (IMultiNodeRoom) getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            Seat seat = (Seat) room.getSeatByAccountId(client.getAccountId());
            //need dirty check for prevent player abuses
            if (isSeatOrBetNotValid(client, seat, message)) {
                return;
            }
            boolean sendMessages = false;
            room.lockSeat(client.getAccountId());
            try {
                seat = (Seat) room.getSeatByAccountId(client.getAccountId());
                if (isSeatOrBetNotValid(client, seat, message)) {
                    return;
                }
                ICrashBetInfo crashBet = seat.getCrashBet(message.getBetId());
                crashBet.setAutoPlay(false);
                crashBet.setAutoPlayMultiplier(null);
                //noinspection unchecked
                room.saveSeat(0, seat);
                sendMessages = true;
            } finally {
                room.unlockSeat(client.getAccountId());
                if (sendMessages) {
                    seat.sendMessage(new CrashCancelAutoEjectResponse(System.currentTimeMillis(), message.getRid(),
                            message.getBetId(), seat.getNickname()), message);
                    //send to seats on this server
                    CrashCancelAutoEjectResponse otherSeatsMessage = new CrashCancelAutoEjectResponse(System.currentTimeMillis(), TObject.SERVER_RID,
                            message.getBetId(), seat.getNickname());
                    Collection<IGameSocketClient> observers = room.getObservers();
                    for (IGameSocketClient observer : observers) {
                        if (!observer.isDisconnected() && observer.getAccountId() != seat.getAccountId()) {
                            sendMessageToOtherSeat(otherSeatsMessage, observer);
                        }
                    }
                    SendSeatsMessageTask sendSeatsMessageTask = (SendSeatsMessageTask) room.createSendSeatsMessageTask(seat.getAccountId(),
                            true, message.getRid(), otherSeatsMessage, true);
                    room.executeOnAllMembers(sendSeatsMessageTask);
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    private boolean isSeatOrBetNotValid(IGameSocketClient client, Seat seat, CrashCancelAutoEject message) {
        if (seat == null) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
            return true;
        }
        ICrashBetInfo crashBet = seat.getCrashBet(message.getBetId());
        if (crashBet == null) {
            sendErrorMessage(client, ErrorCodes.BET_NOT_FOUND, "Not found", message.getRid());
            return true;
        }
        if (crashBet.isEjected()) {
            sendErrorMessage(client, ErrorCodes.BET_NOT_FOUND, "Bet is ejected", message.getRid());
            return true;
        }
        return false;
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @SuppressWarnings("rawtypes")
    private void sendMessageToOtherSeat(CrashCancelAutoEjectResponse otherSeatsMessage, IGameSocketClient observer) {
        try {
            observer.sendMessage(otherSeatsMessage);
        } catch (Exception e) {
            LOG.debug("Cannot send message to otherSeat, accountId={}", observer.getAccountId(), e);
        }
    }

}
