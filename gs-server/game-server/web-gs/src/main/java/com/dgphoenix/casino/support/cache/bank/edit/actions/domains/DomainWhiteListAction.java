package com.dgphoenix.casino.support.cache.bank.edit.actions.domains;

import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.DomainWhiteListCache;
import com.abs.casino.common.cache.data.domain.DomainWhiteList;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.support.cache.bank.edit.forms.domains.DomainWhiteListForm;
import com.dgphoenix.casino.support.cache.bank.edit.forms.domains.GameBean;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class DomainWhiteListAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        DomainWhiteListForm whiteListForm = (DomainWhiteListForm) form;
        List<LabelValueBean> adaptedGameList = new ArrayList<LabelValueBean>();
        List<GameBean> games = new ArrayList<GameBean>();
        for (BaseGameInfoTemplate gameInfoTemplate : BaseGameInfoTemplateCache.getInstance().getAllObjects().values()) {
            String gameName = gameInfoTemplate.getLocalizedGameName("en");
            if (gameName == null) gameName = gameInfoTemplate.getGameName();

            adaptedGameList.add(
                    new LabelValueBean(gameName +
                            " (id=" + gameInfoTemplate.getGameId() + ")",
                            String.valueOf(gameInfoTemplate.getGameId())));

            games.add(new GameBean(gameInfoTemplate.getGameId(), gameName));
        }
        Collections.sort(adaptedGameList, new Comparator<LabelValueBean>() {
            @Override
            public int compare(LabelValueBean o1, LabelValueBean o2) {
                return o1.getLabel().compareToIgnoreCase(o2.getLabel());
            }
        });
        Collections.sort(games, new Comparator<GameBean>() {
            @Override
            public int compare(GameBean o1, GameBean o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        whiteListForm.setGameList(adaptedGameList);

        whiteListForm.setGameBeans(games);
        whiteListForm.setSelectedGameList(null);
        whiteListForm.setDomainForMany("");

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
