package com.dgphoenix.casino.protection.forms;


import org.apache.struts.action.ActionForm;

public class DomainValidForm extends ActionForm {

    private String encrMess;

    public String getEncrMess() {
        return encrMess;
    }

    public void setEncrMess(String encrMess) {
        this.encrMess = encrMess;
    }


}
