<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="com.abs.casino.common.util.property.EnumProperty" %>
<%--
  Created by IntelliJ IDEA.
  User: quant
  Date: 01.07.16
  Time: 17:13
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<%
    long subcasinoId = 0;
    String subcasinoStr = request.getParameter("subcasinoId");
    if (StringUtils.isNotBlank(subcasinoStr)) {
        subcasinoId = Long.valueOf(subcasinoStr);
    }

    long bankId = 0;
    String bankIdStr = request.getParameter("bankId");
    if (StringUtils.isNotBlank(bankIdStr)) {
        bankId = Long.valueOf(bankIdStr);
    }

    String filterProperty = request.getParameter("filterProperty");
    if (filterProperty == null || filterProperty.equals("null")) {
        filterProperty = "";
    }

    List<Long> bankIds = SubCasinoCache.getInstance().getBankIds(subcasinoId);
    if (bankIds != null) {
        Collections.sort(bankIds);
    }

    String filter = "";
    boolean isEnumProp = false;
    if (StringUtils.isNotBlank(filterProperty)) {
        isEnumProp = BankInfo.class.getDeclaredField("KEY_" + filterProperty).isAnnotationPresent(EnumProperty.class);
        if (isEnumProp) {
            filter = "NONE";
        }
    }
    for (Long id : bankIds) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(id);
        if (bankInfo != null) {
            String prop = StringUtils.isNotBlank(filterProperty) ? bankInfo.getStringProperty(filterProperty) : "";
            if (isEnumProp && StringUtils.isBlank(prop)) {
                prop = "NONE";
            }
            if (prop == null) {
                prop = "";
            }
            if (StringUtils.isBlank(filterProperty) || !filter.equals(prop)) {
%>
<option value="<%=id%>" <%=(bankId == id ? "selected" : "")%>>
    <%=bankInfo.getExternalBankIdDescription()%>
    <%if (!StringUtils.isBlank(filterProperty)) {%>
    (<%=filterProperty%>=<%=prop%>)
    <%}%>
</option>
<%
            }
        }
    }
%>