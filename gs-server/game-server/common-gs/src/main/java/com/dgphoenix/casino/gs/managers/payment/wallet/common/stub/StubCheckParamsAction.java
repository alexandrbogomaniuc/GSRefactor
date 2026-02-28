package com.abs.casino.gs.managers.payment.wallet.common.stub;

import com.abs.casino.common.web.BaseAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: ktd
 * Date: 15.04.2010
 */
public class StubCheckParamsAction extends BaseAction {
    private static final Logger LOG = LogManager.getLogger(StubCheckParamsAction.class);

    @Override
    public ActionForward process(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        LOG.debug("StubCheckParamsAction :: Request: " + request.toString());

        LOG.debug("Parameters: " + request.getParameterMap());

        //LOG.debug("USERID:" + request.getParameter(CCommonWallet.PARAM_USERID));

        return mapping.findForward(SUCCESS_FORWARD);
    }

}
