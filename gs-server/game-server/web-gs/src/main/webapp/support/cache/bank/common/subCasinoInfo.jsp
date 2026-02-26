<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ taglib prefix="logic" uri="http://struts.apache.org/tags-logic" %>
<%@ taglib prefix="input" uri="http://struts.apache.org/tags-html" %>
<%
    String strSubCasino = request.getParameter("subcasinoId");
    if (strSubCasino == null) {
        Object subcasinoForm = request.getAttribute("SubcasinoForm");
        if (subcasinoForm != null) {
            try {
                Object idValue = subcasinoForm.getClass().getMethod("getId").invoke(subcasinoForm);
                if (idValue != null) {
                    strSubCasino = String.valueOf(idValue);
                }
            } catch (Exception ignore) {
                // keep fallback path below
            }
        }
    }
    if (strSubCasino == null) {
        response.getWriter().println("subcasinoId is missing.");
        return;
    }
    Long subcasinoId = Long.parseLong(strSubCasino);

    List<Long> listOfBankId = SubCasinoCache.getInstance().getBankIds(subcasinoId);
    List<Long> bankIds = new ArrayList<>();
    if (listOfBankId != null) {
        bankIds.addAll(listOfBankId);
        Collections.sort(bankIds);
    }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head><title><%= subcasinoId %> :: <%= SubCasinoCache.getInstance().get(subcasinoId).getName() %>
</title></head>
<body>

<script language="JavaScript" type="text/javascript">

    function selectedDomainName(oList) {
        var df = document.forms[2];
        eobj = df.domainNameToEdit;
        selected = oList.selectedIndex;
        if (selected != -1) {
            eobj.value = oList.options[selected].value;
        }
    }

</script>
<html:errors/>
<html:messages id="msg" message="true" header="messages.header" footer="messages.footer">
    <LI><span style="color: green"><bean:write name="msg"/></span></LI>
</html:messages>
<% if (listOfBankId != null) { %>
System id: <%= subcasinoId %> <br/>
System info:
<table border="1" cellpadding="7" cellspacing="0">
    <tr>
        <td> ID</td>
        <td> Ext Id</td>
        <td> Name</td>
        <td> Default currency</td>
        <td> Export</td>
        <td> Environment</td>
        <td> Master bank</td>
    </tr>
    <% for (Long id : bankIds) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(id);
        String link = "/support/bankInfo.do?bankId=" + id;
        String link2 = "/support/exportSubCasino.do?id=" + subcasinoId + "&exportedBankId=" + id;
        String link3 = (bankInfo != null &&
                !StringUtils.isTrimmedEmpty(bankInfo.getAPIServiceEnvironmentUrl()) &&
                !StringUtils.isTrimmedEmpty(bankInfo.getAPIServiceFundAccountUrl()) &&
                !StringUtils.isTrimmedEmpty(bankInfo.getAPIServiceActiveTokenUrl())) ?
                "/tools/api/service.jsp?bankId=" + bankInfo.getId() + "&fromSupport=1" : "";
        String serviceString = !StringUtils.isTrimmedEmpty(link3) ? "<a href='" + link3 + "'>Service</a>" : "N/A";
        Long masterBankId = bankInfo == null ? null : bankInfo.getMasterBankId();
        String masterBank = "";
        String masterJackpot = "";
        if (masterBankId != null) {
            masterBank = "/support/compare/banks.do?clusterOne=Current&clusterTwo=Current&bankIdOne=" + id + "&bankIdTwo=" + masterBankId + "&allCurrencies=true";
            masterJackpot = "/support/compare/jackpots.do?clusterOne=Current&clusterTwo=Current&bankIdOne=" + id + "&bankIdTwo=" + masterBankId + "&jackpotType=SPGJackpot";
        }
    %>
    <tr>
        <td><%= id %>
        </td>
        <td><%= bankInfo == null ? "Not found" : bankInfo.getExternalBankId() %>
        </td>
        <td>
            <a href="<%= link %>">
                <%= bankInfo == null ? "Not found" : bankInfo.getExternalBankIdDescription() %>
            </a>
        </td>
        <td>
            <%= bankInfo == null ? "Not found" : bankInfo.getDefaultCurrency().getCode() %>
        </td>
        <td>
            <a href="<%= link2 %>">Export</a>
        </td>
        <td>
            <%=serviceString%>
        </td>
        <td>
            <% if (!StringUtils.isTrimmedEmpty(masterBank)) { %>
            <a href="<%=masterBank%>"><%=masterBankId%>
            </a> + <a href="<%=masterJackpot%>">Jackpot</a>
            <% } %>
        </td>
    </tr>
    <% } %>
</table>
<% } %>

<div style="margin:10px 0">
    <span style="padding-right: 10px"><html:link href="/support/cache/bank/common/subcasinoSelect.jsp"> MAIN PAGE </html:link></span>
    <span style="padding-right: 10px">
    <html:link href="/support/showIntegrationDetails.jsp" target="_blank"> Integration Details
        <html:param name="subcasinoId"><%=subcasinoId%>
        </html:param>
    </html:link>
        </span>
    <span style="padding-right: 10px">
    <html:link href="/tools/subCasinoInfo.jsp" target="_blank"> SubCasino info
        <html:param name="subCasinoId"><%=subcasinoId%>
        </html:param>
    </html:link>
    </span>
</div>

<html:form action="/support/editSubCasino">
    <html:hidden property="id"/>
    <input type="hidden" name="subCasinoXML" value="<%= request.getAttribute("subCasinoXML") %>"/>
    Name: <html:text property="name"/><br>
    Default Bank: <html:select property="defaultBank"><br>
    <html:optionsCollection property="bankIds"/>
</html:select><br>
    <html:submit value="Save"/>
</html:form>
<html:form action="/support/exportSubCasino">
    <html:hidden property="id"/>
    <html:hidden property="exportedBankId" value="-1"/>
    &nbsp;&nbsp;&nbsp;<html:submit value="Export All SubCasino"/>
</html:form>
<br>
<html:form action="/support/additionalDomainName">
    <html:hidden property="id"/>
    Additional Domain Names: <br>
    <html:select property="oldDomainNameToEdit" onchange="selectedDomainName(this)" size="5">
        <html:options property="additionalDomainNames"/>
    </html:select>
    <br>
    Add or Edit Domain Name: <html:text property="domainNameToEdit" size="50"/><br>
    <html:submit value="Add Domain Name" property="button"/>
    <html:submit value="Edit Domain Name" property="button"/>
    <html:submit value="Remove Domain Name" property="button"/>
</html:form>
<br>
<html:form action="/support/loaddefbank">
    <html:hidden property="subcasinoId" value="${param.subcasinoId}"/>
    <html:submit value="Add New Bank"/>
</html:form>

</body>
</html>
