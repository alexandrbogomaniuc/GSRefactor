package com.dgphoenix.casino.web.bonus;

import com.dgphoenix.casino.account.AccountManager;
import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.account.AccountInfo;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: ktd
 * Date: 31.03.11
 */
public class GetBonusHistoryServlet extends AbstractBonusServlet {
    private static final Logger LOG = LogManager.getLogger(GetBonusHistoryServlet.class);
    private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        XmlWriter xw = new XmlWriter(response.getWriter());
        HashMap inParams = new HashMap();
        HashMap outParams = new HashMap();
        try {
            inParams.put(BANK_ID_PARAM, request.getParameter(BANK_ID_PARAM));
            inParams.put(CBonus.PARAM_USERID, request.getParameter(CBonus.PARAM_USERID));
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

            String extUserId = request.getParameter(CBonus.PARAM_USERID);
            if (StringUtils.isTrimmedEmpty(extUserId)) {
                throw new BonusException("UserId parameter not found, bankId=" + bankId +
                        ", subCasinoId=" + subCasinoId, BonusErrors.USER_NOT_FOUND);
            }

            String hash = request.getParameter(CBonus.PARAM_HASH);
            if (StringUtils.isTrimmedEmpty(hash)) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }

            boolean isHashValueEnabled = BankInfoCache.getInstance().getBankInfo(bankId).isHashValueEnable();

            if (isHashValueEnabled) {
                List<String> paramList = new ArrayList();
                paramList.add(extUserId);
                paramList.add(sBankId);

                if (!hash.equals(getHashValue(paramList, bankId))) {
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
            }
            SessionHelper.getInstance().lock(bankId.intValue(), extUserId);
            try {
                SessionHelper.getInstance().openSession();
                AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(subCasinoId.shortValue(),
                        bankId.intValue(), extUserId);
                if (accountInfo == null) {
                    throw new BonusException("AccountInfo not found, bankId=" + bankId +
                            ", subCasinoId=" + subCasinoId + ", extUserId=" + extUserId, BonusErrors.USER_NOT_FOUND);
                }
                List<Bonus> bonuses = BonusManager.getInstance().getFinishedBonuses(accountInfo);
                outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                outParams.put(CBonus.BONUS_LIST, bonuses);
                SessionHelper.getInstance().commitTransaction();
                SessionHelper.getInstance().markTransactionCompleted();
            } finally {
                SessionHelper.getInstance().clearWithUnlock();
            }
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
        buildResponseXML(xw, inParams, outParams);

    }

    protected void printBonusInfo(XmlWriter xw, Bonus bonus) throws XmlWriterException {
        xw.node(CBonus.BONUSID, Long.toString(bonus.getId()));
        xw.node(CBonus.STATUS, bonus.getStatus().name());
        xw.node(CBonus.TYPE, bonus.getType().toString());
        xw.node(CBonus.AWARDDATE, DF.format(Instant.ofEpochMilli(bonus.getTimeAwarded()).atZone(ZoneId.systemDefault())));
        xw.node(CBonus.AMOUNT, Long.toString(bonus.getAmount()));
        xw.node(CBonus.BALANCE, Long.toString(bonus.getAmount()));
        xw.node(CBonus.ROLLOVER, Long.toString((long) (bonus.getRolloverMultiplier() * bonus.getAmount())));
        xw.node(CBonus.COLLECTED, Long.toString(bonus.getBetSum()));
        xw.node(CBonus.DESCRIPTION, bonus.getDescription());
        xw.node(CBonus.GAMEIDS, getGameIds(bonus));
        xw.node(CBonus.ENDDATE, DF.format(Instant.ofEpochMilli(bonus.getEndTime()).atZone(ZoneId.systemDefault())));
    }
}
