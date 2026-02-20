package com.betsoft.casino.mp.amazon.model;

import com.betsoft.casino.mp.amazon.model.math.*;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.common.scenarios.TypeAndSkin;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.FreezePoint;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.TeleportPoint;
import com.betsoft.casino.mp.model.movement.Trajectory;
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
import com.hazelcast.spring.context.SpringAware;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.io.IOException;
import java.util.*;

import static com.betsoft.casino.mp.amazon.model.math.EnemyRange.LargeEnemies;
import static com.betsoft.casino.mp.amazon.model.math.EnemyType.SHAMAN;
import static com.betsoft.casino.mp.amazon.model.math.SwarmType.*;

/**
 * User: flsh
 * Date: 21.09.17.
 */
@SpringAware
public class GameMap extends AbstractGameMap<Enemy, GameMapShape, EnemyRange, EnemyType, GameMap> {

    private static final int SPIDER_QUEEN_SKIN_ID = 1;
    private static final long SPIDER_QUEEN_INVULNERABILITY_TIME = 1000;
    private static final long SPIDER_QUEEN_SPAWN_TIME = 2000;

    private static final int ROCK_GOLEM_SKIN_ID = 2;
    private static final long ROCK_GOLEM_INVULNERABILITY_TIME = 2200;
    private static final long ROCK_GOLEM_SPAWN_TIME = 4200;

    private static final int APE_KING_SKIN_ID = 3;
    private static final long APE_KING_INVULNERABILITY_TIME = 1000;
    private static final long APE_KING_SPAWN_TIME = 2500;

    private static final int FROG_SKIN = 3;
    private static final int ORION_WASP_SKIN = 3;

    private static List<Pair<Integer, Integer>> RUNNER_SHIFTS = Arrays.asList(new Pair<>(0, 5), new Pair<>(5, 5), new Pair<>(5, 0));

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

    public List<Enemy> spawnSnakesSwarm(EnemyType enemyType) {
        List<Enemy> snakes = new ArrayList<>();
        int swarmId = enemyIdsGenerator.getAndIncrement();
        int skinId = getRandomSkin(enemyType);
        float speed = enemyType.getSkin(skinId).getSpeed() * 3;
        Trajectory trajectory = getTrajectory(enemyType, speed, isNeedStandOnPlace(enemyType, skinId),
                false, skinId, true);
        snakes.add(addSwarmEnemy(enemyType, TRIPLE_SNAKE, skinId, trajectory, speed, swarmId));
        snakes.add(addSwarmEnemy(enemyType, TRIPLE_SNAKE, skinId, shiftTrajectory(trajectory, -3, -2, -3, -2, 523), speed, swarmId));
        snakes.add(addSwarmEnemy(enemyType, TRIPLE_SNAKE, skinId, shiftTrajectory(trajectory, -3, -2, 2, 3, 742), speed, swarmId));
        registerSwarm(swarmId, snakes);
        return snakes;
    }

    private Enemy addSwarmEnemy(EnemyType enemyType, SwarmType swarmType, int skinId, Trajectory trajectory, float speed, int swarmId) {
        Enemy enemy = addItem(enemyType, skinId, trajectory, speed, createMathEnemy(enemyType), swarmId);
        enemy.addToSwarm(swarmType, swarmId);
        enemy.setEnergy(enemy.getFullEnergy());
        return enemy;
    }

    public List<Enemy> spawnAntScenarioSwarm(EnemyType enemyType) {
        List<SwarmParams> swarmParams = map.getSwarmParams();
        if (!swarmParams.isEmpty()) {
            SwarmParams params = getRandomElement(swarmParams);
            if (checkCooldown(params.getId())) {
                return spawnAnts(params, enemyType);
            }
        }
        return new ArrayList<>();
    }

