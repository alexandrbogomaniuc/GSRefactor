package com.dgphoenix.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.support.cache.bank.edit.forms.common.SubcasinoForm;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.axis.encoding.Base64;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: vik
 * Date: 19.12.12
 */
public class SubcasinoAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        SubcasinoForm subcasinoForm = (SubcasinoForm) form;

        long subcasinoId = Long.parseLong(request.getParameter("subcasinoId"));
        SubCasino subCasino = SubCasinoCache.getInstance().get(subcasinoId);

        subcasinoForm.setId(String.valueOf(subcasinoId));
        subcasinoForm.setName(subCasino.getName());

        subcasinoForm.setDefaultBank(String.valueOf(subCasino.getDefaultBank()));
        subcasinoForm.setAdditionalDomainNames(subCasino.getDomainNames());
        subcasinoForm.setDomainNameToEdit("");

        List<Long> listOfBankId = subCasino.getBankIds();
        if (listOfBankId == null) {
            return mapping.findForward("success");
        }
        List<Long> bankIds = new ArrayList<Long>();
        bankIds.addAll(listOfBankId);
        Collections.sort(bankIds);

        List<LabelValueBean> labelValueBeanList = new ArrayList<LabelValueBean>();
        for (long id : bankIds) {
            labelValueBeanList.add(new LabelValueBean(String.valueOf(id), String.valueOf(id)));
        }
        subcasinoForm.setBankIds(labelValueBeanList);

        XStream xStream = new XStream(new StaxDriver());
        String xml = xStream.toXML(subCasino);
        request.setAttribute("subCasinoXML", Base64.encode(xml.getBytes()));
        return mapping.findForward("success");
    }
}
