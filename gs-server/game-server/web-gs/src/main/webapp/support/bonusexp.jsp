<%@ page import="com.dgphoenix.casino.account.AccountManager" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.account.AccountInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bonus.BaseBonus" %>
<%@ page import="com.abs.casino.common.exception.BonusException" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.bonus.AbstractBonusManager" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.bonus.BonusManager" %>
<%@ page import="com.dgphoenix.casino.gs.managers.payment.bonus.FRBonusManager" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%!
    private static final String STATUS_OK = "ok";
%>
<%
    boolean xmlOutput;
    long bonusId = -1;
    long frBonusId = -1;
    boolean wasError = false;

    xmlOutput = Boolean.valueOf(request.getParameter("xml"));
    OutputFormat formatter = new HtmlOutputFormat(request);
    if (xmlOutput) {
        formatter = new XmlOutputFormat(request);
    }
    response.setContentType(formatter.getContentType());

    String tmpParameter = request.getParameter("accountId");
    if (tmpParameter != null) {
        try {
            long accountId = Long.parseLong(tmpParameter);
            formatter.setAccountId(accountId);
        } catch (NumberFormatException e) {
            formatter.writeError(out, "accountId is invalid '" + tmpParameter + "'");
            wasError = true;
        }
    }

    tmpParameter = request.getParameter("bonusId");
    if (tmpParameter != null) {
        try {
            bonusId = Long.parseLong(tmpParameter);
            formatter.setBonusId(bonusId);
        } catch (NumberFormatException e) {
            formatter.writeError(out, "bonusId is invalid '" + tmpParameter + "'");
            wasError = true;
        }
    }

    tmpParameter = request.getParameter("frBonusId");
    if (tmpParameter != null) {
        try {
            frBonusId = Long.parseLong(tmpParameter);
            formatter.setFrBonusId(frBonusId);
        } catch (NumberFormatException e) {
            formatter.writeError(out, "frBonusId is invalid '" + tmpParameter + "'");
            wasError = true;
        }
    }

    if (bonusId != -1 && !wasError) {
        formatter.setBonusExpireResult(expireBonus(bonusId, BonusManager.getInstance()));
    }

    if (frBonusId != -1 && !wasError) {
        formatter.setFrBonusExpireResult(expireBonus(frBonusId, FRBonusManager.getInstance()));
    }

    try {
        formatter.writeOutput(out, response);
    } catch (CommonException e) {
        e.printStackTrace(response.getWriter());
    }
