package com.betsoft.casino.mp.revengeofra;

import com.betsoft.casino.mp.revengeofra.model.math.EnemyType;

public class TestNames {
    public static void main(String[] args) {
        for (EnemyType value : EnemyType.values()) {
            System.out.println("Enemy " + (value.getId() + 1) + "  " + value.getName() + "  " + value.getReward());
        }

        for (EnemyType value : EnemyType.values()) {
            System.out.println(value.getName());
        }



    }
}
