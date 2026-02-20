package com.betsoft.casino.mp.bgdragonstone.model.math;

import com.betsoft.casino.mp.model.IEnemyRange;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType.*;
import static com.betsoft.casino.mp.bgdragonstone.model.math.EnemyType.BROWN_RAT;

public enum EnemyRange implements IEnemyRange<EnemyType> {
    BASE_ENEMIES(Arrays.asList(
            BROWN_SPIDER,
            BLACK_SPIDER,
            BROWN_RAT,
            BLACK_RAT,
            BAT,
            RAVEN,
            SKELETON_1,
            IMP_1,
            IMP_2,
            SKELETON_SHIELD,
            GOBLIN,
            HOBGOBLIN,
            DUP_GOBLIN,
            GARGOYLE,
            ORC,
            EMPTY_ARMOR_1,
            EMPTY_ARMOR_2,
            EMPTY_ARMOR_3,
            RED_WIZARD,
            BLUE_WIZARD,
            PURPLE_WIZARD,
            OGRE,
            DARK_KNIGHT,
            CERBERUS,
            SPIRIT_SPECTER,
            FIRE_SPECTER,
            LIGHTNING_SPECTER
    )),

    LARGE_ENEMIES(Arrays.asList(
            OGRE,
            DARK_KNIGHT,
            CERBERUS)),

    SPECTERS(Arrays.asList(
            FIRE_SPECTER,
            LIGHTNING_SPECTER,
            SPIRIT_SPECTER)),

    WIZARDS(Arrays.asList(
            RED_WIZARD,
            BLUE_WIZARD,
            PURPLE_WIZARD)),

    EMPTY_ARMORS_RANGE(Arrays.asList(
            EMPTY_ARMOR_1,
            EMPTY_ARMOR_2,
            EMPTY_ARMOR_3)),

    FLYING_ENEMIES(Arrays.asList(
            BAT,
            RAVEN,
            GARGOYLE)),

    SKELETONS_RANGE(Arrays.asList(SKELETON_1, SKELETON_SHIELD)),
    SPIDERS_RANGE(Arrays.asList(BLACK_SPIDER, BROWN_SPIDER)),
    RATS_RANGE(Arrays.asList(BLACK_RAT, BROWN_RAT)),
    IMPS_RANGE(Arrays.asList(IMP_1, IMP_2)),

    BOSS(Collections.singletonList(EnemyType.DRAGON));

    private List<EnemyType> enemies;

    @Override
    public List<EnemyType> getEnemies() {
        return enemies;
    }

    EnemyRange(List<EnemyType> enemies) {
        this.enemies = enemies;
    }

    public boolean contains(EnemyType enemyType) {
        return getEnemies().contains(enemyType);
    }
}
