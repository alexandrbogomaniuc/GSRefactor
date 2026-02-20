package com.betsoft.casino.mp.piratescommon.model.math;
import com.dgphoenix.casino.common.util.RNG;
import com.google.common.util.concurrent.AtomicDouble;
import java.util.HashMap;
import java.util.Map;

public class MathQuestData {

    public static final Map<Integer, Double> questsCacheWins;
    public static final Map<Treasure, Double> questsKeysWeights;
    public static final double weightedValue;

    static {
        questsCacheWins = new HashMap<>();
        questsCacheWins.put(Treasure.BRONZE_CHEST.id, 100.);
        questsCacheWins.put(Treasure.SILVER_CHEST.id, 200.);
        questsCacheWins.put(Treasure.GOLD_CHEST.id, 500.);

        questsKeysWeights = new HashMap<>();
        questsKeysWeights.put(Treasure.BRONZE_CHEST, 5.);
        questsKeysWeights.put(Treasure.SILVER_CHEST, 2.);
        questsKeysWeights.put(Treasure.GOLD_CHEST, 1.);

        AtomicDouble totalWeightedValue = new AtomicDouble(0);
        double sum = questsKeysWeights.values().stream().mapToDouble(Double::doubleValue).sum();
        questsKeysWeights.forEach((treasure, aDouble) -> {
            totalWeightedValue.addAndGet((questsCacheWins.get(treasure.getId()))* questsKeysWeights.get(treasure) / sum);
        });
        weightedValue = totalWeightedValue.doubleValue();
    }

    public static boolean isKeyAppeared(int betLevel, boolean isWeaponShot) {
        double betContribution = isWeaponShot ? 0.5 * betLevel : 0.1 * betLevel;
        double probQuestComplete = betContribution/weightedValue;
        double probKey = probQuestComplete * 3;
        return RNG.rand() < probKey;
    }
}
