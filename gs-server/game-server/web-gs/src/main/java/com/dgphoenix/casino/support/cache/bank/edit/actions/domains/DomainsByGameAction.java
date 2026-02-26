package com.dgphoenix.casino.support.cache.bank.edit.actions.domains;


import com.dgphoenix.casino.common.cache.DomainWhiteListCache;
import com.abs.casino.common.cache.data.domain.DomainWhiteList;
import com.abs.casino.support.cache.bank.edit.forms.domains.DomainWhiteListForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;

public class DomainsByGameAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        DomainWhiteListForm whiteListForm = (DomainWhiteListForm) form;
        int gameId = Integer.parseInt(whiteListForm.getSelectedGameId());
        List<String> domainList = new LinkedList<String>();
        DomainWhiteList whiteList = DomainWhiteListCache.getInstance().getDomainWhiteList(gameId);
        if (whiteList != null && whiteList.getDomainList() != null) {
            for (String domain : whiteList.getDomainList()) {
                domainList.add(domain);
            }
        }

        whiteListForm.setDomainList(domainList);
        whiteListForm.setRemoveList(new String[0]);
        whiteListForm.setNewDomains("");

        return mapping.findForward("success");
    }
}
