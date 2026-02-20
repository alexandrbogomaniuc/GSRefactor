package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.privateroom.PrivateRoom;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.web.IGameSocketClient;

import java.util.List;

public interface ICAFRoom {

    PrivateRoom getPrivateRoomPlayersStatus();

    UpdatePrivateRoomResponse updatePlayersStatusNicknamesOnly(List<String> nicknames, Status status, boolean isTransitionLimited, boolean updateTime);

    UpdatePrivateRoomResponse updatePlayersStatusAndSendToOwnerNicknamesOnly(List<String> nicknames, Status status);

    void updatePlayerStatusForObserver(String nickname, Status status);

    UpdatePrivateRoomResponse updatePlayersStatusAndSendToOwner(List<IGameSocketClient> clients, Status status);

    UpdatePrivateRoomResponse updatePlayersStatus(List<IGameSocketClient> clients, Status status, boolean isTransitionLimited,
                                                  boolean updateTime);

    String getPrivateRoomId();

    IGameSocketClient getOwnerClient();

    void sendGameInfoToOwner();

    void sendGameInfoToAllObservers();

    void sendGameInfoToSocketClient(IGameSocketClient socketClient);

    default void sendPlayerStatusInPrivateRoomToCanex(String privateRoomId,
                                                      IGameSocketClient gameSocketClient,
                                                      String externalId,
                                                      Status tbgStatus) {

    }

    void sendPlayerStatusInPrivateRoomToCanex(String privateRoomId,
                                              int serverId,
                                              int bankId,
                                              String nickname,
                                              String externalId,
                                              Long accountId,
                                              Status tbgStatus);

    boolean invitePlayersToPrivateRoomAtCanex(List<String> nicknames);
}
