package com.dgphoenix.casino.support.bankInfo;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PropertyInfoAction extends Action {
    private static final Logger LOG = LogManager.getLogger(PropertyInfoAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        LOG.debug("Received form: {}", form);
        PropertyInfoForm propertyInfoForm = (PropertyInfoForm) form;
        ActionForward actionForward = mapping.findForward("result");
        ActionMessages messages = new ActionErrors();

        try {
            long bankId = propertyInfoForm.getBankId();
            String property = propertyInfoForm.getProperty();
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo != null) {
                request.setAttribute("propertyValue", bankInfo.getProperties().get(property));
            } else {
                actionForward = mapping.findForward("form");
                messages.add("bankInfo", new ActionMessage("Bank with this ID isn't found!<br>", false));
            }
        } catch (Exception e) {
            messages.add("bankInfo", new ActionMessage("Unexpected exception has been caused<br>", false));
            actionForward = mapping.findForward("form");
            LOG.warn("An exception occurred: ", e);
        }
        saveErrors(request, messages);
        return actionForward;
    }
}