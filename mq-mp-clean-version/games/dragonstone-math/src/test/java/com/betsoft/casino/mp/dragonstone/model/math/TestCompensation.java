package com.betsoft.casino.mp.dragonstone.model.math;

import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.model.SpecialWeaponType;

public class TestCompensation {
    public static void main(String[] args) {
        GameConfig gameConfig = new GameConfigLoader().loadDefaultConfig();
        double fullWeaponRTP = MathData.calculateFullWeaponRTP(gameConfig, SpecialWeaponType.Railgun.getId());
        System.out.println("fullWeaponRTP:" + fullWeaponRTP);

    }
}
