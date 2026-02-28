package com.abs.casino.actions.api.bonus;

import com.abs.casino.actions.api.bonus.GetBonusForm;
import com.abs.casino.actions.api.bonus.AbstractBonusAction;
import com.abs.casino.actions.api.bonus.BonusForm;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.bonus.BaseBonus;
import com.dgphoenix.casino.common.cache.data.bonus.Bonus;
import com.abs.casino.common.exception.BonusException;
import com.abs.casino.common.exception.XmlWriterException;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.util.xml.xmlwriter.XmlWriter;
import com.abs.casino.common.web.bonus.BonusError;
import com.abs.casino.common.web.bonus.BonusErrors;
import com.abs.casino.common.web.bonus.CBonus;
import com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager;
import com.abs.casino.gs.managers.payment.bonus.IBonusManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/**
 * User: ktd
 * Date: 01.04.11
 */
public class GetBonusAction extends AbstractBonusAction<GetBonusForm> {
    private static final Logger LOG = LogManager.getLogger(GetBonusAction.class);

    @Override
    protected ActionForward process(ActionMapping mapping, GetBonusForm form, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        XmlWriter xw = new XmlWriter(response.getWriter());
        Map<String, String> inParams = new HashMap<>();
        Map<String, Object> outParams = new HashMap<>();
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
            List<Bonus> bonusList = BonusManager.getInstance().getArchivedBonusesByExtId(form.getBankId(), form.getExtBonusId());
            if (bonus != null) {
                bonusList.add(bonus);
            }
            if (bonusList.isEmpty()) {
                throw new BonusException(BonusErrors.BONUS_NOT_FOUND);
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
            outParams.put(CBonus.BONUS_LIST, bonusList);
        } catch (BonusException e) {
            BonusError bonusError = e.getBonusError();
            LOG.error("Error message: {}, bonusError={}", e.getMessage(), bonusError);
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

        buildResponseXML(xw, inParams, outParams, form);
        response.getWriter().flush();
        return null;
    }

    protected void printBonusInfo(XmlWriter xw, BaseBonus baseBonus, BonusForm form) throws XmlWriterException {
        super.printBonusInfo(xw, baseBonus, form);
        xw.node(CBonus.STATUS, baseBonus.getStatus().name());
        if (baseBonus.getEndTime() != null) {
            LocalDateTime endDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(baseBonus.getEndTime()),
                    TimeZone.getDefault().toZoneId());
            xw.node(CBonus.ENDDATE, endDate.format(DATE_FORMATTER));
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
