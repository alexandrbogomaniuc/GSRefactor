package com.abs.casino.web.bonus;

import com.abs.casino.common.cache.BankInfoCache;
import com.abs.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.exception.BonusException;
import com.abs.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.xml.xmlwriter.XmlWriter;
import com.abs.casino.common.web.bonus.BonusError;
import com.abs.casino.common.web.bonus.BonusErrors;
import com.abs.casino.common.web.bonus.CBonus;
import com.abs.casino.gs.managers.payment.bonus.BonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: ktd
 * Date: 31.03.11
 */
public class CheckBonusServlet extends AbstractBonusServlet {
    private static final Logger LOG = LogManager.getLogger(CheckBonusServlet.class);

    @Override
    protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        XmlWriter xw = new XmlWriter(response.getWriter());
        HashMap inParams = new HashMap();
        HashMap outParams = new HashMap();
        try {
            inParams.put(BANK_ID_PARAM, request.getParameter(BANK_ID_PARAM));
            inParams.put(CBonus.PARAM_EXTBONUSID, request.getParameter(CBonus.PARAM_EXTBONUSID));
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

            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            String extBankId = String.valueOf(bankInfo.getExternalBankId());
            inParams.put(BANK_ID_PARAM, extBankId);

            String extBonusId = request.getParameter(CBonus.PARAM_EXTBONUSID);
            if (StringUtils.isTrimmedEmpty(extBonusId)) {
                throw new BonusException(BonusErrors.INVALID_BONUS_ID);
            }

            String hash = request.getParameter(CBonus.PARAM_HASH);
            if (StringUtils.isTrimmedEmpty(hash)) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }

            Bonus bonus = BonusManager.getInstance().get(bankId, extBonusId);
            if (bonus == null) {
                throw new BonusException("bonus is not found", BonusErrors.OPERATION_NOT_FOUND);
            }
            boolean isHashValueEnabled = bankInfo.isHashValueEnable();

            if (isHashValueEnabled) {

                List<String> paramList = new ArrayList<String>();
                paramList.add(extBonusId);
                paramList.add(sBankId);

                if (!hash.equals(getHashValue(paramList, bankId))) {
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
            }
            outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
            outParams.put(CBonus.PARAM_BONUSID, bonus.getId());
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
}
