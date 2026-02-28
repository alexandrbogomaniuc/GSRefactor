package com.abs.casino.actions.api.bonus;

import com.abs.casino.actions.api.bonus.GetInfoForm;
import com.dgphoenix.casino.actions.api.bonus.AbstractBonusAction;
import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.xml.xmlwriter.XmlWriter;
import com.dgphoenix.casino.common.web.bonus.BonusError;
import com.abs.casino.common.web.bonus.BonusErrors;
import com.abs.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.common.web.statistics.StatisticsManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * User: ktd
 * Date: 01.04.11
 */
public class GetInfoAction extends AbstractBonusAction<GetInfoForm> {
    private static final Logger LOG = LogManager.getLogger(GetInfoAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, GetInfoForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        long now = System.currentTimeMillis();
        long startTime = System.currentTimeMillis();
        Map<String, String> inParams = new HashMap<>();
        Map<String, Object> outParams = new HashMap<>();
        try {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(form.getBankId());
            inParams.put(BANK_ID_PARAM, String.valueOf(bankInfo.getExternalBankId()));
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
                SessionHelper.getInstance().lock(form.getBankId(), form.getUserId());
                StatisticsManager.getInstance().updateRequestStatistics("api.bonus.GetInfoAction :process lock",
                        System.currentTimeMillis() - now);
                LOG.debug("api.bonus.GetInfoAction :process lock for hash=" + form.getHash() + ": " +
                        (System.currentTimeMillis() - now));
                now = System.currentTimeMillis();
                try {
                    SessionHelper.getInstance().openSession();
                    AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(form.getSubCasinoId(),
                            form.getBankId(), form.getUserId());
                    if (accountInfo == null) {
                        throw new BonusException(BonusErrors.USER_NOT_FOUND);
                    }
                    BonusManager.getInstance().checkMassAwardsForAccount(accountInfo);
                    StatisticsManager.getInstance().updateRequestStatistics("api.bonus.GetInfoAction :process " +
                            "checkMassAwardsForAccount", System.currentTimeMillis() - now);
                    LOG.debug("api.bonus.GetInfoAction :process checkMassAwardsForAccount for hash=" +
                            form.getHash() + ": " + (System.currentTimeMillis() - now));

                    now = System.currentTimeMillis();
                    List<Bonus> bonuses = BonusManager.getInstance().getActiveBonuses(accountInfo);
                    if (!CollectionUtils.isEmpty(bonuses)) {
                        bonuses = checkAndReplaceActualBonus(bonuses);
                    }
                    StatisticsManager.getInstance().updateRequestStatistics("api.bonus.GetInfoAction :process " +
                            "getActiveBonuses", System.currentTimeMillis() - now);
                    LOG.debug("api.bonus.GetInfoAction :process getActiveBonuses for hash=" +
                            form.getHash() + ": " + (System.currentTimeMillis() - now));
                    now = System.currentTimeMillis();

                    outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                    outParams.put(CBonus.BONUS_LIST, bonuses);
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                } finally {
                    SessionHelper.getInstance().clearWithUnlock();
                    StatisticsManager.getInstance().updateRequestStatistics("api.bonus.GetInfoAction :process " +
                            "finish", System.currentTimeMillis() - now);
                    LOG.debug("api.bonus.GetInfoAction :process finish for hash=" +
                            form.getHash() + ": " + (System.currentTimeMillis() - now));
                }
            } else {
                Bonus bonus = BonusManager.getInstance().getById(bonusId);
                if (bonus == null) {
                    bonus = BonusManager.getInstance().getArchivedBonusById(bonusId);
                }
                if (bonus == null) {
                    LOG.warn("Bonus not found, bankId=" + form.getBankId() +
                            ", subCasinoId=" + form.getSubCasinoId() + ", bonusId=" + bonusId);
                    throw new BonusException(BonusErrors.BONUS_NOT_FOUND);
                }
                outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                outParams.put(CBonus.BONUS_LIST, Collections.singletonList(bonus));
            }
        } catch (BonusException e) {
            BonusError bonusError = e.getBonusError();
            //prevent spam stacktrace to logs
            if (bonusError == BonusErrors.USER_NOT_FOUND) {
                LOG.error(e.getMessage());
            } else {
                LOG.error(e.getMessage(), e);
            }
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
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
        LOG.debug("total processing time for hash=" + form.getHash() + ": " + (System.currentTimeMillis() - startTime));
        return null;
    }

    private List<Bonus> checkAndReplaceActualBonus(List<Bonus> bonuses) {
        try {
            ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
            Bonus bonus = transactionData.getBonus();
            if (bonus != null) {
                int actualBonusIndex = findActualBonusIndex(bonuses, bonus.getId());
                if (actualBonusIndex >= 0) {
                    bonuses.set(actualBonusIndex, bonus);
                }
            }
        } catch (Exception e) {
            LOG.warn("Actual bonus was not replaced", e);
        }
        return bonuses;
    }

    private int findActualBonusIndex(List<Bonus> bonuses, long actualBonusId) {
        int index = -1;
        for (int i = 0; i < bonuses.size(); i++) {
            Bonus bonus = bonuses.get(i);
            if (bonus.getId() == actualBonusId) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
