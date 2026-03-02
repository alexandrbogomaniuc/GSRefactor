package com.betsoft.casino.mp.revengeofra.model;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.revengeofra.model.math.*;
import com.betsoft.casino.mp.utils.ErrorCodes;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.Triple;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.kynosarges.tektosyne.geometry.PointD;
import reactor.core.Disposable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.model.EnemyDestroyReason.SIMPLE_SHOT;
import static com.betsoft.casino.mp.model.SpecialWeaponType.Landmines;
import static com.betsoft.casino.mp.revengeofra.model.math.EnemyRange.*;
import static com.betsoft.casino.mp.revengeofra.model.math.SwarmType.*;
import static com.betsoft.casino.mp.utils.ErrorCodes.*;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

@SuppressWarnings("Duplicates")
public class PlayGameState extends AbstractActionPlayGameState<GameRoom, Seat, GameMap, PlayGameState> {
    private static int MAX_ALIVE_ENEMIES = 50;
    private static int MAX_ALIVE_RATS = 30;
    private static int MAX_ALIVE_CRABS = 40;
    private static int MAX_ALIVE_PIRATES = 4;
    private static int MAX_JUMP_ENEMIES = 2;
    private static final int MAX_SINGLE_MUMMIES = 2;
    protected transient long lastTimeOfGenerationBird;
    private transient boolean needImmediatelySpawn = false;
    static final transient Coords coords = new Coords(960, 540, 96, 96);
    private transient Map<Integer, Long> leaveAndDestroyEnemiesTime = new HashMap<>();
    private transient List<Integer> remainingBosses;

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

    public void setMaxAliveCrabsAndRats(int maxAliveCrabsAndRats) {
        getLog().debug("MAX_ALIVE_CRABS_AND_RATS: {}", maxAliveCrabsAndRats);
        MAX_ALIVE_CRABS = 50;
        MAX_ALIVE_RATS = 50;
    }

    public void setMaxAlivePirates(int maxAlivePirates) {
        getLog().debug("MAX_ALIVE_PIRATES: {}", MAX_ALIVE_PIRATES);
        MAX_ALIVE_PIRATES = maxAlivePirates;
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
    }

