package com.betsoft.casino.mp.amazon;

import com.betsoft.casino.mp.amazon.model.math.MathData;
import com.betsoft.casino.mp.amazon.model.math.Treasure;
import com.betsoft.casino.mp.amazon.model.math.TreasureQuests;
import com.betsoft.casino.mp.model.ITreasure;
import com.betsoft.casino.mp.model.quests.*;
import com.dgphoenix.casino.common.util.RNG;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TestTreasures {
/*    public static void main(String[] args) {
        int cntShots = 10000000;
        int idxQuestForCoin = 0;
        Set<Quest> quests = new HashSet<>();
        int stake = 1;
        int[] questsCompleted = new int[5];
        double[] questsCompletedWin = new double[5];
        double totalWin = 0;
        long totalQuestCompleted = 0;
        long totalTreasures = 0;

        Map<String, AtomicLong> stat = new HashMap<>();
        for (Treasure value : Treasure.values()) {
            stat.put(value.name(), new AtomicLong(0));
        }



        ArrayList<TreasureProgress> treasureProgresses;
        int idx = 1;
        for (TreasureQuests treasureQuest : TreasureQuests.values()) {
            treasureProgresses = new ArrayList<>();
            for (ITreasure treasure : treasureQuest.getTreasures()) {
                treasureProgresses.add(new TreasureProgress(treasure.getId(), 0, 1));
            }
            QuestPrize questPrize = new QuestPrize(new QuestAmount(treasureQuest.getWin(), treasureQuest.getWin()), -1);
            Quest newQuest = new Quest(idxQuestForCoin++, 1, stake, false, 0,
                    new QuestProgress(treasureProgresses), questPrize, Treasure.getById(idx).name());
            quests.add(newQuest);
            idx++;
        }

        quests.forEach(System.out::println);

        for (int i = 0; i < cntShots; i++) {
            boolean needTreasure = RNG.rand() < MathData.getProbTreasureForQuest();
            if (needTreasure) {
                totalTreasures++;
                Treasure randomTreasure = MathData.getRandomTreasure();
                stat.get(randomTreasure.name()).incrementAndGet();

                List<Quest> questsWithPrize = quests.stream().filter(quest -> {
                    List<TreasureProgress> treasures = quest.getProgress().getTreasures();
                    long count = treasures.stream().filter(treasureProgress ->
                            (treasureProgress.getTreasureId() == randomTreasure.getId())).count();
                    return count > 0;
                }).collect(Collectors.toList());

//                if(questsWithPrize.isEmpty())
//                    continue;

                Quest quest = questsWithPrize.get(0);
                List<TreasureProgress> treasures = quest.getProgress().getTreasures();
                TreasureProgress progress = treasures.stream().filter(
                        treasureProgress -> treasureProgress.getTreasureId() == randomTreasure.getId()).findFirst().get();
                progress.setCollect(progress.getCollect() + 1);

                long countUnfinished = treasures.stream().filter(treasureProgress ->
                        treasureProgress.getCollect() < treasureProgress.getGoal()).count();

                boolean needFinish = countUnfinished == 0;
                if (needFinish) {
                    int amount = quest.getQuestPrize().getAmount().getFrom();
                    quest.getProgress().decreaseProgress();
                    questsCompleted[(int) quest.getId()]++;
                    questsCompletedWin[(int) quest.getId()] += amount;
                    totalWin += amount;
                    totalQuestCompleted++;
                }

            }
        }

        System.out.println("totalBet: " + cntShots);

        System.out.println("totalTreasures: " + totalTreasures);
        System.out.println("1 treasure from: " + (double) cntShots / totalTreasures + " shots");

        System.out.println("totalQuestCompleted: " + totalQuestCompleted);
        System.out.println("1 quest from: " + (double) cntShots / totalQuestCompleted + " shots");

        System.out.println("totalWin: " + totalWin);
        System.out.println("RTP: " + totalWin / cntShots);

        System.out.println("questsCompleted: " + Arrays.toString(questsCompleted));
        System.out.println("questsCompletedWin: " + Arrays.toString(questsCompletedWin));

        stat.forEach((s, atomicLong) -> {
                    System.out.println(s + " " + (atomicLong.doubleValue() / cntShots) + "    -----  " + (cntShots/atomicLong.doubleValue()));
                }
        );

    }*/
}
