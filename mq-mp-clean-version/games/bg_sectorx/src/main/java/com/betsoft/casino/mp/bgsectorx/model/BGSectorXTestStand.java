package com.betsoft.casino.mp.bgsectorx.model;

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

public class BGSectorXTestStand {
    private final int featureId;
    private final TestStandFeature feature;

    public BGSectorXTestStand() {
        this.featureId = 0;
        this.feature = null;
    }

    public BGSectorXTestStand(TestStandFeature feature) {
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
        return featureId == 11 || isNeedKill();
    }

    public boolean isNeedKill() {
        return featureId == 6 || featureId == 7 || featureId == 9;
    }

    private SpecialWeaponType getRandomSpecialWeaponType() {
        List<SpecialWeaponType> weapons = Arrays.stream(values())
                .filter(sw -> sw.getAvailableGameIds().contains((int) GameType.BG_SECTOR_X.getGameId()))
                .collect(Collectors.toList());
        return weapons.get(RNG.nextInt(weapons.size()));
    }

    public boolean isNotNeedAnyWin() {
        return featureId == FEATURE_NO_ANY_WIN;
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

    public boolean isNeedLevelUp() {
        return featureId == FEATURE_DROP_LEVEL_UP_WEAPON;
    }
 }
