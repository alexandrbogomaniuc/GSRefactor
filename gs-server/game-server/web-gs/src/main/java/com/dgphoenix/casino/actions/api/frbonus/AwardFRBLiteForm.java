package com.abs.casino.actions.api.frbonus;

import com.abs.casino.actions.api.bonus.BonusForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

public class AwardFRBLiteForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(AwardFRBLiteForm.class);

    private String userId;
    private String games;
    private String rounds;
    private String comment;
    private String description;
    private String extBonusId;
    private String startTime;
    private String expirationTime;
    private String duration;
    private String frbTableRoundChips;
    private String timeZone;
    private String profile;

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

    public String getFrbTableRoundChips() {
        return frbTableRoundChips;
    }

    public void setFrbTableRoundChips(String frbTableRoundChips) {
        this.frbTableRoundChips = frbTableRoundChips;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = new ActionErrors();
        return actionErrors;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
