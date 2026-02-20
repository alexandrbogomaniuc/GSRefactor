package com.betsoft.casino.mp.amazon;

import com.betsoft.casino.mp.amazon.model.math.MathData;

public class TestBossParams {
    public static void main(String[] args) {
        int weaponTypeId = 4;
        int skinId = 3;
        double hitProbabilityForBoss = MathData.getHitProbabilityForBoss(weaponTypeId, skinId);
        System.out.println(hitProbabilityForBoss);
    }
}
