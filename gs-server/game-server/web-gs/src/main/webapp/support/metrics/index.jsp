<%@ page import="com.abs.casino.system.MetricsManager" %>
<%@ page import="com.abs.casino.common.util.system.Metric" %>
<%@ page import="java.util.concurrent.TimeUnit" %>
<%@ page import="com.abs.casino.common.util.Pair" %>
<%@ page import="java.util.List" %>
<%@ page import="com.abs.casino.common.cache.ServerConfigsCache" %>
<%@ page import="com.abs.casino.common.config.GameServerConfig" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="com.abs.casino.common.util.system.MetricStat" %>
<%@ page import="com.abs.casino.gs.GameServer" %>
<%@ page import="java.util.TimeZone" %>
<%!
    private static final int SWITCH_TO_SPARSE_AFTER_MAX_HOURS = 12;
%>
<%
    Metric[] possibleMetrics = Metric.values();
    Map<Integer, GameServerConfig> serverConfigs = ServerConfigsCache.getInstance().getAllObjects();

    String metricId = request.getParameter("metricId");
    if (StringUtils.isTrimmedEmpty(metricId)) {
        metricId = Metric.MEMORY.name();
    }

    int serverId = GameServer.getInstance().getServerId();
    String sServerId = request.getParameter("serverId");
    if (!StringUtils.isTrimmedEmpty(sServerId)) {
        serverId = Integer.parseInt(sServerId);
    }

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat dateFormatWithTimezone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z[zzzz]");

    String startTime = request.getParameter("startTime");
    if (StringUtils.isTrimmedEmpty(startTime)) {
        startTime = dateFormat.format(new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)));
    }
    String endTime = request.getParameter("endTime");
    if (StringUtils.isTrimmedEmpty(endTime)) {
        endTime = dateFormat.format(new Date(System.currentTimeMillis()));
    }

    Date startDate = dateFormat.parse(startTime);
    Date endDate = dateFormat.parse(endTime);

    String values = null;
    String maxValues = null;
    String minValues = null;

    long hoursInterval = TimeUnit.HOURS.convert(endDate.getTime() - startDate.getTime(), TimeUnit.MILLISECONDS);
    if (hoursInterval > SWITCH_TO_SPARSE_AFTER_MAX_HOURS) {
        List<MetricStat> metricStat = MetricsManager.getInstance().getMetricStatValues(
                Metric.valueOf(metricId), serverId, startDate.getTime(), endDate.getTime());

        values = "[[";
        maxValues = "[[";
        minValues = "[[";
        int i = 0;
        for (MetricStat metric : metricStat) {
            values += metric.getStatTime() + ", " + metric.getAverageValue();
            maxValues += metric.getMaxValueTime() + ", " + metric.getMaxValue();
            minValues += metric.getMinValueTime() + ", " + metric.getMinValue();
            i++;
            values += i < metricStat.size() ? "], [" : "";
            maxValues += i < metricStat.size() ? "], [" : "";
            minValues += i < metricStat.size() ? "], [" : "";
        }
        values += "]]";
        maxValues += "]]";
        minValues += "]]";
    } else {
        values = "[[";
        List<Pair<Long, Long>> metricValues = MetricsManager.getInstance().getMetricValues(
                Metric.valueOf(metricId), serverId, startDate.getTime(), endDate.getTime());

        int i = 0;
        for (Pair<Long, Long> metric : metricValues) {
            values += metric.getKey() + ", " + metric.getValue();
            i++;
            values += i < metricValues.size() ? "], [" : "";
        }
        values += "]]";
    }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>Metrics Graph</title>
    <link href="/support/metrics/css/metrics.css" rel="stylesheet" type="text/css">
    <script type="text/javascript" src="/DatePicker/metrics.js"></script>
    <!--[if lte IE 8]>
    <script language="javascript" type="text/javascript" src="/support/metrics/flot/excanvas.min.js"></script><![endif]-->
    <script language="javascript" type="text/javascript" src="/support/metrics/flot/jquery.js"></script>
    <script language="javascript" type="text/javascript" src="/support/metrics/flot/jquery.flot.js"></script>
    <script language="javascript" type="text/javascript" src="/support/metrics/flot/jquery.flot.time.js"></script>
    <script type="text/javascript">

        $(function () {
            var options = {xaxis: {mode: "time"}, yaxis: {tickDecimals: 2}};

            var curDay = 0;
            options.xaxis.tickFormatter = function (value, axis) {
                var date = new Date(value);
                if (curDay != date.getDay()) {
                    curDay = date.getDay();
                    return $.plot.formatDate(date, "%b %d %H:%M");
                } else {
                    return $.plot.formatDate(date, "%H:%M");
                }
            };

            options.yaxis.tickFormatter = function (value, axis) {
                if ($('[name="metricId"] :selected').text().trim() == 'MEMORY') {
                    return Number(value / (1024 * 1024)).toFixed(axis.tickDecimals) + " MB";
                } else {
                    return value;
                }
            };

            var data = [];

            var d = {data: <%=values%>};
            data.push(d);

            <%
                if(hoursInterval > SWITCH_TO_SPARSE_AFTER_MAX_HOURS) {
            %>
            d['label'] = "average";
            var maxValues = {label: "max", data: <%=maxValues%>};
            data.push(maxValues);
            var minValues = {label: "min", data: <%=minValues%>};
            data.push(minValues);
            <%
                }
            %>

            $.plot("#placeholder", data, options);

            $("#whole").click(function () {
                $.plot("#placeholder", data, options);
            });
        });

    </script>
</head>
<body>

<div id="content">
    <table cellspacing="10" cellpadding="10" border="0" width="900" style="padding-left: 40px; padding-top: 10px;">
        <form method="post" action="/support/metrics/">
            <tr>
                <td>Metric:</td>
                <td>
                    <select name="metricId">
                        <%for (Metric metric : possibleMetrics) {%>
                        <option value="<%=metric.name()%>" <%=(metricId.equals(metric.name()) ? "selected" : "")%>><%=metric.name()%>
                        </option>
                        <%}%>
                    </select>
                </td>
                <td rowspan="3">
                    Server time: <%=dateFormatWithTimezone.format(System.currentTimeMillis())%><br>
                </td>
            </tr>
            <tr>
                <td>ServerId:</td>
                <td>
                    <select name="serverId">
                        <%for (GameServerConfig serverConfig : serverConfigs.values()) {%>
                        <option value="<%=serverConfig.getId()%>" <%=(serverId == serverConfig.getId() ? "selected" : "")%>><%=serverConfig.getId()%>
                        </option>
                        <%}%>
                    </select>
                </td>
            </tr>
            <tr>
                <td>From:</td>
                <td>
                    <input name="startTime" size="19" maxlength="19"
                           onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss'})" value="<%=startTime%>"/>
                </td>
            </tr>
            <tr>
                <td>To:</td>
                <td>
                    <input name="endTime" size="19" maxlength="19"
                           onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss'})" value="<%=endTime%>"/><br>
                </td>
                <td><input type="submit" value="Draw"></td>
            </tr>
        </form>
    </table>

    <div class="demo-container">
        <div id="placeholder" class="demo-placeholder" style="width:100%;height:600px"></div>
    </div>

</div>

</body>
</html>
