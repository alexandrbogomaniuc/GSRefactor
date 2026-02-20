package com.betsoft.casino.mp.maxcrashgame.model.math;

import com.betsoft.casino.mp.model.IChestProb;
import com.betsoft.casino.mp.model.Skin;
import com.betsoft.casino.mp.model.IEnemyType;

import java.util.*;

public enum EnemyType implements IEnemyType<EnemyPrize> {

    ROCKET(0, "Rocket", 55, 30, (short) 0,
            Collections.singletonList(new Skin(4.0f, 0, 0)));


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
