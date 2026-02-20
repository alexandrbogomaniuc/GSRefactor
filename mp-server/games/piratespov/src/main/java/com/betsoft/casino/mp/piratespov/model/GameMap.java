package com.betsoft.casino.mp.piratespov.model;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.maps.BirdsGameMapShape;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.movement.common.Offset;
import com.betsoft.casino.mp.movement.generators.SwarmTrajectoryGenerator3D;
import com.betsoft.casino.mp.movement.generators.TrajectoryGenerator3D;
import com.betsoft.casino.mp.piratescommon.model.math.EnemyPrize;
import com.betsoft.casino.mp.piratescommon.model.math.EnemyType;
import com.betsoft.casino.mp.piratescommon.model.math.MathData;
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
import com.hazelcast.spring.context.SpringAware;
import org.kynosarges.tektosyne.geometry.PointI;

import java.io.IOException;
import java.util.*;

@SpringAware
public class GameMap extends AbstractGameMap<Enemy, GameMapShape, EnemyRange, EnemyType, GameMap> {
    private static final long BOSS_LIFE_TIME = 360000;

    private static final Map<EnemyType, Double> crabProbabilities = new EnumMap<>(EnemyType.class);

    static {
        crabProbabilities.put(EnemyType.ENEMY_4, .35);
        crabProbabilities.put(EnemyType.ENEMY_6, .25);
        crabProbabilities.put(EnemyType.ENEMY_7, .25);
        crabProbabilities.put(EnemyType.ENEMY_8, .25);
    }

    private EnemyRange possibleEnemies;

    //empty constructor required for Kryo serialization
    @SuppressWarnings("unused")
    public GameMap() {
        super();
    }

    public GameMap(EnemyRange possibleEnemies, GameMapShape map) {
        super(map);
        this.possibleEnemies = possibleEnemies;
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
        return !EnemyRange.BaseEnemies.getEnemies().contains(enemy.getEnemyClass().getEnemyType());
    }

    public List<Enemy> addRatsSwarm() {
        EnemyType enemyType = getRandomElement(EnemyRange.RATS.getEnemies());
        long spawnTime = System.currentTimeMillis() + SPAWN_DELAY;
        Trajectory trajectory = new TrajectoryGenerator3D(map, getRandomSpawnPoint(), enemyType.getSkin(1).getSpeed())
                .generate(spawnTime, 15, true);
        return spawnSingleLineSwarm(enemyType, trajectory, RNG.nextInt(7, 10), 0, 300);
    }

    public List<Enemy> addCrabsSwarm() {
        List<Enemy> enemies = new ArrayList<>();
        try {
            int swarmId = generateSwarmId();
            EnemyType leadCrabType = GameTools.getRandomNumberKeyFromMap(crabProbabilities);
            EnemyType minionsType = getRandomElement(EnemyRange.SMALL_CRABS.getEnemies());
            float speed = (float) (leadCrabType.getSkin(1).getSpeed() * (1 + 0.25 * RNG.rand()));
            List<Trajectory> trajectories = generateCrabTrajectories(RNG.nextInt(5, 8), speed);
            if (trajectories != null) {
                Enemy leader = addItem(leadCrabType, 1, trajectories.get(0), speed, createMathEnemy(leadCrabType), -1);
                leader.addToSwarm(SwarmType.CRABS, swarmId);
                enemies.add(leader);
                for (int i = 1; i < trajectories.size(); i++) {
                    Enemy enemy = addItem(minionsType, 1, trajectories.get(i), speed, createMathEnemy(minionsType), leader.getId());
                    enemies.add(enemy);
                    enemy.addToSwarm(SwarmType.CRABS, swarmId);
                }
                registerSwarm(swarmId, enemies);
            }
        } catch (Exception e) {
            getLogger().error("Failed to generate crabs swarm", e);
        }
        return enemies;
    }

