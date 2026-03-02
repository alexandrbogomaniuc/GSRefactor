package com.betsoft.casino.mp.bgdragonstone.model;

import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyRange;
import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.bgdragonstone.model.math.MathData;
import com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.bgdragonstone.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.teststand.TestStandFeature;
import com.betsoft.casino.teststand.TestStandLocal;
import com.betsoft.casino.teststand.TeststandConst;
import com.betsoft.casino.utils.TInboundObject;
import com.betsoft.casino.utils.TObject;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.kynosarges.tektosyne.geometry.PointD;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static com.betsoft.casino.mp.bgdragonstone.model.SwarmLimits.getLimit;
import static com.betsoft.casino.mp.bgdragonstone.model.SwarmType.*;
import static com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType.*;
import static com.betsoft.casino.mp.bgdragonstone.model.math.MathData.TURRET_WEAPON_ID;
import static com.betsoft.casino.mp.model.EnemyDestroyReason.REMOVED_ON_SERVER;
import static com.betsoft.casino.mp.model.EnemyDestroyReason.SIMPLE_SHOT;
import static com.betsoft.casino.mp.model.PlaySubround.BOSS;
import static com.betsoft.casino.mp.utils.ErrorCodes.*;
import static com.betsoft.casino.utils.TObject.SERVER_RID;

@SuppressWarnings({"Duplicates", "unchecked"})
public class PlayGameState extends AbstractBattlegroundPlayGameState<GameRoom, Seat, GameMap, PlayGameState> {
    private static final long SPECTER_SPAWN_TIME = 6000;
    private static final long DRAGON_DEATH_ANIMATION_TIME = 5000;
    private static final long SWARM_TYPE_SPAWN_COOLDOWN = 1500L;
    private static final int MAX_ALIVE_SINGLE_ENEMIES = 15;
    private static final int MAX_ALIVE_SWARM_ENEMIES = 35;
    private transient boolean needImmediatelySpawn = false;
    private transient long timeOfRoundFinishSoon;
    private transient long timeForKillingAllEnemiesBeforeBoss;
    private transient long timeBeforeSpawnEnemiesAfterBoss;
    private transient long timeBeforeNextWizardSpawn;
    private transient long timeBeforeNextSpecterSpawn;
    private transient long timeOfLastLargeEnemySpawn;
    private transient Map<EnemyType, Integer> largeEnemiesLives;
    private transient Map<SwarmType, Long> lastSpawnTimesForSwarmTypes;
    private transient long timeOfLastSentBoard;

    public PlayGameState() {}

    public PlayGameState(GameRoom gameRoom) {
        super(gameRoom, null);
    }

    @Override
    public void init() throws CommonException {
        super.init();
        resetSeatData();
        getRoom().bossNumberShots = 0;
    }

    @Override
    protected int getMaxAliveEnemies() {
        return MAX_ALIVE_SINGLE_ENEMIES;
    }

    @Override
    protected int getMaxAliveCritters() {
        return MAX_ALIVE_SWARM_ENEMIES;
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

        if (PlaySubround.BASE.equals(subround)) {
            GameMap map = getMap();

            boolean canProcessAfterKillingBoss = System.currentTimeMillis() > timeBeforeSpawnEnemiesAfterBoss;
            if (needWaitingWhenEnemiesLeave && System.currentTimeMillis() > timeOfRoundFinishSoon + 2000
                    && canProcessAfterKillingBoss
            ) {
                destroyBaseEnemies();
                getLog().debug("no live enemies in room, finish by timeOfRoundFinishSoon");
                nextSubRound();
                return;
            }

            int collectToSpawn = gameRoom.getGame().getConfig(gameRoom.getId()).getFragments().getCollectToSpawn();
            if (map.getDragonStoneFragments() >= collectToSpawn && !isManualGenerationEnemies()) {
                long time = System.currentTimeMillis();
                switchSubround(BOSS);
                map.resetDragonStoneFragments();
                int duration = generateBossRoundDuration();
                timeForKillingAllEnemiesBeforeBoss = System.currentTimeMillis() + 3700;
                spawnBoss(time, duration);
            } else {
                if (canProcessAfterKillingBoss) {
                    try {
                        generateEnemies();
                    } catch (Exception e) {
                        getLog().debug("generateEnemies error: ", e);
                    }
                    timeBeforeSpawnEnemiesAfterBoss = 0;
                }
            }
        } else {
            if (isBossRound() && System.currentTimeMillis() > timeForKillingAllEnemiesBeforeBoss) {
                destroyBaseEnemies();
                timeForKillingAllEnemiesBeforeBoss = Long.MAX_VALUE;
                getLog().debug("destroyBaseEnemies for base round.");
            } else if (getMap().updateBossRound()) {
                switchSubround(PlaySubround.BASE);
            }
        }
    }

    private void switchSubround(PlaySubround subround) {
        this.subround = subround;
        getLog().debug("reset boss data for all seats");
        for (Seat seat_ : gameRoom.getSeats()) {
            if (seat_ != null) {
                seat_.resetTotalBossPayout();
            }
        }
        gameRoom.sendChanges(getTOFactoryService()
                .createChangeMap(getCurrentTime(), gameRoom.getMapId(), subround.name()));
    }

    private void shiftAllTrajectoriesAndMakeInvulnerable(long time, long duration) {
        sendConvertedTrajectories(time, getMap().shiftAllTrajectoriesAndMakeInvulnerable(time, duration));
    }

