package com.betsoft.casino.mp.amazon.model.math;

import com.betsoft.casino.mp.model.IEnemyType;
import com.betsoft.casino.mp.model.Skin;

import java.util.*;

public enum EnemyType implements IEnemyType<EnemyPrize> {

    SKULL_BREAKER(0, "Skullbreaker", 2, 4, (short) 0,
            Collections.singletonList(new Skin(4.f, 0.f, 4.f)), 2, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    SHAMAN(1, "Vine Witch", 2, 4, (short) 0,
            Collections.singletonList(new Skin(4.f, 0.f, 4.f)), 3, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    JUMPER(2, "Crazed Guardian", 2, 4, (short) 0,
            Collections.singletonList(new Skin(12.f, 0.f, 0.f)), 4, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    RUNNER(3, "Jungle Runner", 2, 4, (short) 0,
            Collections.singletonList(new Skin(4.f, 0.f, 4.f)), 5, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    JAGUAR(4, "Stalking Jaguar", 2, 4, (short) 0,
            Collections.singletonList(new Skin(6.f, 0.f, 3.f)), 7, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    SNAKE(5, "Slithering Serpant", 2, 4, (short) 0,
            Collections.singletonList(new Skin(3.2f, 0, 0.8f)), 8, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    //Spiderling
    ANT(6, "Carnivorous Ant/Tiny Toad", 2, 4, (short) 0,
            Arrays.asList(new Skin(4.0f, 0, 0),
                    new Skin(4.0f, 0, 0),
                    new Skin(4.0f, 0, 0)), 10, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    //Bug
    WASP(7, "Venomous Wasp", 2, 4, (short) 0,
            Arrays.asList(new Skin(5.0f, 0, 4),
                    new Skin(5.0f, 0, 4),
                    new Skin(5.0f, 0, 4)),
            12, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    WEAPON_CARRIER(8, "Armed Warrior", 2, 4,
            (short) 0, Collections.singletonList(new Skin(4, 0, 4.f)),
            0, new double[]{0.12, 0.01, 0.01},
            new ChestProb(0.08, new double[]{0.70, 0.10, 0.05, 0.05, 0.05, 0.05}), true
    ),

    EXPLODER(9, "Exploding Toad", 2, 4,
            (short) 0,
            Collections.singletonList(new Skin(9, 0, 0.f)),
            125, new double[]{0.12, 0.01, 0.01},
            new ChestProb(0.08, new double[]{0.70, 0.10, 0.05, 0.05, 0.05, 0.05}), true
    ),

    MULTIPLIER(10, "Spirit Scorpion", 2, 4,
            (short) 0,
            Collections.singletonList(new Skin(6, 0, 0.f)),
            125, new double[]{0.12, 0.01, 0.01},
            new ChestProb(0.08, new double[]{0.70, 0.10, 0.05, 0.05, 0.05, 0.05}), true
    ),

    // Bosses
    Boss(11, "Boss",
            2, 4,
            (short) 10,
            true,
            Arrays.asList(new Skin(3.2f, 0, 0),
                    new Skin(3.2f, 0, 0),
                    new Skin(3.2f, 0, 0)),
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
        this(id, name, width, height, energy, false, skins, reward,
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
