package com.abs.casino.support.cache.bank.edit.actions.domains;


import com.dgphoenix.casino.common.cache.DomainWhiteListCache;
import com.abs.casino.common.cache.data.domain.DomainWhiteList;
import com.abs.casino.common.web.BaseAction;
import com.abs.casino.support.cache.bank.edit.forms.domains.DomainWhiteListForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class EditDomainAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (request.getParameter("button").equals("back")) {
            return BaseAction.getActionRedirectByHost(request, "/support/domainwl.do");
        }
        DomainWhiteListForm whiteListForm = (DomainWhiteListForm) form;
        int gameId = Integer.parseInt(whiteListForm.getSelectedGameId());

        List<String> domains = new LinkedList<String>();
        for (String domain : whiteListForm.getDomainList()) {
            domains.add(domain.trim());
        }
        // remove
        List<String> domainsRemove = new ArrayList<String>();
        if (whiteListForm.getRemoveList() != null) {
            for (String index : whiteListForm.getRemoveList()) {
                domainsRemove.add(domains.get(Integer.parseInt(index)));
                DomainWhiteListCache.getInstance().removeDomain(gameId, domains.get(Integer.parseInt(index)));

            }
        }

        domains.removeAll(domainsRemove);
        // add new
        String newDomainsStr = whiteListForm.getNewDomains();
        StringTokenizer tokenizer = new StringTokenizer(newDomainsStr, " ;");
        while (tokenizer.hasMoreTokens()) {
            domains.add(tokenizer.nextToken());
        }

        whiteListForm.setDomainList(domains);
        whiteListForm.setNewDomains("");
        whiteListForm.setRemoveList(null);


        for (String domain : domains) {
            DomainWhiteListCache.getInstance().addDomainIfAbsent((int) gameId, domain);
        }

        whiteListForm.setAllDomains(getDomainsSet());

        return mapping.findForward("success");
    }

    private Set<String> getDomainsSet() {
        Set<String> domains = new HashSet<String>();
        for (DomainWhiteList domainWhiteList : DomainWhiteListCache.getInstance().getAllObjects().values()) {
            if (domainWhiteList.getDomainList() != null) {
                domains.addAll(domainWhiteList.getDomainList());
            }
        }
        return domains;
    }
}
