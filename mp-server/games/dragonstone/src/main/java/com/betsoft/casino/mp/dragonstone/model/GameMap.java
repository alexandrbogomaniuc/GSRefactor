package com.betsoft.casino.mp.dragonstone.model;

import com.betsoft.casino.mp.WizardTrajectoryGenerator;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.scenarios.TypeAndSkin;
import com.betsoft.casino.mp.dragonstone.model.math.EnemyRange;
import com.betsoft.casino.mp.dragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.*;
import com.betsoft.casino.mp.movement.SpiderSwarmAngleTrajectoryGenerator;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Lists;
import com.hazelcast.spring.context.SpringAware;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.dragonstone.model.SwarmType.*;
import static com.betsoft.casino.mp.dragonstone.model.math.EnemyRange.*;
import static com.betsoft.casino.mp.dragonstone.model.math.EnemyType.*;

/**
 * User: flsh
 * Date: 21.09.17.
 */
@SpringAware
public class GameMap extends AbstractGameMap<Enemy, GameMapShape, EnemyRange, EnemyType, GameMap> {
    public static final long FOG_APPEAR_TIME = 7000L;
    protected static final long BOSS_SPAWN_ANIMATION_DURATION = 8500L;
    protected static final long BOSS_LEAVE_ANIMATION_DURATION = 4000L;
    protected static final String SPECTER_POINTS = "1";
    protected static final String WIZARD_POINTS = "2";
    private static final Long FLIP_XY = 1L;
    private EnemyRange possibleEnemies;
    private long minBossReturnTime;
    private int bossHP;
    private int fragments;
    private transient EnumMap<EnemyType, Long> removeTimes;
    private transient Map<Integer, Long> removeTimesForSwarms;

    //empty constructor required for Kryo serialization
    @SuppressWarnings("unused")
    public GameMap() {
        super();
    }

    public GameMap(EnemyRange possibleEnemies, GameMapShape map) {
        super(map);
        this.possibleEnemies = possibleEnemies;
        this.minBossReturnTime = 0;
        this.bossHP = 0;
        this.fragments = 0;
        this.removeTimes = new EnumMap<>(EnemyType.class);
    }

    @Override
    protected EnemyRange getPossibleEnemies() {
        return possibleEnemies;
    }

    void setPossibleEnemies(EnemyRange possibleEnemies) {
        this.possibleEnemies = possibleEnemies;
    }

    @Override
    protected List<EnemyType> getEnemyTypes() {
        return possibleEnemies.getEnemies();
    }

    @Override
    protected boolean isNotBaseEnemy(Enemy enemy) {
        return !EnemyRange.BASE_ENEMIES.contains(enemy.getEnemyType());
    }

    @Override
    protected EnemyType getEnemyByTypeId(int typeId) {
        return EnemyType.values()[typeId];
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Enemy createEnemy(EnemyType enemyType, int skinId, Trajectory trajectory, float speed,
                             IMathEnemy mathEnemy, long parentEnemyId) {
        EnemyClass enemyClass = new EnemyClass(enemyType.getId(), enemyType.getName(), 1, speed, enemyType);
        Enemy enemy = new Enemy(enemyIdsGenerator.getAndIncrement(), enemyClass, skinId, trajectory, mathEnemy,
                parentEnemyId, new ArrayList<>());
        enemy.setAwardedPrizes(new ArrayList<>());
        enemy.setAwardedSum(Money.ZERO);
        enemy.setEnergy(enemyClass.getEnergy());
        enemy.setSpeed(speed);
        enemy.setMovementStrategy(new TrajectoryMovementStrategy(enemy, this));
        return enemy;
    }

    @Override
    protected void writeEnemies(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, items);
        output.writeInt(possibleEnemies.ordinal(), true);
    }

    @Override
    protected void readEnemies(byte version, Kryo kryo, Input input) {
        items.clear();
        List<Enemy> enemies = (List<Enemy>) kryo.readClassAndObject(input);
        items.addAll(enemies);
        for (Enemy enemy : items) {
            IMovementStrategy<Enemy> movementStrategy = enemy.getMovementStrategy();
            movementStrategy.setMap(this);
        }
        int ordinal = input.readInt(true);
        possibleEnemies = EnemyRange.values()[ordinal];
    }

    @Override
    protected void writeAdditionalFields(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, getInactivityLiveItems());
        output.writeLong(minBossReturnTime, true);
        output.writeInt(bossHP, true);
        output.writeInt(fragments, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void readAdditionalFields(byte version, Kryo kryo, Input input) {
        getInactivityLiveItems().clear();
        Map<Enemy, Long> enemies = (Map<Enemy, Long>) kryo.readClassAndObject(input);
        getInactivityLiveItems().putAll(enemies);
        for (Enemy enemy : getInactivityLiveItems().keySet()) {
            IMovementStrategy<? extends IEnemy<?, ?>> movementStrategy = enemy.getMovementStrategy();
            movementStrategy.setMap(this);
        }
        minBossReturnTime = input.readLong(true);
        bossHP = input.readInt(true);
        fragments = input.readInt(true);
    }

    @Override
    protected void serializeEnemies(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        serializeListField(gen, "items", items, new TypeReference<List<Enemy>>() {});
        gen.writeNumberField("possibleEnemiesId", possibleEnemies.ordinal());
    }

    @Override
    protected void deserializeEnemies(JsonParser p, JsonNode node, DeserializationContext ctxt) {
        items.clear();
        List<Enemy> enemies = ((ObjectMapper) p.getCodec()).convertValue(node.get("items"), new TypeReference<List<Enemy>>() {});
        items.addAll(enemies);
        for (Enemy enemy : items) {
            IMovementStrategy movementStrategy = enemy.getMovementStrategy();
            movementStrategy.setMap(this);
        }
        int ordinal = node.get("possibleEnemiesId").intValue();
        possibleEnemies = EnemyRange.values()[ordinal];
    }

    @Override
    protected void serializeAdditionalFields(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        serializeMapField(gen, "inactivityLiveItems", getInactivityLiveItems(), new TypeReference<Map<Enemy, Long>>() {});
        gen.writeNumberField("minBossReturnTime", minBossReturnTime);
        gen.writeNumberField("bossHP", bossHP);
        gen.writeNumberField("fragments", fragments);
    }

    @Override
    protected void deserializeAdditionalFields(JsonParser p, JsonNode node, DeserializationContext ctxt) {
        getInactivityLiveItems().clear();
        Map<Enemy, Long> enemies = ((ObjectMapper) p.getCodec()).convertValue(node.get("inactivityLiveItems"), new TypeReference<Map<Enemy, Long>>() {});
        getInactivityLiveItems().putAll(enemies);
        for (Enemy enemy : getInactivityLiveItems().keySet()) {
            IMovementStrategy<? extends IEnemy<?, ?>> movementStrategy = enemy.getMovementStrategy();
            movementStrategy.setMap(this);
        }
        minBossReturnTime = node.get("minBossReturnTime").asLong();
        bossHP = node.get("bossHP").asInt();
        fragments = node.get("fragments").asInt();
    }

    @Override
    protected GameMap getDeserialized() {
        return this;
    }

    @Override
    protected Trajectory getTrajectory(EnemyType enemyType, float speed, boolean needStandOnPlace, boolean needNearCenter,
                                       int skinId, boolean needFinalSteps) {
        List<Trajectory> trajectories = map.getPredefinedTrajectories(enemyType.getId(), skinId);
        if (trajectories != null && !trajectories.isEmpty()) {
            return prepareTrajectory(System.currentTimeMillis() + 1000, trajectories.get(RNG.nextInt(trajectories.size())), 0, 0, 0, speed);
        }
        switch (enemyType) {
            case ORC:
                return new LargeEnemyTrajectoryGenerator(map, getRandomLargeEnemiesSpawnPoint(), speed)
                        .generateWithDuration(System.currentTimeMillis() + SPAWN_DELAY, getTrajectoryDuration(), needFinalSteps);
            case GARGOYLE:
                return new GargoyleTrajectoryGenerator(map, speed)
                        .generate(System.currentTimeMillis() + SPAWN_DELAY, 1);
            default:
                return super.getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps);
        }
    }

