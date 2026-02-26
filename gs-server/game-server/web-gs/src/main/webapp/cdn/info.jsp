<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraHostCdnPersister" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="com.abs.casino.common.games.CdnCheckResult" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%!
    PrintWriter pr;
%>

<%
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraHostCdnPersister hostCdnPersister = persistenceManager.getPersister(CassandraHostCdnPersister.class);

    pr = response.getWriter();
    pr.println("<pre>");
    try {


        String ip = request.getRemoteAddr();
        pr.println(ip);
        List<CdnCheckResult> cdnList = hostCdnPersister.getCdnByIp(ip);
        if (cdnList != null) {
            if (request.getParameter("delete") != null) {
                for (CdnCheckResult entry : cdnList) {
                    hostCdnPersister.remove(ip, entry.getCdnUrl());
                }
                cdnList = hostCdnPersister.getCdnByIp(ip);
            }
            if (cdnList != null) {
                for (CdnCheckResult entry : cdnList) {
                    pr.println(entry.getCdnUrl() + " " + entry.getLoadTime());
                }
            }
        }
    } catch (Throwable tr) {
        tr.printStackTrace(pr);
    }
    pr.println("</pre>");
%>

