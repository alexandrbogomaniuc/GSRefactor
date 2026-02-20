package com.betsoft.casino.mp.piratesdmc.model;


import com.betsoft.casino.mp.model.IEnemyGroup;
import com.betsoft.casino.mp.model.IEnemyType;
import com.betsoft.casino.mp.piratescommon.model.math.EnemyType;

import java.util.*;

public enum EnemyGroup implements IEnemyGroup {

    SCARABS(1, "Scarabs", Arrays.asList(EnemyType.ENEMY_1, EnemyType.ENEMY_2, EnemyType.ENEMY_3, EnemyType.ENEMY_4, EnemyType.ENEMY_5, EnemyType.ENEMY_6, EnemyType.ENEMY_7, EnemyType.ENEMY_8)),
    MUMMIES(2, "Mummies", Arrays.asList(
            EnemyType.ENEMY_11, EnemyType.ENEMY_12, EnemyType.ENEMY_13, EnemyType.ENEMY_14, EnemyType.ENEMY_15, EnemyType.ENEMY_16, EnemyType.ENEMY_17, EnemyType.ENEMY_18, EnemyType.ENEMY_19, EnemyType.ENEMY_20, EnemyType.WEAPON_CARRIER)),
    GODS(3, "Gods", Collections.singletonList(EnemyType.Boss));

    private final int id;
    private final String name;
    private final List<IEnemyType> enemies;

    static Map<IEnemyType, EnemyGroup> enemyGroupMap = new HashMap<>();
    static Map<Integer, EnemyGroup> idsMap = new HashMap<>();

    static {
        Arrays
                .stream(EnemyGroup.values())
                .forEach(enemyGroup -> enemyGroup.enemies
                        .forEach(enemyType -> enemyGroupMap.put(enemyType, enemyGroup)));

        for (EnemyGroup group : values()) {
            idsMap.put(group.id, group);
        }
    }

    EnemyGroup(int id, String name, List<IEnemyType> enemies) {
        this.id = id;
        this.name = name;
        this.enemies = enemies;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<IEnemyType> getEnemies() {
        return enemies;
    }

    @Override
    public int getOrdinalValue() {
        return ordinal();
    }


    public static EnemyGroup getGroup(EnemyType type) {
        return enemyGroupMap.get(type);
    }

    public static EnemyGroup getById(int id) {
        return idsMap.get(id);
    }
}
