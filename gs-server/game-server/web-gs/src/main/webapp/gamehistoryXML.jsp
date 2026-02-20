<%@ taglib prefix="bean" uri="http://struts.apache.org/tags-bean" %>
<%@ taglib prefix="html" uri="http://struts.apache.org/tags-html" %>
<%@ page import="com.dgphoenix.casino.web.history.GameHistoryListAction" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dgphoenix.casino.common.util.xml.xmlwriter.XmlWriter" %>
<%@ page import="com.dgphoenix.casino.common.util.xml.xmlwriter.Attribute" %>
<%@ page import="com.dgphoenix.casino.web.history.GameHistoryListEntry" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.apache.struts.action.ActionMessage" %>
<%@ page import="com.dgphoenix.casino.common.configuration.messages.MessageManager" %>
<%@ page import="com.dgphoenix.casino.system.configuration.GameServerConfiguration" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringBuilderWriter" %>
<%@ page contentType="text/xml;charset=UTF-8" language="java" %>
<%!
    public static final String ROOT_TAG_GAME_HISTORY = GameServerConfiguration.getInstance().getBrandApiRootShortTagName() +
            "_game_history";

    public static final String ATTR_COUNT = "count";
    public static final String ATTR_PAGE = "page";
    public static final String ATTR_ITEMS_PER_PAGE = "itemsPerPage";
    public static final String ATTR_SESSION_ID = "sessionId";
    public static final String ATTR_GAME_ID = "gameId";
    public static final String ATTR_START_DAY = "startDay";
    public static final String ATTR_START_MONTH = "startMonth";
    public static final String ATTR_START_YEAR = "startYear";
    public static final String ATTR_END_DAY = "endDay";
    public static final String ATTR_END_MONTH = "endMonth";
    public static final String ATTR_END_YEAR = "endYear";

    public static final String TAG_STATUS = "status";
    public static final String TAG_ERROR_DESC = "error_desc";
    public static final String TAG_SESSIONS = "sessions";
    public static final String TAG_GAME_SESSION = "game_session";
    public static final String TAG_GAME_NAME = "game_name";
    public static final String TAG_START_DATE = "start_date";
    public static final String TAG_END_DATE = "end_date";
    public static final String TAG_VAB_URL = "vab_url";
    public static final String TAG_INCOME = "income";
    public static final String TAG_PAYOUT = "payout";

    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
%>
<bean:define id="form" name="GameHistoryXMLListForm" type="com.dgphoenix.casino.web.history.GameHistoryXMLListForm"/>
<%
    StringBuilderWriter stringWriter = new StringBuilderWriter();
    // generate XML response
    XmlWriter xmlWriter = new XmlWriter(stringWriter);
    xmlWriter.header();

    Attribute[] attributes = new Attribute[11];
    attributes[0] = new Attribute(ATTR_COUNT, String.valueOf(form.getLastPage()));
    attributes[1] = new Attribute(ATTR_PAGE, String.valueOf(form.getPage()));
    attributes[2] = new Attribute(ATTR_ITEMS_PER_PAGE, String.valueOf(form.getItemsPerPage()));
    attributes[3] = new Attribute(ATTR_SESSION_ID, String.valueOf(form.getSessionId()));
    attributes[4] = new Attribute(ATTR_GAME_ID, String.valueOf(form.getGameId() == -1L ? 0 : form.getGameId()));
    attributes[5] = new Attribute(ATTR_START_DAY, String.valueOf(form.getStartDay()));
    attributes[6] = new Attribute(ATTR_START_MONTH, String.valueOf(form.getStartMonth()));
    attributes[7] = new Attribute(ATTR_START_YEAR, String.valueOf(form.getStartYear()));
    attributes[8] = new Attribute(ATTR_END_DAY, String.valueOf(form.getEndDay()));
    attributes[9] = new Attribute(ATTR_END_MONTH, String.valueOf(form.getEndMonth()));
    attributes[10] = new Attribute(ATTR_END_YEAR, String.valueOf(form.getEndYear()));

    xmlWriter.startNode(ROOT_TAG_GAME_HISTORY, attributes);

    if (form.getErrors().isEmpty()) {
        xmlWriter.node(TAG_STATUS, STATUS_OK);
        xmlWriter.startNode(TAG_SESSIONS);

        List<GameHistoryListEntry> gameList = (List<GameHistoryListEntry>) request.getAttribute(GameHistoryListAction.GAME_HISTORY_LIST);
        for (GameHistoryListEntry game : gameList) {
            xmlWriter.startNode(TAG_GAME_SESSION);

            xmlWriter.node(TAG_GAME_NAME, game.getLocalizedGameName());
            xmlWriter.node(TAG_START_DATE, game.getStartDate());
            xmlWriter.node(TAG_END_DATE, game.getEndDate());
            xmlWriter.node(TAG_VAB_URL, game.getHistoryUrl());
            xmlWriter.node(TAG_INCOME, game.getIncome());
            xmlWriter.node(TAG_PAYOUT, game.getPayout());

            xmlWriter.endNode(TAG_GAME_SESSION);
        }
        xmlWriter.endNode(TAG_SESSIONS);
    } else {
        xmlWriter.node(TAG_STATUS, STATUS_ERROR);
        for (Iterator error = form.getErrors().get("history"); error.hasNext(); ) {
            xmlWriter.node(
                    TAG_ERROR_DESC,
                    MessageManager.getInstance().getApplicationMessage(((ActionMessage) error.next()).getKey())
            );
        }
    }
    xmlWriter.endNode(ROOT_TAG_GAME_HISTORY);

    response.getWriter().write(stringWriter.toString());
    response.getWriter().flush();
%>