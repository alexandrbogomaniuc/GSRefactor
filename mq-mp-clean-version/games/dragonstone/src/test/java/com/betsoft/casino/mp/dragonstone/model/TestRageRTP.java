package com.betsoft.casino.mp.dragonstone.model;

import com.betsoft.casino.mp.dragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.dragonstone.model.math.MathData;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.model.Money;
import com.dgphoenix.casino.common.util.RNG;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class TestRageRTP {
    public static void main(String[] args) {
        TestRageRTP testRageRTP = new TestRageRTP();
        //testRageRTP.testRtpRage(-1, RoomMode.NORMAL);
        testRageRTP.testRtpRage(-1, 1);
       // testRageRTP.testRagePayout();
       // testRageRTP.testRtpRage(-1, 1);
    }


    void testRagePayout() {
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        long cntTests = 2000000;
        Map<Integer, AtomicLong> payouts = new HashMap<>();


        for (int i = 0; i < cntTests; i++) {
            int randomRagePayouts = MathData.getRandomRagePayouts(config);
            AtomicLong payoutsOrDefault = payouts.getOrDefault(randomRagePayouts, new AtomicLong());
            payoutsOrDefault.incrementAndGet();
            payouts.put(randomRagePayouts, payoutsOrDefault);

        }

        AtomicLong totalPayout = new AtomicLong();
        AtomicLong cnt2 = new AtomicLong();

        payouts.forEach((integer, atomicLong) -> {
            System.out.println(integer + "  = " + atomicLong.get());
            cnt2.addAndGet(atomicLong.get());
            totalPayout.addAndGet(atomicLong.get() * integer);
        });

        System.out.println("cnt: " + cnt2);
        System.out.println("total payout: " + totalPayout);
        System.out.println("average payout: " + (double) totalPayout.get()/cnt2.get());
    }

    void testRtpRage(int weaponTypeId, int idxForOgre) {
        GameConfig config = new GameConfigLoader().loadDefaultConfig();

        long cntTests = 2000000;

        EnemyType enemyType = EnemyType.OGRE;
        Money stake = Money.fromCents(20);
        int betLevel = 1;
        long totalBet = 0;
        double totalWin = 0;


        for (int i = 0; i < cntTests; i++) {

            totalBet += weaponTypeId == -1 ? stake.toDoubleCents() :
                    stake.getWithMultiplier(MathData.getPaidWeaponCost(config, weaponTypeId)).toDoubleCents();
            double hitBaseRageProbability = MathData.getHitProbability(config, weaponTypeId, enemyType, idxForOgre);
            boolean isHit = RNG.rand() < hitBaseRageProbability;
            if(isHit) {
                int enemyPayout = idxForOgre == 1 ? MathData.getRandomRagePayouts(config) :
                        MathData.getEnemyPayout(config, enemyType, weaponTypeId, idxForOgre);
                int chMult = MathData.getRandomMultForWeapon(config, weaponTypeId);
                totalWin += stake.getWithMultiplier(enemyPayout * chMult * betLevel).toDoubleCents();
            }

        }
        System.out.println("totalBet: " + totalBet + ", totalWin: " + totalWin + ", RTP: " + totalWin / totalBet);
    }
}
