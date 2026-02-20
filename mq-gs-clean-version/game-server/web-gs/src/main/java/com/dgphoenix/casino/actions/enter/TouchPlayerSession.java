package com.dgphoenix.casino.actions.enter;

import com.dgphoenix.casino.common.SessionHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import com.dgphoenix.casino.common.cache.data.session.SessionInfo;
import com.dgphoenix.casino.common.transactiondata.ITransactionData;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.web.BaseAction;
import com.dgphoenix.casino.gs.LocalSessionTracker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User: flsh
 * Date: 10.04.15.
 */
public class TouchPlayerSession extends BaseAction<ActionForm> {

    private static final long UPDATE_ACTIVITY_PERIOD = 180000;
    private static final String SESSION_ID_ATTRIBUTE = "SID";
    private static final Logger LOG = LogManager.getLogger(TouchPlayerSession.class);

    @Override
    protected ActionForward process(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {

        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");

        String sessionId = request.getParameter(SESSION_ID_ATTRIBUTE);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Touching session, SID={}", sessionId);
        }

        String result = "error";
        try {
            if (!StringUtils.isTrimmedEmpty(sessionId) && !"null".equals(sessionId)) {
                SessionHelper.getInstance().lock(sessionId);
                try {
                    SessionHelper.getInstance().openSession();
                    SessionInfo sessionInfo = SessionHelper.getInstance().getTransactionData().getPlayerSession();
                    long now = System.currentTimeMillis();
                    if (sessionInfo == null) {
                        LOG.warn("SessionInfo not found: SID={}", sessionId);
                        //nop, result=error
                    } else if (now - sessionInfo.getLastActivityTime() >= UPDATE_ACTIVITY_PERIOD) {
                        sessionInfo.updateActivity();
                        SessionHelper.getInstance().commitTransaction();
                        result = "updated";
                    } else {
                        result = "delayed";
                    }
                    if (sessionInfo != null) {
                        processLocalSessionTracker();
                    }
                    SessionHelper.getInstance().markTransactionCompleted();
                } finally {
                    SessionHelper.getInstance().clearWithUnlock();
                    LOG.debug("Success unlock, SID={}", sessionId);
                }
            }
        } catch (Exception e) {
            LOG.error("Error of touching session, SID={}", sessionId, e);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Result of touching session, SID={}: result={}", sessionId, result);
        }

        response.getWriter().write(result);
        response.getWriter().flush();
        response.getWriter().close();
        return null;
    }

    private void processLocalSessionTracker() {
        ITransactionData transactionData = SessionHelper.getInstance().getTransactionData();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(transactionData.getBankId());
        GameSession gameSession = transactionData.getGameSession();
        if (gameSession != null && gameSession.isRealMoney() && !gameSession.isBonusGameSession() && !gameSession.isFRBonusGameSession()
                && bankInfo.isUseJvmSessionTracking() && bankInfo.getRealModeSessionTimeout() != null) {
            LocalSessionTracker localSessionTracker = ApplicationContextHelper.getApplicationContext().getBean(LocalSessionTracker.class);
            localSessionTracker.addSession(transactionData.getAccountId(), bankInfo.getRealModeSessionTimeout());
        }
    }

}
