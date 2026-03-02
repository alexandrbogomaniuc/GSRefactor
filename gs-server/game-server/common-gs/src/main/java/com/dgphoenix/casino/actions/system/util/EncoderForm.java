package com.abs.casino.actions.system.util;

import com.abs.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;

/**
 * User: plastical
 * Date: 23.08.2010
 */
public class EncoderForm extends ActionForm {
    private static final Logger LOG = LogManager.getLogger(EncoderForm.class);
    private String command;
    private String text;

    public EncoderForm() {
    }

    public EncoderForm(String command, String text) {
        this.command = command;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors actionErrors = new ActionErrors();

        if (StringUtils.isTrimmedEmpty(command)) {
            LOG.debug(this.getClass().getSimpleName() + "::validate command is empty");
            actionErrors.add("valid_error", new ActionMessage("error.login.generalValidationError"));
        }

        if (!EncoderAction.CMD_DECODE.equals(command) && !EncoderAction.CMD_ENCODE.equals(command)) {
            LOG.debug(this.getClass().getSimpleName() + "::validate command is:" + command);
            actionErrors.add("valid_error", new ActionMessage("error.login.generalValidationError"));
        }

        if (StringUtils.isTrimmedEmpty(text)) {
            LOG.debug(this.getClass().getSimpleName() + "::validate text is empty");
            actionErrors.add("valid_error", new ActionMessage("error.login.generalValidationError"));
        }

        return actionErrors;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("EncoderForm");
        sb.append("{command='").append(command).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
