package com.abs.casino.forms.api.history;

import org.apache.struts.action.ActionForm;

public class HistoryUrlForm extends ActionForm {

    private long bankId;
    private long roundId;
    private String playerId;

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}