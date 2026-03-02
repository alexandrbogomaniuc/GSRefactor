package com.betsoft.casino.mp.piratesdmc.model;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.maps.BirdsGameMapShape;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.common.scenarios.SpawnGroup;
import com.betsoft.casino.mp.common.scenarios.SpawnScenario;
import com.betsoft.casino.mp.common.scenarios.TypeAndSkin;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.InvulnerablePoint;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import com.betsoft.casino.mp.movement.common.Offset;
import com.betsoft.casino.mp.movement.generators.SwarmTrajectoryGenerator3D;
import com.betsoft.casino.mp.movement.generators.TrajectoryGenerator3D;
import com.betsoft.casino.mp.piratescommon.model.math.EnemyPrize;
import com.betsoft.casino.mp.piratescommon.model.math.EnemyType;
import com.betsoft.casino.mp.piratescommon.model.math.MathData;
import com.dgphoenix.casino.common.util.Pair;
import com.abs.casino.common.util.RNG;
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
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.io.IOException;
import java.util.*;

import static com.betsoft.casino.mp.piratesdmc.model.SwarmType.RACING_RATS;

@SpringAware
public class GameMap extends AbstractGameMap<Enemy, GameMapShape, EnemyRange, EnemyType, GameMap> {
    private static final int CRABS_GROUP = -1;
    protected static final int FREEZE_TIME_MAX = 3000;
    private static final int TRAJECTORY_DURATION = 30000;
    private static final long TRAJECTORY_COOLDOWN = 3000;

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

    public List<Enemy> addScarabSwarm(HashSet<Integer> liveIdScenariosCrabs, int max) {
        List<SpawnScenario> scenarios = map.getScenarios();
        if (scenarios != null && !scenarios.isEmpty()) {
            SpawnScenario scenario = null;
            int cnt = 100;
            while (cnt-- > 0) {
                scenario = getRandomElement(scenarios);
                if (!liveIdScenariosCrabs.contains(scenario.getId()))
                    break;
            }
            if (checkCooldown(scenario.getId()) && getScenarioSize(scenario) < max) {
                return spawnByScenario(scenario);
            }
        }
        return new ArrayList<>();
    }