    private List<Trajectory> generateCrabTrajectories(int count, float speed) throws Exception {
        long dt = 0;
        SwarmTrajectoryGenerator3D generator = new SwarmTrajectoryGenerator3D(map, getRandomSpawnPoint(), speed);
        for (int i = 1; i < count; i++) {
            dt += RNG.nextInt(500, 1000);
            generator.addEnemyWithOffset(new Offset(0, RNG.rand() * 2 - 1, dt));
        }
        return generator.generateAll(System.currentTimeMillis() + SPAWN_DELAY, 30, true);
    }

    private List<Enemy> spawnSingleLineSwarm(EnemyType enemyType, Trajectory baseTrajectory, int amount, long delay, long interval) {
        List<Enemy> enemies = new ArrayList<>();
        int swarmId = generateSwarmId();
        for (int i = 0; i < amount; i++) {
            Enemy enemy = addItem(enemyType, 1,
                    shiftTrajectory(baseTrajectory, 0, 0, i * interval + delay),
                    (float) baseTrajectory.getSpeed(),
                    createMathEnemy(enemyType),
                    -1);
            enemy.addToSwarm(SwarmType.RATS.getTypeId(), swarmId);
            enemies.add(enemy);
        }
        registerSwarm(swarmId, enemies);
        return enemies;
    }

    @Override
    protected EnemyType getEnemyByTypeId(int typeId) {
        return EnemyType.values()[typeId];
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Enemy createEnemy(EnemyType enemyType, int skinId, Trajectory trajectory, float speed,
                             IMathEnemy mathEnemy, long parentEnemyId) {
        List<EnemyPrize> enemyPrizes = new ArrayList<>(enemyType.getPayTable().keySet());
        short unitSize = 2;
        EnemyClass enemyClass = new EnemyClass(enemyType.getId(),
                unitSize,
                unitSize,
                enemyType.getName(),
                1,
                speed,
                enemyPrizes,
                enemyType
        );
        Enemy enemy;
        if (enemyType.isBoss()) {
            enemy = new EnemyBoss(enemyIdsGenerator.getAndIncrement(), enemyClass, skinId, trajectory, mathEnemy,
                    parentEnemyId, new ArrayList<>(), new ArrayList<>(), 0);
        } else {
            enemy = new Enemy(enemyIdsGenerator.getAndIncrement(), enemyClass, skinId, trajectory, mathEnemy,
                    parentEnemyId, new ArrayList<>());
        }
        enemy.setAwardedPrizes(new ArrayList<>());
        enemy.setAwardedSum(Money.ZERO);
        enemy.setEnergy(enemy.getFullEnergy());
        enemy.setSpeed(speed);
        enemy.setMovementStrategy(new TrajectoryMovementStrategy(enemy, this));
        return enemy;
    }

    public IMathEnemy createMathEnemy(EnemyType enemyType) {
        int[] levels = MathData.getEnemyData(enemyType.getId()).getLevels();
        int healthForFirstLevel = levels[RNG.nextInt(levels.length)];
        return new MathEnemy(0, "", 0, healthForFirstLevel);
    }

    public IMathEnemy createMathEnemyWithLevel(EnemyType enemyType, int level) {
        int[] levels = MathData.getEnemyData(enemyType.getId()).getLevels();
        int healthForFirstLevel = level == -1 ? levels[RNG.nextInt(levels.length)] : levels[level];
        return new MathEnemy(0, "", 0, healthForFirstLevel);
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
            IMovementStrategy movementStrategy = enemy.getMovementStrategy();
            movementStrategy.setMap(this);
        }
        int ordinal = input.readInt(true);
        possibleEnemies = EnemyRange.values()[ordinal];
    }

    @Override
    protected void writeAdditionalFields(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, getInactivityLiveItems());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void readAdditionalFields(byte version, Kryo kryo, Input input) {
        getInactivityLiveItems().clear();
        Map<Enemy, Long> enemies = (Map<Enemy, Long>) kryo.readClassAndObject(input);
        getInactivityLiveItems().putAll(enemies);
        for (Enemy enemy : getInactivityLiveItems().keySet()) {
            IMovementStrategy movementStrategy = enemy.getMovementStrategy();
            movementStrategy.setMap(this);
        }
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
    }

