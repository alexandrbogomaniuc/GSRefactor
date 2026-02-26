package com.abs.casino.actions.enter.game.frb;


import org.apache.struts.action.ActionForm;

public class RestartGameForm extends ActionForm {

    private long bankId;
    private long gameId;
    private String sessionId;
    private String lang;
    private String mode;
    private Long bonusId = -1l;
    private Long balance = -1l;


    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getMode() {
        if (mode == null) {
            return "real";
        }
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Long getBonusId() {
        return bonusId;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "RestartGameForm[" +
                "bankId=" + bankId +
                ", gameId=" + gameId +
                ", sessionId='" + sessionId + '\'' +
                ", lang='" + lang + '\'' +
                ", mode='" + mode + '\'' +
                ", bonusId=" + bonusId +
                ", balance=" + balance +
                ']';
    }
}