    private ShamanTrajectoryGenerator createWizardTrajectoryGenerator(GameMapShape map, PointI source, double speed,
                                                                      long wizardLifeTime) {
        return new WizardTrajectoryGenerator(map, source, speed, getCoords(), getCurrentWizards())
                .setVisibleArea(50, 100, 910, 490)
                .setInvisibilityStartTime(2045)
                .setTeleportStartTime(2368)
                .setTeleportFinishTime(2369)
                .setInvisibilityFinishTime(2493)
                .setAnimationDuration((int) wizardLifeTime);
    }

    public Enemy createWizard(EnemyType wizardType, int skinId, long wizardLifeTime) {
        int skin = (skinId == -1 ? getRandomSkin(wizardType) : skinId);
        float speed = generateSpeed(wizardType.getSkin(skin));
        Trajectory trajectory = createWizardTrajectoryGenerator(map, new PointI(), speed, wizardLifeTime)
                .generate(System.currentTimeMillis() + SPAWN_DELAY, RNG.nextInt(2, 5), true);
        return addItem(wizardType, 1, trajectory, speed, null, -1);
    }

    private List<AbstractEnemy> getCurrentWizards() {
        List<AbstractEnemy> currentWizards = new ArrayList<>();
        for (Enemy enemy : getItems()) {
            if (WIZARDS.contains(enemy.getEnemyType())) {
                currentWizards.add(enemy);
            }
        }
        return currentWizards;
    }

    @Override
    protected boolean isOgre(IEnemyType enemyType) {
        return false;
    }

    @Override
    public Set<Long> getEnemiesForNewEnemyUpdating(boolean needFinalSteps, boolean needReturnAllEnemies) {
        if (!getInactivityLiveItems().isEmpty())
            clearInactivityLiveItems();
        return new HashSet<>();
    }

    private Trajectory prepareTrajectory(long time, Trajectory template, double offsetX, double offsetY, long offsetTime, float speed) {
        if (FLIP_XY.equals(template.getId())) {
            double temp = offsetX;
            //noinspection SuspiciousNameCombination
            offsetX = offsetY;
            offsetY = temp;
        }

        Trajectory trajectory = new Trajectory(speed);
        Iterator<Point> points = template.getPoints().iterator();
        Point current = points.next();
        trajectory.addPoint(current.getX() + offsetX, current.getY() + offsetY, time + offsetTime);
        while (points.hasNext()) {
            Point next = points.next();
            double dx = current.getX() - next.getX();
            double dy = current.getY() - next.getY();
            time += (long) (Math.sqrt(dx * dx + dy * dy) / (speed / 1000));
            trajectory.addPoint(next.getX() + offsetX, next.getY() + offsetY, time + offsetTime);
            current = next;
        }
        return trajectory;
    }

    @Override
    protected Trajectory generateLeaveTrajectory(ITrajectoryGenerator generator, PointI location, long startTime, Enemy enemy) {
        EnemyType enemyType = enemy.getEnemyType();
        if (WIZARDS.contains(enemyType)) {
            return createWizardTrajectoryGenerator(map, PointI.EMPTY, enemy.getSpeed(), RNG.nextInt(7000, 10000))
                    .generateLeaveTrajectory(startTime, enemy.getTrajectory());
        }
        if (SPECTERS.contains(enemyType) || DRAGON.equals(enemyType)) {
            return null;
        }
        if (GARGOYLE.equals(enemyType)) {
            return cutGargoyleTrajectoryAfterCurrentPhase(startTime, enemy);
        }
        return super.generateLeaveTrajectory(generator, location, startTime, enemy);
    }

    @Override
    public boolean needDoubleSpeed(int enemyTypeId) {
        return enemyTypeId == 3;
    }

    @Override
    public boolean notNeedFreezeAtStartPoint(IEnemyType enemyType) {
        return DRAGON.equals(enemyType);
    }

    @Override
    protected long getBossSpawnAnimationDuration(int skinId) {
        return BOSS_SPAWN_ANIMATION_DURATION;
    }

    @Override
    protected long getBossInvulnerabilityTime(int skinId) {
        return BOSS_INVULNERABILITY_TIME;
    }

