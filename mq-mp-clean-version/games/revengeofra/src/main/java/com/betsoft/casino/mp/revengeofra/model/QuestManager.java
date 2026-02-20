package com.betsoft.casino.mp.revengeofra.model;

import com.betsoft.casino.mp.common.AbstractQuestManager;
import com.betsoft.casino.mp.model.ITreasure;
import com.betsoft.casino.mp.model.TreasureRarity;
import com.betsoft.casino.mp.revengeofra.model.math.EnemyGroup;
import com.betsoft.casino.mp.revengeofra.model.math.EnemyType;
import com.betsoft.casino.mp.revengeofra.model.math.Treasure;
import com.betsoft.casino.mp.revengeofra.model.math.TreasureGroup;
import com.betsoft.casino.mp.service.ITransportObjectsFactoryService;
import com.betsoft.casino.teststand.TestStandFeature;

import java.util.List;


public class QuestManager extends AbstractQuestManager<TreasureGroup, Treasure, EnemyType, EnemyGroup> {

    public QuestManager(ITransportObjectsFactoryService toFactoryService) {
        super(toFactoryService);
    }

    @Override
    public Treasure[] getTreasures() {
        return Treasure.values();
    }

    public Treasure getTreasureById(int id) {
        return Treasure.getById(id);
    }

    @Override
    public List<ITreasure> getTreasures(TreasureRarity rarity) {
        return Treasure.getTreasures(rarity);
    }

    @Override
    public EnemyType[] getEnemyTypes() {
        return EnemyType.values();
    }

    @Override
    public EnemyType getEnemyTypeById(int id) {
        return EnemyType.getById(id);
    }

    @Override
    public EnemyType getBossEnemyType() {
        return EnemyType.Boss;
    }

    @Override
    public Class<TreasureGroup> getTreasureGroupClass() {
        return TreasureGroup.class;
    }

    @Override
    public Class<Treasure> getTreasureClass() {
        return Treasure.class;
    }

    @Override
    public Class<EnemyType> getEnemyTypeClass() {
        return EnemyType.class;
    }

    @Override
    public Class<EnemyGroup> getEnemyGroupClass() {
        return EnemyGroup.class;
    }

    @Override
    public boolean isQuestByFeature(TestStandFeature featureBySid) {
        return featureBySid != null && featureBySid.getId() < 21;
    }
}
