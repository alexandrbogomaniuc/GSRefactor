package com.betsoft.casino.mp.sectorx.model;

import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.betsoft.casino.mp.model.movement.*;
import com.betsoft.casino.mp.sectorx.model.math.*;
import com.betsoft.casino.mp.sectorx.model.math.config.*;
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
import org.apache.commons.lang.mutable.MutableDouble;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.sectorx.model.math.EnemyRange.*;
import static com.betsoft.casino.mp.sectorx.model.math.EnemyType.*;

@SpringAware
public class GameMap extends AbstractGameMap<Enemy, GameMapShape, EnemyRange, EnemyType, GameMap> {
    protected static final String STATIC_POINTS = "2";
    private static final Long FLIP_XY = 1L;
    private EnemyRange possibleEnemies;
    private long minBossReturnTime;
    private int bossHP;
    private transient EnumMap<EnemyType, Long> removeTimes;
    private transient Map<Integer, Long> removeTimesForSwarms;
    private transient double tf = 1;
    private transient SpiralWaveParams spiralWaveParams = new SpiralWaveParams();
    private transient double sizeX = 960;
    private transient double sizeY = 540;
    private transient double crossSpeed;
    private transient PointD lastPointBossTrajectory = null;
    private transient long predefinedCrossTime = 3000;


    private transient Map<Long, Integer> countTrajByIds = new HashMap<>();

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
        Enemy enemy;

