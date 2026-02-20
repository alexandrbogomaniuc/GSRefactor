package com.betsoft.casino.mp.maxcrashgame.model;

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
import static com.betsoft.casino.mp.model.SpecialWeaponType.values;
import static com.betsoft.casino.teststand.TeststandConst.*;


public class MaxCrashStoneTestStand {
    private final int featureId;
    private final TestStandFeature feature;

    public MaxCrashStoneTestStand() {
        this.featureId = 0;
        this.feature = null;
    }

    public MaxCrashStoneTestStand(TestStandFeature feature) {
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

        if(featureId == FEATURE_NO_ANY_WIN) {
            return false;
        }

        Map<Integer, AtomicInteger> featuresAppeared = feature.getFeaturesAppeared();
        if (featuresAppeared.isEmpty()) {
            return true;
        }

        switch (featureId) {
            case FEATURE_RAGE:
            case FEATURE_RAGE_WITH_IK:
                return featuresAppeared.get(FEATURE_RAGE).get() > 0;
            case FEATURE_DUAL_RAGE:
                return featuresAppeared.get(FEATURE_RAGE).get() > 1;
            case FEATURE_RAGE_WITH_STONE:
                return featuresAppeared.get(FEATURE_RAGE).get() > 0 && featuresAppeared.get(FEATURE_FRAGMENT).get() > 0;
            case FEATURE_SLOT_AND_WEAPON:
                return featuresAppeared.get(FEATURE_SLOT).get() > 0 && featuresAppeared.get(FEATURE_RANDOM_WEAPON).get() > 0;
            case FEATURE_DUAL_SLOT:
                return featuresAppeared.get(FEATURE_SLOT).get() > 1;
            case FEATURE_DUAL_SLOT_AND_WEAPON:
                return featuresAppeared.get(FEATURE_SLOT).get() > 1 && featuresAppeared.get(FEATURE_RANDOM_WEAPON).get() > 0;
            case FEATURE_EIGHT_STONES:
                return featuresAppeared.get(FEATURE_FRAGMENT).get() > 7;
            case FEATURE_NO_ANY_WIN:
                return false;
            default:
                return true;
        }
    }

    public boolean isNeedHit() {
        return featureId == 11 || isNeedKill();
    }

    public boolean isNeedKill() {
        return featureId == 6 || featureId == 7 || featureId == 9;
    }

    public boolean isNeedNewWeapons() {
        return (featureId >= 30 && featureId <= 41)
                || featureId == FEATURE_KILL_AND_GET_WEAPON
                || featureId == FEATURE_DROP_TWO_WEAPONS_FROM_SPECTER
                || (featureId == FEATURE_SLOT_AND_WEAPON && feature.getFeaturesAppeared().get(FEATURE_RANDOM_WEAPON).get() < 1)
                || (featureId == FEATURE_DUAL_SLOT_AND_WEAPON && feature.getFeaturesAppeared().get(FEATURE_RANDOM_WEAPON).get() < 1);
    }

    public SpecialWeaponType getSpecialWeaponType() {
        switch (featureId) {
            case 34:
                return Plasma;
            case 35:
                return Landmines;
            case 36:
                return Railgun;
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
                .filter(sw -> sw.getAvailableGameIds().contains((int) GameType.DRAGONSTONE.getGameId()))
                .collect(Collectors.toList());
        return weapons.get(RNG.nextInt(weapons.size()));
    }

    public boolean isNeedTriggerSlot() {
        return featureId == 54 || (featureId >= 69 && featureId <= 73)
                || (featureId == FEATURE_DUAL_SLOT && feature.getFeaturesAppeared().get(FEATURE_SLOT).get() < 2)
                || (featureId == FEATURE_SLOT_AND_WEAPON && feature.getFeaturesAppeared().get(FEATURE_SLOT).get() < 1)
                || (featureId == FEATURE_DUAL_SLOT_AND_WEAPON && feature.getFeaturesAppeared().get(FEATURE_SLOT).get() < 2);
    }

    public int getSlotCombination() {
        return featureId >= 69 && featureId <= 73 ? featureId - 68 : 0;
    }

    public boolean isNeedDragonStoneFragmentDrop() {
        return featureId == 56
                || (featureId == FEATURE_RAGE_WITH_STONE && feature.getFeaturesAppeared().get(FEATURE_FRAGMENT).get() < 1)
                || (featureId == FEATURE_EIGHT_STONES && feature.getFeaturesAppeared().get(FEATURE_FRAGMENT).get() < 8);
    }

    public boolean isRage() {
        return featureId == FEATURE_RAGE || featureId == FEATURE_RAGE_WITH_IK
                || (featureId == FEATURE_RAGE_WITH_STONE && feature.getFeaturesAppeared().get(FEATURE_RAGE).get() < 1)
                || (featureId == FEATURE_DUAL_RAGE && feature.getFeaturesAppeared().get(FEATURE_RAGE).get() < 2);
    }

    public boolean isNotNeedAnyWin(){
        return featureId == FEATURE_NO_ANY_WIN;
    }

    public boolean isRageWithIK() {
        return featureId == FEATURE_RAGE_WITH_IK;
    }

    public boolean isDropTwoWeapons() {
        return featureId == FEATURE_DROP_TWO_WEAPONS_FROM_SPECTER;
    }

    public void countSlot() {
        incrementCounter(FEATURE_SLOT);
    }

    public void countRage() {
        incrementCounter(FEATURE_RAGE);
    }

    public void countRandomWeapon() {
        incrementCounter(FEATURE_RANDOM_WEAPON);
    }

    public void countFragment() {
        incrementCounter(FEATURE_FRAGMENT);
    }

    private void incrementCounter(int featureId) {
        if (hasFeature()) {
            Map<Integer, AtomicInteger> featuresAppeared = feature.getFeaturesAppeared();
            if (featuresAppeared.containsKey(featureId)) {
                featuresAppeared.get(featureId).incrementAndGet();
            }
        }
    }

    @Override
    public String toString() {
        return "MaxCrashStoneTestStand{" +
                "feature=" + feature +
                '}';
    }
}
