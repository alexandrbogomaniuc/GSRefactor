<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.Coin" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameConstants" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.GameVariableType" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<script>
    function doFilter() {
        var banks = document.getElementById("banks").value;
        var currencies = document.getElementById("currencies").value;
        var mode = document.getElementById("mode").value;
        var properties = document.getElementById("properties").value;
        var editmode = document.getElementById("editmode").value;

        var href = "/support/GameConfig/getGamesConfigByBanks.jsp?" +
                ((banks != "") ? ("banks" + "=" + banks + "&") : "") +
                ((currencies != "") ? ("currencies" + "=" + currencies + "&") : "") +
                ((mode != "") ? ("mode" + "=" + mode + "&") : "") +
                ((properties != "") ? ("properties" + "=" + properties + "&") : "") +
                ((editmode != "") ? ("editmode" + "=" + editmode + "&") : "");

        href = href.substring(0, href.length - 1);

        location.href = href;
    }
</script>

<%!
    boolean editmode = false;
    Set<Long> frbGames = BaseGameInfoTemplateCache.getInstance().getFrbGames();

    HashMap<Long, String> chipValues = new HashMap<Long, String>() {{
        put(5L, "0,25|1|5|25|100|500");
        put(9L, "1|5|25|100|500");
        put(10L, "1|5|25|100|500");
        put(12L, "1|5|25|100|500");
        put(13L, "1|5|25|100|500");
        put(43L, "1|5|25|100|500");
        put(44L, "1|5|25|100|500");
        put(48L, "1|5|25|100|500");
        put(63L, "1|5|25|100|500");
        put(64L, "1|5|25|100|500");
        put(79L, "0,25|1|5|25|100|500");
        put(74L, "1|5|25|100|500");
        put(81L, "1|5|25|100|500");
        put(82L, "1|5|25|100|500");
        put(94L, "1|5|25|100|500");
        put(105L, "1|5|25|100|500");
        put(127L, "1|5|25|100|500");
        put(125L, "1|5|25|100|500");
        put(124L, "1|5|25|100|500");
        put(129L, "1|5|25|100|500");
        put(131L, "1|5|25|100|500");
        put(150L, "0,02|0,1|0,2");
        put(171L, "0,25|1|5|25|100|500");
        put(170L, "0,25|1|5|25|100|500");
        put(169L, "1|5|25|100|500");
        put(168L, "1|5|25|100|500");
        put(163L, "1|5|25|100|500");
        put(161L, "1|5|25|100|500");
        put(167L, "1|5|25|100|500");
        put(166L, "1|5|25|100|500");
        put(165L, "1|5|25|100|500");
        put(191L, "1|5|25|100|500");
        put(203L, "1|5|25|100|500");
        put(197L, "0,25|1|5|25|100|500");
        put(198L, "0,25|1|5|25|100|500");
        put(195L, "1|5|25|100|500");
        put(209L, "0,5|1|5|25|100|500");
        put(244L, "1|5|25|100|500");
        put(241L, "1|5|25|100|500");
        put(275L, "0,5|1|5|25|100|250|500");
        put(272L, "1|5|25|100|500");
        put(278L, "1|5|25|100|250");
        put(276L, "0,5|1|5|25|100|250|500");
        put(283L, "1|5|25|100|500");
        put(282L, "1|5|25|100|500");
        put(281L, "1|5|25|100|500");
        put(287L, "1|5|25|100|500");
        put(286L, "1|5|25|100|500");
        put(285L, "1|5|25|100|500");
        put(284L, "1|5|25|100|500");
        put(261L, "1|5|25|100|500");
        put(271L, "1|5|25|100|500");
        put(366L, "1|5|25|100|500");
        put(445L, "1|5|25|100|500");
        put(446L, "1|5|25|100|500");
        put(450L, "1|5|25|100|500");
        put(449L, "1|5|25|100|500");
        put(448L, "1|5|25|100|500");
        put(371L, "0,5|1|5|25|100|250|500");
        put(447L, "1|5|25|100|500");
        put(289L, "1|5|25|100|500");
        put(290L, "1|5|25|100|500");
    }};

    final String PARAM_MODE_COINS = "coins";
    final String PARAM_MODE_FRB = "frb";
    final String PARAM_MODE_LIMIT = "limit";
    final String PARAM_MODE_DEFCOIN = "defcoin";
    final String PARAM_MODE_CHIPS = "chips";
    final String PARAM_MODE_ALL = "all";

    final String[] modes = {PARAM_MODE_COINS, PARAM_MODE_DEFCOIN, PARAM_MODE_FRB, PARAM_MODE_CHIPS, PARAM_MODE_LIMIT};
    final List<String> parameters = Arrays.asList("COINS", "DEFCOIN", "FRB_COIN", "CHIPVALUES", "LIMITS");

    BaseGameInfoTemplateCache instance = BaseGameInfoTemplateCache.getInstance();

    Map<String, Map<String, Set<Long>>> mapGamesParameters = new HashMap<String, Map<String, Set<Long>>>(); // Parameter -> Map of Games

    String getGameParameter(BankInfo bankInfo, IBaseGameInfo gameInfoShared, String parameter) {
        switch (parameter) {
            case "COINS": {
                List<Coin> coins = gameInfoShared.getCoins();
                boolean def = false;
                if (coins == null || coins.isEmpty()) {
                    coins = bankInfo.getCoins();
                    def = true;
                }

                LinkedList<Double> sortedCoins = new LinkedList<>();
                for (Coin coin : coins) {
                    sortedCoins.add(coin.getValue() / 100.0);
                }
                Collections.sort(sortedCoins);

                StringBuilder result = new StringBuilder();
                for (Double coin : sortedCoins) {
                    result.append(coin).append("   ");
                }

                if (def)
                    result.append("(Default) ");
                result.deleteCharAt(result.length() - 1);
                return result.toString();

            }
            case "LIMITS": {
                return (gameInfoShared.getLimit() != null ? gameInfoShared.getLimit().toString() : bankInfo.getLimit().toString());
            }
            case "CHIPVALUES": {
                if (gameInfoShared.getChipValues() == null || gameInfoShared.getChipValues().isEmpty()) {
                    return chipValues.get(gameInfoShared.getId()) + "(default)";
                }
                return gameInfoShared.getChipValues();
            }
            case BaseGameConstants.KEY_FRB_COIN: {
                return gameInfoShared.getProperty(parameter);
            }
            default: {
                return gameInfoShared.getProperty(parameter);
            }
        }
    }

    void addGameToMap(BankInfo bankInfo, IBaseGameInfo game, String parameter) {
        Map<String, Set<Long>> map = mapGamesParameters.get(parameter);
        if (map == null) {
            map = new HashMap<String, Set<Long>>();
            mapGamesParameters.put(parameter, map);
        }

        StringBuilder key = new StringBuilder();
        String value = getGameParameter(bankInfo, game, parameter);
        key.append(game.getBankId()).append("+").append(game.getCurrency().getCode()).append("+").append(value);
        Set<Long> gameSets = map.get(key.toString());
        if (gameSets == null) {
            gameSets = new HashSet<>();
        }
        gameSets.add(game.getId());
        map.put(key.toString(), gameSets);
    }

    Map<String, Long> getSortedGameMap(Set<Long> gameIds) {
        Map<String, Long> sortedMap = new TreeMap<String, Long>();

        for (Long gameId : gameIds) {
            String title = instance.getBaseGameInfoTemplateById(gameId).getTitle();
            sortedMap.put(title, gameId);
        }
        return sortedMap;
    }

    String buildGameList(String parameter, int idx, String bank, String currency, Set<Long> gameIds) {
        StringBuilder games = new StringBuilder();
        games.append("<button id=\"b_" + parameter + idx + "\" onclick=\"showDiv(d_" + parameter + idx + ",b_" + parameter + idx + ")\">Show</button>");
        games.append("<div style=\"background-color: lightgray; z-index: 100;  position: absolute; left: 60%\" hidden id=\"d_" + parameter + idx + "\">");
        games.append("<ul style=\"\">");

        Map<String, Long> sortedGameMap = getSortedGameMap(gameIds);

        for (Map.Entry<String, Long> entryGame : sortedGameMap.entrySet()) {
            String title = entryGame.getKey();
            Long gameId = entryGame.getValue();
            games.append("<li style=\"\">");

            StringBuilder gameName = new StringBuilder();
            gameName.append(title).append("(" + gameId + ")");
            if (editmode) {
                String link = "/support/loadgameinfo.do?bankId=" + bank + "&curCode=" + currency + "&gameId=" + gameId;
                games.append("<a href=\"" + link + " \">" + gameName + "</a>");
            } else
                games.append(title);

            games.append("</li>");
        }
        games.append("<ul>");
        games.append("</div>");

        return games.toString();
    }