        if (SPECIAL_ITEMS.contains(enemyType)) {
            enemy = new EnemySpecialItem(enemyIdsGenerator.getAndIncrement(), enemyClass, skinId, trajectory, mathEnemy,
                    parentEnemyId, new ArrayList<>(), new ArrayList<>(), 0, 1, 0);
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
            IMovementStrategy movementStrategy = enemy.getMovementStrategy();
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

    public boolean isPointsPresent(String key) {
        List<PointD> points = map.getPoints(key);
        return points != null && !points.isEmpty();
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
                .generate(time, (int) enemyLifeTime);
        Enemy staticEnemy = addItem(enemyType, skinId, trajectory, 4, null, -1);
        staticEnemy.setEnergy(staticEnemy.getFullEnergy());
        return staticEnemy;
    }

    private boolean checkDistance(List<PointD> enemyLocations, PointD currentLocation) {
        for (PointD enemyLocation : enemyLocations) {
            if (getDistance(currentLocation.x, currentLocation.y, enemyLocation.x, enemyLocation.y) < 0.1) {
                return false;
            }
        }
        return true;
    }

    public Enemy spawnBoss(BossType bossType, Trajectory trajectory, int fullHP, SpawnConfig spawnConfig) {
        int skinId = bossType.getSkinId();
        int skin = (skinId == -1 ? getRandomSkin(BOSS) : skinId);
        float speed = generateSpeed(BOSS.getSkin(skin));
        Enemy boss = addItem(BOSS, skin, trajectory, speed, null, -1);
        boss.setFullEnergy(fullHP);
        boss.setEnergy(fullHP);
        boss.setLives(RNG.nextInt(3) + 3);
        return boss;
    }

    public Enemy checkReturnedBoss() {
        Enemy res = null;
        lockEnemy.lock();
        List<Long> needClearIds = new ArrayList<>();
        try {
            if (!removedEnemies.isEmpty()) {
                for (Map.Entry<Long, Enemy> entry : removedEnemies.entrySet()) {
                    Enemy enemy = entry.getValue();
                    if (System.currentTimeMillis() > enemy.getReturnTime()) {
                        enemy.setReturnTime(0);
                        items.add(enemy);
                        needClearIds.add(enemy.getId());
                        res = enemy;
                    }
                }
            }

            for (Long needClearId : needClearIds) {
                removedEnemies.remove(needClearId);
            }
        } finally {
            lockEnemy.unlock();
        }
        return res;
    }

    public boolean updateBossRound() {
        lockEnemy.lock();
        try {
            for (Enemy enemy : getItems()) {
                if (enemy.isBoss()) {
                    if (enemy.update()) {
                        bossHP = (int) enemy.getEnergy();
                        if (enemy.getLives() > 0) {
                            enemy.setLives(enemy.getLives() - 1);
                            enemy.setReturnTime(System.currentTimeMillis() + 4000);
                            removedEnemies.put(enemy.getId(), enemy);
                            items.remove(enemy);
                            return false;
                        } else {
                            items.remove(enemy);
                            return true;
                        }
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
        return addItem(enemyType, skin, trajectory, speed, null, -1);
    }

    public Enemy addEnemyWithTrajectory(EnemyType enemyType, Trajectory trajectory, long parentEnemyId) {
        int skin = getRandomSkin(enemyType);
        float speed = generateSpeed(enemyType.getSkin(skin));
        return addItem(enemyType, skin, trajectory, speed, null, parentEnemyId);
    }

    public TrajectorySector getSectorByCenterPoint(double x, double y) {
        if (x > sizeX / 2 && y > sizeY / 2) {
            return TrajectorySector.HGROUPIV;
        } else if (x > sizeX / 2 && y < sizeY / 2) {
            return TrajectorySector.HGROUPI;
        } else if (x < sizeX / 2 && y > sizeY / 2) {
            return TrajectorySector.HGROUPIII;
        } else {
            return TrajectorySector.HGROUPII;
        }
    }

    public EnemyRange getRangeByEnemyType(EnemyType enemyType) {
        if (LOW_PAY_ENEMIES.getEnemies().contains(enemyType)) {
            return LOW_PAY_ENEMIES;
        } else if (MID_PAY_ENEMIES.getEnemies().contains(enemyType)) {
            return MID_PAY_ENEMIES;
        } else {
            return HIGH_PAY_ENEMIES;
        }
    }

    private List<SpawnStageFromConfig> getSpawnStage(SpawnConfig spawnConfig) {
        List<SpawnStageFromConfig> initSpawnStage = new ArrayList<>();
        for (int i = 1; i < spawnConfig.getTimeSlices().size(); i++) {
            List<Double> resultList = new ArrayList<>();
            resultList.add(spawnConfig.getLowPayWeights().get(i - 1) / 100);
            resultList.add(spawnConfig.getMidPayWeights().get(i - 1) / 100);
            resultList.add(spawnConfig.getHighPayWeights().get(i - 1) / 100);
            initSpawnStage.add(new SpawnStageFromConfig(spawnConfig.getTimeSlices().get(i) * 1000L, resultList));
        }
        return initSpawnStage;
    }

    public Pair<EnemyType, Trajectory> getRandomTrajectoryFromSpawnSystem(long startRoundTime, GameConfig config, SpawnConfig spawnConfig) {
        Pair<EnemyType, Trajectory> res = null;

        long currentTime = System.currentTimeMillis();

        MutableDouble totalPay = new MutableDouble(0);
        MutableDouble xPayCenterMass = new MutableDouble(0);
        MutableDouble yPayCenterMass = new MutableDouble(0);
        AtomicInteger totalEnemies = new AtomicInteger(0);
        List<Enemy> enemies = getItems();
        int T = enemies.size();

        SpawnStageFromConfig spawnStageFromConfig = SpawnStage.getStageWeights(startRoundTime, getSpawnStage(spawnConfig));
        if (!enemies.isEmpty()) {
            enemies.forEach(enemy -> {
                PointD enemyLocation = enemy.getLocation(currentTime);
                int enemyPayout;
                boolean isBossOrSpecialItem = enemy.isBoss() || SPECIAL_ITEMS.getEnemies().contains(enemy.getEnemyType());
                if (!isBossOrSpecialItem) {
                    enemyPayout = MathData.getEnemyPayout(config, enemy.getEnemyType(), 0);
                    xPayCenterMass.add(enemyLocation.x * enemyPayout);
                    yPayCenterMass.add(enemyLocation.y * enemyPayout);
                    totalPay.add(enemyPayout);
                    EnemyRange key = getRangeByEnemyType(enemy.getEnemyType());
                    totalEnemies.incrementAndGet();
                }
            });

            xPayCenterMass.setValue(xPayCenterMass.doubleValue() / totalPay.doubleValue());
            yPayCenterMass.setValue(yPayCenterMass.doubleValue() / totalPay.doubleValue());
        }

        TrajectorySector possibleTrajectorySector = getSectorByCenterPoint(xPayCenterMass.doubleValue(), yPayCenterMass.doubleValue());

        getLogger().debug("xPayCenterMass: {}, yPayCenterMass: {}, spawnStageFromConfig: {}, possibleTrajectorySector: {}, totalPay: {} ",
                xPayCenterMass.doubleValue(), yPayCenterMass.doubleValue(), spawnStageFromConfig, possibleTrajectorySector, totalPay);

        Map<Integer, Integer> counter = countEnemyTypes();
        Map<Integer, Double> enemiesWeights = new HashMap<>();
        getLogger().debug("counter: {}", counter);
        BASE_ENEMIES.getEnemies().forEach(enemyType -> {
            if (config.enemyTypeEnabled(enemyType)) {
                double alpha = spawnStageFromConfig.getEnemyWeightByEnemyType(enemyType, 0);
                if (counter.containsKey(enemyType.getId())) {
                    int countEn = counter.get(enemyType.getId());
                    int N = counter.size();
                    N = Math.max(N, 2);
                    double weight = alpha / (N - 1) - alpha * countEn / ((N - 1) * T);
                    enemiesWeights.put(enemyType.getId(), weight);
                } else {
                    int N = BASE_ENEMIES.getEnemies().size();
                    double weight = alpha / (N - 1) - alpha / ((N - 1) * T);
                    enemiesWeights.put(enemyType.getId(), weight);
                }
            }
        });

        double sum = enemiesWeights.values().stream().mapToDouble(Double::doubleValue).sum();

        Map<Integer, Double> enemiesWeightsNorm = enemiesWeights.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / sum));

        EnemyType randomEnemyType = getById(GameTools.getRandomNumberKeyFromMap(enemiesWeightsNorm));

        getLogger().debug("sum: {}, randomEnemyType  :{}", sum, randomEnemyType);
        getLogger().debug("enemiesWeights: {}", enemiesWeights);
        getLogger().debug("enemiesWeightsNorm: {}", enemiesWeightsNorm);

        Trajectory randomTrajectory = getRandomTrajectory(randomEnemyType, spawnConfig, false, possibleTrajectorySector);
        if (randomTrajectory != null) {
            res = new Pair<>(randomEnemyType, randomTrajectory);
        }
        return res;
    }

    public Trajectory getRandomTrajectory(EnemyType enemyType, SpawnConfig spawnConfig) {
        return getRandomTrajectory(enemyType, spawnConfig, -1);
    }

    public Trajectory getRandomTrajectory(EnemyType enemyType, SpawnConfig spawnConfig, long remainingRoundTime) {
        if (SPECIAL_ITEMS.contains(enemyType)) {
            boolean rightCorner = RNG.nextBoolean();
            int delta = RNG.nextInt(15);
            PointD location = new PointD((double) 61 + delta, 11);
            if (rightCorner) {
                location = new PointD((double) 8 + delta, 74);
            }
            long time = System.currentTimeMillis() + 1000;
            long specialItemsStayTime;
            specialItemsStayTime = spawnConfig.getSpecialItemsStayTime();
            Trajectory trajectory = new WeaponCarrierTrajectoryGenerator(map, location.toPointI(), 4)
                    .generate(time, (int) specialItemsStayTime - 3);
            Point lastPoint = trajectory.getLastPoint();
            ITrajectoryGenerator generator = new WaypointTrajectoryGenerator(map);
            Trajectory leaveTrajectory = generateLeaveTrajectoryBySpeed(generator, getLocation(lastPoint.getTime(), trajectory.getPoints()).toPointI(),
                    lastPoint.getTime(), enemyType.getSkins().get(0).getSpeed());
            trajectory.addPoints(leaveTrajectory.getPoints());
            return trajectory;
        } else if (HUGE_PAY_ENEMIES.contains(enemyType)) {
            Trajectory trajectory = getPredefinedHugeTrajectory();
            List<Long> times = spawnConfig.getHugePayItemsStayTimes();
            long startTime = System.currentTimeMillis();
            long timePath = times.get(RNG.nextInt(times.size() - 1)) * 1000L;
            if (enemyType.equals(B3)) {
                timePath = (long) (timePath * 1.2);
            }

            Long min = Collections.min(times) * 1000L;
            getLogger().debug("getRandomTrajectory remainingRoundTime: {}, timePath: {}, min: {} ",
                    remainingRoundTime, timePath, min);

            if (remainingRoundTime < timePath) {
                if (remainingRoundTime < min) {
                    getLogger().debug("remainingRoundTime less minimal time, return without generation for huge enemy");
                    return null;
                } else {
                    getLogger().debug("remainingRoundTime more minimal time, update to min for huge enemy");
                    timePath = min;
                }
            }

            long timeOnePart = timePath / trajectory.getPoints().size();
            int i = 1;
            List<Point> newPoints = new ArrayList<>();
            for (Point point : trajectory.getPoints()) {
                newPoints.add(new Point(point.getX(), point.getY(), startTime + i * timeOnePart));
                i++;
            }
            HybridTrajectory hybridTrajectory = new HybridTrajectory(0, newPoints);
            return hybridTrajectory;
        }
        else {
            return getRandomTrajectory(enemyType, spawnConfig, false, null);
        }
    }

    public Trajectory updatePointBySector(Trajectory trajectory) {
        List<Point> points = trajectory.getPoints();
        double newX = 0;
        double newY = 0;
        TrajectorySector sector = getSectorByCenterPoint(points.get(points.size() - 1).getX(), points.get(points.size() - 1).getY());
        if (sector.equals(TrajectorySector.HGROUPI)) {
            newX = points.get(points.size() - 1).getX() + 60;
            newY = points.get(points.size() - 1).getY() - 60;
        }
        if (sector.equals(TrajectorySector.HGROUPII)) {
            newX = points.get(points.size() - 1).getX() - 60;
            newY = points.get(points.size() - 1).getY() - 60;
        }
        if (sector.equals(TrajectorySector.HGROUPIII)) {
            newX = points.get(points.size() - 1).getX() - 60;
            newY = points.get(points.size() - 1).getY() + 60;
        }
        else if (sector.equals(TrajectorySector.HGROUPIV)) {
            newX = points.get(points.size() - 1).getX() + 60;
            newY = points.get(points.size() - 1).getY() + 60;
        }
        List<Point> newPoints = new ArrayList<>();
        int i = 0;
        for (Point point : points) {
            if (i == (points.size() -1)) {
                newPoints.add(new Point(newX, newY, point.getTime()));
            } else {
                newPoints.add(new Point(point.getX(), point.getY(), point.getTime()));
            }
            i++;
        }
        return new Trajectory(0, newPoints);
    }

    private Trajectory getPredefinedHugeTrajectory() {
        Map<Integer, List<Trajectory>> hugeTrajectoriesMap = map.getPredefinedTrajectories();
        int rndNum = RNG.nextInt(2, hugeTrajectoriesMap.size());
        return hugeTrajectoriesMap.get(rndNum).get(0);
    }

    public int getBossPathSection(SpawnConfig spawnConfig) {
        List<PathSection> pathSections = spawnConfig.getPredefinedBOSSInitalPaths();
        return RNG.nextInt(pathSections.size());
    }

    public boolean isLastTrajectoryInPath(SpawnConfig spawnConfig, int pathNumber, int pathCount) {
        List<PathSection> pathSections = spawnConfig.getPredefinedBOSSInitalPaths();
        return pathSections.get(pathNumber).getPathSections().size() - 1 == pathCount;
    }

    private Trajectory updateStartAndReenterPoint(Trajectory trajectory, double offset) {
        List<Point> points = trajectory.getPoints();
        double x = points.get(0).getX();
        double y = points.get(0).getY();
        PointD newStartPoint = updatePointBySide(new PointD(x, y), offset);
        double lastX = points.get(points.size() - 1).getX();
        double lastY = points.get(points.size() - 1).getY();
        PointD newLastPoint = updatePointBySide(new PointD(lastX, lastY), offset);
        List<Point> newPoints = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (i == 0) {
                newPoints.add(new Point(newStartPoint.x, newStartPoint.y, points.get(i).getTime()));
            } else if (i == points.size() - 1) {
                newPoints.add(new Point(newLastPoint.x, newLastPoint.y, points.get(i).getTime()));
            } else {
                newPoints.add(new Point(points.get(i).getX(), points.get(i).getY(),
                        points.get(i).getTime()));
            }
        }
        return new BezierTrajectory(0, newPoints);
    }

    public Trajectory getBossFirstTrajectory(SpawnConfig spawnConfig) {
        long startTime = System.currentTimeMillis() + 4000;
        List<Point> points = new ArrayList<>();
        List<PredefinedPoint> predefinedPoints = new ArrayList<>();
        List<PathSection> pathSections = spawnConfig.getPredefinedBOSSInitalPaths();
        List<PredefinedPathParam> initPaths = pathSections.get(RNG.nextInt(pathSections.size())).getPathSections();
        long pathTime = 10000L * initPaths.size() + RNG.nextInt(5000 * initPaths.size());
        for (PredefinedPathParam param : initPaths) {
            predefinedPoints.addAll(param.getTrajectoryPoints());
        }
        long timePart = pathTime / predefinedPoints.size();
        for (int i = 0; i < predefinedPoints.size(); i++) {
            points.add(new Point(predefinedPoints.get(i).getX(), predefinedPoints.get(i).getY(), startTime + timePart * i));
        }
        lastPointBossTrajectory = new PointD(points.get(points.size() - 1).getX(), points.get(points.size() - 1).getY());
        return new BezierTrajectory(0, points);
    }

    public Trajectory getFirstBossTrajectory(SpawnConfig spawnConfig, int trajectoryNumber, long startTime, int pathSection) {
        List<PathSection> pathSections = spawnConfig.getPredefinedBOSSInitalPaths();
        List<PredefinedPathParam> initPaths = pathSections.get(pathSection).getPathSections();
        List<PredefinedPoint> trajectoryPoints = initPaths.get(trajectoryNumber).getTrajectoryPoints();
        if (trajectoryNumber == 0) {
            if (initPaths.get(trajectoryNumber).getTrajectoryType().equals(TrajectoryType.QUADBEZIER)) {
                BezierTrajectory quadBezierTrajectory = generateQuadBezierTrajectory(trajectoryPoints, false, 0, false, 0);
                return quadBezierTrajectory;
            }
            if (initPaths.get(trajectoryNumber).getTrajectoryType().equals(TrajectoryType.CUBICBEZIER)) {
                BezierTrajectory cubicBezierTrajectory = generateCubicBezierTrajectory(trajectoryPoints, false, 0, false, 0);
                return cubicBezierTrajectory;
            } else {
                BezierTrajectory linearBezierTrajectory = generateLinearBezierTrajectory(trajectoryPoints, false, 0, false, 0);
                return linearBezierTrajectory;
            }
        } else {
            if (initPaths.get(trajectoryNumber).getTrajectoryType().equals(TrajectoryType.QUADBEZIER)) {
                BezierTrajectory quadBezierTrajectory = generateQuadBezierTrajectory(trajectoryPoints, false, 0, true, startTime);
                return quadBezierTrajectory;
            }
            if (initPaths.get(trajectoryNumber).getTrajectoryType().equals(TrajectoryType.CUBICBEZIER)) {
                BezierTrajectory cubicBezierTrajectory = generateCubicBezierTrajectory(trajectoryPoints, false, 0, true, startTime);
                return cubicBezierTrajectory;
            } else {
                BezierTrajectory linearBezierTrajectory = generateLinearBezierTrajectory(trajectoryPoints, false, 0, true, startTime);
                return linearBezierTrajectory;
            }
        }
    }

    public Trajectory getInitBossTrajectory(SpawnConfig spawnConfig) {
        List<PathSection> pathSections = spawnConfig.getPredefinedBOSSInitalPaths();
        List<PredefinedPathParam> initPaths = pathSections.get(RNG.nextInt(pathSections.size())).getPathSections();
        List<Point> bossPoints = new ArrayList<>();
        boolean isFirstTraj = true;
        long lastPointTime = System.currentTimeMillis();
        for (int i = 0; i < initPaths.size(); i++) {
            List<PredefinedPoint> predefinedPoints = initPaths.get(i).getTrajectoryPoints();
            generatePointsOfTrajectories(bossPoints, predefinedPoints, initPaths.get(i).getTrajectoryType(), lastPointTime, isFirstTraj, 0);
            lastPointTime = bossPoints.get(bossPoints.size() - 1).getTime();
            isFirstTraj = false;
        }
        lastPointBossTrajectory = new PointD(bossPoints.get(bossPoints.size() - 1).getX(), bossPoints.get(bossPoints.size() - 1).getY());
        return new BezierTrajectory(0, bossPoints);
    }

    private PointD updatePointBySide(PointD pointD, double offset) {
        double valueX = pointD.x;
        double valueY = pointD.y;
        SideMap sideMap = getSideByPoint(pointD);
        if (sideMap != null) {
            if (sideMap.equals(SideMap.NORTH)) {
                double diffY = Math.abs(valueY);
                if (diffY < offset) {
                    valueY = valueY - (offset - diffY);
                }
            } else if (sideMap.equals(SideMap.SOUTH)) {
                double diffY = valueY - 540;
                if (diffY < offset) {
                    valueY = valueY + (offset - diffY);
                }
            } else if (sideMap.equals(SideMap.EAST)) {
                double diffX = valueX - 960;
                if (diffX < offset) {
                    valueX = valueX + (offset - diffX);
                }
            } else if (sideMap.equals(SideMap.WEST)) {
                double diffX = Math.abs(valueX);
                if (diffX < offset) {
                    valueX = valueX - (offset - diffX);
                }
            }
        } else {
            getLogger().debug("sideMap is null");
        }
        return new PointD(valueX, valueY);
    }

    public Trajectory getRandomTrajectory(EnemyType enemyType, SpawnConfig spawnConfig, boolean isFirstBossSpawn,
                                          TrajectorySector trajectorySector) {
        List<PredefinedPathParam> predefinedPathParams = spawnConfig.getPredefinedPaths();
        if (predefinedPathParams != null && !predefinedPathParams.isEmpty()) {
            if (enemyType.isBoss()) {
                List<PathSection> predefinedBOSSInitalPaths = spawnConfig.getPredefinedBOSSInitalPaths();
                if (isFirstBossSpawn) {
                    int rndPathSection = RNG.nextInt(predefinedBOSSInitalPaths.size());
                    List<PredefinedPathParam> initPaths = predefinedBOSSInitalPaths.get(rndPathSection).getPathSections();
                    int rndPathTrajectory = 0;
                    List<PredefinedPoint> trajectoryPoints = initPaths.get(rndPathTrajectory).getTrajectoryPoints();
                    BezierTrajectory bezierTrajectory = generateCubicBezierTrajectory(trajectoryPoints, false, 0, false, 0);
                    return bezierTrajectory;
                } else {
                    List<PredefinedPathParam> pathsBySide = determineReenterSidePaths(spawnConfig, lastPointBossTrajectory);
                    int rndReenterPath = RNG.nextInt(pathsBySide.size() - 1);
                    List<PredefinedPoint> trajectoryPoints = pathsBySide.get(rndReenterPath).getTrajectoryPoints();
                    TrajectoryType trajectoryType = pathsBySide.get(rndReenterPath).getTrajectoryType();
                    lastPointBossTrajectory = new PointD(trajectoryPoints.get(trajectoryPoints.size() - 1).getX(),
                            trajectoryPoints.get(trajectoryPoints.size() - 1).getY());
                    BezierTrajectory reenterTrajectory = null;
                    if (trajectoryType.equals(TrajectoryType.LINEAR)) {
                        reenterTrajectory = generateLinearBezierTrajectory(trajectoryPoints, false, 0, false, 0);
                    } else if (trajectoryType.equals(TrajectoryType.QUADBEZIER)) {
                        reenterTrajectory = generateQuadBezierTrajectory(trajectoryPoints, false, 0, false, 0);
                    } else if (trajectoryType.equals(TrajectoryType.CUBICBEZIER)) {
                        reenterTrajectory = generateCubicBezierTrajectory(trajectoryPoints, false, 0, false, 0);
                    }
                    return updateStartAndReenterPoint(reenterTrajectory, 150);
                }
            }

            PredefinedPathParam predefinedPathParam;
            if (trajectorySector == null) {
                predefinedPathParam = predefinedPathParams.get(RNG.nextInt(predefinedPathParams.size()));
            } else {
                List<PredefinedPathParam> predefinedPathParamList = predefinedPathParams.stream()
                        .filter(prePathParam -> prePathParam.getTrajectorySector().equals(trajectorySector))
                        .collect(Collectors.toList());
                predefinedPathParam = predefinedPathParamList.get(RNG.nextInt(predefinedPathParamList.size()));
            }


            List<PredefinedPoint> trajectoryPoints = predefinedPathParam.getTrajectoryPoints();
            if (predefinedPathParam.getTrajectoryType().equals(TrajectoryType.QUADBEZIER)) {
                BezierTrajectory quadBezierTrajectory = generateQuadBezierTrajectory(trajectoryPoints, false, 0, false, 0);
                return quadBezierTrajectory;
            }
            if (predefinedPathParam.getTrajectoryType().equals(TrajectoryType.CUBICBEZIER)) {
                BezierTrajectory cubicBezierTrajectory = generateCubicBezierTrajectory(trajectoryPoints, false, 0, false, 0);
                return cubicBezierTrajectory;
            } else {
                BezierTrajectory linearBezierTrajectory = generateLinearBezierTrajectory(trajectoryPoints, false, 0, false, 0);
                return linearBezierTrajectory;
            }
        }
        return null;
    }

    public Trajectory getTrajectory(PredefinedPathParam predefinedPathParam, boolean isTimePredefined, long time) {
        List<PredefinedPoint> trajectoryPoints = predefinedPathParam.getTrajectoryPoints();
        if (predefinedPathParam.getTrajectoryType().equals(TrajectoryType.QUADBEZIER)) {
            BezierTrajectory quadBezierTrajectory = generateQuadBezierTrajectory(trajectoryPoints, isTimePredefined, time, false, 0);
            return quadBezierTrajectory;
        }
        if (predefinedPathParam.getTrajectoryType().equals(TrajectoryType.CUBICBEZIER)) {
            BezierTrajectory cubicBezierTrajectory = generateCubicBezierTrajectory(trajectoryPoints, isTimePredefined, time, false, 0);
            return cubicBezierTrajectory;
        } else {
            BezierTrajectory linearBezierTrajectory = generateLinearBezierTrajectory(trajectoryPoints, isTimePredefined, time, false, 0);
            return linearBezierTrajectory;
        }
    }

    public SideMap getSideByPoint(PointD lastPoint) {
        double x = lastPoint.x;
        double y = lastPoint.y;
        SideMap sideMap = null;
        if (y < 0 && (x > 0 && x < 960)) {
            sideMap = SideMap.NORTH;
        }
        if (y > 540 && (x > 0 && x < 960)) {
            sideMap = SideMap.SOUTH;
        }
        if (x > 960 && (y > 0 && y < 540)) {
            sideMap = SideMap.EAST;
        }
        if (x < 0 && (y > 0 && y < 540)){
            sideMap = SideMap.WEST;
        }
        if (sideMap == null) {
            sideMap = getSideWithBoundaryCondition(lastPoint);
        }
        return sideMap;
    }

    private SideMap getSideWithBoundaryCondition(PointD lastPoint) {
        double x = lastPoint.x;
        double y = lastPoint.y;
        double coef = 1.4;
        if (y < 0 && x > 960) {
            double newX = x - 960;
            double newY = Math.abs(y) * coef;
            if (newY > newX) {
                return SideMap.NORTH;
            } else {
                return SideMap.EAST;
            }
        }
        if (y > 540 && x > 960) {
            double newX = x - 960;
            double newY = y - 540;
            newY = newY * coef;
            if (newY > newX) {
                return SideMap.SOUTH;
            } else {
                return SideMap.EAST;
            }
        }
        if (y > 540 && x < 0) {
            double newX = Math.abs(x);
            double newY = y - 540;
            newY = newY * coef;
            if (newY > newX) {
                return SideMap.SOUTH;
            } else {
                return SideMap.WEST;
            }
        }
        if (y < 0 && x < 0) {
            double newX = Math.abs(x);
            double newY = Math.abs(y);
            newY = newY * coef;
            if (newY > newX) {
                return SideMap.NORTH;
            } else {
                return SideMap.WEST;
            }
        }
        return null;
    }

    public List<PredefinedPathParam> determineReenterSidePaths(SpawnConfig spawnConfig, PointD lastPoint) {
        List<PredefinedPathParam> reenterPaths = spawnConfig.getPredefinedBOSSReenterPaths();
        List<PredefinedPathParam> resultPaths = new ArrayList<>();
        double x = lastPoint.x;
        double y = lastPoint.y;
        SideMap sideMap = getSideByPoint(lastPoint);
        if (sideMap.equals(SideMap.NORTH)) {
            for (PredefinedPathParam param : reenterPaths) {
                double paramX = param.getTrajectoryPoints().get(0).getX();
                double paramY = param.getTrajectoryPoints().get(0).getY();
                if (paramY < 0 && paramX > 0 && paramX < 960) {
                    resultPaths.add(param);
                }
            }
        }
        if (sideMap.equals(SideMap.SOUTH)) {
            for (PredefinedPathParam param : reenterPaths) {
                double paramX = param.getTrajectoryPoints().get(0).getX();
                double paramY = param.getTrajectoryPoints().get(0).getY();
                if (paramY > 540 && paramX > 0 && paramX < 960) {
                    resultPaths.add(param);
                }
            }
        }
        if (sideMap.equals(SideMap.EAST)) {
            for (PredefinedPathParam param : reenterPaths) {
                double paramX = param.getTrajectoryPoints().get(0).getX();
                double paramY = param.getTrajectoryPoints().get(0).getY();
                if (paramX > 960 && paramY > 0 && paramY < 540) {
                    resultPaths.add(param);
                }
            }
        }
        if (sideMap.equals(SideMap.WEST)) {
            for (PredefinedPathParam param : reenterPaths) {
                double paramX = param.getTrajectoryPoints().get(0).getX();
                double paramY = param.getTrajectoryPoints().get(0).getY();
                if (paramX < 0 && paramY > 0 && paramY < 540) {
                    resultPaths.add(param);
                }
            }
        }
        return resultPaths;
    }

    public List<Enemy> getSpatialEnemiesWithParallelTrajectory(SpawnConfig spawnConfig) {
        List<Enemy> enemies = new ArrayList<>();
        EnemyType enemyType;
        List<SpatialPoint> templatePoints;
        int rndType = RNG.nextInt(1,3);
        if (rndType == 1) {
            enemyType = spawnConfig.getSpatialEnemiesC1().get(RNG.nextInt(spawnConfig.getSpatialEnemiesC1().size()));
            templatePoints = spawnConfig.getSpatialFormationTemplatesC1().get(RNG.nextInt(spawnConfig.getSpatialFormationTemplatesC1().size()));
        } else {
            enemyType = spawnConfig.getSpatialEnemiesC2().get(RNG.nextInt(spawnConfig.getSpatialEnemiesC2().size()));
            templatePoints = spawnConfig.getSpatialFormationTemplatesC2().get(RNG.nextInt(spawnConfig.getSpatialFormationTemplatesC2().size()));
        }
        PredefinedPathParam predefinedPathParam = spawnConfig.getPredefinedPaths().get(RNG.nextInt(spawnConfig.getPredefinedPaths().size()));
        Enemy tmpEnemy = createEnemy(enemyType, 0, new Trajectory(0), 0, null, 0);
        double coordOffset = tmpEnemy.getCircularRadius(enemyType.getId()) * templatePoints.size();
        List<PredefinedPoint> oldPoints = predefinedPathParam.getTrajectoryPoints();
        List<List<Point>> trajectoriesPoints = new ArrayList<>();
        List<PredefinedPoint> newPoints = new ArrayList<>();
        PointD newEndPoint = modifyEndPointBySide(new PointD(oldPoints.get(oldPoints.size() - 1).getX(), oldPoints.get(oldPoints.size() - 1).getY()), coordOffset);
        for (int i = 0; i < oldPoints.size(); i++) {
            if (i == oldPoints.size() - 1) {
                newPoints.add(new PredefinedPoint(newEndPoint.x, newEndPoint.y, 0));
            } else {
                newPoints.add(new PredefinedPoint(oldPoints.get(i).getX(), oldPoints.get(i).getY(), 0));
            }
        }
        List<Point> pointsForLeader = generateBezierTrajectoryByType(newPoints, predefinedPathParam.getTrajectoryType(), true, 15000, false, 0).getPoints();
        boolean isLeaderTrajectory = true;
        for (SpatialPoint spatialPoint : templatePoints) {
            double sh = tmpEnemy.getCircularRadius(enemyType.getId());
            double sw = tmpEnemy.getCircularRadius(enemyType.getId());
            double offsetX = spatialPoint.getDx() * sh * 1.5;
            double offsetY = spatialPoint.getDy() * sw * 1.5;
            ParallelTrajectory parallelTrajectory = new ParallelTrajectory(0, pointsForLeader);
            parallelTrajectory.setParallelOffsetX(offsetX);
            parallelTrajectory.setParallelOffsetY(offsetY);
            if (isLeaderTrajectory) {
                parallelTrajectory.setParalleltrajectory(false);
                isLeaderTrajectory = false;
            }
            enemies.add(addEnemyWithTrajectory(enemyType, parallelTrajectory));
        }
        return enemies;
    }

    public List<Enemy> getSpatialEnemies(SpawnConfig spawnConfig) {
        List<Enemy> enemies = new ArrayList<>();
        EnemyType enemyType;
        List<SpatialPoint> templatePoints;
        int rndType = RNG.nextInt(1,3);
        if (rndType == 1) {
            enemyType = spawnConfig.getSpatialEnemiesC1().get(RNG.nextInt(spawnConfig.getSpatialEnemiesC1().size()));
            templatePoints = spawnConfig.getSpatialFormationTemplatesC1().get(RNG.nextInt(spawnConfig.getSpatialFormationTemplatesC1().size()));
        } else {
            enemyType = spawnConfig.getSpatialEnemiesC2().get(RNG.nextInt(spawnConfig.getSpatialEnemiesC2().size()));
            templatePoints = spawnConfig.getSpatialFormationTemplatesC2().get(RNG.nextInt(spawnConfig.getSpatialFormationTemplatesC2().size()));
        }
        PredefinedPathParam predefinedPathParam = spawnConfig.getPredefinedPaths().get(RNG.nextInt(spawnConfig.getPredefinedPaths().size()));
        Enemy tmpEnemy = createEnemy(enemyType, 0, new Trajectory(0), 0, null, 0);
        double coordOffset = tmpEnemy.getCircularRadius(enemyType.getId()) * templatePoints.size();
        List<PredefinedPoint> oldPoints = predefinedPathParam.getTrajectoryPoints();
        List<List<Point>> trajectoriesPoints = new ArrayList<>();
        List<PredefinedPoint> newPoints = new ArrayList<>();
        PointD newEndPoint = modifyEndPointBySide(new PointD(oldPoints.get(oldPoints.size() - 1).getX(), oldPoints.get(oldPoints.size() - 1).getY()), coordOffset);
        for (int i = 0; i < oldPoints.size(); i++) {
            if (i == oldPoints.size() - 1) {
                newPoints.add(new PredefinedPoint(newEndPoint.x, newEndPoint.y, 0));
            } else {
                newPoints.add(new PredefinedPoint(oldPoints.get(i).getX(), oldPoints.get(i).getY(), 0));
            }
        }
        List<Point> pointsForLeader = generateBezierTrajectoryByType(newPoints, predefinedPathParam.getTrajectoryType(), true, 15000, false, 0).getPoints();
        for (SpatialPoint spatialPoint : templatePoints) {
            double sh = tmpEnemy.getCircularRadius(enemyType.getId());
            double sw = tmpEnemy.getCircularRadius(enemyType.getId());
            double alpha = predefinedPathParam.getRot();
            double xi = spatialPoint.getDx();
            double yi = spatialPoint.getDy();
            xi = xi * sw * 1.5;
            yi = yi * sh * 1.5;
            double offsetX;
            double offsetY;
            offsetX = Math.cos(alpha) * xi - Math.sin(alpha) * yi;
            offsetY = Math.sin(alpha) * xi + Math.cos(alpha) * yi;
            List<Point> pointsForSecondary = new ArrayList<>();
            for (int j = 0; j < pointsForLeader.size(); j++) {
                pointsForSecondary.add(new Point(pointsForLeader.get(j).getX() + offsetX, pointsForLeader.get(j).getY() + offsetY,
                        pointsForLeader.get(j).getTime()));
            }
            trajectoriesPoints.add(pointsForSecondary);
        }
        List<Trajectory> trajectories = new ArrayList<>();
        for (List<Point> points : trajectoriesPoints) {
            enemies.add(addEnemyWithTrajectory(enemyType, new BezierTrajectory(0, points)));
        }
        return enemies;
    }

    public List<Enemy> spawnSpatialFormation(SpawnConfig spawnConfig) {
        List<Enemy> enemies = new ArrayList<>();
        EnemyType enemyType;
        List<SpatialPoint> templatePoints;
        int rndType = RNG.nextInt(1,3);
        if (rndType == 1) {
            enemyType = spawnConfig.getSpatialEnemiesC1().get(RNG.nextInt(spawnConfig.getSpatialEnemiesC1().size()));
            templatePoints = spawnConfig.getSpatialFormationTemplatesC1().get(RNG.nextInt(spawnConfig.getSpatialFormationTemplatesC1().size()));
        } else {
            enemyType = spawnConfig.getSpatialEnemiesC2().get(RNG.nextInt(spawnConfig.getSpatialEnemiesC2().size()));
            templatePoints = spawnConfig.getSpatialFormationTemplatesC2().get(RNG.nextInt(spawnConfig.getSpatialFormationTemplatesC2().size()));
        }
        PredefinedPathParam predefinedPathParam = spawnConfig.getPredefinedPaths().get(RNG.nextInt(spawnConfig.getPredefinedPaths().size()));
        Enemy tmpEnemy = createEnemy(enemyType, 0, new Trajectory(0), 0, null, 0);
        double coordOffset = tmpEnemy.getCircularRadius(enemyType.getId()) * templatePoints.size();
        List<PredefinedPoint> oldPoints = predefinedPathParam.getTrajectoryPoints();
        PointD newEndPoint = modifyEndPointBySide(new PointD(oldPoints.get(oldPoints.size() - 1).getX(), oldPoints.get(oldPoints.size() - 1).getY()), coordOffset);
        List<PredefinedPoint> newPoints = new ArrayList<>();
        for (int i = 0; i < oldPoints.size(); i++) {
            if (i == oldPoints.size() - 1) {
                newPoints.add(new PredefinedPoint(newEndPoint.x, newEndPoint.y, 0));
            } else {
                newPoints.add(new PredefinedPoint(oldPoints.get(i).getX(), oldPoints.get(i).getY(), 0));
            }
        }
        List<Point> pointsForLeader = generatePointsOfBezierTrajectory(newPoints, predefinedPathParam.getTrajectoryType(), 1, false, false);
        List<Trajectory> trajectories = getSpatialFormationTrajectories(templatePoints, predefinedPathParam, pointsForLeader, enemyType);
        for (Trajectory trajectory : trajectories) {
            enemies.add(addEnemyWithTrajectory(enemyType, trajectory));
        }
        return enemies;
    }

    public PointD modifyEndPointBySide(PointD endPointD, double offset) {
        PointD newEndPoint = null;
        SideMap sideMap = getSideByPoint(endPointD);
        if (sideMap.equals(SideMap.NORTH)) {
            newEndPoint = new PointD(endPointD.x, endPointD.y - offset);
        }
        if (sideMap.equals(SideMap.SOUTH)) {
            newEndPoint = new PointD(endPointD.x, endPointD.y + offset);
        }
        if (sideMap.equals(SideMap.EAST)) {
            newEndPoint = new PointD(endPointD.x + offset, endPointD.y);
        }
        if (sideMap.equals(SideMap.WEST)){
            newEndPoint = new PointD(endPointD.x - offset, endPointD.y);
        }
        return newEndPoint;
    }

    private List<Trajectory> getSpatialFormationTrajectories(List<SpatialPoint> templatePoints, PredefinedPathParam predefinedPathParam,
                                                             List<Point> pointsForLeader, EnemyType enemyType) {
        List<Trajectory> trajectories = new ArrayList<>();
        Enemy enemy = createEnemy(enemyType, 0, new Trajectory(0), 0, null, 0);
        double sh = enemy.getCircularRadius(enemyType.getId());
        double sw = enemy.getCircularRadius(enemyType.getId());
        double alpha = predefinedPathParam.getRot();
        tf = 1;
        List<List<Point>> points = generateSpatialFormationPoint(pointsForLeader, templatePoints, sh, sw, alpha, false);
        for (List<Point> list : points) {
            Trajectory trajectory = new BezierTrajectory(0, list);
            trajectories.add(trajectory);
        }
        return trajectories;
    }

    private PointD calculateNewEndPoint(double lastX, double lastY, PredefinedPathParam predefinedPathParam) {
            while ((-60 < lastX && lastX < 1000) && (-60 < lastY && lastY < 580)) {
                tf = tf + 0.01;
                if (predefinedPathParam.getTrajectoryType().equals(TrajectoryType.QUADBEZIER)) {
                    lastX = (1 - tf) * (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(0).getX() + 2 * tf * (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(1).getX() + tf * tf * predefinedPathParam.getTrajectoryPoints().get(2).getX();
                    lastY = (1 - tf) * (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(0).getY() + 2 * tf * (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(1).getY() + tf * tf * predefinedPathParam.getTrajectoryPoints().get(2).getY();
                }
                if (predefinedPathParam.getTrajectoryType().equals(TrajectoryType.CUBICBEZIER)) {
                    lastX = (1 - tf) * (1 - tf) * (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(0).getX() + 3 * tf * (1 - tf) * (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(1).getX()
                            + 3 * tf * tf * (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(2).getX() + tf * tf * tf * predefinedPathParam.getTrajectoryPoints().get(3).getX();

                    lastY = (1 - tf) * (1 - tf) * (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(0).getY() + 3 * tf * (1 - tf) * (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(1).getY()
                            + 3 * tf * tf * (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(2).getY() + tf * tf * tf * predefinedPathParam.getTrajectoryPoints().get(3).getY();
                }
                if (predefinedPathParam.getTrajectoryType().equals(TrajectoryType.LINEAR)) {
                    lastX = (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(0).getX() + tf * predefinedPathParam.getTrajectoryPoints().get(1).getX();
                    lastY = (1 - tf) * predefinedPathParam.getTrajectoryPoints().get(0).getY() + tf * predefinedPathParam.getTrajectoryPoints().get(1).getY();
                }
            }
        PointD pointD = new PointD(lastX, lastY);
        return pointD;
    }

    public List<List<Point>> generateSpatialFormationPoint(List<Point> pointsForLeader, List<SpatialPoint> templatePointsC1, double sh, double sw, double alpha, boolean isQ) {
        List<List<Point>> points = new ArrayList<>();
        for (int i = 0; i < templatePointsC1.size(); i++) {
            double xi = templatePointsC1.get(i).getDx();
            double yi = templatePointsC1.get(i).getDy();
            xi = xi * sw * 1.5;
            yi = yi * sh * 1.5;
            double offsetX;
            double offsetY;
            offsetX = Math.cos(alpha) * xi - Math.sin(alpha) * yi;
            offsetY = Math.sin(alpha) * xi + Math.cos(alpha) * yi;
            List<Point> pointsForSecondary = new ArrayList<>();
            for (int j = 0; j < pointsForLeader.size(); j++) {
                pointsForSecondary.add(new Point(pointsForLeader.get(j).getX() + offsetX, pointsForLeader.get(j).getY() + offsetY,
                        pointsForLeader.get(j).getTime() + 200));
            }
            points.add(pointsForSecondary);
        }
        return points;
    }

    public List<Enemy> spawnHybridFormation(SpawnConfig spawnConfig) {
        List<Enemy> enemies = new ArrayList<>();
        EnemyType minorEnemyType = spawnConfig.getHybridMinorEnemies().get(RNG.nextInt(spawnConfig.getHybridMinorEnemies().size()));
        int countMinorEnemies = RNG.nextInt(spawnConfig.getMinNumOfMinorEnemies(), spawnConfig.getMaxNumOfMinorEnemies());
        EnemyType majorEnemyType = spawnConfig.getHybridMajorEnemies().get(RNG.nextInt(spawnConfig.getHybridMajorEnemies().size()));
        double minorSpeedFPS = spawnConfig.getMinorEnemiesSpeedFPSMul();
        PredefinedPathParam param = spawnConfig.getPredefinedPaths().get(RNG.nextInt(spawnConfig.getPredefinedPaths().size() - 1));
        List<PredefinedPoint> trajectoryPoints = param.getTrajectoryPoints();
        BezierTrajectory spawnTrajectory = generateBezierTrajectoryByType(trajectoryPoints, param.getTrajectoryType(), false, 0, false, 0);
        spawnTrajectory = (BezierTrajectory) updateStartAndReenterPoint(spawnTrajectory, 200);
        Enemy major = addEnemyWithTrajectory(majorEnemyType, spawnTrajectory);
        enemies.add(major);
        double majorSpeed = major.getSpeed();
        int partAngle = 360 / countMinorEnemies;
        int sumAngles = 0;
        for (int i = 0; i < countMinorEnemies; i++) {
            List<Point> points = spawnTrajectory.getPoints();
            List<Point> newPoints = new ArrayList<>();
            for (Point point : points) {
                newPoints.add(new Point(point.getX(), point.getY(), point.getTime()));
            }
            BezierTrajectory newTrajectory = new BezierTrajectory(0, newPoints, sumAngles);
            sumAngles = sumAngles + partAngle;
            Enemy minor = addEnemyWithTrajectory(minorEnemyType, newTrajectory);
            minor.addToSwarm(SwarmType.Hybrid.getTypeId(), i + 1);
            minor.setParentEnemyId(major.getId());
            minor.setParentEnemyTypeId(majorEnemyType.getId());
            minor.setSpeed(majorSpeed * minorSpeedFPS);
            enemies.add(minor);
        }
        return enemies;
    }

    public void clearSpiralWaveParams() {
        spiralWaveParams.setFirstSpawn(true);
    }


    public List<Enemy> spawnSpiralWave(SpawnConfig spawnConfig, InitialWaveType typeWave) {
        if (spiralWaveParams.isFirstSpawn()) {
            spiralWaveParams.setTypeSpiralWave(typeWave);
            spiralWaveParams.setEnemyType(spawnConfig.getInitialWaveEnemies().get(RNG.nextInt(spawnConfig.getInitialWaveEnemies().size() - 1)));
            spiralWaveParams.setPathTime(10000 + RNG.nextInt(10000));
            spiralWaveParams.setCountWaves(3);
        }
        if (spiralWaveParams.getCountWaves() > 0) {
            return generateSpiralEnemies(spawnConfig);
        } else {
            spiralWaveParams.setFirstSpawn(true);
            return null;
        }
    }

    private List<Enemy> generateSpiralEnemies(SpawnConfig spawnConfig) {
        getLogger().debug(spiralWaveParams);
        List<Enemy> enemies = new ArrayList<>();
        List<PredefinedPathParam> params = spawnConfig.getInitialWavesPaths().get(spiralWaveParams.getTypeSpiralWave());
        double speedFPS = spawnConfig.getEnemiesInitialWaveSpeedFPSMul();
        for (int i = 0; i < params.size(); i++) {
            Trajectory trajectory = generateBezierTrajectoryByType(params.get(i).getTrajectoryPoints(), params.get(i).getTrajectoryType(),
                    true, spiralWaveParams.getPathTime(), true, System.currentTimeMillis());
            Enemy enemy = addEnemyWithTrajectory(spiralWaveParams.getEnemyType(), trajectory);
            if (i == 0 && spiralWaveParams.isFirstSpawn()) {
                spiralWaveParams.setEnemySpeed(enemy.getSpeed());
                spiralWaveParams.setFirstSpawn(false);
            }
            enemy.setSpeed(spiralWaveParams.getEnemySpeed() * speedFPS);
            enemies.add(enemy);
        }
        spiralWaveParams.setCountWaves(spiralWaveParams.getCountWaves() - 1);
        if (spiralWaveParams.getCountWaves() == 0) {
            spiralWaveParams.setFirstSpawn(true);
        }
        return enemies;
    }

    public List<Enemy> generateCrossEnemies(SpawnConfig spawnConfig) {
        List<PredefinedPathParam> crossPaths1 = spawnConfig.getInitialWavesPaths().get(InitialWaveType.CrossPaths1);
        List<PredefinedPathParam> crossPaths2 = spawnConfig.getInitialWavesPaths().get(InitialWaveType.CrossPaths2);
        EnemyType enemyType = spawnConfig.getInitialWaveEnemies().get(RNG.nextInt(spawnConfig.getInitialWaveEnemies().size()));
        List<PredefinedPoint> predefinedPoints1 = new ArrayList<>();
        for (PredefinedPathParam predefinedPath : crossPaths1) {
            predefinedPoints1.addAll(predefinedPath.getTrajectoryPoints());
        }
        List<PredefinedPoint> predefinedPoints2 = new ArrayList<>();
        for (PredefinedPathParam predefinedPath : crossPaths2) {
            predefinedPoints2.addAll(predefinedPath.getTrajectoryPoints());
        }
        List<Enemy> enemies = new ArrayList<>();
        long enemyStartTime = System.currentTimeMillis();
        int countEnemies = 8;
        long pathTime = 12000;
        for (int i = 0; i < countEnemies; i++) {
            List<Point> points1 = new ArrayList<>();
            for (int k = 0; k < predefinedPoints1.size(); k++) {
                points1.add(new Point(predefinedPoints1.get(k).getX(), predefinedPoints1.get(k).getY(),
                        enemyStartTime + (pathTime / predefinedPoints1.size() * k) + i * 1000));
            }
            enemies.add(addEnemyWithTrajectory(enemyType, new BezierTrajectory(0, points1)));
            List<Point> points2 = new ArrayList<>();
            for (int k = 0; k < predefinedPoints1.size(); k++) {
                points2.add(new Point(predefinedPoints2.get(k).getX(), predefinedPoints2.get(k).getY(),
                        enemyStartTime + (pathTime / predefinedPoints2.size() * k) + i * 1000));
            }
            enemies.add(addEnemyWithTrajectory(enemyType, new BezierTrajectory(0, points2)));
        }
        return enemies;
    }

    public List<Enemy> getCrossWaveEnemies(SpawnConfig spawnConfig) {
        List<Enemy> enemies = new ArrayList<>();
        List<Point> resultCross1 = new ArrayList<>();
        List<Point> resultCross2 = new ArrayList<>();
        List<PredefinedPathParam> crossPaths1 = spawnConfig.getInitialWavesPaths().get(InitialWaveType.CrossPaths1);
        List<PredefinedPathParam> crossPaths2 = spawnConfig.getInitialWavesPaths().get(InitialWaveType.CrossPaths2);
        EnemyType enemyType = spawnConfig.getTemporalEnemies().get(RNG.nextInt(spawnConfig.getTemporalEnemies().size()));
        long spawnTime = System.currentTimeMillis();
        for (int i = 0; i < crossPaths1.size(); i++) {
            List<PredefinedPoint> predefinedPoints1 = crossPaths1.get(i).getTrajectoryPoints();
            generateCrossWavePointsPointsOfTrajectories(resultCross1, predefinedPoints1, crossPaths1.get(i).getTrajectoryType(), spawnTime, i==0, i);

            List<PredefinedPoint> predefinedPoints2 = crossPaths2.get(i).getTrajectoryPoints();
            generateCrossWavePointsPointsOfTrajectories(resultCross2, predefinedPoints2, crossPaths2.get(i).getTrajectoryType(), spawnTime, i==0, i);
            spawnTime = resultCross1.get(resultCross1.size() - 1).getTime();
        }
        enemies.add(addEnemyWithTrajectory(enemyType, new BezierTrajectory(0, resultCross1)));
        enemies.add(addEnemyWithTrajectory(enemyType, new BezierTrajectory(0, resultCross2)));

        long offset = 0;
        for (int i = 0; i < 7; i++) {
            List<Point> tmpPoint1 = new ArrayList<>();
            List<Point> tmpPoint2 = new ArrayList<>();
            for (int j = 0; j < resultCross1.size(); j++) {
                tmpPoint1.add(new Point(resultCross1.get(j).getX(), resultCross1.get(j).getY(), resultCross1.get(j).getTime() + offset));
                tmpPoint2.add(new Point(resultCross2.get(j).getX(), resultCross2.get(j).getY(), resultCross2.get(j).getTime() + offset));
            }
            enemies.add(addEnemyWithTrajectory(enemyType, new BezierTrajectory(0, tmpPoint1)));
            enemies.add(addEnemyWithTrajectory(enemyType, new BezierTrajectory(0, tmpPoint2)));
            offset = offset + 1000;
        }
        return enemies;
    }

    public List<Enemy> spawnTemporalFormation(SpawnConfig spawnConfig) {
        List<Enemy> enemies = new ArrayList<>();
        long spawnTime = System.currentTimeMillis();
        long timePath = 10000 + RNG.nextInt(10000);
        int rndTemporalTemplate = RNG.nextInt(spawnConfig.getTemporalFormationTemplates().size());
        List<Long> temporalTemplates = spawnConfig.getTemporalFormationTemplates().get(rndTemporalTemplate);
        EnemyType enemyType;
        enemyType = spawnConfig.getTemporalEnemies().get(RNG.nextInt(spawnConfig.getTemporalEnemies().size() - 1));
        List<PredefinedPathParam> predefinedPathParams = spawnConfig.getPredefinedPaths();
        int rngNumberTraj = RNG.nextInt(predefinedPathParams.size());
        PredefinedPathParam path = spawnConfig.getPredefinedPaths().get(rngNumberTraj);
        double r0 = RNG.rand();
        if (r0 < 0.5) {
            double r1 = RNG.rand();
            if (r1 < 0.5) {
                double speed = generateSpeed(enemyType.getSkin(1));
                generateTemporalEnemies(spawnConfig, spawnTime, timePath, path, enemies, temporalTemplates, enemyType, speed);
                List<PredefinedPoint> predefinedPoints = new ArrayList<>();
                double rotation = 3.14159;
                for (PredefinedPoint predefinedPoint : path.getTrajectoryPoints()) {
                    double xi = predefinedPoint.getX();
                    double yi = predefinedPoint.getY();
                    double newXi = (xi - 480) * Math.cos(rotation) - (yi - 270) * Math.sin(rotation) + 480;
                    double newYi = (xi - 480) * Math.sin(rotation) + (yi - 270) * Math.cos(rotation) + 270;
                    predefinedPoints.add(new PredefinedPoint(newXi, newYi, 0));
                }
                PredefinedPathParam rotatedParam = new PredefinedPathParam(0, predefinedPoints, path.getWeight(), path.getTrajectoryType(), path.getTrajectorySector(), path.getRot());
                generateTemporalEnemies(spawnConfig, spawnTime, timePath, rotatedParam, enemies, temporalTemplates, enemyType, speed);
                return enemies;
            } else {
                double speed = generateSpeed(enemyType.getSkin(1));
                generateTemporalEnemies(spawnConfig, spawnTime, timePath, path, enemies, temporalTemplates, enemyType, speed);
                int rndType = RNG.nextInt(MirrorType.X.getTypeId(), MirrorType.D.getTypeId());
                if (rndType == MirrorType.X.getTypeId() || rndType == MirrorType.Y.getTypeId()) {
                    double rotation = path.getRot();
                    if (((Math.abs(Math.toDegrees(rotation)) < 225) && (Math.abs(Math.toDegrees(rotation)) > 135)) ||
                            ((Math.abs(Math.toDegrees(rotation)) < 45) && (Math.abs(Math.toDegrees(rotation)) < 315))) {
                        PredefinedPathParam rotatedParam = getMirrorPathAboutXOrYAxis(path, true, 960, 540);
                        generateTemporalEnemies(spawnConfig, spawnTime, timePath, rotatedParam, enemies, temporalTemplates, enemyType, speed);
                    } else {
                        PredefinedPathParam rotatedParam = getMirrorPathAboutXOrYAxis(path, false, 960, 540);
                        generateTemporalEnemies(spawnConfig, spawnTime, timePath, rotatedParam, enemies, temporalTemplates, enemyType, speed);
                    }
                    return enemies;
                } else if (rndType == MirrorType.D.getTypeId()) {
                    double coef = 0.5625;
                    double mirrorX = RNG.nextInt(0, 960);
                    double mirrorY = mirrorX * coef;
                    PredefinedPathParam rotatedParam = getMirrorPathAboutXOrYAxis(path, false, mirrorX, mirrorY);
                    PredefinedPathParam newRotatedParam = getMirrorPathAboutXOrYAxis(rotatedParam, true, mirrorX, mirrorY);
                    generateTemporalEnemies(spawnConfig, spawnTime, timePath, newRotatedParam, enemies, temporalTemplates, enemyType, speed);
                } else if (rndType == MirrorType.DOff.getTypeId()) {
                    double coef = 0.5625;
                    double mirrorX = RNG.nextInt(0, 960);
                    double mirrorY = 540 - mirrorX*coef;
                    PredefinedPathParam rotatedParam = getMirrorPathAboutXOrYAxis(path, false, mirrorX, mirrorY);
                    PredefinedPathParam newRotatedParam = getMirrorPathAboutXOrYAxis(rotatedParam, true, mirrorX, mirrorY);
                    generateTemporalEnemies(spawnConfig, spawnTime, timePath, newRotatedParam, enemies, temporalTemplates, enemyType, speed);
                }
                return enemies;
            }
        } else {
            double speed = generateSpeed(enemyType.getSkin(1));
            generateTemporalEnemies(spawnConfig, spawnTime, timePath, path, enemies, temporalTemplates, enemyType, speed);
            return enemies;
        }
    }

    public PredefinedPathParam getMirrorPathAboutXOrYAxis(PredefinedPathParam path, boolean aboutXAxis, double xAxis, double yAxis) {
        List<PredefinedPoint> predefinedPoints = new ArrayList<>();
        for (PredefinedPoint predefinedPoint : path.getTrajectoryPoints()) {
            double xi = predefinedPoint.getX();
            double yi = predefinedPoint.getY();
            if (aboutXAxis) {
                if (yi < 0) {
                    yi = 540 + Math.abs(yi);
                } else {
                    yi = 540 - yi;
                }
            } else {
                if (xi < 0) {
                    xi = 960 + Math.abs(xi);
                } else {
                    xi = 960 - xi;
                }
            }
            predefinedPoints.add(new PredefinedPoint(xi, yi, 0));
        }
        return new PredefinedPathParam(0, predefinedPoints, path.getWeight(), path.getTrajectoryType(), path.getTrajectorySector(), path.getRot());
    }

    public void generateTemporalEnemies(SpawnConfig spawnConfig, long spawnTime, long timePath, PredefinedPathParam param, List<Enemy> resultEnemies, List<Long> temporalTemplates, EnemyType enemyType, double speeed) {
        Trajectory firstTrajectory = generateTrajectoryWithTimeDelay(spawnTime, timePath, spawnConfig, param, 0);
        Enemy firstEnemy = addEnemyWithTrajectory(enemyType, firstTrajectory);
        firstEnemy.setSpeed(speeed);
        resultEnemies.add(firstEnemy);
        double D = spawnConfig.getTemporalSpacingD();
        double alpha = (firstEnemy.getCircularRadius(firstEnemy.getEnemyType().getId()) - 10) * D / firstEnemy.getSpeed() / temporalTemplates.size();
        long sumOffsets = 0;
        for (int i = 1; i < temporalTemplates.size(); i++) {
            sumOffsets = sumOffsets + (long) ((temporalTemplates.get(i)) * alpha);
            Trajectory trajectory = generateTrajectoryWithTimeDelay(spawnTime + sumOffsets, timePath, spawnConfig, param, (float) firstEnemy.getSpeed());
            Enemy newEnemy = addItem(enemyType, 1, trajectory, (float) speeed, null, -1);
            resultEnemies.add(newEnemy);
        }
    }

    public List<List<Enemy>> generateSpecialPatternEnemies(SpawnConfig spawnConfig) {
        EnemyType enemyType = spawnConfig.getInitialWaveEnemies().get(RNG.nextInt(spawnConfig.getInitialWaveEnemies().size() - 1));
        List<Enemy> enemiesM = new ArrayList<>();
        List<Enemy> enemiesQ = new ArrayList<>();
        List<List<Enemy>> enemies = new ArrayList<>();
        List<List<SpatialPoint>> offsets = spawnConfig.getInitialWavesSpecialPatternOffsets();
        List<SpatialPoint> offsetsM = offsets.get(0);
        List<SpatialPoint> offsetsQ = offsets.get(1);
        List<PredefinedPathParam> path = spawnConfig.getInitialWavesPaths().get(InitialWaveType.SpecialPattern);
        List<Point> pointsForLeader = generateSpecialPatternTraj(path.get(0).getTrajectoryPoints(), path.get(0).getTrajectoryType(), 1, false);
        List<Point> pointsForLeaderQ = generateSpecialPatternTraj(path.get(0).getTrajectoryPoints(), path.get(0).getTrajectoryType(), 1, true);
        List<Trajectory> trajectories = getSpatialSpecialPatternTrajectories(offsetsM, path.get(0), pointsForLeader, false);
        List<Trajectory> trajectoriesQ = getSpatialSpecialPatternTrajectories(offsetsQ, path.get(0), pointsForLeaderQ, true);
        for (Trajectory trajectory : trajectories) {
            Enemy enemy = addEnemyWithTrajectory(enemyType, trajectory);
            enemiesM.add(enemy);
        }
        enemies.add(enemiesM);
        for (Trajectory trajectory : trajectoriesQ) {
            Enemy enemy = addEnemyWithTrajectory(enemyType, trajectory);
            enemiesQ.add(enemy);
        }
        enemies.add(enemiesQ);
        return enemies;
    }

    private List<Trajectory> getSpatialSpecialPatternTrajectories(List<SpatialPoint> templatePoints, PredefinedPathParam predefinedPathParam,
                                                                  List<Point> pointsForLeader, boolean isQ) {
        List<Trajectory> trajectories = new ArrayList<>();
        double sh = 80;
        double sw = 80;
        double alpha = predefinedPathParam.getRot();
        List<List<Point>> points = generateSpatilaPoint(pointsForLeader, templatePoints, sh, sw, alpha, isQ);
        double tf = 1;
        for (List<Point> list : points) {
            double lastX = list.get(list.size() - 1).getX();
            if (lastX < 960 && lastX > 0) {
                tf = tf + 0.01;
                double Xt1 = (1 - tf) * pointsForLeader.get(0).getX() + tf * pointsForLeader.get(pointsForLeader.size() - 1).getX();
                double Yt1 = (1 - tf) * pointsForLeader.get(0).getY() + tf * pointsForLeader.get(pointsForLeader.size() - 1).getY();
                pointsForLeader.get(list.size() - 1).setX(Xt1);
                pointsForLeader.get(list.size() - 1).setY(Yt1);
                List<PredefinedPoint> newParams = new ArrayList<>();
                List<PredefinedPoint> params = predefinedPathParam.getTrajectoryPoints();
                for (int i = 0; i < params.size(); i++) {
                    if (i == params.size() - 1) {
                        newParams.add(new PredefinedPoint(Xt1, Yt1, 0));
                    } else {
                        newParams.add(new PredefinedPoint(params.get(i).getX(), params.get(i).getY(), 0));
                    }
                }
                List<Point> newPoints = generateSpecialPatternTraj(newParams, predefinedPathParam.getTrajectoryType(), 1, false);
                points = generateSpatilaPoint(newPoints, templatePoints, sh, sw, alpha, isQ);
            }
        }
        for (List<Point> list : points) {
            Trajectory trajectory = new BezierTrajectory(0, list);
            trajectories.add(trajectory);
        }
        return trajectories;
    }

    public List<List<Point>> generateSpatilaPoint(List<Point> pointsForLeader, List<SpatialPoint> templatePointsC1, double sh, double sw, double alpha, boolean isQ) {
        List<List<Point>> points = new ArrayList<>();
        long timeQ = 0;
        if (isQ) {
            timeQ = 3000;
        }
        for (int i = 0; i < templatePointsC1.size(); i++) {
            double xi = templatePointsC1.get(i).getDx();
            double yi = templatePointsC1.get(i).getDy();
            xi = xi * sh;
            yi = yi * sw;
            xi = Math.cos(alpha) * xi - Math.sin(alpha) * yi;
            yi = Math.sin(alpha) * xi + Math.cos(alpha) * yi;
            List<Point> pointsForSecondary = new ArrayList<>();
            long current = System.currentTimeMillis() + 1000;
            for (int j = 0; j < pointsForLeader.size(); j++) {
                pointsForSecondary.add(new Point(pointsForLeader.get(j).getX() + xi, pointsForLeader.get(j).getY() + yi,
                        pointsForLeader.get(j).getTime() + 1000 + timeQ));
            }
            points.add(pointsForSecondary);
        }
        return points;
    }

    public List<Enemy> spawnRandomWave(SpawnConfig spawnConfig) {
        EnemyType enemyType = spawnConfig.getInitialWaveEnemies().get(RNG.nextInt(spawnConfig.getInitialWaveEnemies().size()));
        int numMax = spawnConfig.getMaxNumOfEnemiesRandomWave();
        int numMin = spawnConfig.getMinNumOfEnemiesRandomWave();
        int rndNum = RNG.nextInt(numMax, numMin + 1);
        List<PredefinedPathParam> params = spawnConfig.getPredefinedPaths();
        List<Enemy> enemies = new ArrayList<>();
        List<PredefinedPathParam> resultParams = new ArrayList<>();
        for (int i = 0; i < rndNum; i++) {
            PredefinedPathParam p = params.get(RNG.nextInt(params.size()));
            while (resultParams.contains(p)) {
                p = params.get(RNG.nextInt(params.size()));
            }
            resultParams.add(p);
            Trajectory trajectory = generateBezierTrajectoryByType(p.getTrajectoryPoints(), p.getTrajectoryType(), false, 0, false, 0);
            enemies.add(addEnemyWithTrajectory(enemyType, trajectory));
        }
        return enemies;
    }

    public BezierTrajectory generateTrajectoryWithTimeDelay(Long spawnTime, Long pathTime, SpawnConfig spawnConfig, PredefinedPathParam path, float speed) {
        PredefinedPathParam predefinedPathParam = path;
        List<PredefinedPoint> trajectoryPoints = predefinedPathParam.getTrajectoryPoints();
        long newPath = pathTime;
        if (speed != 0) {
            newPath = (long) (pathTime / speed);
        }
        if (predefinedPathParam.getTrajectoryType().equals(TrajectoryType.QUADBEZIER)) {
            long middleTime = spawnTime + pathTime / 2;
            List<Point> points = new ArrayList<>();
            points.add(new Point(trajectoryPoints.get(0).getX(), trajectoryPoints.get(0).getY(), spawnTime));
            points.add(new Point(trajectoryPoints.get(1).getX(), trajectoryPoints.get(1).getY(), middleTime));
            points.add(new Point(trajectoryPoints.get(2).getX(), trajectoryPoints.get(2).getY(), spawnTime + pathTime));
            BezierTrajectory bezierTrajectory = new BezierTrajectory(0, points);
            return bezierTrajectory;
        }
        if (predefinedPathParam.getTrajectoryType().equals(TrajectoryType.CUBICBEZIER)) {
            List<Point> points = new ArrayList<>();
            long t = pathTime / 2;
            long timeFirstPoint = spawnTime + t;
            long timeSecondPoint = spawnTime + t + t;
            points.add(new Point(trajectoryPoints.get(0).getX(), trajectoryPoints.get(0).getY(), spawnTime));
            points.add(new Point(trajectoryPoints.get(1).getX(), trajectoryPoints.get(1).getY(), timeFirstPoint));
            points.add(new Point(trajectoryPoints.get(2).getX(), trajectoryPoints.get(2).getY(), timeSecondPoint));
            points.add(new Point(trajectoryPoints.get(3).getX(), trajectoryPoints.get(3).getY(), spawnTime + pathTime));
            BezierTrajectory bezierTrajectory = new BezierTrajectory(0, points);
            return bezierTrajectory;
        } else {
            List<Point> points = new ArrayList<>();
            points.add(new Point(trajectoryPoints.get(0).getX(), trajectoryPoints.get(0).getY(), spawnTime));
            points.add(new Point(trajectoryPoints.get(1).getX(), trajectoryPoints.get(1).getY(), spawnTime + pathTime));
            BezierTrajectory bezierTrajectory = new BezierTrajectory(0, points);
            return bezierTrajectory;
        }
    }

    public List<Enemy> spawnClusterFormation(SpawnConfig spawnConfig) {
        List<Enemy> enemies = new ArrayList<>();
        List<EnemyType> clusterEnemies = spawnConfig.getClusterEnemies();
        EnemyType enemyType = clusterEnemies.get(RNG.nextInt(clusterEnemies.size()));
        List<Trajectory> trajectories = generateTrajectoriesForClusterFormation(spawnConfig);
        for (Trajectory trajectory : trajectories) {
            Enemy enemy = addEnemyWithTrajectory(enemyType, trajectory);
            enemies.add(enemy);
        }
        return enemies;
    }

    private List<Trajectory> generateTrajectoriesForClusterFormation(SpawnConfig spawnConfig) {
        int N = RNG.nextInt(15, 20);
        int quadrantNumber = RNG.nextInt(1, 4);
        List<PredefinedPathParam> clusterPaths = new ArrayList<>();
        List<PredefinedPathParam> predefinedPathParams = spawnConfig.getPredefinedPaths();
        List<PredefinedPathParam> quadrantPaths = new ArrayList<>();
        for (PredefinedPathParam predefinedPathParam : predefinedPathParams) {
            if (predefinedPathParam.getTrajectorySector().getTypeId() == quadrantNumber) {
                quadrantPaths.add(predefinedPathParam);
            }
        }
        clusterPaths.add(quadrantPaths.get(RNG.nextInt(quadrantPaths.size())));
        int n = 1;
        while (n < N) {
            PredefinedPathParam predefinedPathParam = quadrantPaths.get(RNG.nextInt(quadrantPaths.size()));
            if (predefinedPathParam.getTrajectoryPoints().get(0).getX() - quadrantPaths.get(0).getTrajectoryPoints().get(0).getX() < 5 &&
                    predefinedPathParam.getTrajectoryPoints().get(0).getY() - quadrantPaths.get(0).getTrajectoryPoints().get(0).getY() < 5 &&
                    predefinedPathParam.getTrajectoryPoints().get(0).getX() != quadrantPaths.get(0).getTrajectoryPoints().get(0).getX() &&
                    predefinedPathParam.getTrajectoryPoints().get(0).getX() != quadrantPaths.get(0).getTrajectoryPoints().get(0).getX()) {
                clusterPaths.add(predefinedPathParam);
                n++;
            }
        }
        List<Trajectory> trajectories = new ArrayList<>();
        for (PredefinedPathParam path : clusterPaths) {
            List<PredefinedPoint> predefinedPoints = path.getTrajectoryPoints();
            if (path.getTrajectoryType().equals(TrajectoryType.LINEAR)) {
                trajectories.add(generateLinearBezierTrajectory(predefinedPoints, false, 0, false, 0));
            } else if (path.getTrajectoryType().equals(TrajectoryType.CUBICBEZIER)) {
                trajectories.add(generateCubicBezierTrajectory(predefinedPoints, false, 0, false, 0));
            } else if (path.getTrajectoryType().equals(TrajectoryType.QUADBEZIER)) {
                trajectories.add(generateQuadBezierTrajectory(predefinedPoints, false, 0, false, 0));
            }
        }
        return trajectories;
    }

    private BezierTrajectory generateBezierTrajectoryByType(List<PredefinedPoint> trajectoryPoints, TrajectoryType trajectoryType, boolean isTimePathPredefined,
                                                            long timePath, boolean isStartTimePredefined, long predefinedStartTime) {
        if (trajectoryType.equals(TrajectoryType.CUBICBEZIER)) {
            return generateCubicBezierTrajectory(trajectoryPoints, isTimePathPredefined, timePath, isStartTimePredefined, predefinedStartTime);
        } else if (trajectoryType.equals(TrajectoryType.QUADBEZIER)) {
            return generateQuadBezierTrajectory(trajectoryPoints, isTimePathPredefined, timePath, isStartTimePredefined, predefinedStartTime);
        } else {
            return generateLinearBezierTrajectory(trajectoryPoints, isTimePathPredefined, timePath, isStartTimePredefined, predefinedStartTime);
        }
    }

    public BezierTrajectory generateQuadBezierTrajectory(List<PredefinedPoint> trajectoryPoints, boolean isTimePathPredefined, long time, boolean isStartTimePredefined, long predefinedStartTime) {
        List<Point> points = new ArrayList<>();
        long startTime = System.currentTimeMillis() + 1000;
        long timePath = 10000 + RNG.nextInt(10000);
        if (isStartTimePredefined) {
            startTime = predefinedStartTime;
        }
        if (isTimePathPredefined) {
            timePath = time;
        }
        long middleTime = startTime + timePath / 2;
        points.add(new Point(trajectoryPoints.get(0).getX(), trajectoryPoints.get(0).getY(), startTime));
        points.add(new Point(trajectoryPoints.get(1).getX(), trajectoryPoints.get(1).getY(), middleTime));
        points.add(new Point(trajectoryPoints.get(2).getX(), trajectoryPoints.get(2).getY(), startTime + timePath));
        BezierTrajectory bezierTrajectory = new BezierTrajectory(0, points);
        return bezierTrajectory;
    }

    public BezierTrajectory generateCubicBezierTrajectory(List<PredefinedPoint> trajectoryPoints, boolean isTimePathPredefined, long time, boolean isStartTimePredefined, long predefinedStartTime) {
        List<Point> points = new ArrayList<>();
        long startTime = System.currentTimeMillis() + 1000;
        long timePath = 10000 + RNG.nextInt(10000);
        if (isStartTimePredefined) {
            startTime = predefinedStartTime;
        }
        if (isTimePathPredefined) {
            timePath = time;
        }
        long timeForOnePart = timePath / 3;
        long timeSecondPoint = startTime + timeForOnePart;
        long timeThirdPoint = startTime + timeForOnePart + timeForOnePart;
        points.add(new Point(trajectoryPoints.get(0).getX(), trajectoryPoints.get(0).getY(), startTime));
        points.add(new Point(trajectoryPoints.get(1).getX(), trajectoryPoints.get(1).getY(), timeSecondPoint));
        points.add(new Point(trajectoryPoints.get(2).getX(), trajectoryPoints.get(2).getY(), timeThirdPoint));
        points.add(new Point(trajectoryPoints.get(3).getX(), trajectoryPoints.get(3).getY(), startTime + timePath));
        BezierTrajectory bezierTrajectory = new BezierTrajectory(0, points);
        return bezierTrajectory;
    }

    public BezierTrajectory generateLinearBezierTrajectory(List<PredefinedPoint> trajectoryPoints, boolean isTimePathPredefined, long time, boolean isStartTimePredefined, long predefinedStartTime) {
        List<Point> points = new ArrayList<>();
        long startTime = System.currentTimeMillis() + 1000;
        long timePath = 10000 + RNG.nextInt(10000);
        if (isStartTimePredefined) {
            startTime = predefinedStartTime;
        }
        if (isTimePathPredefined) {
            timePath = time;
        }
        points.add(new Point(trajectoryPoints.get(0).getX(), trajectoryPoints.get(0).getY(), startTime));
        points.add(new Point(trajectoryPoints.get(1).getX(), trajectoryPoints.get(1).getY(), startTime + timePath));
        BezierTrajectory bezierTrajectory = new BezierTrajectory(0, points);
        return bezierTrajectory;
    }

    public Enemy checkPointsGenerator(SpawnConfig spawnConfig) {
        List<PredefinedPathParam> params = spawnConfig.getPredefinedPaths();
        PredefinedPathParam param = params.get(33);
        List<Point> points = generatePointsOfBezierTrajectory(param.getTrajectoryPoints(), param.getTrajectoryType(), 1, false, false);
        BezierTrajectory trajectory = new BezierTrajectory(0, points);
        Enemy enemy = addEnemyWithTrajectory(S1, trajectory);
        return enemy;
    }

    public List<Point> generateSpecialPatternTraj(List<PredefinedPoint> predefinedPoints, TrajectoryType trajectoryType, double tf, boolean isSpecialPattern) {
        List<Point> trajectoryPoints = new ArrayList<>();
        long offset = 0;
        if (isSpecialPattern) {
            offset = 3000;
        }
        long startTime = System.currentTimeMillis() + 1000;
        if (trajectoryType.equals(TrajectoryType.LINEAR)) {
            double dt = 0.1;
            for (double t = 0; t < 1; t += dt) {
                double newt = t * tf;
                double Xt1 = (1 - newt) * predefinedPoints.get(0).getX() + newt * predefinedPoints.get(1).getX();
                double Yt1 = (1 - newt) * predefinedPoints.get(0).getY() + newt * predefinedPoints.get(1).getY();
                trajectoryPoints.add(new Point(Xt1, Yt1, startTime + 1000 + offset));
                startTime = startTime + 1000;
            }
        }
        return trajectoryPoints;
    }

    private void generateCrossWavePointsPointsOfTrajectories(List<Point> resultPoint, List<PredefinedPoint> predefinedPoints, TrajectoryType trajectoryType,
                                                             long time, boolean isFirstTraj, int trajNumber) {
        long offset = 50;
        long startTime;
        if (isFirstTraj) {
            startTime = System.currentTimeMillis() + 2000;
        } else {
            startTime = time;
        }
        if (trajNumber == 6) {
            offset = 300;
        }
        if (trajectoryType.equals(TrajectoryType.QUADBEZIER)) {
            double dt = 0.01;
            for (double t = 0; t < 1; t += dt) {
                double newt = t;
                startTime = startTime + offset;
                double Xt1 = ((1 - newt) * (1 - newt) * predefinedPoints.get(0).getX() + 2 * newt * (1 - newt) * predefinedPoints.get(1).getX()
                        + newt * newt * predefinedPoints.get(2).getX());
                double Yt1 = ((1 - newt) * (1 - newt) * predefinedPoints.get(0).getY() + 2 * newt * (1 - newt) * predefinedPoints.get(1).getY()
                        + newt * newt * predefinedPoints.get(2).getY());
                resultPoint.add(new Point(Xt1, Yt1, startTime));
            }
        }
        if (trajectoryType.equals(TrajectoryType.CUBICBEZIER)) {
            double dt = 0.05;
            for (double t = 0; t < 1; t += dt) {
                double newt = t;
                startTime = startTime + offset;
                double Xt1 = ((1 - newt) * (1 - newt) * (1 - newt) * predefinedPoints.get(0).getX() + 3 * newt * (1 - newt) * (1 - newt) * predefinedPoints.get(1).getX()
                        + 3 * newt * newt * (1 - newt) * predefinedPoints.get(2).getX() + newt * newt * newt * predefinedPoints.get(3).getX());
                double Yt1 = ((1 - newt) * (1 - newt) * (1 - newt) * predefinedPoints.get(0).getY() + 3 * newt * (1 - newt) * (1 - newt) * predefinedPoints.get(1).getY()
                        + 3 * newt * newt * (1 - newt) * predefinedPoints.get(2).getY() + newt * newt * newt * predefinedPoints.get(3).getY());
                resultPoint.add(new Point(Xt1, Yt1, startTime));
            }
        }
        if (trajectoryType.equals(TrajectoryType.LINEAR)) {
            double dt = 0.05;
            for (double t = 0; t < 1; t += dt) {
                double newt = t;
                startTime = startTime + offset;
                double Xt1 = (1 - newt) * predefinedPoints.get(0).getX() + newt * predefinedPoints.get(1).getX();
                double Yt1 = (1 - newt) * predefinedPoints.get(0).getY() + newt * predefinedPoints.get(1).getY();
                resultPoint.add(new Point(Xt1, Yt1, startTime));
            }
        }
    }

    private void generatePointsOfTrajectories(List<Point> resultPoint, List<PredefinedPoint> predefinedPoints, TrajectoryType trajectoryType,
                                              long time, boolean isFirstTraj, int trajNumber) {
        long offset = 300;
        long startTime;
        if (isFirstTraj) {
            startTime = System.currentTimeMillis() + 4000;
        } else {
            startTime = time;
        }
        if (trajNumber > 0) {
            offset = 200;
        }
        if (trajNumber == 6) {
            offset = 700;
        }
        offset = (long) (offset * 1.2);
        if (trajectoryType.equals(TrajectoryType.QUADBEZIER)) {
            double dt = 0.02;
            for (double t = 0; t < 1; t += dt) {
                double newt = t;
                double Xt1 = ((1 - newt) * (1 - newt) * predefinedPoints.get(0).getX() + 2 * newt * (1 - newt) * predefinedPoints.get(1).getX()
                        + newt * newt * predefinedPoints.get(2).getX());
                double Yt1 = ((1 - newt) * (1 - newt) * predefinedPoints.get(0).getY() + 2 * newt * (1 - newt) * predefinedPoints.get(1).getY()
                        + newt * newt * predefinedPoints.get(2).getY());
                resultPoint.add(new Point(Xt1, Yt1, startTime));
                startTime = startTime + offset;
            }
        }
        if (trajectoryType.equals(TrajectoryType.CUBICBEZIER)) {
            double dt = 0.02;
            for (double t = 0; t < 1; t += dt) {
                double newt = t;
                double Xt1 = ((1 - newt) * (1 - newt) * (1 - newt) * predefinedPoints.get(0).getX() + 3 * newt * (1 - newt) * (1 - newt) * predefinedPoints.get(1).getX()
                        + 3 * newt * newt * (1 - newt) * predefinedPoints.get(2).getX() + newt * newt * newt * predefinedPoints.get(3).getX());
                double Yt1 = ((1 - newt) * (1 - newt) * (1 - newt) * predefinedPoints.get(0).getY() + 3 * newt * (1 - newt) * (1 - newt) * predefinedPoints.get(1).getY()
                        + 3 * newt * newt * (1 - newt) * predefinedPoints.get(2).getY() + newt * newt * newt * predefinedPoints.get(3).getY());
                resultPoint.add(new Point(Xt1, Yt1, startTime));
                startTime = startTime + offset;
            }
        }
        if (trajectoryType.equals(TrajectoryType.LINEAR)) {
            double dt = 0.02;
            for (double t = 0; t < 1; t += dt) {
                double newt = t;
                double Xt1 = (1 - newt) * predefinedPoints.get(0).getX() + newt * predefinedPoints.get(1).getX();
                double Yt1 = (1 - newt) * predefinedPoints.get(0).getY() + newt * predefinedPoints.get(1).getY();
                resultPoint.add(new Point(Xt1, Yt1, startTime));
                startTime = startTime + offset;
            }
        }
    }

    public List<Point> generatePointsOfBezierTrajectory(List<PredefinedPoint> predefinedPoints, TrajectoryType trajectoryType, double tf, boolean isSpecialPattern, boolean isNewEndPoint) {
        List<Point> trajectoryPoints = new ArrayList<>();

        long offset = 200;
        if (isSpecialPattern) {
            offset = offset + 200;
        }
        /*if (isNewEndPoint) {
            offset = 100;
        }*/
        long startTime = System.currentTimeMillis() + 1000;
        if (trajectoryType.equals(TrajectoryType.QUADBEZIER)) {
            double dt = 0.01;
            for (double t = 0; t < 1; t += dt) {
                double newt = t * tf;
                double Xt1 = ((1 - newt) * (1 - newt) * predefinedPoints.get(0).getX() + 2 * newt * (1 - newt) * predefinedPoints.get(1).getX()
                        + newt * newt * predefinedPoints.get(2).getX());
                double Yt1 = ((1 - newt) * (1 - newt) * predefinedPoints.get(0).getY() + 2 * newt * (1 - newt) * predefinedPoints.get(1).getY()
                        + newt * newt * predefinedPoints.get(2).getY());
                trajectoryPoints.add(new Point(Xt1, Yt1, startTime + offset));
                //startTime = startTime + 1000;
                startTime = startTime + offset;
            }
        }
        if (trajectoryType.equals(TrajectoryType.CUBICBEZIER)) {
            double dt = 0.01;
            for (double t = 0; t < 1; t += dt) {
                double newt = t * tf;
                double Xt1 = ((1 - newt) * (1 - newt) * (1 - newt) * predefinedPoints.get(0).getX() + 3 * newt * (1 - newt) * (1 - newt) * predefinedPoints.get(1).getX()
                        + 3 * newt * newt * (1 - newt) * predefinedPoints.get(2).getX() + newt * newt * newt * predefinedPoints.get(3).getX());
                double Yt1 = ((1 - newt) * (1 - newt) * (1 - newt) * predefinedPoints.get(0).getY() + 3 * newt * (1 - newt) * (1 - newt) * predefinedPoints.get(1).getY()
                        + 3 * newt * newt * (1 - newt) * predefinedPoints.get(2).getY() + newt * newt * newt * predefinedPoints.get(3).getY());
                trajectoryPoints.add(new Point(Xt1, Yt1, startTime + offset));
                //startTime = startTime + 1000;
                startTime = startTime + offset;
            }
        }
        if (trajectoryType.equals(TrajectoryType.LINEAR)) {
            double dt = 0.01;
            for (double t = 0; t < 1; t += dt) {
                double newt = t * tf;
                double Xt1 = (1 - newt) * predefinedPoints.get(0).getX() + newt * predefinedPoints.get(1).getX();
                double Yt1 = (1 - newt) * predefinedPoints.get(0).getY() + newt * predefinedPoints.get(1).getY();
                trajectoryPoints.add(new Point(Xt1, Yt1, startTime + offset));
                //startTime = startTime + 1000;
                startTime = startTime + offset;
            }
        }
        return trajectoryPoints;
    }

    @Override
    public Enemy addEnemyByTypeNew(EnemyType enemyType, IMathEnemy mathEnemy, int skinId,
                                   long parentEnemyId, boolean needNearCenter, boolean needFinalSteps, boolean useCustomTrajectories) {
        return null;
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
                    if (!enemy.isBoss()) {
                        deadEnemies.add(enemy);
                        addRemoveTime(enemy.getEnemyType(), time);
                    }
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

                Trajectory oldTrajectory = enemy.getTrajectory();
                List<Point> points = new ArrayList<>();

                for (Point point : oldTrajectory.getPoints()) {
                    points.add(new Point(point.getX(), point.getY(), point.getTime() + freezeTime));
                }

                if (!points.isEmpty()) {

                    Trajectory newTrajectory;

                    if (oldTrajectory instanceof BezierTrajectory) {

                        BezierTrajectory bezierTrajectory = (BezierTrajectory) oldTrajectory;
                        int circularAngle = bezierTrajectory.getCircularAngle();
                        boolean isCircularTrajectory =
                                bezierTrajectory.isCircularTrajectory() != null ? bezierTrajectory.isCircularTrajectory() : false;

                        newTrajectory = new BezierTrajectory(
                                enemy.getSpeed(),
                                points,
                                circularAngle);

                        newTrajectory.setCircularTrajectory(isCircularTrajectory);

                    } else if (enemy.getTrajectory() instanceof HybridTrajectory) {

                        HybridTrajectory hybridTrajectory = (HybridTrajectory) oldTrajectory;
                        int circularAngle = hybridTrajectory.getCircularAngle();
                        boolean isCircularTrajectory =
                                hybridTrajectory.isCircularTrajectory() != null ? hybridTrajectory.isCircularTrajectory() : false;
                        boolean isCircularLargeRadius = hybridTrajectory.isCircularLargeRadius();
                        boolean isCircularStatic = hybridTrajectory.isCircularStatic();

                        newTrajectory = new HybridTrajectory(
                                enemy.getSpeed(),
                                points,
                                circularAngle,
                                isCircularTrajectory,
                                isCircularLargeRadius,
                                isCircularStatic);

                    } else {
                        newTrajectory = new Trajectory(enemy.getSpeed(), points);
                    }

                    enemy.setTrajectory(newTrajectory);
                    trajectories.put(enemy.getId(), newTrajectory);
                    enemy.setLastFreezeTime(time);
                    getLogger().debug("generateFreezeTrajectories enemy: {}", enemy);
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return trajectories;
    }

    public Map<Long, Trajectory> generateBossEffectTrajectory (long time, int freezeTime, double xShot, double yShot, int maxDist) {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        lockEnemy.lock();
        try {
            for (Enemy enemy : getItems()) {
                if (enemy.getEnemyType().getId() == 100) {

                    long enemyId = enemy.getId();
                    double speed = enemy.getSpeed();
                    Trajectory currentTrajectory = enemy.getTrajectory();

                    List<Point> points = new ArrayList<>();

                    for (Point point : currentTrajectory.getPoints()) {
                        points.add(new Point(point.getX(), point.getY(), point.getTime() + freezeTime));
                    }

                    if (!points.isEmpty()) {

                        Trajectory newTrajectory;

                        if (currentTrajectory instanceof BezierTrajectory) {

                            int circularAngle = ((BezierTrajectory) currentTrajectory).getCircularAngle();
                            boolean circularTrajectory = ((BezierTrajectory) currentTrajectory).isCircularTrajectory();

                            BezierTrajectory bezierTrajectory = new BezierTrajectory(speed, points, circularAngle);
                            bezierTrajectory.setCircularTrajectory(circularTrajectory);

                            newTrajectory = bezierTrajectory;

                        } else if (currentTrajectory instanceof HybridTrajectory) {

                            int circularAngle = ((HybridTrajectory) currentTrajectory).getCircularAngle();
                            boolean circularTrajectory = ((HybridTrajectory) currentTrajectory).isCircularTrajectory();
                            boolean circularLargeRadius = ((HybridTrajectory) currentTrajectory).isCircularLargeRadius();
                            boolean isCircularStatic = ((HybridTrajectory) currentTrajectory).isCircularStatic();

                            newTrajectory = new HybridTrajectory(speed, points, circularAngle, circularTrajectory, circularLargeRadius, isCircularStatic);

                        } else {

                            newTrajectory = new Trajectory(speed, points);
                        }

                        enemy.setTrajectory(newTrajectory);

                        trajectories.put(enemyId, newTrajectory);
                        enemy.setLastFreezeTime(time);

                        getLogger().debug("generateBossEffectTrajectory: enemy={}", enemy);
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
        return true;
    }

    public boolean isVisible(int x, int y) {
        // TODO: 19.11.2021 update boss path
        return map.isValid(x, y);// && map.isBossPath(x, y);
    }

    public int getAliveFormationEnemies() {
        int aliveFormationEnemies = 0;
        for (Pair<Integer, Boolean> pair : getItemsTypeIdsAndSwarmState()) {
            if (Boolean.TRUE.equals(pair.getValue())) {
                aliveFormationEnemies++;
            }
        }
        return aliveFormationEnemies;
    }

    public List<GroupParams> getAllGroupsByFormationType(FormationType formationType) {
        List<GroupParams> groupParamsByFormationType = map.getPredefinedGroups().get(formationType.getTypeId());
        return (groupParamsByFormationType != null && !groupParamsByFormationType.isEmpty())
                ? Lists.newArrayList(groupParamsByFormationType) : null;
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

    public boolean checkStartAndEndPoints(Trajectory trajectory, boolean isBoss) {
        long currentTime = System.currentTimeMillis();
        getLogger().debug("currentTime: " + currentTime);
        List<Point> points = trajectory.getPoints();
        long startTime = points.get(0).getTime();
        long endTime = points.get(points.size() - 1).getTime();
        getLogger().debug("startTime: " + startTime);
        getLogger().debug("endTime: " + endTime);
        return currentTime - startTime > 1500 && endTime - currentTime > 3000;
    }

    public boolean isPointOnMapApproximate(Trajectory trajectory, long searchTime, int enemyOffset) {
        List<ValidatedBezierPoint> validatedBezierPoints = approximateBezierTrajectory(trajectory.getPoints());
        long fullTime = trajectory.getPoints().get(trajectory.getPoints().size() - 1).getTime() - trajectory.getPoints().get(0).getTime();
        double percentSearchTime = 0;
        for (int i = 1; i < validatedBezierPoints.size(); i++) {
            boolean isCorrect = validatedBezierPoints.get(i - 1).getCorrectPercent() * fullTime < searchTime && searchTime < validatedBezierPoints.get(i).getCorrectPercent() * fullTime;
            if (isCorrect) {
                long approxTimeOnePart = (long) (validatedBezierPoints.get(i).getCorrectPercent() * fullTime /
                                        (validatedBezierPoints.get(i).getCorrectPercent() * 100));
                percentSearchTime = (double) searchTime / (double) approxTimeOnePart / 100;
            }
        }
        List<Double> positionsX = new ArrayList<>();
        List<Double> positionsY = new ArrayList<>();
        for (Point point : trajectory.getPoints()) {
            positionsX.add(point.getX());
            positionsY.add(point.getY());
        }
        double x = getCurve(positionsX, percentSearchTime);
        double y = getCurve(positionsY, percentSearchTime);
        if ((x > enemyOffset && x < 960 - enemyOffset) && (y > enemyOffset && y < 540 - enemyOffset)) {
            return true;
        } else {
            return false;
        }
    }

    public List<ValidatedBezierPoint> approximateBezierTrajectory (List<Point> points) {
        List<ValidatedBezierPoint> validatedTrajectoryPoints = new ArrayList<>();
        List<ValidatedBezierPoint> finishTrajectoryPoints = new ArrayList<>();
        List<Double> positionsX = new ArrayList<>();
        List<Double> positionsY = new ArrayList<>();
        long firstTimeNum = points.get(0).getTime();
        long lastTimeNum = points.get(points.size() - 1).getTime();
        long fullTimeNum = lastTimeNum - firstTimeNum;

        boolean isTimeCorrect = true;

        for (Point point : points) {
            positionsX.add(point.getX());
            positionsY.add(point.getY());
        }

        for (int i = 0; i < points.size(); i++) {
            double percentNum = ((((double) points.get(i).getTime() - (double) firstTimeNum)) / (double) fullTimeNum);
            if (percentNum > 1) {
                isTimeCorrect = false;
                percentNum = 1;
            }
            double positionX = getCurve(positionsX, percentNum);
            double positionY = getCurve(positionsY, percentNum);
            validatedTrajectoryPoints.add(new ValidatedBezierPoint(positionX, positionY, percentNum, percentNum));
        }

        if (!isTimeCorrect) {
            getLogger().debug("BezierCurve. Incorrect trajectory time for the enemy. " +
                    "The intermediate point time is greater than the last point time.");
        }

        finishTrajectoryPoints.add(validatedTrajectoryPoints.get(0));

        for (int i = 1; i < validatedTrajectoryPoints.size(); i++) {
            ValidatedBezierPoint firstPoint = validatedTrajectoryPoints.get(i - 1);
            ValidatedBezierPoint secondPoint = validatedTrajectoryPoints.get(i);
            List<ValidatedBezierPoint> newApproximatePoints = approximatePoints(firstPoint, secondPoint,
                    positionsX, positionsY, 20);
            finishTrajectoryPoints.addAll(newApproximatePoints);
        }

        int fullTrajectoryLength = 0;

        for (int i = 1; i < finishTrajectoryPoints.size(); i++) {
            PointD first = new PointD(finishTrajectoryPoints.get(i - 1).getX(), finishTrajectoryPoints.get(i - 1).getY());
            PointD second = new PointD(finishTrajectoryPoints.get(i).getX(), finishTrajectoryPoints.get(i).getY());
            finishTrajectoryPoints.get(i).setSegmentLength(getSegmentLength(first, second));
            fullTrajectoryLength += finishTrajectoryPoints.get(i).getSegmentLength();
        }

        int currentTrajectoryLength = 0;
        for (int i = 1; i < finishTrajectoryPoints.size(); i++) {
            currentTrajectoryLength += finishTrajectoryPoints.get(i).getSegmentLength();
            double percentTrajectoryLength = (double) currentTrajectoryLength / (double) fullTrajectoryLength;
            finishTrajectoryPoints.get(i).setCorrectPercent(percentTrajectoryLength);
        }

        return finishTrajectoryPoints;
    }

    private List<ValidatedBezierPoint> approximatePoints(ValidatedBezierPoint first, ValidatedBezierPoint second, List<Double> positionsX, List<Double> positionsY, double accuracy) {
        boolean isAccuracyAchieved = isAccuracyAchieved(first, second, accuracy);
        if (isAccuracyAchieved) {
            List<ValidatedBezierPoint> list = new ArrayList<>();
            list.add(second);
            return list;
        }

        List<ValidatedBezierPoint> finishPoints = new ArrayList<>();
        double halfPercent = (first.getPercent() + second.getPercent()) / 2;
        double posX = getCurve(positionsX, halfPercent);
        double posY = getCurve(positionsY, halfPercent);

        ValidatedBezierPoint middlePoint = new ValidatedBezierPoint(posX, posY, halfPercent, halfPercent);
        double cosABC = cosABC(new PointD(first.getX(), first.getY()), new PointD(middlePoint.getX(), middlePoint.getY()),
                new PointD(second.getX(), second.getY()));
        double accuracyNum = Math.ceil(Math.PI * 3 / (Math.PI - Math.acos(cosABC)));

        List<ValidatedBezierPoint> newFirst = approximatePoints(first, middlePoint, positionsX, positionsY, accuracyNum);
        List<ValidatedBezierPoint> newSecond = approximatePoints(middlePoint, second, positionsX, positionsY, accuracyNum);

        finishPoints.addAll(newFirst);
        finishPoints.addAll(newSecond);

        return finishPoints;
    }

    public double cosABC (PointD pointA, PointD pointB, PointD pointC) {
        PointD vectorAB = new PointD((pointB.x - pointA.x), (pointB.y - pointA.x));
        PointD vectorCB = new PointD((pointB.x - pointC.x), (pointB.y - pointC.x));

        double cosABC = (vectorAB.x * vectorCB.x + vectorAB.y * vectorCB.y) /
                Math.sqrt(vectorAB.x * vectorCB.x + vectorAB.y * vectorCB.y) *
                Math.sqrt(vectorCB.x * vectorCB.x + vectorCB.y * vectorCB.y);

        return cosABC;
    }

    private boolean isAccuracyAchieved (ValidatedBezierPoint first, ValidatedBezierPoint second, double accuracyPoint) {
        if (getSegmentLength(new PointD(first.getX(), first.getY()), new PointD(second.getX(), second.getY())) > accuracyPoint) {
            return false;
        }
        return true;
    }

    private double getSegmentLength(PointD first, PointD second) {
        double x = Math.abs(first.x - second.x);
        double y = Math.abs(first.y - second.y);
        double result = Math.sqrt(x*x + y*y);
        return result;
    }

    private double getCurve(List<Double> positions, double percentT) {
        if (positions.size() == 3) {
            return (1 - percentT) * (1 - percentT) * positions.get(0) +
                    2 * (1 - percentT) * percentT * positions.get(1) + percentT * percentT * positions.get(2);
        } else if (positions.size() == 4) {
            return (1 - percentT) * (1 - percentT) * (1 - percentT) * positions.get(0) +
                    3 * (1 - percentT) * (1 - percentT) * percentT * positions.get(1) + 3 * (1 - percentT) * percentT * percentT * positions.get(2) +
                    percentT * percentT * percentT * positions.get(3);
        } else {
            return (1 - percentT) * positions.get(0) + percentT * positions.get(1);
        }
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
            int payout = config.getEnemyData(enemy.getEnemyType()).getPay();
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
            double payout = config.getEnemyData(enemy.getEnemyType()).getPay();
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
            List<EnemyType> highEnemyIds = spawnConfig.getHighPayEnemies();
            List<EnemyType> midEnemyIds = spawnConfig.getMidPayEnemies();
            EnemyType enemyType = getById(id);
            if (HIGH_PAY_ENEMIES.contains(enemyType) &&
                    (highEnemyIds.isEmpty() || (highEnemyIds.contains(id) && HIGH_PAY_ENEMIES.contains(enemyType)))) {
                enemyTrajectories.forEach(trajectory -> pairs.add(new Pair<>(enemyType, trajectory)));
            } else if (MID_PAY_ENEMIES.contains(enemyType) &&
                    (midEnemyIds.isEmpty() || (midEnemyIds.contains(id) && MID_PAY_ENEMIES.contains(enemyType)))) {
                enemyTrajectories.forEach(trajectory -> pairs.add(new Pair<>(enemyType, trajectory)));
            }
        });

        return pairs;
    }

    public List<PointD> getCurrentEnemiesLocations() {
        return getItems().stream()
                .filter(enemy -> EnemyRange.getEnemiesFromRanges(LOW_PAY_ENEMIES, MID_PAY_ENEMIES, HIGH_PAY_ENEMIES)
                        .contains(enemy.getEnemyType()))
                .map(enemy -> enemy.getLocation(System.currentTimeMillis()))
                .collect(Collectors.toList());
    }

    public Map<Long, Integer> getAllFreezeTimeRemaining(long lastFreezeTime, long currenTime, long freezeTime) {

        Map<Long, Integer> res = new HashMap<>();
        if (lastFreezeTime < 0) {
            return res;
        }

        long remainingTime = freezeTime - (currenTime - lastFreezeTime);

        getLogger().debug("getAllFreezeTimeRemaining remainingTime: {}, lastFreezeTime: {}, remainingTime: {}",
                remainingTime, lastFreezeTime, remainingTime);

        if (remainingTime < 0) {
            return res;
        }

        lockEnemy.lock();
        try {
            for (Enemy enemy : getItems()) {
                res.put(enemy.getId(), (int) remainingTime);
            }
        } finally {
            lockEnemy.unlock();
        }
        return res;
    }
}
