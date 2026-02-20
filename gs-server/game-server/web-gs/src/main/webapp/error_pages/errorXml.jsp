<%@ page contentType="text/xml;charset=UTF-8" %>
<%@ page import="com.dgphoenix.casino.system.configuration.GameServerConfiguration" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Set" %>
<%@ page import="com.dgphoenix.casino.actions.api.bonus.BonusForm" %>
<%@ page import="java.util.Enumeration" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<%
    BonusForm bonusForm = null;
    Enumeration<String> attributes = request.getAttributeNames();
    while (attributes.hasMoreElements()) {
        String attribute = attributes.nextElement();
        if (attribute.endsWith("Form")) {
            bonusForm = (BonusForm) request.getAttribute(attribute);
        }
    }

    if (bonusForm != null && bonusForm.isJson()) {%>
<jsp:forward page="errorJSON.jsp"/>
<%
} else {
    out.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>");
    out.print("<" + GameServerConfiguration.getInstance().getBrandApiRootTagName() + ">");
%>
<TIME><%=new Date()%></TIME>
<%
    out.println("    <REQUEST>");
    for (Map.Entry<String, String[]> parameter : (Set<Map.Entry<String, String[]>>) request.getParameterMap().entrySet()) {
        out.println(String.format("        <" + "%1$s>%2$s</%1$s>", parameter.getKey().toUpperCase(), parameter.getValue()[0]));
    }
    out.println("    </REQUEST>");
    out.print("    <RESPONSE>");
%>
    <RESULT>ERROR</RESULT>
    <DESCRIPTION><html:errors property="valid_error" prefix="" suffix="" header="" footer=""/></DESCRIPTION>
    <CODE><html:errors property="valid_error_code" prefix="" suffix="" header="" footer=""/></CODE>
<%
        out.println("    </RESPONSE>");
        out.println("</" + GameServerConfiguration.getInstance().getBrandApiRootTagName() + ">");
    }
%>
