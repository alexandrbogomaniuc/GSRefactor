package com.abs.casino.actions.enter.game.cwv3;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.abs.casino.actions.enter.game.IStartGameForm;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Predicate;

public class CWRegisterForm extends CommonActionForm implements IStartGameForm {
    private static final Logger LOG = LogManager.getLogger(CWRegisterForm.class);
    protected String token;
    protected String mode;
    protected String gameId;
    protected String lang;
    protected long balance;
    private String serverId;

    protected GameMode gameMode;

    public CWRegisterForm() {
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public boolean isNotGameFRB() {
        return false;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);

        if (!errors.isEmpty()) {
            return errors;
        }

        boolean valid = validateToken(errors)
                .test(request);

        if (valid) {
            String serverIdParameter = BaseAction.extractRequestParameterIgnoreCase(request, "serverId");
            if (!StringUtils.isTrimmedEmpty(serverIdParameter)) {
                setServerId(serverIdParameter);
            }
        }

        return errors;
    }

    protected Predicate<HttpServletRequest> validateToken(ActionErrors errors) {
        return request -> {
            this.token = BaseAction.extractRequestParameterIgnoreCase(request, getTokenParamName());
            if (gameMode != GameMode.FREE && StringUtils.isTrimmedEmpty(token)) {
                getLogger().warn("empty_credentials, ''token' parameter not found");
                errors.add("empty_credentials", new ActionMessage("error.login.incorrectCredentials"));
                return false;
            }
            return true;
        };
    }

    protected String getTokenParamName() {
        return "token";
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public boolean isLaunchChecked() {
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CWRegisterForm");
        sb.append("[token='").append(token).append('\'');
        sb.append(", mode='").append(mode).append('\'');
        sb.append(", gameId=").append(gameId);
        sb.append(", gameMode=").append(gameMode);
        sb.append(", bankId=").append(getBankId());
        sb.append(", balance=").append(balance);
        sb.append(", serverId=").append(serverId);
        sb.append(']');
        return sb.toString();
    }
}