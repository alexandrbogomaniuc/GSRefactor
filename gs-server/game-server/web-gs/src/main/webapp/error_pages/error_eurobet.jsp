<%@ page import="com.dgphoenix.casino.common.web.BaseAction" %>
<%@ page import="com.dgphoenix.casino.gs.GameServer" %>
<%@ page import="org.apache.struts.Globals" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<!DOCTYPE html>
<html>
<head>
    <style>
        body {
            background-color: white;
        }
    </style>
    <title>ERROR</title>
    <%
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setHeader("Expires", "0"); // Proxies.
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String time = formatter.format(new Date());
        Integer serverId = GameServer.getInstance().getServerId();
    %>
</head>
<body>
<br/><br/>
<div align=center>
    <% if (request.getAttribute(BaseAction.SUPPORT_TICKET_ID_ATTRIBUTE) != null) { %>
    <div>Si prega di contattare il servizio clienti e inviargli il seguente numero di richiesta:</div>
    <div>G<%=serverId%>:<%=request.getAttribute(BaseAction.SUPPORT_TICKET_ID_ATTRIBUTE)%>
    </div>
    <% } else if (request.getAttribute(Globals.ERROR_KEY) != null) { %>
    <html:messages id="error" message="false">
        <div><bean:write name="error"/></div>
    </html:messages>
    <% } else { %>
    <div>Unknown error occurred please try again or contact support services</div>
    <%}%>
    <br/>
    <div>Ora: <%=time%>
    </div>
</div>
</body>
</html>
