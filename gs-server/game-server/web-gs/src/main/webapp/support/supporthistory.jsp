<%@ page import="com.abs.casino.init.ApplicationScopeNames" %>
<%@ page import="com.abs.casino.web.history.GameHistoryListAction" %>
<%@ page import="com.dgphoenix.casino.common.util.Pair" %>
<%@ page import="com.dgphoenix.casino.common.util.DigitFormatter" %>
<%@ page import="com.dgphoenix.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.account.AccountManager" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="template" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/casino.tld" prefix="casino" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%
    String accountIdStr = request.getParameter("accountId");
    if (StringUtils.isTrimmedEmpty(accountIdStr)) {
        response.getWriter().append("accountId is missing");
        return;
    }
    try {
        long accountId = Long.parseLong(accountIdStr);

        AccountInfo accountInfo;

        accountInfo = AccountManager.getInstance().getByAccountId(accountId);
        if (accountInfo == null) {
            response.getWriter().append("AccountInfo is null, by accountId=").append(accountIdStr);
            return;
        }
    } catch (Exception e) {
        ThreadLog.error("supporthistory.jsp::", e);
        response.getWriter().append("Can't get accountInfo, by accountId=").append(accountIdStr);
        return;
    }
%>
<style>
    body {
        background-color: #ffffff;
        background-repeat: no-repeat;
        background-position: right top;
        font-size: 12px;
        font-family: Arial;
    }

    .heads {
        color: #000000;
        font-size: 13px;
        font-family: Arial;
    }

    a, td {
        font-size: 12px;
        font-family: Arial;
    }

</style>

<script type="text/javascript" src="/support/js/triple_calendar.js"></script>
<script>
    function openWnd(url, width, height, name, isfullscreen) {
        var optionstr = "menubar=no";
        optionstr += ", toolbar=no";
        optionstr += ", status=no";
        optionstr += ", personalbar=no";
        optionstr += ", resizable=yes";
        if (isfullscreen == "yes") {
            optionstr += ', width=' + screen.width;
            optionstr += ', height=' + screen.height;
            optionstr += ', fullscreen=yes';
        } else {
            optionstr += ', width=' + width;
            optionstr += ', height=' + height;
            optionstr += ', fullscreen=no';
        }
        var wnd = window.open(url, name, optionstr);
        wnd.focus();
        return wnd;
    }
    function clickFilter() {
        if (startCalendar.getDate().getTime() > endCalendar.getDate().getTime()) {
            alert('Start Date cannot be after End Date');
            return false;
        }
        GameHistorySupportForm.page.value = 1;
        return true;
    }

    function doPaging(page) {
        GameHistorySupportForm.page.value = page;
        GameHistorySupportForm.submit();
    }

</script>

<html:form action="/support/gamehistory" method="get">
    <html:hidden property="page"/>
    <html:hidden property="lastGameSessionDateOnPage"/>
    <html:hidden property="accountId" value="<%=accountIdStr%>"/>
    <html:hidden property="itemsPerPage"/>
    <table cellpadding="3" cellspacing="0" style="margin:10px; width:570px" border="0">
        <tr>
            <td class="heads">
                Start Date
            </td>
            <td>
                <html:select property="startYear">
                    <html:options collection="<%=ApplicationScopeNames.YEARS_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
                <html:select property="startMonth" styleClass="adm-form-input-date">
                    <html:options collection="<%=ApplicationScopeNames.MONTH_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
                <html:select property="startDay" styleClass="adm-form-input-date">
                    <html:options collection="<%=ApplicationScopeNames.DAYS_IN_MONTH_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
                <html:select property="startHour">
                    <html:options collection="<%=ApplicationScopeNames.HOURS_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
                <html:select property="startMinute" styleClass="adm-form-input-date">
                    <html:options collection="<%=ApplicationScopeNames.MINUTES_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
                <html:select property="startSecond" styleClass="adm-form-input-date">
                    <html:options collection="<%=ApplicationScopeNames.SECONDS_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
            </td>
        </tr>
        <tr>
            <td class="heads">
                End Date
            </td>
            <td>
                <html:select property="endYear" styleClass="adm-form-input-date">
                    <html:options collection="<%=ApplicationScopeNames.YEARS_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
                <html:select property="endMonth" styleClass="adm-form-input-date">
                    <html:options collection="<%=ApplicationScopeNames.MONTH_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
                <html:select property="endDay" styleClass="adm-form-input-date">
                    <html:options collection="<%=ApplicationScopeNames.DAYS_IN_MONTH_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
                <html:select property="endHour">
                    <html:options collection="<%=ApplicationScopeNames.HOURS_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
                <html:select property="endMinute" styleClass="adm-form-input-date">
                    <html:options collection="<%=ApplicationScopeNames.MINUTES_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
                <html:select property="endSecond" styleClass="adm-form-input-date">
                    <html:options collection="<%=ApplicationScopeNames.SECONDS_VALUES%>"
                                  property="id"
                                  labelProperty="value"/>
                </html:select>
            </td>
        </tr>
        <tr>
            <td class="heads">
                Game
            </td>
            <td>
                <html:select property="gameId" styleClass="adm-form-input-date">
                    <html:option value="-1">All</html:option>

                    <html:options collection="<%=GameHistoryListAction.GAMES_LIST%>"
                                  property="id"
                                  labelProperty="value"/>

                </html:select>
            </td>
        </tr>
        <tr>
            <td class="heads">
                Mode
            </td>
            <td>
                <html:select property="mode" styleClass="adm-form-input-date">
                    <html:option value="0">All</html:option>
                    <html:option value="1">Cash</html:option>
                    <html:option value="2">Bonus</html:option>
                    <html:option value="3">Free Rounds (FRB)</html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td colspan="2" align="center">
                <html:submit styleClass="button" onclick="return clickFilter();">
                    <bean:message key="casino.history.button.filter" locale="false"/>
                </html:submit>
            </td>
        </tr>
    </table>
