package com.betsoft.casino.mp.dragonstone.model.math.slot;

import com.betsoft.casino.mp.dragonstone.model.math.EnemyType;
import com.betsoft.casino.mp.dragonstone.model.math.MathData;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.dragonstone.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.model.SpecialWeaponType;

public class EnemyNameTests {
    public static void main(String[] args) {
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        System.out.println(MathData.getHitProbability(config, -1, EnemyType.CERBERUS, 0));
        System.out.println(MathData.getHitProbability(config, SpecialWeaponType.Flamethrower.getId(), EnemyType.CERBERUS, 0));
        System.out.println(MathData.getHitProbability(config, SpecialWeaponType.Flamethrower.getId(), EnemyType.CERBERUS, 0));

        int weaponId = SpecialWeaponType.Flamethrower.getId();
        Double rtpForDropWeapon = MathData.getRtpForDropWeapon(config, EnemyType.SKELETON_1, weaponId, 0);
        System.out.println("rtpForDropWeapon: " + rtpForDropWeapon);
        Double averageDamageForWeapon = MathData.getAverageDamageForWeapon(config, weaponId);
        System.out.println("averageDamageForWeapon: " + averageDamageForWeapon);
        double avgDropPrice = MathData.calculateAverageDropPrice(config);
        System.out.println(avgDropPrice);
        double probWeapon = rtpForDropWeapon / avgDropPrice / averageDamageForWeapon;
        System.out.println(probWeapon);
    }
}
