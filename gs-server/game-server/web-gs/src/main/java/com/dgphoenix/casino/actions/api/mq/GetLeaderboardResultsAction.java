package com.abs.casino.actions.api.mq;
import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.mp.LeaderboardResultPersister;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.abs.casino.common.web.BaseAction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetLeaderboardResultsAction extends BaseAction<GetLeaderboardResultsForm> {
    protected final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    protected final LeaderboardResultPersister persister;

    public GetLeaderboardResultsAction() {
        persister = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class)
                .getPersister(LeaderboardResultPersister.class);
    }

    @Override
    protected ActionForward process(ActionMapping mapping, GetLeaderboardResultsForm actionForm,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {
        String result = persister.getLeaderboardResult(actionForm.getBankId(), actionForm.getLeaderboardId());
        response.getWriter().println(result);

        return null;
    }
}
