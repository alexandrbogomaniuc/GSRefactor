package com.betsoft.casino.mp.revengeofra.model;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.common.scenarios.SpawnGroup;
import com.betsoft.casino.mp.common.scenarios.SpawnScenario;
import com.betsoft.casino.mp.common.scenarios.TypeAndSkin;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.*;
import com.betsoft.casino.mp.revengeofra.model.math.*;
import com.abs.casino.common.util.Pair;
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

import static com.betsoft.casino.mp.revengeofra.model.math.SwarmType.*;

@SpringAware
public class GameMap extends AbstractGameMap<Enemy, GameMapShape, EnemyRange, EnemyType, GameMap> {
    private static final int CRABS_GROUP = -1;
    protected static final int FREEZE_TIME_MAX = 3000;
    private static final int TRAJECTORY_DURATION = 30000;
    private static final float[] PORTAL_DX = new float[]{0, -1, 1};
    private static final float[] PORTAL_DY = new float[]{0, -0.5f, -0.5f};

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

    public List<Enemy> addSwarmByScenario(HashSet<Integer> liveIdScenariosCrabs, int max) {
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

    public List<Enemy> addSwarmByParams(HashSet<Integer> liveIdSwarmParamsRats, int max) {
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
                    int amount = RNG.nextInt(params.getMinSize(), Math.min(params.getMaxSize(), max));
                    if (params.isFromPortal()) {
                        return spawnEnemiesByParamsFromPortal(params, amount);
                    } else {
                        return spawnRats(params, amount);
                    }
                }
            } else
                return new ArrayList<>();
        }
        return new ArrayList<>();
    }

    private List<Enemy> spawnRats(SwarmParams params, int amount) {
        long spawnTime = System.currentTimeMillis() + SPAWN_DELAY;
        List<Enemy> rats = new ArrayList<>();
        scenarioCooldowns.put(params.getId(), spawnTime + params.getCooldown());
        int swarmId = swarmIdGenerator.getAndIncrement();
        for (int i = 0; i < amount; i++) {
            TypeAndSkin typeAndSkin = getRandomElement(params.getEnemies());
            EnemyType ratType = EnemyType.getById(typeAndSkin.getType());
            Skin skin = ratType.getSkin(typeAndSkin.getSkin());
            RatSwarmTrajectoryGenerator generator = new RatSwarmTrajectoryGenerator(map, coords, params.getAngle());

            Trajectory trajectory = generator
                    .generate(new PointD(params.getStartX() + RNG.nextInt(params.getDeltaX()), params.getStartY() + RNG.nextInt(params.getDeltaY())),
                            params.getDistance(), skin.getSpeed() * 2.25, skin.getSpeedDeltaPositive() / 2, spawnTime, 500);
            Enemy enemy = addItem(ratType, typeAndSkin.getSkin(), trajectory, (float) trajectory.getSpeed(),
                    createMathEnemy(ratType), -1);
            enemy.setEnergy(enemy.getFullEnergy());
            enemy.addToSwarm(SWARM_PARAMS, swarmId);
            enemy.setCurrentTrajectoryId(params.getId());
            rats.add(enemy);
        }
        registerSwarm(swarmId, rats);
        return rats;
    }

    private List<Enemy> spawnEnemiesByParamsFromPortal(SwarmParams params, int amount) {
        long spawnTime = System.currentTimeMillis() + SPAWN_DELAY;
        if (spawnTime < portalCooldown) {
            return new ArrayList<>();
        }
        List<Enemy> enemies = new ArrayList<>();
        // Client requires that farthest enemy should be near center axis
        amount = amount / 3 * 3 + 1;
        scenarioCooldowns.put(params.getId(), spawnTime + params.getCooldown());
        int swarmId = swarmIdGenerator.getAndIncrement();
        int portalId = portalIdGenerator.getAndIncrement();
        boolean moveToLeft = params.getAngle() < 270;
        getLogger().debug("Spawn swarm by params from portal " + portalId);
        for (int i = 0; i < amount; i++) {
            TypeAndSkin typeAndSkin = getRandomElement(params.getEnemies());
            EnemyType ratType = EnemyType.getById(typeAndSkin.getType());
            Skin skin = ratType.getSkin(typeAndSkin.getSkin());
            RatSwarmTrajectoryGenerator generator = new PortalSwarmTrajectoryGenerator(map, coords, params.getAngle(), portalId);

            Trajectory trajectory = generator
                    .generate(getPortalEnemySpawnPoint(new PointI(params.getStartX(), params.getStartY()), moveToLeft, i),
                            params.getDistance(), skin.getSpeed() * 2.25, skin.getSpeedDeltaPositive() / 2, spawnTime, 500);
            Enemy enemy = addItem(ratType, typeAndSkin.getSkin(), trajectory, (float) trajectory.getSpeed(),
                    createMathEnemy(ratType), -1);
            enemy.setEnergy(enemy.getFullEnergy());
            enemy.addToSwarm(SWARM_PARAMS, swarmId);
            enemy.setCurrentTrajectoryId(params.getId());
            enemies.add(enemy);
            updatePortalCooldown(trajectory);
        }
        registerSwarm(swarmId, enemies);
        return enemies;
    }

    PointD getPortalEnemySpawnPoint(PointI base, boolean moveToLeft, int position) {
        int line = position / 3;
        int place = position % 3;
        float dx = PORTAL_DX[place];
        float dy = -line + PORTAL_DY[place];
        if (moveToLeft) {
            return new PointD(base.x + dy, base.y + dx);
        } else {
            return new PointD(base.x + dx, base.y + dy);
        }
    }

    public List<Enemy> spawnWaspSwarm(EnemyType enemyType) {
        List<Enemy> wasps = new ArrayList<>();
        int amount;
        long time = System.currentTimeMillis();
        SwarmSpawnParams params = getRandomElement(map.getSwarmSpawnParams(LOCUST));

        amount = RNG.nextInt(12, 15);
        EnemyType scarabType = EnemyType.values()[enemyType.getId()];
        int skinId = getRandomSkin(scarabType);
        Skin skin = scarabType.getSkin(skinId);
        int swarmId = enemyIdsGenerator.getAndIncrement();

        float speed = skin.getSpeed() + (float) (RNG.rand() * 2.);
        List<Trajectory> trajectories = getLocustTrajectories(params, time, speed, amount);
        for (int i = 0; i < amount; i++) {
            Enemy enemy = addItem(scarabType, skinId, trajectories.get(i), speed, createMathEnemy(enemyType), swarmId);
            enemy.addToSwarm(LOCUST, swarmId);
            enemy.setEnergy(enemy.getFullEnergy());
            enemy.setSpeed(speed);
            wasps.add(enemy);
        }
        registerSwarm(swarmId, wasps);
        return wasps;
    }

    private Trajectory splitWaspTrajectory(Trajectory source) {
        if (RNG.nextBoolean()) {
            Trajectory result = new Trajectory(source.getSpeed());
            Point first = source.getPoints().get(0);
            Point last = source.getPoints().get(1);
            int slowStart = RNG.nextInt(1, 5);
            int slowAmount = RNG.nextInt(1, 4);
            double dx = (last.getX() - first.getX()) / 10;
            double dy = (last.getY() - first.getY()) / 10;
            long dt = (last.getTime() - first.getTime()) / 10;
            result.addPoint(first);
            result.addPoint(first.getX() + dx * slowStart,
                    first.getY() + dy * slowStart,
                    first.getTime() + dt * slowStart);
            result.addPoint(first.getX() + dx * (slowStart + slowAmount),
                    first.getY() + dy * (slowStart + slowAmount),
                    (long) (first.getTime() + dt * (slowStart + slowAmount * 1.5)));
            result.addPoint(first.getX() + dx * (slowStart + slowAmount * 2),
                    first.getY() + dy * (slowStart + slowAmount * 2),
                    first.getTime() + dt * (slowStart + slowAmount * 2));
            result.addPoint(last);
            return result;
        }
        return source;
    }

    private List<Enemy> spawnByScenario(SpawnScenario scenario) {
        List<Enemy> enemies = new ArrayList<>();
        long spawnTime = System.currentTimeMillis() + SPAWN_DELAY;
        for (SpawnGroup group : scenario.getGroups()) {
            if (group.isFromPortal()) {
                return enemies;
            }
        }
        scenarioCooldowns.put(scenario.getId(), spawnTime + scenario.getCooldown());
        int offsetX = RNG.nextInt(scenario.getOffsetX() + 1);
        int offsetY = RNG.nextInt(scenario.getOffsetY() + 1);

        for (SpawnGroup group : scenario.getGroups()) {
            TypeAndSkin mainEnemy = group.getMainEnemy();
            Trajectory baseTrajectory = group.getTrajectory();

            if (!group.isFromPortal() && RNG.nextBoolean()) {
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
                Enemy enemy = addItem(EnemyType.getById(mainEnemy.getType()),
                        mainEnemy.getSkin(), trajectory, (float) trajectory.getSpeed(),
                        null, -1);
                enemy.setCurrentTrajectoryId(scenario.getId());
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
                            null, -1);
                    enemy.setCurrentTrajectoryId(scenario.getId());
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
                            null, -1);
                    enemy.setCurrentTrajectoryId(scenario.getId());
                    enemies.add(enemy);
                }

                if (group.isFromPortal()) {
                    generatePortal(enemies);
                }
            }
        }
        int swarmId = swarmIdGenerator.getAndIncrement();
        for (Enemy enemy : enemies) {
            enemy.addToSwarm(SWARM_SCENARIO, swarmId);
        }
        return enemies;
    }

    private void generatePortal(List<Enemy> enemies) {
        int portalId = portalIdGenerator.getAndIncrement();
        getLogger().debug("Spawn swarm by scenario from portal " + portalId);
        for (Enemy enemy : enemies) {
            Point point = enemy.getTrajectory().getPoints().get(0);
            enemy.getTrajectory().setPoint(0, new PortalPoint(point, portalId));
            updatePortalCooldown(enemy.getTrajectory());
        }
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
        enemy.setEnergy(100);
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
        int enemyTypeId = enemyType.getId();
//        if (map.isDisableRandomTrajectories() && !enemyType.isBoss()) {
//            enemyTypeId = -1;
//        }
        List<Trajectory> trajectories = map.getPredefinedTrajectories(enemyTypeId, skinId);
        if (trajectories != null && !trajectories.isEmpty()) {
            return prepareTrajectory(trajectories.get(RNG.nextInt(trajectories.size())), speed);
        }
        if (enemyType.equals(EnemyType.ENEMY_16)) {
            Trajectory trajectory = new HorusTrajectoryGenerator(map, new PointI(), speed)
                    .generateWithDuration(System.currentTimeMillis() + 1000, TRAJECTORY_DURATION, true);
            getLogger().debug("horus trajectory size: " + trajectory.getPoints().size());
            return trajectory;
        }

        if (EnemyRange.LOW_MUMMIES.getEnemies().contains(enemyType)) {
            long spawnTime = System.currentTimeMillis() + 1000;
            PointI source = getRandomSpawnPoint();
            return new TrajectoryGenerator(map, source, RNG.nextBoolean() ? speed : 4.0f + RNG.rand() * 2)
                    .generateWithDuration(spawnTime, getTrajectoryDuration(), needFinalSteps);
        }

        return super.getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps);
    }

    @Override
    protected boolean isOgre(IEnemyType enemyType) {
        return enemyType.equals(EnemyType.ENEMY_16);
    }

    protected Pair<Integer, Trajectory> getTrajectoryWithSaveStartPosition(EnemyType enemyType, float speed,
                                                                           boolean needStandOnPlace, boolean needNearCenter,
                                                                           int skinId, boolean needFinalSteps) {
        int enemyTypeId = enemyType.getId();
//        if (map.isDisableRandomTrajectories()) {
//            enemyTypeId = -1;
//        }
        List<Trajectory> trajectories = map.getPredefinedTrajectories(enemyTypeId, skinId);
        if (trajectories != null && !trajectories.isEmpty()) {
            int index = RNG.nextInt(trajectories.size());
            return new Pair<>(index, prepareTrajectory(trajectories.get(index), speed));
        }
        return new Pair<>(0, super.getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps));
    }


    @Override
    protected Trajectory getSpecialMiniBossTrajectory(double speed, boolean needFinalSteps) {
        PointI bossSpawnPoint = map.getBossSpawnPoint();

        int x = bossSpawnPoint.x;
        int y = bossSpawnPoint.y;

        PointI spawnPoint = new PointI(x, y);
        List<Point> points = new ArrayList<>();
        long spawnTime = System.currentTimeMillis();
        points.add(new InvulnerablePoint(spawnPoint.x, spawnPoint.y, spawnTime));

        return new TeleportTrajectoryGenerator(map, spawnPoint, speed, 850, 1250, 10000, RNG.nextInt(500, 750), false)
                .setNeedFinalTeleport(true)
                .generate(new Trajectory(speed, points), spawnTime + 2000, 25, true);
    }

    public Enemy addEnemyByType(EnemyType enemyType, IMathEnemy mathEnemy, int skinId, long parentEnemyId,
                                boolean needNearCenter, boolean needFinalSteps, boolean allowPortal) {
        if (allowPortal && !EnemyRange.NON_PORTAL_ENEMIES.getEnemies().contains(enemyType)
                && portalCooldown < System.currentTimeMillis() && RNG.nextInt(2) == 0) {
            int skin = (skinId == -1 ? getRandomSkin(enemyType) : skinId);
            boolean needToRush = !needNearCenter && !enemyType.isBoss() && !map.isDisableRandomTrajectories();
            float speed = needToRush ? getMaxSpeed(enemyType, skin) : generateSpeed(enemyType.getSkin(skin));
            return addItem(enemyType, skin, needToRush ? getPortalTrajectory(speed)
                            : getTrajectory(enemyType, speed, isNeedStandOnPlace(enemyType, skinId), needNearCenter, skin, needFinalSteps),
                    speed, mathEnemy, parentEnemyId);
        }

        return super.addEnemyByTypeNew(enemyType, mathEnemy, skinId, parentEnemyId, needNearCenter, needFinalSteps, false);
    }

    @Override
    public Enemy addEnemyByTypeNew(EnemyType enemyType, IMathEnemy mathEnemy, int skinId, long parentEnemyId,
                                   boolean needNearCenter, boolean needFinalSteps, boolean useCustomTrajectories) {

        if (EnemyRange.BIRDS.getEnemies().contains(enemyType)) {
            int skin = skinId == -1 ? getRandomSkin(enemyType) : skinId;
            float speed = generateSpeed(enemyType.getSkin(skin));
            Pair<Integer, Trajectory> trajectoryWithSaveStartPosition =
                    getTrajectoryWithSaveStartPosition(enemyType, speed, false, needNearCenter, skin, needFinalSteps);
            Enemy enemy = addItem(enemyType, skin, trajectoryWithSaveStartPosition.getValue(),
                    speed, mathEnemy, parentEnemyId);
            enemy.setCurrentTrajectoryId(trajectoryWithSaveStartPosition.getKey());
            return enemy;
        }

        if (EnemyRange.LOW_MUMMIES.getEnemies().contains(enemyType)) {
            int skin = (skinId == -1 ? getRandomSkin(enemyType) : skinId);
            float speed = RNG.nextBoolean() ? getMaxSpeed(enemyType, skinId) : (float) (4.0f + RNG.rand() * 2.f);
            return addItem(enemyType, skin, getInitialTrajectory(speed, needFinalSteps, enemyType),
                    speed, mathEnemy, parentEnemyId);
        }

        return super.addEnemyByTypeNew(enemyType, mathEnemy, skinId, parentEnemyId, needNearCenter, needFinalSteps, useCustomTrajectories);
    }

    Trajectory getPortalTrajectory(float speed) {
        Portal portal = getRandomElement(map.getPortals());
        long spawnTime = System.currentTimeMillis() + 1000;
        int portalId = portalIdGenerator.getAndIncrement();
        getLogger().debug("Spawn single enemy from portal " + portalId);

        Trajectory trajectory = new PortalTrajectoryGenerator(map, portal, speed, portalId)
                .generateWithDuration(spawnTime, getTrajectoryDuration(), true);
        updatePortalCooldown(trajectory);
        return trajectory;
    }

    public Map<Long, Trajectory> generateShortLeaveTrajectories() {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        lockEnemy.lock();
        try {
            long startTime = System.currentTimeMillis();
            ITrajectoryGenerator generator = new WaypointTrajectoryGenerator(map);
            for (Enemy enemy : getItems()) {
                PointI location = enemy.getLocation(startTime).toPointI();
                Trajectory trajectory;
                if (EnemyType.ENEMY_16.equals(enemy.getEnemyClass().getEnemyType())) {
                    long time = System.currentTimeMillis();
                    trajectory = new Trajectory(enemy.getSpeed())
                            .addPoint(new TeleportPoint(location.x, location.y, time, true))
                            .addPoint(new InvulnerablePoint(location.x, location.y, time + 850));
                } else {
                    int cnt = 100;
                    int currentX = location.x;
                    int currentY = location.y;

                    while (cnt-- > 0) {
                        Point point = new Point(currentX, currentY, System.currentTimeMillis());
                        if (getMapShape().isAvailableAndPassable(point)) {
                            break;
                        } else {
                            currentX++;
                        }
                    }

                    location = new PointI(currentX, currentY);
                    trajectory = generateLeaveTrajectory(generator, location, startTime, enemy);
                }
                if (trajectory != null) {
                    enemy.setTrajectory(trajectory);
                    trajectories.put(enemy.getId(), trajectory);
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return trajectories;
    }

    @Override
    protected Trajectory generateLeaveTrajectory(ITrajectoryGenerator generator, PointI location, long startTime, Enemy enemy) {
        if (EnemyType.ENEMY_16.equals(enemy.getEnemyClass().getEnemyType())) {
            return generateHorusLeaveTrajectory(startTime, enemy);
        }

        if (enemy.getTrajectory().getPoints().get(0) instanceof PortalPoint) {
            return generatePortalLeaveTrajectory(generator, location, startTime, enemy);
        }
        return super.generateLeaveTrajectory(generator, location, startTime, enemy);
    }

    protected Trajectory generateHorusLeaveTrajectory(long startTime, Enemy enemy) {
        List<Point> points = enemy.getTrajectory().getPoints();
        List<Point> leavePoints = new ArrayList<>();
        int i = getIndexOfActiveTeleportPoint(points, startTime);
        Point point = points.get(i);
        int j = 0;
        while (j < 6 && i + 1 < points.size()) {
            point = points.get(i);
            if (points.get(i + 1).getTime() >= startTime) {
                leavePoints.add(point);
            }
            if (!point.isFreezePoint()) {
                j++;
            }
            i++;
        }
        leavePoints.add(new TeleportPoint(point.getX(), point.getY(), point.getTime(), false));
        leavePoints.add(new InvulnerablePoint(point.getX(), point.getY(), point.getTime() + HorusTrajectoryGenerator.INVISIBILITY_START_TIME));
        leavePoints.add(new InvulnerablePoint(point.getX(), point.getY(), point.getTime() + HorusTrajectoryGenerator.TELEPORT_START_TIME));
        leavePoints.add(new InvulnerablePoint(0, 0, point.getTime() + HorusTrajectoryGenerator.TELEPORT_FINISH_TIME));
        leavePoints.add(new Point(0, 0, point.getTime() + HorusTrajectoryGenerator.INVISIBILITY_FINISH_TIME));
        leavePoints.add(new Point(0, 0, point.getTime() + HorusTrajectoryGenerator.ANIMATION_DURATION));
        return new Trajectory(enemy.getSpeed(), leavePoints);
    }


    private int getIndexOfActiveTeleportPoint(List<Point> points, long time) {
        int i = 0;
        int teleportIndex = 0;
        while (i < points.size() && points.get(i).getTime() < time) {
            if (points.get(i) instanceof TeleportPoint) {
                teleportIndex = i;
            }
            i++;
        }
        return teleportIndex;
    }

    protected Trajectory generatePortalLeaveTrajectory(ITrajectoryGenerator generator, PointI location, long startTime, Enemy enemy) {
        List<Point> points = enemy.getTrajectory().getPoints();
        if (startTime < points.get(2).getTime()) {
            List<Point> leavePoints = new ArrayList<>();
            leavePoints.add(points.get(0));
            leavePoints.add(points.get(1));
            Point lastPoint = points.get(2);
            leavePoints.addAll(generator.generate((int) lastPoint.getX(), (int) lastPoint.getY(), lastPoint.getTime(),
                    startTime + 10000, enemy.getSpeed()).getPoints());
            return new Trajectory(enemy.getSpeed(), leavePoints);
        }
        return super.generateLeaveTrajectory(generator, location, startTime, enemy);
    }

    public Enemy addEnemyWithPredefinedTrajectories(EnemyType enemyType, IMathEnemy mathEnemy, int skinId, long parentEnemyId,
                                                    boolean needNearCenter, boolean needFinalSteps) {

        int skin = skinId == -1 ? getRandomSkin(enemyType) : skinId;
        List<Trajectory> trajectories = map.getPredefinedTrajectories(-2, skinId);
        float speed = generateSpeed(enemyType.getSkin(skin));
        if (trajectories != null && !trajectories.isEmpty()) {
            int index = RNG.nextInt(trajectories.size());
            Pair<Integer, Trajectory> trajectoryWithSaveStartPosition =
                    new Pair<>(index, prepareTrajectory(trajectories.get(index), speed));
            Enemy enemy = addItem(enemyType, skin, trajectoryWithSaveStartPosition.getValue(),
                    speed, mathEnemy, parentEnemyId);
            enemy.setCurrentTrajectoryId(-2);
            return enemy;
        } else
            return super.addEnemyByTypeNew(enemyType, mathEnemy, skinId, parentEnemyId, needNearCenter, needFinalSteps, false);
    }


    public int getNumberOfEnemiesWithTrajectoryId(int trajectoryId) {
        int res = 0;
        lockEnemy.lock();
        try {
            for (Enemy enemy : items) {
                if (enemy.getCurrentTrajectoryId() == trajectoryId) {
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
            for (Swarm<Enemy> swarm : swarms.values()) {
                generateSwarmUpdateTrajectory(swarm, startTime, needFinalSteps, trajectories);
            }
            /* Uncomment if need to update regular enemies
            for (Enemy enemy : getItems()) {
                generateEnemyUpdateTrajectory(enemy, startTime, needFinalSteps, trajectories);
            }*/
        } finally {
            lockEnemy.unlock();
        }
        return trajectories;
    }

    private void generateSwarmUpdateTrajectory(Swarm<Enemy> swarm, long startTime, boolean needFinalSteps, Map<Long, Trajectory> trajectories) {
        if (map.isDisableRandomTrajectories()) {
            return;
        }
        if (SwarmType.DUAL_SPEED_MUMMIES.getTypeId() == swarm.getSwarmType()) {
            List<Enemy> swarmEnemies = new ArrayList<>();
            for (long enemyId : swarm.getEnemyIds()) {
                Enemy enemy = getItemById(enemyId);
                if (enemy != null) {
                    swarmEnemies.add(enemy);
                }
            }
            if (!swarmEnemies.isEmpty()) {
                Enemy firstEnemy = swarmEnemies.get(0);
                if (firstEnemy.isLocationNearEnd(startTime)) {
                    Point basePoint = firstEnemy.getTrajectory().getPoints().get(0);
                    double speed = RNG.nextBoolean() ? getEnemySkin(firstEnemy).getSpeed() : 4.0f + RNG.rand() * 2;
                    PointI currentLocation = firstEnemy.getLocation(startTime).toPointI();
                    Trajectory baseTrajectory;
                    if (map.isValid(currentLocation.x, currentLocation.y)) {
                        baseTrajectory = new TrajectoryGenerator(map, currentLocation, speed)
                                .generateWithDuration(startTime, TRAJECTORY_DURATION, RNG.nextBoolean());
                    } else {
                        baseTrajectory = new Trajectory(speed)
                                .addPoint(currentLocation.x, currentLocation.y, startTime)
                                .addPoint(currentLocation.x, currentLocation.y, startTime);
                    }
                    firstEnemy.setTrajectory(baseTrajectory);
                    firstEnemy.setSpeed(speed);
                    trajectories.put(firstEnemy.getId(), baseTrajectory);

                    for (int i = 1; i < swarmEnemies.size(); i++) {
                        Enemy enemy = swarmEnemies.get(i);
                        Point initialLocation = enemy.getTrajectory().getPoints().get(0);
                        Trajectory trajectory = TrajectoryUtils.generateSimilarTrajectory(baseTrajectory,
                                initialLocation.getX() - basePoint.getX(),
                                initialLocation.getY() - basePoint.getY(),
                                0, 0, speed, 0, startTime, 0);
                        enemy.setTrajectory(trajectory);
                        enemy.setSpeed(speed);
                        trajectories.put(enemy.getId(), trajectory);
                    }
                }
            }
        } /*else {
            for (long enemyId : swarm.getEnemyIds()) {
                Enemy enemy = getItemById(enemyId);
                if (enemy != null) {
                    generateEnemyUpdateTrajectory(enemy, startTime, needFinalSteps, trajectories);
                }
            }
        }*/
    }

    private Skin getEnemySkin(Enemy enemy) {
        return enemy.getEnemyClass().getEnemyType().getSkin(enemy.getSkin());
    }

    private void generateEnemyUpdateTrajectory(Enemy enemy, long startTime, boolean needFinalSteps, Map<Long, Trajectory> trajectories) {
        if ((needFinalSteps && !enemy.isBoss()) || EnemyRange.Scarabs.getEnemies().contains(enemy.getEnemyClass().getEnemyType()) || enemy.isPartOfSwarm()) {
            return;
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

    @Override
    public Set<Long> getEnemiesForNewEnemyUpdating(boolean needFinalSteps, boolean needReturnAllEnemies) {
        if (!getInactivityLiveItems().isEmpty())
            clearInactivityLiveItems();
        return new HashSet<>();
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
//        if (EnemyRange.MINI_BOSS.getEnemies().contains(enemyType)) {
//            Trajectory trajectory = new Trajectory(0, new ArrayList<>());
//            do {
//                PointI source = getRandomSpawnPoint();
//                try {
//                    trajectory = new MinStepTrajectoryGenerator(map, source, speed, 17, 22)
//                            .generate(System.currentTimeMillis() + 1000, minSteps);
//                } catch (Exception e) {
//                    LOG.error("Failed to generate trajectory from " + source, e);
//                }
//            } while (trajectory.getPoints().isEmpty());
//            return trajectory;
//        }
        if (EnemyRange.Scarabs.getEnemies().contains(enemyType)) {
            PointI source = getRandomSpawnPoint();
            return new FreeAngleTrajectoryGenerator(map, source, speed)
                    .generate(System.currentTimeMillis() + 1000, minSteps, needFinalSteps);
        }
        return super.getRandomTrajectory(enemyType, speed, minSteps, needFinalSteps);
    }

    @Override
    protected Trajectory getInitialTrajectory(double speed, boolean needFinalSteps, IEnemyType enemyType) {
        PointI source = getRandomSpawnPoint();
        if (enemyType.getId() == 0 || enemyType.getId() == 4) {
            return new FreeAngleTrajectoryGenerator(map, source, speed)
                    .generate(System.currentTimeMillis() + 1000, 7, needFinalSteps);
        }
        if (enemyType.equals(EnemyType.ENEMY_16))
            return new HorusTrajectoryGenerator(map, new PointI(), speed)
                    .generate(System.currentTimeMillis() + 1000, 7, needFinalSteps);

        if (EnemyRange.LOW_MUMMIES.getEnemies().contains(enemyType)) {
            return new TrajectoryGenerator(map, source, RNG.nextBoolean() ? speed : 4.0f + RNG.rand() * 2)
                    .generateWithDuration(System.currentTimeMillis() + 1000, getTrajectoryDuration(), false);
        }
        long spawnTime = System.currentTimeMillis() + 1000;
        return new TrajectoryGenerator(map, source, speed).generate(spawnTime, 2, needFinalSteps);
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
    public int getTrajectoryDuration() {
        return 10000;
    }

    public int generateSwarmId() {
        return swarmIdGenerator.getAndIncrement();
    }

    @Override
    protected TrajectoryGenerator getBossTrajectoryGenerator(PointI spawnPoint, double speed, boolean needStandOnPlace) {
        return new BossTrajectoryGenerator(map, spawnPoint, speed);
    }

    public Trajectory getPredefinedTrajectory(ISwarmType swarmType, float speed) {
        List<Trajectory> trajectories = map.getPredefinedTrajectories(swarmType);
        if (trajectories != null) {
            return prepareTrajectory(getRandomElement(trajectories), speed);
        }
        return null;
    }

    @Override
    protected void addPointsFromOldTrajectory(List<Point> points, long startTime, long freezeTime, PointI location, Enemy enemy) {
        if (EnemyType.ENEMY_16.equals(enemy.getEnemyClass().getEnemyType())) {
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
        getLogger().debug("addPointsFromOldTrajectoryWithTeleport");
        points.clear();
        List<Point> oldPoints = enemy.getTrajectory().getPoints();
        for (int i = 0; i < oldPoints.size(); i++) {
            getLogger().debug("i: {}, oldPoints.get(i): {}", i, oldPoints.get(i));
        }

        int i = 0;
        int teleportIndex = 0;
        while (i < oldPoints.size() - 1 && oldPoints.get(i).getTime() <= startTime) {
            if (oldPoints.get(i) instanceof TeleportPoint) {
                teleportIndex = i;
            }
            i++;
        }

        getLogger().debug("teleportIndex: " + teleportIndex + " i: " + i);

        for (int j = teleportIndex; j < i; j++) {
            points.add(oldPoints.get(j));
        }

        getLogger().debug("points 1: " + points);

        boolean freezePoint1 = oldPoints.get(i).isFreezePoint();
        boolean freezePoint2 = i > 0 && oldPoints.get(i - 1).isFreezePoint();
        getLogger().debug("freezePoint1: " + freezePoint1 + " freezePoint2: " + freezePoint2);

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

        for (i = 0; i < points.size(); i++) {
            getLogger().debug("i: {}, points.get(i): {}", i, points.get(i));
        }

    }
}
