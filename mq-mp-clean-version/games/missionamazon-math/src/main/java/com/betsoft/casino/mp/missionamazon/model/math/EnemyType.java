package com.betsoft.casino.mp.missionamazon.model.math;

import com.betsoft.casino.mp.model.IChestProb;
import com.betsoft.casino.mp.model.IEnemyType;
import com.betsoft.casino.mp.model.Skin;
import com.google.common.collect.Maps;

import java.util.*;

public enum EnemyType implements IEnemyType<EnemyPrize> {

    SKULL_BREAKER(0, "Cerulean Skullbreaker", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),

    WITCH(1, "Vine Witch", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),

    GUARDIAN(2, "Crazed Guardian", 2, 4, Collections.singletonList(new Skin(12.f, 0.f, 0.f))),

    RUNNER(3, "Jungle Runner", 2, 4, Collections.singletonList(new Skin(16.f, 0.f, 4.f))),

    JAGUAR(4, "Stalking Jaguar", 2, 4, Collections.singletonList(new Skin(6.f, 0.f, 3.f))),

    SERPENT(5, "Slithering Serpent", 2, 4, Collections.singletonList(new Skin(3.2f, 0, 0.8f))),

    ANT(6, "Carnivorous Ant", 2, 4, Arrays.asList(new Skin(4.0f, 0, 0), new Skin(4.0f, 0, 0))),

    WASP(7, "Venomous Wasp", 2, 4,
            Arrays.asList(new Skin(5.0f, 0, 4),
                    new Skin(5.0f, 0, 4),
                    new Skin(5.0f, 0, 4),
                    new Skin(5.0f, 0, 4))),

    ARMED_WARRIOR(8, "Armed Warrior", 2, 4, Collections.singletonList(new Skin(4, 0, 4.f))),

    EXPLODING_TOAD(9, "Exploding Toad", 2, 4, Collections.singletonList(new Skin(9, 0, 0.f))),

    SCORPION(10, "Spirit Scorpion", 2, 4, Collections.singletonList(new Skin(6, 0, 0.f))),

    TINY_TOAD(11, "Tiny Toad", 2, 4, Collections.singletonList(new Skin(8.0f, 0, 0))),

    FLOWERS_1(12, "Noxious Growth", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),

    FLOWERS_2(13, "Invasive poisoner", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),

    PLANT_1(14, "Crimson Chomper", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),

    PLANT_2(15, "Emerald Maneater", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),

    WEAPON_CARRIER_1(16, "Weapon Carrier Artillery Strike", 2, 4, Collections.singletonList(new Skin(4, 0, 4.f))),

    WEAPON_CARRIER_2(17, "Weapon Carrier Flamethrower", 2, 4, Collections.singletonList(new Skin(4, 0, 4.f))),

    WEAPON_CARRIER_3(18, "Weapon Carrier Cryogun", 2, 4, Collections.singletonList(new Skin(4, 0, 4.f))),

    WEAPON_CARRIER_4(19, "Weapon Carrier Laser", 2, 4, Collections.singletonList(new Skin(4, 0, 4.f))),

    WEAPON_CARRIER_5(20, "Weapon Carrier Plasma Gun", 2, 4, Collections.singletonList(new Skin(4, 0, 4.f))),

    BOSS(21, "Boss", 2, 4, true,
            Arrays.asList(new Skin(3.2f, 0, 0), new Skin(3.2f, 0, 0), new Skin(3.2f, 0, 0)));

    public static final int MAX_SKINS = 100;
    private static final Map<Integer, EnemyType> idsMap = Maps.newHashMap();

    private final int id;
    private String name;
    // TODO: 18.11.2021 define width/height for enemies
    private int width;
    private int height;
    private EnumMap<EnemyPrize, Long> payTable;
    private long sumAward;
    private boolean boss;
    private List<Skin> skins;

    static {
        Arrays.stream(values()).forEach(enemyType -> idsMap.put(enemyType.getId(), enemyType));
    }

    EnemyType(int id, String name, int width, int height, List<Skin> skins) {
        this(id, name, width, height, false, skins);
    }

    EnemyType(int id, String name, int width, int height, boolean boss, List<Skin> skins) {
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
    public boolean isHVenemy() {
        return false;
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
        return new double[0];
    }

    @Override
    public IChestProb getChestProb() {
        return null;
    }

    public static EnemyType getById(int id) {
        return idsMap.get(id);
    }

    @Override
    public String toString() {
        return "EnemyType" + "[" +
                "id=" + id +
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
