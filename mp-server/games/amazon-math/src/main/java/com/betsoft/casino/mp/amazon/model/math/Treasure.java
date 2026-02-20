package com.betsoft.casino.mp.amazon.model.math;

import com.betsoft.casino.mp.model.ITreasure;
import com.betsoft.casino.mp.model.TreasureRarity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.model.TreasureRarity.*;

public enum Treasure implements ITreasure {
    // @formatter:off

    Brass_Scarab_Sigil(1, COMMON, "Pouch of Gold Nuggets"),
    Cartouche_of_Gold_Amulet(2, COMMON, "Jewelled Ring"),
    Eagle_of_Ruby_Emerald(3, COMMON, "Golden Idol"),

    Garnet_Scarab_Amulet(4, COMMON, "Cursed Macuahuitl"),
    Golden_Statue_of_Tutenkhamun(5, COMMON, "Jade Necklace"),
    Vase_of_Osiris(6, COMMON, "Golden Ingots"),

    Golden_Bust_of_Anubis(7, COMMON, "Golden Amulet of the Explorer"),
    Knight_of_Anubis_Bust(8, COMMON, "Jewelled Goblet of Wisdom"),
    Lapis_Cuff_of_the_Queen(9, COMMON, "Golden Shaman's Mask"),

    Offering_Bowl_of_Gems(10, COMMON, "Golden Warrior's Mask"),
    Onyx_servent_of_Bastet(11, COMMON, "The Tear of Alcyone"),
    Onyx_Statue_of_Pharaoh(12, COMMON, "The Eye of Cassiopeia"),

    Sacred_Golden_Pyramid(13, COMMON, "The Heart of Betelgeuse"),
    Golden_Bust_of_Nefertiti(14, COMMON, "The Soul of Sirius"),
    Jade_Brooch(15, COMMON, "Shard of the Sun");

    // @formatter:on

    int id;
    TreasureRarity rarity;
    String realName;

    static Map<TreasureRarity, List<ITreasure>> treasureRarityMap = new HashMap<>();
    static Map<Integer, Treasure> treasureIdsMap = new HashMap<>();

    static {
        Arrays.stream(TreasureRarity.values()).forEach(rarity ->
            treasureRarityMap.put(rarity,
                    Arrays.stream(values())
                            .filter(treasure -> treasure.getRarity().equals(rarity))
                            .collect(Collectors.toList())));

        for (Treasure treasure : values()) {
            treasureIdsMap.put(treasure.getId(), treasure);
        }
    }

    Treasure(int id, TreasureRarity rarity, String realName) {
        this.id = id;
        this.rarity = rarity;
        this.realName = realName;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public TreasureRarity getRarity() {
        return rarity;
    }

    @Override
    public int getOrdinalValue() {
        return ordinal();
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public static List<ITreasure> getTreasures(TreasureRarity rarity) {
        return treasureRarityMap.get(rarity);
    }

    public static Treasure getById(int id) {
        return treasureIdsMap.get(id);
    }


}
