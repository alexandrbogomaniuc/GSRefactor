package com.betsoft.casino.mp.web.handlers.kafka;

import com.betsoft.casino.mp.config.WebSocketRouter;
import com.betsoft.casino.mp.data.persister.ActiveCashBonusSessionPersister;
import com.betsoft.casino.mp.data.persister.ActiveFrbSessionPersister;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.bots.BotConfigInfo;
import com.betsoft.casino.mp.model.bots.dto.BotStatusResult;
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
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.transport.Avatar;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.utils.BGFStatusUtil;
import com.betsoft.casino.mp.utils.BGOStatusUtil;
import com.betsoft.casino.mp.utils.BGStatusUtil;
import com.betsoft.casino.mp.utils.BotConfigInfoUtil;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.service.ChangeBonusStatusTask;
import com.betsoft.casino.mp.web.service.ChangeTournamentStateTask;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.utils.ITransportObject;
import com.abs.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.common.cache.Identifiable;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.kafka.dto.*;
import com.abs.casino.kafka.dto.privateroom.request.*;
import com.abs.casino.kafka.dto.privateroom.response.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static com.betsoft.casino.utils.DateTimeUtils.toHumanReadableFormat;


@Service
public class KafkaMultiPlayerResponseService {
    private static final Logger LOG = LogManager.getLogger(KafkaMultiPlayerResponseService.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final BGPrivateRoomInfoService bgPrivateRoomInfoService;
    private final MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService;
    private final RoomServiceFactory roomServiceFactory;
    private IPrivateRoomPlayersStatusService privateRoomPlayersStatusService;
    private final RoomPlayerInfoService playerInfoService;
    private final LobbySessionService lobbySessionService;
    private final ActiveCashBonusSessionPersister activeCashBonusSessionPersister;
    private final ActiveFrbSessionPersister activeFrbSessionPersister;
    private final BotConfigInfoService botConfigInfoService;
    private final IBotServiceClient botServiceClient;
    private final ServerConfigService serverConfigService;

    @Autowired
    public KafkaMultiPlayerResponseService(BGPrivateRoomInfoService bgPrivateRoomInfoService,
                                           MultiNodePrivateRoomInfoService multiNodePrivateRoomInfoService,
                                           RoomServiceFactory roomServiceFactory,
                                           RoomPlayerInfoService playerInfoService,
                                           LobbySessionService lobbySessionService,
                                           CassandraPersistenceManager persistenceManager,
                                           BotConfigInfoService botConfigInfoService,
                                           IBotServiceClient botServiceClient,
                                           ServerConfigService serverConfigService) {
        this.bgPrivateRoomInfoService = bgPrivateRoomInfoService;
        this.multiNodePrivateRoomInfoService = multiNodePrivateRoomInfoService;
        this.roomServiceFactory = roomServiceFactory;
        this.playerInfoService = playerInfoService;
        this.lobbySessionService = lobbySessionService;
        this.activeCashBonusSessionPersister = persistenceManager.getPersister(ActiveCashBonusSessionPersister.class);
        this.activeFrbSessionPersister = persistenceManager.getPersister(ActiveFrbSessionPersister.class);
        if(bgPrivateRoomInfoService != null) {
            privateRoomPlayersStatusService = bgPrivateRoomInfoService.getPrivateRoomPlayersStatusService();
            if(privateRoomPlayersStatusService != null) {
                privateRoomPlayersStatusService.setRoomServiceFactory(roomServiceFactory);
            }
        }

        this.botConfigInfoService = botConfigInfoService;
        this.botServiceClient = botServiceClient;
        this.serverConfigService = serverConfigService;
    }

    public StringResponseDto getBGPrivateRoomUrl(PrivateRoomURLDto request) {
        String privateRoomUrl = "";
        try {
            LOG.debug("getBGPrivateRoomUrl: Get private room request: {}", request);

            validateCreateRoomRequest(request);

            if(request.getGameId() == GameType.BG_DRAGONSTONE.getGameId()
                    || request.getGameId() == GameType.BG_MISSION_AMAZON.getGameId()
                    || request.getGameId() == GameType.BG_SECTOR_X.getGameId()) {

                privateRoomUrl = bgPrivateRoomInfoService.findOrCreateRoomAndGetUrl(request.getOwnerUsername(),
                        request.getOwnerExternalId(),
                        request.getBankId(),
                        GameType.getByGameId(request.getGameId()),
                        Money.fromCents(request.getBuyIn()), request.getCurrency(), request.getDomainUrl());

            } else if(request.getGameId() == GameType.BG_MAXCRASHGAME.getGameId()) {

                privateRoomUrl = multiNodePrivateRoomInfoService.findOrCreateRoomAndGetUrl(request.getOwnerUsername(),
                        request.getOwnerExternalId(),
                        request.getBankId(),
                        GameType.getByGameId(request.getGameId()),
                        Money.fromCents(request.getBuyIn()), request.getCurrency(), request.getDomainUrl());

            }

            return new StringResponseDto(true, 200, "OK", privateRoomUrl);
        }catch (Exception e) {
            LOG.error("getBGPrivateRoomUrl: Unable create private room", e);
            return new StringResponseDto(false, 500, "getBGPrivateRoomUrl: Unable create private room");
        }
    }

    public PrivateRoomIdResultDto getPrivateRoomId(PrivateRoomIdDto request){
        try {
            LOG.debug("getPrivateRoomId: Get private room id request: {}", request);

            long ownerAccountId = request.getOwnerAccountId();

            List<String> roomsIds = bgPrivateRoomInfoService.getActiveRoomsByOwner(ownerAccountId);
            roomsIds.addAll(multiNodePrivateRoomInfoService.getActiveRoomsByOwner(ownerAccountId));

            if (roomsIds.size() >= 6) {
                return new PrivateRoomIdResultDto(false,400, "player's active room limit exceeded",null, roomsIds);
            }

            String ownerUsername = request.getOwnerUsername();
            String externalId = request.getOwnerExternalId();
            long bankId = request.getBankId();
            GameType gameType = GameType.getByGameId(request.getGameId());
            Money stake = Money.fromCents(request.getBuyIn());
            String currency = request.getCurrency();

            String privateRoomId = "";

            if(request.getGameId() == GameType.BG_DRAGONSTONE.getGameId()
                    || request.getGameId() == GameType.BG_MISSION_AMAZON.getGameId()
                    || request.getGameId() == GameType.BG_SECTOR_X.getGameId()) {

                privateRoomId = bgPrivateRoomInfoService
                        .findOrCreateRoomAndGetId(ownerUsername, externalId, bankId, ownerAccountId, gameType, stake, currency);

            } else if(request.getGameId() == GameType.BG_MAXCRASHGAME.getGameId()) {

                privateRoomId = multiNodePrivateRoomInfoService
                        .findOrCreateRoomAndGetId(ownerUsername, externalId, bankId, ownerAccountId, gameType, stake, currency);
            } else {
                LOG.error("getPrivateRoomId: Unsupported game id={}", request.getGameId());
                return new PrivateRoomIdResultDto(false,400, "Unsupported game id",null, null);
            }
            return new PrivateRoomIdResultDto(true,200, "OK", privateRoomId, null);

        } catch (Exception e) {
            LOG.error("Unable get private room id", e);
            return new PrivateRoomIdResultDto(false,400, "internal error",null, null);
        }
    }

    private void validateCreateRoomRequest(PrivateRoomURLDto request) throws CommonException {
        if (request == null) {
            throw new CommonException("Create room request is null");
        }
        if (StringUtils.isTrimmedEmpty(request.getOwnerUsername())) {
            throw new CommonException("ownerUsername not presented");
        }
        GameType gameType = GameType.getByGameId(request.getGameId());
        if (gameType == null || !gameType.isBattleGroundGame()) {
            throw new CommonException("Game id is not for battleground game. Id:" + request.getGameId());
        }
        if (request.getBankId() <= 0) {
            throw new CommonException("Specified incorrect bankId: " + request.getBankId());
        }
        if (request.getBuyIn() <= 0) {
            throw new CommonException("Specified incorrect buyIn: " + request.getBuyIn());
        }
        if (StringUtils.isTrimmedEmpty(request.getCurrency())) {
            throw new CommonException("currency not presented");
        }
        if (StringUtils.isTrimmedEmpty(request.getDomainUrl())) {
            throw new CommonException("domainUrl not presented");
        }
    }

    public DeactivateRoomResultDto deactivate(DeactivateRoomDto request) {
        DeactivateRoomResultDto deactivateResult;
        try {
            deactivateResult = tryDeactivateBGPrivateRoom(request);

            if(deactivateResult == null) {
                deactivateResult = tryDeactivateMultiNodePrivateRoom(request);
            }

            if(deactivateResult == null) {
                deactivateResult = new DeactivateRoomResultDto(true,200, "room not found");
            }

        } catch (Exception e) {
            LOG.error("Unable deactivate private room", e);
            deactivateResult = new DeactivateRoomResultDto(false,400, "internal error");
        }

        return deactivateResult;
    }

    private DeactivateRoomResultDto tryDeactivateBGPrivateRoom(DeactivateRoomDto request) {

        BGPrivateRoomInfo roomInfo = bgPrivateRoomInfoService.getRoomByPrivateRoomId(request.getRoomId());

        if(roomInfo != null) {

            if (roomInfo.isDeactivated()) {
                return new DeactivateRoomResultDto(true,200, "room is deactivated");
            }

            if (request.getOwnerAccountId() != 0 && request.getOwnerAccountId() != roomInfo.getOwnerAccountId()) {
                return new DeactivateRoomResultDto(false,432, "player is not the Host of this room");
            }

            bgPrivateRoomInfoService.lock(roomInfo.getId());

            try {
                BGPrivateRoomInfo roomInfoToUpdate = bgPrivateRoomInfoService.getRoom(roomInfo.getId());
                roomInfoToUpdate.setDeactivated(true);
                bgPrivateRoomInfoService.update(roomInfoToUpdate);
            } catch (Exception e) {
                LOG.error("tryDeactivateBGPrivateRoom: Unable update room info. Reason: {}", e.getMessage());
                return new DeactivateRoomResultDto(false,400, "internal error");
            } finally {
                bgPrivateRoomInfoService.unlock(roomInfo.getId());
            }

            RoomState state = roomInfo.getState();
            if (state.equals(RoomState.QUALIFY) || state.equals(RoomState.PLAY)) {

                return new DeactivateRoomResultDto(true,200, "room will be deactivated after the end of the round");

            } else {

                ITransportObject roomWasDeactivatedMessage = new Error(ErrorCodes.ROOM_WAS_DEACTIVATED,
                        "Room was deactivated", System.currentTimeMillis(), -1);

                notifyRoomDeactivatedToAllObserversOnLocalNode(roomInfo, roomWasDeactivatedMessage);

                LOG.debug("tryDeactivateBGPrivateRoom: RoomInfo: {} was deleted successfully", roomInfo.getId());
                return new DeactivateRoomResultDto(true,200, "room is deactivated");
            }
        }
        return null;
    }

    private void notifyRoomDeactivatedToAllObserversOnLocalNode(AbstractRoomInfo roomInfo, ITransportObject roomWasDeactivatedMessage ) {
        if(roomInfo != null) {
            try {
                IRoom room = roomServiceFactory.getRoomWithoutCreation(roomInfo.getGameType(), roomInfo.getId());
                if (room == null) {
                    LOG.info("notifyRoomDeactivatedToAllObserversOnLocalNode: Room not found, message={}", toString());
                } else {
                    LOG.debug("notifyRoomDeactivatedToAllObserversOnLocalNode: Room found {}", room);

                    Collection<IGameSocketClient> observers = room.getObservers();
                    for (IGameSocketClient observer : observers) {
                        if (!observer.isDisconnected()) {
                            LOG.debug("notifyRoomDeactivatedToAllObserversOnLocalNode: send message {} to observer: {}", roomWasDeactivatedMessage, observer);
                            observer.sendMessage(roomWasDeactivatedMessage);
                        } else {
                            LOG.debug("notifyRoomDeactivatedToAllObserversOnLocalNode: skip observer (is disconnected): {}", observer);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("notifyRoomDeactivatedToAllObserversOnLocalNode: Cannot send message={}", toString(), e);
            }
        }
    }

    private DeactivateRoomResultDto tryDeactivateMultiNodePrivateRoom(DeactivateRoomDto request) {

        MultiNodePrivateRoomInfo roomInfo = multiNodePrivateRoomInfoService.getRoomByPrivateRoomId(request.getRoomId());

        if(roomInfo != null) {

            if (roomInfo.isDeactivated()) {
                return new DeactivateRoomResultDto(true,200, "room is deactivated");
            }

            if (request.getOwnerAccountId() != 0 && request.getOwnerAccountId() != roomInfo.getOwnerAccountId()) {
                return new DeactivateRoomResultDto(false,432, "player is not the Host of this room");
            }

            multiNodePrivateRoomInfoService.lock(roomInfo.getId());

            try {
                MultiNodePrivateRoomInfo roomInfoToUpdate = multiNodePrivateRoomInfoService.getRoom(roomInfo.getId());
                roomInfoToUpdate.setDeactivated(true);
                multiNodePrivateRoomInfoService.update(roomInfoToUpdate);
            } catch (Exception e) {
                LOG.error("tryDeactivateMultiNodePrivateRoom: Unable update room info. Reason: {}", e.getMessage());
                return new DeactivateRoomResultDto(false,400, "internal error");
            } finally {
                multiNodePrivateRoomInfoService.unlock(roomInfo.getId());
            }

            RoomState state = roomInfo.getState();
            if (state.equals(RoomState.QUALIFY) || state.equals(RoomState.PLAY)) {
                return new DeactivateRoomResultDto(true,200, "room will be deactivated after the end of the round");

            } else {

                ITransportObject roomWasDeactivatedMessage = new Error(ErrorCodes.ROOM_WAS_DEACTIVATED,
                        "Room was deactivated", System.currentTimeMillis(), -1);

                notifyRoomDeactivatedToAllObserversOnAllNodes(roomInfo, roomWasDeactivatedMessage);

                sitOutAllFromMultiNodePrivateRoom(roomInfo);

                LOG.debug("tryDeactivateMultiNodePrivateRoom: RoomInfo: {} was deleted successfully", roomInfo.getId());
                return new DeactivateRoomResultDto(true,200, "room is deactivated");
            }
        }

        return null;
    }

    private void notifyRoomDeactivatedToAllObserversOnAllNodes(AbstractRoomInfo roomInfo, ITransportObject roomWasDeactivatedMessage ) {
        if(roomInfo != null) {
            //send roomWasDeactivatedMessage to all observers over all nodes
            SendSeatsMessageTask sendSeatsMessageTask = multiNodePrivateRoomInfoService.createSendSeatsMessageTask(
                    roomInfo.getId(),
                    roomInfo.getGameType(),
                    -1,
                    null,
                    false,
                    -1,
                    roomWasDeactivatedMessage,
                    true
            );

            LOG.debug("notifyRoomDeactivatedToAllObserversOnAllNodes: remote execute by " +
                    "multiNodePrivateRoomInfoService the sendSeatsMessageTask:{}.", sendSeatsMessageTask);

            multiNodePrivateRoomInfoService.executeOnAllMembers(sendSeatsMessageTask);
        }
    }

    private void sitOutAllFromMultiNodePrivateRoom(MultiNodePrivateRoomInfo roomInfo) {
        try {
            IRoom room = roomServiceFactory.getRoomWithoutCreation(roomInfo.getGameType(), roomInfo.getId());
            if (room != null) {
                LOG.info("sitOutFromMultiNodePrivateRoom: Room={}", room);
                List<ISeat> seats = room.getAllSeats();

                for(ISeat seat : seats) {
                    if(seat != null) {
                        long accountId = seat.getAccountId();
                        LOG.debug("sitOutFromMultiNodePrivateRoom: call room.processSitOut for accountId: {}, " +
                                "seat: {}", accountId, seat);

                        room.processSitOut(null, null, 0, accountId, true);
                    }
                }
            }
        }  catch (Exception e) {
            LOG.error("sitOutFromMultiNodePrivateRoom: Cannot send message={}", toString(), e);
        }
    }

    public UpdateRoomResultDto updatePlayersStatusInPrivateRoom(UpdateRoomDto request) {
        if (request == null) {
            LOG.error("updatePlayersStatusInPrivateRoom: request is null");
            return new UpdateRoomResultDto(false, 400, "MP internal error");
        }

        if (StringUtils.isTrimmedEmpty(request.getPrivateRoomId())) {
            LOG.error("updatePlayersStatusInPrivateRoom: request.getPrivateRoomId() is empty");
            return new UpdateRoomResultDto(false, 400, "MP internal error");
        }

        if (request.getPlayers() == null) {
            LOG.error("updatePlayersStatusInPrivateRoom: request.getPlayers() is null");
            return new UpdateRoomResultDto(false, 400, "MP internal error", request.getPrivateRoomId(), null);
        }

        try {
            List<Player> players = new ArrayList<>();
            for (BGPlayerDto playerDto : request.getPlayers()) {
                Status status = BGStatusUtil.fromBGStatus(playerDto.getStatus());

                String nickname = playerDto.getNickname();
                if (StringUtils.isTrimmedEmpty(nickname)) {
                    LOG.warn("updatePlayersStatusInPrivateRoom: tbgPlayer.getNickname() is empty");
                    continue;
                }

                String externalId = playerDto.getExternalId();
                long accountId = playerDto.getAccountId();

                Player player = new Player(nickname, accountId, externalId, status);
                players.add(player);
            }


            PrivateRoom privateRoom = new PrivateRoom(0, request.getPrivateRoomId(),
                    0, null, null, players, System.currentTimeMillis());

            LOG.debug("updatePlayersStatusInPrivateRoom: isTransitionLimited={} update privateRoom:{}",
                    request.isTransitionLimited(), privateRoom);

            UpdatePrivateRoomResponse updatePrivateRoomResponse
                    = privateRoomPlayersStatusService.updatePlayersStatusInPrivateRoom(privateRoom, request.isTransitionLimited(), true);

            if (updatePrivateRoomResponse == null) {

                LOG.error("updatePlayersStatusInPrivateRoom: updatePrivateRoomResponse is null for {}",
                        request.getPrivateRoomId());
                return new UpdateRoomResultDto(false, 500, "MP internal error", request.getPrivateRoomId(), null);

            } else if(updatePrivateRoomResponse.getPrivateRoom() == null) {
                LOG.error("updatePlayersStatusInPrivateRoom: updatePrivateRoomResponse.getPrivateRoom() is null for {}",
                        request.getPrivateRoomId());
                return new UpdateRoomResultDto(false,500, "MP internal error", request.getPrivateRoomId(), null);
            }
            else {

                if (updatePrivateRoomResponse.getCode() == 200) {
                    LOG.debug("updatePlayersStatusInPrivateRoom: successfully updated privateRoom:{} message:{}",
                            privateRoom.getPrivateRoomId(), updatePrivateRoomResponse.getMessage());
                }  else {
                    LOG.error("updatePlayersStatusInPrivateRoom: fail to update privateRoom:{} code:{} message:{}",
                            privateRoom.getPrivateRoomId(), updatePrivateRoomResponse.getCode(), updatePrivateRoomResponse.getMessage());
                }

                List<BGPlayerDto> playersDto = new ArrayList<>();

                if(updatePrivateRoomResponse.getPrivateRoom().getPlayers() != null
                        && !updatePrivateRoomResponse.getPrivateRoom().getPlayers().isEmpty()) {

                    for(Player player : updatePrivateRoomResponse.getPrivateRoom().getPlayers()) {
                        BGPlayerDto playerDto = new BGPlayerDto(
                                player.getNickname(),
                                player.getAccountId(),
                                player.getExternalId(),
                                BGStatusUtil.toBGStatus(player.getStatus())
                        );
                        playersDto.add(playerDto);
                    }

                } else {
                    LOG.debug("updatePlayersStatusInPrivateRoom: updatePrivateRoomResponse.getPrivateRoom().getPlayers() " +
                            "list is empty for {}", request.getPrivateRoomId());
                }
                return new UpdateRoomResultDto(true,
                        updatePrivateRoomResponse.getCode(),
                        updatePrivateRoomResponse.getMessage(),
                        updatePrivateRoomResponse.getPrivateRoom().getPrivateRoomId(),
                        playersDto);
            }
        } catch (Exception e) {
            LOG.error("updatePlayersStatusInPrivateRoom: Unable to Update Players Status In Private Room {}",
                    request.getPrivateRoomId(), e);
            return new UpdateRoomResultDto(false, 400, "MP internal error", request.getPrivateRoomId(), null);
        }
    }

    public UpdateFriendsResultDto updateFriends(UpdateFriendsDto request) {
        if (request == null) {
            LOG.error("updateFriends: request is null");
            return new UpdateFriendsResultDto(false, 400, "MP internal error");
        }

        if (StringUtils.isTrimmedEmpty(request.getExternalId())) {
            LOG.error("updateFriends: request.getExternalId() is empty");
            return new UpdateFriendsResultDto(
                    false,
                    400,
                    "MP internal error",
                    request.getNickname(),
                    null,
                    null);
        }

        if (request.getFriends() == null || request.getFriends().isEmpty()) {
            LOG.error("updateFriends: request.getFriends() is null");
            return new UpdateFriendsResultDto(
                    false,
                    400,
                    "MP internal error",
                    request.getNickname(),
                    request.getExternalId(),
                    null);
        }

        try {
            Map<String, Friend> friendsMap = new HashMap<>();
            for (BGFriendDto playerDto : request.getFriends()) {
                String externalId = playerDto.getExternalId();
                if (StringUtils.isTrimmedEmpty(externalId)) {
                    LOG.warn("updateFriends: tbgFriend.getExternalId() is empty");
                    continue;
                }

                com.betsoft.casino.mp.model.friends.Status status
                        = BGFStatusUtil.fromBGFStatus(playerDto.getStatus());

                Friend friend = new Friend(playerDto.getNickname(), externalId, status);
                friendsMap.put(externalId, friend);
            }

            if (friendsMap.isEmpty()) {
                LOG.error("updateFriends: friendsMap no items");
                return new UpdateFriendsResultDto(
                        false,
                        400,
                        "MP internal error",
                        request.getNickname(),
                        request.getExternalId(),
                        null);
            }

            Friends friends = new Friends(request.getNickname(), request.getExternalId(), friendsMap, System.currentTimeMillis());

            IPrivateRoomPlayersStatusService privateRoomPlayersStatusService = bgPrivateRoomInfoService.getPrivateRoomPlayersStatusService();

            if (privateRoomPlayersStatusService == null) {
                LOG.error("updateFriends: privateRoomPlayersStatusService is null, for {}", request.getExternalId());
                return new UpdateFriendsResultDto(
                        false,
                        500,
                        "MP internal error",
                        request.getNickname(),
                        request.getExternalId(),
                        null);
            }

            UpdateFriendsResponse updateFriendsResponse = privateRoomPlayersStatusService.updateFriends(friends);

            if (updateFriendsResponse == null) {
                LOG.error("updateFriends: updateFriendsResponse is null for {}", request.getExternalId());
                return new UpdateFriendsResultDto(
                        false,
                        500,
                        "MP internal error",
                        request.getNickname(),
                        request.getExternalId(),
                        null);
            } else if (updateFriendsResponse.getFriends() == null) {
                LOG.error("updateFriends: updateFriendsResponse.getFriends() is null for {}", request.getExternalId());
                return new UpdateFriendsResultDto(
                        false,
                        500,
                        "MP internal error",
                        request.getNickname(),
                        request.getExternalId(),
                        null);
            } else {
                if (updateFriendsResponse.getCode() == 200) {
                    LOG.debug("updateFriends: successfully updated getExternalId:{} message:{}",
                            friends.getExternalId(),
                            updateFriendsResponse.getMessage());
                } else {
                    LOG.error("updateFriends: fail to update getExternalId:{} code:{} message:{}",
                            friends.getExternalId(),
                            updateFriendsResponse.getCode(),
                            updateFriendsResponse.getMessage());
                }

                List<BGFriendDto> friendsList = new ArrayList<>();

                if (updateFriendsResponse.getFriends().getFriends() != null && !updateFriendsResponse.getFriends().getFriends().isEmpty()) {

                    for (Friend friend : updateFriendsResponse.getFriends().getFriends().values()) {
                        BGFriendDto playerDto = new BGFriendDto(
                                friend.getNickname(),
                                friend.getExternalId(),
                                BGFStatusUtil.toBGFStatus(friend.getStatus()));
                        friendsList.add(playerDto);
                    }

                } else {
                    LOG.debug("updateFriends: updateFriendsResponse.getFriends().getFriends() map is empty for {}", request.getExternalId());
                }

                return new UpdateFriendsResultDto(
                        true,
                        updateFriendsResponse.getCode(),
                        updateFriendsResponse.getMessage(),
                        friends.getNickname(),
                        friends.getExternalId(),
                        friendsList
                );
            }
        } catch (Exception e) {
            LOG.error("updateFriends: Unable to Update Friends for {}", request.getExternalId());
            return new UpdateFriendsResultDto(
                    false,
                    400,
                    "MP internal error",
                    request.getNickname(),
                    request.getExternalId(),
                    null);
        }
    }

    public UpdateOnlinePlayersResultDto updateOnlinePlayers(UpdateOnlinePlayersDto request) {
        if (request == null) {
            LOG.error("updateOnlinePlayers: request is null");
            return new UpdateOnlinePlayersResultDto(false, 400, "MP internal error");
        }

        if (request.getOnlinePlayers() == null) {
            LOG.error("updateOnlinePlayers: request.getOnlinePlayers() is null");
            return new UpdateOnlinePlayersResultDto(false, 400, "MP internal error");
        }

        if (request.getOnlinePlayers().isEmpty()) {
            LOG.error("updateOnlinePlayers: request.getOnlinePlayers() no items");
            return new UpdateOnlinePlayersResultDto(false, 400, "MP internal error");
        }

        try {
            List<OnlinePlayer> onlinePlayers = new ArrayList<>();

            for (BGOnlinePlayerDto playerDto : request.getOnlinePlayers()) {
                com.betsoft.casino.mp.model.onlineplayer.Status status
                        = BGOStatusUtil.fromBGOStatus(playerDto.getStatus());

                String externalId = playerDto.getExternalId();
                if (StringUtils.isTrimmedEmpty(externalId)) {
                    LOG.warn("updateOnlinePlayers: tbgOnlinePlayer.getExternalId() is empty");
                    continue;
                }

                OnlinePlayer onlinePlayer = new OnlinePlayer(playerDto.getNickname(), externalId, status);
                onlinePlayers.add(onlinePlayer);
            }

            if (onlinePlayers.isEmpty()) {
                LOG.error("updateOnlinePlayers: onlinePlayers no items");
                return new UpdateOnlinePlayersResultDto(false, 400, "MP internal error");
            }

            IPrivateRoomPlayersStatusService privateRoomPlayersStatusService = bgPrivateRoomInfoService.getPrivateRoomPlayersStatusService();

            if (privateRoomPlayersStatusService == null) {
                LOG.error("updateOnlinePlayers: privateRoomPlayersStatusService is null");
                return new UpdateOnlinePlayersResultDto(false, 500, "MP internal error");
            }

            UpdateOnlinePlayersResponse updateOnlinePlayersResponse = privateRoomPlayersStatusService.updateOnlinePlayers(onlinePlayers);
            if (updateOnlinePlayersResponse == null) {
                LOG.error("updateOnlinePlayers: updateOnlinePlayersResponse is null");
                return new UpdateOnlinePlayersResultDto(false, 500, "MP internal error");

            } else if (updateOnlinePlayersResponse.getOnlinePlayers() == null) {
                LOG.error("updateOnlinePlayers: updateOnlinePlayersResponse.getOnlinePlayers()  is null");
                return new UpdateOnlinePlayersResultDto(false, 500, "MP internal error");
            } else {
                if (updateOnlinePlayersResponse.getCode() == 200) {
                    LOG.debug("updateOnlinePlayers: successfully updated, message:{}", updateOnlinePlayersResponse.getMessage());
                } else {
                    LOG.error("updateOnlinePlayers: fail to update, code:{} message:{}",
                            updateOnlinePlayersResponse.getCode(), updateOnlinePlayersResponse.getMessage());
                }
                List<BGOnlinePlayerDto> playersList = new ArrayList<>();

                if (updateOnlinePlayersResponse.getOnlinePlayers() != null && !updateOnlinePlayersResponse.getOnlinePlayers().isEmpty()) {

                    for (OnlinePlayer onlinePlayer : updateOnlinePlayersResponse.getOnlinePlayers()) {
                        BGOnlinePlayerDto playerDto = new BGOnlinePlayerDto(
                            onlinePlayer.getNickname(),
                            onlinePlayer.getExternalId(),
                            BGOStatusUtil.toBGOStatus(onlinePlayer.getStatus())
                        );
                        playersList.add(playerDto);
                    }

                } else {
                    LOG.debug("updateOnlinePlayers: updateOnlinePlayersResponse.getOnlinePlayers() list is empty");
                }
                return new UpdateOnlinePlayersResultDto(
                    true,
                    updateOnlinePlayersResponse.getCode(),
                    updateOnlinePlayersResponse.getMessage(),
                    playersList);
            }
        } catch (Exception e) {
            LOG.error("updateOnlinePlayers: Unable to Update OnlinePlayers", e);
            return new UpdateOnlinePlayersResultDto(false, 400, "MP internal error");
        }
    }


    public CollectionResponseDto getParticipantAccountIdsInRound(GetParticipantAccountIdsInRoundDto request) {
        try {
            IRoomPlayerInfo playerInfo = findRoomPlayerInfo(request.getAccountId(), request.getGameSessionId(), "getParticipantAccountIdsInBtgRound");
            if (playerInfo != null) {
                long roomId = playerInfo.getRoomId();
                Set<Long> ids = playerInfoService.getForRoom(roomId).stream()
                        .filter(player -> (player.getRoundBuyInAmount() > 0 && !player.isWantSitOut()))
                        .map(Identifiable::getId)
                        .collect(Collectors.toSet());
                LOG.debug("getParticipantAccountIdsInBtgRound, roomId: {}, ids: {}", roomId, ids);
                if(ids.isEmpty()){
                    ids = Collections.emptySet();
                }

                return new CollectionResponseDto(true, 200, "OK", ids);
            }
        } catch (Exception e) {
            LOG.debug("getParticipantAccountIdsInRound, unexpected error", e);
        }

        return new CollectionResponseDto(true, 204, "No Content", Collections.emptySet());
    }

    private IRoomPlayerInfo findRoomPlayerInfo(long accountId, long gameSessionId, String log) {
        IRoomPlayerInfo playerInfo = playerInfoService.get(accountId);
        if (playerInfo == null) {
            LOG.debug("{}: Not found RoomPlayerInfo for accountId={}", log, accountId);
            Collection<IRoomPlayerInfo> players = gameSessionId == -1
                    ? null
                    : playerInfoService.getByGameSessionId(gameSessionId);
            if (players != null && !players.isEmpty()) {
                playerInfo = players.iterator().next();
            } else {
                Collection<LobbySession> lobbySessions = lobbySessionService.getByAccountId(accountId);
                for (LobbySession lobbySession : lobbySessions) {
                    if (lobbySession.getSessionId() != null) {
                        players = playerInfoService.getBySessionId(lobbySession.getSessionId());
                        if (!players.isEmpty()) {
                            playerInfo = players.iterator().next();
                            break;
                        }
                    }
                }
            }
        }
        return playerInfo;
    }

    public RoomInfoResultDto loadCurrentBattlegroundRoomInfoForPlayer(ParticipantGameSessionDto request) {
        IRoomPlayerInfo playerInfo = findRoomPlayerInfo(request.getAccountId(), request.getGameSessionId(), "loadCurrentBattlegroundRoomInfoForPlayer");
        if (playerInfo != null) {
            long roomId = playerInfo.getRoomId();
            BGPrivateRoomInfo bGPrivateRoomInfo = bgPrivateRoomInfoService.getRoom(roomId);
            if (bGPrivateRoomInfo != null) {
                return new RoomInfoResultDto(true, 200, "Ok",true, bGPrivateRoomInfo.getPrivateRoomId());
            }

            MultiNodePrivateRoomInfo multiNodePrivateRoomInfo = multiNodePrivateRoomInfoService.getRoom(roomId);
            if (multiNodePrivateRoomInfo != null) {
                return new RoomInfoResultDto(true, 200, "Ok",true, multiNodePrivateRoomInfo.getPrivateRoomId());
            }

        }
        return new RoomInfoResultDto(false, 400, "Not found",false, null);
    }

    public GetServerRunningRoomsResponse getServerRunningRoomsResponse(GetServerRunningRoomsRequest request) {
        int serverId = 0;
        try {

            serverId = serverConfigService != null ? serverConfigService.getServerId() : IRoomInfo.NOT_ASSIGNED_ID;
            Long gameId = request.getGameId();

            Map<Long, RunningRoomDto> runningRoomsDtoMap = roomServiceFactory.getLocalServerRunningRooms(gameId);

            GetServerRunningRoomsResponse getServerRunningRoomsResponse =
                    new GetServerRunningRoomsResponse(serverId, runningRoomsDtoMap);

            LOG.debug("getServerRunningRoomsResponse: getServerRunningRoomsResponse={}", getServerRunningRoomsResponse);

            return getServerRunningRoomsResponse;

        } catch (Exception e) {
            LOG.error("getServerRunningRoomsResponse: Unable get getServerRunningRoomsResponse", e);
            GetServerRunningRoomsResponse getServerRunningRoomsResponse =
                    new GetServerRunningRoomsResponse(false, 500, "getServerRunningRoomsResponse: " +
                            "Unable get getServerRunningRoomsResponse: " + e.getMessage());
            getServerRunningRoomsResponse.setServerId(serverId);
            return getServerRunningRoomsResponse;
        }
    }

    public PrivateRoomInfoResultDto getPrivateRoomInfo(GetPrivateRoomInfoRequest request) {
        try {
            PrivateRoomInfoResultDto privateRoomInfoResult = null;
            String privateRoomId = request.getPrivateRoomId();
            LOG.debug("getPrivateRoomInfo: Get private room info request, privateRoomId: {}", privateRoomId);
            if (StringUtils.isTrimmedEmpty(privateRoomId)) {
                throw new CommonException("privateRoomId not presented");
            }

            AbstractRoomInfo roomInfo = bgPrivateRoomInfoService.getRoomByPrivateRoomId(privateRoomId);
            if(roomInfo == null) {
                roomInfo = multiNodePrivateRoomInfoService.getRoomByPrivateRoomId(privateRoomId);
            }

            if (roomInfo != null) {

                String currency = roomInfo.getCurrency();
                GameType gameType = roomInfo.getGameType();
                int gameId = (int) gameType.getGameId();
                long bankId = roomInfo.getBankId();
                long buyIn = gameType.isCrashGame() ?
                        roomInfo.getStake().toCents() :
                        roomInfo.getBattlegroundBuyIn();

                int serverId = IRoomInfo.NOT_ASSIGNED_ID;
                if (roomInfo instanceof ISingleNodeRoomInfo) {
                    serverId = ((ISingleNodeRoomInfo) roomInfo).getGameServerId();
                }
                privateRoomInfoResult = new PrivateRoomInfoResultDto(
                    true,
                    200,
                    "Ok",
                    currency,
                    gameId,
                    bankId,
                    buyIn,
                    serverId);

            }

            if (privateRoomInfoResult == null) {
                return new PrivateRoomInfoResultDto(false, 500, "Room with privateRoomId=" + privateRoomId + " does not exist");
            }

            LOG.debug("getPrivateRoomInfo: privateRoomInfoResult={}", privateRoomInfoResult);
            return privateRoomInfoResult;
        } catch (Exception e) {
            LOG.error("getPrivateRoomInfo: Unable get room by privateRoomId", e);
            return new PrivateRoomInfoResultDto(false, 500, "getPrivateRoomInfo: Unable get room by privateRoomId" + e.getMessage());
        }
    }

    public void sendBonusStatus(BonusStatusDto request){
        LOG.debug("sendBonusStatus: received changes of bonus, bonusId: {}, status: {}, accountId: {}",
                request.getBonusId(), request.getStatus(), request.getAccountId());
        if ("RELEASING".equalsIgnoreCase(request.getStatus()) || "CANCELLING".equalsIgnoreCase(request.getStatus())) {
            LOG.debug("Skip sending 'RELEASING'/'CANCELLING' status, not supported on client");
            return;
        }

        ActiveCashBonusSession activeCashBonusSession = activeCashBonusSessionPersister.get(request.getBonusId());
        IActiveFrbSession activeFrbSession = activeFrbSessionPersister.get(request.getBonusId());

        if (activeCashBonusSession != null) {
            playerInfoService.getNotifyService().executeOnAllMembers(new ChangeBonusStatusTask(request.getAccountId(), request.getStatus(),
                    activeCashBonusSession.getStatus(), request.getBonusId()));
        } else if (activeFrbSession != null) {
            playerInfoService.getNotifyService().executeOnAllMembers(new ChangeBonusStatusTask(request.getAccountId(), request.getStatus(),
                    activeFrbSession.getStatus(), request.getBonusId()));
        } else {
            LOG.debug("sendBonusStatus not found activeCashBonusSession");
        }
        LOG.debug("sendBonusStatus end");
    }

    public void sitOut(SitOutRequest2 request) {
        IRoomPlayerInfo playerInfo = findRoomPlayerInfo(request.getAccountId(), request.getGameSessionId(), "sitOut");
        if (playerInfo != null) {
            long roomId = playerInfo.getRoomId();
            try {
                IRoomInfo roomInfo = roomServiceFactory.getRoomInfo(roomId);
                int seatNumber = roomInfo instanceof ISingleNodeRoomInfo ? playerInfo.getSeatNumber() : 0;
                if (roomInfo != null) {
                    IRoom room = roomServiceFactory.getRoomWithoutCreation(roomInfo.getGameType(), roomId);
                    if (room != null) {
                        room.processSitOut(null, null, seatNumber, request.getAccountId(), true);
                        LOG.debug("SitOut call remove roomId: {} accountId: {} seatNumber: {}", roomId, request.getAccountId(), seatNumber);
                    } else {
                        LOG.debug("SitOut call not found roomId: {}, just exit", roomId);
                    }
                } else {
                    LOG.debug("SitOut not found roomInfo: {}, just exit", roomId);
                }
            } catch (Exception e) {
                LOG.debug("Cannot sitOut player: {} roomId: {}", request.getAccountId(), roomId, e);
            }
        }
    }

    public void sendTournamentEnded(TournamentEndedDto request) {
        LOG.debug("sendTournamentEnded: tournamentId: {}, oldStatus: {}, newStatus: {}",
                request.getTournamentId(),
                request.getOldStatus(),
                request.getNewStatus());
        //executeOnAllMembers not required, this method called on all servers
        String correctedNewStatus = "QUALIFICATION".equals(request.getNewStatus()) ? "FINISHED" : request.getNewStatus();
        ChangeTournamentStateTask task = new ChangeTournamentStateTask(request.getTournamentId(), correctedNewStatus, request.getOldStatus());
        task.setApplicationContext(WebSocketRouter.getApplicationContext());
        scheduler.execute(task);

    }

    public void enableBotService(boolean enable) {
        LOG.debug("enableBotService: enable:{}", enable);

        try {
            boolean wasEnabled = botConfigInfoService.setBotServiceEnabled(enable);
            LOG.debug("enableBotService: wasEnabled={}, enable={}", wasEnabled, enable);

        } catch (Exception e) {
            LOG.error("enableBotService: error", e);
        }
    }

    public boolean isBotServiceEnabled() {
        boolean isBotServiceEnabled = false;
        try {
            isBotServiceEnabled = botConfigInfoService.isBotServiceEnabled();
        } catch (Exception e) {
            LOG.error("enableBotService: error", e);
        }

        LOG.debug("enableBotService: isBotServiceEnabled={}", isBotServiceEnabled);

        return isBotServiceEnabled;
    }

    public List<BotConfigInfoDto> getAllBotConfigInfos() {
        LOG.debug("getAllBotConfigInfos: Start:{}", toHumanReadableFormat(System.currentTimeMillis()));

        List<BotConfigInfoDto> tBotConfigInfos = new ArrayList<>();
        try {
            Collection<BotConfigInfo> botConfigInfos = botConfigInfoService.getAll();
            for (BotConfigInfo botConfigInfo : botConfigInfos) {

                LOG.debug("getAllBotConfigInfos: botConfigInfo:{}", botConfigInfo);
                if (botConfigInfo != null) {
                    BotConfigInfoDto tBotConfigInfo = BotConfigInfoUtil.toTBotConfigInfo(botConfigInfo);
                    LOG.debug("getAllBotConfigInfos: tBotConfigInfo:{}", tBotConfigInfo);
                    tBotConfigInfos.add(tBotConfigInfo);
                }
            }

        } catch (Exception e) {
            LOG.error("getAllBotConfigInfos: error", e);
        }

        return tBotConfigInfos;
    }

    public BotConfigInfoDto getBotConfigInfo(long botId) {
        LOG.debug("getBotConfigInfo: botId:{}", botId);

        if(botId < 0) {
            return null;
        }

        BotConfigInfoDto botConfigInfoDto = null;
        try {
            BotConfigInfo botConfigInfo = botConfigInfoService.get(botId);
            LOG.debug("getBotConfigInfo: botConfigInfo:{}", botConfigInfo);
            if (botConfigInfo != null) {
                botConfigInfoDto = BotConfigInfoUtil.toTBotConfigInfo(botConfigInfo);
            }

        } catch (Exception e) {
            LOG.error("getBotConfigInfo: for botId: {}", botId, e);
        }

        LOG.debug("getBotConfigInfo: tBotConfigInfo:{}", botConfigInfoDto);
        return botConfigInfoDto;
    }

    public BotConfigInfoDto getBotConfigInfoByUserName(String username) {
        LOG.debug("getBotConfigInfoByUserName: username:{}", username);

        if (StringUtils.isTrimmedEmpty(username)) {
            return null;
        }

        BotConfigInfoDto botConfigInfoDto = null;
        try {
            BotConfigInfo botConfigInfo = botConfigInfoService.getByUserName(username);
            LOG.debug("getBotConfigInfoByUserName: botConfigInfo:{}", botConfigInfo);
            if (botConfigInfo != null) {
                botConfigInfoDto = BotConfigInfoUtil.toTBotConfigInfo(botConfigInfo);
            }

        } catch (Exception e) {
            LOG.error("getBotConfigInfoByUserName: for username: {}", username, e);
        }

        LOG.debug("getBotConfigInfoByUserName: tBotConfigInfo:{}", botConfigInfoDto);
        return botConfigInfoDto;
    }

    public BotConfigInfoDto getBotConfigInfoByMqNickName(String mqNickname) {
        LOG.debug("getBotConfigInfoByMqNickName: mqNickname:{}", mqNickname);

        if (StringUtils.isTrimmedEmpty(mqNickname)) {
            return null;
        }

        BotConfigInfoDto botConfigInfoDto = null;
        try {
            BotConfigInfo botConfigInfo = botConfigInfoService.getByMqNickName(mqNickname);
            LOG.debug("getBotConfigInfoByMqNickName: botConfigInfo:{}", botConfigInfo);
            if (botConfigInfo != null) {
                botConfigInfoDto = BotConfigInfoUtil.toTBotConfigInfo(botConfigInfo);
            }

        } catch (Exception e) {
            LOG.error("getBotConfigInfoByMqNickName: for mqNickname: {}", mqNickname, e);
        }

        LOG.debug("getBotConfigInfoByMqNickName: tBotConfigInfo:{}", botConfigInfoDto);
        return botConfigInfoDto;
    }

    protected BotConfigInfoDto upsertBotConfigInfo(BotConfigInfoDto tBotConfigInfo) {

        BotConfigInfo botConfigInfoReturn = null;

        if (tBotConfigInfo == null) {
            LOG.error("upsertBotConfigInfo: tBotConfigInfo is null");
            return null;
        }

        try {
            BotConfigInfo botConfigInfo = BotConfigInfoUtil.fromTBotConfigInfo(tBotConfigInfo);
            LOG.debug("upsertBotConfigInfo: tBotConfigInfo:{}, botConfigInfo:{}",
                    tBotConfigInfo, botConfigInfo);

            long botId = tBotConfigInfo.getId();
            BotConfigInfo existingBotConfigInfo = null;

            if (botId > 0) {
                existingBotConfigInfo = botConfigInfoService.get(botId);
            }

            if (existingBotConfigInfo == null) {
                Avatar avatar = new Avatar(RNG.nextInt(2), RNG.nextInt(2), RNG.nextInt(2));
                botConfigInfo.setAvatar(avatar);
                LOG.debug("upsertBotConfigInfo: bot with id:{} does not exist create new {}", botId, botConfigInfo);
                botConfigInfoReturn = botConfigInfoService.create(botConfigInfo);
            } else {
                LOG.debug("upsertBotConfigInfo: bot with id:{} exists {} update to {}",
                        botId, existingBotConfigInfo, botConfigInfo);
                botConfigInfoReturn = botConfigInfoService.update(
                        botId,
                        botConfigInfo.getAllowedGames(),
                        botConfigInfo.isActive(),
                        botConfigInfo.getPassword(),
                        botConfigInfo.getMqNickname(),
                        existingBotConfigInfo.getAvatar(),
                        botConfigInfo.getTimeFrames(),
                        botConfigInfo.getAllowedBankIds(),
                        botConfigInfo.getShootsRates(),
                        botConfigInfo.getBulletsRates(),
                        botConfigInfo.getAllowedRoomValues()
                );
            }

        } catch (Exception e) {
            LOG.error("upsertBotConfigInfo: error to upsertBotConfigInfo for tBotConfigInfo: {}",
                    tBotConfigInfo, e);
        }

        LOG.debug("upsertBotConfigInfo: botConfigInfoReturn:{}", botConfigInfoReturn);

        if(botConfigInfoReturn != null) {

            try {
                if (isBotServiceEnabled() && botServiceClient != null) {
                    BotStatusResult status = botServiceClient.getStatusForNewBot(
                            botConfigInfoReturn.getUsername(),
                            botConfigInfoReturn.getPassword(),
                            botConfigInfoReturn.getMqNickname(),
                            botConfigInfoReturn.getBankId(),
                            GameType.BG_DRAGONSTONE.getGameId()
                    );

                    if (status.isSuccess()) {

                        botConfigInfoService.updateBalance(
                                botConfigInfoReturn.getId(),
                                status.getMmcBalance(),
                                status.getMqcBalance());

                        botConfigInfoReturn.setMqcBalance(status.getMqcBalance());
                        botConfigInfoReturn.setMmcBalance(status.getMmcBalance());
                    }
                }
            } catch (Exception exception) {
                LOG.debug("upsertBotConfigInfo: Exception during update balance for:{}", botConfigInfoReturn, exception);
            }

            return BotConfigInfoUtil.toTBotConfigInfo(botConfigInfoReturn);
        }

        return null;
    }

    public List<BotConfigInfoDto> upsertBotConfigInfo(List<BotConfigInfoDto> botConfigInfos) {
        if (botConfigInfos == null) {
            LOG.debug("upsertBotConfigInfo: tBotConfigInfos is null, skip");
            return null;
        }

        List<BotConfigInfoDto> botConfigInfosReturn = new ArrayList<>();

        for (BotConfigInfoDto botConfigInfo : botConfigInfos) {
            BotConfigInfoDto botConfigInfoReturn = upsertBotConfigInfo(botConfigInfo);
            LOG.debug("upsertBotConfigInfo: tBotConfigInfoReturn:{}", botConfigInfoReturn);
            botConfigInfosReturn.add(botConfigInfoReturn);
        }

        return botConfigInfosReturn;
    }

    protected BotConfigInfoDto removeBotConfigInfo(Long botId) {

        if (botId == null) {
            LOG.error("removeBotConfigInfo: botId is null");
            return null;
        }

        BotConfigInfo botConfigInfo = null;

        try {

            botConfigInfo = botConfigInfoService.get(botId);
            LOG.debug("removeBotConfigInfo: for botId: {}, botConfigInfo={}", botId, botConfigInfo);
            botConfigInfoService.remove(botId);

            if (botConfigInfo != null) {
                return BotConfigInfoUtil.toTBotConfigInfo(botConfigInfo);
            }

        } catch (Exception e) {
            LOG.error("removeBotConfigInfo: error to removeBotConfigInfo for botId: {}", botId, e);
        }

        return null;
    }

    public List<BotConfigInfoDto> removeBotConfigInfo(List<Long> botIds) {

        if (botIds == null) {
            LOG.debug("removeBotConfigInfo: botIds is null, skip");
            return null;
        }

        List<BotConfigInfoDto> tBotConfigInfosReturn = new ArrayList<>();

        for (Long botId : botIds) {
            BotConfigInfoDto tBotConfigInfoReturn = removeBotConfigInfo(botId);
            LOG.debug("removeBotConfigInfo: tBotConfigInfoReturn:{}", tBotConfigInfoReturn);
            tBotConfigInfosReturn.add(tBotConfigInfoReturn);
        }

        return tBotConfigInfosReturn;
    }
}