    @Override
    protected void addPointsFromOldTrajectory(List<Point> points, long startTime, long freezeTime, PointI location, Enemy enemy) {
        if (WIZARDS.contains(enemy.getEnemyType())) {
            addPointsFromOldTrajectoryWithTeleport(points, startTime, freezeTime, enemy);
        } else {
            List<Point> oldPoints = enemy.getTrajectory().getPoints();
            if (oldPoints.get(1).getTime() > startTime && oldPoints.get(0).isFreezePoint()) {
                freezeTime = points.get(0).getTime() - oldPoints.get(0).getTime();
            }
            for (Point point : oldPoints) {
                if (!point.isFreezePoint() && (point.getTime() > startTime)) {
                    points.add(point.create(point.getX(), point.getY(), point.getTime() + freezeTime));
                }
            }
        }
    }

    void addPointsFromOldTrajectoryWithTeleport(List<Point> points, long startTime, long freezeTime, Enemy enemy) {
        points.clear();
        List<Point> oldPoints = enemy.getTrajectory().getPoints();
        int i = 0;
        int teleportIndex = 0;
        while (i < oldPoints.size() - 1 && oldPoints.get(i).getTime() <= startTime) {
            if (oldPoints.get(i) instanceof TeleportPoint) {
                teleportIndex = i;
            }
            i++;
        }
        for (int j = teleportIndex; j < i; j++) {
            points.add(oldPoints.get(j));
        }
        if (oldPoints.get(i).isFreezePoint() && i > 0 && oldPoints.get(i - 1).isFreezePoint()) {
            freezeTime = freezeTime - Math.max(oldPoints.get(i).getTime() - startTime, 0);
        } else {
            Point point = oldPoints.get(i);
            points.add(new FreezePoint(point.getX(), point.getY(), startTime));
            points.add(new FreezePoint(point.getX(), point.getY(), startTime + freezeTime));
        }
        for (int j = i; j < oldPoints.size(); j++) {
            Point point = oldPoints.get(j);
            points.add(point.create(point.getX(), point.getY(), point.getTime() + freezeTime));
        }
    }

    public SwarmParams getRandomActiveSwarmParams() {
        List<SwarmParams> swarmParams = map.getSwarmParams();
        if (!swarmParams.isEmpty()) {
            SwarmParams params = getRandomElement(swarmParams);
            if (checkCooldown(params.getId())) {
                return params;
            }
        }
        return null;
    }

    public List<Enemy> spawnSwarm(GameConfig config, SpawnConfig spawnConfig, SwarmParams params, SwarmType swarmType,
                                  long startRoundTime) {
        long time = System.currentTimeMillis();
        List<Enemy> enemies = new ArrayList<>();
        int amount = RNG.nextInt(params.getMinSize(), params.getMaxSize() + 1);
        long spawnTime = System.currentTimeMillis() + SPAWN_DELAY;
        scenarioCooldowns.put(params.getId(), spawnTime + params.getCooldown());
        int swarmId = params.getId();
        if (time - startRoundTime > spawnConfig.getSwarmTimeOffset(swarmId)
                && getSwarmRemoveTime(swarmId) + spawnConfig.getSwarmWaitTime(swarmId) < time
                && (shouldSwarmSpawn(swarmId) || spawnConfig.isSwarmUnconditionalRespawn(swarmId))) {
            for (int i = 0; i < amount; i++) {
                TypeAndSkin typeAndSkin = getRandomElement(params.getEnemies());
                EnemyType enemyType = EnemyType.getById(typeAndSkin.getType());
                if (config.isEnemyEnabled(enemyType)) {
                    Skin skin = enemyType.getSkin(typeAndSkin.getSkin());
                    Trajectory trajectory = getSwarmTrajectory(params, spawnTime, skin, enemyType);
                    if (trajectory == null) {
                        return Collections.emptyList();
                    }
                    Enemy enemy = addItem(enemyType, typeAndSkin.getSkin(), trajectory, (float) trajectory.getSpeed(),
                            null, -1);
                    enemy.addToSwarm(swarmType, swarmId);
                    enemy.setEnergy(enemy.getFullEnergy());
                    enemies.add(enemy);
                }
            }
        }
        if (!enemies.isEmpty()) {
            registerSwarm(swarmId, enemies);
        }
        return enemies;
    }

    private Trajectory getSwarmTrajectory(SwarmParams params, long spawnTime, Skin skin, EnemyType enemyType) {
        Trajectory trajectory = null;
        if (BLACK_RAT.equals(enemyType) || BROWN_RAT.equals(enemyType)) {
            trajectory = getRatSwarmTrajectory(params, spawnTime, skin);
        }
        if (BLACK_SPIDER.equals(enemyType) || BROWN_SPIDER.equals(enemyType)) {
            trajectory = getSpiderSwarmTrajectory(params, spawnTime, skin);
        }
        return trajectory;
    }

    public List<Enemy> spawnAllGoblinTricksSwarms(GameConfig config, SpawnConfig spawnConfig, long startRoundTime) {
        List<Enemy> enemies = new ArrayList<>();
        List<SwarmSpawnParams> swarmSpawnParams = getSwarmSpawnParams(GOBLINS);
        if (swarmSpawnParams == null || !config.isEnemyEnabled(GOBLIN)) {
            return Collections.emptyList();
        }
        Collections.shuffle(swarmSpawnParams);
        for (SwarmSpawnParams params : swarmSpawnParams) {
            if (getAliveSwarmEnemies() + enemies.size() > spawnConfig.getSwarmEnemiesMax()) {
                return enemies;
            }
            List<Enemy> swarm = spawnGoblinTricksSwarm(spawnConfig, startRoundTime, params);
            enemies.addAll(swarm);
        }
        return enemies;
    }

