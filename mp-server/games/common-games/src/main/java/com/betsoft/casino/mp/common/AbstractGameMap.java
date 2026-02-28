package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.common.scenarios.SpawnGroup;
import com.betsoft.casino.mp.common.scenarios.SpawnScenario;
import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.movement.*;
import com.abs.casino.common.cache.JsonSelfSerializable;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import com.dgphoenix.casino.common.util.Triple;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * User: flsh
 * Date: 08.02.19.
 */

/**
 * Abstract class for map of game room.
 * @param <ENEMY> enemy
 * @param <MAP_SHAPE> map shape of map
 * @param <ENEMY_RANGE> enemy range
 * @param <ENEMY_TYPE> enemy type
 */
@SpringAware
public abstract class AbstractGameMap<ENEMY extends AbstractEnemy, MAP_SHAPE extends IGameMapShape,
        ENEMY_RANGE extends IEnemyRange, ENEMY_TYPE extends IEnemyType, GM extends AbstractGameMap>
        implements IMap<ENEMY, MAP_SHAPE>, KryoSerializable, JsonSelfSerializable<GM>, ApplicationContextAware {
    private static final byte VERSION = 2;

    private transient Logger logger;

    protected static final Long BOSS_SPAWN_ANIMATION_DURATION = 7500L;
    protected static final Long BOSS_INVULNERABILITY_TIME = 3000L;
    protected static final long SPAWN_DELAY = 1000L;
    protected static final int TRAJECTORY_DURATION = 30000;

    private static final int SCREEN_WIDTH = 960;
    private static final int SCREEN_HEIGHT = 540;
    protected transient Coords coords;

    protected transient ApplicationContext context;

    protected short width;
    protected short height;
    /** live enemies on map  */
    protected final List<ENEMY> items = new ArrayList<>();
    /**  not active live enemies out of map (can be returned) */
    private Map<ENEMY, Long> inactivityLiveItems = new HashMap<>();
    protected AtomicInteger enemyIdsGenerator = new AtomicInteger(0);
    protected AtomicInteger swarmIdGenerator = new AtomicInteger(1);
    protected AtomicInteger portalIdGenerator = new AtomicInteger(1);
    /** description of map  */
    protected transient GameMapShape map;
    /** current id of map  */
    protected int mapId;
    /** lock for critical sections  */
    protected transient ReentrantLock lockEnemy = new ReentrantLock();
    protected Map<Integer, Long> scenarioCooldowns = new HashMap<>();
    protected Map<Integer, Long> trajectoryCooldowns = new HashMap<>();
    protected Map<Integer, Swarm<ENEMY>> swarms = new HashMap<>();
    protected Map<Long, ENEMY> removedEnemies = new HashMap<>();
    protected long portalCooldown;

    protected Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(AbstractGameMap.class);
        }
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    protected abstract boolean isNotBaseEnemy(ENEMY enemy);

    protected abstract ENEMY_RANGE getPossibleEnemies();

    protected abstract List<ENEMY_TYPE> getEnemyTypes();

    protected abstract ENEMY_TYPE getEnemyByTypeId(int typeId);

    protected abstract ENEMY createEnemy(ENEMY_TYPE enemyType, int skinId, Trajectory trajectory, float speed,
                                         IMathEnemy mathEnemy, long parentEnemyId);

    //empty constructor required for Kryo serialization
    public AbstractGameMap() {
    }

    public AbstractGameMap(GameMapShape map) {
        this.map = map;
        this.mapId = map.getId();
        this.width = map.getWidth();
        this.height = map.getHeight();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public short getWidth() {
        return width;
    }

    @Override
    public short getHeight() {
        return height;
    }

    /**
     * Get all enemies on map.
     * @return {@code List<ENEMY> } list enemies
     */
    @Override
    public List<ENEMY> getItems() {
        lockEnemy.lock();
        try {
            return Collections.unmodifiableList(items);
        } finally {
            lockEnemy.unlock();
        }
    }

    @Override
    public ReentrantLock getLockEnemy() {
        return lockEnemy;
    }

    /**
     * Get number of enemies on map.
     * @return the number of enemies on map
     */
    public int getItemsSize() {
        lockEnemy.lock();
        try {
            return items.size();
        } finally {
            lockEnemy.unlock();
        }
    }

    public boolean noEnemiesInRoom() {
        lockEnemy.lock();
        try {
            return items.isEmpty();
        } finally {
            lockEnemy.unlock();
        }
    }


    /**
     * Get first id of boss
     * @return enemyId of boss, -1 is boss is not found.
     */
    public Long getAnyBossId() {
        lockEnemy.lock();
        try {
            for (ENEMY enemy : items) {
                if (enemy.isBoss())
                    return enemy.getId();
            }
            return -1L;

        } finally {
            lockEnemy.unlock();
        }
    }

    public int getNumberInactivityItems() {
        lockEnemy.lock();
        try {
            return getInactivityLiveItems().size();
        } finally {
            lockEnemy.unlock();
        }
    }

    /**
     * Get number inactive enemies from specific range
     * @param range specific range
     * @return the number of enemies
     */
    public int getNumberInactivityItemsByRange(IEnemyRange range) {
        AtomicInteger res = new AtomicInteger();
        lockEnemy.lock();
        try {
            getInactivityLiveItems().forEach((enemy, time) -> {
                if (range.getEnemies().contains(enemy.getEnemyClass().getEnemyType()))
                    res.getAndIncrement();
            });
        } finally {
            lockEnemy.unlock();
        }
        return res.get();
    }

    @Override
    public List<Integer> getItemsTypeIds() {
        List<Integer> res = new ArrayList<>();
        lockEnemy.lock();
        try {
            for (ENEMY item : items) {
                res.add(item.getEnemyClass().getEnemyType().getId());
            }
            return res;
        } finally {
            lockEnemy.unlock();
        }
    }

    public List<IEnemyType<?>> getItemTypes() {
        List<IEnemyType<?>> result = new ArrayList<>();
        lockEnemy.lock();
        try {
            for (ENEMY item : items) {
                result.add(item.getEnemyClass().getEnemyType());
            }
            return result;
        } finally {
            lockEnemy.unlock();
        }
    }

    public int getAliveEnemiesCount(ENEMY_RANGE enemyRange) {
        return (int) getItemTypes().stream()
                .filter(enemyType -> enemyRange.getEnemies().contains(enemyType))
                .count();
    }

    public List<Pair<Integer, Boolean>> getItemsTypeIdsAndSwarmState() {
        List<Pair<Integer, Boolean>> res = new ArrayList<>();
        lockEnemy.lock();
        try {
            for (ENEMY item : items) {
                res.add(new Pair<>(item.getEnemyClass().getEnemyType().getId(), item.isPartOfSwarm()));
            }
            return res;
        } finally {
            lockEnemy.unlock();
        }
    }

    public List<Triple<Integer, Boolean, Integer>> getItemsTypeIdsAndSwarmStateAndIds() {
        List<Triple<Integer, Boolean, Integer>> res = new ArrayList<>();
        lockEnemy.lock();
        try {
            for (ENEMY item : items) {
                res.add(new Triple<>(item.getEnemyClass().getEnemyType().getId(), item.isPartOfSwarm(),
                        item.getCurrentTrajectoryId()));
            }
            return res;
        } finally {
            lockEnemy.unlock();
        }
    }

    public List<Triple<Integer, Integer, Integer>> getItemsTypeIdsAndSwarmTypeAndIds() {
        List<Triple<Integer, Integer, Integer>> res = new ArrayList<>();
        lockEnemy.lock();
        try {
            for (ENEMY item : items) {
                res.add(new Triple<>(item.getEnemyClass().getEnemyType().getId(),
                        item.isPartOfSwarm() ? item.getSwarmType() : -1,
                        item.getCurrentTrajectoryId()));
            }
            return res;
        } finally {
            lockEnemy.unlock();
        }
    }

    /**
     * Get enemy by id
     * @param enemyId enemyId
     * @return ENEMY if found, null otherwise.
     */
    @Override
    public ENEMY getItemById(Long enemyId) {
        ENEMY res = null;
        lockEnemy.lock();
        try {
            for (ENEMY enemy : items) {
                if (enemy.getId() == enemyId) {
                    res = enemy;
                    break;
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return res;
    }

    public ENEMY getFirstEnemyByTypeId(int enemyTypeId) {
        ENEMY res = null;
        lockEnemy.lock();
        try {
            for (ENEMY enemy : items) {
                if (enemy.getEnemyClass().getEnemyType().getId() == enemyTypeId) {
                    res = enemy;
                    break;
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return res;
    }

    @Override
    public void updateEnemyMode(Long enemyId, EnemyMode enemyMode) {
        lockEnemy.lock();
        try {
            for (ENEMY enemy : items) {
                if (enemy.getId() == enemyId) {
                    enemy.setEnemyMode(enemyMode);
                    break;
                }
            }
        } finally {
            lockEnemy.unlock();
        }
    }

    @Override
    public Long getNearestEnemy(PointD point, boolean excludeBaseEnemy, Long baseEnemyId, Long customDistance) {
        double distance = customDistance == null ? 100000.0 : customDistance;
        Long nearestEnemyId = null;
        lockEnemy.lock();
        try {

            for (ENEMY enemy : items) {
                boolean notBaseEnemy = isNotBaseEnemy(enemy);
                if (notBaseEnemy || (excludeBaseEnemy && enemy.getId() == baseEnemyId))
                    continue;

                double dist = calculateSquareDist(System.currentTimeMillis(), enemy, point);
                if (dist < distance) {
                    distance = dist;
                    nearestEnemyId = enemy.getId();
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return nearestEnemyId;
    }

    @Override
    public Long getAllNearestEnemy(long time, PointD point, boolean excludeBaseEnemy, Long baseEnemyId, Long customDistance) {
        double distance = customDistance == null ? 100000.0 : customDistance;
        Long nearestEnemyId = null;
        lockEnemy.lock();
        try {
            for (ENEMY enemy : items) {
                if (excludeBaseEnemy && enemy.getId() == baseEnemyId) {
                    continue;
                }

                if (enemy.isInvulnerable(time)) {
                    continue;
                }

                double dist = calculateSquareDist(time, enemy, point);
                if (dist < distance) {
                    distance = dist;
                    nearestEnemyId = enemy.getId();
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return nearestEnemyId;
    }

    @Override
    public Map<Long, Double> getNNearestEnemies(long time, PointD point, Long baseEnemyId, int numberEnemies) {
        lockEnemy.lock();
        try {
            return getNNearestEnemiesWithoutLock(time, point, baseEnemyId, numberEnemies);
        } finally {
            lockEnemy.unlock();
        }
    }

    public Map<Long, Double> getNNearestEnemiesWithoutLock(long time, PointD point, Long baseEnemyId, int numberEnemies) {
        LinkedHashMap<Long, Double> res = new LinkedHashMap<>();
        Map<Long, Double> enemiesDistances = new LinkedHashMap<>();
        for (ENEMY enemy : items) {
            double dist = calculateSquareDist(time, enemy, point);
            if (enemy.getId() == baseEnemyId) {
                res.put(enemy.getId(), dist);
            } else if (!enemy.isInvulnerable(time)) {
                enemiesDistances.put(enemy.getId(), dist);
            }
        }
        LinkedHashMap<Long, Double> newEnemies = enemiesDistances.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(numberEnemies - 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        res.putAll(newEnemies);
        return res;
    }

    /**
     * Calculate dist between enemy and point
     * @param time current time
     * @param enemy enemy
     * @param point point on map
     * @return distance
     */
    protected double calculateSquareDist(long time, ENEMY enemy, PointD point) {
        PointD enemyLocation = enemy.getLocation(time);
        return Math.pow((point.x - enemyLocation.x), 2) + Math.pow((point.y - enemyLocation.y), 2);
    }

    @Override
    public Map<Long, Double> getNNearestEnemiesWithoutBase(long time, PointD point, Long baseEnemyId, int numberEnemies) {
        lockEnemy.lock();
        try {
            return getNNearestEnemiesWithoutBaseWithoutLock(time, point, baseEnemyId, numberEnemies);
        } finally {
            lockEnemy.unlock();
        }
    }

    public Map<Long, Double> getNNearestEnemiesWithoutBaseWithoutLock(long time, PointD point, Long baseEnemyId, int numberEnemies) {
        Map<Long, Double> enemiesDistances = new LinkedHashMap<>();
        for (ENEMY enemy : items) {
            double dist = calculateSquareDist(time, enemy, point);
            if (enemy.getId() != baseEnemyId && enemyCouldBeHit(time, enemy)) {
                enemiesDistances.put(enemy.getId(), dist);
            }
        }
        return enemiesDistances.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(numberEnemies)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    protected boolean enemyCouldBeHit(long time, ENEMY enemy) {
        return !enemy.isInvulnerable(time) && isVisible(time, enemy);
    }

    @Override
    public Map<Long, Double> getNNearestEnemies(long time, PointD point, Long baseEnemyId, int numberEnemies,
                                                IEnemyRange allowedRange, long timeBeforeLastPoint) {

        lockEnemy.lock();
        Map<Long, Double> enemiesDistances = new LinkedHashMap<>();
        LinkedHashMap<Long, Double> res = new LinkedHashMap<>();
        try {
            for (ENEMY enemy : items) {
                if (!allowedRange.getEnemies().contains(enemy.getEnemyClass().getEnemyType()))
                    continue;
                double dist = calculateSquareDist(time, enemy, point);
                if (!enemy.isInvulnerable(time)) {
                    List<Point> points = enemy.getTrajectory().getPoints();
                    boolean enemyIsInNotEnd = time + timeBeforeLastPoint < points.get(points.size() - 1).getTime();
                    if (timeBeforeLastPoint == 0 || enemyIsInNotEnd)
                        enemiesDistances.put(enemy.getId(), dist);
                }
            }
            LinkedHashMap<Long, Double> newEnemies = enemiesDistances.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .limit(numberEnemies - 1)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            res.putAll(newEnemies);
        } finally {
            lockEnemy.unlock();
        }
        return res;

    }

    @Override
    public Map<Long, Double> getNNearestEnemies(long time, PointD point, Long baseEnemyId, int numberEnemies, IEnemyRange allowedRange) {
        return getNNearestEnemies(time, point, baseEnemyId, numberEnemies, allowedRange, 0);
    }

    public Long getNearestEnemyForMine(PointD point, Map<Pair<Integer, Integer>, Double> customDistances) {
        double distance = 100000.0;
        ENEMY nearestEnemy = null;
        long time = System.currentTimeMillis();
        lockEnemy.lock();
        try {
            for (ENEMY enemy : items) {
                double dist = calculateSquareDist(System.currentTimeMillis(), enemy, point);
                if (dist < distance && !enemy.isInvulnerable(time)) {
                    distance = dist;
                    nearestEnemy = enemy;
                }
            }
        } finally {
            lockEnemy.unlock();
        }

        int skin = -1;
        int typeId = -1;
        long nearestEnemyId = -1;
        if (nearestEnemy != null) {
            skin = nearestEnemy.getSkin();
            typeId = nearestEnemy.getEnemyClass().getEnemyType().getId();
            nearestEnemyId = nearestEnemy.getId();
        }

        Double customDistance = customDistances.get(new Pair<>(typeId, skin));
        double enemyDistance = customDistance != null ? customDistance : 40;
        return distance < enemyDistance ? nearestEnemyId : -1;
    }

    /**
     * Remove all enemies on map
     */
    @Override
    public void removeAllEnemies() {
        lockEnemy.lock();
        try {
            items.clear();
            inactivityLiveItems.clear();
            swarms.clear();
            removedEnemies.clear();
            scenarioCooldowns.clear();
        } finally {
            lockEnemy.unlock();
        }
    }

    /**
     * Remove all live enemies and get list ids of removed enemies
     * @return {@code List<Long> } list of id removed enemies
     */
    @Override
    public List<Long> removeAllEnemiesAndGetIds() {
        ArrayList<Long> res = new ArrayList<>();
        lockEnemy.lock();
        try {
            for (ENEMY item : items) {
                res.add(item.getId());
            }
            items.clear();
        } finally {
            lockEnemy.unlock();
        }
        return res;
    }

    @Override
    public ENEMY addRandomEnemyFromPossibleList(short numberSeats, int skinId, boolean needStandOnPlace,
                                                IMathEnemy mathEnemy, long parentEnemyId, boolean needFinalSteps) {
        ENEMY_TYPE enemyType;
        List<ENEMY_TYPE> enemyTypes = getEnemyTypes();
        int size = enemyTypes.size();
        do {
            enemyType = enemyTypes.get(RNG.nextInt(size));
        } while (enemyType.isHVenemy());

        int skin = skinId == -1 ? getRandomSkin(enemyType) : skinId;
        float speed = generateSpeed(enemyType.getSkin(skin));
        return addItem(enemyType, skin, getTrajectory(enemyType, speed, needStandOnPlace, false, skin,
                needFinalSteps), speed, mathEnemy, parentEnemyId);
    }

    protected float generateSpeed(ISkin skin) {
        if (skin.getSpeedDeltaNegative() > 0 && RNG.nextBoolean()) {
            return skin.getSpeed() - (float) (RNG.rand() * skin.getSpeedDeltaNegative());
        } else {
            return skin.getSpeed() + (float) (RNG.rand() * skin.getSpeedDeltaPositive());
        }
    }

    /**
     * Adds enemy to map
     * @param enemyType enemyType
     * @param mathEnemy math enemy data
     * @param skinId skinId
     * @param parentEnemyId parent enemyId
     * @param needNearCenter need generate near center
     * @param needFinalSteps need final steps for out from map
     * @param useCustomTrajectories need use custom predefined trajectories
     * @return {@code ENEMY} new enemy
     */
    public ENEMY addEnemyByTypeNew(ENEMY_TYPE enemyType, IMathEnemy mathEnemy, int skinId, long parentEnemyId,
                                   boolean needNearCenter, boolean needFinalSteps, boolean useCustomTrajectories) {
        int skin = (skinId == -1 ? getRandomSkin(enemyType) : skinId);
        boolean needToRush = !needNearCenter && !enemyType.isBoss();
        float speed = needToRush ? getMaxSpeed(enemyType, skin) : generateSpeed(enemyType.getSkin(skin));
        return addItem(enemyType, skin, needToRush ? getInitialTrajectory(speed, needFinalSteps, enemyType)
                        : getTrajectory(enemyType, speed, isNeedStandOnPlace(enemyType, skinId), needNearCenter, skin, needFinalSteps, useCustomTrajectories),
                speed, mathEnemy, parentEnemyId);
    }

    public ENEMY addConcreteEnemy(ENEMY_TYPE enemyType, int skinId, Trajectory trajectory, short numberSeats, IMathEnemy mathEnemy,
                                  long parentEnemyId, boolean needNearCenter, boolean needFinalSteps, boolean useCustomTrajectories) {
        if (trajectory == null) {
            float currentSpeed = generateSpeed(enemyType.getSkin(skinId));
            trajectory = getTrajectory(enemyType, currentSpeed, false, needNearCenter, skinId, needFinalSteps, useCustomTrajectories);
        }
        return addItem(getEnemyByTypeId(enemyType.getId()), skinId, trajectory, (float) trajectory.getSpeed(), mathEnemy, parentEnemyId);
    }

    public ENEMY addConcreteHVEnemy(ENEMY_TYPE enemyType, int skinId, Trajectory sourceTrajectory, IMathEnemy mathEnemy,
                                    long parentEnemyId) {
        float speed = sourceTrajectory != null ? (float) sourceTrajectory.getSpeed() : generateSpeed(enemyType.getSkin(skinId));
        return addItem(enemyType, skinId, getHVTrajectory(speed, sourceTrajectory), speed, mathEnemy, parentEnemyId);
    }

    /**
     * Add item with exists trajectory to map
     * @param enemyType enemyType
     * @param skinId skinId
     * @param trajectory trajectory
     * @param speed speed of enemy
     * @param mathEnemy math enemy
     * @param parentEnemyId parent enemy id
     * @return {@code ENEMY} new enemy
     */
    public ENEMY addItem(ENEMY_TYPE enemyType, int skinId, Trajectory trajectory, float speed,
                         IMathEnemy mathEnemy, long parentEnemyId) {
        ENEMY enemy;
        lockEnemy.lock();
        try {
            enemy = createEnemy(enemyType, skinId, trajectory, speed, mathEnemy, parentEnemyId);
            items.add(enemy);
        } finally {
            lockEnemy.unlock();
        }

        return enemy;
    }

    protected void addItem(ENEMY enemy) {
        lockEnemy.lock();
        try {
            items.add(enemy);
        } finally {
            lockEnemy.unlock();
        }
    }

    @Override
    public void checkFreezeTimeEnemies(int maxFreezeTime) {
        lockEnemy.lock();
        try {
            for (ENEMY enemy : getItems()) {
                enemy.checkFreezeTime(maxFreezeTime);
            }
        } finally {
            lockEnemy.unlock();
        }
    }


    /**
     * Get freeze time for enemies
     * @param maxFreezeTime freeze time. Default FREEZE_TIME_MAX = 3 sec
     * @return {@code Map<Long, Integer> } map of times, key - enemy id, value - freeze time remaining
     */
    public Map<Long, Integer> getAllFreezeTimeRemaining(int maxFreezeTime) {
        Map<Long, Integer> res = new HashMap<>();
        lockEnemy.lock();
        try {
            for (ENEMY enemy : getItems()) {
                if (enemy.lastFreezeTime != 0) {
                    res.put(enemy.getId(), enemy.getFreezeTimeRemaining(maxFreezeTime));
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return res;
    }

    /**
     * Generate trajectory for scarab from SwarmSpawnParams
     * @param params params of swarm
     * @param time time
     * @param speed speed of enemy
     * @return {@code Trajectory} new trajectory
     */
    protected Trajectory getScarabTrajectory(SwarmSpawnParams params, long time, double speed) {
        long timeStart = time + RNG.nextInt(params.getStartDeltaTime());
        Point startPoint = new Point(
                params.getSourceX() + RNG.nextInt(2 * params.getSourceDeltaX()) - params.getSourceDeltaX(),
                params.getSourceY() + RNG.nextInt(2 * params.getSourceDeltaY()) - params.getSourceDeltaY(),
                timeStart);

        int x = params.getTargetX() + RNG.nextInt(2 * params.getTargetDeltaX()) - params.getTargetDeltaX();
        int y = params.getTargetY() + RNG.nextInt(2 * params.getTargetDeltaY()) - params.getTargetDeltaY();

        long timeEnd = timeStart + getTravelTime(startPoint.getX(), startPoint.getY(), x, y, speed);

        return new Trajectory(speed)
                .addPoint(startPoint)
                .addPoint(new Point(x, y, timeEnd));
    }

    protected List<Trajectory> getLocustTrajectories(SwarmSpawnParams params, long time, double speed, int count) {
        double distance = getDistance(params.getSourceX(), params.getSourceY(), params.getTargetX(), params.getTargetY());
        List<PointD> basePoints = getPointsOnLine(params.getSourceX(), params.getSourceY(), params.getTargetX(), params.getTargetY(), distance / 5, distance / 3);
        List<PointD> offsets = getRadialOffsets(3, count, 45);
        List<Trajectory> trajectories = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            trajectories.add(new Trajectory(speed + speed * RNG.rand() * 0.5).addPoint(
                    basePoints.get(0).x + offsets.get(i).x,
                    basePoints.get(0).y + offsets.get(i).y,
                    time + (long) (RNG.rand() * 3000)));
        }
        for (PointD point : basePoints) {
            for (int i = 0; i < trajectories.size(); i++) {
                Trajectory trajectory = trajectories.get(i);
                Point prevPoint = trajectory.getLastPoint();
                double x = point.x + offsets.get(i).x + RNG.rand() * 5;
                double y = point.y + offsets.get(i).y + RNG.rand() * 5;
                trajectory.addPoint(new Point(x, y,
                        prevPoint.getTime() + getTravelTime(prevPoint.getX(), prevPoint.getY(), x, y, trajectory.getSpeed())));
            }
        }
        return trajectories;
    }

    protected List<PointD> getPointsOnLine(double x1, double y1, double x2, double y2, double minStep, double maxStep) {
        List<PointD> result = new ArrayList<>();
        result.add(new PointD(x1, y1));
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double x = x1;
        double y = y1;
        while (getDistance(x, y, x2, y2) > maxStep) {
            double step = minStep + RNG.rand() * (maxStep - minStep);
            x += step * Math.cos(angle);
            y += step * Math.sin(angle);
            result.add(new PointD(x, y));
        }
        result.add(new PointD(x2, y2));
        return result;
    }

    public List<PointD> getRadialOffsets(double radius, int amount, int minAngle) {
        List<PointD> points = new ArrayList<>();
        if (amount <= 6) {
            addPointsOnRing(radius, amount, points, minAngle);
        } else {
            int firstRing = (int) (amount / 2.5);
            int secondRing = amount - firstRing;
            addPointsOnRing(radius, firstRing, points, minAngle);
            addPointsOnRing(2 * radius, secondRing, points, minAngle);
        }
        return points;
    }

    public List<Triple<PointD, Double, Integer>> getRadialOffsetsWithAngles(double radius, int amount, int minAngle) {
        List<Triple<PointD, Double, Integer>> points = new ArrayList<>();
        if (amount <= 6) {
            addPointsOnRingWithAngles(radius, amount, points, minAngle);
        } else {
            int firstRing = (int) (amount / 2.5);
            int secondRing = amount - firstRing;
            addPointsOnRingWithAngles(radius, firstRing, points, minAngle);
            addPointsOnRingWithAngles(2 * radius, secondRing, points, minAngle);
        }
        return points;
    }

    private void addPointsOnRing(double radius, int amount, List<PointD> points, int minAngle) {
        int maxAngle = 360 / amount;
        int angle = 45;
        for (int i = 0; i < amount; i++) {
            points.add(new PointD(radius * MathUtils.cos(angle), -radius * MathUtils.sin(angle)));
            angle += RNG.nextInt(minAngle, maxAngle);
        }
    }

    private void addPointsOnRingWithAngles(double radius, int amount, List<Triple<PointD, Double, Integer>> points, int minAngle) {
        int maxAngle = 360 / amount;
        int angle = 45;
        for (int i = 0; i < amount; i++) {
            PointD pointD = new PointD(radius * MathUtils.cos(angle), -radius * MathUtils.sin(angle));
            points.add(new Triple<>(pointD, radius, angle));
            angle += RNG.nextInt(minAngle, maxAngle);
        }
    }

    protected double getDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    protected long getTravelTime(double x1, double y1, double x2, double y2, double speed) {
        return (long) (getDistance(x1, y1, x2, y2) / (speed / 1000));
    }

    protected Trajectory getTrajectory(ENEMY_TYPE enemyType, float speed, boolean needStandOnPlace,
                                       boolean needNearCenter, int skinId, boolean needFinalSteps,
                                       boolean useCustomTrajectories) {
        return getTrajectory(enemyType, speed, needStandOnPlace, needNearCenter, skinId, needFinalSteps);
    }

    protected Trajectory getTrajectory(ENEMY_TYPE enemyType, float speed, boolean needStandOnPlace,
                                       boolean needNearCenter, int skinId, boolean needFinalSteps) {
        if (enemyType.isBoss()) {
            return getBossTrajectory(speed, needStandOnPlace, skinId);
        } else if (needNearCenter) {
            return isOgre(enemyType) ? getSpecialMiniBossTrajectory(speed, needFinalSteps) : getSpecialTrajectory(speed, needFinalSteps);
        } else {
            return getRandomTrajectory(speed, needFinalSteps);
        }
    }

    protected boolean isOgre(IEnemyType enemyType) {
        return false;
    }

    public List<Integer> getPredefinedTrajectoryIds(int key) {
        return map.getPredefinedTrajectoryIds(key);
    }

    protected Trajectory getRandomTrajectory(double speed, boolean needFinalSteps) {
        long spawnTime = System.currentTimeMillis() + 1000;

        PointI source = getRandomSpawnPoint();

        return new TrajectoryGenerator(map, source, speed).generateWithDuration(spawnTime, getTrajectoryDuration(), needFinalSteps);
    }

    /**
     * Generate random trajectory
     * @param enemyType enemyType
     * @param speed speed of enemy
     * @param minSteps min points
     * @param needFinalSteps need final steps
     * @return {@code Trajectory} new trajectory
     */
    protected Trajectory getRandomTrajectory(IEnemyType enemyType, double speed, short minSteps, boolean needFinalSteps) {
        long spawnTime = System.currentTimeMillis() + 1000;
        PointI source = getRandomSpawnPoint();
        return new TrajectoryGenerator(map, source, speed).generate(spawnTime, minSteps, needFinalSteps);
    }

    protected Trajectory getInitialTrajectory(double speed, boolean needFinalSteps, IEnemyType enemyType) {
        long spawnTime = System.currentTimeMillis() + 1000;
        PointI source = getRandomSpawnPoint();
        return new TrajectoryGenerator(map, source, speed).generate(spawnTime, 7, needFinalSteps);
    }

    protected float getMaxSpeed(IEnemyType enemyType, int skinId) {
        ISkin skin = enemyType.getSkin(skinId);
        return skin.getSpeed() + skin.getSpeedDeltaPositive();
    }

    /**
     * Generate boss trajectory
     * @param speed speed
     * @param needStandOnThePlace need stand on one place
     * @param skinId skin id of boss
     * @return {@code Trajectory} new trajectory
     */
    protected Trajectory getBossTrajectory(double speed, boolean needStandOnThePlace, int skinId) {
        long spawnTime = System.currentTimeMillis() + 2000;
        PointI spawnPoint = getBossSpawnPoint(skinId);
        List<Point> points = new ArrayList<>();
        points.add(new InvulnerablePoint(spawnPoint.x, spawnPoint.y, spawnTime));
        points.add(new Point(spawnPoint.x, spawnPoint.y, spawnTime + getBossInvulnerabilityTime(skinId)));

        return getBossTrajectoryGenerator(spawnPoint, speed, needStandOnThePlace).generate(new Trajectory(speed, points),
                spawnTime + getBossSpawnAnimationDuration(skinId), 70, false);
    }

    protected TrajectoryGenerator getBossTrajectoryGenerator(PointI spawnPoint, double speed, boolean needStandOnPlace) {
        return needStandOnPlace
                ? new StandOnPlaceTrajectoryGenerator(map, spawnPoint, speed)
                : new TrajectoryGenerator(map, spawnPoint, speed);
    }

    protected long getBossSpawnAnimationDuration(int skinId) {
        return BOSS_SPAWN_ANIMATION_DURATION;
    }

    protected long getBossInvulnerabilityTime(int skinId) {
        return BOSS_INVULNERABILITY_TIME;
    }

    protected PointI getBossSpawnPoint(int skinId) {
        return getMapId() == 202 && skinId == 3 ? new PointI(54, 56) : map.getBossSpawnPoint();
    }

    protected Trajectory getSpecialMiniBossTrajectory(double speed, boolean needFinalSteps) {
        Pair<Long, PointI> spawnTimeAndPoint = getSpecialSpawnTimeAndPoint();
        PointI bossSpawnPoint = map.getBossSpawnPoint();
        long spawnTime = spawnTimeAndPoint.getKey();
        PointI spawnPoint = spawnTimeAndPoint.getValue();
        int x = spawnPoint.x;
        int y = spawnPoint.y;
        int shiftX;
        int shiftY;
        while (map.isWall(x, y) || map.isBorder(x, y)) {
            shiftX = RNG.nextInt(15);
            shiftY = RNG.nextInt(15);
            x = RNG.nextBoolean() ? bossSpawnPoint.x + shiftX : bossSpawnPoint.x - shiftX;
            y = RNG.nextBoolean() ? bossSpawnPoint.y + shiftY : bossSpawnPoint.y - shiftY;
        }

        spawnPoint = new PointI(x, y);
        List<Point> points = new ArrayList<>();
        points.add(new Point(spawnPoint.x, spawnPoint.y, spawnTime));

        return new JumpTrajectoryGenerator(map, spawnPoint, speed).generate(new Trajectory(speed, points),
                spawnTime + 1500, 25, needFinalSteps);
    }

    private Trajectory getSpecialTrajectory(double speed, boolean needFinalSteps) {
        Pair<Long, PointI> spawnTimeAndPoint = getSpecialSpawnTimeAndPoint();
        long spawnTime = spawnTimeAndPoint.getKey();
        PointI spawnPoint = spawnTimeAndPoint.getValue();
        List<Point> points = new ArrayList<>();
        points.add(new Point(spawnPoint.x, spawnPoint.y, spawnTime));

        return new TrajectoryGenerator(map, spawnPoint, speed).generate(new Trajectory(speed, points),
                spawnTime + 1500, 25, needFinalSteps);

    }

    protected Pair<Long, PointI> getSpecialSpawnTimeAndPoint() {
        long spawnTime = System.currentTimeMillis() + 2000;
        PointI spawnPoint = map.getBossSpawnPoint();
        int shiftX = RNG.nextInt(10);
        int shiftY = RNG.nextInt(10);
        int x = RNG.nextBoolean() ? spawnPoint.x + shiftX : spawnPoint.x - shiftX;
        int y = RNG.nextBoolean() ? spawnPoint.y + shiftY : spawnPoint.y - shiftY;
        return new Pair<>(spawnTime, new PointI(x, y));
    }

    private Trajectory getHVTrajectory(double speed, Trajectory sourceTrajectory) {
        List<Point> points = sourceTrajectory.getPoints();
        Trajectory trajectoryAdditional;
        Trajectory trajectoryTotal = new Trajectory(sourceTrajectory.getSpeed());

        getLogger().debug("old points: {}, getHVTrajectory", points);
        Point currentPoint = getCurrentPoint(System.currentTimeMillis(), points);
        getLogger().debug("currentPoint: {}", currentPoint);

        trajectoryAdditional =
                new TrajectoryGenerator(map, new PointI((int) currentPoint.getX(), (int) currentPoint.getY()), speed)
                        .generateWithoutFirstStepWithDuration(currentPoint.getTime(), getTrajectoryDuration());

        for (Point trajectoryPoint : trajectoryAdditional.getPoints()) {
            trajectoryTotal.addPoint(trajectoryPoint);
        }
        return trajectoryTotal;
    }

    /**
     * Get current point from trajectory by current time
     * @param time current time
     * @param points list of points
     * @return current point of enemy on map
     */
    public Point getCurrentPoint(long time, List<Point> points) {
        if (points.get(0).getTime() > time) {
            return points.get(0);
        }
        Point lastPoint = points.get(points.size() - 1);
        if (time >= lastPoint.getTime()) {
            return lastPoint;
        }
        int i = 1;
        while (i < points.size() && time > points.get(i).getTime()) {
            i++;
        }
        Point a = points.get(i - 1);
        Point b = points.get(i);
        double percent = ((double) (time - a.getTime())) / (b.getTime() - a.getTime());
        double x = a.getX() + (b.getX() - a.getX()) * percent;
        double y = a.getY() + (b.getY() - a.getY()) * percent;
        Point point = new Point(x, y, System.currentTimeMillis());
        return point;

    }

    protected int getRandomSkin(ENEMY_TYPE enemyType) {
        int skinsCount = enemyType.getSkins().size();
        if (skinsCount <= 1) {
            return 1;
        } else {
            return RNG.nextInt(skinsCount) + 1;
        }
    }

    /**
     * Get random spawn point on map
     * @return {@code PointI} random spawn point
     */
    protected PointI getRandomSpawnPoint() {
        return getRandomElement(map.getSpawnPoints());
    }

    protected PointI getRandomLargeEnemiesSpawnPoint() {
        return getRandomElement(map.getLargeEnemiesSpawnPoints());
    }

    /**
     * Remove item from map
     * @param itemId enemyId
     */
    public void removeItem(Long itemId) {
        lockEnemy.lock();
        try {
            ENEMY enemy = getItemById(itemId);
            if (enemy != null) {
                if (enemy.isPartOfSwarm()) {
                    removeEnemyFromSwarm(enemy);
                }
                items.remove(enemy);
            }
        } finally {
            lockEnemy.unlock();
        }
    }

    public Map<Long, Integer> getPossibleItemsId() {
        Map<Long, Integer> res = new HashMap<>();
        lockEnemy.lock();
        try {
            for (ENEMY enemy : getItems()) {
                res.put(enemy.getId(), enemy.getEnemyClass().getEnemyType().getId());
            }
        } finally {
            lockEnemy.unlock();
        }
        return res;
    }

    /**
     * Update enemies on map. Enemies with completed trajectories will be removed from the map.
     */
    public void update() {
        lockEnemy.lock();
        try {
            List<ENEMY> deadEnemies = new ArrayList<>();
            for (ENEMY enemy : getItems()) {
                if (enemy.isMovable() && enemy.update()) {
                    if (enemy.shouldReturn) {
                        removedEnemies.put(enemy.getId(), enemy);
                    }
                    if (enemy.isPartOfSwarm()) {
                        Swarm<ENEMY> swarm = swarms.get(enemy.getSwarmId());
                        if (swarm != null && swarm.incrementRemovedAndUpdate() && !swarm.isShouldReturn()) {
                            swarms.remove(enemy.getSwarmId());
                        }
                    }
                    if (enemy.shouldReturn && !enemy.isPartOfSwarm()) {
                        enemy.setReturnTime(System.currentTimeMillis() + enemy.getRespawnDelay());
                    }
                    deadEnemies.add(enemy);
                }
            }
            if (!deadEnemies.isEmpty()) {
                items.removeAll(deadEnemies);
            }
        } finally {
            lockEnemy.unlock();
        }
    }

    public List<Integer> updateAndReturnListTypes() {
        List<Integer> listTypesOfRemovedEnemies = new ArrayList<>();
        lockEnemy.lock();
        try {
            List<ENEMY> deadEnemies = new ArrayList<>();
            for (ENEMY enemy : getItems()) {
                if (enemy.isMovable() && enemy.update()) {
                    if (enemy.shouldReturn) {
                        removedEnemies.put(enemy.getId(), enemy);
                    }
                    if (enemy.isPartOfSwarm()) {
                        Swarm<ENEMY> swarm = swarms.get(enemy.getSwarmId());
                        if (swarm != null && swarm.incrementRemovedAndUpdate() && !swarm.isShouldReturn()) {
                            swarms.remove(enemy.getSwarmId());
                        }
                    }
                    if (enemy.shouldReturn && !enemy.isPartOfSwarm()) {
                        enemy.setReturnTime(System.currentTimeMillis() + enemy.getRespawnDelay());
                    }
                    deadEnemies.add(enemy);
                    listTypesOfRemovedEnemies.add(enemy.getEnemyClass().getEnemyType().getId());
                }
            }
            if (!deadEnemies.isEmpty()) {
                items.removeAll(deadEnemies);
            }
        } finally {
            lockEnemy.unlock();
        }
        return listTypesOfRemovedEnemies;
    }

    public void updateEnemyLimits(IEnemyLimitChecker<ENEMY> limitChecker) {
        lockEnemy.lock();
        try {
            limitChecker.reset();
            getItems().forEach(limitChecker::countEnemy);
        } finally {
            lockEnemy.unlock();
        }
    }


    @Override
    public boolean relaxedRange() {
        return false;
    }

    @Override
    public boolean canMakeStep(PointI source, PointI target) {
        return false;
    }

    @Override
    public double getStepCost(PointI source, PointI target) {
        int dx = source.x - target.x;
        int dy = source.y - target.y;
        // No need to calculate sqrt, because for each a > 0 and b > 0: sqrt(a) > sqrt(b) <=> a > b
        return dx * dx + dy * dy;
    }

    @Override
    public boolean isWall(double x, double y) {
        return x < 0 || y < 0 || x >= width || y >= height || map.isWall((int) x, (int) y);
    }

    public void setMapShape(GameMapShape shape) {
        map = shape;
        mapId = map.getId();
        coords = null;
    }

    @Override
    public MAP_SHAPE getMapShape() {
        return (MAP_SHAPE) map;
    }

    @Override
    public int getId() {
        return mapId;
    }

    public int getMapId() {
        return mapId;
    }

    /**
     * Generate random shot trajectories for leave enemies from map in end of round.
     * @return {@code Map<Long, Trajectory> }  map of short leave trajectories for each enemy
     */
    public Map<Long, Trajectory> generateShortLeaveTrajectories() {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        lockEnemy.lock();
        try {
            long startTime = System.currentTimeMillis();
            ITrajectoryGenerator generator = new WaypointTrajectoryGenerator(map);
            for (ENEMY enemy : getItems()) {
                PointI location = enemy.getLocation(startTime).toPointI();
                Trajectory trajectory = generateLeaveTrajectory(generator, location, startTime, enemy);
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

    protected Trajectory generateLeaveTrajectory(ITrajectoryGenerator generator, PointI location, long startTime, ENEMY enemy) {
        if (!map.isValid(location.x, location.y)) {
            getLogger().debug("generateShortLeaveTrajectories skipped as enemy {} stands outside of the map: {}", enemy.getId(), location);
            return new Trajectory(enemy.getSpeed(), Collections.singletonList(new Point(location.x, location.y, startTime)));
        }
        try {
            return generator.generate(location.x, location.y, startTime, startTime + 10000, enemy.speed);
        } catch (Exception e) {
            getLogger().debug("generateShortLeaveTrajectories failed from location {}, error in enemy: {}", location, enemy, e);
            return null;
        }
    }

    protected Trajectory generateLeaveTrajectoryBySpeed(ITrajectoryGenerator generator, PointI location, long startTime, double speed) {
        if (!map.isValid(location.x, location.y)) {
            getLogger().debug("generateLeaveTrajectoryBySpeed skipped as stands outside of the map: {}",  location);
            return new Trajectory(speed, Collections.singletonList(new Point(location.x, location.y, startTime)));
        }
        try {
            return generator.generate(location.x, location.y, startTime, startTime + 10000, speed);
        } catch (Exception e) {
            getLogger().debug("generateLeaveTrajectoryBySpeed failed from location {}, error in ", location,  e);
            return null;
        }
    }

    /**
     * Get current location by time from points of enemy
     * @param time current time
     * @param points points of enemy
     * @return {@code PointD} current point of enemy
     */
    public PointD getLocation(long time, List<Point> points) {
        if (points.get(0).getTime() > time) {
            return new PointD(points.get(0).getX(), points.get(0).getY());
        }
        Point lastPoint = points.get(points.size() - 1);
        if (time >= lastPoint.getTime()) {
            return new PointD(lastPoint.getX(), lastPoint.getY());
        }
        int i = 1;
        while (i < points.size() && time > points.get(i).getTime()) {
            i++;
        }
        Point a = points.get(i - 1);
        Point b = points.get(i);
        double percent = ((double) (time - a.getTime())) / (b.getTime() - a.getTime());
        return new PointD(a.getX() + (b.getX() - a.getX()) * percent, a.getY() + (b.getY() - a.getY()) * percent);
    }

    /**
     * Generate update trajectories for all enemies
     * @param needFinalSteps true if trajectory should be with final steps
     * @return {@code Map<Long, Trajectory> }  map of update trajectories for each enemy
     */
    public Map<Long, Trajectory> generateUpdateTrajectories(boolean needFinalSteps) {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        lockEnemy.lock();
        try {
            long startTime = System.currentTimeMillis();
            for (ENEMY enemy : getItems()) {
                if (needFinalSteps && !enemy.isBoss())
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

                    TrajectoryGenerator trajectoryGenerator;
                    if (isNeedStandOnPlace(enemy.getEnemyClass().getEnemyType(), enemy.getSkin())) {
                        trajectoryGenerator = new StandOnPlaceTrajectoryGenerator(map, getBossSpawnPoint(enemy.getSkin()), speed);
                    } else {
                        boolean ogre = isOgre(enemy.getEnemyClass().getEnemyType());
                        trajectoryGenerator = ogre
                                ? new JumpTrajectoryGenerator(map, location, speedNew)
                                : new TrajectoryGenerator(map, location, speedNew);
                    }
                    Trajectory trajectory = trajectoryGenerator.generateWithDuration(startTime,
                            getTrajectoryDuration(), false);
                    enemy.setTrajectory(trajectory);
                    trajectories.put(enemy.getId(), trajectory);
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return trajectories;
    }

    public Map<Long, Trajectory> generateCustomUpdateTrajectories(IEnemyRange range, long duration, boolean needFinalSteps) {
        Map<Long, Trajectory> trajectories = new HashMap<>();
        lockEnemy.lock();
        try {
            long startTime = System.currentTimeMillis();
            for (ENEMY enemy : getItems()) {
                IEnemyType enemyType = enemy.getEnemyClass().getEnemyType();
                boolean enemyIsInRange = range.getEnemies().contains(enemyType);
                if (!enemyIsInRange) {
                    continue;
                }

                getLogger().debug("generateCustomUpdateTrajectories, processing enemyId: {}, " +
                                "name: {}, range.getEnemies(): {}, enemyType: {}, enemyIsInRange: {} ",
                        enemy.getId(), enemy.getEnemyClass().getEnemyType().getName(), range.getEnemies(), enemyType, enemyIsInRange);

                PointI location = enemy.getLocation(startTime).toPointI();
                List<Point> points = new ArrayList<>();

                boolean needDoubleSpeed = needDoubleSpeed(enemy.getEnemyClass().getEnemyType().getId());
                float speed = generateSpeed(enemy.getEnemyClass().getEnemyType().getSkin(enemy.getSkin()));
                double speedNew = (needDoubleSpeed && RNG.nextBoolean()) ? speed * 3 : speed;
                Trajectory trajectory;
                if (isNeedStandOnPlace(enemy.getEnemyClass().getEnemyType(), enemy.getSkin())) {
                    trajectory = getBossTrajectory(speed, true, enemy.getSkin());
                    points.addAll(trajectory.getPoints());
                } else {
                    points.add(new Point(location.x, location.y, startTime));
                    trajectory = new TrajectoryGenerator(map, location, speedNew)
                            .generateWithDuration(startTime + duration, getTrajectoryDuration(), needFinalSteps);
                    points.addAll(trajectory.getPoints());
                }
                if (trajectory != null) {
                    Trajectory trajectoryNew = new Trajectory(trajectory.getSpeed(), points);
                    getLogger().debug("trajectory: {} ", trajectory);
                    getLogger().debug("trajectoryNew: {} ", trajectoryNew);
                    enemy.setTrajectory(trajectoryNew);
                    trajectories.put(enemy.getId(), trajectoryNew);
                }

            }
        } finally {
            lockEnemy.unlock();
        }
        return trajectories;
    }


    public abstract boolean needDoubleSpeed(int enemyTypeId);

    /**
     * Generate freeze trajectories for all enemies near shot
     * @param time current time
     * @param freezeTime freeze time
     * @param xShot x coordinate  of shot
     * @param yShot y coordinate  of shot
     * @param maxDist dist of freeze for shot
     * @return {@code Map<Long, Trajectory> }  map of freeze trajectories for enemies
     */
    @Override
    public Map<Long, Trajectory> generateFreezeTrajectories(long time, int freezeTime, double xShot, double yShot, int maxDist) {
        Map<Long, Trajectory> trajectories = new HashMap<>();

        lockEnemy.lock();
        try {
            for (ENEMY enemy : getItems()) {
                PointI location = enemy.getLocation(time).toPointI();

                if (!shouldIgnoreFreeze(enemy, location, time)
                        && map.isValid(location.x, location.y)
                        && calcDistance(location, xShot, yShot) <= maxDist) {

                    ArrayList<Point> points = new ArrayList<>();
                    points.add(new FreezePoint(location.x, location.y, time));
                    points.add(new FreezePoint(location.x, location.y, time + freezeTime));

                    if (getMapId() == 202 && enemy.isBoss() && enemy.getSkin() == 3) {
                        generateFreezePoints(enemy.getTrajectory(), time, points, time + freezeTime);
                    } else {
                        addPointsFromOldTrajectory(points, time, freezeTime, location, enemy);
                    }

                    Trajectory trajectory = new Trajectory(enemy.speed, points);
                    enemy.setTrajectory(trajectory);
                    trajectories.put(enemy.getId(), trajectory);

                    enemy.setLastFreezeTime(time);
                    getLogger().debug("generateFreezeTrajectories enemy: {}", enemy);
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return trajectories;
    }

    protected double calcDistance(PointI location, double shotX, double shotY) {
        double screenX = getCoords().toScreenX(location.x, location.y);
        double screenY = getCoords().toScreenY(location.x, location.y);
        return Math.sqrt(Math.pow((shotX - screenX), 2) + Math.pow((shotY - screenY), 2));
    }

    protected boolean shouldIgnoreFreeze(ENEMY enemy, PointI location, long time) {
        if (enemy.isInvulnerable(time)) {
            return true;
        }
        Point firstPoint = enemy.getTrajectory().getPoints().get(0);
        boolean isStartPoint = location.x == firstPoint.getX() && location.y == firstPoint.getY();
        return isStartPoint && notNeedFreezeAtStartPoint(enemy.getEnemyClass().getEnemyType());
    }

    protected void addPointsFromOldTrajectory(List<Point> points, long startTime, long freezeTime, PointI location, ENEMY enemy) {
        List<Point> oldPoints = enemy.getTrajectory().getPoints();
        long time = startTime + freezeTime;
        int x = location.x;
        int y = location.y;
        for (Point point : oldPoints) {
            if (!point.isFreezePoint() && (point.getTime() > startTime) && !isBossAtTheStartPoint(enemy, point, location)) {
                PointI pointI = (new PointD(point.getX(), point.getY())).toPointI();
                int nx = pointI.x;
                int ny = pointI.y;
                time += (long) (Math.max(Math.abs(nx - x), Math.abs(ny - y)) / (enemy.speed / 1000));
                points.add(point.create(point.getX(), point.getY(), time));
                x = nx;
                y = ny;
            }
        }
    }

    private boolean isBossAtTheStartPoint(ENEMY enemy, Point point, PointI location) {
        return enemy.getEnemyClass().getEnemyType().isBoss()
                && point.getX() == location.x
                && point.getY() == location.y;
    }

    protected void generateFreezePoints(Trajectory oldTrajectory, long startTime, ArrayList<Point> points, long time) {
    }

    public boolean notNeedFreezeAtStartPoint(IEnemyType enemyType) {
        return false;
    }

    public boolean isNeedStandOnPlace(IEnemyType enemyType, int skinId) {
        return false;
    }

    public Map<ENEMY, Long> getInactivityLiveItems() {
        return inactivityLiveItems == null ? new HashMap<>() : inactivityLiveItems;
    }

    public void clearInactivityLiveItems() {
        lockEnemy.lock();
        try {
            inactivityLiveItems.clear();
        } finally {
            lockEnemy.unlock();
        }
    }

    protected boolean checkCooldown(int id) {
        Long cooldown = scenarioCooldowns.get(id);
        return cooldown == null || cooldown < System.currentTimeMillis();
    }

    /**
     * Return random element from list
     * @param list
     * @return random element
     * @param <T> type of element
     */
    protected <T> T getRandomElement(List<T> list) {
        return list.get(RNG.nextInt(list.size()));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameMap[");
        sb.append("width=").append(width);
        sb.append(", height=").append(height);
        sb.append(", items=").append(items);
        sb.append(", inactivityLiveItems=").append(inactivityLiveItems);
        sb.append(", possibleEnemies=").append(getPossibleEnemies());
        sb.append(']');
        return sb.toString();
    }

    protected abstract void serializeEnemies(JsonGenerator gen, SerializerProvider serializers) throws IOException;

    protected abstract void deserializeEnemies(JsonParser p, JsonNode node, DeserializationContext ctxt);

    protected void serializeAdditionalFields(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        //nop by default
    }

    protected void deserializeAdditionalFields(JsonParser p, JsonNode node, DeserializationContext ctxt) {
        //nop by default
    }

    protected abstract GM getDeserialized();


    protected abstract void writeEnemies(Kryo kryo, Output output);

    protected abstract void readEnemies(byte version, Kryo kryo, Input input);

    protected void writeAdditionalFields(Kryo kryo, Output output) {
        //nop by default
    }

    protected void readAdditionalFields(byte version, Kryo kryo, Input input) {
        //nop by default
    }

    @Override
    public void serializeObject(JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumberField("width", width);
        gen.writeNumberField("width", width);
        serializeEnemies(gen, serializers);
        gen.writeNumberField("enemyIdsGenerator", enemyIdsGenerator.get());
        gen.writeNumberField("mapId", mapId);
        serializeMapField(gen, "scenarioCooldowns", scenarioCooldowns, new TypeReference<Map<Integer, Long>>() {});
        serializeAdditionalFields(gen, serializers);
        serializeMapField(gen, "swarms", swarms, new TypeReference<Map<Integer, Swarm<ENEMY>>>() {});
        gen.writeNumberField("swarmIdGenerator", swarmIdGenerator.get());
        serializeMapField(gen, "removedEnemies", removedEnemies, new TypeReference<Map<Long, ENEMY>>() {});
        gen.writeNumberField("portalIdGenerator", portalIdGenerator.get());
        gen.writeNumberField("portalCooldown", portalCooldown);
        serializeMapField(gen, "trajectoryCooldowns", trajectoryCooldowns, new TypeReference<Map<Integer, Long>>() {});
    }

    @Override
    public GM deserializeObject(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        width = node.get("width").shortValue();
        height = node.get("height").shortValue();
        deserializeEnemies(p, node, ctxt);
        enemyIdsGenerator = new AtomicInteger(node.get("enemyIdsGenerator").intValue());
        mapId = node.get("mapId").intValue();
        if (context != null) { //may be null on xstream serialization tests
            GameMapStore gameMapStore = context.getBean("gameMapStore", GameMapStore.class);
            map = gameMapStore.getMap(mapId);
        }
        scenarioCooldowns = ((ObjectMapper) p.getCodec()).convertValue(node.get("scenarioCooldowns"), new TypeReference<Map<Integer, Long>>() {});
        deserializeAdditionalFields(p, node, ctxt);
        swarms = ((ObjectMapper) p.getCodec()).convertValue(node.get("scenarioCooldowns"), new TypeReference<Map<Integer, Swarm<ENEMY>>>() {});
        swarmIdGenerator = new AtomicInteger(node.get("swarmIdGenerator").intValue());
        removedEnemies = ((ObjectMapper) p.getCodec()).convertValue(node.get("scenarioCooldowns"), new TypeReference<Map<Long, ENEMY>>() {});
        portalIdGenerator = new AtomicInteger(node.get("portalIdGenerator").intValue());
        portalCooldown = node.get("portalCooldown").longValue();
        trajectoryCooldowns = ((ObjectMapper) p.getCodec()).convertValue(node.get("scenarioCooldowns"), new TypeReference<Map<Integer, Long>>() {});;

        return getDeserialized();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeShort(width);
        output.writeShort(height);
        writeEnemies(kryo, output);
        output.writeInt(enemyIdsGenerator.get(), true);
        output.writeInt(mapId, true);
        kryo.writeClassAndObject(output, scenarioCooldowns);
        writeAdditionalFields(kryo, output);
        kryo.writeClassAndObject(output, swarms);
        output.writeInt(swarmIdGenerator.get(), true);
        kryo.writeClassAndObject(output, removedEnemies);
        output.writeInt(portalIdGenerator.get(), true);
        output.writeLong(portalCooldown, true);
        kryo.writeClassAndObject(output, trajectoryCooldowns);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void read(Kryo kryo, Input input) {
        byte version = input.readByte();
        width = input.readShort();
        height = input.readShort();
        readEnemies(version, kryo, input);
        enemyIdsGenerator = new AtomicInteger(input.readInt(true));
        mapId = input.readInt(true);
        if (context != null) { //may be null on xstream serialization tests
            GameMapStore gameMapStore = context.getBean("gameMapStore", GameMapStore.class);
            map = gameMapStore.getMap(mapId);
        }
        scenarioCooldowns = (Map<Integer, Long>) kryo.readClassAndObject(input);
        readAdditionalFields(version, kryo, input);
        swarms = (Map<Integer, Swarm<ENEMY>>) kryo.readClassAndObject(input);
        swarmIdGenerator = new AtomicInteger(input.readInt(true));
        removedEnemies = (Map<Long, ENEMY>) kryo.readClassAndObject(input);
        portalIdGenerator = new AtomicInteger(input.readInt(true));
        portalCooldown = input.readLong(true);
        trajectoryCooldowns = (Map<Integer, Long>) kryo.readClassAndObject(input);
    }

    public int getTrajectoryDuration() {
        return TRAJECTORY_DURATION;
    }

    protected Trajectory shiftTrajectory(Trajectory source, double minDx, double maxDx, double minDy, double maxDy, long dt) {
        Trajectory trajectory = new Trajectory(source.getSpeed());
        for (Point point : source.getPoints()) {
            trajectory.addPoint(
                    point.getX() + minDx + RNG.rand() * (maxDx - minDx),
                    point.getY() + minDy + RNG.rand() * (maxDy - minDy),
                    point.getTime() + (long) (RNG.rand() * dt));
        }
        return trajectory;
    }

    public Trajectory shiftTrajectory(Trajectory source, double dx, double dy, long dt) {
        Trajectory trajectory = new Trajectory(source.getSpeed());
        for (Point point : source.getPoints()) {
            trajectory.addPoint(
                    point.getX() + dx,
                    point.getY() + dy,
                    point.getTime() + dt);
        }
        return trajectory;
    }

    public void registerSwarm(int swarmId, List<ENEMY> enemies) {
        registerSwarm(swarmId, enemies, false, 0);
    }

    protected void registerSwarm(int swarmId, List<ENEMY> enemies, boolean shouldReturn, int respawnDelay) {
        lockEnemy.lock();
        try {
            this.swarms.put(swarmId, new Swarm<>(enemies, shouldReturn, respawnDelay));
        } finally {
            lockEnemy.unlock();
        }
    }

    protected void removeEnemyFromSwarm(ENEMY enemy) {
        int swarmId = enemy.getSwarmId();
        Swarm<ENEMY> swarm = swarms.get(swarmId);
        if (swarm != null && swarm.getEnemyIds().remove(enemy.getId()) && swarm.getEnemyIds().size() == 0) {
            swarms.remove(swarmId);
        }
    }

    public int swarmCount(ISwarmType swarmType) {
        lockEnemy.lock();
        try {
            int swarmTypeId = swarmType.getTypeId();
            return (int) swarms.values().stream()
                    .filter(swarm -> swarm.getSwarmType() == swarmTypeId)
                    .count();
        } finally {
            lockEnemy.unlock();
        }
    }

    public int swarmCount(ISwarmType... swarmTypes) {
        return Arrays.stream(swarmTypes).mapToInt(this::swarmCount).sum();
    }

    public int swarmCount() {
        return swarms.size();
    }

    public boolean isEnemyRemoved(ENEMY_TYPE enemyType) {
        for (ENEMY enemy : removedEnemies.values()) {
            if (enemy.getEnemyClass().getEnemyType().equals(enemyType)) {
                return true;
            }
        }
        return false;
    }

    protected void updatePortalCooldown(Trajectory trajectory) {
        long portalCloseTime = trajectory.getPoints().get(1).getTime();
        portalCooldown = portalCloseTime + 2000;
    }


    public void addEnemiesToMap(List<ENEMY> enemies) {
        lockEnemy.lock();
        try {
            //items.clear();
            inactivityLiveItems.clear();
            swarms.clear();
            items.addAll(enemies);
        } finally {
            lockEnemy.unlock();
        }
    }

    public int getScenarioSize(SpawnScenario scenario) {
        int result = 0;
        for (SpawnGroup group : scenario.getGroups()) {
            if (group.isNeedRetinueEnemies()) {
                result += 7;
            } else {
                result += group.getMaxSize();
            }
        }
        return result;
    }

    public int generateSwarmId() {
        return swarmIdGenerator.getAndDecrement();
    }

    @Override
    public Map<Long, Integer> getAdditionalEnemyModes() {
        Map<Long, Integer> res = new HashMap<>();

        lockEnemy.lock();
        try {
            for (ENEMY enemy : items) {
                if (!enemy.getEnemyMode().equals(EnemyMode.X_1)) {
                    res.put(enemy.getId(), enemy.getEnemyMode().ordinal());
                }
            }
        } finally {
            lockEnemy.unlock();
        }
        return res;
    }

    public Coords getCoords() {
        if (coords == null) {
            coords = new Coords(SCREEN_WIDTH, SCREEN_HEIGHT, map.getWidth(), map.getHeight());
        }
        return coords;
    }

    public List<ENEMY> getSwarmEnemies(Swarm<ENEMY> swarm) {
        List<ENEMY> enemies = new ArrayList<>();
        for (long enemyId : swarm.getEnemyIds()) {
            ENEMY enemy = getItemById(enemyId);
            if (enemy != null) {
                enemies.add(enemy);
            }
        }
        return enemies;
    }

    public Map<Integer, Integer> countEnemyTypes() {
        Map<Integer, Integer> result = new HashMap<>();
        lockEnemy.lock();
        try {
            for (ENEMY enemy : items) {
                int id = enemy.getEnemyClass().getEnemyType().getId();
                result.put(id, result.getOrDefault(id, 0) + 1);
            }
            for (ENEMY enemy : removedEnemies.values()) {
                int id = enemy.getEnemyClass().getEnemyType().getId();
                result.put(id, result.getOrDefault(id, 0) + 1);
            }
        } finally {
            lockEnemy.unlock();
        }
        return result;
    }

    public boolean isValid(int x, int y) {
        return map.isValid(x, y);
    }

    public boolean isVisible(long time, ENEMY enemy) {
        return true;
    }

    public boolean isShotNearCenter(IShot shot) {
        return calcDistance(map.getCenter().toPointI(), shot.getX(), shot.getY())
                < (RNG.nextBoolean() ? SCREEN_WIDTH : SCREEN_HEIGHT) * RNG.nextInt(10, 16) * 0.01;
    }

    public boolean isValidPointForFlyingEnemies(int x, int y) {
        double screenX = getCoords().toScreenX(x, y);
        double screenY = getCoords().toScreenY(x, y);
        return screenX >= 0 && screenY >= 0 && screenX < SCREEN_WIDTH && screenY < SCREEN_HEIGHT;
    }
}
