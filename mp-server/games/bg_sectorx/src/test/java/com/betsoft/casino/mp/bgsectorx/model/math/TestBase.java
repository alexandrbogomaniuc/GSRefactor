package com.betsoft.casino.mp.bgsectorx.model.math;

import com.betsoft.casino.mp.bgsectorx.model.math.config.GameConfig;
import com.betsoft.casino.mp.bgsectorx.model.math.config.GameConfigLoader;
import com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfig;
import com.betsoft.casino.mp.bgsectorx.model.math.config.SpawnConfigLoader;
import com.betsoft.casino.mp.model.gameconfig.GameTools;

import java.util.*;
import java.util.stream.Collectors;

import static com.betsoft.casino.mp.bgsectorx.model.math.EnemyRange.BASE_ENEMIES;
import static com.betsoft.casino.mp.bgsectorx.model.math.EnemyType.getById;

public class TestBase {
    public static void main(String[] args) {
        new TestBase().testWeights();
    }

    void testBase() {
        //        for (EnemyType value : EnemyType.values()) {
//            //  System.out.println(value.getId() + ";" + value.name() + ";" + value.getName());
//        }

        GameConfig gameConfig = new GameConfigLoader().loadDefaultConfig();
        //EnemyData enemyData = gameConfig.getEnemyData(EnemyType.S10);
        Map<Integer, List<List<EnemyType>>> enemiesForItemsByPay = gameConfig.getEnemiesForItemsByPay();
        enemiesForItemsByPay.forEach((pay, listsRandomEnemies) -> {
            System.out.println("-----------------------------------------payout: " + pay);
            for (List<EnemyType> enemyTypes : listsRandomEnemies) {
                enemyTypes.forEach(enemyType1 -> System.out.print(enemyType1.getName() + ", "));
                //System.out.println(enemyTypes);
                System.out.println("");
            }

        });


        Map<Integer, List<KillerItemData>> killItemsDataByPay = gameConfig.getKillItemsDataByPay();
        killItemsDataByPay.forEach((pay, killerItemData) -> {
            System.out.println("-----------------------------------------payout: " + pay);
            killerItemData.forEach(kld -> {
                System.out.print(kld + ", ");
            });
            System.out.println("");
        });

        List<EnemyType> enemiesForKilling =
                new ArrayList<>(Arrays.asList(EnemyType.S5, EnemyType.S5, EnemyType.S6, EnemyType.S6, EnemyType.S6, EnemyType.S1));

        //Map<String, List<EnemyType>> map = enemiesForKilling.stream().collect(Collectors.groupingBy(EnemyType::name));
        Map<Integer, List<EnemyType>> map = enemiesForKilling
                .stream().collect(Collectors.groupingBy(EnemyType::getId));
        map.forEach((s, enemyTypes) -> System.out.println("s: " + s + ", enemyTypes size: " + enemyTypes.size()));


        System.out.println(map);
        // long count = enemiesForKilling.stream().filter(enemyType -> enemyType.equals(EnemyType.S5)).count();
        // System.out.println(count);
//        System.out.println(enemyData);
//        double hitProbability = MathData.getHitProbability(gameConfig, EnemyType.S10);
//        System.out.println(hitProbability);
//
//        Map<EnemyType, List<SpecialItem>> items = gameConfig.getItems();
//        items.forEach((enemyType, specialItems) -> {
//            System.out.println(enemyType.getName() + ", " + specialItems.get(0));
//        });
//
//        SpecialItem specialItem = gameConfig.getItems().get(EnemyType.F1).get(0);
//        List<Prize> prizes = specialItem.getPrizes();
//        System.out.println(prizes);
//
//
//        Paytable paytable = PayTableInst.getTable();
//        List<EnemyPays> enemyPayouts = paytable.getEnemyPayouts();
//        for (EnemyPays enemyPayout : enemyPayouts) {
//            System.out.println(enemyPayout.getNameEnemy() + ", " + enemyPayout.getPrize().getMinPayout());
//        }
    }

    private List<SpawnStageFromConfig> getSpawnStage(SpawnConfig spawnConfig) {
        List<SpawnStageFromConfig> initSpawnStage = new ArrayList<>();
        for (int i = 1; i < spawnConfig.getTimeSlices().size(); i++) {
            List<Double> resultList = new ArrayList<>();
            resultList.add(spawnConfig.getLowPayWeights().get(i - 1) / 100);
            resultList.add(spawnConfig.getMidPayWeights().get(i - 1) / 100);
            resultList.add(spawnConfig.getHighPayWeights().get(i - 1) / 100);
            initSpawnStage.add(new SpawnStageFromConfig(spawnConfig.getTimeSlices().get(i) * 1000L, resultList));
        }
        return initSpawnStage;
    }

    void testWeights() {

        Map<Integer, Double> enemiesWeights = new HashMap<>();
        int T = 8;
        long startRoundTime = System.currentTimeMillis() - 10000;
        SpawnConfig spawnConfig = new SpawnConfigLoader().loadDefaultConfig();
        SpawnStageFromConfig spawnStageFromConfig = SpawnStage.getStageWeights(startRoundTime, getSpawnStage(spawnConfig));

        // 22=4, 24=1, 28=3
        Map<Integer, Integer> counter = new HashMap<>();
        counter.put(22, 4);
        counter.put(24, 1);
        counter.put(28, 3);


        BASE_ENEMIES.getEnemies().forEach(enemyType -> {
            double alpha = spawnStageFromConfig.getEnemyWeightByEnemyType(enemyType, 0);
            if (counter.containsKey(enemyType.getId())) {
                int countEn = counter.get(enemyType.getId());
                int N = counter.size();
                N = Math.max(N, 2);
                double weight = alpha / (N - 1) - alpha * countEn / ((N - 1) * T);
                enemiesWeights.put(enemyType.getId(), weight);
            } else {
                int N = BASE_ENEMIES.getEnemies().size();
                double weight = alpha / (N - 1) - alpha / ((N - 1) * T);
                enemiesWeights.put(enemyType.getId(), weight);
            }
        });

        double sum = enemiesWeights.values().stream().mapToDouble(Double::doubleValue).sum();

        Map<Integer, Double> enemiesWeightsNorm = enemiesWeights.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue() / sum));

        EnemyType randomEnemyType = getById(GameTools.getRandomNumberKeyFromMap(enemiesWeightsNorm));

        System.out.println("spawnStageFromConfig: " + spawnStageFromConfig);
        System.out.println("enemiesWeights: " + enemiesWeights);
        System.out.println("enemiesWeightsNorm: " + enemiesWeightsNorm);
        System.out.println("randomEnemyType: " + randomEnemyType);

    }
}
