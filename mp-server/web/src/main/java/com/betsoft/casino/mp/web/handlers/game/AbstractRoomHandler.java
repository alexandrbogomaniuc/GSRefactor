package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IRoomPlayerInfo;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.IRoomInfoService;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.handlers.IMessageHandler;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.utils.TInboundObject;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 17.11.17.
 */
public abstract class AbstractRoomHandler<MESSAGE extends TObject, CLIENT extends IGameSocketClient>
        implements IMessageHandler<MESSAGE, CLIENT> {
    private static final int RETRY_PENDING_OPERATIONS_COUNT = 20;
    protected final IMessageSerializer serializer;
    protected final SingleNodeRoomInfoService singleNodeRoomInfoService;
    protected final MultiNodeRoomInfoService multiNodeRoomInfoService;
    protected final RoomPlayerInfoService playerInfoService;
    protected final RoomServiceFactory roomServiceFactory;
    protected final ServerConfigService serverConfigService;

    public AbstractRoomHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                               MultiNodeRoomInfoService multiNodeRoomInfoService,
                               RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                               ServerConfigService serverConfigService) {
        this.serializer = serializer;
        this.singleNodeRoomInfoService = singleNodeRoomInfoService;
        this.multiNodeRoomInfoService = multiNodeRoomInfoService;
        this.playerInfoService = playerInfoService;
        this.roomServiceFactory = roomServiceFactory;
        this.serverConfigService = serverConfigService;
    }

    @SuppressWarnings("rawtypes")
    protected IRoom getRoomWithCheck(int requestId, Long roomId, CLIENT client, GameType gameType) throws CommonException {

        if(roomId == null) {
            getLog().error("roomId is null, please first OpenRoom");
            sendErrorMessage(client, ErrorCodes.ROOM_NOT_FOUND, "Room not found: roomId is null, please first OpenRoom",
                    requestId);
            return null;
        }

        //not required, call to distributed (hazelcast) map is too expensive
/*        IRoomInfo roomInfo = roomInfoService.getRoom(roomId);
        if(roomInfo == null) {
            sendErrorMessage(client, ErrorCodes.ROOM_NOT_FOUND, "Room not found", requestId);
            return null;
        }*/

        IRoom room = roomServiceFactory.getRoom(gameType, roomId);

        if (room == null) {
            sendErrorMessage(client, ErrorCodes.ROOM_MOVED, "Room moved to another server", requestId);
            return null;
        }

        return room;
    }

    @SuppressWarnings("rawtypes")
    protected AbstractActionGameRoom getActionRoomWithCheck(int requestId, Long roomId, CLIENT client, GameType gameType) throws CommonException {
        IRoom room = getRoomWithCheck(requestId, roomId, client, gameType);
        if (room instanceof AbstractActionGameRoom) {
            return (AbstractActionGameRoom) room;
        } else {
            throw new CommonException("Unsupported room type");
        }
    }

    public boolean hasPlayersWithPendingOperation(long roomId) {
        int cnt = RETRY_PENDING_OPERATIONS_COUNT;
        boolean hasPendingOperations = false;
        while (cnt-- > 0) {
            hasPendingOperations = playerInfoService.hasPlayersWithPendingOperation(roomId);
            if (hasPendingOperations) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    getLog().error(e.getMessage(), e);
                }
            } else {
                break;
            }
        }
        return hasPendingOperations;
    }

    public boolean hasPendingOperations(long accountId, CLIENT client, TInboundObject message) {
        int cnt = RETRY_PENDING_OPERATIONS_COUNT;
        boolean hasPendingOperation = false;
        IRoomPlayerInfo playerInfo = null;
        while (cnt-- > 0) {
            playerInfo = playerInfoService.get(accountId);
            if (playerInfo == null) {
                break;
            }
            hasPendingOperation = playerInfo.isPendingOperation();
            if (!hasPendingOperation) {
                break;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                getLog().error(e.getMessage(), e);
            }
        }

        getLog().debug("cnt:{},  playerInfo:{}, hasPendingOperation: {}, message: {}",
                cnt, playerInfo, hasPendingOperation, message);

        if (playerInfo != null && hasPendingOperation) {
            getLog().error("handle: found payment operation, playerInfo={}", playerInfo);
            sendErrorMessage(client, ErrorCodes.FOUND_PENDING_OPERATION, "Found pending operation", message.getRid());
        }
        return hasPendingOperation;
    }

    protected IRoomInfoService getRoomInfoService(IGameSocketClient client) {
        return client.getGameType().isSingleNodeRoomGame() ? singleNodeRoomInfoService : multiNodeRoomInfoService;
    }
}
