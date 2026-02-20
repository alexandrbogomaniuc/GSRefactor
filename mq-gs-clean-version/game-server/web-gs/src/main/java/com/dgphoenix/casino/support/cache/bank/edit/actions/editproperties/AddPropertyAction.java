package com.dgphoenix.casino.support.cache.bank.edit.actions.editproperties;


import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper;
import com.dgphoenix.casino.support.cache.bank.edit.forms.editproperties.BankPropertiesForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AddPropertyAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {

        BankPropertiesForm bankForm = (BankPropertiesForm) form;
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(Long.parseLong(bankForm.getBankId()));
        bankInfo.setProperty(bankForm.getNewKey().trim(), bankForm.getNewValue().trim());

        RemoteCallHelper.getInstance().saveAndSendNotification(bankInfo);
        return mapping.findForward("success");
    }
}
