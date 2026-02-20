package com.betsoft.casino.mp.web.handlers.lobby;

import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.GetPrivateBattlegroundStartGameUrl;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.ILobbySocketClient;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Start game handler fo private battleground mode. Handle GetPrivateBattlegroundStartGameUrl messages from client.
 */
@Component
public class GetPrivateBattlegroundStartGameUrlHandler extends AbstractStartGameUrlHandler<GetPrivateBattlegroundStartGameUrl, ILobbySocketClient> {
    private static final Logger LOG = LogManager.getLogger(GetPrivateBattlegroundStartGameUrlHandler.class);

    public GetPrivateBattlegroundStartGameUrlHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager,
                                              SingleNodeRoomInfoService singleNodeRoomInfoService, MultiNodeRoomInfoService multiNodeRoomInfoService,
                                              ServerConfigService serverConfigService,
                                              RoomServiceFactory roomServiceFactory, RoomTemplateService roomTemplateService,
                                              RoomPlayerInfoService playerInfoService, BGPrivateRoomInfoService bgPrivateRoomInfoService,
                                              MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService, RoomPlayersMonitorService roomPlayersMonitorService) {
        super(gson, lobbySessionService, lobbyManager, singleNodeRoomInfoService, multiNodeRoomInfoService, serverConfigService, roomServiceFactory,
                roomTemplateService, playerInfoService, bgPrivateRoomInfoService, multiNodePrivateRoomInfoService, roomPlayersMonitorService);
    }

    @Override
    public Logger getLog() {
        return LOG;
    }

    @Override
    long getRequestedRoomIdFromMessage(GetPrivateBattlegroundStartGameUrl getPrivateBattlegroundStartGameUrl) {
        return -1;
    }

    @Override
    public boolean checkIsBadMessageAndSendError(GetPrivateBattlegroundStartGameUrl message, ILobbySocketClient client,
                                                 ILobbySession lobbySession) {
        String privateRoomId = message.getPrivateRoomId();

        if (StringUtils.isTrimmedEmpty(privateRoomId)) {
            sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "privateRoomId is empty", message.getRid());
            return true;
        }

        long battlegroundBuyIn = -1;
        boolean isDeactivated = false;
        boolean roomInfoFound = false;

        BGPrivateRoomInfo bgPrivateRoomInfo = bgPrivateRoomInfoService.getRoomByPrivateRoomId(privateRoomId);
        if(bgPrivateRoomInfo != null) {
            roomInfoFound = true;
            isDeactivated = bgPrivateRoomInfo.isDeactivated();
            battlegroundBuyIn = bgPrivateRoomInfo.getStake().toCents();

        } else {
            MultiNodePrivateRoomInfo multiNodePrivateRoomInfo = multiNodePrivateRoomInfoService.getRoomByPrivateRoomId(privateRoomId);
            if(multiNodePrivateRoomInfo != null) {
                roomInfoFound = true;
                isDeactivated = multiNodePrivateRoomInfo.isDeactivated();
                battlegroundBuyIn = multiNodePrivateRoomInfo.getStake().toCents();
            }
        }

        if (!roomInfoFound) {
            LOG.error("Private room {} not found ", privateRoomId);
            sendErrorMessage(client, ErrorCodes.ROOM_NOT_FOUND, "Private room not found", message.getRid());
            return true;
        }

        if (isDeactivated) {
            LOG.error("Private room {} was deactivated ", privateRoomId);
            sendErrorMessage(client, ErrorCodes.ROOM_WAS_DEACTIVATED, "Private room was deactivated", message.getRid());
            return true;
        }

        if(!lobbySession.isBattlegroundAllowed()) {
            LOG.error("Battleground is not allowed for room {}", privateRoomId);
            sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Battleground is not allowed", message.getRid());
            return true;
        }

        if (lobbySession.getBattlegroundBuyIns() == null || !lobbySession.getBattlegroundBuyIns().contains(battlegroundBuyIn)) {
            LOG.error("BuyIn {} not allowed, allowed only {}, client={} ", battlegroundBuyIn, lobbySession.getBattlegroundBuyIns(), client);
            sendErrorMessage(client, ErrorCodes.BAD_REQUEST, "Illegal BuyIn value", message.getRid());
            return true;
        }

        return false;
    }

    @Override
    IRoomInfo getBestRoomForStake(LobbySession lobbySession, GameType gameType, ILobbySocketClient client, String currency, GetPrivateBattlegroundStartGameUrl message) throws CommonException {
        if (client.getMoneyType() != MoneyType.REAL) {
            throw new CommonException("Incorrect mode found in lobby session");
        }
        String privateRoomId = message.getPrivateRoomId();
        AbstractRoomInfo roomInfo = bgPrivateRoomInfoService.getRoomByPrivateRoomId(privateRoomId);
        if(roomInfo == null) {
            roomInfo = multiNodePrivateRoomInfoService.getRoomByPrivateRoomId(privateRoomId);
        }
        LOG.debug("Private room: {}", roomInfo);
        return roomInfo;
    }

    @Override
    Money getStakeFromMessage(GetPrivateBattlegroundStartGameUrl message) throws CommonException {
        String privateRoomId = message.getPrivateRoomId();
        AbstractRoomInfo roomInfo = this.bgPrivateRoomInfoService.getRoomByPrivateRoomId(privateRoomId);
        if (roomInfo == null) {
            roomInfo = multiNodePrivateRoomInfoService.getRoomByPrivateRoomId(privateRoomId);
            if(roomInfo == null) {
                throw new CommonException(String.format("Room with privateId: %s not found", privateRoomId));
            }
        }
        return roomInfo.getStake();
    }
}
