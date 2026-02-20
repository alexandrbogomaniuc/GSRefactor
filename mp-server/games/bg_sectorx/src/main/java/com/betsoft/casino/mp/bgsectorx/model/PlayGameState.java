package com.betsoft.casino.mp.bgsectorx.model;

import com.betsoft.casino.mp.bgsectorx.model.math.config.GameConfig;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.battleground.IBattleScoreInfo;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.movement.HybridTrajectory;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.bgsectorx.model.math.*;
import com.betsoft.casino.mp.bgsectorx.model.math.config.*;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.betsoft.casino.teststand.TeststandConst;
import com.betsoft.casino.utils.TInboundObject;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringUtils;

import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.model.EnemyDestroyReason.REMOVED_ON_SERVER;
import static com.betsoft.casino.mp.model.EnemyDestroyReason.SIMPLE_SHOT;
import static com.betsoft.casino.mp.model.Money.BG_STAKE;
import static com.betsoft.casino.mp.model.PlaySubround.BOSS;
import static com.betsoft.casino.mp.bgsectorx.model.math.EnemyRange.*;
import static com.betsoft.casino.mp.bgsectorx.model.math.EnemyType.*;
import static com.betsoft.casino.mp.utils.ErrorCodes.*;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

@SuppressWarnings("Duplicates")
public class PlayGameState extends AbstractBattlegroundPlayGameState<GameRoom, Seat, GameMap, PlayGameState> {
    private transient long timeOfRoundFinishSoon;
    private transient List<Pair<EnemyType, Trajectory>> initialSpawnList;
    private transient PriorityQueue<Long> staticEnemiesDieOrDisappearTime = new PriorityQueue<>();
    private transient boolean spawnBossTestStand = false;
    protected static final int FREEZE_TIME = 8745;
    protected static final int FREEZE_TIME_TOTAL = 10000;
    private static final int MAX_HUGE_ENEMY_ITEMS_ON_SCREEN_ALLOWED = 1;
    private static final int MIN_INITIAL_WAVE_ALIVE_ENEMIES = 7;
    private transient long lastFreezeTime = -1;
    private transient long timeRestored = -1;
    private transient Trajectory initBossTrajectory;
    private transient boolean isCrossWaveActive = false;
    private transient boolean isFormationSpawnActive = false;
    private transient long lastSpiralWaveSpawnTime = 0;
    private transient int initialWaveTypeNum;
    private transient long initialWaveEndTime = 0;
    private transient int bossSection = -1;
    private transient int countFormationMobs = 0;
    private transient int countSpiralWaves = 0;
    private transient int bossTrajectoryCounter = 0;
    private transient List<List<Enemy>> crossEnemies = new ArrayList<>();
    private transient List<Long> temporalActiveEnemiesIds = new ArrayList<>();
    private transient String SEAT_FREEZE_WIN_AMOUNT = "SEAT_FREEZE_WIN_AMOUNT";
    private transient final String COUNTER_SPECIAL_ITEMS = "COUNTER_SPECIAL_ITEMS";
    private transient final String TIMES_SPECIAL_ITEMS = "TIMES_SPECIAL_ITEMS";
    private transient final String TIMES_HUGE_ENEMIES = "TIMES_HUGE_ENEMIES";
    private transient final String COUNTER_HUGE_ENEMIES = "COUNTER_HUGE_ENEMIES";
    private transient long prevSpecialItemSpawnTime = 0;
    private transient long prevHugeEnemySpawnTime = 0;
    private transient boolean isInitialWaveSpawned = false;
    private transient int hugeEnemyCounter = 0;
    private transient boolean isGameRestored = false;
    private transient long timeAfterReboot = 0;
    transient int swarmIdCurrent = 0;
    private transient List<Enemy> initialWaveEnemies = new ArrayList<>();
    private transient long lastTimeBossShot = 0;
    private transient long timeInterval = 0;
    private transient int freezeChMult = 1;
    private transient int betLevelF2Shot = 1;

    public PlayGameState() {
        super();
    }

    public PlayGameState(GameRoom gameRoom) {
        super(gameRoom, null);
    }

    @Override
    public void init() throws CommonException {
        super.init();
        getRoom().setBossNumberShots(0);
        staticEnemiesDieOrDisappearTime.clear();
        spawnBossTestStand = false;
        clearAdditionalActionCounters();
        clearAdditionalActionSpawnTimes();
        isGameRestored = false;
    }

    public boolean allowChangeBetLevel(Seat account) {
        return lastFreezeTime == -1 || account.getAdditionalTempCounters(SEAT_FREEZE_WIN_AMOUNT) == 0;
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
        if (needForceFinishRound && lastFreezeTime == -1) {
            nextSubRound();
            return;
        }
        if (needWaitingWhenEnemiesLeave && System.currentTimeMillis() > timeOfRoundFinishSoon + 2000 && lastFreezeTime == -1) {
            destroyBaseEnemies();
            getLog().debug("no live enemies in room, finish by timeOfRoundFinishSoon");
            nextSubRound();
            return;
        }

        SpawnConfig spawnConfig = getRoom().getGame().getSpawnConfig(getRoom().getId());
        NewBossParams bossParams = spawnConfig.getBossParams();
        long currentTimeMillis = System.currentTimeMillis();

        if (lastFreezeTime > 0 && ((currentTimeMillis - lastFreezeTime > FREEZE_TIME_TOTAL) || noAnyEnemiesInRound())) {
            getLog().debug("Need check freeze unpaid freeze win for seat, lastFreezeTime: {}, currentTimeMillis: {} ",
                    lastFreezeTime, currentTimeMillis);
            gameRoom.getSeats().forEach(seat -> {
                int additionalFreezeWin = seat.getAdditionalTempCounters(SEAT_FREEZE_WIN_AMOUNT);
                if (additionalFreezeWin > 0) {
                    Pair<List<Integer>, List<IBattleScoreInfo>> currentKingOfHillSeatId = gameRoom.getCurrentKings();
                    List<Integer> kingIdForBattleGroundOld = currentKingOfHillSeatId.getKey();

                    getLog().debug("kingIdForBattleGroundOld: {} ", kingIdForBattleGroundOld);

                    seat.resetAdditionalTempCounter(SEAT_FREEZE_WIN_AMOUNT);
                    int betLevel = betLevelF2Shot;
                    Money payout = BG_STAKE.getWithMultiplier(additionalFreezeWin * betLevel);
                    getLog().debug("additionalFreezeWin: {} found for seat (accountId): {}, payout cents): {}, betLevel: {} ",
                            additionalFreezeWin, seat.getAccountId(), payout.toCents(), betLevel);
                    seat.incrementRoundWin(payout);
                    seat.getCurrentPlayerRoundInfo().addTotalPayouts(payout);
                    if (!isFRB()) {
                        try {
                            seat.transferWinToAmmo();
                        } catch (CommonException e) {
                            getLog().warn(" transferWinToAmmo exception: ", e);
                        }
                    }
                    String enemyNameKey = F2.getId() + "_" + F2.getName() + "_" + betLevelF2Shot;
                    boolean isSpecialWeapon = betLevelF2Shot != 1;
                    String title = isSpecialWeapon ? SpecialWeaponType.values()[16].getTitle() : null;
                    seat.getCurrentPlayerRoundInfo().updateKillAwardWinWithLevelUp(payout, enemyNameKey, isSpecialWeapon, title);
                    //seat.getCurrentPlayerRoundInfo().updateKillAwardWin(payout, enemyNameKey);
                    ISeatWinForQuest seatWinForQuest = getTOFactoryService().createSeatWinForQuest(currentTimeMillis, SERVER_RID, seat.getNumber(), F2.getId(),
                            payout.toCents(), -1);
                    gameRoom.sendChanges(seatWinForQuest);
                    freezeChMult = 1;
                    gameRoom.sentBattlegroundMessageToPlayers(kingIdForBattleGroundOld, SERVER_RID);
                }
            });
            lastFreezeTime = -1;
        }

        if (lastFreezeTime > 0 || needWaitingWhenEnemiesLeave) {
            return;
        }

        long currentTime;
        if (System.currentTimeMillis() > startRoundTime + gameRoom.getRoundDuration() * 1000L && !isGameRestored) {
            timeAfterReboot = System.currentTimeMillis();
            currentTime = currentTimeMillis - timeAfterReboot;
        } else if (System.currentTimeMillis() > startRoundTime + gameRoom.getRoundDuration() * 1000L && isGameRestored) {
            currentTime = currentTimeMillis - timeAfterReboot;
        } else {
            currentTime = currentTimeMillis - startRoundTime;
        }
        long nextBossSpawn = getBossSPawnTimeByBossNumber(spawnedBossesCounter, bossParams);
        if (PlaySubround.BASE.equals(subround)) {
            boolean baseCase = spawnBossTestStand || (nextBossSpawn != -1 && nextBossSpawn < currentTime && bossParams.getMaxBosses() > spawnedBossesCounter &&
                    (prevBossSpawnTime == 0 || (System.currentTimeMillis() > prevBossSpawnTime + (bossParams.getTa() * 1000L) && nextBossSpawn <= bossParams.getT2() * 1000L)));
            boolean bossIsLiveOrOut = getMap().isEnemyRemoved(EnemyType.BOSS) || getMap().getAnyBossId() != -1;
            if (!bossIsLiveOrOut && baseCase) {
                switchSubround(BOSS);
                int bossForRoom = spawnBossTestStand ? TestStandLocal.getInstance().getBossForRoom(getRoom().getId()) : -1;
                BossType bossType = (spawnBossTestStand && bossForRoom != -1) ? BossType.getBySkinId(bossForRoom) :
                        MathData.getRandomBossType(getRoom().getGame().getConfig(getRoom().getId()), getRoom().getSeatsCount());
                try {
                    initBossTrajectory = getMap().getBossFirstTrajectory(spawnConfig);
                    spawnBoss(bossType, initBossTrajectory);
                } catch (Exception e) {
                    getLog().debug("generate boss error: ", e);
                }
                spawnedBossesCounter++;
                spawnBossTestStand = false;
            } else {
                tryGenerateEnemies();
            }
        } else {
            Enemy enemy = getMap().checkReturnedBoss();
            boolean bossIsRemoved = getMap().isEnemyRemoved(EnemyType.BOSS);
            if (enemy != null) {
                enemy.setTrajectory(getMap().getRandomTrajectory(EnemyType.BOSS, spawnConfig, false, null));
                getLog().debug("boss re-enter to map, boss: {} ", enemy);
                gameRoom.sendNewEnemyMessage(enemy);
            } else if (!bossIsRemoved) {
                if (getMap().updateBossRound()) {
                    switchSubround(PlaySubround.BASE);
                    prevBossSpawnTime = System.currentTimeMillis();
                }
                tryGenerateEnemies();
                if (nextBossSpawn != -1 && currentTime > nextBossSpawn) {
                    bossesSpawnTime.remove(spawnedBossesCounter);
                }
            }
        }
    }

