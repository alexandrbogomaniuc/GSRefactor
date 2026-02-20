package com.betsoft.casino.mp.sectorx.model;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.movement.HybridTrajectory;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.sectorx.model.math.*;
import com.betsoft.casino.mp.sectorx.model.math.config.*;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.betsoft.casino.teststand.TeststandConst;
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
import static com.betsoft.casino.mp.model.PlaySubround.BOSS;
import static com.betsoft.casino.mp.sectorx.model.math.EnemyRange.*;
import static com.betsoft.casino.mp.sectorx.model.math.EnemyType.*;
import static com.betsoft.casino.mp.utils.ErrorCodes.*;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

@SuppressWarnings("Duplicates")
public class PlayGameState extends AbstractActionPlayGameState<GameRoom, Seat, GameMap, PlayGameState> {
    private transient long timeOfRoundFinishSoon;
    private transient List<Pair<EnemyType, Trajectory>> initialSpawnList;
    private transient PriorityQueue<Long> staticEnemiesDieOrDisappearTime = new PriorityQueue<>();
    private transient boolean spawnBossTestStand = false;
    protected static final int FREEZE_TIME = 8745;
    protected static final int FREEZE_TIME_TOTAL = 10000;
    protected static final int FREEZE_TIME_BOSS = 3100;
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
    public transient String SEAT_FREEZE_WIN_AMOUNT = "SEAT_FREEZE_WIN_AMOUNT";
    public transient final String COUNTER_SPECIAL_ITEMS = "COUNTER_SPECIAL_ITEMS";
    public transient final String TIMES_SPECIAL_ITEMS = "TIMES_SPECIAL_ITEMS";
    public transient final String TIMES_HUGE_ENEMIES = "TIMES_HUGE_ENEMIES";
    public transient final String COUNTER_HUGE_ENEMIES = "COUNTER_HUGE_ENEMIES";
    private transient long prevSpecialItemSpawnTime = 0;
    private transient long prevHugeEnemySpawnTime = 0;
    private transient boolean isInitialWaveSpawned = false;
    private transient int hugeEnemyCounter = 0;
    private transient boolean isGameRestored = false;
    private transient long timeAfterReboot = 0;
    private transient List<Enemy> initialWaveEnemies = new ArrayList<>();
    transient int swarmIdCurrent = 0;
    private transient long lastHitF6Time = 0;

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

    public long getLastFreezeTime() {
        return lastFreezeTime;
    }

    public boolean allowChangeBetLevel(Seat account) {
        return lastFreezeTime == -1 || account.getAdditionalTempCounters(SEAT_FREEZE_WIN_AMOUNT) == 0;
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
        gameRoom.setPossibleEnemies(EnemyRange.BASE_ENEMIES);
    }

