package com.betsoft.casino.mp.common.testmodel;

import com.betsoft.casino.mp.model.quests.IQuestAmount;
import com.betsoft.casino.mp.model.quests.IQuestPrize;

import java.util.Objects;

public class StubQuestPrize implements IQuestPrize {
    private IQuestAmount amount;
    private int specialWeaponId;

    public StubQuestPrize(IQuestAmount amount, int specialWeaponId) {
        this.amount = amount;
        this.specialWeaponId = specialWeaponId;
    }

    @Override
    public IQuestAmount getAmount() {
        return amount;
    }

    @Override
    public void setAmount(IQuestAmount amount) {
        this.amount = amount;
    }

    @Override
    public int getSpecialWeaponId() {
        return specialWeaponId;
    }

    @Override
    public void setSpecialWeaponId(int specialWeaponId) {
        this.specialWeaponId = specialWeaponId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StubQuestPrize that = (StubQuestPrize) o;
        return amount == that.amount &&
                specialWeaponId == that.specialWeaponId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, specialWeaponId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Prize[");
        sb.append("amount=").append(amount);
        sb.append(", specialWeaponId=").append(specialWeaponId);
        sb.append(']');
        return sb.toString();
    }
}
