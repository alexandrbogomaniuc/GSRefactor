package com.betsoft.casino.mp.model;

import java.util.List;
import java.util.Map;

/**
 * User: flsh
 * Date: 08.02.19.
 */
public interface IEnemyType<ENEMY_PRIZE extends IEnemyPrize> {
    int getId();

    String getName();

    int getMaxSkins();

    int getWidth();

    int getHeight();

    Map<ENEMY_PRIZE, Long> getPayTable();

    double getSumAward();

    boolean isBoss();

    boolean isHVenemy();

    List<Skin> getSkins();

    ISkin getSkin(int skin);

    int getReward();

    double[] getTreasureDropRates();

    IChestProb getChestProb();

    default IEnemyMathModel getModel(){
        return null;
    }
}
