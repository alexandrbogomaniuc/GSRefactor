package com.dgphoenix.casino.actions.game.pov;

/**
 * User: flsh
 * Date: 18.09.2020.
 */
public class StartPovGameParams {
    private String bankId;
    private String sessionId;
    private String gameId;
    private String lang;
    private String mode;
    private String websocket;
    private String serverId;
    private String noFRB;
    private String cashierUrl;
    private String homeURL;
    private String helpPath;
    private Integer timerOffset;
    private boolean testSystem;
    private String customerSettingsUrl;
    private String activePromos;
    private String weaponsMode;
    private boolean weaponsSavingAllowed;
    private String bonusId;
    private String tournamentId;
    private boolean disableAutofiring;

    public StartPovGameParams(String bankId, String sessionId, String gameId, String lang, String mode,
                              String websocket, String serverId, String noFRB, String cashierUrl, String homeURL,
                              String helpPath, Integer timerOffset, boolean testSystem, String customerSettingsUrl,
                              String activePromos, String weaponsMode, boolean weaponsSavingAllowed, String bonusId,
                              String tournamentId, boolean disableAutofiring) {
        this.bankId = bankId;
        this.sessionId = sessionId;
        this.gameId = gameId;
        this.lang = lang;
        this.mode = mode;
        this.websocket = websocket;
        this.serverId = serverId;
        this.noFRB = noFRB;
        this.cashierUrl = cashierUrl;
        this.homeURL = homeURL;
        this.helpPath = helpPath;
        this.timerOffset = timerOffset;
        this.testSystem = testSystem;
        this.customerSettingsUrl = customerSettingsUrl;
        this.activePromos = activePromos;
        this.weaponsMode = weaponsMode;
        this.weaponsSavingAllowed = weaponsSavingAllowed;
        this.bonusId = bonusId;
        this.tournamentId = tournamentId;
        this.disableAutofiring = disableAutofiring;
    }

    public String getBankId() {
        return bankId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getGameId() {
        return gameId;
    }

    public String getLang() {
        return lang;
    }

    public String getMode() {
        return mode;
    }

    public String getWebsocket() {
        return websocket;
    }

    public String getServerId() {
        return serverId;
    }

    public String getNoFRB() {
        return noFRB;
    }

    public String getCashierUrl() {
        return cashierUrl;
    }

    public String getHomeURL() {
        return homeURL;
    }

    public String getHelpPath() {
        return helpPath;
    }

    public Integer getTimerOffset() {
        return timerOffset;
    }

    public boolean isTestSystem() {
        return testSystem;
    }

    public String getCustomerSettingsUrl() {
        return customerSettingsUrl;
    }

    public String getActivePromos() {
        return activePromos;
    }

    public String getWeaponsMode() {
        return weaponsMode;
    }

    public boolean isWeaponsSavingAllowed() {
        return weaponsSavingAllowed;
    }

    public String getBonusId() {
        return bonusId;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public boolean isDisableAutofiring() {
        return disableAutofiring;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StartPovGameParams [");
        sb.append("bankId='").append(bankId).append('\'');
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", gameId='").append(gameId).append('\'');
        sb.append(", lang='").append(lang).append('\'');
        sb.append(", mode='").append(mode).append('\'');
        sb.append(", websocket='").append(websocket).append('\'');
        sb.append(", serverId='").append(serverId).append('\'');
        sb.append(", noFRB='").append(noFRB).append('\'');
        sb.append(", cashierUrl='").append(cashierUrl).append('\'');
        sb.append(", homeURL='").append(homeURL).append('\'');
        sb.append(", helpPath='").append(helpPath).append('\'');
        sb.append(", timerOffset=").append(timerOffset);
        sb.append(", testSystem=").append(testSystem);
        sb.append(", customerSettingsUrl='").append(customerSettingsUrl).append('\'');
        sb.append(", activePromos='").append(activePromos).append('\'');
        sb.append(", weaponsMode='").append(weaponsMode).append('\'');
        sb.append(", weaponsSavingAllowed=").append(weaponsSavingAllowed);
        sb.append(", bonusId='").append(bonusId).append('\'');
        sb.append(", tournamentId='").append(tournamentId).append('\'');
        sb.append(", disableAutofiring=").append(disableAutofiring);
        sb.append(']');
        return sb.toString();
    }
}
