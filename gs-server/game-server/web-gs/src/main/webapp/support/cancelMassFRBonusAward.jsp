<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.services.bonus.CancelMassFRBonusAward" %>
<%@ page import="org.apache.commons.lang.exception.ExceptionUtils" %>
<%--
  Author: svvedenskiy
  Date: 7/24/18
--%>
<%
    /*
        Parameters:
            massAwardId - long - mass award ID;
            cancelCreatedFRBonus - (optional) true|false - if true search and cancel created FRBonus for players.
     */

    _response = response;

    String strMassAwardId = request.getParameter("massAwardId");
    if (StringUtils.isTrimmedEmpty(strMassAwardId)) {
        _response.getWriter().write("parameter 'massAwardId' can't be empty");
        return;
    }

    boolean cancelCreatedFRBonus = false;
    if ("true".equalsIgnoreCase(request.getParameter("cancelCreatedFRBonus"))) {
        cancelCreatedFRBonus = true;
    }

    _response.getWriter().write("massAwardId: " + strMassAwardId + "<br/>");
    _response.getWriter().write("cancelCreatedFRBonus: " + cancelCreatedFRBonus + "<br/>");

    _response.getWriter().write("<br/>");

    long massAwardId = Long.parseLong(strMassAwardId);

    try {
        CancelMassFRBonusAward.CancelResult result = new CancelMassFRBonusAward().cancelMassAward(massAwardId, cancelCreatedFRBonus);

        _response.getWriter().write(
                (result.isBaseMassAwardCanceled() ?
                        "BaseMassAward has been canceled" : "Active BaseMassAward not found")
                        + "<br/>");

        _response.getWriter().write(
                (result.isDelayedMassAwardRemoved() ?
                        "DelayedMassAward has been removed" : "DelayedMassAward does not exist")
                        + "<br/>");

        if (cancelCreatedFRBonus) {
            _response.getWriter().write("Number FRBonus canceled: " + result.getNumberFRBonusCanceled() + "<br/>");
        }

    } catch (Throwable e) {
        String str = ExceptionUtils.getStackTrace(e);
        str = str.replaceAll("\n", "<br/>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

        _response.getWriter().write(str);
    }
%>

<%!
    HttpServletResponse _response = null;
%>
