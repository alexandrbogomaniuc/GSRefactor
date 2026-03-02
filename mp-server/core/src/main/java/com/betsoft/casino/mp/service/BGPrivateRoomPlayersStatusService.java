package com.betsoft.casino.mp.service;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.friends.Friend;
import com.betsoft.casino.mp.model.friends.Friends;
import com.betsoft.casino.mp.model.friends.UpdateFriendsResponse;
import com.betsoft.casino.mp.model.onlineplayer.OnlinePlayer;
import com.betsoft.casino.mp.model.onlineplayer.UpdateOnlinePlayersResponse;
import com.betsoft.casino.mp.model.privateroom.Player;
import com.betsoft.casino.mp.model.privateroom.PrivateRoom;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.privateroom.UpdatePrivateRoomResponse;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.abs.casino.common.util.string.StringUtils;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.MapListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BGPrivateRoomPlayersStatusService implements IPrivateRoomPlayersStatusService {
    private static final Logger LOG = LogManager.getLogger(BGPrivateRoomPlayersStatusService.class);
    public static final String ROOM_PLAYER_STATUS_STORE = "BGPrivateRoomPlayersStatusesStore";
    public static final String FRIENDS_STORE = "FriendsStore";
    public static final String ONLINE_PLAYER_STORE = "OnlinePlayerStore";
    protected final AsyncExecutorService asyncExecutorService;
    protected HazelcastInstance hazelcast;
    protected IRoomServiceFactory roomServiceFactory;
    protected BGPrivateRoomInfoService bgPrivateRoomInfoService;
    protected MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService;
    protected ISocketService socketService;

    protected IMap<String, PrivateRoom> privateRoomPlayersStatuses;
    protected IMap<String, Friends> friends;
    protected IMap<String, OnlinePlayer> onlinePlayers;

    public BGPrivateRoomPlayersStatusService(HazelcastInstance hazelcast, AsyncExecutorService asyncExecutorService) {
        this.hazelcast = hazelcast;
        this.asyncExecutorService = asyncExecutorService;
    }

    @PostConstruct
    protected void init() {
        privateRoomPlayersStatuses = hazelcast.getMap(ROOM_PLAYER_STATUS_STORE);
        privateRoomPlayersStatuses.addIndex("privateRoomId", true);
        privateRoomPlayersStatuses.addIndex("ownerNickname", false);
        privateRoomPlayersStatuses.addIndex("ownerExternalId", false);

        friends = hazelcast.getMap(FRIENDS_STORE);
        friends.addIndex("nickname", false);
        friends.addIndex("externalId", false);

        onlinePlayers = hazelcast.getMap(ONLINE_PLAYER_STORE);
        onlinePlayers.addIndex("nickname", false);
        onlinePlayers.addIndex("externalId", false);

        LOG.info("init: completed");
    }

    @Override
    public String registerPrivateRoomListener(MapListener listener) {
        String listenerId = privateRoomPlayersStatuses.addEntryListener(listener, true);
        LOG.debug("registerPrivateRoomListener: {}, {}", listenerId, listener);
        return listenerId;
    }

    @Override
    public boolean unregisterPrivateRoomListener(String listenerId) {
        LOG.debug("unregisterPrivateRoomListener: {}", listenerId);
        return privateRoomPlayersStatuses.removeEntryListener(listenerId);
    }

    @Override
    public String registerFriendsListener(MapListener listener) {
        String listenerId = friends.addEntryListener(listener, true);
        LOG.debug("registerFriendsListener: {}, {}", listenerId, listener);
        return listenerId;
    }

    @Override
    public boolean unregisterFriendsListener(String listenerId) {
        LOG.debug("unregisterFriendsListener: {}", listenerId);
        return friends.removeEntryListener(listenerId);
    }

    @Override
    public String registerOnlinePlayersListener(MapListener listener) {
        String listenerId = onlinePlayers.addEntryListener(listener, true);
        LOG.debug("registerOnlinePlayersListener: {}, {}", listenerId, listener);
        return listenerId;
    }

    @Override
    public boolean unregisterOnlinePlayersListener(String listenerId) {
        LOG.debug("unregisterOnlinePlayersListener: {}", listenerId);
        return onlinePlayers.removeEntryListener(listenerId);
    }

    public void privateRoomLock(String privateRoomId) {
        privateRoomPlayersStatuses.lock(privateRoomId);
    }

    public void privateRoomUnlock(String privateRoomId) {
        privateRoomPlayersStatuses.unlock(privateRoomId);
    }

    public void privateRoomForceUnlock(String privateRoomId) {
        privateRoomPlayersStatuses.forceUnlock(privateRoomId);
    }

    public boolean privateRoomIsLocked(String privateRoomId) {
        return privateRoomPlayersStatuses.isLocked(privateRoomId);
    }

    public boolean privateRoomTryLock(String privateRoomId) {
        return privateRoomPlayersStatuses.tryLock(privateRoomId);
    }

    public boolean privateRoomTryLock(String privateRoomId, long time, TimeUnit timeunit) throws InterruptedException {
        return privateRoomPlayersStatuses.tryLock(privateRoomId, time, timeunit);
    }

    public Collection<PrivateRoom> privateRoomGetAll() {
        return privateRoomPlayersStatuses.values();
    }

    public void friendsLock(String externalId) {
        friends.lock(externalId);
    }

    public void friendsUnlock(String externalId) {
        friends.unlock(externalId);
    }

    public void friendsForceUnlock(String externalId) {
        friends.forceUnlock(externalId);
    }

    public boolean friendsIsLocked(String externalId) {
        return friends.isLocked(externalId);
    }

    public boolean friendsTryLock(String externalId) {
        return friends.tryLock(externalId);
    }

    public boolean friendsTryLock(String externalId, long time, TimeUnit timeunit) throws InterruptedException {
        return friends.tryLock(externalId, time, timeunit);
    }

    public Collection<Friends> friendsGetAll() {
        return friends.values();
    }

    public void onlinePlayersLock(String externalId) {
        onlinePlayers.lock(externalId);
    }

    public void onlinePlayersUnlock(String externalId) {
        onlinePlayers.unlock(externalId);
    }

    public void onlinePlayersForceUnlock(String externalId) {
        onlinePlayers.forceUnlock(externalId);
    }

    public boolean onlinePlayersIsLocked(String externalId) {
        return onlinePlayers.isLocked(externalId);
    }

    public boolean onlinePlayersTryLock(String externalId) {
        return onlinePlayers.tryLock(externalId);
    }

    public boolean onlinePlayersTryLock(String externalId, long time, TimeUnit timeunit) throws InterruptedException {
        return onlinePlayers.tryLock(externalId, time, timeunit);
    }

    public Collection<OnlinePlayer> onlinePlayersGetAll() {
        return onlinePlayers.values();
    }

    @Override
    public ISocketService getSocketService() {
        return socketService;
    }

    @Override
    public void setSocketService(ISocketService socketService) {
        this.socketService = socketService;
    }

    @Override
    public void setBGPrivateRoomInfoService(IPrivateRoomInfoService bgPrivateRoomInfoService) {
        this.bgPrivateRoomInfoService = (BGPrivateRoomInfoService)bgPrivateRoomInfoService;
    }

    @Override
    public IPrivateRoomInfoService getBGPrivateRoomInfoService() {
        return bgPrivateRoomInfoService;
    }

    @Override
    public void setMultiNodePrivateRoomInfoService(IPrivateRoomInfoService multiNodePrivateRoomInfoService) {
        this.multiNodePrivateRoomInfoService = (MultiNodePrivateRoomInfoService)multiNodePrivateRoomInfoService;
    }

    @Override
    public IPrivateRoomInfoService getMultiNodePrivateRoomInfoService() {
        return multiNodePrivateRoomInfoService;
    }

    @Override
    public void setRoomServiceFactory(IRoomServiceFactory roomServiceFactory) {
        this.roomServiceFactory = roomServiceFactory;
    }

    @Override
    public IRoomServiceFactory getRoomServiceFactory() {
        return roomServiceFactory;
    }

    @Override
    public Friends getFriendsForExternalId(String externalId) {
        if (friends != null) {
            return friends.get(externalId);
        }
        return null;
    }

    @Override
    public UpdateFriendsResponse updateFriends(Friends newFriends) {

        //Create Direct Link Friend->Friends
        UpdateFriendsResponse updateFriendsResponse = _updateFriends(newFriends);

        //Create Backwards Link Friends->Friend
        if (newFriends != null && newFriends.getFriends() != null && newFriends.getFriends().size() > 0
                && !StringUtils.isTrimmedEmpty(newFriends.getExternalId())) {

            LOG.debug("updateFriends: To create back friend links for {}", newFriends);

            Friend requesterFriend = new Friend(
                    newFriends.getNickname(),
                    newFriends.getExternalId(),
                    null);

            Map<String, Friend> requesterFriendAsMap = new HashMap<>();
            requesterFriendAsMap.put(requesterFriend.getExternalId(), requesterFriend);


            for (Friend newFriend : newFriends.getFriends().values()) {

                LOG.debug("updateFriends: newFriend.getStatus() {}", newFriend.getStatus());
                if (newFriend.getStatus() == com.betsoft.casino.mp.model.friends.Status.friend
                        || newFriend.getStatus() == com.betsoft.casino.mp.model.friends.Status.rejected
                        || newFriend.getStatus() == com.betsoft.casino.mp.model.friends.Status.blocked) {
                    requesterFriend.setStatus(newFriend.getStatus());

                    Friends backFriends = new Friends(
                            newFriend.getNickname(),
                            newFriend.getExternalId(),
                            requesterFriendAsMap);

                    UpdateFriendsResponse updateBackFriendsResponse = _updateFriends(backFriends);

                    if (updateBackFriendsResponse != null && !StringUtils.isTrimmedEmpty(updateBackFriendsResponse.getMessage())) {
                        updateFriendsResponse.addMessage(updateBackFriendsResponse.getMessage());
                    }
                } else {
                    LOG.debug("updateFriends: newFriend.getStatus() is not {} skip processing for {}",
                            com.betsoft.casino.mp.model.friends.Status.friend, newFriend);
                }
            }
        } else {
            LOG.debug("updateFriends: Skip to create back friend links for {}", newFriends);
        }

        return updateFriendsResponse;
    }


    protected UpdateFriendsResponse _updateFriends(Friends newFriends) {

        UpdateFriendsResponse response = new UpdateFriendsResponse();

        LOG.debug("updateFriends: newFriends={}", newFriends);

        if (newFriends == null) {
            LOG.error("updateFriends: newFriends is null");
            response.setMessage("newFriends is null");
            response.setCode(400);
            return response;
        }

        if (StringUtils.isTrimmedEmpty(newFriends.getExternalId())) {
            LOG.error("updateFriends: ExternalId is empty: {}", newFriends);
            response.setMessage("ExternalId is empty");
            response.setCode(400);
            return response;
        }

        if (newFriends.getFriends() == null) {
            newFriends.setFriends(new HashMap<>());
        }

        String externalId = newFriends.getExternalId();

        try {
            friendsLock(externalId);
            Friends existingFriends = friends.get(externalId);
            boolean saveChanges = true;

            if (existingFriends == null) {

                LOG.debug("updateFriends: no existing Friends were found " +
                        "for {}, add it", externalId);
                existingFriends = newFriends;

            } else {
                if (existingFriends.equals(newFriends)) {
                    LOG.debug("updateFriends: existingFriends is equal to newFriends skip update" +
                            "for {}", newFriends);
                    saveChanges = false;
                } else {
                    LOG.debug("updateFriends: existing Friends were found " +
                            "for {}, update it", externalId);

                    //update existingFriends fields if these are present in newFriends
                    if (!StringUtils.isTrimmedEmpty(newFriends.getNickname())) {
                        existingFriends.setNickname(newFriends.getNickname());
                    }
                    if (!StringUtils.isTrimmedEmpty(newFriends.getExternalId())) {
                        existingFriends.setExternalId(newFriends.getExternalId());
                    }

                    if (existingFriends.getFriends() == null) {
                        LOG.debug("updateFriends: existingFriends.getFriends() " +
                                "is null, use incoming friends map, for externalId={} ", externalId);
                        existingFriends.setFriends(newFriends.getFriends());
                    } else {
                        for (Friend newFriend : newFriends.getFriends().values()) {

                            if (StringUtils.isTrimmedEmpty(newFriend.getNickname())) {
                                LOG.debug("updateFriends: friend's nickname is empty skip {}", newFriend);
                                response.addMessage("skip player: " + newFriend);
                                continue;
                            }

                            if (StringUtils.isTrimmedEmpty(newFriend.getExternalId())) {
                                LOG.debug("updateFriends: friend's externalId is empty skip {}", newFriend);
                                response.addMessage("skip player: " + newFriend);
                                continue;
                            }

                            Friend existingFriend = existingFriends.getFriend(newFriend.getExternalId());

                            if (existingFriend == null) {
                                LOG.debug("updateFriends: add friend {} to the map " +
                                        "of friends in the existing Friends", newFriend);
                                existingFriends.getFriends().put(newFriend.getExternalId(), newFriend);
                            } else {

                                if (existingFriend.equals(newFriend)) {
                                    LOG.debug("updateFriends: existingFriend is equal to newFriend skip update" +
                                            "for {}", newFriend);
                                } else {
                                    LOG.debug("updateFriends: update existing newFriend {} in the map " +
                                            "of friends by {}", existingFriend, newFriend);

                                    //update existingFriend fields if these are present in newPlayer
                                    if (!StringUtils.isTrimmedEmpty(newFriend.getExternalId())) {
                                        existingFriend.setExternalId(newFriend.getExternalId());
                                    }

                                    if (!StringUtils.isTrimmedEmpty(newFriend.getNickname())) {
                                        existingFriend.setNickname(newFriend.getNickname());
                                    }

                                    if (newFriend.getStatus() != null) {
                                        existingFriend.setStatus(newFriend.getStatus());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (saveChanges) {
                existingFriends.setUpdateTime(System.currentTimeMillis());
                friends.set(externalId, existingFriends);
            }

        } catch (Exception e) {
            LOG.error("updateFriends: exception during Friends update {}, message:{}",
                    newFriends, e.getMessage(), e);
            response.setCode(500);
            response.addMessage("exception during Friends update");
            response.setFriends(null);
            return response;
        } finally {
            friendsUnlock(externalId);
        }

        Friends friendsUpdated = friends.get(externalId);

        if (friendsUpdated != null) {
            response.setCode(200);
            response.addMessage("updated");
        } else {
            response.setCode(400);
            response.addMessage("Friends is null");
        }

        response.setFriends(friendsUpdated);

        return response;
    }

    @Override
    public UpdateOnlinePlayersResponse updateOnlinePlayers(List<OnlinePlayer> newOnlinePlayers) {

        UpdateOnlinePlayersResponse response = new UpdateOnlinePlayersResponse();

        LOG.debug("updateOnlinePlayers: newOnlinePlayers={}", newOnlinePlayers);

        if (newOnlinePlayers == null) {
            LOG.error("updateOnlinePlayers: newOnlinePlayers is null");
            response.setMessage("newOnlinePlayers is null");
            response.setCode(400);
            return response;
        }

        if (newOnlinePlayers.size() == 0) {
            LOG.error("updateOnlinePlayers: newOnlinePlayers is empty");
            response.setMessage("newOnlinePlayers is empty");
            response.setCode(400);
            return response;
        }

        StringBuilder sbError = new StringBuilder();
        boolean isError = false;
        List<OnlinePlayer> onlinePlayersUpdated = new ArrayList<>();

        for (OnlinePlayer newOnlinePlayer : newOnlinePlayers) {
            if (StringUtils.isTrimmedEmpty(newOnlinePlayer.getExternalId())) {
                LOG.error("updateOnlinePlayers: ExternalId is empty: {}", newOnlinePlayer);
                sbError.append("ExternalId is empty for ").append(newOnlinePlayer).append(";");
                isError = true;
                continue;
            }
            boolean saveChanges = true;
            String externalId = newOnlinePlayer.getExternalId();
            try {
                onlinePlayersLock(externalId);
                OnlinePlayer exisitngOnlinePlayer = onlinePlayers.get(externalId);

                if (exisitngOnlinePlayer == null) {

                    LOG.debug("updateOnlinePlayers: no existing OnlinePlayer was found " +
                            "for {}, add it", externalId);
                    exisitngOnlinePlayer = newOnlinePlayer;
                } else {

                    if (exisitngOnlinePlayer.equals(newOnlinePlayer)) {

                        LOG.debug("updateOnlinePlayers: exisitngOnlinePlayer is equal to newOnlinePlayer skip update" +
                                "for {}", newOnlinePlayer);
                        saveChanges = false;

                    } else {
                        LOG.debug("updateOnlinePlayers: existing OnlinePlayer was found " +
                                "for {}, update it", externalId);

                        //update OnlinePlayer fields if these are present in newFriends
                        if (!StringUtils.isTrimmedEmpty(newOnlinePlayer.getNickname())) {
                            exisitngOnlinePlayer.setNickname(newOnlinePlayer.getNickname());
                        }

                        if (!StringUtils.isTrimmedEmpty(newOnlinePlayer.getExternalId())) {
                            exisitngOnlinePlayer.setExternalId(newOnlinePlayer.getExternalId());
                        }

                        if (newOnlinePlayer.getStatus() != null) {
                            exisitngOnlinePlayer.setStatus(newOnlinePlayer.getStatus());
                        }
                    }
                }

                if (saveChanges) {
                    onlinePlayers.set(externalId, exisitngOnlinePlayer);
                }

            } catch (Exception e) {
                LOG.error("updateOnlinePlayers: exception OnlinePlayers update {}, message:{}",
                        newOnlinePlayers, e.getMessage(), e);
                response.setCode(500);
                response.addMessage("exception during OnlinePlayers update");
                response.setOnlinePlayers(null);
                return response;
            } finally {
                onlinePlayersUnlock(externalId);
            }

            OnlinePlayer onlinePlayerUpdated = onlinePlayers.get(externalId);
            onlinePlayersUpdated.add(onlinePlayerUpdated);
        }

        if (!isError) {
            response.setCode(200);
            response.addMessage("updated");
        } else {
            response.setCode(400);
            response.addMessage(sbError.toString());
        }

        response.setOnlinePlayers(onlinePlayersUpdated);

        return response;
    }

    @Override
    public Map<String, OnlinePlayer> getOnlinePlayers(Set<String> externalIds) {
        return onlinePlayers.getAll(externalIds);
    }

    @Override
    public List<OnlinePlayer> getOnlinePlayersNotInPrivateRoom(IRoomInfo roomInfo) {

        if(roomInfo == null) {
            LOG.debug("getOnlinePlayersNotInPrivateRoom: roomInfo is null");
            return null;
        }

        Map<String, Friend> friendsNotInPrivateRoom = getFriendsNotInPrivateRoom(roomInfo);

        long roomId = roomInfo.getId();

        LOG.debug("getOnlinePlayersWithOnlineStatus: roomId={}, friendsNotInPrivateRoom: {}", roomId, friendsNotInPrivateRoom);

        if (friendsNotInPrivateRoom == null || friendsNotInPrivateRoom.isEmpty()) {
            LOG.debug("getOnlinePlayersWithOnlineStatus: roomId={}, friendsNotInPrivateRoom is empty", roomId);
        } else {
            Set<String> exteranlIds = friendsNotInPrivateRoom.keySet();

            Map<String, OnlinePlayer> onlinePlayersMap = getOnlinePlayers(exteranlIds);

            LOG.debug("getOnlinePlayersWithOnlineStatus: roomId={}, onlinePlayersMap: {}", roomId, onlinePlayersMap);

            return new ArrayList<>(onlinePlayersMap.values());
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Friend> getFriendsNotInPrivateRoom(IRoomInfo roomInfo) {

        if(roomInfo == null) {
            LOG.debug("getFriendsNotInPrivateRoom: roomInfo is null");
            return null;
        }

        long roomId = roomInfo.getId();
        if (roomInfo.isPrivateRoom()) {
            String ownerExternalId = getOwnerExternalId(roomInfo);
            if (StringUtils.isTrimmedEmpty(ownerExternalId)) {
                LOG.debug("getFriendsNotInPrivateRoom: roomId={}, ownerExternalId is empty for room:{}", roomId, roomInfo);
            } else {
                LOG.debug("getFriendsNotInPrivateRoom: roomId={}, ownerExternalId:{}", roomId, ownerExternalId);
                Friends friends = getFriendsForExternalId(ownerExternalId);

                Map<String, Friend> friendsMap = friends == null ? null : friends.getFriends();

                if (friendsMap == null || friendsMap.isEmpty()) {
                    LOG.debug("getFriendsNotInPrivateRoom: roomId={}, friendsMap is empty for ownerExternalId:{}", roomId, ownerExternalId);
                } else {

                    PrivateRoom privateRoom = getPrivateRoomPlayersStatus(roomInfo);

                    if (privateRoom != null && privateRoom.getPlayers() != null && !privateRoom.getPlayers().isEmpty()) {
                        LOG.debug("getFriendsNotInPrivateRoom: roomId={}, there are room players registered for the game {}" +
                                ", remove these from firendsMap {}", roomId, privateRoom.getPlayers(), friendsMap);

                        Set<String> privateRoomPlayersNickNames = privateRoom.getPlayers().stream()
                                .map(Player::getNickname)
                                .collect(Collectors.toSet());

                        friendsMap = friendsMap.entrySet().stream()
                                .filter(f -> f.getValue() != null
                                        && !StringUtils.isTrimmedEmpty(f.getValue().getNickname())
                                        && !privateRoomPlayersNickNames.contains(f.getValue().getNickname())
                                        && f.getValue().getStatus() == com.betsoft.casino.mp.model.friends.Status.friend)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    }
                }
                return friendsMap;
            }
        }
        return null;
    }

    @Override
    public PrivateRoom getPrivateRoomPlayersStatus(IRoomInfo roomInfo) {
        PrivateRoom privateRoom = null;

        if(roomInfo == null) {
            LOG.debug("getPrivateRoomPlayersStatus: roomInfo is null");
            return null;
        }

        if (roomInfo.isPrivateRoom()) {
            String privateRoomId = roomInfo.getPrivateRoomId();
            if (!StringUtils.isTrimmedEmpty(privateRoomId)) {
                privateRoom = getPrivateRoom(privateRoomId);
                if (privateRoom == null) {
                    LOG.error("getPrivateRoomInfoForRoom: privateRoom is null for privateRoomId: {}",
                            privateRoomId);
                }
            }
        }

        return privateRoom;
    }

    @Override
    public String getOwnerExternalId(IRoomInfo roomInfo) {

        if(roomInfo == null) {
            LOG.error("getOwnerExternalId: roomInfo is null");
            return null;
        }

        PrivateRoom privateRoom = getPrivateRoomPlayersStatus(roomInfo);
        if (privateRoom != null) {
            LOG.debug("getOwnerExternalId: ownerExternalId={}, for privateRoom:{}",
                    privateRoom.getOwnerExternalId(), privateRoom);
            return privateRoom.getOwnerExternalId();
        }

        LOG.error("getOwnerExternalId: privateRoom is null for {}", roomInfo);
        return null;
    }

    @Override
    public String getPrivateRoomId(IRoom room, IRoomInfo roomInfo) {

        if (room == null) {
            LOG.error("getPrivateRoomId: room is null");
            return null;
        }


        if (roomInfo == null) {
            LOG.error("getPrivateRoomId: roomInfo is null");
            return null;
        }

        if (room.isBattlegroundMode()) {

            if (roomInfo.isPrivateRoom()) {

                String privateRoomId = roomInfo.getPrivateRoomId();
                //LOG.debug("getPrivateRoomId: privateRoomId: {} for room id: {}", privateRoomId, roomInfo.getId());
                return privateRoomId;
            } else {
                LOG.error("getPrivateRoomId: isPrivateRoom is false: {}", roomInfo.getId());
            }

        } else {
            LOG.error("getPrivateRoomId: isBattlegroundMode is false: {}", roomInfo.getId());
        }
        return null;
    }

    @Override
    public List<com.betsoft.casino.mp.model.onlineplayer.Friend> convertOnlinePlayersToGameInfoFriends(
            List<OnlinePlayer> onlinePlayers) {

        List<com.betsoft.casino.mp.model.onlineplayer.Friend> friendsNotInPrivateRoom = new ArrayList<>();

        if (onlinePlayers != null && !onlinePlayers.isEmpty()) {

            for (OnlinePlayer onlinePlayer : onlinePlayers) {

                if (StringUtils.isTrimmedEmpty(onlinePlayer.getNickname())) {
                    LOG.error("convertOnlinePlayersToGameInfoFriends: nickname is empty: {}, skip it",
                            onlinePlayer);
                    continue;
                }

                com.betsoft.casino.mp.model.onlineplayer.Friend friend =
                        new com.betsoft.casino.mp.model.onlineplayer.Friend(
                                onlinePlayer.getNickname(),
                                onlinePlayer.isOnline());

                friendsNotInPrivateRoom.add(friend);
            }
        } else {
            LOG.debug("convertOnlinePlayersToGameInfoFriends: onlinePlayers is empty");
        }

        return friendsNotInPrivateRoom;
    }

    @Override
    public UpdatePrivateRoomResponse updatePlayersStatusInPrivateRoom(PrivateRoom newPrivateRoom,
                                                                      boolean isTransitionLimited, boolean updateTime) {

        UpdatePrivateRoomResponse response = new UpdatePrivateRoomResponse();

        LOG.debug("updatePlayersStatusInPrivateRoom: isTransitionLimited={}, updateTime={}," +
                " update Players Status In Private Room {}, ", isTransitionLimited, updateTime, newPrivateRoom);

        if (newPrivateRoom == null) {
            LOG.error("updatePlayersStatusInPrivateRoom: newPrivateRoom is null");
            response.setMessage("newPrivateRoom is null");
            response.setCode(400);
            return response;
        }

        if (StringUtils.isTrimmedEmpty(newPrivateRoom.getPrivateRoomId())) {
            LOG.error("updatePlayersStatusInPrivateRoom: privateRoomId is empty: {}", newPrivateRoom);
            response.setMessage("privateRoomId is empty");
            response.setCode(400);
            return response;
        }

        if (newPrivateRoom.getPlayers() == null) {
            newPrivateRoom.setPlayers(new ArrayList<>());
        }

        String privateRoomId = newPrivateRoom.getPrivateRoomId();
        List<Player> cancelKickPlayers = new ArrayList<>();

        try {
            privateRoomLock(privateRoomId);
            PrivateRoom existingPrivateRoom = privateRoomPlayersStatuses.get(privateRoomId);

            if (existingPrivateRoom == null) {

                LOG.debug("updatePlayersStatusInPrivateRoom: no existing private Room was found " +
                        "for {}, add it", privateRoomId);
                existingPrivateRoom = newPrivateRoom;

            } else {
                LOG.debug("updatePlayersStatusInPrivateRoom: existing private Room was found " +
                        "for {}, update it", privateRoomId);

                //update existingPrivateRoom fields if these are present in newPrivateRoom
                if (newPrivateRoom.getRoomId() != 0) {
                    existingPrivateRoom.setRoomId(newPrivateRoom.getRoomId());
                }
                if (newPrivateRoom.getOwnerAccountId() != 0) {
                    existingPrivateRoom.setOwnerAccountId(newPrivateRoom.getOwnerAccountId());
                }
                if (!StringUtils.isTrimmedEmpty(newPrivateRoom.getOwnerNickname())) {
                    existingPrivateRoom.setOwnerNickname(newPrivateRoom.getOwnerNickname());
                }
                if (!StringUtils.isTrimmedEmpty(newPrivateRoom.getOwnerExternalId())) {
                    existingPrivateRoom.setOwnerExternalId(newPrivateRoom.getOwnerExternalId());
                }

                if (existingPrivateRoom.getPlayers() == null) {
                    LOG.debug("updatePlayersStatusInPrivateRoom: existingPrivateRoom.getPlayers() " +
                            "is null, use incoming players list, for privateRoomId={} ", privateRoomId);
                    existingPrivateRoom.setPlayers(newPrivateRoom.getPlayers());
                } else {
                    for (Player newPlayer : newPrivateRoom.getPlayers()) {

                        if (StringUtils.isTrimmedEmpty(newPlayer.getNickname())) {
                            LOG.debug("updatePlayersStatusInPrivateRoom: player's nickname is empty skip {}", newPlayer);
                            response.addMessage("skip player: " + newPlayer);
                            continue;
                        }

                        Player existingPlayer = existingPrivateRoom.getPlayers().stream()
                                .filter(p -> !StringUtils.isTrimmedEmpty(p.getNickname())
                                        && p.getNickname().equals(newPlayer.getNickname()))
                                .findFirst()
                                .orElse(null);

                        if (existingPlayer == null) {
                            LOG.debug("updatePlayersStatusInPrivateRoom: add player {} to the list " +
                                    "of players in the existing Private Room", newPlayer);
                            existingPrivateRoom.getPlayers().add(newPlayer);
                        } else {

                            LOG.debug("updatePlayersStatusInPrivateRoom: update existing newPlayer {} in the list " +
                                    "of players in the room by {}", existingPlayer, newPlayer);

                            //update existingPlayer fields if these are present in newPlayer
                            if (newPlayer.getAccountId() != 0) {
                                existingPlayer.setAccountId(newPlayer.getAccountId());
                            }
                            if (!StringUtils.isTrimmedEmpty(newPlayer.getExternalId())) {
                                existingPlayer.setExternalId(newPlayer.getExternalId());
                            }
                            Status existingPlayerStatus = existingPlayer.getStatus();
                            Status newPlayerStatus = newPlayer.getStatus();
                            if ((isTransitionLimited && existingPlayerStatus.canTransitionWithLimitsTo(newPlayerStatus))
                                    || (!isTransitionLimited && existingPlayerStatus.canTransitionTo(newPlayerStatus))) {
                                LOG.debug("updatePlayersStatusInPrivateRoom: existingPlayerStatus={} " +
                                        "can Transit To newPlayerStatus={}", existingPlayerStatus, newPlayerStatus);
                                existingPlayer.setStatus(newPlayerStatus);

                                if((existingPlayerStatus == Status.KICKED && newPlayerStatus == Status.INVITED)
                                   || (existingPlayerStatus == Status.KICKED && newPlayerStatus == Status.WAITING)) {
                                    cancelKickPlayers.add(existingPlayer);
                                }

                            } else {
                                LOG.debug("updatePlayersStatusInPrivateRoom: existingPlayerStatus={} " +
                                        "can not Transit To newPlayerStatus={}", existingPlayerStatus, newPlayerStatus);
                                response.addMessage("Player " + existingPlayer + " with old status "
                                        + existingPlayerStatus + " can't Transit To new status " + newPlayerStatus);
                            }
                        }
                    }
                }
            }

            if (updateTime) {
                existingPrivateRoom.setUpdateTime(System.currentTimeMillis());
            }

            privateRoomPlayersStatuses.set(privateRoomId, existingPrivateRoom);

        } catch (Exception e) {
            LOG.error("updatePlayersStatusInPrivateRoom: exception during PrivateRoom update {}, message:{}",
                    newPrivateRoom, e.getMessage(), e);
            response.setCode(500);
            response.addMessage("exception during PrivateRoom update");
            response.setPrivateRoom(null);
            return response;
        } finally {
            privateRoomUnlock(privateRoomId);
        }

        //process cancelKick messages if there are any
        if (!cancelKickPlayers.isEmpty()) {
            LOG.debug("updatePlayersStatusInPrivateRoom: cancelKickPlayers={}", cancelKickPlayers);

            ICAFRoom room = this.getICAFRoom(privateRoomId);

            for (Player cancelKickPlayer : cancelKickPlayers) {
                this.cancelKick(room, cancelKickPlayer);
            }

            room.sendGameInfoToOwner();
        }

        PrivateRoom privateRoom = privateRoomPlayersStatuses.get(privateRoomId);

        if (privateRoom != null) {
            response.setCode(200);
            response.addMessage("updated");
        } else {
            response.setCode(400);
            response.addMessage("Private Room is null");
        }

        response.setPrivateRoom(privateRoom);

        return response;
    }

    @Override
    public PrivateRoom getPrivateRoom(String privateRoomId) {
        if (StringUtils.isTrimmedEmpty(privateRoomId)) {
            LOG.error("get: privateRoomId is null");
            return null;
        }
        return privateRoomPlayersStatuses.get(privateRoomId);
    }

    protected ICAFRoom getICAFRoom(String privateRoomId) {
        if (bgPrivateRoomInfoService == null) {
            LOG.error("getAbstractBattlegroundGameRoom: privateRoomInfoService bean is null");
            return null;
        }

        if (multiNodePrivateRoomInfoService == null) {
            LOG.error("getAbstractBattlegroundGameRoom: multiNodePrivateRoomInfoService bean is null");
            return null;
        }

        if (roomServiceFactory == null) {
            LOG.error("getAbstractBattlegroundGameRoom: roomServiceFactory bean is null");
            return null;
        }

        if (StringUtils.isTrimmedEmpty(privateRoomId)) {
            LOG.error("getAbstractBattlegroundGameRoom: privateRoomId is empty");
            return null;
        }

        IRoomInfo privateRoomInfo = this.bgPrivateRoomInfoService.getRoomByPrivateRoomId(privateRoomId);
        if (privateRoomInfo == null) {

            privateRoomInfo = this.multiNodePrivateRoomInfoService.getRoomByPrivateRoomId(privateRoomId);

            if (privateRoomInfo == null) {
                LOG.error("getAbstractBattlegroundGameRoom: privateRoomInfo is null, privateRoomId: {}", privateRoomId);
                return null;
            }
        }

        if (!privateRoomInfo.isPrivateRoom()) {
            LOG.error("getAbstractBattlegroundGameRoom: privateRoomInfo is not Private Room, privateRoomId: {}", privateRoomInfo);
            return null;
        }

        GameType gameType = privateRoomInfo.getGameType();
        long roomId = privateRoomInfo.getId();

        LOG.debug("getAbstractBattlegroundGameRoom: privateRoomId: {}, gameType={}, roomId={}",
                privateRoomId, gameType, roomId);

        try {
            IRoom room = roomServiceFactory.getRoomWithoutCreation(gameType, roomId);

            if (room == null) {
                LOG.error("getAbstractBattlegroundGameRoom: room is null, gameType: {}, roomId: {}", gameType, roomId);
                return null;
            }

            if (!(room instanceof ICAFRoom)) {
                LOG.error("getAbstractBattlegroundGameRoom: room is not ICAFRoom, gameType: {}, roomId: {}",
                        gameType, roomId);
                return null;
            }

            return (ICAFRoom) room;

        } catch (Exception e) {
            LOG.error("getAbstractBattlegroundGameRoom: exception: {}", e.getMessage(), e);
            return null;
        }
    }

    protected void sendFullGameInfoToSocketClient(ICAFRoom room, IGameSocketClient socketClient) {

        if (room == null) {
            LOG.error("sendFullGameInfoToSocketClient: room is null, socketClient: {}", socketClient);
            return;
        }

        if (socketClient == null) {
            LOG.error("sendFullGameInfoToSocketClient: socketClient is null, ICAFRoom: {}", room);
            return;
        }

        room.sendGameInfoToSocketClient(socketClient);
    }

    protected boolean cancelKick(ICAFRoom room, Player player) {

        if (room == null) {
            LOG.error("cancelKick: room is null, player: {}", player);
            return false;
        }

        IRoomInfo roomInfo = ((IRoom)room).getRoomInfo();
        if (roomInfo == null) {
            LOG.error("cancelKick: roomInfo is null, player: {}", player);
            return false;
        }

        if (bgPrivateRoomInfoService == null) {
            LOG.error("cancelKick: bgPrivateRoomInfoService is null for room: {}", roomInfo);
            return false;
        }

        if (multiNodePrivateRoomInfoService == null) {
            LOG.error("cancelKick: multiNodePrivateRoomInfoService is null for room: {}", roomInfo);
            return false;
        }

        if (player == null) {
            LOG.error("cancelKick: player is null for room: {}", roomInfo);
            return false;
        }

        if (player.getAccountId() != 0) {
            //accountId exists for cancelKick Candidate, make a cancel kick process over RoomInfo Listener
            if ((roomInfo instanceof BGPrivateRoomInfo)) {
                LOG.debug("cancelKick: roomInfo is BGPrivateRoomInfo make a cancel kick process over " +
                                "RoomInfo Listener, accountId: {}", player.getAccountId());

                BGPrivateRoomInfo bgPrivateRoomInfo = (BGPrivateRoomInfo) roomInfo;

                long roomId = roomInfo.getId();

                bgPrivateRoomInfoService.lock(roomId);
                try {
                    bgPrivateRoomInfo.cancelKick(player.getAccountId());
                    bgPrivateRoomInfoService.update(bgPrivateRoomInfo);
                } finally {
                    bgPrivateRoomInfoService.unlock(roomId);
                }

            } else if( roomInfo instanceof MultiNodePrivateRoomInfo) {
                LOG.debug("cancelKick: roomInfo is MultiNodePrivateRoomInfo make a cancel kick process over " +
                        "RoomInfo Listener, accountId: {}", player.getAccountId());

                MultiNodePrivateRoomInfo multiNodePrivateRoomInfo = (MultiNodePrivateRoomInfo) roomInfo;

                long roomId = roomInfo.getId();

                multiNodePrivateRoomInfoService.lock(roomId);
                try {
                    multiNodePrivateRoomInfo.cancelKick(player.getAccountId());
                    multiNodePrivateRoomInfoService.update(multiNodePrivateRoomInfo);
                } finally {
                    multiNodePrivateRoomInfoService.unlock(roomId);
                }

            }
            else {
                LOG.error("cancelKick: roomInfo is not BGPrivateRoomInfo/MultiNodePrivateRoomInfo , " +
                        "roomInfo: {}, accountId: {}", roomInfo, player.getAccountId());
            }

        } else {
            //private room is updated for accounts in ACCEPTED status by caller function
            //updatePlayersStatusInPrivateRoom we need just to Notify Canex about this player
            try {

                String privateRoomId = ((IRoom)room).getRoomInfo().getPrivateRoomId();

                LOG.debug("cancelKick: privateRoomId:{}, Status:{}, player:{}",
                        privateRoomId, Status.ACCEPTED, player);

                if (StringUtils.isTrimmedEmpty(privateRoomId)) {
                    LOG.error("cancelKick: privateRoomId is empty, skip sendPlayerStatusInPrivateRoomToCanex");
                    return false;
                }

                if (StringUtils.isTrimmedEmpty(player.getNickname())) {
                    LOG.error("cancelKick: nickname is empty, skip sendPlayerStatusInPrivateRoomToCanex");
                    return false;
                }

                room.sendPlayerStatusInPrivateRoomToCanex(privateRoomId, 0, 0,
                        player.getNickname(), player.getExternalId(), null, Status.ACCEPTED);

            } catch (Exception e) {
                LOG.error("handle: Exception to sendPlayerStatusInPrivateRoomToCanex, {}", e.getMessage(), e);
            }
        }

        return true;
    }

    @Override
    public void sendPlayerStatusInPrivateRoomToCanex(int serverId, int bankId, String privateRoomId,
                                                     String nickname, String externalId, long accountId,
                                                     Status tbgStatus) {

        LOG.debug("sendPlayerStatusInPrivateRoomToCanex: serverId:{}, bankId:{}, privateRoomId:{}, nickname:{}, " +
                        "externalId:{}, accountId:{}, tbgStatus:{}",
                serverId, bankId, privateRoomId, nickname, externalId, accountId, tbgStatus);

        asyncExecutorService.execute(() ->
                _sendPlayerStatusInPrivateRoomToCanex(serverId, bankId, privateRoomId, nickname, externalId, accountId, tbgStatus));
    }

    private void _sendPlayerStatusInPrivateRoomToCanex(int serverId, int bankId, String privateRoomId,
                                                       String nickname, String externalId, long accountId,
                                                       Status status) {
        LOG.debug("_sendPlayerStatusInPrivateRoomToCanex: serverId:{}, bankId:{}, privateRoomId:{}, nickname:{}, " +
                        "externalId:{}, accountId:{}, status:{}",
                serverId, bankId, privateRoomId, nickname, externalId, accountId, status);
        final int maxRetries = 3;
        int attempts = 0;
        IBGUpdateRoomResult tbgUpdateRoomResult = null;

        while (attempts < maxRetries) {
            try {
                LOG.debug("sendPlayerStatusInPrivateRoomToCanex attempt: {}", attempts + 1);
                if (StringUtils.isTrimmedEmpty(privateRoomId)) {
                    LOG.error("sendPlayerStatusInPrivateRoomToCanex: privateRoomId is empty, nickname:{}", nickname);
                    return;
                }

                //try to get externalId if it is empty
                if (StringUtils.isTrimmedEmpty(externalId)) {

                    PrivateRoom privateRoom = getPrivateRoom(privateRoomId);

                    if (privateRoom == null) {
                        LOG.error("sendPlayerStatusInPrivateRoomToCanex: privateRoom is null, for privateRoomId: {}," +
                                " nickname:{}", privateRoomId, nickname);
                        return;
                    }

                    if (privateRoom.getPlayers() == null || privateRoom.getPlayers().size() == 0) {
                        LOG.error("sendPlayerStatusInPrivateRoomToCanex: privateRoom.getPlayers().size() is empty, " +
                                "for privateRoomId: {}, nickname:{}", privateRoomId, nickname);
                        return;
                    }

                    //find player's externalId from the existing record
                    Player existsngPlayer = privateRoom.getPlayers().stream()
                            .filter(p -> p.getNickname().equals(nickname))
                            .findFirst()
                            .orElse(null);

                    if (existsngPlayer != null) {
                        externalId = existsngPlayer.getExternalId();

                    }
                }

                IBGPlayer tbgPlayer = new BGPlayer(nickname, accountId, externalId, status);
                IBGUpdatePrivateRoom request = new BGUpdatePrivateRoom(privateRoomId, Arrays.asList(tbgPlayer), bankId);
                LOG.debug("sendPlayerStatusInPrivateRoomToCanex: TBGUpdateRoomRequest: {}", request);

                tbgUpdateRoomResult =
                        socketService.updatePlayersStatusInPrivateRoom(serverId, request);

                // Check if the operation was successful, add your success condition here
                // For example, if the result should not be null and should have a specific code
                if (tbgUpdateRoomResult != null && tbgUpdateRoomResult.getCode() == 200) {
                    // Success, break from the loop
                    LOG.debug("sendPlayerStatusInPrivateRoomToCanex: success " +
                            "code(): {}, message: {}", tbgUpdateRoomResult.getCode(), tbgUpdateRoomResult.getMessage());
                    return;
                } else {
                    LOG.error("sendPlayerStatusInPrivateRoomToCanex: attempt {} failed with code: {}, message: {}",
                            attempts + 1, tbgUpdateRoomResult.getCode(), tbgUpdateRoomResult.getMessage());
                }
            } catch (Exception e) {
                LOG.error("sendPlayerStatusInPrivateRoomToCanex: attempt {} failed due to exception", attempts + 1, e);
            }
            attempts++;
            if (attempts < maxRetries) {
                try {
                    Thread.sleep(1000); // Wait for 1 second before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    LOG.error("sendPlayerStatusInPrivateRoomToCanex: interrupted while waiting for retry", ie);
                    return; // Or handle interruption appropriately
                }
            }
        }
        LOG.error("sendPlayerStatusInPrivateRoomToCanex: all attempts failed. tbgUpdateRoomResult: {}", tbgUpdateRoomResult);
    }

    @Override
    public boolean invitePlayersToPrivateRoomAtCanex(int serverId, Set<Friend> friends, String privateRoomId) {
        LOG.debug("invitePlayersToPrivateRoomAtCanex: serverId:{}, friends:{}, privateRoomId:{}",
                serverId, friends, privateRoomId);

        try {
            if (friends == null || friends.size() == 0) {
                LOG.error("invitePlayersToPrivateRoomAtCanex: friends is empty:{}", friends);
                return false;
            }

            List<IBGPlayer> tbgPlayers = new ArrayList<>();

            for (Friend friend : friends) {

                IBGPlayer tbgPlayer = new BGPlayer(
                        friend.getNickname(),
                        0,
                        friend.getExternalId(),
                        Status.INVITED
                );

                tbgPlayers.add(tbgPlayer);
            }

            LOG.debug("invitePlayersToPrivateRoomAtCanex: tbgPlayers:{}", tbgPlayers);

            return socketService.invitePlayersToPrivateRoom(serverId, tbgPlayers, privateRoomId);

        } catch (Exception e) {
            LOG.error("invitePlayersToPrivateRoomAtCanex: failed due to exception:{}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public List<Friend> getFriendsFromCanex(int serverId, Friend friend) {
        LOG.debug("getFriendsFromCanex: serverId:{}, friend:{}", serverId, friend);

        try {
            if (friend == null) {
                LOG.error("getFriendsFromCanex: friend is null");
                return null;
            }

            if (StringUtils.isTrimmedEmpty(friend.getExternalId())) {
                LOG.error("getFriendsFromCanex: friend.getExternalId() is empty:{}", friend);
                return null;
            }


            List<Friend> friends = socketService.getFriends(serverId, friend);

            return friends;

        } catch (Exception e) {
            LOG.error("getFriendsFromCanex: failed due to exception:{}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void getFriendsFromCanexAsync(int serverId, Friend friend) {
        LOG.debug("getFriendsFromCanexAsync: serverId:{}, friend:{}", serverId, friend);
        asyncExecutorService.execute(() -> getFriendsFromCanex(serverId, friend));
    }

    @Override
    public List<OnlinePlayer> getOnlineStatusFromCanex(int serverId, Collection<Friend> friends) {
        LOG.debug("getOnlineStatusFromCanex: serverId:{}, friends:{}", serverId, friends);

        try {
            if (friends == null || friends.size() == 0) {
                LOG.error("getOnlineStatusFromCanex: friends is empty:{}", friends);
                return null;
            }


            List<OnlinePlayer> onlinePlayer
                    = socketService.getOnlineStatus(serverId, friends);

            return onlinePlayer;

        } catch (Exception e) {
            LOG.error("getOnlineStatusFromCanex: failed due to exception:{}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void getOnlineStatusFromCanexAsync(int serverId, Collection<Friend> friends) {
        LOG.debug("getOnlineStatusFromCanexAsync: serverId:{}, friends:{}", serverId, friends);
        asyncExecutorService.execute(() -> getOnlineStatusFromCanex(serverId,friends));
    }

}
