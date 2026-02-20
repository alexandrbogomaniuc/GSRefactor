package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 16.07.2020.
 */
public interface ISitOutCashBonusSessionResult extends ISitOutResult {
    IActiveCashBonusSession getCashBonus();

    Long getActiveFRBonusId();
}