%>

<%
    mapGamesParameters.clear();
    String banksFromRequest = request.getParameter("banks");
    if (banksFromRequest == null || banksFromRequest.isEmpty()) {
        banksFromRequest = "";
    }

    String strMode = request.getParameter("mode");
    if (strMode == null || strMode.isEmpty()) {
        strMode = "all";
    }

    String strProps = request.getParameter("properties");
    if (strProps == null || strProps.isEmpty()) {
        strProps = "";
    }

    String strCurrencies = request.getParameter("currencies");
    if (strCurrencies == null || strCurrencies.isEmpty()) {
        strCurrencies = "";
    }

    if ("true".equalsIgnoreCase(request.getParameter("editmode"))) {
        editmode = true;
    }
%>

Банки: <INPUT style="width: 10%" type="text" id="banks" value="<%=banksFromRequest%>" title="271,272,583... or empty = 271"/>
Валюты: <INPUT style="width: 10%" type="text" id="currencies" value="<%=strCurrencies%>" title="EUR,USD,MBC... or empty = <ALL>"/>
Режим: <SELECT id="mode" style="width: 5%">
    <OPTION value="<%=PARAM_MODE_ALL%>"     <% if (strMode.equals(PARAM_MODE_ALL)) { %> selected="selected" <% } %>><%=PARAM_MODE_ALL%>
    </OPTION>
    <OPTION value="<%=PARAM_MODE_COINS%>"   <% if (strMode.equals(PARAM_MODE_COINS)) { %> selected="selected" <% } %>><%=PARAM_MODE_COINS%>
    </OPTION>
    <OPTION value="<%=PARAM_MODE_FRB%>"     <% if (strMode.equals(PARAM_MODE_FRB)) { %> selected="selected" <% } %>><%=PARAM_MODE_FRB%>
    </OPTION>
    <OPTION value="<%=PARAM_MODE_LIMIT%>"   <% if (strMode.equals(PARAM_MODE_LIMIT)) { %> selected="selected" <% } %>><%=PARAM_MODE_LIMIT%>
    </OPTION>
    <OPTION value="<%=PARAM_MODE_DEFCOIN%>" <% if (strMode.equals(PARAM_MODE_DEFCOIN)) { %> selected="selected" <% } %>><%=PARAM_MODE_DEFCOIN%>
    </OPTION>
    <OPTION value="<%=PARAM_MODE_CHIPS%>"   <% if (strMode.equals(PARAM_MODE_CHIPS)) { %> selected="selected" <% } %>><%=PARAM_MODE_CHIPS%>
    </OPTION>
