package com.betsoft.casino.mp.amazon.model;

import com.betsoft.casino.mp.amazon.model.math.EnemyData;
import com.betsoft.casino.mp.amazon.model.math.EnemyType;
import com.betsoft.casino.mp.amazon.model.math.MathData;
import com.betsoft.casino.mp.amazon.model.math.WeaponData;
import com.betsoft.casino.mp.model.Money;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.dgphoenix.casino.common.util.RNG;

public class TestSmall {
    public static void main(String[] args) {
        int weaponTypeId = -1;
        int levelId = 0;
        double currentEnergy = 10;

        EnemyType enemyType = EnemyType.EXPLODER;
        EnemyData enemyData = MathData.getEnemyData(enemyType.getId());
        double rtpForWeapon = MathData.getRtpForWeapon(weaponTypeId);
        Double avgPayout = enemyData.getSwAvgPayouts(weaponTypeId, 0, MathData.PAY_HIT_PERCENT);
        double hitProbability = rtpForWeapon / avgPayout;
        boolean isHit = RNG.rand() < hitProbability;

        WeaponData weaponData = enemyData.getWeaponDataMap(weaponTypeId, levelId);

        double instanceKillEV = weaponData.getInstanceKillEV() / 100;
        double prob = instanceKillEV / ((currentEnergy) * MathData.PAY_HIT_PERCENT);

        boolean isIK = RNG.rand() < prob;

        System.out.println("instanceKillEV: " + instanceKillEV + " prob: " + prob);


        int numberShots = 20;
        Money stake = Money.fromCents(10);
        SpecialWeaponType plasma = SpecialWeaponType.Plasma;
        Double rtpForWeaponPlasma = MathData.getRtpForWeapon(plasma.getId()) / 100;

        Double averageDamageForWeapon = MathData.getAverageDamageForWeapon(plasma.getId());
        Money compensation = stake.getWithMultiplier(rtpForWeaponPlasma * averageDamageForWeapon);

        System.out.println("rtpForWeaponPlasma: " + rtpForWeaponPlasma);
        System.out.println("averageDamageForWeapon: " + averageDamageForWeapon);
        System.out.println("compensation for one shot: " + compensation.toDoubleCents()/100);
        System.out.println("compensation for all shot: " + (compensation.toDoubleCents() * numberShots)/100);

    }
}
