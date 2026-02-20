package com.betsoft.casino.mp.pirates.model;

import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.pirates.model.math.*;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.RNG;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.betsoft.casino.mp.model.SpecialWeaponType.values;
import static com.betsoft.casino.mp.pirates.model.math.EnemyRange.BaseEnemies;

/**
 * User: flsh
 * Date: 07.02.19.
 */
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
        return GameType.PIRATES;
    }

    @Override
    protected IGameState getWaitingPlayersGameState() {
        return new WaitingPlayersGameState(this);
    }

    @Override
    protected List<Integer> getWeaponLootBoxPrices() {
        return MathData.WEAPON_LOOT_BOX_PRICES;
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

        List<WeaponLootBoxProb.WeaponEntry> weaponEntries = WeaponLootBoxProb.getTables200().get(0);
        Weapon weapon = getSpecialWeapon(weaponEntries.get(RNG.nextInt(weaponEntries.size())), size);
        Integer cost = MathData.WEAPON_LOOT_BOX_PRICES.get(size);
        int shots = weapon.getShots();
        if (getState() == RoomState.PLAY && !getGameState().isRoundWasFinished()) {
            PlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
            roundInfo.addLootBoxStat(cost, size, shots, weapon.getType().getTitle());
            roundInfo.addKpiInfoLootbox(cost * seat.getStake().toCents());
            return addWeaponLootBoxToSeat(seat, weapon, rid, size, usedAmmoAmount);
        } else if (getState() == RoomState.QUALIFY) {
            //need return money
            seat.incrementRoundWin(usedMoney);
            getLog().warn("generateWeaponLootBox: round finished return money: {}, seat={}", usedMoney.toCents(), seat);
        } else {
            getLog().error("generateWeaponLootBox: error found, round in unexpected state. " +
                    "cannot return money: {}, seat={}", usedMoney.toCents(), seat);
        }
        return null;
    }

    @Override
    protected void calculateWeaponsSurplusCompensation(Seat seat) {
    }

    private Weapon getSpecialWeapon(WeaponLootBoxProb.WeaponEntry weapon, int size) {
        SpecialWeaponType specialWeaponType = values()[weapon.getType()];
        int shots = weapon.getShots();
        if (size == 1)
            shots = 2 * shots;
        else if (size == 2) {
            shots = 3 * shots;
        }
        return new Weapon(shots, specialWeaponType);
    }

    @Override
    protected boolean isBossImmortal(IGameSocketClient client) {
        return false;
    }


    @Override
    public void addFreeShotsToQueue(Seat seat, IAddFreeShotsToQueue message) throws CommonException {
        getLog().debug("addFreeShotsToQueue for seat: {}, message: {} ", seat.getAccountId(), message);
        seat.getSocketClient().sendMessage(getTOFactoryService().getOkResponse(getCurrentTime(), message.getRid()), message);
        seat.getFreeShots().processTempFreeShots(FreeShotQueueType.valueOf(message.getQueue().toUpperCase()));
        getLog().debug("addFreeShotsToQueue for seat: {}, freeShots: {} ", seat.getAccountId(), seat.getFreeShots());
    }


    public void saveMines(Seat seat) {
        int minesCount = seat.getSeatMines().size();
        if (minesCount > 0) {
            getLog().debug("add {} mines  to player: {}, w: {}", minesCount, seat.getAccountId(),
                    seat.getWeapons());
            seat.addWeapon(SpecialWeaponType.Landmines.getId(), minesCount);
            seat.getSeatMines().clear();
        }
    }

    @Override
    public void compensateSpecialWeapons(Seat seat) {
        ShotCalculator.compensateSpecialWeapons(seat, getRoomInfo(), getTOFactoryService(), getLog());
    }
}
