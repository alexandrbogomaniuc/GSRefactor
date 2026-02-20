package com.betsoft.casino.mp.sectorx.model;

import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.common.AbstractPlayGameState;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.*;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.sectorx.model.math.EnemyRange;
import com.betsoft.casino.mp.sectorx.model.math.EnemyType;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.dgphoenix.casino.common.exception.CommonException;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.betsoft.casino.mp.sectorx.model.math.EnemyRange.BASE_ENEMIES;

public class GameRoom extends AbstractActionGameRoom<EnemyGame, GameMap, Seat, GameRoomSnapshot, Enemy, EnemyType, ISingleNodeRoomInfo,
        IActionRoomPlayerInfo> {
    private final List<ITransportEnemy> possibleEnemies;
    private transient int bossNumberShots;
    private transient Map<Integer, Boolean> usedWaves = new HashMap<>();

    public GameRoom(ApplicationContext context, Logger logger, ISingleNodeRoomInfo roomInfo, GameMap map,
                    IPlayerStatsService playerStatsService, IWeaponService weaponService,
                    IExecutorService remoteExecutorService, IPlayerQuestsService playerQuestsService,
                    IPlayerProfileService playerProfileService,
                    IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                    IActiveCashBonusSessionService activeCashBonusSessionService,
                    ITournamentService tournamentService, IGameConfigProvider gameConfigProvider,
                    ISpawnConfigProvider spawnConfigProvider) {
        super(context, logger, new Seat[roomInfo.getMaxSeats()], roomInfo,
                new EnemyGame(logger, gameConfigService, gameConfigProvider, spawnConfigProvider), map,
                playerStatsService, playerQuestsService, weaponService,
                remoteExecutorService, playerProfileService, gameConfigService, activeFrbSessionService,
                activeCashBonusSessionService, tournamentService);
        possibleEnemies = convertEnemies(EnemyType.values());
    }

    public GameRoom(ApplicationContext context, Logger logger, ISingleNodeRoomInfo roomInfo, IPlayerStatsService playerStatsService,
                    IWeaponService weaponService, IExecutorService remoteExecutorService, GameRoomSnapshot snapshot,
                    IPlayerQuestsService playerQuestsService,
                    IPlayerProfileService playerProfileService, IGameConfigService gameConfigService,
                    IActiveFrbSessionService activeFrbSessionService,
                    IActiveCashBonusSessionService activeCashBonusSessionService, ITournamentService tournamentService,
                    IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
        super(context, logger, snapshot.getSeats(), roomInfo,
                new EnemyGame(logger, gameConfigService, gameConfigProvider, spawnConfigProvider), snapshot.getMap(),
                playerStatsService, playerQuestsService, weaponService, remoteExecutorService,
                playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService,
                tournamentService);
        this.nextMapId = snapshot.getNextMapId();
        this.gameState = snapshot.getGameState();
        possibleEnemies = convertEnemies(EnemyType.values());
    }

    public Map<Integer, Boolean> getUsedWaves() {
        return usedWaves;
    }

    public void setUsedWave(Integer key, Boolean obj) {
        usedWaves.put(key, obj);
    }

    @Override
    public GameType getGameType() {
        return GameType.SECTOR_X;
    }

    @Override
    protected IGameState getWaitingPlayersGameState() {
        return new WaitingPlayersGameState(this);
    }

    @Override
    protected List<Integer> getWeaponLootBoxPrices() {
        return new ArrayList<>();
    }

    @Override
    protected List<EnemyType> getBaseEnemyTypes() {
        return BASE_ENEMIES.getEnemies();
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

    @Override
    public void sendNewEnemyMessage(Enemy enemy) {
        sendChanges(getTOFactoryService().createNewEnemy(getCurrentTime(), convert(enemy, true)));
    }

    @Override
    public IRoomEnemy convert(Enemy enemy, boolean fillTrajectory) {
        EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
        Trajectory trajectory = enemy.getTrajectory() instanceof BezierTrajectory || enemy.getTrajectory() instanceof HybridTrajectory
                || enemy.getTrajectory() instanceof ParallelTrajectory || enemy instanceof EnemySpecialItem
                ? enemy.getTrajectory() : convertTrajectory(enemy.getTrajectory(),  System.currentTimeMillis());

        return getTOFactoryService().createRoomEnemy(
                enemy.getId(),
                enemyType.getId(),
                enemyType.isBoss(),
                enemy.getSpeed(),
                enemy.getAwardedPrizesAsString(),
                enemy.getAwardedSum().toDoubleCents(),
                getHP(enemy),
                enemy.getSkin(),
                fillTrajectory ? trajectory : null,
                enemy.getParentEnemyId(),
                getFullHP(enemy),
                enemy.getMembers(),
                enemy.getSwarmId(),
                enemy.getSwarmType(),
                enemy.getParentEnemyTypeId()
        );
    }

    private double getHP(Enemy enemy) {
        return enemy.isBoss() ? enemy.getEnergy() : enemy.getLives() + 1.;
    }

    private double getFullHP(Enemy enemy) {
        return enemy.isBoss() ? enemy.getFullEnergy() : 1;
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
    public IGetRoomInfoResponse getRoomInfoResponse(int requestId, IGameSocketClient client, String playerCurrency) throws CommonException {
        IGetRoomInfoResponse response = super.getRoomInfoResponse(requestId, client, playerCurrency);
        response.setBossNumberShots(bossNumberShots);
        response.setEndTime(gameState.getEndRoundTime());
        IGameState gameState = getGameState();
        if (gameState instanceof AbstractPlayGameState) {
            response.setNeedWaitingWhenEnemiesLeave(((PlayGameState) gameState).isNeedWaitingWhenEnemiesLeave());
        }
        return response;
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
    }

    @Override
    protected List<ITransportSeat> getTransportSeats() {
        List<ITransportSeat> result = new ArrayList<>();
        getSeats()
                .stream()
                .filter(Objects::nonNull)
                .forEach(seat -> {
                            IActiveFrbSession frbSession = seat.getPlayerInfo().getActiveFrbSession();
                            long roundWin = frbSession != null
                                    ? frbSession.getWinSum() + seat.getRoundWin().toCents()
                                    : seat.getRoundWin().add(seat.getRebuyFromWin()).toCents();
                            result.add(getTOFactoryService().createSeat(seat.getNumber(), seat.getNickname(),
                                    seat.getJoinDate(), seat.getTotalScore() == null ? 0 : seat.getTotalScore().getAmount(),
                                    seat.getCurrentScore() == null ? 0 : seat.getCurrentScore().getAmount(),
                                    seat.getAvatar(), seat.getSpecialWeaponId(), seat.getLevel(),
                                    seat.getFreeShots().getUnplayedFreeShots(), seat.getCurrentPlayerRoundInfo().getTotalDamage(),
                                    seat.getBetLevel(), roundWin));
                        }
                );
        return result;
    }

    @Override
    public IFullGameInfo getFullGameInfo(IGetFullGameInfo request, IGameSocketClient client) {
        IFullGameInfo gameInfo = (IFullGameInfo) super.getFullGameInfo(request, client);
        gameInfo.setBossNumberShots(bossNumberShots);
        gameInfo.setEndTime(gameState.getEndRoundTime());
        IGameState gameState = getGameState();
        if (gameState instanceof AbstractPlayGameState) {
            gameInfo.setNeedWaitingWhenEnemiesLeave(((PlayGameState) gameState).isNeedWaitingWhenEnemiesLeave());
        }
        return gameInfo;
    }

    @Override
    public boolean tryChangeBetLevel(long accountId, int betLevel) {
        Seat seatByAccountId = getSeatByAccountId(accountId);
        IGameState gameState = getGameState();
        if (gameState.getRoomState().equals(RoomState.PLAY)) {
            PlayGameState playGameState = (PlayGameState) gameState;
            boolean isLocked = false;
            ReentrantLock lockShots = playGameState.getLockShots();
            try {
                getLog().debug("tryChangeBetLevel: {}", lockShots);
                isLocked = lockShots.tryLock(10, TimeUnit.SECONDS);
                if (isLocked) {
                    if (getRoomInfo().getMoneyType().equals(MoneyType.FRB)) {
                        getLog().debug("tryChangeBetLevel FRB mode, change  not allowed");
                        return false;
                    }
                    if (!playGameState.allowChangeBetLevel(seatByAccountId)) {
                        getLog().debug("tryChangeBetLevel round is in freeze mode, change  not allowed");
                        return false;
                    } else {
                        int oldBetLevel = seatByAccountId.getBetLevel();
                        seatByAccountId.setBetLevel(betLevel);
                        getLog().debug("tryChangeBetLevel old betLevel: {}, new betLevel: {}", oldBetLevel, betLevel);
                        return true;
                    }
                } else {
                    getLog().warn("tryChangeBetLevel: cannot obtain lock");
                }
            } catch (Exception exc) {
                getLog().warn("tryChangeBetLevel:  error", exc);
            } finally {
                if (isLocked) {
                    lockShots.unlock();
                }
            }
        } else {
            getLog().debug("tryChangeBetLevel wrong game state: {}", gameState);
        }
        return false;

    }

    public void setBossNumberShots(int bossNumberShots) {
        this.bossNumberShots = bossNumberShots;
    }

    public int getBossNumberShots() {
        return bossNumberShots;
    }
}
