package com.betsoft.casino.mp.bgmissionamazon.model;

import com.betsoft.casino.mp.bgmissionamazon.model.math.*;
import com.betsoft.casino.mp.bgmissionamazon.model.math.config.GameConfig;
import com.betsoft.casino.mp.bgmissionamazon.model.math.config.SpawnBossParams;
import com.betsoft.casino.mp.bgmissionamazon.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.betsoft.casino.teststand.TeststandConst;
import com.betsoft.casino.utils.TInboundObject;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyRange.*;
import static com.betsoft.casino.mp.bgmissionamazon.model.math.EnemyType.*;
import static com.betsoft.casino.mp.bgmissionamazon.model.math.SwarmType.*;
import static com.betsoft.casino.mp.model.EnemyDestroyReason.REMOVED_ON_SERVER;
import static com.betsoft.casino.mp.model.EnemyDestroyReason.SIMPLE_SHOT;
import static com.betsoft.casino.mp.model.Money.BG_STAKE;
import static com.betsoft.casino.mp.model.PlaySubround.BOSS;
import static com.betsoft.casino.mp.utils.ErrorCodes.*;
import static com.betsoft.casino.utils.TObject.SERVER_RID;


@SuppressWarnings("Duplicates")
public class PlayGameState extends AbstractBattlegroundPlayGameState<GameRoom, Seat, GameMap, PlayGameState> {
    private transient long timeOfRoundFinishSoon;
    private transient List<Pair<EnemyType, Trajectory>> initialSpawnList;
    private transient boolean spawnBossTestStand = false;
    private transient long weaponCarrierOffsetTime = -1;
    private transient long weaponCarrierNextSpawnTime = -1;
    private transient long staticEnemyNextSpawnTime = -1;

    public PlayGameState() {
        super();

    }

    public PlayGameState(GameRoom gameRoom) {
        super(gameRoom, null);
        resetSeatData();

    }

    private void resetSeatData() {
        for (Seat seat : getRoom().getSeats()) {
            if (seat != null) {
                seat.setBetLevel(gameRoom.getDefaultBetLevel());
                seat.clearGems();
            }
        }
    }

    @Override
    public void init() throws CommonException {
        super.init();
        getRoom().setBossNumberShots(0);
        weaponCarrierOffsetTime = -1;
        weaponCarrierNextSpawnTime = -1;
        gameRoom.getStaticEnemiesDieOrDisappearTime().clear();
        staticEnemyNextSpawnTime = -1;
    }

    @Override
    protected void setWaitingGameState() throws CommonException {
        IGameState waitingState = gameRoom.getWaitingPlayersGameState();
        gameRoom.setGameState(waitingState);
    }

    @Override
    protected void setQualifyGameState() throws CommonException {
        gameRoom.setGameState(new QualifyGameState(gameRoom, getCurrentMapId(), pauseTime, startRoundTime, endRoundTime));
    }

    @Override
    protected void setPossibleEnemies() {
        gameRoom.setPossibleEnemies(EnemyRange.BASE_ENEMIES);
    }

    @Override
    protected void updateWithLock() {
        if (needForceFinishRound) {
            nextSubRound();
            return;
        }
        SpawnConfig spawnConfig = getRoom().getGame().getSpawnConfig(getRoom().getId());
        SpawnBossParams bossParams = spawnConfig.getBossParams();
        long currentTime = System.currentTimeMillis() - startRoundTime;
        long nextBossSpawn = getBossSPawnTimeByBossNumber(spawnedBossesCounter, bossParams);
        if (needWaitingWhenEnemiesLeave && System.currentTimeMillis() > timeOfRoundFinishSoon + 2000) {
            destroyBaseEnemies();
            getLog().debug("no live enemies in room, finish by timeOfRoundFinishSoon");
            nextSubRound();
            return;
        }
        if (PlaySubround.BASE.equals(subround)) {
            if (spawnBossTestStand || (nextBossSpawn != -1 && nextBossSpawn < currentTime && bossParams.getMaxBosses() > spawnedBossesCounter &&
                    (prevBossSpawnTime == 0 || (System.currentTimeMillis() > prevBossSpawnTime + (bossParams.getTa() * 1000L) && nextBossSpawn <= bossParams.getT2() * 1000L)))) {
                switchSubround(BOSS);
                int bossForRoom = spawnBossTestStand ? TestStandLocal.getInstance().getBossForRoom(getRoom().getId()) : -1;
                BossType bossType = (spawnBossTestStand && bossForRoom != -1) ? BossType.getBySkinId(bossForRoom) :
                        MathData.getRandomBossType(getRoom().getGame().getConfig(getRoom().getId()));
                spawnBoss(bossType);
                spawnedBossesCounter++;
                spawnBossTestStand = false;
            } else {
                tryGenerateEnemies();
            }
        } else {
            tryGenerateEnemies();
            if (getMap().updateBossRound()) {
                switchSubround(PlaySubround.BASE);
                prevBossSpawnTime = System.currentTimeMillis();
            }
            if (nextBossSpawn != -1 && currentTime > nextBossSpawn) {
                bossesSpawnTime.remove(spawnedBossesCounter);
            }
        }
    }

    private long getBossSPawnTimeByBossNumber(int bossNumber, SpawnBossParams bossParams) {
        if (bossesSpawnTime == null) {
            List<Long> triggerTimeSlots = new ArrayList<>();
            double dt = 0.1;
            double t0 = 0;
            double alpha = 0;
            double b = 0;
            boolean firstBossTriggered = false;
            for (double t = bossParams.getT1(); t <= bossParams.getT2(); t += dt) {
                double r = RNG.rand();
                if (!firstBossTriggered && r < f(t, bossParams.getSigma(), bossParams.getMu(), bossParams.getA())) {
                    t0 = t;
                    triggerTimeSlots.add((long) t0 * 1000);
                    b = f(t, bossParams.getSigma(), bossParams.getMu(), bossParams.getA()) * bossParams.getLambda();
                    if (b < 0.00001) {
                        alpha = 1;
                    } else {
                        alpha = -((Math.log(0.00001 / b)) / (bossParams.getT2() - t0));
                    }
                    firstBossTriggered = true;
                } else if (firstBossTriggered) {
                    if (r < b * Math.exp(-alpha * (t - t0))) {
                        triggerTimeSlots.add((long) t * 1000);
                    }
                }
            }
            setBossesSpawnTime(triggerTimeSlots);
        }
        return bossesSpawnTime.size() > bossNumber ? bossesSpawnTime.get(bossNumber) : -1;
    }

    private double f(double t, double sigma, double mu, double A) {
        return A * Math.exp(-(Math.pow(((t - mu) / sigma), 2)));
    }

    private void tryGenerateEnemies() {
        try {
            generateEnemies();
        } catch (Exception e) {
            getLog().debug("generateEnemies error: ", e);
        }
    }

    private void switchSubround(PlaySubround subround) {
        this.subround = subround;
        getLog().debug("reset boss data for all seats");
        for (Seat seat : gameRoom.getSeats()) {
            if (seat != null) {
                seat.resetTotalBossPayout();
            }
        }
        gameRoom.sendChanges(getTOFactoryService()
                .createChangeMap(getCurrentTime(), gameRoom.getMapId(), subround.name()));
    }

    private void generateEnemies() {
        GameMap map = getMap();
        GameConfig config = getRoom().getGame().getConfig(getRoom().getId());
        SpawnConfig spawnConfig = getRoom().getGame().getSpawnConfig(getRoom().getId());

        map.update();

        checkTestStandFeatures();
        getMap().checkFreezeTimeEnemies(FREEZE_TIME_MAX);

        List<Pair<Integer, Boolean>> itemsTypeIdsAndSwarmState = map.getItemsTypeIdsAndSwarmState();

        if (isNeedMinimalEnemies() && itemsTypeIdsAndSwarmState.size() > 3) {
            return;
        }

        if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            getLog().debug("no live enemies in room, finish");
            nextSubRound();
            return;
        }

