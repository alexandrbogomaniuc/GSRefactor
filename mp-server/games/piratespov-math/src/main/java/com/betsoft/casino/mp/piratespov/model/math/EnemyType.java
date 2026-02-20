package com.betsoft.casino.mp.piratespov.model.math;

import com.betsoft.casino.mp.model.Skin;
import com.betsoft.casino.mp.model.IEnemyType;
import java.util.*;

//        Weapon Carrier: Doomed Cannoneer
//        Troll: Berserk Abomination
//        Mini Troll: Raging Marauder
//        Sword Runner: Charging Corsair
//        Bag Runner: Crazed Looter
//        Red Captain: Cursed First Mate
//        White Captain: Risen Bosun
//        Purple Pirate: Undying Raider
//        Yellow Pirate: Decaying Pirate
//        Green Deckhand: Zombie Crewman
//        Blue Deckhand: Drowned Deckhand
//        Red Bird: Tainted Albatross
//        White Bird: Mutated Gull
//        Ruby Crab: Ruby Crab
//        Sapphire Crab: Sapphire Crab
//        Emerald Crab: Emerald Crab
//        Black Crab: Carrion Crab
//        Brown Crab: Scuttling Swarmer
//        White Rat: Corrupted Rat
//        Black Rat: Plagued Rat
//        Brown Rat: Devouring Rat

public enum EnemyType implements IEnemyType<EnemyPrize> {

