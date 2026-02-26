package com.abs.casino.common.web.login.apub;

public class GameServerResponse {
    private String status;
    private Long accountId;
    private String sessionId;
    private Long gameSessionId;
    private String oneclick;
    private String availableMoney;
    private boolean isLasthand;
    private String description;

    private String additionalParam1;
    private String additionalParam2;
    private String additionalParam3;
    private String additionalParam4;

    public GameServerResponse() {
        super();
        this.isLasthand = false;
    }

    public GameServerResponse(String status, String description) {
        this();
        this.status = status;
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOneclick() {
        return oneclick;
    }

    public void setOneclick(String oneclick) {
        this.oneclick = oneclick;
    }

    public boolean isLasthand() {
        return isLasthand;
    }

    public void setLasthand(boolean isLasthand) {
        this.isLasthand = isLasthand;
    }

    public String getAvailableMoney() {
        return availableMoney;
    }

    public void setAvailableMoney(String availableMoney) {
        this.availableMoney = availableMoney;
    }

    public String getAdditionalParam1() {
        return additionalParam1;
    }

    public void setAdditionalParam1(String additionalParam1) {
        this.additionalParam1 = additionalParam1;
    }

    public String getAdditionalParam2() {
        return additionalParam2;
    }

    public void setAdditionalParam2(String additionalParam2) {
        this.additionalParam2 = additionalParam2;
    }

    public String getAdditionalParam3() {
        return additionalParam3;
    }

    public void setAdditionalParam3(String additionalParam3) {
        this.additionalParam3 = additionalParam3;
    }

    public String getAdditionalParam4() {
        return additionalParam4;
    }

    public void setAdditionalParam4(String additionalParam4) {
        this.additionalParam4 = additionalParam4;
    }

    public String toString() {
        return "GameServerResponse [" +
                super.toString() +
                ", status=" + this.status +
                ", accountId=" + this.accountId +
                ", sessionId=" + this.sessionId +
                ", gameSessionId=" + this.gameSessionId +
                ", oneclick=" + this.oneclick +
                ", availableMoney=" + this.availableMoney +
                ", isLasthand=" + this.isLasthand +
                ", description=" + this.description +
                ", additionalParam1=" + this.additionalParam1 +
                ", additionalParam2=" + this.additionalParam2 +
                ", additionalParam3=" + this.additionalParam3 +
                ", additionalParam4=" + this.additionalParam4 +
                "]";
    }
}
