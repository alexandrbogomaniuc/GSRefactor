package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.GetStartGameUrl;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.abs.casino.common.exception.CommonException;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;


/**
 * Start game handler fo usual mode. Handle GetStartGameUrl messages from client.
 */
@Component
public class GetStartGameUrlHandler extends AbstractStartGameUrlHandler<GetStartGameUrl, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(GetStartGameUrlHandler.class);

    public GetStartGameUrlHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                                  SingleNodeRoomInfoService singleNodeRoomInfoService, MultiNodeRoomInfoService multiNodeRoomInfoService,
                                  ServerConfigService serverConfigService,
                                  RoomServiceFactory roomServiceFactory, RoomTemplateService roomTemplateService,
                                  RoomPlayerInfoService playerInfoService, BGPrivateRoomInfoService bgPrivateRoomInfoService,
                                  MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService, RoomPlayersMonitorService roomPlayersMonitorService) {
        super(gson, lobbySessionService, lobbyManager, singleNodeRoomInfoService, multiNodeRoomInfoService, serverConfigService, roomServiceFactory,
                roomTemplateService, playerInfoService, bgPrivateRoomInfoService, multiNodePrivateRoomInfoService, roomPlayersMonitorService);

    }

    @Override
    long getRequestedRoomIdFromMessage(GetStartGameUrl message) {
        return message.getRoomId() == null ? -1 : message.getRoomId();
    }

    protected boolean checkIsBadMessageAndSendError(GetStartGameUrl message, ILobbySocketClient client,
                                                    ILobbySession lobbySession) {
        if (message.getStake() <= 0) {
            sendIllegalStakeMessage(message, client);
            return true;
        }

        IActiveFrbSession activeFrbSession = lobbySession.getActiveFrbSession();
        if (activeFrbSession != null) {
            if (activeFrbSession.getStake() == message.getStake()) {
                return false;
            } else {
                LOG.error("stake {} not allowed, allowed only {} ", message.getStake(), lobbySession.getStakes());
                sendIllegalStakeMessage(message, client);
                return true;
            }
        }
        if (lobbySession.getStakes() == null || !lobbySession.getStakes().contains(message.getStake())) {
            LOG.error("stake {} not allowed, allowed only {} ", message.getStake(), lobbySession.getStakes());
            sendIllegalStakeMessage(message, client);
            return true;
        }
        return false;
    }

    private void sendIllegalStakeMessage(GetStartGameUrl message, ILobbySocketClient client) {
        sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Illegal stake value", message.getRid());
    }

    @Override
    protected Money getStakeFromMessage(GetStartGameUrl message) {
        return Money.fromCents(message.getStake());
    }

    @Override
    public IRoomInfo getBestRoomForStake(LobbySession lobbySession, GameType gameType, ILobbySocketClient client,
                                         String currency, GetStartGameUrl message) throws CommonException {
        IRoomInfo bestRoom;
        client.setGameType(gameType);
        MoneyType moneyType = client.getMoneyType();
        if (client.getPlayerInfo().isGuest()) {
            client.setMoneyType(MoneyType.FREE);
            moneyType = MoneyType.FREE;
        }
        if (moneyType == MoneyType.FRB || moneyType == MoneyType.CASHBONUS || moneyType == MoneyType.TOURNAMENT) {
            Money stake;
            if (moneyType == MoneyType.FRB) {
                IActiveFrbSession activeFRBonus = lobbySession.getActiveFrbSession();
                if (activeFRBonus == null) {
                    throw new CommonException("FRBonus not found in lobby session");
                }
                stake = Money.fromCents(activeFRBonus.getStake());
            } else {
                stake = getStakeFromMessage(message);
            }
            bestRoom = getSpecialRoom(gameType, client, moneyType, stake);
        } else {
            bestRoom = getRoomInfoByStake(client, getStakeFromMessage(message), gameType, currency, moneyType);
        }
        return bestRoom;
    }


    @Override
    public Logger getLog() {
        return LOG;
    }
}
