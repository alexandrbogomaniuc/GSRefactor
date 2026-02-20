package com.betsoft.casino.mp.piratescommon.model.math;

import com.betsoft.casino.mp.model.ITreasure;
import com.betsoft.casino.mp.model.TreasureRarity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.model.TreasureRarity.*;

public enum Treasure implements ITreasure {
    // @formatter:off

    BRONZE_CHEST(1, COMMON),
    SILVER_CHEST(2, COMMON),
    GOLD_CHEST(3, COMMON);

    // @formatter:on

    int id;
    TreasureRarity rarity;

    static Map<TreasureRarity, List<ITreasure>> treasureRarityMap = new HashMap<>();
    static Map<Integer, Treasure> treasureIdsMap = new HashMap<>();

    static {
        Arrays.stream(TreasureRarity.values()).forEach(rarity ->
            treasureRarityMap.put(rarity,
                    Arrays.stream(values())
                            .filter(treasure -> treasure.getRarity().equals(rarity))
                            .collect(Collectors.toList())));

        for (Treasure treasure : values()) {
            treasureIdsMap.put(treasure.getId(), treasure);
        }
    }

    Treasure(int id, TreasureRarity rarity) {
        this.id = id;
        this.rarity = rarity;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public TreasureRarity getRarity() {
        return rarity;
    }

    @Override
    public int getOrdinalValue() {
        return ordinal();
    }

    public static List<ITreasure> getTreasures(TreasureRarity rarity) {
        return treasureRarityMap.get(rarity);
    }

    public static Treasure getById(int id) {
        return treasureIdsMap.get(id);
    }


}
