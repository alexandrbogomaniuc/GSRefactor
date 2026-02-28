package com.abs.casino.actions.game.tournament;

import com.abs.casino.actions.enter.CommonActionForm;
import com.abs.casino.actions.enter.game.IStartGameForm;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.common.web.ClientTypeFactory;
import com.abs.casino.common.web.MobileDetector;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: flsh
 * Date: 05.08.2020.
 */
public class TournamentStartGameForm extends CommonActionForm implements IStartGameForm {
    private static final Logger LOG = LogManager.getLogger(TournamentStartGameForm.class);

    private String token;
    private String sessionId;
    private String gameId;
    private long tournamentId;
    private String homeUrl;

    private String lang;

    public TournamentStartGameForm() {
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    public static Logger getLOG() {
        return LOG;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public GameMode getGameMode() {
        return GameMode.REAL;
    }

    @Override
    public String getMode() {
        return GameMode.REAL.name();
    }

    @Override
    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(long tournamentId) {
        this.tournamentId = tournamentId;
    }

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public boolean isNotGameFRB() {
        return true;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        this.token = BaseAction.extractRequestParameterIgnoreCase(request, "token");
        this.sessionId = BaseAction.extractRequestParameterIgnoreCase(request, "sessionId");
        if (StringUtils.isTrimmedEmpty(token) && StringUtils.isTrimmedEmpty(sessionId)) {
            LOG.error("token and sessionId is undefined, exit.");
            errors.add("empty_credentials", new ActionMessage("error.login.incorrectCredentials"));
            return errors;
        }

        try {
            String strGameId = BaseAction.extractRequestParameterIgnoreCase(request, "gameId");
            if (StringUtils.isTrimmedEmpty(strGameId)) {
                errors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
                return errors;
            }

            this.gameId = strGameId;

            IBaseGameInfo gameInfo = BaseGameCache.getInstance()
                    .getGameInfoById(getBankId(), Long.parseLong(gameId), null);
            if (gameInfo == null) {
                errors.add("empty_credentials", new ActionMessage("error.login.gameInfoNotDefined"));
                return errors;
            }
            gameInfo = MobileDetector.checkGameInfo(gameInfo, getClientType(), request.getHeader("User-Agent"));
            this.gameId = Long.toString(gameInfo.getId());

            String strBankId = BaseAction.extractRequestParameterIgnoreCase(request, "bankId");
            if (StringUtils.isTrimmedEmpty(strBankId)) {
                errors.add("empty_credentials", new ActionMessage("error.login.incorrectBank"));
                return errors;
            }

            this.bankId = Integer.parseInt(strBankId);
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo == null) {
                errors.add("empty_credentials", new ActionMessage("error.login.incorrectBank"));
                return errors;
            }

            if (!gameInfo.isBankEnabled()) {
                errors.add("empty_credentials", new ActionMessage("error.login.bankIsNotEnabled"));
                return errors;
            }
            if (!BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameInfo.getId()).
                    isMultiplayerGame()) {
                errors.add("empty_credentials", new ActionMessage("error.login.notMultiplayerGame"));
                return errors;
            }

            String sHomeUrl = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.PARAM_HOME_URL);
            if (StringUtils.isTrimmedEmpty(sHomeUrl)) {
                errors.add("empty_credentials", new ActionMessage("error.login.homeUrlEmpty"));
                return errors;
            }
            this.homeUrl = sHomeUrl;
            this.lang = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.LANG_ID_ATTRIBUTE);
            this.clientType = ClientTypeFactory.getByHttpRequest(request);
        } catch (Exception e) {
            getLogger().error("BSStartGameForm::validate error:", e);
            errors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
            return errors;
        }

        try {
            String strTournamentId = BaseAction.extractRequestParameterIgnoreCase(request, "tournamentId");
            if (StringUtils.isTrimmedEmpty(strTournamentId)) {
                errors.add("empty_credentials", new ActionMessage("error.login.tournamentIdNotDefined"));
                return errors;
            }

            this.tournamentId = Long.parseLong(strTournamentId);
        } catch (Exception e) {
            getLogger().error("TournamentStartGameForm::validate error:", e);
            errors.add("empty_credentials", new ActionMessage("error.login.tournamentIdNotDefined"));
            return errors;
        }
        return errors;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TournamentStartGameForm [");
        sb.append("token='").append(token).append('\'');
        sb.append(", sessionId='").append(sessionId).append('\'');
        sb.append(", gameId='").append(gameId).append('\'');
        sb.append(", tournamentId=").append(tournamentId);
        sb.append(", lang='").append(lang).append('\'');
        sb.append(", bankId=").append(bankId);
        sb.append(", subCasinoId=").append(subCasinoId);
        sb.append(", clientType=").append(clientType);
        sb.append(']');
        return sb.toString();
    }
}
