package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.bgsectorx.model.math.MathData;
import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.common.SeatBullet;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.IActionGameSeat;
import com.betsoft.casino.mp.model.LobbySession;
import com.betsoft.casino.mp.model.RoomState;
import com.betsoft.casino.mp.service.LobbySessionService;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.Bullet;
import com.betsoft.casino.mp.transport.BulletResponse;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import com.betsoft.casino.mp.web.service.SocketService;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Set;

import static com.betsoft.casino.utils.TObject.SERVER_RID;

@Component
public class BulletHandler extends AbstractRoomHandler<Bullet, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(BulletHandler.class);
    private final SocketService socketService;
    protected final LobbySessionService lobbySessionService;

    public BulletHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                         MultiNodeRoomInfoService multiNodeRoomInfoService,
                         RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                         SocketService socketService,
                         ServerConfigService serverConfigService,
                         LobbySessionService lobbySessionService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
        this.socketService = socketService;
        this.lobbySessionService = lobbySessionService;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handle(WebSocketSession session, Bullet message, IGameSocketClient client) {
        Long accountId = client.getAccountId();
        if (client.getRoomId() == null || client.getSeatNumber() < 0 || accountId == null) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
            return;
        }
        LobbySession lobbySession = lobbySessionService.get(client.getSessionId());
        if (lobbySession == null) {
            sendErrorMessage(client, ErrorCodes.INVALID_SESSION, "Session not found", message.getRid());
            return;
        }
        try {

            AbstractActionGameRoom room = getActionRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());
            if (room != null) {
                if (hasPendingOperations(accountId, client, message)) {
                    return;
                }
                playerInfoService.lock(accountId);
                getLog().debug("handle HS lock: {}", accountId);
                try {
                    IActionGameSeat seat = (IActionGameSeat) room.getSeat(client.getSeatNumber());
                    if (seat == null || seat.getAccountId() != accountId) {
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid(), message);
                    } else {

                        int betLevel = seat.getBetLevel();
                        GameType gameType = room.getGameType();

                        short maxBulletsOnMap = room.getRoomInfo().getGameType().getMaxBulletsOnMap();
                        RoomState state = room.getState();
                        if (!state.equals(RoomState.PLAY)) {
                            sendErrorMessage(client, ErrorCodes.ROUND_NOT_STARTED, "Round not started",
                                    message.getRid(), message);
                            return;
                        }

                        Set bulletsOnMap = seat.getBulletsOnMap();
                        String bulletId = message.getBulletId();
                        int seatIdMessage = Integer.parseInt(bulletId.split("_")[0]);
                        if (maxBulletsOnMap == 0 || bulletsOnMap.size() == maxBulletsOnMap) {
                            LOG.error("handle: Not allowed place bullet to map (maxBulletsOnMap reached), " +
                                            "seat.getNumber()={}, maxBulletsOnMap={}, bulletsOnMap.size()={}, bulletsOnMap={}",
                                    seat.getNumber(), maxBulletsOnMap, bulletsOnMap.size(), bulletsOnMap);
                            sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_PLACE_BULLET,
                                    "Not allowed place bullet to map (maxBulletsOnMap reached)", message.getRid(), message);
                            return;
                        }

                        if (!room.bulletPlaceAllowed(seat.getNumber())) {
                            LOG.error("handle: Not allowed place bullet in this state, seat.getNumber()={}", seat.getNumber());
                            sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_PLACE_BULLET,
                                    "Not allowed place bullet in this state seat " + seat.getNumber(), message.getRid(), message);
                            return;
                        }

                        if (seatIdMessage != client.getSeatNumber()) {
                            LOG.error("handle: Not allowed place bullet to map (wrong seat id), seat.getNumber()={}, seatIdMessage={}",
                                    seat.getNumber(), seatIdMessage);
                            sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_PLACE_BULLET,
                                    "Not allowed place bullet to map (wrong seat id)", message.getRid(), message);
                            return;
                        }

                        if (message.getWeaponId() != seat.getCurrentWeaponId()) {
                            LOG.error("handle: Not allowed place bullet to map (wrong weapon id), seat.getCurrentWeaponId()={}, message.getWeaponId()={}",
                                    seat.getCurrentWeaponId(), message.getWeaponId());
                            sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_PLACE_BULLET,
                                    "Not allowed place bullet to map (wrong weapon id)", message.getRid(), message);
                            return;
                        }

                        SeatBullet seatBullet = new SeatBullet(message.getBulletTime(),
                                message.getBulletAngle(),
                                bulletId,
                                message.getStartPointX(),
                                message.getStartPointY(),
                                message.getEndPointX(),
                                message.getEndPointY(),
                                message.getWeaponId()
                        );

                        boolean isSuccess = seat.addSeatBullet(seatBullet);

                        if (isSuccess) {

                            LOG.debug("handle: bullet {}, added for seat: {}, all bullets: {}",
                                    seatBullet, seat.getAccountId(), seat.getBulletsOnMap());

                            BulletResponse allMessage = new BulletResponse(System.currentTimeMillis(), SERVER_RID,
                                    seatBullet.getBulletTime(),
                                    seatBullet.getBulletAngle(),
                                    seatBullet.getBulletId(),
                                    message.getStartPointX(),
                                    message.getStartPointY(),
                                    message.getEndPointX(),
                                    message.getEndPointY(),
                                    seatBullet.getWeaponId());

                            BulletResponse seatMessage = new BulletResponse(System.currentTimeMillis(), message.getRid(),
                                    seatBullet.getBulletTime(),
                                    seatBullet.getBulletAngle(),
                                    seatBullet.getBulletId(),
                                    message.getStartPointX(),
                                    message.getStartPointY(),
                                    message.getEndPointX(),
                                    message.getEndPointY(),
                                    seatBullet.getWeaponId());

                            room.sendChanges(allMessage, seatMessage, accountId, message);

                        } else {
                            LOG.error("handle: Not allowed place bullet to map, seat.getNumber()={}, message={}", seat.getNumber(), message);
                            sendErrorMessage(client, ErrorCodes.NOT_ALLOWED_PLACE_BULLET, "Not allowed place bullet to map", message.getRid(), message);
                        }
                    }
                } finally {
                    playerInfoService.unlock(accountId);
                    getLog().debug("handle HS unlock: {}", accountId);
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}

