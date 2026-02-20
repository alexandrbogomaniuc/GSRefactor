package com.betsoft.casino.mp.missionamazon.model.math.config;

import com.betsoft.casino.mp.model.gameconfig.ISpawnConfig;
import com.dgphoenix.casino.common.util.RNG;

import java.util.Collections;
import java.util.List;

public class SpawnConfig implements ISpawnConfig {
    private int enemiesWithPredefinedTrajectoriesMax;
    private int initialSpawnEnemies;
    private int swarmEnemiesMax;
    private int staticEnemyMax;
    private List<Long> staticStayTimes;
    private List<Long> staticEnemiesSpawnTimeDelay;
    private List<Long> teleportingStayTimes;
    private List<Integer> teleportingHopsNumber;
    private List<Integer> weaponCarrierTypes;
    private List<Long> weaponCarrierSpawnTimeDelay;
    private List<Long> weaponCarrierStayTimes;
    private List<Long> weaponCarrierOffsetTime;
    private List<Integer> highPayEnemies;
    private List<Integer> midPayEnemies;
    private List<Integer> lowPayEnemies;
    private int allEnemiesMax;
    private List<Integer> dividers;
    private List<Double> highPayWeights;
    private List<Double> midPayWeights;
    private List<Double> lowPayWeights;
    private List<Integer> timeSlices;
    private List<List<Integer>> weightsByTimeSlice;
    private List<Integer> killEnemyByIds;
    private List<Integer> killCertainNumberOfEnemies;
    private NewBossParams bossParams;

    public SpawnConfig(int enemiesWithPredefinedTrajectoriesMax, int initialSpawnEnemies, int swarmEnemiesMax, int staticEnemyMax,
                       List<Long> staticStayTimes, List<Long> teleportingStayTimes, List<Integer> teleportingHopsNumber,
                       List<Integer> weaponCarrierTypes, List<Long> weaponCarrierSpawnTimeDelay, List<Long> weaponCarrierStayTimes,
                       List<Integer> highPayEnemies, List<Integer> midPayEnemies, List<Integer> lowPayEnemies,
                       int allEnemiesMax, List<Integer> dividers, List<Double> highPayWeights,
                       List<Double> midPayWeights, List<Double> lowPayWeights,
                       List<Integer> timeSlices, List<List<Integer>> weightsByTimeSlice,
                       List<Integer> killEnemyByIds, List<Integer> killCertainNumberOfEnemies, List<Long> weaponCarrierOffsetTime,
                       List<Long> staticEnemiesSpawnTimeDelay, NewBossParams bossParams) {
        this.swarmEnemiesMax = swarmEnemiesMax;
        this.enemiesWithPredefinedTrajectoriesMax = enemiesWithPredefinedTrajectoriesMax;
        this.initialSpawnEnemies = initialSpawnEnemies;
        this.staticEnemyMax = staticEnemyMax;
        this.staticStayTimes = staticStayTimes;
        this.teleportingStayTimes = teleportingStayTimes;
        this.teleportingHopsNumber = teleportingHopsNumber;
        this.weaponCarrierTypes = weaponCarrierTypes;
        this.weaponCarrierSpawnTimeDelay = weaponCarrierSpawnTimeDelay;
        this.weaponCarrierStayTimes = weaponCarrierStayTimes;
        this.highPayEnemies = highPayEnemies;
        this.midPayEnemies = midPayEnemies;
        this.lowPayEnemies = lowPayEnemies;
        this.allEnemiesMax = allEnemiesMax;
        this.dividers = dividers;
        this.highPayWeights = highPayWeights;
        this.midPayWeights = midPayWeights;
        this.lowPayWeights = lowPayWeights;
        this.timeSlices = timeSlices;
        this.weightsByTimeSlice = weightsByTimeSlice;
        this.killEnemyByIds = killEnemyByIds;
        this.killCertainNumberOfEnemies = killCertainNumberOfEnemies;
        this.weaponCarrierOffsetTime = weaponCarrierOffsetTime;
        this.staticEnemiesSpawnTimeDelay = staticEnemiesSpawnTimeDelay;
        this.bossParams = bossParams;
    }

    public int getSwarmEnemiesMax() {
        return swarmEnemiesMax;
    }

    public int getEnemiesWithPredefinedTrajectoriesMax() {
        return enemiesWithPredefinedTrajectoriesMax;
    }

    public int getInitialSpawnEnemies() {
        return initialSpawnEnemies;
    }

    public int getStaticEnemyMax() {
        return staticEnemyMax;
    }

    public List<Integer> getWeaponCarrierTypes() {
        return weaponCarrierTypes == null ? Collections.emptyList() : weaponCarrierTypes;
    }

    public List<Long> getWeaponCarrierSpawnTimeDelay() {
        return weaponCarrierSpawnTimeDelay;
    }

    public List<Long> getStaticEnemiesSpawnTimeDelay() {
        return staticEnemiesSpawnTimeDelay;
    }

    public List<Integer> getHighPayEnemies() {
        return highPayEnemies == null ? Collections.emptyList() : highPayEnemies;
    }

