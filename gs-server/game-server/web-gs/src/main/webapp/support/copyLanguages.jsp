<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.abs.casino.common.util.logkit.ThreadLog" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%--
  User: shegan
  Date: 29.04.14
  Time: 18:30
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    Long copyFrom;

    Long subCasinoId;
    List<Long> banks;

    String copyFromAsString = request.getParameter("copyFrom");
    if (copyFromAsString == null) {
        response.getWriter().print("Missing parameter : copyFrom");
        return;
    }

    try {
        copyFrom = Long.valueOf(copyFromAsString);
    } catch (Exception e) {
        response.getWriter().print("Parameter 'copyFrom' : wrong format " + copyFromAsString);
        return;
    }

    if (request.getParameter("subCasinoId") != null && request.getParameter("bankIds") != null) {
        response.getWriter().print("Too many parameters, delete 'subCasinoId' or 'bankIds'");
        return;
    } else if (request.getParameter("subCasinoId") != null) {
        copyFromAsString = request.getParameter("subCasinoId");
        try {
            subCasinoId = Long.parseLong(copyFromAsString);
        } catch (Exception e) {
            response.getWriter().print("Parameter 'subCasinoId' : wrong format " + copyFromAsString);
            return;
        }
        banks = new ArrayList<>(SubCasinoCache.getInstance().getBankIds(subCasinoId));
        banks.remove(copyFrom);
    } else if (request.getParameter("bankIds") != null) {
        copyFromAsString = request.getParameter("bankIds");
        StringTokenizer tokenizer = new StringTokenizer(copyFromAsString, ",.;:|-+");
        banks = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            try {
                copyFromAsString = tokenizer.nextToken();
                banks.add(Long.parseLong(copyFromAsString));
            } catch (Exception e) {
                response.getWriter().print("Parameter 'bankIds' : wrong format " + copyFromAsString);
                return;
            }
        }
    } else {
        banks = new ArrayList<>(SubCasinoCache.getInstance().getBankIds(BankInfoCache.getInstance().getSubCasinoId(copyFrom)));
        banks.remove(copyFrom);
        ThreadLog.debug("copyLanguages: mode 3, banks=" + banks);
    }

    Currency copyFromCurrency = BankInfoCache.getInstance().getBankInfo(copyFrom).getDefaultCurrency();
    for (long bank : banks) {
        response.getWriter().print("BankId = " + bank + " </br>");
        response.getWriter().flush();
        ThreadLog.debug("copyLanguages: BankId = " + bank + " process");
        Currency currency = BankInfoCache.getInstance().getBankInfo(bank).getDefaultCurrency();
        for (Long gameId : BaseGameInfoTemplateCache.getInstance().getAllGameIds()) {
            IBaseGameInfo baseGameInfo = BaseGameCache.getInstance().getGameInfoShared(copyFrom, gameId, copyFromCurrency);
            IBaseGameInfo baseGameInfoToChange = BaseGameCache.getInstance().getGameInfoShared(bank, gameId, currency);

            if ((baseGameInfo == null && baseGameInfoToChange != null) || (baseGameInfo != null && baseGameInfoToChange == null)) {
                response.getWriter().print("Bank (copy from) " + copyFrom + " does not include game with id " + gameId + "(" +
                        BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId).getTitle() + ")" + "</br>");
                response.getWriter().flush();
                ThreadLog.debug("copyLanguages: Bank (copy from) " + copyFrom + " does not include game with id " + gameId + "(" +
                        BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId).getTitle() + ")");
                continue;
            } else if (baseGameInfo != null) {
                baseGameInfoToChange.setLanguages(baseGameInfo.getLanguages());
                ThreadLog.debug("copyLanguages: start save for bank = " + bank);
                RemoteCallHelper.getInstance().saveAndSendNotification(baseGameInfoToChange);
                ThreadLog.debug("copyLanguages: saved for bank = " + bank);
            }
        }
    }
    response.getWriter().print("ALL DONE");
%>