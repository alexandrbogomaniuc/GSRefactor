package com.betsoft.casino.mp.dragonstone.model.math;

import com.betsoft.casino.mp.model.IChestProb;
import com.betsoft.casino.mp.model.Skin;
import com.betsoft.casino.mp.model.IEnemyType;

import java.util.*;

public enum EnemyType implements IEnemyType<EnemyPrize> {

    BROWN_SPIDER(0, "Brown Spider", 55, 30, (short) 0, Collections.singletonList(new Skin(4.0f, 0, 0))),
    BLACK_SPIDER(1, "Black Spider", 50, 30, (short) 0, Collections.singletonList(new Skin(4.0f, 0, 0))),
    BROWN_RAT(2, "Brown Rat", 30, 20, (short) 0, Collections.singletonList(new Skin(7.6f, 0, 0))),
    BLACK_RAT(3, "Black Rat", 30, 20, (short) 0, Collections.singletonList(new Skin(7.6f, 0, 0))),
    BAT(4, "Bat", 45, 30, (short) 0, Collections.singletonList(new Skin(10.0f, 0.f, 0.f))),
    RAVEN(5, "Raven", 60, 35, (short) 0, Collections.singletonList(new Skin(15.0f, 0.f, 0.f))),
    SKELETON_1(6, "Skeleton", 35, 95, (short) 0, Collections.singletonList(new Skin(4.5f, 0.f, 0.f))),
    IMP_1(7, "Gluttonous Imp", 60, 90, (short) 0, Collections.singletonList(new Skin(4.5f, 0.f, 0.f))),
    IMP_2(8, "Plaqued Imp", 60, 90, (short) 0, Collections.singletonList(new Skin(4.5f, 0.f, 0.f))),
    SKELETON_SHIELD(9, "Skeletal Commander", 35, 95, (short) 0, Collections.singletonList(new Skin(4.5f, 0.f, 0.f))),
    GOBLIN(10, "Goblin", 30, 70, (short) 0, Collections.singletonList(new Skin(4.0f, 0.f, 0.f))),
    HOBGOBLIN(11, "Hobgoblin", 30, 70, (short) 0, Collections.singletonList(new Skin(4.0f, 0.f, 0.f))),
    DUP_GOBLIN(12, "Spectral Goblin", 30, 70, (short) 0, Collections.singletonList(new Skin(4.0f, 0.f, 0.f))),
    GARGOYLE(13, "Gargoyle", 180, 130, (short) 0, Collections.singletonList(new Skin(12.5f, 0.f, 0.f))),
    ORC(14, "Orc", 60, 130, (short) 0, Collections.singletonList(new Skin(5.75f, 0.f, 0.f))),
    EMPTY_ARMOR_1(15, "Knight's Armor", 35, 100, (short) 0, Collections.singletonList(new Skin(5.5f, 0.f, 0.f))),
    EMPTY_ARMOR_2(16, "Tarnished Armor", 35, 100, (short) 0, Collections.singletonList(new Skin(5.5f, 0.f, 0.f))),
    EMPTY_ARMOR_3(17, "Champion's Armor", 35, 100, (short) 0, Collections.singletonList(new Skin(5.5f, 0.f, 0.f))),
    RED_WIZARD(18, "Red Wizard", 30, 100, (short) 0, Collections.singletonList(new Skin(4.0f, 0.f, 0.f))),
    BLUE_WIZARD(19, "Blue Wizard", 30, 100, (short) 0, Collections.singletonList(new Skin(4.0f, 0.f, 0.f))),
    PURPLE_WIZARD(20, "Purple Wizard", 30, 100, (short) 0, Collections.singletonList(new Skin(4.0f, 0.f, 0.f))),
    OGRE(21, "Ogre", 110, 200, (short) 0, Collections.singletonList(new Skin(3.5f, 0.f, 0.f))),
    DARK_KNIGHT(22, "Dark Knight", 65, 160, (short) 0, Collections.singletonList(new Skin(4.0f, 0.f, 0.f))),
    CERBERUS(23, "Cerberus", 160, 130, (short) 0, Collections.singletonList(new Skin(5.625f, 0.f, 0.f))),

    SPIRIT_SPECTER(24, "Spirit Specter", 90, 150, (short) 0,
            Collections.singletonList(new Skin(4.0f, 0.f, 0.f))),

    FIRE_SPECTER(25, "Fire Specter", 90, 150, (short) 0,
            Collections.singletonList(new Skin(4.0f, 0.f, 0.f))),

    LIGHTNING_SPECTER(26, "Lightning Specter", 90, 150, (short) 0,
            Collections.singletonList(new Skin(4.0f, 0.f, 0.f))),


    DRAGON(27, "Dragon", 500, 500, (short) 10, true,
            Collections.singletonList(new Skin(4.0f, 0.f, 0.f))
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

    private static final Map<Integer, EnemyType> idsMap = new HashMap<>();

    static {
        for (EnemyType type : values()) {
            idsMap.put(type.id, type);
        }
    }

    EnemyType(int id, String name, int width, int height, short energy, List<Skin> skins) {
        this(id, name, width, height, energy, false, skins);
    }

    EnemyType(int id, String name, int width, int height, short energy, boolean boss, List<Skin> skins) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.payTable = new EnumMap<>(EnemyPrize.class);
        this.sumAward = 0;
        this.boss = boss;
        this.skins = skins;
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
        return 0;
    }

    @Override
    public double[] getTreasureDropRates() {
        return null;
    }

    @Override
    public IChestProb getChestProb() {
        return null;
    }

    @Override
    public boolean isHVenemy() {
        return false;
    }

    public static EnemyType getById(int id) {
        return idsMap.get(id);
    }

    @Override
    public String toString() {
        return "EnemyType [id=" + getId() +
                ", name='" + name + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", payTable=" + payTable +
                ", sumAward=" + sumAward +
                ", boss=" + boss +
                ", skins=" + skins +
                ']';
    }
}