    @Override
    protected void updateWithLock() throws CommonException {
        GameMap map = getMap();

        if (allowSpawn && isManualGenerationEnemies())
            return;

        List<Integer> removedEnemies = map.updateAndReturnListTypes();
        removedEnemies.forEach(typeId -> getLeaveAndDestroyEnemiesTime().put(typeId, System.currentTimeMillis()));

        if (!needWaitingWhenEnemiesLeave) {
            sendUpdateTrajectories(false);
        }

        checkTestStandFeatures();

        try {
            calculateMinesOnMap();
        } catch (Exception e) {
            getLog().debug("calculateMinesOnMap error: ", e);
        }

        getMap().checkFreezeTimeEnemies(FREEZE_TIME_MAX);

        if (map.getItemsSize() >= MAX_ALIVE_ENEMIES) {
            return;
        }

        HashSet<Integer> liveIdScenariosCrabs = new HashSet<>();

        int aliveScarabs = 0;
        int aliveBombEnemy = 0;
        int aliveWeaponCarriers = 0;
        int aliveJumpEnemies = 0;
        int aliveLowMummies = 0;
        int aliveLowMummiesInSwarmLarge = 0;
        int aliveLowMummiesInSwarmSmall = 0;
        int alivePharaohMummies = 0;
        int aliveLocustSmall = 0;
        int aliveLocustLarge = 0;
        int aliveScorpions = 0;
        int aliveHorus = 0;
        int aliveBrawlerBerserk = 0;

        List<Triple<Integer, Integer, Integer>> itemsTypeIdsAndSwarmState = map.getItemsTypeIdsAndSwarmTypeAndIds();

        if (isNeedMinimalEnemies() && itemsTypeIdsAndSwarmState.size() > 3)
            return;

        for (Triple<Integer, Integer, Integer> triple : itemsTypeIdsAndSwarmState) {
            EnemyType enemyType = EnemyType.getById(triple.first());
            if (EnemyRange.Scarabs.getEnemies().contains(enemyType)) {
                aliveScarabs++;
                liveIdScenariosCrabs.add(triple.third());
            } else if (LOW_MUMMIES.getEnemies().contains(enemyType)) {
                Integer swarmTypeId = triple.second();
                if (swarmTypeId != -1) {
                    if (swarmTypeId == SMALL_GROUP_MUMMIES.getTypeId()) {
                        aliveLowMummiesInSwarmSmall++;
                    } else {
                        aliveLowMummiesInSwarmLarge++;
                    }
                } else {
                    aliveLowMummies++;
                }
            } else if (PHARAOH_MUMMIES.getEnemies().contains(enemyType)) {
                alivePharaohMummies++;
            } else if (EnemyType.ENEMY_6.equals(enemyType)) {
                aliveLocustLarge++;
            } else if (EnemyType.ENEMY_17.equals(enemyType)) {
                aliveLocustSmall++;
            } else if (JUMP_ENEMIES.getEnemies().contains(enemyType)) {
                aliveJumpEnemies++;
            } else if (EnemyType.WEAPON_CARRIER.equals(enemyType)) {
                aliveWeaponCarriers++;
            } else if (EnemyType.ENEMY_14.equals(enemyType)) {
                aliveBombEnemy++;
            } else if (EnemyType.ENEMY_7.equals(enemyType)) {
                aliveScorpions++;
            } else if (EnemyType.ENEMY_16.equals(enemyType)) {
                aliveHorus++;
            } else if (EnemyType.ENEMY_18.equals(enemyType)) {
                aliveBrawlerBerserk++;
            }

        }

        if (subround.equals(PlaySubround.BASE)) {
            if (!needWaitingWhenEnemiesLeave && allowSpawn) {
                if ((RNG.nextInt(100) < 3 || isNeedImmediatelySpawn())) {
                    checkForGenerationStandaloneHV(EnemyRange.WEAPON_CARRIER, aliveWeaponCarriers);
                }

                if ((RNG.nextInt(100) < 3 || isNeedImmediatelySpawn())) {
                    checkForGenerationStandaloneHV(Brawler_Berserk, aliveBrawlerBerserk);
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
            if (aliveScarabs < getMaxAliveCrabs() && RNG.nextInt(5) == 0) {
                if (RNG.nextInt(10) == 0) {
                    spawnScarabSwarmByScenario(liveIdScenariosCrabs, getMaxAliveCrabs() - aliveScarabs);
                } else {
                    spawnScarabSwarmByParams(liveIdScenariosCrabs, getMaxAliveCrabs() - aliveScarabs);
                }
            }

            if (aliveLowMummiesInSwarmSmall == 0 && RNG.nextInt(50) == 0) {
                spawnEnemyWithClones(LOW_MUMMIES, SMALL_GROUP_MUMMIES);
            }

            if (aliveLowMummiesInSwarmLarge == 0 && RNG.nextInt(50) == 0) {
                spawnEnemyWithClones(LOW_MUMMIES, DUAL_SPEED_MUMMIES);
            }

            if (aliveLowMummies < MAX_SINGLE_MUMMIES && RNG.nextInt(50) == 0) {
                spawnEnemy(LOW_MUMMIES, -1, null, -1);
            }

            if (alivePharaohMummies == 0 && RNG.nextInt(40) == 0) {
                spawnEnemyWithClones(PHARAOH_MUMMIES, PHARAON_MUMMIES);
            }

            if (aliveJumpEnemies < MAX_JUMP_ENEMIES || isNeedImmediatelySpawn()) {
                if (RNG.nextInt(100) < 15) {
                    spawnEnemy(JUMP_ENEMIES, -1, null, -1);
                }
            }

            if (aliveScorpions == 0 && RNG.nextInt(40) == 0) {
                spawnEnemy(SCORPION, -1, null, -1);
            }

            if (aliveBombEnemy < 1 || isNeedImmediatelySpawn()) {
                if (RNG.nextInt(100) < 15 && enemyCanBeGenerated(BOMB_ENEMY, 15000)) {
                    spawnEnemy(BOMB_ENEMY, -1, null, -1);
                }
            }


            if (aliveHorus < 1 || isNeedImmediatelySpawn()) {
                if (RNG.nextInt(120) == 0) {
                    spawnEnemy(HORUS, -1, null, -1);
                }
            }


            boolean realCase = (aliveLocustSmall < 1 && aliveLocustLarge < 1) && System.currentTimeMillis() - lastTimeOfGenerationBird > 3000;
            if (realCase || isNeedImmediatelySpawn()) {
                spawnWasp(RNG.nextBoolean() ? EnemyType.ENEMY_6 : EnemyType.ENEMY_17);
                lastTimeOfGenerationBird = System.currentTimeMillis();
            }

        } else if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            nextSubRound();
        }
    }

    private void spawnWasp(EnemyType enemy) {
        if (!isNeedMinimalEnemies()) {
            List<Enemy> swarm = getMap().spawnWaspSwarm(enemy);
            gameRoom.sendNewEnemiesMessage(swarm);
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

    private boolean checkForGenerationStandaloneHV(EnemyRange range, int numberLiveEnemies) {
        int skinId = 1;
        if (numberLiveEnemies >= 1) {
            return false;
        }

        boolean canGenerateEnemyFromRange = enemyCanBeGenerated(range, 10000);
        if (!canGenerateEnemyFromRange) {
            return false;
        }

        long spawnEnemyId = spawnEnemy(range, skinId, null, -1);
        if (spawnEnemyId != -1) {
            getLog().debug("checkForGenerationStandaloneHV " + " skinId: " + skinId + " enemyRange: "
                    + " spawnEnemyId: " + spawnEnemyId);
        }
        return true;
    }

    private boolean enemyCanBeGenerated(EnemyRange range, long time) {
        AtomicBoolean canGenerateEnemyFromRange = new AtomicBoolean(true);
        long currentTime = System.currentTimeMillis();
        range.getEnemies().forEach(enemyType -> {
            Long lastTime = getLeaveAndDestroyEnemiesTime().get(enemyType.getId());
            if (lastTime != null && (currentTime - lastTime) < time) {
                canGenerateEnemyFromRange.set(false);
            }
        });
        return canGenerateEnemyFromRange.get();
    }

    public void calculateMinesOnMap() throws CommonException {
        calculateMinesOnMap(false);
    }

    public void calculateMinesOnMap(boolean anyDistance) throws CommonException {
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
            if ((!seat.isDisconnected() || anyDistance) && !seatMines.isEmpty()) {
                for (MinePoint seatMine : seatMines) {
                    if (seatMine == null)
                        continue;
                    PointD point = new PointD(seatMine.getX(), seatMine.getY());
                    Long nearestEnemyFotMine = map.getNearestEnemyForMine(point, customDistances);

                    if (nearestEnemyFotMine == -1 && anyDistance) {
                        long time = System.currentTimeMillis();
                        nearestEnemyFotMine = map.getAllNearestEnemy(time, point, false,
                                -1L, null);
                    }
                    if (nearestEnemyFotMine != -1) {
                        try {
                            String mineId = seatMine.getMineId((int) seat.getId());
                            Boolean isPaidSpecialShot = seat.getMineStates().get(mineId);
                            gameRoom.processShot(seat, getTOFactoryService().createShot(seatMine.getTimePlace(), -1,
                                    SpecialWeaponType.Landmines.getId(),
                                    nearestEnemyFotMine, 0, 0, isPaidSpecialShot), true);
                            mineExploded = true;
                        } catch (Exception e) {
                            getLog().debug("calculateMinesOnMap, error processing processShot for account: {}",
                                    seat.getAccountId(), e);
                        }
                    }
                }
                if (mineExploded) {
                    getLog().debug("calculateMinesOnMap account: {}, seatMinesNew: {}",
                            seat.getAccountId(), seat.getSeatMines());
                    getLog().debug("calculateMinesOnMap seat mine weapon: {}",
                            seat.getWeapons().get(SpecialWeaponType.Landmines));
                }
            }
        }
    }

    private void spawnScarabSwarmByScenario(HashSet<Integer> liveIdScenariosCrabs, int max) {
        gameRoom.sendNewEnemiesMessage(gameRoom.getMap().addSwarmByScenario(liveIdScenariosCrabs, max));
    }

    private void spawnScarabSwarmByParams(HashSet<Integer> liveIdSwarmParamsRats, int max) {
        gameRoom.sendNewEnemiesMessage(gameRoom.getMap().addSwarmByParams(liveIdSwarmParamsRats, max));
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

    private Long spawnEnemy(EnemyRange range, int skinId, Trajectory oldTrajectory, long parentEnemyId) {
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

            boolean isBomberWithoutHP = enemyType.equals(EnemyType.ENEMY_14);
            IMathEnemy mathEnemy = isBomberWithoutHP ? null : getMap().createMathEnemy(enemyType);
            boolean needNearCenter = HORUS.getEnemies().contains(enemyType);

            Enemy enemy = getMap().addEnemyByType(enemyType, mathEnemy, skinId, parentEnemyId, needNearCenter,
                    needFinalSteps(), true);
            if (!isBomberWithoutHP)
                enemy.setEnergy(mathEnemy.getFullEnergy());

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


    private Long spawnHVEnemyFromTestStand(EnemyType enemyType, int skinId, Trajectory oldTrajectory, long parentEnemyId) {
        long res;
        MathEnemy nextMathEnemy = new MathEnemy(-1, enemyType.name(), 1, 1);
        boolean needNearCenter = false;

        Enemy enemy = (oldTrajectory == null) ? getMap().addEnemyByTypeNew(enemyType, nextMathEnemy, skinId,
                parentEnemyId, needNearCenter, needFinalSteps(), false) :
                getMap().addConcreteHVEnemy(enemyType, skinId, oldTrajectory, nextMathEnemy, parentEnemyId);

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
            case 2:
                enemyRange = EnemyRange.WEAPON_CARRIER;
                break;
            default:
                break;
        }
        spawnHVEnemyFromTestStand(enemyRange.getEnemies().get(0), 1, null, -1);
    }

    private void spawnScarab() {
        spawnEnemy(EnemyRange.Scarabs, 1, null, -1);
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
            EnemyBoss enemy = (EnemyBoss) getMap().addEnemyByTypeNew(EnemyType.Boss, mathEnemy, bossSkinId,
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

        gameRoom.getPlayerInfoService().lock(seat.getAccountId());

        try {
            if (seat.getCurrentWeaponId() != SpecialWeaponType.Landmines.getId()) {
                sendError(seat, mineCoordinates, WRONG_WEAPON, "Wrong weapon", mineCoordinates);
                return;
            }

            int shots = seat.getWeapons().get(SpecialWeaponType.Landmines).getShots();
            if (shots <= 0 && !mineCoordinates.isPaidSpecialShot()) {
                sendError(seat, mineCoordinates, WRONG_WEAPON, "Wrong weapon", mineCoordinates);
                return;
            }

            double x = coords.toX(mineCoordinates.getX(), mineCoordinates.getY());
            double y = coords.toY(mineCoordinates.getX(), mineCoordinates.getY());
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
                        screenX = coords.toScreenX(x, y);
                        screenY = coords.toScreenY(x, y);
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

            int multiplierPaidWeapons = seat.getBetLevel();
            if (mineCoordinates.isPaidSpecialShot()) {
                multiplierPaidWeapons *= MathData.getPaidWeaponCost(Landmines.getId());
            }

            getLog().debug("aid: {}, placeMine decrement ammo amount, multiplierPaidWeapons: {}, " +
                            "old seat.getAmmoAmount(): {}, seat.getBetLevel(): {}",
                    seat.getAccountId(), multiplierPaidWeapons, seat.getAmmoAmount(), seat.getBetLevel());

            if (mineCoordinates.isPaidSpecialShot() && seat.getAmmoAmount() < multiplierPaidWeapons) {
                sendError(seat, mineCoordinates, NOT_ENOUGH_BULLETS, "Wrong amount for shot", mineCoordinates);
                getLog().debug("placeMineToMap, seat.getAmmoAmount(): {} less then weapon cost: {}",
                        seat.getAmmoAmount(), multiplierPaidWeapons);
                return;
            }

            if (mineCoordinates.isPaidSpecialShot()) {
                seat.decrementAmmoAmount(multiplierPaidWeapons);
            }

            getLog().debug("aid: {}, placeMine decrement ammo amount, new seat.getAmmoAmount(): {}",
                    seat.getAccountId(), seat.getAmmoAmount());

            seat.getSeatMines().add(minePoint);
            seat.addMineState(minePoint.getMineId((int) seat.getId()), mineCoordinates.isPaidSpecialShot());

            getLog().debug("For account: {}, add mine to point:{}, allMines: {} ",
                    seat.getAccountId(), minePoint, seat.getSeatMines());

            if (!mineCoordinates.isPaidSpecialShot())
                seat.consumeSpecialWeapon(SpecialWeaponType.Landmines.getId());

            long currentTime = getCurrentTime();
            String mineId = minePoint.getMineId(seat.getNumber());

            IMinePlace minePlaceSeat = getTOFactoryService().createMinePlace(currentTime, mineCoordinates.getRid(), seat.getNumber(),
                    (float) screenX, (float) screenY, mineId);

            IMinePlace minePlaceAll = getTOFactoryService().createMinePlace(currentTime, -1, seat.getNumber(),
                    (float) screenX, (float) screenY, mineId);

            getRoom().sendChanges(minePlaceAll, minePlaceSeat, seat.getAccountId(), null);
        } finally {
            gameRoom.getPlayerInfoService().unlock(seat.getAccountId());
        }
    }

    @Override
    public void processShot(Seat seat, IShot shot, boolean isInternalShot) throws CommonException {
        long time = System.currentTimeMillis();
        final boolean paidSpecialShot = shot.isPaidSpecialShot();
        Disposable updateTimer = gameRoom.getUpdateTimer();
        if (updateTimer == null || updateTimer.isDisposed()) {
            sendError(seat, shot, ErrorCodes.ROUND_NOT_STARTED, "Round not started", shot);
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

            if (!needWaitingWhenEnemiesLeave) {
                lastShotTime = time;
            }

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
            if (lock)
                lockShots.unlock();
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


    protected ShootResult shootWithRegularWeaponAndUpdateState(long time, Seat seat, Long itemIdForShot, ShotMessages messages)
            throws CommonException {
        ShootResult result = shootToOneEnemy(time, seat, itemIdForShot, seat.getCurrentWeaponId(), false,
                1);

        getLog().debug("shootResult: {}", result);

//        processBombEnemy(seat, result);

        if (result.isNeedGenerateHVEnemy() && allowSpawnHW) {
            generateHVEnemy(result, messages, seat.getPlayerInfo().getSessionId());
        }
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
        Optional<ShootResult> first = results.stream().filter(shootResult -> shootResult.getNeedExplodeHP() > 0).findFirst();
        if (first.isPresent()) {
            Map<Long, Double> nNearestEnemies = getMap().getNNearestEnemies(time, locationOfBaseEnemy, itemIdForShot,
                    RNG.nextInt(4) + 8, BaseEnemiesWithoutWC);

            ShootResult mainShootResult = first.get();
            Money killAward = mainShootResult.getKillAwardWin();
            Money addWin = Money.ZERO;

            ShootResult shootResult = first.get();
            int needExplodeHP = shootResult.getNeedExplodeHP();

            getLog().debug("processingExplodedShots, nNearestEnemies: " + nNearestEnemies
                    + " shootResult.getNeedExplodeHP(): " + needExplodeHP);

            if (nNearestEnemies.size() > 0) {
                int[] damages = MathData.getDistributionByEnemy(needExplodeHP, nNearestEnemies.size());
                getLog().debug("processingExplodedShots, nNearestEnemies.size(): " + nNearestEnemies.size()
                        + ", damages: " + Arrays.toString(damages));

                int cnt = 0;
                for (Map.Entry<Long, Double> enemyPair : nNearestEnemies.entrySet()) {
                    ShootResult res = shootToOneEnemyExplode(seat, enemyPair.getKey(), damages[cnt++]);
                    if (!res.getWin().equals(Money.INVALID)) {
                        res.setExplode(true);
                        results.add(res);
                    }
                }
                getLog().debug("results after processingExplodedShots: {}", results);
            } else {
                addWin = seat.getStake().getWithMultiplier(MathData.getBomberHPWin() * MathData.PAY_HIT_PERCENT *
                        seat.getBetLevel());
            }

            killAward = killAward.add(addWin);
            getLog().debug("exploder compensate win: {}, new Win: {} ", addWin, killAward);
            mainShootResult.setKillAwardWin(killAward);

        }
    }

    private void makeCompensationForPoorPlaying(Seat seat, int weaponId, double numberDamages, int realNumberOfShots,
                                                boolean isPaidMode) {
        if (realNumberOfShots < numberDamages) {
            getLog().debug("realNumberOfShots:{} less then numberDamages: {}, need compensate:", realNumberOfShots, numberDamages);
            List<IWeaponSurplus> weaponSurplus = seat.getWeaponSurplus();
            getLog().debug("weaponSurplus before:{} ", weaponSurplus);
            int lostHits = (int) (numberDamages - realNumberOfShots);

            Double rtpForWeapon = MathData.getRtpCompensateSpecialWeapons(weaponId, isPaidMode) / 100;

            Money newCompensation = Money.ZERO;
            newCompensation = newCompensation.add(seat.getStake().multiply(rtpForWeapon * lostHits * seat.getBetLevel()));

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
        boolean isBombEnemyKilled = result.getEnemy() != null &&
                result.getEnemy().getEnemyClass().getEnemyType().equals(EnemyType.ENEMY_14) && result.isDestroyed();
        boolean isHit = isPrize || isWin || isWeapon || isBossWin || isAwardedWeapons || isWheelWin || isBombEnemyKilled;
        getLog().debug("isHit : {}, isPrize: {}, isWin: {}, isWeapon: {}, isBossWin: {}, " +
                        "isAwardedWeapons: {}, isWheelWin: {}, isBombEnemyKilled: {}",
                isHit, isPrize, isWin, isWeapon, isBossWin, isAwardedWeapons, isWheelWin, isBombEnemyKilled);

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

        if (result.isKilledMiss() || result.isInvulnerable()) {
            seat.incrementMissCount();
            seat.getCurrentPlayerRoundInfo().addKilledMissCounter(shot.getWeaponId(), 1);
            if (isPaidShotToBaseEnemy) {
                getLog().debug("found main result of killedMiss, result: {}", result);
            }

            messages.add(
                    getTOFactoryService().createMiss(getCurrentTime(), TObject.SERVER_RID, seat.getNumber(),
                            result.isKilledMiss(), awardedWeaponId,
                            enemyId, usedSpecialWeapon, seat.getSpecialWeaponRemaining(), diffScore, isLastResult, shot.getX(),
                            shot.getY(), newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                            seat.getBetLevel(), shot.getBulletId()),
                    getTOFactoryService().createMiss(getCurrentTime(), shot.getRid(), seat.getNumber(),
                            result.isKilledMiss(), awardedWeaponId, enemyId, usedSpecialWeapon,
                            seat.getSpecialWeaponRemaining(), diffScore, isLastResult, shot.getX(), shot.getY(),
                            newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                            seat.getBetLevel(), shot.getBulletId()));
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
                                    result.isInstanceKill(), result.getChMult(), enemyId,
                                    shot.getEnemyId(), shot.getBulletId());

                            hitOwn.setAwardedWeapons(result.getAwardedWeapons());
                            hitOwn.setNeedExplode(result.isNeedExplode());
                            hitOwn.setExplode(result.isExplode());
                            hitOwn.setKillBonusPay(killAwardWin.toDoubleCents());
                            hitOwn.setBetLevel(betLevel);
                            hitOwn.setPaidSpecialShot(shot.isPaidSpecialShot());
                            hitOwn.setMoneyWheelWin(result.getMoneyWheelWin().toDoubleCents());

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
                                    result.isInstanceKill(), result.getChMult(), enemyId, shot.getEnemyId(),
                                    shot.getBulletId());
                            hit.setBetLevel(betLevel);
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

//        if(result.getEnemyAnimation().equals(EnemyAnimation.BOSS_WEEK_STATE)){
//            sendCustomUpdateTrajectories(EnemyRange.Boss, result.getEnemyAnimation());
//        }

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
        EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
        getLog().debug("PlayerId: " + seat.getPlayerInfo().getId() + ", isBot:  "
                + isBot + " itemIdForShot for shot: " + itemIdForShot + " enemyType: " + enemyType.name());

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
            if (enemyType.equals(EnemyType.ENEMY_14)) {
                getLog().debug("shootToOneEnemy,update last time killing bomb enemy: {}", map.getItemsSize());
                getLeaveAndDestroyEnemiesTime().put(enemyType.getId(), time);
            }
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
        questManager = new QuestManager(getTOFactoryService());
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

    public static int getMaxAliveRats() {
        return MAX_ALIVE_RATS;
    }

    public static int getMaxAliveCrabs() {
        return MAX_ALIVE_CRABS;
    }

    private void spawnEnemyWithClones(EnemyRange enemyRange, SwarmType swarmType) {
        try {
            int numberEnemiesInGroup = 0;
            boolean isLowMummies = LOW_MUMMIES.equals(enemyRange);
            boolean isPharaohMummies = PHARAOH_MUMMIES.equals(enemyRange);

            int groupStartTime = 0;
            int radius = 5;
            int minAngle = 45;

            if (isLowMummies) {
                if (swarmType.equals(DUAL_SPEED_MUMMIES)) {
                    numberEnemiesInGroup = RNG.nextInt(2) + 5;
                } else if (swarmType.equals(SMALL_GROUP_MUMMIES)) {
                    numberEnemiesInGroup = RNG.nextInt(2) + 1;
                    radius = 2;
                }
            } else if (isPharaohMummies) {
                numberEnemiesInGroup = 4;
            }

            getLog().debug("spawnEnemyWithClones enemyRange: {}, numberEnemiesInGroup: {}, swarmType: {}",
                    enemyRange, numberEnemiesInGroup, swarmType);

            List<Enemy> listEnemiesInGroup = new ArrayList<>();

            EnemyType firstEnemyType = null;

            List<EnemyType> possibleEnemyTypes = new ArrayList<>();

            if (isLowMummies) {
                List<EnemyType> enemies = LOW_MUMMIES.getEnemies();
                firstEnemyType = enemies.get(RNG.nextInt(enemies.size()));
                possibleEnemyTypes.addAll(LOW_MUMMIES.getEnemies());
            } else if (isPharaohMummies) {
                groupStartTime = 1500;
                radius = 4;
                minAngle = 90;
                firstEnemyType = EnemyType.ENEMY_13;
                EnemyType finalFirstEnemyType = firstEnemyType;
                possibleEnemyTypes.addAll(
                        PHARAOH_MUMMIES.getEnemies().stream()
                                .filter(enemyType -> !enemyType.equals(finalFirstEnemyType))
                                .collect(Collectors.toList()));
            }

            Enemy firstEnemy = getMap().addEnemyByTypeNew(firstEnemyType, getMap().createMathEnemy(firstEnemyType), 1,
                    -1, false, needFinalSteps(), false);
            firstEnemy.setEnergy(firstEnemy.getFullEnergy());

            float enemySpeed = (float) firstEnemy.getSpeed();
            Trajectory predefinedTrajectory = getMap().getPredefinedTrajectory(swarmType, enemySpeed);
            if (predefinedTrajectory != null) {
                firstEnemy.setTrajectory(predefinedTrajectory);
                firstEnemy.setSpeed(enemySpeed);
            } else {
                firstEnemy.setSpeed(firstEnemy.getTrajectory().getSpeed());
            }

            firstEnemy.setParentEnemyId(firstEnemy.getId());
            listEnemiesInGroup.add(firstEnemy);
            Trajectory baseTrajectory = firstEnemy.getTrajectory();
            List<Point> originalPoints = baseTrajectory.getPoints();
            List<PointD> enemyOffsets = getMap().getRadialOffsets(radius, numberEnemiesInGroup, minAngle);
            getLog().debug("firstEnemy baseTrajectory: " + baseTrajectory);
            getLog().debug("firstEnemy.getSpeed(): {}", firstEnemy.getSpeed());

            for (int i = 0; i < numberEnemiesInGroup; i++) {
                EnemyType enemyType = possibleEnemyTypes.get(RNG.nextInt(possibleEnemyTypes.size()));
                Enemy additionalEnemy = getMap().addEnemyByTypeNew(enemyType, getMap().createMathEnemy(enemyType), 1,
                        firstEnemy.getParentEnemyId(), false, needFinalSteps(), false);
                additionalEnemy.setEnergy(additionalEnemy.getFullEnergy());
                Trajectory trajectory = TrajectoryUtils.generateSimilarTrajectory(baseTrajectory,
                        enemyOffsets.get(i).x, enemyOffsets.get(i).y, 0, 0,
                        firstEnemy.getSpeed(), 0,
                        originalPoints.get(0).getTime(), groupStartTime);

                getLog().debug("additional enemy i: {} trajectory: {} ", i, baseTrajectory);
                additionalEnemy.setTrajectory(trajectory);
                additionalEnemy.setSpeed(firstEnemy.getSpeed());
                listEnemiesInGroup.add(additionalEnemy);
            }

            int swarmId = getMap().generateSwarmId();
            for (Enemy enemy : listEnemiesInGroup) {
                enemy.addToSwarm(swarmType, swarmId);
            }
            getMap().registerSwarm(swarmId, listEnemiesInGroup);

            gameRoom.sendNewEnemiesMessage(listEnemiesInGroup);
        } catch (Exception e) {
            getLog().debug("spawnEnemyWithClones error: ", e);
        }
    }

    @Override
    protected PlayGameState getDeserializer() {
        return this;
    }
}
