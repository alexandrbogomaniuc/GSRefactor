package com.abs.casino.support.bankInfo;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by rus-nura on 25.12.17.
 * 10:57
 */
public class PropertyInfoForm extends ActionForm {
    private long bankId;
    private String property = "";

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        if (bankId >= 0 && !property.isEmpty()) {
            return null;
        }

        ActionErrors errors = new ActionErrors();
        if (bankId < 0) {
            errors.add("bankInfo", new ActionMessage("Please, define the 'bankId' field!<br />", false));
        }
        if (property == null || property.isEmpty()) {
            errors.add("bankInfo", new ActionMessage("Please, define the 'bankProperty' field!<br />", false));
        }
        return errors;
    }

    @Override
    public String toString() {
        return "PropertyInfoForm{" +
                "bankId=" + bankId +
                ", property='" + property + '\'' +
                '}';
    }
}