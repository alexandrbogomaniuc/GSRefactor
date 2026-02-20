package com.dgphoenix.casino.support.cache.bank.edit.actions.editproperties;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.support.cache.bank.edit.forms.editproperties.BankPropertiesForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BankSupportAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        BankPropertiesForm allBanksForm = (BankPropertiesForm) form;

        List<LabelValueBean> banks = new ArrayList<LabelValueBean>();
        Map<Long, BankInfo> mapBanks = BankInfoCache.getInstance().getAllObjects();
        for (BankInfo bankInfo : mapBanks.values()) {
            banks.add(new LabelValueBean(bankInfo.getExternalBankIdDescription(), String.valueOf(bankInfo.getId())));
        }

        allBanksForm.setAllBanks(banks);

        return mapping.findForward("success");
    }

}
