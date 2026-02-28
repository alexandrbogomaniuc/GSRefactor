package com.abs.casino.forms.registration.cw;

import com.dgphoenix.casino.actions.enter.CommonActionForm;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.abs.casino.common.web.BaseAction;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * User: plastical
 * Date: 05.04.2010
 */
public class CWRegisterForm extends CommonActionForm {
    private final static Logger LOG = LogManager.getLogger(CWRegisterForm.class);

    private String firstName;
    private String lastName;
    private String email;
    private String userId;
    private String username;
    private String currencyCode;    //ISO 4217 currency code (3 uppercase chars)
    private String stubPassword;


    public CWRegisterForm() {
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public boolean isLaunchChecked() {
        return true;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getStubPassword() {
        return stubPassword;
    }

    public void setStubPassword(String stubPassword) {
        this.stubPassword = stubPassword;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = super.validate(mapping, request);

        if (!actionErrors.isEmpty()) return actionErrors;

        String userName = BaseAction.extractRequestParameterIgnoreCase(request, "username");
        if (StringUtils.isTrimmedEmpty(userName)) {
            getLogger().error("validate error username is empty");
            actionErrors.add("valid_error", new ActionMessage("error.login.generalValidationError"));
            return actionErrors;
        }
        this.username = userName;

        String userId = BaseAction.extractRequestParameterIgnoreCase(request, "userId");
        if (StringUtils.isTrimmedEmpty(userId)) {
            getLogger().error("validate error userId is empty");
            actionErrors.add("valid_error", new ActionMessage("error.login.generalValidationError"));
            return actionErrors;
        }
        this.userId = userId;

        this.currencyCode = BaseAction.extractRequestParameterIgnoreCase(request, "currencyCode");
        if (!StringUtils.isTrimmedEmpty(this.currencyCode)) {
            this.currencyCode = this.currencyCode.toUpperCase();
        } else {
            this.currencyCode = getBankInfo().getDefaultCurrency().getCode().toUpperCase();
        }

        this.firstName = BaseAction.extractRequestParameterIgnoreCase(request, "firstName");
        this.lastName = BaseAction.extractRequestParameterIgnoreCase(request, "lastName");
        this.email = BaseAction.extractRequestParameterIgnoreCase(request, "email");


        return actionErrors;
    }

    @Override
    public void reset(ActionMapping mapping, ServletRequest request) {
        super.reset(mapping, request);

        username = null;
        userId = null;
        email = null;
        firstName = null;
        lastName = null;
        currencyCode = null;
        stubPassword = null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CWRegisterForm");
        sb.append("[firstName='").append(firstName).append('\'');
        sb.append(", lastName='").append(lastName).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", stubPassword='").append(stubPassword).append('\'');
        sb.append(", currencyCode='").append(currencyCode).append('\'');
        sb.append(']');
        return sb.toString();
    }

}
