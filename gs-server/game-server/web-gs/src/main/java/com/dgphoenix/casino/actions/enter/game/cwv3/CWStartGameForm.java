package com.abs.casino.actions.enter.game.cwv3;

import com.abs.casino.actions.enter.CommonActionForm;
import com.abs.casino.actions.enter.game.IStartGameForm;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.common.web.MobileDetector;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Predicate;

public class CWStartGameForm extends CommonActionForm implements IStartGameForm {
    private static final Logger LOG = LogManager.getLogger(CWStartGameForm.class);
    private static final String NEW_GAMES_ROUTE_GAME_ID_KEY = "NEW_GAMES_ROUTE_GAME_ID";
    private static final String NEW_GAMES_ROUTE_GAME_ID_DEFAULT = "00010";
    protected String token;
    protected String mode;
    protected String gameId;
    protected String lang;
    protected long balance;
    private String serverId;

    protected GameMode gameMode;

    public CWStartGameForm() {
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

    protected ActionErrors validateCommonParams(ActionMapping mapping, HttpServletRequest request) {
        return super.validate(mapping, request);
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
        boolean valid = validateGameMode(errors)
                .and(validateGameId(errors))
                .and(validateToken(errors))
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

    protected Predicate<HttpServletRequest> validateGameId(ActionErrors errors) {
        return request -> {
            try {
                String strGameId = BaseAction.extractRequestParameterIgnoreCase(request, getGameIdParamName());
                if (StringUtils.isTrimmedEmpty(strGameId)) {
                    getLogger().warn("empty_credentials, ''gameId' parameter not found");
                    errors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
                    return false;
                }
                if (strGameId.equals("1") || strGameId.equals("0")) {
                    return true;
                }
                Long originalId = BaseGameCache.getInstance().getOriginalGameId(strGameId, getBankId());
                if (originalId == null) {
                    try {
                        this.gameId = strGameId;
                    } catch (NumberFormatException e) {
                        errors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
                        return false;
                    }
                } else {
                    this.gameId = String.valueOf(originalId.longValue());
                    getLogger().info("Game id was redefined from " + strGameId + ", to " + gameId);
                }

                if (isNewGamesRouteVirtualId(this.gameId)) {
                    return true;
                }

                IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(getBankId(), Long.parseLong(gameId), null);
                if (gameInfo == null) {
                    getLogger().warn("Cannot load gameInfo: bankId=" + getBankId() + ", gameId=" + gameId);
                    errors.add("empty_credentials", new ActionMessage("error.login.gameInfoNotDefined"));
                    return false;
                }
                gameInfo = MobileDetector.checkGameInfo(gameInfo, getClientType(), request.getHeader("User-Agent"));
                this.gameId = Long.toString(gameInfo.getId());
                if (!gameInfo.isBankEnabled()) {
                    errors.add("empty_credentials", new ActionMessage("error.login.bankIsNotEnabled"));
                    return false;
                }
                if (!gameInfo.isEnabled()) {
                    getLogger().debug("validation game is not enabled in casino:" + gameId);
                    errors.add("invalid_game", new ActionMessage("error.login.gameIsNotEnabled"));
                    return false;
                }
            } catch (Exception e) {
                getLogger().error("validate error:", e);
                errors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
                return false;
            }
            return true;
        };
    }

    protected boolean isNewGamesRouteVirtualId(String candidateGameId) {
        if (StringUtils.isTrimmedEmpty(candidateGameId)) {
            return false;
        }
        try {
            Long.parseLong(candidateGameId);
        } catch (NumberFormatException e) {
            return false;
        }

        String configuredGameId = NEW_GAMES_ROUTE_GAME_ID_DEFAULT;
        try {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(getBankId());
            if (bankInfo != null && bankInfo.getProperties() != null) {
                String configured = bankInfo.getProperties().get(NEW_GAMES_ROUTE_GAME_ID_KEY);
                if (!StringUtils.isTrimmedEmpty(configured)) {
                    configuredGameId = configured;
                }
            }
        } catch (Exception e) {
            getLogger().warn("Unable resolve bank New Games route id, using default", e);
        }

        if (configuredGameId.equals(candidateGameId)) {
            return true;
        }
        try {
            return Long.parseLong(configuredGameId) == Long.parseLong(candidateGameId);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected String getGameIdParamName() {
        return "gameId";
    }

    protected Predicate<HttpServletRequest> validateGameMode(ActionErrors errors) {
        return request -> {
            this.mode = BaseAction.extractRequestParameterIgnoreCase(request, "mode");
            if (StringUtils.isTrimmedEmpty(mode)) {
                getLogger().warn("empty_credentials, ''mode' parameter not found");
                errors.add("invalid_mode", new ActionMessage("error.deposit.modeNotDefined"));
                return false;
            }
            gameMode = GameMode.getByName(mode);
            if (gameMode == null) {
                errors.add("invalid_mode", new ActionMessage("error.deposit.modeNotDefined"));
                return false;
            }
            return true;
        };
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
        sb.append("CWStartGameForm");
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
