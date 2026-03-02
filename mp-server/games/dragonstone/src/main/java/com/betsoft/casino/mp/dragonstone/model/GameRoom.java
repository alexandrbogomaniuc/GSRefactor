package com.betsoft.casino.mp.dragonstone.model;

import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.common.AbstractPlayGameState;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.dragonstone.model.math.EnemyRange;
import com.betsoft.casino.mp.dragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.dragonstone.model.math.MathData;
import com.betsoft.casino.mp.dragonstone.model.math.slot.MiniSlot;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.abs.casino.common.exception.CommonException;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.betsoft.casino.mp.dragonstone.model.math.EnemyRange.BASE_ENEMIES;
import static com.betsoft.casino.mp.dragonstone.model.math.EnemyType.CERBERUS;
import static com.betsoft.casino.mp.dragonstone.model.math.EnemyType.OGRE;

/**
 * User: flsh
 * Date: 07.02.19.
 */
public class GameRoom extends AbstractActionGameRoom<EnemyGame, GameMap, Seat, GameRoomSnapshot, Enemy, EnemyType, ISingleNodeRoomInfo, IActionRoomPlayerInfo> {
    private final List<ITransportEnemy> possibleEnemies;
    public transient int bossNumberShots;

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
        return GameType.DRAGONSTONE;
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

    public void sendNewEnemyMessage(Enemy enemy) {
        sendChanges(getTOFactoryService().createNewEnemy(getCurrentTime(), convert(enemy, true)));
    }

    @Override
    public IRoomEnemy convert(Enemy enemy, boolean fillTrajectory) {
        EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
        Trajectory trajectory = EnemyType.DRAGON.equals(enemyType)
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
                enemy.getSwarmType()
        );
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
        try {
            getLog().debug("compensateSpecialWeapons starting, getAccountId: {}", seat.getAccountId());
            StringBuilder vbaData = new StringBuilder();
            int totalCountShots = 0;
            Money totalCompensation = Money.ZERO;
            Money stake = getRoomInfo().getStake();

            List<IWeaponSurplus> weaponSurpluses = seat.getWeaponSurplus();
            getLog().debug("compensateSpecialWeapons, account: {}, weaponSurpluses before: {}",
                    seat.getAccountId(), weaponSurpluses);

            for (Map.Entry<SpecialWeaponType, Weapon> weaponTypeWeaponEntry : seat.getWeapons().entrySet()) {
                Weapon weapon = weaponTypeWeaponEntry.getValue();
                SpecialWeaponType key = weaponTypeWeaponEntry.getKey();
                int weaponId = weapon.getType().getId();
                Double rtpForWeapon = MathData.getFullRtpForWeapon(getGame().getConfig(seat), weaponId);

                int shots = weapon.getShots();
                Money newCompensation = Money.ZERO;

                double averageTargets = MathData.getAverageDamageForWeapon(getGame().getConfig(seat), weaponId);
                double multiplier = new BigDecimal(shots, MathContext.DECIMAL32)
                        .multiply(new BigDecimal(rtpForWeapon, MathContext.DECIMAL32))
                        .multiply(new BigDecimal(averageTargets, MathContext.DECIMAL32))
                        .multiply(new BigDecimal(seat.getBetLevel(), MathContext.DECIMAL32))
                        .doubleValue();
                newCompensation = newCompensation.add(stake.getWithMultiplier(multiplier));

                boolean weaponWasFound = false;
                if (weaponSurpluses.size() > 0) {
                    for (IWeaponSurplus weaponSurplus : weaponSurpluses) {
                        if (weaponSurplus.getId() == key.getId()) {
                            weaponWasFound = true;
                            int shotsOld = weaponSurplus.getShots();
                            long winBonusNew = weaponSurplus.getWinBonus();
                            int newShots = shotsOld + weapon.getShots();
                            weaponSurplus.setShots(newShots);
                            newCompensation = Money.fromCents(winBonusNew + newCompensation.toCents());
                            weaponSurplus.setWinBonus(newCompensation.toCents());
                        }
                    }
                }
                if (!weaponWasFound && newCompensation.greaterThan(Money.ZERO)) {
                    weaponSurpluses.add(getTOFactoryService().createWeaponSurplus(key.getId(), weapon.getShots(),
                            newCompensation.toCents()));
                }
                if (newCompensation.greaterThan(Money.ZERO)) {
                    vbaData.append(key.getTitle()).append(",")
                            .append(weapon.getShots())
                            .append(",")
                            .append(newCompensation);
                    vbaData.append("&");
                    totalCompensation = totalCompensation.add(newCompensation);
                }
            }

            if (totalCompensation.greaterThan(Money.ZERO)) {
                seat.setWeaponSurplus((ArrayList<IWeaponSurplus>) weaponSurpluses);
                seat.incrementRoundWin(totalCompensation);
                seat.setCompensateSpecialWeapons(new Money(totalCompensation.getValue()));
                IPlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
                currentPlayerRoundInfo.addWeaponSurplusMoney(totalCompensation);
                currentPlayerRoundInfo.addTotalPayouts(totalCompensation);
                currentPlayerRoundInfo.addWeaponSurplusVBA(vbaData.toString());
            }
            getLog().debug("compensateSpecialWeapons end, totalCompensation: {}," +
                            " totalCountShots: {} , getAccountId: {}, weaponSurpluses: {}",
                    totalCompensation, totalCountShots, seat.getAccountId(), weaponSurpluses);

        } finally {
            seat.resetWeapons();
        }
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
    public IFullGameInfo getFullGameInfo(IGetFullGameInfo request, IGameSocketClient client) {
        IFullGameInfo gameInfo = (IFullGameInfo) super.getFullGameInfo(request, client);
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
        return MiniSlot.getReels();
    }
}
