package com.abs.casino.common.client;

import com.abs.casino.common.client.canex.request.friends.GetFriendsResponse;
import com.abs.casino.common.client.canex.request.onlineplayer.GetOnlinePlayersResponse;
import com.abs.casino.common.client.canex.request.onlinerooms.Room;
import com.abs.casino.common.client.canex.request.privateroom.PrivateRoom;
import com.abs.casino.common.exception.CommonException;

import java.util.List;

/** Mark common-wallet client as client which use JSON in request and response body */
public interface IJsonCWClient {

    boolean updatePlayerStatusInPrivateRoom(PrivateRoom privateRoom) throws CommonException;

    boolean invitePlayersToPrivateRoom(List<String> externalIds, String privateRoomId) throws CommonException;

    GetFriendsResponse getFriends(String externalId, String nickname) throws CommonException;

    GetOnlinePlayersResponse getOnlineStatus(List<String> externalIds) throws CommonException;

    boolean pushRoomsPlayers(List<Room> rooms) throws CommonException;
}
