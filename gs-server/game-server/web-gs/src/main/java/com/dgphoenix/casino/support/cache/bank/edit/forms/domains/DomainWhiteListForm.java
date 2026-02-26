package com.abs.casino.support.cache.bank.edit.forms.domains;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DomainWhiteListForm extends ActionForm {

    private Collection<LabelValueBean> gameList;
    private List<String> domainList;
    private String selectedGameId;
    private String[] removeList;
    private String newDomains;

    private String domainForMany;
    private List<GameBean> gameBeans;
    private String[] selectedGameList;

    private Set<String> allDomains;

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        removeList = null;
        newDomains = null;
        selectedGameList = null;
    }


    public Set<String> getAllDomains() {
        return allDomains;
    }

    public void setAllDomains(Set<String> allDomains) {
        this.allDomains = allDomains;
    }


    public String getDomainForMany() {
        return domainForMany;
    }

    public void setDomainForMany(String domainForMany) {
        this.domainForMany = domainForMany;
    }

    public String[] getSelectedGameList() {
        return selectedGameList;
    }

    public void setSelectedGameList(String[] selectedGameList) {
        this.selectedGameList = selectedGameList;
    }

    public List<GameBean> getGameBeans() {
        return gameBeans;
    }

    public void setGameBeans(List<GameBean> gameBeans) {
        this.gameBeans = gameBeans;
    }

    public String getNewDomains() {
        return newDomains;
    }

    public void setNewDomains(String newDomains) {
        this.newDomains = newDomains;
    }

    public String[] getRemoveList() {
        return removeList;
    }

    public void setRemoveList(String[] removeList) {
        this.removeList = removeList;
    }

    public List<String> getDomainList() {
        return domainList;
    }

    public void setDomainList(List<String> domainList) {
        this.domainList = domainList;
    }

    public String getSelectedGameId() {
        return selectedGameId;
    }

    public void setSelectedGameId(String selectedGameId) {
        this.selectedGameId = selectedGameId;
    }

    public Collection<LabelValueBean> getGameList() {
        return gameList;
    }

    public void setGameList(Collection<LabelValueBean> gameList) {
        this.gameList = gameList;
    }
}
