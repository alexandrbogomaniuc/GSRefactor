package com.abs.casino.support.cache.bank.edit.forms.language;


import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.upload.FormFile;

import javax.servlet.http.HttpServletRequest;

public class UploadForm extends ActionForm {

    private FormFile file;
    private String setPartiallyDone;

    public UploadForm() {
    }

    public FormFile getFile() {
        return file;
    }

    public void setFile(FormFile file) {
        this.file = file;
    }

    public String getSetPartiallyDone() {
        return setPartiallyDone;
    }

    public void setSetPartiallyDone(String value) {
        setPartiallyDone = value;
    }

    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (file.getFileSize() > 1024 * 1024) {
            errors.add("file", new ActionMessage("error.file.size"));
        }
        return errors;
    }
}