</SELECT>
Дополнительные параметры: <INPUT style="width: 20%" type="text" id="properties" value="<%=strProps%>" title="KEY_ACS_ENABLED,LGA_APPROVED..."/>
Редактирование: <SELECT id="editmode" style="width: 60px">
    <OPTION value="true"  <% if (editmode) { %> selected="selected" <% } %>>true</OPTION>
    <OPTION value="false" <% if (!editmode) { %> selected="selected" <% } %>>false</OPTION>
</SELECT>

<INPUT style="width: 80px" type="button" value="ОК" onclick="doFilter()"/>


<%
    String server = request.getHeader("Host");

    String mode = "all"; // coins | frb | limit | defcoin
    if (!strMode.isEmpty()) {
        if (strMode.equals("coins") || strMode.equals("frb") || strMode.equals("limit") || strMode.equals("defcoin") || strMode.equals("chips"))
            mode = strMode;
    }

    ArrayList<String> additionalProperties = null;
    if (!strProps.isEmpty()) {
        strProps = strProps.replaceAll(" ", "");
        String[] strPropertiesArray = strProps.split(",");

        additionalProperties = new ArrayList<>();
        for (String strProperty : strPropertiesArray) {
            if (!additionalProperties.contains(strProperty) && !parameters.contains(strProperty)) {
                additionalProperties.add(strProperty);
            }
        }
    }

    List<String> filterCurrencies = null;
    if (!strCurrencies.isEmpty()) {
        strCurrencies = strCurrencies.replaceAll(" ", "");
        filterCurrencies = Arrays.asList(strCurrencies.split(","));
    }

    Set<Long> bankIds;
    if (banksFromRequest.isEmpty()) {
        bankIds = new HashSet<>();
        bankIds.add(271l);
    } else {
        bankIds = new HashSet<>();
        StringTokenizer st = new StringTokenizer(banksFromRequest, ",");
        while (st.hasMoreTokens()) {
            bankIds.add(Long.parseLong(st.nextToken()));
        }
    }

    String casino = request.getParameter("casino");
    if (casino != null) {
        List<Long> bankIds_ = SubCasinoCache.getInstance().get(Long.parseLong(casino)).getBankIds();
        bankIds = new HashSet<>(bankIds_);
    }

    for (Long bankId : bankIds) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        List<Currency> currencies = bankInfo.getCurrencies();

        for (Currency currency : currencies) {
            if (filterCurrencies != null && !filterCurrencies.contains(currency.getCode())) continue;
            Map<Long, IBaseGameInfo> games = BaseGameCache.getInstance().getAllGameInfosAsMap(bankId, currency);
            for (Map.Entry<Long, IBaseGameInfo> gameInfoEntry : games.entrySet()) {
                Long gameId = gameInfoEntry.getKey();
                IBaseGameInfo gameInfoShared = gameInfoEntry.getValue();
                BaseGameInfoTemplate templateById = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
                if (templateById == null) {
                    System.out.println("Cannot load template for game: " + gameId);
                    continue;
                }
                String title = templateById.getTitle();
                boolean isEmpty = title == null || title.equals("null");
                if (isEmpty) continue;

                if (gameInfoShared.getVariableType().equals(GameVariableType.COIN)) {
                    if (mode.equals(PARAM_MODE_ALL) || mode.equals(PARAM_MODE_COINS)) {
                        addGameToMap(bankInfo, gameInfoShared, "COINS");
                    }

                    if (mode.equals(PARAM_MODE_ALL) || mode.equals(PARAM_MODE_DEFCOIN)) {
                        addGameToMap(bankInfo, gameInfoShared, BaseGameConstants.KEY_DEFAULT_COIN);
                    }

                    if (mode.equals(PARAM_MODE_ALL) || mode.equals(PARAM_MODE_FRB)) {
                        if (frbGames.contains(gameId)) {
                            addGameToMap(bankInfo, gameInfoShared, BaseGameConstants.KEY_FRB_COIN);
                        }
                    }
                } else {
                    if (mode.equals(PARAM_MODE_ALL) || mode.equals(PARAM_MODE_LIMIT)) {
                        addGameToMap(bankInfo, gameInfoShared, "LIMITS");
                    }

                    if (mode.equals(PARAM_MODE_ALL) || mode.equals(PARAM_MODE_CHIPS)) {
                        addGameToMap(bankInfo, gameInfoShared, "CHIPVALUES");
                    }
                }

                if (additionalProperties != null && !additionalProperties.isEmpty()) {
                    for (String property : additionalProperties) {
                        addGameToMap(bankInfo, gameInfoShared, property);
                    }
                }
            }
        }
    }

