package com.abs.casino.actions.api.frbonus;


import com.abs.casino.actions.api.frbonus.GetFRBInfoLiteForm;
import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.actions.api.bonus.AbstractBonusAction;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import com.dgphoenix.casino.common.web.bonus.BonusError;
import com.dgphoenix.casino.common.web.bonus.BonusErrors;
import com.dgphoenix.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetFRBInfoLiteAction extends AbstractBonusAction<GetFRBInfoLiteForm> {
    private static final Logger LOG = LogManager.getLogger(GetFRBInfoAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, GetFRBInfoLiteForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        XmlWriter xw = new XmlWriter(response.getWriter());
        Map<String, String> inParams = new HashMap<>();
        Map<String, Object> outParams = new HashMap<>();
        try {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(form.getBankId());
            form.setSendDetailsOnFrbInfo(bankInfo.isSendDetailsOnFrbInfo());
            inParams.put(BANK_ID_PARAM, String.valueOf(bankInfo.getExternalBankId()));
            inParams.put(CBonus.PARAM_USERID, form.getUserId());
            if (StringUtils.isTrimmedEmpty(form.getHash())) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }
            inParams.put(CBonus.PARAM_HASH, form.getHash());
            boolean isHashValueEnabled = bankInfo.isHashValueEnable();
            if (isHashValueEnabled) {
                List<String> paramList = new ArrayList<>();
                paramList.add(form.getUserId());
                paramList.add(String.valueOf(bankInfo.getExternalBankId()));
                if (!form.getHash().equals(getHashValue(paramList, form.getBankId()))) {
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
            }
            SessionHelper.getInstance().lock(form.getBankId(), form.getUserId());
            try {
                SessionHelper.getInstance().openSession();

                AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(form.getSubCasinoId(),
                        form.getBankId(), form.getUserId());
                if (accountInfo == null) {
                    throw new BonusException(BonusErrors.USER_NOT_FOUND);
                }
                FRBonusManager.getInstance().checkMassAwardsForAccount(accountInfo);
                List<FRBonus> bonuses = FRBonusManager.getInstance().getActiveBonuses(accountInfo);
                outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                outParams.put(CBonus.BONUS_LIST, bonuses);
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
        } catch (BonusException e) {
            BonusError bonusError = e.getBonusError();
            //prevent spam stacktrace to logs
            if (bonusError == BonusErrors.USER_NOT_FOUND) {
                LOG.error(e.getMessage());
            } else {
                LOG.error(e.getMessage(), e);
            }
            response.setContentType("text/xml");
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
            outParams.put(CBonus.CODE_TAG,
                    (bonusError != null) ? bonusError.getCode() : BonusErrors.INTERNAL_ERROR.getCode());
            outParams.put(CBonus.DESCRIPTION_TAG,
                    (bonusError != null) ? bonusError.getDescription() : BonusErrors.INTERNAL_ERROR.getDescription());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            response.setContentType("text/xml");
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
            outParams.put(CBonus.CODE_TAG, BonusErrors.INTERNAL_ERROR.getCode());
            outParams.put(CBonus.DESCRIPTION_TAG, BonusErrors.INTERNAL_ERROR.getDescription());
        }
        buildResponseXML(xw, inParams, outParams, form);
        response.getWriter().flush();
        return null;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
