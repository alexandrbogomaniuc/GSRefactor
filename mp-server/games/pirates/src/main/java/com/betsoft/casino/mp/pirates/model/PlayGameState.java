package com.betsoft.casino.mp.pirates.model;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.BossPartEnemy;
import com.betsoft.casino.mp.model.gameconfig.EnemyParams;
import com.betsoft.casino.mp.model.gameconfig.GameConfig;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.quests.ITreasureProgress;
import com.betsoft.casino.mp.pirates.model.math.*;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.Triple;
import com.google.common.collect.ImmutableList;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.model.EnemyDestroyReason.SIMPLE_SHOT;
import static com.betsoft.casino.mp.model.SpecialWeaponType.Airstrike;
import static com.betsoft.casino.mp.model.SpecialWeaponType.DoubleStrengthPowerUp;
import static com.betsoft.casino.mp.pirates.model.math.EnemyRange.*;
import static com.betsoft.casino.mp.pirates.model.math.EnemyType.*;
import static com.betsoft.casino.mp.utils.ErrorCodes.*;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

@SuppressWarnings("Duplicates")
public class PlayGameState extends AbstractActionPlayGameState<GameRoom, Seat, GameMap, PlayGameState> {
    protected static final int LEVIATHAN_SKIN_ID = 3;
    private static int MAX_ALIVE_ENEMIES = 50;
    private static int MAX_ALIVE_PIRATES = 4;
    protected transient long lastTimeOfGenerationBird;
    private transient boolean needImmediatelySpawn = false;
    private transient Map<Integer, Long> leaveAndDestroyEnemiesTime = new HashMap<>();
    static transient List<Integer> crabsWithOpenLuke = ImmutableList.of(19, 20, 21, 22, 23);
    static transient List<Integer> ratsWithOpenLuke = ImmutableList.of(102, 104);
    transient List<Integer> notSafeHatchTrajectories = new ArrayList<>();
    transient LimitChecker limitChecker = new LimitChecker();

    public PlayGameState() {
        super();
    }

    public PlayGameState(GameRoom gameRoom) {
        super(gameRoom, new QuestManager(gameRoom.getTOFactoryService()));
    }

