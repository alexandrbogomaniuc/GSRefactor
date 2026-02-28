package com.abs.casino.web.bonus;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import com.dgphoenix.casino.common.web.bonus.BonusError;
import com.abs.casino.common.web.bonus.BonusErrors;
import com.abs.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * User: ktd
 * Date: 31.03.11
 */
public class GetBonusInfoServlet extends AbstractBonusServlet {
    private static final Logger LOG = LogManager.getLogger(GetBonusInfoServlet.class);

    @Override
    protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        XmlWriter xw = new XmlWriter(response.getWriter());
        HashMap<String, Object> inParams = new HashMap<>();
        HashMap<String, Object> outParams = new HashMap<>();
        try {
            String userId = request.getParameter(CBonus.PARAM_USERID);
            if (!StringUtils.isTrimmedEmpty(userId)) {
                inParams.put(CBonus.PARAM_USERID, userId);
            }
            String bonusId = request.getParameter(CBonus.PARAM_BONUSID);
            if (!StringUtils.isTrimmedEmpty(bonusId)) {
                inParams.put(CBonus.PARAM_BONUSID, bonusId);
            }
            inParams.put(CBonus.PARAM_HASH, request.getParameter(CBonus.PARAM_HASH));

            String sBankId = request.getParameter(BANK_ID_PARAM);
            if (StringUtils.isTrimmedEmpty(sBankId)) {
                throw new BonusException(BonusErrors.INVALID_PARAMETERS);
            }
            Long bankId;
            try {
                bankId = Long.parseLong(sBankId);
            } catch (NumberFormatException e) {
                throw new BonusException(e, BonusErrors.INVALID_PARAMETERS);
            }

            String extBankId = String.valueOf(BankInfoCache.getInstance().getBankInfo(bankId).getExternalBankId());
            inParams.put(BANK_ID_PARAM, extBankId);

            String sSubCasino = request.getParameter(SUBCASINO_ID_PARAM);
            Long subCasinoId = Long.parseLong(sSubCasino);

            String hash = request.getParameter(CBonus.PARAM_HASH);
            if (StringUtils.isTrimmedEmpty(hash)) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }

            boolean isHashValueEnabled = BankInfoCache.getInstance().getBankInfo(bankId).isHashValueEnable();

            if (isHashValueEnabled) {
                List<String> paramList = new ArrayList<>();
                if (!StringUtils.isTrimmedEmpty(userId)) {
                    paramList.add(userId.trim());
                }
                if (!StringUtils.isTrimmedEmpty(bonusId)) {
                    paramList.add(bonusId);
                }
                if (!hash.equals(getHashValue(paramList, bankId))) {
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
            }
            if (StringUtils.isTrimmedEmpty(bonusId)) {
                if (StringUtils.isTrimmedEmpty(userId)) {
                    throw new BonusException("UserId parameter not found, bankId=" + bankId +
                            ", subCasinoId=" + subCasinoId, BonusErrors.USER_NOT_FOUND);
                }
                String extUserId = userId.trim();
                SessionHelper.getInstance().lock(bankId.intValue(), extUserId);
                try {
                    SessionHelper.getInstance().openSession();
                    AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(subCasinoId.shortValue(),
                            bankId.intValue(), extUserId);
                    if (accountInfo == null) {
                        throw new BonusException("AccountInfo not found, bankId=" + bankId +
                                ", subCasinoId=" + subCasinoId + ", extUserId=" + extUserId, BonusErrors.USER_NOT_FOUND);
                    }
                    BonusManager.getInstance().checkMassAwardsForAccount(accountInfo);
                    List<Bonus> bonuses = BonusManager.getInstance().getActiveBonuses(accountInfo);
                    outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                    outParams.put(CBonus.BONUS_LIST, bonuses);
                    SessionHelper.getInstance().commitTransaction();
                    SessionHelper.getInstance().markTransactionCompleted();
                } finally {
                    SessionHelper.getInstance().clearWithUnlock();
                }
            } else {
                LOG.debug("process: bonusId=" + bonusId + ", hash=" + hash);
                Bonus bonus = BonusManager.getInstance().getById(Long.valueOf(bonusId));
                if (bonus == null) {
                    LOG.warn("Bonus not found, bankId=" + bankId +
                            ", subCasinoId=" + subCasinoId + ", bonusId=" + bonusId);
                    throw new BonusException(BonusErrors.BONUS_NOT_FOUND);
                }
                outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                outParams.put(CBonus.BONUS_LIST, Collections.singletonList(bonus));
            }
        } catch (BonusException e) {
            BonusError bonusError = e.getBonusError();
            //prevent spam stacktrace to logs
            if (bonusError == BonusErrors.USER_NOT_FOUND) {
                LOG.warn(e.getMessage());
            } else {
                LOG.error(e.getMessage(), e);
            }
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
            outParams.put(CBonus.CODE_TAG, (bonusError != null) ? bonusError.getCode() : BonusErrors.INTERNAL_ERROR.getCode());
            outParams.put(CBonus.DESCRIPTION_TAG,
                    (bonusError != null) ? bonusError.getDescription() : BonusErrors.INTERNAL_ERROR.getDescription());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
            outParams.put(CBonus.CODE_TAG, BonusErrors.INTERNAL_ERROR.getCode());
            outParams.put(CBonus.DESCRIPTION_TAG, BonusErrors.INTERNAL_ERROR.getDescription());
        }

        buildResponseXML(xw, inParams, outParams);

    }
}
