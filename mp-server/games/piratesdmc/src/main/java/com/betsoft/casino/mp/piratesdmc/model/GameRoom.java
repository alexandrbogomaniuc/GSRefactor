package com.betsoft.casino.mp.piratesdmc.model;

import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.piratescommon.model.math.EnemyType;
import com.betsoft.casino.mp.piratescommon.model.math.ShotCalculator;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.abs.casino.common.exception.CommonException;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.betsoft.casino.mp.piratesdmc.model.EnemyRange.BaseEnemies;


public class GameRoom extends AbstractActionGameRoom<EnemyGame, GameMap, Seat, GameRoomSnapshot, Enemy, EnemyType, ISingleNodeRoomInfo,
        IActionRoomPlayerInfo> {


    private final List<ITransportEnemy> possibleEnemies;

    public GameRoom(ApplicationContext context, Logger logger, ISingleNodeRoomInfo roomInfo, GameMap map,
                    IPlayerStatsService playerStatsService, IWeaponService weaponService,
                    IExecutorService remoteExecutorService, IPlayerQuestsService playerQuestsService,
                    IPlayerProfileService playerProfileService,
                    IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                    IActiveCashBonusSessionService activeCashBonusSessionService, ITournamentService tournamentService) {
        super(context, logger, new Seat[roomInfo.getMaxSeats()], roomInfo, new EnemyGame(logger, gameConfigService), map,
                playerStatsService, playerQuestsService, weaponService, remoteExecutorService,
                playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService,
                tournamentService);
        possibleEnemies = convertEnemies(EnemyType.values());
    }

    public GameRoom(ApplicationContext context, Logger logger, ISingleNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService,
                    IWeaponService weaponService, IExecutorService remoteExecutorService, GameRoomSnapshot snapshot,
                    IPlayerQuestsService playerQuestsService,
                    IPlayerProfileService playerProfileService, IGameConfigService gameConfigService,
                    IActiveFrbSessionService activeFrbSessionService,
                    IActiveCashBonusSessionService activeCashBonusSessionService, ITournamentService tournamentService) {
        super(context, logger, snapshot.getSeats(), roomInfo, new EnemyGame(logger, gameConfigService), snapshot.getMap(),
                playerStatsService, playerQuestsService, weaponService, remoteExecutorService,
                playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService,
                tournamentService);
        this.nextMapId = snapshot.getNextMapId();
        this.gameState = snapshot.getGameState();
        possibleEnemies = convertEnemies(EnemyType.values());
    }

    @Override
    public GameType getGameType() {
        return GameType.DMC_PIRATES;
    }

    @Override
    protected IGameState getWaitingPlayersGameState() {
        return new WaitingPlayersGameState(this);
    }

    @Override
    protected List<Integer> getWeaponLootBoxPrices() {
        return Collections.EMPTY_LIST;
    }

    @Override
    protected List<EnemyType> getBaseEnemyTypes() {
        return BaseEnemies.getEnemies();
    }

    void setPossibleEnemies(EnemyRange possibleEnemies) {
        getMap().setPossibleEnemies(possibleEnemies);
    }

    @Override
    public GameRoomSnapshot getSnapshot() {
        return new GameRoomSnapshot(getId(), getRoomInfo().getRoundId(), seats, getMap(), nextMapId, gameState);
    }

    @Override
    public Seat createSeat(IActionRoomPlayerInfo playerInfo, IGameSocketClient socketClient, double currentRate) {
        return new Seat(playerInfo, socketClient, currentRate);
    }

    @Override
    public void placeMineToMap(Seat seat, IMineCoordinates mineCoordinates) throws CommonException {
        getLog().debug("placeMineToMap: AID={}, mineCoordinates={}", seat.getAccountId(), mineCoordinates);
        getGameState().placeMineToMap(seat, mineCoordinates);
    }

    @Override
    protected List<IMinePlace> getAllMinePlaces() {
        List<Seat> seats = getSeats();
        List<IMinePlace> mines = new ArrayList<>();
        for (Seat seat : seats) {
            mines.addAll(seat.getMinePlaces(map.getCoords()));
        }
        return mines;
    }

    @Override
    public Logger getLog() {
        return logger;
    }

    @Override
    protected List<ITransportEnemy> getTransportEnemies() {
        return possibleEnemies;
    }

    public void sendNewEnemyMessage(Enemy enemy) {
        sendChanges(getTOFactoryService().createNewEnemy(getCurrentTime(), convert(enemy, true)));
    }

    @Override
    public IRoomEnemy convert(Enemy enemy, boolean fillTrajectory) {
        EnemyType enemyType = enemy.getEnemyClass().getEnemyType();

        return getTOFactoryService().createRoomEnemy(
                enemy.getId(),
                enemyType.getId(),
                enemyType.isBoss(),
                enemy.getSpeed(),
                enemy.getAwardedPrizesAsString(),
                enemy.getAwardedSum().toDoubleCents(),
                enemy.getEnergy(),
                enemy.getSkin(),
                fillTrajectory ? convertTrajectory(enemy.getTrajectory(), System.currentTimeMillis()) : null,
                enemy.getParentEnemyId(),
                enemy.getFullEnergy(),
                enemy.getMembers(),
                enemy.getSwarmId(),
                enemy.getSwarmType()
        );
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameRoom gameRoom = (GameRoom) o;
        return Objects.equals(roomInfo, gameRoom.roomInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomInfo);
    }

    @Override
    public IWeaponLootBox generateWeaponLootBox(Seat seat, int rid, int size, int usedAmmoAmount, Money usedMoney) {
        return null;
    }

    @Override
    protected void calculateWeaponsSurplusCompensation(Seat seat) {
    }

    @Override
    protected boolean isBossImmortal(IGameSocketClient client) {
        return false;
    }


    @Override
    public void addFreeShotsToQueue(Seat seat, IAddFreeShotsToQueue message) throws CommonException {
    }


    public void saveMines(Seat seat) {
    }

    @Override
    public void saveMinesWithLock(Seat seat) {
    }

    @Override
    public void compensateSpecialWeaponsWithLock(Seat seat) {
        if (getRoomInfo().isBonusSession()) {
            getLog().debug("compensateSpecialWeapons isBonusSessions, weapons will be reset without compensation");
            seat.resetWeapons();
        } else {
            compensateSpecialWeapons(seat);
        }
    }

    @Override
    public void compensateSpecialWeapons(Seat seat) {
        ShotCalculator.compensateSpecialWeapons(seat, getTOFactoryService(), getLog());
    }
}
