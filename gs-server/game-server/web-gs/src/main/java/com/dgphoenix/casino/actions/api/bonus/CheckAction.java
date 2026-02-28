package com.abs.casino.actions.api.bonus;

import com.abs.casino.actions.api.bonus.CheckForm;
import com.dgphoenix.casino.actions.api.bonus.AbstractBonusAction;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.dgphoenix.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter;
import com.dgphoenix.casino.common.web.bonus.BonusError;
import com.abs.casino.common.web.bonus.BonusErrors;
import com.abs.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.dgphoenix.casino.gs.managers.payment.bonus.IBonusManager;
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

/**
 * User: ktd
 * Date: 01.04.11
 */
public class CheckAction extends AbstractBonusAction<CheckForm> {
    private static final Logger LOG = LogManager.getLogger(CheckAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, CheckForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        XmlWriter xw = new XmlWriter(response.getWriter());
        Map<String, String> inParams = new HashMap();
        Map<String, Object> outParams = new HashMap();
        try {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(form.getBankId());
            inParams.put(BANK_ID_PARAM, String.valueOf(form.getBankId()));
            if (StringUtils.isTrimmedEmpty(form.getExtBonusId())) {
                throw new BonusException(BonusErrors.INVALID_BONUS_ID);
            }
            inParams.put(CBonus.PARAM_EXTBONUSID, form.getExtBonusId());
            if (StringUtils.isTrimmedEmpty(form.getHash())) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }
            inParams.put(CBonus.PARAM_HASH, form.getHash());

            IBonusManager bonusManager = BonusManager.getInstance();
            Bonus bonus = bonusManager.get(form.getBankId(), form.getExtBonusId());
            if (bonus == null) {
                throw new BonusException(BonusErrors.OPERATION_NOT_FOUND);
            }
            if (bankInfo.isHashValueEnable()) {
                List<String> paramList = new ArrayList();
                paramList.add(form.getExtBonusId());
                paramList.add(String.valueOf(bankInfo.getExternalBankId()));
                if (!form.getHash().equals(getHashValue(paramList, form.getBankId()))) {
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

        buildResponseXML(xw, inParams, outParams, form);
        response.getWriter().flush();
        return null;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
