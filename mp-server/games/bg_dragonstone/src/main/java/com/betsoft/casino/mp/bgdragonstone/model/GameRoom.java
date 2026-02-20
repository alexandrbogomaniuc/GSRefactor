package com.betsoft.casino.mp.bgdragonstone.model;

import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyRange;
import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.common.AbstractBattlegroundGameRoom;
import com.betsoft.casino.mp.common.AbstractPlayGameState;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.dgphoenix.casino.common.exception.CommonException;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.betsoft.casino.mp.bgdragonstone.model.math.EnemyRange.BASE_ENEMIES;
import static com.betsoft.casino.mp.bgdragonstone.model.math.EnemyRange.EMPTY_ARMORS_RANGE;
import static com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType.CERBERUS;
import static com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType.OGRE;


public class GameRoom extends AbstractBattlegroundGameRoom<EnemyGame, GameMap, Seat, GameRoomSnapshot, Enemy, EnemyType, ISingleNodeRoomInfo,
        IBattlegroundRoomPlayerInfo> {
    private final List<ITransportEnemy> possibleEnemies;
    public transient int bossNumberShots;
    public static final int TIME_AFTER_START_NO_SHOTS = 4;

    public GameRoom(ApplicationContext context, Logger logger, ISingleNodeRoomInfo roomInfo, GameMap map,
                    IPlayerStatsService playerStatsService, IWeaponService weaponService,
                    IExecutorService remoteExecutorService, IPlayerQuestsService playerQuestsService,
                    IPlayerProfileService playerProfileService,
                    IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService,
                    IActiveCashBonusSessionService activeCashBonusSessionService,
                    ITournamentService tournamentService,
                    IGameConfigProvider gameConfigProvider, ISpawnConfigProvider spawnConfigProvider) {
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

    @Override
    public GameType getGameType() {
        return GameType.BG_DRAGONSTONE;
    }

    @Override
    protected IGameState getWaitingPlayersGameState() {
        boolean isPrivate = roomInfo.isPrivateRoom();
        return isPrivate ? new PrivateBTGWaitingGameState(this) : new WaitingPlayersGameState(this);
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
    public Seat createSeat(IBattlegroundRoomPlayerInfo playerInfo, IGameSocketClient socketClient, double currentRate) {
        Seat seat = new Seat(playerInfo, socketClient, currentRate);
        seat.setBetLevel(3);
        seat.setOwner(socketClient.isOwner());
        return seat;
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

        Trajectory trajectory = EMPTY_ARMORS_RANGE.contains(enemyType) || EnemyType.DRAGON.equals(enemyType)
                ? convertFullTrajectory(enemy.getTrajectory())
                : convertTrajectory(enemy.getTrajectory(), System.currentTimeMillis());

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
                enemy.getSwarmType());
    }

    private double getHP(Enemy enemy) {
        return enemy.getLives() + 1.;
    }

    private double getFullHP(Enemy enemy) {
        if (CERBERUS.equals(enemy.getEnemyType())) {
            return 3;
        } else if (OGRE.equals(enemy.getEnemyType())) {
            return 2;
        } else {
            return 1;
        }
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
        response.setFragments(getMap().getDragonStoneFragments());
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
    public void compensateSpecialWeaponsWithLock(Seat seat) {
        seat.resetWeapons();
    }

    @Override
    public void compensateSpecialWeapons(Seat seat) {
        seat.resetWeapons();
    }

    @Override
    protected List<ITransportSeat> getTransportSeats() {
        List<ITransportSeat> result = new ArrayList<>();
        for (Seat seat : getAllSeats()) {
            if (seat != null) {
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
        }
        return result;
    }

    @Override
    public void processCloseRoom(IGameSocketClient client, ICloseRoom request) throws CommonException {
        super.processCloseRoom(client, request);
        resetBossOptions();
    }

    @Override
    public void processCloseRoom(long accountId) throws CommonException {
        super.processCloseRoom(accountId);
        resetBossOptions();
    }

    public void resetBossOptions() {
        if (allSeatsAreFree()) {
            getMap().resetDragonStoneFragments();
            getMap().setBossHP(0);
        }
    }

    @Override
    public int getDefaultBetLevel() {
        return 3;
    }

    @Override
    public IFullGameInfo getFullGameInfo(IGetFullGameInfo request, IGameSocketClient client) {
        IFullGameInfo gameInfo = super.getFullGameInfo(request, client);
        gameInfo.setFragments(getMap().getDragonStoneFragments());
        gameInfo.setBossNumberShots(bossNumberShots);
        gameInfo.setEndTime(gameState.getEndRoundTime());
        IGameState gameState = getGameState();
        if (gameState instanceof AbstractPlayGameState) {
            gameInfo.setNeedWaitingWhenEnemiesLeave(((PlayGameState) gameState).isNeedWaitingWhenEnemiesLeave());
        }
        return gameInfo;
    }


    @Override
    public int[][] getConfigReels() {
        IGameConfig config = getGame().getGameConfig(getId());
        if (config == null) {
            return null;
        }
        return ((GameConfig) config).getSlot().getReels();
    }

    @Override
    public int getRoundDuration() {
        return (getRoomInfo() != null ? getRoomInfo().getRoundDuration() : super.getRoundDuration()) + TIME_AFTER_START_NO_SHOTS;
    }

    @Override
    public boolean bulletPlaceAllowed(int seatId) {

        if (System.currentTimeMillis() - gameState.getStartRoundTime() < TIME_AFTER_START_NO_SHOTS * 1000) {
            getLog().debug("bulletPlaceAllowed: seatId={}, not allowed, start round", seatId);
            return false;
        }

        if (!gameState.isBossRound() && System.currentTimeMillis() > gameState.getEndRoundTime()) {
            getLog().debug("bulletPlaceAllowed: seatId={}, not allowed, end round",seatId);
            return false;
        }

        Seat seat = getSeat(seatId);
        if (seat != null && seat.getCurrentWeaponId() == SpecialWeaponType.LevelUp.getId()) {

            long numberOfBulletsOnMap = seat.getBulletsOnMap().stream()
                    .filter(
                            seatBullet -> seatBullet.getWeaponId() == SpecialWeaponType.LevelUp.getId())
                    .count();

            int shots = seat.getWeapons().get(SpecialWeaponType.LevelUp).getShots();

            boolean allowed = numberOfBulletsOnMap < shots;

            getLog().debug("bulletPlaceAllowed: seatId={}, AID={} for LevelUP, allowed:{}, shots={}, numberOfBulletsOnMap={}, remainingShots={}",
                    seatId, seat.getAccountId(), allowed, shots, numberOfBulletsOnMap, shots - numberOfBulletsOnMap);

            return allowed;

        } else {
            return super.bulletPlaceAllowed(seatId);
        }
    }
}
