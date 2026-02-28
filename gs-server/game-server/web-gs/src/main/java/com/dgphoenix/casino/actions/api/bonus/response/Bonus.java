package com.abs.casino.actions.api.bonus.response;


import com.abs.casino.actions.api.bonus.AbstractBonusAction;
import com.abs.casino.actions.api.bonus.BonusForm;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.web.bonus.CBonus;
import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class Bonus extends BaseBonus {
    @SerializedName(CBonus.TYPE)
    protected String type;
    @SerializedName(CBonus.AMOUNT)
    protected Long amount;
    @SerializedName(CBonus.BALANCE)
    protected Long balance;
    @SerializedName(CBonus.ROLLOVER)
    protected Long rollover;
    @SerializedName(CBonus.COLLECTED)
    protected Long collected;
    @SerializedName(CBonus.MAXWIN)
    protected Long maxWin;
    @SerializedName(CBonus.EXPDATE)
    protected String expDate;

    public Bonus(com.dgphoenix.casino.common.cache.data.bonus.Bonus bonus, BonusForm form) throws CommonException {
        super(bonus, form);
        this.type = bonus.getType().toString();
        this.amount = bonus.getAmount();
        this.balance = bonus.getBalance();
        this.rollover = (long) (bonus.getRolloverMultiplier() * bonus.getAmount());
        this.collected = bonus.getBetSum();
        this.maxWin = bonus.getMaxWinLimit();
        LocalDateTime expTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(bonus.getExpirationDate()),
                TimeZone.getDefault().toZoneId());
        this.expDate = expTime.format(AbstractBonusAction.DATE_FORMATTER);
    }
}
