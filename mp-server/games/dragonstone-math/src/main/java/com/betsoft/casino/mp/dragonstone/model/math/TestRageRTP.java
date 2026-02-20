package com.betsoft.casino.mp.dragonstone.model.math;

import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.model.SpecialWeaponType;

public class TestRageRTP {
    public static void main(String[] args) {
        //TestOne();
    }

    private static void TestOne() {
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        long cnt = 1000000;
        long tBets = 0;
        double tWins = 0;

        for (int i = 0; i < cnt; i++) {
//            int totalRagePayout = MathData.getTotalRagePayout(config, SpecialWeaponType.Flamethrower.getId(), RoomMode.NORMAL);
//            tWins+=totalRagePayout;
//            tBets++;
        }

        System.out.println("tBets: " + tBets);
        System.out.println("tWins: " + tWins);
        System.out.println("RTP: " + tWins/tBets);
    }
}
