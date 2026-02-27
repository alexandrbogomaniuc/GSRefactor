<%@ page import="java.util.Map" %>
<%@ page import="com.abs.casino.common.cache.data.bonus.restriction.MassAwardRestriction" %>
<%@ page import="com.dgphoenix.casino.common.cache.MassAwardCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bonus.BaseMassAward" %>
<%@ page import="com.abs.casino.gs.managers.payment.bonus.mass.MassAwardBonusManager" %>
<%@ page import="com.abs.casino.gs.managers.payment.bonus.restriction.NoAwardRestriction" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%
    String responseStart = "<table border=\"1\"><tr><td>MassAwardId</td><td>Restriction</td><td>MassAward</td></tr>";
    String responseEnd = "</table>";
    Long massAwardId = null;
    if (request.getParameter("massAwardId") != null) {
        massAwardId = Long.parseLong(request.getParameter("massAwardId"));
    }
    MassAwardBonusManager massAwardBonusManager = ApplicationContextHelper.getBean(MassAwardBonusManager.class);
    if (massAwardId != null) {
        BaseMassAward massAward = MassAwardCache.getInstance().getById(massAwardId);
        if (massAward != null) {
            response.getWriter().write(responseStart);
            MassAwardRestriction restriction = massAwardBonusManager.getMassAwardRestriction(massAwardId);
            response.getWriter().write("<tr><td>" + massAward.getId() + "</td><td>");
            if (!(restriction instanceof NoAwardRestriction)) {
                response.getWriter().write(restriction.toString());
            }
            response.getWriter().write("</td><td>" + massAward + "</td></tr>");
            response.getWriter().write(responseEnd);
        } else {
            response.getWriter().write("MassAwardId " + massAwardId + " not found");
        }
    } else {
        Map<Long, BaseMassAward> massAwards = MassAwardCache.getInstance().getAllObjects();
        response.getWriter().write(responseStart);
        for (Map.Entry<Long, BaseMassAward> entry : massAwards.entrySet()) {
            MassAwardRestriction restriction = massAwardBonusManager.getMassAwardRestriction(entry.getKey());
            response.getWriter().write("<tr><td>" + entry.getKey() + "</td><td>");
            if (!(restriction instanceof NoAwardRestriction)) {
                response.getWriter().write(restriction.toString());
            }
            response.getWriter().write("</td><td>" + entry.getValue() + "</td></tr>");
        }
        response.getWriter().write(responseEnd);
    }
%>