%>

<% for (int i = 0; i < modes.length; i++) {
    String parameter = parameters.get(i);
    if (mapGamesParameters.get(parameter) == null) continue;

    if (mode.equals(PARAM_MODE_ALL) || mode.equals(modes[i])) {%>
<h1>Table of "<%=parameter%>"</h1>
<table class="sort" align="center">
    <thead>
    <tr>
        <td>Bank</td>
        <td>Currency</td>
        <td><%=parameter%>
        </td>
        <td>Count of games</td>
        <td>List of games</td>
    </tr>
    </thead>
    <tbody>
    <%
        int idx = 0;
        if (mapGamesParameters.get(parameter) != null) {
            for (Map.Entry<String, Set<Long>> gamesEntry : mapGamesParameters.get(parameter).entrySet()) {
                StringTokenizer st = new StringTokenizer(gamesEntry.getKey(), "+");
                if (st.countTokens() != 3) {
                    continue;
                }
                String bank = st.nextToken();
                String currency = st.nextToken();
                String value = st.nextToken();
                int count = gamesEntry.getValue().size();
                String games = buildGameList(parameter, idx, bank, currency, gamesEntry.getValue());
                idx++;
    %>
    <tr>
        <td onclick="closeAllDiv()"><%=bank%>
        </td>
        <td onclick="closeAllDiv()"><%=currency%>
        </td>
        <td onclick="closeAllDiv()"><%=value%>
        </td>
        <td onclick="closeAllDiv()"><%=count%>
        </td>
        <td><%=games%>
        </td>
    </tr>
    <%
            }
        }
    %>
    </tbody>
</table>
<%
        }
    }
