package com.betsoft.casino.mp.sectorx.model.math.config;

import com.betsoft.casino.mp.sectorx.model.math.EnemyType;
import com.betsoft.casino.mp.sectorx.model.math.InitialWaveType;
import com.betsoft.casino.mp.sectorx.model.math.SpawnStageFromConfig;
import com.betsoft.casino.mp.model.gameconfig.ISpawnConfig;
import com.dgphoenix.casino.common.util.RNG;

import java.util.*;

public class SpawnConfig implements ISpawnConfig {
    private final int MaxEntites;
    private final int MaxNumOfTemporalFormationsPerScreen;
    private final int MaxNumOfSpatialFormationsPerScreen;
    private final int MaxNumOfClusterFormationsPerScreen;
    private final int MaxNumOfHybridFormationsPerScreen;
    private final int MinNumOfEnemiesRandomWave;
    private final int MaxNumOfEnemiesRandomWave;
    private final double despawnPoint;
    private final double TemporalSpacingD;
    private final int MinNumOfMinorEnemies;
    private final int MaxNumOfMinorEnemies;
    private final int EnemiesInitialWaveSpeedFPSMul;
    private final double MinorEnemiesSpeedFPSMul;

    private final List<Double> EntitiesWeights;
    private final List<Double> HugePayItemsPickWeights;
    private final List<Double> WeightToPickInitialWave;
    private final NewBossParams bossParams;
    private final SpecialItemsParams specialItemsParams;
    private final HugePayItemsParams HugePayItemsParams;
    private final List<Long> specialItemsStayTimes;
    private final List<Long> HugePayItemsStayTimes;
    private final List<Double> highPayWeights;
    private final List<Double> midPayWeights;
    private final List<Double> lowPayWeights;
    private final List<Integer> timeSlices;
    private final List<EnemyType> highPayEnemies;
    private final List<EnemyType> midPayEnemies;
    private final List<EnemyType> lowPayEnemies;
    private final List<EnemyType> soloEnemies;
    private final List<EnemyType> TemporalEnemies;
    private final List<EnemyType> SpatialEnemiesC1;
    private final List<EnemyType> SpatialEnemiesC2;
    private final List<EnemyType> ClusterEnemies;
    private final List<EnemyType> HybridMajorEnemies;
    private final List<EnemyType> HybridMinorEnemies;
    private final Map<InitialWaveType, List<PredefinedPathParam>> InitialWavesPaths;
    private final List<List<SpatialPoint>> InitialWavesSpecialPatternOffsets;
    private final List<List<SpatialPoint>> SpatialFormationTemplatesC1;
    private final List<List<SpatialPoint>> SpatialFormationTemplatesC2;
    private final List<EnemyType> InitialWaveEnemies;
    private final List<List<Long>> TemporalFormationTemplates;
    private List<SpawnStageFromConfig> spawnStage;
    private final List<PredefinedPathParam> predefinedPaths;
    private final List<PathSection> predefinedBOSSInitalPaths;
    private final List<PredefinedPathParam> predefinedBOSSReenterPaths;


