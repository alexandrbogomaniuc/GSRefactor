package com.abs.casino.support.cache.bank.edit.actions.common;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: vik
 * Date: 24.12.12
 */
public abstract class AbstractCRUDAction<T extends ActionForm> extends Action {
    public final Logger logger = LogManager.getLogger(this.getClass());

    public static final String BUTTON = "button";

    private static final String READ_COMMAND = "Read";
    private static final String CREATE_COMMAND = "Create";
    private static final String EDIT_COMMAND = "Edit";
    private static final String REMOVE_COMMAND = "Remove";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        T actionForm = (T) form;
        if (request.getParameter(BUTTON) != null) {
            String buttonValue = request.getParameter(BUTTON);
            if (buttonValue.equals(getReadCommand(request))) {
                read(actionForm);
            }
            if (buttonValue.equals(getCreateCommand(request))) {
                create(actionForm);
            }
            if (buttonValue.equals(getEditCommand(request))) {
                edit(actionForm);
            }
            if (buttonValue.equals(getRemoveCommand(request))) {
                remove(actionForm);
            }
        }
        return mapping.findForward("success");
    }

    public String getReadCommand(HttpServletRequest request) {
        return READ_COMMAND;
    }

    public String getCreateCommand(HttpServletRequest request) {
        return CREATE_COMMAND;
    }

    public String getEditCommand(HttpServletRequest request) {
        return EDIT_COMMAND;
    }

    public String getRemoveCommand(HttpServletRequest request) {
        return REMOVE_COMMAND;
    }

    public void read(T form) throws Exception {
    }

    public void create(T form) throws Exception {
    }

    public void edit(T form) throws Exception {
    }

    public void remove(T form) throws Exception {
    }

}