    public List<Enemy> spawnWaspSwarm(EnemyType enemyType) {
        List<Enemy> wasps = new ArrayList<>();
        int amount;
        long time = System.currentTimeMillis();
        SwarmSpawnParams params = getRandomElement(map.getSwarmSpawnParams(WASP_REGULAR));

        amount = RNG.nextInt(5, 8);
        EnemyType scarabType = EnemyType.values()[enemyType.getId()];
        int skinId = getRandomSkin(scarabType);
        Skin skin = scarabType.getSkin(skinId);
        int swarmId = enemyIdsGenerator.getAndIncrement();

        for (int i = 0; i < amount; i++) {
            float newSpeed = skin.getSpeed() * 1.75f;
            Enemy enemy = addItem(scarabType, skinId, getScarabTrajectory(params, time, newSpeed),
                    newSpeed, createMathEnemy(enemyType), swarmId);
            enemy.addToSwarm(WASP_REGULAR, swarmId);
            enemy.setEnergy(enemy.getFullEnergy());
            enemy.setSpeed(newSpeed);
            wasps.add(enemy);
        }
        registerSwarm(swarmId, wasps);
        return wasps;
    }

    public List<Enemy> spawnWaspOrionSwarm(EnemyType enemyType) {
        List<Enemy> wasps = new ArrayList<>();
        long time = System.currentTimeMillis();
        SwarmSpawnParams params = map.getSwarmSpawnParams(WASP_ORION).get(RNG.nextInt(map.getSwarmSpawnParams().size()));
        Skin skin = EnemyType.WASP.getSkin(ORION_WASP_SKIN);
        int swarmId = enemyIdsGenerator.getAndIncrement();
        Trajectory baseTrajectory = getScarabTrajectory(params, time, skin.getSpeed());
        for (int i = 0; i < 3; i++) {
            Enemy enemy = addItem(EnemyType.WASP, ORION_WASP_SKIN, shiftTrajectory(baseTrajectory, 0, 0, i * 200),
                    skin.getSpeed(), createMathEnemy(enemyType), swarmId);
            enemy.addToSwarm(WASP_ORION, swarmId);
            enemy.setEnergy(enemy.getFullEnergy());
            wasps.add(enemy);
        }
        registerSwarm(swarmId, wasps);
        return wasps;
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

    private List<Enemy> spawnAnts(SwarmParams params, EnemyType enemyType) {
        List<Enemy> ants = new ArrayList<>();
        int amount = RNG.nextInt(params.getMinSize(), params.getMaxSize());
        long spawnTime = System.currentTimeMillis() + SPAWN_DELAY;
        scenarioCooldowns.put(params.getId(), spawnTime + params.getCooldown());
        TypeAndSkin typeAndSkin = getRandomElement(params.getEnemies());
        EnemyType antType = EnemyType.getById(typeAndSkin.getType());
        Skin skin = antType.getSkin(typeAndSkin.getSkin());
        int swarmId = swarmIdGenerator.getAndIncrement();
        for (int i = 0; i < amount; i++) {
            Trajectory trajectory = new RatSwarmTrajectoryGenerator(map, getCoords(), params.getAngle())
                    .generate(new PointD(params.getStartX() + RNG.nextInt(params.getDeltaX()),
                                    params.getStartY() + RNG.nextInt(params.getDeltaY())),
                            params.getDistance(), skin.getSpeed() * 1.25, 1, spawnTime, 3000);
            Enemy enemy = addItem(antType, typeAndSkin.getSkin(), trajectory, (float) trajectory.getSpeed(),
                    createMathEnemy(enemyType), -1);
            enemy.addToSwarm(ANT_SCENARIO, swarmId);
            enemy.setEnergy(enemy.getFullEnergy());
            ants.add(enemy);
        }
        registerSwarm(swarmId, ants);
        return ants;
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
            IMovementStrategy movementStrategy = enemy.getMovementStrategy();
            movementStrategy.setMap(this);
        }
        int ordinal = input.readInt(true);
        possibleEnemies = EnemyRange.values()[ordinal];
    }

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
    protected void writeAdditionalFields(Kryo kryo, Output output) {
        kryo.writeClassAndObject(output, getInactivityLiveItems());
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

    public static void main(String[] args) {
        Kryo kryo = new Kryo();
        Output output = new Output(1024);
        GameMap map = new GameMap(EnemyRange.BaseEnemies, new GameMapShape(1, (byte) 127, (byte) 127));
        map.addItem(EnemyType.Boss, 1, null, 4f, null, -1);
        kryo.writeClassAndObject(output, map);
        output.close();

        Input input = new Input(output.getBuffer(), 0, (int) output.total());
        Object list2 = kryo.readClassAndObject(input);
        System.out.println(list2);
    }

    @Override
    protected Trajectory getTrajectory(EnemyType enemyType, float speed, boolean needStandOnPlace, boolean needNearCenter,
                                       int skinId, boolean needFinalSteps) {
        List<Trajectory> trajectories = map.getPredefinedTrajectories(enemyType.getId(), skinId);
        if (trajectories != null && !trajectories.isEmpty()) {
            return prepareTrajectory(trajectories.get(RNG.nextInt(trajectories.size())), speed);
        }
        switch (enemyType) {
            case ANT:
            case WASP:
                return new FreeAngleTrajectoryGenerator(map, getRandomSpawnPoint(), speed)
                        .generateWithDuration(System.currentTimeMillis() + 1000, TRAJECTORY_DURATION, needFinalSteps);
            case SNAKE:
                return new LargeEnemyFreeAngleTrajectoryGenerator(map, getRandomLargeEnemiesSpawnPoint(), speed)
                        .generateWithDuration(System.currentTimeMillis() + 1000, TRAJECTORY_DURATION, needFinalSteps);
            case RUNNER:
                return new LargeEnemyTrajectoryGenerator(map, getRandomLargeEnemiesSpawnPoint(), speed)
                        .generateWithDuration(System.currentTimeMillis() + 1000, getTrajectoryDuration(), needFinalSteps);
            case SHAMAN:
                return new ShamanTrajectoryGenerator(map, new PointI(), speed)
                        .generateWithDuration(System.currentTimeMillis() + 1000, TRAJECTORY_DURATION, true);
            case EXPLODER:
                return new LargeEnemyJumpTrajectoryGenerator(map, getRandomLargeEnemiesSpawnPoint(), speed, 9, 12)
                        .generateWithDuration(System.currentTimeMillis() + 1000, TRAJECTORY_DURATION, needFinalSteps);
            case JUMPER:
                return new JumpTrajectoryGenerator(map, getRandomSpawnPoint(), speed, 18, 29)
                        .generateWithDuration(System.currentTimeMillis() + 1000, TRAJECTORY_DURATION, needFinalSteps);
            default:
                return super.getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps);
        }
    }

