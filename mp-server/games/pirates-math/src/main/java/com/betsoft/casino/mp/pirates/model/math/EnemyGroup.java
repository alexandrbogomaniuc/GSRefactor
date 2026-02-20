package com.betsoft.casino.mp.pirates.model.math;


import com.betsoft.casino.mp.model.IEnemyGroup;
import com.betsoft.casino.mp.model.IEnemyType;
import java.util.*;
import static com.betsoft.casino.mp.pirates.model.math.EnemyType.*;

public enum EnemyGroup implements IEnemyGroup {

    SCARABS(1, "Scarabs", Arrays.asList(ENEMY_1, ENEMY_2, ENEMY_3, ENEMY_4, ENEMY_5, ENEMY_6, ENEMY_7, ENEMY_8)),
    MUMMIES(2, "Mummies", Arrays.asList(
            ENEMY_11, ENEMY_12, ENEMY_13, ENEMY_14, ENEMY_15, ENEMY_16, ENEMY_17, ENEMY_18, ENEMY_19, ENEMY_20, WEAPON_CARRIER)),
    GODS(3, "Gods", Collections.singletonList(Boss));

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
