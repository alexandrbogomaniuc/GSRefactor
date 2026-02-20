<%@ page import="java.io.InputStream" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Properties" %>
<%
    Map<String, String> localClusterMap = new LinkedHashMap<String, String>();
    Map<String, String> copyClusterMap = new LinkedHashMap<String, String>();
    Map<String, String> liveClusterMap = new LinkedHashMap<String, String>();

    Properties clusterProps = new Properties();
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("cluster-hosts.properties")) {
        if (is != null) {
            clusterProps.load(is);
        }
    } catch (Exception ignore) {
    }

    loadClusterEntries(clusterProps.getProperty("TEMPLATE_MANAGER_LOCAL_CLUSTERS"), localClusterMap);
    loadClusterEntries(clusterProps.getProperty("TEMPLATE_MANAGER_COPY_CLUSTERS"), copyClusterMap);
    loadClusterEntries(clusterProps.getProperty("TEMPLATE_MANAGER_LIVE_CLUSTERS"), liveClusterMap);

    String currentAddress = request.getScheme() + "://" + request.getServerName();
    int port = request.getServerPort();
    boolean includePort = ("http".equalsIgnoreCase(request.getScheme()) && port != 80)
            || ("https".equalsIgnoreCase(request.getScheme()) && port != 443);
    if (includePort) {
        currentAddress += ":" + port;
    }
    if (localClusterMap.isEmpty()) {
        localClusterMap.put("CURRENT CLUSTER", currentAddress);
    }
%>

<%!
    private void loadClusterEntries(String raw, Map<String, String> target) {
        if (raw == null) {
            return;
        }
        String[] entries = raw.split(";");
        for (String entry : entries) {
            String normalized = entry == null ? "" : entry.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            int splitIndex = normalized.indexOf('|');
            if (splitIndex <= 0 || splitIndex >= normalized.length() - 1) {
                continue;
            }
            String label = normalized.substring(0, splitIndex).trim();
            String url = normalized.substring(splitIndex + 1).trim();
            if (!label.isEmpty() && !url.isEmpty()) {
                target.put(label, url);
            }
        }
    }
%>


