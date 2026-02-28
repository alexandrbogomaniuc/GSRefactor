package com.abs.casino.actions.api.bonus.response;

import com.dgphoenix.casino.actions.api.bonus.AbstractBonusAction;
import com.dgphoenix.casino.actions.api.bonus.BonusForm;
import com.dgphoenix.casino.common.exception.CommonException;
import com.abs.casino.common.web.bonus.CBonus;
import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class FRBonus extends BaseBonus {
    @SerializedName(CBonus.EXTBONUSID)
    protected String extId;
    @SerializedName(CBonus.ROUNDS)
    protected Long rounds;
    @SerializedName(CBonus.ROUNDSLEFT)
    protected Long roundsLeft;
    @SerializedName(CBonus.START_DATE)
    protected String startDate;
    @SerializedName(CBonus.EXPDATE)
    protected String expDate;
    @SerializedName(CBonus.DURATION)
    protected Long duration;
    @SerializedName(CBonus.MAXWIN)
    protected Long maxWinLimit;
    @SerializedName(CBonus.WINSUM)
    protected Long winSum;
    @SerializedName(CBonus.BETSUM)
    protected Long betSum;

    public FRBonus(com.dgphoenix.casino.common.cache.data.bonus.FRBonus frBonus, BonusForm form) throws CommonException {
        super(frBonus, form);
        if (form.isSendDetailsOnFrbInfo()) {
            this.extId = frBonus.getExtId();
        }
        this.rounds = frBonus.getRounds();
        this.roundsLeft = frBonus.getRoundsLeft();

        if (frBonus.getStartDate() != null) {
            LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(frBonus.getStartDate()),
                    TimeZone.getDefault().toZoneId());
            this.startDate = startDateTime.format(AbstractBonusAction.DATE_TIME_FORMATTER);
        }
        if (frBonus.getExpirationDate() != null) {
            LocalDateTime expDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(frBonus.getExpirationDate()),
                    TimeZone.getDefault().toZoneId());
            this.expDate = expDateTime.format(AbstractBonusAction.DATE_TIME_FORMATTER);
        }

        this.duration = frBonus.getFreeRoundValidity();
        this.maxWinLimit = frBonus.getMaxWinLimit();

        this.winSum = frBonus.getWinSum();
        this.betSum = frBonus.getBetSum();
    }
}