    private long getHugeEnemySpawnTimeByNumber(int hugeEnemiesNumber, HugePayItemsParams params) {
        List<Long> hugeEnemiesTimes = getAdditionalActionSpawnTimes().get(TIMES_HUGE_ENEMIES);

        if (hugeEnemiesTimes == null) {
            List<Long> triggerTimeSlots = new ArrayList<>();
            int t0 = RNG.nextInt(params.getT0(), params.getT1());
            triggerTimeSlots.add((long) t0 * 1000);
            double dt = 0.5;
            double alpha = -(Math.log(params.getDelta()) / (90 - params.getT1() - params.getTa()));
            for (double t = t0 + params.getTa(); t <= 90; t += dt) {
                double r = RNG.rand();
                double funcSpecialRes = params.getA() * Math.exp(-alpha * (t - params.getT1() - params.getTa()));
                if (r < funcSpecialRes) {
                    triggerTimeSlots.add((long) t * 1000);
                }
            }
            addAdditionalActionSpawnTimes(TIMES_HUGE_ENEMIES, triggerTimeSlots);
            getLog().debug("init getAdditionalActionSpawnTimes(): {}", getAdditionalActionSpawnTimes());
        }
        boolean needCalculate = hugeEnemiesTimes != null && hugeEnemiesTimes.size() > hugeEnemiesNumber
                && params.getA() > 0;

        return needCalculate ? hugeEnemiesTimes.get(hugeEnemiesNumber) : -1;
    }

    private long getSpecialItemsSpawnTimeByNumber(int specialItemsNumber, SpecialItemsParams params) {
        List<Long> specialItemsTimes = getAdditionalActionSpawnTimes().get(TIMES_SPECIAL_ITEMS);

        if (specialItemsTimes == null) {
            List<Long> triggerTimeSlots = new ArrayList<>();
            int t0 = RNG.nextInt(params.getT0(), params.getT1());
            triggerTimeSlots.add((long) t0 * 1000);
            double dt = 0.5;
            double alpha = -(Math.log(params.getDelta()) / (90 - params.getT1() - params.getTa()));
            for (double t = t0 + params.getTa(); t <= 90; t += dt) {
                double r = RNG.rand();
                double funcSpecialRes = params.getA() * Math.exp(-alpha * (t - params.getT1() - params.getTa()));
                if (r < funcSpecialRes) {
                    triggerTimeSlots.add((long) t * 1000);
                }
            }
            addAdditionalActionSpawnTimes(TIMES_SPECIAL_ITEMS, triggerTimeSlots);
            getLog().debug("getAdditionalActionSpawnTimes(): {}", getAdditionalActionSpawnTimes());
        }
        return specialItemsTimes != null && specialItemsTimes.size() > specialItemsNumber
                && params.getA() > 0 ? specialItemsTimes.get(specialItemsNumber) : -1;
    }