    private List<Enemy> spawnGoblinTricksSwarm(SpawnConfig spawnConfig, long startRoundTime,
                                               SwarmSpawnParams params) {
        long time = System.currentTimeMillis();
        int amount;
        amount = RNG.nextInt(5, 8);
        int skinId = 1;
        Skin skin = GOBLIN.getSkin(skinId);
        int swarmId = params.getId();
        List<Enemy> swarm = new ArrayList<>();
        float newSpeed = skin.getSpeed() * 1.75f;
        EnemyType mainGoblinType = RNG.nextBoolean() ? HOBGOBLIN : DUP_GOBLIN;
        if (time - startRoundTime > spawnConfig.getSwarmTimeOffset(swarmId)
                && getSwarmRemoveTime(swarmId) + spawnConfig.getSwarmWaitTime(swarmId) < time
                && (shouldSwarmSpawn(swarmId) || spawnConfig.isSwarmUnconditionalRespawn(swarmId))) {
            for (int i = 0; i < amount; i++) {
                Enemy enemy = addItem(i == 0 ? mainGoblinType : GOBLIN, skinId, getScarabTrajectory(params, time, newSpeed),
                        newSpeed, null, swarmId);
                enemy.addToSwarm(GOBLINS, swarmId);
                enemy.setEnergy(enemy.getFullEnergy());
                enemy.setSpeed(newSpeed);
                swarm.add(enemy);
            }
        }
        if (!swarm.isEmpty()) {
            registerSwarm(swarmId, swarm);
        }
        return swarm;
    }

    private List<SwarmSpawnParams> getSwarmSpawnParams(SwarmType swarmType) {
        List<SwarmSpawnParams> swarmSpawnParams = map.getSwarmSpawnParams(swarmType);
        return (swarmSpawnParams != null && !swarmSpawnParams.isEmpty()) ?
                Lists.newArrayList(swarmSpawnParams) : null;
    }

    public List<SwarmParams> getSwarmParams() {
        List<SwarmParams> swarmParams = map.getSwarmParams();
        return (swarmParams != null && !swarmParams.isEmpty()) ? Lists.newArrayList(swarmParams) : null;
    }

    public Enemy createSpecter(long time, long animationDelay, long specterLifetime, EnemyType specterType) {
        PointD location = generateSpecterLocation();
        Trajectory trajectory = new Trajectory(1)
                .addPoint(new InvulnerablePoint(location.x, location.y, time + 1000))
                .addPoint(location.x, location.y, time + 1000 + animationDelay)
                .addPoint(location.x, location.y, time + 1000 + specterLifetime + animationDelay);
        Enemy specter = addItem(specterType, 1, trajectory, 1, null, -1);
        specter.setEnergy(specter.getFullEnergy());
        specter.setSpeed(1);
        return specter;
    }

    private PointD generateSpecterLocation() {
        List<PointD> specterPoints = map.getPoints(SPECTER_POINTS);
        PointD location = getRandomElement(specterPoints);
        for (Enemy enemy : getItems()) {
            if (SPECTERS.contains(enemy.getEnemyType())) {
                PointD enemyLocation = enemy.getLocation(System.currentTimeMillis());
                while (getDistance(location.x, location.y, enemyLocation.x, enemyLocation.y) < 10) {
                    location = getRandomElement(specterPoints);
                }
            }
        }
        return location;
    }

    public Integer collectDragonStoneFragment(int fragmentsForSpawn) {
        if (fragments < fragmentsForSpawn) {
            return ++fragments;
        }
        return null;
    }

    public void resetDragonStoneFragments() {
        fragments = 0;
    }

    public int getDragonStoneFragments() {
        return fragments;
    }

    public Enemy spawnCerberus() {
        float speed = CERBERUS.getSkin(1).getSpeed();
        Trajectory trajectory = getTrajectory(CERBERUS, speed, false, false, 1, false);
        Enemy enemy = addItem(CERBERUS, 1, trajectory, speed, null, -1);
        enemy.setEnergy(enemy.getFullEnergy());
        enemy.setSpeed(speed);
        enemy.setLives(2); // cerberus should be restored with full HP two times after death as he has 3 heads
        enemy.setShouldReturn(true);
        enemy.setRespawnDelay(15000);
        return enemy;
    }

    public Map<Long, Trajectory> shiftAllTrajectoriesAndMakeInvulnerable(long time, long duration) {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        lockEnemy.lock();
        try {
            for (Enemy enemy : items) {
                Trajectory trajectory;
                if (GARGOYLE.equals(enemy.getEnemyType())) {
                    trajectory = cutGargoyleTrajectoryAfterCurrentPhase(time, enemy);
                } else {
                    trajectory = shiftTrajectoryFromCurrentLocation(time, enemy, duration);
                }
                enemy.setTrajectory(trajectory);
                trajectories.put(enemy.getId(), trajectory);
            }
        } finally {
            lockEnemy.unlock();
        }
        return trajectories;
    }

    private Trajectory shiftTrajectoryFromCurrentLocation(long time, Enemy enemy, long duration) {
        long animationEndTime = time + SPAWN_DELAY + FOG_APPEAR_TIME;
        long bossLeaveTime = time + SPAWN_DELAY + BOSS_SPAWN_ANIMATION_DURATION + duration;
        List<Point> points = enemy.getTrajectory().getPoints();
        Trajectory trajectory = enemy.getTrajectory().copyWithPassedPoints(animationEndTime);
        PointD location = enemy.getLocation(animationEndTime);
        trajectory.addPoint(new InvulnerableFixedPoint(location.x, location.y, animationEndTime));
        trajectory.addPoint(location.x, location.y, bossLeaveTime);
        for (int i = trajectory.getPoints().size() - 2; i < points.size(); i++) {
            Point point = points.get(i);
            trajectory.addPoint(point.create(point.getX(), point.getY(), point.getTime() + duration));
        }
        return trajectory;
    }

    Trajectory cutGargoyleTrajectoryAfterCurrentPhase(long time, Enemy enemy) {
        Trajectory trajectory = new Trajectory(enemy.getSpeed());
        int phaseStart = enemy.getTrajectory().getIndexOfFirstPassedPoint(time) / 8 * 8;
        List<Point> points = enemy.getTrajectory().getPoints();
        for (int i = phaseStart; i < phaseStart + 8; i++) {
            trajectory.addPoint(points.get(i));
        }
        if (points.size() > phaseStart + 8 && points.get(phaseStart + 7).getTime() < time + 1000) {
            for (int i = phaseStart + 8; i < phaseStart + 16; i++) {
                trajectory.addPoint(points.get(i));
            }
        }
        return trajectory;
    }

