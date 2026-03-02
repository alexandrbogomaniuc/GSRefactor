package com.betsoft.casino.mp.clashofthegods.model;

import com.betsoft.casino.mp.clashofthegods.model.math.*;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.betsoft.casino.utils.TObject;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import com.abs.casino.common.util.Triple;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.clashofthegods.model.math.EnemyRange.*;
import static com.betsoft.casino.mp.model.EnemyDestroyReason.SIMPLE_SHOT;
import static com.betsoft.casino.mp.utils.ErrorCodes.*;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

@SuppressWarnings("Duplicates")
public class PlayGameState extends AbstractActionPlayGameState<GameRoom, Seat, GameMap, PlayGameState> {
    private static final int MAX_ALIVE_ENEMIES = 50;
    private transient boolean needImmediatelySpawn = false;
    private transient Map<Integer, Long> leaveAndDestroyEnemiesTime = new HashMap<>();
    private transient List<Integer> remainingBosses;
    private static transient Map<Integer, Long> limitsByTypes;
    private static transient Map<Integer, Long> limitsBySwarm;
    private transient ConcurrentHashMap<Long, Long> enemiesWithX2Mode;
    private static final long respawnCommonDelay = 10000;
    private transient boolean phoenixCanBeAppearedInRound;
    private static final long defaultTimeX2 = 5000;
    private transient long roundTimeX2 = 5000;

    public PlayGameState() {
        super();
    }

    public PlayGameState(GameRoom gameRoom) {
        super(gameRoom, null);
    }

    @Override
    protected int getMaxAliveEnemies() {
        return MAX_ALIVE_ENEMIES;
    }


    public static Map<Integer, Long> getLimitsByTypes() {
        if (limitsByTypes == null || limitsByTypes.isEmpty()) {
            limitsByTypes = new HashMap<>();
            limitsByTypes.put(EnemyType.Snake.getId(), 1L);
            limitsByTypes.put(EnemyType.Tiger.getId(), 1L);
            limitsByTypes.put(EnemyType.Spirits_1_RED.getId(), 1L);
            limitsByTypes.put(EnemyType.Spirits_2_ORANGE.getId(), 1L);
            limitsByTypes.put(EnemyType.Spirits_3_GREEN.getId(), 1L);
            limitsByTypes.put(EnemyType.Spirits_4_BLUE.getId(), 1L);
            limitsByTypes.put(EnemyType.Spirits_5_VIOLETT.getId(), 1L);
        }
        return limitsByTypes;
    }

    public static Map<Integer, Long> getLimitsBySwarm() {
        if (limitsBySwarm == null || limitsBySwarm.isEmpty()) {
            limitsBySwarm = new HashMap<>();
            limitsBySwarm.put(SwarmType.EVIL_SPIRIT.getTypeId(), 1L);
            limitsBySwarm.put(SwarmType.EVIL_SPIRIT_LINE.getTypeId(), 2L);
            limitsBySwarm.put(SwarmType.EVIL_SPIRIT_LIZARD_MAN.getTypeId(), 1L);
            limitsBySwarm.put(SwarmType.LIZARD_MAN.getTypeId(), 1L);
            limitsBySwarm.put(SwarmType.LIZARD_MAN_LINE.getTypeId(), 1L);
            limitsBySwarm.put(SwarmType.OWL.getTypeId(), 2L);
            limitsBySwarm.put(SwarmType.PHOENIX_LANTERN.getTypeId(), 1L);
            limitsBySwarm.put(SwarmType.DRAGON_FLY_MODE_1.getTypeId(), 2L);
            limitsBySwarm.put(SwarmType.DRAGON_FLY_MODE_2.getTypeId(), 1L);
            limitsBySwarm.put(SwarmType.BEETLE_SWARM_1.getTypeId(), 1L);
            limitsBySwarm.put(SwarmType.BEETLE_SWARM_2.getTypeId(), 1L);
        }
        return limitsBySwarm;
    }

    public static void setLimitsByTypes(Map<Integer, Long> limitsByTypes) {
        PlayGameState.limitsByTypes = limitsByTypes;
    }

    @Override
    protected int getMaxAliveCritters() {
        return 0;
    }

    @Override
    protected void setWaitingGameState() throws CommonException {
        gameRoom.setGameState(new WaitingPlayersGameState(gameRoom));
    }

    @Override
    protected void setQualifyGameState() throws CommonException {
        gameRoom.setGameState(new QualifyGameState(gameRoom, getCurrentMapId(), pauseTime, startRoundTime, endRoundTime));
    }

    @Override
    protected void setPossibleEnemies() {
        gameRoom.setPossibleEnemies(EnemyRange.BaseEnemies);
    }

    public List<Integer> getRemainingBosses() {
        if (remainingBosses == null) {
            remainingBosses = new ArrayList<>();
        }
        return remainingBosses;
    }

    @Override
    public void init() throws CommonException {
        super.init();
        leaveAndDestroyEnemiesTime = new HashMap<>();
        getRemainingBosses().clear();
        getEnemiesWithX2Mode().clear();
        roundTimeX2 = defaultTimeX2;
        phoenixCanBeAppearedInRound = RNG.nextInt(3) == 0;
        getLog().debug("phoenixCanBeAppearedInRound: {}, startRoundTime: {}, endRoundTime: {}",
                phoenixCanBeAppearedInRound, getStartRoundTime(), getEndRoundTime());
    }

    @Override
    protected void updateWithLock() throws CommonException {
        GameMap map = getMap();

        updateEnemiesX2Mode();
        if (needLongX2FromTestStand()) {
            getLog().debug("updateWithLock set x2 time to 3 minutes from teststand");
            roundTimeX2 = 3 * 60 * 1000;
        }

        if (allowSpawn && isManualGenerationEnemies())
            return;

        List<Integer> removedEnemies = map.updateAndReturnListTypes();
        removedEnemies.forEach(typeId -> getLeaveAndDestroyEnemiesTime().put(typeId, System.currentTimeMillis()));

        if (!needWaitingWhenEnemiesLeave) {
            sendUpdateTrajectories(false);
        }

        checkTestStandFeatures();

        if (map.getItemsSize() >= MAX_ALIVE_ENEMIES) {
            return;
        }

        List<Triple<Integer, Integer, Integer>> itemsTypeIdsAndSwarmState = map.getItemsTypeIdsAndSwarmTypeAndIds();

        if (isNeedMinimalEnemies() && itemsTypeIdsAndSwarmState.size() > 3)
            return;

        Map<Integer, Long> actualCountersByType = new HashMap<>();
        Map<Integer, Set<Integer>> swarmUniqLiveCounts = getMap().getSwarmUniqLiveCounts();
        Map<Integer, Set<Integer>> swarmUniqRemovedCounts = getMap().getSwarmUniqRemovedCounts();

        int cntDragons = 0;
        for (Triple<Integer, Integer, Integer> triple : itemsTypeIdsAndSwarmState) {
            EnemyType enemyType = EnemyType.getById(triple.first());
            Integer swarmTypeId = triple.second();
            if (swarmTypeId == -1) {
                Long cnt = actualCountersByType.get(enemyType.getId());
                actualCountersByType.put(enemyType.getId(), cnt == null ? 1L : cnt + 1);
                if (EnemyRange.DRAGONS.getEnemies().contains(enemyType)) {
                    cntDragons++;
                }
            }
        }


        if (needWaitingWhenEnemiesLeave || !allowSpawn) {
            getRemainingBosses().clear();
        }

        if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            getLog().debug("no live enemies in room:, finish ");
            nextSubRound();
            return;
        }

        boolean needRandomBoss = RNG.nextInt(50) == 0;

        if (subround.equals(PlaySubround.BASE) && !getRemainingBosses().isEmpty()
                && (isNeedImmediatelySpawn() || needRandomBoss || getMap().noEnemiesInRoom())) {
            mainBossIsAvailable = true;
            nextSubRound();
            mainBossIsAvailable = false;
            getLog().debug(" generate Boss, remainingNumberOfBoss : {}, getRemainingBosses(): {}",
                    remainingNumberOfBoss, getRemainingBosses());
            return;
        }

