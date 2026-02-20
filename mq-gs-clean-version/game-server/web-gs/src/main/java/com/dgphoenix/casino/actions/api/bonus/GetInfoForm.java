package com.dgphoenix.casino.actions.api.bonus;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * User: ktd
 * Date: 01.04.11
 */
public class GetInfoForm extends BonusForm {
    private final static Logger LOG = LogManager.getLogger(GetInfoForm.class);

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
        sb.append("GetInfoForm");
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