    public SpawnConfig(int maxEntites, int maxNumOfTemporalFormationsPerScreen, int maxNumOfSpatialFormationsPerScreen, int maxNumOfClusterFormationsPerScreen,
                       int maxNumOfHybridFormationsPerScreen, int minNumOfEnemiesRandomWave, int maxNumOfEnemiesRandomWave, double despawnPoint,
                       double temporalSpacingD, int minNumOfMinorEnemies, int maxNumOfMinorEnemies, int enemiesInitialWaveSpeedFPSMul,
                       double minorEnemiesSpeedFPSMul, List<Double> entitiesWeights, List<Double> hugePayItemsPickWeights, List<Double> weightToPickInitialWave, NewBossParams bossParams,
                       SpecialItemsParams specialItemsParams, HugePayItemsParams hugePayItemsParams, List<Long> specialItemsStayTimes, List<Long> hugePayItemsStayTimes, List<Double> highPayWeights, List<Double> midPayWeights,
                       List<Double> lowPayWeights, List<Integer> timeSlices, List<EnemyType> highPayEnemies, List<EnemyType> midPayEnemies,
                       List<EnemyType> lowPayEnemies, List<EnemyType> soloEnemies, List<EnemyType> temporalEnemies, List<EnemyType> spatialEnemiesC1,
                       List<EnemyType> spatialEnemiesC2, List<EnemyType> clusterEnemies, List<EnemyType> hybridMajorEnemies,
                       List<EnemyType> hybridMinorEnemies, Map<InitialWaveType, List<PredefinedPathParam>> initialWavesPaths,
                       List<List<SpatialPoint>> initialWavesSpecialPatternOffsets, List<List<SpatialPoint>> spatialFormationTemplatesC1, List<List<SpatialPoint>> spatialFormationTemplatesC2, List<EnemyType> initialWaveEnemies,
                       List<List<Long>> temporalFormationTemplates, List<List<Integer>> weightsByTimeSlice, List<PredefinedPathParam> predefinedPaths, List<PathSection> predefinedBOSSInitalPaths, List<PredefinedPathParam> predefinedBOSSReenterPaths) {
        this.MaxEntites = maxEntites;
        this.MaxNumOfTemporalFormationsPerScreen = maxNumOfTemporalFormationsPerScreen;
        this.MaxNumOfSpatialFormationsPerScreen = maxNumOfSpatialFormationsPerScreen;
        this.MaxNumOfClusterFormationsPerScreen = maxNumOfClusterFormationsPerScreen;
        this.MaxNumOfHybridFormationsPerScreen = maxNumOfHybridFormationsPerScreen;
        this.MinNumOfEnemiesRandomWave = minNumOfEnemiesRandomWave;
        this.MaxNumOfEnemiesRandomWave = maxNumOfEnemiesRandomWave;
        this.despawnPoint = despawnPoint;
        this.TemporalSpacingD = temporalSpacingD;
        this.MinNumOfMinorEnemies = minNumOfMinorEnemies;
        this.MaxNumOfMinorEnemies = maxNumOfMinorEnemies;
        this.EnemiesInitialWaveSpeedFPSMul = enemiesInitialWaveSpeedFPSMul;
        this.MinorEnemiesSpeedFPSMul = minorEnemiesSpeedFPSMul;
        this.EntitiesWeights = entitiesWeights;
        HugePayItemsPickWeights = hugePayItemsPickWeights;
        this.WeightToPickInitialWave = weightToPickInitialWave;
        this.bossParams = bossParams;
        this.specialItemsParams = specialItemsParams;
        this.HugePayItemsParams = hugePayItemsParams;
        this.specialItemsStayTimes = specialItemsStayTimes;
        HugePayItemsStayTimes = hugePayItemsStayTimes;
        this.highPayWeights = highPayWeights;
        this.midPayWeights = midPayWeights;
        this.lowPayWeights = lowPayWeights;
        this.timeSlices = timeSlices;
        this.highPayEnemies = highPayEnemies;
        this.midPayEnemies = midPayEnemies;
        this.lowPayEnemies = lowPayEnemies;
        this.soloEnemies = soloEnemies;
        this.TemporalEnemies = temporalEnemies;
        this.SpatialEnemiesC1 = spatialEnemiesC1;
        this.SpatialEnemiesC2 = spatialEnemiesC2;
        this.ClusterEnemies = clusterEnemies;
        this.HybridMajorEnemies = hybridMajorEnemies;
        this.HybridMinorEnemies = hybridMinorEnemies;
        this.InitialWavesPaths = initialWavesPaths;
        InitialWavesSpecialPatternOffsets = initialWavesSpecialPatternOffsets;
        this.SpatialFormationTemplatesC1 = spatialFormationTemplatesC1;
        this.SpatialFormationTemplatesC2 = spatialFormationTemplatesC2;
        this.InitialWaveEnemies = initialWaveEnemies;
        this.TemporalFormationTemplates = temporalFormationTemplates;
        this.predefinedPaths = predefinedPaths;
        this.predefinedBOSSInitalPaths = predefinedBOSSInitalPaths;
        this.predefinedBOSSReenterPaths = predefinedBOSSReenterPaths;
    }

