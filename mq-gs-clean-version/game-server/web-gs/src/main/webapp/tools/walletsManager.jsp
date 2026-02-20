<%@ page import="org.apache.commons.lang.StringUtils" %>
<%--
  Created by IntelliJ IDEA.
  User: quant
  Date: 27.01.16
  Time: 16:40
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="./js/jquery.min.js"></script>


<script type="text/javascript">
    function setActionParams(gameId, accountData) {
        $("#gameId")[0].value = gameId;
        $("#accountData")[0].value = accountData;
        return true;
    }

    function confirmDeletion(gameId, accountData, operationId) {
        if (confirm("Are you sure you want to delete " + accountData + " id " + operationId + " for game id " + gameId + "?")) {
            return setActionParams(gameId, accountData);
        }
        return false;
    }

    function confirmDeletionFrbNotif(accountData, operationId) {
        if (confirm("Are you sure you want to delete FRBonusNotification id " + operationId + "?")) {
            return setActionParams(null, accountData);
        }
        return false;
    }

    function validateAccountId() {
        var numbers = /^[0-9]+$/;
        if ($("#accountId")[0].value != "") {
            if (!$("#accountId")[0].value.match(numbers)) {
                alert("Account id must contain only digits");
                return false;
            }
            return true;
        }
        alert("Account id not specified.");
        return false;
    }

    function validateBankIdAndUserId() {
        var numbers = /^[0-9]+$/;
        if (($("#bankId")[0].value != "" && $("#extUserId")[0].value != "")) {
            if (!$("#bankId")[0].value.match(numbers)) {
                alert("Bank id must contain only digits");
                return false;
            }
            return true;
        }

        alert("Bank id or External user id not specified.");
        return false;
    }
</script>


<html>
<head>
    <title></title>
</head>
<body>


<%
    long accountId = 0;
    int bankId = 0;
    String extUserId = "";
    String accId = request.getParameter("accountId");
    if (StringUtils.isNotEmpty(accId)) {
        accountId = Long.valueOf(accId);
    } else {
        String bnkId = request.getParameter("bankId");
        if (StringUtils.isNotEmpty(bnkId)) {
            bankId = Integer.valueOf(bnkId);
        }
        extUserId = request.getParameter("extUserId");
    }
%>

<form id="accountDataFormId" action="/tools/walletsManager.do" method="get">
    <table>
        <tr>
            <td>Account id</td>
            <td>
                <input type="text" id="accountId" name="accountId" value='<%=(accountId != 0 ? String.valueOf(accountId) : "")%>'/>
            </td>
        </tr>
        <tr>
            <td>
                <button type="submit" id="submitAccountId" name="changeType" value="show" onclick="return setActionParams('', 'show') && validateAccountId()">
                    show
                </button>
            </td>
        </tr>
    </table>
    <br>

    <table>
        <tr>
            <td>
                <jsp:include page="../support/cache/bank/common/subcasinoList.jsp"/>
            </td>
            <td>
                <table>
                    <tr>
                        <td>External user id</td>
                    </tr>
                    <tr>
                        <td>
                            <input type="text" id="extUserId" name="extUserId" value='<%=(extUserId != null ? extUserId : "")%>'/>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td>
                <input type="hidden" id="gameId" name="gameId"/>
            </td>
            <td>
                <input type="hidden" id="accountData" name="accountData"/>
            </td>
        </tr>
        <tr>
            <td>
                <button type="submit" id="submitBankUserId" name="changeType" value="show"> show</button>
            </td>
        </tr>
    </table>

    <div id="showAccountDataId"></div>
</form>

</body>
</html>

<script>
    banksListUrl = "/tools/banksList.jsp";

    $("#accountDataFormId").submit(function (event) {
        event.preventDefault();
        $.ajax({
            type: this.method,
            url: this.action,
            data: $(this).serialize(),
            success: function (data) {
                $("#showAccountDataId").html(data);
            },
            error: function (xhr, status, error) {
                $("#showAccountDataId").html(xhr.responseText);
            }
        });
    });

    $("#submitBankUserId").click(function () {
        setActionParams('', 'show');
        if (validateBankIdAndUserId()) {
            $("#accountId").val("");
            return true;
        }
        return false;
    });
</script>
