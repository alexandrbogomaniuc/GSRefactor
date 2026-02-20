package com.dgphoenix.casino.support.cache.bank.edit.actions.common;


import com.dgphoenix.casino.support.cache.bank.edit.forms.common.SubcasinoForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AddSubcasinoAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        if (request.getParameter("button").equals("back")) {
            return mapping.findForward("back");
        }

        SubcasinoForm scForm = (SubcasinoForm) form;


        return mapping.findForward("success");
    }
}
