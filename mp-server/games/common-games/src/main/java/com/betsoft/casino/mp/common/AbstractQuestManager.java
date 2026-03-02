package com.betsoft.casino.mp.common;

import com.betsoft.casino.mp.model.*;
import com.betsoft.casino.mp.model.quests.*;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.teststand.TestStandFeature;
import com.abs.casino.common.util.RNG;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: flsh
 * Date: 15.02.19.
 */
public abstract class AbstractQuestManager< TREASURE_GROUP extends ITreasureGroup, TREASURE extends ITreasure,
        ENEMY_TYPE extends IEnemyType, ENEMY_GROUP extends IEnemyGroup> {
    protected static final Integer MAX_NUMBER_QUESTS = 25;

    protected final ITransportObjectsFactoryService toFactoryService;

    protected AbstractQuestManager(ITransportObjectsFactoryService toFactoryService) {
        this.toFactoryService = toFactoryService;
    }

    public abstract TREASURE[] getTreasures();

    public abstract TREASURE getTreasureById(int id);

    public abstract List<ITreasure> getTreasures(TreasureRarity rarity);

    public abstract ENEMY_TYPE[] getEnemyTypes();

    public abstract ENEMY_TYPE getEnemyTypeById(int id);

    public abstract IEnemyType getBossEnemyType();

    public abstract <ENUM extends Enum<?>> Class<ENUM> getTreasureGroupClass();

    public abstract <ENUM extends Enum<?>> Class<ENUM> getTreasureClass();

    public abstract <ENUM extends Enum<?>> Class<ENUM> getEnemyTypeClass();

    public abstract <ENUM extends Enum<?>> Class<ENUM> getEnemyGroupClass();

    public int maxNumberQuests() {
        return MAX_NUMBER_QUESTS;
    }

    public int generateExp(ENEMY_TYPE enemyType) {
        IChestProb chestProb = enemyType.getChestProb();
        if (RNG.rand() > chestProb.getChance()) {
            int chestSizeId = IChestProb.roll(chestProb.getProbabilities());
            if (chestSizeId != -1) {
                return chestProb.getSizes()[chestSizeId];
            }
        }
        return 0;
    }

    public boolean isQuestByFeature(TestStandFeature featureBySid) {
        return featureBySid != null;
    }

    public IQuest generateNewRandomQuest(TestStandFeature featureBySid, QuestType questType, double currencyRate) {

        if (questType == null)
            questType = getRandomValue(QuestType.class);

        int enemyGroupId = -1;
        int treasureTypeId = -1;
        int treasureGroupId = -1;
        ArrayList<IEnemyProgress> enemyProgresses = new ArrayList<>();
        ArrayList<ITreasureProgress> treasureProgresses = new ArrayList<>();
        int totalGoal = -1;

        if (isQuestByFeature(featureBySid)) {
            questType = QuestType.values()[featureBySid.getId() - 11];
        }

        switch (questType) {
            case COLLECT_SET_TREASURES: {
                TreasureRarity treasureRarity = getRandomValue(TreasureRarity.class);
                treasureTypeId = treasureRarity.ordinal();
                for (ITreasure treasure : getTreasures(treasureRarity)) {
                    treasureProgresses.add(toFactoryService.createTreasureProgress(treasure.getOrdinalValue(), 0, 1));
                }
            }
            break;
            case COLLECT_GROUP_TREASURES: {
                ITreasureGroup treasureGroup = getRandomValue(getTreasureGroupClass());
                treasureGroupId = treasureGroup.getOrdinalValue();
                for (ITreasure treasure : treasureGroup.getTreasures()) {
                    treasureProgresses.add(toFactoryService.createTreasureProgress(treasure.getOrdinalValue(), 0, 1));
                }
            }
            break;
            case COLLECT_N_TREASURES_SPECIFIC_TYPE: {
                ITreasure randomTreasure = getRandomValue(getTreasureClass());
                int goal = RNG.nextInt(10) + 1;
                treasureProgresses.add(toFactoryService.createTreasureProgress(randomTreasure.getOrdinalValue(), 0, goal));
            }
            break;
            case COLLECT_N_ANY_TREASURES_FROM_GROUP: {
                totalGoal = RNG.nextInt(10) + 4;
                TreasureRarity treasureRarity = getRandomValue(TreasureRarity.class);
                treasureTypeId = treasureRarity.ordinal();
                for (ITreasure treasure : getTreasures(treasureRarity)) {
                    treasureProgresses.add(toFactoryService.createTreasureProgress(treasure.getOrdinalValue(), 0, -1));
                }
            }
            break;
            case KILL_N_ENEMIES_SPECIFIC_TYPE: {
                IEnemyType randomEnemyType;
                do {
                    randomEnemyType = getRandomValue(getEnemyTypeClass());
                }
                while (randomEnemyType.isHVenemy());

                List<Skin> skins = randomEnemyType.getSkins();
                int skinId = 0;
                if (skins.size() > 1) {
                    skinId = RNG.nextInt(skins.size());
                }
                int goal = RNG.nextInt(10) + 5;

                enemyProgresses.add(toFactoryService.createEnemyProgress(randomEnemyType.getId(), skinId + 1, 0, goal));
            }
            break;
            case KILL_N_ENEMIES_EACH_TYPE_IN_RANDOM_GROUP: {
                int goal = RNG.nextInt(10) + 5;
                IEnemyGroup randomGroup = getRandomValue(getEnemyGroupClass());
                enemyGroupId = randomGroup.getId() - 1;
                for (IEnemyType enemy : randomGroup.getEnemies()) {
                    List<Skin> skins = enemy.getSkins();
                    for (int skinId = 0; skinId < skins.size(); skinId++) {
                        enemyProgresses.add(toFactoryService.createEnemyProgress(enemy.getId(), skinId + 1, 0, goal));
                    }
                }
            }
            break;
            case KILL_N_RANDOM_ENEMIES_IN_RANDOM_GROUP: {
                totalGoal = RNG.nextInt(10) + 5;
                IEnemyGroup randomGroup = getRandomValue(getEnemyGroupClass());
                enemyGroupId = randomGroup.getId() - 1;
                for (IEnemyType enemy : randomGroup.getEnemies()) {
                    List<Skin> skins = enemy.getSkins();
                    for (int skinId = 0; skinId < skins.size(); skinId++) {
                        enemyProgresses.add(toFactoryService.createEnemyProgress(enemy.getId(), skinId + 1, 0, -1));
                    }
                }
            }
            break;
            case KILL_N_TIMES_EACH_ENEMY: {
                int goal = RNG.nextInt(10) + 5;
                for (IEnemyType enemy : getEnemyTypes()) {
                    if (enemy.isHVenemy())
                        continue;
                    List<Skin> skins = enemy.getSkins();
                    for (int skinId = 0; skinId < skins.size(); skinId++) {
                        enemyProgresses.add(toFactoryService.createEnemyProgress(enemy.getId(), skinId + 1, 0, goal));
                    }
                }
            }
            break;
            case KILL_ONE_RANDOM_GOD: {
                enemyProgresses.add(toFactoryService.createEnemyProgress(getBossEnemyType().getId(),
                        (RNG.nextInt(3)) + 1, 0, 1));
            }
            break;
            case KILL_ALL_GODS: {
                for (int skinId = 0; skinId < 3; skinId++) {
                    enemyProgresses.add(toFactoryService.createEnemyProgress(getBossEnemyType().getId(), skinId + 1, 0, 1));
                }
            }
            break;

        }

//        QuestProgress progress = new QuestProgress(enemyProgresses, treasureProgresses, totalGoal);
//        long xpForDifficulty = questType.equals(QuestType.KILL_ALL_GODS) ? 250 : getXPForProgress(progress);
//        QuestDifficulty difficulty = getDifficulty(xpForDifficulty);
//        xpForDifficulty *= currencyRate;

//        return new Quest(-1, questType.ordinal(), difficulty, false, false,
//                xpForDifficulty, enemyGroupId, treasureTypeId, treasureGroupId, progress);
        return null;
    }

    private static QuestDifficulty getDifficulty(long xp) {
        if (xp > 1000) return QuestDifficulty.Legendary;
        else if (xp > 700) return QuestDifficulty.Master;
        else if (xp > 300) return QuestDifficulty.Experienced;
        else if (xp > 100) return QuestDifficulty.Intermediate;
        else return QuestDifficulty.Novice;
    }

    private ENEMY_TYPE getCorrectEnemyType(int ordinal) {
        ENEMY_TYPE enemyType;
        do {
            enemyType = (ENEMY_TYPE) getRandomValue(getEnemyTypeClass());
            double[] treasureDropRates = enemyType.getTreasureDropRates();
            if (treasureDropRates.length - 1 < ordinal)
                continue;
            if (treasureDropRates[ordinal] != 0)
                break;
        } while (true);
        return enemyType;
    }

    public void updateTreasures(AtomicBoolean changed, List<ITreasureProgress> treasures, List<Award> awards) {
        treasures.forEach(progress -> {
            long count = awards.stream().flatMap(award -> award.getTreasures().stream()).
                    filter(t -> t.getOrdinalValue() == progress.getTreasureId()).count();
            if (count > 0) {
                progress.addCollect((int) count);
                changed.set(true);
            }
        });
    }

    private void updateEnemies(AtomicBoolean changed, List<IEnemyProgress> enemies, int lastEnemyTypeId,
                                      int type, int skin) {
        enemies.stream().filter(e -> (e.getTypeId() == lastEnemyTypeId) && (e.getSkin() == skin)).forEach(enemy -> {
            if (type == 7 || type == 5) {
                if (enemy.getKills() < enemy.getGoal()) {
                    enemy.incrementKills();
                    changed.set(true);
                }
            } else {
                enemy.incrementKills();
                changed.set(true);
            }
        });
    }


    private <T extends Enum<?>> T getRandomValue(Class<T> clazz) {
        return clazz.getEnumConstants()[RNG.nextInt(clazz.getEnumConstants().length)];
    }


    boolean newQuestAllowed(Set<IQuest> quests) {
        return quests.size() < MAX_NUMBER_QUESTS;
    }

    boolean needRemoveOldQuests(Set<IQuest> quests) {
        return quests.size() >= MAX_NUMBER_QUESTS;
    }
}
