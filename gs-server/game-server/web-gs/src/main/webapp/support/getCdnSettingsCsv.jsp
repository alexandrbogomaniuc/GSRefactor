<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%
    response.getWriter().write("System;BankID;BankName;MCDN;Quantil;BSG China;HighWinds;Cloudflare;Raw;ForceAuto;DisableOrigin<br>");
    response.getWriter().write(";;;;;;<br>");
    Map<Long, SubCasino> casinos = SubCasinoCache.getInstance().getAllObjects();
    for (SubCasino casino : casinos.values()) {
        if (casino.getId() == 58) continue;
        List<Long> bankIds = SubCasinoCache.getInstance().getBankIds(casino.getId());
        StringBuilder stringBuffer = new StringBuilder();
        String casinoName = "";
        for (long bankId : bankIds) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo == null || !bankInfo.isEnabled()
                    || StringUtils.isTrimmedEmpty(bankInfo.getExternalBankId())) continue;
            String cdnUrls = bankInfo.getCdnUrls();
            if (!StringUtils.isTrimmedEmpty(cdnUrls)) {
                if (!casinoName.equals(casino.getName())) {
                    stringBuffer.append(casino.getName()).append(";");
                    casinoName = casino.getName();
                } else {
                    stringBuffer.append(";");
                }

                String auto = bankInfo.getProperties().get("CDN_FORCE_AUTO");
                boolean forceAuto = auto == null ? Boolean.FALSE : Boolean.TRUE.toString().equalsIgnoreCase(auto);

                String origin = bankInfo.getProperties().get("CDN_DISABLE_ORIGIN");
                boolean disableOrigin = origin == null ? Boolean.FALSE : Boolean.TRUE.toString().equalsIgnoreCase(origin);

                stringBuffer.append(bankId).append(";").append(bankInfo.getExternalBankIdDescription()).append(";")
                        .append(cdnUrls.contains("MCDN") ? "+" : "-").append(";")
                        .append(cdnUrls.contains("-cnc.") ? "+" : "-").append(";")
                        .append(cdnUrls.contains("CHINA=cdn-") ? "+" : "-").append(";")
                        .append(cdnUrls.contains(".hwcdn.net") ? "+" : "-").append(";")
                        .append(cdnUrls.contains("cloudflare") ? "+" : "-").append(";")
                        .append(cdnUrls.replaceAll(";", ",")).append(";")
                        .append(forceAuto ? "+" : "-").append(";")
                        .append(disableOrigin ? "+" : "-")
                        .append("<br>");
            }
        }
        if (stringBuffer.length() > 0) {
            response.getWriter().write(stringBuffer.toString() + "<br>");
        }
    }

%>
