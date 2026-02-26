package com.abs.casino.support.cache.bank.edit.actions.editproperties;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.web.BaseAction;
import com.abs.casino.support.cache.bank.edit.forms.editproperties.BankPropertiesForm;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.axis.encoding.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;


public class BankSelectAction extends Action {
    private static final Logger LOG = LogManager.getLogger(BankSelectAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response)
            throws Exception {

        BankPropertiesForm bankPropForm = (BankPropertiesForm) form;
        long bankId = Long.parseLong(bankPropForm.getBankId() != null ?
                bankPropForm.getBankId() : request.getParameter("bankId"));

        if (request.getParameter("button") != null) {
            if (request.getParameter("button").equals("languagesSupport")) {
                ActionRedirect redirect = BaseAction.getActionRedirectByHost(request, "/support/languagetable.do");
                redirect.addParameter("bankId", bankId);
                return redirect;
            }
            if (request.getParameter("button").equals("acsConfig")) {
                ActionRedirect redirect = BaseAction.getActionRedirectByHost(request, "/support/loadAcsData.do");
                redirect.addParameter("bankId", bankId);
                return redirect;
            }
            if (request.getParameter("button").equals("FaceBookConfig")) {
                ActionRedirect redirect = BaseAction.getActionRedirectByHost(request, "/support/fbconfig.do");
                redirect.addParameter("bankId", bankId);
                return redirect;
            }
        }

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
        bankPropForm.setMode("full");
        bankPropForm.setAllowedRefererDomains(bankInfo.getAllowedRefererDomains());
        bankPropForm.setForbiddenRefererDomains(bankInfo.getForbiddenRefererDomains());

        XStream xStream = new XStream(new StaxDriver());
        String xml = xStream.toXML(bankInfo);
        request.setAttribute("bankInfoXML", Base64.encode(xml.getBytes()));
        return mapping.findForward("success");
    }
}
