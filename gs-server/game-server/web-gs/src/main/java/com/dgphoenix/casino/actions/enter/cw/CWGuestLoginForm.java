package com.abs.casino.actions.enter.cw;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.dgphoenix.casino.actions.enter.game.IStartGameForm;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.common.web.MobileDetector;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: plastical
 * Date: 23.04.2010
 */
public class CWGuestLoginForm extends CommonActionForm implements IStartGameForm {
    private final static Logger LOG = LogManager.getLogger(CWGuestLoginForm.class);

    protected String gameId;
    protected String lang;
    private String profileId;
    private String currencyCode;
    private Currency currency;

    public CWGuestLoginForm() {
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public boolean isLaunchChecked() {
        return true;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public GameMode getGameMode() {
        return GameMode.FREE;
    }

    @Override
    public String getMode() {
        return GameMode.FREE.name();
    }

    @Override
    public boolean isNotGameFRB() {
        return false;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);
        if (!actionErrors.isEmpty()) {
            return actionErrors;
        }

        try {
            String strGameId = BaseAction.extractRequestParameterIgnoreCase(request, getGameIdParamName());
            if (StringUtils.isTrimmedEmpty(strGameId)) {
                actionErrors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
                return actionErrors;
            }
            //prevent loading by extGameId for 7red
            Long originalId = subCasinoId == 22 ? null :
                    BaseGameCache.getInstance().getOriginalGameId(strGameId, getBankId());
            if (originalId == null) {
                this.gameId = strGameId;
            } else {
                this.gameId = Long.toString(originalId);
            }

            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(getBankId(), Long.parseLong(gameId), null);
            if (gameInfo == null) {
                actionErrors.add("empty_credentials", new ActionMessage("error.login.gameInfoNotDefined"));
                return actionErrors;
            }
            if (currencyCode != null && !StringUtils.isTrimmedEmpty(currencyCode)) {
                Currency localCurrency = CurrencyCache.getInstance().get(currencyCode);
                if (localCurrency == null) {
                    actionErrors.add("wrong_currency", new ActionMessage("error.login.currencyCodeValidationError"));
                    return actionErrors;
                }
                currency = super.bankInfo.isCurrencyExist(localCurrency) ? localCurrency : null;
            }
            gameInfo = MobileDetector.checkGameInfo(gameInfo, getClientType(), request.getHeader("User-Agent"));
            this.gameId = Long.toString(gameInfo.getId());
            if (!gameInfo.isBankEnabled()) {
                actionErrors.add("empty_credentials", new ActionMessage("error.login.bankIsNotEnabled"));
                return actionErrors;
            }
            if (!gameInfo.isEnabled()) {
                actionErrors.add("empty_credentials", new ActionMessage("error.login.gameIsNotEnabled"));
                return actionErrors;
            }
        } catch (Exception e) {
            getLogger().error("validate error:", e);
            actionErrors.add("empty_credentials", new ActionMessage("error.login.gameIdNotDefined"));
            return actionErrors;
        }

        return actionErrors;
    }

    protected String getGameIdParamName() {
        return "gameId";
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        gameId = null;
    }
}
