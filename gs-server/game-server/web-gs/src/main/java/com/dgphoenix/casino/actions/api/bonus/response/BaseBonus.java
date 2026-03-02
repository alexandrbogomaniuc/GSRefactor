package com.abs.casino.actions.api.bonus.response;

import com.abs.casino.actions.api.bonus.AbstractBonusAction;
import com.abs.casino.actions.api.bonus.BonusForm;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.web.bonus.CBonus;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class BaseBonus {
    @SerializedName(CBonus.BONUSID)
    protected long id;
    @SerializedName(CBonus.GAMEIDS)
    protected String[] gameIds;
    @SerializedName(CBonus.DESCRIPTION)
    protected String description;
    @SerializedName(CBonus.AWARDDATE)
    protected String dateAwarded;
    @SerializedName(CBonus.AWARDTIME)
    protected String timeAwarded;
    @SerializedName(CBonus.ENDDATE)
    protected String endDate;
    @SerializedName(CBonus.STATUS)
    protected String status;

    public BaseBonus(com.abs.casino.common.cache.data.bonus.BaseBonus bonus, BonusForm form) throws CommonException {
        this.id = bonus.getId();
        this.gameIds = StringUtils.split(AbstractBonusAction.getGameIds(bonus), ',');
        LocalDateTime awardDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(bonus.getTimeAwarded()),
                TimeZone.getDefault().toZoneId());
        this.dateAwarded = awardDate.format(AbstractBonusAction.DATE_FORMATTER);
        if (form.isSendBonusAwardTime()) {
            this.timeAwarded = awardDate.format(AbstractBonusAction.TIME_FORMATTER);
        }
        this.description = bonus.getDescription();

        this.status = bonus.getStatus().name();
        if (bonus.getEndTime() != null) {
            LocalDateTime endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(bonus.getEndTime()),
                    TimeZone.getDefault().toZoneId());
            this.endDate = endTime.format(AbstractBonusAction.DATE_FORMATTER);
        }
    }
}
