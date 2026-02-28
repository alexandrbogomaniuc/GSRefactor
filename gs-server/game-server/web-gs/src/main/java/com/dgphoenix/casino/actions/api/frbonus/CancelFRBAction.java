package com.abs.casino.actions.api.frbonus;


import com.abs.casino.actions.api.bonus.AbstractBonusAction;
import com.abs.casino.actions.api.frbonus.CancelFRBForm;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BonusStatus;
import com.dgphoenix.casino.common.cache.data.bonus.FRBonus;
import com.abs.casino.common.exception.BonusException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.xml.xmlwriter.XmlWriter;
import com.abs.casino.common.web.bonus.BonusError;
import com.abs.casino.common.web.bonus.BonusErrors;
import com.abs.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager;
import com.abs.casino.gs.managers.payment.bonus.IFRBonusManager;
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

public class CancelFRBAction extends AbstractBonusAction<CancelFRBForm> {
    private static final Logger LOG = LogManager.getLogger(CancelFRBAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, CancelFRBForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        XmlWriter xw = new XmlWriter(response.getWriter());
        Map<String, String> inParams = new HashMap<>();
        Map<String, Object> outParams = new HashMap<>();

        LOG.info("form: {}", form);
        try {
            if (StringUtils.isTrimmedEmpty(form.getBonusId())) {
                throw new BonusException(BonusErrors.INVALID_BONUS_ID);
            }
            if (StringUtils.isTrimmedEmpty(form.getHash())) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }
            inParams.put(CBonus.PARAM_BONUSID, form.getBonusId());
            inParams.put(CBonus.PARAM_HASH, form.getHash());
            Long bonusId = Long.parseLong(form.getBonusId());
            FRBonus bonus = FRBonusManager.getInstance().getById(bonusId);
            if (bonus == null) {
                bonus = FRBonusManager.getInstance().getArchivedFRBonusById(bonusId);
                if (bonus == null) {
                    throw new BonusException(BonusErrors.BONUS_NOT_FOUND);
                }
            }
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bonus.getBankId());
            form.setSendDetailsOnFrbInfo(bankInfo.isSendDetailsOnFrbInfo());

            if (bankInfo.isHashValueEnable()) {
                List<String> paramList = new ArrayList<>();
                paramList.add(form.getBonusId());
                if (!form.getHash().equals(getHashValue(paramList, bonus.getBankId()))) {
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
            }

            IFRBonusManager bonusManager = FRBonusManager.getInstance();
            if (bonus.getStatus() != BonusStatus.ACTIVE || bonusManager.cancelBonus(bonus)) {
                outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                outParams.put(CBonus.PARAM_BONUSID, String.valueOf(bonus.getId()));
            } else {
                fillErrorParams(outParams, null, BonusErrors.OPERATION_NOT_FOUND);
            }
        } catch (BonusException e) {
            fillErrorParams(outParams, e, e.getBonusError());
        } catch (Exception e) {
            fillErrorParams(outParams, e, null);
        }
        buildResponseXML(xw, inParams, outParams, form);
        response.getWriter().flush();
        return null;
    }

    protected void fillErrorParams(Map<String, Object> outParams, Exception e, BonusError bonusError) {
        if (e != null) {
            LOG.warn(e.getMessage(), e);
        }
        outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
        outParams.put(CBonus.CODE_TAG,
                (bonusError != null) ? bonusError.getCode() : BonusErrors.INTERNAL_ERROR.getCode());
        outParams.put(CBonus.DESCRIPTION_TAG,
                (bonusError != null) ? bonusError.getDescription() : BonusErrors.INTERNAL_ERROR.getDescription());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
