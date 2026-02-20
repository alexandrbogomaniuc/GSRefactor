<%@ page import="com.dgphoenix.casino.actions.support.apiIssues.APIIssuesForm" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.init.ApplicationScopeNames" %>
<%@ page import="com.dgphoenix.casino.cassandra.persist.CassandraCallStatisticsPersister" %>
<%@ page import="com.dgphoenix.casino.actions.support.apiIssues.APIIssuesAction" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.ZoneId" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>APIIssues</title>
    <style type="text/css">
        .noborders td {
            border: 0;
        }
    </style>

    <script language="JavaScript" type="text/javascript">
        function checkDates() {
            var dateDiff = endCalendar.getDate().getTime() - startCalendar.getDate().getTime();
            if (dateDiff < 0) {
                alert('Start Date cannot be after End Date');
                return false;
            } else if (dateDiff / (1000 * 3600 * 24) > 30) {
                alert('Maximum 30 days are allowed');
                return false;
            }
            return true;
        }
    </script>
</head>
<body>

<html:form action="/support/showAPIIssues" method="post">
    <table cellpadding="3" cellspacing="0" style="margin:10px; width:570px" border="0">
        <tr>
            <td class="heads">
                Start Date
            </td>
            <td colspan="2">
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
            </td>
        </tr>
        <tr>
            <td class="heads">
                End Date
            </td>
            <td colspan="2">
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
            </td>
        </tr>
        <tr>
            <td class="heads">
                URL filter
            </td>
            <td>
                <html:text property="urlFilter"></html:text>
            </td>
        </tr>
        <tr>
            <td class="heads">
                Sort by
            </td>
            <td>
                <html:select property="sortBy">
                    <html:option value="<%=APIIssuesAction.URL_FIELD%>"><%=APIIssuesAction.URL_FIELD%>
                    </html:option>
                    <html:option value="<%=APIIssuesAction.SUCCESS_FIELD%>"><%=APIIssuesAction.SUCCESS_FIELD%>
                    </html:option>
                    <html:option value="<%=APIIssuesAction.FAILED_FIELD%>"><%=APIIssuesAction.FAILED_FIELD%>
                    </html:option>
                    <html:option value="<%=APIIssuesAction.FAILED_PERCENT_FIELD%>"><%=APIIssuesAction.FAILED_PERCENT_FIELD%>
                    </html:option>
                </html:select>
            </td>
        </tr>
        <tr>
            <td class="heads">
                Desc
            </td>
            <td>
                <html:checkbox property="descendingOrder"/>
            </td>
        </tr>
        <tr>
            <td class="heads">
                Sort by count
            </td>
            <td>
                <html:checkbox property="sortByCount"/>
            </td>
        </tr>
        <tr>
            <td>
                <html:submit value="show" onclick="return checkDates()"/>
            </td>
        </tr>
    </table>
</html:form>

<script type="text/javascript" src="/support/js/triple_calendar.js"></script>

<script>
    var startCalendar = new TripleCalendar(APIIssuesForm.startDay,
            APIIssuesForm.startMonth,
            APIIssuesForm.startYear
    );
    var endCalendar = new TripleCalendar(APIIssuesForm.endDay,
            APIIssuesForm.endMonth,
            APIIssuesForm.endYear
    );

    var currentTime = new Date();
    var day = currentTime.getDate();
    var month = currentTime.getMonth();
    var year = currentTime.getFullYear();
    var indexy = 0;
    var years = endCalendar.yearCtrl.options;
    for (var i = 1; i < years.length; i++)
        if (years[i].text == year) indexy = i;

    if (endCalendar.dayCtrl.options.selectedIndex == 0 && endCalendar.yearCtrl.options.selectedIndex == 0
            && endCalendar.monthCtrl.options.selectedIndex == 0 && startCalendar.dayCtrl.options.selectedIndex == 0
            && startCalendar.yearCtrl.options.selectedIndex == 0
            && startCalendar.monthCtrl.options.selectedIndex == 0) {
        endCalendar.dayCtrl.options[day - 1].selected = true;
        endCalendar.yearCtrl.options[indexy].selected = true;
        endCalendar.monthCtrl.options[month].selected = true;
        var startDate = new Date(year, month, day - 10);
        startCalendar.yearCtrl.options[indexy].selected = true;
        startCalendar.monthCtrl.options[startDate.getMonth()].selected = true;
        startCalendar.dayCtrl.options[startDate.getDate() - 1].selected = true;
    }
</script>

<table border="1">
    <%
        APIIssuesForm form = (APIIssuesForm) request.getAttribute("APIIssuesForm");
        if (form != null) {
            Map.Entry<Date, Map<Object, List<String>>>[] issues = form.getApiIssues();
            if (issues == null || issues.length == 0) {
                if (request.getAttribute("javax.servlet.forward.request_uri") != null) {
    %>
    <div style="color: red">Can not find any issues for specified period and filters.</div>
    <%
        }
    } else {
        for (Map.Entry<Date, Map<Object, List<String>>> day : issues) {
            String date = CassandraCallStatisticsPersister.DATE_FORMAT.
                    format(LocalDateTime.ofInstant(day.getKey().toInstant(), ZoneId.systemDefault()));
    %>
    <tr class="noborders">
        <td>
            <br><b><%=date%>
        </b></br>
        </td>
    </tr>

    <tr>
        <td><%=APIIssuesAction.URL_FIELD%>
        </td>
        <td><%=APIIssuesAction.SUCCESS_FIELD%>
        </td>
        <td><%=APIIssuesAction.FAILED_FIELD%>
        </td>
        <td><%=APIIssuesAction.LAST_FAIL_TIME_FIELD%>
        </td>
        <td><%=APIIssuesAction.FAILED_PERCENT_FIELD%>
        </td>
    </tr>
    <%
        Collection<List<String>> values = day.getValue().values();
        for (List<String> dataRow : values) {
            Double failedPercent = Double.valueOf(dataRow.get(dataRow.size() - 1));
            if (failedPercent > 0.1) {
    %>
    <tr bgcolor="red">
            <%
    } else {
    %>
    <tr><%
        }
        for (int i = 0; i < dataRow.size() - 1; ++i) {
    %>
        <td>
            <%=dataRow.get(i)%>
        </td>
        <%
            }
            DecimalFormat df = new DecimalFormat("##0.00");
        %>
        <td>
            <%=df.format(failedPercent * 100.0) + '%'%>
        </td>
    </tr>
    <%
        }
    %>
    <%
                }
            }
        }
    %>
</table>

</body>
</html>