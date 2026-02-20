package com.dgphoenix.casino.common.exception;

/**
 * User: van0ss
 * Date: 06.04.2017
 */
public class StartParameters {
    private long gameId;
    private String externalBankId;
    private long subcasinoId;
    private String lang;

    public StartParameters() {
    }

    public StartParameters(long gameId, String externalBankId, long subcasinoId, String lang) {
        this.gameId = gameId;
        this.externalBankId = externalBankId;
        this.subcasinoId = subcasinoId;
        this.lang = lang;
    }

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getExternalBankId() {
        return externalBankId;
    }

    public void setExternalBankId(String externalBankId) {
        this.externalBankId = externalBankId;
    }

    public long getSubcasinoId() {
        return subcasinoId;
    }

    public void setSubcasinoId(long subcasinoId) {
        this.subcasinoId = subcasinoId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
