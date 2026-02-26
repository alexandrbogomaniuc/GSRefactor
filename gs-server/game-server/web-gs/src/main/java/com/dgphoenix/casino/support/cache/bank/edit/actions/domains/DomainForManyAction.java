package com.abs.casino.support.cache.bank.edit.actions.domains;

import com.dgphoenix.casino.common.cache.DomainWhiteListCache;
import com.abs.casino.common.cache.data.domain.DomainWhiteList;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.support.cache.bank.edit.forms.domains.DomainWhiteListForm;
import com.abs.casino.support.cache.bank.edit.forms.domains.GameBean;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class DomainForManyAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        DomainWhiteListForm whiteListForm = (DomainWhiteListForm) form;
        if (!request.getParameter("button").equals("save")) {
            readGames(whiteListForm);
        } else {
            saveGames(whiteListForm);
        }
        //sort
        List<GameBean> games = whiteListForm.getGameBeans();
        final String domainForMany = whiteListForm.getDomainForMany();

        if (!StringUtils.isTrimmedEmpty(domainForMany)) {
            Collections.sort(games, new Comparator<GameBean>() {
                @Override
                public int compare(GameBean o1, GameBean o2) {
                    if (isContainsDomain(o1, domainForMany) && isContainsDomain(o2, domainForMany) ||
                            !isContainsDomain(o1, domainForMany) && !isContainsDomain(o2, domainForMany)) {
                        return o1.getName().compareTo(o2.getName());
                    } else {
                        if (isContainsDomain(o1, domainForMany)) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                }
            });
        }
        return mapping.findForward("success");
    }

    private boolean isContainsDomain(GameBean gameBean, String domain) {
        int gameId = Integer.parseInt(gameBean.getId());
        return DomainWhiteListCache.getInstance().isContainsDomain(gameId, domain);
    }

    private void readGames(DomainWhiteListForm whiteListForm) {
        List<String> gamesWithDomain = new ArrayList<String>();
        String domainForMany = whiteListForm.getDomainForMany();
        if (StringUtils.isTrimmedEmpty(domainForMany)) return;
        for (DomainWhiteList domainWhiteList : DomainWhiteListCache.getInstance().getAllObjects().values()) {
            if (DomainWhiteListCache.getInstance().isContainsDomain(domainWhiteList.getGameId(), domainForMany)) {
                gamesWithDomain.add(String.valueOf(domainWhiteList.getGameId()));
            }
        }
        whiteListForm.setSelectedGameList(gamesWithDomain.toArray(new String[0]));
    }

    private void saveGames(DomainWhiteListForm whiteListForm) {
        // remove all
        String domainForMany = whiteListForm.getDomainForMany();
        if (StringUtils.isTrimmedEmpty(domainForMany)) return;
        for (DomainWhiteList domainWhiteList : DomainWhiteListCache.getInstance().getAllObjects().values()) {
            if (DomainWhiteListCache.getInstance().isContainsDomain(domainWhiteList.getGameId(), domainForMany)) {
                domainWhiteList.removeDomain(domainForMany);
            }
        }

        // save selected
        if (whiteListForm.getSelectedGameList() != null) {
            for (String strGameId : whiteListForm.getSelectedGameList()) {
                long gameId = Long.parseLong(strGameId);
                DomainWhiteListCache.getInstance().addDomainIfAbsent((int) gameId, domainForMany);

            }
        }

        whiteListForm.setAllDomains(getDomainsSet());

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
