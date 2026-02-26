package com.abs.casino.actions.api.frbonus;

import com.dgphoenix.casino.actions.api.bonus.BonusForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class AwardFRBForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(AwardFRBForm.class);

    private String userId;
    private String games;
    private String rounds;
    private String comment;
    private String description;
    private String extBonusId;
    private String startTime;
    private String expirationTime;
    private String duration;
    private String expirationHours;
    private String frbTableRoundChips;
    private String timeZone;
    private String profileId;
    private String maxWinLimit;

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

    public String getGames() {
        return games;
    }

    public void setGames(String games) {
        this.games = games;
    }

    public String getRounds() {
        return rounds;
    }

    public void setRounds(String rounds) {
        this.rounds = rounds;
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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getExpirationHours() {
        return expirationHours;
    }

    public void setExpirationHours(String expirationHours) {
        this.expirationHours = expirationHours;
    }

    public String getFrbTableRoundChips() {
        return frbTableRoundChips;
    }

    public void setFrbTableRoundChips(String frbTableRoundChips) {
        this.frbTableRoundChips = frbTableRoundChips;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getMaxWinLimit() {
        return maxWinLimit;
    }

    public void setMaxWinLimit(String maxWinLimit) {
        this.maxWinLimit = maxWinLimit;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