%>

<%
    if (additionalProperties != null && !additionalProperties.isEmpty()) {
        for (String parameter : additionalProperties) {%>
<h1>Table of "<%=parameter%>"</h1>
<table class="sort" align="center">
    <thead>
    <tr>
        <td>Bank</td>
        <td>Currency</td>
        <td><%=parameter%>
        </td>
        <td>Count of games</td>
        <td>List of games</td>
    </tr>
    </thead>
    <tbody>
    <%
        int idx = 0;
        for (Map.Entry<String, Set<Long>> gamesEntry : mapGamesParameters.get(parameter).entrySet()) {
            StringTokenizer st = new StringTokenizer(gamesEntry.getKey(), "+");
            if (st.countTokens() != 3) {
                continue;
            }
            String bank = st.nextToken();
            String currency = st.nextToken();
            String value = st.nextToken();
            int count = gamesEntry.getValue().size();
            String games = buildGameList(parameter, idx, bank, currency, gamesEntry.getValue());
            idx++;
    %>
    <tr>
        <td onclick="closeAllDiv()"><%=bank%>
        </td>
        <td onclick="closeAllDiv()"><%=currency%>
        </td>
        <td onclick="closeAllDiv()"><%=value%>
        </td>
        <td onclick="closeAllDiv()"><%=count%>
        </td>
        <td><%=games%>
        </td>
    </tr>
    <%}%>
    </tbody>
</table>
<%}%>
<%}%>

<style type="text/css">
    table.sort {
        border-spacing: 0.1em;
        margin-bottom: 1em;
        margin-top: 1em
    }

    table.sort td {
        border: 1px solid #CCCCCC;
        padding: 0.3em 1em
    }

    table.sort thead td {
        cursor: pointer;
        cursor: hand;
        font-weight: bold;
        text-align: center;
        vertical-align: middle
    }

    table.sort thead td.curcol {
        background-color: #999999;
        color: #FFFFFF
    }
</style>


