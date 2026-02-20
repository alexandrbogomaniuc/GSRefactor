package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.exceptions.BuyInFailedException;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.betsoft.casino.mp.model.gameconfig.IGameConfig;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.privateroom.Status;
import com.betsoft.casino.mp.model.room.IRoom;
import com.betsoft.casino.mp.model.room.IRoomInfo;
import com.betsoft.casino.mp.model.room.ISingleNodeRoomInfo;
import com.betsoft.casino.mp.payment.IPendingOperation;
import com.betsoft.casino.mp.payment.PendingOperationType;
import com.betsoft.casino.mp.service.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.mp.web.IGameSocketClient;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.hazelcast.core.IExecutorService;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.betsoft.casino.mp.utils.ErrorCodes.WRONG_WEAPON;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

/**
 * User: flsh
 * Date: 18.01.2022.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractActionGameRoom<GAME extends IGame, MAP extends IMap, SEAT extends IActionGameSeat,
        SNAPSHOT extends IGameRoomSnapshot, ENEMY extends IEnemy, ENEMY_TYPE extends IEnemyType, ROOM_INFO extends ISingleNodeRoomInfo,
        RPI extends IActionRoomPlayerInfo>
        extends AbstractSingleNodeGameRoom<GAME, MAP, SEAT, SNAPSHOT, ENEMY, ENEMY_TYPE, ROOM_INFO, RPI> {

    public AbstractActionGameRoom(ApplicationContext context, Logger logger, SEAT[] seats, ROOM_INFO roomInfo, GAME game, MAP map, IPlayerStatsService playerStatsService, IPlayerQuestsService playerQuestsService, IWeaponService weaponService, IExecutorService remoteExecutorService, IPlayerProfileService playerProfileService, IGameConfigService gameConfigService, IActiveFrbSessionService activeFrbSessionService, IActiveCashBonusSessionService activeCashBonusSessionService, ITournamentService tournamentService) {
        super(context, logger, seats, roomInfo, game, map, playerStatsService, playerQuestsService, weaponService, remoteExecutorService, playerProfileService, gameConfigService, activeFrbSessionService, activeCashBonusSessionService, tournamentService);
    }

    protected abstract List<IMinePlace> getAllMinePlaces();

    protected abstract List<ENEMY_TYPE> getBaseEnemyTypes();

    public abstract void placeMineToMap(SEAT seat, IMineCoordinates mineCoordinates) throws CommonException;

    public abstract void addFreeShotsToQueue(SEAT seat, IAddFreeShotsToQueue addFreeShotsToQueue) throws CommonException;

    public abstract IWeaponLootBox generateWeaponLootBox(SEAT seat, int rid, int size, int usedAmmoAmount, Money usedMoney);

    public abstract void compensateSpecialWeapons(SEAT seat);

    public abstract void saveMines(SEAT seat);

    public Money getWeaponLootBoxPrice(SEAT seat, int size) {
        return seat.getStake().multiply(getWeaponLootBoxPrices().get(size));
    }

    public List<Money> getWeaponLootBoxPrices(SEAT seat) {
        List<Integer> weaponLootBoxPrices = getWeaponLootBoxPrices();
        List<Money> prices = new ArrayList<>(weaponLootBoxPrices.size());
        weaponLootBoxPrices.forEach(price -> prices.add(seat.getStake().multiply(price)));
        return prices;
    }

    protected IWeaponLootBox addWeaponLootBoxToSeat(SEAT seat, Weapon weapon, int rid, int size, int usedAmmoAmount) {
        seat.addWeapon(weapon);
        getCurrentPlayerRoundInfo(seat).addTotalBetsSpecialWeapons(getWeaponLootBoxPrice(seat, size));
        return getTOFactoryService().createWeaponLootBox(System.currentTimeMillis(), rid, weapon.getType().getId(),
                weapon.getShots(), getBalance(seat), seat.getRoundWin().toFloatCents(),
                usedAmmoAmount);
    }

    public boolean bulletPlaceAllowed(int seatId) {
        return true;
    }

    @Override
    protected void finishSitIn(SEAT seat) throws CommonException {
        super.finishSitIn(seat);
        loadWeapons(seat);
    }

    @Override
    protected int getUnplayedFreeShots(SEAT seat) {
        return seat.getFreeShots().getUnplayedFreeShots();
    }
    protected void loadWeapons(SEAT seat) {
        Long specialModeId = seat.getSpecialModeId();
        Map<Integer, Integer> weapons;
        long gameId = roomInfo.getGameType().getGameId();
        if (specialModeId == null) {
            weapons = weaponService.loadWeapons(seat.getBankId(), seat.getAccountId(),
                    roomInfo.getMoneyType().ordinal(), seat.getStake(), gameId);
        } else {
            weapons = weaponService.loadSpecialModeWeapons(specialModeId, seat.getAccountId(),
                    roomInfo.getMoneyType().ordinal(), seat.getStake(), gameId);
        }
        if (weapons != null) {
            weapons.forEach((wpId, shots) -> {
                if (SpecialWeaponType.values()[wpId].getAvailableGameIds().contains((int) gameId)) {
                    seat.addWeapon(wpId, shots);
                }
            });
        }
    }

    @Override
    public boolean tryChangeBetLevel(long accountId, int betLevel) {
        SEAT seatByAccountId = getSeatByAccountId(accountId);
        List seatMines = seatByAccountId.getSeatMines();
        IGameState gameState = getGameState();
        if (gameState.getRoomState().equals(RoomState.PLAY)) {
            AbstractPlayGameState playGameState = (AbstractPlayGameState) gameState;
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
                    if (seatByAccountId.isAnyWeaponShotAvailable()) {
                        getLog().debug("tryChangeBetLevel player has free weapons, change  not allowed");
                        return false;
                    } else if (seatMines != null && !seatMines.isEmpty()) {
                        getLog().debug("tryChangeBetLevel player has mines on map, change  not allowed, seatMines: {}", seatMines);
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

    @Override
    protected int getAmmoAmount(SEAT seat) {
        return seat.getAmmoAmount();
    }

    @Override
    protected Money getReturnedBet(SEAT seat) {
        return seat.retrieveRemainingAmmo();
    }

    @Override
    protected boolean isNoActivityInRound(SEAT seat, IPlayerBet playerBet) {
        return seat.getAmmoAmountTotalInRound() == 0
                && playerBet.getBet() == 0 && playerBet.getWin() == 0 && seat.getRoundWin().equals(Money.ZERO)
                && seat.getAmmoAmount() == 0;
    }
    @Override
    protected void compensateUnusedFeatures(SEAT seat) {
        super.compensateUnusedFeatures(seat);
        saveMines(seat);
        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        boolean poorCompensationIsNotEmpty = !seat.getWeaponSurplus().isEmpty();
        boolean isBonusSession = getRoomInfo().getMoneyType().equals(MoneyType.CASHBONUS) ||
                getRoomInfo().getMoneyType().equals(MoneyType.FRB) || getRoomInfo().getMoneyType().equals(MoneyType.TOURNAMENT);

        if ((playerInfo.getBuyInCount() > 0 && playerInfo.getRoundBuyInAmount() > 0)
                || poorCompensationIsNotEmpty) {
            if (!isBonusSession) {
                compensateSpecialWeapons(seat);
            }
        }
    }

    @Override
    protected void sendSitOutMessage(SEAT seat, ISitOut request, int oldSeatNumber,  long nextRoomId, boolean hasNextFrb, boolean frbSitOut) {
        long compensateSpecialWeapons = frbSitOut ? 0 : seat.getCompensateSpecialWeapons().toCents();
        long returnedSpecialWeapons = frbSitOut ? 0 : seat.getTotalReturnedSpecialWeapons().toCents();

        sendChanges(getTOFactoryService().createSitOutResponse(getCurrentTime(), TObject.SERVER_RID, oldSeatNumber,
                        seat.getNickname(), getCurrentTime(), compensateSpecialWeapons,
                        0, returnedSpecialWeapons, nextRoomId, hasNextFrb),
                getTOFactoryService().createSitOutResponse(getCurrentTime(),
                        request != null ? request.getRid() : TObject.SERVER_RID,
                        oldSeatNumber, seat.getNickname(), getCurrentTime(), compensateSpecialWeapons,
                        0, returnedSpecialWeapons, nextRoomId, hasNextFrb), seat.getAccountId(), request
        );
    }

    @Override
    protected void persistCrossRoundSeatStats(SEAT seat) {
        savePlayerWeapons(seat);
    }

    @Override
    protected void clearCrossRoundSeatStats(SEAT seat) {
        seat.resetWeapons();
    }

    /**
     * Persist player weapons to cassandra.
     * @param seat seat of player
     */
    private void savePlayerWeapons(SEAT seat) {
        if (!getRoomInfo().getMoneyType().equals(MoneyType.FREE)) {
            Map<SpecialWeaponType, IWeapon> seatWeapons = seat.getWeapons();
            Map<Integer, Integer> weapons = new HashMap<>();
            seatWeapons.values().forEach(weapon -> weapons.put(weapon.getType().getId(), weapon.getShots()));
            Long specialModeId = seat.getSpecialModeId();
            getLog().info("save weapons {}, specialModeId={}", weapons, specialModeId);
            if (specialModeId == null) {
                weaponService.saveWeapons(seat.getBankId(), seat.getAccountId(), getRoomInfo().getMoneyType().ordinal(),
                        seat.getStake(), weapons, getRoomInfo().getGameType().getGameId());
            } else {
                weaponService.saveSpecialModeWeapons(specialModeId, seat.getAccountId(), getRoomInfo().getMoneyType().ordinal(),
                        seat.getStake(), weapons, getRoomInfo().getGameType().getGameId());
            }
        }
    }
    /**
     * Makes compensation for unused special weapons. Compensation will be converted to win.
     * @param seat seat of player
     */
    public void compensateSpecialWeaponsWithLock(SEAT seat) {
        long accountId = seat.getAccountId();
        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        if (playerInfo == null) {
            playerInfo = playerInfoService.get(accountId);
        }
        if (playerInfo == null) {
            getLog().error("finish: playerInfo not found for seat: {}", seat);
            return;
        }
        boolean poorCompensationIsNotEmpty = !seat.getWeaponSurplus().isEmpty();
        if ((playerInfo.getBuyInCount() > 0 && playerInfo.getRoundBuyInAmount() > 0)
                || poorCompensationIsNotEmpty) {
            playerInfoService.lock(accountId);
            getLog().debug("compensateSpecialWeaponsWithLock lock: {}", seat.getAccountId());
            try {
                compensateSpecialWeapons(seat);
            } catch (Exception e) {
                getLog().error("compensateSpecialWeaponsWithLock: error, seat={}", seat, e);
            } finally {
                playerInfoService.unlock(accountId);
                getLog().debug("compensateSpecialWeaponsWithLock unlock: {}", seat.getAccountId());
            }
        }
    }

    @Override
    protected List<ITransportSeat> getTransportSeats() {
        List<ITransportSeat> result = new ArrayList<>();
        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                IActiveFrbSession frbSession = seat.getPlayerInfo().getActiveFrbSession();
                long roundWin = frbSession != null
                        ? frbSession.getWinSum() + seat.getRoundWin().toCents()
                        : seat.getRoundWin().add(seat.getRebuyFromWin()).toCents();
                result.add(getTOFactoryService().createSeat(getSeatNumber(seat), seat.getNickname(),
                        seat.getJoinDate(), seat.getTotalScore() == null ? 0 : seat.getTotalScore().getAmount(),
                        seat.getCurrentScore() == null ? 0 : seat.getCurrentScore().getAmount(),
                        seat.getAvatar(), seat.getSpecialWeaponId(), seat.getLevel(),
                        getUnplayedFreeShots(seat), seat.getCurrentPlayerRoundInfo().getTotalDamage(), roundWin));
            }
        }
        return result;
    }

    @Override
    public void clearSeatDataFromPreviousRound() {
        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                seat.setEnemiesKilledCount(0);
            }
        }
    }

    /**
     * Creates round result data for sending to clients
     * @param winAmount winAmount
     * @param realWinAmount winAmount
     * @param balance current balance of player
     * @param level level of player for leaderboard (not used)
     * @param prevLevel level of player for leaderboard (not used)
     * @param totalKillsXP XP kill value for leaderboard (not used)
     * @param seat seat of player
     * @param transportSeats transport shot data for all seats.
     * @param roundInfo player round info
     * @return {@code IRoundResult} result of round for player
     */
    @Override
    protected IRoundResult createRoundResult(double winAmount, double realWinAmount, long balance, int level, int prevLevel, long totalKillsXP,
                                             SEAT seat, List<ITransportSeat> transportSeats, IPlayerRoundInfo roundInfo) {
        return getTOFactoryService().createRoundResult(getCurrentTime(), SERVER_RID,
                winAmount,
                seat.getRebuyFromWin().toDoubleCents(),
                balance,
                seat.getCurrentScore().getLongAmount(), seat.getTotalScore().getLongAmount(),
                seat.getHitCount(), seat.getMissCount(), nextMapId, transportSeats,
                seat.getEnemiesKilledCount(), seat.getRoundWinInCredits(), seat.getAmmoAmount(),
                seat.getStake().multiply(seat.getAmmoAmount()).toDoubleCents(),
                seat.getStake().multiply(seat.getAmmoAmountTotalInRound()).toDoubleCents(),
                AchievementHelper.getXP(level), seat.getWeaponSurplus(),
                totalKillsXP, seat.getTotalTreasuresCount(), seat.getTotalTreasuresXPAsLong(),
                getTOFactoryService().createLevelInfo(prevLevel, seat.getPlayerInfo().getPrevXP().getLongAmount(),
                        AchievementHelper.getXP(prevLevel), AchievementHelper.getXP(prevLevel + 1)),
                getTOFactoryService().createLevelInfo(level, seat.getTotalScore().getLongAmount(),
                        AchievementHelper.getXP(level), AchievementHelper.getXP(level + 1)),
                0, seat.getQuestsCompletedCount(), seat.getQuestsPayouts(),
                getRoomInfo().getRoundId(), seat.getWeaponsReturned(), seat.getBulletsFired(),
                realWinAmount, roundInfo.getFreeShotsWon(), roundInfo.getMoneyWheelCompleted(),
                roundInfo.getMoneyWheelPayouts(), roundInfo.getTotalDamage(), null
        );
    }

    /**
     * Reset round seat data
     * @param seat seat of player
     */
    @Override
    protected void resetSeatRoundResult(SEAT seat) {
        if (!seat.getBulletsOnMap().isEmpty()) {
            seat.getBulletsOnMap().clear();
        }
        super.resetSeatRoundResult(seat);
    }

    /**
     * Clears all bullets on map in end of round.
     */
    public void clearAllBullets() {
        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                long accountId = seat.getAccountId();
                playerInfoService.lock(accountId);
                getLog().debug("clearAllBullets lock: {}", accountId);
                try {
                    @SuppressWarnings("unchecked")
                    Set<SeatBullet> bulletsOnMap = seat.getBulletsOnMap();
                    getLog().debug("clearAllBullets bullets {} will be cleared for accountId: {}",
                            bulletsOnMap, seat.getAccountId());
                    bulletsOnMap.clear();
                    sendChanges(getTOFactoryService()
                            .createBulletClearResponse(System.currentTimeMillis(), SERVER_RID, getSeatNumber(seat)));
                } finally {
                    playerInfoService.unlock(accountId);
                    getLog().debug("clearAllBullets unlock: {}", accountId);
                }
            }
        }
    }

    /**
     * Gets number of real seats in room
     * @return number of real seats
     */
    @Override
    public short getRealSeatsCount() {
        short count = 0;
        for (SEAT seat : getAllSeats()) {
            if (seat == null) {
                continue;
            }
            if ((seat.getSocketClient() != null || seat.getAmmoAmount() > 0 || seat.getRoundWin().greaterThan(Money.ZERO)) &&
                    !seat.isBot() &&
                    !seat.isSitOutStarted()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Save mines to persister.
     * @param seat seat of player
     */
    public void saveMinesWithLock(SEAT seat) {
        long accountId = seat.getAccountId();
        playerInfoService.lock(accountId);
        getLog().debug("saveMinesWithLock lock: {}", seat.getAccountId());
        try {
            saveMines(seat);
        } catch (Exception e) {
            getLog().error("saveMinesWithLock: error, seat={}", seat, e);
        } finally {
            playerInfoService.unlock(accountId);
            getLog().debug("saveMinesWithLock unlock: {}", seat.getAccountId());
        }
    }

    /**
     * Prepares full game info for client about room.
     * @param request client request
     * @param client client game socket
     * @return {@code IFullGameInfo} detail information about room.
     */
    @Override
    public IFullGameInfo getFullGameInfo(IGetFullGameInfo request, IGameSocketClient client) {
        SEAT alreadySeat = null;
        Set<SeatBullet> allBullets = new HashSet<>();
        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                allBullets.addAll(seat.getBulletsOnMap());
                if (seat.getAccountId() == client.getAccountId()) {
                    alreadySeat = seat;
                }
            }
        }
        return getTOFactoryService().createFullGameInfo(getCurrentTime(), request.getRid(), gameState.getCurrentMapId(),
                gameState.getSubround().name(), gameState.getStartTime(), gameState.getRoomState(),
                getLiveRoomEnemies(), getTransportSeats(), getAllMinePlaces(), gameState.getFreezeTimeRemaining(),
                isBossImmortal(client), getRoomInfo().getRoundId(), getSeatGems(alreadySeat),
                alreadySeat == null ? getDefaultBetLevel() : alreadySeat.getBetLevel(), map.getAdditionalEnemyModes(),
                allBullets, 0L, getReels(), 0, null
        );
    }

    protected Map<Integer, Integer> getSeatGems(SEAT seat) {
        return new HashMap<>();
    }

    /**
     * Current room info. Sent to the client in response to an OpenRoom request.
     * @param requestId requestId of client
     * @param client client game socket
     * @param playerCurrency player currency
     * @return {@code IGetRoomInfoResponse} Current room info
     * @throws CommonException if any unexpected error occur
     */
    @Override
    public IGetRoomInfoResponse getRoomInfoResponse(int requestId, IGameSocketClient client, String playerCurrency)
            throws CommonException {
        checkAndStartRoom();
        long accountId = client.getAccountId();
        SEAT alreadySeat = null;
        Set<SeatBullet> allBullets = new HashSet<>();
        for (SEAT seat : getAllSeats()) {
            if (seat != null) {
                allBullets.addAll(seat.getBulletsOnMap());
                if (seat.getAccountId() == client.getAccountId()) {
                    alreadySeat = seat;
                }
            }
        }
        float minBuyIn = getRoomInfo().getMinBuyIn();
        double stake = getRoomInfo().getStake().toCents();
        IRoomPlayerInfo playerInfo = alreadySeat == null ? null : alreadySeat.getPlayerInfo();

        long activeFrbWin = 0;
        IActiveCashBonusSession activeCashBonusSession = null;
        ITournamentSession activeTournamentSession = null;
        boolean isFRBSession = getRoomInfo().getMoneyType() == MoneyType.FRB;

        if (isFRBSession) {
            if (playerInfo != null) {
                IActiveFrbSession activeFrbSession = playerInfo.getActiveFrbSession();
                if (activeFrbSession != null) {
                    activeFrbWin = activeFrbSession.getWinSum();
                }
            } else {
                ILobbySession lobbySession = lobbySessionService == null ? null :
                        lobbySessionService.get(client.getSessionId());
                if (lobbySession != null) {
                    activeFrbWin = lobbySession.getActiveFrbSession().getWinSum();
                } else {
                    List<IActiveFrbSession> frbSessions = activeFrbSessionService.getByAccountId(accountId);
                    if (frbSessions.size() > 0) {
                        if (frbSessions.size() > 1) {
                            getLog().error("Found many frbSessions, this error, please fix. frbSessions={}", frbSessions);
                        } else {
                            IActiveFrbSession activeFrbSession = frbSessions.get(0);
                            activeFrbWin = activeFrbSession.getWinSum();
                        }
                    }
                }
            }
        } else if (getRoomInfo().getMoneyType() == MoneyType.CASHBONUS) {
            if (playerInfo != null) {
                activeCashBonusSession = playerInfo.getActiveCashBonusSession();
            } else {
                ILobbySession lobbySession = lobbySessionService == null ? null :
                        lobbySessionService.get(client.getSessionId());
                activeCashBonusSession = lobbySession == null ? null : lobbySession.getActiveCashBonusSession();
                if (activeCashBonusSession == null) {
                    getLog().error("getRoomInfoResponse: activeCashBonusSession is null, lobbySession={}", lobbySession);
                }
            }
        } else if (getRoomInfo().getMoneyType() == MoneyType.TOURNAMENT) {
            if (playerInfo != null) {
                activeTournamentSession = playerInfo.getTournamentSession();
            } else {
                ILobbySession lobbySession = lobbySessionService == null ? null :
                        lobbySessionService.get(client.getSessionId());
                activeTournamentSession = lobbySession == null ? null : lobbySession.getTournamentSession();
                if (activeTournamentSession == null) {
                    getLog().error("getRoomInfoResponse: activeTournamentSession is null, lobbySession={}",
                            lobbySession);
                }
            }
        }
        getLog().debug("getRoomInfoResponse: room.stake={}, player currency={}, seatNumber={}",
                getRoomInfo().getStake().toCents(), playerCurrency,
                (alreadySeat == null ? -1 : getSeatNumber(alreadySeat)));

        Map<Integer, Integer> seatGems = alreadySeat == null ? new HashMap<>() : getSeatGems(alreadySeat);

        long alreadySitInWin = alreadySeat == null ? 0 : (long) alreadySeat.getRoundWin().toDoubleCents();
        if (isFRBSession) {
            getLog().debug("activeFrbWin: {}, alreadySitInWin: {}", activeFrbWin, alreadySitInWin);
            alreadySitInWin += activeFrbWin;
        }

        int betLevel = 1;
        if (alreadySeat != null) {
            betLevel = alreadySeat.getBetLevel();
        }

        return getTOFactoryService().createGetRoomInfoResponse(getCurrentTime(), getId(), requestId,
                getName(), getMaxSeats(), minBuyIn, stake, stake,
                getState(), getTransportSeats(), getTimeToNextState(), roomInfo.getWidth(), roomInfo.getHeight(),
                getTransportEnemies(), getLiveRoomEnemies(),
                alreadySeat == null ? -1 : getSeatNumber(alreadySeat),
                alreadySeat == null ? 0 : alreadySeat.getAmmoAmount(),
                alreadySeat == null ? 0 : getBalance(alreadySeat),
                alreadySitInWin,
                gameState.getCurrentMapId(),
                gameState.getSubround().name(),
                GameType.getAmmoValues(getRoomInfo().getMoneyType(), getRoomInfo().getStake().toFloatCents()),
                getAllMinePlaces(), gameState.getFreezeTimeRemaining(), false, getRoomInfo().getRoundId(),
                seatGems, activeCashBonusSession, activeTournamentSession,
                betLevel,
                getMap().getAdditionalEnemyModes(), allBullets, null, getReels(), null);
    }

    @Override
    protected long getTotalKillsXP(SEAT seat) {
        return seat.getCurrentScore().getLongAmount() - seat.getTotalTreasuresXPAsLong();
    }

    @Override
    protected void updateStatOnEndRound(SEAT seat, IPlayerRoundInfo roundInfo) {
        roundInfo.updateStatOnEndRound(seat.getAmmoAmountTotalInRound(), seat.getCurrentScore(),
                seat.getAmmoAmount());
    }

    public void addEnemyFromTeststand(int typeId, int skinId, Trajectory trajectory) {
        getLog().debug("add Enemy from teststand, typeId: {}, skinId: {}", typeId, skinId);
        if (gameState.getRoomState().equals(RoomState.PLAY)) {
            ((AbstractActionPlayGameState) gameState).spawnEnemyFromTeststand(typeId, skinId, trajectory, -1);
        }
    }

    protected boolean isAllSeatsWithoutShoot(Set<SEAT> seatsForProcess) {
        for (SEAT seat : seatsForProcess) {
            if (getCurrentPlayerRoundInfo(seat).getShotsCount() > 0) {
                return false;
            }
        }
        return true;
    }

    protected  boolean isAllSeatsWithoutPayout(Set<SEAT> seatsForProcess) {
        for (SEAT seat : seatsForProcess) {
            if (getCurrentPlayerRoundInfo(seat).getTotalPayouts().toCents() > 0) {
                return false;
            }
        }
        return true;
    }

    protected IActionGamePlayerRoundInfo getCurrentPlayerRoundInfo(SEAT seat) {
        return (IActionGamePlayerRoundInfo) seat.getCurrentPlayerRoundInfo();
    }

    /**
     * Process shot from Shot handler with lock
     * @param seat seat of player
     * @param shot shot message from client
     * @param isInternalShot true if internal shot(mines), otherwise false.
     * @throws CommonException  if any unexpected error occur
     */
    public void processShot(SEAT seat, IShot shot, boolean isInternalShot) throws CommonException {
        lock();
        try {
            innerProcessShot(seat, shot, isInternalShot);
        } finally {
            unlock();
        }
    }

    /**
     * Internal processing of shot from Shot handler without lock
     * @param seat seat of player
     * @param shot shot message from client
     * @param isInternalShot true if internal shot(mines), otherwise false.
     * @throws CommonException  if any unexpected error occur
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void innerProcessShot(SEAT seat, IShot shot, boolean isInternalShot) throws CommonException {
        assertRoomStarted();
        long now = System.currentTimeMillis();
        MoneyType moneyType = getRoomInfo().getMoneyType();
        IGameConfig config = getGame().getGameConfig(getId());
        Integer weaponPriceFromConfig = config != null ? config.getWeaponPrices().get(shot.getWeaponId()) : null;

        if (getGameType().isCheckWeaponPrices() && config != null
                && (weaponPriceFromConfig == null || weaponPriceFromConfig != shot.getWeaponPrice())) {
            seat.sendMessage(getTOFactoryService().createError(WRONG_WEAPON,
                    "wrong weapon price", System.currentTimeMillis(),
                    shot.getRid()), shot);
            return;
        }

        if (moneyType.equals(MoneyType.FRB)) {
            if (seat.getBetLevel() > 1 || shot.isPaidSpecialShot()) {
                seat.sendMessage(getTOFactoryService().createError(WRONG_WEAPON,
                        "paid shots is not allowed in bonus modes", System.currentTimeMillis(),
                        shot.getRid()), shot);
                return;
            }
        }

        if (!seat.getStake().equals(roomInfo.getStake())) {
            getLog().error("handle: Cannot make shot, found bad stake seat.getStake()={}, roomInfo.getStake(): {}",
                    seat.getStake(), roomInfo.getStake());
            seat.sendMessage(getTOFactoryService().createError(WRONG_WEAPON,
                    "wrong player stake", System.currentTimeMillis(),
                    shot.getRid()), shot);
            return;
        }

        Money totalBetsBefore = seat.getCurrentPlayerRoundInfo().getTotalBets();

        IGameState gameState = getGameState();
        if (!(gameState instanceof AbstractActionPlayGameState)) {
            gameState.throwUnsupportedOperationException("Shot");
        }
        @SuppressWarnings("ConstantConditions")
        AbstractActionPlayGameState playGameState = (AbstractActionPlayGameState) gameState;
        if (isInternalShot) {
            playerInfoService.lock(seat.getAccountId());
            getLog().debug("processShot lock: {}", seat.getAccountId());
            try {
                playGameState.processShot(seat, shot, true);
            } finally {
                playerInfoService.unlock(seat.getAccountId());
                getLog().debug("processShot unlock: {}", seat.getAccountId());
            }
        } else {
            playGameState.processShot(seat, shot, false);
        }

        StatisticsManager.getInstance().updateRequestStatistics("GameRoom: processShot",
                System.currentTimeMillis() - now, seat.getPlayerInfo().getSessionId() + ":" + shot.getRid());

        IPlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
        currentPlayerRoundInfo.setRoomRoundId(getRoomInfo().getRoundId());
        Money totalBets = currentPlayerRoundInfo.getTotalBets();
        Money totalPayouts = currentPlayerRoundInfo.getTotalPayouts();
        getLog().debug("getPossibleBalanceAmount: {} ", seat.getPossibleBalanceAmount());
        getLog().debug("Shot :: AID: {}, Round Win: {},  getRebuyFromWin: {}, VBA totalBetsBefore: {}, " +
                        "VBA totalBets: {}, VBA totalPayouts: {}, getLastWin: {}, ammo={}", seat.getAccountId(),
                seat.getRoundWin(), seat.getRebuyFromWin(), totalBetsBefore, totalBets, totalPayouts, seat.getLastWin(),
                seat.getAmmoAmount());
        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();

        IActiveFrbSession activeFrbSession = playerInfo.getActiveFrbSession();
        IActiveCashBonusSession activeCashBonusSession = playerInfo.getActiveCashBonusSession();

        if (activeFrbSession != null) {
            double currentFRBWin = totalPayouts.toDoubleCents() + activeFrbSession.getWinSum();
            boolean appearedMaxLimit = activeFrbSession.getMaxWinLimit() != -1 && currentFRBWin >= activeFrbSession.getMaxWinLimit();
            boolean canShot = seat.getAmmoAmount() > 0 ||
                    (!getGameType().isNeedRegularAmmoForSpecialWeaponShot() && seat.isAnyWeaponShotAvailable());
            boolean needFinishFRB = !canShot
                    || !activeFrbSession.getStatus().equalsIgnoreCase("ACTIVE")
                    || appearedMaxLimit;

            activeFrbSession.setCurrentAmmoAmount(seat.getAmmoAmount());
            getLog().debug("shot in FRB bonus mode, needFinishFRB: {}, appearedMaxLimit: {}, canShot={}",
                    needFinishFRB, appearedMaxLimit, canShot);
            if (needFinishFRB) {
                if (appearedMaxLimit) {
                    getLog().debug("seat.setAmmoAmount reset, old: {}", seat.getAmmoAmount());
                    seat.setAmmoAmount(0);
                }

                getLog().info("processShot:  need to finish  the bonus session, try sitOut");
                finish(true);
            }

        } else if (activeCashBonusSession != null) {
            getLog().debug("shot in cash bonus mode");
            boolean needReleased;
            boolean needLost;
            playerInfoService.lock(seat.getAccountId());
            getLog().debug("processShot lock: {}", seat.getAccountId());
            try {
                Money possibleBalanceAmount = seat.getPossibleBalanceAmount();
                needLost = possibleBalanceAmount.smallerThan(seat.getStake());
                long betSum = activeCashBonusSession.getBetSum();
                long currentBets = totalBets.subtract(totalBetsBefore).toCents();
                double rolloverMultiplier = activeCashBonusSession.getRolloverMultiplier();
                long amount = activeCashBonusSession.getAmount();
                needReleased = amount * rolloverMultiplier <= betSum + currentBets;
                activeCashBonusSession.incrementBetSum(currentBets);
                getLog().debug("rolloverMultiplier: {}, amount: {}, betSum: {}, " +
                                "currentBets: {}, possibleBalanceAmount: {}",
                        rolloverMultiplier, amount, betSum, currentBets, possibleBalanceAmount);

                //save lobbySession and roomPlayerInfo too expensive operation, save only for release case
                if (needReleased || needLost) {
                    ILobbySession lobbySession = lobbySessionService.get(seat.getAccountId());
                    lobbySession.setActiveCashBonusSession(activeCashBonusSession);
                    lobbySessionService.add(lobbySession);
                    IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(seat.getAccountId());
                    roomPlayerInfo.setActiveCashBonusSession(activeCashBonusSession);
                    playerInfoService.put(roomPlayerInfo);
                }
                getLog().debug("updated activeCashBonusSession: {} ", activeCashBonusSession);
            } finally {
                playerInfoService.unlock(seat.getAccountId());
                getLog().debug("processShot unlock: {}", seat.getAccountId());
            }

            if (needReleased || needLost) {
                getLog().info("processShot:  need to release the round in bonus session, " +
                                "needLost: {}, activeCashBonusSession: {}",
                        needLost, activeCashBonusSession);
                finish(true);
            }
        }
    }

    public void makeBuyInForCashBonus(SEAT seat) {
        //need transfer balance to ammo
        IActiveCashBonusSession session = seat.getPlayerInfo().getActiveCashBonusSession();
        long lobbyBalance = getBalance(seat);
        Money seatBalance = Money.fromCents(lobbyBalance);
        long ammoAmount = seatBalance.divideBy(seat.getStake());
        getLog().debug("makeBuyInForCashBonus: before seatBalance={}, ammoAmount={}, seat.ammAmount={}",
                seatBalance.toCents(), ammoAmount, getAmmoAmount(seat));
        if (ammoAmount > 0) {
            seat.incrementAmmoAmount((int) ammoAmount);
            seat.incrementTotalAmmoAmount((int) ammoAmount);
            long balanceDecrement = ammoAmount * seat.getStake().toCents();
            setBalance(seat, lobbyBalance - balanceDecrement);
            session.setBalance(lobbyBalance - balanceDecrement);
        }
        long finalBalance = getBalance(seat);
        session.setBalance(finalBalance);
        persistCashBonusSession(session);
        getLog().debug("makeBuyInForCashBonus: after lobbySession.balance={}, seat.ammoAmount={}, " +
                "player.activeCashBonusSession={}", finalBalance, getAmmoAmount(seat), session);
    }

    @Override
    public void makeBuyInForTournament(SEAT seat) {
        //need transfer balance to ammo
        ITournamentSession session = seat.getPlayerInfo().getTournamentSession();
        long lobbyBalance = getBalance(seat);
        Money seatBalance = Money.fromCents(lobbyBalance);
        long ammoAmount = seatBalance.divideBy(seat.getStake());
        getLog().debug("makeBuyInForTournament: before seatBalance={}, ammoAmount={}, seat.ammAmount={}",
                seatBalance.toCents(), ammoAmount, seat.getAmmoAmount());
        if (ammoAmount > 0) {
            seat.incrementAmmoAmount((int) ammoAmount);
            seat.incrementTotalAmmoAmount((int) ammoAmount);
            long balanceDecrement = ammoAmount * seat.getStake().toCents();
            setBalance(seat, lobbyBalance - balanceDecrement);
            session.setBalance(lobbyBalance - balanceDecrement);
        }
        long finalBalance = getBalance(seat);
        session.setBalance(finalBalance);
        persistTournamentSession(session);
        getLog().debug("makeBuyInForTournament: after lobbySession.balance={}, seat.ammoAmount={}, " +
                "player.tournamentSession={}", finalBalance, seat.getAmmoAmount(), session);
    }

    /**
     * Calculates the round results at the end of the round and sends the data to the gs side.  Can make sitout of player.
     */
    @Override
    public void convertBulletsToMoney() {
        assertRoomStarted();
        long now = System.currentTimeMillis();
        Set<SEAT> seatsForProcess = new HashSet<>(getSeats());
        Set<SEAT> wantSitOutCandidates = new HashSet<>();
//        CountDownLatch asyncCallLatch = new CountDownLatch(seatsForProcess.size());
        Map<Long, IAddWinRequest> winRequests = new HashMap<>(seatsForProcess.size());
        Map<Long, Pair<Money, Money>> handleInfo = new HashMap<>(seatsForProcess.size());
        for (SEAT seat : seatsForProcess) {
            IAddWinRequest winRequest = _convertBulletsToMoneyForSeat(seat, wantSitOutCandidates, handleInfo);
            if (winRequest != null) {
                winRequests.put(seat.getAccountId(), winRequest);
            }
        }
        if (!winRequests.isEmpty()) {
            Map<Long, IAddWinResult> addWinResults = addBatchWin(new HashSet<>(winRequests.values()));
            for (SEAT seat : seatsForProcess) {
                if (winRequests.containsKey(seat.getAccountId())) {
                    long accountId = seat.getAccountId();
                    IAddWinResult result = addWinResults.get(accountId);
                    final int ammoAmount = getAmmoAmount(seat);
                    final Money roundWin = handleInfo.get(accountId).getKey();
                    final Money returnedBet = handleInfo.get(accountId).getValue();
                    IGameSocketClient socketClient = seat.getSocketClient();
                    int seatNumber = getSeatNumber(seat);
                    if (result != null) {
                        String sessionId = socketClient != null ? socketClient.getSessionId() : seat.getPlayerInfo().getSessionId();
                        handleAddWinResult(result, seat, socketClient, accountId,
                                roundWin, ammoAmount, seatNumber, wantSitOutCandidates, sessionId,
                                returnedBet, null);
                    } else {
                        setPendingWinForPlayer(seat, roundWin, ammoAmount, returnedBet.toCents(), socketClient, seatNumber);
                    }
                }
            }
        }
        try {
            for (SEAT sitOutCandidate : wantSitOutCandidates) {
                getLog().info("convertBulletsToMoney: sitOut disconnected seat={}", sitOutCandidate);
                processSitOut(sitOutCandidate.getSocketClient(), null, getSeatNumber(sitOutCandidate),
                        sitOutCandidate.getAccountId(), false, true);
            }
            StatisticsManager.getInstance().updateRequestStatistics("GameRoom::convertBulletsToMoney",
                    System.currentTimeMillis() - now, "" + getRoomInfo().getId() + ":" + getRoomInfo().getRoundId());
            getLog().debug("convertBulletsToMoney: all seats process");
        } catch (Exception e) {
            getLog().error("Interrupted", e);
        }
    }

    private IAddWinRequest _convertBulletsToMoneyForSeat(SEAT seat, Set<SEAT> wantSitOutCandidates, Map<Long, Pair<Money, Money>> handleInfo) {
        IGameSocketClient socketClient = seat.getSocketClient();
        long accountId = seat.getAccountId();
        IAddWinRequest addWinRequest = null;
        getLog().debug("convertBulletsToMoneyForSeat: seat.getPlayerInfo(): {}", seat.getPlayerInfo());
        try {
            final IRoomPlayerInfo playerInfoFromService = playerInfoService.get(accountId);
            //may be already sitOut
            if (playerInfoFromService == null) {
                getLog().debug("convertBulletsToMoneyForSeat: after lock player not found in playerInfoService, " +
                        "accountId={}", accountId);
                return null;
            } else if (playerInfoFromService.getRoomId() != getRoomInfo().getId()) {
                getLog().debug("convertBulletsToMoneyForSeat: after lock player found playerInfo for other room, " +
                        "playerInfoFromService={}", playerInfoFromService);
                return null;
            }
            IPendingOperation pendingOperation = pendingOperationService.get(seat.getAccountId());
            if (pendingOperation != null && pendingOperation.getOperationType() == PendingOperationType.ADD_WIN) {
                getLog().debug("convertBulletsToMoneyForSeat: player {} has pendingOperation already", seat.getAccountId());
                return null;
            }
            final IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
            IActiveFrbSession activeFrbSession = playerInfo.getActiveFrbSession();
            IActiveCashBonusSession activeCashBonusSession = playerInfo.getActiveCashBonusSession();
            ITournamentSession tournamentSession = playerInfo.getTournamentSession();

            int serverId = socketClient != null ? socketClient.getServerId() :
                    IRoom.extractServerId(playerInfo.getSessionId());
            String sessionId = socketClient != null ? socketClient.getSessionId() : playerInfo.getSessionId();
            long gameSessionId = playerInfo.getGameSessionId();
            getLog().debug("convertBulletsToMoneyForSeat: seat={}", seat);

            IPlayerBet playerBet = getPlayerBet(seat, playerInfo);
            boolean noActivity = isNoActivityInRound(seat, playerBet);
            IPlayerProfile playerProfile = playerProfileService.load(playerInfo.getBankId(), seat.getAccountId());
            if (activeFrbSession != null) {
                convertBulletsForFrbSeat(activeFrbSession, seat, accountId, playerInfo, serverId, gameSessionId,
                        sessionId, playerBet, noActivity, socketClient, playerProfile, wantSitOutCandidates, null);
            } else {
                final Money roundWin = seat.retrieveRoundWin();
                final Money returnedBet = getReturnedBet(seat);
                handleInfo.put(accountId, new Pair<>(roundWin, returnedBet));

                Money correctedRoundWin = roundWin;
                Money correctedReturnedBet = returnedBet;
                if (seat.getRebuyFromWin().toCents() > 0) {
                    if (seat.getRebuyFromWin().lessOrEqualsTo(returnedBet)) {
                        correctedRoundWin = roundWin.add(seat.getRebuyFromWin());
                        correctedReturnedBet = returnedBet.subtract(seat.getRebuyFromWin());
                    } else {
                        correctedRoundWin = roundWin.add(returnedBet);
                        correctedReturnedBet = Money.ZERO;
                    }
                    getLog().debug("convertBulletsToMoneyForSeat: found reBuyFromWin={}, need correcting amounts. roundWin={}, returnedBet={}, " +
                                    "correctedRoundWin={}, correctedReturnedBet={}",
                            seat.getRebuyFromWin().toCents(), roundWin.toCents(),
                            returnedBet.toCents(), correctedRoundWin.toCents(), correctedReturnedBet.toCents());
                }
                if (activeCashBonusSession != null) {
                    convertBulletsForCashBonusSeat(seat, activeCashBonusSession, correctedRoundWin, correctedReturnedBet,
                            playerProfile, playerBet, playerInfo, socketClient, accountId, wantSitOutCandidates,
                            sessionId, null);
                } else if (tournamentSession != null) {
                    convertBulletsToMoneyForTournamentSeat(seat, tournamentSession, correctedRoundWin, correctedReturnedBet,
                            playerProfile, playerBet, playerInfo, socketClient, accountId, sessionId, null);
                } else {
                    IBattlegroundRoundInfo bgRoundInfo = null;
                    if (playerInfo instanceof IBattlegroundRoomPlayerInfo) {
                        IBattlegroundRoomPlayerInfo castedPlayerInfo = (IBattlegroundRoomPlayerInfo) playerInfo;
                        bgRoundInfo = castedPlayerInfo.getBattlegroundRoundInfo();
                    }
                    addWinRequest = getTOFactoryService().createAddWinRequest(sessionId, gameSessionId, correctedRoundWin.toCents(), correctedReturnedBet.toCents(),
                            accountId, playerBet, bgRoundInfo, playerInfo.getExternalRoundId(), false);
//                    socketService.addWin(serverId,
//                                    sessionId,
//                                    gameSessionId,
//                                    correctedRoundWin,
//                                    correctedReturnedBet,
//                                    playerInfo.getExternalRoundId(),
//                                    getRoomInfo().getId(),
//                                    accountId,
//                                    playerBet,
//                                    bgRoundInfo)
//                            .doOnSuccess(addWinResult -> handleAddWinResult(addWinResult, seat, socketClient, accountId,
//                                    roundWin, ammoAmount, seatNumber, wantSitOutCandidates, sessionId,
//                                    returnedBet, asyncCallLatch))
//                            .doOnError(error -> handleAddWinError(error, seat, wantSitOutCandidates, roundWin,
//                                    ammoAmount, returnedBet, accountId, socketClient, asyncCallLatch))
//                            .subscribe();
                }
            }
        } catch (Exception e) {
            getLog().error("convertBulletsToMoneyForSeat failed for accountId={}", accountId, e);
        }
        addToWantSitOutCandidatesIfNeed(wantSitOutCandidates, seat);
        return addWinRequest;
    }

    private IPlayerBet getPlayerBet(SEAT seat, IRoomPlayerInfo playerInfo) {
        IPlayerBet playerBet = seat.getCurrentPlayerRoundInfo().getPlayerBet(playerInfo.createNewPlayerBet(), -1);
        playerBet.setStartRoundTime(getGameState().getStartRoundTime());
        boolean noActivity = isNoActivityInRound(seat, playerBet);
        if (noActivity) {
            getLog().debug("convertBulletsToMoneyForSeat: has no activity in round");
            playerBet.setData("");
        }
        return playerBet;
    }

    @Override
    protected void convertBulletsForFrbSeat(IActiveFrbSession activeFrbSession, SEAT seat, long accountId,
                                          IRoomPlayerInfo playerInfo, int serverId, long gameSessionId,
                                          String sessionId, IPlayerBet playerBet, boolean noActivity,
                                          IGameSocketClient socketClient, IPlayerProfile playerProfile,
                                          Set<SEAT> wantSitOutCandidates, CountDownLatch asyncCallLatch) throws Exception {
        boolean sessionWasClosed = "CANCELLED".equalsIgnoreCase(activeFrbSession.getStatus())
                || "EXPIRED".equalsIgnoreCase(activeFrbSession.getStatus());

        long seatRoundWin = seat.retrieveRoundWin().toCents();
        getLog().debug("convertBulletsToMoney, seatRoundWin: {}, activeFrbSession: {}, sessionWasClosed: {}",
                seatRoundWin, activeFrbSession, sessionWasClosed);
        if (!sessionWasClosed) {
            savePlayerBetForFRB(accountId, seat, playerInfo, serverId,
                    gameSessionId, playerBet, sessionId, noActivity);
            activeFrbSession.setCurrentAmmoAmount(getAmmoAmount(seat));
            activeFrbSession.incrementWinSum(seatRoundWin);
            seat.getPlayerInfo().setActiveFrbSession(activeFrbSession);
            activeFrbSessionService.persist(activeFrbSession);
            playerInfoService.put(playerInfo);
            getLog().debug("FRB activeFrbSession updated: {}", activeFrbSession);
        }

        ILobbySession lobbySession = null;
        if (socketClient != null && socketClient.getSessionId() != null) {
            lobbySession = lobbySessionService.get(socketClient.getSessionId());
        }

        if (lobbySession != null) {
            IActiveFrbSession lobbySessionActiveFrbSession = lobbySession.getActiveFrbSession();
            lobbySessionActiveFrbSession.setCurrentAmmoAmount(getAmmoAmount(seat));
            lobbySessionActiveFrbSession.setWinSum(activeFrbSession.getWinSum());
            lobbySessionService.add(lobbySession);
            getLog().debug("FRB lobbySession updated: {}", lobbySession);
        }

        socketService.sendMQDataSync(serverId, seat, activeFrbSession, playerProfile,
                roomInfo.getGameType().getGameId(), Collections.emptySet(), Collections.emptyMap());

        //need check for finalize FRB

        boolean achievedWinLimit = activeFrbSession.getMaxWinLimit() != -1 &&
                activeFrbSession.getWinSum() >= activeFrbSession.getMaxWinLimit();
        if ((activeFrbSession.getCurrentAmmoAmount() <= 0) || sessionWasClosed || achievedWinLimit) {
            getLog().debug("convertBulletsToMoney: found conditions for close FRB, seat={}, " +
                            "activeFrbSession={}, sessionWasClosed: {}, achievedWinLimit: {}",
                    seat, activeFrbSession, sessionWasClosed, achievedWinLimit);
            closeFRB(serverId, accountId, sessionId, gameSessionId, activeFrbSession, seat, socketClient,
                    playerInfo, wantSitOutCandidates, asyncCallLatch);
        }
    }

    protected void savePlayerBetForFRB(long accountId, SEAT seat, IRoomPlayerInfo playerInfo, int serverId,
                                     long gameSessionId, IPlayerBet playerBet, String sessionId, boolean noActivity) {
        if (!noActivity) {
            long oldPlayerRoundId = seat.getCurrentPlayerRoundInfo().getPlayerRoundId();
            Boolean playerBetForFRBisSaved = socketService.savePlayerBetForFRB(serverId, sessionId, gameSessionId,
                    playerInfo.getExternalRoundId(), accountId, playerBet);
            if (playerBetForFRBisSaved) {
                getLog().debug("savePlayerBetForFRB: success in activeFrbSession," +
                                " reset seat player info data, old playerRoundInfo:{}, oldPlayerRoundId: {} ",
                        seat.getCurrentPlayerRoundInfo(), oldPlayerRoundId);
                seat.initCurrentRoundInfo(playerInfo);
                seat.getCurrentPlayerRoundInfo().setPlayerRoundId(oldPlayerRoundId);
                getLog().debug("savePlayerBetForFRB: new playerRoundInfo: {}", seat.getCurrentPlayerRoundInfo());
            } else {
                getLog().debug("savePlayerBetForFRB: error in activeFrbSession");
            }
        }
    }

    protected void closeFRB(int serverId, long accountId, String sessionId, long gameSessionId,
                          IActiveFrbSession activeFrbSession, SEAT seat, IGameSocketClient socketClient,
                          IRoomPlayerInfo playerInfo, Set<SEAT> wantSitOutCandidates, CountDownLatch asyncCallLatch) {
        try {
            IFrbCloseResult frbCloseResult = socketService.closeFRBonusAndSession(serverId, accountId, sessionId, gameSessionId,
                    getRoomInfo().getGameType().getGameId(), activeFrbSession.getBonusId(),
                    activeFrbSession.getWinSum());
            //need change locker before put
            getLog().debug("convertBulletsToMoney 1 forceUnlock : {}", accountId);
            playerInfoService.lock(accountId);
            getLog().debug("convertBullet 1 HS lock: {}", accountId);
            getLog().debug("convertBulletsToMoney: success close activeFrbSession");
            activeFrbSessionService.remove(activeFrbSession.getBonusId());

            clearCrossRoundSeatStats(seat);
            persistCrossRoundSeatStats(seat);

            if (socketClient != null) {
                String closeReason = "Completed";
                long winSum = activeFrbSession.getWinSum();
                if (frbCloseResult.getErrorCode() > 0) {
                    winSum = 0;
                    closeReason = (frbCloseResult.getErrorCode() == 3 || frbCloseResult.getErrorCode() == 6)
                            ? "Cancelled" : "Expired";
                }

                IFRBEnded frbEnded = getTOFactoryService().createFRBEnded(getCurrentTime(),
                        winSum,
                        closeReason,
                        frbCloseResult.isHasNextFrb(),
                        frbCloseResult.getRealWinSum());

                socketClient.sendMessage(frbEnded);
                IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
                setBalance(seat, frbCloseResult.getBalance());
                createRoundCompletedTask(seat, sessionId, roomPlayerInfo, frbCloseResult.getBalance());
            }
            //sitOut later
            seat.setWantSitOut(true);
            wantSitOutCandidates.add(seat);
        } catch (Exception e) {
            if (socketClient != null) {
                socketClient.sendMessage(getTOFactoryService().createError(ErrorCodes.FOUND_PENDING_OPERATION,
                        "Close FRB failed", getCurrentTime(), TObject.SERVER_RID));
            }
        } finally {
            playerInfoService.unlock(accountId);
            getLog().debug("convertBulletsToMoney 2 unlock : {}", accountId);
        }
    }

    protected void convertBulletsForCashBonusSeat(SEAT seat, IActiveCashBonusSession activeCashBonusSession,
                                                Money correctedRoundWin, Money correctedReturnedBet,
                                                IPlayerProfile playerProfile, IPlayerBet playerBet,
                                                IRoomPlayerInfo playerInfo, IGameSocketClient socketClient,
                                                long accountId, Set<SEAT> wantSitOutCandidates, String sessionId,
                                                CountDownLatch asyncCallLatch) throws CommonException {
        long balance = getBalance(seat);
        getLog().debug("convertBulletsToMoney: before change, activeCashBonusSession={}, " +
                "balance: {}", activeCashBonusSession, balance);
        activeCashBonusSession.setBalance(correctedRoundWin.toCents() + correctedReturnedBet.toCents()
                + balance);
        getLog().debug("convertBulletsToMoney: after change, activeCashBonusSession={}",
                activeCashBonusSession);
        persistCashBonusSession(activeCashBonusSession);

        String oldStatus = activeCashBonusSession.getStatus();

        if (activeCashBonusSession.isActive()) {
            IActiveCashBonusSession savedCashBonus = socketService.saveCashBonusRoundResult(
                    getGameType().getGameId(), seat, activeCashBonusSession, playerProfile,
                    Collections.emptySet(), Collections.emptyMap(), playerBet,
                    playerInfo.getExternalRoundId());
            persistCashBonusSession(savedCashBonus);
            activeCashBonusSession = savedCashBonus;
        }
        getLog().debug("convertBulletsToMoney: success, reset seat player info data, old playerRoundInfo: " +
                "{}", seat.getCurrentPlayerRoundInfo());
        seat.initCurrentRoundInfo(playerInfo);
        getLog().debug("convertBulletsToMoney: new playerRoundInfo: {}", seat.getCurrentPlayerRoundInfo());

        socketClient.sendMessage(getTOFactoryService().createBalanceUpdated(
                getCurrentTime(), activeCashBonusSession.getBalance(), getAmmoAmount(seat)));
        ILobbySession lobbySession = lobbySessionService.get(socketClient.getSessionId());
        //lobby session may be already removed
        if (lobbySession != null) {
            lobbySession.setActiveCashBonusSession(activeCashBonusSession);
            lobbySessionService.add(lobbySession);
        }
        playerInfoService.lock(accountId);
        try {
            getLog().debug("convertBullet 2 HS lock: {}", accountId);
            IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
            roomPlayerInfo.setActiveCashBonusSession(activeCashBonusSession);
            roomPlayerInfo.finishCurrentRound();
            if (activeCashBonusSession.isActive()) {
                updateCashBonus(seat, activeCashBonusSession.getBalance(),
                        activeCashBonusSession.getBetSum());
            } else {
                activeCashBonusSessionService.remove(activeCashBonusSession.getId());
                //sitOut later
                seat.setWantSitOut(true);
                wantSitOutCandidates.add(seat);
            }
            if (!oldStatus.equalsIgnoreCase(activeCashBonusSession.getStatus())) {
                socketClient.sendMessage(getTOFactoryService().createBonusStatusChangedMessage(
                        activeCashBonusSession.getId(), oldStatus, activeCashBonusSession.getStatus(),
                        "", BonusType.CASHBONUS.name()));
            }
            playerInfoService.put(roomPlayerInfo);
            seat.setPlayerInfo(roomPlayerInfo);
            createRoundCompletedTask(seat, sessionId, roomPlayerInfo, activeCashBonusSession.getBalance());
        } finally {
            playerInfoService.unlock(accountId);
            getLog().debug("convertBulletsToMoney 5 unlock : {}", accountId);
        }
    }

    private void convertBulletsToMoneyForTournamentSeat(SEAT seat, ITournamentSession tournamentSession,
                                                        Money correctedRoundWin, Money correctedReturnedBet,
                                                        IPlayerProfile playerProfile, IPlayerBet playerBet,
                                                        IRoomPlayerInfo playerInfo, IGameSocketClient socketClient,
                                                        long accountId, String sessionId,
                                                        CountDownLatch asyncCallLatch) throws CommonException {
        long balance = getBalance(seat);
        getLog().debug("convertBulletsToMoney: before change, tournament={}, balance={}",
                tournamentSession, balance);
        tournamentSession.setBalance(correctedRoundWin.toCents() + correctedReturnedBet.toCents()
                + balance);
        getLog().debug("convertBulletsToMoney: after change, tournament={}", tournamentSession);
        persistTournamentSession(tournamentSession);
        socketService.saveTournamentRoundResult(getGameType().getGameId(), seat, tournamentSession,
                playerProfile, Collections.emptySet(), Collections.emptyMap(), playerBet,
                playerInfo.getExternalRoundId());
        getLog().debug("convertBulletsToMoney: success, reset seat player info data, old playerRoundInfo: " +
                "{}", seat.getCurrentPlayerRoundInfo());
        seat.initCurrentRoundInfo(playerInfo);
        getLog().debug("convertBulletsToMoney: new playerRoundInfo: {}", seat.getCurrentPlayerRoundInfo());

        socketClient.sendMessage(getTOFactoryService().createBalanceUpdated(
                getCurrentTime(), tournamentSession.getBalance(), getAmmoAmount(seat)));
        ILobbySession lobbySession = lobbySessionService.get(seat.getPlayerInfo().getSessionId());
        if (lobbySession != null) {
            lobbySession.setTournamentSession(tournamentSession);
            lobbySessionService.add(lobbySession);
        }
        playerInfoService.lock(accountId);
        try {
            getLog().debug("convertBullet 3 HS lock: {}", accountId);
            IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
            roomPlayerInfo.setTournamentSession(tournamentSession);
            roomPlayerInfo.finishCurrentRound();
            playerInfoService.put(roomPlayerInfo);
            seat.setPlayerInfo(roomPlayerInfo);
            createRoundCompletedTask(seat, sessionId, roomPlayerInfo, tournamentSession.getBalance());
        } finally {
            playerInfoService.unlock(accountId);
            getLog().debug("convertBulletsToMoney 7 unlock : {}", accountId);
        }
    }

    private void setPendingWinForPlayer(SEAT seat, Money roundWin, int ammoAmount, long returnedBet, IGameSocketClient socketClient, int seatNumber) {
        getLog().warn("addWin: found pending for seat {}", seat);
        long accountId = seat.getAccountId();
        try {
            playerInfoService.lock(accountId);
            seat.initCurrentRoundInfo(seat.getPlayerInfo());
            getLog().debug("setPendingWinForPlayer lock: {}", accountId);
            IRoomPlayerInfo roomPlayerInfo = playerInfoService.get(accountId);
            roomPlayerInfo.finishCurrentRound();
            roomPlayerInfo.setPendingOperation(true, "addWin, roundWin=" + roundWin.toCents() +
                    ", ammoAmount=" + ammoAmount);
            IError errorMessage = getTOFactoryService().createError(ErrorCodes.FOUND_PENDING_OPERATION, "Send player win is pending", getCurrentTime(), TObject.SERVER_RID);
            socketClient.sendMessage(errorMessage);
            seat.setPlayerInfo(roomPlayerInfo);
            saveSeat(seatNumber, seat);
            playerInfoService.put(roomPlayerInfo);
        } finally {
            playerInfoService.unlock(accountId);
            getLog().debug("setPendingWinForPlayer unlock: {}", accountId);
        }
    }

    /**
     * Adds batch wins for sending to gs side.
     * @param winRequests set for batch requests
     * @return {@code Map<Long, IAddWinResult>} map of results from gs side.
     */
    protected Map<Long, IAddWinResult> addBatchWin(Set<IAddWinRequest> winRequests) {
        IRoomInfo roomInfo = getRoomInfo();
        return socketService.addBatchWin(roomInfo.getId(), roomInfo.getRoundId(), getGameType().getGameId(), winRequests, roomInfo.getBankId(),
                TimeUnit.SECONDS.toMillis(3));
    }

    /**
     * Makes sitout of player from room
     * @param seat seat of player
     * @param ammoAmount current ammo amount
     * @param activeCashBonusSession frb bonus session
     * @param bulletsConvertedToMoney true if you need convert bullets to money
     * @param socketClient game socket client of player
     * @param client  game socket client of player
     * @param request SitOut request from client
     * @param serverId serverId
     * @param seatNumber seat number
     * @param oldSeatNumber old seat number
     * @param tournamentSession tournament bonus session
     * @param accountId accountId of player
     * @throws CommonException  if any unexpected error occur
     */
    protected void processSitOutForNonFrbMode(SEAT seat, int ammoAmount, IActiveCashBonusSession activeCashBonusSession,
                                              boolean bulletsConvertedToMoney,
                                              IGameSocketClient socketClient, IGameSocketClient client,
                                              ISitOut request, int serverId, int seatNumber, int oldSeatNumber,
                                              ITournamentSession tournamentSession, long accountId) throws CommonException {

        int seatAmmoAmount = getAmmoAmount(seat);
        final Money roundWin;
        final Money returnedBet;

        roundWin = seat.retrieveRoundWin();
        returnedBet = getReturnedBet(seat);
        IRoomPlayerInfo playerInfo = seat.getPlayerInfo();
        playerInfo.setPendingOperation(true, "sitOut, roundWin=" + roundWin.toCents() + ", ammoAmount=" + ammoAmount + ", returnedBet=" + returnedBet);

        playerInfoService.put(playerInfo);
        Pair<Money, Money> correctedRoundWinAndBet = calculateCorrectedWinAndBet(roundWin, returnedBet, seat);
        Money correctedRoundWin = correctedRoundWinAndBet.getKey();
        Money correctedReturnedBet = correctedRoundWinAndBet.getValue();
        IPlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
        updateStatOnEndRound(seat, roundInfo);
        final IPlayerBet playerBet = seat.getCurrentPlayerRoundInfo().getPlayerBet(
                playerInfo.createNewPlayerBet(), seatAmmoAmount);
        try {
            IPlayerProfile playerProfile = playerProfileService.load(playerInfo.getBankId(), seat.getAccountId());
            if (activeCashBonusSession != null) {
                processSitOutCashBonusSession(activeCashBonusSession, bulletsConvertedToMoney,
                        correctedRoundWin, correctedReturnedBet, seat, playerProfile, playerBet, playerInfo,
                        socketClient, client, request, roundWin, ammoAmount, serverId, seatNumber, oldSeatNumber);
            } else if (tournamentSession != null) {
                processSitOutTournamentSession(tournamentSession, bulletsConvertedToMoney, correctedRoundWin,
                        correctedReturnedBet, seat, playerProfile, playerBet, playerInfo, socketClient,
                        client, request, roundWin, ammoAmount, serverId, seatNumber, oldSeatNumber);
            } else {
                try {
                    long gameId = roomInfo.getGameType().getGameId();
                    Set<IQuest> allQuests = playerQuestsService.getAllQuests(seat.getBankId(),
                            seat.getAccountId(), MoneyType.REAL.ordinal(), gameId);
                    Map<Long, Map<Integer, Integer>> allWeapons = weaponService.getAllWeaponsLong(
                            seat.getBankId(), seat.getAccountId(), MoneyType.REAL.ordinal(), gameId);

                    socketService.sendMQDataSync(serverId, seat, null, playerProfile,
                            gameId, allQuests, allWeapons);
                } catch (Exception e) {
                    getLog().error("sendMQDataSync error, profile={}", playerProfile, e);
                }
                IBattlegroundRoundInfo bgRoundInfo = null;
                if (playerInfo instanceof IBattlegroundRoomPlayerInfo) {
                    IBattlegroundRoomPlayerInfo castedPlayerInfo = (IBattlegroundRoomPlayerInfo) playerInfo;
                    bgRoundInfo = castedPlayerInfo.getBattlegroundRoundInfo();
                    if (bgRoundInfo == null) {
                        castedPlayerInfo.createBattlegroundRoundInfo(roomInfo.getStake().toCents(), 0,
                                0, 0, null, 0,
                                null, seat.getAccountId(), 1, playerInfo.getGameSessionId(), seat.getTotalScore().getLongAmount(),
                                roomInfo.getRoundId(), roomInfo.getLastRoundStartDate(), 0.0, roomInfo.getPrivateRoomId());
                        bgRoundInfo = castedPlayerInfo.getBattlegroundRoundInfo();
                    }
                }
                IPendingOperation pendingOperation = pendingOperationService.get(accountId);
                if (pendingOperation == null) {
                    if (correctedRoundWin.toCents() >= 0 || correctedReturnedBet.toCents() >= 0) {
                        IAddWinRequest winRequest = getTOFactoryService().createAddWinRequest(playerInfo.getSessionId(),  playerInfo.getGameSessionId(), correctedRoundWin.toCents(), correctedReturnedBet.toCents(),
                                accountId, playerBet, bgRoundInfo, playerInfo.getExternalRoundId(), true);
                        if (winRequest != null) {
                            Map<Long, IAddWinResult> addWinResults = addBatchWin(new HashSet<>(Collections.singleton(winRequest)));
                            IAddWinResult result = addWinResults.get(accountId);
                            if (result == null || !result.isSuccess()) {
                                setPendingWinForPlayer(seat, roundWin, ammoAmount, returnedBet.toCents(), socketClient, seatNumber);
                            }
                        }
                    } else {
                        boolean sitOutResult = socketService.closeGameSession(serverId, playerInfo.getSessionId(), accountId, playerInfo.getGameSessionId(),
                                getId(), getGameType().getGameId(), playerInfo.getBankId(), 0L);
                        if (!sitOutResult) {
                            handleCloseGameSessionError(seat, playerInfo, client, request, socketClient, seatNumber, oldSeatNumber);
                            return;
                        }
                    }
                }else {
                    getLog().warn("processSitOutForNonFrbMode: skip call closeGameSession, found pendingOperation={}", pendingOperation);
                }
                checkResultAndFinishSitOut(null, seat, playerInfo, client, request, roundWin, ammoAmount,
                        socketClient, null, serverId, seatNumber, oldSeatNumber);
            }
        } catch (Exception e) {
            handleSitOutError(e, seat, roundWin, ammoAmount, playerInfo, request, seatNumber, oldSeatNumber, client);
        }
    }

    /**
     * Corrects win and bet data from returnedBet amount
     * @param roundWin current round win
     * @param returnedBet returned bet
     * @param seat seat of player
     * @return {@code Pair<Money, Money> } corrected bet/win of player
     */
    private Pair<Money, Money> calculateCorrectedWinAndBet(Money roundWin, Money returnedBet, SEAT seat) {
        Money correctedRoundWin = roundWin;
        Money correctedReturnedBet = returnedBet;
        if (seat.getRebuyFromWin().toCents() > 0) {
            if (seat.getRebuyFromWin().lessOrEqualsTo(returnedBet)) {
                correctedRoundWin = roundWin.add(seat.getRebuyFromWin());
                correctedReturnedBet = returnedBet.subtract(seat.getRebuyFromWin());
            } else {
                correctedRoundWin = roundWin.add(returnedBet);
                correctedReturnedBet = Money.ZERO;
            }
            getLog().debug("processSitOut: Found rebuyFromWin={}, need correcting amounts. roundWin={}, " +
                            "returnedBet={}, correctedRoundWin={}, correctedReturnedBet={}",
                    seat.getRebuyFromWin().toCents(), roundWin.toCents(),
                    returnedBet.toCents(), correctedRoundWin.toCents(), correctedReturnedBet.toCents());
        }
        return new Pair<>(correctedRoundWin, correctedReturnedBet);
    }

    protected void handleCloseGameSessionError(SEAT seat, IRoomPlayerInfo playerInfo, IGameSocketClient client, ISitOut request, IGameSocketClient socketClient, int seatNumber, int oldSeatNumber) throws CommonException {
        getLog().error("processSitOut: failed, but pending transaction created, " +
                "rollback not required");
        if (client != null) {
            client.sendMessage(getTOFactoryService().createError(ErrorCodes.FOUND_PENDING_OPERATION,
                    "close game session operation in progress", getCurrentTime(),
                    request != null ? request.getRid() : TObject.SERVER_RID));
        }
//        processFinishSitOut(socketClient, playerInfo, seat, request, seatNumber, oldSeatNumber);
        getGameState().processSitOut(seat);
    }

    /**
     * Processing after buyIn for player
     * @param serverId serverId
     * @param accountId accountId of player
     * @param sessionId sessionId of player
     * @param amount amount of buyIn.
     * @param gameSessionId game sessionId of player
     * @param roomId roomId
     * @param betNumber number of bet
     * @param tournamentId tournamentId
     * @param currentBalance current balance of player
     * @param buyInResult {@code IBuyInResult} buiIn result from gs side.
     * @throws BuyInFailedException  if buyInResult is success but player already sitOut
     */
    @Override
    public void buyInPostProcess(int serverId, long accountId, String sessionId, Money amount, long gameSessionId, long roomId, int betNumber, Long tournamentId, Long currentBalance, IBuyInResult buyInResult) throws BuyInFailedException {
        SEAT seatByAccountId = getSeatByAccountId(accountId);
        if (seatByAccountId == null && buyInResult != null && buyInResult.isSuccess()) {
            IPendingOperation buyInPendingOperation = pendingOperationService.createBuyInPendingOperation(accountId, sessionId, gameSessionId, roomId,
                    buyInResult.getAmount(), betNumber, tournamentId, currentBalance, 0, 0);
            getLog().debug("BuyIn operation response is late, need add operation on tracker. accountId: {}, operation: {}", accountId, buyInPendingOperation);
            pendingOperationService.create(buyInPendingOperation);
            playerInfoService.remove(roomInfoService, roomId, accountId);
            throw new BuyInFailedException("Player already sitOut, BuyInPendingOperation applied", false, true);
        }
    }

    /**
     * Removes seats from room if players has pending operations.
     */
    @Override
    public void removeSeatsWithPendingOperations() {
        for (SEAT seat : getAllSeats()) {
            if (seat == null) {
                continue;
            }
            IRoomPlayerInfo playerInfo = playerInfoService.get(seat.getAccountId());
            try {
                if (playerInfo != null && playerInfo.isPendingOperation()) {
                    getLog().debug("Found player with pending operation, accountId: {} need finishSitOut", seat.getAccountId());
                    seat.setSitOutStarted(false);
                    int oldSeatNumber = getSeatNumber(seat);
                    removeSeat(seat.getNumber(), seat);
                    setSeatNumber(seat, -1);
                    if (!isBattlegroundMode()) {
                        Money roundWin = seat.retrieveRoundWin();
                        Money returnedBet = getReturnedBet(seat);
                        Pair<Money, Money> correctedRoundWinAndBet = calculateCorrectedWinAndBet(roundWin, returnedBet, seat);
                        Money correctedRoundWin = correctedRoundWinAndBet.getKey();
                        Money correctedReturnedBet = correctedRoundWinAndBet.getValue();
                        if (correctedRoundWin.toCents() > 0 || correctedReturnedBet.toCents() > 0) {
                            IGameSocketClient socketClient = seat.getSocketClient();
                            String sessionId = socketClient != null ? socketClient.getSessionId() : playerInfo.getSessionId();
                            long gameSessionId = playerInfo.getGameSessionId();
                            IPlayerBet playerBet = getPlayerBet(seat, playerInfo);
                            IPendingOperation operation = pendingOperationService.createWinPendingOperation(seat.getAccountId(), sessionId, gameSessionId, roomInfo.getId(), roomInfo.getGameType().getGameId(), seat.getBankId(), correctedRoundWin.toCents(), correctedReturnedBet.toCents(), playerInfo.getExternalRoundId(), playerBet, null);
                            pendingOperationService.create(operation);
                            getLog().debug("Crated win pending operation, accountId: {}, operation={}", seat.getAccountId(), operation);

                        }
                        sendSitOutMessage(seat, null, oldSeatNumber, -1, false, false);
                    }
                    IGameSocketClient socketClient = seat.getSocketClient();
                    if (socketClient != null) {
                        socketClient.setSeatNumber(-1);
                        socketClient.setRoomId(null);
                    }
                    removeObserverByAccountId(playerInfo.getId());
                }
            } catch (Exception e) {
                getLog().error("removeSeatsWithPendingOperations failed, seat.accountId={}", seat.getAccountId(), e);
            }
        }
    }

    @Override
    public boolean hasNotReadyNotKickedSeat() {
        return false;
    }
}
