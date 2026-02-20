<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%
    Set<Long> bankIds = BankInfoCache.getInstance().getBankIds();
    String gid = request.getParameter("gid");

    Long gameId = 78l;
    if (gid != null) {
        gameId = Long.parseLong(gid);
    }

    response.getWriter().write("</br>GameName: " + BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(Long.parseLong(gid)).getGameName());

    Set<String> subcusinoOut = new HashSet<>();

    Map<Long, List<Long>> subcasinos = new HashMap<>();
    Map<Long, SubCasino> allObjects = SubCasinoCache.getInstance().getAllObjects();
    for (Map.Entry<Long, SubCasino> subCasinoEntry : allObjects.entrySet()) {
        subcasinos.put(subCasinoEntry.getKey(), subCasinoEntry.getValue().getBankIds());
    }

    for (Long bankId : bankIds) {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        IBaseGameInfo gameInfoShared = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, bankInfo.getDefaultCurrency());
        if (gameInfoShared != null) {
            Long sid = 1000l;
            for (Long aLong : subcasinos.keySet()) {
                if (subcasinos.get(aLong).contains(bankId)) {
                    subcusinoOut.add(String.valueOf(aLong));
                    sid = aLong;
                }
            }
            response.getWriter().write(
                    "SubCusino:  " + sid + "  bankId:  " + bankId + "  BankName: " + bankInfo.getExternalBankIdDescription()
                            + "<br>");
        }
    }

    response.getWriter().write("SubCasinoIds: " + subcusinoOut + "<br>");
    for (String s : subcusinoOut) {
        SubCasino subCasino = SubCasinoCache.getInstance().get(Long.parseLong(s));
        response.getWriter().write(subCasino.getName() + "   " + subCasino.getDomainNamesAsString() + "</br>");
    }
%>