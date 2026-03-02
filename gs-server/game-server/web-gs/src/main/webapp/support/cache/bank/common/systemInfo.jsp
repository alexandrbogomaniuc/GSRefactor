<%@ page import="java.util.List" %>
<%@ page import="com.abs.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.ArrayList" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<html>
<head><title>Subcasino info page</title></head>
<body>

<html:form action="/support/loaddefbank">
    <html:hidden property="subcasinoId" value="${param.subcasinoId}"/>
    <html:submit value="Add New Bank"/>
</html:form>
<%
    long subcasinoId = Long.parseLong(request.getParameter("subcasinoId"));
    List<Long> listOfBankId = SubCasinoCache.getInstance().getBankIds(subcasinoId);
    List<Long> bankIds = new ArrayList<Long>();
    bankIds.addAll(listOfBankId);
    Collections.sort(bankIds);

    response.getWriter().println("System id: " + subcasinoId + "<br>");

    response.getWriter().println("System info:");
    response.getWriter().println("<table border=1 cellpadding=\"7\" cellspacing=\"0\">");
    response.getWriter().println("<tr><td> ID </td><td> Ext ID </td><td> Name </td></tr>");
    for (Long id : bankIds) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(id);
        response.getWriter().println("<tr>");

        response.getWriter().println("<td>");
        response.getWriter().println(id);
        response.getWriter().println("</td>");

        response.getWriter().println("<td>");
        response.getWriter().println(bankInfo.getExternalBankId());
        response.getWriter().println("</td>");

        response.getWriter().println("<td>");
        String link = "\"" + "/support/bankInfo.do?bankId=" + id + "\"";
        response.getWriter().println("<a href=" + link + ">");
        response.getWriter().println(bankInfo.getExternalBankIdDescription());
        response.getWriter().println("</a>");
        response.getWriter().println("</td>");

        response.getWriter().println("</tr>");
    }
    response.getWriter().println("</table>");
%>


</body>
</html>