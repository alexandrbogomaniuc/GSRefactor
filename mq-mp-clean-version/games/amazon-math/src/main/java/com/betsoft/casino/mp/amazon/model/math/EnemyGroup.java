package com.betsoft.casino.mp.amazon.model.math;


import com.betsoft.casino.mp.model.IEnemyGroup;
import com.betsoft.casino.mp.model.IEnemyType;
import java.util.*;
import static com.betsoft.casino.mp.amazon.model.math.EnemyType.*;

public enum EnemyGroup implements IEnemyGroup {

    SCARABS(1, "Scarabs", Arrays.asList(ANT, WASP, SNAKE)),
    MUMMIES(2, "Mummies", Arrays.asList(
            SKULL_BREAKER, SHAMAN, JUMPER, RUNNER, JAGUAR, WEAPON_CARRIER, EXPLODER, MULTIPLIER)),
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
