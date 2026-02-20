package com.betsoft.casino.mp.maxcrashgame.model.math;

import com.betsoft.casino.mp.common.math.*;
import com.betsoft.casino.mp.maxcrashgame.model.math.config.GameConfig;
import com.betsoft.casino.mp.maxcrashgame.model.math.config.GameConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

public class PayTableInst {
    private static final Logger LOG = LogManager.getLogger(PayTableInst.class);

    private static Paytable TABLE = createPayTable();

    private static Paytable createPayTable() {
        LOG.debug("createPayTable start");
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        List<EnemyPays> enemyPayouts = new ArrayList<>();
        List<LootboxPrizes> lootboxPrizes = new ArrayList<>();

        List<Integer> bossPayouts = new ArrayList<>();

        List<SWPaidCosts> weaponPaidMultiplier = new ArrayList<>();
        Paytable paytable = new Paytable(enemyPayouts, 0, lootboxPrizes, weaponPaidMultiplier,
                null, null, getEnemyPayoutsByWeapons(), MathData.getPossibleBetLevels());
        paytable.setReels(createReels());

        LOG.debug("createPayTable: new paytable={}", paytable);
        return paytable;
    }

    public static Paytable getTable() {
        return TABLE;
    }

    public static Map<Integer, Map<Integer, Prize>> getEnemyPayoutsByWeapons() {
        Map<Integer, Map<Integer, Prize>> enemyPayoutsByWeapons = new HashMap<>();
        return enemyPayoutsByWeapons;
    }

    private static Map<Integer, List<Integer>> createReels() {
        Map<Integer, List<Integer>> reels = new HashMap<>();
        return reels;
    }
}



