package com.abs.casino.kafka.dto;

public class MQQuestPrizeDto {
    private MQQuestAmountDto amount;
    private int specialWeaponId;

    public MQQuestPrizeDto() {}

    public MQQuestPrizeDto(MQQuestAmountDto amount,
            int specialWeaponId) {
        super();
        this.amount = amount;
        this.specialWeaponId = specialWeaponId;
    }

    public MQQuestAmountDto getAmount() {
        return amount;
    }

    public int getSpecialWeaponId() {
        return specialWeaponId;
    }

    public void setAmount(MQQuestAmountDto amount) {
        this.amount = amount;
    }

    public void setSpecialWeaponId(int specialWeaponId) {
        this.specialWeaponId = specialWeaponId;
    }
}
