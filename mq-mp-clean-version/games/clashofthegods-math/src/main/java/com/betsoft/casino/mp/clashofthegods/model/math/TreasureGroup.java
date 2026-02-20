package com.betsoft.casino.mp.clashofthegods.model.math;

import com.betsoft.casino.mp.model.ITreasure;
import com.betsoft.casino.mp.model.ITreasureGroup;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.betsoft.casino.mp.clashofthegods.model.math.Treasure.*;

public enum TreasureGroup implements ITreasureGroup {

    NAUTICAL_ARTIFACTS(1, Arrays.asList(
    )),

    GEMS(2, Arrays.asList(
    )),

    TREASURE(3, Arrays.asList(
            WHEEL_KEY
    )),

    JEWELRY(4, Arrays.asList(
    ));

    private final int id;
    private final List<ITreasure> treasures;

    static Map<ITreasure, TreasureGroup> treasureGroupMap = new HashMap<>();
    static Map<Integer, TreasureGroup> idsMap = new HashMap<>();

    static {
        Arrays
                .stream(TreasureGroup.values())
                .forEach(treasureGroup -> treasureGroup.treasures
                        .forEach(treasure -> treasureGroupMap.put(treasure, treasureGroup)));

        for (TreasureGroup group : values()) {
            idsMap.put(group.getId(), group);
        }
    }

    TreasureGroup(int id, List<ITreasure> treasures) {
        this.id = id;
        this.treasures = treasures;
    }

    public int getId() {
        return id;
    }

    @Override
    public List<ITreasure> getTreasures() {
        return treasures;
    }

    @Override
    public int getOrdinalValue() {
        return ordinal();
    }

    public static TreasureGroup getGroup(Treasure treasure) {
        return treasureGroupMap.get(treasure);
    }

    public static TreasureGroup getById(int id) {
        return idsMap.get(id);
    }
}
