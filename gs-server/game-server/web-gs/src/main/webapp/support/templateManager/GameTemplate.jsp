<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.*" %>

<%!
    String normalize(String value) {
        if (value != null && value.trim().equals("null")) return null;
        return value;
    }

    class GameTemplate {
        PrintWriter writer = null;
        LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> templateProperties = new LinkedHashMap<String, String>();

        public GameTemplate(PrintWriter writer, BaseGameInfoTemplate template) {
            properties.put("id", String.valueOf(template.getGameId()));
            properties.put("gameName", String.valueOf(template.getGameName()));
            properties.put("title", String.valueOf(template.getTitle()));
            properties.put("swfLocation", String.valueOf(template.getSwfLocation()));
            properties.put("gameControllerClass", String.valueOf(template.getGameControllerClass()));
            properties.put("roundFinishedHelper", String.valueOf((template.getRoundFinishedHelper() != null) ? template.getRoundFinishedHelper().name() : null));
            properties.put("endRoundSignature", String.valueOf(template.getEndRoundSignature()));
            properties.put("servlet", String.valueOf(template.getServlet()));
            properties.put("type", String.valueOf(template.getDefaultGameInfo().getGameType()));
            properties.put("group", String.valueOf(template.getDefaultGameInfo().getGroup()));
            properties.put("varType", String.valueOf(template.getDefaultGameInfo().getVariableType()));
            properties.put("oldTranslation", String.valueOf(template.isOldTranslation()));

            properties.putAll(template.getDefaultGameInfo().getPropertiesMap());
            templateProperties.putAll(template.getDefaultGameInfo().getPropertiesMap());
        }

        public String getId() {
            return normalize(properties.get("id"));
        }

        public String getTitle() {
            return normalize(properties.get("title"));
        }

        public String getGameName() {
            return normalize(properties.get("gameName"));
        }

        public String getServlet() {
            return normalize(properties.get("servlet"));
        }

        public String getSwfLocation() {
            return normalize(properties.get("swfLocation"));
        }

        public String getGameControllerClass() {
            return normalize(properties.get("gameControllerClass"));
        }

        public String getEndRoundSignature() {
            return normalize(properties.get("endRoundSignature"));
        }

        public String getFullName() {
            return (getTitle() != null ? getTitle() : getGameName());
        }

        public Integer getValueId() {
            return Integer.parseInt(properties.get("id"));
        }

        public boolean getValueIsJackpot() {
            return Boolean.valueOf(properties.get("JACKPOT_MULTIPLIER") != null);
        }

        public GameType getValueGameType() {
            String strType = normalize(properties.get("type"));
            return (strType != null) ? GameType.valueOf(strType) : null;
        }

        public GameGroup getValueGameGroup() {
            String strGroup = normalize(properties.get("group"));
            return (strGroup != null) ? GameGroup.valueOf(strGroup) : null;
        }

        public GameVariableType getValueGameVariableType() {
            String strVarType = normalize(properties.get("varType"));
            return (strVarType != null) ? GameVariableType.valueOf(strVarType) : null;
        }

        public RoundFinishedHelper getValueRoundFinishedHelper() {
            String strHelper = normalize(properties.get("roundFinishedHelper"));
            return (strHelper != null) ? RoundFinishedHelper.valueOf(strHelper) : null;
        }

        public GameTemplate(PrintWriter writer, String line) {
            this.writer = writer;
            fromString(line);
        }

        public GameTemplate(PrintWriter writer, String[] lines) {
            this.writer = writer;
            fromArray(lines);
        }

        public String toString() {
            String result = "";

            for (String key : properties.keySet()) {
                result += key + "=" + properties.get(key) + "$";
            }

            if (!result.isEmpty())
                result = result.substring(0, result.length() - 1);

            return result;
        }

        public void fromString(String line) {
            try {
                fromArray(line.split("\\$"));
            } catch (Exception ex) {
                writer.write("ERROR: " + line + "<br>");
            }
        }

        public void fromArray(String[] strArrayParameter) {
            properties.clear();

            for (String parameter : strArrayParameter) {
                String[] info = parameter.split("=", 2);
                String key = info[0], value = "";
                if (info.length == 2) value = info[1];

                value = value.replaceAll(String.valueOf((char) 160), " ");
                properties.put(key, value);

                if (key.toUpperCase() == key)
                    templateProperties.put(key, value);
            }
        }
    }
%>