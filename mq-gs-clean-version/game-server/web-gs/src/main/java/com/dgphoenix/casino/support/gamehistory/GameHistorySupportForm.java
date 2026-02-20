package com.dgphoenix.casino.support.gamehistory;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.language.LanguageType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.init.Initializer;
import com.dgphoenix.casino.web.history.GameHistoryListForm;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

/**
 * User: van0ss
 * Date: 01.04.2016
 * Form for using gamehistory without login.
 */
public class GameHistorySupportForm extends GameHistoryListForm {
    private static final Logger LOG = Logger.getLogger(GameHistorySupportForm.class);
    private Long accountId;
    private int itemsPerPage = 40; // More than default = 10

    public Long getAccountId() {
        return accountId;
    }

    @Override
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        LOG.debug("GameHistorySupportForm::validate start " + this.toString());

        try {
            if (LanguageType.toLanguageType(getLang()) == null) {
                LOG.error("Invalid language code:" + getLang());
                setLang("en");
            }

            if (getAccountId() == null) {
                LOG.error("GameHistorySupportForm::validate accountId: null gameId:" + getGameId());

                errors.add("history", new ActionMessage("error.differencerForm.empty", "accountId"));
                return errors;
            }

            if (getAccountId() < 0) {
                LOG.error("GameHistorySupportForm::validate accountId:" + getAccountId() + " gameId:" + getGameId());

                errors.add("history", new ActionMessage("error.differencerForm.incorrectProperty", "accountId", getAccountId()));
                return errors;
            }

            AccountInfo accountInfo;
            try {
                accountInfo = AccountManager.getInstance().getByAccountId(accountId);
                if (accountInfo == null) {
                    errors.add("history", new ActionMessage("error.history.accountNotFound"));
                    return errors;
                }
            } catch (CommonException e) {
                LOG.error("GameHistorySupportForm::validate accountId:" + getAccountId() + " gameId:" + getGameId()
                        + " error:", e);
                errors.add("history", new ActionMessage("error.history.accountNotFound"));
                return errors;
            }
            bankId = accountInfo.getBankId();

            if (getGameId() == null) {
                setGameId(ALL_GAMES);
                populate(request);
            } else {
                if (isPopulate(request)) {
                    populate(request);
                }

                if (getGameId() != ALL_GAMES) {
                    if (!BaseGameCache.getInstance().isExist(bankId, getGameId(), accountInfo.getCurrency())) {
                        LOG.error("GameHistorySupportForm::validate accountId:" + getAccountId() + " gameId:" + getGameId()
                                + " game doesn't exist");
                        errors.add("history", new ActionMessage("error.history.invalidGame"));
                        return errors;
                    }
                }
            }

            if (getStartYear() < Initializer.PROJECT_START_YEAR) {
                setStartYear(Initializer.PROJECT_START_YEAR);
                setStartMonth(1);
                setStartDay(1);
            }

            if (getEndYear() == 0) {
                setEndYear(Calendar.getInstance().get(Calendar.YEAR));
                setEndMonth(Calendar.getInstance().get(Calendar.MONTH) + 1);
                setEndDay(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
                setEndHour(23);
                setEndMinute(59);
                setEndSecond(59);
            }

            if (getMode() < 0 || getMode() > 3)
                setMode(0);            // all
        } catch (Exception e) {
            LOG.error("GameHistorySupportForm::validate accountId:" + getAccountId() + " gameId:" + getGameId()
                    + " error:", e);
            errors.add("history", new ActionMessage("error.history.internalError"));
            return errors;
        }

        return errors;
    }

    @Override
    public String toString() {
        return "GameHistorySupportForm{" +
                "accountId=" + accountId +
                ", itemsPerPage=" + itemsPerPage +
                "} " + super.toString();
    }
}
