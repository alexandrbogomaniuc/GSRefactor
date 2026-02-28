package com.abs.casino.actions.api.bonus;

import com.abs.casino.actions.api.bonus.BonusForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * User: ktd
 * Date: 01.04.11
 */
public class AwardForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(AwardForm.class);

    private String userId;
    private String type;
    private String amount;
    private String multiplier;
    private String games;
    private String gameIds;
    private String expDate;
    private String comment;
    private String description;
    private String extBonusId;
    private String timeZone;
    private String autoRelease;
    private String startTime;
    private String maxWinMultiplier;

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    public String getGames() {
        return games;
    }

    public void setGames(String games) {
        this.games = games;
    }

    public String getGameIds() {
        return gameIds;
    }

    public void setGameIds(String gameIds) {
        this.gameIds = gameIds;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExtBonusId() {
        return extBonusId;
    }

    public void setExtBonusId(String extBonusId) {
        this.extBonusId = extBonusId;
    }

    public String isAutoRelease() {
        return autoRelease;
    }

    public void setAutoRelease(String autoRelease) {
        this.autoRelease = autoRelease;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getMaxWinMultiplier() {
        return maxWinMultiplier;
    }

    public void setMaxWinMultiplier(String maxWinMultiplier) {
        this.maxWinMultiplier = maxWinMultiplier;
    }

    @Override
    public String toString() {
        final String TAB = "    ";
        final StringBuilder sb = new StringBuilder();
        sb.append("AwardForm");
        sb.append("[ " + super.toString() + TAB);
        sb.append("userId='").append(userId).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", amount=").append(amount);
        sb.append(", multiplier=").append(multiplier);
        sb.append(", games='").append(games).append('\'');
        sb.append(", gameIds='").append(gameIds).append('\'');
        sb.append(", expDate='").append(expDate).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", extBonusId='").append(extBonusId).append('\'');
        sb.append(", autoRelease='").append(autoRelease).append('\'');
        sb.append(", startTime='").append(startTime).append('\'');
        sb.append(", maxWinMultiplier='").append(maxWinMultiplier).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}

