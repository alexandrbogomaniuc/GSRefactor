<%@ page import="java.util.*" %>

<%@include file="GameTemplate.jsp" %>
<script type="text/javascript" src="../js/scriptjava.js"></script>

<script type="text/javascript">
    function showHide(name) {
        var gameElement = document.getElementsByClassName(name);

        for (var i = 0; i < gameElement.length; i++) {
            if (gameElement[i].style.display != "table-row")
                gameElement[i].style.display = "table-row";
            else
                gameElement[i].style.display = "none";
        }
    }

    function sendProperty(clusterAddress, lineId, id, key, value) {
        var address = clusterAddress + "/support/templateManager/setProperty.jsp";

        $$a({
            type: 'post',
            url: address,
            data: {'id': id, 'key': key, 'value': value},
            response: 'text',
            success: function (data) {
                var propElement = document.getElementsByClassName('prop_' + lineId + '_' + key);
                var buttonElement = document.getElementsByClassName('button_' + lineId + '_' + key);

                for (var i = 0; i < propElement.length; i++) {
                    propElement[i].style.backgroundColor = 'white';
                    propElement[i].innerText = value;
                }
                for (var i = 0; i < buttonElement.length; i++) {
                    buttonElement[i].style.display = "none";
                }
            },
            error: function (data) {
                alert("ERROR: " + data);
            }
        });

    }

    function cloneTemplate(clusterIndex, clusterAddress, gameId) {
        var address = clusterAddress + "/support/templateManager/cloneTemplate.jsp";

        var result = "";

        var keyElements = document.getElementsByClassName('key_' + gameId);
        for (var i = 0; i < keyElements.length; i++) {
            var key = keyElements[i].innerText;
            var value = document.getElementsByClassName('prop_' + gameId + '_' + key)[clusterIndex].innerText;

            result += key + "=" + value + "$";
        }

        result = result.substring(0, result.length - 1);

        $$a({
            type: 'post',
            url: address,
            data: {'id': gameId, 'result': result},
            response: 'text',
            success: function (data) {
                alert(data);

                // Update properties in table //////////////////////////////////////////////////////////////////////////
                var title = document.getElementById("title_" + gameId);
                title.style.backgroundColor = 'white';

                var anotherCluster = (clusterIndex == 1) ? 0 : 1;
                for (var i = 0; i < keyElements.length; i++) {
                    var key = keyElements[i].innerText;
                    var originalValue = document.getElementsByClassName('prop_' + gameId + '_' + key)[clusterIndex];
                    var clusterParameter = document.getElementsByClassName('prop_' + gameId + '_' + key)[anotherCluster];

                    clusterParameter.innerText = originalValue.innerText;
                    clusterParameter.style.backgroundColor = 'white';
                }
                ////////////////////////////////////////////////////////////////////////////////////////////////////////
            },
            error: function (data) {
                alert(data);
            }
        });
    }

</script>


