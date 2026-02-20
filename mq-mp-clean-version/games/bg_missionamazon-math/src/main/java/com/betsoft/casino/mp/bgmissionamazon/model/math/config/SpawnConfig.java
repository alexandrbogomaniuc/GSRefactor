package com.betsoft.casino.mp.bgmissionamazon.model.math.config;

import com.betsoft.casino.mp.model.gameconfig.ISpawnConfig;
import com.dgphoenix.casino.common.util.RNG;

import java.util.Collections;
import java.util.List;

public class SpawnConfig implements ISpawnConfig {
    private int enemiesWithPredefinedTrajectoriesMax;
    private int initialSpawnEnemies;
    private int swarmEnemiesMax;
    private SpawnBossParams bossParams;
    private int staticEnemyMax;
    private List<Long> staticStayTimes;
    private List<Long> teleportingStayTimes;
    private List<Integer> teleportingHopsNumber;
    private List<Integer> weaponCarrierTypes;
    private List<Long> weaponCarrierSpawnTimeDelay;
    private List<Long> staticEnemiesSpawnTimeDelay;
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

    public SpawnConfig(int enemiesWithPredefinedTrajectoriesMax, int initialSpawnEnemies, int swarmEnemiesMax, SpawnBossParams bossParams,
                       int staticEnemyMax, List<Long> staticStayTimes, List<Long> teleportingStayTimes,
                       List<Integer> teleportingHopsNumber, List<Integer> weaponCarrierTypes, List<Long> weaponCarrierSpawnTimeDelay,
                       List<Long> staticEnemiesSpawnTimeDelay, List<Long> weaponCarrierStayTimes, List<Long> weaponCarrierOffsetTime,
                       List<Integer> highPayEnemies, List<Integer> midPayEnemies,
                       List<Integer> lowPayEnemies, int allEnemiesMax, List<Integer> dividers, List<Double> highPayWeights,
                       List<Double> midPayWeights, List<Double> lowPayWeights, List<Integer> timeSlices, List<List<Integer>> weightsByTimeSlice,
                       List<Integer> killEnemyByIds, List<Integer> killCertainNumberOfEnemies) {
        this.swarmEnemiesMax = swarmEnemiesMax;
        this.enemiesWithPredefinedTrajectoriesMax = enemiesWithPredefinedTrajectoriesMax;
        this.initialSpawnEnemies = initialSpawnEnemies;
        this.bossParams = bossParams;
        this.staticEnemyMax = staticEnemyMax;
        this.staticStayTimes = staticStayTimes;
        this.teleportingStayTimes = teleportingStayTimes;
        this.teleportingHopsNumber = teleportingHopsNumber;
        this.weaponCarrierTypes = weaponCarrierTypes;
        this.weaponCarrierSpawnTimeDelay = weaponCarrierSpawnTimeDelay;
        this.staticEnemiesSpawnTimeDelay = staticEnemiesSpawnTimeDelay;
        this.weaponCarrierStayTimes = weaponCarrierStayTimes;
        this.weaponCarrierOffsetTime = weaponCarrierOffsetTime;
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

    public List<Long> getStaticStayTimes() {
        return staticStayTimes;
    }

    public List<Long> getTeleportingStayTimes() {
        return teleportingStayTimes;
    }

    public List<Integer> getTeleportingHopsNumber() {
        return teleportingHopsNumber;
    }

    public List<Integer> getWeaponCarrierTypes() {
        return weaponCarrierTypes == null ? Collections.emptyList() : weaponCarrierTypes;
    }

    public List<Long> getWeaponCarrierStayTimes() {
        return weaponCarrierStayTimes;
    }

    public List<Long> getWeaponCarrierOffsetTime() {
        return weaponCarrierOffsetTime;
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

    public List<Long> getWeaponCarrierSpawnTimeDelay() {
        return weaponCarrierSpawnTimeDelay;
    }

    public List<Long> getStaticEnemiesSpawnTimeDelay() {
        return staticEnemiesSpawnTimeDelay;
    }

    public long getRandomStaticStayTime() {
        return staticStayTimes.get(RNG.nextInt(staticStayTimes.size())) * 1000L;
    }

    public long getRandomTeleportingStayTime() {
        return teleportingStayTimes.get(RNG.nextInt(teleportingStayTimes.size())) * 1000L;
    }

    public int getRandomTeleportingHops() {
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

    public int getRandomKillNumberOfEnemies() {
        if (killCertainNumberOfEnemies == null || killCertainNumberOfEnemies.isEmpty() || killCertainNumberOfEnemies.contains(-1)) {
            return -1;
        }
        return killCertainNumberOfEnemies.get(RNG.nextInt(killCertainNumberOfEnemies.size()));
    }

    public SpawnBossParams getBossParams() {
        return bossParams;
    }

    @Override
    public String toString() {
        return "SpawnConfig{" +
                "enemiesWithPredefinedTrajectoriesMax=" + enemiesWithPredefinedTrajectoriesMax +
                ", initialSpawnEnemies=" + initialSpawnEnemies +
                ", swarmEnemiesMax=" + swarmEnemiesMax +
                ", bossParams=" + bossParams +
                ", staticEnemyMax=" + staticEnemyMax +
                ", staticStayTimes=" + staticStayTimes +
                ", teleportingStayTimes=" + teleportingStayTimes +
                ", teleportingHopsNumber=" + teleportingHopsNumber +
                ", weaponCarrierTypes=" + weaponCarrierTypes +
                ", weaponCarrierSpawnTimeDelay=" + weaponCarrierSpawnTimeDelay +
                ", staticEnemiesSpawnTimeDelay=" + staticEnemiesSpawnTimeDelay +
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
                '}';
    }
}
