package com.betsoft.casino.mp.missionamazon.model;

import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.teststand.TestStandFeature;
import com.dgphoenix.casino.common.util.RNG;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.model.SpecialWeaponType.*;
import static com.betsoft.casino.teststand.TeststandConst.*;

public class MissionAmazonTestStand {
    private final int featureId;
    private final TestStandFeature feature;

    public MissionAmazonTestStand() {
        this.featureId = 0;
        this.feature = null;
    }

    public MissionAmazonTestStand(TestStandFeature feature) {
        this.featureId = feature != null ? feature.getId() : 0;
        this.feature = feature;
    }

    public int getFeatureId() {
        return feature.getId();
    }

    public boolean hasFeature() {
        return feature != null;
    }

    public boolean isFeatureCompleted() {
        if (feature == null) {
            return false;
        }

        if (featureId == FEATURE_NO_ANY_WIN) {
            return false;
        }

        Map<Integer, AtomicInteger> featuresAppeared = feature.getFeaturesAppeared();
        if (featuresAppeared.isEmpty()) {
            return true;
        }

        return true;
    }

    public boolean isNeedHit() {
        return featureId == 11 || isNeedKill() || featureId == FEATURE_BIG_WIN || featureId == FEATURE_HUGE_WIN || featureId == FEATURE_MEGA_WIN;
    }

    public int getWin() {
        switch (featureId) {
            case FEATURE_BIG_WIN: return 75;
            case FEATURE_HUGE_WIN: return 150;
            case FEATURE_MEGA_WIN: return 300;
        }
        return 0;
    }

    public boolean isNeedKill() {
        return featureId == 6 || featureId == 7 || featureId == 9;
    }

    public boolean isNeedNewWeaponsWithKill() {
        return (featureId >= 30 && featureId <= 41)
                || featureId == FEATURE_KILL_AND_GET_WEAPON
                || featureId == FEATURE_DROP_TWO_WEAPONS_FROM_SPECTER
                || (featureId == FEATURE_SLOT_AND_WEAPON && feature.getFeaturesAppeared().get(FEATURE_RANDOM_WEAPON).get() < 1)
                || (featureId == FEATURE_DUAL_SLOT_AND_WEAPON && feature.getFeaturesAppeared().get(FEATURE_RANDOM_WEAPON).get() < 1);
    }

    public boolean isNeedNewWeaponsWithoutKill() {
        return featureId == FEATURE_GET_WEAPON_WITHOUT_KILL;
    }

    public SpecialWeaponType getSpecialWeaponType() {
        switch (featureId) {
            case 33:
                return Ricochet;
            case 34:
                return Plasma;
            case 37:
                return ArtilleryStrike;
            case 39:
                return Flamethrower;
            case 40:
                return Cryogun;
            default:
                return getRandomSpecialWeaponType();
        }
    }

    private SpecialWeaponType getRandomSpecialWeaponType() {
        List<SpecialWeaponType> weapons = Arrays.stream(values())
                .filter(sw -> sw.getAvailableGameIds().contains((int) GameType.MISSION_AMAZON.getGameId()))
                .collect(Collectors.toList());
        return weapons.get(RNG.nextInt(weapons.size()));
    }

    public boolean isNotNeedAnyWin() {
        return featureId == FEATURE_NO_ANY_WIN;
    }

    public void countRandomWeapon() {
        incrementCounter(FEATURE_RANDOM_WEAPON);
    }

    public boolean isNeedGem() {
        return featureId == FEATURE_DROP_GEM;
    }

    private void incrementCounter(int featureId) {
        if (hasFeature()) {
            Map<Integer, AtomicInteger> featuresAppeared = feature.getFeaturesAppeared();
            if (featuresAppeared.containsKey(featureId)) {
                featuresAppeared.get(featureId).incrementAndGet();
            }
        }
    }

    public boolean isNeedSpawnBoss() {
        return featureId == 9;
    }

    public boolean isNeedCriticalHit() {
        return featureId == FEATURE_NEED_CH;
    }
}
