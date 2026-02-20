package com.dgphoenix.casino.support.cache.bank.edit.actions.common;

import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.dgphoenix.casino.common.cache.data.bank.SubCasino;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.support.CacheObjectComparator;
import com.dgphoenix.casino.support.cache.bank.edit.forms.common.SubcasinoForm;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.apache.axis.encoding.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.HashSet;

/**
 * User: vik
 * Date: 18.12.12
 */
public class EditSubCasinoAction extends Action {
    private static final Logger LOG = LogManager.getLogger(EditSubCasinoAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        ActionMessages messages = new ActionMessages();
        ActionMessages errors = getErrors(request);
        if (errors == null) {
            errors = new ActionErrors();
        }

        SubcasinoForm subcasinoForm = (SubcasinoForm) form;
        long subCasinoId = Long.parseLong(subcasinoForm.getId());
        SubCasino subCasino = SubCasinoCache.getInstance().get(subCasinoId);

        XStream xStream = new XStream(new StaxDriver());
        String subCasinoXML = request.getParameter("subCasinoXML");
        if (StringUtils.isTrimmedEmpty(subCasinoXML)) {
            LOG.error("parameter not found: subCasinoXML");
            return mapping.findForward("error");
        }

        SubCasino defaultSubCasino = (SubCasino) xStream.fromXML(new String(Base64.decode(subCasinoXML)));

        if (subCasino == null) {
            return mapping.findForward("error");
        }

        HashMap<String, HashSet<String>> differentProperties = CacheObjectComparator.compare(defaultSubCasino, subCasino);

        String newName = subcasinoForm.getName();
        if (!StringUtils.isTrimmedEmpty(newName) && !newName.equals(subCasino.getName())) {
            if (differentProperties.containsKey("name") && !differentProperties.get("name").isEmpty()) {
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.versioning.cannotChangeValue",
                        "Name", defaultSubCasino.getName(), subCasino.getName(), newName));
            } else {
                messages.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("message.EditSubCasinoAction.subCasinoNameChanged", subCasino.getName(), newName));
                subCasino.setName(newName);
            }
        }

        long newDefaultBank = Long.parseLong(subcasinoForm.getDefaultBank());
        if (subCasino.getBankIds().contains(newDefaultBank) && newDefaultBank != subCasino.getDefaultBank()) {
            if (differentProperties.containsKey("defaultBank") && !differentProperties.get("defaultBank").isEmpty()) {
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionMessage("error.versioning.cannotChangeValue",
                        "Default bank", defaultSubCasino.getDefaultBank(), subCasino.getDefaultBank(), newDefaultBank));
            } else {
                messages.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage("message.EditSubCasinoAction.subCasinoDefaultBankChanged", subCasino.getDefaultBank(), newDefaultBank));
                subCasino.setDefaultBank(newDefaultBank);
            }
        }

        saveErrors(request.getSession(), errors);
        saveMessages(request.getSession(), messages);
        return BaseAction.getActionRedirectByHost(request, "/support/subCasino.do?subcasinoId=" + subCasinoId);
    }
}

