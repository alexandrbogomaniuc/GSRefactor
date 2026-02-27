package com.abs.casino.support.logviewer;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.CassandraHttpCallInfoPersister;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.util.support.AdditionalInfoAttribute;
import com.abs.casino.common.util.support.HttpCallInfo;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.dgphoenix.casino.common.web.BaseAction.SUCCESS_FORWARD;
import static java.util.Collections.emptyList;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 10.02.2020
 */
public class LogViewerAction extends Action {

    private final CassandraHttpCallInfoPersister httpCallInfoPersister;

    public LogViewerAction() {
        httpCallInfoPersister = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class)
                .getPersister(CassandraHttpCallInfoPersister.class);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {
        LogViewerForm logViewerForm = (LogViewerForm) form;
        if (logViewerForm.getSearchAttribute() != null) {
            List<HttpCallInfo> httpCallInfoList = getFromPersister(logViewerForm);
            if (!httpCallInfoList.isEmpty()) {
                logViewerForm.setHttpCallInfoList(httpCallInfoList);
                request.setAttribute("form", logViewerForm);
            } else {
                logViewerForm.setMessage("Nothing found");
            }
        }
        return mapping.findForward(SUCCESS_FORWARD);
    }

    private List<HttpCallInfo> getFromPersister(LogViewerForm logViewerForm) {
        String searchValue = logViewerForm.getSearchValue().trim();
        AdditionalInfoAttribute attribute = AdditionalInfoAttribute.valueOf(logViewerForm.getSearchAttribute());
        switch (attribute) {
            case SESSION_ID:
            case SUPPORT_TICKET_ID:
                return httpCallInfoPersister.getById(searchValue);
            case TOKEN:
                return httpCallInfoPersister.getByToken(searchValue);
            case GAME_SESSION_ID:
                long gameSessionId = Long.parseLong(searchValue);
                return httpCallInfoPersister.getByGameSessionId(gameSessionId);
            case ROUND_ID:
                long roundId = Long.parseLong(searchValue);
                return httpCallInfoPersister.getByRoundId(roundId);
            case TRANSACTION_ID:
                long transactionId = Long.parseLong(searchValue);
                return httpCallInfoPersister.getByTransactionId(transactionId);
            case EXTERNAL_ID:
                long bankId = logViewerForm.getBankId();
                return httpCallInfoPersister.getByExternalId(bankId, searchValue);
            default:
                return emptyList();
        }
    }
}