    public List<Enemy> addRatsSwarm(HashSet<Integer> liveIdSwarmParamsRats, int max) {
        List<SwarmParams> swarmParams = map.getSwarmParams();

        if (!swarmParams.isEmpty() && RNG.nextBoolean()) {
            SwarmParams params = null;
            int cnt = 100;
            while (cnt-- > 0) {
                params = getRandomElement(swarmParams);
                if (!liveIdSwarmParamsRats.contains(params.getId()))
                    break;
            }
            if (cnt > 0) {
                if (checkCooldown(params.getId()) && max >= params.getMinSize()) {
                    return spawnRats(params, RNG.nextInt(params.getMinSize(), Math.min(params.getMaxSize(), max)));
                }
            } else
                return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private List<Enemy> spawnRats(SwarmParams params, int amount) {
        List<Enemy> rats = new ArrayList<>();
        long spawnTime = System.currentTimeMillis() + SPAWN_DELAY;
        scenarioCooldowns.put(params.getId(), spawnTime + params.getCooldown());
        TypeAndSkin typeAndSkin = getRandomElement(params.getEnemies());
        int swarmId = swarmIdGenerator.getAndIncrement();
        for (int i = 0; i < amount; i++) {
            EnemyType ratType = EnemyType.getById(typeAndSkin.getType());
            Skin skin = ratType.getSkin(typeAndSkin.getSkin());
            Trajectory trajectory = new RatSwarmTrajectoryGenerator(map, coords, params.getAngle(), 20, 35, 35)
                    .generate(new PointD(params.getStartX(), params.getStartY()),
                            params.getDistance(), skin.getSpeed() * 2.25, skin.getSpeedDeltaPositive() / 2, spawnTime, 500);
            Enemy enemy = addItem(ratType, typeAndSkin.getSkin(), trajectory, (float) trajectory.getSpeed(),
                    createMathEnemy(ratType), -1);
            enemy.addToSwarm(RACING_RATS, swarmId);
            enemy.setEnergy(enemy.getFullEnergy());
            enemy.setCurrentTrajectoryId(params.getId());
            rats.add(enemy);
        }
        registerSwarm(swarmId, rats);
        return rats;
    }

    private List<Enemy> spawnByScenario(SpawnScenario scenario) {
        List<Enemy> enemies = new ArrayList<>();
        long spawnTime = System.currentTimeMillis() + SPAWN_DELAY;
        scenarioCooldowns.put(scenario.getId(), spawnTime + scenario.getCooldown());
        int offsetX = RNG.nextInt(scenario.getOffsetX() + 1);
        int offsetY = RNG.nextInt(scenario.getOffsetY() + 1);
        boolean reverseTrajectory = RNG.nextBoolean();
        Trajectory scenarioTrajectory = getAvailableTrajectory(scenario.getTrajectoryIds(), spawnTime);

        for (SpawnGroup group : scenario.getGroups()) {
            TypeAndSkin mainEnemy = group.getMainEnemy();
            Trajectory baseTrajectory = scenarioTrajectory != null
                    ? scenarioTrajectory
                    : getSpawnGroupTrajectory(group, spawnTime);

            if (reverseTrajectory) {
                List<Point> newPoints = new ArrayList<>(baseTrajectory.getPoints());
                Collections.reverse(newPoints);
                double speed = baseTrajectory.getSpeed();
                baseTrajectory = new Trajectory(speed, newPoints);
            }

            double baseDistance = TrajectoryUtils.calculateTotalDistance(baseTrajectory);
            double baseSpeed = baseDistance * 1000 / group.getTravelTime();
            int delayTime = group.getDelayTime();

            List<Point> mainEnemyPoints = new ArrayList<>();
            if (mainEnemy != null) {
                Trajectory trajectory = TrajectoryUtils.generateSimilarTrajectory(baseTrajectory, offsetX, offsetY,
                        group.getDeltaX(), group.getDeltaY(), baseSpeed, 0, spawnTime, group.getStartTime());
                EnemyType enemyType = EnemyType.getById(mainEnemy.getType());
                Enemy enemy = addItem(enemyType,
                        mainEnemy.getSkin(), trajectory, (float) trajectory.getSpeed(),
                        createMathEnemy(enemyType), -1);
                enemy.setCurrentTrajectoryId(scenario.getId());
                enemy.setEnergy(enemy.getFullEnergy());
                enemies.add(enemy);
                spawnTime = spawnTime + delayTime;
                mainEnemyPoints = trajectory.getPoints();
            }

            if (group.isNeedRetinueEnemies()) {
                TypeAndSkin typeAndSkin = group.getEnemies().get(0);
                EnemyType type = EnemyType.getById(typeAndSkin.getType());
                for (int idxRetinue = 0; idxRetinue < 6; idxRetinue++) {
                    Trajectory trajectory = TrajectoryUtils.generateRetinueTrajectory(mainEnemyPoints,
                            offsetX, offsetY, group.getDeltaX(), group.getDeltaY(), baseSpeed,
                            spawnTime, group.getStartTime(), idxRetinue);
                    Enemy enemy = addItem(type, typeAndSkin.getSkin(), trajectory, (float) trajectory.getSpeed(),
                            createMathEnemy(type), -1);
                    enemy.setCurrentTrajectoryId(scenario.getId());
                    enemy.setEnergy(enemy.getFullEnergy());
                    enemies.add(enemy);
                }
            } else {
                int groupSize = RNG.nextInt(group.getMinSize(), group.getMaxSize() + 1);

                TypeAndSkin typeAndSkin;
                try {
                    typeAndSkin = getRandomElement(group.getEnemies());
                } catch (Exception e) {
                    getLogger().error("Failed to spawn by scenario" + group, e);
                    return new ArrayList<>();
                }

                EnemyType type = EnemyType.getById(typeAndSkin.getType());
                for (int i = 0; i < groupSize; i++) {
                    long spawnTime_ = spawnTime + (i * delayTime + delayTime);
                    Trajectory trajectory = TrajectoryUtils.generateSimilarTrajectory(baseTrajectory, offsetX, offsetY,
                            group.getDeltaX(), group.getDeltaY(), baseSpeed, 0, spawnTime_, group.getStartTime());
                    Enemy enemy = addItem(type, typeAndSkin.getSkin(), trajectory, (float) trajectory.getSpeed(),
                            createMathEnemy(type), -1);
                    enemy.setCurrentTrajectoryId(scenario.getId());
                    enemy.setEnergy(enemy.getFullEnergy());
                    enemies.add(enemy);
                }
            }
        }
        return enemies;
    }

    private Trajectory getSpawnGroupTrajectory(SpawnGroup group, long time) {
        Trajectory trajectory = getAvailableTrajectory(group.getTrajectoryIds(), time);
        if (trajectory == null) {
            return group.getTrajectory();
        }
        return trajectory;
    }

    private Trajectory getAvailableTrajectory(List<Integer> trajectoryIds, long time) {
        if (trajectoryIds == null || trajectoryIds.isEmpty()) {
            return null;
        }
        List<Integer> availableTrajectories = new ArrayList<>();
        for (int trajectoryId : trajectoryIds) {
            if (trajectoryCooldowns.getOrDefault(trajectoryId, 0L) < time) {
                availableTrajectories.add(trajectoryId);
            }
        }
        // If all trajectories already used, ignore cooldown to increase density of enemies in room
        int trajectoryId = availableTrajectories.isEmpty()
                ? getRandomElement(trajectoryIds)
                : getRandomElement(availableTrajectories);
        trajectoryCooldowns.put(trajectoryId, time + TRAJECTORY_COOLDOWN);
        return map.getTrajectory(trajectoryId);
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
        List<Integer> predefinedTrajectoryIds = map.getPredefinedTrajectoryIds(enemyType);
        if (predefinedTrajectoryIds != null && !predefinedTrajectoryIds.isEmpty()) {
            long time = System.currentTimeMillis();
            return prepareTrajectory(getAvailableTrajectory(predefinedTrajectoryIds, time), speed);
        } else {
            List<Trajectory> trajectories = map.getPredefinedTrajectories(enemyType.getId(), skinId);
            if (trajectories != null && !trajectories.isEmpty()) {
                return prepareTrajectory(getRandomElement(trajectories), speed);
            }
        }
        return super.getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps);
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
        if (useCustomTrajectories) {
            List<Integer> bossRoundTrajectoryIds = map.getPredefinedTrajectoryIds(10000 + enemyType.getId());
            if (bossRoundTrajectoryIds != null) {
                long time = System.currentTimeMillis();
                return prepareTrajectory(getAvailableTrajectory(bossRoundTrajectoryIds, time), speed);
            }
        }
        return getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps);
    }

