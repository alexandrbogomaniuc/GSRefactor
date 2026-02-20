package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.common.scenarios.SpawnScenario;
import com.betsoft.casino.mp.model.gameconfig.IMapConfig;
import com.betsoft.casino.mp.model.movement.Trajectory;
import org.kynosarges.tektosyne.geometry.PointD;
import org.kynosarges.tektosyne.geometry.PointI;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameMapMeta implements IMapConfig {
    private PointI bossSpawnPoint;
    private PointD center;
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

    public GameMapMeta(PointI bossSpawnPoint, PointD center, Map<Integer, List<SwarmSpawnParams>> swarmSpawnParams,
                       Map<Integer, Trajectory> trajectories,
                       Map<Integer, List<Trajectory>> predefinedTrajectories,
                       Map<Integer, List<Integer>> predefinedTrajectoryIds,
                       List<SpawnScenario> scenarios,
                       List<SwarmParams> swarmParams, List<Portal> portals, boolean disableRandomTrajectories,
                       Map<String, List<PointD>> points, Map<Integer, List<GroupParams>> predefinedGroups) {
        this.bossSpawnPoint = bossSpawnPoint;
        this.center = center;
        this.swarmSpawnParams = swarmSpawnParams;
        this.trajectories = trajectories;
        this.predefinedTrajectories = predefinedTrajectories;
        this.predefinedTrajectoryIds = predefinedTrajectoryIds;
        this.scenarios = scenarios;
        this.swarmParams = swarmParams;
        this.portals = portals;
        this.disableRandomTrajectories = disableRandomTrajectories;
        this.points = points;
        this.predefinedGroups = predefinedGroups;
    }

    public PointI getBossSpawnPoint() {
        return bossSpawnPoint;
    }

    public PointD getCenter() {
        return center;
    }

    public void setBossSpawnPoint(PointI bossSpawnPoint) {
        this.bossSpawnPoint = bossSpawnPoint;
    }

    public Map<Integer, List<SwarmSpawnParams>> getSwarmSpawnParams() {
        return swarmSpawnParams;
    }

    public void setSwarmSpawnParams(Map<Integer, List<SwarmSpawnParams>> swarmSpawnParams) {
        this.swarmSpawnParams = swarmSpawnParams;
    }

    public Map<Integer, Trajectory> getTrajectories() {
        return trajectories;
    }

    public Map<Integer, List<Trajectory>> getPredefinedTrajectories() {
        return predefinedTrajectories;
    }

    public Map<Integer, List<Integer>> getPredefinedTrajectoryIds() {
        return predefinedTrajectoryIds;
    }

    public List<SpawnScenario> getScenarios() {
        return scenarios;
    }

    public List<SwarmParams> getSwarmParams() {
        return swarmParams;
    }

    public List<Portal> getPortals() {
        return portals;
    }

    public boolean isDisableRandomTrajectories() {
        return disableRandomTrajectories;
    }

    public Map<String, List<PointD>> getPoints() {
        return points;
    }

    public Map<Integer, List<GroupParams>> getPredefinedGroups() {
        return predefinedGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameMapMeta that = (GameMapMeta) o;
        return disableRandomTrajectories == that.disableRandomTrajectories &&
                Objects.equals(bossSpawnPoint, that.bossSpawnPoint) &&
                Objects.equals(center, that.center) &&
                Objects.equals(swarmSpawnParams, that.swarmSpawnParams) &&
                Objects.equals(trajectories, that.trajectories) &&
                Objects.equals(predefinedTrajectories, that.predefinedTrajectories) &&
                Objects.equals(predefinedTrajectoryIds, that.predefinedTrajectoryIds) &&
                Objects.equals(scenarios, that.scenarios) &&
                Objects.equals(swarmParams, that.swarmParams) &&
                Objects.equals(portals, that.portals) &&
                Objects.equals(points, that.points) &&
                Objects.equals(predefinedGroups, that.predefinedGroups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bossSpawnPoint, center, swarmSpawnParams, trajectories, predefinedTrajectories,
                predefinedTrajectoryIds, scenarios, swarmParams, portals, disableRandomTrajectories, points,
                predefinedGroups);
    }

    @Override
    public String toString() {
        return "GameMapMeta{" +
                "bossSpawnPoint=" + bossSpawnPoint +
                ", center=" + center +
                ", swarmSpawnParams=" + swarmSpawnParams +
                ", trajectories=" + trajectories +
                ", predefinedTrajectories=" + predefinedTrajectories +
                ", predefinedTrajectoryIds=" + predefinedTrajectoryIds +
                ", scenarios=" + scenarios +
                ", swarmParams=" + swarmParams +
                ", portals=" + portals +
                ", disableRandomTrajectories=" + disableRandomTrajectories +
                ", points=" + points +
                ", predefinedGroups=" + predefinedGroups +
                '}';
    }
}
