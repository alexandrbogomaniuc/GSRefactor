package com.abs.casino.actions.api.frbonus;

import com.abs.casino.actions.api.bonus.BonusForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class GetFRBInfoForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(GetFRBInfoForm.class);

    private String userId;
    private Long bonusId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    @Override
    public String toString() {
        final String TAB = "    ";
        final StringBuilder sb = new StringBuilder();
        sb.append("GetFRBInfoForm");
        sb.append("[ " + super.toString() + TAB);
        sb.append("userId='").append(userId).append('\'');
        sb.append(", bonusId=").append(bonusId);
        sb.append(']');
        return sb.toString();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