    private void correctTrajectoriesAfterBossKill(IEnemy<EnemyClass, Enemy> boss, long deathTime) {
        long leaveTime = boss.getTrajectory().getPoints().get(2).getTime();
        if (leaveTime > deathTime + DRAGON_DEATH_ANIMATION_TIME) {
            Map<Long, Trajectory> trajectories = getMap()
                    .unshiftTrajectories(deathTime + DRAGON_DEATH_ANIMATION_TIME, leaveTime - deathTime - DRAGON_DEATH_ANIMATION_TIME);
            sendConvertedTrajectories(deathTime, trajectories);
        }
    }

    private void sendConvertedTrajectories(long time, Map<Long, Trajectory> trajectories) {
        Map<Long, Trajectory> convertedTrajectories = new HashMap<>();
        if (!trajectories.isEmpty()) {
            trajectories.forEach((id, trajectory) ->
                    convertedTrajectories.put(id, gameRoom.convertFullTrajectory(trajectory)));
            gameRoom.sendChanges(getTOFactoryService()
                    .createUpdateTrajectories(time, SERVER_RID, convertedTrajectories));
        }
    }

    private int generateBossRoundDuration() {
        return RNG.nextInt(16, 30) * 1000;
    }

    private void generateEnemies() {
        GameConfig config = getRoom().getGame().getConfig(getRoom().getId());
        SpawnConfig spawnConfig = getRoom().getGame().getSpawnConfig(getRoom().getId());

        GameMap map = getMap();
        map.update();

        checkTestStandFeatures();
        getMap().checkFreezeTimeEnemies(FREEZE_TIME_MAX);

        int aliveSingleEnemies = 0;
        int aliveSwarmEnemies = 0;
        List<Pair<Integer, Boolean>> itemsTypeIdsAndSwarmState = map.getItemsTypeIdsAndSwarmState();

        if (isNeedMinimalEnemies() && itemsTypeIdsAndSwarmState.size() > 3) {
            return;
        }

        for (Pair<Integer, Boolean> pair : itemsTypeIdsAndSwarmState) {
            if (pair.getValue()) {
                aliveSwarmEnemies++;
            } else {
                aliveSingleEnemies++;
            }
        }

        if (allowSpawn && startRoundTime != 0 && !isManualGenerationEnemies()) {
            Map<Integer, Integer> counter = map.countEnemyTypes();
            long time = System.currentTimeMillis();
            if (aliveSingleEnemies < spawnConfig.getSingleEnemiesMax() || isNeedImmediatelySpawn()) {
                int wizardsCount = countWizards(counter);
                int spectersCount = countSpecters(counter);
                if (timeOfLastLargeEnemySpawn + 20000 < time) {
                    if (config.isEnemyEnabled(CERBERUS) && time - startRoundTime > spawnConfig.getTimeOffset(CERBERUS)
                            && map.getRemoveTime(CERBERUS) + spawnConfig.getWaitTime(CERBERUS) < time
                            && getLargeEnemiesLives().get(CERBERUS) > 0 && shouldSpawn(counter, CERBERUS)) {
                        spawnCerberus();
                    } else if (config.isEnemyEnabled(DARK_KNIGHT) && time - startRoundTime > spawnConfig.getTimeOffset(DARK_KNIGHT)
                            && map.getRemoveTime(DARK_KNIGHT) + spawnConfig.getWaitTime(DARK_KNIGHT) < time
                            && getLargeEnemiesLives().get(DARK_KNIGHT) > 0 && shouldSpawn(counter, DARK_KNIGHT)) {
                        spawnEnemy(DARK_KNIGHT, 1);
                    } else if (config.isEnemyEnabled(OGRE) && time - startRoundTime > spawnConfig.getTimeOffset(OGRE)
                            && map.getRemoveTime(OGRE) + spawnConfig.getWaitTime(OGRE) < time
                            && getLargeEnemiesLives().get(OGRE) > 0 && shouldSpawn(counter, OGRE)) {
                        spawnEnemy(OGRE, 1);
                    }
                }
                if (!map.getMapShape().getPoints(GameMap.WIZARD_POINTS).isEmpty() && wizardsCount < 1
                        && map.getRemoveTime(EnemyRange.WIZARDS) + timeBeforeNextWizardSpawn < time) {
                    int wizardProbability = RNG.nextInt(5);
                    if (config.isEnemyEnabled(RED_WIZARD) && (wizardProbability == 0 || wizardProbability == 1)
                            && time - startRoundTime > spawnConfig.getTimeOffset(RED_WIZARD)) {
                        spawnWizard(RED_WIZARD, 1, spawnConfig);
                    } else if (config.isEnemyEnabled(BLUE_WIZARD) && (wizardProbability == 2 || wizardProbability == 3)
                            && time - startRoundTime > spawnConfig.getTimeOffset(BLUE_WIZARD)) {
                        spawnWizard(BLUE_WIZARD, 1, spawnConfig);
                    } else if (config.isEnemyEnabled(PURPLE_WIZARD) && wizardProbability == 4
                            && time - startRoundTime > spawnConfig.getTimeOffset(PURPLE_WIZARD)) {
                        spawnWizard(PURPLE_WIZARD, 1, spawnConfig);
                    }
                }
                if (!map.getMapShape().getPoints(GameMap.SPECTER_POINTS).isEmpty() && spectersCount < 1
                        && map.getRemoveTime(EnemyRange.SPECTERS) + timeBeforeNextSpecterSpawn < time) {
                    int specterProbability = RNG.nextInt(6);
                    if (config.isEnemyEnabled(SPIRIT_SPECTER) && (specterProbability == 0 || specterProbability == 1
                            || specterProbability == 2)
                            && time - startRoundTime > spawnConfig.getTimeOffset(SPIRIT_SPECTER)) {
                        spawnSpecter(SPIRIT_SPECTER, spawnConfig);
                    } else if (config.isEnemyEnabled(FIRE_SPECTER) && (specterProbability == 3 || specterProbability == 4)
                            && time - startRoundTime > spawnConfig.getTimeOffset(FIRE_SPECTER)) {
                        spawnSpecter(FIRE_SPECTER, spawnConfig);
                    } else if (config.isEnemyEnabled(LIGHTNING_SPECTER) && specterProbability == 5
                            && time - startRoundTime > spawnConfig.getTimeOffset(LIGHTNING_SPECTER)) {
                        spawnSpecter(LIGHTNING_SPECTER, spawnConfig);
                    }
                }
                if (config.isEnemyEnabled(GARGOYLE) && time - startRoundTime > spawnConfig.getTimeOffset(GARGOYLE)
                        && map.getRemoveTime(GARGOYLE) + spawnConfig.getWaitTime(GARGOYLE) < time
                        && (spawnConfig.isUnconditionalRespawn(GARGOYLE) || shouldSpawn(counter, GARGOYLE))) {
                    spawnEnemy(GARGOYLE, 1);
                }
            }

            switch (RNG.nextInt(6)) {
                case 0:
                    spawnGroupsBySwarmType(config, spawnConfig, BATS);
                    break;
                case 1:
                    spawnGroupsBySwarmType(config, spawnConfig, SKELETONS);
                    break;
                case 2:
                    spawnGroupsBySwarmType(config, spawnConfig, EMPTY_ARMORS);
                    break;
                case 3:
                    if (shouldSpawn(counter, ORC)) {
                        spawnGroupsBySwarmType(config, spawnConfig, ORC_PLATOON);
                    }
                    break;
                case 4:
                    spawnGroupsBySwarmType(config, spawnConfig, IMPS);
                    break;
                default:
                    if (RNG.nextBoolean()) {
                        spawnGoblins(config, spawnConfig);
                    } else {
                        spawnRatsOrSpiders(config, spawnConfig);
                    }
            }

        }
    }

