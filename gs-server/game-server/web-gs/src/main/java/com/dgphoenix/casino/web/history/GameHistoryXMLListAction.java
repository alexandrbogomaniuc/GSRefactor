package com.abs.casino.web.history;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: zhevlakoval
 * Date: 15.03.12
 * Time: 13:33
 * To change this template use File | Settings | File Templates.
 */
public class GameHistoryXMLListAction extends GameHistoryListAction {
    private static final Logger LOG = LogManager.getLogger(GameHistoryXMLListAction.class);

    @Override
    public ActionForward process(ActionMapping mapping, GameHistoryListForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        GameHistoryXMLListForm frm = (GameHistoryXMLListForm) form;
        if (frm.getErrors().isEmpty()) {
            return super.process(mapping, frm, request, response);
        }
        return mapping.findForward(SUCCESS_FORWARD);
    }
}