    @Override
    protected boolean isOgre(IEnemyType enemyType) {
        return EnemyRange.MINI_BOSS.getEnemies().contains(enemyType);
    }

    protected Pair<Integer, Trajectory> getTrajectoryWithSaveStartPosition(EnemyType enemyType, float speed,
                                                                           boolean needStandOnPlace, boolean needNearCenter,
                                                                           int skinId, boolean needFinalSteps) {
        List<Trajectory> trajectories = map.getPredefinedTrajectories(enemyType.getId(), skinId);
        if (trajectories != null && !trajectories.isEmpty()) {
            int index = RNG.nextInt(trajectories.size());
            return new Pair<>(index, prepareTrajectory(trajectories.get(index), speed));
        }
        return new Pair<>(0, getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps));
    }

    @Override
    public Enemy addEnemyByTypeNew(EnemyType enemyType, IMathEnemy mathEnemy, int skinId, long parentEnemyId,
                                   boolean needNearCenter, boolean needFinalSteps, boolean useCustomTrajectories) {

        if (enemyType.equals(EnemyType.SNAKE) || enemyType.equals(EnemyType.ANT)) {
            skinId = getRandomSkin(enemyType);
            float speed = generateSpeed(enemyType.getSkin(skinId));
            return addItem(enemyType, skinId,
                    getTrajectory(enemyType, speed, isNeedStandOnPlace(enemyType, skinId), needNearCenter, skinId, needFinalSteps),
                    speed, mathEnemy, parentEnemyId);
        }

        if (EnemyRange.BIRDS.getEnemies().contains(enemyType)) {
            int skin = skinId == -1 ? getRandomSkin(enemyType) : skinId;
            float speed = generateSpeed(enemyType.getSkin(skin));
            Pair<Integer, Trajectory> trajectoryWithSaveStartPosition =
                    getTrajectoryWithSaveStartPosition(enemyType, speed, false, needNearCenter, skin, needFinalSteps);
            Enemy enemy = addItem(enemyType, skin, trajectoryWithSaveStartPosition.getValue(),
                    speed, mathEnemy, parentEnemyId);
            enemy.setCurrentTrajectoryId(trajectoryWithSaveStartPosition.getKey());
            return enemy;
        } else {
            return super.addEnemyByTypeNew(enemyType, mathEnemy, skinId, parentEnemyId, needNearCenter, needFinalSteps, useCustomTrajectories);
        }
    }

    @Override
    public Set<Long> getEnemiesForNewEnemyUpdating(boolean needFinalSteps, boolean needReturnAllEnemies) {
        if (!getInactivityLiveItems().isEmpty())
            clearInactivityLiveItems();
        return new HashSet<>();
    }

    public Map<Long, Trajectory> generateUpdateTrajectories(boolean needFinalSteps) {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        lockEnemy.lock();
        try {
            long startTime = System.currentTimeMillis();
            for (Enemy enemy : getItems()) {
                boolean isRunner = enemy.getEnemyClass().getEnemyType().equals(EnemyType.RUNNER);
                if (!isRunner && needFinalSteps && !enemy.isBoss())
                    continue;

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

                    TrajectoryGenerator trajectoryGenerator;
                    EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
                    switch (enemyType) {
                        case MULTIPLIER:
                            trajectoryGenerator = new JumpTrajectoryGenerator(map, location, speedNew);
                            break;
                        case EXPLODER:
                            trajectoryGenerator = new LargeEnemyJumpTrajectoryGenerator(map, location, speedNew, 9, 12);
                            break;
                        case ANT:
                        case WASP:
                            trajectoryGenerator = new FreeAngleTrajectoryGenerator(map, location, speedNew);
                            break;
                        case SNAKE:
                            trajectoryGenerator = new LargeEnemyFreeAngleTrajectoryGenerator(map, location, speedNew);
                            break;
                        case SHAMAN:
                            trajectoryGenerator = new ShamanTrajectoryGenerator(map, location, speedNew);
                            break;
                        case JUMPER:
                            trajectoryGenerator = new JumpTrajectoryGenerator(map, location, speedNew, 21, 29);
                            break;
                        default:
                            trajectoryGenerator = new TrajectoryGenerator(map, location, speedNew);
                    }
                    trajectory = trajectoryGenerator.generateWithDuration(startTime, TRAJECTORY_DURATION,
                            false);
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

    private Trajectory prepareTrajectory(Trajectory template, float speed) {
        Trajectory trajectory = new Trajectory(template.getSpeed());
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
        switch ((EnemyType) enemyType) {
            case MULTIPLIER:
                return getMiniBossRandomTrajectory(speed, minSteps);
            case ANT:
            case WASP:
                return new FreeAngleTrajectoryGenerator(map, getRandomSpawnPoint(), speed)
                        .generate(System.currentTimeMillis() + 1000, minSteps, needFinalSteps);
            case SNAKE:
                return new LargeEnemyFreeAngleTrajectoryGenerator(map, getRandomLargeEnemiesSpawnPoint(), speed)
                        .generate(System.currentTimeMillis() + 1000, minSteps, needFinalSteps);
            case EXPLODER:
                return new LargeEnemyJumpTrajectoryGenerator(map, getRandomSpawnPoint(), speed, 9, 12)
                        .generate(System.currentTimeMillis() + 1000, minSteps, needFinalSteps);
            case JUMPER:
                return new JumpTrajectoryGenerator(map, getRandomSpawnPoint(), speed, 21, 29)
                        .generate(System.currentTimeMillis() + 1000, minSteps, needFinalSteps);
            default:
                return super.getRandomTrajectory(enemyType, speed, minSteps, needFinalSteps);
        }
    }

    private Trajectory getMiniBossRandomTrajectory(double speed, int minSteps) {
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

    @Override
    protected Trajectory getInitialTrajectory(double speed, boolean needFinalSteps, IEnemyType enemyType) {
        switch ((EnemyType) enemyType) {
            case ANT:
            case WASP:
                return new FreeAngleTrajectoryGenerator(map, getRandomSpawnPoint(), speed)
                        .generate(System.currentTimeMillis() + 1000, 7, needFinalSteps);
            case SNAKE:
                return new LargeEnemyFreeAngleTrajectoryGenerator(map, getRandomLargeEnemiesSpawnPoint(), speed)
                        .generate(System.currentTimeMillis() + 1000, 7, needFinalSteps);
            case EXPLODER:
                return new LargeEnemyJumpTrajectoryGenerator(map, getRandomSpawnPoint(), speed, 9, 12)
                        .generate(System.currentTimeMillis() + 1000, 7, needFinalSteps);
            case SHAMAN:
                return new ShamanTrajectoryGenerator(map, new PointI(), speed)
                        .generate(System.currentTimeMillis() + 1000, 7, needFinalSteps);
            case JUMPER:
                return new JumpTrajectoryGenerator(map, getRandomSpawnPoint(), speed, 21, 29)
                        .generate(System.currentTimeMillis() + 1000, 7, needFinalSteps);
            default:
                return super.getInitialTrajectory(speed, needFinalSteps, enemyType);
        }
    }

    @Override
    protected Trajectory generateLeaveTrajectory(ITrajectoryGenerator generator, PointI location, long startTime, Enemy enemy) {
        if (SHAMAN.equals(enemy.getEnemyClass().getEnemyType())) {
            return new ShamanTrajectoryGenerator(map, PointI.EMPTY, enemy.getSpeed())
                    .generateLeaveTrajectory(startTime, enemy.getTrajectory());
        }
        return super.generateLeaveTrajectory(generator, location, startTime, enemy);
    }

    @Override
    public boolean needDoubleSpeed(int enemyTypeId) {
        return enemyTypeId == 3;
    }

    @Override
    public boolean notNeedFreezeAtStartPoint(IEnemyType enemyType) {
        return false;
    }

    @Override
    protected long getBossSpawnAnimationDuration(int skinId) {
        if (skinId == SPIDER_QUEEN_SKIN_ID) {
            return SPIDER_QUEEN_SPAWN_TIME;
        } else if (skinId == ROCK_GOLEM_SKIN_ID) {
            return ROCK_GOLEM_SPAWN_TIME;
        } else if (skinId == APE_KING_SKIN_ID) {
            return APE_KING_SPAWN_TIME;
        } else {
            return BOSS_SPAWN_ANIMATION_DURATION;
        }
    }

    @Override
    protected long getBossInvulnerabilityTime(int skinId) {
        if (skinId == SPIDER_QUEEN_SKIN_ID) {
            return SPIDER_QUEEN_INVULNERABILITY_TIME;
        } else if (skinId == ROCK_GOLEM_SKIN_ID) {
            return ROCK_GOLEM_INVULNERABILITY_TIME;
        } else if (skinId == APE_KING_SKIN_ID) {
            return APE_KING_INVULNERABILITY_TIME;
        } else {
            return BOSS_INVULNERABILITY_TIME;
        }
    }

    public List<Enemy> respawnEnemies() {
        long now = System.currentTimeMillis();
        int aliveLargeEnemies = getAliveEnemiesCount(LargeEnemies);
        List<Enemy> respawnedEnemies = new ArrayList<>();
        List<Long> respawnedIds = new ArrayList<>();
        for (Enemy enemy : removedEnemies.values()) {
            if (enemy.getReturnTime() > 0 && enemy.getReturnTime() < now) {
                if (LargeEnemies.getEnemies().contains(enemy.getEnemyClass().getEnemyType())) {
                    if (aliveLargeEnemies < 3) {
                        aliveLargeEnemies++;
                    } else {
                        continue;
                    }
                }
                EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
                Trajectory trajectory = getTrajectory(enemyType, (float) enemy.getSpeed(), false,
                        false, enemy.getSkin(), true);
                enemy.setTrajectory(trajectory);
                enemy.setReturnTime(0);
                addItem(enemy);
                respawnedEnemies.add(enemy);
                respawnedIds.add(enemy.getId());
            }
        }
        for (Swarm<Enemy> swarm : swarms.values()) {
            if (swarm.isShouldReturn() && swarm.getReturnTime() > 0 && swarm.getReturnTime() < now) {
                if (swarm.getSwarmType() == RUNNERS.getTypeId()) {
                    respawnedEnemies.addAll(respawnRunnerFormation(swarm));
                }
            }
        }
        respawnedIds.forEach(id -> removedEnemies.remove(id));
        return respawnedEnemies;
    }

    public List<Enemy> spawnRunnerFormation(EnemyType enemyType) {
        List<Enemy> enemies = new ArrayList<>();
        int skinId = getRandomSkin(enemyType);
        int count = RNG.nextInt(2, 4);
        float speed = enemyType.getSkin(skinId).getSpeed() * 4;
        int swarmId = swarmIdGenerator.getAndIncrement();
        Trajectory trajectory = getTrajectory(enemyType, speed, false, false, skinId, true);
        Enemy enemy = addSwarmEnemy(enemyType, RUNNERS, skinId, trajectory, speed, swarmId);
        enemy.setShouldReturn(true);
        enemies.add(enemy);
        for (int i = 1; i < count; i++) {
            int dx = RUNNER_SHIFTS.get(i - 1).getKey();
            int dy = RUNNER_SHIFTS.get(i - 1).getValue();
            enemy = addSwarmEnemy(enemyType, RUNNERS, skinId, shiftTrajectory(trajectory, dx, dy, RNG.nextInt(200)), speed, swarmId);
            enemy.setShouldReturn(true);
            enemies.add(enemy);
        }
        registerSwarm(swarmId, enemies, true, 15000);
        return enemies;
    }

    public List<Enemy> respawnRunnerFormation(Swarm<Enemy> swarm) {
        List<Enemy> enemies = new ArrayList<>();
        List<Long> enemyIds = swarm.getEnemyIds();
        Enemy enemy = pickRemovedEnemy(enemyIds.get(0));
        if (enemy == null) {
            getLogger().warn("First runner not found, removing swarm: " + swarm);
        } else {
            Trajectory trajectory = getNewTrajectory(enemy);
            enemy.setTrajectory(trajectory);
            addItem(enemy);
            enemies.add(enemy);

            for (int i = 1; i < enemyIds.size(); i++) {
                enemy = pickRemovedEnemy(enemyIds.get(i));
                if (enemy != null) {
                    int dx = RUNNER_SHIFTS.get(i - 1).getKey();
                    int dy = RUNNER_SHIFTS.get(i - 1).getValue();
                    enemy.setTrajectory(shiftTrajectory(trajectory, dx, dy, RNG.nextInt(200)));
                    addItem(enemy);
                    enemies.add(enemy);
                } else {
                    getLogger().error("Secondary enemy {} in runner formation {} is not found", enemyIds.get(i), swarm);
                }
            }
        }
        enemyIds.forEach(id -> removedEnemies.remove(id));
        swarm.resetReturnTime();
        return enemies;
    }

    private Enemy pickRemovedEnemy(long enemyId) {
        Enemy enemy = removedEnemies.get(enemyId);
        removedEnemies.remove(enemyId);
        return enemy;
    }

    private Trajectory getNewTrajectory(Enemy enemy) {
        EnemyType enemyType = enemy.getEnemyClass().getEnemyType();
        return getTrajectory(enemyType, (float) enemy.getSpeed(), false, false, enemy.getSkin(), true);
    }

    public List<Enemy> spawnExplodedFrogs(long time, IEnemy enemy) {
        List<Enemy> frogs = new ArrayList<>();
        Skin skin = EnemyType.ANT.getSkin(FROG_SKIN);
        for (int i = 0; i < 4; i++) {
            Trajectory trajectory = new ShortLeaveJumpTrajectoryGenerator(map, enemy.getLocation(time).toPointI(), skin.getSpeed(), 3, 7, i)
                    .generate(time + SPAWN_DELAY);
            if (trajectory != null) {
                Enemy frog = addItem(EnemyType.ANT, FROG_SKIN, trajectory, skin.getSpeed(), createMathEnemy(EnemyType.ANT), enemy.getId());
                frog.setEnergy(frog.getFullEnergy());
                frogs.add(frog);
            }
        }
        return frogs;
    }

    @Override
    protected void addPointsFromOldTrajectory(List<Point> points, long startTime, long freezeTime, PointI location, Enemy enemy) {
        if (SHAMAN.equals(enemy.getEnemyClass().getEnemyType())) {
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
}
