package com.betsoft.casino.mp.common.math;

import com.betsoft.casino.mp.model.SpecialWeaponType;

import java.util.HashMap;
import java.util.Map;

import static com.betsoft.casino.mp.model.SpecialWeaponType.DoubleStrengthPowerUp;

public class WeaponCompensation {

    private static Map<SpecialWeaponType, double[]> weights = new HashMap<>();
    private static Map<SpecialWeaponType, double[]> prizes = new HashMap<>();

    static {
        weights.put(DoubleStrengthPowerUp, new double[]{90.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0});
        prizes.put(DoubleStrengthPowerUp, new double[]{0.0, 2.0, 3.0, 5.0, 7.0, 10.0, 20.0, 50.0});

    }

    public static double[] getWeights(SpecialWeaponType key) {
        return weights.get(key);
    }

    public static double getPrize(SpecialWeaponType key, int index) {
        return prizes.get(key)[index];
    }
}
