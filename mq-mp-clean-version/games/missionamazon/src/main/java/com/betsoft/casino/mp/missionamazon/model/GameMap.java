package com.betsoft.casino.mp.missionamazon.model;

import com.betsoft.casino.mp.WizardTrajectoryGenerator;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.scenarios.TypeAndSkin;
import com.betsoft.casino.mp.missionamazon.model.math.*;
import com.betsoft.casino.mp.missionamazon.model.math.config.GameConfig;
import com.betsoft.casino.mp.missionamazon.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.movement.*;
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

import static com.betsoft.casino.mp.missionamazon.model.math.EnemyRange.*;
import static com.betsoft.casino.mp.missionamazon.model.math.EnemyType.*;
import static com.betsoft.casino.mp.missionamazon.model.math.SwarmType.*;

/**
 * User: flsh
 * Date: 21.09.17.
 */
@SpringAware
public class GameMap extends AbstractGameMap<Enemy, GameMapShape, EnemyRange, EnemyType, GameMap> {
    protected static final String STATIC_POINTS = "2";
    private static final int ORION_WASP_SKIN = 3;
    private static final Long FLIP_XY = 1L;
    private static final int STATIC_ENEMY_SPAWN_COUNTER = 3000;

    private EnemyRange possibleEnemies;
    private long minBossReturnTime;
    private int bossHP;
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
        return !BASE_ENEMIES.contains(enemy.getEnemyType());
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
        return super.getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps);
    }

    private ShamanTrajectoryGenerator createWizardTrajectoryGenerator(GameMapShape map, PointI source, double speed,
                                                                      long wizardLifeTime) {
        return new WizardTrajectoryGenerator(map, source, speed, getCoords(), getCurrentEnemiesFromRange(STATIC_ENEMIES))
                .setVisibleArea(50, 100, 910, 490)
                .setInvisibilityStartTime(2045)
                .setTeleportStartTime(2368)
                .setTeleportFinishTime(2369)
                .setInvisibilityFinishTime(2493)
                .setAnimationDuration((int) wizardLifeTime);
    }

    public Enemy createWitch(EnemyType witchType, long witchLifeTime, int teleportCounter) {
        int skin = getRandomSkin(witchType);
        float speed = generateSpeed(witchType.getSkin(skin));
        Trajectory trajectory = createWizardTrajectoryGenerator(map, new PointI(), speed, witchLifeTime)
                .generate(System.currentTimeMillis() + SPAWN_DELAY, teleportCounter, true);
        return addItem(witchType, 1, trajectory, speed, null, -1);
    }

    private List<AbstractEnemy> getCurrentEnemiesFromRange(EnemyRange enemyRange) {
        List<AbstractEnemy> enemies = new ArrayList<>();
        for (Enemy enemy : getItems()) {
            if (enemyRange.contains(enemy.getEnemyType())) {
                enemies.add(enemy);
            }
        }
        return enemies;
    }

    public EnemyType getRandomEnemyFromConfig(List<Integer> configEnemyIds, EnemyRange enemyRange) {
        if (configEnemyIds != null && !configEnemyIds.isEmpty()) {
            EnemyType randomEnemy = EnemyType.getById(RNG.nextInt(configEnemyIds.size()));
            if (randomEnemy != null && enemyRange.contains(randomEnemy)) {
                return randomEnemy;
            }
        }
        return enemyRange.getRandomEnemy();
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
        if (WITCH.equals(enemyType)) {
            return new ShamanTrajectoryGenerator(map, PointI.EMPTY, enemy.getSpeed())
                    .generateLeaveTrajectory(startTime, enemy.getTrajectory());
        }
        if (FLOWERS.contains(enemyType) || PLANTS.contains(enemyType)) {
            return null;
        }
        return super.generateLeaveTrajectory(generator, location, startTime, enemy);
    }

    @Override
    public boolean needDoubleSpeed(int enemyTypeId) {
        return enemyTypeId == 3;
    }

    @Override
    protected long getBossSpawnAnimationDuration(int skinId) {
        return BossType.getBySkinId(skinId).getSpawnTime();
    }

    @Override
    protected long getBossInvulnerabilityTime(int skinId) {
        return BossType.getBySkinId(skinId).getInvulnerabilityTime();
    }

    @Override
    protected void addPointsFromOldTrajectory(List<Point> points, long startTime, long freezeTime, PointI location, Enemy enemy) {
        if (WITCH.equals(enemy.getEnemyType())) {
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

    private List<Enemy> spawnSwarm(GameConfig config, SpawnConfig spawnConfig, SwarmParams params, SwarmType swarmType,
                                   long startRoundTime) {
        List<Enemy> enemies = new ArrayList<>();
        int amount = RNG.nextInt(params.getMinSize(), params.getMaxSize() + 1);
        long spawnTime = System.currentTimeMillis() + SPAWN_DELAY;
        scenarioCooldowns.put(params.getId(), spawnTime + params.getCooldown());
        int swarmId = params.getId();
        TypeAndSkin typeAndSkin = getRandomElement(params.getEnemies());
        EnemyType enemyType = EnemyType.getById(typeAndSkin.getType());
        Skin skin = enemyType.getSkin(typeAndSkin.getSkin());
        if (allowSpawnToSwarm(swarmId)) {
            for (int i = 0; i < amount; i++) {
                if (config.isEnemyEnabled(enemyType)) {
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
        if (ANT.equals(enemyType)) {
            trajectory = getRatSwarmTrajectory(params, spawnTime, skin);
        }
        return trajectory;
    }

    private Trajectory getRatSwarmTrajectory(SwarmParams params, long spawnTime, Skin skin) {
        return new RatSwarmTrajectoryGenerator(map, getCoords(), params.getAngle())
                .generate(new PointD(params.getStartX() + RNG.nextInt(params.getDeltaX()),
                                params.getStartY() + RNG.nextInt(params.getDeltaY())),
                        params.getDistance(), skin.getSpeed() * 1.25, 1, spawnTime, 3000);
    }

    private List<Enemy> spawnWaspSwarm(SwarmSpawnParams params, GameConfig config, SpawnConfig spawnConfig,
                                       long startRoundTime) {
        List<Enemy> wasps = new ArrayList<>();
        int amount = RNG.nextInt(2, 4);
        int skinId = getRandomSkin(WASP);
        Skin skin = WASP.getSkin(skinId);
        int swarmId = params.getId();

        if (config.isEnemyEnabled(WASP) && allowSpawnToSwarm(swarmId)) {
            for (int i = 0; i < amount; i++) {
                float newSpeed = skin.getSpeed() * 1.75f;
                Enemy enemy = addItem(WASP, skinId, getScarabTrajectory(params, System.currentTimeMillis(), newSpeed),
                        newSpeed, null, swarmId);
                enemy.addToSwarm(WASP_REGULAR, swarmId);
                enemy.setEnergy(enemy.getFullEnergy());
                enemy.setSpeed(newSpeed);
                wasps.add(enemy);
            }
        }
        if (!wasps.isEmpty()) {
            registerSwarm(swarmId, wasps);
        }
        return wasps;
    }

    private List<Enemy> spawnWaspOrionSwarm(SwarmSpawnParams params, GameConfig config,
                                            SpawnConfig spawnConfig, long startRoundTime) {
        List<Enemy> wasps = new ArrayList<>();
        int amount = 3;
        Skin skin = WASP.getSkin(ORION_WASP_SKIN);
        Trajectory baseTrajectory = getScarabTrajectory(params, System.currentTimeMillis(), skin.getSpeed());
        int swarmId = params.getId();

        if (config.isEnemyEnabled(WASP) && allowSpawnToSwarm(swarmId)) {
            for (int i = 0; i < amount; i++) {
                Enemy enemy = addItem(WASP, ORION_WASP_SKIN, shiftTrajectory(baseTrajectory, 0, 0, i * 500),
                        skin.getSpeed(), null, swarmId);
                enemy.addToSwarm(WASP_ORION, swarmId);
                enemy.setEnergy(enemy.getFullEnergy());
                wasps.add(enemy);
            }
        }
        if (!wasps.isEmpty()) {
            registerSwarm(swarmId, wasps);
        }
        return wasps;
    }

    public List<Enemy> spawnAllWaspsFromSwarmSpawnParams(SwarmType swarmType, GameConfig config,
                                                         SpawnConfig spawnConfig, long startRoundTime) {
        List<Enemy> enemies = new ArrayList<>();
        List<SwarmSpawnParams> swarmSpawnParams = getSwarmSpawnParams(swarmType);
        if (swarmSpawnParams == null) {
            return Collections.emptyList();
        }
        Collections.shuffle(swarmSpawnParams);
        for (SwarmSpawnParams params : swarmSpawnParams) {
            if (getAliveSwarmEnemies() + enemies.size() > spawnConfig.getSwarmEnemiesMax()) {
                return enemies;
            }
            if (params != null) {
                if (swarmType.equals(WASP_REGULAR)) {
                    enemies.addAll(spawnWaspSwarm(params, config, spawnConfig, startRoundTime));
                } else if (swarmType.equals(WASP_ORION)) {
                    enemies.addAll(spawnWaspOrionSwarm(params, config, spawnConfig, startRoundTime));
                }
            }
        }
        return enemies;
    }

    public List<Enemy> spawnAllAntsFromSwarmParams(GameConfig config, SpawnConfig spawnConfig, long startRoundTime) {
        List<Enemy> enemies = new ArrayList<>();
        List<SwarmParams> swarmParams = getSwarmParams();
        if (swarmParams == null) {
            return Collections.emptyList();
        }
        Collections.shuffle(swarmParams);
        for (SwarmParams params : swarmParams) {
            if (getAliveSwarmEnemies() + enemies.size() > spawnConfig.getSwarmEnemiesMax()) {
                return enemies;
            }
            if (params != null) {
                int swarmId = params.getId();
                if (swarmId > 200 && swarmId < 300) {
                    enemies.addAll(spawnSwarm(config, spawnConfig, params, ANTS, startRoundTime));
                }
            }
        }
        return enemies;
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

    public boolean isPointsPresent(String key) {
        List<PointD> points = map.getPoints(key);
        return points != null && !points.isEmpty();
    }

    public Enemy createStaticEnemy(EnemyType enemyType, long enemyLifeTime) {
        PointD location = generateStaticLocation();
        int skinId = getRandomSkin(enemyType);
        long time = System.currentTimeMillis();
        Trajectory trajectory = new Trajectory(1)
                .addPoint(new InvulnerablePoint(location.x, location.y, time + 1000))
                .addPoint(location.x, location.y, time + 1000 + SPAWN_DELAY)
                .addPoint(location.x, location.y, time + 1000 + enemyLifeTime + SPAWN_DELAY);
        Enemy staticEnemy = addItem(enemyType, skinId, trajectory, 1, null, -1);
        staticEnemy.setEnergy(staticEnemy.getFullEnergy());
        staticEnemy.setSpeed(1);
        return staticEnemy;
    }

    public Enemy createWeaponCarrierEnemy(EnemyType enemyType, long enemyLifeTime) {
        boolean rightCorner = RNG.nextBoolean();
        int delta = RNG.nextInt(15);
        PointD location = new PointD((double) 61 + delta, 11);
        if (rightCorner) {
            location = new PointD((double) 18 + delta, 69);
        }
        int skinId = getRandomSkin(enemyType);
        long time = System.currentTimeMillis() + 1000;
        Trajectory trajectory = new WeaponCarrierTrajectoryGenerator(map, location.toPointI(), 4)
                .generate(time, (int) enemyLifeTime - 3);
        Enemy weaponCarrierEnemy = addItem(enemyType, skinId, trajectory, 4, null, -1);
        addLeaveTrajectory(weaponCarrierEnemy);
        weaponCarrierEnemy.setEnergy(weaponCarrierEnemy.getFullEnergy());
        return weaponCarrierEnemy;
    }

    public boolean allowCreateStaticEnemy(SpawnConfig spawnConfig) {
        if (spawnConfig.getStaticEnemyMax() == -1) {
            return true;
        }
        int enemyCounter = 0;
        for (Enemy enemy : getItems()) {
            if (STATIC_ENEMIES.contains(enemy.getEnemyType())) {
                enemyCounter++;
            }
        }
        return spawnConfig.getStaticEnemyMax() > enemyCounter;
    }

    private PointD generateStaticLocation() {
        PointD location = getRandomStaticPoint();
        long currentTime = System.currentTimeMillis();
        List<PointD> staticEnemyLocation = getItems().stream()
                .filter(enemy -> STATIC_ENEMIES.contains(enemy.getEnemyType()))
                .map(enemy -> enemy.getLocation(currentTime))
                .collect(Collectors.toList());

        int counter = 0;
        while (!checkDistance(staticEnemyLocation, location)) {
            location = getRandomStaticPoint();
            if (++counter > STATIC_ENEMY_SPAWN_COUNTER) {
                break;
            }
        }
        return location;
    }

    private boolean checkDistance(List<PointD> enemyLocations, PointD currentLocation) {
        for (PointD enemyLocation : enemyLocations) {
            if (getDistance(currentLocation.x, currentLocation.y, enemyLocation.x, enemyLocation.y) < 0.1) {
                return false;
            }
        }
        return true;
    }

    private PointD getRandomStaticPoint() {
        return GameTools.getRandomNumberKeyFromMapWithNorm(
                MathData.calculateStaticEnemiesSpawnWeights(map.getPoints(STATIC_POINTS), getCurrentEnemiesLocations()));
    }

    public Enemy spawnBoss(BossType bossType, int fullHP) {
        Enemy boss = addEnemyByTypeNew(BOSS, null, bossType.getSkinId(), -1,
                false, false, false);
        addLeaveTrajectory(boss);
        boss.setFullEnergy(fullHP);
        boss.setEnergy(fullHP);
        getLogger().debug("spawnBoss: {}", boss);
        return boss;
    }

    public void addLeaveTrajectory(Enemy enemy) {
        Point lastPoint = enemy.getTrajectory().getLastPoint();
        ITrajectoryGenerator generator = new WaypointTrajectoryGenerator(map);
        Trajectory leaveTrajectory = generateLeaveTrajectory(generator, enemy.getLocation(lastPoint.getTime()).toPointI(), lastPoint.getTime(), enemy);
        enemy.getTrajectory().addPoints(leaveTrajectory.getPoints());
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

    public Enemy addEnemyWithTrajectory(EnemyType enemyType, Trajectory trajectory) {
        int skin = getRandomSkin(enemyType);
        float speed = generateSpeed(enemyType.getSkin(skin));
        return addItem(enemyType, skin, prepareTrajectory(System.currentTimeMillis() + 1000, trajectory, 0, 0, 0, speed)
                , speed, null, -1);
    }

    @Override
    public Enemy addEnemyByTypeNew(EnemyType enemyType, IMathEnemy mathEnemy, int skinId, long parentEnemyId, boolean needNearCenter, boolean needFinalSteps, boolean useCustomTrajectories) {
        int skin = (skinId == -1 ? getRandomSkin(enemyType) : skinId);
        float speed = generateSpeed(enemyType.getSkin(skin));
        return addItem(enemyType, skin, getTrajectory(enemyType, speed, false, needNearCenter, skin, needFinalSteps, false),
                speed, mathEnemy, parentEnemyId);

    }

    @Override
    protected Trajectory getBossTrajectory(double speed, boolean needStandOnThePlace, int skinId) {
        long spawnTime = System.currentTimeMillis() + 2000;
        PointI spawnPoint = getBossSpawnPoint(skinId);
        List<Point> points = new ArrayList<>();
        points.add(new InvulnerablePoint(spawnPoint.x, spawnPoint.y, spawnTime));
        points.add(new Point(spawnPoint.x, spawnPoint.y, spawnTime + getBossInvulnerabilityTime(skinId)));

        int bossScreenTimeBeforeLeave = RNG.nextInt(45, 65) * 1000;
        return getBossTrajectoryGenerator(spawnPoint, speed, needStandOnThePlace).generate(new Trajectory(speed, points),
                spawnTime + getBossSpawnAnimationDuration(skinId), 70, bossScreenTimeBeforeLeave);
    }

    public List<Long> removeBaseEnemiesAndGetIds() {
        lockEnemy.lock();
        try {
            List<Long> result = new ArrayList<>();
            List<Enemy> toRemove = new ArrayList<>();
            for (Enemy enemy : getItems()) {
                result.add(enemy.getId());
                toRemove.add(enemy);
            }
            toRemove.forEach(enemy -> removeItem(enemy.getId()));
            return result;
        } finally {
            lockEnemy.unlock();
        }
    }

    @Override
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

    private Map<EnemyType, Long> getRemoveTimes() {
        if (removeTimes == null) {
            removeTimes = new EnumMap<>(EnemyType.class);
        }
        return removeTimes;
    }

    public long getRemoveTime(EnemyType enemyType) {
        return getRemoveTimes().getOrDefault(enemyType, 0L);
    }

    public void addRemoveTime(EnemyType enemyType, long time) {
        getRemoveTimes().put(enemyType, time);
    }

    private Map<Integer, Long> getRemoveTimesForSwarms() {
        if (removeTimesForSwarms == null) {
            removeTimesForSwarms = new HashMap<>();
        }
        return removeTimesForSwarms;
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


    public int getAliveSwarmEnemies() {
        return (int) getItems().stream().filter(enemy -> SWARM_ENEMIES.contains(enemy.getEnemyType())).count();
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
        List<Enemy> swarm = new ArrayList<>();
        int swarmId = template.getId();
        List<Integer> indexes = getRandomIndexes(group.getMinSize(), group.getMaxSize(), group.getTrajectories().size());
        indexes.removeAll(usedTrajectories);
        if (!indexes.isEmpty() && allowSpawnToSwarm(swarmId)) {
            for (GroupMember member : template.getEnemies()) {
                if (config.isEnemyEnabled(EnemyType.getById(member.getType()))) {
                    for (int index : indexes) {
                        Enemy enemy = createEnemyWithTrajectory(member, group.getTrajectories().get(index));//
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

    public List<Enemy> spawnExplodedFrogs(IEnemy enemy, long delay) {
        List<Enemy> frogs = new ArrayList<>();
        long time = System.currentTimeMillis();
        int skinId = getRandomSkin(TINY_TOAD);
        Skin skin = TINY_TOAD.getSkin(skinId);
        for (int i = 0; i < 4; i++) {
            Trajectory trajectory = new ShortLeaveJumpTrajectoryGenerator(
                    map, enemy.getLocation(time).toPointI(), skin.getSpeed(), 3, 7, i)
                    .generate(time + delay);
            if (trajectory != null) {
                Enemy frog = addItem(TINY_TOAD, skinId, trajectory, skin.getSpeed(), null, enemy.getId());
                frog.setEnergy(frog.getFullEnergy());
                frogs.add(frog);
            }
        }
        return frogs;
    }

    public List<Enemy> spawnGroupWithTrajectory(GameConfig config, SpawnConfig spawnConfig, SwarmType swarmType,
                                                Trajectory trajectory, long startRoundTime) {
        List<GroupParams> groupParams = map.getPredefinedGroups().get(swarmType.getTypeId());
        for (GroupParams param : groupParams) {
            if (param.getTrajectories().contains(trajectory)) {
                return spawnGroup(config, spawnConfig, param, trajectory, startRoundTime);
            }
        }
        return Collections.emptyList();
    }

    private List<Enemy> spawnGroup(GameConfig config, SpawnConfig spawnConfig, GroupParams group,
                                   Trajectory trajectory, long startRoundTime) {
        List<Enemy> swarm = new ArrayList<>();
        GroupTemplate template = getRandomElement(group.getTemplates());
        int swarmId = template.getId();
        if (allowSpawnToSwarm(swarmId)) {
            for (GroupMember member : template.getEnemies()) {
                if (config.isEnemyEnabled(EnemyType.getById(member.getType()))) {
                    Enemy enemy = createEnemyWithTrajectory(member, trajectory);
                    enemy.addToSwarm(group.getType(), swarmId);
                    swarm.add(enemy);
                }
            }
        }
        if (!swarm.isEmpty()) {
            registerSwarm(swarmId, swarm);
        }
        return swarm;
    }

    private boolean allowSpawnToSwarm(int swarmId) {
        return shouldSwarmSpawn(swarmId);
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

    private Enemy createEnemyWithTrajectory(GroupMember member, Trajectory template) {
        EnemyType enemyType = EnemyType.getById(member.getType());
        Skin skin = enemyType.getSkin(getRandomSkin(enemyType));
        long spawnTime = System.currentTimeMillis() + 1000 + RNG.nextInt(100);
        Trajectory trajectory = prepareTrajectory(spawnTime, template, member.getDx(), member.getDy(),
                member.getDt(), skin.getSpeed());
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

    @Override
    public Map<Long, Trajectory> generateFreezeTrajectories(long time, int freezeTime, double xShot, double yShot, int maxDist) {
        Map<Long, Trajectory> trajectories = new HashMap<>();

        lockEnemy.lock();
        try {
            for (Enemy enemy : getItems()) {
                PointI location = enemy.getLocation(time).toPointI();
                if (WEAPON_CARRIERS.contains(enemy.getEnemyType())) {
                    continue;
                }
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
        return isVisible((int) location.x, (int) location.y);
    }

    public boolean isVisible(int x, int y) {
        // TODO: 19.11.2021 update boss path
        return map.isValid(x, y);// && map.isBossPath(x, y);
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

    public List<Pair<EnemyType, Trajectory>> getEnemyWithPredefinedTrajectoryPairs(SpawnConfig spawnConfig) {
        GameMapShape map = getMapShape();
        List<Pair<EnemyType, Trajectory>> pairs = new ArrayList<>();

        map.getPredefinedTrajectories().forEach((id, enemyTrajectories) -> {
            List<Integer> highEnemyIds = spawnConfig.getHighPayEnemies();
            List<Integer> midEnemyIds = spawnConfig.getMidPayEnemies();
            EnemyType enemyType = getById(id);
            if (HIGH_PAY_ENEMIES.contains(enemyType) &&
                    (highEnemyIds.isEmpty() || (highEnemyIds.contains(id) && HIGH_PAY_ENEMIES.contains(enemyType)))) {
                enemyTrajectories.forEach(trajectory -> pairs.add(new Pair<>(enemyType, trajectory)));
            } else if (MID_PAY_ENEMIES.contains(enemyType) &&
                    (midEnemyIds.isEmpty() || (midEnemyIds.contains(id) && MID_PAY_ENEMIES.contains(enemyType)))) {
                enemyTrajectories.forEach(trajectory -> pairs.add(new Pair<>(enemyType, trajectory)));
            }
        });

        List<Integer> lowPayEnemies = spawnConfig.getLowPayEnemies();
        if (lowPayEnemies.isEmpty() || lowPayEnemies.contains(RUNNER.getId())) {
            map.getPredefinedGroups().get(RUNNERS.getTypeId()).forEach(groupParams ->
                    groupParams.getTrajectories().forEach(trajectory -> pairs.add(new Pair<>(RUNNER, trajectory))));
        }

        return pairs;
    }

    public List<PointD> getCurrentEnemiesLocations() {
        return getItems().stream()
                .filter(enemy -> EnemyRange.getEnemiesFromRanges(LOW_PAY_ENEMIES, MID_PAY_ENEMIES, HIGH_PAY_ENEMIES)
                        .contains(enemy.getEnemyType()))
                .map(enemy -> enemy.getLocation(System.currentTimeMillis()))
                .collect(Collectors.toList());
    }

    public boolean isMapFullOfEnemy(SpawnConfig spawnConfig) {
        if (spawnConfig.getAllEnemiesMax() == -1) {
            return false;
        }
        return getItemsSize() >= spawnConfig.getAllEnemiesMax();
    }
}
