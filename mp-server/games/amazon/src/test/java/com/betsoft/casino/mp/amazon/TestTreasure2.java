package com.betsoft.casino.mp.amazon;

import com.betsoft.casino.mp.amazon.model.math.MathData;
import com.betsoft.casino.mp.amazon.model.math.Treasure;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class TestTreasure2 {

    public static void main(String[] args) {
        int cntShots = 10000000;
        Map<String, AtomicLong> stat = new HashMap<>();
        for (Treasure value : Treasure.values()) {
            stat.put(value.name(), new AtomicLong(0));
        }


        for (int i = 0; i < cntShots; i++) {
            Treasure randomTreasure = MathData.getRandomTreasure();
            stat.get(randomTreasure.name()).incrementAndGet();
        }

        stat.forEach((s, atomicLong) -> System.out.println(s + " " + (atomicLong.doubleValue() / cntShots)));

    }
}