        if (allowSpawn) {
            for (Map.Entry<Integer, Long> enemyLimit : getLimitsByTypes().entrySet()) {
                Integer enemyTypeId = enemyLimit.getKey();
                Long actualNumber = actualCountersByType.get(enemyTypeId);
                if (actualNumber == null)
                    actualNumber = 0L;

                int countRemovedEnemiesByTypeId = getMap().getCountRemovedEnemiesByTypeId(enemyTypeId);
                long totalEnemies = actualNumber + countRemovedEnemiesByTypeId;
                Enemy returnedEnemy = getMap().getReadyRemovedEnemiesByTypeId(enemyTypeId);
                if (RNG.nextInt(20) == 0 && (totalEnemies < enemyLimit.getValue() || returnedEnemy != null)) {
                    spawnEnemy(EnemyType.values()[enemyTypeId], -1, null, -1,
                            returnedEnemy);
                }
            }

            if (cntDragons == 0 && RNG.nextInt(70) == 0 && System.currentTimeMillis() < getEndRoundTime() - 30000) {
                int cntRedDragons = getMap().getCountRemovedEnemiesByTypeId(EnemyType.Golden_Dragon.getId());
                int cntSilverDragons = getMap().getCountRemovedEnemiesByTypeId(EnemyType.Silver_Dragon.getId());
                EnemyType enemyType = RNG.nextBoolean() ? EnemyType.Golden_Dragon : EnemyType.Silver_Dragon;
                Enemy returnedEnemy = getMap().getReadyRemovedEnemiesByTypeId(EnemyType.Golden_Dragon.getId());
                if (returnedEnemy == null)
                    returnedEnemy = getMap().getReadyRemovedEnemiesByTypeId(EnemyType.Silver_Dragon.getId());

                boolean needWaitSwarm = returnedEnemy == null && (cntSilverDragons != 0 || cntRedDragons != 0);
                if (!needWaitSwarm) {
                    spawnEnemy(enemyType, -1, null, -1, returnedEnemy);
                }
            }

            for (Map.Entry<Integer, Long> enemyLimit : getLimitsBySwarm().entrySet()) {
                Integer swarmTypeId = enemyLimit.getKey();
                boolean isPhoenix = SwarmType.PHOENIX_LANTERN.getTypeId() == swarmTypeId;
                int liveSwarmCounts = swarmUniqLiveCounts.get(swarmTypeId) != null ? swarmUniqLiveCounts.get(swarmTypeId).size() : 0;
                int removedSwarmCounts = swarmUniqRemovedCounts.get(swarmTypeId) != null ? swarmUniqRemovedCounts.get(swarmTypeId).size() : 0;
                int totalSwarms = liveSwarmCounts + removedSwarmCounts;
                boolean maxReached = totalSwarms >= enemyLimit.getValue();
                if ((!maxReached || (removedSwarmCounts > 0)) && RNG.nextInt(20) == 0) {
                    long currentTime = System.currentTimeMillis();
                    boolean needPhoenixInRealGame = phoenixCanBeAppearedInRound
                            && currentTime > (getStartRoundTime() + 30000)
                            && RNG.nextInt(20) == 0
                            && currentTime < (getEndRoundTime() - 60000);

                    if (!isPhoenix || needPhoenixInRealGame || needPhoenixFromTestStand())
                        spawnEnemyWithClones(swarmTypeId, liveSwarmCounts, removedSwarmCounts, maxReached);
                }
            }

        } else if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            nextSubRound();
        }
    }

    boolean findFeatureIdInTeststand(int featureId) {
        boolean res = false;
        TestStandLocal testStandLocal = TestStandLocal.getInstance();
        if (!testStandLocal.featuresIsEmpty()) {
            for (Seat seat : gameRoom.getSeats()) {
                String sessionId = seat.getPlayerInfo().getSessionId();
                TestStandFeature featureBySid = testStandLocal.getFeatureBySid(sessionId);
                if (featureBySid != null && featureBySid.getId() == featureId) {
                    res = true;
                    testStandLocal.removeFeatureBySid(sessionId);
                }
            }
        }
        return res;
    }

    boolean needLongX2FromTestStand() {
        return findFeatureIdInTeststand(68);
    }

    boolean needPhoenixFromTestStand() {
        return findFeatureIdInTeststand(65);
    }

    private void updateEnemiesX2Mode() {
        boolean lock = false;
        Map<Long, Long> enemiesWithX2Mode = getEnemiesWithX2Mode();
        if (!enemiesWithX2Mode.isEmpty()) {
            getLog().debug("updateEnemiesX2Mode enemiesWithX2Mode: {}", enemiesWithX2Mode);
        }

        if (lockShots == null)
            return;

        try {
            lock = lockShots.tryLock(200, TimeUnit.MILLISECONDS);
            if (!lock) {
                getLog().debug("updateEnemiesX2Mode can't get lock");
                return;
            }

            List<Long> needRemoveEnemies = new ArrayList<>();
            Set<Map.Entry<Long, Long>> entries = enemiesWithX2Mode.entrySet();
            for (Map.Entry<Long, Long> enemies : entries) {
                Long enemyId = enemies.getKey();
                Long time = enemies.getValue();
                boolean needRemove = System.currentTimeMillis() > (time + roundTimeX2);
                getLog().debug("need remove enemyId:  {}, needRemove: {}", enemyId, needRemove);
                if (needRemove) {
                    gameRoom.sendChanges(getTOFactoryService().createChangeEnemyModeMessage(enemyId, EnemyMode.X_1));
                    needRemoveEnemies.add(enemyId);
                    getMap().updateEnemyMode(enemyId, EnemyMode.X_1);
                    getLog().debug("remove enemyId: {}, updateEnemiesX2Mode enemiesWithX2Mode {}",
                            enemyId, enemiesWithX2Mode);
                }

                if (!needRemoveEnemies.isEmpty()) {
                    getLog().debug("needRemovedEnemies {}", needRemoveEnemies);
                    needRemoveEnemies.forEach(enemiesWithX2Mode::remove);
                }
            }
        } catch (InterruptedException e) {
            getLog().warn(" updateEnemiesX2Mode exception: ", e);
        } finally {
            if (lock) {
                lockShots.unlock();
            }
        }
    }

    protected void onTimerWithLock(boolean needClearEnemy) {
        getLog().debug("PlayGameState:: onTimer: current={}", this);
        getLog().debug("End round, aliveMummies: " + getMap().getItemsSize()
                + " needWaitingWhenEnemiesLeave: " + needWaitingWhenEnemiesLeave
                + " getRemainingBosses: " + getRemainingBosses()
                + " remainingNumberOfBoss: " + remainingNumberOfBoss
        );

        if (!needWaitingWhenEnemiesLeave) {
            needWaitingWhenEnemiesLeave = true;
            allowSpawn = false;
            allowSpawnHW = false;
            mainBossIsAvailable = false;
            remainingNumberOfBoss = 0;
            getRemainingBosses().clear();
            getMap().clearInactivityLiveItems();
            if (needClearEnemy)
                getMap().removeAllEnemies();
            sendLeaveTrajectories();
            gameRoom.sendChanges(getTOFactoryService().createRoundFinishSoon(System.currentTimeMillis()));
        }
    }

    boolean noAnyEnemiesInRound() {
        return getMap().noEnemiesInRoom() && getCountRemainingEnemiesByModel() == 0 && getRemainingBosses().isEmpty()
                && getMap().getNumberInactivityItems() == 0;
    }

    @Override
    public void nextSubRound() throws CommonException {
//        needWaitingWhenEnemiesLeave = false;
        GameMap map = gameRoom.getMap();
        switch (subround) {
            case BASE:
                if (mainBossIsAvailable && !getRemainingBosses().isEmpty()) {
                    map.setPossibleEnemies(EnemyRange.Boss);
                    firePlaySubroundFinished(false);
                    int bossSkinId = getRemainingBosses().get(0) + 1;
                    Integer bossForRoom = TestStandLocal.getInstance().getBossForRoom(gameRoom.getId());
                    if (bossForRoom != null && bossForRoom != -1) {
                        bossSkinId = bossForRoom;
                        getLog().debug("boss from teststand: {}", bossForRoom);
                    }

                    spawnBoss(bossSkinId);
                    timeOfStartBossRound = lastShotTime = System.currentTimeMillis();
                    getRemainingBosses().remove(0);
                } else {
                    doFinishWithLock();
                }
                break;
            case BOSS:
            default:
                doFinishWithLock();
                break;
        }
    }

    @Override
    public void doFinishWithLock() {
        lockShots.lock();
        try {
            getLog().debug("doFinishWithLock: getRemainingBosses(): {},  remainingNumberOfBoss: {}, timer: {}",
                    getRemainingBosses(), remainingNumberOfBoss, gameRoom.getTimerTime());
            finish();
        } catch (CommonException e) {
            getLog().error("Unexpected error", e);
        } finally {
            if (lockShots != null && lockShots.isLocked()) {
                lockShots.unlock();
            }
        }
    }

    @Override
    protected void spawnMummy() {
//        spawnEnemy(Mummies, -1, null, -1);
    }

    private boolean needFinalSteps() {
        return true;
    }

    private Long spawnEnemy(EnemyType enemyType, int skinId, Trajectory oldTrajectory,
                            long parentEnemyId, Enemy returnedEnemy) {
        long res = -1;

        try {
            IMathEnemy mathEnemy = getMap().createMathEnemy(enemyType);
            boolean needNearCenter = HORUS.getEnemies().contains(enemyType);
            getLog().debug("spawnEnemy, returnedEnemy: {}", returnedEnemy);
            Enemy enemy;
            if (returnedEnemy != null) {
                enemy = returnedEnemy;
                enemy.setTrajectory(getMap().getInitialTrajectory(enemy.getSpeed(), true, enemyType));
            } else {
                enemy = getMap().addEnemyByType(enemyType, mathEnemy, skinId, parentEnemyId, needNearCenter,
                        needFinalSteps(), false);
                enemy.setEnergy(mathEnemy.getFullEnergy());
            }

            enemy.setReturnTime(enemy.getTrajectory().getLastPoint().getTime() + respawnCommonDelay);
            enemy.setRespawnDelay(respawnCommonDelay);

            enemy.setShouldReturn(true);


            long startTime = enemy.getTrajectory().getPoints().get(0).getTime();
            long endTime = enemy.getTrajectory().getLastPoint().getTime();
            getLog().debug("spawnEnemy, new enemy: {}", enemy);
            getLog().debug("spawnEnemy, new startTime: {}, endTime: {}, diff {} ",
                    startTime, endTime, (endTime - startTime));
            res = enemy.getId();
            gameRoom.sendNewEnemyMessage(enemy);

            if (returnedEnemy != null)
                getMap().reEnterToMap(Collections.singletonList(returnedEnemy));

        } catch (Exception e) {
            getLog().debug("spawn enemy error , skinId: {}, oldTrajectory: {}", skinId, oldTrajectory);
            getLog().debug("spawn enemy error: ", e);
        }
        return res;
    }

    @Override
    protected void generateHVEnemy(ShootResult result, ShotMessages messages, String sessionId) {
    }


    @Override
    public void spawnEnemyFromTeststand(int typeId, int skinId, Trajectory trajectory, long parentEnemyId) {
        EnemyType enemyType = EnemyType.values()[typeId];
        Enemy enemy = getMap().addConcreteEnemy(enemyType, skinId, trajectory, gameRoom.getSeatsCount(),
                new MathEnemy(-1, EnemyType.values()[typeId].name(), 1, 1),
                parentEnemyId, false, needFinalSteps(), false);

        getLog().debug("spawnEnemyFromTeststand enemy: {}", enemy);
        gameRoom.sendNewEnemyMessage(enemy);
    }

    private void spawnBoss(int bossSkinId) {
        lockShots.lock();
        try {
            Integer healthBoss = MathData.getBossParams().get(bossSkinId - 1).second();
            MathEnemy mathEnemy = new MathEnemy(0, "", 0, healthBoss);
            Enemy enemy = getMap().addEnemyByTypeNew(EnemyType.Boss, mathEnemy, bossSkinId,
                    -1, false, needFinalSteps(), false);
            enemy.setEnergy(healthBoss);
            getLog().debug("spawnBoss, new enemy: {}, ", enemy);
            gameRoom.sendNewEnemyMessage(enemy);
        } finally {
            lockShots.unlock();
        }
    }

    @Override
    public void placeMineToMap(Seat seat, IMineCoordinates mineCoordinates) throws CommonException {
        sendError(seat, mineCoordinates, WRONG_WEAPON, "Wrong weapon", mineCoordinates);
    }

    @Override
    public void processShot(Seat seat, IShot shot, boolean isInternalShot) throws CommonException {
        long time = System.currentTimeMillis();
        final boolean paidSpecialShot = shot.isPaidSpecialShot();
        boolean lock = false;
        try {
            lock = lockShots.tryLock(3, TimeUnit.SECONDS);
            if (!lock) {
                sendError(seat, shot, WRONG_WEAPON, "get lock failed", shot);
                return;
            }

            int currentWeaponIdOld = seat.getCurrentWeaponId();

            String bulletId = shot.getBulletId();
            boolean isShotWithBulletId = !StringUtils.isTrimmedEmpty(bulletId);
            getLog().debug("processShot: aid: {},  shot:{},  isInternalShot: {}, " +
                            "ammo: {}, seat.getBetLevel(): {}, isShotWithBulletId: {}, currentWeaponIdOld: {}, bulletId: {}",
                    seat.getAccountId(),
                    shot, isInternalShot,
                    seat.getAmmoAmount(),
                    seat.getBetLevel(),
                    isShotWithBulletId, currentWeaponIdOld, bulletId);

            if (isShotWithBulletId) {
                Set<SeatBullet> bulletsOnMap = seat.getBulletsOnMap();
                getLog().debug("aid: {}, bulletsOnMap: {}", seat.getAccountId(), bulletsOnMap);
                if (bulletsOnMap.stream().noneMatch(seatBullet -> seatBullet.getBulletId().equals(bulletId))) {
                    sendError(seat, shot, BAD_REQUEST, "BulletId is not found", shot);
                    return;
                }
                if (shot.getWeaponId() != -1) {
                    sendError(seat, shot, BAD_REQUEST, "Shot with bulletId and wrong weaponId", shot);
                    return;
                }
            }

            if (!isInternalShot && paidSpecialShot && currentWeaponIdOld == -1) {
                sendError(seat, shot, BAD_REQUEST, "Wrong weapon mode", shot);
                return;
            }
            boolean isMine = shot.getWeaponId() == SpecialWeaponType.Landmines.getId();

            if (paidSpecialShot && getRoomInfo().getMoneyType().equals(MoneyType.FRB)) {
                sendError(seat, shot, BAD_REQUEST, "Wrong weapon mode, paid weapons is not allowed in FRB", shot);
                return;
            }

            boolean needRestoreWeapon = false;
            if (shot.getWeaponId() == SpecialWeaponType.Landmines.getId()) {
                if (!isInternalShot) {
                    sendError(seat, shot, WRONG_WEAPON, "Wrong weapon", shot);
                    return;
                }

                seat.setWeapon(SpecialWeaponType.Landmines.getId());
                needRestoreWeapon = currentWeaponIdOld != SpecialWeaponType.Landmines.getId();
            } else if (isShotWithBulletId) {
                needRestoreWeapon = true;
                seat.setWeapon(shot.getWeaponId());
            }

            seat.setLastWin(Money.ZERO);
            gameRoom.getSeats().forEach(AbstractSeat::resetShotTotalWin);

            boolean isPaidShot = shot.getWeaponId() == -1;
            boolean allowWeaponSaveInAllGames = seat.getPlayerInfo().isAllowWeaponSaveInAllGames();

            int multiplierPaidWeapons = seat.getBetLevel();
            if (paidSpecialShot) {
                SpecialWeaponType weaponType = SpecialWeaponType.values()[seat.getCurrentWeaponId()];
                Integer paidWeaponCost = MathData.getPaidWeaponCost(weaponType.getId());
                multiplierPaidWeapons *= paidWeaponCost;
                getLog().debug("paidWeaponCost:{}", paidWeaponCost);
            }

            long realStakeInCents = seat.getStake().toCents() * multiplierPaidWeapons;
            getLog().debug("multiplierPaidWeapons: {}, paidSpecialShot: {}, realStakeInCents:{}",
                    multiplierPaidWeapons, paidSpecialShot, realStakeInCents);

            PlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
            int numberOfKilledMissOld = roundInfo.getKilledMissedNumber(seat.getCurrentWeaponId());

            if (seat.getAmmoAmountTotalInRound() == 0 && seat.getPlayerInfo().getRoundBuyInAmount() == 0
                    && !allowWeaponSaveInAllGames && !isPaidShot) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets", shot);
            } else if (paidSpecialShot && seat.getAmmoAmount() < multiplierPaidWeapons && !isMine) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets", shot);
                getLog().debug("paidSpecialShot, seat.getAmmoAmount(): {} less then weapon cost: {}",
                        seat.getAmmoAmount(), multiplierPaidWeapons);
                return;
            } else if ((seat.getAmmoAmount() <= 0 || (seat.getAmmoAmount() - seat.getBetLevel() < 0)) && isPaidShot) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets", shot);
            } else if (shot.getRealWeaponId() != seat.getCurrentWeaponId()) {
                sendError(seat, shot, WRONG_WEAPON, "Wrong weapon", shot);
            } else {
                if (shot.getWeaponId() == REGULAR_WEAPON) {
                    seat.setActualShot(shot);
                    shootWithRegularWeapon(time, seat, shot);
                    roundInfo.addRealShotsCounter(seat.getCurrentWeaponId(), 1);
                    if (roundInfo.isShotSuccess(seat.getCurrentWeaponId(), numberOfKilledMissOld)) {
                        roundInfo.addKpiInfoPaidRegularShots(realStakeInCents);
                        roundInfo.addKpiInfoSWShotsCount(-1, 1, seat.getBetLevel(), false);
                    }
                } else if (isInternalShot || seat.getCurrentWeapon().getShots() > 0 || paidSpecialShot) {
                    if (shot.getWeaponId() == SpecialWeaponType.Cryogun.getId()) {
                        if (!needWaitingWhenEnemiesLeave) {
                            sendFreezeTrajectories(time, shot.getX(), shot.getY(), 280);
                        }
                    }
                    seat.setActualShot(shot);
                    int weaponId = seat.getCurrentWeaponId();
                    shootWithSpecialWeapon(time, seat, shot);
                    roundInfo.addRealShotsCounter(weaponId, 1);
                    if (roundInfo.isShotSuccess(weaponId, numberOfKilledMissOld)) {
                        roundInfo.addKpiInfoSWShotsCount(weaponId, 1, seat.getBetLevel(), !paidSpecialShot);
                        if (paidSpecialShot) {
                            roundInfo.addKpiInfoSwShots(realStakeInCents);
                        } else {
                            roundInfo.addKpiInfoFreeShotsCount(1);
                        }
                    }
                } else {
                    sendError(seat, shot, WRONG_WEAPON, "Weapon not found", shot);
                }
            }

            if (!needWaitingWhenEnemiesLeave)
                lastShotTime = time;

            if (needRestoreWeapon) {
                seat.setWeapon(currentWeaponIdOld);
            }

            gameRoom.getSeats().forEach(AbstractSeat::updateScoreShotTotalWin);

            getLog().debug("shot, end  getCountRemainingEnemiesByModel: {} , ammo: {}, allowSpawn: {} ",
                    getCountRemainingEnemiesByModel(), seat.getAmmoAmount(), allowSpawn);

        } catch (InterruptedException e) {
            getLog().warn(" shot exception: ", e);
        } finally {
            seat.setActualShot(null);
            if (lock) {
                lockShots.unlock();
            }
        }
    }

    protected void shootWithRegularWeapon(long time, Seat seat, IShot shot) throws CommonException {
        getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(), shot.getWeaponId(), 0);
        ShotMessages messages = new ShotMessages(seat, shot, gameRoom,
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0),
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0));
        List<ShootResult> results = new LinkedList<>();

        PointD locationOfBaseEnemy;
        long itemIdForShot = shot.getEnemyId();

        if (getMap().getItemById(itemIdForShot) != null) {
            Enemy enemy = getMap().getItemById(itemIdForShot);
            if (enemy.isInvulnerable(time)) {
                ShootResult shootResult = new ShootResult(seat.getStake(), Money.INVALID, false, false, enemy, true);
                ArrayList<ShootResult> shootResults = new ArrayList<>();
                shootResults.add(shootResult);
                processSingleShotResult(seat, shot, messages, shootResult, shootResults);
                getLog().debug("shootWithRegularWeapon base enemy {} is invulnerable", shot.getEnemyId());
            } else {
                locationOfBaseEnemy = enemy.getLocation(time);
                int enemyTypeId = enemy.getEnemyClass().getEnemyType().getId();
                getLog().debug("shootWithRegularWeapon Base enemy: {} enemyTypeId: {}", enemy, enemyTypeId);

                ShootResult result = shootWithRegularWeaponAndUpdateState(time, seat, shot.getEnemyId(), messages);
                result.setMainShot(true);
                results.add(result);

                processingExplodedShots(time, results, locationOfBaseEnemy, itemIdForShot, seat);

                processSingleShotResult(seat, shot, messages, result, results);
                seat.incrementBulletsFired();
            }
        } else {
            ShootResult shootResult = new ShootResult(seat.getStake(), Money.INVALID, false, false, null);
            ArrayList<ShootResult> shootResults = new ArrayList<>();
            shootResults.add(shootResult);
            processSingleShotResult(seat, shot, messages, shootResult, shootResults);
            getLog().debug("shootWithRegularWeapon base enemy {} was killed before", shot.getEnemyId());
        }
    }

    private void processSingleShotResult(Seat seat, IShot shot, ShotMessages messages, ShootResult result,
                                         List<ShootResult> results) throws CommonException {

        if (!result.isKilledMiss() && !result.isInvulnerable()) {
            int multiplierPaidWeapons = seat.getBetLevel();
            if (shot.isPaidSpecialShot())
                multiplierPaidWeapons = MathData.getPaidWeaponCost(shot.getWeaponId());

            getLog().debug("processSingleShotResult specialWeapon: {} multiplierPaidWeapons: {}",
                    shot.getWeaponId(), multiplierPaidWeapons);
            seat.decrementAmmoAmount(multiplierPaidWeapons);
        }

        AtomicInteger cnt = new AtomicInteger(results.size());
        results.forEach(shootResult -> {
            boolean isLastResult = cnt.decrementAndGet() == 0;
            int awardedWeaponId = !result.isNewWeapon() ? -1 : result.getWeapon().getType().getId();
            getLog().debug("----------processSingleShotResult " + " isLastResult: {}  cnt: {} shootResult: {}",
                    isLastResult, cnt, shootResult);
            processShootResult(seat, shot, shootResult, messages, awardedWeaponId, isLastResult);
        });

        List<Seat> seats = gameRoom.getSeats();
        for (Seat seatCurrent : seats) {
            seatCurrent.transferWinToAmmo();
        }

        if ((result.isShotToBoss() && result.isDestroyed())) {
            subround = PlaySubround.BASE;
        }
        messages.send(shot);
    }

    private void spawnEnemyWithClones(int swarmTypeId, int liveSwarmCounts, int removedSwarmCounts, boolean maxReached) {
        try {


            int numberEnemiesInGroup;
            boolean isOwl = SwarmType.OWL.getTypeId() == swarmTypeId;
            boolean isEvils = SwarmType.EVIL_SPIRIT.getTypeId() == swarmTypeId;
            boolean isLizard = SwarmType.LIZARD_MAN.getTypeId() == swarmTypeId;
            boolean isEvilLizard = SwarmType.EVIL_SPIRIT_LIZARD_MAN.getTypeId() == swarmTypeId;
            boolean isEvilsLine = SwarmType.EVIL_SPIRIT_LINE.getTypeId() == swarmTypeId;
            boolean isLizardLine = SwarmType.LIZARD_MAN_LINE.getTypeId() == swarmTypeId;
            boolean isPhoenix = SwarmType.PHOENIX_LANTERN.getTypeId() == swarmTypeId;
            boolean isDragonFlyMode_1 = SwarmType.DRAGON_FLY_MODE_1.getTypeId() == swarmTypeId;
            boolean isDragonFlyMode_2 = SwarmType.DRAGON_FLY_MODE_2.getTypeId() == swarmTypeId;
            boolean isBeetleSwarm1 = SwarmType.BEETLE_SWARM_1.getTypeId() == swarmTypeId;
            boolean isBeetleSwarm2 = SwarmType.BEETLE_SWARM_2.getTypeId() == swarmTypeId;


            int groupStartTime = 0;
            int radius = 5;
            int minAngle = 45;
            long cloneEnemyTimeShift = 0;

            EnemyType firstEnemyType;
            List<EnemyType> possibleEnemyTypes = new ArrayList<>();
            List<Enemy> preReturnedEnemies = getMap().getPreReturnedEnemies(swarmTypeId);
            boolean needReuseSwarm = !preReturnedEnemies.isEmpty();

            Enemy firstReUseSwarmEnemy = null;
            List<Enemy> swarmReusedEnemies = new ArrayList<>();
            if (needReuseSwarm) {
                preReturnedEnemies.forEach(enemy -> getLog().debug("preReturnedEnemies, enemyId: {}, type: {}",
                        enemy.getId(), enemy.getEnemyClass().getEnemyType().getName()));
                Optional<Enemy> first = preReturnedEnemies.stream().filter(enemy -> enemy.getId() == enemy.getParentEnemyId()).findFirst();
                if (first.isPresent()) {
                    firstReUseSwarmEnemy = first.get();
                    long firstReUseSwarmEnemyId = firstReUseSwarmEnemy.getId();
                    swarmReusedEnemies = preReturnedEnemies.stream().filter(enemy -> enemy.getId() != firstReUseSwarmEnemyId).
                            collect(Collectors.toList());
                } else {
                    getMap().clearRemovedEnemies(preReturnedEnemies);
                    getLog().debug("clear preReturnedEnemies, because first enemy not found");
                    needReuseSwarm = false;
                }
            }

            getLog().debug("firstReUseSwarmEnemy: {}, needReuseSwarm: {}, swarmReusedEnemies.size: {}, removedSwarmCounts: {}, liveSwarmCounts:{}",
                    firstReUseSwarmEnemy,
                    needReuseSwarm,
                    swarmReusedEnemies.size(),
                    removedSwarmCounts, liveSwarmCounts);

            if (maxReached && (removedSwarmCounts == 0 || (removedSwarmCounts > 0 && !needReuseSwarm))) {
                return;
            }

            double deltaSpeed = 0;

            if (isOwl) {
                firstEnemyType = EnemyType.Owl;
                possibleEnemyTypes.add(EnemyType.Owl);
                numberEnemiesInGroup = needReuseSwarm ? swarmReusedEnemies.size() : 2;
                minAngle = 60;
                radius = 8;
                groupStartTime = 300 + RNG.nextInt(400);
                cloneEnemyTimeShift = groupStartTime;
            } else if (isEvils) {
                firstEnemyType = getRandomEnemy(Evils);
                possibleEnemyTypes.addAll(getRandomTypeFromRangeWithoutTypes(Evils, Collections.singletonList(firstEnemyType)));
                numberEnemiesInGroup = needReuseSwarm ? swarmReusedEnemies.size() : RNG.nextInt(3) + 4;
                radius = 4;
            } else if (isLizard) {
                firstEnemyType = getRandomEnemy(Lizards);
                possibleEnemyTypes.add(firstEnemyType);
                numberEnemiesInGroup = needReuseSwarm ? swarmReusedEnemies.size() : RNG.nextInt(2) + 1;
            } else if (isEvilsLine) {
                firstEnemyType = getRandomEnemy(Evils);
                possibleEnemyTypes.addAll(getRandomTypeFromRangeWithoutTypes(Evils, Collections.singletonList(firstEnemyType)));
                numberEnemiesInGroup = needReuseSwarm ? swarmReusedEnemies.size() : 4;
                radius = 1;
                cloneEnemyTimeShift = 800;
                groupStartTime = 700;
            } else if (isLizardLine) {
                firstEnemyType = getRandomEnemy(Lizards);
                possibleEnemyTypes.add(firstEnemyType);
                numberEnemiesInGroup = needReuseSwarm ? swarmReusedEnemies.size() : 2;
                radius = 1;
                cloneEnemyTimeShift = 1200;
                groupStartTime = 1200;
            } else if (isEvilLizard) {
                firstEnemyType = getRandomEnemy(Evils);
                possibleEnemyTypes.addAll(Evils.getEnemies());
                numberEnemiesInGroup = needReuseSwarm ? swarmReusedEnemies.size() : 6;
                radius = 6;
                minAngle = 60;
            } else if (isPhoenix) {
                firstEnemyType = EnemyType.Phoenix;
                possibleEnemyTypes.add(EnemyType.Lantern);
                numberEnemiesInGroup = needReuseSwarm ? swarmReusedEnemies.size() : 6;
                radius = 12;
                minAngle = 60;
            } else if (isDragonFlyMode_1) {
                firstEnemyType = EnemyType.Dragonfly_Green;
                possibleEnemyTypes.add(firstEnemyType);
                numberEnemiesInGroup = needReuseSwarm ? swarmReusedEnemies.size() : 5;
                radius = 2;
                groupStartTime = 300 + RNG.nextInt(70);
                cloneEnemyTimeShift = 300 + RNG.nextInt(70);
                minAngle = 45;
                deltaSpeed = 7;
            } else if (isDragonFlyMode_2) {
                firstEnemyType = RNG.nextBoolean() ? EnemyType.Dragonfly_Red : EnemyType.Dragonfly_Green;
                possibleEnemyTypes.add(firstEnemyType);
                numberEnemiesInGroup = needReuseSwarm ? swarmReusedEnemies.size() : 5;
                radius = 4;
                groupStartTime = 300 + RNG.nextInt(70);
                cloneEnemyTimeShift = 50 + RNG.nextInt(20);
                minAngle = 60;
                deltaSpeed = 7;
            } else if (isBeetleSwarm1) {
                firstEnemyType = EnemyType.Beetle_1;
                possibleEnemyTypes.add(EnemyType.Beetle_1);
                numberEnemiesInGroup = needReuseSwarm ? swarmReusedEnemies.size() : RNG.nextInt(3) + 8;
                radius = 3;
            } else if (isBeetleSwarm2) {
                firstEnemyType = EnemyType.Beetle_2;
                possibleEnemyTypes.add(EnemyType.Beetle_2);
                numberEnemiesInGroup = needReuseSwarm ? swarmReusedEnemies.size() : RNG.nextInt(3) + 8;
                radius = 3;
            } else
                return;

            List<Enemy> listEnemiesInGroup = new ArrayList<>();

            Enemy firstEnemy;

            if (!needReuseSwarm) {
                firstEnemy = getMap().addEnemyByTypeNew(firstEnemyType, getMap().createMathEnemy(firstEnemyType), 1,
                        -1, false, needFinalSteps(), false);
                firstEnemy.setEnergy(firstEnemy.getFullEnergy());
            } else {
                firstEnemy = firstReUseSwarmEnemy;
                EnemyType enemyType = firstEnemy.getEnemyClass().getEnemyType();
                ISkin skin = enemyType.getSkin(1);
                float speed = skin.getSpeed() + skin.getSpeedDeltaPositive();
                Trajectory initialTrajectory = getMap().getInitialTrajectory(speed, true, enemyType);
                firstEnemy.setTrajectory(initialTrajectory);
            }

            firstEnemy.setParentEnemyId(firstEnemy.getId());
            firstEnemy.setShouldReturn(true);
            firstEnemy.setReturnTime(firstEnemy.getTrajectory().getLastPoint().getTime() + respawnCommonDelay);
            firstEnemy.setRespawnDelay(respawnCommonDelay);
            listEnemiesInGroup.add(firstEnemy);

            if (isEvilsLine || isLizardLine) {
                EnemyType enemyTypeForTrajectory = EnemyType.Evil_Spirit;
                if (isLizardLine)
                    enemyTypeForTrajectory = EnemyType.Lizard;

                Pair<Integer, Trajectory> trajectoryWithSaveStartPosition =
                        getMap().getTrajectoryWithSaveStartPosition(enemyTypeForTrajectory,
                                (float) firstEnemy.getTrajectory().getSpeed(),
                                false, false, 1, true,
                                enemyTypeForTrajectory.getId());
                firstEnemy.setTrajectory(trajectoryWithSaveStartPosition.getValue());
                firstEnemy.setCurrentTrajectoryId(trajectoryWithSaveStartPosition.getKey());
                getLog().debug("trajectoryWithSaveStartPosition.getValue(): " + trajectoryWithSaveStartPosition.getValue());
            }

            Trajectory baseTrajectory = firstEnemy.getTrajectory();
            if (!isOwl)
                firstEnemy.setSpeed(baseTrajectory.getSpeed());

            long spawnTime = baseTrajectory.getPoints().get(0).getTime();
            List<Triple<PointD, Double, Integer>> enemyOffsets = new ArrayList<>();
            if (numberEnemiesInGroup > 0) {
                enemyOffsets = getMap().getRadialOffsetsWithAngles(radius, numberEnemiesInGroup, minAngle);
            }

            getLog().debug("swarmTypeId: " + swarmTypeId + "  firstEnemy: " + firstEnemy);
            getLog().debug("firstEnemy.getSpeed(): {}", firstEnemy.getSpeed());
            getLog().debug(" enemyOffsets: " + enemyOffsets);

            for (int i = 0; i < numberEnemiesInGroup; i++) {
                Enemy additionalEnemy;
                if (!needReuseSwarm) {
                    EnemyType enemyType = possibleEnemyTypes.get(RNG.nextInt(possibleEnemyTypes.size()));
                    additionalEnemy = getMap().addEnemyByTypeNew(enemyType, getMap().createMathEnemy(enemyType), 1,
                            firstEnemy.getParentEnemyId(), false, needFinalSteps(), false);
                    additionalEnemy.setEnergy(additionalEnemy.getFullEnergy());
                } else {
                    additionalEnemy = swarmReusedEnemies.get(i);
                }

                Trajectory trajectory;

                long realSpawnTime = spawnTime + (cloneEnemyTimeShift * i);
                getLog().debug("realSpawnTime: {}, originalPoints.get(0).getTime(): {}," +
                                " cloneEnemyTimeShift:{}, groupStartTime:{}",
                        realSpawnTime, spawnTime, cloneEnemyTimeShift, groupStartTime);

                if (isPhoenix) {
                    trajectory = TrajectoryUtils.generateSimilarTrajectoryWithCircle(baseTrajectory,
                            enemyOffsets.get(i), firstEnemy.getSpeed(),
                            realSpawnTime, groupStartTime);
                    List<Point> points = trajectory.getPoints();
                    List<Point> newPoints = new ArrayList<>();
                    for (int j = 1; j < points.size() - 1; j++) {
                        newPoints.add(points.get(j));
                    }
                    trajectory = new Trajectory(trajectory.getSpeed(), newPoints);
                } else {
                    boolean isDragonFly = isDragonFlyMode_1 || isDragonFlyMode_2;

                    trajectory = TrajectoryUtils.generateSimilarTrajectory(baseTrajectory,
                            enemyOffsets.get(i).first().x, enemyOffsets.get(i).first().y, 0, 0,
                            firstEnemy.getSpeed(), deltaSpeed, realSpawnTime, groupStartTime, true);

                    if (isDragonFly) {
                        List<Point> points = trajectory.getPoints();
                        List<Point> newPoints = new ArrayList<>();
                        long shift = 0;
                        for (int j = 0; j < points.size(); j++) {
                            Point point = points.get(j);
                            if (j == 2 || j == 4 || j == 6) {
                                shift += 1000;
                            }
                            newPoints.add(new Point(point.getX(), point.getY(), point.getTime() + shift));
                        }
                        trajectory = new Trajectory(trajectory.getSpeed(), newPoints);
                    }
                }

                getLog().debug("additional enemy i: {} trajectory: {} ", i, trajectory);
                additionalEnemy.setTrajectory(trajectory);
                additionalEnemy.setSpeed(firstEnemy.getSpeed());
                additionalEnemy.setShouldReturn(true);
                additionalEnemy.setReturnTime(additionalEnemy.getTrajectory().getLastPoint().getTime()
                        + respawnCommonDelay);
                additionalEnemy.setRespawnDelay(respawnCommonDelay);
                listEnemiesInGroup.add(additionalEnemy);
            }

            if (!needReuseSwarm) {
                int swarmId = getMap().generateSwarmId();
                for (Enemy enemy : listEnemiesInGroup) {
                    enemy.addToSwarm(swarmTypeId, swarmId);
                }
                getMap().registerSwarm(swarmId, listEnemiesInGroup);
            } else {
                getMap().reEnterToMap(preReturnedEnemies);
            }
            gameRoom.sendNewEnemiesMessage(listEnemiesInGroup);
        } catch (Exception e) {
            getLog().debug("spawnEnemyWithClones error: ", e);
        }
    }

    protected ShootResult shootWithRegularWeaponAndUpdateState(long time, Seat seat, Long itemIdForShot, ShotMessages messages)
            throws CommonException {
        ShootResult result = shootToOneEnemy(time, seat, itemIdForShot, seat.getCurrentWeaponId(), false,
                1);

        getLog().debug("shootResult: {}", result);
        getLog().debug("allowSpawnHW: {}, shootResult: {}", allowSpawn, result);
        return result;
    }


    @Override
    protected List<ShootResult> shootWithSpecialWeaponAndUpdateState(long time, Seat seat, IShot shot, int weaponId,
                                                                     ShotMessages messages) throws CommonException {
        List<ShootResult> results = new LinkedList<>();
        GameMap map = getMap();
        Long itemIdForShot = shot.getEnemyId();

        boolean paidSpecialShot = shot.isPaidSpecialShot();

        int liveEnemies = map.getItemsSize();

        if (liveEnemies == 0) {
            results.add(new ShootResult(seat.getStake(), Money.INVALID, false, false, null));
            getLog().debug("no live enemies, return kill Miss: message");
            return results;
        }

        double numberDamages = MathData.getRandomDamageForWeapon(weaponId);

        PointD locationOfBaseEnemy;
        if (map.getItemById(itemIdForShot) != null) {
            Enemy enemy = map.getItemById(itemIdForShot);
            if (enemy.isInvulnerable(time)) {
                getLog().debug("Base enemy is invulnerable");
                return Collections.singletonList(new ShootResult(seat.getStake(), Money.INVALID, false, false, enemy, true));
            }
            locationOfBaseEnemy = enemy.getLocation(time);
            int enemyTypeId = enemy.getEnemyClass().getEnemyType().getId();
            getLog().debug("Base enemy: {} enemyTypeId: {}", enemy, enemyTypeId);
        } else {
            // killed earlier
            getLog().debug("Base enemy: was killed before");
            return Collections.singletonList(new ShootResult(seat.getStake(),
                    Money.INVALID, false, false, null));
        }

        if (liveEnemies > numberDamages) liveEnemies = (int) numberDamages;

        Map<Long, Double> nNearestEnemies = map.getNNearestEnemies(time, locationOfBaseEnemy, itemIdForShot, liveEnemies);
        getLog().debug("numberDamages:  {} nNearestEnemies: {}", numberDamages, nNearestEnemies);

        if (nNearestEnemies.size() == 0) {
            getLog().debug("no enemies for shooting");
            return Collections.singletonList(new ShootResult(seat.getStake(),
                    Money.INVALID, false, false, null));
        }

        if (nNearestEnemies.size() != liveEnemies) {
            getLog().debug("possible error logic, nNearestEnemies.size():" + nNearestEnemies.size()
                    + " damages.size(): " + (int) numberDamages + " liveEnemies: " + liveEnemies);
        }

        int realNumberOfShots = 0;
        for (Long enemyId : nNearestEnemies.keySet()) {
            ShootResult res = shootToOneEnemy(time, seat, enemyId, weaponId, false, 1);
            boolean killedMissShot = res.getWin().equals(Money.INVALID);
            if (enemyId.equals(itemIdForShot)) {
                res.setMainShot(true);
                if (killedMissShot) {
                    getLog().debug("shootWithSpecialWeaponAndUpdateState error, " +
                            "found base killed mis shot: {},", res);
                    return Collections.singletonList(new ShootResult(seat.getStake(),
                            Money.INVALID, false, false, null));
                }
            }
            results.add(res);
            if (!killedMissShot) {
                realNumberOfShots++;
            }
        }

        getLog().debug("realNumberOfShots: {}, numberDamages: {}", realNumberOfShots, numberDamages);
        if (realNumberOfShots < numberDamages) {
            int cnt = 100;
            while (cnt-- > 0) {
                Long nearestEnemy = map.getAllNearestEnemy(time, locationOfBaseEnemy,
                        false, itemIdForShot, null);
                if (nearestEnemy == null) {
                    getLog().debug("account: {}, No enemies for shooting from SW", seat.getAccountId());
                    break;
                }
                ShootResult res = shootToOneEnemy(time, seat, nearestEnemy, weaponId, false, 1);
                results.add(res);
                if (!res.getWin().equals(Money.INVALID))
                    realNumberOfShots++;

                if (realNumberOfShots == numberDamages) {
                    getLog().debug("account: {}, all damages is made from SW", seat.getAccountId());
                    break;
                }
            }
            boolean isBonusSession = getRoomInfo().getMoneyType().equals(MoneyType.CASHBONUS) ||
                    getRoomInfo().getMoneyType().equals(MoneyType.FRB) || getRoomInfo().getMoneyType().equals(MoneyType.TOURNAMENT);
            getLog().debug("before compensation " + "realNumberOfShots: {}, numberDamages: {}, isBonusSession: {}",
                    realNumberOfShots, numberDamages, isBonusSession);
            if (!isBonusSession)
                makeCompensationForPoorPlaying(seat, weaponId, numberDamages, realNumberOfShots, shot.isPaidSpecialShot());
        }

        processingExplodedShots(time, results, locationOfBaseEnemy, itemIdForShot, seat);

        Optional<ShootResult> first = results.stream().filter(shootResult ->
                shootResult.getEnemy() != null && (shootResult.getEnemy().getId() == itemIdForShot)).findFirst();

        if (!first.isPresent()) {
            AtomicBoolean foundWin = new AtomicBoolean(false);
            results.forEach(shootResult -> {
                if (shootResult.getWin().greaterThan(Money.ZERO) || !shootResult.getAwardedWeapons().isEmpty() ||
                        !shootResult.getPrize().isEmpty()) {
                    foundWin.set(true);
                }
            });
            if (foundWin.get()) {
                getLog().debug(" found shot results  without main enemy, shot:  {}, results: {} ", shot, results);
            }
            return results;
        } else {
            ShootResult mainShootResult = first.get();
            if (weaponId != SpecialWeaponType.Landmines.getId()) {
                if (paidSpecialShot) {
                    int multiplierPaidWeapons;
                    multiplierPaidWeapons = MathData.getPaidWeaponCost(shot.getWeaponId());
                    int totalAmmoOfBet = multiplierPaidWeapons * seat.getBetLevel();
                    seat.decrementAmmoAmount(totalAmmoOfBet);
                    getLog().debug("decrementAmmoAmount Base enemy: " + " shot.getWeaponId(): " + shot.getWeaponId()
                            + " multiplierPaidWeapons: " + multiplierPaidWeapons);
                } else {
                    seat.consumeSpecialWeapon(weaponId);
                }
            }

            getLog().debug("shootResult: ");
            for (ShootResult shootResult : results) {
                getLog().debug(shootResult);
            }

            getLog().debug("allowSpawnHW: {}", allowSpawnHW);
            if (mainShootResult.isNeedGenerateHVEnemy() && allowSpawnHW) {
                generateHVEnemy(mainShootResult, messages, seat.getPlayerInfo().getSessionId());
            }
        }

        return results;
    }

    private void processingExplodedShots(long time, List<ShootResult> results, PointD locationOfBaseEnemy,
                                         Long itemIdForShot, Seat seat) throws CommonException {
        Set<ShootResult> shootResults = results.stream().filter(shootResult -> shootResult.getNeedExplodeHP() > 0)
                .collect(Collectors.toSet());
        getLog().debug("processingExplodedShots, start");

        for (ShootResult shootResult : shootResults) {
            getLog().debug("processingExplodedShots, shootResult.getEnemy(): {} ", shootResult.getEnemy());
            if (shootResult.getEnemy() != null) {
                IEnemyType enemyType = shootResult.getEnemy().getEnemyClass().getEnemyType();
                boolean isx2Enemy = enemyType.equals(EnemyType.Spirits_2_ORANGE);
                int numberEnemies = enemyType.equals(EnemyType.Spirits_5_VIOLETT) ? 4 : 2;
                Map<Long, Double> nNearestEnemies = getMap().getNNearestEnemies(time, locationOfBaseEnemy, itemIdForShot,
                        numberEnemies, BaseEnemiesWithoutSpirits);

                Money killAward = shootResult.getKillAwardWin();
                Money addWin = Money.ZERO;
                int needExplodeHP = shootResult.getNeedExplodeHP();

                getLog().debug("processingExplodedShots, shootResult: {}, nNearestEnemies: {} "
                        + " shootResult.getNeedExplodeHP(): {} ", shootResult, nNearestEnemies, needExplodeHP);

                if (nNearestEnemies.size() > 0) {
                    int[] damages = MathData.getDistributionByEnemy(needExplodeHP, nNearestEnemies.size());
                    getLog().debug("processingExplodedShots, nNearestEnemies.size(): {}, damages: {}",
                            nNearestEnemies.size(), Arrays.toString(damages));

                    int cnt = 0;
                    for (Map.Entry<Long, Double> enemyPair : nNearestEnemies.entrySet()) {
                        Long enemyId = enemyPair.getKey();
                        ShootResult res = shootToOneEnemyExplode(seat, enemyId, damages[cnt++]);
                        if (!res.getWin().equals(Money.INVALID)) {
                            res.setExplode(true);
                            results.add(res);
                        }
                    }
                    getLog().debug("results after processingExplodedShots: {}", results);
                } else {
                    addWin = seat.getStake().getWithMultiplier(needExplodeHP * MathData.PAY_HIT_PERCENT * seat.getBetLevel());
                    getLog().debug("no enemies, return average win processingExplodedShots: results : {}, " +
                                    "addWin {}, needExplodeHP: {}, seat.getBetLevel(): {}",
                            results, addWin, needExplodeHP, seat.getBetLevel());
                }

                if (isx2Enemy) {
                    Map<Long, Double> nNearestEnemiesForX2 = getMap().getNNearestEnemies(time, locationOfBaseEnemy, itemIdForShot,
                            2, EnemyRange.BaseEnemiesWithoutSpirits, 7000);
                    if (!nNearestEnemiesForX2.isEmpty()) {
                        Map.Entry<Long, Double> entry = nNearestEnemiesForX2.entrySet().iterator().next();
                        Long nearestEnemy = entry.getKey();
                        getEnemiesWithX2Mode().put(nearestEnemy, time);
                        getMap().updateEnemyMode(nearestEnemy, EnemyMode.X_2);
                        getLog().debug("enemiesWithX2Mode: {} updated, nearestEnemy: {} ", getEnemiesWithX2Mode(), nearestEnemy);
                        shootResult.setEnemiesWithUpdatedMode(Collections.singletonList(getTOFactoryService().
                                createGameEnemyMode(nearestEnemy, EnemyMode.X_2.ordinal())));
                    }
                }

                killAward = killAward.add(addWin);
                getLog().debug("exploder compensate win: {}, new Win: {} ", addWin, killAward);
                shootResult.setKillAwardWin(killAward);
            } else {
                getLog().debug("exploder enemyId {} is not found ", shootResult.getEnemyId());
            }
        }

    }

    private void makeCompensationForPoorPlaying(Seat seat, int weaponId, double numberDamages, int realNumberOfShots, boolean isPaidMode) {
        if (realNumberOfShots < numberDamages) {
            getLog().debug("realNumberOfShots:{} less then numberDamages: {}, need compensate:", realNumberOfShots, numberDamages);
            List<IWeaponSurplus> weaponSurplus = seat.getWeaponSurplus();
            getLog().debug("weaponSurplus before:{} ", weaponSurplus);
            int lostHits = (int) (numberDamages - realNumberOfShots);
            double rtpForWeapon = isPaidMode ? MathData.getFullRtpForWeapon(weaponId) / 100
                    : MathData.getRtpForWeapon(weaponId) / 100;
            Money newCompensation = Money.ZERO;
            newCompensation = newCompensation.add(seat.getStake().multiply(rtpForWeapon * lostHits * seat.getBetLevel()));
            seat.getCurrentPlayerRoundInfo().addCompensateHitsCounter(weaponId, lostHits);
            getLog().debug("lostHits :{}, newCompensation: {} ", lostHits, newCompensation);
            EnemyGame.updateWeaponSurplus(weaponId, newCompensation, weaponSurplus, getTOFactoryService());
            getLog().debug("weaponSurplus after:{} ", weaponSurplus);
        }
    }

    @Override
    protected void processShootResult(Seat seat, IShot shot, IShootResult result, ShotMessages messages,
                                      int awardedWeaponId, boolean isLastResult) {

        long enemyId = result.getEnemy() == null ? shot.getEnemyId() : result.getEnemyId();
        getLog().debug("processing result: {}, enemyId: {}", result, enemyId);

        int realShotTypeId = shot.getWeaponId();
        int usedSpecialWeapon = shot.getWeaponId();
        seat.getCurrentPlayerRoundInfo().updateAdditionalData(currentModel);

        IExperience score = collectScore(result, seat);
        seat.addTotalKillsXP(score.getAmount() / 10);
        double diffScore = seat.addScore(score.getAmount());
        checkLevelUp(seat, messages);

        int betLevel = seat.getBetLevel();
        int newShots = 0;
        if (result.getWeapon() != null) {
            newShots = result.getWeapon().getShots();
        }

        boolean mainShot = result.isMainShot();
        getLog().debug("result.isMainShot(): {}", mainShot);

        boolean isSpecialWeapon = shot.getWeaponId() != -1;
        boolean isPrize = !result.getPrize().isEmpty();
        boolean isWin = result.getWin().greaterThan(Money.ZERO);
        boolean isWeapon = awardedWeaponId != -1 || result.getNewFreeShotsCount() > 0;
        boolean isBossWin = result.isShotToBoss()
                && (result.getWin().greaterThan(Money.ZERO));

        boolean isWheelWin = result.getMoneyWheelWin().greaterThan(Money.ZERO);

        boolean isAwardedWeapons = !result.getAwardedWeapons().isEmpty();
        boolean isHit = isPrize || isWin || isWeapon || isBossWin || isAwardedWeapons || isWheelWin;
        getLog().debug("isHit : {}, isPrize: {}, isWin: {}, isWeapon: {}, isBossWin: {}, " +
                        "isAwardedWeapons: {}, isWheelWin: {}",
                isHit, isPrize, isWin, isWeapon, isBossWin, isAwardedWeapons, isWheelWin);

        Map<Integer, List<IWinPrize>> hitResultBySeats = new HashMap<>();
        Map<Seat, IHit> messagesForSeatsLocal = new HashMap<>();
        IHit hitOwn = null;
        List<IHit> hitsForObserversLocal = new ArrayList<>();

        Money stake = (isSpecialWeapon || !mainShot) ? Money.ZERO : seat.getStake().getWithMultiplier(seat.getBetLevel());

        Money paidStake = Money.ZERO;
        boolean isPaidShotToBaseEnemy = shot.isPaidSpecialShot() && mainShot;
        if (isPaidShotToBaseEnemy) {
            stake = seat.getStake().getWithMultiplier(MathData.getPaidWeaponCost(shot.getWeaponId()) * seat.getBetLevel());
            paidStake = new Money(stake.getValue());
        }

        getLog().debug("real stake: {}, isPaidShotToBaseEnemy: {}", stake.toDoubleCents(), isPaidShotToBaseEnemy);

        List<IEnemyMode> enemiesWithUpdatedMode = result.getEnemiesWithUpdatedMode();

        if (result.isKilledMiss() || result.isInvulnerable()) {
            seat.incrementMissCount();
            seat.getCurrentPlayerRoundInfo().addKilledMissCounter(shot.getWeaponId(), 1);
            if (isPaidShotToBaseEnemy) {
                getLog().debug("found main result of killedMiss, result: {}", result);
            }

            messages.add(getTOFactoryService().createMiss(getCurrentTime(), TObject.SERVER_RID, seat.getNumber(),
                    result.isKilledMiss(), awardedWeaponId, enemyId, usedSpecialWeapon, seat.getSpecialWeaponRemaining(),
                    diffScore, isLastResult, shot.getX(), shot.getY(), newShots, result.getMineId(), shot.getEnemyId(),
                    result.isInvulnerable(), seat.getBetLevel(), shot.getBulletId()),
                    getTOFactoryService().createMiss(getCurrentTime(), shot.getRid(), seat.getNumber(), result.isKilledMiss(),
                            awardedWeaponId, enemyId, usedSpecialWeapon, seat.getSpecialWeaponRemaining(), diffScore,
                            isLastResult, shot.getX(), shot.getY(), newShots, result.getMineId(), shot.getEnemyId(),
                            result.isInvulnerable(), seat.getBetLevel(), shot.getBulletId()));


        } else {
            IEnemyClass enemyClass = result.getEnemy().getEnemyClass();
            IEnemyType enemyType = enemyClass.getEnemyType();
            String enemyNameKey = enemyType.getId() + "_" + enemyType.getName() + "_" + seat.getBetLevel();

            if (isHit) {
                seat.getCurrentPlayerRoundInfo().addHitCounter(seat.getCurrentWeaponId(), 1);
                seat.incrementHitsCount();
                IRoomEnemy enemy = gameRoom.convert((Enemy) result.getEnemy(), false);
                String title = isSpecialWeapon ? SpecialWeaponType.values()[realShotTypeId].getTitle() : null;

                if (result.isDestroyed())
                    incCurrentKilledEnemies();

                Money commonWin = result.getWin();

                List<Seat> seats = gameRoom.getSeats();
                Money totalSimpleWin = Money.ZERO;
                Money killAwardWin = result.getKillAwardWin();

                for (Seat seatCurrent : seats) {
                    if (seatCurrent != null) {
                        boolean isOwner = seat.getAccountId() == seatCurrent.getAccountId();
                        List<Pair<Integer, Money>> additionalWins = result.getAdditionalWins();
                        Money enemyWinForSeat = Money.ZERO;

                        if (isOwner) {
                            PlayerRoundInfo currentPlayerRoundInfo = seatCurrent.getCurrentPlayerRoundInfo();

                            enemyWinForSeat = enemyWinForSeat.add(commonWin);
                            Money moneyWheelWin = result.getMoneyWheelWin();
                            if (moneyWheelWin.greaterThan(Money.ZERO)) {
                                getLog().debug("moneyWheelWin: {}", moneyWheelWin);
                                currentPlayerRoundInfo.updateAdditionalWin("wheelWin", moneyWheelWin);
                                currentPlayerRoundInfo.addMoneyWheelCompleted(1);
                                currentPlayerRoundInfo.addMoneyWheelPayouts((long) moneyWheelWin.toDoubleCents());
                                seatCurrent.incrementRoundWin(moneyWheelWin);
                                seatCurrent.incrementShotTotalWin(moneyWheelWin);
                                seatCurrent.addLastWin(moneyWheelWin);
                            }
                            getLog().debug("enemy shared Win: seat accountId: " + seatCurrent.getAccountId()
                                    + " enemyWinForSeat: " + enemyWinForSeat
                                    + " seatId: " + seatCurrent.getId()
                                    + " additionalWins: " + additionalWins
                            );
                            seatCurrent.incrementRoundWin(enemyWinForSeat);
                            seatCurrent.incrementShotTotalWin(enemyWinForSeat);
                            seatCurrent.addLastWin(enemyWinForSeat);
                            totalSimpleWin = totalSimpleWin.add(enemyWinForSeat);


                            if (killAwardWin.greaterThan(Money.ZERO)) {
                                getLog().debug("  KillAwardWin: {}", killAwardWin);
                                currentPlayerRoundInfo.updateAdditionalWin("KillAwardWin", killAwardWin);
                                seatCurrent.incrementRoundWin(killAwardWin);
                                seatCurrent.incrementShotTotalWin(killAwardWin);
                                seatCurrent.addLastWin(killAwardWin);
                                totalSimpleWin = totalSimpleWin.add(killAwardWin);
                            }
                        }

                        if (result.isDestroyed()) {
                            seatCurrent.removeDamageForEnemyId(enemyId);
                        }

                        int realAwardedWeaponId = isOwner ? awardedWeaponId : -1;
                        int remainingSWShots = seatCurrent.getSpecialWeaponRemaining();
                        int realNewShots = isOwner ? newShots : 0;

                        getLog().debug("aid current: {}, aid of shooter current: {}, " +
                                        "realAwardedWeaponId: {}, remainingSWShots: {},  realNewShots: {}",
                                seatCurrent.getAccountId(), seat.getAccountId(), realAwardedWeaponId,
                                remainingSWShots, realNewShots);

                        if (isOwner) {
                            addWeaponToSeat(seat, result);
                            Money win = new Money(enemyWinForSeat.getValue());
                            List<Pair<Integer, Money>> additionalWin = new ArrayList<>(result.getAdditionalWins());
                            if (result.getMoneyWheelWin().greaterThan(Money.ZERO)) {
                                additionalWin.add(new Pair<>(4, result.getMoneyWheelWin()));
                            }

                            updateHitResultBySeats(seat.getNumber(), totalSimpleWin, "",
                                    hitResultBySeats, -1,
                                    additionalWin, result.getAwardedWeapons());

                            result.setWin(win);

                            hitOwn = getTOFactoryService().createHit(getCurrentTime(), shot.getRid(), seat.getNumber(),
                                    result.getDamage(), result.getWin().toDoubleCents(), realAwardedWeaponId,
                                    usedSpecialWeapon, remainingSWShots, diffScore, enemy, isLastResult,
                                    seatCurrent.getRoundWin().toDoubleCents(), result.getHvEnemyId(), shot.getX(),
                                    shot.getY(), realNewShots, result.isDestroyed(), result.getMineId(),
                                    result.getNewFreeShotsCount(), seat.getNumber(),
                                    result.isInstanceKill(), result.getChMult(), enemyId, shot.getEnemyId(),
                                    shot.getBulletId());

                            hitOwn.setAwardedWeapons(result.getAwardedWeapons());
                            hitOwn.setNeedExplode(result.isNeedExplode());
                            hitOwn.setExplode(result.isExplode());
                            hitOwn.setKillBonusPay(killAwardWin.toDoubleCents());
                            hitOwn.setBetLevel(betLevel);
                            hitOwn.setPaidSpecialShot(shot.isPaidSpecialShot());
                            hitOwn.setMoneyWheelWin(result.getMoneyWheelWin().toDoubleCents());
                            if (enemiesWithUpdatedMode != null)
                                hitOwn.setEnemiesWithUpdatedMode(enemiesWithUpdatedMode);


                            getLog().debug("isSpecialWeapon {}, isLastResult: {}, stake: {}, title: {}, realStake: {}",
                                    isSpecialWeapon, isLastResult, seat.getStake(), title, stake);

                            seat.getCurrentPlayerRoundInfo().updateStatNew(stake, result.isShotToBoss(), isSpecialWeapon,
                                    title, enemyWinForSeat, result.isDestroyed(), enemyNameKey, paidStake);
                            seat.getCurrentPlayerRoundInfo().addDamage(result.getDamage());
                        } else {
                            IHit hit = getTOFactoryService().createHit(getCurrentTime(), SERVER_RID, seat.getNumber(), result.getDamage(),
                                    enemyWinForSeat.toDoubleCents(),
                                    realAwardedWeaponId, usedSpecialWeapon, remainingSWShots, diffScore, enemy, isLastResult, 0,
                                    result.getHvEnemyId(), shot.getX(), shot.getY(), realNewShots, result.isDestroyed(),
                                    result.getMineId(), result.getNewFreeShotsCount(), seat.getNumber(),
                                    result.isInstanceKill(), result.getChMult(), enemyId, shot.getEnemyId(), shot.getBulletId());
                            hit.setBetLevel(betLevel);
                            hit.setExplode(result.isExplode());
                            if (enemiesWithUpdatedMode != null)
                                hit.setEnemiesWithUpdatedMode(enemiesWithUpdatedMode);
                            messagesForSeatsLocal.put(seatCurrent, hit);
                        }
                    }
                }

                updateHitResultBySeats(seat.getNumber(), Money.ZERO, null, hitResultBySeats,
                        awardedWeaponId, new ArrayList<>(), null);

                // hit for observers
                IHit hitForObservers = getTOFactoryService().createHit(getCurrentTime(), SERVER_RID, seat.getNumber(), result.getDamage(), 0,
                        awardedWeaponId, usedSpecialWeapon, seat.getSpecialWeaponRemaining(),
                        diffScore, enemy, isLastResult, 0,
                        result.getHvEnemyId(), shot.getX(), shot.getY(), newShots, result.isDestroyed(),
                        result.getMineId(), result.getNewFreeShotsCount(), seat.getNumber(),
                        result.isInstanceKill(), result.getChMult(), enemyId, shot.getEnemyId(), shot.getBulletId());

                hitForObservers.setKillBonusPay(killAwardWin.toDoubleCents());
                hitForObservers.setBetLevel(seat.getBetLevel());
                hitForObservers.setPaidSpecialShot(shot.isPaidSpecialShot());
                hitForObservers.setBetLevel(betLevel);
                hitForObservers.setExplode(result.isExplode());
                hitsForObserversLocal.add(hitForObservers);
                if (enemiesWithUpdatedMode != null)
                    hitForObservers.setEnemiesWithUpdatedMode(enemiesWithUpdatedMode);


                if (result.isDestroyed()) {
                    messages.add(
                            getTOFactoryService().createEnemyDestroyed(getCurrentTime(), SERVER_RID,
                                    result.getEnemyId(), SIMPLE_SHOT.ordinal()),
                            getTOFactoryService().createEnemyDestroyed(getCurrentTime(), shot.getRid(),
                                    result.getEnemyId(), SIMPLE_SHOT.ordinal()));

                    if (result.isShotToBoss()) {
                        subround = PlaySubround.BASE;
                        getLog().debug("allow spawn after killing of boss, subround set to BASE");
                    }
                }

            } else {
                seat.getCurrentPlayerRoundInfo().addMissCounter(seat.getCurrentWeaponId(), 1);

                String title = isSpecialWeapon ? SpecialWeaponType.values()[realShotTypeId].getTitle() : null;
                seat.getCurrentPlayerRoundInfo().updateStatNew(stake, result.isShotToBoss(), isSpecialWeapon,
                        title, Money.ZERO, result.isDestroyed(), enemyNameKey, paidStake);

                seat.incrementMissCount();
                int specialWeaponRemaining = seat.getSpecialWeaponRemaining();
                messages.add(getTOFactoryService().createMiss(getCurrentTime(), TObject.SERVER_RID, seat.getNumber(),
                        false, awardedWeaponId, enemyId, usedSpecialWeapon, specialWeaponRemaining, diffScore,
                        isLastResult, shot.getX(), shot.getY(), newShots, result.getMineId(), shot.getEnemyId(),
                        result.isInvulnerable(), seat.getBetLevel(), shot.getBulletId()),
                        getTOFactoryService().createMiss(getCurrentTime(), shot.getRid(), seat.getNumber(), false,
                                awardedWeaponId, enemyId, usedSpecialWeapon, specialWeaponRemaining, diffScore,
                                isLastResult, shot.getX(), shot.getY(), newShots, result.getMineId(),
                                shot.getEnemyId(), result.isInvulnerable(), seat.getBetLevel(), shot.getBulletId()));
            }
        }

        if (!shot.getBulletId().isEmpty())
            seat.removeBulletById(shot.getBulletId());

        if (hitOwn != null) {
            hitOwn.setHitResultBySeats(hitResultBySeats);
            messages.addOwnMessage(hitOwn);
        }

        for (Map.Entry<Seat, IHit> hitEntry : messagesForSeatsLocal.entrySet()) {
            IHit hit = hitEntry.getValue();
            hit.setHitResultBySeats(hitResultBySeats);
            messages.addMessageForSeat(hitEntry.getKey(), hit);
        }

        for (IHit hit : hitsForObserversLocal) {
            hit.setHitResultBySeats(hitResultBySeats);
            messages.addMessageForRealObservers(hit);
        }
    }

    private void updateHitResultBySeats(Integer seatId, Money win, String prize, Map<Integer,
            List<IWinPrize>> hitResultBySeats, int specialWeaponId, List<Pair<Integer, Money>> additionalWin,
                                        List<ITransportWeapon> weapons) {

        boolean isCacheWin = win.greaterThan(Money.ZERO);
        boolean isPrize = prize != null && !prize.isEmpty();
        boolean isAdditionalWin = !additionalWin.isEmpty();

        if (isCacheWin || isPrize || isAdditionalWin || weapons != null) {
            List<IWinPrize> winPrizes = hitResultBySeats.get(seatId);
            if (winPrizes == null)
                winPrizes = new ArrayList<>();

            if (isCacheWin)
                winPrizes.add(getTOFactoryService().createWinPrize(0, String.valueOf(win.toDoubleCents())));
            if (isPrize) {
                String[] keys = prize.split("\\|");
                for (String key : keys) {
                    Treasure treasure = Treasure.valueOf(key);
                    winPrizes.add(getTOFactoryService().createWinPrize(1, String.valueOf(treasure.getId())));
                }
            }

            if (isAdditionalWin) {
                for (Pair<Integer, Money> wins : additionalWin) {
                    winPrizes.add(getTOFactoryService().createWinPrize(wins.getKey(),
                            String.valueOf(wins.getValue().toDoubleCents())));
                }
            }

            if (weapons != null) {
                List<IWinPrize> finalWinPrizes = winPrizes;
                weapons.forEach(weapon -> finalWinPrizes.add(getTOFactoryService().createWinPrize(2,
                        String.valueOf(weapon.getId()))));
            }

            hitResultBySeats.put(seatId, winPrizes);
        }

    }


    private void addWeaponToSeat(Seat seat, IShootResult result) {
        getLog().debug("add weapons to seat aid {}, weapons before: {} ", seat.getAccountId(), seat.getWeapons());
        result.getAwardedWeapons().forEach(weapon -> {
            Weapon newWeapon = new Weapon(weapon.getShots(), SpecialWeaponType.values()[weapon.getId()]);
            seat.addWeapon(newWeapon);
            PlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
            currentPlayerRoundInfo.addWeaponSourceStat(WeaponSource.ENEMY.getTitle(),
                    SpecialWeaponType.values()[newWeapon.getType().getId()].getTitle(), weapon.getShots());
            currentPlayerRoundInfo.addFreeShotsWon(newWeapon.getShots());
            getLog().debug("add weapons to seat aid {}, newWeapon  {} ", seat.getAccountId(), newWeapon);
        });
        getLog().debug("add weapons to seat aid {}, weapons after: {} ", seat.getAccountId(), seat.getWeapons());
    }

    @Override
    public ShootResult shootToOneEnemy(long time, Seat seat, Long itemIdForShot, int weaponId, boolean isNearLandMine,
                                       double damageMultiplier) throws CommonException {

        GameMap map = getMap();

        boolean isShotWithSpecialWeapon = weaponId != REGULAR_WEAPON;

        Enemy enemy = map.getItemById(itemIdForShot);
        Money stake = seat.getStake();
        // killed earlier
        if (enemy == null) {
            return new ShootResult(isShotWithSpecialWeapon ? Money.ZERO : stake, Money.INVALID, false, false, null);
        }
        if (enemy.isInvulnerable(time)) {
            return new ShootResult(isShotWithSpecialWeapon ? Money.ZERO : stake, Money.INVALID, false, false, null, true);
        }

        boolean isBot = seat.getSocketClient().isBot();
        getLog().debug("PlayerId: " + seat.getPlayerInfo().getId() + ", isBot:  "
                + isBot + " itemIdForShot for shot: " + itemIdForShot + " enemyType: " + enemy.getEnemyClass()
                .getEnemyType().name());

        ShootResult shootResult = gameRoom.getGame().doShoot(enemy, seat, stake, subround.equals(PlaySubround.BOSS),
                isNearLandMine, damageMultiplier, getTOFactoryService());

        getLog().debug("shootResult: {}", shootResult);

        if (!isBot && shootResult.isBossShouldBeAppeared() && !isBossRound() && allowSpawn) {
            totalCountMainBossAppeared++;
            getRemainingBosses().add(shootResult.getBossSkinId());
            getLog().debug("Boss will be appeared later, remainingNumberOfBoss:  " + remainingNumberOfBoss
                    + " totalCountMainBossAppeared: " + totalCountMainBossAppeared
                    + " getRemainingBosses: " + getRemainingBosses()
            );
        }

        if (shootResult.isDestroyed()) {
            getLog().debug("enemy " + shootResult.getEnemyId() + " is killed");
            map.removeItem(shootResult.getEnemyId());
            getLog().debug("shootToOneEnemy, count of enemies after: {}", map.getItemsSize());
            getLog().debug("getCountRemainingEnemiesByModel: {}", getCountRemainingEnemiesByModel());
        }

        shootResult.setWeaponSurpluses(seat.getWeaponSurplus());
        return shootResult;
    }

    public ShootResult shootToOneEnemyExplode(Seat seat, Long itemIdForShot, int damageMultiplier) throws CommonException {

        GameMap map = getMap();
        Enemy enemy = map.getItemById(itemIdForShot);

        // killed earlier
        if (enemy == null) {
            return new ShootResult(Money.ZERO, Money.INVALID, false, false, null);
        }

        boolean isBot = seat.getSocketClient().isBot();
        getLog().debug("PlayerId: " + seat.getPlayerInfo().getId() + ", isBot:  "
                + isBot + " itemIdForShot for shot: " + itemIdForShot + " enemyType: " + enemy.getEnemyClass()
                .getEnemyType().name());

        ShootResult shootResult = (ShootResult) gameRoom.getGame().doShootWithExplode(enemy, seat, damageMultiplier,
                getTOFactoryService());
        shootResult.setMainShot(false);
        shootResult.setBet(Money.ZERO);
        getLog().debug("shootToOneEnemyExplode, shootResult: {}", shootResult);

        if (shootResult.isDestroyed()) {
            getLog().debug("enemy " + shootResult.getEnemyId() + " is killed");
            map.removeItem(shootResult.getEnemyId());
            getLog().debug("shootToOneEnemyExplode, count of enemies after: {}", map.getItemsSize());
        }

        return shootResult;
    }

    @Override
    public void restoreGameRoom(GameRoom gameRoom) throws CommonException {
        super.restoreGameRoom(gameRoom);
        lockShots = new ReentrantLock();
    }

    @Override
    public boolean isBossRound() {
        return subround.equals(PlaySubround.BOSS);
    }

    @Override
    public Map<Long, Integer> getFreezeTimeRemaining() {
        return getMap().getAllFreezeTimeRemaining(FREEZE_TIME_MAX);
    }

    public boolean isNeedImmediatelySpawn() {
        return needImmediatelySpawn;
    }

    public void setNeedImmediatelySpawn(boolean needImmediatelySpawn) {
        this.needImmediatelySpawn = needImmediatelySpawn;
    }

    public Map<Integer, Long> getLeaveAndDestroyEnemiesTime() {
        return leaveAndDestroyEnemiesTime == null ? new HashMap<>() : leaveAndDestroyEnemiesTime;
    }

    public Map<Long, Long> getEnemiesWithX2Mode() {
        if (enemiesWithX2Mode == null)
            enemiesWithX2Mode = new ConcurrentHashMap<>();
        return enemiesWithX2Mode;
    }

    private List<EnemyType> getRandomTypeFromRangeWithoutTypes(EnemyRange range,
                                                               List<EnemyType> excludeEnemies) {
        List<EnemyType> res = new ArrayList<>();
        for (EnemyType enemy : range.getEnemies()) {
            if (!excludeEnemies.contains(enemy))
                res.add(enemy);
        }
        return res;
    }

    private EnemyType getRandomEnemy(EnemyRange range) {
        List<EnemyType> enemies = range.getEnemies();
        return enemies.get(RNG.nextInt(enemies.size()));
    }

    @Override
    protected PlayGameState getDeserializer() {
        return this;
    }
}