    public Enemy spawnBoss(long time, int duration, double defeatTresHold) {
        long spawnTime = time + SPAWN_DELAY;
        Trajectory trajectory = new Trajectory(1)
                .addPoint(new InvulnerablePoint(48, 48, spawnTime))
                .addPoint(48, 48, spawnTime + BOSS_SPAWN_ANIMATION_DURATION)
                .addPoint(new InvulnerablePoint(48, 48, spawnTime + BOSS_SPAWN_ANIMATION_DURATION + duration))
                .addPoint(new InvulnerablePoint(48, 48, spawnTime + BOSS_SPAWN_ANIMATION_DURATION + duration
                        + BOSS_LEAVE_ANIMATION_DURATION));
        Enemy boss = addItem(DRAGON, 1, trajectory, 1, null, -1);

        boss.setFullEnergy(defeatTresHold);
        boss.setEnergy(bossHP > 0 ? bossHP : defeatTresHold);
        getLogger().debug("spawnBoss: {}", boss);
        return boss;
    }

    public boolean updateBossRound() {
        lockEnemy.lock();
        try {
            for (Enemy enemy : getItems()) {
                if (enemy.isBoss()) {
                    if (enemy.update()) {
                        bossHP = (int) enemy.getEnergy();
                        items.remove(enemy);
                        return true;
                    }
                    return false;
                }
            }
            return true;
        } finally {
            lockEnemy.unlock();
        }
    }

    public void setBossHP(int bossHP) {
        this.bossHP = bossHP;
    }

    private Trajectory getSpiderSwarmTrajectory(SwarmParams params, long spawnTime, Skin skin) {
        return new RatSwarmTrajectoryGenerator(map, getCoords(), params.getAngle())
                .generate(new PointD(params.getStartX() + (double) RNG.nextInt(params.getDeltaX()),
                                params.getStartY() + (double) RNG.nextInt(params.getDeltaY())),
                        params.getDistance(), skin.getSpeed(), 1, spawnTime, 3000);
    }

    private Trajectory getRatSwarmTrajectory(SwarmParams params, long spawnTime, Skin skin) {
        return new RatSwarmTrajectoryGenerator(map, getCoords(), params.getAngle(), 20, 35, 35)
                .generate(new PointD(params.getStartX(), params.getStartY()),
                        params.getDistance(), skin.getSpeed(), 0, spawnTime, 500);
    }

    private Trajectory getRavenSwarmTrajectory(SwarmParams params, long spawnTime, Skin skin) {
        return new RatSwarmTrajectoryGenerator(map, getCoords(), params.getAngle(), 20, 35, 35)
                .generate(new PointD(params.getStartX() + (double) RNG.nextInt(params.getDeltaX()),
                                params.getStartY() + (double) RNG.nextInt(params.getDeltaY())),
                        params.getDistance(), skin.getSpeed(), 1, spawnTime, 1000);
    }

