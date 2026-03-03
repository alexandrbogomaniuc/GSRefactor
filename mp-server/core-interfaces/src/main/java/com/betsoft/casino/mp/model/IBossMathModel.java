package com.betsoft.casino.mp.model;
import com.abs.casino.common.util.Pair;

import java.util.List;
import java.util.Map;

public interface IBossMathModel<PRIZE extends IEnemyPrize> extends IEnemyMathModel {
    Map<Integer, List<Pair<PRIZE, Long>>> getExtraAwards();

    double[] getExtraAwardsProbabilitiesByWeapon(SpecialWeaponType weaponType);

    double[] getTable2ForSharedPrize(int cntSeats);

    double[] getTable3ForSharedPrize(int cntSeats);
}
