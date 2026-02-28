<%@ page import="com.dgphoenix.casino.actions.api.bonus.BonusForm" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.abs.casino.common.web.bonus.CBonus" %>
<%@ page import="org.apache.struts.action.ActionErrors" %>
<%@ page import="org.apache.struts.Globals" %>
<%@ page import="org.apache.struts.action.ActionMessage" %>
<%@ page import="com.dgphoenix.casino.actions.api.bonus.AbstractBonusAction"%>
<%@ page contentType="application/json;charset=UTF-8"%>


<%
    BonusForm form = null;
    Enumeration<String> attributes =  request.getAttributeNames();
    while (attributes.hasMoreElements()) {
        String attribute = attributes.nextElement();
        if (attribute.endsWith("Form")) {
            form = (BonusForm)request.getAttribute(attribute);
        }
    }

    if (form != null) {
        Map<String, String> inParams = new HashMap<>();
        for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
            inParams.put(parameter.getKey().toUpperCase(), parameter.getValue()[0]);
        }
        Map<String, Object> outParams = new HashMap<>();
        outParams.put(CBonus.RESULT_TAG, CBonus.RESULT_ERROR);
        ActionErrors errors = (ActionErrors)request.getAttribute(Globals.ERROR_KEY);
        outParams.put(CBonus.DESCRIPTION_TAG, ((ActionMessage)errors.get("valid_error").next()).getKey());
        outParams.put(CBonus.CODE_TAG, ((ActionMessage)errors.get("valid_error_code").next()).getKey());

        AbstractBonusAction.buildResponseJSON(response, inParams, outParams, form);

        response.getWriter().flush();
    }
%>