    public List<Integer> getMidPayEnemies() {
        return midPayEnemies == null ? Collections.emptyList() : midPayEnemies;
    }

    public List<Integer> getLowPayEnemies() {
        return lowPayEnemies == null ? Collections.emptyList() : lowPayEnemies;
    }

    public int getAllEnemiesMax() {
        return allEnemiesMax;
    }

    public List<Long> getStaticStayTimes() {
        return staticStayTimes;
    }

    public List<Long> getTeleportingStayTimes() {
        return teleportingStayTimes;
    }

    public List<Integer> getTeleportingHopsNumber() {
        return teleportingHopsNumber;
    }

    public List<Long> getWeaponCarrierStayTimes() {
        return weaponCarrierStayTimes;
    }

    public List<Integer> getDividers() {
        return dividers;
    }

    public List<Double> getHighPayWeights() {
        return highPayWeights;
    }

    public List<Double> getMidPayWeights() {
        return midPayWeights;
    }

    public List<Double> getLowPayWeights() {
        return lowPayWeights;
    }

    public List<Integer> getTimeSlices() {
        return timeSlices;
    }

    public List<List<Integer>> getWeightsByTimeSlice() {
        return weightsByTimeSlice;
    }

    public List<Integer> getKillEnemyByIds() {
        return killEnemyByIds == null ? Collections.singletonList(-1) : killEnemyByIds;
    }

    public List<Integer> getKillCertainNumberOfEnemies() {
        return killCertainNumberOfEnemies;
    }

    public List<Long> getWeaponCarrierOffsetTime() {
        return weaponCarrierOffsetTime;
    }

    public NewBossParams getBossParams() {
        return bossParams;
    }

    public int getKillNumberOfEnemies() {
        if (killCertainNumberOfEnemies == null || killCertainNumberOfEnemies.isEmpty() || killCertainNumberOfEnemies.contains(-1)) {
            return -1;
        }
        return killCertainNumberOfEnemies.get(RNG.nextInt(killCertainNumberOfEnemies.size()));
    }

    public long getStaticStayTime() {
        return staticStayTimes.get(RNG.nextInt(staticStayTimes.size())) * 1000L;
    }

    public long getTeleportingStayTime() {
        return teleportingStayTimes.get(RNG.nextInt(teleportingStayTimes.size())) * 1000L;
    }

    public int getTeleportingHops() {
        if (teleportingHopsNumber == null || teleportingHopsNumber.isEmpty()) {
            return RNG.nextInt(2, 5);
        }
        return teleportingHopsNumber.get(RNG.nextInt(teleportingHopsNumber.size()));
    }

    public long getWeaponCarrierStayTime() {
        return weaponCarrierStayTimes.get(RNG.nextInt(weaponCarrierStayTimes.size())) * 1000L;
    }

    public long getRandomWCOffsetTime() {
        return weaponCarrierOffsetTime.get(RNG.nextInt(weaponCarrierOffsetTime.size())) * 1000L;
    }

    public long getRandomWeaponCarrierSpawnTimeDelay() {
        return weaponCarrierSpawnTimeDelay.get(RNG.nextInt(weaponCarrierSpawnTimeDelay.size())) * 1000L;
    }

    public long getRandomStaticEnemiesSpawnTimeDelay() {
        return staticEnemiesSpawnTimeDelay.get(RNG.nextInt(staticEnemiesSpawnTimeDelay.size())) * 1000L;
    }

    @Override
    public String toString() {
        return "SpawnConfig{" +
                "enemiesWithPredefinedTrajectoriesMax=" + enemiesWithPredefinedTrajectoriesMax +
                ", initialSpawnEnemies=" + initialSpawnEnemies +
                ", swarmEnemiesMax=" + swarmEnemiesMax +
                ", staticEnemyMax=" + staticEnemyMax +
                ", staticStayTimes=" + staticStayTimes +
                ", staticEnemiesSpawnTimeDelay=" + staticEnemiesSpawnTimeDelay +
                ", teleportingStayTimes=" + teleportingStayTimes +
                ", teleportingHopsNumber=" + teleportingHopsNumber +
                ", weaponCarrierTypes=" + weaponCarrierTypes +
                ", weaponCarrierSpawnTimeDelay=" + weaponCarrierSpawnTimeDelay +
                ", weaponCarrierStayTimes=" + weaponCarrierStayTimes +
                ", weaponCarrierOffsetTime=" + weaponCarrierOffsetTime +
                ", highPayEnemies=" + highPayEnemies +
                ", midPayEnemies=" + midPayEnemies +
                ", lowPayEnemies=" + lowPayEnemies +
                ", allEnemiesMax=" + allEnemiesMax +
                ", dividers=" + dividers +
                ", highPayWeights=" + highPayWeights +
                ", midPayWeights=" + midPayWeights +
                ", lowPayWeights=" + lowPayWeights +
                ", timeSlices=" + timeSlices +
                ", weightsByTimeSlice=" + weightsByTimeSlice +
                ", killEnemyByIds=" + killEnemyByIds +
                ", killCertainNumberOfEnemies=" + killCertainNumberOfEnemies +
                ", bossParams=" + bossParams +
                '}';
    }
}
