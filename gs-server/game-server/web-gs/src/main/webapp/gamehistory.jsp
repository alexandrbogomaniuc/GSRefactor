<%@ page import="com.dgphoenix.casino.init.ApplicationScopeNames" %>
<%@ page import="com.abs.casino.web.history.GameHistoryListAction" %>
<%@ page import="com.dgphoenix.casino.common.util.Pair" %>
<%@ page import="com.dgphoenix.casino.common.util.DigitFormatter" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.system.configuration.GameServerConfiguration" %>
<%@ page import="java.util.Locale" %>
<%@ page import="org.apache.commons.lang.LocaleUtils" %>
<%@ page import="org.apache.struts.Globals" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="template" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/casino.tld" prefix="casino" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<logic:present name="<%=GameHistoryListAction.SESSION_ID%>" scope="request">
    <%
        String lang = request.getParameter("lang");
        if (lang != null) {
            try {
                Locale locale = LocaleUtils.toLocale(lang);
                session.setAttribute(Globals.LOCALE_KEY, locale);
            } catch (IllegalArgumentException ignored) {
            }
        }

        Integer bankId = (Integer) request.getAttribute(GameHistoryListAction.BANK_ID);
        if (bankId == 77) { //casino Tropeiza
    %>

    <link rel="stylesheet" type="text/css" media="screen"
          href="https://www.tropeziapalace.com/css/<%=GameServerConfiguration.getInstance().getBrandName()%>_gamehistory.css"/>

    <%
    } else { //all other casinos
    %>

    <style>
        body {
            background-color: #ffffff;
            background-repeat: no-repeat;
            background-position: right top;
            font-size: 12px;
            font-family: Arial, sans-serif;
        }

        .heads {
            color: #000000;
            font-size: 13px;
            font-family: Arial, sans-serif;
        }

        a, td {
            font-size: 12px;
            font-family: Arial, sans-serif;
        }

    </style>

    <%
        }
    %>
    <script type="text/javascript" src="/js/triple_calendar.js"></script>
    <%
        if (bankId == 4) {
    %>
    <table width="100%" height=180>
        <tr>
            <td align="center"><img src="/images/pw_logo.jpg" alt="" width="100%" height="100%"></td>
        </tr>
    </table>
    <%
        }
    %>
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
                alert('<bean:message key="report.start_date_cannot_be_after_end_date"/>');
                return false;
            }
            GameHistoryListForm.page.value = 1;
            return true;
        }

        function doPaging(page) {
            GameHistoryListForm.page.value = page;
            GameHistoryListForm.submit();
        }

    </script>

    <html:form action="/gamehistory">
        <html:hidden property="page"/>
        <html:hidden property="lastGameSessionDateOnPage"/>
        <html:hidden property="sessionId" value="<%=(String)request.getAttribute(GameHistoryListAction.SESSION_ID)%>"/>
        <html:hidden property="lang"/>
        <table cellpadding="3" cellspacing="0" style="margin:10px; width:570px" border="0">
            <tr>
                <td class="heads">
                    <bean:message key="report.start_date"/>
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
                    <bean:message key="report.end_date"/>
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
                    <bean:message key="report.game"/>
                </td>
                <td>
                    <html:select property="gameId" styleClass="adm-form-input-date">
                        <html:option value="-1"><bean:message key="report.all"/></html:option>

                        <html:options collection="<%=GameHistoryListAction.GAMES_LIST%>"
                                      property="id"
                                      labelProperty="value"/>

                    </html:select>
                </td>
            </tr>
            <tr>
                <td class="heads">
                    <bean:message key="report.mode"/>
                </td>
                <td>
                    <html:select property="mode" styleClass="adm-form-input-date">
                        <html:option value="0"><bean:message key="report.all"/></html:option>
                        <html:option value="1"><bean:message key="report.cash"/></html:option>
                        <html:option value="2"><bean:message key="report.bonus"/></html:option>
                        <html:option value="3"><bean:message key="report.free_rounds.frb"/></html:option>
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

    <script>
        var startCalendar = new TripleCalendar(GameHistoryListForm.startDay,
                GameHistoryListForm.startMonth,
                GameHistoryListForm.startYear
        );
        var endCalendar = new TripleCalendar(GameHistoryListForm.endDay,
                GameHistoryListForm.endMonth,
                GameHistoryListForm.endYear
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
            GameHistoryListForm.startHour.options[0].selected = true;
            GameHistoryListForm.startMinute.options[0].selected = true;
            GameHistoryListForm.startSecond.options[0].selected = true;
            GameHistoryListForm.endHour.options[23].selected = true;
            GameHistoryListForm.endMinute.options[59].selected = true;
            GameHistoryListForm.endSecond.options[59].selected = true;

            <%
                if ((bankId == 77) || (bankId == 477) ) { // Casino Tropeiza & Mansion
            %>
            GameHistoryListForm.submit();
            <%
            }
            %>

        }
    </script>
    <%
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        String betColumnName = "You lost";
        String winColumnName = "You won";
        String valueFormatString = bankInfo.getCurrencyFormatString();
        int historyTimeOffset = bankInfo.getHistoryOffsetInclDst(System.currentTimeMillis());
        String timeZone = "GMT";
        if (historyTimeOffset != 0) {
            long hoursOffset = (historyTimeOffset - 1440) / 60;
            timeZone += (hoursOffset < 0) ? hoursOffset : "+" + hoursOffset;
        } else {
            timeZone += "+0";
        }
    %>
    &nbsp;&nbsp;&nbsp;<bean:message key="report.note.history"/> (<bean:message key="report.timezone"/>: <%=timeZone%>)<br/>
    <table cellspacing="0" cellpadding="0" width="100%" border="1">
        <tr>

            <%
                if (bankId == 77) { //casino Tropeiza
            %>
            <th><bean:message key="report.game_name"/></th>
            <th><bean:message key="report.start_time"/></th>
            <th><bean:message key="report.end_time"/></th>
            <th><%=betColumnName%></th>
            <th><%=winColumnName%></th>
            <%
            } else { //all another casino
            %>
            <td><bean:message key="report.game_name"/></td>
            <td><bean:message key="report.start_time"/></td>
            <td><bean:message key="report.end_time"/></td>
            <td><%=betColumnName%></td>
            <td><%=winColumnName%></td>
            <%
                }
            %>

        </tr>
        <logic:present name="<%=GameHistoryListAction.GAME_HISTORY_LIST%>">
            <logic:iterate id="entry" name="<%=GameHistoryListAction.GAME_HISTORY_LIST%>"
                           type="com.abs.casino.web.history.GameHistoryListEntry">
                <tr>
                    <td>
                        <a href="gamehistory.jsp#"
                           onclick="openWnd('<%=entry.getHistoryUrl()%>','950','475','VAB_WINDOW2'); return false;">
                            <bean:write name="entry" property="localizedGameName"/></a>
                    </td>
                    <td><bean:write name="entry" property="startDate"/></td>
                    <td><bean:write name="entry" property="endDate"/></td>
                    <td><%=MessageFormat.format(valueFormatString, entry.getIncome())%></td>
                    <td><%=MessageFormat.format(valueFormatString, entry.getPayout())%></td>
                </tr>
            </logic:iterate>
        </logic:present>
        <%
            Pair<Long, Long> totals = (Pair<Long, Long>) request.getAttribute(GameHistoryListAction.TOTALS);
            Pair<Long, Long> subTotals = (Pair<Long, Long>) request.getAttribute(GameHistoryListAction.SUBTOTALS);
            if (subTotals != null) {
        %>
        <tr>
            <td><bean:message key="report.subtotals"/>:</td>
            <td colspan="2"></td>
            <td><%=MessageFormat.format(valueFormatString, DigitFormatter.getDollarsFromCents(subTotals.getKey()))%></td>
            <td><%=MessageFormat.format(valueFormatString, DigitFormatter.getDollarsFromCents(subTotals.getValue()))%></td>
        </tr>
        <%
            }
            if (totals != null) {
        %>
        <tr>
            <td><bean:message key="report.totals"/>:</td>
            <td colspan="2"></td>
            <td><%=MessageFormat.format(valueFormatString, DigitFormatter.getDollarsFromCents(totals.getKey()))%></td>
            <td><%=MessageFormat.format(valueFormatString, DigitFormatter.getDollarsFromCents(totals.getValue()))%></td>
        </tr>
        <%
            }
        %>
    </table>

    <br/>
    <div align=center>
        <casino:pageNavigationV2 formName="GameHistoryListForm"/>
    </div>

    <br/>
</logic:present>

<logic:notPresent name="<%=GameHistoryListAction.SESSION_ID%>" scope="request">
    <div id="center" align=center>
        <bean:message key="error.history.invalidParameters"/>
    </div>
</logic:notPresent>

