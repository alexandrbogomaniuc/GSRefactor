<%@page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino,
                com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>


<%
    /*
    Parameters:
        newSubcasinoId
        subCasinoName
        firstBankId
        hostName
    */

    boolean hasNoErrors = true;

    if (!isParametresEmpty(request)) {

        try {
            String newSubcasinoId = request.getParameter("newSubcasinoId");
            long targetSubcasinoId;

            try {
                targetSubcasinoId = Long.parseLong(newSubcasinoId);
                //response.getWriter().println("newSubcasinoId: " + targetSubcasinoId);
            } catch (NumberFormatException ex) {
                response.getWriter().println("<font color=red>Incorrect format. Parameter 'newSubcasinoId': " + newSubcasinoId + "</font>");
                return;
            }

            String firstBankIdStr = request.getParameter("firstBankId");
            long firstBankId;

            try {
                firstBankId = Long.parseLong(firstBankIdStr);
                //response.getWriter().println("firstBankId: " + firstBankId);
            } catch (NumberFormatException ex) {
                response.getWriter().println("<font color=red>Incorrect format. Parameter 'firstBankId': " + firstBankIdStr + "</font>");
                return;
            }

            String subCasinoName = request.getParameter("subCasinoName");
            if (!StringUtils.isTrimmedEmpty(subCasinoName)) {
                //response.getWriter().println("subCasinoName: " + subCasinoName);
            } else {
                response.getWriter().println("<font color=red>Parameter 'subCasinoName' can't be empty</font>");
                return;
            }

            String hostName = request.getParameter("hostName");
            if (StringUtils.isTrimmedEmpty(hostName)) {
                response.getWriter().println("<font color=red>Parameter 'hostNames' can't be empty</font>");
                return;
            }

            List<Long> banksList = new ArrayList();
            banksList.add(firstBankId);

            List<String> hostsList = new ArrayList();
            hostsList.add(hostName);

            String checkMessage = isExist(targetSubcasinoId, firstBankId, hostName);
            if (checkMessage != null) {
                response.getWriter().println("<BR><font color=red>" + checkMessage + "</font>");
                return;
            }

            SubCasino casino = new SubCasino(targetSubcasinoId, subCasinoName, firstBankId, banksList, hostsList);

            RemoteCallHelper.getInstance().saveAndSendNotification(casino);

%>

<p>SubCasino has been created:<%=subCasinoName%> (<%=newSubcasinoId%>)</p>
<a href="/support/cache/bank/common/subcasinoSelect.jsp"> Back to Subcasino List </a>

<%


    } catch (Exception e) {
        e.printStackTrace(response.getWriter());
    }

} else {%>

<p><b>Creating new SubCasino:</b></p>
<table>
    <form action="/support/createSubCasino.jsp">
        <tr>
            <td>SubCasino Id:</td>
            <td><input type="text" name="newSubcasinoId"></td>
        </tr>
        <tr>
            <td>SubCasino name:</td>
            <td><input type="text" name="subCasinoName"></td>
        </tr>
        <tr>
            <td>First bank Id:</td>
            <td><input type="text" name="firstBankId"></td>
        </tr>
        <tr>
            <td>SubCasino domain name:</td>
            <td><input type="text" name="hostName"></td>
        </tr>
        <tr>
            <td colspan="2"><input type="submit" value="Create"></td>
        </tr>
    </form>
</table>

<%
    }

%>

<%!
    String isExist(long subcasinoId, long bankId, String hostName) {
        if (SubCasinoCache.getInstance().get(subcasinoId) != null)
            return "Subcasino (" + subcasinoId + ") is already exist!";
        else if (BankInfoCache.getInstance().getBankInfo(bankId) != null)
            return "Bank (" + bankId + ") is already exist!";
        else if (SubCasinoCache.getInstance().getSubCasinoByDomainName(hostName) != null)
            return "Host (" + hostName + ") is already exist!";
        else
            return null;
    }

    private boolean isParametresEmpty(HttpServletRequest request) {
        return request.getQueryString() == null || request.getQueryString().isEmpty();
    }

%>
