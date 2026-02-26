package com.abs.casino.support.cache.bank.edit.actions.domains;


import com.dgphoenix.casino.common.cache.DomainWhiteListCache;
import com.abs.casino.common.cache.data.domain.DomainWhiteList;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.support.cache.bank.edit.forms.domains.DomainWhiteListForm;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class ReadDomainsAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        DomainWhiteListForm whiteListForm = (DomainWhiteListForm) form;
        List<String> gamesWithDomain = new ArrayList<String>();
        String domainForMany = whiteListForm.getDomainForMany();
        if (StringUtils.isTrimmedEmpty(domainForMany)) return mapping.findForward("success");
        for (DomainWhiteList domainWhiteList : DomainWhiteListCache.getInstance().getAllObjects().values()) {
            if (DomainWhiteListCache.getInstance().isContainsDomain(domainWhiteList.getGameId(), domainForMany)) {
                gamesWithDomain.add(String.valueOf(domainWhiteList.getGameId()));
            }
        }
        whiteListForm.setSelectedGameList(gamesWithDomain.toArray(new String[gamesWithDomain.size()]));

        return mapping.findForward("success");
    }
}