        if (allowSpawn && startRoundTime != 0 && !isManualGenerationEnemies() && !getMap().isMapFullOfEnemy(spawnConfig)) {
            Map<Integer, Integer> counter = map.countEnemyTypes();
            spawnEnemyWithPredefinedTrajectory(config, spawnConfig, counter);
            spawnStaticEnemy(config, spawnConfig, counter);
            spawnWeaponCarrier(config, spawnConfig, counter);
            spawnSwarms(config, spawnConfig);

        } else if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            nextSubRound();
        }
    }

    private void spawnEnemyWithPredefinedTrajectory(GameConfig config, SpawnConfig spawnConfig, Map<Integer, Integer> counter) {
        Pair<EnemyType, Trajectory> enemyPathPair = getEnemyWithPredefinedTrajectory(spawnConfig);
        if (enemyPathPair == null || countEnemyRanges(counter, LOW_PAY_ENEMIES, MID_PAY_ENEMIES, HIGH_PAY_ENEMIES)
                >= spawnConfig.getEnemiesWithPredefinedTrajectoriesMax()) {
            return;
        }

        EnemyType enemyType = enemyPathPair.getKey();
        if (!config.isEnabledEnemy(enemyType)) {
            return;
        }
        Trajectory trajectory = enemyPathPair.getValue();
        if (WALKING_ENEMIES.contains(enemyType)
                && allowSpawnToSingleEnemy(enemyType, counter)) {
            spawnEnemyWithTrajectory(enemyType, trajectory);
        } else if (RUNNER.equals(enemyType)) {
            spawnGroupBySwarmTypeWithTrajectory(config, spawnConfig, RUNNERS, trajectory);
        }
    }

    private void spawnStaticEnemy(GameConfig config, SpawnConfig spawnConfig, Map<Integer, Integer> counter) {
        EnemyType staticEnemy = STATIC_ENEMIES.getRandomEnemy();
        if (config.isEnabledEnemy(staticEnemy) && getMap().isPointsPresent(GameMap.STATIC_POINTS) && System.currentTimeMillis() - startRoundTime > 10000
                && allowSpawnToSingleEnemy(staticEnemy, counter) && getMap().allowCreateStaticEnemy(spawnConfig)) {
            if (WITCH.equals(staticEnemy)) {
                spawnWitch(staticEnemy, spawnConfig);
            } else if (PLANTS.contains(staticEnemy) || FLOWERS.contains(staticEnemy)) {
                long stayTime = spawnConfig.getRandomStaticStayTime();
                if (allowedTimeToSpawnStaticEnemy(spawnConfig)) {
                    spawnStaticEnemy(staticEnemy, stayTime);
                    gameRoom.getStaticEnemiesDieOrDisappearTime().add(System.currentTimeMillis() + stayTime);
                    staticEnemyNextSpawnTime = -1;
                }
            }
        }
    }

    private boolean allowedTimeToSpawnStaticEnemy(SpawnConfig spawnConfig) {
        PriorityQueue<Long> staticEnemiesDieOrDisappearTime = gameRoom.getStaticEnemiesDieOrDisappearTime();
        Long peek = staticEnemiesDieOrDisappearTime.peek();
        if (peek != null && System.currentTimeMillis() > peek && staticEnemyNextSpawnTime == -1) {
            staticEnemyNextSpawnTime = staticEnemiesDieOrDisappearTime.remove() + spawnConfig.getRandomStaticEnemiesSpawnTimeDelay();
        }
        return System.currentTimeMillis() > staticEnemyNextSpawnTime;
    }

    private void spawnWeaponCarrier(GameConfig config, SpawnConfig spawnConfig, Map<Integer, Integer> counter) {
        EnemyType weaponCarrier = getMap().getRandomEnemyFromConfig(spawnConfig.getWeaponCarrierTypes(), WEAPON_CARRIERS);
        if (config.isEnabledEnemy(weaponCarrier) && getMap().isPointsPresent(GameMap.STATIC_POINTS) && allowedTimeForSpawnEnemy(spawnConfig)
                && allowSpawnToSingleEnemy(weaponCarrier, counter)
                && countEnemyRanges(counter, WEAPON_CARRIERS) < 1) {

            MutableBoolean seatsHasPowerShots = new MutableBoolean(false);
            gameRoom.getSeats().forEach(seat -> seat.getWeapons().forEach((weaponType, weapon) -> {
                if (weaponType.isPowerUp() && weapon.getShots() > 0) {
                    seatsHasPowerShots.setValue(true);
                }
            }));
            if (!seatsHasPowerShots.booleanValue()) {
                long stayTime = spawnConfig.getWeaponCarrierStayTime();
                spawnWeaponCarrierEnemy(weaponCarrier, stayTime);
                weaponCarrierNextSpawnTime = System.currentTimeMillis() + stayTime + spawnConfig.getRandomWeaponCarrierSpawnTimeDelay();
            }
        }
    }

    private boolean allowedTimeForSpawnEnemy(SpawnConfig spawnConfig) {
        long roundTime = System.currentTimeMillis() - startRoundTime;
        if (weaponCarrierOffsetTime == -1) {
            weaponCarrierOffsetTime = spawnConfig.getRandomWCOffsetTime();
        }
        return roundTime > weaponCarrierOffsetTime && System.currentTimeMillis() > weaponCarrierNextSpawnTime;
    }

    private void spawnSwarms(GameConfig config, SpawnConfig spawnConfig) {
        switch (SWARM_ENEMIES.getRandomEnemy()) {
            case SERPENT:
                spawnGroupsBySwarmType(config, spawnConfig, SNAKES);
                break;
            case WASP:
                spawnWasps(config, spawnConfig);
                break;
            case ANT:
            default:
                spawnAnts(config, spawnConfig);
        }
    }

    private boolean allowSpawnToSingleEnemy(EnemyType enemyType,
                                            Map<Integer, Integer> counter) {
        return shouldSpawn(counter, enemyType);
    }

    private boolean shouldSpawn(Map<Integer, Integer> counter, EnemyType enemyType) {
        return !counter.containsKey(enemyType.getId());
    }

    private int countEnemyRanges(Map<Integer, Integer> counter, EnemyRange... enemyRanges) {
        int count = 0;
        for (EnemyType enemyType : EnemyRange.getEnemiesFromRanges(enemyRanges)) {
            count += counter.getOrDefault(enemyType.getId(), 0);
        }
        return count;
    }

    protected void onTimerWithLock(boolean needClearEnemy) {
        getLog().debug("PlayGameState:: onTimer: current={}", this);
        getLog().debug("End round, aliveMummies: {} needWaitingWhenEnemiesLeave: {} remainingNumberOfBoss: {}",
                getMap().getItemsSize(), needWaitingWhenEnemiesLeave, remainingNumberOfBoss
        );

        if (!needWaitingWhenEnemiesLeave) {
            needWaitingWhenEnemiesLeave = true;

            timeOfRoundFinishSoon = System.currentTimeMillis();
            spawnBossTestStand = false;
            allowSpawn = false;
            getMap().clearInactivityLiveItems();
            destroyBaseEnemies();
            getMap().removeAllEnemies();
            gameRoom.sendChanges(getTOFactoryService().createRoundFinishSoon(System.currentTimeMillis()));
        }
    }

    private void destroyBaseEnemies() {
        long time = System.currentTimeMillis();
        getMap().removeBaseEnemiesAndGetIds().forEach(id -> gameRoom.sendChanges(getTOFactoryService()
                .createEnemyDestroyed(time, SERVER_RID, id, REMOVED_ON_SERVER.ordinal())));
    }

    boolean noAnyEnemiesInRound() {
        return getMap().noEnemiesInRoom() && getCountRemainingEnemiesByModel() == 0
                && getMap().getNumberInactivityItems() == 0;
    }

    private void spawnGroupsBySwarmType(GameConfig config, SpawnConfig spawnConfig, SwarmType swarmType) {
        List<Enemy> enemies = getMap().spawnAllGroupsBySwarmType(config, spawnConfig, swarmType, startRoundTime);
        gameRoom.sendNewEnemiesMessage(enemies);
    }

    private void spawnGroupBySwarmTypeWithTrajectory(GameConfig config, SpawnConfig spawnConfig, SwarmType swarmType,
                                                     Trajectory trajectory) {
        List<Enemy> enemies = getMap().spawnGroupWithTrajectory(config, spawnConfig, swarmType, trajectory, startRoundTime);
        gameRoom.sendNewEnemiesMessage(enemies);
    }

    private void spawnWasps(GameConfig config, SpawnConfig spawnConfig) {
        SwarmType swarmType = RNG.nextBoolean() ? WASP_REGULAR : WASP_ORION;
        List<Enemy> enemies = getMap().spawnAllWaspsFromSwarmSpawnParams(swarmType, config, spawnConfig, startRoundTime);
        gameRoom.sendNewEnemiesMessage(enemies);
    }

    private void spawnAnts(GameConfig config, SpawnConfig spawnConfig) {
        List<Enemy> enemies = getMap().spawnAllAntsFromSwarmParams(config, spawnConfig, startRoundTime);
        gameRoom.sendNewEnemiesMessage(enemies);
    }

    @Override
    public void nextSubRound() {
        doFinishWithLock();
    }

    @Override
    public void doFinishWithLock() {
        lockShots.lock();
        try {
            getLog().debug("doFinishWithLock: timer: {}", gameRoom.getTimerTime());
            resetSeatData();
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
        //ignored
    }

    private void spawnEnemyWithTrajectory(EnemyType enemyType, Trajectory trajectory) {
        Enemy enemy = getMap().addEnemyWithTrajectory(enemyType, trajectory);
        gameRoom.sendNewEnemyMessage(enemy);
    }

    @Override
    public void spawnEnemyFromTeststand(int typeId, int skinId, Trajectory trajectory, long parentEnemyId) {
        //ignored
    }

    private void spawnBoss(BossType bossType) {
        lockShots.lock();
        try {
            GameConfig config = gameRoom.getGame().getConfig(gameRoom.getRoomInfo().getId());
            gameRoom.sendNewEnemyMessage(getMap().spawnBoss(bossType, config.getBossParams().getBossHP().get(bossType.getSkinId())));
        } finally {
            lockShots.unlock();
        }
    }

    @Override
    public void processShot(Seat seat, IShot shot, boolean isInternalShot) throws CommonException {
        long time = System.currentTimeMillis();
        processShot(time, seat, shot, isInternalShot);
    }

    private void updateBossNumberShots(int weaponId) {
        if (!isBossRound()) {
            return;
        }
        int cnt = 1; // default
        if (SpecialWeaponType.Flamethrower.getId() == weaponId) {
            cnt = 3;
        } else if (SpecialWeaponType.Cryogun.getId() == weaponId
                || SpecialWeaponType.ArtilleryStrike.getId() == weaponId) {
            cnt = 2;
        } else if (SpecialWeaponType.Plasma.getId() == weaponId) {
            cnt = 12;
        } else if (SpecialWeaponType.Ricochet.getId() == weaponId) {
            cnt = 4;
        }
        gameRoom.setBossNumberShots(gameRoom.getBossNumberShots() + cnt);
    }

    public void processShot(long time, Seat seat, IShot shot, boolean isInternalShot) throws CommonException {
        final boolean paidSpecialShot = shot.isPaidSpecialShot();

        if (shot.isPaidSpecialShot()) {
            sendError(seat, shot, BAD_REQUEST, "paid shot is not allowed in BG mode", shot);
            return;
        }

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

            getLog().debug("processShot: aid: {}, shot:{}, isInternalShot: {}, " +
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
                if (shot.getWeaponId() != -1 && shot.getWeaponId() != SpecialWeaponType.LevelUp.getId()) {
                    sendError(seat, shot, BAD_REQUEST, "Shot with bulletId and wrong weaponId", shot);
                    return;
                }
            }

            if (!isInternalShot && paidSpecialShot && currentWeaponIdOld == -1) {
                sendError(seat, shot, BAD_REQUEST, "Wrong weapon mode", shot);
                return;
            }

            if (paidSpecialShot && getRoomInfo().getMoneyType().equals(MoneyType.FRB)) {
                sendError(seat, shot, BAD_REQUEST, "Wrong weapon mode, paid weapons is not allowed in FRB", shot);
                return;
            }

            boolean needRestoreWeapon = false;
            if (isShotWithBulletId) {
                needRestoreWeapon = true;
                seat.setWeapon(shot.getWeaponId());
            }

            seat.setLastWin(Money.ZERO);
            gameRoom.getSeats().forEach(AbstractSeat::resetShotTotalWin);

            boolean isPaidShot = shot.getWeaponId() == -1;
            boolean allowWeaponSaveInAllGames = seat.getPlayerInfo().isAllowWeaponSaveInAllGames();

            int multiplierPaidWeapons = seat.getBetLevel();

            Enemy itemById = getMap().getItemById(shot.getEnemyId());
            boolean isBoss = false;
            if (itemById != null)
                isBoss = itemById.isBoss();

            long realStakeInCents = BG_STAKE.toCents() * shot.getWeaponId() != SpecialWeaponType.LevelUp.getId()
                    ? gameRoom.getDefaultBetLevel() : seat.getBetLevel();
            getLog().debug(" realStakeInCents: {}", realStakeInCents);


            PlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
            int numberOfKilledMissOld = roundInfo.getKilledMissedNumber(seat.getCurrentWeaponId());

            if (seat.getAmmoAmountTotalInRound() == 0 && seat.getPlayerInfo().getRoundBuyInAmount() == 0
                    && !allowWeaponSaveInAllGames && !isPaidShot) {
                getLog().debug("Not enough bullets 1, seat.getRoundBuyInAmount: {} ", seat.getPlayerInfo().getRoundBuyInAmount());
                getLog().debug("Not enough bullets 1 seat: {}", seat);
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets 1", shot);
            } else if (paidSpecialShot && seat.getAmmoAmount() < multiplierPaidWeapons) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets 2", shot);
                getLog().debug("paidSpecialShot, seat.getAmmoAmount(): {} less then weapon cost: {}",
                        seat.getAmmoAmount(), multiplierPaidWeapons);
                return;
            } else if ((seat.getAmmoAmount() <= 0 || (seat.getAmmoAmount() - seat.getBetLevel() < 0)) && isPaidShot) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets 3", shot);
            } else if (shot.getRealWeaponId() != seat.getCurrentWeaponId()) {
                sendError(seat, shot, WRONG_WEAPON, "Wrong weapon", shot);
            } else if (shot.getWeaponId() == REGULAR_WEAPON) {
                seat.setActualShot(shot);
                if (isBoss) {
                    updateBossNumberShots(REGULAR_WEAPON);
                }
                shootWithSpecialWeapon(time, seat, shot);
                roundInfo.addRealShotsCounter(seat.getCurrentWeaponId(), 1);
                if (roundInfo.isShotSuccess(seat.getCurrentWeaponId(), numberOfKilledMissOld)) {
                    roundInfo.addKpiInfoPaidRegularShots(realStakeInCents);
                    roundInfo.addKpiInfoSWShotsCount(-1, 1, seat.getBetLevel(), false);
                }
            } else if (isInternalShot || seat.getCurrentWeapon().getShots() > 0 || paidSpecialShot) {
                if (shot.getWeaponId() == SpecialWeaponType.Cryogun.getId() || shot.getWeaponId() == SpecialWeaponType.PowerUp_Cryogun.getId()) {
                    sendFreezeTrajectories(time, shot.getX(), shot.getY(), 280);
                }
                seat.setActualShot(shot);
                int weaponId = seat.getCurrentWeaponId();
                if (isBoss) {
                    updateBossNumberShots(weaponId);
                }
                shootWithSpecialWeapon(time, seat, shot);
                roundInfo.addRealShotsCounter(seat.getCurrentWeaponId(), 1);
                if (roundInfo.isShotSuccess(seat.getCurrentWeaponId(), numberOfKilledMissOld)) {
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

            if (!needWaitingWhenEnemiesLeave) {
                lastShotTime = time;
            }

            if (needRestoreWeapon) {
                seat.setWeapon(currentWeaponIdOld);
            }

            String sessionId = seat.getPlayerInfo().getSessionId();
            TestStandFeature featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
            if (featureBySid != null && featureBySid.getId() == TeststandConst.FEATURE_NO_ANY_WIN) {
                TestStandLocal.getInstance().removeFeatureBySid(sessionId);
            }

            gameRoom.getSeats().forEach(AbstractSeat::updateScoreShotTotalWin);

            getLog().debug("shot, end getCountRemainingEnemiesByModel: {}, ammo: {}, allowSpawn: {}",
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

    private void spawnStaticEnemy(EnemyType specterType, long stayTime) {
        gameRoom.sendNewEnemyMessage(getMap().createStaticEnemy(specterType, stayTime));
    }

    private void spawnWitch(EnemyType wizardType, SpawnConfig spawnConfig) {
        gameRoom.sendNewEnemyMessage(getMap().createWitch(wizardType, spawnConfig.getRandomTeleportingStayTime(), spawnConfig.getRandomTeleportingHops()));
    }

    private void spawnWeaponCarrierEnemy(EnemyType specterType, long stayTime) {
        gameRoom.sendNewEnemyMessage(getMap().createWeaponCarrierEnemy(specterType, stayTime));
    }

    @Override
    protected void shootWithSpecialWeapon(long time, Seat seat, IShot shot) throws CommonException {
        ShotMessages messages = new ShotMessages(seat, shot, gameRoom,
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0),
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0));

        getLog().debug("processing shot from special weapon: {}, seat.getSpecialWeaponId(): {}",
                shot.getWeaponId(), seat.getSpecialWeaponId());

        int betLevelOld = seat.getBetLevel();
        boolean isSpecialWeapon = seat.getCurrentWeaponId() != -1;

        getLog().debug("processing shot from special weapon: {}, seat.getSpecialWeaponId(): {}, isSpecialWeapon: {}",
                shot.getWeaponId(), seat.getSpecialWeaponId(), isSpecialWeapon);

        if (shot.getWeaponId() != SpecialWeaponType.LevelUp.getId()) {
            seat.setBetLevel(gameRoom.getDefaultBetLevel());
            getLog().debug("processing shot shootWithSpecialWeapon betLevelOld: {}, set seat bet level to default: {}",
                    betLevelOld, gameRoom.getDefaultBetLevel());

        }

        List<ShootResult> shootResults = shootWithSpecialWeaponAndUpdateState(time, seat, shot, shot.getWeaponId(), messages);

        boolean needLevelUp = false;

        int size = shootResults.size();
        for (ShootResult result : shootResults) {
            size--;
            int awardedWeaponId = !result.isNewWeapon() ? -1 : result.getWeapon().getType().getId();
            if (!result.getAwardedWeapons().isEmpty() && result.getAwardedWeapons().stream().anyMatch(wp -> wp.getId() == SpecialWeaponType.LevelUp.getId())) {
                needLevelUp = true;
            }
            processShootResult(seat, shot, result, messages, awardedWeaponId, size == 0);
        }

        seat.setBetLevel(betLevelOld);

        if (needLevelUp) {
            boolean levelUpFromBaseWeapon = betLevelOld == gameRoom.getDefaultBetLevel() && shot.getWeaponId() == -1;
            boolean levelUpFromLevelUp = betLevelOld == 5 && shot.getWeaponId() == SpecialWeaponType.LevelUp.getId();
            if (levelUpFromBaseWeapon || levelUpFromLevelUp) {
                ArrayList<Integer> possibleBetLevels = new ArrayList<>(MathData.getPossibleBetLevels());
                int idxOld = possibleBetLevels.indexOf(betLevelOld);
                int newIdx = Math.min(idxOld + 1, possibleBetLevels.size() - 1);
                int newBetLevelCurrent = possibleBetLevels.get(newIdx);
                TInboundObject betLevelResponse = getTOFactoryService()
                        .createBetLevelResponse(System.currentTimeMillis(), SERVER_RID, newBetLevelCurrent, seat.getNumber());
                seat.setBetLevel(newBetLevelCurrent);
                getLog().debug("processing levelUp from special aid: {}, weapon: {}, " +
                                "seat.getSpecialWeaponId(): {}, betLevelOld: {}, " +
                                "newBetLevelCurrent: {}, levelUpFromBaseWeapon: {}, levelUpFromLevelUp: {}",
                        seat.getAccountId(), shot.getWeaponId(), seat.getSpecialWeaponId(), betLevelOld, newBetLevelCurrent,
                        levelUpFromBaseWeapon, levelUpFromLevelUp);
                gameRoom.sendChanges(betLevelResponse);
            }
        }

        if (seat.getCurrentWeaponId() == SpecialWeaponType.LevelUp.getId() && seat.getCurrentWeapon().getShots() == 0) {
            int newBetLevelCurrent = 3;
            TInboundObject betLevelResponse = getTOFactoryService()
                    .createBetLevelResponse(System.currentTimeMillis(), SERVER_RID, newBetLevelCurrent, seat.getNumber());
            seat.setBetLevel(newBetLevelCurrent);
            gameRoom.sendChanges(betLevelResponse);
            getLog().debug("processing shot, levelUp weapons is zero, reset bet level for aid: {}", seat.getAccountId());
        }

        if (isSpecialWeapon && !seat.isAnyWeaponShotAvailable()) {
            seat.resetAdditionalTempCounter(AbstractActionSeat.ADD_COUNTER_WEAPON_DROPPED);
            seat.resetAdditionalTempCounter(AbstractActionSeat.ADD_COUNTER_POWER_UP_MULT);
            getLog().debug("processing shot, reset additional weapon counters for aid: {}", seat.getAccountId());
        }

        messages.send(shot);
    }

    public int getMyScoreId(int seatId, Map<Integer, Long> winsBySeatId) {
        int res = 0;
        if (winsBySeatId.get(seatId) == 0) {
            return res;
        }

        int playerSize = winsBySeatId.size();
        long ts = winsBySeatId.values().stream().mapToLong(Long::longValue).sum();
        long minDSI = ts;
        Map<Integer, Long> tempDSI = new HashMap<>();
        for (Map.Entry<Integer, Long> entry : winsBySeatId.entrySet()) {
            long tempSeatDSI = playerSize * entry.getValue() - ts;
            tempDSI.put(entry.getKey(), tempSeatDSI);
            if (tempSeatDSI < minDSI) {
                minDSI = tempSeatDSI;
            }
        }

        long finalMinDSI = minDSI;
        Map<Integer, Double> tempFinalDSI = tempDSI.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new Double((e.getValue() - finalMinDSI))));

        double tds = tempFinalDSI.values().stream().mapToDouble(Double::doubleValue).sum();
        double rank = (tempFinalDSI.get(seatId) / tds) * 100;

        if (rank < 25) res = 1;
        else if (rank < 50) res = 2;
        else if (rank < 75) res = 3;
        else res = 4;
        getLog().debug("seatId: {},  tds: {}  , rank: {} , res: {}, tempFinalDSI: {}, winsBySeatId: {} ",
                seatId, tds, rank, res, tempFinalDSI, winsBySeatId);
        return res;
    }


    @Override
    protected List<ShootResult> shootWithSpecialWeaponAndUpdateState(long time, Seat seat, IShot shot, int weaponId,
                                                                     ShotMessages messages) throws CommonException {
        List<ShootResult> results = new LinkedList<>();
        GameMap map = getMap();
        Long itemIdForShot = shot.getEnemyId();

        int liveEnemies = map.getItemsSize();

        if (liveEnemies == 0) {
            results.add(new ShootResult(BG_STAKE, Money.INVALID, false, false, null));
            getLog().debug("shootWithSpecialWeaponAndUpdateState: no live enemies, return kill Miss: message");
            return results;
        }

        SpawnConfig spawnConfig = getRoom().getGame().getSpawnConfig(getRoom().getId());

        int killNumberFromConfig = weaponId == -1 ? -1 : spawnConfig.getRandomKillNumberOfEnemies();

        List<Integer> killEnemiesById = spawnConfig.getKillEnemyByIds();
        List<Long> items = null;
        if (!killEnemiesById.isEmpty() && !killEnemiesById.contains(-1)) {
            items = getMap().getItems().stream()
                    .filter(item -> killEnemiesById.contains(item.getEnemyType().getId()))
                    .map(AbstractEnemy::getId)
                    .collect(Collectors.toList());
        }

        Enemy baseEnemy = map.getItemById(itemIdForShot);
        boolean isBossTarget = baseEnemy != null && baseEnemy.isBoss();

        int numberDamages = MathData.getRandomDamageForWeapon(gameRoom.getGame().getConfig(seat), seat.getCurrentWeapon() == null ? null : seat.getCurrentWeapon().getType(), isBossTarget);
        numberDamages = killNumberFromConfig > 0 ? killNumberFromConfig : numberDamages;
        numberDamages = items != null ? items.size() : numberDamages;

        Map<Integer, Long> winsBySeatId = new HashMap<>();
        for (Seat seatCurrent : gameRoom.getSeats()) {
            PlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
            winsBySeatId.put(seatCurrent.getNumber(), currentPlayerRoundInfo != null ? currentPlayerRoundInfo.getTotalPayouts().toCents() : 0);
        }

        int scoreIdx = getMyScoreId(seat.getNumber(), winsBySeatId);

        getLog().debug("shootWithSpecialWeaponAndUpdateState: numberDamages: {}, scoreIdx: {}", numberDamages, scoreIdx);


        if (baseEnemy != null) {
            getLog().debug("shootWithSpecialWeaponAndUpdateState: Base enemy: {} enemyTypeId: {}", baseEnemy, baseEnemy.getEnemyType().getId());
            if (!map.isVisible(time, baseEnemy)) {
                getLog().debug("shootWithSpecialWeaponAndUpdateState: attempt to shoot on non visible target at {}", baseEnemy.getLocation(time));
                return createErrorResult(seat, baseEnemy, true);
            }
            if (killNumberFromConfig-- > 0 || killEnemiesById.contains(baseEnemy.getEnemyType().getId())) {
                addTestStandShotFeature(seat.getPlayerInfo().getSessionId());
            } else if (items != null) {
                TestStandLocal testStandLocal = TestStandLocal.getInstance();
                TestStandFeature featureById = testStandLocal.getPossibleFeatureById(42).copy();
                testStandLocal.addFeature(seat.getPlayerInfo().getSessionId(), featureById);
            }
            boolean success = singleShot(time, seat, baseEnemy, weaponId, results, true, scoreIdx, isBossTarget);
            if (success) {
                results.get(0).setMainShot(true);
                numberDamages--;
            } else {
                getLog().debug("shootWithSpecialWeaponAndUpdateState: Base enemy is invulnerable");
                return createErrorResult(seat, baseEnemy, true);
            }
        } else {
            // killed earlier
            getLog().debug("shootWithSpecialWeaponAndUpdateState: Base enemy was killed before");
            return createErrorResult(seat, null, false);
        }
        PointD locationOfBaseEnemy = baseEnemy.getLocation(time);

        if (liveEnemies > numberDamages) {
            liveEnemies = numberDamages;
        }

        Map<Long, Double> nNearestEnemies = null;

        if (items == null) {
            GameConfig config = getRoom().getGame().getConfig(getRoom().getId());
            if (weaponId == SpecialWeaponType.ArtilleryStrike.getId()) {
                if (RNG.nextBoolean()) {
                    nNearestEnemies = map.getNNearestLowPayEnemiesWithoutBase(time, locationOfBaseEnemy, itemIdForShot,
                            liveEnemies, config);
                } else if (RNG.nextInt(5) < 2 && map.isShotNearCenter(shot)) {
                    nNearestEnemies = map.getAllLowPayEnemies(time, (int) Math.round(liveEnemies * 0.6), config);
                }
            } else if ((weaponId == SpecialWeaponType.Flamethrower.getId() || weaponId == SpecialWeaponType.Cryogun.getId())
                    && RNG.nextBoolean()) {
                nNearestEnemies = map.getNNearestLowPayEnemiesWithoutBase(time, locationOfBaseEnemy, itemIdForShot,
                        liveEnemies, config);
            }
            if (nNearestEnemies == null) {
                nNearestEnemies = map.getNNearestEnemiesWithoutBase(time, locationOfBaseEnemy, itemIdForShot, liveEnemies);
            }
            getLog().debug("shootWithSpecialWeaponAndUpdateState: numberDamages: {} nNearestEnemies: {}", numberDamages, nNearestEnemies);

            if (nNearestEnemies.size() != liveEnemies) {
                getLog().debug("shootWithSpecialWeaponAndUpdateState: possible error logic, nNearestEnemies.size(): {}, damages.size(): {}, liveEnemies: {}",
                        nNearestEnemies.size(), numberDamages, liveEnemies);
            }
        }

        long shotsToBoss = 0;
        if (isBossRound() && nNearestEnemies != null && !nNearestEnemies.isEmpty() && weaponId != -1) {
            int randomNumberShotsToBoss = MathData.getRandomNumberShotsToBoss(gameRoom.getGame().getConfig(seat), SpecialWeaponType.values()[weaponId], isBossTarget);
            double grossShotsToBoss = numberDamages * ((double) randomNumberShotsToBoss / 100);
            shotsToBoss = Math.round(grossShotsToBoss);
        }

        int realNumberOfShots = 0;
        for (Long enemyId : items != null ? items : nNearestEnemies.keySet()) {
            Enemy enemy = map.getItemById(enemyId);
            if (shotsToBoss-- > 0) {
                long bossId = map.getActiveBossId(time);
                if (bossId != -1) {
                    enemy = map.getItemById(bossId);
                }
            }
            if (killNumberFromConfig-- > 0 || items != null) {
                addTestStandShotFeature(seat.getPlayerInfo().getSessionId());
            }
            if (enemy != null && singleShot(time, seat, enemy, weaponId, results, false, scoreIdx, isBossTarget)) {
                realNumberOfShots++;
            }
        }

        getLog().debug("shootWithSpecialWeaponAndUpdateState: realNumberOfShots: {}, numberDamages: {}", realNumberOfShots, numberDamages);
        if (realNumberOfShots < numberDamages && items == null) {
            int cnt = 100;
            while (cnt-- > 0) {
                Long nearestEnemy = map.getAllNearestEnemy(time, locationOfBaseEnemy,
                        false, itemIdForShot, null);
                if (nearestEnemy == null) {
                    getLog().debug("shootWithSpecialWeaponAndUpdateState: account: {}, No enemies for shooting from SW", seat.getAccountId());
                    break;
                }
                Enemy enemy = map.getItemById(nearestEnemy);
                if (shotsToBoss-- > 0) {
                    long bossId = map.getActiveBossId(time);
                    if (bossId != -1) {
                        enemy = map.getItemById(bossId);
                    }
                }
                if (singleShot(time, seat, enemy, weaponId, results, false, scoreIdx, isBossTarget)) {
                    realNumberOfShots++;
                }

                if (realNumberOfShots == numberDamages) {
                    getLog().debug("shootWithSpecialWeaponAndUpdateState: account: {}, all damages is made from SW", seat.getAccountId());
                    break;
                }
            }
            getLog().debug("shootWithSpecialWeaponAndUpdateState: before compensation realNumberOfShots: {}, numberDamages: {}",
                    realNumberOfShots, numberDamages);
        }

        results.stream()
                .filter(this::isWCKilled)
                .findFirst()
                .ifPresent(result -> weaponCarrierNextSpawnTime = System.currentTimeMillis() + spawnConfig.getRandomWeaponCarrierSpawnTimeDelay());

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
                getLog().error("shootWithSpecialWeaponAndUpdateState: found shot results without main enemy, shot: {}, results: {} ", shot, results);
                throw new CommonException("Invalid shoot results");
            }
            return results;
        } else {

            int betLevel = seat.getBetLevel();

            if (weaponId == MathData.TURRET_WEAPON_ID) {
                getLog().debug("shootWithSpecialWeaponAndUpdateState: decrement ammo for regular weapon with betLevel: {}", betLevel);
                seat.decrementAmmoAmount(betLevel);
                seat.incrementBulletsFired();
            } else {
                if (shot.isPaidSpecialShot()) {
                    int multiplierPaidWeapons = 1;
                    int totalAmmoOfBet = multiplierPaidWeapons * betLevel;
                    seat.decrementAmmoAmount(totalAmmoOfBet);
                    getLog().debug("shootWithSpecialWeaponAndUpdateState: decrementAmmoAmount Base enemy: shot.getWeaponId(): {} multiplierPaidWeapons: {}",
                            shot.getWeaponId(), multiplierPaidWeapons);
                } else {
                    seat.consumeSpecialWeapon(weaponId);
                    getLog().debug("shootWithSpecialWeaponAndUpdateState: decrement ammo for levelUp weapon with betLevel: {}", betLevel);
                }
            }

            getLog().debug("shootWithSpecialWeaponAndUpdateState: shootResult: ");
            for (ShootResult shootResult : results) {
                getLog().debug(shootResult);
            }

            for (ShootResult result : results) {
                if (result.isDestroyed()) {
                    map.addRemoveTime((EnemyType) result.getEnemy().getEnemyClass().getEnemyType(), time);
                }
            }
        }

        return results;
    }

    private boolean isWCKilled(ShootResult shootResult) {
        return shootResult.isDestroyed() && shootResult.getEnemy() != null && shootResult.getEnemy().getLives() <= 0 &&
                WEAPON_CARRIERS_IDS.contains(shootResult.getEnemy().getEnemyClass().getEnemyType().getId());
    }

    private void addTestStandShotFeature(String sessionId) {
        TestStandLocal testStandLocal = TestStandLocal.getInstance();
        TestStandFeature featureById = testStandLocal.getPossibleFeatureById(6).copy();
        testStandLocal.addFeature(sessionId, featureById);
    }

    private List<ShootResult> createErrorResult(Seat seat, Enemy enemy, boolean invulnerable) {
        ShootResult result = new ShootResult(seat.getStake(), Money.INVALID, false, false, enemy, invulnerable);
        result.setMainShot(true);
        return Collections.singletonList(result);
    }

    private boolean singleShot(long time, Seat seat, Enemy enemy, int weaponId, List<ShootResult> results, boolean isMainShot, int scoreIdx, boolean isBossTarget) throws CommonException {
        boolean success = false;
        ShootResult result = shootToOneEnemy(time, seat, enemy, weaponId, isMainShot, scoreIdx, isBossTarget);
        if (!result.getWin().equals(Money.INVALID)) {
            results.add(result);
            success = true;
        }
        return success;
    }

    @Override
    protected void processShootResult(Seat seat, IShot shot, IShootResult result, ShotMessages messages,
                                      int awardedWeaponId, boolean isLastResult) {


        long enemyId = result.getEnemy() == null ? shot.getEnemyId() : result.getEnemyId();
        getLog().debug("processing result: {}, enemyId: {}", result, enemyId);

        int realShotTypeId = shot.getWeaponId();
        int usedSpecialWeapon = shot.getWeaponId();
        seat.getCurrentPlayerRoundInfo().updateAdditionalData(currentModel);

        int betLevel = seat.getBetLevel();
        int newShots = 0;
        if (result.getWeapon() != null) {
            newShots = result.getWeapon().getShots();
        }

        boolean mainShot = result.isMainShot();


        boolean isSpecialWeapon = shot.getWeaponId() != -1;
        int powerUpMult = 0;

        List<ITransportWeapon> awardedWeapons = result.getAwardedWeapons();
        boolean awardedPowerUpWeapons = awardedWeapons.stream().anyMatch(weapon -> (SpecialWeaponType.values()[weapon.getId()].isPowerUp()));
        boolean isPowerUpShot = isSpecialWeapon && SpecialWeaponType.values()[shot.getWeaponId()].isPowerUp();
        powerUpMult = isPowerUpShot || awardedPowerUpWeapons ? seat.getAdditionalTempCounters(AbstractActionSeat.ADD_COUNTER_POWER_UP_MULT) : 0;
        getLog().debug("result.isMainShot(): {}, isPowerUpShot: {}, powerUpMult: {} ", mainShot, isPowerUpShot, powerUpMult);

        boolean isPrize = !result.getPrize().isEmpty();
        boolean isWin = result.getWin().greaterThan(Money.ZERO);
        boolean isWeapon = awardedWeaponId != -1 || result.getNewFreeShotsCount() > 0;
        boolean isBossWin = result.isShotToBoss()
                && (result.getWin().greaterThan(Money.ZERO) || result.getKillAwardWin().greaterThan(Money.ZERO));
        boolean isAwardedWeapons = !result.getAwardedWeapons().isEmpty();
        boolean isGems = result.getGems() != null && !result.getGems().isEmpty() &&
                result.getGems().stream().reduce(0, Integer::sum) > 0;

        boolean isHit = isPrize || isWin || isWeapon || isBossWin || isAwardedWeapons || isGems;
        getLog().debug("isHit: {}, isPrize: {}, isWin: {}, isWeapon: {}, isBossWin: {}, isAwardedWeapons: {}, isGems: {}",
                isHit, isPrize, isWin, isWeapon, isBossWin, isAwardedWeapons, isGems);

        Map<Integer, List<IWinPrize>> hitResultBySeats = new HashMap<>();
        Map<Seat, IHit> messagesForSeatsLocal = new HashMap<>();
        IHit hitOwn = null;
        List<IHit> hitsForObserversLocal = new ArrayList<>();

        Money stake = (isSpecialWeapon || !mainShot) ? Money.ZERO : seat.getStake().getWithMultiplier(seat.getBetLevel());

        Money paidStake = Money.ZERO;
        boolean isPaidShotToBaseEnemy = shot.isPaidSpecialShot() && mainShot;
        if (isPaidShotToBaseEnemy) {
            stake = seat.getStake().getWithMultiplier(seat.getBetLevel());
            paidStake = new Money(stake.getValue());
        }

        getLog().debug("real stake: {}, isPaidShotToBaseEnemy: {}", stake.toDoubleCents(), isPaidShotToBaseEnemy);

        if (result.isKilledMiss() || result.isInvulnerable()) {
            seat.incrementMissCount();
            seat.getCurrentPlayerRoundInfo().addKilledMissCounter(shot.getWeaponId(), 1);
            if (isPaidShotToBaseEnemy) {
                getLog().debug("found main result of killedMiss, result: {}", result);
            }

            messages.add(
                    getTOFactoryService().createMiss(getCurrentTime(), TObject.SERVER_RID,
                            seat.getNumber(), result.isKilledMiss(), awardedWeaponId, enemyId, usedSpecialWeapon,
                            seat.getSpecialWeaponRemaining(), 0, isLastResult, shot.getX(), shot.getY(),
                            newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                            seat.getBetLevel(), 0, result.getEffects(), shot.getBulletId()),
                    getTOFactoryService().createMiss(getCurrentTime(), shot.getRid(),
                            seat.getNumber(), result.isKilledMiss(), awardedWeaponId, enemyId, usedSpecialWeapon,
                            seat.getSpecialWeaponRemaining(), 0, isLastResult, shot.getX(), shot.getY(),
                            newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                            seat.getBetLevel(), 0, result.getEffects(), shot.getBulletId()));

        } else {
            IEnemyClass enemyClass = result.getEnemy().getEnemyClass();
            IEnemyType enemyType = enemyClass.getEnemyType();
            String enemyNameKey = enemyType.getId() + "_" + enemyType.getName() + "_" + seat.getBetLevel();

            if (isHit) {
                seat.getCurrentPlayerRoundInfo().addHitCounter(seat.getCurrentWeaponId(), 1);
                seat.incrementHitsCount();
                IRoomEnemy enemy = gameRoom.convert((Enemy) result.getEnemy(), false);
                String title = isSpecialWeapon ? SpecialWeaponType.values()[realShotTypeId].getTitle() : null;

                if (result.isDestroyed()) {
                    incCurrentKilledEnemies();
                }

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
                            getLog().debug("enemy shared Win: seat accountId: {} enemyWinForSeat: {} seatId: {} additionalWins: {}",
                                    seatCurrent.getAccountId(), enemyWinForSeat, seatCurrent.getId(), additionalWins);
                            seatCurrent.incrementRoundWin(enemyWinForSeat);
                            seatCurrent.incrementShotTotalWin(enemyWinForSeat);
                            seatCurrent.addLastWin(enemyWinForSeat);
                            totalSimpleWin = totalSimpleWin.add(enemyWinForSeat);


                            if (killAwardWin.greaterThan(Money.ZERO)) {
                                getLog().debug("  KillAwardWin: {}", killAwardWin);
                                currentPlayerRoundInfo.updateAdditionalWin(result.isShotToBoss() ? "killBossAwardWind" : "KillAwardWin", killAwardWin);
                                seatCurrent.incrementRoundWin(killAwardWin);
                                seatCurrent.incrementShotTotalWin(killAwardWin);
                                seatCurrent.addLastWin(killAwardWin);
                                totalSimpleWin = totalSimpleWin.add(killAwardWin);
                            }

                            Money totalGemsPayout = result.getTotalGemsPayout();
                            if (totalGemsPayout.greaterThan(Money.ZERO)) {
                                currentPlayerRoundInfo.updateAdditionalWin("totalGemsPayout", totalGemsPayout);
                                seatCurrent.incrementRoundWin(totalGemsPayout);
                                seatCurrent.incrementShotTotalWin(totalGemsPayout);
                                seatCurrent.addLastWin(totalGemsPayout);
                                totalSimpleWin = totalSimpleWin.add(totalGemsPayout);
                            }
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
                            updateHitResultBySeats(seat.getNumber(), totalSimpleWin, "",
                                    hitResultBySeats,
                                    result.getAdditionalWins(), result.getAwardedWeapons());

                            result.setWin(win);

                            hitOwn = getTOFactoryService().createHit(getCurrentTime(), shot.getRid(), seat.getNumber(),
                                    result.getDamage(), result.getWin().toDoubleCents(), realAwardedWeaponId,
                                    usedSpecialWeapon, remainingSWShots, 0, enemy, isLastResult,
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
                            hitOwn.setEffects(result.getEffects());
                            hitOwn.setBossNumberShots(gameRoom.getBossNumberShots());
                            hitOwn.setGems(result.getGems());
                            hitOwn.setGemsPayout(result.getTotalGemsPayout().toDoubleCents());
                            if (powerUpMult != 0) {
                                hitOwn.setCurrentPowerUpMultiplier(powerUpMult);
                            }

                            getLog().debug("isSpecialWeapon {}, isLastResult: {}, stake: {}, title: {}, realStake: {}",
                                    isSpecialWeapon, isLastResult, seat.getStake(), title, stake);

                            seat.getCurrentPlayerRoundInfo().updateStatNew(stake, result.isShotToBoss(), isSpecialWeapon,
                                    title, enemyWinForSeat, result.isDestroyed(), enemyNameKey, paidStake);
                            seat.getCurrentPlayerRoundInfo().addDamage(result.getDamage());
                        } else {
                            IHit hit = getTOFactoryService().createHit(getCurrentTime(), SERVER_RID, seat.getNumber(), result.getDamage(),
                                    enemyWinForSeat.toDoubleCents(),
                                    realAwardedWeaponId, usedSpecialWeapon, remainingSWShots, 0, enemy, isLastResult, 0,
                                    result.getHvEnemyId(), shot.getX(), shot.getY(), realNewShots, result.isDestroyed(),
                                    result.getMineId(), result.getNewFreeShotsCount(), seat.getNumber(),
                                    result.isInstanceKill(), result.getChMult(), enemyId, shot.getEnemyId(),
                                    shot.getBulletId());
                            hit.setBetLevel(betLevel);
                            hit.setEffects(result.getEffects());
                            hit.setGems(result.getGems());
                            hit.setGemsPayout(result.getTotalGemsPayout().toDoubleCents());
                            hit.setBossNumberShots(gameRoom.getBossNumberShots());
                            hit.setKillBonusPay(killAwardWin.toDoubleCents());
                            if (powerUpMult != 0) {
                                hit.setCurrentPowerUpMultiplier(powerUpMult);
                            }
                            messagesForSeatsLocal.put(seatCurrent, hit);
                        }
                    }
                }

                updateHitResultBySeats(seat.getNumber(), Money.ZERO, null, hitResultBySeats,
                        new ArrayList<>(), null);

                // hit for observers
                IHit hitForObservers = getTOFactoryService().createHit(getCurrentTime(), SERVER_RID, seat.getNumber(), result.getDamage(), 0,
                        awardedWeaponId, usedSpecialWeapon, seat.getSpecialWeaponRemaining(),
                        0, enemy, isLastResult, 0,
                        result.getHvEnemyId(), shot.getX(), shot.getY(), newShots, result.isDestroyed(),
                        result.getMineId(), result.getNewFreeShotsCount(), seat.getNumber(),
                        result.isInstanceKill(), result.getChMult(), enemyId, shot.getEnemyId(), shot.getBulletId());

                hitForObservers.setKillBonusPay(killAwardWin.toDoubleCents());
                hitForObservers.setBetLevel(seat.getBetLevel());
                hitForObservers.setPaidSpecialShot(shot.isPaidSpecialShot());
                hitForObservers.setBetLevel(betLevel);
                hitForObservers.setEffects(result.getEffects());
                hitForObservers.setBossNumberShots(gameRoom.getBossNumberShots());
                hitsForObserversLocal.add(hitForObservers);

                if (result.isDestroyed()) {
                    addEnemyDestroyedMessage(messages, result.getEnemyId(), shot.getRid());

                    if (result.isShotToBoss()) {
                        gameRoom.setBossNumberShots(0);
                        switchSubround(PlaySubround.BASE);
                        getMap().setBossHP(0);
                        prevBossSpawnTime = System.currentTimeMillis();
                        getLog().debug("allow spawn after killing of boss, subround set to BASE");
                    }

                    if (EXPLODING_TOAD.equals(enemyType)) {
                        getLog().debug("Spawn tiny toads from exploding toad");
                        long delay = 1700;
                        if (shot.getWeaponId() == SpecialWeaponType.ArtilleryStrike.getId()) delay = 5200;
                        else if (shot.getWeaponId() == SpecialWeaponType.Flamethrower.getId()) delay = 2760;
                        else if (shot.getWeaponId() == SpecialWeaponType.Cryogun.getId()) delay = 2200;
                        else if (shot.getWeaponId() == SpecialWeaponType.Ricochet.getId()) delay = 3000;
                        else if (shot.getWeaponId() == SpecialWeaponType.Plasma.getId()) delay = 2800;
                        messages.addAllMessage(gameRoom.convertNewEnemies(getCurrentTime(),
                                getMap().spawnExplodedFrogs(result.getEnemy(), delay)));
                    }
                }

            } else {
                seat.getCurrentPlayerRoundInfo().addMissCounter(seat.getCurrentWeaponId(), 1);

                String title = isSpecialWeapon ? SpecialWeaponType.values()[realShotTypeId].getTitle() : null;
                seat.getCurrentPlayerRoundInfo().updateStatNew(stake, result.isShotToBoss(), isSpecialWeapon,
                        title, Money.ZERO, result.isDestroyed(), enemyNameKey, paidStake);

                seat.incrementMissCount();
                int specialWeaponRemaining = seat.getSpecialWeaponRemaining();
                IMiss allMiss = getTOFactoryService().createMiss(getCurrentTime(), SERVER_RID,
                        seat.getNumber(), false, awardedWeaponId, enemyId, usedSpecialWeapon,
                        specialWeaponRemaining, 0, isLastResult, shot.getX(), shot.getY(),
                        newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                        seat.getBetLevel(), 0, result.getEffects(), shot.getBulletId());
                IMiss ownMessage = getTOFactoryService().createMiss(getCurrentTime(), shot.getRid(),
                        seat.getNumber(), false, awardedWeaponId, enemyId, usedSpecialWeapon,
                        specialWeaponRemaining, 0, isLastResult, shot.getX(), shot.getY(),
                        newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                        seat.getBetLevel(), 0, result.getEffects(), shot.getBulletId());
                if (allMiss != null) {
                    allMiss.setBossNumberShots(gameRoom.getBossNumberShots());
                    ownMessage.setBossNumberShots(gameRoom.getBossNumberShots());
                }
                messages.add(allMiss, ownMessage);
            }
        }

        if (!shot.getBulletId().isEmpty()) {
            seat.removeBulletById(shot.getBulletId());
        }

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

    private void addEnemyDestroyedMessage(ShotMessages messages, long enemyId, int rid) {
        messages.add(
                getTOFactoryService().createEnemyDestroyed(getCurrentTime(), SERVER_RID,
                        enemyId, SIMPLE_SHOT.ordinal()),
                getTOFactoryService().createEnemyDestroyed(getCurrentTime(), rid,
                        enemyId, SIMPLE_SHOT.ordinal()));
    }

    private void updateHitResultBySeats(Integer seatId, Money win, String prize, Map<Integer,
            List<IWinPrize>> hitResultBySeats, List<Pair<Integer, Money>> additionalWin,
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
            PlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
            roundInfo.addWeaponSourceStat(WeaponSource.ENEMY.getTitle(),
                    SpecialWeaponType.values()[newWeapon.getType().getId()].getTitle(), weapon.getShots());
            roundInfo.addFreeShotsWon(newWeapon.getShots());
            getLog().debug("add weapons to seat aid {}, newWeapon  {} ", seat.getAccountId(), newWeapon);
        });
        getLog().debug("add weapons to seat aid {}, weapons after: {} ", seat.getAccountId(), seat.getWeapons());
    }

    public ShootResult shootToOneEnemy(long time, Seat seat, Enemy enemy, int weaponId, boolean isMainShot, int scoreIdx, boolean isBossTarget) throws CommonException {
        boolean isShotWithSpecialWeapon = weaponId != REGULAR_WEAPON;

        Money stake = Money.BG_STAKE;
        // killed earlier
        if (enemy == null) {
            return new ShootResult(isShotWithSpecialWeapon ? Money.ZERO : stake, Money.INVALID, false, false, null);
        }
        if (enemy.isInvulnerable(time)) {
            return new ShootResult(isShotWithSpecialWeapon ? Money.ZERO : stake, Money.INVALID, false, false, null, true);
        }

        getLog().debug("PlayerId: {}, enemyId: {}, enemyType: {}",
                seat.getPlayerInfo().getId(), enemy.getId(), enemy.getEnemyType().getName());

        ShootResult shootResult = gameRoom.getGame().doShoot(enemy, seat, stake, isBossRound(), getTOFactoryService(), isMainShot, scoreIdx, isBossTarget);

        if (shootResult.isBossShouldBeAppeared() && !isBossRound() && allowSpawn) {
            spawnBossTestStand = true;
            getLog().debug("Boss will be appeared later");
        }

        if (shootResult.isDestroyed() && shootResult.getEnemy().getLives() <= 0 &&
                STATIC_ENEMIES_IDS.contains(shootResult.getEnemy().getEnemyClass().getEnemyType().getId())) {
            PriorityQueue<Long> staticEnemiesDieOrDisappearTime = gameRoom.getStaticEnemiesDieOrDisappearTime();
            if (staticEnemiesDieOrDisappearTime.peek() != null) {
                staticEnemiesDieOrDisappearTime.remove();
            }
            staticEnemiesDieOrDisappearTime.add(System.currentTimeMillis());
        }

        getLog().debug("shootResult: {}", shootResult);
        checkEnemyKilled(shootResult);
        shootResult.setWeaponSurpluses(seat.getWeaponSurplus());
        return shootResult;
    }

    private void checkEnemyKilled(ShootResult shootResult) {
        IEnemy<EnemyClass, Enemy> enemy = shootResult.getEnemy();
        if (shootResult.isDestroyed()) {
            if (enemy.getLives() > 0) {
                getLog().debug("enemy {} lost a life, {} lives remaining",
                        enemy.getId(), enemy.getLives());
                enemy.setLives(enemy.getLives() - 1);
                enemy.setEnergy(enemy.getFullEnergy());
                shootResult.setDestroyed(false);
            } else {
                getLog().debug("enemy {} is killed", enemy.getId());
                getMap().removeItem(enemy.getId());
                getLog().debug("count of enemies after shoot: {}", getMap().getItemsSize());
                getLog().debug("getCountRemainingEnemiesByModel: {}", getCountRemainingEnemiesByModel());
            }
        }
    }

    @Override
    public void restoreGameRoom(GameRoom gameRoom) throws CommonException {
        super.restoreGameRoom(gameRoom);
        lockShots = new ReentrantLock();
    }

    @Override
    public boolean isBossRound() {
        return subround.equals(BOSS);
    }

    @Override
    public Map<Long, Integer> getFreezeTimeRemaining() {
        return getMap().getAllFreezeTimeRemaining(FREEZE_TIME_MAX);
    }

    @Override
    protected void generateHVEnemy(ShootResult result, ShotMessages messages, String sessionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void shootWithRegularWeapon(long time, Seat seat, IShot shot) {
        // As in this game we can hit multiple targets from regular weapon, we use the same logic as for special weapons
        throw new UnsupportedOperationException();
    }

    @Override
    protected ShootResult shootToOneEnemy(long time, Seat seat, Long itemIdForShot, int weaponId, boolean isNearLandMine,
                                          double damageMultiplier) {
        throw new UnsupportedOperationException();
    }

    private Pair<EnemyType, Trajectory> getEnemyWithPredefinedTrajectory(SpawnConfig spawnConfig) {
        return getInitialSpawnList(spawnConfig).isEmpty() ? getRandomEnemyWithPredefinedTrajectory(spawnConfig) : getInitialSpawnList(spawnConfig).remove(0);
    }

    private Pair<EnemyType, Trajectory> getRandomEnemyWithPredefinedTrajectory(SpawnConfig spawnConfig) {
        return GameTools.getRandomNumberKeyFromMapWithNorm(
                MathData.calculateCurrentSpawnWeights(getMap().getEnemyWithPredefinedTrajectoryPairs(spawnConfig),
                        getMap().getCurrentEnemiesLocations(), startRoundTime, getSpawnStage(spawnConfig)));
    }

    private List<SpawnStageFromConfig> getSpawnStage(SpawnConfig spawnConfig) {
        List<SpawnStageFromConfig> initSpawnStage = new ArrayList<>();
        for (int i = 1; i < spawnConfig.getTimeSlices().size(); i++) {
            initSpawnStage.add(new SpawnStageFromConfig(spawnConfig.getTimeSlices().get(i) * 1000L, convertWeightsForTimeSlice(spawnConfig.getWeightsByTimeSlice().get(i - 1))));
        }
        return initSpawnStage;
    }

    private List<Double> convertWeightsForTimeSlice(List<Integer> weights) {
        List<Double> resultList = new ArrayList<>();
        for (int weight : weights) {
            resultList.add((double) weight / 100);
        }
        return resultList;
    }

    private List<Pair<EnemyType, Trajectory>> getInitialSpawnList(SpawnConfig spawnConfig) {
        if (initialSpawnList == null || getMap().getItemsSize() == 0) {
            initialSpawnList = initializeSpawnList(MathData.calculateInitialSpawnWeights(
                            getMap().getEnemyWithPredefinedTrajectoryPairs(spawnConfig), buildInitialWeightsFromConfig(spawnConfig),
                            buildWeightDividers(spawnConfig.getDividers())),
                    getRoom().getGame().getSpawnConfig(getRoom().getId()).getInitialSpawnEnemies());
        }
        return initialSpawnList;
    }

    private Map<EnemyRange, double[]> buildInitialWeightsFromConfig(SpawnConfig spawnConfig) {
        return ImmutableMap.of(
                LOW_PAY_ENEMIES, convertWeights(spawnConfig.getLowPayWeights()),
                MID_PAY_ENEMIES, convertWeights(spawnConfig.getMidPayWeights()),
                HIGH_PAY_ENEMIES, convertWeights(spawnConfig.getHighPayWeights()));
    }

    private double[] convertWeights(List<Double> weights) {
        List<Double> converted = new ArrayList<>();
        for (Double weight : weights) {
            converted.add(weight / 100);
        }
        return converted.stream()
                .mapToDouble(Double::doubleValue).toArray();
    }

    private int[] buildWeightDividers(List<Integer> weightDividers) {
        return weightDividers.stream()
                .mapToInt(Integer::intValue).toArray();
    }

    private List<Pair<EnemyType, Trajectory>> initializeSpawnList(Map<Pair<EnemyType, Trajectory>, Double> spawnWeights,
                                                                  int limit) {
        List<Pair<EnemyType, Trajectory>> spawnList = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            spawnList.add(GameTools.getRandomNumberKeyFromMapWithNorm(spawnWeights));
        }
        return spawnList;
    }

    @Override
    protected PlayGameState getDeserializer() {
        return this;
    }
}
