package com.abs.casino.web.history;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: zhevlakoval
 * Date: 15.03.12
 * Time: 13:35
 * To change this template use File | Settings | File Templates.
 */
public class GameHistoryXMLListForm extends GameHistoryListForm {

    private int itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
    private ActionErrors errors;

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public ActionErrors getErrors() {
        return errors;
    }

    /**
     * @return Количество отображаемых страниц
     */
    public int getLastPage() {
        return (int) Math.ceil((double) getCount() / getItemsPerPage());
    }

    @Override
    public void setGameId(Long gameId) {
        if (gameId == null || gameId == 0) {
            super.setGameId(-1L);
        } else {
            super.setGameId(gameId);
        }
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        errors = super.validate(mapping, request);
        return new ActionErrors();
    }
}