</html:form>

<script language="JavaScript" type="text/javascript">
    var startCalendar = new TripleCalendar(GameHistorySupportForm.startDay,
            GameHistorySupportForm.startMonth,
            GameHistorySupportForm.startYear
    );
    var endCalendar = new TripleCalendar(GameHistorySupportForm.endDay,
            GameHistorySupportForm.endMonth,
            GameHistorySupportForm.endYear
    );

    var currentTime = new Date(<%=((Long) request.getAttribute(GameHistoryListAction.CURRENT_TIME))%>);
    var day = currentTime.getDate();
    var month = currentTime.getMonth() + 1;
    var year = currentTime.getFullYear();
    var indexy = 0;
    var ss = endCalendar.yearCtrl.options;
    for (var i = 1; i < ss.length; i++)
        if (ss[i].text == year) indexy = i;

    if (endCalendar.dayCtrl.options.selectedIndex == 0 && endCalendar.yearCtrl.options.selectedIndex == 0
            && endCalendar.monthCtrl.options.selectedIndex == 0 && startCalendar.dayCtrl.options.selectedIndex == 0 && startCalendar.yearCtrl.options.selectedIndex == 0
            && startCalendar.monthCtrl.options.selectedIndex == 0) {
        endCalendar.dayCtrl.options[day - 1].selected = true;
        endCalendar.yearCtrl.options[indexy].selected = true;
        endCalendar.monthCtrl.options[month - 1].selected = true;
        startCalendar.dayCtrl.options[day - 1].selected = true;
        startCalendar.yearCtrl.options[indexy].selected = true;
        startCalendar.monthCtrl.options[month - 1].selected = true;
        GameHistorySupportForm.startHour.options[0].selected = true;
        GameHistorySupportForm.startMinute.options[0].selected = true;
        GameHistorySupportForm.startSecond.options[0].selected = true;
        GameHistorySupportForm.endHour.options[23].selected = true;
        GameHistorySupportForm.endMinute.options[59].selected = true;
        GameHistorySupportForm.endSecond.options[59].selected = true;
    }


</script>
&nbsp;&nbsp;&nbsp;Note: VBA history older than 3 months is archived and is not available<br/>
<table cellspacing="0" cellpadding="0" width="100%" border="1">
    <tr>
        <td>Game name</td>
        <td>Start time</td>
        <td>End time</td>
        <td>Income</td>
        <td>Payout</td>
    </tr>
    <logic:present name="<%=GameHistoryListAction.GAME_HISTORY_LIST%>">
        <logic:iterate id="entry" name="<%=GameHistoryListAction.GAME_HISTORY_LIST%>">
            <bean:define id="historyUrl" name="entry" property="historyUrl" type="java.lang.String"/>
            <tr>
                <td>
                    <a href="gamehistory.jsp#"
                       onclick="openWnd('<%=historyUrl%>','950','475','VAB_WINDOW2'); return false;">
                        <bean:write name="entry" property="localizedGameName"/></a>
                </td>
                <td><bean:write name="entry" property="startDate"/></td>
                <td><bean:write name="entry" property="endDate"/></td>
                <td><bean:write name="entry" property="income"/></td>
                <td><bean:write name="entry" property="payout"/></td>
            </tr>
        </logic:iterate>
    </logic:present>
    <%
        Pair<Long, Long> totals = (Pair<Long, Long>) request.getAttribute(GameHistoryListAction.TOTALS);
        Pair<Long, Long> subTotals = (Pair<Long, Long>) request.getAttribute(GameHistoryListAction.SUBTOTALS);
        if (subTotals != null) {
    %>
    <tr>
        <td>Subtotals:</td>
        <td colspan="2"></td>
        <td><%=DigitFormatter.getDollarsFromCents(subTotals.getKey())%>
        </td>
        <td><%=DigitFormatter.getDollarsFromCents(subTotals.getValue())%>
        </td>
    </tr>
    <%
        }
        if (totals != null) {
    %>
    <tr>
        <td>Totals:</td>
        <td colspan="2"></td>
        <td><%=DigitFormatter.getDollarsFromCents(totals.getKey())%>
        </td>
        <td><%=DigitFormatter.getDollarsFromCents(totals.getValue())%>
        </td>
    </tr>
    <%
        }
    %>
</table>

</br>
<div align=center>
    <casino:pageNavigationV2 formName="GameHistorySupportForm"/>
</div>
<br/>