    private long getBossSPawnTimeByBossNumber(int bossNumber, NewBossParams bossParams) {
        if (bossesSpawnTime == null) {
            List<Long> triggerTimeSlots = new ArrayList<>();
            double dt = 0.1;
            double t0 = 0;
            double alpha = 0;
            double b = 0;
            double delta = bossParams.getDelta();
            boolean firstBossTriggered = false;
            for (double t = bossParams.getT1(); t <= bossParams.getT2(); t += dt) {
                double r = RNG.rand();
                if (!firstBossTriggered && r < f(t, bossParams.getSigma(), bossParams.getMu(), bossParams.getA())) {
                    t0 = t;
                    triggerTimeSlots.add((long) t0 * 1000);
                    b = f(t, bossParams.getSigma(), bossParams.getMu(), bossParams.getA()) * bossParams.getLambda();
                    if (b < delta) {
                        alpha = 1;
                    } else {
                        alpha = -((Math.log(delta / b)) / (bossParams.getT2() - t0));
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
            generateEnemiesBySpawnLogic();
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

    int getNextSwarmId() {
        if (swarmIdCurrent + 1 == Integer.MAX_VALUE) {
            swarmIdCurrent = 0;
        } else {
            swarmIdCurrent++;
        }
        return swarmIdCurrent;
    }


    private void generateEnemiesBySpawnLogic() {
        GameMap map = getMap();
        GameConfig config = getRoom().getGame().getConfig(getRoom().getId());
        SpawnConfig spawnConfig = getRoom().getGame().getSpawnConfig(getRoom().getId());

        map.update();

        checkTestStandFeatures();
        getMap().checkFreezeTimeEnemies(FREEZE_TIME);

        List<Pair<Integer, Boolean>> itemsTypeIdsAndSwarmState = map.getItemsTypeIdsAndSwarmState();

        if (isNeedMinimalEnemies() && itemsTypeIdsAndSwarmState.size() > 3) {
            return;
        }

        if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            getLog().debug("no live enemies in room, finish");
            nextSubRound();
            return;
        }

        if (allowSpawn && startRoundTime != 0 && !isManualGenerationEnemies()) {

            boolean initialWavesFinished = initialWavesFinished();
            boolean isInitialWaveAlive = isInitialWaveAlive();
            long initialWaveAliveEnemiesCount =  updateAndCountInitialWaveAliveEnemies();
            boolean isMinInitialWaveAliveEnemies = isInitialWaveAlive ? initialWaveAliveEnemiesCount <= MIN_INITIAL_WAVE_ALIVE_ENEMIES : false;

            getLog().debug("generateEnemiesBySpawnLogic: initialWavesFinished={}, isInitialWaveAlive={}, initialWaveAliveEnemiesCount={}," +
                            " isMinInitialWaveAliveEnemies={}",
                    initialWavesFinished, isInitialWaveAlive, initialWaveAliveEnemiesCount, isMinInitialWaveAliveEnemies);

            if (initialWavesFinished || isMinInitialWaveAliveEnemies) {
                createRegularWave(map, config, spawnConfig);
            }

            if (!isCrossWaveActive) {
                isCrossWaveActive = true;
            }

            createInitialWave(spawnConfig);
        }
    }

    private void createRegularWave(GameMap map, GameConfig config, SpawnConfig spawnConfig) {
        List<Double> spawnConfigEntitiesWeights = spawnConfig.getEntitiesWeights();
        int idx = GameTools.getIndexFromDoubleProb(spawnConfigEntitiesWeights.stream().mapToDouble(Double::doubleValue).toArray());

        if (idx < spawnConfigEntitiesWeights.size() - 1) {
            HashSet<Integer> existSwarmIds = new HashSet<>();
            for(Enemy enemy : map.getItems()) {
                if (enemy.getSwarmType() == idx + 1) {
                    existSwarmIds.add(enemy.getSwarmId());
                }
            }

            int sizeRequiredSwarms = existSwarmIds.size();
            getLog().debug("createRegularWave: idx={}, existSwarmIds={} ", idx, existSwarmIds);

            switch (idx) {
                case 0:
                    if (sizeRequiredSwarms < spawnConfig.getMaxNumOfTemporalFormationsPerScreen()) {
                        createTemporalFormation(spawnConfig);
                    }
                    break;
                case 1:
                    if (sizeRequiredSwarms < spawnConfig.getMaxNumOfSpatialFormationsPerScreen()) {
                        createSpatialFormation(spawnConfig);
                    }
                    break;
                case 2:
                    if (sizeRequiredSwarms < spawnConfig.getMaxNumOfClusterFormationsPerScreen()) {
                        createClusterFormation(spawnConfig);
                    }
                    break;
                case 3:
                    if (sizeRequiredSwarms < spawnConfig.getMaxNumOfHybridFormationsPerScreen()) {
                        createHybridFormation(spawnConfig);
                    }
                    break;
                default:
                    break;
            }
        } else {
            int count = (int) map.getItems().stream().filter(enemy -> enemy.getSwarmType() == 0).count();
            int diff = Math.min(spawnConfig.getMaxEntites() - count, 5);
            if (diff > 0) {
                do {
                    checkAndSpawnUsualItems(config, spawnConfig, map);
                } while (diff-- > 0);
            }
        }
        checkAndSpawnSpecialItems(config, spawnConfig, map);
        checkAndSpawnHugeEnemy(config, spawnConfig, map);
    }

    private void createInitialWave(SpawnConfig spawnConfig) {
        if (gameRoom.getUsedWaves().size() == 6) {
            gameRoom.getUsedWaves().clear();
        }

        getLog().debug("createInitialWave: isInitialWaveSpawned={}, gameRoom.getUsedWaves().size()={}",
                isInitialWaveSpawned, gameRoom.getUsedWaves().size());

        if (!isInitialWaveSpawned) {
            initialWaveTypeNum = GameTools.getIndexFromDoubleProb(spawnConfig.getWeightToPickInitialWave().stream().mapToDouble(Double::doubleValue).toArray());
            if (gameRoom.getUsedWaves().containsKey(initialWaveTypeNum)) {
                initialWaveTypeNum = GameTools.getIndexFromDoubleProb(spawnConfig.getWeightToPickInitialWave().stream().mapToDouble(Double::doubleValue).toArray());
            }
            gameRoom.setUsedWave(initialWaveTypeNum, true);
            getMap().clearSpiralWaveParams();
            isInitialWaveSpawned = true;
        }

        getLog().debug("createInitialWave: isInitialWaveSpawned={}, initialWaveTypeNum={}, gameRoom.getUsedWaves().size()={}",
                isInitialWaveSpawned, initialWaveTypeNum, gameRoom.getUsedWaves().size());

        List<Enemy> enemies = spawnInitialWave(spawnConfig, initialWaveTypeNum);

        if (enemies != null) {
            if (initialWaveTypeNum == 0 || initialWaveTypeNum == 1) { //Spiral Wave up to 3 cycles
                if(initialWaveEnemies == null) initialWaveEnemies = new ArrayList<>();
                initialWaveEnemies.addAll(enemies);
            } else {
                initialWaveEnemies = enemies;
            }
        }
    }

    private boolean isInitialWaveAlive() {
        List<Enemy> mapEnemies = getMap().getItems();
        if (!isInitialWaveSpawned || initialWaveEnemies == null  || initialWaveEnemies.size() == 0 || mapEnemies == null) {
            return false;
        }

        return initialWaveEnemies.stream()
                .anyMatch(
                        enemy -> mapEnemies.contains(enemy)
                );
    }

    private long updateAndCountInitialWaveAliveEnemies() {
        List<Enemy> mapEnemies = getMap().getItems();
        if(!isInitialWaveSpawned || initialWaveEnemies == null || initialWaveEnemies.size() == 0 || mapEnemies == null) {
            return 0;
        }

        List<Enemy> initialWaveAliveEnemies = initialWaveEnemies.stream()
                .filter(enemy -> mapEnemies.contains(enemy))
                .collect(Collectors.toList());

        getLog().debug("updateAndCountInitialWaveAliveEnemies: initialWaveEnemies.size={}, initialWaveAliveEnemies.size()={}. " +
                "Update initialWaveEnemies by initialWaveAliveEnemies", initialWaveEnemies.size(), initialWaveAliveEnemies.size());

        initialWaveEnemies = initialWaveAliveEnemies;
        return initialWaveEnemies.size();
    }

    private void checkAndSpawnUsualItems(GameConfig config, SpawnConfig spawnConfig, GameMap map) {
        Pair<EnemyType, Trajectory> enemyPathPair = map.getRandomTrajectoryFromSpawnSystem(startRoundTime, config, spawnConfig);
        if (enemyPathPair != null) {
            spawnEnemyWithTrajectory(enemyPathPair.getKey(), enemyPathPair.getValue());
        }
    }

    private void checkAndSpawnHugeEnemy(GameConfig config, SpawnConfig spawnConfig, GameMap map) {
        Map<Integer, Integer> counter = map.countEnemyTypes();

        AtomicInteger hugeEnemiesOnScreenCounter = new AtomicInteger();

        counter.forEach((typeId, count) -> {
            if (HUGE_PAY_ENEMIES.contains(EnemyType.getById(typeId))) {
                hugeEnemiesOnScreenCounter.incrementAndGet();
            }
        });

        HugePayItemsParams hugePayItemsParams = spawnConfig.getHugePayEnemiesParams();
        int maxItemsForRound = hugePayItemsParams.getMaxItems();

        boolean allowSpawnHugeEnemy = true;
        if (hugeEnemiesOnScreenCounter.get() >= MAX_HUGE_ENEMY_ITEMS_ON_SCREEN_ALLOWED || hugeEnemyCounter >= maxItemsForRound) {
            allowSpawnHugeEnemy = false;
        }

        if (allowSpawnHugeEnemy) {
            Integer enemyIdForRoom = TestStandLocal.getInstance().getEnemyIdForRoom(gameRoom.getId());
            if (enemyIdForRoom == 8 || enemyIdForRoom == 9 || enemyIdForRoom == 10) {
                int hugePayItemId = B3.getId() + enemyIdForRoom - 8;
                getLog().debug("generate from teststand enemyIdForRoom {}, hugeEnemiesNumber.get(): {}, hugePayItemId: {} ",
                        enemyIdForRoom, hugeEnemiesOnScreenCounter.get(), hugePayItemId);
                EnemyType enemyType = EnemyType.getById(hugePayItemId);
                spawnHugePayEnemy(spawnConfig, map, enemyType);
                hugeEnemyCounter++;
                TestStandLocal.getInstance().removeEnemyIdForRoom(gameRoom.getId());
            } else {
                long currentTime = System.currentTimeMillis() - startRoundTime;
                Integer hugeEnemiesCount = getAdditionalActionCounters().get(COUNTER_HUGE_ENEMIES);
                if (hugeEnemiesCount == null) {
                    hugeEnemiesCount = 0;
                }
                long nextHugeEnemySpawn = getHugeEnemySpawnTimeByNumber(hugeEnemiesCount, hugePayItemsParams);

                boolean b1 = nextHugeEnemySpawn < currentTime;
                boolean b2 = System.currentTimeMillis() > (prevHugeEnemySpawnTime + (hugePayItemsParams.getTa() * 1000L));
                boolean needSpawnHugeEnemy = (nextHugeEnemySpawn != -1 && b1 && (prevHugeEnemySpawnTime == 0 || b2));

                if (needSpawnHugeEnemy) {
                    long spawnTime = System.currentTimeMillis();
                    prevHugeEnemySpawnTime = spawnTime;

                    /*if (hugeEnemiesNumber.get() >= 1) {
                        getLog().debug("can`t spawn huge-pay enemy, hugeEnemiesNumber.get(): {}", hugeEnemiesNumber.get());
                        return;
                    }*/

                    List<Double> doubleList = new ArrayList<>();
                    spawnConfig.getHugePayItemsPickWeights().stream().map(m -> (m / 100)).forEach(doubleList::add);

                    int rndTypeNum = GameTools.getIndexFromDoubleProb(doubleList.stream().mapToDouble(Double::doubleValue).toArray()) + B3.getId();

                    EnemyType enemyTypeForSpawn = EnemyType.getById(rndTypeNum);
                    addAdditionalActionCounter(COUNTER_HUGE_ENEMIES, hugeEnemiesCount + 1);
                    spawnHugePayEnemy(spawnConfig, map, enemyTypeForSpawn);
                    hugeEnemyCounter++;
                }
            }
        }
    }

    private void spawnHugePayEnemy(SpawnConfig spawnConfig, GameMap map, EnemyType enemyTypeForSpawn) {
        int swarmId = getNextSwarmId();
        Enemy hugePayEnemy;
        List<Point> hybridPoints = null;
        if (enemyTypeForSpawn.equals(B3)) {
            Trajectory trajectory = map.getRandomTrajectory(enemyTypeForSpawn, spawnConfig);
            HybridTrajectory hybridTrajectory = new HybridTrajectory(0, trajectory.getPoints());
            hugePayEnemy = getMap().addEnemyWithTrajectory(enemyTypeForSpawn, hybridTrajectory);
            hugePayEnemy.addToSwarm(SwarmType.HugeEnemy.getTypeId(), swarmId);
            gameRoom.sendNewEnemyMessage(hugePayEnemy);
            hybridPoints = hugePayEnemy.getTrajectory().getPoints();
        } else {
            Trajectory trajectory = map.getRandomTrajectory(enemyTypeForSpawn, spawnConfig);
            hugePayEnemy = spawnEnemyWithTrajectory(enemyTypeForSpawn, trajectory);
        }

        List<Enemy> enemies = new ArrayList<>();
        if (enemyTypeForSpawn.equals(B3) && hybridPoints != null) {
            double hugeSpeed = hugePayEnemy.getSpeed();
            int jellyPartAngel = 360 / 8;
            int sumJellyAngles = 0;
            for (int i = 0; i < 8; i++) {
                List<Point> newPoints = new ArrayList<>();
                for (Point point : hybridPoints) {
                    newPoints.add(new Point(point.getX(), point.getY(), point.getTime()));
                }
                Trajectory trajectory = new HybridTrajectory(0, newPoints, sumJellyAngles, true, false, true);
                sumJellyAngles += jellyPartAngel;
                Enemy jellyEnemy = getMap().addEnemyWithTrajectory(S5, trajectory, hugePayEnemy.getId());
                jellyEnemy.addToSwarm(SwarmType.HugeEnemy.getTypeId(), swarmId);
                jellyEnemy.setParentEnemyId(hugePayEnemy.getId());
                jellyEnemy.setParentEnemyTypeId(hugePayEnemy.getEnemyType().getId());
                jellyEnemy.setSpeed(hugeSpeed);
                enemies.add(jellyEnemy);
            }

            int flyerPartAngel = 360 / 14;
            int sumFlyerAngles = 0;
            int rndFlyerType = RNG.nextInt(S1.getId(), S4.getId() + 1);
            for (int i = 0; i < 14; i++) {
                List<Point> newPoints = new ArrayList<>();
                for (Point point : hybridPoints) {
                    newPoints.add(new Point(point.getX(), point.getY(), point.getTime()));
                }
                Trajectory trajectory = new HybridTrajectory(0, newPoints, sumFlyerAngles, true, true, false);
                sumFlyerAngles += flyerPartAngel;
                Enemy flyerEnemy = getMap().addEnemyWithTrajectory(EnemyType.getById(rndFlyerType), trajectory, hugePayEnemy.getId());
                flyerEnemy.addToSwarm(SwarmType.HugeEnemy.getTypeId(), swarmId);
                flyerEnemy.setParentEnemyId(hugePayEnemy.getId());
                flyerEnemy.setParentEnemyTypeId(hugePayEnemy.getEnemyType().getId());
                flyerEnemy.setSpeed(hugeSpeed);
                enemies.add(flyerEnemy);
            }
            for (Enemy enemy : enemies) {
                gameRoom.sendNewEnemyMessage(enemy);
            }
        }
    }

    private void checkAndSpawnSpecialItems(GameConfig config, SpawnConfig spawnConfig, GameMap map) {
        Map<Integer, Integer> counter = map.countEnemyTypes();

        AtomicBoolean foundFreezeEnemy = new AtomicBoolean(false);
        AtomicInteger enemySpecialsNumbers = new AtomicInteger();

        counter.forEach((typeId, count) -> {
            if (SPECIAL_ITEMS.getEnemies().contains(EnemyType.getById(typeId))) {
                if (typeId == F2.getId()) {
                    foundFreezeEnemy.set(true);
                }
                enemySpecialsNumbers.incrementAndGet();
            }
        });

        SpecialItemsParams specialItemsParams = spawnConfig.getSpecialItemsParams();
        int maxItems = specialItemsParams.getMaxItems();

        boolean allowSpawnOtherSpecialEnemy = true;
        if (foundFreezeEnemy.get() || enemySpecialsNumbers.get() >= maxItems) {
            allowSpawnOtherSpecialEnemy = false;
        }

        if (allowSpawnOtherSpecialEnemy) {
            Integer enemyIdForRoom = TestStandLocal.getInstance().getEnemyIdForRoom(gameRoom.getId());
            if (enemyIdForRoom != -1 && enemyIdForRoom != 8 && enemyIdForRoom != 9 && enemyIdForRoom != 10) {
                int realSpecialId = F1.getId() + enemyIdForRoom - 1;
                if (realSpecialId == F2.getId() && enemySpecialsNumbers.get() > 0) {
                    getLog().debug("can`t spawn F2 enemy, enemySpecialsNumbers.get(): {}", enemySpecialsNumbers.get());
                    return;
                }
                getLog().debug("generate from teststand enemyIdForRoom {}, enemySpecialsNumbers.get(): {}, realSpecialId: {}, foundFreezeEnemy: {} ",
                        enemyIdForRoom, enemySpecialsNumbers.get(), realSpecialId, foundFreezeEnemy);
                EnemyType enemyType = EnemyType.getById(realSpecialId);
                long pathTime = spawnConfig.getSpecialItemsStayTime();
                spawnEnemyWithTrajectory(enemyType, getAndUpdateSpecialTrajectory(enemyType, spawnConfig, pathTime));
                TestStandLocal.getInstance().removeEnemyIdForRoom(gameRoom.getId());
            } else {
                long currentTime = System.currentTimeMillis() - startRoundTime;

                Integer specialItemsNumber = getAdditionalActionCounters().get(COUNTER_SPECIAL_ITEMS);
                if (specialItemsNumber == null) {
                    specialItemsNumber = 0;
                }
                long nextSpecialItemSpawn = getSpecialItemsSpawnTimeByNumber(specialItemsNumber,
                        specialItemsParams);

                boolean needSpawnSpecialItem = (nextSpecialItemSpawn != -1 && nextSpecialItemSpawn < currentTime
                        && (prevSpecialItemSpawnTime == 0 || (System.currentTimeMillis() > prevSpecialItemSpawnTime + (specialItemsParams.getTa() * 1000L))
                ) && specialItemsNumber < maxItems);


                if (needSpawnSpecialItem) {
                    long spawnTime = System.currentTimeMillis();
                    getLog().debug("generate SpecialItem from logic, prevSpecialItemSpawnTime: {}, " +
                                    "spawnTime: {}, specialItemsNumber: {}, specialItemsParams: {} ",
                            prevSpecialItemSpawnTime, spawnTime, specialItemsNumber, specialItemsParams);
                    prevSpecialItemSpawnTime = spawnTime;

                    long pathTime = spawnConfig.getSpecialItemsStayTime();

                    Map<EnemyType, Double> doubleMap = getRoom().getGame().getConfig(getRoom().getId()).getItems()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new Double(entry.getValue().get(0).getPickWeight())));

                    if (enemySpecialsNumbers.get() > 0 || (System.currentTimeMillis() + pathTime + FREEZE_TIME_TOTAL) > startRoundTime + gameRoom.getRoundDuration() * 1000L) {
                        doubleMap.remove(F2);
                        getLog().debug("doubleMap of special items after freeze removing: {} ", doubleMap);
                    }

                    if (doubleMap.isEmpty()) {
                        return;
                    }

                    EnemyType enemyType = GameTools.getRandomNumberKeyFromMapWithNorm(doubleMap);
                    addAdditionalActionCounter(COUNTER_SPECIAL_ITEMS, 1);
                    Enemy specialEnemy = spawnEnemyWithTrajectory(enemyType, getAndUpdateSpecialTrajectory(enemyType, spawnConfig, pathTime));

                    if (!enemyType.equals(F1)) {
                        EnemySpecialItem enemySpecialItem = (EnemySpecialItem) specialEnemy;
                        Map<Integer, List<EnemyType>> enemiesForKillingCount = enemySpecialItem.getEnemiesForKilling()
                                .stream().collect(Collectors.groupingBy(EnemyType::getId));

                        getLog().debug("enemySpecialItem: {} ", enemySpecialItem);
                        getLog().debug("enemiesForKillingCount: {} ", enemiesForKillingCount);

                        enemiesForKillingCount.forEach((enemyTypeId, enemyTypes) -> {
                            EnemyType enemy = getById(enemyTypeId);
                            if (config.enemyTypeEnabled(enemy)) {
                                int oldCntEnemies = counter.get(enemyTypeId) != null ? counter.get(enemyTypeId) : 0;
                                int diff = enemyTypes.size() - oldCntEnemies;
                                while (diff >= 0) {
                                    spawnEnemyWithTrajectory(enemy, map.getRandomTrajectory(enemy, spawnConfig));
                                    diff--;
                                    getLog().debug("generate additional enemy for special weapon, enemy: {}, diff: {}, enemySpecialId: {} ",
                                            enemy.getName(), diff, enemyType);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private Trajectory getAndUpdateSpecialTrajectory(EnemyType enemyType, SpawnConfig spawnConfig, long pathTime) {
        Trajectory specialItemTrajectory = getMap().getSpecialItemRandomTrajectory(enemyType, spawnConfig, pathTime);
        Trajectory tmpTraj = gameRoom.convertTrajectory(specialItemTrajectory, System.currentTimeMillis());
        return getMap().updatePointBySector(tmpTraj);
    }

    public Enemy generatePredefinedEnemies(boolean isSpecialItem, Enemy enemy, EnemyType enemyType) {
        if (isSpecialItem) {
            GameConfig config = gameRoom.getGame().getConfig(gameRoom.getRoomInfo().getId());
            EnemySpecialItem enemySpecialItem = (EnemySpecialItem) enemy;
            SpecialItem specialItem = config.getItems().get(enemyType).get(0);
            int totalPayout;
            int enemiesPayout = 0;
            int additionalPayout = 0;

            if (enemyType.equals(F4)) {
                List<Prize> multipliersPrizes = specialItem.getPrizes();
                Prize multiplier = MathData.getRandomElementFromWeightedList(multipliersPrizes);
                int minPay = multipliersPrizes.stream().min(Comparator.comparing(Prize::getPay)).get().getPay();
                totalPayout = minPay;
                int realMultiplier = multiplier.getPay() / minPay;
                enemySpecialItem.setCurrentMultiplier(realMultiplier);
            } else if (enemyType.equals(F1)) {
                Prize randomPrize = MathData.getRandomElementFromWeightedList(specialItem.getPrizes());
                totalPayout = randomPrize.getPay();
            } else {
                totalPayout = MathData.getRandomElementFromWeightedList(specialItem.getPrizes()).getPay();
            }

            if (!enemyType.equals(F1) && !enemyType.equals(F4)) {
                List<Double> multipliers = config.getCriticalMultiplier().get(CriticalMultiplierType.SP);
                int chMult = GameTools.getIndexFromDoubleProb(multipliers.stream().mapToDouble(Double::doubleValue).toArray()) + 1;
                enemySpecialItem.setCurrentMultiplier(chMult);
            }

            enemySpecialItem.setTotalPayout(totalPayout);

            getLog().debug("spawnEnemyWithTrajectory [special enemy]: {} totalPayout: {}", enemyType.getName(), totalPayout);

            Map<Integer, List<List<EnemyType>>> enemiesForItemsByPay = config.getEnemiesForItemsByPay();
            if (enemyType.equals(F2)) {
                enemySpecialItem.setAdditionalKillAwardPayout(enemySpecialItem.getTotalPayout());
            } else if (enemyType.equals(F3)) {
                Map<Integer, List<KillerItemData>> killItemsDataByPay = config.getKillItemsDataByPay();
                List<KillerItemData> killerItemData = killItemsDataByPay.get(totalPayout);
                KillerItemData itemData = killerItemData.get(RNG.nextInt(killerItemData.size()));
                List<EnemyType> chain = new ArrayList<>();
                for (int i = 0; i < itemData.getCount(); i++) {
                    chain.add(itemData.getEnemy());
                }
                enemySpecialItem.setEnemiesForKilling(chain);
                enemySpecialItem.setAdditionalKillAwardPayout(itemData.getExtraPayout());
                getLog().debug("spawnEnemyWithTrajectory  F3 [special enemy] chain: {}, enemySpecialItem: {}", chain, enemySpecialItem);
            } else if (enemiesForItemsByPay.containsKey(totalPayout)) {
                enemySpecialItem.setAdditionalKillAwardPayout(0);
                List<List<EnemyType>> possibleChains = enemiesForItemsByPay.get(totalPayout);
                List<EnemyType> currentChain = possibleChains.get(RNG.nextInt(possibleChains.size()));
                enemySpecialItem.setEnemiesForKilling(currentChain);
                getLog().debug("spawnEnemyWithTrajectory [special enemy] predefined currentChain: {}", currentChain);
            } else {
                int maxCnt = 1000;
                List<EnemyType> enemies = MID_PAY_ENEMIES.getEnemies();
                List<EnemyType> randomCurrentChain = new ArrayList<>();
                while (maxCnt-- > 0) {
                    EnemyType nextEnemy = enemies.get(RNG.nextInt(enemies.size()));
                    int pay = config.getEnemyData(nextEnemy).getPay();
                    if (enemiesPayout + pay < totalPayout) {
                        enemiesPayout += pay;
                        randomCurrentChain.add(nextEnemy);
                    } else {
                        additionalPayout = totalPayout - enemiesPayout;
                        maxCnt = 0;
                    }
                }
                enemySpecialItem.setEnemiesForKilling(randomCurrentChain);
                enemySpecialItem.setAdditionalKillAwardPayout(additionalPayout);
                getLog().debug("spawnEnemyWithTrajectory [special enemy] randomCurrentChain: {}, additionalPayout: {}, enemySpecialItem: {} ",
                        randomCurrentChain, additionalPayout, enemySpecialItem);
            }
        }
        return enemy;
    }

    public List<Enemy> spawnInitialWave(SpawnConfig spawnConfig, int typeNum) {
        switch (typeNum) {
            case 0:
                return createSpiralWave(spawnConfig, InitialWaveType.SpiralWave1);
            case 1:
                return createSpiralWave(spawnConfig, InitialWaveType.SpiralWave2);
            case 2:
                return createSpecialPatternWave(spawnConfig);
            case 3:
                return createRandomWave(spawnConfig);
            case 4:
                return createCrossWave(spawnConfig);
            case 5:
                //for no use initial wave case
                initialWaveEndTime = System.currentTimeMillis();
                break;
        }
        return null;
    }

    private boolean initialWavesFinished() {
        return (System.currentTimeMillis() - initialWaveEndTime > 0) && initialWaveEndTime != 0;
    }

    private List<Enemy> createSpiralWave(SpawnConfig spawnConfig, InitialWaveType type) {
        if ((System.currentTimeMillis() - lastSpiralWaveSpawnTime) > 4000 && countSpiralWaves < 3) {
            List<Enemy> enemies = getMap().spawnSpiralWave(spawnConfig, type);
            if (enemies != null) {
                for (Enemy enemy : enemies) {
                    gameRoom.sendNewEnemyMessage(enemy);
                }
                lastSpiralWaveSpawnTime = System.currentTimeMillis();
                countSpiralWaves++;
                if (countSpiralWaves == 2) {
                    initialWaveEndTime = System.currentTimeMillis() + 1000;
                }
            }
            getLog().debug("createSpiralWave: enemies.size={}, InitialWaveType={}", enemies.size(), type);
            return enemies;
        }
        return null;
    }

    private List<Enemy> createCrossWave(SpawnConfig spawnConfig) {
        if (initialWaveEndTime == 0) {
            List<Enemy> enemies = getMap().generateCrossEnemies(spawnConfig);
            for (Enemy enemy : enemies) {
                gameRoom.sendNewEnemyMessage(enemy);
            }
            initialWaveEndTime = enemies.get(enemies.size() - 1).getLeaveTime() - 12000;
            getLog().debug("createCrossWave: enemies.size={}", enemies.size());
            return enemies;
        }
        return null;
    }

    public List<Enemy> createRandomWave(SpawnConfig spawnConfig) {
        if (initialWaveEndTime == 0) {
            List<Enemy> enemies = getMap().spawnRandomWave(spawnConfig);
            for (Enemy enemy : enemies) {
                gameRoom.sendNewEnemyMessage(enemy);
            }
            initialWaveEndTime = enemies.get(enemies.size() - 1).getLeaveTime() - 26000;
            getLog().debug("createRandomWave: enemies.size={}", enemies.size());
            return enemies;
        }
        return null;
    }

    private List<Enemy> createSpecialPatternWave(SpawnConfig spawnConfig) {
        if (initialWaveEndTime == 0) {
            List<List<Enemy>> enemies = getMap().generateSpecialPatternEnemies(spawnConfig);
            List<Enemy> enemiesM = enemies.get(0);
            List<Enemy> enemiesQ = enemies.get(1);

            List<Enemy> enemiesResult = new ArrayList<>();
            enemiesResult.addAll(enemiesM);
            enemiesResult.addAll(enemiesQ);

            for (Enemy enemy : enemiesResult) {
                gameRoom.sendNewEnemyMessage(enemy);
            }

            initialWaveEndTime = enemiesResult.get(enemiesResult.size() - 1).getLeaveTime() - 9000;
            getLog().debug("createSpecialPatternWave: enemiesResult.size={}", enemiesResult.size());
            return enemiesResult;
        }
        return null;
    }

    private void createSpatialFormation(SpawnConfig spawnConfig) {
        List<Enemy> enemies = getMap().getSpatialEnemies(spawnConfig);
        int swarmId = getNextSwarmId();
        for (Enemy enemy : enemies) {
            enemy.addToSwarm(SwarmType.Spatial.getTypeId(), swarmId);
            gameRoom.sendNewEnemyMessage(enemy);
        }
        countFormationMobs = enemies.size();
    }

    private void createHybridFormation(SpawnConfig spawnConfig) {
        List<Enemy> enemies = getMap().spawnHybridFormation(spawnConfig);
        int swarmId = getNextSwarmId();
        for (Enemy enemy : enemies) {
            enemy.addToSwarm(SwarmType.Hybrid.getTypeId(), swarmId);
            gameRoom.sendNewEnemyMessage(enemy);
        }
    }

    private void createClusterFormation(SpawnConfig spawnConfig) {
        List<Enemy> enemies = getMap().spawnClusterFormation(spawnConfig);
        int swarmId = getNextSwarmId();
        for (Enemy enemy : enemies) {
            enemy.addToSwarm(SwarmType.Cluster.getTypeId(), swarmId);
            gameRoom.sendNewEnemyMessage(enemy);
        }
        countFormationMobs = enemies.size();
    }

    private void createTemporalFormation(SpawnConfig spawnConfig) {
        List<Enemy> enemies = getMap().spawnTemporalFormation(spawnConfig);
        int swarmId = getNextSwarmId();
        for (Enemy enemy : enemies) {
            enemy.addToSwarm(SwarmType.Temporal.getTypeId(), swarmId);
            if (temporalActiveEnemiesIds == null) {
                temporalActiveEnemiesIds = new ArrayList<>();
            }
            temporalActiveEnemiesIds.add(enemy.getId());
            gameRoom.sendNewEnemyMessage(enemy);
        }
        countFormationMobs = enemies.size();
        getLog().debug("active ids: " + temporalActiveEnemiesIds);
    }

    protected void onTimerWithLock(boolean needClearEnemy) {
        getLog().debug("PlayGameState:: onTimer: current={}", this);
        getLog().debug("End round, aliveMummies: {} needWaitingWhenEnemiesLeave: {} remainingNumberOfBoss: {}",
                getMap().getItemsSize(), needWaitingWhenEnemiesLeave, remainingNumberOfBoss
        );

        if (!needWaitingWhenEnemiesLeave) {
            needWaitingWhenEnemiesLeave = true;

            timeOfRoundFinishSoon = System.currentTimeMillis();
            allowSpawn = false;
            getMap().clearInactivityLiveItems();
            if (needClearEnemy) {
                destroyBaseEnemies();
                getMap().removeAllEnemies();
            }
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

    @Override
    public void nextSubRound() {
        doFinishWithLock();
    }

    @Override
    public void doFinishWithLock() {
        lockShots.lock();
        try {
            getLog().debug("doFinishWithLock: timer: {}", gameRoom.getTimerTime());
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

    private Enemy spawnEnemyWithTrajectory(EnemyType enemyType, Trajectory trajectory) {
        Enemy enemy = getMap().addEnemyWithTrajectory(enemyType, trajectory);
        boolean isSpecialItem = SPECIAL_ITEMS.contains(enemyType);
        enemy = generatePredefinedEnemies(isSpecialItem, enemy, enemyType);
        gameRoom.sendNewEnemyMessage(enemy);
        return enemy;
    }

    @Override
    public void spawnEnemyFromTeststand(int typeId, int skinId, Trajectory trajectory, long parentEnemyId) {
        //ignored
    }

    private void spawnBoss(BossType bossType, Trajectory trajectory) {
        lockShots.lock();
        try {
            GameConfig config = gameRoom.getGame().getConfig(gameRoom.getRoomInfo().getId());
            BossParams bossParams = MathData.getBossesByCountPlayers(config, getRoom().getSeatsCount()).get(bossType.getSkinId());
            int defeatTresHold = RNG.nextInt(bossParams.getMinPay(), bossParams.getMaxPay());
            gameRoom.sendNewEnemyMessage(getMap().spawnBoss(bossType, trajectory, defeatTresHold,
                    getRoom().getGame().getSpawnConfig(getRoom().getId())));
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

        getLog().debug("ammo amount: " + seat.getAmmoAmount());
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
            if (paidSpecialShot) {
                SpecialWeaponType weaponType = SpecialWeaponType.values()[seat.getCurrentWeaponId()];
                multiplierPaidWeapons *= MathData.getPaidWeaponCost(gameRoom.getGame().getConfig(seat), weaponType.getId());
            }

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
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets", shot);
            } else if (paidSpecialShot && seat.getAmmoAmount() < multiplierPaidWeapons) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets", shot);
                getLog().debug("paidSpecialShot, seat.getAmmoAmount(): {} less then weapon cost: {}",
                        seat.getAmmoAmount(), multiplierPaidWeapons);
                return;
            } else if ((seat.getAmmoAmount() <= 0 || (seat.getAmmoAmount() - seat.getBetLevel() < 0)) && isPaidShot) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets", shot);
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
            boolean levelUpFromLevelUp = betLevelOld == 2;
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

        getLog().debug("seat.getCurrentWeapon().getShots(): " + seat.getCurrentWeapon());
        if (seat.getCurrentWeaponId() == SpecialWeaponType.LevelUp.getId() && seat.getCurrentWeapon().getShots() == 0) {
            int newBetLevelCurrent = 1;
            TInboundObject betLevelResponse = getTOFactoryService()
                    .createBetLevelResponse(System.currentTimeMillis(), SERVER_RID, newBetLevelCurrent, seat.getNumber());
            seat.setBetLevel(newBetLevelCurrent);
            gameRoom.sendChanges(betLevelResponse);
            getLog().debug("processing shot, levelUp weapons is zero, reset bet level for aid: {}", seat.getAccountId());
        }

        /*List<Seat> seats = gameRoom.getSeats();
        for (Seat seatCurrent : seats) {
            if (!isFRB()) {
                seatCurrent.transferWinToAmmo();
            }
        }*/
        messages.send(shot);
    }

    protected void sendUpdateBossTrajectory(long time, long id, Trajectory trajectory) {
        Map<Long, Trajectory> bossTrajectories = new HashMap<>();
        bossTrajectories.put(id, trajectory);
        gameRoom.sendChanges(getTOFactoryService().createUpdateTrajectories(time, SERVER_RID,
                bossTrajectories, 0, EnemyAnimation.NO_ANIMATION.getAnimationId()));
    }

    protected void sendFreezeTrajectories(long time, double x, double y, int d) {
        Map<Long, Trajectory> freezeTrajectories = gameRoom.getMap().generateFreezeTrajectories(time, FREEZE_TIME, x, y, d);
        gameRoom.sendChanges(getTOFactoryService().createUpdateTrajectories(time, SERVER_RID,
                freezeTrajectories, FREEZE_TIME_TOTAL, EnemyAnimation.NO_ANIMATION.getAnimationId()));
    }

    protected void sendUpdateTrajectory(long time, int freezeTime, double x, double y, int d) {
        Map<Long, Trajectory> freezeTrajectories = gameRoom.getMap().generateFreezeTrajectories(time, freezeTime, x, y, d);
        gameRoom.sendChanges(getTOFactoryService().createUpdateTrajectories(time, SERVER_RID,
                freezeTrajectories, 3000, EnemyAnimation.NO_ANIMATION.getAnimationId()));
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
        Money bgStake = Money.BG_STAKE;
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

        Enemy baseEnemy = map.getItemById(itemIdForShot);
        EnemyType baseEnemyType;
        ShootResult baseShootResult;

        Map<Integer, Long> winsBySeatId = new HashMap<>();
        for (Seat seatCurrent : gameRoom.getSeats()) {
            PlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
            winsBySeatId.put(seatCurrent.getNumber(), currentPlayerRoundInfo != null ? currentPlayerRoundInfo.getTotalPayouts().toCents() : 0);
        }

        int scoreIdx = getMyScoreId(seat.getNumber(), winsBySeatId);
        getLog().debug("shootWithSpecialWeaponAndUpdateState: scoreIdx:{}", scoreIdx);

        if (baseEnemy != null) {
            baseEnemyType = baseEnemy.getEnemyType();
            getLog().debug("shootWithSpecialWeaponAndUpdateState: Base enemy: {} enemyTypeId: {}", baseEnemy, baseEnemyType.getId());
            if (!map.isVisible(time, baseEnemy)) {
                getLog().debug("shootWithSpecialWeaponAndUpdateState: attempt to shoot on non visible target at {}", baseEnemy.getLocation(time));
                return createErrorResult(seat, baseEnemy, true);
            }

            boolean success = singleShot(time, seat, baseEnemy, weaponId, results, true);
            if (success) {
                baseShootResult = results.get(0);
                baseShootResult.setMainShot(true);
                if (baseShootResult.isDestroyed() && !baseEnemy.getEnemyType().equals(EnemyType.BOSS)) {
                    boolean isFreezeEnemy = baseEnemy.getTrajectory().getPoints().stream().noneMatch(Point::isFreezePoint);
                    if (lastFreezeTime > 0 && isFreezeEnemy) {
                        int remainingAdditionalWin = seat.getAdditionalTempCounters(SEAT_FREEZE_WIN_AMOUNT);
                        if (RNG.nextBoolean() && remainingAdditionalWin > 0) {
                            int randomPay = RNG.nextInt(2, 6) * freezeChMult;
                            if (randomPay > remainingAdditionalWin) {
                                randomPay = remainingAdditionalWin;
                            }
                            int newRemainingAdditionalWin = remainingAdditionalWin - randomPay;
                            seat.resetAdditionalTempCounter(SEAT_FREEZE_WIN_AMOUNT);
                            String enemyNameKey = baseEnemy.getEnemyType().getId() + "_" + baseEnemy.getEnemyType().getName() + "_" + seat.getBetLevel();
                            Money payoutFromFreezeItem = BG_STAKE.getWithMultiplier(randomPay * seat.getBetLevel());
                            boolean isSpecialWeapon = shot.getWeaponId() != -1;
                            String title = isSpecialWeapon ? SpecialWeaponType.values()[shot.getWeaponId()].getTitle() : null;
                            //seat.getCurrentPlayerRoundInfo().updatePayoutsFromItems(payoutFromFreezeItem, enemyNameKey, F2.getName());
                            seat.getCurrentPlayerRoundInfo().updatePayoutsFromItemsWithMultiplier(Money.ZERO, false, isSpecialWeapon, title, payoutFromFreezeItem,
                                    true, enemyNameKey, Money.ZERO, 1, F2.getName());
                            baseShootResult.setWin(baseShootResult.getWin().add(BG_STAKE.getWithMultiplier(randomPay * seat.getBetLevel())));
                            if (newRemainingAdditionalWin > 0) {
                                seat.addAdditionalTempCounter(SEAT_FREEZE_WIN_AMOUNT, newRemainingAdditionalWin);
                            }
                            getLog().debug("shootWithSpecialWeaponAndUpdateState: remainingAdditionalWin {}, randomPay: {}, newRemainingAdditionalWin: {}",
                                    remainingAdditionalWin, randomPay, newRemainingAdditionalWin);
                        }
                    }
                }
            } else {
                getLog().debug("shootWithSpecialWeaponAndUpdateState: Base enemy is invulnerable");
                return createErrorResult(seat, baseEnemy, true);
            }
        } else {
            // killed earlier
            getLog().debug("shootWithSpecialWeaponAndUpdateState: Base enemy was killed before, id to shoot: {}", itemIdForShot);
            return createErrorResult(seat, null, false);
        }

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

            if (seat.getCurrentWeaponId() == MathData.TURRET_WEAPON_ID) {
                getLog().debug("shootWithSpecialWeaponAndUpdateState: decrement ammo for regular weapon with betLevel: {}", betLevel);
                //seat.decrementAmmoAmount(betLevel);
                seat.incrementBulletsFired();
            } else {
                seat.consumeSpecialWeapon(weaponId);
                getLog().debug("shootWithSpecialWeaponAndUpdateState: decrement ammo for levelUp weapon with betLevel: {}", betLevel);
            }

            GameConfig gameConfig = gameRoom.getGame().getConfig(seat);
            if (EnemyRange.SPECIAL_ITEMS.contains(baseEnemyType) && baseShootResult.isDestroyed()) {
                int chMult;
                if (baseEnemyType.equals(F2)) {
                    EnemySpecialItem enemySpecialItem = (EnemySpecialItem) baseEnemy;
                    sendFreezeTrajectories(time, shot.getX(), shot.getY(), 0);
                    lastFreezeTime = System.currentTimeMillis();
                    seat.addAdditionalTempCounter(SEAT_FREEZE_WIN_AMOUNT, (int) enemySpecialItem.getAdditionalKillAwardPayout()
                            * enemySpecialItem.getCurrentMultiplier());
                    freezeChMult = enemySpecialItem.getCurrentMultiplier();
                    chMult = freezeChMult;
                    betLevelF2Shot = seat.getBetLevel();
                    baseShootResult.setChMult(chMult);
                } else if (!baseEnemyType.equals(F1)) {
                    EnemySpecialItem enemySpecialItem = (EnemySpecialItem) baseEnemy;
                    long additionalKillAwardPayout = enemySpecialItem.getAdditionalKillAwardPayout();
                    List<EnemyType> enemiesForKilling = enemySpecialItem.getEnemiesForKilling();
                    chMult = enemySpecialItem.getCurrentMultiplier();
                    if (chMult == 0) {
                        chMult = 1;
                    }

                    Long anyBossId = getMap().getAnyBossId();
                    int totalPayout = enemySpecialItem.getTotalPayout();
                    int randomBossPay = 0;
                    if (baseEnemyType.equals(F6) || baseEnemyType.equals(F7)) {
                        Enemy boss;
                        MinMaxParams minMaxParams = gameConfig.getPercentPayOnBoss().get(baseEnemy.getEnemyType());
                        if (isBossRound() && minMaxParams != null && minMaxParams.getMax() > 0 && anyBossId != 1) {
                            boss = getMap().getItemById(anyBossId);
                            if (boss != null && isAllowedShootBoss()) {
                                double percentRangeMode = 10;
                                double rageThreshold = (boss.getFullEnergy() * percentRangeMode) / 100;
                                boolean regularBossMode = boss.getEnergy() > rageThreshold;
                                if (regularBossMode) {
                                    sendUpdateTrajectory(time, 3000, shot.getX(), shot.getY(), 0);
                                    int randomPay = RNG.nextInt((int) (minMaxParams.getMin() * 100), (int) (minMaxParams.getMax() * 100));
                                    double averagePercent = ((double) randomPay) / 100;
                                    randomBossPay = (int) (averagePercent * totalPayout);

                                    if (randomBossPay > boss.getEnergy()) {
                                        randomBossPay = (int) boss.getEnergy();
                                    }

                                    Money payout = bgStake.getWithMultiplier(chMult * randomBossPay * betLevel);
                                    seat.addTotalBossPayout(payout.toCents());
                                    String enemyNameKey = boss.getEnemyType().getId() + "_" + boss.getEnemyType().getName() + "_" + seat.getBetLevel();
                                    seat.getCurrentPlayerRoundInfo().updatePayoutsFromItems(payout, enemyNameKey, baseEnemy.getEnemyType().getName());

                                    double newEnergy = boss.getEnergy() - randomBossPay;
                                    if (newEnergy < 0) {
                                        newEnergy = 0;
                                    }
                                    boss.setEnergy(newEnergy);
                                    ShootResult shootResult = new ShootResult(Money.ZERO, payout, false, false, boss);
                                    shootResult.setExplode(true);
                                    results.add(shootResult);
                                    getLog().debug("shootWithSpecialWeaponAndUpdateState: special item boss random pay: {},  averagePercent: {}, newEnergy: {} ",
                                            randomPay, averagePercent, newEnergy);
                                    lastTimeBossShot = System.currentTimeMillis();
                                    timeInterval = MathData.getBossRewardingTimeByCountPlayers(gameConfig, getRoom().getSeatsCount());
                                }
                            }
                        }
                    }

                    getLog().debug("shootWithSpecialWeaponAndUpdateState: special item chMult: {}, randomBossPay: {}, totalPayout: {}, additionalKillAwardPayout: {}",
                            chMult, randomBossPay, totalPayout, additionalKillAwardPayout);

                    if (randomBossPay > 0) {
                        if (additionalKillAwardPayout > randomBossPay) {
                            additionalKillAwardPayout -= randomBossPay;
                            randomBossPay = 0;
                        } else {
                            randomBossPay -= additionalKillAwardPayout;
                            additionalKillAwardPayout = 0;
                        }
                    }

                    long killAwardFromEnemies = additionalKillAwardPayout * chMult;

                    getLog().debug("shootWithSpecialWeaponAndUpdateState: special item pay data chMult: {}, randomBossPay: {}, totalPayout: {}," +
                                    " additionalKillAwardPayout: {}, killAwardFromEnemies: {}",
                            chMult, randomBossPay, totalPayout, additionalKillAwardPayout, killAwardFromEnemies);

                    int missPay = randomBossPay;

                    for (EnemyType killEnemyType : enemiesForKilling) {
                        Enemy firstEnemyByTypeId = null;
                        List<Enemy> enemiesToKill = getEnemiesByTypeWithApproximateTrajectory(killEnemyType, baseEnemyType);
                        //Enemy firstEnemyByTypeId = map.getFirstEnemyByTypeId(killEnemyType.getId());
                        int enemyPayout = MathData.getEnemyPayout(gameConfig, killEnemyType, 0);
                        if (enemiesToKill.size() != 0) {
                            firstEnemyByTypeId = enemiesToKill.get(RNG.nextInt(enemiesToKill.size()));
                        }
                        int enemyPay = chMult * enemyPayout;
                        if (missPay > 0) {
                            missPay -= enemyPayout;
                            if (missPay < 0) {
                                int additionalMissPayout = Math.abs(missPay);
                                killAwardFromEnemies += (long) additionalMissPayout * chMult;
                                missPay = 0;
                                getLog().debug("shootWithSpecialWeaponAndUpdateState: special item enemy pay additionalMissPayout: {}, killAwardFromEnemies: {}",
                                        additionalMissPayout, killAwardFromEnemies);
                            }
                        } else {
                            if (firstEnemyByTypeId != null) {
                                Money payout = bgStake.getWithMultiplier(enemyPay * betLevel);
                                ShootResult shootResult = new ShootResult(Money.ZERO, payout, false, true,
                                        firstEnemyByTypeId);
                                shootResult.setExplode(true);
                                results.add(shootResult);
                                shootResult.setChMult(chMult);
                                shootResult.setPrize(baseEnemyType.getName());
                                checkEnemyKilled(shootResult);
                            } else {
                                killAwardFromEnemies += enemyPay;
                            }
                        }
                    }
                    Money killAwardWin = bgStake.getWithMultiplier(killAwardFromEnemies * betLevel);
                    Money resultKillAwardWin = baseShootResult.getKillAwardWin();
                    Money newKillAwardWin = resultKillAwardWin.add(killAwardWin);
                    baseShootResult.setKillAwardWin(newKillAwardWin);
                    baseShootResult.setChMult(chMult);
                    getLog().debug("shootWithSpecialWeaponAndUpdateState: special item payouts: killAwardFromEnemies: {}, killAwardWin:{}, newKillAwardWin: {} ",
                            killAwardFromEnemies, killAwardWin, newKillAwardWin);
                }
            }

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

    private boolean isAllowedShootBoss() {
        return (System.currentTimeMillis() - lastTimeBossShot) > timeInterval;
    }

    private List<Enemy> getEnemiesByTypeWithApproximateTrajectory (EnemyType enemyType, EnemyType enemySpecialItemType) {
        List<Enemy> result = new ArrayList<>();
        List<Enemy> enemies = getMap().getItems();
        for (Enemy enemy : enemies) {
            if (enemy.getEnemyType().equals(enemyType)) {
                if (isEnemyAwayFromBorder(enemy, enemySpecialItemType)) {
                    result.add(enemy);
                }
            }
        }
        return result;
    }

    private boolean isEnemyAwayFromBorder (Enemy enemy, EnemyType specialItemType) {
        long currentTime = System.currentTimeMillis();
        Trajectory trajectory = enemy.getTrajectory();
        long timeToSearch = currentTime - trajectory.getPoints().get(0).getTime();
        if (specialItemType.equals(F5)) {
            timeToSearch += 2000;
        } else if (specialItemType.equals(F3)) {
            timeToSearch += 3000;
        } else if (specialItemType.equals(F4)) {
            timeToSearch += 3000;
        } else if (specialItemType.equals(F7)) {
            timeToSearch += 4000;
        } else if (specialItemType.equals(F6)) {
            timeToSearch += 2000;
        }
        getLog().debug("enemyType: " + enemy.getEnemyType());
        return getMap().isPointOnMapApproximate(trajectory, timeToSearch, 0);
    }

    private void addTestStandShotFeature(String sessionId) {
        TestStandLocal testStandLocal = TestStandLocal.getInstance();
        TestStandFeature featureById = testStandLocal.getPossibleFeatureById(6).copy();
        testStandLocal.addFeature(sessionId, featureById);
    }

    private List<ShootResult> createErrorResult(Seat seat, Enemy enemy, boolean invulnerable) {
        ShootResult result = new ShootResult(seat.getStake(), Money.INVALID, false, false, enemy, invulnerable);
        //necessary to correctly change the betlevel on client side
        result.setMainShot(true);
        return Collections.singletonList(result);
    }

    private boolean singleShot(long time, Seat seat, Enemy enemy, int weaponId, List<ShootResult> results, boolean isMainShot) throws CommonException {
        boolean success = false;
        ShootResult result = shootToOneEnemy(time, seat, enemy, weaponId, isMainShot);
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
        getLog().debug("result.isMainShot(): {}", mainShot);

        boolean isSpecialWeapon = shot.getWeaponId() != -1;
        boolean isPrize = !result.getPrize().isEmpty();
        boolean isWin = result.getWin().greaterThan(Money.ZERO);
        boolean isWeapon = awardedWeaponId != -1 || result.getNewFreeShotsCount() > 0;
        boolean isBossWin = result.isShotToBoss()
                && (result.getWin().greaterThan(Money.ZERO) || result.getKillAwardWin().greaterThan(Money.ZERO));
        boolean isAwardedWeapons = !result.getAwardedWeapons().isEmpty();
        boolean isGems = result.getGems() != null && !result.getGems().isEmpty() &&
                result.getGems().stream().reduce(0, Integer::sum) > 0;

        boolean isSpecialItemKill = result.getEnemy() != null && result.isDestroyed()
                && SPECIAL_ITEMS.contains((EnemyType) result.getEnemy().getEnemyClass().getEnemyType());

        boolean isKillAward = result.getKillAwardWin().greaterThan(Money.ZERO);
        boolean isHit = isPrize || isWin || isWeapon || isBossWin || isAwardedWeapons || isSpecialItemKill || isKillAward;
        getLog().debug("isHit: {}, isPrize: {}, isWin: {}, isWeapon: {}, isBossWin: {}, isAwardedWeapons: {}, isSpecialItemKill: {}, isKillAward: {}",
                isHit, isPrize, isWin, isWeapon, isBossWin, isAwardedWeapons, isSpecialItemKill, isKillAward);

        Map<Integer, List<IWinPrize>> hitResultBySeats = new HashMap<>();
        Map<Seat, IHit> messagesForSeatsLocal = new HashMap<>();
        IHit hitOwn = null;
        List<IHit> hitsForObserversLocal = new ArrayList<>();

        Money stake = (isSpecialWeapon || !mainShot) ? Money.ZERO : seat.getStake().getWithMultiplier(seat.getBetLevel());
        Money bet = result.getBet();

        Money paidStake = Money.ZERO;
        boolean isPaidShotToBaseEnemy = shot.isPaidSpecialShot() && mainShot;
        if (isPaidShotToBaseEnemy) {
            stake = seat.getStake().getWithMultiplier(MathData.getPaidWeaponCost(gameRoom.getGame().getConfig(seat), shot.getWeaponId()) * seat.getBetLevel());
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
                                /*currentPlayerRoundInfo.updateStatNewWithMultiplier(seat.getStake(), false, false, null,
                                        killAwardWin, true, enemyNameKey, Money.ZERO, result.getChMult() == 0 ? 1 : result.getChMult(), null);*/
                                currentPlayerRoundInfo.updateKillAwardWinWithLevelUp(killAwardWin, enemyNameKey, isSpecialWeapon, title);
                                currentPlayerRoundInfo.updateAdditionalWin("KillAwardWin", killAwardWin);
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

                            getLog().debug("isSpecialWeapon {}, isLastResult: {}, stake: {}, title: {}, realStake: {}",
                                    isSpecialWeapon, isLastResult, seat.getStake(), title, stake);

                            getLog().debug("processing name: " + enemyNameKey);
                            getLog().debug("bet.getValue(): {}", bet.getValue());
                            seat.getCurrentPlayerRoundInfo().updateStatNewWithMultiplier(stake, result.isShotToBoss(), isSpecialWeapon,
                                    title, enemyWinForSeat, result.isDestroyed(), enemyNameKey, paidStake, result.getChMult() == 0 ? 1 : result.getChMult(), result.getPrize());


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
                            hit.setNeedExplode(result.isNeedExplode());
                            hit.setExplode(result.isExplode());
                            hit.setBossNumberShots(gameRoom.getBossNumberShots());
                            hit.setKillBonusPay(killAwardWin.toDoubleCents());
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
                hitForObservers.setNeedExplode(result.isNeedExplode());
                hitForObservers.setExplode(result.isExplode());
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
                }

            } else {
                seat.getCurrentPlayerRoundInfo().addMissCounter(seat.getCurrentWeaponId(), 1);

                String title = isSpecialWeapon ? SpecialWeaponType.values()[realShotTypeId].getTitle() : null;

                seat.getCurrentPlayerRoundInfo().updateStatNewWithMultiplier(stake, result.isShotToBoss(), isSpecialWeapon,
                        title, Money.ZERO, result.isDestroyed(), enemyNameKey, paidStake, result.getChMult() == 0 ? 1 : result.getChMult(), result.getPrize());

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

    public ShootResult shootToOneEnemy(long time, Seat seat, Enemy enemy, int weaponId, boolean isMainShot) throws CommonException {
        boolean isShotWithSpecialWeapon = weaponId != REGULAR_WEAPON;
        GameConfig config = getRoom().getGame().getConfig(getRoom().getId());
        Money stake = Money.BG_STAKE;
        // killed earlier
        if (enemy == null) {
            return new ShootResult(isShotWithSpecialWeapon ? Money.ZERO : stake, Money.INVALID, false, false, null);
        }
        if (enemy.getEnemyType().getId() == B3.getId()) {
            for (Enemy item : getMap().getItems()) {
                if (item.getParentEnemyId() == enemy.getId() || item.getParentEnemyTypeId() == enemy.getEnemyType().getId()) {
                    return new ShootResult(isShotWithSpecialWeapon ? Money.ZERO : stake, Money.INVALID, false, false, null, true);
                }
            }
        }

        getLog().debug("PlayerId: {}, enemyId: {}, enemyType: {}",
                seat.getPlayerInfo().getId(), enemy.getId(), enemy.getEnemyType().getName());

        ShootResult shootResult = gameRoom.getGame().doShoot(enemy, seat, stake, isBossRound(), getTOFactoryService(), gameRoom.getSeatsCount(), lastTimeBossShot,
                timeInterval);

        if (enemy.isBoss() && shootResult.getWin().greaterThan(Money.ZERO)) {
            lastTimeBossShot = System.currentTimeMillis();
            timeInterval = MathData.getBossRewardingTimeByCountPlayers(config, gameRoom.getSeatsCount());
        }

        if (shootResult.isBossShouldBeAppeared() && !isBossRound() && allowSpawn) {
            spawnBossTestStand = true;
            getLog().debug("Boss will be appeared later");
        }

        getLog().debug("shootResult: {}", shootResult);

        checkEnemyKilled(shootResult);
        shootResult.setWeaponSurpluses(seat.getWeaponSurplus());
        return shootResult;
    }

    private void checkEnemyKilled(ShootResult shootResult) {
        IEnemy<EnemyClass, Enemy> enemy = shootResult.getEnemy();
        if (shootResult.isDestroyed()) {
            if (!enemy.isBoss() && enemy.getLives() > 0) {
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
        lockShots = new ReentrantLock();
        super.restoreGameRoom(gameRoom);
        lastFreezeTime = -1;
        timeRestored = System.currentTimeMillis();
        isGameRestored = true;
    }

    @Override
    public boolean isBossRound() {
        return subround.equals(BOSS);
    }

    @Override
    public Map<Long, Integer> getFreezeTimeRemaining() {
        long currenTime = System.currentTimeMillis();
        getLog().debug("getFreezeTimeRemaining lastFreezeTime: {}, currenTime: {}", lastFreezeTime, currenTime);
        return getMap().getAllFreezeTimeRemaining(lastFreezeTime, currenTime, FREEZE_TIME);
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

    private List<Pair<EnemyType, Trajectory>> initializeSpawnList(Map<Pair<EnemyType, Trajectory>, Double> spawnWeights,
                                                                  int limit) {
        List<Pair<EnemyType, Trajectory>> spawnList = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            spawnList.add(GameTools.getRandomNumberKeyFromMapWithNorm(spawnWeights));
        }
        return spawnList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SectorX PlayGameState [");
        sb.append("timeOfRoundFinishSoon=").append(timeOfRoundFinishSoon);
        sb.append(", lastFreezeTime=").append(lastFreezeTime);
        sb.append(", prevSpecialItemSpawnTime=").append(prevSpecialItemSpawnTime).append(", ");
        sb.append(super.toString());
        sb.append(']');
        return sb.toString();
    }

    @Override
    protected PlayGameState getDeserializer() {
        return this;
    }
}
