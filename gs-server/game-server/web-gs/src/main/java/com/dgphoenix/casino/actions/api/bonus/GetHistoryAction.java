package com.abs.casino.actions.api.bonus;

import com.abs.casino.actions.api.bonus.GetHistoryForm;
import com.dgphoenix.casino.actions.api.bonus.AbstractBonusAction;
import com.dgphoenix.casino.actions.api.bonus.BonusForm;
import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BaseBonus;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.exception.XmlWriterException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import com.dgphoenix.casino.common.web.bonus.BonusError;
import com.dgphoenix.casino.common.web.bonus.BonusErrors;
import com.dgphoenix.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * User: ktd
 * Date: 01.04.11
 */
public class GetHistoryAction extends AbstractBonusAction<GetHistoryForm> {
    private static final Logger LOG = LogManager.getLogger(GetHistoryAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, GetHistoryForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        Map<String, String> inParams = new HashMap();
        Map<String, Object> outParams = new HashMap();
        try {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(form.getBankId());
            inParams.put(BANK_ID_PARAM, String.valueOf(bankInfo.getExternalBankId()));
            inParams.put(CBonus.PARAM_USERID, form.getUserId());
            if (StringUtils.isTrimmedEmpty(form.getHash())) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }
            inParams.put(CBonus.PARAM_HASH, form.getHash());
            if (bankInfo.isHashValueEnable()) {
                List<String> paramList = new ArrayList();
                paramList.add(form.getUserId());
                paramList.add(String.valueOf(bankInfo.getExternalBankId()));
                if (!form.getHash().equals(getHashValue(paramList, form.getBankId()))) {
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
            }
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(form.getSubCasinoId(),
                    form.getBankId(), form.getUserId());
            if (accountInfo == null) {
                throw new BonusException(BonusErrors.USER_NOT_FOUND);
            }
            BonusManager.getInstance().getActiveBonuses(accountInfo); //need for check expired
            List<Bonus> bonuses = BonusManager.getInstance().getFinishedBonuses(accountInfo);
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
            outParams.put(CBonus.BONUS_LIST, bonuses);
        } catch (BonusException e) {
            LOG.error(e.getMessage(), e);
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
            BonusError bonusError = e.getBonusError();
            outParams.put(CBonus.CODE_TAG,
                    (bonusError != null) ? bonusError.getCode() : BonusErrors.INTERNAL_ERROR.getCode());
            outParams.put(CBonus.DESCRIPTION_TAG,
                    (bonusError != null) ? bonusError.getDescription() : BonusErrors.INTERNAL_ERROR.getDescription());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
            outParams.put(CBonus.CODE_TAG, BonusErrors.INTERNAL_ERROR.getCode());
            outParams.put(CBonus.DESCRIPTION_TAG, BonusErrors.INTERNAL_ERROR.getDescription());
        }
        if (form.isJson()) {
            buildResponseJSON(response, inParams, outParams, form);
        } else {
            XmlWriter xw = new XmlWriter(response.getWriter());
            buildResponseXML(xw, inParams, outParams, form);
        }
        response.getWriter().flush();
        return null;
    }

    @Override
    protected void printBonusInfo(XmlWriter xw, BaseBonus bonus, BonusForm form) throws XmlWriterException {
        super.printBonusInfo(xw, bonus, form);
        xw.node(CBonus.STATUS, bonus.getStatus().name());
        LocalDateTime endDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(bonus.getEndTime()),
                TimeZone.getDefault().toZoneId());
        xw.node(CBonus.ENDDATE, endDate.format(DATE_FORMATTER));
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
