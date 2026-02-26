package com.abs.casino.actions.api.promo;

import org.apache.struts.action.ActionForm;

/**
 * User: flsh
 * Date: 03.09.2019.
 */
public class GetTournamentPlayerInfoForm extends ActionForm {
    private Long bankId;
    private Long campaignId;
    private String extUserId;

    public Long getBankId() {
        return bankId;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getExtUserId() {
        return extUserId;
    }

    public void setExtUserId(String extUserId) {
        this.extUserId = extUserId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GetTournamentPlayerInfoForm [");
        sb.append("bankId=").append(bankId);
        sb.append(", campaignId=").append(campaignId);
        sb.append(", extUserId='").append(extUserId).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
