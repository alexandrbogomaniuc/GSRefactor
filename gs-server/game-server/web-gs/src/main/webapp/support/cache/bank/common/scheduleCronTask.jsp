<%@ page import="com.abs.casino.cassandra.persist.CassandraBankInfoPersister" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.init.QuartzInitializer" %>
<%@ page import="org.quartz.TriggerKey" %>
<%@ page import="org.springframework.context.ApplicationContext" %>
<%@ page import="org.springframework.scheduling.quartz.SchedulerFactoryBean" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.quartz.Trigger" %>
<%@ page import="org.quartz.JobKey" %>
<%@ page import="com.dgphoenix.casino.cassandra.CassandraPersistenceManager" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    CassandraPersistenceManager persistenceManager = ApplicationContextHelper.getApplicationContext()
            .getBean("persistenceManager", CassandraPersistenceManager.class);
    CassandraBankInfoPersister bankInfoPersister = persistenceManager.getPersister(CassandraBankInfoPersister.class);

    if (request.getParameterMap().containsKey("bankId")) {
        String bankId = request.getParameter("bankId");
        String action = request.getParameterMap().containsKey("action") ? request.getParameter("action") : "get";
        ApplicationContext context = ApplicationContextHelper.getApplicationContext();
        if ("set".equals(action)) {
            BankInfo bi = bankInfoPersister.get(bankId);

            context.getBean(QuartzInitializer.class).scheduleBankShutdownJobForBank(bi);
            response.getWriter().println(bankId + ": SCHEDULED");
            return;
        }

        SchedulerFactoryBean sfb = context.getBean(SchedulerFactoryBean.class);
        Trigger trigger = sfb.getScheduler().getTrigger(new TriggerKey(String.valueOf(bankId)));
        if (trigger == null) {
            response.getWriter().println(bankId + " No trigger found");
            return;
        }

        if ("get".equals(action)) {
            Date nextFireTime = trigger.getNextFireTime();
            response.getWriter().println(bankId + " Next fire at: " + nextFireTime);
        } else if ("delete".equals(action)) {
            if (sfb.getScheduler().deleteJob(new JobKey(bankId))) {
                response.getWriter().println(bankId + " Job removed");
            } else {
                response.getWriter().println(bankId + " Job NOT removed");
            }
        }
    } else {
        response.getWriter().println("Provide bankId");
    }
%>