%>
<%!
    private <T extends BaseBonus> String expireBonus(long bonusId,
                                                     AbstractBonusManager<T> manager) throws IOException {
        T bonus = manager.getById(bonusId);
        if (bonus != null) {
            try {
                if (manager.expireBonus(bonus)) {
                    return STATUS_OK;
                } else {
                    return "Could not expire bonus: Internal error";
                }
            } catch (BonusException e) {
                return "Error retrieving Bonus '" + bonusId + "' : " + e.getMessage();
            }
        } else {
            return "Bonus not found '" + bonusId + "'";
        }
    }

    private abstract class OutputFormat {
        protected long bonusId = -1;
        protected long frBonusId = -1;
        protected long accountId = -1;
        protected String bonusExpireResult;
        protected String frBonusExpireResult;
        protected HttpServletRequest request;

        public void setBonusId(long bonusId) {
            this.bonusId = bonusId;
        }

        public void setFrBonusId(long frBonusId) {
            this.frBonusId = frBonusId;
        }

        public void setAccountId(long accountId) {
            this.accountId = accountId;
        }

        public void setBonusExpireResult(String bonusExpireResult) {
            this.bonusExpireResult = bonusExpireResult;
        }

        public void setFrBonusExpireResult(String frBonusExpireResult) {
            this.frBonusExpireResult = frBonusExpireResult;
        }


        public OutputFormat(HttpServletRequest request) {
            this.request = request;
        }

        public abstract String getContentType();

        public abstract void writeOutput(JspWriter writer, HttpServletResponse response) throws IOException, CommonException;

        public abstract void writeError(JspWriter writer, String error, Throwable e) throws IOException;

        public void writeError(JspWriter writer, String error) throws IOException {
            writeError(writer, error, null);
        }
    }

    private class HtmlOutputFormat extends OutputFormat {
        @Override
        public String getContentType() {
            return "text/html";
        }

        public HtmlOutputFormat(HttpServletRequest request) {
            super(request);
        }

        @Override
        public void writeError(JspWriter writer, String error, Throwable e) throws IOException {
            writer.println("<font color=\"red\">" + error + "</font><br/>\n");
            if (e != null) {
                writer.println(e.toString() + "<br/>\n");
            }
        }

        @Override
        public void writeOutput(JspWriter writer, HttpServletResponse response) throws IOException, CommonException {
            writer.println("<html><head><title>FRB\\BONUS expire page</title></head><body>\n");
            if (bonusExpireResult != null) {
                if (STATUS_OK.equals(bonusExpireResult)) {
                    writer.println("<font color=\"green\">Bonus '" + bonusId + "' expired</font><br/>\n");
                } else {
                    writeError(writer, bonusExpireResult);
                }
            }

            if (frBonusExpireResult != null) {
                if (STATUS_OK.equals(frBonusExpireResult)) {
                    writer.println("<font color=\"green\">FRBonus '" + frBonusId + "' expired</font><br/>\n");
                } else {
                    writeError(writer, frBonusExpireResult);
                }
            }

            AccountInfo accountInfo = null;
            if (accountId != -1) {
                accountInfo = AccountManager.getInstance().getAccountInfo(accountId);
                if (accountInfo == null) {
                    writeError(writer, "Account not found");
                }
            }

            writer.println("<form action=\"" + request.getRequestURL().toString() + "\">\n");
            writer.println("<table>\n<tr>\n<td>AccountId:</td>\n");
            writer.println("<td><input title=\"accountId\" type=\"text\" name=\"accountId\"\n");
            if (accountId == -1) {
                writer.println("value=\"\">");
            } else {
                writer.println("value=\"" + accountId + "\">");
            }
            writer.println("</td>\n</tr>\n<tr>\n");
            writer.println("<td colspan=\"2\" align=\"right\">" +
                    "<input type=\"submit\" value=\"Show active bonuses\"></td>\n");
            writer.println("</tr>\n</table>\n</form>\n<br/>\n\n");
            if (accountInfo != null) {
                writer.println("<table border=1\" cellpadding=\"5\" width=\"100%\">\n");
                writer.println("<tr>\n<th>Info</th>\n<th width=\"200px\"> Action</th>\n");
                writer.println("</tr>\n");
                writer.println(getBonuses(FRBonusManager.getInstance().getActiveBonuses(accountInfo),
                        "FRBonus", accountId, request.getRequestURL().toString()));
                writer.println(getBonuses(BonusManager.getInstance().getActiveBonuses(accountInfo),
                        "Bonus", accountId, request.getRequestURL().toString()));
                writer.println("</table>\n");
            }
            writer.println("</body>\n</html>\n");
        }

        private String getBonuses(List<? extends BaseBonus> bList, String type, long accountId, String context) {
            StringBuilder sb = new StringBuilder();
            sb.append("<tr><th colspan=\"2\">").append(type).append("</th></tr>");
            for (BaseBonus b : bList) {
                sb.append("<tr>");
                sb.append("<td style=\"word-wrap: break-word\">").append(b).append("</td>");
                sb.append("<td style=\"min-width:150px\">");
                String url;
                if ("FRBonus".equals(type)) {
                    url = getExpireUrl(b.getId(), -1, accountId, context);
                } else {
                    url = getExpireUrl(-1, b.getId(), accountId, context);
                }
                sb.append("<a href=\"").append(url).append("\">Set expired</a></td>");
                sb.append("</tr>\n");
            }
            return sb.toString();
        }

        private String getExpireUrl(long frBonusId, long bonusId, long accountId, String context) {
            StringBuilder sb = new StringBuilder(context);
            if (frBonusId != -1) {
                sb.append("?frBonusId=").append(frBonusId);
            } else {
                if (bonusId != -1) {
                    sb.append("?bonusId=").append(bonusId);
                }
            }
            if (accountId != -1) {
                sb.append("&accountId=").append(accountId);
            }
            return sb.toString();
        }
    }

    private class XmlOutputFormat extends OutputFormat {
        private boolean wasError;
        private boolean wasOutput;

        public XmlOutputFormat(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getContentType() {
            return "text/xml";
        }

        @Override
        public void writeError(JspWriter writer, String error, Throwable e) throws IOException {
            if (!(wasError || wasOutput)) {
                wasError = true;
                writer.println(getErrorXml(request.getParameterMap(), error, e));
            }
        }

        @Override
        public void writeOutput(JspWriter writer, HttpServletResponse response) throws IOException,
                CommonException {
            writer.println(getResultXml(request.getParameterMap(), bonusExpireResult, frBonusExpireResult));
        }

        private String getResultXml(Map<String, String[]> reqParams,
                                    String bonusExpireResult, String frBonusExpireResult) {
            if (wasError || wasOutput) {
                return "";
            }

            wasOutput = true;
            StringBuilder xml = new StringBuilder();
            xml.append(getTag("bonusexp"));
            xml.append(getTag("request", 4));
            for (Map.Entry<String, String[]> parameter : reqParams.entrySet()) {
                if ("xml".equals(parameter.getKey())) {
                    continue;
                }
                xml.append(lead(wrapWithTags(parameter.getKey(), parameter.getValue()[0]), 8));
            }
            xml.append(getTag("request", 4, true));
            xml.append(getTag("response", 4));
            xml.append(getTag("bonus", 8));
            if (bonusExpireResult != null) {
                if (STATUS_OK.equals(bonusExpireResult)) {
                    xml.append(lead(wrapWithTags("result", STATUS_OK), 12));
                } else {
                    xml.append(lead(wrapWithTags("result", "error"), 12));
                    xml.append(lead(wrapWithTags("error", bonusExpireResult), 12));
                }
            }
            xml.append(getTag("bonus", 8, true));
            xml.append(getTag("frBonus", 8));
            if (frBonusExpireResult != null) {
                if (STATUS_OK.equals(frBonusExpireResult)) {
                    xml.append(lead(wrapWithTags("result", STATUS_OK), 12));
                } else {
                    xml.append(lead(wrapWithTags("result", "error"), 12));
                    xml.append(lead(wrapWithTags("error", frBonusExpireResult), 12));
                }
            }
            xml.append(getTag("frBonus", 8, true));
            xml.append(getTag("response", 4, true));
            xml.append(getTag("bonusexp", true));
            return xml.toString();
        }

        private String getErrorXml(Map<String, String[]> reqParams, String error, Throwable exception) {
            StringBuilder xml = new StringBuilder();
            xml.append(getTag("bonusexp"));
            xml.append(getTag("request", 4));
            for (Map.Entry<String, String[]> parameter : reqParams.entrySet()) {
                if ("xml".equals(parameter.getKey())) {
                    continue;
                }
                xml.append(lead(wrapWithTags(parameter.getKey(), parameter.getValue()[0]), 8));
            }
            xml.append(getTag("request", 4, true));
            xml.append(getTag("response", 4));
            if (error != null) {
                xml.append(lead(wrapWithTags("error", error), 8));
            }
            if (exception != null) {
                xml.append(lead(wrapWithTags("exception", exception.getMessage()), 8));
            }
            xml.append(getTag("response", 4, true));
            xml.append(getTag("bonusexp", true));
            return xml.toString();
        }

        private String lead(String s, int lead) {
            char[] chars = new char[s.length() + lead];
            Arrays.fill(chars, ' ');
            System.arraycopy(s.toCharArray(), 0, chars, chars.length - s.length(), s.length());
            return new String(chars);
        }

        private String getTag(String tagName, int lead) {
            return getTag(tagName, lead, false);
        }

        private String getTag(String tagName) {
            return getTag(tagName, 0, false);
        }

        private String getTag(String tagName, boolean isClosing) {
            return getTag(tagName, 0, isClosing);
        }

        private String getTag(String tagName, int lead, boolean isClosing) {
            if (isClosing) {
                return lead("</" + tagName + ">\n", lead);
            }
            return lead("<" + tagName + ">\n", lead);
        }

        private String wrapWithTags(String tagName, String value) {
            return String.format("<%1$s>%2$s</%1$s>%n", tagName, value);
}
}
%>