    @Override
    protected void deserializeAdditionalFields(JsonParser p, JsonNode node, DeserializationContext ctxt) {
        getInactivityLiveItems().clear();
        Map<Enemy, Long> enemies = ((ObjectMapper) p.getCodec()).convertValue(node.get("inactivityLiveItems"), new TypeReference<Map<Enemy, Long>>() {});
        getInactivityLiveItems().putAll(enemies);
        for (Enemy enemy : getInactivityLiveItems().keySet()) {
            IMovementStrategy movementStrategy = enemy.getMovementStrategy();
            movementStrategy.setMap(this);
        }
    }

    @Override
    protected GameMap getDeserialized() {
        return this;
    }

    @Override
    protected Trajectory getTrajectory(EnemyType enemyType, float speed, boolean needStandOnPlace, boolean needNearCenter,
                                       int skinId, boolean needFinalSteps) {
        if (EnemyRange.TROLLS.getEnemies().contains(enemyType)) {
            return generateRandomTrollTrajectory(speed);
        }
        if (EnemyRange.BIRDS.getEnemies().contains(enemyType)) {
            getLogger().error("Invalid trajectory generation for birds invoked");
        }
        if (EnemyType.WEAPON_CARRIER.equals(enemyType)) {
            long spawnTime = System.currentTimeMillis() + 1000;
            return getWeaponCarrierTrajectoryGenerator(speed)
                    .generate(spawnTime, 30, false);
        }
        if (EnemyType.Boss.equals(enemyType)) {
            long spawnTime = System.currentTimeMillis() + 1000;
            return new Trajectory(1)
                    .addPoint(map.getBossSpawnPoint().x, map.getBossSpawnPoint().y, spawnTime)
                    .addPoint(map.getBossSpawnPoint().x, map.getBossSpawnPoint().y, spawnTime + BOSS_LIFE_TIME);
        }
        return super.getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps);
    }

    private Trajectory generateUpdatedTrajectory(Enemy enemy, boolean needFinalSteps) {
        EnemyType enemyType = enemy.getEnemyType();
        float speed = (float) enemy.getSpeed();
        TrajectoryGenerator3D generator;
        if (EnemyRange.TROLLS.getEnemies().contains(enemyType)) {
            generator = getTrollTrajectoryGenerator(PointI.EMPTY, speed);
        } else if (EnemyType.WEAPON_CARRIER.equals(enemyType)) {
            generator = getWeaponCarrierTrajectoryGenerator(speed);
        } else {
            generator = new TrajectoryGenerator3D(map, PointI.EMPTY, speed);
        }
        return generator.generateUpdateTrajectory(enemy.getTrajectory(), System.currentTimeMillis(), 30, needFinalSteps);
    }

    public Trajectory generateRandomTrollTrajectory(float speed) {
        long spawnTime = System.currentTimeMillis() + 1000;
        for (int i = 0; i < 100; i++) {
            PointI source = getRandomSpawnPoint();
            Trajectory trajectory = getTrollTrajectoryGenerator(source, speed)
                    .generate(spawnTime, 15, false);
            if (!trajectory.isEmpty()) {
                return trajectory;
            }
        }
        return null;
    }

    private TrajectoryGenerator3D getTrollTrajectoryGenerator(PointI source, float speed) {
        return new TrajectoryGenerator3D(map, source, speed)
                .setMinStep(8)
                .setMaxStep(10)
                .setAnimationDelay(2000);
    }

    private TrajectoryGenerator3D getWeaponCarrierTrajectoryGenerator(float speed) {
        IGameMapShape birdsMap = new BirdsGameMapShape(map);
        PointI source = getRandomElement(birdsMap.getSpawnPoints());
        return new TrajectoryGenerator3D(birdsMap, source, speed)
                .setMinStep(2)
                .setMaxStep(20)
                .setAngleStep(10)
                .setMaxDeltaAngle(60);
    }

    public List<Trajectory> generateBirdsTrajectories(double speed, List<Offset> offsets) throws Exception {
        long spawnTime = System.currentTimeMillis() + 1000;
        IGameMapShape birdsMap = new BirdsGameMapShape(map);
        PointI source = getRandomElement(birdsMap.getSpawnPoints());
        SwarmTrajectoryGenerator3D generator = new SwarmTrajectoryGenerator3D(birdsMap, source, speed);
        generator.setMinStep(2)
                .setMaxStep(20)
                .setAngleStep(10)
                .setMaxDeltaAngle(60);
        for (Offset offset : offsets) {
            generator.addEnemyWithOffset(offset);
        }
        return generator.generateAll(spawnTime, 30, true);
    }

    @Override
    protected Trajectory getTrajectory(EnemyType enemyType, float speed, boolean needStandOnPlace, boolean needNearCenter,
                                       int skinId, boolean needFinalSteps, boolean useCustomTrajectories) {
        return getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps);
    }

    @Override
    protected boolean isOgre(IEnemyType enemyType) {
        return EnemyRange.MINI_BOSS.getEnemies().contains(enemyType);
    }

    @Override
    public Enemy addEnemyByTypeNew(EnemyType enemyType, IMathEnemy mathEnemy, int skinId, long parentEnemyId,
                                   boolean needNearCenter, boolean needFinalSteps, boolean useCustomTrajectories) {
        int skin = (skinId == -1 ? getRandomSkin(enemyType) : skinId);
        boolean needToRush = !needNearCenter && !enemyType.isBoss();
        float speed = needToRush ? getMaxSpeed(enemyType, skin) : generateSpeed(enemyType.getSkin(skin));
        Trajectory trajectory = getTrajectory(enemyType, speed, isNeedStandOnPlace(enemyType, skinId), needNearCenter,
                skin, needFinalSteps);
        if (trajectory != null) {
            Enemy enemy = addItem(enemyType, skin, trajectory, speed, mathEnemy, parentEnemyId);
            enemy.setEnergy(enemy.getFullEnergy());
            return enemy;
        } else {
            return null;
        }
    }

    @Override
    public Map<Long, Trajectory> generateUpdateTrajectories(boolean needFinalSteps) {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        lockEnemy.lock();
        try {
            long currentTime = System.currentTimeMillis();
            for (Enemy enemy : getItems()) {
                if (!enemy.isPartOfSwarm() && enemy.isLocationNearEnd(currentTime)) {
                    Trajectory trajectory = generateUpdatedTrajectory(enemy, needFinalSteps);
                    if (trajectory != null && !trajectory.isEmpty()) {
                        enemy.setTrajectory(trajectory);
                        trajectories.put(enemy.getId(), trajectory);
                        getLogger().info("Trajectory updated for enemy {}, endTime: {}", enemy.getId(), trajectory.getLastPoint().getTime());
                    }
                }
            }
//            for (Swarm<Enemy> swarm : swarms.values()) {
//                Enemy enemy = getItemById(swarm.getEnemyIds().get(0));
//                if (enemy != null && enemy.isLocationNearEnd(currentTime)) {
//                    updateSwarmTrajectories(swarm, trajectories, currentTime);
//                }
//            }
        } finally {
            lockEnemy.unlock();
        }
        return trajectories;
    }

    private void updateSwarmTrajectories(Swarm<Enemy> swarm, Map<Long, Trajectory> trajectories, long currentTime) {
        if (swarm.getSwarmType() == SwarmType.RATS.getTypeId()) {
            updateRatsTrajectories(swarm, trajectories, currentTime);
        } else if (swarm.getSwarmType() == SwarmType.CRABS.getTypeId()) {
            updateCrabsTrajectories(swarm, trajectories, currentTime);
        } else if (swarm.getSwarmType() == SwarmType.WHITE_BIRDS.getTypeId()
                || swarm.getSwarmType() == SwarmType.RED_BIRDS.getTypeId()) {
            updateBirdsTrajectories(swarm, trajectories, currentTime);
        }
    }

    private void updateRatsTrajectories(Swarm<Enemy> swarm, Map<Long, Trajectory> trajectories, long currentTime) {
        List<Enemy> enemies = getSwarmEnemies(swarm);
        if (!enemies.isEmpty()) {
            Enemy firstEnemy = enemies.get(0);
            long baseTime = firstEnemy.getTrajectory().getPoints().get(0).getTime();
            Trajectory baseTrajectory = new TrajectoryGenerator3D(map, PointI.EMPTY, firstEnemy.getSpeed())
                    .generateUpdateTrajectoryWithoutOldPoints(firstEnemy.getTrajectory(), 30, false);
            firstEnemy.setTrajectory(baseTrajectory);
            trajectories.put(firstEnemy.getId(), baseTrajectory);
            for (int i = 1; i < enemies.size(); i++) {
                Enemy enemy = enemies.get(i);
                long dt = enemy.getTrajectory().getPoints().get(0).getTime() - baseTime;
                Trajectory trajectory = combineTrajectories(enemy.getTrajectory(), shiftTrajectory(baseTrajectory, 0, 0, dt), currentTime);
                enemy.setTrajectory(trajectory);
                trajectories.put(enemy.getId(), trajectory);
            }
        }
    }

    private Trajectory combineTrajectories(Trajectory original, Trajectory updated, long currentTime) {
        Trajectory trajectory = new Trajectory(original.getSpeed());
        List<Point> points = original.getPoints();
        for (int i = original.getIndexOfFirstPassedPoint(currentTime); i < points.size(); i++) {
            trajectory.addPoint(points.get(i));
        }
        for (Point point : updated.getPoints()) {
            trajectory.addPoint(point);
        }
        return trajectory;
    }

    private void updateCrabsTrajectories(Swarm<Enemy> swarm, Map<Long, Trajectory> trajectories, long currentTime) {

    }

    private void updateBirdsTrajectories(Swarm<Enemy> swarm, Map<Long, Trajectory> trajectories, long currentTime) {

    }

    @Override
    public Set<Long> getEnemiesForNewEnemyUpdating(boolean needFinalSteps, boolean needReturnAllEnemies) {
        if (!getInactivityLiveItems().isEmpty())
            clearInactivityLiveItems();
        return new HashSet<>();
    }

    @Override
    protected Trajectory getRandomTrajectory(IEnemyType enemyType, double speed, short minSteps, boolean needFinalSteps) {
        long spawnTime = System.currentTimeMillis() + 1000;
        PointI source = getRandomSpawnPoint();
        return new TrajectoryGenerator3D(map, source, speed)
                .generate(spawnTime, minSteps, needFinalSteps);
    }

    @Override
    protected Trajectory getInitialTrajectory(double speed, boolean needFinalSteps, IEnemyType enemyType) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Trajectory generateLeaveTrajectory(ITrajectoryGenerator generator, PointI location, long startTime, Enemy enemy) {
        if (enemy.getSkin() == PlayGameState.LEVIATHAN_SKIN_ID) {
            return new Trajectory(0, Arrays.asList(
                    new Point(location.x, location.y, startTime),
                    new Point(location.x, location.y, startTime + 10000)));
        }
        return super.generateLeaveTrajectory(generator, location, startTime, enemy);
    }

    @Override
    public boolean needDoubleSpeed(int enemyTypeId) {
        return false;
    }

    @Override
    protected void generateFreezePoints(Trajectory oldTrajectory, long startTime, ArrayList<Point> points, long time) {
        long newTime = time;
        for (Point point : oldTrajectory.getPoints()) {
            if (!point.isFreezePoint() && (point.getTime() > startTime)) {
                newTime += 3000;
                points.add(point.create(point.getX(), point.getY(), newTime));
            }
        }
    }

    @Override
    protected Trajectory getRandomTrajectory(double speed, boolean needFinalSteps) {
        long spawnTime = System.currentTimeMillis() + 1000;
        PointI source = getRandomSpawnPoint();
        return new TrajectoryGenerator3D(map, source, speed)
                .generate(spawnTime, 30, needFinalSteps);
    }

    @Override
    public boolean notNeedFreezeAtStartPoint(IEnemyType enemyType) {
        return false;
    }

    @Override
    public boolean isNeedStandOnPlace(IEnemyType enemyType, int skinId) {
        return false;
    }

    @Override
    public int getTrajectoryDuration() {
        return 10000;
    }

    @Override
    protected void addPointsFromOldTrajectory(List<Point> points, long startTime, long freezeTime, PointI location, Enemy enemy) {
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

    public List<Enemy> respawnEnemies(LimitChecker limitChecker) {
        long now = System.currentTimeMillis();
        List<Enemy> respawnedEnemies = new ArrayList<>();
        List<Long> respawnedIds = new ArrayList<>();
        for (Enemy enemy : removedEnemies.values()) {
            EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
            if (enemy.getReturnTime() > 0 && enemy.getReturnTime() < now && limitChecker.isSpawnAllowed(enemyType)) {
                Trajectory trajectory = getTrajectory(enemyType, (float) enemy.getSpeed(), false,
                        false, enemy.getSkin(), false);
                enemy.setTrajectory(trajectory);
                enemy.setReturnTime(0);
                addItem(enemy);
                respawnedEnemies.add(enemy);
                respawnedIds.add(enemy.getId());
                limitChecker.countEnemy(enemy);
            }
        }
        respawnedIds.forEach(id -> removedEnemies.remove(id));
        return respawnedEnemies;
    }

    @Override
    public Coords getCoords() {
        if (coords == null) {
            coords = new Coords3D(map.getWidth() / 2, map.getHeight() / 2);
        }
        return coords;
    }

    @Override
    public Map<Long, Trajectory> generateShortLeaveTrajectories() {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        lockEnemy.lock();
        try {
            long startTime = System.currentTimeMillis();
            ITrajectoryGenerator generator = new WaypointTrajectoryGenerator(map);
            for (Enemy enemy : getItems()) {
                List<Point> freezePoints = getActiveFreezePoints(enemy.getTrajectory(), startTime);
                if (freezePoints.isEmpty()) {
                    PointI location = enemy.getLocation(startTime).toPointI();
                    Trajectory trajectory = generateLeaveTrajectory(generator, location, startTime, enemy);
                    if (trajectory != null) {
                        enemy.setTrajectory(trajectory);
                        trajectories.put(enemy.getId(), trajectory);
                    }
                } else {
                    Point lastFreezePoint = freezePoints.get(freezePoints.size() - 1);
                    PointI location = new PointI((int) lastFreezePoint.getX(), (int) lastFreezePoint.getY());
                    Trajectory trajectory = generateLeaveTrajectory(generator, location, lastFreezePoint.getTime(), enemy);
                    if (trajectory != null) {
                        Trajectory combined = combineTrajectories(freezePoints, trajectory);
                        enemy.setTrajectory(combined);
                        trajectories.put(enemy.getId(), combined);
                    }
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return trajectories;
    }

    private List<Point> getActiveFreezePoints(Trajectory trajectory, long time) {
        List<Point> freezePoints = new ArrayList<>();
        List<Point> points = trajectory.getPoints();
        int i = 0;
        while (i < points.size() && points.get(i).getTime() < time) {
            i++;
        }
        while (i < points.size() && points.get(i).isFreezePoint()) {
            freezePoints.add(points.get(i));
            i++;
        }
        return freezePoints;
    }

    private Trajectory combineTrajectories(List<Point> points, Trajectory trajectory) {
        Trajectory combined = new Trajectory(trajectory.getSpeed());
        for (Point point : points) {
            combined.addPoint(point);
        }
        for (Point point : trajectory.getPoints()) {
            combined.addPoint(point);
        }
        return combined;
    }
}
