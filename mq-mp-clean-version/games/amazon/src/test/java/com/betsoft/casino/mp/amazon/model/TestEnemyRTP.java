package com.betsoft.casino.mp.amazon.model;

import com.betsoft.casino.mp.model.gameconfig.GameTools;
import com.dgphoenix.casino.common.util.RNG;

import java.util.HashMap;
import java.util.Map;

public class TestEnemyRTP {
    static final double IK_EV_PERCENT = 0.01;
    static final double CH_EV_PERCENT = 0.02;
    static final double PAY_HIT_PERCENT = 0.1;

    static final Map<Long,Double> enemyHiMap = new HashMap<>();
    static final Map<Long,Double> enemyLowMap = new HashMap<>();

    static {
        enemyHiMap.put(0L, 2./100);
        enemyHiMap.put(5L, 40./100);
        enemyHiMap.put(10L, 30./100);
        enemyHiMap.put(15L, 20./100);
        enemyHiMap.put(20L, 8./100);

        enemyLowMap.put(0L, 4./100);
        enemyLowMap.put(10L, 96./100);
    }

    public static void main(String[] args) {
        int health = 4500;
        long currentHealth = 0;
        double betsNormal = 0;
        double winsNormal = 0;
        double winsCHNormal = 0;
        double betsIK = 0;
        double winsIK = 0;

        int cntTests = 1000000;
        int cntInstantKills = 0;
        int cntCriticalHits = 0;

        for (int i = 0; i < cntTests; i++) {
            currentHealth = health;
            while (currentHealth > 0) {
                double prob = IK_EV_PERCENT / (((double) currentHealth) * PAY_HIT_PERCENT);
                if (prob > 1) {
                    System.out.println("error : " + prob + " currentHealth: " + currentHealth);
                }
                boolean isIK = RNG.rand() < prob;
                if (isIK) {
                    betsIK++;
                    winsIK += (double) currentHealth * PAY_HIT_PERCENT;
                    cntInstantKills++;
                    currentHealth  = 0;
                } else {
                    betsNormal++;
                    Long hits = GameTools.getRandomNumberKeyFromMap(currentHealth >= 20 ? enemyHiMap : enemyLowMap);
                    double win =  PAY_HIT_PERCENT * (double) hits ;

                    if(RNG.rand() <= CH_EV_PERCENT){
                        cntCriticalHits++;
                        winsCHNormal += win * 2;
                        currentHealth = currentHealth -  2 * hits;
                    }else {
                        currentHealth = currentHealth -  hits;
                        winsNormal += win;
                    }
                }

            }
        }

        double totalBets = betsIK + betsNormal;
        double totalWin =winsNormal + winsIK + winsCHNormal;

        System.out.println("totalBets: " + totalBets);
        System.out.println("totalWin: " + totalWin);
        System.out.println("Total RTP: " + totalWin/totalBets);

        System.out.println("IK");
        System.out.println("cntInstantKills: " + cntInstantKills);
        System.out.println("winsIK: " + winsIK);
        System.out.println("betsIK: " + betsIK);
        System.out.println("winsIK/(totalBets): " + winsIK/ totalBets);

        System.out.println("Normal");
        System.out.println("betsNormal: " + betsNormal);
        System.out.println("winsNormal: " + winsNormal);
        System.out.println("winsCHNormal: " + winsCHNormal);
        System.out.println("cntCriticalHits: " + cntCriticalHits);
        System.out.println("(winsNormal)/totalBets: " + (winsNormal)/totalBets);
        System.out.println("(winsCHNormal)/totalBets: " + (winsCHNormal)/totalBets);
        System.out.println("(winsCHNormal + winsNormal)/totalBets: " + (winsCHNormal + winsNormal)/totalBets);



    }
}
