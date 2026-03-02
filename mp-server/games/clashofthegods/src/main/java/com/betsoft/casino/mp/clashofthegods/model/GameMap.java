package com.betsoft.casino.mp.clashofthegods.model;

import com.betsoft.casino.mp.clashofthegods.model.math.EnemyPrize;
import com.betsoft.casino.mp.clashofthegods.model.math.EnemyRange;
import com.betsoft.casino.mp.clashofthegods.model.math.EnemyType;
import com.betsoft.casino.mp.clashofthegods.model.math.MathData;
import com.betsoft.casino.mp.common.*;
import com.betsoft.casino.mp.common.math.MathEnemy;
import com.betsoft.casino.mp.model.IEnemy;
import com.betsoft.casino.mp.model.IEnemyType;
import com.betsoft.casino.mp.model.IMathEnemy;
import com.betsoft.casino.mp.model.IMovementStrategy;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.movement.*;
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
import org.kynosarges.tektosyne.geometry.PointI;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.betsoft.casino.mp.common.BullBossTrajectoryGenerator.TIME_STEP_1_TURN;
import static com.betsoft.casino.mp.common.BullBossTrajectoryGenerator.TIME_STEP_2_HOOF;

@SpringAware
public class GameMap extends AbstractGameMap<Enemy, GameMapShape, EnemyRange, EnemyType, GameMap> {
    private static final int TRAJECTORY_DURATION = 30000;
    private static final float[] PORTAL_DX = new float[]{0, -1, 1};
    private static final float[] PORTAL_DY = new float[]{0, -0.5f, -0.5f};
    private EnemyRange possibleEnemies;
    private static List<Pair<Integer, Long>> pointsTigerDelays;

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
        enemy = new Enemy(enemyIdsGenerator.getAndIncrement(), enemyClass, skinId, trajectory, mathEnemy,
                parentEnemyId, new ArrayList<>());
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
            IMovementStrategy<? extends IEnemy<?, ?>> movementStrategy = enemy.getMovementStrategy();
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

        List<Trajectory> trajectories = map.getPredefinedTrajectories(enemyTypeId, skinId);
        if (trajectories != null && !trajectories.isEmpty()) {
            return prepareTrajectory(trajectories.get(RNG.nextInt(trajectories.size())), speed);
        }

        if (enemyType.equals(EnemyType.Snake)) {
            Trajectory trajectory = new HorusTrajectoryGenerator(map, new PointI(), speed)
                    .generateWithDuration(System.currentTimeMillis() + 1000, TRAJECTORY_DURATION, true);
            getLogger().debug("horus trajectory size: " + trajectory.getPoints().size());
            return trajectory;
        }

        if (enemyType.equals(EnemyType.Lizard)) {
            long spawnTime = System.currentTimeMillis() + 1000;
            PointI source = getRandomSpawnPoint();
            int speedN = RNG.nextInt(3);
            if (speedN == 1) // (40% faster)
                speed = speed * 1.4f;
            else if (speedN == 2) { // (60% faster)
                speed = speed * 1.6f;
            }
            getLogger().debug("lizard speedN : " + speedN + " speed: " + speed);

            return new TrajectoryGenerator(map, source, speed)
                    .generateWithDuration(spawnTime, getTrajectoryDuration(), needFinalSteps);
        }

