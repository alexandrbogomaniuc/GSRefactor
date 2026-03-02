package com.betsoft.casino.mp.amazon.model;

import com.betsoft.casino.mp.amazon.model.math.*;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.BossPartEnemy;
import com.betsoft.casino.mp.model.gameconfig.EnemyParams;
import com.betsoft.casino.mp.model.gameconfig.GameConfig;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.quests.ITreasureProgress;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.betsoft.casino.utils.TObject;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.amazon.model.math.EnemyType.*;
import static com.betsoft.casino.mp.amazon.model.math.SwarmLimits.getLimit;
import static com.betsoft.casino.mp.amazon.model.math.SwarmType.*;
import static com.betsoft.casino.mp.model.EnemyDestroyReason.SIMPLE_SHOT;
import static com.betsoft.casino.mp.utils.ErrorCodes.*;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

@SuppressWarnings("Duplicates")
public class PlayGameState extends AbstractActionPlayGameState<GameRoom, Seat, GameMap, PlayGameState> {
    private static int MAX_ALIVE_CRABS_AND_RATS = 10;
    private static int MAX_ALIVE_PIRATES = 4;
    private static int MAX_ALIVE_WALKING_ENEMIES_IN_BOSS_ROUND = 8;
    private static int MAX_ALIVE_ENEMIES_IN_BOSS_ROUND = 20;
    protected transient long lastTimeOfGenerationBird;
    private transient boolean needImmediatelySpawn = false;
    private transient List<Integer> remainingBosses;

    public PlayGameState() {
        super();
    }

    public PlayGameState(GameRoom gameRoom) {
        super(gameRoom, new QuestManager(gameRoom.getTOFactoryService()));
    }

    @Override
    public void init() throws CommonException {
        super.init();
        getRemainingBosses().clear();
    }

    @Override
    protected int getMaxAliveEnemies() {
        return MAX_ALIVE_PIRATES;
    }

    public void setMaxAliveCrabsAndRats(int maxAliveCrabsAndRats) {
        getLog().debug("MAX_ALIVE_CRABS_AND_RATS: {}", maxAliveCrabsAndRats);
        MAX_ALIVE_CRABS_AND_RATS = maxAliveCrabsAndRats;
    }

    public void setMaxAlivePirates(int maxAlivePirates) {
        getLog().debug("MAX_ALIVE_PIRATES: {}", MAX_ALIVE_PIRATES);
        MAX_ALIVE_PIRATES = maxAlivePirates;
    }

    public List<Integer> getRemainingBosses() {
        if (remainingBosses == null) {
            remainingBosses = new ArrayList<>();
        }
        return remainingBosses;
    }

