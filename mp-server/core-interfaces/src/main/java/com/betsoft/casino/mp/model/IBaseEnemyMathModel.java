package com.betsoft.casino.mp.model;
import com.dgphoenix.casino.common.util.Pair;

import java.util.List;

public interface IBaseEnemyMathModel<PRIZE extends IEnemyPrize> extends IEnemyMathModel {

    double getKillingProbability(SpecialWeaponType weaponType, boolean isNeaLandMine);

    double getBossProbability();

    List<Pair<PRIZE, Long>> getAwards();

    double[] getAwardsProbabilitiesByWeapon(SpecialWeaponType weaponType, boolean isNeaLandMine);

    double getSpecialBossProbability(SpecialWeaponType weaponType);


    default double getKillingProbability(SpecialWeaponType weaponType, boolean isNeaLandMine, int potMultiplier){
        return getKillingProbability(weaponType, isNeaLandMine);
    }

    default double getBossProbability(int potMultiplier){
        return getBossProbability();
    }

    default double[] getAwardsProbabilitiesByWeapon(SpecialWeaponType weaponType, boolean isNeaLandMine, int potMultiplier){
        return getAwardsProbabilitiesByWeapon(weaponType, isNeaLandMine);
    }

    default double getSpecialBossProbability(SpecialWeaponType weaponType, int potMultiplier){
        return getSpecialBossProbability(weaponType);
    }

}