<%!
    HashMap<String, GameTemplate> mapGameTemplate_1 = null;
    HashMap<String, GameTemplate> mapGameTemplate_2 = null;

    PrintWriter writer;


    HashMap<String, GameTemplate> getMapFromRequest(PrintWriter writer, String request) {
        HashMap<String, GameTemplate> map = new HashMap<String, GameTemplate>();

        if (request.contains("id")) {
            String[] array_line = request.split("\\{@###@}");

            for (String line : array_line) {
                GameTemplate gameTemplate = new GameTemplate(writer, line);

                Long gameId = Long.parseLong(gameTemplate.properties.get("id"));

                map.put(String.valueOf(gameId), gameTemplate);
            }
        }

        return map;
    }

    String toPlanText(String text) {
        return text.replaceAll("\\s", "&nbsp;");
    }

    boolean isCorrectValue(String value) {
        return (value != null && !value.toUpperCase().equals("NULL") && !value.equals("[none]") && !value.isEmpty());
    }

    boolean isCloneProperty(String key) {
        if (key.equals("title") || key.equals("swfLocation") || key.equals("gameControllerClass") || key.equals("servlet") ||
                key.equals("id") || key.equals("gameName") || key.equals("PDF_RULES_NAME") || key.equals("GAME_COMBO_DETECTOR_NAME") ||
                key.equals("PC") || key.equals("IOSMOBILE") || key.equals("ANDROID") || key.equals("WINDOWSPHONE") ||
                key.equals("SIDE_JP_GAME_IDS") || key.equals("JACKPOT_NAME") || key.equals("FAKE_ID_FOR") ||
                key.equals("REPOSITORY_FILE")) {
            return true;
        }

        return false;
    }

    int checkErrorType(ArrayList<String> array_keys, GameTemplate gameTemplate_1, GameTemplate gameTemplate_2) {
        for (String key : array_keys) {
            if (arrayCustomProperties != null && !arrayCustomProperties.isEmpty() && !arrayCustomProperties.contains(key)) continue;

            if (gameTemplate_1 == null || gameTemplate_2 == null) return 2;

            String value_1 = gameTemplate_1.properties.get(key);
            String value_2 = gameTemplate_2.properties.get(key);

            if (value_1 == null) value_1 = "[none]";
            if (value_2 == null) value_2 = "[none]";

            if (!value_1.trim().equals(value_2.trim())) {
                return 1;
            }

        }
        return 0;
    }

    private HashMap<String, String> mergeGameMaps(HashMap<String, GameTemplate> mapGameTemplate_1,
                                                  HashMap<String, GameTemplate> mapGameTemplate_2) {
        HashSet<String> allKeys = new HashSet<String>();

        allKeys.addAll(mapGameTemplate_1.keySet());
        allKeys.addAll(mapGameTemplate_2.keySet());


        HashMap<String, String> mapGames = new HashMap<String, String>();

        for (String key : allKeys) {
            GameTemplate gameTemplate_1 = mapGameTemplate_1.get(key);
            GameTemplate gameTemplate_2 = mapGameTemplate_2.get(key);

            String name = "";
            if (gameTemplate_1 != null) {
                name = gameTemplate_1.getTitle();
                if (name == null || name.equals("null")) name = gameTemplate_1.getTitle();
                if (name == null || name.equals("null")) name = gameTemplate_1.getGameName();
            } else if (gameTemplate_2 != null) {
                name = gameTemplate_2.getTitle();
                if (name == null || name.equals("null")) name = gameTemplate_2.getTitle();
                if (name == null || name.equals("null")) name = gameTemplate_2.getGameName();
            }

            mapGames.put(key, name);
        }

        return sortMap(mapGames);
    }


    private HashMap<String, String> sortMap(HashMap<String, String> map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }


    private ArrayList<String> getKeysFromTemplates(GameTemplate gameTemplate_1, GameTemplate gameTemplate_2) {
        ArrayList<String> array_keys = new ArrayList<String>();
        if (gameTemplate_1 != null) {
            for (String key : gameTemplate_1.properties.keySet())
                if (!array_keys.contains(key)) array_keys.add(key);
        }
        if (gameTemplate_2 != null) {
            for (String key : gameTemplate_2.properties.keySet())
                if (!array_keys.contains(key)) array_keys.add(key);
        }

        return array_keys;
    }

    LinkedHashMap<String, String> mapCorrectProperties = new LinkedHashMap<String, String>();
    ArrayList<String> arrayCustomProperties = new ArrayList<String>();

    private void addPropertyToMap(String id, String property) {
        String value = mapCorrectProperties.get(id);
        if (value == null)
            value = "";

        if (!value.isEmpty()) value += "$";
        value += property;

        mapCorrectProperties.put(id, value);
    }

    String[] parseStringArray(String strArray) {
        return strArray.replaceAll(", ", " ").replaceAll(",", " ").split(" ");
    }
%>

