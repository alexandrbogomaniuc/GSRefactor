package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.room.ILatencyResponse;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.Latency;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import static java.lang.Math.round;

/**
 * Handler for latency for each player session
 */
@Component
public class LatencyHandler extends AbstractRoomHandler<Latency, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(LatencyHandler.class);
    private static final double DEFAULT_LATENCY_THRESHOLD_MS = 100;
    private final LobbySessionService lobbySessionService;

    @Value("${websocket.latency.threshold.ms}")
    private double latencyThresholdMS;
    @Value("${websocket.latency.standard.type.enabled}")
    protected boolean latencyStandardTypeEnabled;

    public LatencyHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                          MultiNodeRoomInfoService multiNodeRoomInfoService, RoomPlayerInfoService playerInfoService,
                          RoomServiceFactory roomServiceFactory, ServerConfigService serverConfigService,
                          LobbySessionService lobbySessionService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.lobbySessionService = lobbySessionService;
        if (latencyThresholdMS == 0) {
            latencyThresholdMS = DEFAULT_LATENCY_THRESHOLD_MS;
        }
    }

    /**
     * Handles Latency message.
     *
     * @param session web socket session of owner
     * @param message Latency message
     * @param client  game socket client
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void handle(WebSocketSession session, Latency message, IGameSocketClient client) {

        if (!latencyStandardTypeEnabled){
            return;
        }
        getLog().debug("handle: message:{}, client:{}, session:{}", message, client, session);
        long now = System.currentTimeMillis();

        if (message.getStep() != 2 && message.getStep() != 4) {
            sendErrorMessage(client, ErrorCodes.WRONG_STEP, "Step is not allowed", message.getRid());
            return;
        }
        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession == null) {
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }

        try {
            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room == null) {
                sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "No room identified", message.getRid());
                return;
            }

            if (message.getStep() == 2) {
                ILatencyResponse response = room.getTOFactoryService()
                        .createLatencyResponse(System.currentTimeMillis(), message.getRid(), 3,
                                message.getServerTs(),
                                now,
                                message.getClientTs(),
                                message.getClientAckTs());
                client.sendMessage(response);
                getLog().debug("Step 2 server process time: {} ms;",System.currentTimeMillis() - now);
                return;
            }
            if (message.getStep() == 4) {
                long serverLatency = message.getServerAckTs() - message.getServerTs();
                long clientLatency = message.getClientAckTs() - message.getClientTs();
                long latency = round((serverLatency + clientLatency) * 0.5);

                client.getLatencyStatistic().update(latency, "Server latency: " + serverLatency + " ms. Client latency: " + clientLatency + " ms.");

                if (latency > latencyThresholdMS) {
                    String gameType = room.getGameType() == null ? "UNKNOWN" : room.getGameType().name();
                    getLog().warn("HIGH LATENCY: {} ms; Player: {}; sessionId: {};  game: {}; Server latency: {} ms; Client latency: {} ms; method time: {}",
                            latency, client.getNickname(), session.getId(), gameType, serverLatency, clientLatency, System.currentTimeMillis() - now);
                } else {
                    String gameType = room.getGameType() == null ? "UNKNOWN" : room.getGameType().name();
                    getLog().debug("LOW LATENCY: {} ms; Player: {}; sessionId: {};  game: {}; Server latency: {} ms; Client latency: {} ms; method time: {}",
                            latency, client.getNickname(), session.getId(), gameType, serverLatency, clientLatency, System.currentTimeMillis() - now);
                }
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
