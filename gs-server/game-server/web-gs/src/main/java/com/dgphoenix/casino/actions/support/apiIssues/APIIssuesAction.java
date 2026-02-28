package com.abs.casino.actions.support.apiIssues;

import com.dgphoenix.casino.cassandra.CassandraPersistenceManager;
import com.abs.casino.cassandra.persist.CassandraCallIssuesPersister;
import com.abs.casino.cassandra.persist.CassandraCallStatisticsPersister;
import com.abs.casino.common.cache.data.URLCallCounters;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by quant on 03.12.15.
 */
public class APIIssuesAction extends Action {
    public static final String SUCCESS_FIELD = "success count";
    public static final String FAILED_FIELD = "failed count";
    public static final String FAILED_PERCENT_FIELD = "failed percent";
    public static final String LAST_FAIL_TIME_FIELD = "last fail time";
    public static final String URL_FIELD = "URL";

    private static final Logger LOG = Logger.getLogger(APIIssuesAction.class);

    private final CassandraCallIssuesPersister callIssuesPersister;

    public APIIssuesAction() {
        CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
                .getBean("persistenceManager", CassandraPersistenceManager.class);
        callIssuesPersister = persistenceManager.getPersister(CassandraCallIssuesPersister.class);
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        APIIssuesForm issuesForm = (APIIssuesForm) form;
        LOG.debug(issuesForm);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        Calendar from = Calendar.getInstance();
        from.setTime(issuesForm.getStartDate());
        Calendar to = Calendar.getInstance();
        to.setTime(issuesForm.getEndDate());

        Map<Date, Map<Object, List<String>>> apiIssues = new TreeMap<Date, Map<Object, List<String>>>();
        for (; from.before(to); from.add(Calendar.DATE, 1)) {
            String date = CassandraCallStatisticsPersister.DATE_FORMAT.format(
                    from.getTime().toInstant().atZone(ZoneId.systemDefault()));
            Collection<URLCallCounters> counters = callIssuesPersister.getByDate(date);
            if (counters != null && !counters.isEmpty()) {

                Map<Object, List<String>> sortedCounters = null;
                if (issuesForm.isDescendingOrder()) {
                    sortedCounters = new TreeMap<Object, List<String>>(new Comparator<Object>() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            return ((Comparable<Object>) o2).compareTo(o1);
                        }
                    });
                } else {
                    sortedCounters = new TreeMap<Object, List<String>>();
                }

                for (URLCallCounters data : counters) {
                    final List<String> ar = new ArrayList<String>();
                    if ((issuesForm.getUrlFilter() == null || data.getUrl().contains(issuesForm.getUrlFilter()))) {
                        ar.add(data.getUrl());
                        ar.add(String.valueOf(data.getSuccessCount()));
                        ar.add(String.valueOf(data.getFailedCount()));
                        ar.add(df.format(new Date((data.getLastFailTime()))));
                        double failedPercent = 0.0;
                        long total = data.getFailedCount() + data.getSuccessCount();
                        if (total != 0) {
                            failedPercent = (double) data.getFailedCount() / total;
                        }
                        ar.add(String.valueOf(failedPercent));
                        if (issuesForm.getSortBy() == null) {
                            sortedCounters.put(sortedCounters.size(), ar);
                        } else if (issuesForm.getSortBy().equals(URL_FIELD)) {
                            int hostPos = data.getUrl().indexOf("://");
                            hostPos += (hostPos != -1) ? 3 : 1;
                            sortedCounters.put(data.getUrl().substring(hostPos), ar);
                        } else if (issuesForm.getSortBy().equals(SUCCESS_FIELD)) {
                            sortedCounters.put(data.getSuccessCount(), ar);
                        } else if (issuesForm.getSortBy().equals(FAILED_FIELD)) {
                            sortedCounters.put(data.getFailedCount(), ar);
                        } else if (issuesForm.getSortBy().equals(FAILED_PERCENT_FIELD)) {
                            sortedCounters.put(failedPercent, ar);
                        }
                    }
                }
                apiIssues.put(from.getTime(), sortedCounters);
            }
        }

        Set<Map.Entry<Date, Map<Object, List<String>>>> issues = apiIssues.entrySet();
        if (issues.size() == 0) {
            LOG.warn("Can not find any issues from=" + from.getTime() + " to=" + to.getTime() + " and specified filters.");
        } else {
            Map.Entry<Date, Map<Object, List<String>>>[] sortedByCountIssues = new Map.Entry[issues.size()];
            issues.toArray(sortedByCountIssues);
            if (issuesForm.isSortByCount()) {
                Arrays.sort(sortedByCountIssues, new Comparator<Map.Entry<Date, Map<Object, List<String>>>>() {
                    @Override
                    public int compare(Map.Entry<Date, Map<Object, List<String>>> left,
                                       Map.Entry<Date, Map<Object, List<String>>> right) {
                        return left.getValue().size() - right.getValue().size();
                    }
                });
            }
            issuesForm.setApiIssues(sortedByCountIssues);
            LOG.info("Found count=" + sortedByCountIssues.length + " issues.");
        }

        return mapping.findForward("success");
    }
}
