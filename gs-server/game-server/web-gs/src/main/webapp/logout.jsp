<%@page session="false" %>
<%@ page import="com.dgphoenix.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.dgphoenix.casino.system.configuration.GameServerConfiguration" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.net.HttpURLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%
    try {
        String sUrl = request.getParameter("url");
        if (StringUtils.isTrimmedEmpty(sUrl)) {
            return;
        }
        URL url = new URL(sUrl);
        String host = url.getHost();
        PrintWriter responseWriter = response.getWriter();
        String domain = GameServerConfiguration.getInstance().getDomain();
        String gsDomain = GameServerConfiguration.getInstance().getGsDomain();
        if (host.endsWith(domain) || (gsDomain != null && host.endsWith(gsDomain))) {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod(request.getMethod());
            int clength = request.getContentLength();
            if (clength > 0) {
                con.setDoInput(true);
                byte[] idata = new byte[clength];
                request.getInputStream().read(idata, 0, clength);
                con.getOutputStream().write(idata, 0, clength);
            }
            response.setContentType(con.getContentType());

            BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                responseWriter.write(line + "\r\n");
            }
            rd.close();
        } else {
            ThreadLog.warn("logout.jsp: suspicious request (illegal domain found): " + url);
            responseWriter.write("Illegal request");
        }
        responseWriter.flush();
        responseWriter.close();
    } catch (Exception e) {
        response.setStatus(500);
    }
%>