        return super.getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps);
    }


    @Override
    protected Trajectory getBossTrajectory(double speed, boolean needStandOnThePlace, int skinId) {
        if (skinId == 3) {
            long spawnTime = System.currentTimeMillis() + 2000;
            PointI spawnPoint = getBossSpawnPoint(skinId);
            List<Point> points = new ArrayList<>();
            points.add(new InvulnerablePoint(spawnPoint.x, spawnPoint.y, spawnTime));
            long time = spawnTime + getBossInvulnerabilityTime(skinId);
            BullBossTrajectoryGenerator bullBossTrajectoryGenerator = new BullBossTrajectoryGenerator(getMapShape(), spawnPoint, speed);
            return bullBossTrajectoryGenerator.generate(new Trajectory(speed, points, 800), time, 70, true);
        } else {
            return super.getBossTrajectory(speed, needStandOnThePlace, skinId);
        }
    }

    @Override
    protected boolean isOgre(IEnemyType enemyType) {
        return enemyType.equals(EnemyType.Snake);
    }

    protected Pair<Integer, Trajectory> getTrajectoryWithSaveStartPosition(EnemyType enemyType, float speed,
                                                                           boolean needStandOnPlace, boolean needNearCenter,
                                                                           int skinId, boolean needFinalSteps, int enemyTypeId) {
        List<Trajectory> trajectories = map.getPredefinedTrajectories(enemyTypeId, skinId);
        if (trajectories != null && !trajectories.isEmpty()) {
            int index = RNG.nextInt(trajectories.size());

            if (EnemyRange.BOMB_ENEMY.getEnemies().contains(enemyType)) {
                List<Long> existsId = new ArrayList<>();
                items.forEach(enemy -> {
                    if (EnemyRange.BOMB_ENEMY.getEnemies().contains(enemy.getEnemyClass().getEnemyType()))
                        existsId.add(enemy.getTrajectory().getId());
                });
                int cnt = 100;
                while (cnt-- > 0) {
                    index = RNG.nextInt(trajectories.size());
                    if (!existsId.contains((long) index)) {
                        break;
                    }
                }
            }

            boolean isPhoenix = EnemyType.Phoenix.equals(enemyType);
            boolean isDragonFly = EnemyType.Dragonfly_Red.equals(enemyType)
                    || EnemyType.Dragonfly_Green.equals(enemyType);
            Trajectory template;
            List<Point> newPoints;
            if (isPhoenix || isDragonFly) {
                newPoints = new ArrayList<>();
                List<Point> points = trajectories.get(index).getPoints();
                Point firstPoint = new Point(points.get(0));
                Point lastPoint = new Point(points.get(points.size() - 1));


                if (isPhoenix) {
                    boolean needIncrement = firstPoint.getX() < firstPoint.getY();
                    newPoints.add(firstPoint);
                    int cnt = 100;
                    double lastX = firstPoint.getX();
                    double lastY = firstPoint.getY();
                    while (cnt-- > 0) {
                        lastX += needIncrement ? 1 : -1;
                        lastY += needIncrement ? -1 : 1;
                        newPoints.add(new Point(lastX, lastY, 0));
                        if ((!needIncrement && lastX >= lastPoint.getY())
                                || (needIncrement && lastX <= lastPoint.getY()))
                            break;
                    }
                }

                if (isDragonFly) {
                    long time = System.currentTimeMillis() + 1000;
                    if (RNG.nextBoolean()) {
                        firstPoint = new Point(points.get(points.size() - 1));
                        lastPoint = new Point(points.get(0));
                    }

                    boolean needIncrement = firstPoint.getX() < lastPoint.getX();
                    double len = (needIncrement ? lastPoint.getX() - firstPoint.getX() :
                            firstPoint.getX() - lastPoint.getX()) / 4;
                    firstPoint.setTime(time);
                    newPoints.add(new Point(firstPoint.getX(), firstPoint.getY(), firstPoint.getTime()));
                    long lastCurrentTime = 0;
                    for (int i = 1; i <= 3; i++) {
                        double x = firstPoint.getX() + (needIncrement ? len * (i) : -len * (i));
                        double y = firstPoint.getY();
                        Point lastTempPoint = newPoints.get(newPoints.size() - 1);
                        lastCurrentTime = getTime(lastTempPoint.getTime(), lastTempPoint, x, y, speed);
                        newPoints.add(new Point(x, y, lastCurrentTime));
                        lastCurrentTime += 1000;
                        newPoints.add(new Point(x, y, lastCurrentTime));
                    }
                    Point lastTempPoint = newPoints.get(newPoints.size() - 1);
                    lastPoint.setTime(getTime(lastCurrentTime, lastTempPoint, lastPoint.getX(), lastPoint.getY(), speed));
                    newPoints.add(lastPoint);
                    return new Pair<>(index, new Trajectory(speed, newPoints));
                }
            } else {
                newPoints = new ArrayList<>(trajectories.get(index).getPoints());
            }

            if (RNG.nextBoolean())
                Collections.reverse(newPoints);
            template = new Trajectory(speed, newPoints);
            return new Pair<>(index, prepareTrajectory(template, speed));
        }
        return new Pair<>(0, super.getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps));
    }

    private long getTime(long time, Point current, double nextX, double nextY, double speed) {
        double dx = current.getX() - nextX;
        double dy = current.getY() - nextY;
        time += (long) (Math.sqrt(dx * dx + dy * dy) / (speed / 1000));
        return time;
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
        return addEnemyByTypeNew(enemyType, mathEnemy, skinId, parentEnemyId, needNearCenter,
                needFinalSteps, false);
    }

    public Trajectory getPredefinedTrajectory(int swarmTypeId, float speed) {
        List<Trajectory> trajectories = map.getPredefinedTrajectories(swarmTypeId);
        if (trajectories != null) {
            return prepareTrajectory(getRandomElement(trajectories), speed);
        }
        return null;
    }


    public List<Enemy> getPreReturnedEnemies(int swarmTypeId) {
        List<Enemy> res = new ArrayList<>();
        lockEnemy.lock();
        try {
            long currentTimeMillis = System.currentTimeMillis();
            if (swarmTypeId > 0) {
                Optional<Map.Entry<Long, Enemy>> first = removedEnemies.entrySet().stream()
                        .filter(longEnemyEntry -> {
                            Enemy enemy = longEnemyEntry.getValue();
                            return currentTimeMillis > enemy.getReturnTime()
                                    && enemy.getSwarmType() == swarmTypeId;
                        })
                        .findFirst();
                if (first.isPresent() && items.stream()
                        .noneMatch(enemy -> enemy.getSwarmId() == first.get().getValue().getSwarmId())) {
                    int swarmId = first.get().getValue().getSwarmId();
                    removedEnemies.forEach((aLong, enemy) -> {
                        if (enemy.getSwarmId() == swarmId) {
                            res.add(enemy);
                        }
                    });
                }

                if (!res.isEmpty()) {
                    AtomicBoolean notAllEnemiesReady = new AtomicBoolean(false);
                    res.forEach(enemy -> {
                        if (currentTimeMillis < enemy.getReturnTime())
                            notAllEnemiesReady.set(true);
                    });
                    if (notAllEnemiesReady.get()) {
                        res.clear();
                    }
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return res;
    }

    public void reEnterToMap(List<Enemy> enemies) {
        lockEnemy.lock();
        try {
            for (Enemy enemy : enemies) {
                removedEnemies.remove(enemy.getId());
                items.add(enemy);
            }
        } finally {
            lockEnemy.unlock();
        }
    }

    public void clearRemovedEnemies(List<Enemy> enemies) {
        lockEnemy.lock();
        try {
            for (Enemy enemy : enemies) {
                removedEnemies.remove(enemy.getId());
                swarms.remove(enemy.getSwarmId());
            }
        } finally {
            lockEnemy.unlock();
        }
    }


    public Map<Integer, Set<Integer>> getSwarmUniqLiveCounts() {
        return getSwarmUniqCounts(items);
    }

    public Map<Integer, Set<Integer>> getSwarmUniqRemovedCounts() {
        return getSwarmUniqCounts(removedEnemies.values());
    }

    public int getCountRemovedEnemiesByTypeId(int typeId) {
        long res;
        lockEnemy.lock();
        try {
            res = removedEnemies.values()
                    .stream()
                    .filter(enemy -> enemy.getEnemyClass().getEnemyType().getId() == typeId)
                    .count();
        } finally {
            lockEnemy.unlock();
        }
        return (int) res;
    }

    public Enemy getReadyRemovedEnemiesByTypeId(int typeId) {
        Optional<Enemy> res;
        lockEnemy.lock();
        try {
            long currentTimeMillis = System.currentTimeMillis();
            res = removedEnemies.values()
                    .stream()
                    .filter(enemy ->
                            enemy.getEnemyClass().getEnemyType().getId() == typeId
                                    && currentTimeMillis > enemy.getReturnTime())
                    .findFirst();
        } finally {
            lockEnemy.unlock();
        }
        return res.orElse(null);
    }


    public Map<Integer, Set<Integer>> getSwarmUniqCounts(Collection<Enemy> enemies) {
        Map<Integer, Set<Integer>> res = new HashMap<>();
        lockEnemy.lock();
        try {
            for (Enemy item : enemies) {
                int swarmType = item.getSwarmType();
                int swarmId = item.getSwarmId();
                if (res.get(swarmType) == null) {
                    HashSet<Integer> set = new HashSet<>();
                    set.add(swarmId);
                    res.put(swarmType, set);
                } else {
                    res.get(swarmType).add(swarmId);
                }
            }
            return res;
        } finally {
            lockEnemy.unlock();
        }
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
                if (EnemyType.Snake.equals(enemy.getEnemyClass().getEnemyType())) {
                    long time = System.currentTimeMillis();
                    trajectory = new Trajectory(enemy.getSpeed())
                            .addPoint(new TeleportPoint(location.x, location.y, time, true))
                            .addPoint(new InvulnerablePoint(location.x, location.y, time + 850));
                } else {
                    trajectory = generateLeaveTrajectory(generator, new PointI(location.x, location.y), startTime, enemy);
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

        if (EnemyRange.DRAGONS.getEnemies().contains(enemy.getEnemyClass().getEnemyType()))
            return null;

        if (enemy.isBoss() && enemy.getSkin() == 3) {
            return generateBullBossLeaveTrajectory(startTime, enemy);
        }

        if (EnemyRange.FLY_ENEMIES.getEnemies().contains(enemy.getEnemyClass().getEnemyType())) {
            Trajectory trajectory = enemy.getTrajectory();
            List<Point> leavePoints = new ArrayList<>();
            leavePoints.add(new Point(location.x, location.y, startTime));
            List<Point> points = trajectory.getPoints();
            Point lastPoint = points.get(points.size() - 1);
            double dist = Math.sqrt(Math.pow((location.x - lastPoint.getX()), 2) + Math.pow((location.y - lastPoint.getY()), 2));
            double speed = enemy.getTrajectory().getSpeed() * 3;
            int needTime = (int) (dist / (speed / 1000));
            leavePoints.add(new Point(lastPoint.getX(), lastPoint.getY(), startTime + needTime));
            return new Trajectory(speed, leavePoints);
        }
        return super.generateLeaveTrajectory(generator, location, startTime, enemy);
    }

    protected Trajectory generateBullBossLeaveTrajectory(long startTime, Enemy enemy) {
        List<Point> leavePoints = new ArrayList<>();
        PointI location = enemy.getLocation(startTime).toPointI();
        PointI randomSpawnPoint = getRandomSpawnPoint();
        leavePoints.add(new Point(location.x, location.y, startTime));
        leavePoints.add(new Point(location.x, location.y, startTime + TIME_STEP_1_TURN));
        leavePoints.add(new Point(location.x, location.y, startTime + TIME_STEP_2_HOOF));
        double dist = Math.sqrt(Math.pow((location.x - randomSpawnPoint.x), 2) + Math.pow((location.y - randomSpawnPoint.y), 2));
        double speed = enemy.getTrajectory().getSpeed();
        int needTime = (int) (dist / (speed / 1000));
        long time = startTime + TIME_STEP_2_HOOF + needTime;
        leavePoints.add(new Point(randomSpawnPoint.x, randomSpawnPoint.y, time));
        getLogger().debug("generateBullBossLeaveTrajectory: leavePoints: {} ", leavePoints);
        return new Trajectory(enemy.getSpeed(), leavePoints);
    }

    // TODO: refactor, add Factory Method for TrajectoryGenerator into EnemyType
    public Map<Long, Trajectory> generateUpdateTrajectories(boolean needFinalSteps) {
        return Collections.EMPTY_MAP;
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

    protected Trajectory prepareTrajectoryWithDuplicatePointsDelays(Trajectory template,
                                                                    float speed,
                                                                    boolean needJump) {
        Trajectory trajectory = new Trajectory(speed);
        long time = System.currentTimeMillis() + 1000;
        Iterator<Point> points = template.getPoints().iterator();
        Point current = points.next();
        double currentX = current.getX();
        double currentY = current.getY();
        List<Pair<Integer, Long>> pointsTigerDelays = getPointsTigerDelays();
        trajectory.addPoint(currentX, currentY, time);
        int idx = 0;

        while (points.hasNext()) {
            int finalIdx = idx;

            if (needJump) {
                Optional<Pair<Integer, Long>> first = pointsTigerDelays.stream().filter(pair ->
                        pair.getKey() == finalIdx).findFirst();
                if (first.isPresent()) {
                    time += first.get().getValue();
                    trajectory.addPoint(new TeleportPoint(currentX, currentY, time, false));
                    if (first.get().getKey() == 2) {
                        time += 400;
                        trajectory.addPoint(new Point(currentX, currentY, time));
                    }
                }
            }

            Point next = points.next();
            double dx = currentX - next.getX();
            double dy = currentY - next.getY();
            time += (long) (Math.sqrt(dx * dx + dy * dy) / (speed / 1000));
            trajectory.addPoint(next.getX(), next.getY(), time);
            currentX = next.getX();
            currentY = next.getY();

            idx++;
        }
        return trajectory;
    }

    @Override
    protected Trajectory getRandomTrajectory(IEnemyType enemyType, double speed, short minSteps, boolean needFinalSteps) {
        return super.getRandomTrajectory(enemyType, speed, minSteps, needFinalSteps);
    }

    @Override
    protected Trajectory getInitialTrajectory(double speed, boolean needFinalSteps, IEnemyType enemyType_) {

        EnemyType enemyType = (EnemyType) enemyType_;
        boolean isBombEnemy = EnemyRange.BOMB_ENEMY.getEnemies().contains(enemyType);
        int defaultSkiId = 1;

        boolean isDragons = EnemyRange.DRAGONS.getEnemies().contains(enemyType);
        boolean isPhoenix = EnemyType.Phoenix.equals(enemyType);
        boolean isOwls = EnemyRange.BIRDS.getEnemies().contains(enemyType);
        boolean isFlyEnemy = EnemyRange.FLY_ENEMIES.getEnemies().contains(enemyType);
        boolean isDragoFly = EnemyType.Dragonfly_Green.equals(enemyType) || EnemyType.Dragonfly_Red.equals(enemyType);
        boolean isBeetles = EnemyType.Beetle_1.equals(enemyType) || EnemyType.Beetle_2.equals(enemyType);

        boolean needCustomTrajectory = isPhoenix || isOwls || isDragons || isBombEnemy || isDragoFly || isBeetles;
        if (needCustomTrajectory) {
            EnemyType enemyTypePredefined = isBombEnemy ? EnemyType.Spirits_1_RED : enemyType;
            int customId = enemyTypePredefined.getId();
            if (isFlyEnemy && !isPhoenix && !isOwls) {
                customId = -10; // customId for all fly enemies
            } else if (isBeetles) {
                customId = 0;
            }

            if (isOwls && RNG.nextBoolean()) {
                speed = speed * 0.75;
            }

            Pair<Integer, Trajectory> trajectoryWithSaveStartPosition =
                    getTrajectoryWithSaveStartPosition(enemyTypePredefined,
                            (float) speed,
                            false,
                            false,
                            defaultSkiId,
                            needFinalSteps, customId);

            Trajectory trajectory = trajectoryWithSaveStartPosition.getValue();
            if (isDragons) {
                long shift = 3000;
                List<Point> newPoints = new ArrayList<>();
                List<Point> points = trajectory.getPoints();
                for (int i = 0; i < points.size(); i++) {
                    Point point = points.get(i);
                    if (i == 0) {
                        newPoints.add(point);
                        newPoints.add(new Point(point.getX(), point.getY(), point.getTime() + shift));
                    } else if (i == points.size() - 1) {
                        newPoints.add(new Point(point.getX(), point.getY(), point.getTime() + shift));
                        newPoints.add(new Point(point.getX(), point.getY(), point.getTime() + 2 * shift));
                    } else {
                        newPoints.add(new Point(point.getX(), point.getY(), point.getTime() + shift));
                    }
                }
                trajectory = new Trajectory(trajectory.getSpeed(), newPoints);
            }

            if (EnemyRange.BOMB_ENEMY.getEnemies().contains(enemyType)) {
                trajectory.setId(Long.valueOf(trajectoryWithSaveStartPosition.getKey()));
            }
            return trajectory;
        }

        if (EnemyType.Tiger.equals(enemyType)) {
            speed = generateSpeed(enemyType.getSkin(defaultSkiId));
            boolean needDoubleSpeed = RNG.nextInt(3) > 1;
            List<Trajectory> trajectories = map.getPredefinedTrajectories(enemyType.getId(), 0);
            int index = RNG.nextInt(trajectories.size());
            Trajectory template = trajectories.get(index);
            boolean needJump = false;
            if (needDoubleSpeed) {
                speed *= 2.7;
                needJump = RNG.nextBoolean();
            }

            if (RNG.nextBoolean()) {
                List<Point> newPoints = new ArrayList<>(template.getPoints());
                Collections.reverse(newPoints);
                template = new Trajectory(speed, newPoints);
            }

            Trajectory trajectory = prepareTrajectoryWithDuplicatePointsDelays(template, (float) speed, needJump);
            return trajectory;
        }


        PointI source = getRandomSpawnPoint();

        if (enemyType.equals(EnemyType.Snake))
            return new HorusTrajectoryGenerator(map, new PointI(), speed)
                    .generate(System.currentTimeMillis() + 1000, 7, needFinalSteps);

        if (enemyType.equals(EnemyType.Lizard)) {
            long spawnTime = System.currentTimeMillis() + 1000;
            int speedN = RNG.nextInt(3);
            if (speedN == 1) // (40% faster)
                speed = speed * 1.4f;
            else if (speedN == 2) { // (60% faster)
                speed = speed * 1.6f;
            }
            getLogger().debug("lizard speedN : " + speedN + " speed: " + speed);

            return new TrajectoryGenerator(map, source, speed)
                    .generateWithDuration(spawnTime, getTrajectoryDuration(), needFinalSteps);
        }

        if (EnemyRange.BOMB_ENEMY.getEnemies().contains(enemyType)) {
            return new TrajectoryGenerator(map, source, speed)
                    .generateWithDuration(System.currentTimeMillis() + 1000, 30000, needFinalSteps);

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

    @Override
    protected long getBossInvulnerabilityTime(int skinId) {
        return skinId == 3 ? 4200 : 2000;
    }

    @Override
    protected long getBossSpawnAnimationDuration(int skinId) {
        return 50;
    }

    @Override
    protected void addPointsFromOldTrajectory(List<Point> points, long startTime, long freezeTime, PointI location, Enemy enemy) {
        if (EnemyType.Snake.equals(enemy.getEnemyClass().getEnemyType())) {
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

    public static List<Pair<Integer, Long>> getPointsTigerDelays() {
        if (pointsTigerDelays == null) {
            pointsTigerDelays = new ArrayList<>();
            pointsTigerDelays.add(new Pair<>(1, 500L));
            pointsTigerDelays.add(new Pair<>(2, 600L));
        }
        return pointsTigerDelays;
    }
}
