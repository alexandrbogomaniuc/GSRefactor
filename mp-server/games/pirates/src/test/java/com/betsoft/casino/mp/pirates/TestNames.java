package com.betsoft.casino.mp.pirates;

import com.betsoft.casino.mp.model.SpecialWeaponType;
import com.betsoft.casino.mp.pirates.model.math.EnemyType;
import com.betsoft.casino.mp.pirates.model.math.MathData;

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
