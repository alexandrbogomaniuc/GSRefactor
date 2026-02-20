package com.betsoft.casino.mp.sectorx.model.math;

import com.betsoft.casino.mp.model.IChestProb;
import com.betsoft.casino.mp.model.IEnemyType;
import com.betsoft.casino.mp.model.Skin;
import com.google.common.collect.Maps;

import java.util.*;

public enum EnemyType implements IEnemyType<EnemyPrize> {

    S1(0, "Malachite Eye Flyer", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S2(1, "Amber Eye Flyer", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S3(2, "Amethyst Eye Flyer", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S4(3, "Citrine Eye Flyer", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S5(4, "Jellied Skyswimmer", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S6(5, "Malignant Ray", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S7(6, "Emerald Jumper", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S8(7, "Sapphire Jumper", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S9(8, "Albino Jumper", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S10(9, "Grotesque Slug", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S11(10, "Invader Trooper", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S12(11, "Spiked Triclops", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S13(12, "Buzzing Watcher", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S14(13, "Ocular Terror", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S15(14, "Nimble Jumper", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S16(15, "Scarlet Glider", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S17(16, "Azure Devourer", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S18(17, "Crimson Devourer", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S19(18, "Albino Devourer", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S20(19, "Yellow Devourer", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S21(20, "Fluttering Screecher", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S22(21, "Invader Commander", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S23(22, "Cyborg Raider", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S24(23, "Trinocular Leaper", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S25(24, "Crawling Hellmouth", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S26(25, "Razortooth", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S27(26, "Spiked Beholder", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S28(27, "Darkwing Mutant", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S29(28, "Hivemind Overlord", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S30(29, "Living Magma", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    S31(30, "Rampaging Behemoth", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),

    B3(71, "Concealed Coins", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    B2(72, "Mega Cyborg Raider", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    B1(73, "Mega Rampaging Behemoth", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),

    F1(51, "Money Wheel", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    F2(52, "Flash Blizzard", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    F3(53, "Enemy Seeker", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    F4(54, "Multiplier Bomb", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    F5(55, "Chain Reaction Shot", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    F6(56, "Arc Lighthing", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),
    F7(57, "Laser Net", 2, 4, Collections.singletonList(new Skin(4.f, 0.f, 4.f))),

    BOSS(100, "Boss", 2, 4, true,
            Arrays.asList(
                    new Skin(3.2f, 0, 0),
                    new Skin(3.2f, 0, 0),
                    new Skin(3.2f, 0, 0),
                    new Skin(3.2f, 0, 0)
            )
    );

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
