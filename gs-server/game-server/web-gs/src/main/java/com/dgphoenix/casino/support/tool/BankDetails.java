package com.dgphoenix.casino.support.tool;

import java.util.ArrayList;
import java.util.List;

public class BankDetails {
    private Long bankId;
    private String description;
    private Long masterBankId;
    private List<GameInfoDetails> gameInfoDetails = new ArrayList<>();

    public List<GameInfoDetails> getGameInfoDetails() {
        return gameInfoDetails;
    }

    public Long getBankId() {
        return bankId;
    }

    public Long getMasterBankId() {
        return masterBankId;
    }

    public String getDescription() {
        return description;
    }

    public void setBankId(Long bankId) {
        this.bankId = bankId;
    }

    public void setMasterBankId(Long masterBankId) {
        this.masterBankId = masterBankId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addGameInfoDetails(GameInfoDetails gameInfoDetails) {
        this.gameInfoDetails.add(gameInfoDetails);
    }
}