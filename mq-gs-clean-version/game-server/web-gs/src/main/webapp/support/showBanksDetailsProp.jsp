<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%
    String value;

    Map<Long, List<Long>> bankIds = new HashMap<>();  //SubCasinoId, BankIds (SubCasino == -1 for "bankId" values)
    Set<String> prop = null;
    value = request.getParameter("bankId");
    if (value != null) {
        StringTokenizer banks = new StringTokenizer(value, ",.;:|-+");
        List<Long> list = new ArrayList<Long>();
        while (banks.hasMoreTokens()) {
            list.add(Long.parseLong(banks.nextToken()));
        }
        Collections.sort(list);
        bankIds.put(-1L, list);
    }

    value = request.getParameter("prop");
    if (value != null) {
        StringTokenizer types = new StringTokenizer(value, ",.;:|-+");
        prop = new HashSet<>();
        while (types.hasMoreTokens()) {
            prop.add(types.nextToken());
        }
    }

    value = request.getParameter("subCasinoId");
    if (value != null) {
        StringTokenizer types = new StringTokenizer(value, ",.;:|-+");
        List<Long> list = new ArrayList<>();
        while (types.hasMoreTokens()) {
            Long subCasinoId = Long.parseLong(types.nextToken());
            list.addAll(SubCasinoCache.getInstance().getBankIds(subCasinoId));
            Collections.sort(list);
            bankIds.put(subCasinoId, list);
        }
    }


    if (bankIds != null) {
        response.getWriter().print("<table>");
        List<Long> list = new ArrayList<>(bankIds.keySet());
        Collections.sort(list);
        for (long subCasinoId : list) {
            if (subCasinoId != -1) {
                response.getWriter().print("<tr>");
                response.getWriter().print("<td>");
                response.getWriter().print("SubCasino " + subCasinoId + "/" + SubCasinoCache.getInstance().get(subCasinoId).getName());
                response.getWriter().print("</td>");
                response.getWriter().print("</tr>");
            }
            for (long bankId : bankIds.get(subCasinoId)) {
                response.getWriter().print("<tr>");
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                if (bankInfo == null) {
                    response.getWriter().print("<td colspan=\"2\">");
                    response.getWriter().print("Bank (id = " + bankId + ") is not exists<br>");
                    response.getWriter().print("</td>");
                    continue;
                }
                response.getWriter().print("<td>");
                response.getWriter().print("<a href=\"/support/bankSelectAction.do?bankId=" + bankId + "\">");
                response.getWriter().print(bankId + "/" + bankInfo.getExternalBankIdDescription());
                response.getWriter().print("</a>");
                response.getWriter().print("</td>");

                response.getWriter().print("<td>");
                if (prop == null) {
                    continue;
                }
                for (String property : prop) {
                    response.getWriter().print(property + " : " + bankInfo.getStringProperty(property));
                    response.getWriter().print("<br>");
                }
                response.getWriter().print("</td>");
                response.getWriter().print("</tr>");

            }
        }

        response.getWriter().print("</table>");
    }
%>