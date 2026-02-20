package com.betsoft.casino.mp.pirates;

import com.betsoft.casino.mp.pirates.model.math.MathData;
import com.betsoft.casino.mp.pirates.model.math.MathQuestData;
import com.betsoft.casino.mp.pirates.model.math.Treasure;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.RNG;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;

public class TestQuests {

    public static void main(String[] args) {
        doTest1();
//        doTest2(Treasure.SILVER_CHEST);
//        for (Treasure value : Treasure.values()) {
//            System.out.println("value: " + value.name() + "   " +  MathQuestData.getProbabilityOkKey(value.getId()));
//        }

    }

    private static void doTest2(Treasure treasure) {

        long cntTests = 100000000;
        double totalWin = 0;
        for (int i = 0; i < cntTests; i++) {
            Pair<Integer, Integer> randomWin = MathQuestData.getRandomWeapon(treasure.getId());
            Double rtpForWeapon = MathData.getRtpForWeapon(randomWin.getKey()) / 100;
            double multiplier = new BigDecimal(randomWin.getValue(), MathContext.DECIMAL32)
                    .multiply(new BigDecimal(rtpForWeapon, MathContext.DECIMAL32))
                    .multiply(new BigDecimal(MathData.getAverageDamageForWeapon(randomWin.getKey()), MathContext.DECIMAL32))
                    .doubleValue();
            totalWin += multiplier;
        }

        System.out.println(totalWin + " averageWin: " + totalWin/cntTests);
    }

    private static void doTest1() {
        long cntTests = 100000000;
        double totalBet = 0;
        double[] totalWinQuests = new double[] { 0, 0, 0 } ;
        double[] totalSpecialWins = new double[] { 0, 0, 0 } ;

        int[] wins = {20, 50, 150};
        int[] treasures = new int[]{0, 0, 0};
        double[] probKeys = new double[]{0., 0., 0.};
        long[] questsCompleted = new long[]{0, 0, 0};

        for (int i = 0; i < cntTests; i++) {
            totalBet += 1;
            Arrays.stream(Treasure.values()).forEach(treasure -> {
                if (RNG.rand() < MathQuestData.getProbabilityOkKey(treasure.getId())) {
                    int idx = treasure.getId() - 1;
                    probKeys[idx]++;
                    treasures[idx]++;
                    if(treasures[idx] == 3) {
                        questsCompleted[idx]++;
                        Pair<Integer, Integer> randomWin = MathQuestData.getRandomWeapon(treasure.getId());
                        totalWinQuests[idx] += wins[idx];
                        Double rtpForWeapon = MathData.getRtpForWeapon(randomWin.getKey()) / 100;
                        double multiplier = new BigDecimal(randomWin.getValue(), MathContext.DECIMAL32)
                                .multiply(new BigDecimal(rtpForWeapon, MathContext.DECIMAL32))
                                .multiply(new BigDecimal(MathData.getAverageDamageForWeapon(randomWin.getKey()), MathContext.DECIMAL32))
                                .doubleValue();
                        totalSpecialWins[idx] += multiplier;
                        treasures[idx] = 0;
                    }
                }
            });
        }


        System.out.println("totalBet:  " + totalBet);
        System.out.println("totalWinQuests:  " + Arrays.toString(totalWinQuests));
        System.out.println("totalSpecialWins:  " + Arrays.toString(totalSpecialWins));

        double totalRTP = 0;
        double totalCache = 0;
        double totalSW = 0;

        System.out.println("by treasures");
        for (int i = 0; i < treasures.length; i++) {
            double rtpCache = totalWinQuests[i] / totalBet;
            double rtpSW = totalSpecialWins[i] / totalBet;
            double commonRTP  = rtpCache + rtpSW;
            totalRTP+=commonRTP;
            totalCache += rtpCache;
            totalSW += rtpSW;

            System.out.println("treasure: " + i + " cacheWin RTP: "
                    + rtpCache
                    + " SW RTP: " + rtpSW
                    + " common RTP: " + commonRTP
                    + " prob treasure: " + probKeys[i]/cntTests +  " (" +    cntTests/probKeys[i] + ")"
            );
        }

        System.out.println("Total RTP: "+ totalRTP);
        System.out.println("Total Cache RTP: "+ totalCache);
        System.out.println("Total SW RTP: "+ totalSW);
        System.out.println("questsCompleted: " + Arrays.toString(questsCompleted));
    }

}