    public List<Double> getHugePayItemsPickWeights() {
        return HugePayItemsPickWeights;
    }

    public List<Long> getHugePayItemsStayTimes() {
        return HugePayItemsStayTimes;
    }

    public Long getHugePayItemsStayTime() {
        return HugePayItemsStayTimes.get(RNG.nextInt(HugePayItemsStayTimes.size())) * 1000L;
    }

    public HugePayItemsParams getHugePayEnemiesParams() {
        return HugePayItemsParams;
    }

    public List<List<SpatialPoint>> getInitialWavesSpecialPatternOffsets() {
        return InitialWavesSpecialPatternOffsets;
    }

    public List<Double> getWeightToPickInitialWave() {
        return WeightToPickInitialWave;
    }

    public List<EnemyType> getSoloEnemies() {
        return soloEnemies;
    }

    public List<EnemyType> getTemporalEnemies() {
        return TemporalEnemies;
    }

    public List<EnemyType> getSpatialEnemiesC1() {
        return SpatialEnemiesC1;
    }

    public List<EnemyType> getSpatialEnemiesC2() {
        return SpatialEnemiesC2;
    }

    public List<EnemyType> getClusterEnemies() {
        return ClusterEnemies;
    }

    public List<EnemyType> getHybridMajorEnemies() {
        return HybridMajorEnemies;
    }

    public List<EnemyType> getHybridMinorEnemies() {
        return HybridMinorEnemies;
    }

    public List<List<SpatialPoint>> getSpatialFormationTemplatesC1() {
        return SpatialFormationTemplatesC1;
    }

    public List<List<SpatialPoint>> getSpatialFormationTemplatesC2() {
        return SpatialFormationTemplatesC2;
    }

    public List<EnemyType> getInitialWaveEnemies() {
        return InitialWaveEnemies;
    }

    public int getMaxEntites() {
        return MaxEntites;
    }

    public int getMaxNumOfTemporalFormationsPerScreen() {
        return MaxNumOfTemporalFormationsPerScreen;
    }

    public int getMaxNumOfSpatialFormationsPerScreen() {
        return MaxNumOfSpatialFormationsPerScreen;
    }

    public int getMaxNumOfClusterFormationsPerScreen() {
        return MaxNumOfClusterFormationsPerScreen;
    }

    public int getMaxNumOfHybridFormationsPerScreen() {
        return MaxNumOfHybridFormationsPerScreen;
    }

    public int getMinNumOfEnemiesRandomWave() {
        return MinNumOfEnemiesRandomWave;
    }

    public int getMaxNumOfEnemiesRandomWave() {
        return MaxNumOfEnemiesRandomWave;
    }

    public double getDespawnPoint() {
        return despawnPoint;
    }

    public double getTemporalSpacingD() {
        return TemporalSpacingD;
    }

    public int getMinNumOfMinorEnemies() {
        return MinNumOfMinorEnemies;
    }

    public int getMaxNumOfMinorEnemies() {
        return MaxNumOfMinorEnemies;
    }

    public int getEnemiesInitialWaveSpeedFPSMul() {
        return EnemiesInitialWaveSpeedFPSMul;
    }

    public double getMinorEnemiesSpeedFPSMul() {
        return MinorEnemiesSpeedFPSMul;
    }

    public List<Double> getEntitiesWeights() {
        return EntitiesWeights;
    }



    public List<List<Long>> getTemporalFormationTemplates() {
        return TemporalFormationTemplates;
    }

    public Map<InitialWaveType, List<PredefinedPathParam>> getInitialWavesPaths() {
        return InitialWavesPaths;
    }

    public List<Long> getSpecialItemsStayTimes() {
        return specialItemsStayTimes;
    }

    public long getSpecialItemsStayTime() {
        return specialItemsStayTimes.get(RNG.nextInt(specialItemsStayTimes.size())) * 1000L;
    }

    public List<EnemyType> getHighPayEnemies() {
        return highPayEnemies == null ? Collections.emptyList() : highPayEnemies;
    }

