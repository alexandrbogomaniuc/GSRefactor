package com.abs.casino.support.cache.bank.edit.actions.editproperties;


import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.abs.casino.support.cache.bank.edit.forms.editproperties.BankPropertiesForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class SelectModeAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        BankPropertiesForm bankPropForm = (BankPropertiesForm) form;
        long bankId = Long.parseLong(bankPropForm.getBankId());

        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);

        Map<String, String> bankProperties = bankInfo.getProperties();
        bankPropForm.setBankProperties(bankProperties);

        bankPropForm.setExternalBankId(bankInfo.getExternalBankId());
        bankPropForm.setExternalBankIdDescription(bankInfo.getExternalBankIdDescription());
        bankPropForm.setDefaultCurrencyCode(bankInfo.getDefaultCurrency().getCode());
        bankPropForm.setLimitId(Long.toString(bankInfo.getLimit().getId()));
        bankPropForm.setDefaultLanguage(bankInfo.getDefaultLanguage());
        bankPropForm.setFreeGameOverRedirectUrl(bankInfo.getFreeGameOverRedirectUrl());
        bankPropForm.setCashierUrl(bankInfo.getCashierUrl());
        bankPropForm.setAllowedRefererDomains(bankInfo.getAllowedRefererDomains());
        bankPropForm.setForbiddenRefererDomains(bankInfo.getForbiddenRefererDomains());

        return mapping.findForward("success");
    }
}
