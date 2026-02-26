package com.abs.casino.entities.game.requests;

import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.forms.game.CommonStartGameForm;
import com.dgphoenix.casino.helpers.game.processors.StartGameProcessor;
import org.apache.commons.lang.BooleanUtils;

/**
 * User: isirbis
 * Date: 30.09.14
 */
public class StartGameRequest<F extends CommonStartGameForm> {
    private SessionInfo sessionInfo;
    private AccountInfo accountInfo;
    private Integer bankId;
    private Integer gameId;
    private GameMode gameMode;
    private String language;
    private ClientType clientType;

    private Boolean notGameFRB;

    private Boolean checkWalletOps;

    private String profileId;

    protected StartGameProcessor startGameProcessor = new StartGameProcessor();

    private void baseSetFields(SessionInfo sessionInfo, AccountInfo accountInfo, F form) {
        this.sessionInfo = sessionInfo;
        this.accountInfo = accountInfo;
        this.bankId = form.getBankId();
        this.gameId = form.getGameId();
        this.gameMode = form.getGameMode();
        this.language = form.getLang();
        this.clientType = form.getClientType();
    }

    public StartGameRequest(SessionInfo sessionInfo, AccountInfo accountInfo, F form) {
        baseSetFields(sessionInfo, accountInfo, form);
    }

    public StartGameRequest(SessionInfo sessionInfo, AccountInfo accountInfo, F form, boolean checkWalletOps) {
        baseSetFields(sessionInfo, accountInfo, form);
        this.checkWalletOps = checkWalletOps;
    }

    public SessionInfo getSessionInfo() {
        return sessionInfo;
    }

    public void setSessionInfo(SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public Integer getBankId() {
        return bankId;
    }

    public void setBankId(Integer bankId) {
        this.bankId = bankId;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean isNotGameFRB() {
        return BooleanUtils.isTrue(notGameFRB);
    }

    public void setNotGameFRB(boolean notGameFRB) {
        this.notGameFRB = notGameFRB;
    }

    public Boolean getCheckWalletOps() {
        if (checkWalletOps == null) {
            return false;
        }
        return checkWalletOps;
    }

    public void setCheckWalletOps(Boolean checkWalletOps) {
        this.checkWalletOps = checkWalletOps;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public StartGameProcessor getStartGameProcessor() {
        return startGameProcessor;
    }

    public void setStartGameProcessor(StartGameProcessor startGameProcessor) {
        this.startGameProcessor = startGameProcessor;
    }
}