    @Override
    protected boolean isOgre(IEnemyType enemyType) {
        return EnemyRange.MINI_BOSS.getEnemies().contains(enemyType);
    }

    protected Pair<Integer, Trajectory> getTrajectoryWithSaveStartPosition(EnemyType enemyType, float speed,
                                                                           boolean needStandOnPlace, boolean needNearCenter,
                                                                           int skinId, boolean needFinalSteps, boolean useCustomTrajectories) {
        List<Trajectory> trajectories = map.getPredefinedTrajectories(enemyType.getId(), skinId);
        if (trajectories != null && !trajectories.isEmpty()) {
            int index = RNG.nextInt(trajectories.size());
            return new Pair<>(index, prepareTrajectory(trajectories.get(index), speed));
        }
        return new Pair<>(0, super.getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps, useCustomTrajectories));
    }

    @Override
    public Enemy addEnemyByTypeNew(EnemyType enemyType, IMathEnemy mathEnemy, int skinId, long parentEnemyId,
                                   boolean needNearCenter, boolean needFinalSteps, boolean useCustomTrajectories) {
        if (EnemyRange.BIRDS.getEnemies().contains(enemyType)) {
            int skin = skinId == -1 ? getRandomSkin(enemyType) : skinId;
            float speed = generateSpeed(enemyType.getSkin(skin));
            Pair<Integer, Trajectory> trajectoryWithSaveStartPosition =
                    getTrajectoryWithSaveStartPosition(enemyType, speed, false, needNearCenter, skin, needFinalSteps, useCustomTrajectories);
            Enemy enemy = addItem(enemyType, skin, trajectoryWithSaveStartPosition.getValue(),
                    speed, mathEnemy, parentEnemyId);
            enemy.setCurrentTrajectoryId(trajectoryWithSaveStartPosition.getKey());
            enemy.setEnergy(enemy.getFullEnergy());
            return enemy;
        } else {
            int skin = (skinId == -1 ? getRandomSkin(enemyType) : skinId);
            boolean needToRush = !needNearCenter && !enemyType.isBoss();
            float speed = needToRush ? getMaxSpeed(enemyType, skin) : generateSpeed(enemyType.getSkin(skin));
            Enemy enemy = addItem(enemyType, skin, needToRush ? getInitialTrajectory(speed, needFinalSteps, enemyType, true)
                            : getTrajectory(enemyType, speed, isNeedStandOnPlace(enemyType, skinId), needNearCenter,
                            skin, needFinalSteps, useCustomTrajectories),
                    speed, mathEnemy, parentEnemyId);
            enemy.setEnergy(enemy.getFullEnergy());
            return enemy;
        }
    }


    public int getNumberOfEnemiesWithTrajectoryId(int trajectoryId) {
        int res = 0;
        lockEnemy.lock();
        try {
            for (Enemy enemy : items) {
                Long enemyTrajectoryId = enemy.getTrajectory().getId();
                if (enemy.getCurrentTrajectoryId() == trajectoryId || (enemyTrajectoryId != null && trajectoryId == enemyTrajectoryId)) {
                    res++;
                    break;
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return res;
    }

    // TODO: refactor, add Factory Method for TrajectoryGenerator into EnemyType
    public Map<Long, Trajectory> generateUpdateTrajectories(boolean needFinalSteps) {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        lockEnemy.lock();
        try {
            long startTime = System.currentTimeMillis();
            for (Enemy enemy : getItems()) {
                if ((needFinalSteps && !enemy.isBoss()) || EnemyRange.Scarabs.getEnemies().contains(enemy.getEnemyClass().getEnemyType())) {
                    continue;
                }

                List<Point> points = enemy.getTrajectory().getPoints();
                if (points.size() == 0) {
                    getLogger().debug("enemy has empty trajectory: {}", enemy);
                }
                if (enemy.isLocationNearEnd(startTime)) {
                    PointI location = enemy.getLocation(startTime).toPointI();
                    boolean needDoubleSpeed = needDoubleSpeed(enemy.getEnemyClass().getEnemyType().getId());
                    float speed = generateSpeed(enemy.getEnemyClass().getEnemyType().getSkin(enemy.getSkin()));
                    double speedNew = (needDoubleSpeed && RNG.nextBoolean()) ? speed * 3 : speed;
                    Trajectory trajectory;
                    if (isNeedStandOnPlace(enemy.getEnemyClass().getEnemyType(), enemy.getSkin())) {
                        trajectory = new StandOnPlaceTrajectoryGenerator(map, getBossSpawnPoint(enemy.getSkin()), speed)
                                .generate(enemy.getTrajectory(), enemy.getTrajectory().getLeaveTime(), 100, false);
                    } else {
                        TrajectoryGenerator trajectoryGenerator;
                        if (isOgre(enemy.getEnemyClass().getEnemyType())) {
                            trajectoryGenerator = new JumpTrajectoryGenerator(map, location, speedNew);
                        } else if (enemy.getEnemyClass().getEnemyType().getId() == 0 || enemy.getEnemyClass().getEnemyType().getId() == 4) {
                            trajectoryGenerator = new FreeAngleTrajectoryGenerator(map, location, speedNew);
                        } else {
                            trajectoryGenerator = new TrajectoryGenerator(map, location, speedNew);
                        }
                        trajectory = trajectoryGenerator.generateWithDuration(startTime, TRAJECTORY_DURATION,
                                false);
                    }
                    if (trajectory != null) {
                        enemy.setTrajectory(trajectory);
                        trajectories.put(enemy.getId(), trajectory);
                    }
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return trajectories;
    }

    @Override
    public Set<Long> getEnemiesForNewEnemyUpdating(boolean needFinalSteps, boolean needReturnAllEnemies) {
        if (!getInactivityLiveItems().isEmpty())
            clearInactivityLiveItems();
        return new HashSet<>();
    }

    private Trajectory prepareTrajectory(Trajectory template, float speed) {
        Trajectory trajectory = new Trajectory(template.getId(), template.getSpeed());
        long time = System.currentTimeMillis() + 1000;
        Iterator<Point> points = template.getPoints().iterator();
        Point current = points.next();
        trajectory.addPoint(current.getX(), current.getY(), time);
        while (points.hasNext()) {
            Point next = points.next();
            double dx = current.getX() - next.getX();
            double dy = current.getY() - next.getY();
            time += (long) (Math.sqrt(dx * dx + dy * dy) / (speed / 1000));
            trajectory.addPoint(next.getX(), next.getY(), time);
            current = next;
        }
        return trajectory;
    }

    @Override
    protected Trajectory getRandomTrajectory(IEnemyType enemyType, double speed, short minSteps, boolean needFinalSteps) {
        if (EnemyRange.MINI_BOSS.getEnemies().contains(enemyType)) {
            Trajectory trajectory = new Trajectory(0, new ArrayList<>());
            do {
                PointI source = getRandomSpawnPoint();
                try {
                    trajectory = new MinStepTrajectoryGenerator(map, source, speed, 17, 22)
                            .generate(System.currentTimeMillis() + 1000, minSteps);
                } catch (Exception e) {
                    getLogger().error("Failed to generate trajectory from " + source, e);
                }
            } while (trajectory.getPoints().isEmpty());
            return trajectory;
        }
        if (EnemyRange.Scarabs.getEnemies().contains(enemyType)) {
            PointI source = getRandomSpawnPoint();
            return new FreeAngleTrajectoryGenerator(map, source, speed)
                    .generate(System.currentTimeMillis() + 1000, minSteps, needFinalSteps);
        }
        return super.getRandomTrajectory(enemyType, speed, minSteps, needFinalSteps);
    }

    @Override
    protected Trajectory getInitialTrajectory(double speed, boolean needFinalSteps, IEnemyType enemyType) {
        long time = System.currentTimeMillis();
        List<Integer> predefinedTrajectoryIds = map.getPredefinedTrajectoryIds(enemyType);
        if (predefinedTrajectoryIds != null && !predefinedTrajectoryIds.isEmpty()) {
            return prepareTrajectory(getAvailableTrajectory(predefinedTrajectoryIds, time), (float) speed);
        } else {
            List<Trajectory> trajectories = map.getPredefinedTrajectories(enemyType.getId(), 1);
            if (trajectories != null && !trajectories.isEmpty()) {
                return prepareTrajectory(trajectories.get(RNG.nextInt(trajectories.size())), (float) speed);
            }
            if (enemyType.getId() == 0 || enemyType.getId() == 4) {
                PointI source = getRandomSpawnPoint();
                return new FreeAngleTrajectoryGenerator(map, source, speed)
                        .generate(time + 1000, 7, needFinalSteps);
            }
            long spawnTime = time + 1000;
            PointI source = getRandomSpawnPoint();
            return new TrajectoryGenerator(map, source, speed).generate(spawnTime, 2, needFinalSteps);
        }
    }

    protected Trajectory getInitialTrajectory(double speed, boolean needFinalSteps, IEnemyType enemyType, boolean useCustomTrajectories) {
        if (useCustomTrajectories) {
            List<Integer> bossRoundTrajectoryIds = map.getPredefinedTrajectoryIds(10000 + enemyType.getId());
            if (bossRoundTrajectoryIds != null) {
                long time = System.currentTimeMillis();
                return prepareTrajectory(getAvailableTrajectory(bossRoundTrajectoryIds, time), (float) speed);
            }
        }
        return getInitialTrajectory(speed, needFinalSteps, enemyType);
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
        return enemyTypeId == 4;
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
        return new TrajectoryGenerator(map, source, speed).generateWithDurationAndMinSteps(spawnTime,
                getTrajectoryDuration(), needFinalSteps, 8);
    }

    @Override
    public boolean notNeedFreezeAtStartPoint(IEnemyType enemyType) {
        return false;
    }

    @Override
    public boolean isNeedStandOnPlace(IEnemyType enemyType, int skinId) {
        return enemyType.isBoss() && getMapId() == 802 && skinId == PlayGameState.LEVIATHAN_SKIN_ID;
    }

    @Override
    public int getTrajectoryDuration() {
        return 10000;
    }

    @Override
    protected Trajectory getBossTrajectory(double speed, boolean needStandOnThePlace, int skinId) {
        if (mapId == 802) {
            long spawnTime = System.currentTimeMillis() + 2000;
            PointI spawnPoint = getBossSpawnPoint(skinId);
            List<Point> points = new ArrayList<>();
            points.add(new InvulnerablePoint(spawnPoint.x, spawnPoint.y, spawnTime));
            points.add(new Point(spawnPoint.x, spawnPoint.y, spawnTime + getBossInvulnerabilityTime(skinId)));
            long spawnEndTime = spawnTime + getBossSpawnAnimationDuration(skinId);
            points.add(new Point(spawnPoint.x, spawnPoint.y, spawnEndTime));

            // We should move above luke on ship without checks for passable cells
            int dy = 12;
            long dt = (long) (dy / (speed / 1000));
            points.add(new Point(spawnPoint.x, spawnPoint.y + dy, spawnEndTime + dt));

            return getBossTrajectoryGenerator(new PointI(spawnPoint.x, spawnPoint.y + dy), speed, needStandOnThePlace)
                    .generate(new Trajectory(speed, points), spawnEndTime + dt, 70, false);
        } else {
            return super.getBossTrajectory(speed, needStandOnThePlace, skinId);
        }
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

    public List<Enemy> respawnEnemies(LimitChecker limitChecker, boolean customTrajectories) {
        long now = System.currentTimeMillis();
        List<Enemy> respawnedEnemies = new ArrayList<>();
        List<Long> respawnedIds = new ArrayList<>();
        for (Enemy enemy : removedEnemies.values()) {
            EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
            if (enemy.getReturnTime() > 0 && enemy.getReturnTime() < now && limitChecker.isSpawnAllowed(enemyType)) {
                Trajectory trajectory = getTrajectory(enemyType, (float) enemy.getSpeed(), false,
                        false, enemy.getSkin(), true, customTrajectories);
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
}
