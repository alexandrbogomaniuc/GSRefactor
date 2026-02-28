package com.abs.casino.actions.api.frbonus;

import com.abs.casino.actions.enter.CommonActionForm;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * User: ktd
 * Date: 01.04.11
 */
public class BonusLiteForm extends CommonActionForm {
    private final static Logger LOG = LogManager.getLogger(BonusLiteForm.class);

    private String hash;
    private String extBankId;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getExtBankId() {
        return extBankId;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = new ActionErrors();
        return actionErrors;
    }


    @Override
    public String toString() {
        final String TAB = "    ";
        final StringBuilder sb = new StringBuilder();
        sb.append("BonusForm");
        sb.append("[ " + super.toString() + TAB);
        sb.append("hash='").append(hash).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