<%
    writer = response.getWriter();

    mapCorrectProperties.clear();
    arrayCustomProperties.clear();

    String cluster_1 = request.getParameter("cluster_1");
    String cluster_2 = request.getParameter("cluster_2");

    String request_1 = request.getParameter("result_1");
    String request_2 = request.getParameter("result_2");

    String address_1 = request.getParameter("custom_address_1");
    String address_2 = request.getParameter("custom_address_2");
    String custom_properties = request.getParameter("custom_properties");

    if (custom_properties != null && !custom_properties.isEmpty()) {
        String[] strProperties = parseStringArray(custom_properties);

        Collections.addAll(arrayCustomProperties, strProperties);
    }


    if (request_1 != null && request_2 != null) {
        mapGameTemplate_1 = getMapFromRequest(response.getWriter(), request_1);
        mapGameTemplate_2 = getMapFromRequest(response.getWriter(), request_2);

        HashMap<String, String> mapGames = mergeGameMaps(mapGameTemplate_1, mapGameTemplate_2);

        for (String strGameId : mapGames.keySet()) {
            GameTemplate gameTemplate_1 = mapGameTemplate_1.get(strGameId);
            GameTemplate gameTemplate_2 = mapGameTemplate_2.get(strGameId);

            if (gameTemplate_1 == null && gameTemplate_2 == null) continue;


            ArrayList<String> array_keys = getKeysFromTemplates(gameTemplate_1, gameTemplate_2);


            String strNameLeft = (gameTemplate_1 != null ? (gameTemplate_1.getFullName() + "(" + gameTemplate_1.getId() + ")") : "\"\"");
            String strNameRight = (gameTemplate_2 != null ? (gameTemplate_2.getFullName() + "(" + gameTemplate_2.getId() + ")") : "\"\"");

            String name = (!strNameLeft.isEmpty() ? strNameLeft : strNameRight);

            if (!strNameLeft.equals(strNameRight)) {
                name = strNameLeft + " -- " + strNameRight;
            }

            int errorType = checkErrorType(array_keys, gameTemplate_1, gameTemplate_2);
            String color = "#ffffff";
            if (errorType > 0) color = (errorType == 1 ? "#ffc8c8" : "#aaaaaa");

            String clusterAddressWithOutTemplate = null;
            int clusterIndexWithTemplate = 0;
            if (gameTemplate_1 == null) {
                clusterAddressWithOutTemplate = address_1;
                clusterIndexWithTemplate = 1;
            }
            if (gameTemplate_2 == null) {
                clusterAddressWithOutTemplate = address_2;
                clusterIndexWithTemplate = 0;
            }

%>
<div>
    <TABLE border="2">
        <TR>
            <TD id="title_<%=strGameId%>" colspan="3" style="width: 1440px; font-size: large; font-weight: bold; text-align: center; background-color: <%=color%>">
                <a href="javascript:void(0)" onclick="showHide('game_<%=strGameId%>')"><%=name%>
                </a>
                <%if (clusterAddressWithOutTemplate != null && (arrayCustomProperties == null || arrayCustomProperties.isEmpty())) { %>
                <input class="button_template_<%=strGameId%>" style="width: 120px; float: right; height: 24px;" type="button" onclick="cloneTemplate(<%=clusterIndexWithTemplate%>, '<%=clusterAddressWithOutTemplate%>', '<%=strGameId%>')" value="CLONE TEMPLATE"/> <%
                } %>
            </TD>
        </TR>

        <TR class="game_<%=strGameId%>" style="display: none;">
            <TD style="width: 240px; font-weight: bold; background-color: #9d9d9d; text-align: center">key</TD>
            <TD style="width: 600px; font-weight: bold; background-color: #9d9d9d; text-align: center"><%=cluster_1 + "(" + address_1 + ")"%>
            </TD>
            <TD style="width: 600px; font-weight: bold; background-color: #9d9d9d; text-align: center"><%=cluster_2 + "(" + address_2 + ")"%>
            </TD>
        </TR>

        <%
            for (String key : array_keys) {
                if (arrayCustomProperties != null && !arrayCustomProperties.isEmpty() && !arrayCustomProperties.contains(key)) continue;

                String value_1 = (gameTemplate_1 != null ? gameTemplate_1.properties.get(key) : "TEMPLATE=NULL");
                String value_2 = (gameTemplate_2 != null ? gameTemplate_2.properties.get(key) : "TEMPLATE=NULL");

                if (value_1 == null) value_1 = "[none]";
                if (value_2 == null) value_2 = "[none]";

                String COLOR = "#ffffff";

                String gameIdLeft = (gameTemplate_1 != null ? gameTemplate_1.properties.get("id") : strGameId);
                String gameIdRight = (gameTemplate_2 != null ? gameTemplate_2.properties.get("id") : strGameId);

                boolean canSend = false;
                if (gameTemplate_1 != null && gameTemplate_2 != null) {
                    if (!value_1.equals(value_2)) {

                        COLOR = "#ff0000";

                        if (!key.equals("id") && !key.equals("gameName")) {
                            canSend = true;

                            if (isCorrectValue(value_1) && !isCorrectValue(value_2))
                                addPropertyToMap(gameIdLeft, key + "=" + value_1);
                            else if (!isCorrectValue(value_1) && isCorrectValue(value_2))
                                addPropertyToMap(gameIdRight, key + "=" + value_2);
                        }

                        if (value_1.trim().equals(value_2.trim()))
                            COLOR = "#ffaaaa";
                    }

                    // Clones //////////////////////////////////////////////////////////////////////////////////////
                    if (!gameTemplate_1.getId().equals(gameTemplate_2.getId()) && isCloneProperty(key)) {
                        COLOR = "#ccccff";
                        canSend = false;
                    }
                    ////////////////////////////////////////////////////////////////////////////////////////////////
                }
        %>
        <TR class="game_<%=strGameId%>" style="display: none;">
            <TD class="key_<%=strGameId%>" style="font-weight: bold; background-color: #dbdbdb; text-align: center"><%=key%>
            </TD>
            <TD class="prop_<%=strGameId%>_<%=key%>" style="background-color: <%=COLOR%>; "><%=toPlanText(value_1)%>
                <%if (canSend) { %>
                <input class="button_<%=strGameId%>_<%=key%>" style="width: 80px; float: right; height: 24px;" type="button" onclick="sendProperty('<%=address_2%>', '<%=strGameId%>', '<%=gameIdRight%>', '<%=key%>', '<%=value_1%>')" value="SEND"/> <% } %>
            </TD>
            <TD class="prop_<%=strGameId%>_<%=key%>" style="background-color: <%=COLOR%>; "><%=toPlanText(value_2)%>
                <%if (canSend) { %>
                <input class="button_<%=strGameId%>_<%=key%>" style="width: 80px; float: right; height: 24px;" type="button" onclick="sendProperty('<%=address_1%>', '<%=strGameId%>', '<%=gameIdLeft%>', '<%=key%>', '<%=value_2%>')" value="SEND"/> <% } %>
            </TD>
        </TR>
        <%
            }
        %>
    </TABLE>
</div>
<%
        }
    }
%>

<HR>
<b>Map of correct properties:</b> <BR>

<%
    for (String key : mapCorrectProperties.keySet()) {
        String value = mapCorrectProperties.get(key);
%>
<DIV>mapCorrectProperties.put(<%=key%>, "<%=value%>");</DIV>
<%
    }
%>