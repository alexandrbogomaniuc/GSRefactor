package com.abs.casino.actions.system.util;

import com.abs.casino.common.util.ZipUtils;
import com.abs.casino.common.web.BaseAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: plastical
 * Date: 23.08.2010
 */
public class EncoderAction extends BaseAction<EncoderForm> {
    private static final Logger LOG = LogManager.getLogger(EncoderAction.class);
    public static final String CMD_ENCODE = "encode";
    public static final String CMD_DECODE = "decode";

    @Override
    protected ActionForward process(ActionMapping mapping, EncoderForm actionForm, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        String command = actionForm.getCommand();

        String text = actionForm.getText();

        String result;
        if (CMD_ENCODE.equals(command)) {
            byte[] bytes;
            try {
                bytes = ZipUtils.zipStringToBytes(text);
            } catch (Throwable e) {
                LOG.error(this.getClass().getSimpleName() + "::process error:", e);
                addError(request, "error.common.zipFailed");
                return mapping.findForward(ERROR_FORWARD);
            }

            try {
                result = new String(ZipUtils.encodeMIME(bytes));
            } catch (Throwable e) {
                LOG.error(this.getClass().getSimpleName() + "::process error:", e);
                addError(request, "error.common.mimeEncodingFailed");
                return mapping.findForward(ERROR_FORWARD);
            }
        } else {
            byte[] unMimed;
            try {
                unMimed = ZipUtils.decodeMIME(text.getBytes());
            } catch (Throwable e) {
                LOG.error(this.getClass().getSimpleName() + "::process error:", e);
                addError(request, "error.common.mimeDecodingFailed");
                return mapping.findForward(ERROR_FORWARD);
            }

            try {
                result = ZipUtils.unzipStringFromBytes(unMimed);
            } catch (Throwable e) {
                LOG.error(this.getClass().getSimpleName() + "::process error:", e);
                addError(request, "error.common.unzipFailed");
                return mapping.findForward(ERROR_FORWARD);
            }
        }

        request.setAttribute("result", result);

        return mapping.findForward(SUCCESS_FORWARD);
    }
}