    @Override
    protected int getMaxAliveCritters() {
        return MAX_ALIVE_CRABS_AND_RATS;
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
    protected void updateWithLock() throws CommonException {
        boolean isBossRound = subround == PlaySubround.BOSS;

        GameMap map = getMap();
        map.update();

        checkTestStandFeatures();
        if (!needWaitingWhenEnemiesLeave && !isManualGenerationEnemies()) {
            try {
                gameRoom.sendNewEnemiesMessage(map.respawnEnemies());
                boolean needFinalSteps = needFinalSteps();
                if (!isBossRound) {
                    map.getEnemiesForNewEnemyUpdating(needFinalSteps, getLiveSimpleEnemiesOfRound().isEmpty()).forEach(enemyId ->
                            gameRoom.sendNewEnemyMessage(getMap().getItemById(enemyId)));
                }
            } catch (Exception e) {
                getLog().debug("getEnemiesForNewEnemyUpdating error: ", e);
            }
        }

        try {
            calculateMinesOnMap();
        } catch (Exception e) {
            getLog().debug("calculateMinesOnMap error: ", e);
        }

        getMap().checkFreezeTimeEnemies(FREEZE_TIME_MAX);

        int aliveMummies = 0;
        List<Pair<Integer, Boolean>> itemsTypeIdsAndSwarmState = map.getItemsTypeIdsAndSwarmState();

        boolean needMinimalEnemies = isNeedMinimalEnemies();

        if (needMinimalEnemies && itemsTypeIdsAndSwarmState.size() > 3) {
            return;
        }

        for (Pair<Integer, Boolean> pair : itemsTypeIdsAndSwarmState) {
            EnemyType enemyType = EnemyType.getById(pair.getKey());
            if (EnemyRange.Mummies.getEnemies().contains(enemyType)) {
                aliveMummies++;
            }
        }

        aliveMummies += map.getNumberInactivityItemsByRange(EnemyRange.Mummies);

        if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            getLog().debug("no live enemies in room:, finish ");
            nextSubRound();
            return;
        }

        boolean needRandomBoss = RNG.nextInt(50) == 0;

        if (subround.equals(PlaySubround.BASE) && !getRemainingBosses().isEmpty() && (isNeedImmediatelySpawn() || needRandomBoss ||
                getMap().noEnemiesInRoom())) {
            mainBossIsAvailable = true;
            nextSubRound();
            mainBossIsAvailable = false;
            getLog().debug(" generate Boss, remainingNumberOfBoss : {}, getRemainingBosses(): {}",
                    remainingNumberOfBoss, getRemainingBosses());
            return;
        }

        if (allowSpawn && !isManualGenerationEnemies()) {
            if (subround.equals(PlaySubround.BASE)) {
                if (RNG.nextInt(10) == 0) {
                    spawnSwarm();
                    if (needMinimalEnemies) {
                        return;
                    }
                }

                if (RNG.nextInt(10) == 0 && getMap().swarmCount(SwarmType.TRIPLE_SNAKE) < 3) {
                    gameRoom.sendNewEnemiesMessage(getMap().spawnSnakesSwarm(SNAKE));
                    if (needMinimalEnemies) {
                        return;
                    }
                }

                boolean realCase = aliveMummies < getMaxAliveEnemies() && RNG.nextInt(80) < spawnProbability;
                if (realCase || isNeedImmediatelySpawn()) {
                    spawnMummy();
                }
            } else {
                if (map.swarmCount(ANT_SCENARIO, WASP_ORION, WASP_REGULAR) == 0) {
                    spawnSwarmInBossRound();
                    if (needMinimalEnemies) {
                        return;
                    }
                }

                boolean shouldSpawn = aliveMummies < MAX_ALIVE_WALKING_ENEMIES_IN_BOSS_ROUND
                        && map.getItemsSize() < MAX_ALIVE_ENEMIES_IN_BOSS_ROUND
                        && RNG.nextInt(80) < spawnProbability;
                if (shouldSpawn || isNeedImmediatelySpawn()) {
                    spawnMummy();
                }
            }
        } else if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            nextSubRound();
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

    private void checkForGenerationStandaloneHV() {
        int skinId = 1;
        long spawnEnemyId = spawnEnemy(EnemyRange.HV_ENEMIES, skinId, -1, 0);
        if (spawnEnemyId != -1)
            getLog().debug("checkForGenerationStandaloneHV " + " skinId: " + skinId + " enemyRange: "
                    + " spawnEnemyId: " + spawnEnemyId);

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


    private void spawnSwarm() {
        GameMap map = getMap();
        List<Enemy> swarm = new ArrayList<>();
        switch (RNG.nextInt(4)) {
            case 0:
                if (map.swarmCount(ANT_SCENARIO) < getLimit(ANT_SCENARIO)) {
                    swarm = getMap().spawnAntScenarioSwarm(ANT);
                }
                break;
            case 1:
                if (map.swarmCount(WASP_REGULAR) < getLimit(WASP_REGULAR)) {
                    swarm = getMap().spawnWaspSwarm(WASP);
                }
                break;
            case 2:
                if (map.swarmCount(WASP_ORION) < getLimit(WASP_ORION)) {
                    swarm = getMap().spawnWaspOrionSwarm(WASP);
                }
                break;
            case 3:
                if (map.swarmCount(RUNNERS) < getLimit(RUNNERS)) {
                    swarm = getMap().spawnRunnerFormation(RUNNER);
                }
                break;
        }
        gameRoom.sendNewEnemiesMessage(swarm);
    }

    private void spawnSwarmInBossRound() {
        List<Enemy> swarm = new ArrayList<>();
        switch (RNG.nextInt(3)) {
            case 0:
                swarm = getMap().spawnAntScenarioSwarm(ANT);
                break;
            case 1:
                swarm = getMap().spawnWaspSwarm(WASP);
                break;
            case 2:
                swarm = getMap().spawnWaspOrionSwarm(WASP);
                break;
        }
        gameRoom.sendNewEnemiesMessage(swarm);
    }

    @Override
    public void nextSubRound() throws CommonException {
//        needWaitingWhenEnemiesLeave = false;
        GameMap map = gameRoom.getMap();
        getLog().debug("nextSubRound, getRemainingBosses(): {}", getRemainingBosses());
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
            lockShots.unlock();
        }
    }

    @Override
    protected void spawnMummy() {
        if (!isManualGenerationEnemies())
            spawnEnemy(EnemyRange.Mummies, -1, -1, RNG.nextInt(10000, 15000));
    }

    private boolean needFinalSteps() {
        return !(subround == PlaySubround.BOSS);
    }

    private Long spawnEnemy(EnemyRange range, int skinId, long parentEnemyId, long respawnDelay) {
        long res = -1;

        try {
            List<EnemyType> enemies = range.getEnemies();
            EnemyType enemyType = enemies.get(RNG.nextInt(enemies.size()));

            if (enemyType.equals(EnemyType.EXPLODER)) {
                if (getMap().getPossibleItemsId().containsValue(EnemyType.EXPLODER.getId())) {
                    return res;
                }
            }

            if (EnemyRange.HV_ENEMIES.getEnemies().contains(enemyType) || enemyType.equals(SHAMAN) || enemyType.equals(JAGUAR) || enemyType.equals(JUMPER)) {
                List<Integer> itemsTypeIds = getMap().getItemsTypeIds();
                long count = itemsTypeIds.stream().filter(id -> id == enemyType.getId()).count();
                getLog().debug("enemyType: {} count: {}", enemyType, count);
                getLog().debug("itemsTypeIds: {}", itemsTypeIds);
                if (count >= 1) {
                    return res;
                }
            }

            if (getMap().isEnemyRemoved(enemyType)) {
                return res;
            }

            if (EnemyRange.LargeEnemies.getEnemies().contains(enemyType)
                    && getMap().getAliveEnemiesCount(EnemyRange.LargeEnemies) > 2) {
                return res;
            }

            boolean needNearCenter = range.equals(EnemyRange.MINI_BOSS);

            IMathEnemy mathEnemy = getMap().createMathEnemy(enemyType);

            boolean needFinalSteps = needFinalSteps();
            if (enemyType.equals(EnemyType.RUNNER)) {
                needFinalSteps = false;
            }

            Enemy enemy = getMap().addEnemyByTypeNew(enemyType, mathEnemy, skinId, parentEnemyId, needNearCenter,
                    needFinalSteps, false);
            enemy.setEnergy(mathEnemy.getFullEnergy());
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
            getLog().debug("spawn enemy error , range: {}, skinId: {}", range, skinId);
            getLog().debug("spawn enemy error: ", e);
        }
        return res;
    }

    private EnemyType getRandomEnemyType(EnemyRange range) {
        List<EnemyType> enemies = range.getEnemies();
        return enemies.get(RNG.nextInt(enemies.size()));
    }

    private Long spawnHVEnemyFromTestStand(EnemyType enemyType, int skinId, Trajectory oldTrajectory, long parentEnemyId) {
        long res;
        IMathEnemy nextMathEnemy = new MathEnemy(-1, enemyType.name(), 1, 1);
        boolean needNearCenter = EnemyRange.MINI_BOSS.getEnemies().contains(enemyType);

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
        EnemyType enemyType = getRandomEnemyType(EnemyRange.Scarabs);
        if (enemyType.equals(EnemyType.SNAKE) && getMap().swarmCount(SwarmType.TRIPLE_SNAKE) < 3) {
            gameRoom.sendNewEnemiesMessage(getMap().spawnSnakesSwarm(enemyType));
        } else {
            spawnEnemy(EnemyRange.Scarabs, 1, -1, 0);
        }
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
    public void placeMineToMap(Seat seat, IMineCoordinates mineCoordinates) {
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

            IMinePlace minePlaceSeat = getTOFactoryService().createMinePlace(currentTime, mineCoordinates.getRid(), seat.getNumber(),
                    (float) screenX, (float) screenY, mineId);

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
        boolean lock = false;
        try {
            lock = lockShots.tryLock(3, TimeUnit.SECONDS);
            if (!lock) {
                sendError(seat, shot, WRONG_WEAPON, "get lock failed", shot);
                return;
            }
            getLog().debug("processShot: aid: {},  shot:{},  isInternalShot: {}, ammo: {}", seat.getAccountId(),
                    shot, isInternalShot, seat.getAmmoAmount());
            int currentWeaponIdOld = seat.getCurrentWeaponId();
            boolean needRestoreWeapon = false;
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
            int numberOfKilledMissOld = roundInfo.getKilledMissedNumber(seat.getCurrentWeaponId());

            if (seat.getAmmoAmountTotalInRound() == 0 && seat.getPlayerInfo().getRoundBuyInAmount() == 0
                    && !allowWeaponSaveInAllGames && !isPaidShot) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets", shot);
            } else if (seat.getAmmoAmount() <= 0 && isPaidShot) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets", shot);
            } else if (shot.getRealWeaponId() != seat.getCurrentWeaponId()) {
                sendError(seat, shot, WRONG_WEAPON, "Wrong weapon", shot);
            } else if (shot.getWeaponId() == REGULAR_WEAPON) {
                shootWithRegularWeapon(seat, shot);
                roundInfo.addRealShotsCounter(seat.getCurrentWeaponId(), 1);
                if (roundInfo.isShotSuccess(seat.getCurrentWeaponId(), numberOfKilledMissOld)) {
                    roundInfo.addKpiInfoPaidRegularShots(seat.getStake().toCents());
                    roundInfo.addKpiInfoSWShotsCount(-1, 1, seat.getBetLevel(), false);
                }
            } else if (isInternalShot || seat.getCurrentWeapon().getShots() > 0) {
                int weaponId = seat.getCurrentWeaponId();
                if (weaponId == SpecialWeaponType.Cryogun.getId()) {
                    if (!needWaitingWhenEnemiesLeave) {
                        sendFreezeTrajectories(time, shot.getX(), shot.getY(), 280);
                    }
                }
                shootWithSpecialWeapon(time, seat, shot);
                roundInfo.addRealShotsCounter(weaponId, 1);
                if (roundInfo.isShotSuccess(weaponId, numberOfKilledMissOld)) {
                    roundInfo.addKpiInfoFreeShotsCount(1);
                    roundInfo.addKpiInfoSWShotsCount(weaponId, 1, seat.getBetLevel(), true);
                }
            } else {
                sendError(seat, shot, WRONG_WEAPON, "Weapon not found", shot);
            }

            if (!needWaitingWhenEnemiesLeave)
                lastShotTime = System.currentTimeMillis();

            if (needRestoreWeapon) {
                seat.setWeapon(currentWeaponIdOld);
            }

            gameRoom.getSeats().forEach(AbstractSeat::updateScoreShotTotalWin);

            getLog().debug("shot, end  getCountRemainingEnemiesByModel: {} , ammo: {}, allowSpawn: {} ",
                    getCountRemainingEnemiesByModel(), seat.getAmmoAmount(), allowSpawn);

        } catch (InterruptedException e) {
            getLog().warn(" shot exception: ", e);
        } finally {
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

        if (maxDamage < energy)
            return true;
        return false;
    }

    protected void shootWithRegularWeapon(Seat seat, IShot shot) throws CommonException {
        long time = System.currentTimeMillis();

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
            seat.decrementAmmoAmount();
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

        if (result.isDestroyed() && EXPLODER.equals(result.getEnemy().getEnemyClass().getEnemyType())) {
            getLog().debug("Spawn frogs from exploder");
            messages.addAllMessage(gameRoom.convertNewEnemies(getCurrentTime(),
                    getMap().spawnExplodedFrogs(time, result.getEnemy())));
        }

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
            ShootResult res = shootToOneEnemy(time, seat, enemyId, weaponId, false,
                    1);
            results.add(res);
            if (!res.getWin().equals(Money.INVALID)) {
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
            getLog().debug("before compensation " + "realNumberOfShots: {}, numberDamages: {}",
                    realNumberOfShots, numberDamages);
            makeCompensationForPoorPlaying(seat, weaponId, numberDamages, realNumberOfShots);
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
            if (weaponId != SpecialWeaponType.Landmines.getId() && !isFreeShot)
                seat.consumeSpecialWeapon(weaponId);

            getLog().debug("shootResult: ");
            for (ShootResult shootResult : results) {
                getLog().debug(shootResult);
            }

            getLog().debug("allowSpawnHW: {}", allowSpawnHW);
            if (mainShootResult.isNeedGenerateHVEnemy() && allowSpawnHW) {
                generateHVEnemy(mainShootResult, messages, seat.getPlayerInfo().getSessionId());
            }

            for (ShootResult result : results) {
                if (result.isDestroyed() && EXPLODER.equals(result.getEnemy().getEnemyClass().getEnemyType())) {
                    getLog().debug("Spawn frogs from exploder");
                    messages.addAllMessage(gameRoom.convertNewEnemies(getCurrentTime(),
                            getMap().spawnExplodedFrogs(time, result.getEnemy())));
                }
            }
        }

        return results;
    }

    private void processingExplodedShots(long time, List<ShootResult> results, PointD locationOfBaseEnemy,
                                         Long itemIdForShot, Seat seat) throws CommonException {
        Optional<ShootResult> first = results.stream().filter(shootResult -> shootResult.getNeedExplodeHP() > 0).findFirst();
        if (first.isPresent()) {
            Map<Long, Double> nNearestEnemies = getMap().getNNearestEnemies(time, locationOfBaseEnemy, itemIdForShot,
                    RNG.nextInt(3) + 2, EnemyRange.ExplodeEnemies);

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
                addWin = seat.getStake().getWithMultiplier(MathData.getAvgHpWinExploder() * MathData.PAY_HIT_PERCENT);
            }

            killAward = killAward.add(addWin);
            getLog().debug("exploder compensate win: {}, new Win: {} ", addWin, killAward);
            mainShootResult.setKillAwardWin(killAward);

        }
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
        boolean isGems = result.getGems() != null && !result.getGems().isEmpty() &&
                result.getGems().stream().reduce(0, Integer::sum) > 0;

        //boolean isMultiplier = result.getMultiplierPay() > 0;

        boolean isHit = isPrize || isWin || isWeapon || isBossWin || isAwardedWeapons || isGems;
        getLog().debug("isHit : {}, isPrize: {}, isWin: {}, isWeapon: {}, isBossWin: {}, isAwardedWeapons: {}, isGems: {}",
                isHit, isPrize, isWin, isWeapon, isBossWin, isAwardedWeapons, isGems);

        Map<Integer, List<IWinPrize>> hitResultBySeats = new HashMap<>();
        Map<Seat, IHit> messagesForSeatsLocal = new HashMap<>();
        IHit hitOwn = null;
        List<IHit> hitsForObserversLocal = new ArrayList<>();


        if (result.isKilledMiss() || result.isInvulnerable()) {
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

                            Money totalGemsPayout = result.getTotalGemsPayout();
                            if (totalGemsPayout.greaterThan(Money.ZERO)) {
                                currentPlayerRoundInfo.updateAdditionalWin("totalGemsPayout", totalGemsPayout);
                                seatCurrent.incrementRoundWin(totalGemsPayout);
                                seatCurrent.incrementShotTotalWin(totalGemsPayout);
                                seatCurrent.addLastWin(totalGemsPayout);
                                totalSimpleWin = totalSimpleWin.add(totalGemsPayout);
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
                            hitOwn.setNeedExplode(result.isNeedExplode());
                            hitOwn.setExplode(result.isExplode());
                            hitOwn.setMultiplierPay(result.getMultiplierPay());
                            hitOwn.setKillBonusPay(killAwardWin.toDoubleCents());
                            hitOwn.setGems(result.getGems());

                            getLog().debug("isSpecialWeapon {}, isLastResult: {}, stake: {}, title: {}," +
                                            " isExplode: {} ", isSpecialWeapon,
                                    isLastResult, seat.getStake(), title, result.isExplode());

                            Money stake = (isSpecialWeapon || result.isExplode()) ? Money.ZERO : seat.getStake();
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

                            hit.setNeedExplode(result.isNeedExplode());
                            hit.setExplode(result.isExplode());
                            hit.setMultiplierPay(result.getMultiplierPay());
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
                    boolean isProcessed = processQuests(seat, result);
                    if (isProcessed) {
                        updateHitResultBySeats(seat.getNumber(), Money.ZERO, result.getPrize(),
                                hitResultBySeats, -1, new ArrayList<>(), null);
                    }
                }

                // hit for observers
                IHit hitForObservers = getTOFactoryService().createHit(getCurrentTime(), SERVER_RID, seat.getNumber(), result.getDamage(), 0,
                        awardedWeaponId, usedSpecialWeapon, seat.getSpecialWeaponRemaining(),
                        diffScore, enemy, isLastResult, 0,
                        result.getHvEnemyId(), shot.getX(), shot.getY(), newShots, result.isDestroyed(),
                        result.getMineId(), result.getNewFreeShotsCount(), seat.getNumber(),
                        result.isInstanceKill(), result.getChMult(), enemyId, shot.getEnemyId());

                hitForObservers.setNeedExplode(result.isNeedExplode());
                hitForObservers.setExplode(result.isExplode());
                hitForObservers.setMultiplierPay(result.getMultiplierPay());
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
                        result.isInvulnerable()),
                        getTOFactoryService().createMiss(getCurrentTime(), shot.getRid(), seat.getNumber(), false,
                                awardedWeaponId, enemyId, usedSpecialWeapon, specialWeaponRemaining, diffScore,
                                isLastResult, shot.getX(), shot.getY(), newShots, result.getMineId(),
                                shot.getEnemyId(), result.isInvulnerable()));
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

//        if(result.getEnemyAnimation().equals(EnemyAnimation.BOSS_WEEK_STATE)){
//            sendCustomUpdateTrajectories(EnemyRange.Boss, result.getEnemyAnimation());
//        }

    }

    protected boolean processQuests(Seat seat, IShootResult result) {
        boolean res = false;
        IPlayerQuests playerQuests = seat.getPlayerInfo().getPlayerQuests();
        getLog().debug("old player quests: {}", playerQuests.getQuests());

        String[] keys = result.getPrize().split("\\|");
        for (String key : keys) {
            res = processingQuestKey(Treasure.valueOf(key).getId(), seat, result.getEnemyId());
        }
        return res;
    }

    private boolean processingQuestKey(int prizeId, Seat seat, long enemyId) {
        Set<IQuest> quests = seat.getPlayerInfo().getPlayerQuests().getQuests();
        List<IQuest> questsWithPrize = quests.stream().filter(quest -> {
            List<ITreasureProgress> treasures = quest.getProgress().getTreasures();
            long count = treasures.stream().filter(treasureProgress ->
                    (treasureProgress.getTreasureId() == prizeId)).count();
            return count > 0;
        }).collect(Collectors.toList());

        getLog().debug("prizeId: {}, questsWithPrize: {}", prizeId, questsWithPrize);

        if (questsWithPrize.isEmpty()) {
            return false;
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
        getLog().debug(" quest: {}", quest);

        List<ITreasureProgress> treasures = quest.getProgress().getTreasures();
        ITreasureProgress progress = treasures.stream().filter(
                treasureProgress -> treasureProgress.getTreasureId() == prizeId).findFirst().get();
        progress.setCollect(progress.getCollect() + 1);

        long countUnfinished = treasures.stream().filter(treasureProgress ->
                treasureProgress.getCollect() < treasureProgress.getGoal()).count();

        boolean needFinish = countUnfinished == 0;
        Money win = Money.ZERO;

        if (needFinish) {
            getLog().debug("need finish quest : {}", quest.getName());
            int amount = quest.getQuestPrize().getAmount().getFrom();
            win = seat.getStake().multiply(amount);
            getLog().debug(" pay : " + amount + " win: " + win);

            IPlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
            seat.setQuestsPayouts(seat.getQuestsPayouts() + win.toCents());
            getLog().debug("quest win add to roundWin: {}", win);
            seat.incrementRoundWin(win);
            currentPlayerRoundInfo.updateQuestCompletedTotalData(win, -1, 0);
            quest.setNeedReset(true);
        }

        seat.sendMessage(getTOFactoryService().createUpdateQuest(System.currentTimeMillis(), quest, enemyId));
        seat.sendMessage(getTOFactoryService().createNewTreasure(System.currentTimeMillis(), -1, prizeId,
                enemyId, needFinish ? (int) quest.getId() : -1, quest.getId()));

        if (needFinish) {
            seat.setQuestsCompletedCount(seat.getQuestsCompletedCount() + 1);
            quest.setNeedReset(false);
            quest.setCollectedAmount(0);
            quest.getProgress().decreaseProgress();
            quest.getQuestPrize().setSpecialWeaponId(-1);

            gameRoom.sendChanges(getTOFactoryService().createSeatWinForQuest(System.currentTimeMillis(), SERVER_RID, seat.getNumber(), enemyId,
                    win.toCents(), -1));

            getLog().debug("quest is reset {}", quest);
        }

        getLog().debug("quests after processQuests: {}", quests);

        return true;
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

    @Override
    protected PlayGameState getDeserializer() {
        return this;
    }
}
