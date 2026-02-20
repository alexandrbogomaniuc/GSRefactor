package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.GetRoomInfo;
import com.betsoft.casino.mp.transport.GetRoomInfoResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.handlers.MessageHandler;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.dgphoenix.casino.common.currency.CurrencyRate;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.*;


@Component
public class GetRoomInfoHandler extends MessageHandler<GetRoomInfo, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(GetRoomInfoHandler.class);

    private final SingleNodeRoomInfoService singleNodeRoomInfoService;
    private final MultiNodeRoomInfoService multiNodeRoomInfoService;
    private final RoomPlayerInfoService playerInfoService;
    private final CurrencyRateService currencyRateService;
    private final BGPrivateRoomInfoService bgPrivateRoomInfoService;
    private final MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService;

    public GetRoomInfoHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                              SingleNodeRoomInfoService singleNodeRoomInfoService, MultiNodeRoomInfoService multiNodeRoomInfoService,
                              RoomPlayerInfoService playerInfoService, CurrencyRateService currencyRateService,
                              BGPrivateRoomInfoService bgPrivateRoomInfoService, MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService) {
        super(gson, lobbySessionService, lobbyManager);
        this.singleNodeRoomInfoService = singleNodeRoomInfoService;
        this.multiNodeRoomInfoService = multiNodeRoomInfoService;
        this.playerInfoService = playerInfoService;
        this.currencyRateService = currencyRateService;
        this.bgPrivateRoomInfoService = bgPrivateRoomInfoService;
        this.multiNodePrivateRoomInfoService = multiNodePrivateRoomInfoService;
    }

    @Override
    public void handle(WebSocketSession session, GetRoomInfo message, ILobbySocketClient client) {
        LOG.warn("handle: {}", message);
        ILobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession == null) {
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }
        if (checkLogin(message, client)) {
            try {
                IRoomInfoService roomInfoService = getRoomInfoService(client);
                IRoomInfo room = getRoomInfo(message.getRid(), message.getRoomId(), client, roomInfoService);
                if (room != null) {
                    IRoomPlayerInfo alreadySeatPlayerInfo = null;
                    int alreadySeatNumber = -1;
                    if (client.getAccountId() != null) {
                        IRoomPlayerInfo playerInfo = playerInfoService.get(client.getAccountId());
                        LOG.debug("playerInfo: {}", playerInfo);
                        if (playerInfo != null) {
                            alreadySeatPlayerInfo = playerInfo;
                            alreadySeatNumber = playerInfo.getSeatNumber();
                        }
                    }
                    Collection<IRoomPlayerInfo> playerInfos = playerInfoService.getForRoom(message.getRoomId());
                    List<ITransportSeat> seats = convert(playerInfos);

                    float minBuyIn = room.getMinBuyIn();

                    CurrencyRate currencyRate = currencyRateService.get(lobbySession.getCurrency().getCode(),
                            room.getCurrency());
                    double playerStake = room.getStake().toCents();
                    LOG.debug("handle: player={}, room.id={}, room.stake={}, playerStake={}, rate={}, player " +
                                    "currency={}", lobbySession.getNickname(), room.getId(), room.getStake().toCents(),
                            playerStake, currencyRate, lobbySession.getCurrency().getCode());

                    GetRoomInfoResponse response = new GetRoomInfoResponse(System.currentTimeMillis(),
                            room.getId(),
                            message.getRid(),
                            room.getName(),
                            room.getMaxSeats(),
                            minBuyIn,
                            playerStake,
                            playerStake,
                            seats.isEmpty() ? RoomState.WAIT : RoomState.PLAY, //not used in lobby
                            seats,
                            0,
                            room.getWidth(),
                            room.getHeight(),
                            null,
                            null,
                            alreadySeatNumber,
                            0,
                            lobbySession.getBalance(),
                            0,
                            room.getMapId(),
                            null,
                            GameType.getAmmoValues(room.getMoneyType(), room.getStake().toFloatCents()),
                            new ArrayList<>(), new HashMap<>(),
                            false,
                            room.getRoundId(), new HashMap<>(),
                            alreadySeatPlayerInfo == null ? null : alreadySeatPlayerInfo.getActiveCashBonusSession(),
                            alreadySeatPlayerInfo == null ? null : alreadySeatPlayerInfo.getTournamentSession(), 1,
                            new HashMap<>(), new HashSet<>(), null, null, null
                    );

                    client.sendMessage(response, message);
                }
            } catch (Exception e) {
                LOG.warn("Unable to get room info", e);
                sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Room not found", message.getRid());
            }
        }
    }

    private IRoomInfoService getRoomInfoService(ILobbySocketClient client) {
        if(client.isPrivateRoom()) {
            return client.getGameType().isSingleNodeRoomGame() ? bgPrivateRoomInfoService : multiNodePrivateRoomInfoService;
        } else {
            return client.getGameType().isSingleNodeRoomGame() ? singleNodeRoomInfoService : multiNodeRoomInfoService;
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
