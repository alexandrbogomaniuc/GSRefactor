package com.betsoft.casino.mp.piratespov.model;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.model.quests.IQuest;
import com.betsoft.casino.mp.model.quests.ITreasureProgress;
import com.betsoft.casino.mp.movement.common.Offset;
import com.betsoft.casino.mp.piratescommon.model.math.*;
import com.betsoft.casino.teststand.TestStandLocal;
import com.betsoft.casino.utils.TObject;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.model.EnemyDestroyReason.SIMPLE_SHOT;
import static com.betsoft.casino.mp.piratescommon.model.math.EnemyType.*;
import static com.betsoft.casino.mp.piratespov.model.EnemyRange.*;
import static com.betsoft.casino.mp.piratespov.model.SwarmType.RED_BIRDS;
import static com.betsoft.casino.mp.piratespov.model.SwarmType.WHITE_BIRDS;
import static com.betsoft.casino.mp.utils.ErrorCodes.*;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

@SuppressWarnings("Duplicates")
public class PlayGameState extends AbstractActionPlayGameState<GameRoom, Seat, GameMap, PlayGameState> {
    protected static final int LEVIATHAN_SKIN_ID = 3;
    private static int MAX_ALIVE_ENEMIES = 100;
    private static int MAX_ALIVE_PIRATES = 40;
    protected transient long lastTimeOfGenerationBird;
    private transient boolean needImmediatelySpawn = false;
    private transient Map<Integer, Long> leaveAndDestroyEnemiesTime = new HashMap<>();
    transient LimitChecker limitChecker = new LimitChecker();
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

        checkTestStandFeatures();

        if (allowSpawn && !needWaitingWhenEnemiesLeave && PlaySubround.BASE.equals(subround)) {
            sendUpdateTrajectories(false);
        }

        if (needWaitingWhenEnemiesLeave || !allowSpawn) {
            getRemainingBosses().clear();
        }

        if (map.getItemsSize() >= MAX_ALIVE_ENEMIES && !isManualGenerationEnemies()) {
            return;
        }

        if (isNeedMinimalEnemies() && limitChecker.getTotalEnemies() > 3) {
            return;
        }

