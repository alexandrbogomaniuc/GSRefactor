package com.betsoft.casino.mp.bgsectorx.model;

import java.util.Arrays;
import java.util.Optional;

public enum EnemyCircularRadius {
    S1(0, 30),
    S2(1, 30),
    S3(2, 30),
    S4(3, 30),
    S5(4, 44),
    S6(5, 52),
    S7(6,56),
    S8(7,56),
    S9(8,56),
    S10(9, 50),
    S11(10,52),
    S12(11,60),
    S13(12,54),
    S14(13,66),
    S15(14,66),
    S16(15,58),
    S17(16,66),
    S18(17,66),
    S19(18,66),
    S20(19,66),
    S21(20,64),
    S22(21,64),
    S23(22,74),
    S24(23,74),
    S25(24,70),
    S26(25,76),
    S27(26,70),
    S28(27,66),
    S29(28,76),
    S30(29,82),
    S31(30,84);

    private final int id;
    private final int radius;

    EnemyCircularRadius(int id, int radius) {
        this.id = id;
        this.radius = radius;
    }

    public int getId() {
        return id;
    }

    public int getRadius() {
        return radius;
    }

    public static Optional<EnemyCircularRadius> getRadiusByID(int typeId){
        return Arrays.stream(EnemyCircularRadius.values()).filter(item -> item.getId() == typeId).findFirst();
    }

    @Override
    public String toString() {
        return "EnemyCircularRadius{" +
                "id=" + id +
                ", radius=" + radius +
                '}';
    }
}
