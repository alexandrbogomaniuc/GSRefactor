package com.betsoft.casino.mp.bgdragonstone.model.math;

import com.betsoft.casino.mp.common.math.*;
import com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfig;
import com.betsoft.casino.mp.bgdragonstone.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.bgdragonstone.model.math.slot.MiniSlot;
import com.betsoft.casino.mp.model.GameType;
import com.betsoft.casino.mp.model.SpecialWeaponType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;
import java.util.stream.Collectors;
import static com.betsoft.casino.mp.bgdragonstone.model.math.EnemyRange.SPECTERS;
import static com.betsoft.casino.mp.model.SpecialWeaponType.values;

public class PayTableInst {
    private static final Logger LOG = LogManager.getLogger(PayTableInst.class);

    private static Paytable TABLE = createPayTable();

    private static Paytable createPayTable() {
        LOG.debug("createPayTable start");
        GameConfig config = new GameConfigLoader().loadDefaultConfig();
        List<EnemyPays> enemyPayouts = new ArrayList<>(EnemyType.values().length);
        List<LootboxPrizes> lootboxPrizes = new ArrayList<>();
        config.getEnemies().forEach((enemyType, enemyData) -> {
            EnemyPays enemyPays;
            EnemyData data = enemyData.get(0);
            if (!enemyType.equals(EnemyType.OGRE) && !enemyType.equals(EnemyType.SPIRIT_SPECTER)) {
                int payout = data.getPayout();
                enemyPays = new EnemyPays(enemyType.getId(), enemyType.getName(), new Prize(payout, payout));
            } else {
                List<Integer> allPayouts = new ArrayList<>();
                for (EnemyData datum : enemyData) {
                    allPayouts.add(datum.getPayout());
                }
                if(enemyType.equals(EnemyType.OGRE)){
                    allPayouts.add(config.getRageMin());
                    allPayouts.add(config.getRageMax());
                }else {
                    allPayouts.add(config.getSpiritMin());
                    allPayouts.add(config.getSpiritMax());
                }
                enemyPays = new EnemyPays(enemyType.getId(), enemyType.getName(), new Prize(Collections.min(allPayouts),
                        Collections.max(allPayouts)));
            }
            enemyPayouts.add(enemyPays);
        });

        List<Integer> bossPayouts = new ArrayList<>(config.getBoss().getPartialPays().keySet());
        EnemyPays bossPays = new EnemyPays(EnemyType.DRAGON.getId(), EnemyType.DRAGON.getName(),
                new Prize(Collections.min(bossPayouts), Collections.max(bossPayouts)));
        enemyPayouts.add(bossPays);


        Paytable paytable = new Paytable(enemyPayouts, 0, lootboxPrizes, null,
                null, null, getEnemyPayoutsByWeapons(), MathData.getPossibleBetLevels());
        paytable.setReels(createReels(config.getSlot().getReels()));

        LOG.debug("createPayTable: new paytable={}", paytable);
        return paytable;
    }

    public static Paytable getTable() {
        return TABLE;
    }

    public static Map<Integer, Map<Integer, Prize>> getEnemyPayoutsByWeapons() {
        Map<Integer, Map<Integer, Prize>> enemyPayoutsByWeapons = new HashMap<>();
        for (EnemyType enemyType : EnemyType.values()) {
            if (!SPECTERS.contains(enemyType)) {
                int enemyId = enemyType.getId();
                HashSet<Integer> weapons = new HashSet<>();
                weapons.add(-1);
                for (SpecialWeaponType weaponType : SpecialWeaponType.values()) {
                    if (weaponType.getAvailableGameIds().contains(838)) {
                        weapons.add(weaponType.getId());
                    }
                }
            }
        }
        return enemyPayoutsByWeapons;
    }

    private static Map<Integer, List<Integer>> createReels(int[][] reelsFromConfig) {
        Map<Integer, List<Integer>> reels = new HashMap<>();
        int i = 1;
        for (int[] reel : reelsFromConfig) {
            reels.put(i++, Arrays.stream(reel).boxed().collect(Collectors.toList()));
        }
        return reels;
    }
}



