package com.dgphoenix.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.support.cache.bank.edit.forms.common.NewBankNSubCasinoForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * User: vik
 * Date: 16.01.13
 */
public class NewBankNSubCasinoAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        List<Long> allBankIdOfSubCasino = new ArrayList<Long>();
        for (Long id : SubCasinoCache.getInstance().getAllObjects().keySet()) {
            SubCasino subCasino = SubCasinoCache.getInstance().get(id);
            allBankIdOfSubCasino.addAll(subCasino.getBankIds());
        }
        List<LabelValueBean> freeBanks = new ArrayList<LabelValueBean>();
        for (Long bankId : BankInfoCache.getInstance().getBankIds()) {
            if (!allBankIdOfSubCasino.contains(bankId)) {
                freeBanks.add(new LabelValueBean(String.valueOf(bankId), String.valueOf(bankId)));
            }
        }
        NewBankNSubCasinoForm bankNSubCasinoForm = (NewBankNSubCasinoForm) form;

        bankNSubCasinoForm.setBankIds(freeBanks);


        return mapping.findForward("success");
    }

}
