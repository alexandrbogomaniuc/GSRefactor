package com.abs.casino.actions.game.battleground;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.abs.casino.actions.enter.game.IStartGameForm;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.common.web.ClientTypeFactory;
import com.dgphoenix.casino.common.web.MobileDetector;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

public class BattlegroundStartGameFromMQBForm extends CommonActionForm implements IStartGameForm {
    private static final Logger LOG = LogManager.getLogger(BattlegroundStartGameFromMQBForm.class);
    public static final String EMPTY_CREDENTIALS = "empty_credentials";

    private String token;
    private String gameId;
    private String homeUrl;
    private String lang;
    private Long buyIn;
    private String prefRoomId;


    @Override
    protected Logger getLogger() {
        return LOG;
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

    @Override
    public String getLang() {
        return lang;
    }

    @Override
    public boolean isNotGameFRB() {
        return true;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Long getBuyIn() {
        return buyIn;
    }

    public void setBuyIn(Long buyIn) {
        this.buyIn = buyIn;
    }

    public String getPrefRoomId() {
        return prefRoomId;
    }

    public void setPrefRoomId(String prefRoomId) {
        this.prefRoomId = prefRoomId;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        this.token = BaseAction.extractRequestParameterIgnoreCase(request, "token");
        try {
            String strGameId = BaseAction.extractRequestParameterIgnoreCase(request, "gameId");
            if (StringUtils.isTrimmedEmpty(strGameId)) {
                errors.add(EMPTY_CREDENTIALS, new ActionMessage("error.login.gameIdNotDefined"));
                return errors;
            }

            this.gameId = strGameId;

            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(getBankId(), Long.parseLong(gameId), null);
            if (gameInfo == null) {
                errors.add(EMPTY_CREDENTIALS, new ActionMessage("error.login.gameInfoNotDefined"));
                return errors;
            }
            gameInfo = MobileDetector.checkGameInfo(gameInfo, getClientType(), request.getHeader("User-Agent"));
            this.gameId = Long.toString(gameInfo.getId());

            String strBankId = BaseAction.extractRequestParameterIgnoreCase(request, "bankId");
            if (StringUtils.isTrimmedEmpty(strBankId)) {
                errors.add(EMPTY_CREDENTIALS, new ActionMessage("error.login.incorrectBank"));
                return errors;
            }

            this.bankId = Integer.parseInt(strBankId);
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo == null) {
                errors.add(EMPTY_CREDENTIALS, new ActionMessage("error.login.incorrectBank"));
                return errors;
            }

            if (!gameInfo.isBankEnabled()) {
                errors.add(EMPTY_CREDENTIALS, new ActionMessage("error.login.bankIsNotEnabled"));
                return errors;
            }
            if (!BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameInfo.getId()).
                    isMultiplayerGame()) {
                errors.add(EMPTY_CREDENTIALS, new ActionMessage("error.login.notMultiplayerGame"));
                return errors;
            }
            setBankInfo(bankInfo);

            this.homeUrl = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.PARAM_HOME_URL);
            this.lang = BaseAction.extractRequestParameterIgnoreCase(request, BaseAction.LANG_ID_ATTRIBUTE);
            this.clientType = ClientTypeFactory.getByHttpRequest(request);
        } catch (Exception e) {
            getLogger().error("BattlegroundStartGameFromMQBForm::validate error:", e);
            errors.add(EMPTY_CREDENTIALS, new ActionMessage("error.login.gameIdNotDefined"));
            return errors;
        }

        return errors;
    }

    @Override
    public String toString() {
        return "BattlegroundStartGameFromMQBForm{" +
                "bankId=" + bankId +
                ", subCasinoId=" + subCasinoId +
                ", clientType=" + clientType +
                ", token='" + token + '\'' +
                ", gameId='" + gameId + '\'' +
                ", buyIn='" + buyIn + '\'' +
                ", prefRoomId='" + prefRoomId + '\'' +
                ", lang='" + lang + '\'' +
                '}';
    }
}
