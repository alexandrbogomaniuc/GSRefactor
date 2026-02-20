package com.betsoft.casino.mp.amazon.model.math;

import com.betsoft.casino.mp.model.ITreasure;
import java.util.Arrays;
import java.util.List;

public enum TreasureQuests {
    QuestOne(Arrays.asList(Treasure.Brass_Scarab_Sigil, Treasure.Vase_of_Osiris, Treasure.Onyx_servent_of_Bastet), 50),
    QuestTwo(Arrays.asList(Treasure.Cartouche_of_Gold_Amulet, Treasure.Golden_Bust_of_Anubis, Treasure.Onyx_Statue_of_Pharaoh), 75),
    QuestThree(Arrays.asList(Treasure.Eagle_of_Ruby_Emerald, Treasure.Knight_of_Anubis_Bust, Treasure.Sacred_Golden_Pyramid), 100),
    QuestFour(Arrays.asList(Treasure.Garnet_Scarab_Amulet, Treasure.Lapis_Cuff_of_the_Queen, Treasure.Golden_Bust_of_Nefertiti), 150),
    QuestFive(Arrays.asList(Treasure.Golden_Statue_of_Tutenkhamun, Treasure.Offering_Bowl_of_Gems, Treasure.Jade_Brooch), 200);


    private final List<ITreasure> treasures;
    private int win;

    TreasureQuests(List<ITreasure> treasures, int win) {
        this.treasures = treasures;
        this.win = win;
    }

    public List<ITreasure> getTreasures() {
        return treasures;
    }

    public int getWin() {
        return win;
    }
}