    @Override
    protected void updateWithLock() {
        if (needForceFinishRound && lastFreezeTime == -1) {
            nextSubRound();
            return;
        }

        if (needWaitingWhenEnemiesLeave && lastFreezeTime == -1) {
            getMap().update();
            long currentTimeMillis = System.currentTimeMillis();
            boolean noEnemiesInRoom = true;
            for (Enemy item : getMap().getItems()) {
                long lastTime = item.getTrajectory().getLastPoint().getTime();
                if (lastTime > currentTimeMillis) {
                    getLog().debug("item live: {}, lastTime: {}", item.getEnemyType().getName(), lastTime);
                    noEnemiesInRoom = false;
                }
            }
            if (noEnemiesInRoom) {
                getLog().debug("no live enemies in room, finish by timeOfRoundFinishSoon");
                getMap().removeAllEnemies();
                nextSubRound();
                return;
            }
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
                    seat.resetAdditionalTempCounter(SEAT_FREEZE_WIN_AMOUNT);
                    int betLevel = seat.getBetLevel();
                    Money payout = seat.getStake().getWithMultiplier(additionalFreezeWin * betLevel);
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
                    String enemyNameKey = F2.getId() + "_" + F2.getName() + "_" + seat.getBetLevel();
                    seat.getCurrentPlayerRoundInfo().updateKillAwardWin(payout, enemyNameKey);
                    ISeatWinForQuest seatWinForQuest = getTOFactoryService().createSeatWinForQuest(currentTimeMillis, SERVER_RID, seat.getNumber(), F2.getId(),
                            payout.toCents(), -1);
                    gameRoom.sendChanges(seatWinForQuest);
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
                        MathData.getRandomBossType(getRoom().getGame().getConfig(getRoom().getId()));
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
            double alpha = -(Math.log(params.getDelta()) / (300 - params.getT1() - params.getTa()));
            for (double t = t0 + params.getTa(); t <= 300; t += dt) {
                double r = RNG.rand();
                double funcSpecialRes = params.getA() * Math.exp(-alpha * (t - params.getT1() - params.getTa()));
                if (r < funcSpecialRes) {
                    triggerTimeSlots.add((long) t * 1000);
                }
            }
            addAdditionalActionSpawnTimes(TIMES_HUGE_ENEMIES, triggerTimeSlots);
            getLog().debug("getAdditionalActionSpawnTimes(): {}", getAdditionalActionSpawnTimes());
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
            double alpha = -(Math.log(params.getDelta()) / (300 - params.getT1() - params.getTa()));
            for (double t = t0 + params.getTa(); t <= 300; t += dt) {
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

        boolean allowSpawnHugeEnemy = hugeEnemiesOnScreenCounter.get() < MAX_HUGE_ENEMY_ITEMS_ON_SCREEN_ALLOWED && hugeEnemyCounter < maxItemsForRound;

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
        long roundTime = getStartRoundTime();
        int roundDuration = gameRoom.getRoundDuration() * 1000;
        long currentTimeMillis = System.currentTimeMillis();
        getLog().debug("spawnHugePayEnemy roundTime: {}, roundDuration: {}, currentTimeMillis: {}", roundTime, roundDuration, currentTimeMillis);
        long remainingTime = roundTime + roundDuration - currentTimeMillis;

        if (enemyTypeForSpawn.equals(B3)) {
            Trajectory trajectory = map.getRandomTrajectory(enemyTypeForSpawn, spawnConfig, remainingTime);
            if (trajectory == null) {
                return;
            }
            HybridTrajectory hybridTrajectory = new HybridTrajectory(0, trajectory.getPoints());
            hugePayEnemy = getMap().addEnemyWithTrajectory(enemyTypeForSpawn, hybridTrajectory);
            hugePayEnemy.addToSwarm(SwarmType.HugeEnemy.getTypeId(), swarmId);
            gameRoom.sendNewEnemyMessage(hugePayEnemy);
            hybridPoints = hugePayEnemy.getTrajectory().getPoints();
        } else {
            Trajectory trajectory = map.getRandomTrajectory(enemyTypeForSpawn, spawnConfig, remainingTime);
            if (trajectory == null) {
                return;
            }
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
                spawnEnemyWithTrajectory(enemyType, getAndUpdateSpecialTrajectory(enemyType, spawnConfig));
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

                    Map<EnemyType, Double> doubleMap = getRoom().getGame().getConfig(getRoom().getId()).getItems()
                            .entrySet()
                            .stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> new Double(entry.getValue().get(0).getPickWeight())));

                    if (enemySpecialsNumbers.get() > 0) {
                        doubleMap.remove(F2);
                        getLog().debug("doubleMap of special items after freeze removing: {} ", doubleMap);
                    }

                    if (doubleMap.isEmpty()) {
                        return;
                    }

                    EnemyType enemyType = GameTools.getRandomNumberKeyFromMapWithNorm(doubleMap);
                    addAdditionalActionCounter(COUNTER_SPECIAL_ITEMS, 1);
                    Enemy specialEnemy = spawnEnemyWithTrajectory(enemyType, getAndUpdateSpecialTrajectory(enemyType, spawnConfig));

                    if (!enemyType.equals(F1)) {
                        EnemySpecialItem enemySpecialItem = (EnemySpecialItem) specialEnemy;
                        Map<Integer, List<EnemyType>> enemiesForKillingCount = enemySpecialItem.getEnemiesForKilling()
                                .stream().collect(Collectors.groupingBy(EnemyType::getId));

                        getLog().debug("enemySpecialItem: {} ", enemySpecialItem);
                        getLog().debug("enemiesForKillingCount: {} ", enemiesForKillingCount);

                        enemiesForKillingCount.forEach((enemyTypeId, enemyTypes) -> {
                            EnemyType childEnemyType = getById(enemyTypeId);
                            if (config.enemyTypeEnabled(childEnemyType)) {
                                int oldCntEnemies = counter.get(enemyTypeId) != null ? counter.get(enemyTypeId) : 0;
                                int diff = enemyTypes.size() - oldCntEnemies;
                                while (diff >= 0) {
                                    Trajectory trajectory = map.getRandomTrajectory(childEnemyType, spawnConfig);
                                    spawnEnemyWithTrajectory(childEnemyType, trajectory);
                                    diff--;
                                    getLog().debug("generate additional childEnemyType for EnemySpecialItem, " +
                                                    "childEnemyType: {}, diff: {}, enemySpecialId: {} ",
                                            childEnemyType.getName(), diff, enemyType);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private Trajectory getAndUpdateSpecialTrajectory(EnemyType enemyType, SpawnConfig spawnConfig) {
        Trajectory specialItemTrajectory = getMap().getRandomTrajectory(enemyType, spawnConfig);
        Trajectory tmpTraj = gameRoom.convertTrajectory(specialItemTrajectory, System.currentTimeMillis());
        return getMap().updatePointBySector(tmpTraj);
    }

    public Enemy generatePredefinedEnemies(boolean isSpecialItem, Enemy enemy, EnemyType enemyType) {
        if (isSpecialItem) {
            GameConfig config = gameRoom.getGame().getConfig(gameRoom.getRoomInfo().getId());
            EnemySpecialItem enemySpecialItem = (EnemySpecialItem) enemy;
            SpecialItem specialItem = config.getItems().get(enemyType).get(0);
            int specialItemPay;
            int enemiesPayout = 0;
            int additionalPayout = 0;

            specialItemPay = getPayForSpecialItem(enemySpecialItem, config);
            getLog().debug("Payout: Special EnemyId: {}, EnemyName: {}, Payout: {}", enemySpecialItem.getId(),
                    specialItem.getName(), specialItemPay);

            enemySpecialItem.setTotalPayout(specialItemPay);

            Map<Integer, List<List<EnemyType>>> enemiesForItemsByPay = config.getEnemiesForItemsByPay();

            if (enemyType.equals(F2)) {
                //F2: "freeze"

                enemySpecialItem.setAdditionalKillAwardPayout(enemySpecialItem.getTotalPayout());

            } else if (enemyType.equals(F3)) {
                //F3: "kill enemies of same type"

                Map<Integer, List<KillerItemData>> killItemsDataByPay = config.getKillItemsDataByPay();
                List<KillerItemData> killerItemData = killItemsDataByPay.get(specialItemPay);
                KillerItemData itemData = killerItemData.get(RNG.nextInt(killerItemData.size()));
                List<EnemyType> chain = new ArrayList<>();

                for (int i = 0; i < itemData.getCount(); i++) {
                    chain.add(itemData.getEnemy());
                }

                enemySpecialItem.setEnemiesForKilling(chain);
                enemySpecialItem.setAdditionalKillAwardPayout(itemData.getExtraPayout());

                getLog().debug("spawnEnemyWithTrajectory  F3 [special enemy] chain: {}, enemySpecialItem: {}",
                        chain, enemySpecialItem);

            } else if (enemiesForItemsByPay.containsKey(specialItemPay)) {

                enemySpecialItem.setAdditionalKillAwardPayout(0);

                List<List<EnemyType>> possibleChains = enemiesForItemsByPay.get(specialItemPay);
                List<EnemyType> currentChain = possibleChains.get(RNG.nextInt(possibleChains.size()));

                for(EnemyType currentEnemyType : currentChain) {
                    int pay = config.getEnemyData(currentEnemyType).getPay();
                    enemiesPayout += pay;
                }

                additionalPayout = specialItemPay - enemiesPayout;

                enemySpecialItem.setEnemiesForKilling(currentChain);
                enemySpecialItem.setAdditionalKillAwardPayout(additionalPayout);

                getLog().debug("spawnEnemyWithTrajectory [special enemy] predefined currentChain: {}, " +
                        "additionalPayout: {}, , enemySpecialItem: {} ", currentChain, additionalPayout, enemySpecialItem);

            } else {

                int maxCnt = 1000;
                List<EnemyType> midPayEnemies = MID_PAY_ENEMIES.getEnemies();
                List<EnemyType> currentChain = new ArrayList<>();

                while (maxCnt-- > 0) {

                    EnemyType nextEnemyType = midPayEnemies.get(RNG.nextInt(midPayEnemies.size()));
                    int pay = config.getEnemyData(nextEnemyType).getPay();

                    if (enemiesPayout + pay < specialItemPay) {
                        enemiesPayout += pay;
                        currentChain.add(nextEnemyType);
                    } else {
                        additionalPayout = specialItemPay - enemiesPayout;
                        maxCnt = 0;
                    }
                }

                enemySpecialItem.setEnemiesForKilling(currentChain);
                enemySpecialItem.setAdditionalKillAwardPayout(additionalPayout);

                getLog().debug("spawnEnemyWithTrajectory [special enemy] generated currentChain: {}, " +
                        "additionalPayout: {}, enemySpecialItem: {} ", currentChain, additionalPayout, enemySpecialItem);
            }

            if(Arrays.asList(EnemyType.F4, EnemyType.F5, EnemyType.F6, EnemyType.F7).contains(enemyType)) {
                //MQLEG-262: remove payout for Special Enemy Item F6 or F7, to avoid double payout
                //MQLEG-275: remove payout for Special Enemy Item F4 or F5, to avoid double payout
                //from the special Item and from its children
                enemySpecialItem.setTotalPayout(0);
            }
        }
        return enemy;
    }

    private static int getPayForSpecialItem(EnemySpecialItem enemySpecialItem, GameConfig config) {
        int pay;

        Map<EnemyType, List<SpecialItem>> configItems = config.getItems();
        EnemyType enemyType = enemySpecialItem.getEnemyType();
        List<SpecialItem> specialItems = configItems.get(enemyType);
        SpecialItem specialItem = specialItems.get(0);
        List<Prize> prizes = specialItem.getPrizes();

        if (Arrays.asList(EnemyType.F1, EnemyType.F7).contains(enemyType)) {
            //F1: "direct cash"
            //F7: "laser net"
            Prize randomPrize = MathData.getRandomElementFromWeightedList(prizes);
            pay = randomPrize.getPay();

        } else if (enemyType.equals(EnemyType.F4)) {
            //F4: "multiplier bomb"
            Prize randomPrize = MathData.getRandomElementFromWeightedList(prizes);
            pay = RNG.nextIntUniform(randomPrize.getMinPay(), randomPrize.getMaxPay());

        } else {
            //F2: "freeze"
            //F3: "kill enemies of same type"
            //F5: "chain reaction"
            //F6: "lightning"
            // Currently requires two items in the prizes list, good refactor candidate
            int min = prizes.stream().map(Prize::getPay).min(Integer::compareTo).orElse(0);
            int max = prizes.stream().map(Prize::getPay).max(Integer::compareTo).orElse(0);

            pay = RNG.nextIntUniform(min, max);
        }
        return pay;
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
            BossParams bossParams = config.getBosses().get(bossType.getSkinId());

            int defeatTresHold = bossParams.getMaxHP();

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
                if (shot.getWeaponId() != -1) {
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

            long realStakeInCents = seat.getStake().toCents() * multiplierPaidWeapons;
            getLog().debug("multiplierPaidWeapons: {}, paidSpecialShot: {}, realStakeInCents: {}",
                    multiplierPaidWeapons, paidSpecialShot, realStakeInCents);

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
            } else if (isInternalShot || (seat.getCurrentWeapon() != null && seat.getCurrentWeapon().getShots() > 0) || paidSpecialShot) {
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

        List<ShootResult> shootResults = shootWithSpecialWeaponAndUpdateState(time, seat, shot, shot.getWeaponId(), messages);

        for (ShootResult result : shootResults) {
            int awardedWeaponId = !result.isNewWeapon() ? -1 : result.getWeapon().getType().getId();
            processShootResult(seat, shot, result, messages, awardedWeaponId, result.isMainShot());
        }
        List<Seat> seats = gameRoom.getSeats();
        for (Seat seatCurrent : seats) {
            if (!isFRB()) {
                seatCurrent.transferWinToAmmo();
            }
        }
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

    protected void sendUpdateTrajectory(long time, double x, double y, int d, int freezeTime) {
        Map<Long, Trajectory> freezeTrajectories = gameRoom.getMap().generateBossEffectTrajectory(time, freezeTime, x, y, d);
        gameRoom.sendChanges(getTOFactoryService().createUpdateTrajectories(time, SERVER_RID,
                freezeTrajectories, freezeTime, EnemyAnimation.NO_ANIMATION.getAnimationId()));
    }

    private void adjustBossIfExists(EnemySpecialItem enemySpecialItem, Seat seat, IShot shot,
                                    ShootResultCalculator shootResultCalculator, List<ShootResult> results, int chMult) {

        GameConfig gameConfig = gameRoom.getGame().getConfig(seat);
        Long anyBossId = getMap().getAnyBossId();
        int betLevel = seat.getBetLevel();
        EnemyType enemyType = enemySpecialItem.getEnemyType();
        int totalPayout = enemySpecialItem.getTotalPayout();
        int randomBossPay = 0;


        if (enemyType.equals(F6) || enemyType.equals(F7)) {

            Enemy anyBoss;
            MinMaxParams minMaxParams = gameConfig.getPercentPayOnBoss().get(enemyType);

            if (isBossRound() && minMaxParams != null && minMaxParams.getMax() > 0 && anyBossId != 1) {

                anyBoss = getMap().getItemById(anyBossId);

                if (anyBoss != null) {

                    if (isEnemyAwayFromBorder(anyBoss, enemyType)) {

                        double percentRangeMode = 10;
                        double rageThreshold = (anyBoss.getFullEnergy() * percentRangeMode) / 100;
                        boolean regularBossMode = anyBoss.getEnergy() > rageThreshold;

                        if (regularBossMode) {

                            randomBossPay = shootResultCalculator.getBossRandomPayResult().getPay();

                            if (randomBossPay > anyBoss.getEnergy()) {
                                randomBossPay = (int) anyBoss.getEnergy();
                            }

                            Money payout = seat.getStake().getWithMultiplier(chMult * randomBossPay * betLevel);
                            seat.addTotalBossPayout(payout.toCents());

                            getLog().debug("adjustBossIfExists: Payout for Boss, chMult: {}, payout: {}, " +
                                            "randomBossPay: {}, betLevel: {}", chMult, payout, randomBossPay, betLevel);

                            String enemyNameKey = anyBoss.getEnemyType().getId() + "_" +
                                    anyBoss.getEnemyType().getName() + "_" + seat.getBetLevel();
                            seat.getCurrentPlayerRoundInfo()
                                    .updatePayoutsFromItems(payout, enemyNameKey, enemyType.getName());

                            double newEnergy = anyBoss.getEnergy() - randomBossPay;
                            if (newEnergy < 0) {
                                newEnergy = 0;
                            }

                            anyBoss.setEnergy(newEnergy);
                            ShootResult shootResult =
                                    new ShootResult(Money.ZERO, payout, false, false, anyBoss);
                            shootResult.setExplode(true);
                            results.add(shootResult);

                            long timeHitBoss = System.currentTimeMillis();
                            long timeToFreezeBoss = FREEZE_TIME_BOSS;

                            if (timeHitBoss - lastHitF6Time < FREEZE_TIME_BOSS && lastHitF6Time != 0) {
                                timeToFreezeBoss = timeHitBoss - lastHitF6Time;
                            }

                            lastHitF6Time = timeHitBoss;
                            sendUpdateTrajectory(timeHitBoss, shot.getX(), shot.getY(), 0, (int) timeToFreezeBoss);
                        }
                    }
                }
            }
        }

        getLog().debug("adjustBossIfExists: special item chMult: {}, randomBossPay: {}, totalPayout: {}",
                chMult, randomBossPay, totalPayout);
    }

    private void processSpecialItem(EnemySpecialItem enemySpecialItem, Seat seat, IShot shot, long time,
                                    ShootResultCalculator shootResultCalculator, List<ShootResult> results,
                                    ShootResult baseShootResult) {

        GameConfig gameConfig = gameRoom.getGame().getConfig(seat);
        EnemyType enemyType = enemySpecialItem.getEnemyType();
        int betLevel = seat.getBetLevel();

        getLog().debug("processSpecialItem: enemy is from special items list, enemy type:{}", enemySpecialItem);

        if (enemyType.equals(F2)) {
            //F2: "freeze"

            sendFreezeTrajectories(time, shot.getX(), shot.getY(), 0);
            lastFreezeTime = System.currentTimeMillis();
            seat.addAdditionalTempCounter(SEAT_FREEZE_WIN_AMOUNT, (int) enemySpecialItem.getAdditionalKillAwardPayout());

            getLog().debug("processSpecialItem: enemy is {} set lastFreezeTime={}", enemyType.getName(), lastFreezeTime);

        } else if (!enemyType.equals(F1)) {
            //F3: "Enemy Seeker"
            //F4: "Multiplier Bomb"
            //F5: "Chain Reaction Shot"
            //F6: "Arc Lighthing"
            //F7: "Laser Net"

            int chMult = enemySpecialItem.getCurrentMultiplier();
            if (chMult == 0) {
                chMult = 1;
            }

            adjustBossIfExists(enemySpecialItem, seat, shot, shootResultCalculator, results, chMult);

            long additionalKillAwardPayout = enemySpecialItem.getAdditionalKillAwardPayout();
            getLog().debug("processSpecialItem: additionalKillAwardPayout: {}", additionalKillAwardPayout);

            ShootResultCalculator.Result newAdditionalKillAwardPayResult =
                    shootResultCalculator.calculateNewAdditionalKillAwardPay();

            additionalKillAwardPayout = newAdditionalKillAwardPayResult.getPay();

            long killAwardFromEnemies = additionalKillAwardPayout * chMult;

            ShootResultCalculator.Result missPayResult = shootResultCalculator.calculateMissPay();
            int missPay = missPayResult.getPay();

            getLog().debug("processSpecialItem: New additionalKillAwardPayout: {}, chMult: {}, " +
                            "killAwardFromEnemies: {}, missPay: {} ",
                    additionalKillAwardPayout, chMult, killAwardFromEnemies, missPay);

            List<EnemyType> enemiesForKilling = enemySpecialItem.getEnemiesForKilling();

            for (EnemyType killEnemyType : enemiesForKilling) {

                int enemyPayout = MathData.getEnemyPayout(gameConfig, killEnemyType, 0);
                int enemyPay = chMult * enemyPayout;

                if (missPay > 0) {

                    missPay -= enemyPayout;

                    if (missPay < 0) {

                        int additionalMissPayout = Math.abs(missPay);
                        killAwardFromEnemies += (long) additionalMissPayout * chMult;
                        missPay = 0;
                        getLog().debug("processSpecialItem: special item enemy pay " +
                                        "additionalMissPayout: {}, killAwardFromEnemies: {}",
                                additionalMissPayout, killAwardFromEnemies);
                    }

                } else {

                    List<Enemy> enemiesToKill = getEnemiesByTypeWithApproximateTrajectory(killEnemyType, enemyType);
                    Enemy enemyToKill = null;

                    if (!enemiesToKill.isEmpty()) {
                        enemyToKill = enemiesToKill.get(RNG.nextInt(enemiesToKill.size()));
                    }

                    if (enemyToKill == null) {

                        killAwardFromEnemies += enemyPay;
                        getLog().debug("processSpecialItem: special item enemy pay " +
                                "killAwardFromEnemies: {}, enemyPay: {}", killAwardFromEnemies, enemyPay);

                    } else {

                        Money payout = seat.getStake().getWithMultiplier(enemyPay * betLevel);
                        ShootResult shootResult = new ShootResult(Money.ZERO, payout, false, true,
                                enemyToKill);

                        getLog().debug("processSpecialItem: Payout: {}, " +
                                "enemyPay: {}, betLevel: {}", payout, enemyPay, betLevel);

                        shootResult.setExplode(true);
                        shootResult.setChMult(chMult);

                        results.add(shootResult);

                        String enemyNameKey = enemyToKill.getEnemyType().getId() + "_" + enemyToKill.getEnemyType().getName() + "_" + seat.getBetLevel();

                        seat.getCurrentPlayerRoundInfo().updateStatNewWithMultiplier(Money.ZERO, false, false,
                                null, shootResult.getWin(), true, enemyNameKey, Money.ZERO, chMult, enemyType.getName());

                        checkEnemyKilled(shootResult);

                    }
                }
            }

            Money killAwardWin = seat.getStake().getWithMultiplier(killAwardFromEnemies * betLevel);
            Money resultKillAwardWin = baseShootResult.getKillAwardWin();
            Money newKillAwardWin = resultKillAwardWin.add(killAwardWin);

            getLog().debug("processSpecialItem: killAwardFromEnemies={}, " +
                            "killAwardWin={}, newKillAwardWin={}, enemies baseShootResult: {}",
                    killAwardFromEnemies, killAwardWin, newKillAwardWin, baseShootResult);

            baseShootResult.setKillAwardWin(newKillAwardWin);
            baseShootResult.setChMult(chMult);

            getLog().debug("processSpecialItem: Set Kill award enemies baseShootResult: {}", baseShootResult);
        }
    }

    @Override
    protected List<ShootResult> shootWithSpecialWeaponAndUpdateState(long time, Seat seat, IShot shot, int weaponId,
                                                                     ShotMessages messages) throws CommonException {
        List<ShootResult> results = new LinkedList<>();
        GameMap map = getMap();
        Long itemIdForShot = shot.getEnemyId();

        int liveEnemies = map.getItemsSize();

        if (liveEnemies == 0) {
            results.add(new ShootResult(seat.getStake(), Money.INVALID, false, false, null));
            getLog().debug("shootWithSpecialWeaponAndUpdateState: no live enemies, return kill Miss: message");
            return results;
        }

        Enemy baseEnemy = map.getItemById(itemIdForShot);
        EnemyType baseEnemyType;
        ShootResult baseShootResult;

        ShootResultCalculator shootResultCalculator =
                new ShootResultCalculator(this, baseEnemy, seat, getLog());

        if (baseEnemy != null) {

            baseEnemyType = baseEnemy.getEnemyType();
            getLog().debug("shootWithSpecialWeaponAndUpdateState: enemyTypeId: {}, Base enemy: {}",
                    baseEnemyType.getId(), baseEnemy);

            if (!map.isVisible(time, baseEnemy)) {

                getLog().debug("shootWithSpecialWeaponAndUpdateState: attempt to shoot on non visible target at {}",
                        baseEnemy.getLocation(time));
                return createErrorResult(seat, baseEnemy, true);
            }

            int payoutFromFreezeBossAndEnemies = shootResultCalculator.getFinalResult().getPay();

            boolean success =
                    singleShot(time, seat, baseEnemy, weaponId, results, true, payoutFromFreezeBossAndEnemies);

            if (success) {

                baseShootResult = results.get(0);
                baseShootResult.setMainShot(true);

                if (baseShootResult.isDestroyed()) {

                    ShootResultCalculator.Result freezeResult = shootResultCalculator.getFreezeResult();

                    if (freezeResult.isApplicable()) {

                        int remainingAdditionalWin = seat.getAdditionalTempCounters(SEAT_FREEZE_WIN_AMOUNT);
                        int randomPay = freezeResult.getPay();

                        int newRemainingAdditionalWin = remainingAdditionalWin - randomPay;
                        seat.resetAdditionalTempCounter(SEAT_FREEZE_WIN_AMOUNT);
                        String enemyNameKey = baseEnemy.getEnemyType().getId() + "_" + baseEnemy.getEnemyType().getName() + "_" + seat.getBetLevel();
                        Money payoutFromFreezeItem = seat.getStake().getWithMultiplier(randomPay * seat.getBetLevel());
                        seat.getCurrentPlayerRoundInfo().updatePayoutsFromItems(payoutFromFreezeItem, enemyNameKey, F2.getName());
                        baseShootResult.setWin(baseShootResult.getWin().add(payoutFromFreezeItem));

                        getLog().debug("shootWithSpecialWeaponAndUpdateState: Payout payoutFromFreezeItem: {}, " +
                                "betLevel: {}", payoutFromFreezeItem, seat.getBetLevel());

                        if (newRemainingAdditionalWin > 0) {
                            seat.addAdditionalTempCounter(SEAT_FREEZE_WIN_AMOUNT, newRemainingAdditionalWin);
                        }

                        getLog().debug("shootWithSpecialWeaponAndUpdateState: remainingAdditionalWin {}, " +
                                        "randomPay: {}, newRemainingAdditionalWin: {}",
                                remainingAdditionalWin, randomPay, newRemainingAdditionalWin);

                    }
                }

            } else {

                getLog().debug("shootWithSpecialWeaponAndUpdateState: Base enemy is invulnerable");
                return createErrorResult(seat, baseEnemy, true);
            }

        } else {

            // killed earlier
            getLog().debug("shootWithSpecialWeaponAndUpdateState: Base enemy was killed earlier, id to shoot:{}",
                    itemIdForShot);
            return createErrorResult(seat, null, false);
        }

        Optional<ShootResult> first = results.stream()
                .filter(shootResult ->
                        shootResult.getEnemy() != null && (shootResult.getEnemy().getId() == itemIdForShot)
                ).findFirst();

        // Understand if there is a chance for this to happen, looks like it can be removed
        if (!first.isPresent()) {

            boolean winExists = results.stream().anyMatch(shootResult ->
                    shootResult.getWin().greaterThan(Money.ZERO) ||
                    !shootResult.getAwardedWeapons().isEmpty() ||
                    !shootResult.getPrize().isEmpty()
            );

            if (winExists) {
                getLog().error("shootWithSpecialWeaponAndUpdateState: found shot results without main enemy, " +
                        "shot: {}, results: {} ", shot, results);
                throw new CommonException("Invalid shoot results");
            }

            return results;

        } else {

            int betLevel = seat.getBetLevel();
            getLog().debug("shootWithSpecialWeaponAndUpdateState: decrement ammo for regular weapon with " +
                    "betLevel: {}", betLevel);
            seat.decrementAmmoAmount(betLevel);
            seat.incrementBulletsFired();

            if (EnemyRange.SPECIAL_ITEMS.contains(baseEnemyType) && baseShootResult.isDestroyed()) {

                processSpecialItem((EnemySpecialItem) baseEnemy, seat, shot, time,
                        shootResultCalculator, results, baseShootResult);
            }

            for (ShootResult shootResult : results) {

                getLog().debug("shootWithSpecialWeaponAndUpdateState: shootResult={}", shootResult);

                if (shootResult.isDestroyed()) {
                    EnemyType enemyType = (EnemyType) shootResult.getEnemy().getEnemyClass().getEnemyType();
                    map.addRemoveTime(enemyType, time);
                }
            }
        }

        return results;
    }

    private List<Enemy> getEnemiesByTypeWithApproximateTrajectory(EnemyType enemyType, EnemyType enemySpecialItemType) {
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

    public boolean isEnemyAwayFromBorder(Enemy enemy, EnemyType specialItemType) {
        if (!getMap().checkStartAndEndPoints(enemy.getTrajectory(), enemy.isBoss())) {
            return false;
        }
        if (enemy.isBoss()) {
            return getMap().checkStartAndEndPoints(enemy.getTrajectory(), enemy.isBoss());
        }
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
            timeToSearch += 3000;
        }
        getLog().debug("enemyType: " + enemy.getEnemyType());
        int offset = enemy.getCircularRadius(enemy.getEnemyType().getId());
        return getMap().isPointOnMapApproximate(trajectory, timeToSearch, offset);
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

    private boolean singleShot(long time, Seat seat, Enemy enemy, int weaponId, List<ShootResult> results, boolean isMainShot, int expectedPayoutFromFreezeBossAndEnemies) throws CommonException {

        getLog().debug("singleShot: time: {}, seat: {}, enemy: {}, weaponId: {}, results: {}, isMainShot:{}, " +
                        "expectedPayoutFromFreezeBossAndEnemies:{}",
                time, seat, enemy, weaponId, results, isMainShot, expectedPayoutFromFreezeBossAndEnemies);

        boolean success = false;

        ShootResult shootResult = shootToOneEnemy(time, seat, enemy, weaponId, isMainShot, expectedPayoutFromFreezeBossAndEnemies);

        getLog().debug("singleShot: shootResult: {}", shootResult);

        if (!shootResult.getWin().equals(Money.INVALID)) {
            results.add(shootResult);
            success = true;
        }

        getLog().debug("singleShot: success: {}, results: {}", success, results);

        return success;
    }

    @Override
    protected void processShootResult(Seat seat, IShot shot, IShootResult result, ShotMessages messages,
                                      int awardedWeaponId, boolean isLastResult) {

        long enemyId = result.getEnemy() == null ? shot.getEnemyId() : result.getEnemyId();
        getLog().debug("processShootResult: {}, enemyId: {}", result, enemyId);

        int realShotTypeId = shot.getWeaponId();
        int usedSpecialWeapon = shot.getWeaponId();
        seat.getCurrentPlayerRoundInfo().updateAdditionalData(currentModel);

        int betLevel = seat.getBetLevel();
        int newShots = 0;
        if (result.getWeapon() != null) {
            newShots = result.getWeapon().getShots();
        }

        boolean mainShot = result.isMainShot();
        getLog().debug("processShootResult: result.isMainShot(): {}", mainShot);

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
        getLog().debug("processShootResult: isHit: {}, isPrize: {}, isWin: {}, isWeapon: {}, isBossWin: {}, isAwardedWeapons: {}, isSpecialItemKill: {}, isKillAward: {}",
                isHit, isPrize, isWin, isWeapon, isBossWin, isAwardedWeapons, isSpecialItemKill, isKillAward);

        Map<Integer, List<IWinPrize>> hitResultBySeats = new HashMap<>();
        Map<Seat, IHit> messagesForSeatsLocal = new HashMap<>();
        IHit hitOwn = null;
        List<IHit> hitsForObserversLocal = new ArrayList<>();

        Money stake = (isSpecialWeapon || !mainShot) ? Money.ZERO : seat.getStake().getWithMultiplier(betLevel);
        Money bet = result.getBet();

        Money paidStake = Money.ZERO;
        boolean isPaidShotToBaseEnemy = shot.isPaidSpecialShot() && mainShot;
        if (isPaidShotToBaseEnemy) {
            stake = seat.getStake().getWithMultiplier(MathData.getPaidWeaponCost(gameRoom.getGame().getConfig(seat), shot.getWeaponId()) * seat.getBetLevel());
            paidStake = new Money(stake.getValue());
        }

        getLog().debug("processShootResult: real stake: {}, isPaidShotToBaseEnemy: {}", stake.toDoubleCents(), isPaidShotToBaseEnemy);

        if (result.isKilledMiss() || result.isInvulnerable()) {
            seat.incrementMissCount();
            seat.getCurrentPlayerRoundInfo().addKilledMissCounter(shot.getWeaponId(), 1);
            if (isPaidShotToBaseEnemy) {
                getLog().debug("processShootResult: found main result of killedMiss, result: {}", result);
            }

            messages.add(
                    getTOFactoryService().createMiss(getCurrentTime(), TObject.SERVER_RID,
                            seat.getNumber(), result.isKilledMiss(), awardedWeaponId, enemyId, usedSpecialWeapon,
                            seat.getSpecialWeaponRemaining(), 0, isLastResult, shot.getX(), shot.getY(),
                            newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                            betLevel, 0, result.getEffects(), shot.getBulletId()),
                    getTOFactoryService().createMiss(getCurrentTime(), shot.getRid(),
                            seat.getNumber(), result.isKilledMiss(), awardedWeaponId, enemyId, usedSpecialWeapon,
                            seat.getSpecialWeaponRemaining(), 0, isLastResult, shot.getX(), shot.getY(),
                            newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                            betLevel, 0, result.getEffects(), shot.getBulletId()));

        } else {
            IEnemyClass enemyClass = result.getEnemy().getEnemyClass();
            IEnemyType enemyType = enemyClass.getEnemyType();
            String enemyNameKey = enemyType.getId() + "_" + enemyType.getName() + "_" + betLevel;

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
                            getLog().debug("processShootResult: enemy shared Win: seat accountId: {} enemyWinForSeat: {} seatId: {} additionalWins: {}",
                                    seatCurrent.getAccountId(), enemyWinForSeat, seatCurrent.getId(), additionalWins);
                            seatCurrent.incrementRoundWin(enemyWinForSeat);
                            seatCurrent.incrementShotTotalWin(enemyWinForSeat);
                            seatCurrent.addLastWin(enemyWinForSeat);
                            totalSimpleWin = totalSimpleWin.add(enemyWinForSeat);


                            if (killAwardWin.greaterThan(Money.ZERO)) {
                                getLog().debug("processShootResult: KillAwardWin: {}", killAwardWin);
                                /*currentPlayerRoundInfo.updateStatNewWithMultiplier(seat.getStake(), false, false, null,
                                        killAwardWin, true, enemyNameKey, Money.ZERO, result.getChMult() == 0 ? 1 : result.getChMult(), null);*/
                                currentPlayerRoundInfo.updateKillAwardWin(killAwardWin, enemyNameKey);
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

                        getLog().debug("processShootResult: aid current: {}, aid of shooter current: {}, " +
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

                            getLog().debug("processShootResult: isSpecialWeapon {}, isLastResult: {}, stake: {}, title: {}, realStake: {}",
                                    isSpecialWeapon, isLastResult, seat.getStake(), title, stake);

                            if (bet.getValue() > 0) {
                                getLog().debug("processShootResult: processing name: {}", enemyNameKey);
                                seat.getCurrentPlayerRoundInfo().updateStatNewWithMultiplier(stake, result.isShotToBoss(), isSpecialWeapon,
                                        title, enemyWinForSeat, result.isDestroyed(), enemyNameKey, paidStake, result.getChMult() == 0 ? 1 : result.getChMult(), null);
                            }
                            /*seat.getCurrentPlayerRoundInfo().updateStatNew(stake, result.isShotToBoss(), isSpecialWeapon,
                                    title, enemyWinForSeat, result.isDestroyed(), enemyNameKey, paidStake);*/
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
                        getLog().debug("processShootResult: allow spawn after killing of boss, subround set to BASE");
                    }
                }

            } else {
                seat.getCurrentPlayerRoundInfo().addMissCounter(seat.getCurrentWeaponId(), 1);

                String title = isSpecialWeapon ? SpecialWeaponType.values()[realShotTypeId].getTitle() : null;

                seat.getCurrentPlayerRoundInfo().updateStatNewWithMultiplier(stake, result.isShotToBoss(), isSpecialWeapon,
                        title, Money.ZERO, result.isDestroyed(), enemyNameKey, paidStake, result.getChMult() == 0 ? 1 : result.getChMult(), null);

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

    public ShootResult shootToOneEnemy(long time, Seat seat, Enemy enemy, int weaponId, boolean isMainShot, int expectedPayoutFromFreezeBossAndEnemies) throws CommonException {
        boolean isShotWithSpecialWeapon = weaponId != REGULAR_WEAPON;
        Money stake = seat.getStake();

        getLog().debug("shootToOneEnemy: isShotWithSpecialWeapon: {}, stake:{}", isShotWithSpecialWeapon, stake);

        // killed earlier
        if (enemy == null) {
            getLog().debug("shootToOneEnemy: enemy is null, killed earlier ");
            return new ShootResult(isShotWithSpecialWeapon ? Money.ZERO : stake, Money.INVALID, false, false, null);
        }

        getLog().debug("PlayerId: {}, enemyId: {}, enemyType: {}",
                seat.getPlayerInfo().getId(), enemy.getId(), enemy.getEnemyType().getName());

        if (enemy.getEnemyType().getId() == B3.getId()) {
            getLog().debug("shootToOneEnemy: enemy type is {}", enemy.getEnemyType());

            for (Enemy item : getMap().getItems()) {
                if (item.getParentEnemyId() == enemy.getId() || item.getParentEnemyTypeId() == enemy.getEnemyType().getId()) {
                    getLog().debug("shootToOneEnemy: child enemy found: {}", item);
                    return new ShootResult(isShotWithSpecialWeapon ? Money.ZERO : stake, Money.INVALID, false, false, null, true);
                }
            }
        }

        Integer numberOfPlayers = (int) gameRoom.getAllSeats().stream().filter(Objects::nonNull).count();
        ShootResult shootResult = gameRoom.getGame()
                .doShoot(enemy, seat, stake, isBossRound(), getTOFactoryService(), isMainShot, expectedPayoutFromFreezeBossAndEnemies, numberOfPlayers);

        if (shootResult.isBossShouldBeAppeared() && !isBossRound() && allowSpawn) {
            spawnBossTestStand = true;
            getLog().debug("shootToOneEnemy: Boss will be appeared later");
        }

        checkEnemyKilled(shootResult);
        shootResult.setWeaponSurpluses(seat.getWeaponSurplus());

        getLog().debug("shootToOneEnemy: shootResult={}", shootResult);

        return shootResult;
    }

    private void checkEnemyKilled(ShootResult shootResult) {
        IEnemy<EnemyClass, Enemy> enemy = shootResult.getEnemy();

        if (shootResult.isDestroyed()) {

            if (!enemy.isBoss() && enemy.getLives() > 0) {

                getLog().debug("checkEnemyKilled: enemy {} lost a life, {} lives remaining", enemy.getId(), enemy.getLives());

                enemy.setLives(enemy.getLives() - 1);
                enemy.setEnergy(enemy.getFullEnergy());
                shootResult.setDestroyed(false);

            } else {

                getLog().debug("checkEnemyKilled: enemy {} is killed", enemy.getId());

                getMap().removeItem(enemy.getId());

                getLog().debug("checkEnemyKilled: count of enemies after shoot: {}", getMap().getItemsSize());
                getLog().debug("checkEnemyKilled: getCountRemainingEnemiesByModel: {}", getCountRemainingEnemiesByModel());
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
