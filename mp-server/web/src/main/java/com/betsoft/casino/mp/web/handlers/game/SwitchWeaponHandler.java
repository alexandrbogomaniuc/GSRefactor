package com.betsoft.casino.mp.web.handlers.game;

import com.betsoft.casino.mp.common.SeatBullet;
import com.betsoft.casino.mp.data.service.ServerConfigService;
import com.betsoft.casino.mp.model.IActionGameSeat;
import com.betsoft.casino.mp.model.IActionRoomPlayerInfo;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.service.MultiNodeRoomInfoService;
import com.betsoft.casino.mp.service.RoomPlayerInfoService;
import com.betsoft.casino.mp.service.SingleNodeRoomInfoService;
import com.betsoft.casino.mp.transport.SwitchWeapon;
import com.betsoft.casino.mp.transport.Weapon;
import com.betsoft.casino.mp.transport.WeaponSwitched;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.mp.web.IMessageSerializer;
import com.betsoft.casino.mp.web.service.RoomServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.betsoft.casino.utils.TObject.SERVER_RID;

@Component
public class SwitchWeaponHandler extends AbstractRoomHandler<SwitchWeapon, IGameSocketClient> {
    private static final Logger LOG = LogManager.getLogger(SwitchWeaponHandler.class);

    public SwitchWeaponHandler(IMessageSerializer serializer, SingleNodeRoomInfoService singleNodeRoomInfoService,
                               MultiNodeRoomInfoService multiNodeRoomInfoService,
                               RoomPlayerInfoService playerInfoService, RoomServiceFactory roomServiceFactory,
                               ServerConfigService serverConfigService) {
        super(serializer, singleNodeRoomInfoService, multiNodeRoomInfoService, playerInfoService, roomServiceFactory, serverConfigService);
    }

    @Override
    public void handle(WebSocketSession session, SwitchWeapon message, IGameSocketClient client) {
        if (client.getRoomId() == null || client.getSeatNumber() < 0) {
            sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());
            return;
        }
        try {

            IRoom room = getRoomWithCheck(message.getRid(), client.getRoomId(), client, client.getGameType());

            if (room != null) {

                IActionGameSeat seat = (IActionGameSeat) room.getSeat(client.getSeatNumber());

                if (seat == null || client.getAccountId() == null || seat.getAccountId() != client.getAccountId()) {

                    sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Not seat", message.getRid());

                } else {

                    IActionRoomPlayerInfo playerInfo = (IActionRoomPlayerInfo) seat.getPlayerInfo();

                    if (playerInfo == null) {

                        getLog().error("handle: client={}, seat={}", client, seat);
                        sendErrorMessage(client, ErrorCodes.NOT_SEATER, "Room player not found", message.getRid());

                    } else {

                        seat.setWeapon(message.getWeaponId());
                        Map<SpecialWeaponType, com.betsoft.casino.mp.common.Weapon> seatWeapons = seat.getWeapons();
                        List<Weapon> weapons = new ArrayList<>();

                        seatWeapons.forEach(
                                (type, weapon) ->
                                        weapons.add(new Weapon(type.getId(), weapon.getShots()))
                        );

                        Set<SeatBullet> seatBullets = seat.getBulletsOnMap();

                        for (Weapon weapon : weapons) {

                            long numberOfBulletsOnMap = seatBullets.stream()
                                    .filter(
                                            seatBullet -> seatBullet.getWeaponId() == weapon.getId())
                                    .count();

                            int realShotsRemained = weapon.getShots() - (int)numberOfBulletsOnMap;

                            if(realShotsRemained < 0) {
                                realShotsRemained = 0;
                            }

                            getLog().debug("handle: weapon.getId()={}, realShotsRemained={}, weapon.getShots()={}, numberOfBulletsOnMap={}, seat={}",
                                    weapon.getId(), realShotsRemained, weapon.getShots(), numberOfBulletsOnMap, seat);

                            weapon.setShots(realShotsRemained);
                        }


                        room.sendChanges(
                                new WeaponSwitched(System.currentTimeMillis(), SERVER_RID, message.getWeaponId(), seat.getNumber(), new ArrayList<>()),
                                new WeaponSwitched(System.currentTimeMillis(), message.getRid(), message.getWeaponId(), seat.getNumber(), weapons),
                                seat.getAccountId(), message);
                        updatePlayerInfo(message.getWeaponId(), playerInfo);
                    }
                }
            }
        } catch (Exception e) {
            processUnexpectedError(client, message, e);
        }
    }

    private void updatePlayerInfo(int specialWeaponId, IActionRoomPlayerInfo playerInfo) {
        playerInfoService.lock(playerInfo.getId());
        getLog().debug("updatePlayerInfo HS lock: {}", playerInfo.getId());
        try {
            IActionRoomPlayerInfo roomPlayerInfo = (IActionRoomPlayerInfo) playerInfoService.get(playerInfo.getId());
            if (roomPlayerInfo != null) {
                roomPlayerInfo.setSpecialWeaponId(specialWeaponId);
                playerInfoService.put(roomPlayerInfo);
            } else {
                //paranoid: rat race case
                LOG.warn("updatePlayerInfo: roomPlayerInfo not found for accountId={}, roomId={}", playerInfo.getId(),
                        playerInfo.getRoomId());
            }
        } finally {
            playerInfoService.unlock(playerInfo.getId());
            getLog().debug("updatePlayerInfo HS unlock: {}", playerInfo.getId());
        }
    }

    @Override
    public Logger getLog() {
        return LOG;
    }
}
