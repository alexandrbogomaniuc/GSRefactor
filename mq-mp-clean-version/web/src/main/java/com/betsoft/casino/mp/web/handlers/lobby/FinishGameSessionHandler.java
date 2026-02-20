package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IFinishGameSessionResponse;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.*;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * Enter lobby handler. Handle EnterLobby message from client.
 */
@Component
public class FinishGameSessionHandler extends MessageHandler<FinishGameSession, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(FinishGameSessionHandler.class);

    private final RoomPlayersMonitorService roomPlayersMonitorService;

    @SuppressWarnings("rawtypes")
    public FinishGameSessionHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                                    RoomPlayersMonitorService roomPlayersMonitorService) {
        super(gson, lobbySessionService, lobbyManager);
        this.roomPlayersMonitorService = roomPlayersMonitorService;
    }

    @Override
    public void handle(WebSocketSession session, FinishGameSession message, ILobbySocketClient client) {
        try {
            getLog().debug("handle: message: {}", message);

            int rid = message.getRid();
            String sid = message.getSid();
            String privateRoomId = message.getPrivateRoomId();
            
            if(StringUtils.isTrimmedEmpty(sid)) {
                getLog().error("handle: sid is null or empty for rid:{}", rid);
                sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Bad sid", rid);
                return;
            }

            LobbySession lobbySession = lobbySessionService.get(sid);
            getLog().debug("handle: sid={}, lobbySession={}", sid, lobbySession);

            if(lobbySession == null) {
                getLog().error("handle: sid={}, lobbySession is null", sid);
                sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Bad sid", rid);
                return;
            }

            if(!sid.equals(lobbySession.getSessionId())) {
                getLog().error("handle: sid:{} not equal to lobbySession.getSessionId():{}",
                        sid, lobbySession.getSessionId());
                sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Bad sid", rid);
                return;
            }

            //response straight away back to not provide any delays
            IFinishGameSessionResponse response =
                    new FinishGameSessionResponse(System.currentTimeMillis(), message.getRid(), true);
            client.sendMessage(response);

            try {
                getLog().debug("handle: call for finishGameSessionAndMakeSitOutAsync with sid:{}, " +
                        "privateRoomId:{}", sid, privateRoomId);
                roomPlayersMonitorService.finishGameSessionAndMakeSitOutAsync(client.getServerId(), sid, privateRoomId);
            } catch (Exception e) {
                getLog().error("handle: exception sid:{}, privateRoomId:{}, message:{}",
                        sid, privateRoomId, e.getMessage(), e);
            }

        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
