<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.dgphoenix.casino.common.cache.ServerConfigsCache" %>
<%@ taglib uri="/WEB-INF/struts-tiles.tld" prefix="template" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/casino.tld" prefix="casino" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>

<%=ServerConfigsCache.getInstance().getThisServerId()%>