    @Override
    protected int getMaxAliveEnemies() {
        return MAX_ALIVE_PIRATES;
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

    @Override
    public void init() throws CommonException {
        super.init();
        leaveAndDestroyEnemiesTime = new HashMap<>();
        notSafeHatchTrajectories = new ArrayList<>();
        if (getMap().getId() == 202) {
            for (EnemyType enemyType : EnemyType.values()) {
                List<Integer> ids = getMap().getPredefinedTrajectoryIds(enemyType.getId());
                if (ids != null) {
                    notSafeHatchTrajectories.addAll(ids);
                }
            }
        }
    }

    @Override
    protected void updateWithLock() throws CommonException {
        GameMap map = getMap();
        List<Integer> removedEnemies = map.updateAndReturnListTypes();
        removedEnemies.forEach(typeId -> getLeaveAndDestroyEnemiesTime().put(typeId, System.currentTimeMillis()));

        checkTestStandFeatures();

        try {
            calculateMinesOnMap();
        } catch (Exception e) {
            getLog().debug("calculateMinesOnMap error: ", e);
        }

        getMap().checkFreezeTimeEnemies(FREEZE_TIME_MAX);

        if (map.getItemsSize() >= MAX_ALIVE_ENEMIES && !isManualGenerationEnemies()) {
            return;
        }

        HashSet<Integer> liveIdScenariosCrabs = new HashSet<>();
        HashSet<Integer> liveIdSwarmParamsRats = new HashSet<>();

        List<Triple<Integer, Boolean, Integer>> itemsTypeIdsAndSwarmState = map.getItemsTypeIdsAndSwarmStateAndIds();

        if (isNeedMinimalEnemies() && limitChecker.getTotalEnemies() > 3) {
            return;
        }

        for (Triple<Integer, Boolean, Integer> triple : itemsTypeIdsAndSwarmState) {
            EnemyType enemyType = EnemyType.getById(triple.first());
            if (EnemyRange.Scarabs.getEnemies().contains(enemyType)) {
                if (enemyType.getId() < 3) {
                    liveIdSwarmParamsRats.add(triple.third());
                } else {
                    liveIdScenariosCrabs.add(triple.third());
                }
            }
        }

        if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            getLog().debug("no live enemies in room:, finish ");
            nextSubRound();
            return;
        }

        boolean needRandomBoss = RNG.nextInt(50) == 0;

        AtomicBoolean notAllowLeviathan = new AtomicBoolean(false);
        if (getCurrentMapId() == 202) {
            crabsWithOpenLuke.forEach(id -> {
                if (liveIdScenariosCrabs.contains(id)) {
                    notAllowLeviathan.set(true);
                }
            });
            ratsWithOpenLuke.forEach(id -> {
                if (liveIdSwarmParamsRats.contains(id)) {
                    notAllowLeviathan.set(true);
                }
            });
            notSafeHatchTrajectories.forEach(id -> {
                if (map.getNumberOfEnemiesWithTrajectoryId(id) > 0) {
                    notAllowLeviathan.set(true);
                }
            });
        }

        if (!isManualGenerationEnemies() && !needWaitingWhenEnemiesLeave  &&
                !notAllowLeviathan.get() && subround.equals(PlaySubround.BASE) && remainingNumberOfBoss > 0
                && (isNeedImmediatelySpawn() || needRandomBoss || getMap().noEnemiesInRoom())) {
            mainBossIsAvailable = true;
            nextSubRound();
            mainBossIsAvailable = false;
            getLog().debug(" generate Boss, remainingNumberOfBoss : {} ", remainingNumberOfBoss);
            return;
        }

        if (allowSpawn && !isManualGenerationEnemies()) {
            boolean notAllowTrajectoriesViaHatch = isLeviathanLive() ||
                    (remainingNumberOfBoss > 0 && getCurrentMapId() == 202 && notAllowLeviathan.get());

            map.updateEnemyLimits(limitChecker);
            gameRoom.sendNewEnemiesMessage(map.respawnEnemies(limitChecker, notAllowTrajectoriesViaHatch));

            if (!needWaitingWhenEnemiesLeave && allowSpawn) {
                if (limitChecker.isRunnerSpawnAllowed() && (RNG.nextInt(100) < 7 || isNeedImmediatelySpawn())) {
                    checkForGenerationStandaloneHV(EnemyRange.RUNNERS, notAllowTrajectoriesViaHatch);
                }

                if (RNG.nextInt(100) < 3 || isNeedImmediatelySpawn()) {
                    checkForGenerationStandaloneHV(EnemyRange.TROLLS, notAllowTrajectoriesViaHatch);
                } else if (RNG.nextInt(100) < 3 || isNeedImmediatelySpawn()) {
                    checkForGenerationStandaloneHV(EnemyRange.WEAPON_CARRIER, notAllowTrajectoriesViaHatch);
                }
            }

            if (limitChecker.isCrabsSpawnAllowed() && RNG.nextInt(10) == 0) {
                if (notAllowTrajectoriesViaHatch) {
                    liveIdScenariosCrabs.addAll(crabsWithOpenLuke); // not allow crabs via open luke
                }
                spawnCrabsSwarm(liveIdScenariosCrabs, limitChecker.getCrabsAllowed());
            }

            if (limitChecker.isRatsSpawnAllowed() && RNG.nextInt(5) == 0) {
                if (notAllowTrajectoriesViaHatch) {
                    liveIdSwarmParamsRats.addAll(ratsWithOpenLuke); // not allow rats via open luke
                }
                spawnRatsSwarm(liveIdSwarmParamsRats, limitChecker.getRatsAllowed());
            }

            if (limitChecker.isDeckhandSpawnAllowed() && RNG.nextInt(25) == 0) {
                List<EnemyType> enemies = ENEMIES_DECKHAND.getEnemies();
                EnemyType enemyType = enemies.get(RNG.nextInt(enemies.size()));
                spawnEnemyWithClones(enemyType, notAllowTrajectoriesViaHatch);
            }

            if (getMap().swarmCount(SwarmType.NECKBEARDS) < 2 && RNG.nextInt(20) == 0) {
                List<EnemyType> enemies = ENEMIES_NECKBEARD.getEnemies();
                EnemyType enemyType = enemies.get(RNG.nextInt(enemies.size()));
                spawnEnemyWithClones(enemyType, notAllowTrajectoriesViaHatch);
            }

            if (limitChecker.getCount(Mummies) < getMaxAliveEnemies() || isNeedImmediatelySpawn()) {
                if (limitChecker.isCaptainsSpawnAllowed() && RNG.nextInt(100) < 30) {
                    if (limitChecker.isSpawnAllowed(ENEMY_15)) {
                        if (limitChecker.isSpawnAllowed(ENEMY_16)) {
                            spawnEnemy(CAPTAINS, -1, null, -1, 15000, notAllowTrajectoriesViaHatch);
                        } else {
                            spawnEnemy(ENEMY_15, 1, notAllowTrajectoriesViaHatch);
                        }
                    } else if (limitChecker.isSpawnAllowed(ENEMY_16)) {
                        spawnEnemy(ENEMY_16, 1, notAllowTrajectoriesViaHatch);
                    }
                } else {
                    spawnEnemy(SINGLE_ENEMIES, -1, null, -1, 15000, notAllowTrajectoriesViaHatch);
                }
            }

            boolean needBirdsSmall = limitChecker.isSpawnAllowed(ENEMY_9);
            boolean needBirdsNormal = limitChecker.isSpawnAllowed(ENEMY_10);
            boolean realCase = (needBirdsSmall || needBirdsNormal) && System.currentTimeMillis() - lastTimeOfGenerationBird > 3000;
            if (realCase || isNeedImmediatelySpawn()) {
                if (needBirdsSmall) spawnEnemyWithClones(ENEMY_9, false);
                if (needBirdsNormal) spawnEnemyWithClones(EnemyType.ENEMY_10, false);
                lastTimeOfGenerationBird = System.currentTimeMillis();
            }
        } else if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            nextSubRound();
        }
    }

    private boolean isLeviathanLive() {
        Long bossId = getMap().getAnyBossId();
        if (bossId != -1 && getCurrentMapId() == 202) {
            Enemy itemById = getMap().getItemById(bossId);
            return itemById.getSkin() == LEVIATHAN_SKIN_ID;
        }
        return false;
    }

    protected void onTimerWithLock(boolean needClearEnemy) {
        getLog().debug("PlayGameState:: onTimer: current=" + this);
        getLog().debug("End round, aliveMummies: " + getMap().getItemsSize()
                + " needWaitingWhenEnemiesLeave: " + needWaitingWhenEnemiesLeave);

        if (!needWaitingWhenEnemiesLeave) {
            needWaitingWhenEnemiesLeave = true;
            allowSpawn = false;
            allowSpawnHW = false;
            mainBossIsAvailable = false;
            remainingNumberOfBoss = 0;
            getMap().clearInactivityLiveItems();
            if(needClearEnemy)
                getMap().removeAllEnemies();

            sendLeaveTrajectories();
            gameRoom.sendChanges(getTOFactoryService().createRoundFinishSoon(System.currentTimeMillis()));
        }
    }

    boolean noAnyEnemiesInRound() {
        return getMap().noEnemiesInRoom() && getCountRemainingEnemiesByModel() == 0 && remainingNumberOfBoss <= 0
                && getMap().getNumberInactivityItems() == 0;
    }

    private boolean checkForGenerationStandaloneHV(EnemyRange range, boolean useCustomTrajectories) {
        int skinId = 1;
        if (limitChecker.getCount(range) >= 1) {
            return false;
        }

        AtomicBoolean canGenerateEnemyFromRange = new AtomicBoolean(true);
        long currentTime = System.currentTimeMillis();
        range.getEnemies().forEach(enemyType -> {
            Long lastTime = getLeaveAndDestroyEnemiesTime().get(enemyType.getId());
            if (lastTime != null && (currentTime - lastTime) < 10000) {
                canGenerateEnemyFromRange.set(false);
            }
        });

        if (!canGenerateEnemyFromRange.get()) {
            return false;
        }

        long spawnEnemyId = spawnEnemy(range, skinId, null, -1, 15000, useCustomTrajectories);
        if (spawnEnemyId != -1) {
            getLog().debug("checkForGenerationStandaloneHV " + " skinId: " + skinId + " enemyRange: "
                    + " spawnEnemyId: " + spawnEnemyId);
        }
        return true;
    }


    private void calculateMinesOnMap() throws CommonException {
        List<Seat> seats = getRoom().getSeats();
        GameMap map = getMap();
        Map<Pair<Integer, Integer>, Double> customDistances;
        customDistances = new HashMap<>();
        customDistances.put(new Pair<>(EnemyType.Boss.getId(), 1), 80.);
        customDistances.put(new Pair<>(EnemyType.Boss.getId(), 2), 80.);
        customDistances.put(new Pair<>(EnemyType.Boss.getId(), 3), 80.);

        for (Seat seat : seats) {
            List<MinePoint> seatMines = new ArrayList<>(seat.getSeatMines());
            boolean mineExploded = false;
            if (!seat.isDisconnected() && !seatMines.isEmpty()) {
                for (MinePoint seatMine : seatMines) {
                    if (seatMine == null)
                        continue;
                    Long nearestEnemyFotMine = map.getNearestEnemyForMine(
                            new PointD(seatMine.getX(), seatMine.getY()), customDistances);

                    if (nearestEnemyFotMine != -1) {
                        try {
                            String mineId = seatMine.getMineId((int) seat.getId());
                            Boolean isPaidSpecialShot = seat.getMineStates().get(mineId);
                            gameRoom.processShot(seat, getTOFactoryService().createShot(seatMine.getTimePlace(), -1,
                                    SpecialWeaponType.Landmines.getId(), nearestEnemyFotMine, 0, 0, isPaidSpecialShot),
                                    true);
                            mineExploded = true;
                        } catch (Exception e) {
                            getLog().error("calculateMinesOnMap, error processing processShot for account: {}",
                                    seat.getAccountId(), e);
                        }
                    }
                }
                if (mineExploded) {
                    getLog().debug("calculateMinesOnMap account: {}, seatMinesNew: {}", seat.getAccountId(),
                            seat.getSeatMines());
                    getLog().debug("calculateMinesOnMap seat mine weapon: {}",
                            seat.getWeapons().get(SpecialWeaponType.Landmines));
                }
            }
        }
    }

    private void spawnCrabsSwarm(HashSet<Integer> liveIdScenariosCrabs, int max) {
        gameRoom.sendNewEnemiesMessage(gameRoom.getMap().addScarabSwarm(liveIdScenariosCrabs, max));
    }

    private void spawnRatsSwarm(HashSet<Integer> liveIdSwarmParamsRats, int max) {
        gameRoom.sendNewEnemiesMessage(gameRoom.getMap().addRatsSwarm(liveIdSwarmParamsRats, max));
    }


    @Override
    public void nextSubRound() throws CommonException {
//        needWaitingWhenEnemiesLeave = false;
        GameMap map = gameRoom.getMap();
        switch (subround) {
            case BASE:
                if (mainBossIsAvailable && remainingNumberOfBoss > 0) {
                    map.setPossibleEnemies(EnemyRange.Boss);
                    firePlaySubroundFinished(false);
                    int bossSkinId = RNG.nextInt(3) + 1;
                    Integer bossForRoom = TestStandLocal.getInstance().getBossForRoom(gameRoom.getId());
                    if (bossForRoom != null && bossForRoom != -1) {
                        bossSkinId = bossForRoom;
                        getLog().debug("boss from teststand: {}", bossForRoom);
                    }

                    if (bossSkinId == LEVIATHAN_SKIN_ID && getCurrentMapId() != 202) {
                        bossSkinId = RNG.nextInt(2) + 1;
                    }

                    spawnBoss(bossSkinId);
                    timeOfStartBossRound = lastShotTime = System.currentTimeMillis();
                    remainingNumberOfBoss--;
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
            finish();
        } catch (CommonException e) {
            getLog().error("Unexpected error", e);
        } finally {
            if(lockShots != null && lockShots.isLocked()) {
                lockShots.unlock();
            }
        }
    }

    @Override
    protected void spawnMummy() {
    }

    private boolean needFinalSteps() {
//        boolean isBossRound = subround == PlaySubround.BOSS;
//        boolean smallNumberEnemies = getMap().getItems().size() + getMap().getNumberInactivityItems() + getCountRemainingEnemiesByModel() <= 10;
//
//        if (isBossRound || smallNumberEnemies) {
//            return false;
//        }
        return true;
    }

    private Long spawnEnemy(EnemyType enemyType, int skinId, boolean useCustomTrajectories) {
        long res = -1;

        try {
            Enemy enemy = getMap().addEnemyByTypeNew(enemyType, getMap().createMathEnemy(enemyType), skinId, -1,
                    false, true, useCustomTrajectories);

            long startTime = enemy.getTrajectory().getPoints().get(0).getTime();
            long endTime = enemy.getTrajectory().getLastPoint().getTime();
            getLog().debug("spawnEnemy, new enemy: {}", enemy);
            getLog().debug("spawnEnemy, new startTime: {}, endTime: {}, diff {} ",
                    startTime, endTime, (endTime - startTime));
            res = enemy.getId();
            gameRoom.sendNewEnemyMessage(enemy);
        } catch (Exception e) {
            getLog().debug("spawn enemy error type: {}, skinId: {}", enemyType, skinId, e);
        }
        return res;
    }

    private Long spawnEnemy(EnemyRange range, int skinId, Trajectory oldTrajectory, long parentEnemyId, long respawnDelay, boolean useCustomTrajectories) {
        long res = -1;

        try {
            List<EnemyType> enemies = range.getEnemies();
            EnemyType enemyType = enemies.get(RNG.nextInt(enemies.size()));

            if (range.equals(EnemyRange.HV_ENEMIES)) {
                List<Integer> itemsTypeIds = getMap().getItemsTypeIds();
                long count = itemsTypeIds.stream().filter(id -> id == enemyType.getId()).count();
                getLog().debug("finalEnemyType: {} count: {}", enemyType, count);
                getLog().debug("itemsTypeIds: {}", itemsTypeIds);
                if (count >= 1)
                    return res;
            }

            boolean needNearCenter = range.equals(EnemyRange.MINI_BOSS);
            Enemy enemy = getMap().addEnemyByTypeNew(enemyType, getMap().createMathEnemy(enemyType), skinId, parentEnemyId, needNearCenter,
                    needFinalSteps(), useCustomTrajectories);
            if (respawnDelay > 0) {
                enemy.setShouldReturn(true);
                enemy.setRespawnDelay(respawnDelay);
            }

            long startTime = enemy.getTrajectory().getPoints().get(0).getTime();
            long endTime = enemy.getTrajectory().getLastPoint().getTime();
            getLog().debug("spawnEnemy, new enemy: {}", enemy);
            getLog().debug("spawnEnemy, new startTime: {}, endTime: {}, diff {} ",
                    startTime, endTime, (endTime - startTime));
            res = enemy.getId();
            gameRoom.sendNewEnemyMessage(enemy);

        } catch (Exception e) {
            getLog().debug("spawn enemy error , range: {}, skinId: {}, oldTrajectory: {}", range, skinId, oldTrajectory);
            getLog().debug("spawn enemy error: ", e);
        }
        return res;
    }

    private void spawnEnemyWithClones(EnemyType enemyType, boolean useCustomTrajectories) {
        try {
            int numberEnemiesInGroup = 0;
            boolean isDeckhand = ENEMIES_DECKHAND.getEnemies().contains(enemyType);
            boolean isNeckbeard = ENEMIES_NECKBEARD.getEnemies().contains(enemyType);
            SwarmType swarmType = null;

            if (enemyType.equals(ENEMY_9)) {
                numberEnemiesInGroup = 4;
                swarmType = SwarmType.WHITE_BIRDS;
            } else if (enemyType.equals(EnemyType.ENEMY_10)) {
                numberEnemiesInGroup = 1;
                swarmType = SwarmType.RED_BIRDS;
            } else if (isDeckhand) {
                numberEnemiesInGroup = RNG.nextInt(2) + 2;
                swarmType = SwarmType.DECKHANDS;
            } else if (isNeckbeard) {
                numberEnemiesInGroup = 1;
                swarmType = SwarmType.NECKBEARDS;
            }


            getLog().debug("spawnEnemyWithClones enemyType: {}, numberEnemiesInGroup: {}", enemyType,
                    numberEnemiesInGroup);

            List<Enemy> listEnemiesInGroup = new ArrayList<>();

            EnemyType firstEnemyType = enemyType;
            boolean needNeckhandInHand = false;
            if (isDeckhand && RNG.nextBoolean()) {
                firstEnemyType = EnemyType.ENEMY_14;
                numberEnemiesInGroup = 2;
                needNeckhandInHand = true;
            }

            boolean isBird = BIRDS.getEnemies().contains(firstEnemyType);
            Long bossId = getMap().getAnyBossId();
            boolean isLeviathanLive = false;

            if (bossId != -1 && getCurrentMapId() == 202) {
                Enemy itemById = getMap().getItemById(bossId);
                isLeviathanLive = itemById.getSkin() == LEVIATHAN_SKIN_ID;
            }

            boolean canTrajectoriesViaLuke202 = !isBird && !isLeviathanLive &&
                    getCurrentMapId() == 202 && RNG.nextInt(100) < 80 && remainingNumberOfBoss == 0;

            Enemy firstEnemy = getMap().addEnemyByTypeNew(firstEnemyType, getMap().createMathEnemy(enemyType), 1,
                    -1, false, needFinalSteps(), useCustomTrajectories);

            firstEnemy.setParentEnemyId(firstEnemy.getId());
            listEnemiesInGroup.add(firstEnemy);
            List<Point> originalPoints = firstEnemy.getTrajectory().getPoints();
            long startShiftTime = 0;


            int[][] shiftsBirds = new int[][]{{0, 6}, {-2, -8}, {8, 2}, {-8, -2}};
            int[] shiftTimesBirds = new int[]{110, 80, 70, 40};
            int[][] shifts = shiftsBirds;
            int[] shiftTimes = shiftTimesBirds;

            if (needNeckhandInHand) {
                shifts = new int[][]{{0, 4}, {0, -4}, {4, 0}, {-4, 0}};
                shiftTimes = new int[]{800, 50, 50, 50};
            } else if (isDeckhand || isNeckbeard) {
                shifts = new int[][]{{0, 5}, {0, -5}, {5, 0}, {-5, 0}};
                shiftTimes = new int[]{50, 50, 50, 50};
            }

            List<Integer> randomIndexes = Arrays.asList(0, 1, 2, 3);
            if (isDeckhand)
                Collections.shuffle(randomIndexes);

            getLog().debug("spawnEnemyWithClones originalPoints: {}", originalPoints);

            for (int i = 0; i < numberEnemiesInGroup; i++) {
                startShiftTime += shiftTimes[i];
                Enemy additionalEnemy = getMap().addEnemyByTypeNew(enemyType, getMap().createMathEnemy(enemyType), 1,
                        firstEnemy.getParentEnemyId(), false, needFinalSteps(), useCustomTrajectories);

                List<Point> newPoints = new LinkedList<>();

                for (int j = 0; j < originalPoints.size(); j++) {
                    Point point = originalPoints.get(j);
                    boolean lastPoint = j == originalPoints.size() - 1;

                    int sx = shifts[randomIndexes.get(i)][0];
                    int sy = shifts[randomIndexes.get(i)][1];
                    long newTime = point.getTime() + startShiftTime;

                    double x = point.getX() + sx;
                    double y = point.getY() + sy;

                    if (x > 95) x = 95;
                    if (y > 95) y = 95;
                    if (x < 0) x = 0;
                    if (y < 0) y = 0;


                    Point lastNewPoint = null;
                    if (!newPoints.isEmpty()) {
                        lastNewPoint = newPoints.get(newPoints.size() - 1);
                    }

                    Point newPoint = new Point(x, y, newTime);
                    newPoints.add(newPoint);

                    Point additionalPoint = null;
                    PointI source = new PointI((int) x, (int) y);
                    GameMapShape mapShape = getMap().getMapShape();
                    boolean availableAndPassable = mapShape.isAvailableAndPassable(newPoint);
                    boolean spawnPoint = mapShape.isSpawnPoint((int) newPoint.getX(), (int) newPoint.getY());

                    getLog().debug("spawnEnemyWithClones source: {}, availableAndPassable: {}, isSpawnPoint: {}",
                            source, availableAndPassable, spawnPoint);


                    if (lastPoint && (availableAndPassable || !spawnPoint)) {
                        getLog().debug("spawnEnemyWithClones lastNewPoint: {}, newPoint: {}", lastNewPoint, newPoint);

                        double x0 = lastNewPoint.getX();
                        double y0 = lastNewPoint.getY();
                        double x1 = newPoint.getX();
                        double y1 = newPoint.getY();
                        double dx = x1 - x0;
                        double dy = y1 - y0;
                        double l = Math.sqrt(dx * dx + dy * dy);
                        double dirX_ = dx / l;
                        double dirY_ = dy / l;

                        int expectedDistanceStart = 10;
                        double resX = 0, resY = 0;
                        Point tempPoint = null;
                        int ii;
                        for (ii = 0; ii < 500; ii++) {
                            resX = dirX_ * expectedDistanceStart + x1;
                            resY = dirY_ * expectedDistanceStart + y1;
                            expectedDistanceStart++;
                            if (resX > 95) resX = 95;
                            if (resY > 95) resY = 95;
                            if (resX < 0) resX = 0;
                            if (resY < 0) resY = 0;

                            tempPoint = new Point(resX, resY, newPoint.getTime() + 800);

                            double dx_ = tempPoint.getX() - x0;
                            double dy_ = tempPoint.getY() - y0;
                            double l_new = Math.sqrt(dx_ * dx_ + dy_ * dy_);

                            boolean availableAndPassable_ = mapShape.isAvailableAndPassable(tempPoint);
                            if (l_new > l && (!availableAndPassable_)) {
//                                getLog().debug("spawnEnemyWithClones found spawn point " + tempPoint
//                                        + " l_new: " + l_new + " l: " + l
//                                );
                                break;
                            }
                        }

                        getLog().debug("ii: {} tempPoint: {}", ii, tempPoint);
                        getLog().debug("resX: {}", resX);
                        getLog().debug("resY: {}", resY);
                        additionalPoint = tempPoint;
                    }

                    getLog().debug("additionalPoint: {} ", additionalPoint);
                    if (additionalPoint != null) {
                        newPoints.add(additionalPoint);
                    }
                }

                double speed = additionalEnemy.getTrajectory().getSpeed();
                additionalEnemy.setTrajectory(new Trajectory(speed, newPoints));
                listEnemiesInGroup.add(additionalEnemy);

                getLog().debug("spawnEnemyWithClones enemyId: {}, newPoints: {}", additionalEnemy.getId(), newPoints);

            }

            if (swarmType != null) {
                int swarmId = getMap().generateSwarmId();
                for (Enemy enemy : listEnemiesInGroup) {
                    enemy.addToSwarm(swarmType, swarmId);
                }
                getMap().registerSwarm(swarmId, listEnemiesInGroup);
            }

            gameRoom.sendNewEnemiesMessage(listEnemiesInGroup);

        } catch (Exception e) {
            getLog().debug("spawnEnemyWithClones error: ", e);
        }
    }

    private Long spawnHVEnemyFromTestStand(EnemyType enemyType, int skinId, Trajectory oldTrajectory,
                                           long parentEnemyId) {
        long res;
        IMathEnemy mathEnemy = getMap().createMathEnemy(enemyType);
        mathEnemy.setSettingsEnemyId(1);
        boolean needNearCenter = EnemyRange.MINI_BOSS.getEnemies().contains(enemyType);

        Enemy enemy = (oldTrajectory == null) ? getMap().addEnemyByTypeNew(enemyType, mathEnemy, skinId,
                parentEnemyId, needNearCenter, needFinalSteps(), false) :
                getMap().addConcreteHVEnemy(enemyType, skinId, oldTrajectory, mathEnemy, parentEnemyId);

        getLog().debug("spawnEnemyFromTestStand , new HV enemy: {}", enemy);
        res = enemy.getId();
        gameRoom.sendNewEnemyMessage(enemy);
        return res;
    }


    @Override
    protected void generateHVEnemy(ShootResult result, ShotMessages messages, String sessionId) {

        TestStandFeature featureBySid = null;
        if (sessionId != null) {
            featureBySid = TestStandLocal.getInstance().getFeatureBySid(sessionId);
        }

        boolean needKillEnemyAndTryGetHVEnemy = featureBySid != null && featureBySid.getId() == 5;
        if (!needKillEnemyAndTryGetHVEnemy) {
            //this method is used in Pirates only for teststand.
            return;
        }
        EnemyRange enemyRange = null;
        int idx = RNG.nextInt(4);
        switch (idx) {
            case 0:
                enemyRange = EnemyRange.MINI_BOSS;
                break;
            case 1:
                enemyRange = EnemyRange.RUNNER;
                break;
            case 2:
                enemyRange = EnemyRange.WEAPON_CARRIER;
                break;
            case 3:
                enemyRange = EnemyRange.LOOT_RUNNER;
                break;
            default:
                break;
        }
        spawnHVEnemyFromTestStand(enemyRange.getEnemies().get(0), 1, null, -1);
    }

    private void spawnScarab() {
        spawnEnemy(EnemyRange.Scarabs, 1, null, -1, 0, false);
    }

    private EnemyRange getSmallEnemies() {
        return EnemyRange.Scarabs;
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

            EnemyBoss enemy = (EnemyBoss) getMap().addEnemyByTypeNew(EnemyType.Boss, mathEnemy,
                    bossSkinId,-1, false, needFinalSteps(), false);
            enemy.setEnergy(healthBoss);

            getLog().debug("spawnBoss, new enemy: {}, ", enemy);
            gameRoom.sendNewEnemyMessage(enemy);
        } finally {
            lockShots.unlock();
        }
    }

    @Override
    public void placeMineToMap(Seat seat, IMineCoordinates mineCoordinates) throws CommonException {

        gameRoom.getPlayerInfoService().lock(seat.getAccountId());
        getLog().debug("placeMineToMap HS lock: {}", seat.getAccountId());

        try {
            if (seat.getCurrentWeaponId() != SpecialWeaponType.Landmines.getId()) {
                sendError(seat, mineCoordinates, WRONG_WEAPON, "Wrong weapon", mineCoordinates);
                return;
            }

            int shots = seat.getWeapons().get(SpecialWeaponType.Landmines).getShots();
            if (shots <= 0) {
                sendError(seat, mineCoordinates, WRONG_WEAPON, "Wrong weapon", mineCoordinates);
                return;
            }

            double x = getCoords().toX(mineCoordinates.getX(), mineCoordinates.getY());
            double y = getCoords().toY(mineCoordinates.getX(), mineCoordinates.getY());
            getLog().debug("placeMineToMap , x: {}, y: {}", x, y);

            GameMap gameMap = getRoom().getMap();
            double screenX = mineCoordinates.getX();
            double screenY = mineCoordinates.getY();

            GameMapShape mapShape = gameMap.getMapShape();
            boolean notMarked = mapShape.isNotMarked((int) x, (int) y);
            getLog().debug("x:{}, y:{}, notMarked:{}", x, y, notMarked);

            if (!notMarked) {
                Long nearestEnemy = gameMap.getNearestEnemy(new PointD(x, y), false, -1L, null);
                Long anyBossId = getMap().getAnyBossId();
                if (nearestEnemy != null || anyBossId != -1) {
                    Enemy itemById = gameMap.getItemById(nearestEnemy == null ? anyBossId : nearestEnemy);
                    if (itemById != null) {
                        PointD location = itemById.getLocation(System.currentTimeMillis());
                        x = location.x;
                        y = location.y;
                        screenX = getCoords().toScreenX(x, y);
                        screenY = getCoords().toScreenY(x, y);
                        getLog().debug("x,y will be corrected to nearest enemy, x:{}, y:{}, screenX:{}, screenY: {}",
                                x, y, screenX, screenX);
                    } else {
                        sendError(seat, mineCoordinates, WRONG_MINE_COORDINATES, "Wrong coordinates", mineCoordinates);
                        return;
                    }
                } else {
                    sendError(seat, mineCoordinates, WRONG_MINE_COORDINATES, "Wrong coordinates", mineCoordinates);
                    return;
                }
            }

            MinePoint minePoint = new MinePoint(x, y);

            seat.getSeatMines().add(minePoint);
            seat.addMineState(minePoint.getMineId((int) seat.getId()), mineCoordinates.isPaidSpecialShot());

            getLog().debug("For account: {}, add mine to point:{}, allMines: {} ",
                    seat.getAccountId(), minePoint, seat.getSeatMines());

            seat.consumeSpecialWeapon(SpecialWeaponType.Landmines.getId());
            long currentTime = getCurrentTime();
            String mineId = minePoint.getMineId(seat.getNumber());

            IMinePlace minePlaceSeat = getTOFactoryService().createMinePlace(currentTime, mineCoordinates.getRid(),
                    seat.getNumber(), (float) screenX, (float) screenY, mineId);

            IMinePlace minePlaceAll = getTOFactoryService().createMinePlace(currentTime, -1, seat.getNumber(),
                    (float) screenX, (float) screenY, mineId);

            getRoom().sendChanges(minePlaceAll, minePlaceSeat, seat.getAccountId(), null);
        } finally {
            gameRoom.getPlayerInfoService().unlock(seat.getAccountId());
            getLog().debug("placeMineToMap HS unlock: {}", seat.getAccountId());
        }
    }

    @Override
    public void processShot(Seat seat, IShot shot, boolean isInternalShot) throws CommonException {
        long time = System.currentTimeMillis();
        boolean needRestoreWeapon = false;
        int currentWeaponIdOld = seat.getCurrentWeaponId();
        boolean lock = false;
        try {
            lock = lockShots.tryLock(3, TimeUnit.SECONDS);
            if (!lock) {
                sendError(seat, shot, WRONG_WEAPON, "get lock failed", shot);
                return;
            }
            getLog().debug("processShot: aid: {},  shot:{},  isInternalShot: {}, ammo: {}, seat number: {}",
                    seat.getAccountId(),
                    shot, isInternalShot,
                    seat.getAmmoAmount(),
                    seat.getNumber()
            );

            if (shot.getWeaponId() == SpecialWeaponType.Landmines.getId()) {
                if (!isInternalShot) {
                    sendError(seat, shot, WRONG_WEAPON, "Wrong weapon", shot);
                    return;
                }
                if (isOptimalStrategy()) {
                    if (processOptimalForLandMines(shot)) {
                        return;
                    }
                }
                seat.setWeapon(SpecialWeaponType.Landmines.getId());
                needRestoreWeapon = currentWeaponIdOld != SpecialWeaponType.Landmines.getId();
            } else if (shot.getWeaponId() == SpecialWeaponType.Airstrike.getId()) {
                if (!isInternalShot) {
                    sendError(seat, shot, WRONG_WEAPON, "Wrong weapon", shot);
                    return;
                }
                needRestoreWeapon = true;
                seat.setWeapon(shot.getWeaponId());
            }

            seat.setLastWin(Money.ZERO);
            gameRoom.getSeats().forEach(AbstractSeat::resetShotTotalWin);

            boolean isPaidShot = shot.getWeaponId() == -1;
            boolean allowWeaponSaveInAllGames = seat.getPlayerInfo().isAllowWeaponSaveInAllGames();

            PlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
            roundInfo.addRealShotsCounter(seat.getCurrentWeaponId(), 1);
            int numberOfKilledMissOld = roundInfo.getKilledMissedNumber(seat.getCurrentWeaponId());

            if (seat.getAmmoAmountTotalInRound() == 0
                    && seat.getPlayerInfo().getRoundBuyInAmount() == 0 && !allowWeaponSaveInAllGames && !isPaidShot) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets", shot);
            } else if (seat.getAmmoAmount() <= 0 && isPaidShot) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets", shot);
            } else if (shot.getRealWeaponId() != seat.getCurrentWeaponId()) {
                sendError(seat, shot, WRONG_WEAPON, "Wrong weapon", shot);
            } else if (shot.getWeaponId() == REGULAR_WEAPON) {
                shootWithRegularWeapon(time, seat, shot);
                if(roundInfo.isShotSuccess(seat.getCurrentWeaponId(), numberOfKilledMissOld)){
                    roundInfo.addKpiInfoPaidRegularShots(seat.getStake().toCents());
                    roundInfo.addKpiInfoSWShotsCount(-1, 1, seat.getBetLevel(), false);
                }
            } else if (isInternalShot || seat.getCurrentWeapon().getShots() > 0) {
                int weaponId = seat.getCurrentWeaponId();
                if (shot.getWeaponId() == SpecialWeaponType.Cryogun.getId()) {
                    if (!needWaitingWhenEnemiesLeave) {
                        sendFreezeTrajectories(time, shot.getX(), shot.getY(), 280);
                    }
                }
                shootWithSpecialWeapon(time, seat, shot);
                if(roundInfo.isShotSuccess(weaponId, numberOfKilledMissOld)){
                    roundInfo.addKpiInfoFreeShotsCount(1);
                    roundInfo.addKpiInfoSWShotsCount(weaponId, 1, seat.getBetLevel(), true);
                }
            } else {
                sendError(seat, shot, WRONG_WEAPON, "Weapon not found", shot);
            }

            if (!needWaitingWhenEnemiesLeave)
                lastShotTime = System.currentTimeMillis();

            gameRoom.getSeats().forEach(AbstractSeat::updateScoreShotTotalWin);

            getLog().debug("shot, end  getCountRemainingEnemiesByModel: {} , ammo: {}, allowSpawn: {} ",
                    getCountRemainingEnemiesByModel(), seat.getAmmoAmount(), allowSpawn);

        } catch (InterruptedException e) {
            getLog().warn(" shot exception: ", e);
        } finally {
            if (needRestoreWeapon) {
                seat.setWeapon(currentWeaponIdOld);
            }
            if (lock) {
                lockShots.unlock();
            }
        }
    }

    private boolean processOptimalForLandMines(IShot shot) {
        GameConfig gameConfig = gameRoom.getGame().getCurrentGameConfig(gameRoom.getGameConfigService());
        Map<String, Map<Double, Double>> damage_probability = new ConcurrentHashMap<>();
        Enemy itemById = getMap().getItemById(shot.getEnemyId());
        double energy = itemById.getEnergy();
        if (!itemById.isBoss()) {
            IMathEnemy mathEnemy = itemById.getMathEnemy();
            EnemyParams enemyParams = gameConfig.getEnemies().get(mathEnemy.getTypeName()).get(mathEnemy.getSettingsEnemyId());
            damage_probability = enemyParams.getDamage_probability();
        } else {
            EnemyBoss enemyBoss = (EnemyBoss) itemById;
            BossPartEnemy bossPartEnemy = null;
            if (!enemyBoss.getHeadEnemies().isEmpty()) {
                bossPartEnemy = enemyBoss.getHeadEnemies().get(0);
            } else if (!enemyBoss.getTailEnemies().isEmpty()) {
                bossPartEnemy = enemyBoss.getTailEnemies().get(0);
            }
            if (bossPartEnemy != null) {
                damage_probability = bossPartEnemy.getEnemyParams().getDamage_probability();
                energy = bossPartEnemy.getCurrentHealth();
            }
        }
        Map<String, Map<Double, Integer>> wParams = gameConfig.getWeapons().get(SpecialWeaponType.Landmines.getMathTitle());
        Map<Double, Integer> strength = wParams.get("strength");

        double maxDamage;
        double maxDamageMultiplier = 0;
        for (Map.Entry<Double, Integer> doubleIntegerEntry : strength.entrySet()) {
            maxDamageMultiplier += doubleIntegerEntry.getKey() * doubleIntegerEntry.getValue();
        }

        Map<Double, Double> prob_ = damage_probability.get(SpecialWeaponType.Landmines.getMathTitle());
        maxDamage = maxDamageMultiplier * prob_.keySet().stream().max(Double::compareTo).get();

        if (maxDamage < energy) {
            return true;
        }
        return false;
    }

    @Override
    protected void shootWithRegularWeapon(long time, Seat seat, IShot shot) throws CommonException {
        ShotMessages messages = new ShotMessages(seat, shot, gameRoom,
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0),
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0));
        ShootResult result = shootWithRegularWeaponAndUpdateState(time, seat, shot.getEnemyId(), messages);
        processSingleShotResult(seat, shot, messages, result);
        seat.incrementBulletsFired();
    }

    private void shootWithCombinedWeapon(long time, Seat seat, IShot shot) throws CommonException {
        ShotMessages messages = new ShotMessages(seat, shot, gameRoom,
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0),
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0));

        getLog().debug("processing shot from combined weapon: " + shot.getWeaponId() +
                " seat.getSpecialWeaponId(): " + seat.getSpecialWeaponId());
        ShootResult result = shootWithRegularWeaponAndUpdateState(time, seat, shot.getEnemyId(), messages);
        if (!result.isKilledMiss()) {
            seat.consumeSpecialWeapon(shot.getWeaponId());
        }
        processSingleShotResult(seat, shot, messages, result);
    }

    private void processSingleShotResult(Seat seat, IShot shot, ShotMessages messages, ShootResult result) throws CommonException {
        int awardedWeaponId = !result.isNewWeapon() ? -1 : result.getWeapon().getType().getId();
        if (!result.isKilledMiss()) {
            seat.decrementAmmoAmount();
        }
        processShootResult(seat, shot, result, messages, awardedWeaponId, true);
        List<Seat> seats = gameRoom.getSeats();
        for (Seat seatCurrent : seats) {
            seatCurrent.transferWinToAmmo();
        }

        if ((result.isShotToBoss() && result.isDestroyed())) {
            subround = PlaySubround.BASE;
        }
        messages.send(seat.getSpecialWeaponRemaining(), shot);
    }


    protected ShootResult shootWithRegularWeaponAndUpdateState(long time, Seat seat, Long itemIdForShot, ShotMessages messages)
            throws CommonException {
        ShootResult result = shootToOneEnemy(time, seat, itemIdForShot, seat.getCurrentWeaponId(), false, 1);

        getLog().debug("shootResult: {}", result);

        if (result.isNeedGenerateHVEnemy() && allowSpawnHW) {
            generateHVEnemy(result, messages, seat.getPlayerInfo().getSessionId());
        }
        getLog().debug("allowSpawnHW: {}, shootResult: {}", allowSpawnHW, result);
        return result;
    }


    @Override
    protected List<ShootResult> shootWithSpecialWeaponAndUpdateState(long time, Seat seat, IShot shot, int weaponId,
                                                                     ShotMessages messages) throws CommonException {

        List<ShootResult> results = new LinkedList<>();
        GameMap map = getMap();
        Long itemIdForShot = shot.getEnemyId();

        boolean isFreeShot = weaponId == SpecialWeaponType.Airstrike.getId();

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
            locationOfBaseEnemy = enemy.getLocation(time);
            int enemyTypeId = enemy.getEnemyClass().getEnemyType().getId();
            getLog().debug("Base enemy: " + enemy + " enemyTypeId: " + enemyTypeId);
        } else {
            // killed earlier
            getLog().debug("Base enemy: was killed before");
            return Collections.singletonList(new ShootResult(seat.getStake(),
                    Money.INVALID, false, false, null));
        }

        if (liveEnemies > numberDamages) liveEnemies = (int) numberDamages;

        Map<Long, Double> nNearestEnemies = map.getNNearestEnemies(time, locationOfBaseEnemy, itemIdForShot, liveEnemies);
        getLog().debug("nNearestEnemies: " + nNearestEnemies + ", weaponId: " + weaponId);

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
            results.add(res);
            if (!res.getWin().equals(Money.INVALID))
                realNumberOfShots++;
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
            getLog().debug("before compensation " + "realNumberOfShots: {}, numberDamages: {}",
                    realNumberOfShots, numberDamages);
            makeCompensationForPoorPlaying(seat, weaponId, numberDamages, realNumberOfShots);
        }


        Optional<ShootResult> first = results.stream().filter(shootResult ->
                shootResult.getEnemy() != null && (shootResult.getEnemy().getId() == itemIdForShot)).findFirst();

        if (!first.isPresent()) {
            return results;
        } else {
            ShootResult mainShootResult = first.get();
            if (weaponId != SpecialWeaponType.Landmines.getId() && !isFreeShot)
                seat.consumeSpecialWeapon(weaponId);

            getLog().debug("shootResult: ");
            for (ShootResult shootResult : results) {
                getLog().debug(shootResult);
            }

            getLog().debug("allowSpawnHW: " + allowSpawnHW);
            if (mainShootResult.isNeedGenerateHVEnemy() && allowSpawnHW) {
                generateHVEnemy(mainShootResult, messages, seat.getPlayerInfo().getSessionId());
            }
        }

        return results;
    }

    private void makeCompensationForPoorPlaying(Seat seat, int weaponId, double numberDamages, int realNumberOfShots) {
        if (realNumberOfShots < numberDamages) {
            getLog().debug("realNumberOfShots:{} less then numberDamages: {}, need compensate:", realNumberOfShots, numberDamages);
            List<IWeaponSurplus> weaponSurplus = seat.getWeaponSurplus();
            getLog().debug("weaponSurplus before:{} ", weaponSurplus);
            int lostHits = (int) (numberDamages - realNumberOfShots);

            Double rtpForWeapon = MathData.getRtpForWeapon(weaponId) / 100;

            Money newCompensation = Money.ZERO;
            newCompensation = newCompensation.add(seat.getStake().multiply(rtpForWeapon * lostHits));

            seat.getCurrentPlayerRoundInfo().addCompensateHitsCounter(weaponId, lostHits);
            getLog().debug("lostHits :{}, newCompensation: {} ", lostHits, newCompensation);

            boolean weaponsWasFound = false;
            if (weaponSurplus.size() > 0) {
                for (IWeaponSurplus surplus : weaponSurplus) {
                    if (surplus.getId() == weaponId) {
                        int shotsOld = surplus.getShots();
                        long oldCompensation = surplus.getWinBonus();
                        surplus.setShots(shotsOld);
                        surplus.setWinBonus(oldCompensation + newCompensation.toCents());
                        weaponsWasFound = true;
                    }
                }
            }
            if (!weaponsWasFound) {
                weaponSurplus.add(getTOFactoryService().createWeaponSurplus(weaponId, 0, newCompensation.toCents()));
            }
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

        int newShots = 0;
        if (result.getWeapon() != null) {
            newShots = result.getWeapon().getShots();
        }

        boolean isSpecialWeapon = shot.getWeaponId() != -1;
        boolean isPrize = !result.getPrize().isEmpty();
        boolean isWin = result.getWin().greaterThan(Money.ZERO);
        boolean isWeapon = awardedWeaponId != -1 || result.getNewFreeShotsCount() > 0;
         boolean isBossWin = result.isShotToBoss()
                && (result.getWin().greaterThan(Money.ZERO) ||
                (result.getAdditionalWins() != null && !result.getAdditionalWins().isEmpty()));
        boolean isAwardedWeapons = !result.getAwardedWeapons().isEmpty();

        boolean isHit = isPrize || isWin || isWeapon || isBossWin || isAwardedWeapons;
        getLog().debug("isHit : {}, isPrize: {}, isWin: {}, isWeapon: {}, isBossWin: {}, isAwardedWeapons: {}",
                isHit, isPrize, isWin, isWeapon, isBossWin, isAwardedWeapons);

        Map<Integer, List<IWinPrize>> hitResultBySeats = new HashMap<>();
        Map<Seat, IHit> messagesForSeatsLocal = new HashMap<>();
        IHit hitOwn = null;
        List<IHit> hitsForObserversLocal = new ArrayList<>();


        if (result.isKilledMiss()) {
            seat.incrementMissCount();
            seat.getCurrentPlayerRoundInfo().addKilledMissCounter(shot.getWeaponId(), 1);
            messages.add(getTOFactoryService().createMiss(getCurrentTime(), TObject.SERVER_RID, seat.getNumber(), result.isKilledMiss(), awardedWeaponId,
                    enemyId, usedSpecialWeapon, seat.getSpecialWeaponRemaining(), diffScore, isLastResult, shot.getX(), shot.getY(),
                    newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable()),
                    getTOFactoryService().createMiss(getCurrentTime(), shot.getRid(), seat.getNumber(), result.isKilledMiss(), awardedWeaponId,
                            enemyId, usedSpecialWeapon, seat.getSpecialWeaponRemaining(), diffScore, isLastResult, shot.getX(), shot.getY(),
                            newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable()));

        } else {
            String name = result.getEnemy().getEnemyClass().getName();
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
                        List<Pair<Integer, Money>> realAdditionalWin = new ArrayList<>();

                        Money enemyWinForSeat = Money.ZERO;

                        if (isOwner) {
                            enemyWinForSeat = enemyWinForSeat.add(commonWin);
                            if (isBossWin && result.isDestroyed()) {
                                for (Pair<Integer, Money> additionalWin_ : additionalWins) {
                                    enemyWinForSeat = enemyWinForSeat.add(additionalWin_.getValue());
                                }
                            }
                            getLog().debug("enemy shared Win: seat accountId: " + seatCurrent.getAccountId()
                                    + " enemyWinForSeat: " + enemyWinForSeat
                                    + " seatId: " + seatCurrent.getId()
                                    + " additionalWins: " + additionalWins
                                    + " realAdditionalWin: " + realAdditionalWin
                            );
                            seatCurrent.incrementRoundWin(enemyWinForSeat);
                            seatCurrent.incrementShotTotalWin(enemyWinForSeat);
                            seatCurrent.addLastWin(enemyWinForSeat);
                            totalSimpleWin = totalSimpleWin.add(enemyWinForSeat);

                            PlayerRoundInfo currentPlayerRoundInfo = seatCurrent.getCurrentPlayerRoundInfo();

                            if (killAwardWin.greaterThan(Money.ZERO)) {
                                getLog().debug("  KillAwardWin: {}", killAwardWin);
                                currentPlayerRoundInfo.updateAdditionalWin("KillAwardWin", killAwardWin);
                                seatCurrent.incrementRoundWin(killAwardWin);
                                seatCurrent.incrementShotTotalWin(killAwardWin);
                                seatCurrent.addLastWin(killAwardWin);
                                totalSimpleWin = totalSimpleWin.add(killAwardWin);
                            }


                        }


                        if (result.isDestroyed())
                            seatCurrent.removeDamageForEnemyId(enemyId);

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
                                    hitResultBySeats, -1,
                                    result.getAdditionalWins(), result.getAwardedWeapons());

                            result.setWin(win);

                            hitOwn = getTOFactoryService().createHit(getCurrentTime(), shot.getRid(), seat.getNumber(),
                                    result.getDamage(), result.getWin().toDoubleCents(), realAwardedWeaponId,
                                    usedSpecialWeapon, remainingSWShots, diffScore, enemy, isLastResult,
                                    seatCurrent.getRoundWin().toDoubleCents(), result.getHvEnemyId(), shot.getX(),
                                    shot.getY(), realNewShots, result.isDestroyed(), result.getMineId(),
                                    result.getNewFreeShotsCount(), seat.getNumber(),
                                    result.isInstanceKill(), result.getChMult(), enemyId, shot.getEnemyId());

                            hitOwn.setAwardedWeapons(result.getAwardedWeapons());
                            hitOwn.setKillBonusPay(killAwardWin.toDoubleCents());

                            getLog().debug("isSpecialWeapon {}, isLastResult: {}, stake: {}, title: {}", isSpecialWeapon,
                                    isLastResult, seat.getStake(), title);

                            Money stake = isSpecialWeapon ? Money.ZERO : seat.getStake();
                            seat.getCurrentPlayerRoundInfo().updateStat(stake, result.isShotToBoss(), Money.ZERO, Money.ZERO, isSpecialWeapon,
                                    title, enemyWinForSeat, result.isDestroyed(), name, Money.ZERO);
                        } else {
                            updateHitResultBySeats(seatCurrent.getNumber(), enemyWinForSeat,
                                    "", hitResultBySeats, -1, new ArrayList<>(), null);

                            IHit hit = getTOFactoryService().createHit(getCurrentTime(), SERVER_RID, seat.getNumber(), result.getDamage(),
                                    enemyWinForSeat.toDoubleCents(),
                                    realAwardedWeaponId, usedSpecialWeapon, remainingSWShots, diffScore, enemy, isLastResult, 0,
                                    result.getHvEnemyId(), shot.getX(), shot.getY(), realNewShots, result.isDestroyed(),
                                    result.getMineId(), result.getNewFreeShotsCount(), seat.getNumber(),
                                    result.isInstanceKill(), result.getChMult(), enemyId, shot.getEnemyId());

                            hit.setKillBonusPay(killAwardWin.toDoubleCents());

                            messagesForSeatsLocal.put(seatCurrent, hit);
                            seatCurrent.getCurrentPlayerRoundInfo().updateStat(Money.ZERO, result.isShotToBoss(), Money.ZERO, Money.ZERO, false,
                                    null, enemyWinForSeat, false, name, Money.ZERO);

                        }
                    }
                }

                updateHitResultBySeats(seat.getNumber(), Money.ZERO, null, hitResultBySeats,
                        awardedWeaponId, new ArrayList<>(), null);

                if (!result.getEnemy().isFake() && !seat.getSocketClient().isBot()
                        && !result.getPrize().isEmpty()) {
                    updateHitResultBySeats(seat.getNumber(), Money.ZERO, result.getPrize(),
                            hitResultBySeats, -1, new ArrayList<>(), null);
                    processQuests(seat, result);
                }

                // hit for observers
                IHit hitForObservers = getTOFactoryService().createHit(getCurrentTime(), SERVER_RID, seat.getNumber(), result.getDamage(), 0,
                        awardedWeaponId, usedSpecialWeapon, seat.getSpecialWeaponRemaining(),
                        diffScore, enemy, isLastResult, 0,
                        result.getHvEnemyId(), shot.getX(), shot.getY(), newShots, result.isDestroyed(),
                        result.getMineId(), result.getNewFreeShotsCount(), seat.getNumber(),
                        result.isInstanceKill(), result.getChMult(), enemyId, shot.getEnemyId());


                hitForObservers.setKillBonusPay(killAwardWin.toDoubleCents());

                hitsForObserversLocal.add(hitForObservers);

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
                Money stake = isSpecialWeapon ? Money.ZERO : seat.getStake();
                String title = isSpecialWeapon ? SpecialWeaponType.values()[realShotTypeId].getTitle() : null;
                seat.getCurrentPlayerRoundInfo().updateStat(stake, result.isShotToBoss(), Money.ZERO, Money.ZERO, isSpecialWeapon,
                        title, Money.ZERO, result.isDestroyed(), name, Money.ZERO);

                seat.incrementMissCount();
                int specialWeaponRemaining = seat.getSpecialWeaponRemaining();
                messages.add(getTOFactoryService().createMiss(getCurrentTime(), TObject.SERVER_RID, seat.getNumber(),
                        false, awardedWeaponId, enemyId, usedSpecialWeapon, specialWeaponRemaining, diffScore,
                        isLastResult, shot.getX(), shot.getY(), newShots, result.getMineId(), shot.getEnemyId(),
                        false),
                        getTOFactoryService().createMiss(getCurrentTime(), shot.getRid(), seat.getNumber(), false,
                                awardedWeaponId, enemyId, usedSpecialWeapon, specialWeaponRemaining, diffScore,
                                isLastResult, shot.getX(), shot.getY(), newShots, result.getMineId(),
                                shot.getEnemyId(), false));
            }
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

    protected void processQuests(Seat seat, IShootResult result) {
        IPlayerQuests playerQuests = seat.getPlayerInfo().getPlayerQuests();
        getLog().debug("old player quests: {}", playerQuests.getQuests());

        String[] keys = result.getPrize().split("\\|");
        for (String key : keys) {
            processingQuestKey(Treasure.valueOf(key).getId(), seat, result.getEnemyId());
        }
    }

    private void processingQuestKey(int prizeId, Seat seat, long enemyId) {
        Set<IQuest> quests = seat.getPlayerInfo().getPlayerQuests().getQuests();
        List<IQuest> questsWithPrize = quests.stream().filter(quest -> {
            List<ITreasureProgress> treasures = quest.getProgress().getTreasures();
            long count = treasures.stream().filter(treasureProgress ->
                    (treasureProgress.getTreasureId() == prizeId
                            && treasureProgress.getCollect() < treasureProgress.getGoal())).count();
            return count > 0;
        }).collect(Collectors.toList());

        getLog().debug("prizeId: " + prizeId + ", questsWithPrize: " + questsWithPrize);

        if (questsWithPrize.size() == 0) {
            return;
        }

        if (questsWithPrize.size() > 1) {
            getLog().warn("questsWithPrize has prizes more 1 : {}", questsWithPrize.size());
        }
        Treasure treasure = (Treasure) questManager.getTreasureById(prizeId);
        if (treasure != null) {
            Map<Treasure, Integer> roundTreasures = seat.getRoundTreasures();
            roundTreasures.merge(treasure, 1, Integer::sum);
            seat.setTotalTreasuresCount(seat.getTotalTreasuresCount() + 1);
            IPlayerStats roundStats = seat.getPlayerInfo().getRoundStats();
            Map<Integer, Long> roundStatsTreasures = roundStats.getTreasures();
            roundStatsTreasures.put(0, roundStatsTreasures.isEmpty() ? 1 : roundStatsTreasures.get(0) + 1);
        } else {
            getLog().warn("Unknown treasure with id={}", prizeId);
        }


        IQuest quest = questsWithPrize.get(0);
        getLog().debug(" quest: " + quest);

        List<ITreasureProgress> treasures = quest.getProgress().getTreasures();
        ITreasureProgress progress = treasures.stream().filter(
                treasureProgress -> treasureProgress.getTreasureId() == prizeId).findFirst().get();
        progress.setCollect(progress.getCollect() + 1);

        long countUnfinished = treasures.stream().filter(treasureProgress ->
                treasureProgress.getCollect() < treasureProgress.getGoal()).count();

        boolean needFinish = countUnfinished == 0;
        long winInCents = 0;
        int awardedWeaponId = -1;

        if (needFinish) {
            getLog().debug("need finish quest : {}", quest.getName());

            int amount = quest.getQuestPrize().getAmount().getFrom();
            Money win = seat.getStake().multiply(amount);
            getLog().debug(" pay : " + amount + " win: " + win);

            IPlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
            seat.setQuestsPayouts(seat.getQuestsPayouts() + win.toCents());
            getLog().debug("quest win add to roundWin: {}", win);
            seat.incrementRoundWin(win);

            winInCents = win.toCents();

            Pair<Integer, Integer> weaponForKey = MathQuestData.getRandomWeapon(prizeId);
            int specialWeaponId = weaponForKey.getKey();
            awardedWeaponId = specialWeaponId;

            int shots = weaponForKey.getValue();
            getLog().debug("weaponForKey: {}", weaponForKey);

            SpecialWeaponType value = SpecialWeaponType.values()[specialWeaponId];
            com.betsoft.casino.mp.common.Weapon weapon = new com.betsoft.casino.mp.common.Weapon(shots, value);
            if (!value.equals(Airstrike)) {
                seat.addWeapon(weapon);
                getLog().debug("quest weapons add: {}", weapon);
            } else {
                seat.getFreeShots().addToTempQueue(Airstrike.getId(), weapon.getShots());
            }

            currentPlayerRoundInfo.updateQuestCompletedTotalData(win, specialWeaponId, shots);
            quest.setCollectedAmount(shots);
            quest.setNeedReset(true);
            quest.getQuestPrize().setSpecialWeaponId(specialWeaponId);
        }

        seat.sendMessage(getTOFactoryService().createUpdateQuest(System.currentTimeMillis(), quest, enemyId));

        seat.sendMessage(getTOFactoryService().createNewTreasure(System.currentTimeMillis(), -1, prizeId,
                enemyId, needFinish ? (int) quest.getId() : -1, quest.getId()));

        if (needFinish) {
            seat.setQuestsCompletedCount(seat.getQuestsCompletedCount() + 1);
            quest.setNeedReset(false);
            quest.setCollectedAmount(0);
            quest.getProgress().resetProgress();
            quest.getQuestPrize().setSpecialWeaponId(-1);
            getLog().debug("quest is reset {}", quest);
            gameRoom.sendChanges(getTOFactoryService().createSeatWinForQuest(System.currentTimeMillis(), SERVER_RID,
                    seat.getNumber(), enemyId, winInCents, awardedWeaponId));

        }

        getLog().debug("quests after processQuests: {}", quests);

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
                    winPrizes.add(getTOFactoryService().createWinPrize(1, key));
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
            seat.getCurrentPlayerRoundInfo().
                    addWeaponSourceStat(WeaponSource.ENEMY.getTitle(),
                            SpecialWeaponType.values()[newWeapon.getType().getId()].getTitle(), weapon.getShots());
            getLog().debug("add weapons to seat aid {}, newWeapon  {} ", seat.getAccountId(), newWeapon);
        });
        getLog().debug("add weapons to seat aid {}, weapons after: {} ", seat.getAccountId(), seat.getWeapons());
    }


    @Override
    public ShootResult shootToOneEnemy(long time, Seat seat, Long itemIdForShot, int weaponId, boolean isNearLandMine,
                                       double damageMultiplier) throws CommonException {

        GameMap map = getMap();
        boolean currentBaseRound = subround.equals(PlaySubround.BASE);

        boolean isShotWithCombinedWeapon = weaponId == DoubleStrengthPowerUp.getId();
        boolean isShotWithSpecialWeapon = weaponId != REGULAR_WEAPON && !isShotWithCombinedWeapon;

        Enemy enemy = map.getItemById(itemIdForShot);
        Money stake = seat.getStake();
        // killed earlier
        if (enemy == null) {
            return new ShootResult(isShotWithSpecialWeapon ? Money.ZERO : stake, Money.INVALID, false, false, null);
        }

        boolean isBot = seat.getSocketClient().isBot();
        EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
        getLog().debug("shootToOneEnemy PlayerId: " + seat.getPlayerInfo().getId() + ", isBot:  "
                + isBot + " itemIdForShot for shot: " + itemIdForShot + " enemyType: " + enemyType.name()
                + " weaponId: " + weaponId);

        ShootResult shootResult = gameRoom.getGame().doShoot(enemy, seat, stake, subround.equals(PlaySubround.BOSS),
                isNearLandMine, damageMultiplier, getTOFactoryService());

        getLog().debug("shootToOneEnemy, seat.getCurrentWeaponId(): {}", seat.getCurrentWeaponId());
        getLog().debug("shootToOneEnemy, shootResult: {}", shootResult);

        if (!isBot && shootResult.isBossShouldBeAppeared() && !isBossRound() && allowSpawn
                && totalCountMainBossAppeared == 0) {
            totalCountMainBossAppeared++;
            remainingNumberOfBoss++;
            getLog().debug("shootToOneEnemy, Boss will be appeared later, remainingNumberOfBoss:  " +
                    remainingNumberOfBoss + " totalCountMainBossAppeared: " + totalCountMainBossAppeared);
        }

        if (shootResult.isDestroyed()) {
            getLog().debug("shootToOneEnemy, enemy " + shootResult.getEnemyId() + " is killed");
            map.removeItem(shootResult.getEnemyId());
            getLeaveAndDestroyEnemiesTime().put(enemyType.getId(), time);
            getLog().debug("shootToOneEnemy, count of enemies after: {}", map.getItemsSize());
            getLog().debug("shootToOneEnemy, getCountRemainingEnemiesByModel: {}", getCountRemainingEnemiesByModel());
            getLog().debug("shootToOneEnemy, leaveAndDestroyEnemiesTime: {}", leaveAndDestroyEnemiesTime);
        }

        shootResult.setWeaponSurpluses(seat.getWeaponSurplus());
        return shootResult;
    }

    @Override
    public void restoreGameRoom(GameRoom gameRoom) throws CommonException {
        super.restoreGameRoom(gameRoom);
        questManager = new QuestManager(getTOFactoryService());
        lockShots = new ReentrantLock();
        limitChecker = new LimitChecker();
        notSafeHatchTrajectories = new ArrayList<>();
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

    @Override
    protected PlayGameState getDeserializer() {
        return this;
    }
}
