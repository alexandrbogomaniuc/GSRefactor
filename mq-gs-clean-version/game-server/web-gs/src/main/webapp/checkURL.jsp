<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%!
    public static int getResponseCode(String urlString) {
        try {
            URL u = new URL(urlString);
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();
            huc.setRequestMethod("GET");
            huc.connect();
            return huc.getResponseCode();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 404;
    }
%>