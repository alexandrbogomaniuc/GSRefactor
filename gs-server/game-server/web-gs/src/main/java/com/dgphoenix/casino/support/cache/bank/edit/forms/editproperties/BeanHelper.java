package com.abs.casino.support.cache.bank.edit.forms.editproperties;


public class BeanHelper {

    private String gameId;
    private String bankId;

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BeanHelper [");
        sb.append("gameId='").append(gameId).append('\'');
        sb.append(", bankId='").append(bankId).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
