package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.common.scenarios.SpawnScenario;
import com.betsoft.casino.mp.model.IEnemyType;
import com.betsoft.casino.mp.model.IGameMapShape;
import com.betsoft.casino.mp.model.MoveDirection;
import com.betsoft.casino.mp.model.movement.Point;
import com.betsoft.casino.mp.model.movement.Trajectory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameMapShape implements IGameMapShape {

    private static final Logger LOG = LogManager.getLogger(GameMapShape.class);

    private static final byte ISO_DEFAULT_MAP_SIZE = 96;
    private static final byte POV_DEFAULT_MAP_SIZE = 120;

    public static final byte FREE_MASK = 0;
    public static final byte WALL_MASK = 1;
    public static final byte SPAWN_POINT_MASK = 2;
    public static final byte BORDER_MASK = 4;
    public static final byte MARKED_MASK = WALL_MASK | SPAWN_POINT_MASK | BORDER_MASK;
    public static final byte BOSS_MASK = 8;
    public static final byte LARGE_ENEMIES_WALL_MASK = 16;
    public static final byte WAYPOINT_A = 32;
    public static final byte WAYPOINT_B = 64;

    private static final int SWARM_TRAJECTORIES_OFFSET = 10000;

    private final String fileName;
    private final byte version;
    private final short width;
    private final short height;
    private final byte map[][];

    private List<PointI> cachedSpawnPoints;
    private List<PointI> cashedLargeEnemiesSpawnPoints;
    private PointI bossSpawnPoint;
    private PointD center;
    // Key is SwarmType Id
    private Map<Integer, List<SwarmSpawnParams>> swarmSpawnParams;
    private Map<Integer, Trajectory> trajectories;
    private Map<Integer, List<Trajectory>> predefinedTrajectories;
    private Map<Integer, List<Integer>> predefinedTrajectoryIds;
    private List<SpawnScenario> scenarios;
    private List<SwarmParams> swarmParams;
    private List<Portal> portals;
    private boolean disableRandomTrajectories;
    private Map<String, List<PointD>> points;
    private Map<Integer, List<GroupParams>> predefinedGroups;

    private int id;

    //test only!
    public GameMapShape(int id, byte width, byte height) {
        this.version = 0;
        this.fileName = "test";
        this.id = id;
        this.width = width;
        this.height = height;
        this.map = new byte[height][width];
    }

    public GameMapShape(String fileName, DataInputStream source) throws IOException {
        try {
            this.fileName = fileName;
            byte firstByte = source.readByte();
            if (firstByte == ISO_DEFAULT_MAP_SIZE || firstByte == POV_DEFAULT_MAP_SIZE) {
                version = 0;
                width = firstByte;
                height = source.readByte();
            } else {
                version = firstByte;
                width = readShort(source);
                height = readShort(source);
            }
            map = new byte[height][width];
            for (int i = 0; i < height; i++) {
                source.readFully(map[i]);
            }
            updateSpawnPoints();
        } catch (IOException e) {
            LOG.error("Failed to load map shape", e);
            throw e;
        }
    }

    /**
     * Custom 7-bit format used as bytes with first bit set in resource files are represented as two bytes
     */
    private short readShort(DataInputStream stream) throws IOException {
        byte[] bytes = new byte[2];
        stream.readFully(bytes);
        return (short) (128 * (bytes[0] & 0x7f) + bytes[1]);
    }

    @Override
    public void updateSpawnPoints() {
        cachedSpawnPoints = new ArrayList<>();
        cashedLargeEnemiesSpawnPoints = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (isSpawnPoint(x, y)) {
                    cachedSpawnPoints.add(new PointI(x, y));
                    if (!isWallForLargeEnemies(x, y)) {
                        cashedLargeEnemiesSpawnPoints.add(new PointI(x, y));
                    }
                }
            }
        }
    }

    public MoveDirection getMoveDirection(int x, int y) {
        boolean a = (map[y][x] & WAYPOINT_A) == WAYPOINT_A;
        boolean b = (map[y][x] & WAYPOINT_B) == WAYPOINT_B;
        return a
                ? b ? MoveDirection.LEFT : MoveDirection.RIGHT
                : b ? MoveDirection.TOP : MoveDirection.BOTTOM;
    }

    public void setMoveDirection(int x, int y, MoveDirection direction) {
        switch (direction) {
            case LEFT:
                map[y][x] |= WAYPOINT_A;
                map[y][x] |= WAYPOINT_B;
                break;
            case RIGHT:
                map[y][x] |= WAYPOINT_A;
                map[y][x] &= ~WAYPOINT_B;
                break;
            case TOP:
                map[y][x] &= ~WAYPOINT_A;
                map[y][x] |= WAYPOINT_B;
                break;
            case BOTTOM:
                map[y][x] &= ~WAYPOINT_A;
                map[y][x] &= ~WAYPOINT_B;
                break;
        }
    }

    @Override
    public short getWidth() {
        return width;
    }

    @Override
    public short getHeight() {
        return height;
    }

    @Override
    public boolean isMasked(int x, int y, byte mask) {
        return isValid(x, y) && (map[y][x] & mask) == mask;
    }

    @Override
    public boolean isPassable(int x, int y) {
        return isValid(x, y) && (map[y][x] & WALL_MASK) == 0;
    }

    public boolean isPassable(PointI point) {
        return isValid(point.x, point.y) && (map[point.y][point.x] & WALL_MASK) == 0;
    }

    public boolean isPassableForLargeEnemies(int x, int y) {
        return isValid(x, y) && (map[y][x] & WALL_MASK) == 0 && (map[y][x] & LARGE_ENEMIES_WALL_MASK) == 0;
    }

    @Override
    public boolean isAvailableAndPassable(Point point) {
        int x = (int) point.getX();
        int y = (int) point.getY();
        return isValid(x, y) && isPassable(x, y);
    }

    @Override
    public boolean isSpawnPoint(int x, int y) {
        return isValid(x, y) && (map[y][x] & SPAWN_POINT_MASK) == SPAWN_POINT_MASK;
    }

    @Override
    public boolean isWall(int x, int y) {
        return (map[y][x] & WALL_MASK) == WALL_MASK;
    }

    @Override
    public boolean isBorder(int x, int y) {
        return (map[y][x] & BORDER_MASK) == BORDER_MASK;
    }

    @Override
    public boolean isNotMarked(int x, int y) {
        return (map[y][x] & MARKED_MASK) == 0;
    }

    @Override
    public boolean isBossPath(int x, int y) {
        return (map[y][x] & BOSS_MASK) == BOSS_MASK;
    }

    @Override
    public boolean isWallForLargeEnemies(int x, int y) {
        return (map[y][x] & LARGE_ENEMIES_WALL_MASK) == LARGE_ENEMIES_WALL_MASK || isWall(x, y);
    }

    public void setWallForLargeEnemies(int x, int y) {
        map[y][x] |= LARGE_ENEMIES_WALL_MASK;
    }

    @Override
    public List<PointI> getSpawnPoints() {
        return cachedSpawnPoints;
    }

    public List<PointI> getLargeEnemiesSpawnPoints() {
        return cashedLargeEnemiesSpawnPoints;
    }

    @Override
    public void addWall(int x, int y) {
        map[y][x] |= WALL_MASK;
    }

    @Override
    public void removeWall(int x, int y) {
        map[y][x] &= ~WALL_MASK;
    }

    public void addSpawnPoint(int x, int y) {
        map[y][x] |= SPAWN_POINT_MASK;
    }

    public void removeSpawnPoint(int x, int y) {
        map[y][x] &= ~SPAWN_POINT_MASK;
    }

    public void addBorder(int x, int y) {
        map[y][x] |= BORDER_MASK;
    }

    public void removeBorder(int x, int y) {
        map[y][x] &= ~BORDER_MASK;
    }

    public byte getCell(int x, int y) {
        return map[y][x];
    }

    public void setBossSpawnPoint(PointI bossSpawnPoint) {
        this.bossSpawnPoint = bossSpawnPoint;
    }

    public PointI getBossSpawnPoint() {
        return bossSpawnPoint;
    }

    @Override
    public PointD getCenter() {
        return center;
    }

    public void setCenter(PointD center) {
        this.center = center;
    }

    public Map<Integer, List<SwarmSpawnParams>> getSwarmSpawnParams() {
        return swarmSpawnParams;
    }

    public List<SwarmSpawnParams> getSwarmSpawnParams(ISwarmType swarmType) {
        return swarmSpawnParams.get(swarmType.getTypeId());
    }

    public void setSwarmSpawnParams(Map<Integer, List<SwarmSpawnParams>> swarmSpawnParams) {
        this.swarmSpawnParams = swarmSpawnParams;
    }

    public Trajectory getTrajectory(int id) {
        return trajectories.get(id);
    }

    public Map<Integer, Trajectory> getTrajectories() {
        return trajectories;
    }

    public void setTrajectories(Map<Integer, Trajectory> trajectories) {
        this.trajectories = trajectories;
    }

    public Map<Integer, List<Trajectory>> getPredefinedTrajectories() {
        return predefinedTrajectories;
    }

    public List<Trajectory> getPredefinedTrajectories(int typeId, int skinId) {
        List<Trajectory> trajectories = predefinedTrajectories.get(typeId);
        if (trajectories != null && !trajectories.isEmpty()) {
            return trajectories;
        } else {
            return predefinedTrajectories.get(typeId * 100 + skinId);
        }
    }

    public List<Trajectory> getPredefinedTrajectories(ISwarmType swarmType) {
        return predefinedTrajectories.get(SWARM_TRAJECTORIES_OFFSET + swarmType.getTypeId());
    }

    public List<Trajectory> getPredefinedTrajectories(int swarmTypeId) {
        return predefinedTrajectories.get(SWARM_TRAJECTORIES_OFFSET + swarmTypeId);
    }

    public void setPredefinedTrajectories(Map<Integer, List<Trajectory>> predefinedTrajectories) {
        this.predefinedTrajectories = predefinedTrajectories;
    }

    public Map<Integer, List<Integer>> getPredefinedTrajectoryIds() {
        return predefinedTrajectoryIds;
    }

    public List<Integer> getPredefinedTrajectoryIds(IEnemyType<?> enemyType) {
        return predefinedTrajectoryIds.get(enemyType.getId());
    }

    public List<Integer> getPredefinedTrajectoryIds(int key) {
        return predefinedTrajectoryIds.get(key);
    }

    public void setPredefinedTrajectoryIds(Map<Integer, List<Integer>> predefinedTrajectoryIds) {
        this.predefinedTrajectoryIds = predefinedTrajectoryIds;
    }

    public List<SpawnScenario> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<SpawnScenario> scenarios) {
        this.scenarios = scenarios;
    }

    public List<SwarmParams> getSwarmParams() {
        return swarmParams;
    }

    public void setSwarmParams(List<SwarmParams> swarmParams) {
        this.swarmParams = swarmParams;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isValid(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public boolean isValidWithAssumption(int x, int y, int assumption) {
        return x >= -assumption && y >= -assumption && x < width + assumption && y < height + assumption;
    }

    public List<Portal> getPortals() {
        return portals;
    }

    public void setPortals(List<Portal> portals) {
        this.portals = portals;
    }

    public boolean isDisableRandomTrajectories() {
        return disableRandomTrajectories;
    }

    public void setDisableRandomTrajectories(boolean disableRandomTrajectories) {
        this.disableRandomTrajectories = disableRandomTrajectories;
    }

    public List<PointD> getPoints(String key) {
        return points.getOrDefault(key, new ArrayList<>());
    }

    public void setPoints(Map<String, List<PointD>> points) {
        this.points = points;
    }

    public Map<Integer, List<GroupParams>> getPredefinedGroups() {
        return predefinedGroups;
    }

    public void setPredefinedGroups(Map<Integer, List<GroupParams>> predefinedGroups) {
        this.predefinedGroups = predefinedGroups;
    }

    public boolean validate() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if ((isSpawnPoint(x, y) && (isBorder(x, y) || isWall(x, y))) || (isBorder(x, y) && isWall(x, y))) {
                    System.out.println("Bad point at (" + x + ", " + y + ")");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameMapShape [");
        sb.append("fileName='").append(fileName).append('\'');
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append(']');
        return sb.toString();
    }
}
