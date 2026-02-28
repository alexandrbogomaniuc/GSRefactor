package com.abs.casino.web.bonus;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.xml.xmlwriter.XmlWriter;
import com.abs.casino.common.web.bonus.BonusError;
import com.abs.casino.common.web.bonus.BonusErrors;
import com.abs.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.abs.casino.gs.managers.payment.bonus.IBonusManager;
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
public class CancelBonusServlet extends AbstractBonusServlet {
    private static final Logger LOG = LogManager.getLogger(CancelBonusServlet.class);

    @Override
    protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        XmlWriter xw = new XmlWriter(response.getWriter());
        HashMap inParams = new HashMap();
        HashMap outParams = new HashMap();
        try {
            inParams.put(CBonus.PARAM_BONUSID, request.getParameter(CBonus.PARAM_BONUSID));
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

            String bonusId = request.getParameter(CBonus.PARAM_BONUSID);
            if (StringUtils.isTrimmedEmpty(bonusId)) {
                throw new BonusException(BonusErrors.INVALID_BONUS_ID);
            }

            String hash = request.getParameter(CBonus.PARAM_HASH);
            if (StringUtils.isTrimmedEmpty(hash)) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }

            Bonus bonus = BonusManager.getInstance().getById(Long.parseLong(bonusId));
            if (bonus == null) {
                throw new BonusException("bonus is not found", BonusErrors.OPERATION_NOT_FOUND);
            }
            if (bonus.getBankId() != bankId) {
                throw new BonusException(BonusErrors.INVALID_PARAMETERS);
            }
            boolean isHashValueEnabled = BankInfoCache.getInstance().getBankInfo(bankId).
                    isHashValueEnable();
            if (isHashValueEnabled) {
                List<String> paramList = new ArrayList();
                paramList.add(bonusId);
                if (!hash.equals(getHashValue(paramList, bankId))) {
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
            }

            try {
                IBonusManager bonusManager = BonusManager.getInstance();
                if (bonusManager.cancelBonus(bonus)) {
                    outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                    outParams.put(CBonus.PARAM_BONUSID, bonusId);
                } else {
                    outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
                    outParams.put(CBonus.CODE_TAG, BonusErrors.OPERATION_NOT_FOUND.getCode());
                    outParams.put(CBonus.DESCRIPTION_TAG, BonusErrors.OPERATION_NOT_FOUND.getDescription());
                }

            } catch (BonusException e) {
                LOG.error(e.getMessage(), e);
                outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
                outParams.put(CBonus.CODE_TAG, BonusErrors.INTERNAL_ERROR.getCode());
                outParams.put(CBonus.DESCRIPTION_TAG, BonusErrors.INTERNAL_ERROR.getDescription());
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
}
