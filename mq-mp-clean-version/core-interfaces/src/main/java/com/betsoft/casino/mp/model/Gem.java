package com.betsoft.casino.mp.model;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;

public enum Gem {
    DIAMOND(0),
    RUBY(1),
    EMERALD(2),
    SAPPHIRE(3),
    TOPAZ(4);

    private static Map<Integer, Gem> gemMap = Maps.newHashMap();
    private int id;

    static {
        Arrays.stream(values()).forEach(gem -> gemMap.put(gem.getId(), gem));
    }

    Gem(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Gem getById(int id) {
        return gemMap.getOrDefault(id, SAPPHIRE);
    }
}
