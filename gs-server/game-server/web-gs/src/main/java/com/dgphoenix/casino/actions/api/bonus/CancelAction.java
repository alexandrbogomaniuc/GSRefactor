package com.abs.casino.actions.api.bonus;

import com.abs.casino.actions.api.bonus.CancelForm;
import com.dgphoenix.casino.actions.api.bonus.AbstractBonusAction;
import com.dgphoenix.casino.account.AccountManager;
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
public class CancelAction extends AbstractBonusAction<CancelForm> {
    private static final Logger LOG = LogManager.getLogger(CancelAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, CancelForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        XmlWriter xw = new XmlWriter(response.getWriter());
        Map<String, String> inParams = new HashMap<>();
        Map<String, Object> outParams = new HashMap<>();
        try {
            if (StringUtils.isTrimmedEmpty(form.getBonusId())) {
                throw new BonusException(BonusErrors.INVALID_BONUS_ID);
            }
            if (StringUtils.isTrimmedEmpty(form.getHash())) {
                throw new BonusException(BonusErrors.INVALID_HASH);
            }
            inParams.put(CBonus.PARAM_BONUSID, form.getBonusId());
            inParams.put(CBonus.PARAM_HASH, form.getHash());
            Bonus bonus = BonusManager.getInstance().getById(Long.parseLong(form.getBonusId()));
            if (bonus == null) {
                throw new BonusException(BonusErrors.OPERATION_NOT_FOUND);
            }
            AccountInfo accountInfo = AccountManager.getInstance().getAccountInfo(bonus.getAccountId());
            boolean isHashValueEnabled =
                    BankInfoCache.getInstance().getBankInfo(accountInfo.getBankId()).isHashValueEnable();
            if (isHashValueEnabled) {
                List<String> paramList = new ArrayList<>();
                paramList.add(form.getBonusId());
                if (!form.getHash().equals(getHashValue(paramList, form.getBankId()))) {
                    throw new BonusException(BonusErrors.INVALID_HASH);
                }
            }
            try {
                IBonusManager bonusManager = BonusManager.getInstance();
                if (bonusManager.cancelBonus(bonus)) {
                    outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_OK);
                    outParams.put(CBonus.PARAM_BONUSID, String.valueOf(bonus.getId()));
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
        buildResponseXML(xw, inParams, outParams, form);
        response.getWriter().flush();
        return null;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
