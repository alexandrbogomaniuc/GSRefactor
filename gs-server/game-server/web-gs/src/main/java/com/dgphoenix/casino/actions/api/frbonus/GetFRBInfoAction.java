package com.dgphoenix.casino.actions.api.frbonus;


import com.abs.casino.actions.api.frbonus.GetFRBInfoForm;
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
import java.util.*;

public class GetFRBInfoAction extends AbstractBonusAction<GetFRBInfoForm> {
    private static final Logger LOG = LogManager.getLogger(GetFRBInfoAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, GetFRBInfoForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        Map<String, String> inParams = new HashMap<>();
        Map<String, Object> outParams = new HashMap<>();
        try {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(form.getBankId());
            inParams.put(BANK_ID_PARAM, String.valueOf(bankInfo.getExternalBankId()));
            form.setSendDetailsOnFrbInfo(bankInfo.isSendDetailsOnFrbInfo());
            form.setSendBonusAwardTime(bankInfo.isSendBonusAwardTime());
            String userId = form.getUserId();
            if (userId != null) {
                inParams.put(CBonus.PARAM_USERID, userId);
            }
            Long bonusId = form.getBonusId();
            if (bonusId != null) {
                inParams.put(CBonus.PARAM_BONUSID, bonusId.toString());
            }
            if (StringUtils.isTrimmedEmpty(form.getHash())) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }
            inParams.put(CBonus.PARAM_HASH, form.getHash());
            if (bankInfo.isHashValueEnable()) {
                List<String> paramList = new ArrayList<>();
                if (userId != null) {
                    paramList.add(form.getUserId());
                }
                if (bonusId != null) {
                    paramList.add(bonusId.toString());
                }
                paramList.add(String.valueOf(bankInfo.getExternalBankId()));
                if (!form.getHash().equals(getHashValue(paramList, form.getBankId()))) {
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
            }
            if (bonusId == null) {
                String extUserId = form.getUserId().trim();
                SessionHelper.getInstance().lock(form.getBankId(), form.getUserId());
                try {
                    SessionHelper.getInstance().openSession();
                    AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(form.getSubCasinoId(),
                            form.getBankId(), extUserId);
                    if (accountInfo == null) {
                        LOG.warn("AccountInfo not found, bankId={}, subCasinoId={}, extUserId={}",form.getBankId(), form.getSubCasinoId(), extUserId);
                        throw new BonusException(BonusErrors.USER_NOT_FOUND);
                    } else {
                        LOG.debug("before check checkMassAwardsForAccount frbMassAwardIdsList: {} for account={}",
                                accountInfo.getFrbMassAwardIds(), accountInfo.getId());
                    }
                    FRBonusManager.getInstance().checkMassAwardsForAccount(accountInfo);
                    LOG.debug("after check checkMassAwardsForAccount frbMassAwardIdsList: {} for account={}",
                            accountInfo.getFrbMassAwardIds(), accountInfo.getId());

                    List<FRBonus> bonuses = FRBonusManager.getInstance().getActiveBonuses(accountInfo);
                    LOG.debug("frbBonus.size: {} for account={}", bonuses.size(), accountInfo.getId());

                    outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                    outParams.put(CBonus.BONUS_LIST, bonuses);
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                } finally {
                    SessionHelper.getInstance().clearWithUnlock();
                }
            } else {
                FRBonus frBonus = FRBonusManager.getInstance().getById(bonusId);
                if (frBonus == null) {
                    frBonus = FRBonusManager.getInstance().getArchivedFRBonusById(bonusId);
                }
                if (frBonus == null) {
                    LOG.warn("FRBonus not found, bankId={}, subCasinoId={}, frBonusId={}", form.getBankId(), form.getSubCasinoId(), bonusId);
                    throw new BonusException(BonusErrors.BONUS_NOT_FOUND);
                }
                outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                outParams.put(CBonus.BONUS_LIST, Collections.singletonList(frBonus));
            }
        } catch (BonusException e) {
            if (BonusErrors.USER_NOT_FOUND.equals(e.getBonusError())) {
                //LOG.warn("AccountInfo not found");
            } else {
                LOG.error(e.getMessage(), e);
            }
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
    protected Logger getLogger() {
        return LOG;
    }
}
