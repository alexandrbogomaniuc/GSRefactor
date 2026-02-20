package com.betsoft.casino.mp.clashofthegods;

import com.betsoft.casino.mp.clashofthegods.model.math.EnemyType;
import com.betsoft.casino.mp.clashofthegods.model.math.MathData;
import com.betsoft.casino.mp.clashofthegods.model.math.PayTableInst;
import com.betsoft.casino.mp.common.math.Paytable;

public class TestNames {
    public static void main(String[] args) {
        for (EnemyType value : EnemyType.values()) {
            System.out.println(value.getId() + "  " + value.getName());
        }

        for (EnemyType value : EnemyType.values()) {
            System.out.println(value.getName());
        }


        System.out.println(MathData.getRtpWeapons());
        System.out.println(MathData.getRtpWeaponsAll());

        System.out.println(PayTableInst.getTable());
    }
}