    ENEMY_1(0, "Devouring Rat", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.5f, 0.f,0.f)), 2, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    ENEMY_2(1, "Plagued Rat", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.5f, 0.f,0.f)), 3, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    ENEMY_3(2, "Corrupted Rat", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.5f, 0.f,0.f)), 4, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    ENEMY_4(3, "Scuttling Swarmer", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(1.2075f, 0.f,0.f)), 5, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    ENEMY_5(4, "Carrion Crab", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(1.2075f, 0.f,0.f)), 7, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    ENEMY_6(5, "Sapphire Crab", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(1.2075f, 0.f, 0.f)
            ), 8, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    ENEMY_7(6, "Emerald Crab", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(1.2075f, 0.f,0.f)), 10, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    ENEMY_8(7, "Ruby Crab", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(1.2075f, 0.f,0.f)), 12, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),


    ENEMY_9(8, "Mutated Gull", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.4f, 0.f, 0.f)),
            30, new double[]{0.12, 0.03, 0.02},
            new ChestProb(0.08, new double[]{0.45, 0.40, 0.10, 0.05}), false
    ),

    ENEMY_10(9, "Tainted Albatross", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.4f,0.f, 0.f)),
            35, new double[]{0.12, 0.03, 0.02},
            new ChestProb(0.08, new double[]{0.45, 0.40, 0.10, 0.05}), false
    ),

    ENEMY_11(10, "Zombie Crewman", 2, 4, (short) 0,
            Collections.singletonList(new Skin(1.8f,0.f, 0.0f)),
            15, new double[]{0.12, 0.03, 0.02},
            new ChestProb(0.08, new double[]{0.45, 0.40, 0.10, 0.05}), false
    ),

    ENEMY_12(11, "Drowned Deckhand", 2, 4, (short) 0,
            Collections.singletonList(new Skin(1.68f,0.f, 0.f)),
            18, new double[]{0.12, 0.03, 0.02},
            new ChestProb(0.08, new double[]{0.45, 0.40, 0.10, 0.05}), false
    ),

    ENEMY_13(12, "Decaying Pirate", 2, 4, (short) 0,
            Collections.singletonList(new Skin(1.1f,0.f, 0.0f)),
            20, new double[]{0.12, 0.03, 0.02},
            new ChestProb(0.08, new double[]{0.45, 0.40, 0.10, 0.05}), false
    ),

    ENEMY_14(13, "Undying Raider", 2, 4, (short) 0,
            Collections.singletonList(new Skin(1.1f,0.f, 0.0f)),
            25, new double[]{0.12, 0.03, 0.02},
            new ChestProb(0.08, new double[]{0.45, 0.40, 0.10, 0.05}), false
    ),

    ENEMY_15(14, "Risen Bosun", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(2.2f, 0.f,0.f)),
            40, new double[]{0.1, 0.02, 0.01},
            new ChestProb(0.08, new double[]{0.50, 0.20, 0.20, 0.05, 0.05}), false
    ),


    ENEMY_16(15, "Cursed First Mate", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(2.2f, 0.f,0.f)),
            45, new double[]{0.1, 0.02, 0.01},
            new ChestProb(0.08, new double[]{0.50, 0.20, 0.20, 0.05, 0.05}), false
    ),

    ENEMY_17(16, "Charging Corsair", 2, 4,
            (short) 0, Collections.singletonList(new Skin(7.5f, 0, 0.f)),
            50, new double[]{0.12, 0.01, 0.01},
            new ChestProb(0.08, new double[]{0.70, 0.10, 0.05, 0.05, 0.05, 0.05}), true
    ),

    ENEMY_18(17, "Crazed Looter", 2, 4,
            (short) 0, Collections.singletonList(new Skin(7.5f, 0, 0.f)),
            55, new double[]{0.12, 0.01, 0.01},
            new ChestProb(0.08, new double[]{0.70, 0.10, 0.05, 0.05, 0.05, 0.05}), true
    ),


    ENEMY_19(18, "Raging Marauder", 2, 4,
            (short) 0,
            Collections.singletonList(new Skin(10, 0.f, 0.f)),
            60, new double[]{0.12, 0.01, 0.01},
            new ChestProb(0.08, new double[]{0.70, 0.10, 0.05, 0.05, 0.05, 0.05}), true
    ),


    ENEMY_20(19, "Berserk Abomination", 2, 4,
            (short) 0,
            Collections.singletonList(new Skin(10, 0, 0.f)),
            70, new double[]{0.12, 0.01, 0.01},
            new ChestProb(0.08, new double[]{0.70, 0.10, 0.05, 0.05, 0.05, 0.05}), true
    ),

    WEAPON_CARRIER(20, "Doomed Cannoneer", 2, 4,
            (short) 0, Collections.singletonList(new Skin(4.4f, 0, 0.f)),
            0, new double[]{0.12, 0.01, 0.01},
            new ChestProb(0.08, new double[]{0.70, 0.10, 0.05, 0.05, 0.05, 0.05}), true
    ),

    // Bosses
    Boss(21, "Boss",
            2, 4,
            (short) 10,
            true,
            Arrays.asList(new Skin(1.1f, 0, 0),
                    new Skin(1.3f, 0, 0),
                    new Skin(1.f, 0, 0)),
            15,
            new double[]{0.00, 0.30, 0.30, 0.00, 0.40},
            new ChestProb(0.25, new double[]{0.00, 0.40, 0.30, 0.15, 0.05, 0.05, 0.05}),
            false
    );


    public static final int MAX_SKINS = 100;

    private final int id;
    private String name;
    private int width;
    private int height;
    private EnumMap<EnemyPrize, Long> payTable;

    //sumAward > 0 for regular, 0 for Boss
    private long sumAward;
    //energy > 0 for Boss, 0 for regular
    private boolean boss;
    private List<Skin> skins;
    private final int reward;
    private final double[] treasureDropRates;
    private final ChestProb chestProb;
    boolean isHVenemy;

    private static final Map<Integer, EnemyType> idsMap = new HashMap<>();

    static {
        for (EnemyType type : values()) {
            idsMap.put(type.id, type);
        }
    }

    EnemyType(int id, String name, int width, int height, short energy, List<Skin> skins,
              int reward, double[] treasureDropRates, ChestProb chestProb, boolean isHVenemy) {
        this(id, name, width, height,  energy, false, skins, reward,
                treasureDropRates, chestProb, isHVenemy);
    }

    EnemyType(int id, String name, int width, int height, short energy, boolean boss,
              List<Skin> skins, int reward, double[] treasureDropRates, ChestProb chestProb, boolean isHVenemy) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.payTable = new EnumMap<>(EnemyPrize.class);
        this.sumAward = 0;
        this.boss = boss;
        this.skins = skins;
        this.reward = reward;
        this.treasureDropRates = treasureDropRates;
        this.chestProb = chestProb;
        this.isHVenemy = isHVenemy;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getMaxSkins() {
        return MAX_SKINS;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Map<EnemyPrize, Long> getPayTable() {
        return payTable;
    }

    @Override
    public double getSumAward() {
        return sumAward;
    }

    @Override
    public boolean isBoss() {
        return boss;
    }

    @Override
    public List<Skin> getSkins() {
        return skins;
    }

    @Override
    public Skin getSkin(int skin) {
        return skins.get(skin - 1);
    }

    @Override
    public int getReward() {
        return reward;
    }

    @Override
    public double[] getTreasureDropRates() {
        return treasureDropRates;
    }

    @Override
    public ChestProb getChestProb() {
        return chestProb;
    }

    @Override
    public boolean isHVenemy() {
        return isHVenemy;
    }

    public static EnemyType getById(int id) {
        return idsMap.get(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EnemyType [");
        sb.append("id=").append(getId());
        sb.append(", name='").append(name).append('\'');
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        sb.append(", payTable=").append(payTable);
        sb.append(", sumAward=").append(sumAward);
        sb.append(", boss=").append(boss);
        sb.append(", skins=").append(skins);
        sb.append(", reward=").append(reward);
        sb.append(", treasureDropRates=").append(Arrays.toString(treasureDropRates));
        sb.append(", chestProb").append(chestProb);
        sb.append(", isHVenemy").append(isHVenemy);
        sb.append(']');
        return sb.toString();
    }
}
