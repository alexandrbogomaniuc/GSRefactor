package com.abs.casino.protection.forms;


import org.apache.struts.action.ActionForm;

public class SessionKeyAccessForm extends ActionForm {

    private String encrMess;


    public String getEncrMess() {
        return encrMess;
    }

    public void setEncrMess(String encrMess) {
        this.encrMess = encrMess;
    }
}
