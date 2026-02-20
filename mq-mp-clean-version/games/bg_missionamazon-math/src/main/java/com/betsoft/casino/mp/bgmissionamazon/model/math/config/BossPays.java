package com.betsoft.casino.mp.bgmissionamazon.model.math.config;

import com.betsoft.casino.mp.model.SpecialWeaponType;

import java.util.Map;

public class BossPays {
    private Map<Integer, Double> partialPays;
    private Map<Integer, Double> partialPaysWeightsTurret;
    private Map<SpecialWeaponType, Map<Integer, Double>> partialPaysWeights;
    private Map<Integer, Double> partialPaysProbTurretTargetBoss;
    private Map<SpecialWeaponType, Map<Integer, Double>> partialPaysProbTargetEn;
    private Map<SpecialWeaponType, Map<Integer, Double>> partialPaysProbTargetBoss;

    public BossPays(Map<Integer, Double> partialPaysProbTurretTargetBoss,
                    Map<SpecialWeaponType, Map<Integer, Double>> partialPaysProbTargetEn,
                    Map<Integer, Double> partialPays,
                    Map<Integer, Double> partialPaysWeightsTurret,
                    Map<SpecialWeaponType, Map<Integer, Double>> partialPaysWeights,
                    Map<SpecialWeaponType, Map<Integer, Double>> partialPaysProbTargetBoss) {
        this.partialPaysProbTurretTargetBoss = partialPaysProbTurretTargetBoss;
        this.partialPaysProbTargetEn = partialPaysProbTargetEn;
        this.partialPays = partialPays;
        this.partialPaysWeightsTurret = partialPaysWeightsTurret;
        this.partialPaysWeights = partialPaysWeights;
        this.partialPaysProbTargetBoss = partialPaysProbTargetBoss;
    }

    public Map<Integer, Double> getPartialPays() {
        return partialPays;
    }

    public Map<Integer, Double> getPartialPaysWeightsTurret() {
        return partialPaysWeightsTurret;
    }

    public Map<SpecialWeaponType, Map<Integer, Double>> getPartialPaysWeights() {
        return partialPaysWeights;
    }

    public Map<SpecialWeaponType, Map<Integer, Double>> getPartialPaysProbTargetEn() {
        return partialPaysProbTargetEn;
    }

    public Map<SpecialWeaponType, Map<Integer, Double>> getPartialPaysProbTargetBoss() {
        return partialPaysProbTargetBoss;
    }

    public Map<Integer, Double> getPartialPaysProbTurretTargetBoss() {
        return partialPaysProbTurretTargetBoss;
    }


    @Override
    public String toString() {
        return "BossPays{" +
                "partialPays=" + partialPays +
                ", partialPaysWeightsTurret=" + partialPaysWeightsTurret +
                ", partialPaysWeights=" + partialPaysWeights +
                ", partialPaysProbTurretTargetBoss=" + partialPaysProbTurretTargetBoss +
                ", partialPaysProbTargetEn=" + partialPaysProbTargetEn +
                ", partialPaysProbTargetBoss=" + partialPaysProbTargetBoss +
                '}';
    }
}
