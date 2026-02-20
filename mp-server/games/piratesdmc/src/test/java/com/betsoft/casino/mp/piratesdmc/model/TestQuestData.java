package com.betsoft.casino.mp.piratesdmc.model;

import com.betsoft.casino.mp.piratescommon.model.math.MathData;
import com.betsoft.casino.mp.piratescommon.model.math.MathQuestData;
import com.betsoft.casino.mp.piratescommon.model.math.Treasure;

public class TestQuestData {
    public static void main(String[] args) {
        int cntDrop = 0;
        int cntAll = 10000000;
//        for (int i = 0; i < cntAll; i++) {
//            Treasure treasure = MathQuestData.getTreasure(2, false);
//            if(treasure!=null)
//                cntDrop++;
//        }

        System.out.println("cntDrop: " + cntDrop + " freq: " + cntAll/cntDrop);

    }
}
