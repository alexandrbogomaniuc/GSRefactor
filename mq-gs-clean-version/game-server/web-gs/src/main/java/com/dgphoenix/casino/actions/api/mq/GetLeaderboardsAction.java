package com.dgphoenix.casino.actions.api.mq;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.dgphoenix.casino.cassandra.persist.mp.LeaderboardInfo;
import com.dgphoenix.casino.cassandra.persist.mp.LeaderboardResultPersister;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.web.BaseAction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public class GetLeaderboardsAction extends BaseAction<GetLeaderboardsForm> {
    protected final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    protected final LeaderboardResultPersister persister;

    public GetLeaderboardsAction() {
        persister = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class)
                .getPersister(LeaderboardResultPersister.class);
    }

    @Override
    protected ActionForward process(ActionMapping mapping, GetLeaderboardsForm actionForm,
                                    HttpServletRequest request, HttpServletResponse response) throws Exception {
        long bankId = actionForm.getBankId();
        response.getWriter().println(gson.toJson(new Leaderboards(bankId, persister.getLeaderboards(bankId))));

        return null;
    }

    private static class Leaderboards {
        private final long bankId;

        @SerializedName("leaderboards")
        private final List<LeaderboardInfo> leaderboardInfos;

        public Leaderboards(long bankId, List<LeaderboardInfo> leaderboardInfos) {
            this.bankId = bankId;
            this.leaderboardInfos = leaderboardInfos;
        }

        public long getBankId() {
            return bankId;
        }

        public List<LeaderboardInfo> getLeaderboards() {
            return leaderboardInfos;
        }
    }
}
