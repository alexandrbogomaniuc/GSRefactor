<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%--
  Created by IntelliJ IDEA.
  User: fy
  Date: 3/4/14
  Time: 6:15 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html:errors/>

<a href="${pageContext.request.contextPath}/support/cache/bank/common/subcasinoSelect.jsp">Subcasino select</a>
<br/>
<%
    String subCasinoId = request.getParameter("subcasinoId");
    String bankId = request.getParameter("bankId");
    if (StringUtils.isTrimmedEmpty(subCasinoId) & !StringUtils.isTrimmedEmpty(bankId)) {
        try {
            long id = Long.parseLong(bankId);
            subCasinoId = String.valueOf(BankInfoCache.getInstance().getBankInfo(id).getSubCasinoId());
        } catch (Exception ignored) {

        }
    }
%>
<a href="${pageContext.request.contextPath}/support/subCasino.do?subcasinoId=<%=subCasinoId%>">Bank select</a>
<br/>
<a href="${pageContext.request.contextPath}/support/bankInfo.do?bankId=<%=bankId%>">Bank info</a>