    public List<EnemyType> getMidPayEnemies() {
        return midPayEnemies == null ? Collections.emptyList() : midPayEnemies;
    }

    public List<EnemyType> getLowPayEnemies() {
        return lowPayEnemies == null ? Collections.emptyList() : lowPayEnemies;
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

    public NewBossParams getBossParams() {
        return bossParams;
    }

    public SpecialItemsParams getSpecialItemsParams() {
        return specialItemsParams;
    }

    public List<PredefinedPathParam> getPredefinedPaths() {
        return predefinedPaths;
    }

    public List<PathSection> getPredefinedBOSSInitalPaths() {
        return predefinedBOSSInitalPaths;
    }

    public List<PredefinedPathParam> getPredefinedBOSSReenterPaths() {
        return predefinedBOSSReenterPaths;
    }


    @Override
    public String toString() {
        return "SpawnConfig{" +
                "MaxEntites=" + MaxEntites +
                ", MaxNumOfTemporalFormationsPerScreen=" + MaxNumOfTemporalFormationsPerScreen +
                ", MaxNumOfSpatialFormationsPerScreen=" + MaxNumOfSpatialFormationsPerScreen +
                ", MaxNumOfClusterFormationsPerScreen=" + MaxNumOfClusterFormationsPerScreen +
                ", MaxNumOfHybridFormationsPerScreen=" + MaxNumOfHybridFormationsPerScreen +
                ", MinNumOfEnemiesRandomWave=" + MinNumOfEnemiesRandomWave +
                ", MaxNumOfEnemiesRandomWave=" + MaxNumOfEnemiesRandomWave +
                ", despawnPoint=" + despawnPoint +
                ", TemporalSpacingD=" + TemporalSpacingD +
                ", MinNumOfMinorEnemies=" + MinNumOfMinorEnemies +
                ", MaxNumOfMinorEnemies=" + MaxNumOfMinorEnemies +
                ", EnemiesInitialWaveSpeedFPSMul=" + EnemiesInitialWaveSpeedFPSMul +
                ", MinorEnemiesSpeedFPSMul=" + MinorEnemiesSpeedFPSMul +
                ", EntitiesWeights=" + EntitiesWeights +
                ", weightToPickInitialWave=" + WeightToPickInitialWave +
                ", bossParams=" + bossParams +
                ", specialItemsParams=" + specialItemsParams +
                ", hugePayEnemiesParams=" + HugePayItemsParams +
                ", specialItemsStayTimes=" + specialItemsStayTimes +
                ", highPayWeights=" + highPayWeights +
                ", midPayWeights=" + midPayWeights +
                ", lowPayWeights=" + lowPayWeights +
                ", timeSlices=" + timeSlices +
                ", highPayEnemies=" + highPayEnemies +
                ", midPayEnemies=" + midPayEnemies +
                ", lowPayEnemies=" + lowPayEnemies +
                ", soloEnemies=" + soloEnemies +
                ", TemporalEnemies=" + TemporalEnemies +
                ", SpatialEnemiesC1=" + SpatialEnemiesC1 +
                ", SpatialEnemiesC2=" + SpatialEnemiesC2 +
                ", ClusterEnemies=" + ClusterEnemies +
                ", HybridMajorEnemies=" + HybridMajorEnemies +
                ", HybridMinorEnemies=" + HybridMinorEnemies +
                ", InitialWavesPaths=" + InitialWavesPaths +
                ", InitialWavesSpecialPatternOffsets=" + InitialWavesSpecialPatternOffsets +
                ", SpatialFormationTemplatesC1=" + SpatialFormationTemplatesC1 +
                ", SpatialFormationTemplatesC2=" + SpatialFormationTemplatesC2 +
                ", InitialWaveEnemies=" + InitialWaveEnemies +
                ", TemporalFormationTemplates=" + TemporalFormationTemplates +
                ", spawnStage=" + spawnStage +
                ", predefinedPaths=" + predefinedPaths +
                ", predefinedBOSSInitalPaths=" + predefinedBOSSInitalPaths +
                ", predefinedBOSSReenterPaths=" + predefinedBOSSReenterPaths +
                '}';
    }
}
