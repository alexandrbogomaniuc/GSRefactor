<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Properties" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Cluster Hosts Config</title>
    <link rel="stylesheet" href="/support/css/bootstrap.min.css"/>
</head>
<body>
<div class="container col-xs-10 col-xs-offset-1">
    <h2>Cluster Hosts Config</h2>
    <p>Source: <code>cluster-hosts.properties</code> (classpath)</p>
    <p>
        Quick links:
        <a href="/support/modernizationProgress.html">Progress</a> |
        <a href="/support/modernizationDocs.jsp">Docs Index</a> |
        <a href="/support/modernizationRunbook.jsp">Runbook</a> |
        <a href="/support/configPortal.jsp">Config Portal</a>
    </p>
    <%
        Properties props = new Properties();
        Properties descriptions = new Properties();
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("cluster-hosts.properties")) {
            if (is != null) {
                props.load(is);
            }
        }
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("cluster-hosts-descriptions.properties")) {
            if (is != null) {
                descriptions.load(is);
            }
        }
        List<String> keys = new ArrayList<>(props.stringPropertyNames());
        Collections.sort(keys);
    %>
    <% if (keys.isEmpty()) { %>
    <div class="alert alert-danger">cluster-hosts.properties is missing or empty.</div>
    <% } else { %>
    <table class="table table-bordered table-striped">
        <thead>
        <tr>
            <th>Key</th>
            <th>Value</th>
            <th>Description</th>
        </tr>
        </thead>
        <tbody>
        <% for (String key : keys) { %>
        <tr>
            <td><code><%= key %></code></td>
            <td><code><%= props.getProperty(key) %></code></td>
            <td><%= descriptions.getProperty(key, "") %></td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <% } %>
</div>
</body>
</html>
