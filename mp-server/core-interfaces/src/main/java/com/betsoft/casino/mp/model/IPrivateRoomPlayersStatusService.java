package com.betsoft.casino.mp.model;

import com.betsoft.casino.mp.model.friends.Friend;
import com.betsoft.casino.mp.model.friends.Friends;
import com.betsoft.casino.mp.model.friends.UpdateFriendsResponse;
import com.betsoft.casino.mp.model.onlineplayer.OnlinePlayer;
import com.betsoft.casino.mp.model.onlineplayer.UpdateOnlinePlayersResponse;
import com.betsoft.casino.mp.model.privateroom.PrivateRoom;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.service.IPrivateRoomInfoService;
import com.betsoft.casino.mp.service.IRoomServiceFactory;
import com.betsoft.casino.mp.service.ISocketService;
import com.abs.casino.common.util.string.StringUtils;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.MapEvent;
import com.hazelcast.map.listener.MapListener;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IPrivateRoomPlayersStatusService {

    interface IPrivateRoomChangeHandler {
        String getPrivateRoomId();
        void processPrivateRoomChange(PrivateRoom privateRoom);
    }

    class PrivateRoomListener implements EntryListener<Long, IRoomInfo> {

        protected final IPrivateRoomChangeHandler privateRoomChangeHandler;
        public PrivateRoomListener(IPrivateRoomChangeHandler privateRoomChangeHandler) {
            this.privateRoomChangeHandler = privateRoomChangeHandler;
        }
        @Override
        public void entryAdded(EntryEvent event) {
            if(privateRoomChangeHandler != null
                    && !StringUtils.isTrimmedEmpty(privateRoomChangeHandler.getPrivateRoomId())
                    && event.getKey().equals(privateRoomChangeHandler.getPrivateRoomId())) {
                privateRoomChangeHandler.processPrivateRoomChange((PrivateRoom)event.getValue());
            }
        }

        @Override
        public void entryEvicted(EntryEvent event) {
        }

        @Override
        public void entryRemoved(EntryEvent event) {
        }

        @Override
        public void entryUpdated(EntryEvent event) {
            if(privateRoomChangeHandler != null
                    && !StringUtils.isTrimmedEmpty(privateRoomChangeHandler.getPrivateRoomId())
                    && event.getKey().equals(privateRoomChangeHandler.getPrivateRoomId())) {
                privateRoomChangeHandler.processPrivateRoomChange((PrivateRoom)event.getValue());
            }
        }

        @Override
        public void mapCleared(MapEvent event) {

        }

        @Override
        public void mapEvicted(MapEvent event) {

        }
    }

    interface IFriendsChangeHandler {
        String getOwnerExternalId();
        void processFriendsChange(Friends friends);
    }

    class FriendsListener implements EntryListener<Long, IRoomInfo> {

        protected final IFriendsChangeHandler friendsChangeHandler;
        public FriendsListener(IFriendsChangeHandler friendsChangeHandler) {
            this.friendsChangeHandler = friendsChangeHandler;
        }
        @Override
        public void entryAdded(EntryEvent event) {
            if(friendsChangeHandler != null
                    && !StringUtils.isTrimmedEmpty(friendsChangeHandler.getOwnerExternalId())
                    && event.getKey().equals(friendsChangeHandler.getOwnerExternalId())) {
                friendsChangeHandler.processFriendsChange((Friends)event.getValue());
            }
        }

        @Override
        public void entryEvicted(EntryEvent event) {
        }

        @Override
        public void entryRemoved(EntryEvent event) {
        }

        @Override
        public void entryUpdated(EntryEvent event) {
            if(friendsChangeHandler != null
                    && !StringUtils.isTrimmedEmpty(friendsChangeHandler.getOwnerExternalId())
                    && event.getKey().equals(friendsChangeHandler.getOwnerExternalId())) {
                friendsChangeHandler.processFriendsChange((Friends)event.getValue());
            }
        }

        @Override
        public void mapCleared(MapEvent event) {

        }

        @Override
        public void mapEvicted(MapEvent event) {

        }
    }

    interface IOnlinePlayerChangeHandler {
        Set<String> getOnlinePlayerExternalIds();
        void processOnlinePlayerChange(OnlinePlayer onlinePlayer);
    }

    class OnlinePlayersListener implements EntryListener<Long, IRoomInfo> {

        protected final IOnlinePlayerChangeHandler onlinePlayerChangeHandler;

        public OnlinePlayersListener(IOnlinePlayerChangeHandler onlinePlayerChangeHandler) {
            this.onlinePlayerChangeHandler = onlinePlayerChangeHandler;
        }

        @Override
        public void entryAdded(EntryEvent event) {
            if(onlinePlayerChangeHandler != null) {
                Set<String> onlinePlayerExternalIds = onlinePlayerChangeHandler.getOnlinePlayerExternalIds();
                if (onlinePlayerExternalIds != null && !onlinePlayerExternalIds.isEmpty() && onlinePlayerExternalIds.contains(event.getKey())) {
                    onlinePlayerChangeHandler.processOnlinePlayerChange((OnlinePlayer) event.getValue());
                }
            }
        }

        @Override
        public void entryEvicted(EntryEvent event) {
        }

        @Override
        public void entryRemoved(EntryEvent event) {
        }

        @Override
        public void entryUpdated(EntryEvent event) {
            if(onlinePlayerChangeHandler != null) {
                Set<String> onlinePlayerExternalIds = onlinePlayerChangeHandler.getOnlinePlayerExternalIds();
                if (onlinePlayerExternalIds != null && !onlinePlayerExternalIds.isEmpty() && onlinePlayerExternalIds.contains(event.getKey())) {
                    onlinePlayerChangeHandler.processOnlinePlayerChange((OnlinePlayer) event.getValue());
                }
            }
        }

        @Override
        public void mapCleared(MapEvent event) {

        }

        @Override
        public void mapEvicted(MapEvent event) {

        }
    }

    String registerPrivateRoomListener(MapListener listener);

    boolean unregisterPrivateRoomListener(String listenerId);

    String registerFriendsListener(MapListener listener);

    boolean unregisterFriendsListener(String listenerId);

    String registerOnlinePlayersListener(MapListener listener);

    boolean unregisterOnlinePlayersListener(String listenerId);


    UpdatePrivateRoomResponse updatePlayersStatusInPrivateRoom(PrivateRoom privateRoom,
                                                               boolean isTransitionLimited, boolean updateTime);
    PrivateRoom getPrivateRoom(String privateRoomId);

    void setSocketService(ISocketService socketService);

    ISocketService getSocketService();

    void setBGPrivateRoomInfoService(IPrivateRoomInfoService bgPrivateRoomInfoService);

    IPrivateRoomInfoService getBGPrivateRoomInfoService();

    void setMultiNodePrivateRoomInfoService(IPrivateRoomInfoService multiNodePrivateRoomInfoService);

    IPrivateRoomInfoService getMultiNodePrivateRoomInfoService();

    void setRoomServiceFactory(IRoomServiceFactory roomServiceFactory);

    IRoomServiceFactory getRoomServiceFactory();

    void sendPlayerStatusInPrivateRoomToCanex(int serverId, int bankId, String privateRoomId,
                                                             String nickname, String externalId, long accountId,
                                                             Status tbgStatus);

    Friends getFriendsForExternalId(String externalId);

    UpdateFriendsResponse updateFriends(Friends friends);

    UpdateOnlinePlayersResponse updateOnlinePlayers(List<OnlinePlayer> onlinePlayers);

    Map<String, OnlinePlayer> getOnlinePlayers(Set<String> externalIds);

    boolean invitePlayersToPrivateRoomAtCanex(int serverId, Set<Friend> friends, String privateRoomId);

    List<Friend> getFriendsFromCanex(int serverId, Friend friend);

    List<OnlinePlayer> getOnlinePlayersNotInPrivateRoom(IRoomInfo roomInfo);

    Map<String, Friend> getFriendsNotInPrivateRoom(IRoomInfo roomInfo);

    PrivateRoom getPrivateRoomPlayersStatus(IRoomInfo roomInfo);

    String getOwnerExternalId(IRoomInfo roomInfo);

    String getPrivateRoomId(IRoom room, IRoomInfo roomInfo);

    List<com.betsoft.casino.mp.model.onlineplayer.Friend> convertOnlinePlayersToGameInfoFriends(
            List<OnlinePlayer> onlinePlayers);

    void getFriendsFromCanexAsync(int serverId, Friend friend);

    List<OnlinePlayer> getOnlineStatusFromCanex(int serverId, Collection<Friend> friends);

    void getOnlineStatusFromCanexAsync(int serverId, Collection<Friend> friends);
}