    public long getActiveBossId(long time) {
        lockEnemy.lock();
        try {
            for (Enemy enemy : items) {
                if (enemy.isBoss()) {
                    return enemy.isInvulnerable(time) ? -1 : enemy.getId();
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return -1;
    }

    @Override
    protected boolean enemyCouldBeHit(long time, Enemy enemy) {
        return !enemy.isInvulnerable(time);
    }

    @Override
    public Enemy addEnemyByTypeNew(EnemyType enemyType, IMathEnemy mathEnemy, int skinId, long parentEnemyId, boolean needNearCenter, boolean needFinalSteps, boolean useCustomTrajectories) {
        int skin = (skinId == -1 ? getRandomSkin(enemyType) : skinId);
        float speed = generateSpeed(enemyType.getSkin(skin));
        return addItem(enemyType, skin, getTrajectory(enemyType, speed, false, needNearCenter, skin, needFinalSteps, false),
                speed, mathEnemy, parentEnemyId);
    }

    public Map<Long, Double> getRageTargets(long time, PointD point, Long baseEnemyId, int numberEnemies) {
        lockEnemy.lock();
        try {
            return getRageTargetsWithoutLock(time, point, baseEnemyId, numberEnemies);
        } finally {
            lockEnemy.unlock();
        }
    }

    public Map<Long, Double> getRageTargetsWithoutLock(long time, PointD point, Long baseEnemyId, int numberEnemies) {
        Map<Long, Double> enemiesDistances = new LinkedHashMap<>();
        for (Enemy enemy : items) {
            if (!SPECTERS.contains(enemy.getEnemyType()) && !OGRE.equals(enemy.getEnemyType())
                    && !DARK_KNIGHT.equals(enemy.getEnemyType()) && !CERBERUS.equals(enemy.getEnemyType())
            ) {
                double dist = calculateSquareDist(time, enemy, point);
                if (enemy.getId() != baseEnemyId && enemyCouldBeHit(time, enemy)) {
                    enemiesDistances.put(enemy.getId(), dist);
                }
            }
        }
        return enemiesDistances.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(numberEnemies)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public List<Long> removeBaseEnemiesAndGetIds() {
        lockEnemy.lock();
        try {
            List<Long> result = new ArrayList<>();
            List<Enemy> toRemove = new ArrayList<>();
            for (Enemy enemy : getItems()) {
                if (!DRAGON.equals(enemy.getEnemyType())) {
                    result.add(enemy.getId());
                    toRemove.add(enemy);
                }
            }
            toRemove.forEach(enemy -> removeItem(enemy.getId()));
            return result;
        } finally {
            lockEnemy.unlock();
        }
    }

    public Map<Long, Trajectory> unshiftTrajectories(long time, long deltaTime) {
        lockEnemy.lock();
        try {
            Map<Long, Trajectory> trajectories = new HashMap<>();
            for (Enemy enemy : getItems()) {
                List<Point> points = enemy.getTrajectory().getPoints();
                Trajectory trajectory = enemy.getTrajectory().copyWithPassedPoints(time);
                for (int i = trajectory.getPoints().size(); i < points.size(); i++) {
                    Point point = points.get(i);
                    trajectory.addPoint(point.create(point.getX(), point.getY(), point.getTime() - deltaTime));
                }
                enemy.setTrajectory(trajectory);
                trajectories.put(enemy.getId(), trajectory);
            }
            return trajectories;
        } finally {
            lockEnemy.unlock();
        }
    }

    public long getBossSpawnTime() {
        lockEnemy.lock();
        try {
            for (Enemy enemy : getItems()) {
                if (enemy.isBoss()) {
                    return enemy.getTrajectory().getPoints().get(0).getTime();
                }
            }
            return 0;
        } finally {
            lockEnemy.unlock();
        }
    }

    public List<Enemy> spawnSpidersWithAngleTrajectory(GameConfig config) {
        long time = System.currentTimeMillis();
        List<Enemy> swarm = new ArrayList<>();
        List<Trajectory> trajectories = map.getPredefinedTrajectories(ANGLE_SPIDERS);
        List<EnemyType> enemies = SPIDERS_RANGE.getEnemies();
        EnemyType type = enemies.get(RNG.nextInt(enemies.size()));
        if (trajectories != null && config.isEnemyEnabled(type)) {
            Trajectory baseTrajectory = getRandomElement(trajectories);
            SpiderSwarmAngleTrajectoryGenerator generator = new SpiderSwarmAngleTrajectoryGenerator();
            int swarmSize = RNG.nextInt(7, 12);
            int swarmId = generateSwarmId();
            int skinId = 1;
            double speed = type.getSkin(skinId).getSpeed() * 1.25;
            for (int i = 0; i < swarmSize; i++) {
                Trajectory trajectory = generator.generate(time, baseTrajectory.getPoints(), speed);
                Enemy enemy = addItem(type, skinId, trajectory, (float) speed, null, -1);
                enemy.addToSwarm(ANGLE_SPIDERS, swarmId);
                enemy.setEnergy(enemy.getFullEnergy());
                swarm.add(enemy);
            }
            registerSwarm(swarmId, swarm);
        }
        return swarm;
    }

    public List<Enemy> spawnOrcsPlatoon() {
        List<Enemy> swarm = new ArrayList<>();
        List<Trajectory> trajectories = map.getPredefinedTrajectories(ORC_PLATOON);
        if (trajectories != null) {
            int swarmId = swarmIdGenerator.getAndIncrement();
            float speed = ORC.getSkin(1).getSpeed();
            Trajectory baseTrajectory = prepareTrajectory(System.currentTimeMillis() + 1000, getRandomElement(trajectories), 0, 0, 0, speed);
            int swarmTriples = RNG.nextInt(4, 7);
            for (int i = 0; i < swarmTriples; i++) {
                for (int position = 0; position < 3; position++) {
                    Enemy enemy = createPlatoonOrc(baseTrajectory, i, position, speed);
                    enemy.addToSwarm(ORC_PLATOON, swarmId);
                    enemy.setEnergy(enemy.getFullEnergy());
                    swarm.add(enemy);
                }
            }
            registerSwarm(swarmId, swarm);
        }
        return swarm;
    }

    public void update() {
        lockEnemy.lock();
        try {
            long time = System.currentTimeMillis();
            List<Enemy> deadEnemies = new ArrayList<>();
            for (Enemy enemy : getItems()) {
                if (enemy.isMovable() && enemy.update()) {
                    if (enemy.isPartOfSwarm()) {
                        Swarm<Enemy> swarm = swarms.get(enemy.getSwarmId());
                        if (swarm != null && swarm.incrementRemovedAndUpdate() && !swarm.isShouldReturn()) {
                            swarms.remove(enemy.getSwarmId());
                            addSwarmRemoveTime(enemy.getSwarmId(), time);
                        }
                    }
                    deadEnemies.add(enemy);
                    addRemoveTime(enemy.getEnemyType(), time);
                }
            }
            if (!deadEnemies.isEmpty()) {
                items.removeAll(deadEnemies);
            }
        } finally {
            lockEnemy.unlock();
        }
    }

    private Enemy createPlatoonOrc(Trajectory baseTrajectory, int triple, int position, float speed) {
        switch (position) {
            case 0:
                return addItem(ORC, 1, shiftTrajectory(baseTrajectory, 0, 0, triple * 1500L),
                        speed, null, -1);
            case 1:
                return addItem(ORC, 1, shiftTrajectory(baseTrajectory, 0, 8, triple * 1500L + 70L),
                        speed, null, -1);
            default:
            case 2:
                return addItem(ORC, 1, shiftTrajectory(baseTrajectory, 5, 4, triple * 1500L - 150L),
                        speed, null, -1);
        }
    }

    private Map<EnemyType, Long> getRemoveTimes() {
        if (removeTimes == null) {
            removeTimes = new EnumMap<>(EnemyType.class);
        }
        return removeTimes;
    }

    public long getRemoveTime(EnemyType enemyType) {
        return getRemoveTimes().getOrDefault(enemyType, 0L);
    }

    public long getSwarmRemoveTime(Integer swarmId) {
        return getRemoveTimesForSwarms().getOrDefault(swarmId, 0L);
    }

    public void addSwarmRemoveTime(int swarmId, long swarmRemoveTime) {
        getRemoveTimesForSwarms().put(swarmId, swarmRemoveTime);
    }

    private boolean shouldSwarmSpawn(Integer swarmId) {
        return !getSwarms().containsKey(swarmId);
    }

    public long getRemoveTime(EnemyRange enemyRange) {
        long lastRemoveTime = 0;
        for (EnemyType enemyType : enemyRange.getEnemies()) {
            long removeTime = getRemoveTime(enemyType);
            if (removeTime > lastRemoveTime) {
                lastRemoveTime = removeTime;
            }
        }
        return lastRemoveTime;
    }

    public void addRemoveTime(EnemyType enemyType, long time) {
        getRemoveTimes().put(enemyType, time);
    }

    public List<Enemy> spawnGroupBySwarmType(GameConfig config, SwarmType swarmType) {
        List<Enemy> enemies = new ArrayList<>();
        lockEnemy.lock();
        try {
            GroupParams group = getGroupBySwarmType(swarmType);
            if (group != null) {
                int swarmId = generateSwarmId();
                List<Integer> indexes = getRandomIndexes(group.getMinSize(), group.getMaxSize(), group.getTrajectories().size());
                GroupTemplate template = getRandomElement(group.getTemplates());
                for (GroupMember member : template.getEnemies()) {
                    if (config.isEnemyEnabled(EnemyType.getById(member.getType()))) {
                        for (int index : indexes) {
                            Enemy enemy = createEnemyWithTrajectory(member, group.getTrajectories().get(index), group.isCanStop());
                            enemy.addToSwarm(group.getType(), swarmId);
                            enemies.add(enemy);
                        }
                    }
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return enemies;
    }

    public int getAliveSwarmEnemies() {
        int aliveSwarmEnemies = 0;
        for (Pair<Integer, Boolean> pair : getItemsTypeIdsAndSwarmState()) {
            if (Boolean.TRUE.equals(pair.getValue())) {
                aliveSwarmEnemies++;
            }
        }
        return aliveSwarmEnemies;
    }

    public List<Enemy> spawnAllGroupsBySwarmType(GameConfig config, SpawnConfig spawnConfig, SwarmType swarmType,
                                                 long startRoundTime) {
        List<Enemy> enemies = new ArrayList<>();
        lockEnemy.lock();
        try {
            List<GroupParams> allGroupsBySwarmType = getAllGroupsBySwarmType(swarmType);
            if (allGroupsBySwarmType == null) {
                return Collections.emptyList();
            }
            Collections.shuffle(allGroupsBySwarmType);
            for (GroupParams group : allGroupsBySwarmType) {
                if (group != null) {
                    List<GroupTemplate> templates = Lists.newArrayList(group.getTemplates());
                    Collections.shuffle(templates);
                    Set<Integer> usedTrajectoriesForGroup = new HashSet<>();
                    for (GroupTemplate template : templates) {
                        if (getAliveSwarmEnemies() + enemies.size() > spawnConfig.getSwarmEnemiesMax()) {
                            return enemies;
                        }
                        List<Enemy> templateEnemies = spawnGroup(config, spawnConfig, startRoundTime, group, template,
                                usedTrajectoriesForGroup);
                        enemies.addAll(templateEnemies);
                    }
                    usedTrajectoriesForGroup.clear();
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return enemies;
    }

    private List<Enemy> spawnGroup(GameConfig config, SpawnConfig spawnConfig, long startRoundTime, GroupParams group,
                                   GroupTemplate template, Set<Integer> usedTrajectories) {
        long time = System.currentTimeMillis();
        List<Enemy> swarm = new ArrayList<>();
        int swarmId = template.getId();
        List<Integer> indexes = getRandomIndexes(group.getMinSize(), group.getMaxSize(), group.getTrajectories().size());
        indexes.removeAll(usedTrajectories);
        if (!indexes.isEmpty() && time - startRoundTime > spawnConfig.getSwarmTimeOffset(swarmId)
                && getSwarmRemoveTime(swarmId) + spawnConfig.getSwarmWaitTime(swarmId) < time
                && (shouldSwarmSpawn(swarmId) || spawnConfig.isSwarmUnconditionalRespawn(swarmId))) {
            for (GroupMember member : template.getEnemies()) {
                if (config.isEnemyEnabled(EnemyType.getById(member.getType()))) {
                    for (int index : indexes) {
                        Enemy enemy = createEnemyWithTrajectory(member, group.getTrajectories().get(index), group.isCanStop());
                        enemy.addToSwarm(group.getType(), swarmId);
                        swarm.add(enemy);
                    }
                }
            }
        }
        if (!swarm.isEmpty()) {
            registerSwarm(swarmId, swarm);
            usedTrajectories.addAll(indexes);
        }
        return swarm;
    }

    private GroupParams getAvailableGroup() {
        Map<Integer, List<GroupParams>> groups = map.getPredefinedGroups();
        Set<Integer> aliveGroups = new HashSet<>();
        for (Enemy enemy : getItems()) {
            aliveGroups.add(enemy.getSwarmType());
        }
        Set<Integer> availableGroups = new HashSet<>(groups.keySet());
        availableGroups.removeAll(aliveGroups);
        if (availableGroups.isEmpty()) {
            return null;
        }
        return getRandomElement(groups.get(getRandomElement(new ArrayList<>(availableGroups))));
    }

    private GroupParams getGroupBySwarmType(SwarmType swarmType) {
        List<GroupParams> groupParamsBySwarmType = map.getPredefinedGroups().get(swarmType.getTypeId());
        return (groupParamsBySwarmType != null && !groupParamsBySwarmType.isEmpty()) ?
                getRandomElement(groupParamsBySwarmType) : null;
    }

    private List<GroupParams> getAllGroupsBySwarmType(SwarmType swarmType) {
        List<GroupParams> groupParamsBySwarmType = map.getPredefinedGroups().get(swarmType.getTypeId());
        return (groupParamsBySwarmType != null && !groupParamsBySwarmType.isEmpty())
                ? Lists.newArrayList(groupParamsBySwarmType) : null;
    }

    private List<Integer> getRandomIndexes(int min, int max, int limit) {
        int count = min < max ? RNG.nextInt(min, max + 1) : max;
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            result.add(i);
        }
        for (int i = limit; i > count; i--) {
            result.remove(RNG.nextInt(result.size()));
        }
        return result;
    }

    private Enemy createEnemyWithTrajectory(GroupMember member, Trajectory template, boolean canStop) {
        double dx = 0;
        double dy = 0;
        if (member.getType() == RAVEN.getId()) {
            dx = RNG.rand() / 3;
            dy = RNG.rand() / 3;
        }
        int dt = (member.getType() == BAT.getId() || member.getType() == RAVEN.getId()) ? RNG.nextInt(1000) : 0;
        EnemyType enemyType = EnemyType.getById(member.getType());
        Skin skin = enemyType.getSkin(member.getSkin());
        long spawnTime = System.currentTimeMillis() + 1000 + RNG.nextInt(100);
        Trajectory trajectory = prepareTrajectory(spawnTime, template, member.getDx() + dx, member.getDy() + dy, member.getDt() + dt, skin.getSpeed());
        return addItem(enemyType, member.getSkin(), trajectory, skin.getSpeed(), null, -1);
    }

    @Override
    protected Trajectory getInitialTrajectory(double speed, boolean needFinalSteps, IEnemyType enemyType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Long, Trajectory> generateUpdateTrajectories(boolean needFinalSteps) {
        throw new UnsupportedOperationException();
    }

    public Map<Long, Trajectory> generateFreezeTrajectories(long time, int freezeTime, double xShot, double yShot, int maxDist) {
        Map<Long, Trajectory> trajectories = new HashMap<>();

        lockEnemy.lock();
        try {
            for (Enemy enemy : getItems()) {
                PointI location = enemy.getLocation(time).toPointI();

                if (!shouldIgnoreFreeze(enemy, location, time)
                        && map.isValid(location.x, location.y)
                        && calcDistance(location, xShot, yShot) <= maxDist
                        && isVisible(time, enemy)) {

                    List<Point> points = new ArrayList<>();

                    Trajectory currentTrajectory = enemy.getTrajectory();
                    if (getIndexOfFixedPoint(time, currentTrajectory) >= 0) {
                        points = freezeWithFixedPoint(time, enemy.getTrajectory(), freezeTime, location);
                    } else {
                        points.add(new FreezePoint(location.x, location.y, time));
                        points.add(new FreezePoint(location.x, location.y, time + freezeTime));

                        addPointsFromOldTrajectory(points, time, freezeTime, location, enemy);
                    }

                    if (!points.isEmpty()) {
                        Trajectory trajectory = new Trajectory(enemy.getSpeed(), points);
                        enemy.setTrajectory(trajectory);
                        trajectories.put(enemy.getId(), trajectory);

                        enemy.setLastFreezeTime(time);
                        getLogger().debug("generateFreezeTrajectories enemy: {}", enemy);
                    }
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return trajectories;
    }

    List<Point> freezeWithFixedPoint(long time, Trajectory original, long freezeTime, PointI location) {
        List<Point> sourcePoints = original.getPoints();
        int fixedIndex = getIndexOfFixedPoint(time, original);
        List<Point> points = new ArrayList<>();
        if (fixedIndex >= 0) {
            long fixedTime = sourcePoints.get(fixedIndex).getTime();
            freezeTime = Math.min((int) (fixedTime - time), freezeTime);
            if (freezeTime > 0) {
                int passedIndex = original.getIndexOfFirstPassedPoint(time);
                if (sourcePoints.get(passedIndex).isFreezePoint()) {
                    passedIndex++;
                }
                points.add(new FreezePoint(location.x, location.y, time));
                points.add(new FreezePoint(location.x, location.y, time + freezeTime));
                for (int i = passedIndex + 1; i < fixedIndex; i++) {
                    Point point = sourcePoints.get(i);
                    points.add(point.create(point.getX(), point.getY(), Math.min(point.getTime() + freezeTime, fixedTime)));
                }
                for (int i = fixedIndex; i < original.getPoints().size(); i++) {
                    points.add(sourcePoints.get(i));
                }
            }
        } else {
            return original.getPoints();
        }
        return points;
    }

    private int getIndexOfFixedPoint(long time, Trajectory trajectory) {
        List<Point> points = trajectory.getPoints();
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (point.getTime() >= time && point.isFixed()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isVisible(long time, Enemy enemy) {
        PointD location = enemy.getLocation(time);
        int height = (int) (enemy.getEnemyType().getHeight() / getCoords().getCellHeight()) - 1;
        for (int i = 0; i < height; i++) {
            if (isVisible((int) location.x - i, (int) location.y - i, enemy.getEnemyType())) {
                return true;
            }
        }
        return false;
    }

    public boolean isVisible(int x, int y, EnemyType enemyType) {
        return FLYING_ENEMIES.contains(enemyType) ? isValidPointForFlyingEnemies(x, y)
                : map.isValid(x, y) && map.isBossPath(x, y);
    }

    private Map<Integer, Long> getRemoveTimesForSwarms() {
        if (removeTimesForSwarms == null) {
            removeTimesForSwarms = new HashMap<>();
        }
        return removeTimesForSwarms;
    }

    public Map<Integer, Swarm<Enemy>> getSwarms() {
        lockEnemy.lock();
        try {
            return Collections.unmodifiableMap(swarms);
        } finally {
            lockEnemy.unlock();
        }
    }

    @Override
    protected void removeEnemyFromSwarm(Enemy enemy) {
        int swarmId = enemy.getSwarmId();
        Swarm<Enemy> swarm = swarms.get(swarmId);
        if (swarm != null && swarm.getEnemyIds().remove(enemy.getId()) && swarm.getEnemyIds().isEmpty()) {
            swarms.remove(swarmId);
            addSwarmRemoveTime(swarmId, System.currentTimeMillis());
        }
    }

    public Map<Long, Double> getNNearestLowPayEnemiesWithoutBase(long time, PointD point, Long baseEnemyId,
                                                                 int numberEnemies, GameConfig config) {
        lockEnemy.lock();
        try {
            return getNNearestLowPayEnemiesWithoutBaseWithoutLock(time, point, baseEnemyId, numberEnemies, config);
        } finally {
            lockEnemy.unlock();
        }
    }

    private Map<Long, Double> getNNearestLowPayEnemiesWithoutBaseWithoutLock(long time, PointD point, Long baseEnemyId,
                                                                             int numberEnemies, GameConfig config) {
        Map<Long, Double> enemiesPayoutsDistances = new LinkedHashMap<>();
        for (Enemy enemy : items) {
            if (enemy.isBoss()) {
                continue;
            }
            double dist = calculateSquareDist(time, enemy, point);
            int payout = config.getEnemyData(enemy.getEnemyType(), 0).getPayout();
            if (enemy.getId() != baseEnemyId && enemyCouldBeHit(time, enemy)) {
                enemiesPayoutsDistances.put(enemy.getId(), payout * dist);
            }
        }
        return enemiesPayoutsDistances.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(numberEnemies)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public Map<Long, Double> getAllLowPayEnemies(long time, int numberEnemies, GameConfig config) {
        lockEnemy.lock();
        try {
            return getAllLowPayEnemiesWithoutLock(time, numberEnemies, config);
        } finally {
            lockEnemy.unlock();
        }
    }

    private Map<Long, Double> getAllLowPayEnemiesWithoutLock(long time, int numberEnemies, GameConfig config) {
        Map<Long, Double> enemiesPayouts = new LinkedHashMap<>();
        for (Enemy enemy : items) {
            if (enemy.isBoss()) {
                continue;
            }
            double payout = config.getEnemyData(enemy.getEnemyType(), 0).getPayout();
            if (enemyCouldBeHit(time, enemy)) {
                enemiesPayouts.put(enemy.getId(), payout);
            }
        }
        return enemiesPayouts.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(numberEnemies)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }
}
