package com.betsoft.casino.mp.bgdragonstone.model.math.slot;

import com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.bgdragonstone.model.math.MathData;
import com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.model.SpecialWeaponType;

public class EnemyNameTests {
    public static void main(String[] args) {
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        System.out.println(MathData.getHitProbability(config, -1, EnemyType.CERBERUS, 0));
        System.out.println(MathData.getHitProbability(config, SpecialWeaponType.Flamethrower.getId(), EnemyType.CERBERUS, 0));
        System.out.println(MathData.getHitProbability(config, SpecialWeaponType.Flamethrower.getId(), EnemyType.CERBERUS, 0));
    }
}