        if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            getLog().debug("no live enemies in room:, finish ");
            nextSubRound();
            return;
        }

        boolean needRandomBoss = RNG.nextInt(50) == 0 || getMap().noEnemiesInRoom();

        Long lastTime = getLeaveAndDestroyEnemiesTime().get(EnemyType.Boss.getId());
        if (lastTime != null && (System.currentTimeMillis() - lastTime) < 30000) {
            needRandomBoss = false;
        }

        if (!isManualGenerationEnemies() && !needWaitingWhenEnemiesLeave
                && subround.equals(PlaySubround.BASE) && !getRemainingBosses().isEmpty()
                && (isNeedImmediatelySpawn() || needRandomBoss)) {
            mainBossIsAvailable = true;
            nextSubRound();
            mainBossIsAvailable = false;
            getLog().debug(" generate Boss, remainingNumberOfBoss : {} ", getRemainingBosses());
            return;
        }

        if (allowSpawn && !isManualGenerationEnemies() && PlaySubround.BASE.equals(subround)) {
            map.updateEnemyLimits(limitChecker);
            gameRoom.sendNewEnemiesMessage(map.respawnEnemies(limitChecker));

            if (!needWaitingWhenEnemiesLeave && allowSpawn) {
                if (limitChecker.isRunnerSpawnAllowed() && (RNG.nextInt(100) < 7 || isNeedImmediatelySpawn())) {
                    checkForGenerationStandaloneHV(EnemyRange.RUNNERS);
                }

                if (RNG.nextInt(100) < 3 || isNeedImmediatelySpawn()) {
                    checkForGenerationStandaloneHV(EnemyRange.TROLLS);
                } else if (RNG.nextInt(100) < 3 || isNeedImmediatelySpawn()) {
                    checkForGenerationStandaloneHV(EnemyRange.WEAPON_CARRIER);
                }
            }

            if (limitChecker.isCrabsSpawnAllowed() && RNG.nextInt(10) == 0) {
                spawnCrabsSwarm();
            }

            if (limitChecker.isRatsSpawnAllowed() && RNG.nextInt(5) == 0) {
                spawnRatsSwarm();
            }

            if (limitChecker.getCount(Mummies) < getMaxAliveEnemies() || isNeedImmediatelySpawn()) {
                if (limitChecker.isCaptainsSpawnAllowed() && RNG.nextInt(100) < 30) {
                    if (limitChecker.isSpawnAllowed(ENEMY_15)) {
                        if (limitChecker.isSpawnAllowed(ENEMY_16)) {
                            spawnEnemy(CAPTAINS, -1, null, -1, 15000);
                        } else {
                            spawnEnemy(ENEMY_15, 1);
                        }
                    } else if (limitChecker.isSpawnAllowed(ENEMY_16)) {
                        spawnEnemy(ENEMY_16, 1);
                    }
                } else {
                    spawnEnemy(SINGLE_ENEMIES, -1, null, -1, 15000);
                }
            }

            boolean needBirdsSmall = limitChecker.isSpawnAllowed(ENEMY_9);
            boolean needBirdsNormal = limitChecker.isSpawnAllowed(ENEMY_10);
            boolean realCase = (needBirdsSmall || needBirdsNormal) && System.currentTimeMillis() - lastTimeOfGenerationBird > 3000;
            if (realCase || isNeedImmediatelySpawn()) {
                if (needBirdsSmall) {
                    spawnBirds(ENEMY_9);
                }
                if (needBirdsNormal) {
                    spawnBirds(ENEMY_10);
                }
                lastTimeOfGenerationBird = System.currentTimeMillis();
            }
        } else if (needWaitingWhenEnemiesLeave && noAnyEnemiesInRound()) {
            nextSubRound();
        }
    }

    protected void onTimerWithLock(boolean needClearEnemy) {
        getLog().debug("PlayGameState:: onTimer: current={}", this);
        getLog().debug("End round, aliveMummies: {} needWaitingWhenEnemiesLeave: {}",
                getMap().getItemsSize(), needWaitingWhenEnemiesLeave);

        if (!needWaitingWhenEnemiesLeave) {
            needWaitingWhenEnemiesLeave = true;
            allowSpawn = false;
            allowSpawnHW = false;
            mainBossIsAvailable = false;
            remainingNumberOfBoss = 0;
            getMap().clearInactivityLiveItems();
            if (needClearEnemy)
                getMap().removeAllEnemies();

            sendLeaveTrajectories();
            gameRoom.sendChanges(getTOFactoryService().createRoundFinishSoon(System.currentTimeMillis()));
        }
    }

    boolean noAnyEnemiesInRound() {
        return getMap().noEnemiesInRoom() && getCountRemainingEnemiesByModel() == 0 && remainingNumberOfBoss <= 0
                && getMap().getNumberInactivityItems() == 0;
    }

    private boolean checkForGenerationStandaloneHV(EnemyRange range) {
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

        long spawnEnemyId = spawnEnemy(range, skinId, null, -1, 15000);
        if (spawnEnemyId != -1) {
            getLog().debug("checkForGenerationStandaloneHV skinId: {} spawnEnemyId: {}", skinId, spawnEnemyId);
        }
        return true;
    }


    private void spawnCrabsSwarm() {
        gameRoom.sendNewEnemiesMessage(gameRoom.getMap().addCrabsSwarm());
    }

    private void spawnRatsSwarm() {
        gameRoom.sendNewEnemiesMessage(gameRoom.getMap().addRatsSwarm());
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
                    removeEnemies();
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
    }

    private boolean needFinalSteps() {
        return true;
    }

    private Long spawnEnemy(EnemyType enemyType, int skinId) {
        long res = -1;

        try {
            Enemy enemy = getMap().addEnemyByTypeNew(enemyType, getMap().createMathEnemy(enemyType), skinId, -1,
                    false, true, false);
            if (enemy != null) {
                long startTime = enemy.getTrajectory().getPoints().get(0).getTime();
                long endTime = enemy.getTrajectory().getLastPoint().getTime();
                getLog().debug("spawnEnemy, new enemy: {}", enemy);
                getLog().debug("spawnEnemy, new startTime: {}, endTime: {}, diff {} ",
                        startTime, endTime, (endTime - startTime));
                res = enemy.getId();
                gameRoom.sendNewEnemyMessage(enemy);
            }
        } catch (Exception e) {
            getLog().debug("spawn enemy error type: {}, skinId: {}", enemyType, skinId, e);
        }
        return res;
    }

    private Long spawnEnemy(EnemyRange range, int skinId, Trajectory oldTrajectory, long parentEnemyId, long respawnDelay) {
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

            Enemy enemy = getMap().addEnemyByTypeNew(enemyType, getMap().createMathEnemy(enemyType), skinId, parentEnemyId, false,
                    needFinalSteps(), false);
            if (enemy != null) {
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
            }
        } catch (Exception e) {
            getLog().debug("spawn enemy error , range: {}, skinId: {}, oldTrajectory: {}", range, skinId, oldTrajectory);
            getLog().debug("spawn enemy error: ", e);
        }
        return res;
    }

    private void spawnBirds(EnemyType enemyType) {
        try {
            getLog().debug("spawnBirds enemyType: {}", enemyType);
            SwarmType swarmType = enemyType.equals(ENEMY_9) ? WHITE_BIRDS : RED_BIRDS;

            double speed = enemyType.getSkin(1).getSpeed() * (1 + 0.35 * RNG.rand());
            List<Offset> offsets = new ArrayList<>();
            offsets.add(new Offset(0, 4, 150));
            if (WHITE_BIRDS.equals(swarmType)) {
                offsets.add(new Offset(0, -4, 100));
                offsets.add(new Offset(-4, 0, 70));
                offsets.add(new Offset(4, 0, 40));
            }
            List<Trajectory> trajectories = getMap().generateBirdsTrajectories(speed, offsets);
            if (trajectories != null) {
                int swarmId = getMap().generateSwarmId();
                List<Enemy> enemies = new ArrayList<>();
                for (Trajectory trajectory : trajectories) {
                    Enemy enemy = getMap().addItem(enemyType, 1, trajectory, (float) speed, getMap().createMathEnemy(enemyType), -1);
                    enemy.addToSwarm(swarmType, swarmId);
                    enemies.add(enemy);
                }
                getMap().registerSwarm(swarmId, enemies);

                gameRoom.sendNewEnemiesMessage(enemies);
            }
        } catch (Exception e) {
            getLog().debug("spawnBirds error: ", e);
        }
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

            EnemyBoss enemy = (EnemyBoss) getMap().addEnemyByTypeNew(EnemyType.Boss, mathEnemy,
                    bossSkinId, -1, false, needFinalSteps(), false);
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
            } else if (paidSpecialShot && seat.getAmmoAmount() < multiplierPaidWeapons) {
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
                    }
                } else if (isInternalShot || seat.getCurrentWeapon().getShots() > 0 || paidSpecialShot) {
                    seat.setActualShot(shot);
                    shootWithSpecialWeapon(time, seat, shot);
                    roundInfo.addRealShotsCounter(seat.getCurrentWeaponId(), 1);
                    if (roundInfo.isShotSuccess(seat.getCurrentWeaponId(), numberOfKilledMissOld)) {
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
            if (lock) {
                lockShots.unlock();
            }
        }
    }

    @Override
    protected void shootWithRegularWeapon(long time, Seat seat, IShot shot) throws CommonException {
        getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(), shot.getWeaponId(), 0);
        ShotMessages messages = new ShotMessages(seat, shot, gameRoom,
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0),
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0));
        List<ShootResult> results = new LinkedList<>();

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
                int enemyTypeId = enemy.getEnemyClass().getEnemyType().getId();
                getLog().debug("shootWithRegularWeapon Base enemy: {} enemyTypeId: {}", enemy, enemyTypeId);

                ShootResult result = shootWithRegularWeaponAndUpdateState(time, seat, shot.getEnemyId(), messages);
                result.setMainShot(true);
                results.add(result);

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
            getLog().debug("possible error logic, nNearestEnemies.size(): {} damages.size(): {} liveEnemies: {}",
                    nNearestEnemies.size(), (int) numberDamages, liveEnemies);
        }

        int realNumberOfShots = 0;
        for (Long enemyId : nNearestEnemies.keySet()) {
            ShootResult res = shootToOneEnemy(time, seat, enemyId, weaponId, false,
                    1);
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
            getLog().debug("before compensation realNumberOfShots: {}, numberDamages: {}, isBonusSession: {}",
                    realNumberOfShots, numberDamages, isBonusSession);
            if (!isBonusSession)
                makeCompensationForPoorPlaying(seat, weaponId, numberDamages, realNumberOfShots, shot.isPaidSpecialShot());
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
                getLog().debug("found shot results without main enemy, shot: {}, results: {}", shot, results);
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
                    getLog().debug("decrementAmmoAmount Base enemy: shot.getWeaponId(): {} multiplierPaidWeapons: {}",
                            shot.getWeaponId(), multiplierPaidWeapons);
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
            ShotCalculator.updateWeaponSurplus(weaponId, newCompensation, weaponSurplus, getTOFactoryService());
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

        Map<Integer, List<IWinPrize>> hitResultBySeats = new HashMap<>();

        boolean mainShot = result.isMainShot();
        boolean isWrongResult = result.isKilledMiss() || result.isInvulnerable();
        getLog().debug("result.isMainShot(): {}, isWrongResult: {}", mainShot, isWrongResult);
        if (mainShot && !isWrongResult) {
            boolean newKeyOnShot = !ShotCalculator.getQuestKey(seat).isEmpty();
            getLog().debug("Quest newKeyOnShot: {}", newKeyOnShot);
            if (newKeyOnShot) {
                updateHitResultBySeats(seat.getNumber(), Money.ZERO, result.getPrize(),
                        hitResultBySeats, -1, new ArrayList<>(), null);
                processQuests(seat, result);
            }
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
                            getLog().debug("enemy shared Win: seat accountId: {} enemyWinForSeat: {} seatId: {} additionalWins: {}",
                                    seatCurrent.getAccountId(), enemyWinForSeat, seatCurrent.getId(), additionalWins);
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
                            hitOwn.setKillBonusPay(killAwardWin.toDoubleCents());
                            hitOwn.setBetLevel(betLevel);
                            hitOwn.setPaidSpecialShot(shot.isPaidSpecialShot());
                            getLog().debug("isSpecialWeapon {}, isLastResult: {}, stake: {}, title: {}", isSpecialWeapon,
                                    isLastResult, seat.getStake(), title);

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
                        result.isInstanceKill(), result.getChMult(), enemyId, shot.getEnemyId());


                hitForObservers.setKillBonusPay(killAwardWin.toDoubleCents());
                hitForObservers.setBetLevel(seat.getBetLevel());
                hitForObservers.setPaidSpecialShot(shot.isPaidSpecialShot());
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

    }

    protected void processQuests(Seat seat, IShootResult result) {
        IPlayerQuests playerQuests = seat.getPlayerInfo().getPlayerQuests();
        getLog().debug("old player quests: {}", playerQuests.getQuests());
        processingQuestKey(1, seat, result.getEnemyId());
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

        getLog().debug("prizeId: {}, questsWithPrize: {}", prizeId, questsWithPrize);

        if (questsWithPrize.isEmpty()) {
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
        getLog().debug(" quest: {}", quest);

        List<ITreasureProgress> treasures = quest.getProgress().getTreasures();
        ITreasureProgress progress = treasures.stream().filter(
                treasureProgress -> treasureProgress.getTreasureId() == prizeId).findFirst().get();
        progress.setCollect(progress.getCollect() + 1);

        long countUnfinished = treasures.stream().filter(treasureProgress ->
                treasureProgress.getCollect() < treasureProgress.getGoal()).count();

        boolean needFinish = countUnfinished == 0;
        long winInCents = 0;
        int idxBox = -1;
        if (needFinish) {
            getLog().debug("need finish quest : {}", quest.getName());

            int amount = quest.getQuestPrize().getAmount().getFrom();
            Treasure randomNumberKeyFromMapWithNorm = GameTools.getRandomNumberKeyFromMapWithNorm(MathQuestData.questsKeysWeights);
            idxBox = randomNumberKeyFromMapWithNorm.ordinal();
            Money win = seat.getStake().getWithMultiplier(MathQuestData.questsCacheWins.get(idxBox + 1));
            getLog().debug(" pay : {} win: {}", amount, win);

            IPlayerRoundInfo currentPlayerRoundInfo = seat.getCurrentPlayerRoundInfo();
            seat.setQuestsPayouts(seat.getQuestsPayouts() + win.toCents());
            getLog().debug("quest win add to roundWin: {}", win);
            seat.incrementRoundWin(win);
            winInCents = win.toCents();
            currentPlayerRoundInfo.updateQuestCompletedTotalData(win, idxBox, 0);
            quest.setNeedReset(true);
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
                    seat.getNumber(), enemyId, winInCents, idxBox));

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
        getLog().debug("PlayerId: {}, isBot: {} itemIdForShot for shot: {} enemyType: {}",
                seat.getPlayerInfo().getId(), isBot, itemIdForShot, enemy.getEnemyType().name());

        ShootResult shootResult = gameRoom.getGame().doShoot(enemy, seat, stake, subround.equals(PlaySubround.BOSS),
                isNearLandMine, damageMultiplier, getTOFactoryService());

        getLog().debug("shootResult: {}", shootResult);

        if (!isBot && shootResult.isBossShouldBeAppeared() && !isBossRound() && allowSpawn) {
            totalCountMainBossAppeared++;
            getRemainingBosses().add(shootResult.getBossSkinId());
            getLog().debug("Boss will be appeared later, remainingNumberOfBoss: {} totalCountMainBossAppeared: {} getRemainingBosses: {}",
                    remainingNumberOfBoss, totalCountMainBossAppeared, getRemainingBosses());
        }

        if (shootResult.isDestroyed()) {
            getLog().debug("enemy {} is killed", shootResult.getEnemyId());
            map.removeItem(shootResult.getEnemyId());
            int enemyTypeId = shootResult.getEnemy().getEnemyClass().getEnemyType().getId();
            getLeaveAndDestroyEnemiesTime().put(enemyTypeId, time);
            getLog().debug("shootToOneEnemy, count of enemies after: {}", map.getItemsSize());
            getLog().debug("getCountRemainingEnemiesByModel: {}", getCountRemainingEnemiesByModel());
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
    protected void sendUpdateTrajectories(boolean needFinalSteps) {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        Map<Long, Trajectory> updateTrajectories = gameRoom.getMap().generateUpdateTrajectories(needFinalSteps);
        if (!updateTrajectories.isEmpty()) {
            getLog().debug("updateTrajectories: {}", updateTrajectories);
            updateTrajectories.forEach((id, trajectory) ->
                    trajectories.put(id, gameRoom.convertFullTrajectory(trajectory)));
            gameRoom.sendChanges(getTOFactoryService().createUpdateTrajectories(System.currentTimeMillis(), SERVER_RID,
                    trajectories, 0, EnemyAnimation.NO_ANIMATION.getAnimationId()));
        }
    }

    @Override
    protected PlayGameState getDeserializer() {
        return this;
    }
}
