<%@ page import="com.abs.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.abs.casino.system.configuration.GameServerConfiguration" %>
<%@ page import="com.sun.management.OperatingSystemMXBean" %>
<%@ page import="java.lang.management.ManagementFactory" %>
<%@ page import="java.lang.management.MemoryMXBean" %>
<%@ page import="java.lang.management.MemoryUsage" %>
<%@ page import="java.lang.management.ThreadMXBean" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<%
    GameServerConfiguration bean = ApplicationContextHelper.getBean(GameServerConfiguration.class);

    response.getWriter().println("Server:" + bean.getServerLabel());
    response.getWriter().println("</br>");

    OperatingSystemMXBean platformMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    double processCpuLoad = platformMXBean.getProcessCpuLoad() * 100;
    response.getWriter().println(String.format("Process Cpu Load: %.2f%%%n",processCpuLoad));
    response.getWriter().println("</br>");

    double systemCpuLoad = platformMXBean.getSystemCpuLoad() * 100;
    response.getWriter().println(String.format("System Cpu Load: %.2f%%%n", systemCpuLoad));
    response.getWriter().println("</br>");

    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
    response.getWriter().println(String.format("Heap Memory Used: %d MB%n", heapUsage.getUsed() / (1024 *1024)));
    response.getWriter().println("</br>");
    response.getWriter().println(String.format("Heap Memory Max: %d MB%n", heapUsage.getMax() / (1024 *1024)));
    response.getWriter().println("</br>");

    MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
    response.getWriter().println(String.format("Non-Heap Memory Used: %d MB%n", nonHeapUsage.getUsed() / (1024 *1024)));
    response.getWriter().println("</br>");
    response.getWriter().println(String.format("Non-Heap Memory Max: %d MB%n", nonHeapUsage.getMax() / (1024 *1024)));
    response.getWriter().println("</br>");

    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    int threadCount = threadBean.getThreadCount();
    response.getWriter().println(String.format("Current Thread Count: %d%n", threadCount));
    response.getWriter().println("</br>");
%>
</body>
</html>