    private boolean shouldSpawn(Map<Integer, Integer> counter, EnemyType enemyType) {
        return !counter.containsKey(enemyType.getId());
    }

    private boolean shouldSpawn(Map<Integer, Integer> counter, EnemyType... enemyTypes) {
        for (EnemyType enemyType : enemyTypes) {
            if (counter.containsKey(enemyType.getId())) {
                return false;
            }
        }
        return RNG.nextInt(10) == 0;
    }

    private int countWizards(Map<Integer, Integer> counter) {
        return counter.getOrDefault(RED_WIZARD.getId(), 0) +
                counter.getOrDefault(BLUE_WIZARD.getId(), 0) +
                counter.getOrDefault(PURPLE_WIZARD.getId(), 0);
    }

    private int countSpecters(Map<Integer, Integer> counter) {
        return counter.getOrDefault(LIGHTNING_SPECTER.getId(), 0) +
                counter.getOrDefault(SPIRIT_SPECTER.getId(), 0) +
                counter.getOrDefault(FIRE_SPECTER.getId(), 0);
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
            gameRoom.sendChanges(getTOFactoryService().createRoundFinishSoon(System.currentTimeMillis()));
            destroyBaseEnemies();
            if(!isBossRound()) {
                getMap().removeAllEnemies();
            }
            gameRoom.clearAllBullets();
        }
    }

    private void destroyBaseEnemies() {
        long time = System.currentTimeMillis();
        List<Long> ids = getMap().removeBaseEnemiesAndGetIds();
        for (Long id : ids) {
            gameRoom.sendChanges(getTOFactoryService().createEnemyDestroyed(time, SERVER_RID, id, REMOVED_ON_SERVER.ordinal()));
        }
    }

    boolean noAnyEnemiesInRound() {
        return getMap().noEnemiesInRoom() && getCountRemainingEnemiesByModel() == 0
                && getMap().getNumberInactivityItems() == 0;
    }

    private void spawnSwarm(GameConfig config, SpawnConfig spawnConfig) {
        GameMap map = getMap();
        List<Enemy> swarm = new ArrayList<>();
        switch (RNG.nextInt(3)) {
            case 0:
                if (config.isEnemyEnabled(GOBLIN) && map.swarmCount(GOBLINS) < getLimit(GOBLINS)) {
                    swarm = getMap().spawnAllGoblinTricksSwarms(config, spawnConfig, startRoundTime);
                }
                break;
            case 1:
                if (map.swarmCount(ANGLE_SPIDERS) < getLimit(ANGLE_SPIDERS)) {
                    swarm = getMap().spawnSpidersWithAngleTrajectory(config);
                }
                break;
            default:
                swarm = spawnSwarmWithRandomParams(config, spawnConfig);
        }
        gameRoom.sendNewEnemiesMessage(swarm);
    }

    private void spawnGroupsBySwarmType(GameConfig config, SpawnConfig spawnConfig, SwarmType swarmType) {
        long currentTime = System.currentTimeMillis();
        if (swarmType != null && currentTime - getLastSpawnTimeForSwarmType(swarmType) > SWARM_TYPE_SPAWN_COOLDOWN) {
            gameRoom.sendNewEnemiesMessage(getMap().spawnAllGroupsBySwarmType(config, spawnConfig, swarmType, startRoundTime));
            addLastSpawnTimeForSwarmType(swarmType, currentTime);
        }
    }

    private void spawnGoblins(GameConfig config, SpawnConfig spawnConfig) {
        gameRoom.sendNewEnemiesMessage(getMap().spawnAllGoblinTricksSwarms(config, spawnConfig, startRoundTime));
    }

    private void spawnRatsOrSpiders(GameConfig config, SpawnConfig spawnConfig) {
        gameRoom.sendNewEnemiesMessage(spawnSwarmWithRandomParams(config, spawnConfig));
    }

    private List<Enemy> spawnSwarmWithRandomParams(GameConfig config, SpawnConfig spawnConfig) {
        GameMap map = getMap();
        List<Enemy> enemies = new ArrayList<>();
        List<SwarmParams> swarmParams = map.getSwarmParams();
        if (swarmParams == null) {
            return Collections.emptyList();
        }
        Collections.shuffle(swarmParams);
        for (SwarmParams params : swarmParams) {
            if (map.getAliveSwarmEnemies() + enemies.size() > spawnConfig.getSwarmEnemiesMax()) {
                return enemies;
            }
            if (params != null) {
                int swarmId = params.getId();
                if (swarmId > 500 && swarmId < 600 && map.swarmCount(RATS) < getLimit(RATS)) {
                    enemies.addAll(map.spawnSwarm(config, spawnConfig, params, RATS, startRoundTime));
                } else if (swarmId > 200 && swarmId < 300 && map.swarmCount(SPIDERS) < getLimit(SPIDERS)) {
                    enemies.addAll(map.spawnSwarm(config, spawnConfig, params, SPIDERS, startRoundTime));
                }
            }
        }
        return enemies;
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
            getMap().resetDragonStoneFragments();
            gameRoom.getSeats().forEach(seat -> seat.setBetLevel(gameRoom.getDefaultBetLevel()));
            finish();
        } catch (CommonException e) {
            getLog().error("Unexpected error", e);
        } finally {
            if (lockShots != null && lockShots.isLocked()) {
                lockShots.unlock();
            }
        }
    }

    private void resetSeatData() {
        getMap().resetDragonStoneFragments();
        for (Seat seat : getRoom().getSeats()) {
            if (seat != null) {
                seat.setBetLevel(gameRoom.getDefaultBetLevel());
            }
        }
    }

    @Override
    protected void spawnMummy() {
    }

    private void spawnCerberus() {
        if (!getMap().isEnemyRemoved(CERBERUS)) {
            timeOfLastLargeEnemySpawn = System.currentTimeMillis();
            gameRoom.sendNewEnemyMessage(getMap().spawnCerberus());
        }
    }

    private boolean needFinalSteps() {
        return subround != BOSS;
    }

    private void spawnEnemy(EnemyType enemyType, int skinId) {
        Enemy enemy = getMap().addEnemyByTypeNew(enemyType, null, skinId, -1,
                false, true, false);
        if (enemyType.equals(OGRE)) {
            enemy.setLives(1);
        }
        if (EnemyRange.LARGE_ENEMIES.contains(enemyType)) {
            timeOfLastLargeEnemySpawn = System.currentTimeMillis();
        }
        gameRoom.sendNewEnemyMessage(enemy);
    }


    @Override
    public void spawnEnemyFromTeststand(int typeId, int skinId, Trajectory trajectory, long parentEnemyId) {
//
//        EnemyType enemyType = EnemyType.values()[typeId];
//        Enemy enemy = getMap().addConcreteEnemy(enemyType, skinId, trajectory, gameRoom.getSeatsCount(),
//                null,
//                parentEnemyId, false, needFinalSteps(), false);
//
//        getLog().debug("spawnEnemyFromTeststand enemy: " + enemy);
//        gameRoom.sendNewEnemyMessage(enemy);
    }

    private void spawnBoss(long time, int duration) {
        lockShots.lock();
        try {
            gameRoom.sendNewEnemyMessage(getMap().spawnBoss(time, duration));
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
        if (!isBossRound())
            return;
        int cnt = 1; // default
        if (SpecialWeaponType.Flamethrower.getId() == weaponId) {
            cnt = 3;
        } else if (SpecialWeaponType.Cryogun.getId() == weaponId
                || SpecialWeaponType.ArtilleryStrike.getId() == weaponId) {
            cnt = 2;
        } else if (SpecialWeaponType.Plasma.getId() == weaponId) {
            cnt = 12;
        } else if (SpecialWeaponType.Railgun.getId() == weaponId) {
            cnt = 4;
        }
        gameRoom.bossNumberShots += cnt;
    }

    public void processShot(long time, Seat seat, IShot shot, boolean isInternalShot) throws CommonException {

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
                if(!needWaitingWhenEnemiesLeave) {
                    Set<SeatBullet> bulletsOnMap = seat.getBulletsOnMap();
                    getLog().debug("aid: {}, bulletsOnMap: {}", seat.getAccountId(), bulletsOnMap);
                    if (bulletsOnMap.stream().noneMatch(seatBullet -> seatBullet.getBulletId().equals(bulletId))) {
                        sendError(seat, shot, BAD_REQUEST, "BulletId is not found", shot);
                        return;
                    }
                }
                if (shot.getWeaponId() != -1 && shot.getWeaponId() != SpecialWeaponType.LevelUp.getId()) {
                    sendError(seat, shot, BAD_REQUEST, "Shot with bulletId and wrong weaponId", shot);
                    return;
                }
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

            Enemy itemById = getMap().getItemById(shot.getEnemyId());
            boolean isBoss = false;
            if (itemById != null)
                isBoss = itemById.isBoss();

            long realStakeInCents = Money.BG_STAKE.toCents() * shot.getWeaponId() != SpecialWeaponType.LevelUp.getId()
                    ? gameRoom.getDefaultBetLevel() : seat.getBetLevel();
            getLog().debug(" realStakeInCents: {}", realStakeInCents);

            PlayerRoundInfo roundInfo = seat.getCurrentPlayerRoundInfo();
            int numberOfKilledMissOld = roundInfo.getKilledMissedNumber(seat.getCurrentWeaponId());

            if (seat.getAmmoAmountTotalInRound() == 0 && seat.getPlayerInfo().getRoundBuyInAmount() == 0
                    && !allowWeaponSaveInAllGames && !isPaidShot) {
                getLog().debug("Not enough bullets 1, seat.getRoundBuyInAmount: {} ", seat.getPlayerInfo().getRoundBuyInAmount());
                getLog().debug("Not enough bullets 1 seat: {}", seat);
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets 1", shot);
            } else if ((seat.getAmmoAmount() <= 0 || (seat.getAmmoAmount() - seat.getBetLevel() < 0)) && isPaidShot) {
                sendError(seat, shot, NOT_ENOUGH_BULLETS, "Not enough bullets 2", shot);
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
            } else if (isInternalShot || seat.getCurrentWeapon().getShots() > 0) {
                if (shot.getWeaponId() == SpecialWeaponType.Cryogun.getId()) {
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
                    roundInfo.addKpiInfoSWShotsCount(weaponId, 1, seat.getBetLevel(), false);
                    roundInfo.addKpiInfoFreeShotsCount(1);
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

    private void spawnSpecter(EnemyType specterType, SpawnConfig spawnConfig) {
        gameRoom.sendNewEnemyMessage(getMap().createSpecter(System.currentTimeMillis(), SPECTER_SPAWN_TIME,
                spawnConfig.getStayTime(specterType), specterType));
        timeBeforeNextSpecterSpawn = spawnConfig.getWaitTime(specterType);
    }

    private void spawnWizard(EnemyType wizardType, int skinId, SpawnConfig spawnConfig) {
        gameRoom.sendNewEnemyMessage(getMap().createWizard(wizardType, skinId, spawnConfig.getStayTime(wizardType)));
        timeBeforeNextWizardSpawn = spawnConfig.getWaitTime(wizardType);
    }

    private List<ISpin> convertSpins(Seat seat, IShootResult shootResult) {
        Money bet = Money.BG_STAKE.multiply(seat.getBetLevel());
        List<ISpin> spins = new ArrayList<>();
        for (ISpinResult spinResult : shootResult.getSpinResults()) {
            Money win = bet.multiply(spinResult.getPayment());
            spins.add(getTOFactoryService().createSpin(spinResult.getPositions(), win.toCents()));
        }
        return spins;
    }

    private int getSlotWin(IShootResult shootResult) {
        return shootResult.getSpinResults().stream().mapToInt(ISpinResult::getPayment).sum();
    }

    protected void shootWithSpecialWeapon(long time, Seat seat, IShot shot) throws CommonException {
        ShotMessages messages = new ShotMessages(seat, shot, gameRoom,
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0),
                getTOFactoryService().createShotResponse(time, shot.getRid(), seat.getNumber(),
                        shot.getWeaponId(), 0));

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

        seat.setBetLevel(betLevelOld);

        boolean needLevelUp = false;

        int size = shootResults.size();
        for (ShootResult result : shootResults) {
            size--;
            int awardedWeaponId = !result.isNewWeapon() ? -1 : result.getWeapon().getType().getId();
            if (!result.getAwardedWeapons().isEmpty()) {
                if (result.getAwardedWeapons().stream().anyMatch(wp -> wp.getId() == SpecialWeaponType.LevelUp.getId())) {
                    needLevelUp = true;
                }
                ;
            }
            processShootResult(seat, shot, result, messages, awardedWeaponId, size == 0);
        }

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

        if(isSpecialWeapon && !seat.isAnyWeaponShotAvailable()){
            seat.resetAdditionalTempCounter(EnemyGame.WEAPON_DROPPED_COUNTER);
            getLog().debug("processing shot, reset additional weapon counters for aid: {}", seat.getAccountId());
        }

        messages.send(shot);
    }

    @Override
    protected List<ShootResult> shootWithSpecialWeaponAndUpdateState(long time, Seat seat, IShot shot, int weaponId,
                                                                     ShotMessages messages) throws CommonException {
        List<ShootResult> results = new LinkedList<>();
        GameMap map = getMap();
        Long itemIdForShot = shot.getEnemyId();

        int liveEnemies = map.getItemsSize();

        if (liveEnemies == 0) {
            results.add(new ShootResult(Money.BG_STAKE, Money.INVALID, false, false, null));
            getLog().debug("shootWithSpecialWeaponAndUpdateState: no live enemies, return kill Miss: message");
            return results;
        }

        int numberDamages = MathData.getRandomDamageForWeapon(gameRoom.getGame().getConfig(seat), weaponId);
        getLog().debug("shootWithSpecialWeaponAndUpdateState: numberDamages: {}", numberDamages);

        short seatsCount = gameRoom.getRealSeatsCount();
        long bossId = map.getActiveBossId(time);
        Enemy baseEnemy = map.getItemById(itemIdForShot);
        if (baseEnemy != null) {
            getLog().debug("shootWithSpecialWeaponAndUpdateState: Base enemy: {} enemyTypeId: {}", baseEnemy, baseEnemy.getEnemyType().getId());
            if (bossId != -1 && !baseEnemy.isBoss()) {
                getLog().debug("shootWithSpecialWeaponAndUpdateState: is boss round but enemy is not boss, wrong shot");
                return createErrorResult(seat, baseEnemy, true);
            }
            if (!map.isVisible(time, baseEnemy)) {
                getLog().debug("shootWithSpecialWeaponAndUpdateState: attempt to shoot on non visible target at {}", baseEnemy.getLocation(time));
                return createErrorResult(seat, baseEnemy, true);
            }

            if(needWaitingWhenEnemiesLeave && !isBossRound()){
                getLog().debug("shootWithSpecialWeaponAndUpdateState: Base enemy is invulnerable because needWaitingWhenEnemiesLeave");
                return createErrorResult(seat, baseEnemy, true);
            }

            boolean success = singleShot(time, seat, baseEnemy, weaponId, results, seatsCount, true);
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

        int realNumberOfShots = 0;
        if (bossId == -1) {
            for (Long enemyId : nNearestEnemies.keySet()) {
                Enemy enemy = map.getItemById(enemyId);
                if (enemy != null && singleShot(time, seat, enemy, weaponId, results, seatsCount, false)) {
                    realNumberOfShots++;
                }
            }
        } else {
            long baseBossEnemyId = baseEnemy.getId();
            for (int idx = 0; idx < numberDamages; idx++) {
                Enemy enemy = map.getItemById(baseBossEnemyId);
                if (enemy != null && singleShot(time, seat, enemy, weaponId, results, seatsCount, false)) {
                    realNumberOfShots++;
                }
            }
        }

        getLog().debug("shootWithSpecialWeaponAndUpdateState: realNumberOfShots: {}, numberDamages: {}", realNumberOfShots, numberDamages);
        if (realNumberOfShots < numberDamages) {
            int cnt = 100;
            while (cnt-- > 0) {
                Long nearestEnemy = map.getAllNearestEnemy(time, locationOfBaseEnemy,
                        false, itemIdForShot, null);
                if (nearestEnemy == null) {
                    getLog().debug("shootWithSpecialWeaponAndUpdateState: account: {}, No enemies for shooting from SW", seat.getAccountId());
                    break;
                }
                Enemy enemy = map.getItemById(nearestEnemy);
                if (singleShot(time, seat, enemy, weaponId, results, seatsCount, false)) {
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

        List<ShootResult> rageResults = new ArrayList<>();
        for (ShootResult result : results) {
            if (result.isRage()) {
                processRage(time, result, seat, messages, rageResults);
            }
        }
        results.addAll(rageResults);

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

            if (weaponId == TURRET_WEAPON_ID) {
                getLog().debug("shootWithSpecialWeaponAndUpdateState: decrement ammo for regular weapon with betLevel: {}", betLevel);
                //seat.decrementAmmoAmount(betLevel);
                seat.incrementBulletsFired();
            } else {
                seat.consumeSpecialWeapon(weaponId);
                getLog().debug("shootWithSpecialWeaponAndUpdateState: decrement ammo for levelUp weapon with betLevel: {}", betLevel);
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

    private List<ShootResult> createErrorResult(Seat seat, Enemy enemy, boolean invulnerable) {
        ShootResult result = new ShootResult(Money.BG_STAKE, Money.INVALID,
                false, false, enemy, invulnerable);
        result.setMainShot(true);
        return Collections.singletonList(result);
    }

    private boolean singleShot(long time, Seat seat, Enemy enemy, int weaponId, List<ShootResult> results,
                               int seatsNumber, boolean isMainShot) throws CommonException {
        boolean success = false;
        ShootResult result = shootToOneEnemy(time, seat, enemy, weaponId, seatsNumber, isMainShot);
        if (!result.getWin().equals(Money.INVALID)) {
            results.add(result);
            success = true;
        }
        return success;
    }

    private void processRage(long time, ShootResult baseResult, Seat seat, ShotMessages messages,
                             List<ShootResult> results) throws CommonException {
        List<IDamage> rageTargets = new ArrayList<>();
        IEnemy baseEnemy = baseResult.getEnemy();
        List<Integer> ragePayouts = baseResult.getGems();
        int totalDamage = ragePayouts.stream()
                .mapToInt(Integer::intValue)
                .sum();
        Map<Long, Double> nearestEnemies = getMap()
                .getRageTargets(time, baseEnemy.getLocation(time), baseEnemy.getId(), ragePayouts.size());
        long awardedDamage = 0;
        getLog().debug("processRage, totalDamage: {}, ragePayouts{}, nearestEnemies: {}",
                totalDamage, ragePayouts, nearestEnemies);

        if (!nearestEnemies.isEmpty()) {
            int i = 0;
            for (Map.Entry<Long, Double> enemyPair : nearestEnemies.entrySet()) {
                int damage = ragePayouts.get(i++);
                Enemy enemy = getMap().getItemById(enemyPair.getKey());
                if (enemy != null) {
                    awardedDamage += hitEnemyWithRageDamage(enemy, seat, damage, rageTargets, results);
                } else {
                    getLog().error("Rage target is already destroyed: {}", enemyPair.getKey());
                }
            }
            baseResult.setRageTargets(rageTargets);
        }

        if (awardedDamage > totalDamage) {
            getLog().error("Awarded damage is greater than expected: {}/{}", awardedDamage, totalDamage);
            throw new CommonException("Invalid Rage results");
        }
    }

    private long hitEnemyWithRageDamage(Enemy enemy, Seat seat, int damage, List<IDamage> rageTargets,
                                        List<ShootResult> results) {
        ShootResult result = gameRoom.getGame().doRageDamage(enemy, seat, damage, Money.BG_STAKE, seat.getBetLevel());
        checkEnemyKilled(result);
        rageTargets.add(getTOFactoryService().createRageDamage(enemy.getId(), damage));
        results.add(result);
        return damage;
    }


    @Override
    protected void processShootResult(Seat seat, IShot shot, IShootResult result, ShotMessages messages,
                                      int awardedWeaponId, boolean isLastResult) {

        long enemyId = result.getEnemy() == null ? shot.getEnemyId() : result.getEnemyId();
        getLog().debug("processing result: {}, enemyId: {}", result, enemyId);

        int realShotTypeId = shot.getWeaponId();
        int usedSpecialWeapon = shot.getWeaponId();
        seat.getCurrentPlayerRoundInfo().updateAdditionalData(currentModel);

        Integer fragmentId = PlaySubround.BASE.equals(subround) && result.isBossShouldBeAppeared()
                ? getMap().collectDragonStoneFragment(getRoom().getGame().getConfig(seat).getFragments().getCollectToSpawn())
                : null;

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
        boolean isSlot = isSlotsResultsExist(result);
        boolean isRage = result.isRage();

        boolean isHit = isPrize || isWin || isWeapon || isBossWin || isAwardedWeapons || isSlot || isRage;
        getLog().debug("isHit: {}, isPrize: {}, isWin: {}, isWeapon: {}, isBossWin: {}, isAwardedWeapons: {}, isSlot: {}",
                isHit, isPrize, isWin, isWeapon, isBossWin, isAwardedWeapons, isSlot);

        Map<Integer, List<IWinPrize>> hitResultBySeats = new HashMap<>();
        Map<Seat, IHit> messagesForSeatsLocal = new HashMap<>();
        IHit hitOwn = null;
        List<IHit> hitsForObserversLocal = new ArrayList<>();
        List<ISpin> spins = isSlot ? convertSpins(seat, result) : null;

        Money stake = (isSpecialWeapon || !mainShot) ? Money.ZERO : Money.BG_STAKE.getWithMultiplier(seat.getBetLevel());


        getLog().debug("real stake: {}", stake.toDoubleCents());

        if (result.isKilledMiss() || result.isInvulnerable()) {
            seat.incrementMissCount();
            seat.getCurrentPlayerRoundInfo().addKilledMissCounter(shot.getWeaponId(), 1);

            messages.add(
                    getTOFactoryService().createMiss(getCurrentTime(), TObject.SERVER_RID,
                            seat.getNumber(), result.isKilledMiss(), awardedWeaponId, enemyId, usedSpecialWeapon,
                            seat.getSpecialWeaponRemaining(), 0, isLastResult, shot.getX(), shot.getY(),
                            newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                            seat.getBetLevel(), fragmentId, result.getEffects(), shot.getBulletId()),
                    getTOFactoryService().createMiss(getCurrentTime(), shot.getRid(),
                            seat.getNumber(), result.isKilledMiss(), awardedWeaponId, enemyId, usedSpecialWeapon,
                            seat.getSpecialWeaponRemaining(), 0, isLastResult, shot.getX(), shot.getY(),
                            newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                            seat.getBetLevel(), fragmentId, result.getEffects(), shot.getBulletId()));

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
                                currentPlayerRoundInfo.updateAdditionalWin("KillAwardWin", killAwardWin);
                                seatCurrent.incrementRoundWin(killAwardWin);
                                seatCurrent.incrementShotTotalWin(killAwardWin);
                                seatCurrent.addLastWin(killAwardWin);
                                totalSimpleWin = totalSimpleWin.add(killAwardWin);
                            }

                            if (isSlot) {
                                int slotTotalWin = getSlotWin(result);
                                currentPlayerRoundInfo.addMoneyWheelCompleted(1);
                                if (slotTotalWin > 0) {
                                    getLog().debug("slotWin: {}", slotTotalWin);
                                    Money slotWin = Money.BG_STAKE.multiply(seat.getBetLevel()).multiply(slotTotalWin);
                                    currentPlayerRoundInfo.updateAdditionalWin("slotWin", slotWin);
                                    // reusing money wheel fields for slot stats
                                    currentPlayerRoundInfo.addMoneyWheelPayouts((long) slotWin.toDoubleCents());
                                    seatCurrent.incrementRoundWin(slotWin);
                                    seatCurrent.incrementShotTotalWin(slotWin);
                                    seatCurrent.addLastWin(slotWin);
                                }
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
                                    hitResultBySeats, -1,
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
                            hitOwn.setFragmentId(fragmentId);
                            hitOwn.setSlot(spins);
                            hitOwn.setEffects(result.getEffects());
                            hitOwn.setBossNumberShots(gameRoom.bossNumberShots);

                            if (isRage) {
                                hitOwn.setRage(result.getRageTargets());
                            }

                            getLog().debug("isSpecialWeapon {}, isLastResult: {}, stake: {}, title: {}, realStake: {}",
                                    isSpecialWeapon, isLastResult, seat.getStake(), title, stake);

                            seat.getCurrentPlayerRoundInfo().updateStatNew(stake, result.isShotToBoss(), isSpecialWeapon,
                                    title, enemyWinForSeat, result.isDestroyed(), enemyNameKey, Money.ZERO);
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
                            hit.setFragmentId(fragmentId);
                            hit.setSlot(spins);
                            hit.setEffects(result.getEffects());
                            hit.setBossNumberShots(gameRoom.bossNumberShots);
                            if (isBossWin)
                                hit.setKillBonusPay(killAwardWin.toDoubleCents());

                            if (isRage) {
                                hit.setRage(result.getRageTargets());
                            }

                            messagesForSeatsLocal.put(seatCurrent, hit);
                        }
                    }
                }

                updateHitResultBySeats(seat.getNumber(), Money.ZERO, null, hitResultBySeats,
                        awardedWeaponId, new ArrayList<>(), null);

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
                hitForObservers.setFragmentId(fragmentId);
                hitForObservers.setSlot(spins);
                hitForObservers.setEffects(result.getEffects());
                hitForObservers.setBossNumberShots(gameRoom.bossNumberShots);
                if (isBossWin)
                    hitForObservers.setKillBonusPay(killAwardWin.toDoubleCents());

                hitsForObserversLocal.add(hitForObservers);


                if (isRage) {
                    hitForObservers.setRage(result.getRageTargets());
                }

                if (result.isDestroyed()) {
                    addEnemyDestroyedMessage(messages, result.getEnemyId(), shot.getRid());

                    if (result.isShotToBoss()) {
                        gameRoom.bossNumberShots = 0;
                        switchSubround(PlaySubround.BASE);
                        if(allowSpawn) {
                            timeBeforeSpawnEnemiesAfterBoss = System.currentTimeMillis() + 9500;
                        }
                        getMap().setBossHP(0);
                        getLog().debug("allow spawn after killing of boss, subround set to BASE");
                        if (!isManualGenerationEnemies())
                            correctTrajectoriesAfterBossKill(result.getEnemy(), getCurrentTime());
                    }
                }

            } else {
                seat.getCurrentPlayerRoundInfo().addMissCounter(seat.getCurrentWeaponId(), 1);

                String title = isSpecialWeapon ? SpecialWeaponType.values()[realShotTypeId].getTitle() : null;
                seat.getCurrentPlayerRoundInfo().updateStatNew(stake, result.isShotToBoss(), isSpecialWeapon,
                        title, Money.ZERO, result.isDestroyed(), enemyNameKey, Money.ZERO);

                seat.incrementMissCount();
                int specialWeaponRemaining = seat.getSpecialWeaponRemaining();
                IMiss allMiss = getTOFactoryService().createMiss(getCurrentTime(), SERVER_RID,
                        seat.getNumber(), false, awardedWeaponId, enemyId, usedSpecialWeapon,
                        specialWeaponRemaining, 0, isLastResult, shot.getX(), shot.getY(),
                        newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                        seat.getBetLevel(), fragmentId, result.getEffects(), shot.getBulletId());
                IMiss ownMessage = getTOFactoryService().createMiss(getCurrentTime(), shot.getRid(),
                        seat.getNumber(), false, awardedWeaponId, enemyId, usedSpecialWeapon,
                        specialWeaponRemaining, 0, isLastResult, shot.getX(), shot.getY(),
                        newShots, result.getMineId(), shot.getEnemyId(), result.isInvulnerable(),
                        seat.getBetLevel(), fragmentId, result.getEffects(), shot.getBulletId());
                if (allMiss != null) {
                    allMiss.setBossNumberShots(gameRoom.bossNumberShots);
                    ownMessage.setBossNumberShots(gameRoom.bossNumberShots);
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

    private boolean isSlotsResultsExist(IShootResult result) {
        List<ISpinResult> spinResults = result.getSpinResults();
        if (spinResults == null || spinResults.isEmpty()) {
            return false;
        }
        for (ISpinResult spinResult : spinResults) {
            if (spinResult.getPayment() != 0) {
                return true;
            }
        }
        return false;
    }

    private void addEnemyDestroyedMessage(ShotMessages messages, long enemyId, int rid) {
        messages.add(
                getTOFactoryService().createEnemyDestroyed(getCurrentTime(), SERVER_RID,
                        enemyId, SIMPLE_SHOT.ordinal()),
                getTOFactoryService().createEnemyDestroyed(getCurrentTime(), rid,
                        enemyId, SIMPLE_SHOT.ordinal()));
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

    public ShootResult shootToOneEnemy(long time, Seat seat, Enemy enemy, int weaponId, int seatsNumber, boolean isMainShot) throws CommonException {
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
                seat.getPlayerInfo().getId(), enemy.getId(), enemy.getEnemyType().name());

        ShootResult shootResult = gameRoom.getGame().doShoot(enemy, seat, stake, getTOFactoryService(), seatsNumber, isMainShot);
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

    public boolean isNeedImmediatelySpawn() {
        return needImmediatelySpawn;
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

    public Map<EnemyType, Integer> getLargeEnemiesLives() {
        if (largeEnemiesLives == null) {
            largeEnemiesLives = new EnumMap<>(EnemyType.class);
            largeEnemiesLives.put(OGRE, 1);
            largeEnemiesLives.put(CERBERUS, RNG.nextInt(1, 3));
            largeEnemiesLives.put(DARK_KNIGHT, RNG.nextInt(1, 4));
        }
        return largeEnemiesLives;
    }

    public Map<SwarmType, Long> getLastSpawnTimesForSwarmTypes() {
        if (lastSpawnTimesForSwarmTypes == null) {
            lastSpawnTimesForSwarmTypes = new EnumMap<>(SwarmType.class);
        }
        return lastSpawnTimesForSwarmTypes;
    }

    public long getLastSpawnTimeForSwarmType(SwarmType swarmType) {
        return getLastSpawnTimesForSwarmTypes().getOrDefault(swarmType, 0L);
    }

    public void addLastSpawnTimeForSwarmType(SwarmType swarmType, long spawnTime) {
        getLastSpawnTimesForSwarmTypes().put(swarmType, spawnTime);
    }

    @Override
    protected PlayGameState getDeserializer() {
        return this;
    }
}
