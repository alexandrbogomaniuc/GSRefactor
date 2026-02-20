package com.betsoft.casino.mp.clashofthegods.model.math;

import com.betsoft.casino.mp.model.Skin;
import com.betsoft.casino.mp.model.IEnemyType;
import java.util.*;

public enum EnemyType implements IEnemyType<EnemyPrize> {

    Evil_Spirit(0, "UNDERGROUND SPIRIT", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.f, 0.f,3.f)), 2, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Evil_Spirit_1(1, "SWAMP SPIRIT", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.f, 0.f,3.f)), 2, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Evil_Spirit_2(2, "FLAME SPIRIT", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.f, 0.f,3.f)), 2, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Owl(3, "EVIL EYES OWL", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(13.f, 0.f,0.f)), 3, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Snake(4, "VICIOUS SNAKE SPIRIT", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(2.f, 0.f,3.f)), 5, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Lizard(5, "LIZARD WARRIOR", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(7.f, 0.f,0.f)), 7, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Lizard_1(6, "ICE LIZARDMAN", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(7.f, 0.f,0.f)), 7, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Lizard_2(7, "WATER LIZARDMAN", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(7.f, 0.f,0.f)), 7, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Tiger(8, "MASTER TIGER SPIRIT", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(8.f, 0.f,0.f)),
            8, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Golden_Dragon(9, "ROYAL DRAGON", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.5f, 0.f, 0.f)
            ), 12, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Silver_Dragon(10, "JADE DRAGON", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(6.75f, 0.f, 0.f)
            ), 12, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),


    Spirits_1_RED(11, "INFERNO SPIRIT", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.f, 0.f,4.f)), 15, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Spirits_2_ORANGE(12, "FORTUNE SPIRIT", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(13.f, 0.f, 1.f)), 20, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),


    Spirits_3_GREEN(13, "POISON SPIRIT", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(13f, 0.f, 1.f)),
            25, new double[]{0.12, 0.03, 0.02},
            new ChestProb(0.08, new double[]{0.45, 0.40, 0.10, 0.05}), false
    ),

    Spirits_4_BLUE(14, "THUNDER SPIRIT", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(13f,0.f, 1.f)),
            50, new double[]{0.12, 0.03, 0.02},
            new ChestProb(0.08, new double[]{0.45, 0.40, 0.10, 0.05}), false
    ),

    Spirits_5_VIOLETT(15, "VOID SPIRIT", 2, 4, (short) 0,
            Collections.singletonList(new Skin(4.f,0.f, 2.8f)),
            60, new double[]{0.12, 0.03, 0.02},
            new ChestProb(0.08, new double[]{0.45, 0.40, 0.10, 0.05}), false
    ),

    Phoenix(16, "RISING PHOENIX", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.5f, 0.f, 0.f)
            ), 12, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Lantern(17, "FESTIVAL LANTERNS", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(6.75f, 0.f, 0.f)
            ), 12, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Dragonfly_Red(18, "WICKED MESSENGER", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(18, 0.f, 0.f)
            ), 12, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Dragonfly_Green(19, "VICIOUS MESSENGER", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(18, 0.f, 0.f)
            ), 12, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Beetle_1(20, "UNDERWORLD SCARAB", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.f, 0.f,3.f)), 2, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    Beetle_2(21, "IMMORTAL SCARAB", 2, 4,  (short) 0,
            Collections.singletonList(new Skin(4.f, 0.f,3.f)), 2, new double[]{0.1, 0.02},
            new ChestProb(0.05, new double[]{0.60, 0.20, 0.10, 0.05, 0.05}), false
    ),

    // Bosses (skin: 1-LionSnake, 2-TaoWu, 3-Bull_Demon)
    Boss(22, "Boss",
            2, 4,
            (short) 10,
            true,
            Arrays.asList(new Skin(3.2f, 0, 0),
                    new Skin(3.2f, 0, 0),
                    new Skin(7.f, 0, 0)),
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
