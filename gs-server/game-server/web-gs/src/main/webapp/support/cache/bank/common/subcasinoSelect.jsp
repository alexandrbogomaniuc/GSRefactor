<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<html>
<head><title>Subcasino select page</title></head>
<body>
<br>

<%
    Set<Long> setOfKeys = SubCasinoCache.getInstance().getAllObjects().keySet();
    List<Long> ids = new ArrayList<>(setOfKeys);
    Collections.sort(ids);
    String url = "/support/subCasino.do?subcasinoId=";
    String infoURL = "/tools/subCasinoInfo.jsp?subCasinoId=";
%>

<p><b>All subcasino:</b></p>

<table border="1" cellpadding="7" cellspacing="0">
    <tr>
        <td>ID</td>
        <td>Name</td>
        <td>Domain Name</td>
        <td>Info</td>
    </tr>

    <%
        for (Long id : ids) {
            String link = url + id;
            String infoLink = infoURL + id;
            final SubCasino subCasino = SubCasinoCache.getInstance().get(id);
            String sName = subCasino.getName();
            if (StringUtils.isTrimmedEmpty(sName)) {
                Long bankId = SubCasinoCache.getInstance().getDefaultBankId(id);
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                if (bankInfo != null)
                    sName = bankInfo.getExternalBankIdDescription();
            }
    %>

    <tr>
        <td><%=id%>
        </td>
        <td><a href="<%=link%>"><%=sName%>
        </a></td>
        <td><%=subCasino.getDomainNamesAsString()%>
        </td>
        <td><a href="<%=infoLink%>" target="_blank">INFO</a></td>
    </tr>

    <%
        }
    %>

</table>

<form action="/support/createSubCasino.jsp" style="margin:10px 0;">
    <input type="submit" value="Create new subCasino">
</form>

<html:form action="/support/importSubCasino" method="post" enctype="multipart/form-data">
    Import SubCasino/Bank: &nbsp;<html:file property="file" size="50"/>&nbsp;<html:submit value="import"/>
</html:form>

</body>
</html>