<script type="text/javascript">

    initial_sort_id = 3;
    initial_sort_up = 1;

    function closeAllDiv() {
        var divs = document.getElementsByTagName("div");
        for (var i = 0; i < divs.length; i++) {
            divs[i].hidden = true;
        }
    }

    function showDiv(divid, butttoId) {
        closeAllDiv();
//        var elementById = document.getElementById('id');
        if (divid.hidden) {
            divid.hidden = false;
//            butttoId.innerHTML = "Hide"
        } else {
            divid.hidden = true;
//            butttoId.innerHTML = "Show"
        }
    }


    var img_dir = "/i/";
    var sort_case_sensitive = false;

    function _sort(a, b) {
        var a = a[0];
        var b = b[0];
        var _a = (a + '').replace(/,/, '.');
        var _b = (b + '').replace(/,/, '.');
        if (parseFloat(_a) && parseFloat(_b)) return sort_numbers(parseFloat(_a), parseFloat(_b));
        else if (!sort_case_sensitive) return sort_insensitive(a, b);
        else return sort_sensitive(a, b);
    }

    function sort_numbers(a, b) {
        return a - b;
    }

    function sort_insensitive(a, b) {
        var anew = a.toLowerCase();
        var bnew = b.toLowerCase();
        if (anew < bnew) return -1;
        if (anew > bnew) return 1;
        return 0;
    }

    function sort_sensitive(a, b) {
        if (a < b) return -1;
        if (a > b) return 1;
        return 0;
    }

    function getConcatenedTextContent(node) {
        var _result = "";
        if (node == null) {
            return _result;
        }
        var childrens = node.childNodes;
        var i = 0;
        while (i < childrens.length) {
            var child = childrens.item(i);
            switch (child.nodeType) {
                case 1: // ELEMENT_NODE
                case 5: // ENTITY_REFERENCE_NODE
                    _result += getConcatenedTextContent(child);
                    break;
                case 3: // TEXT_NODE
                case 2: // ATTRIBUTE_NODE
                case 4: // CDATA_SECTION_NODE
                    _result += child.nodeValue;
                    break;
                case 6: // ENTITY_NODE
                case 7: // PROCESSING_INSTRUCTION_NODE
                case 8: // COMMENT_NODE
                case 9: // DOCUMENT_NODE
                case 10: // DOCUMENT_TYPE_NODE
                case 11: // DOCUMENT_FRAGMENT_NODE
                case 12: // NOTATION_NODE
                    // skip
                    break;
            }
            i++;
        }
        return _result;
    }

    function sort(e) {
        var el = window.event ? window.event.srcElement : e.currentTarget;
        while (el.tagName.toLowerCase() != "td") el = el.parentNode;
        var a = new Array();
        var name = el.lastChild.nodeValue;
        var dad = el.parentNode;
        var table = dad.parentNode.parentNode;
        var up = table.up;
        var node, arrow, curcol;
        for (var i = 0; (node = dad.getElementsByTagName("td").item(i)); i++) {
            if (node.lastChild.nodeValue == name) {
                curcol = i;
                if (node.className == "curcol") {
                    arrow = node.firstChild;
                    table.up = Number(!up);
                } else {
                    node.className = "curcol";
                    arrow = node.insertBefore(document.createElement("img"), node.firstChild);
                    table.up = 0;
                }
                arrow.src = img_dir + table.up + ".gif";
                arrow.alt = "";
            } else {
                if (node.className == "curcol") {
                    node.className = "";
                    if (node.firstChild) node.removeChild(node.firstChild);
                }
            }
        }
        var tbody = table.getElementsByTagName("tbody").item(0);
        for (var i = 0; (node = tbody.getElementsByTagName("tr").item(i)); i++) {
            a[i] = new Array();
            a[i][0] = getConcatenedTextContent(node.getElementsByTagName("td").item(curcol));
            a[i][1] = getConcatenedTextContent(node.getElementsByTagName("td").item(1));
            a[i][2] = getConcatenedTextContent(node.getElementsByTagName("td").item(0));
            a[i][3] = node;
        }
        a.sort(_sort);
        if (table.up) a.reverse();
        for (var i = 0; i < a.length; i++) {
            tbody.appendChild(a[i][3]);
        }
    }

    function init(e) {
        if (!document.getElementsByTagName) return;

        for (var j = 0; (thead = document.getElementsByTagName("thead").item(j)); j++) {
            var node;
            for (var i = 0; (node = thead.getElementsByTagName("td").item(i)); i++) {
                if (node.addEventListener) node.addEventListener("click", sort, false);
                else if (node.attachEvent) node.attachEvent("onclick", sort);
                node.title = "";
            }
            thead.parentNode.up = 0;

            if (typeof (initial_sort_id) != "undefined") {
                td_for_event = thead.getElementsByTagName("td").item(initial_sort_id);
                if (document.createEvent) {
                    var evt = document.createEvent("MouseEvents");
                    evt.initMouseEvent("click", false, false, window, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, td_for_event);
                    td_for_event.dispatchEvent(evt);
                } else if (td_for_event.fireEvent) td_for_event.fireEvent("onclick");
                if (typeof (initial_sort_up) != "undefined" && initial_sort_up) {
                    if (td_for_event.dispatchEvent) td_for_event.dispatchEvent(evt);
                    else if (td_for_event.fireEvent) td_for_event.fireEvent("onclick");
                }
            }
        }
    }

    var root = window.addEventListener || window.attachEvent ? window : document.addEventListener ? document : null;
    if (root) {
        if (root.addEventListener) root.addEventListener("load", init, false);
        else if (root.attachEvent) root.attachEvent("onload", init);
    }
    //-->
</script>