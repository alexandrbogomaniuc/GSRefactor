package com.betsoft.casino.mp.revengeofra.model;

import com.betsoft.casino.mp.common.AbstractActionGameRoom;
import com.betsoft.casino.mp.common.MinePoint;
import com.betsoft.casino.mp.common.Weapon;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.revengeofra.model.math.EnemyRange;
import com.betsoft.casino.mp.revengeofra.model.math.EnemyType;
import com.betsoft.casino.mp.revengeofra.model.math.MathData;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.dgphoenix.casino.common.exception.CommonException;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

import static com.betsoft.casino.mp.model.SpecialWeaponType.Landmines;

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
        return GameType.REVENGE_OF_RA;
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
        return EnemyRange.BaseEnemies.getEnemies();
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
        List<MinePoint> seatMines = seat.getSeatMines();
        Map<String, Boolean> mineStates = seat.getMineStates();

        getLog().debug("aid: {}, ammoAmountOld: {}", seat.getAccountId(), seat.getAmmoAmount());
        int minesCount = seatMines.size();

        if (minesCount > 0) {
            getLog().debug("add {} mines  to player: {}, w: {}, seatMines: {}",
                    minesCount,
                    seat.getAccountId(),
                    seat.getWeapons(),
                    seatMines);
            int cntBonusMines = 0;
            int cntPaidMines = 0;
            int id = (int) seat.getId();
            for (MinePoint seatMine : seatMines) {
                String mineId = seatMine.getMineId(id);
                Boolean isPaid = mineStates.get(mineId);
                if (isPaid != null) {
                    if (isPaid)
                        cntPaidMines++;
                    else
                        cntBonusMines++;
                } else {
                    getLog().debug("wrong state for mineId {}, seat.getId(): {}, seatMines: {}, mineStates: {} ",
                            mineId, seat.getId(), seatMines, mineStates);
                }
            }

            int specialWeaponId = Landmines.getId();
            getLog().debug("aid: {}, saveMines ammoAmountOld: {}", seat.getAccountId(), seat.getAmmoAmount());

            if (cntBonusMines > 0) {
                if (getRoomInfo().isBonusSession()){
                    getLog().debug("aid: {}, saveMines isBonusSession mines bonus mines" +
                                    " will be cleared: {}, minesCount: {}, cntBonusMines: {} ",
                            seat.getAccountId(), seat.getAmmoAmount(), minesCount, cntBonusMines);
                    seat.getSeatMines().clear();
                    seat.resetWeapons();
                }else {
                    seat.addWeapon(specialWeaponId, cntBonusMines);
                    getLog().debug("aid: {} saveMines, increment ammoAmount from cntBonusMines: {}", seat.getAccountId(), cntBonusMines);
                }
            }
            Money compensation = Money.ZERO;
            int returnedAmmoAmount = 0;

            if (cntPaidMines > 0) {
                List<IWeaponSurplus> weaponSurpluses = seat.getWeaponsReturned();
                returnedAmmoAmount = MathData.getPaidWeaponCost(Landmines.getId()) * cntPaidMines * seat.getBetLevel();
                getLog().debug("saveMines cntPaidMines: {}, seat.getBetLevel(): {}, returnedAmmoAmount: {}" ,
                        cntPaidMines, seat.getBetLevel(), returnedAmmoAmount);
                compensation = seat.getStake().getWithMultiplier(returnedAmmoAmount);

                getLog().debug("add returnedAmmoAmount: {} to seat ammo amount: {}",
                        returnedAmmoAmount, seat.getAmmoAmount());

                seat.incrementAmmoAmount(returnedAmmoAmount);
                seat.setTotalReturnedSpecialWeapons(new Money(compensation.getValue()));

                boolean weaponWasFound = false;
                if (weaponSurpluses.size() > 0) {
                    for (IWeaponSurplus weaponSurplus : weaponSurpluses) {
                        if (weaponSurplus.getId() == Landmines.getId()) {
                            weaponWasFound = true;
                            int shotsOld = weaponSurplus.getShots();
                            long winBonusNew = weaponSurplus.getWinBonus();
                            int newShots = shotsOld + cntPaidMines;
                            weaponSurplus.setShots(newShots);
                            Money newCompensation = Money.fromCents(winBonusNew + compensation.toCents());
                            weaponSurplus.setWinBonus(newCompensation.toCents());
                        }
                    }
                }

                getLog().debug("weaponSurplus after save mines {}", seat.getWeaponSurplus());
                if (!weaponWasFound) {
                    weaponSurpluses.add(getTOFactoryService().createWeaponSurplus(Landmines.getId(), cntPaidMines,
                            compensation.toCents()));
                }
            }

            getLog().debug("aid: {}, cntBonusMines: {}, cntPaidMines: {}, compensation: {}, paidReturnedAmmoAmount: {}",
                    seat.getAccountId(), cntBonusMines, cntPaidMines, compensation, returnedAmmoAmount);
            getLog().debug("aid: {}, saveMines ammoAmountNew: {}", seat.getAccountId(), seat.getAmmoAmount());
            seat.getSeatMines().clear();
        }

        if(getRoomInfo().isBonusSession()){
            getLog().debug("aid: {}, saveMines isBonusSession: weapons will be reset", seat.getWeapons());
            seat.resetWeapons();
        }
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
                Double rtpForWeapon = MathData.getRtpCompensateSpecialWeapons(weaponId, false) / 100;

                int shots = weapon.getShots();
                Money newCompensation = Money.ZERO;

                double multiplier = new BigDecimal(shots, MathContext.DECIMAL32)
                        .multiply(new BigDecimal(rtpForWeapon, MathContext.DECIMAL32))
                        .multiply(new BigDecimal(MathData.getAverageDamageForWeapon(weaponId), MathContext.DECIMAL32))
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
}
