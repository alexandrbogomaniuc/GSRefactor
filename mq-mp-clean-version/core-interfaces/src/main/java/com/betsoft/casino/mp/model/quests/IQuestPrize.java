package com.betsoft.casino.mp.model.quests;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IQuestPrize<QA extends IQuestAmount> {
    QA getAmount();

    void setAmount(QA amount);

    int getSpecialWeaponId();

    void setSpecialWeaponId(int specialWeaponId);
}
