package com.betsoft.casino.mp.web.handlers;

import com.betsoft.casino.mp.common.AchievementHelper;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.IRoomInfoService;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.transport.Seat;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.ISocketClient;
import com.betsoft.casino.mp.web.service.LobbyManager;
import com.betsoft.casino.utils.TInboundObject;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class MessageHandler<MESSAGE extends TInboundObject, CLIENT extends ISocketClient>
        implements IMessageHandler<MESSAGE, CLIENT> {
    protected Gson gson;
    protected LobbySessionService lobbySessionService;
    protected LobbyManager lobbyManager;

    public MessageHandler(Gson gson, LobbySessionService lobbySessionService, LobbyManager lobbyManager) {
        this.gson = gson;
        this.lobbySessionService = lobbySessionService;
        this.lobbyManager = lobbyManager;
    }

    protected boolean checkLogin(MESSAGE message, ISocketClient client) {
        if (client.isLoggedIn()) {
            return true;
        } else {
            client.sendMessage(createErrorMessage(ErrorCodes.NOT_LOGGED_IN, "Not logged in", message.getRid()),
                    message);
            return false;
        }
    }

    public static List<ITransportSeat> convert(Collection<IRoomPlayerInfo> playerInfos) {
        List<ITransportSeat> seats = new ArrayList<>(playerInfos.size());
        for (IRoomPlayerInfo info : playerInfos) {
            if (info != null) {
                IActiveFrbSession frbSession = info.getActiveFrbSession();
                long roundWin = frbSession != null ? frbSession.getWinSum() : 0;
                seats.add(new Seat(info.getSeatNumber(), info.getNickname(), info.getEnterDate(),
                        info.getTotalScore().getAmount(), info.getCurrentScore().getAmount(),
                        info.getAvatar(),
                        info instanceof IActionRoomPlayerInfo ? (((IActionRoomPlayerInfo)info).getSpecialWeaponId()) : -1,
                        AchievementHelper.getPlayerLevel(info.getTotalScore()), 0, 0, roundWin));
            }
        }
        return seats;
    }

    protected IRoomInfo getRoomInfo(int requestId, long roomId, CLIENT client, IRoomInfoService roomInfoService) {
        IRoomInfo roomInfo = roomInfoService.getRoom(roomId);
        if (roomInfo == null) {
            sendErrorMessage(client, ErrorCodes.ROOM_NOT_FOUND, "RoomInfo not found", requestId);
            return null;
        }
        return roomInfo;
    }

    protected boolean isLeaderboardDisabled(CLIENT client) {
        ILobbySession session = lobbySessionService.get(client.getSessionId());
        if (session != null) {
            return session.isLeaderboardDisabled();
        }
        return true;
    }
}
