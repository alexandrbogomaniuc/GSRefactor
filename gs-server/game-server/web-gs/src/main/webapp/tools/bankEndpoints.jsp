<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.security.MessageDigest" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String bankIdParam = request.getParameter("bankId");
    if (StringUtils.isTrimmedEmpty(bankIdParam)) {
        response.getWriter().write("Parameter 'bankId' can't be empty");
        return;
    }

    long bankId;
    try {
        bankId = Long.parseLong(bankIdParam);
    } catch (NumberFormatException e) {
        response.getWriter().write("Parameter 'bankId' not well formatted");
        return;
    }

    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
    if (bankInfo == null) {
        response.getWriter().write("Bank " + bankId + " not found");
        return;
    }

    String origin = resolveOrigin(request);
    String reportUrl = "/support/bankReleaseReport.jsp?bankId=" + bankId;
    String effectiveBankId = getEffectiveBankId(bankInfo);
    String protocol = detectProtocol(bankInfo);

    String gameId = "838";
    String userToken = resolveUserToken(bankInfo);
    String userId = userToken;
    String extBonusId = "1";
    String bonusId = "1";
    String amount = "10000";
    String multiplier = "2";
    String bonusGames = "all";
    String bonusGameIds = gameId;
    String bonusType = "Deposit";
    String rounds = "5";
    String frbGames = gameId;
    String lang = "en";
    String expDate = new SimpleDateFormat("dd.MM.yyyy").format(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30));

    String bsAwardHash = hashWithBonusKey(bankInfo, userId, effectiveBankId, bonusType, amount, multiplier,
            bonusGames, bonusGameIds, expDate, extBonusId);
    String bsHistoryHash = hashWithBonusKey(bankInfo, userId, effectiveBankId);
    String bsInfoHash = hashWithBonusKey(bankInfo, userId, effectiveBankId);
    String bsInfoByIdHash = hashWithBonusKey(bankInfo, userId, bonusId, effectiveBankId);
    String bsCancelHash = hashWithBonusKey(bankInfo, bonusId);
    String bsCheckHash = hashWithBonusKey(bankInfo, extBonusId, effectiveBankId);

    String frbAwardHash = hashWithBonusKey(bankInfo, userId, effectiveBankId, rounds, frbGames, extBonusId);
    String frbInfoHash = hashWithBonusKey(bankInfo, userId, effectiveBankId);
    String frbInfoByIdHash = hashWithBonusKey(bankInfo, userId, bonusId, effectiveBankId);
    String frbCancelHash = hashWithBonusKey(bankInfo, bonusId);
    String frbCheckHash = hashWithBonusKey(bankInfo, extBonusId, effectiveBankId);
    String frbHistoryHash = hashWithBonusKey(bankInfo, userId, effectiveBankId);

    String startGuestUrl;
    String startGameUrl;
    String historyUrl;
    if ("CT".equals(protocol)) {
        startGuestUrl = buildUrl(origin, "/guestmode.do", new String[][]{
                {"bankId", effectiveBankId},
                {"gameId", gameId},
                {"lang", lang}
        });
        startGameUrl = buildUrl(origin, "/ctenter.do", new String[][]{
                {"bankId", effectiveBankId},
                {"gameId", gameId},
                {"mode", "real"},
                {"token", userToken},
                {"balance", "100000"},
                {"lang", lang}
        });
        historyUrl = buildUrl(origin, "/ctstarthistory.do", new String[][]{
                {"bankId", effectiveBankId},
                {"token", userToken}
        });
    } else if ("WT".equals(protocol)) {
        startGuestUrl = buildUrl(origin, "/guestmode.do", new String[][]{
                {"bankId", effectiveBankId},
                {"gameId", gameId},
                {"lang", lang}
        });
        startGameUrl = buildUrl(origin, "/wtstartgame.do", new String[][]{
                {"token", userToken},
                {"gameId", gameId},
                {"lang", lang},
                {"bankId", effectiveBankId},
                {"mode", "real"}
        });
        historyUrl = buildUrl(origin, "/wtstarthistory.do", new String[][]{
                {"bankId", effectiveBankId},
                {"token", userToken}
        });
    } else if ("CW1".equals(protocol)) {
        startGuestUrl = buildUrl(origin, "/cwguestlogin.do", new String[][]{
                {"bankId", effectiveBankId},
                {"gameId", gameId},
                {"lang", lang}
        });
        startGameUrl = buildUrl(origin, "/cwstartgame.do", new String[][]{
                {"sessionId", userToken},
                {"gameId", gameId},
                {"mode", "real"}
        });
        historyUrl = buildUrl(origin, "/cwstarthistory.do", new String[][]{
                {"bankId", effectiveBankId},
                {"token", userToken}
        });
    } else {
        startGuestUrl = buildUrl(origin, "/cwguestlogin.do", new String[][]{
                {"bankId", effectiveBankId},
                {"gameId", gameId},
                {"lang", lang}
        });
        startGameUrl = buildUrl(origin, "/" + getCwStartGameAction(bankInfo), new String[][]{
                {"bankId", effectiveBankId},
                {"gameId", gameId},
                {"mode", "real"},
                {"token", userToken},
                {"lang", lang}
        });
        historyUrl = buildUrl(origin, "/cwstarthistory.do", new String[][]{
                {"bankId", effectiveBankId},
                {"token", userToken}
        });
    }

    String bsAwardUrl = buildUrl(origin, "/bsaward.do", new String[][]{
            {"bankId", effectiveBankId},
            {"amount", amount},
            {"games", bonusGames},
            {"hash", bsAwardHash},
            {"extBonusId", extBonusId},
            {"gameIds", bonusGameIds},
            {"userId", userId},
            {"expDate", expDate},
            {"type", bonusType},
            {"multiplier", multiplier}
    });
    String bsHistoryUrl = buildUrl(origin, "/bshistory.do", new String[][]{
            {"bankId", effectiveBankId},
            {"userId", userId},
            {"hash", bsHistoryHash}
    });
    String bsInfoUrl = buildUrl(origin, "/bsinfo.do", new String[][]{
            {"bankId", effectiveBankId},
            {"userId", userId},
            {"hash", bsInfoHash}
    });
    String bsInfoByIdUrl = buildUrl(origin, "/bsinfo.do", new String[][]{
            {"bankId", effectiveBankId},
            {"userId", userId},
            {"bonusId", bonusId},
            {"hash", bsInfoByIdHash}
    });
    String bsCancelUrl = buildUrl(origin, "/bscancel.do", new String[][]{
            {"bankId", effectiveBankId},
            {"bonusId", bonusId},
            {"hash", bsCancelHash}
    });
    String bsCheckUrl = buildUrl(origin, "/bscheck.do", new String[][]{
            {"bankId", effectiveBankId},
            {"extBonusId", extBonusId},
            {"hash", bsCheckHash}
    });
    String bsStartGameUrl = buildUrl(origin, "/bsstartgame.do", new String[][]{
            {"bankId", effectiveBankId},
            {"gameId", gameId},
            {"mode", "bonus"},
            {"bonusId", bonusId},
            {"token", userToken},
            {"lang", lang}
    });

    String frbAwardUrl = buildUrl(origin, "/frbaward.do", new String[][]{
            {"bankId", effectiveBankId},
            {"userId", userId},
            {"rounds", rounds},
            {"games", frbGames},
            {"extBonusId", extBonusId},
            {"hash", frbAwardHash}
    });
    String frbCancelUrl = buildUrl(origin, "/frbcancel.do", new String[][]{
            {"bankId", effectiveBankId},
            {"bonusId", bonusId},
            {"hash", frbCancelHash}
    });
    String frbInfoUrl = buildUrl(origin, "/frbinfo.do", new String[][]{
            {"bankId", effectiveBankId},
            {"userId", userId},
            {"hash", frbInfoHash}
    });
    String frbInfoByIdUrl = buildUrl(origin, "/frbinfo.do", new String[][]{
            {"bankId", effectiveBankId},
            {"userId", userId},
            {"bonusId", bonusId},
            {"hash", frbInfoByIdHash}
    });
    String frbCheckUrl = buildUrl(origin, "/frbcheck.do", new String[][]{
            {"bankId", effectiveBankId},
            {"extBonusId", extBonusId},
            {"hash", frbCheckHash}
    });
    String frbHistoryUrl = buildUrl(origin, "/frbhistory.do", new String[][]{
            {"bankId", effectiveBankId},
            {"userId", userId},
            {"hash", frbHistoryHash}
    });
    String frbGameListUrl = buildUrl(origin, "/frbgamelist.do", new String[][]{
            {"bankId", effectiveBankId}
    });

    String jackpot4Url = buildUrl(origin, "/jackpots/jackpot4_" + effectiveBankId + ".xml", new String[][]{});
    String winnersUrl = buildUrl(origin, "/winners/winners_" + effectiveBankId + ".xml", new String[][]{});
    String gamesFeedUrl = buildUrl(origin, "/gamelist.do", new String[][]{
            {"bankId", effectiveBankId}
    });
%>
<html>
<head>
    <title>Bank Endpoints <%=bankInfo.getId()%></title>
    <style type="text/css">
        body { font-family: Arial, sans-serif; margin: 16px; line-height: 1.45; }
        h1 { margin: 0 0 8px 0; }
        h2 { margin: 18px 0 6px 0; }
        p { margin: 4px 0; }
        .meta { margin-bottom: 14px; }
        .note { background: #f4f7ff; border: 1px solid #cad6ff; padding: 8px 10px; border-radius: 4px; }
        .section { margin-top: 14px; }
        .label { font-weight: bold; display: inline-block; min-width: 270px; }
        .line { margin: 4px 0; }
    </style>
</head>
<body>
<h1>Bank Endpoints</h1>
<p class="meta">
    Bank: <b><%=bankInfo.getId()%></b>
    (<%=bankInfo.getExternalBankId()%> - <%=bankInfo.getExternalBankIdDescription()%>)<br/>
    Protocol: <b><%=protocol%></b>
</p>
<p class="note">
    All links below are clickable and generated with concrete values for this bank.
    Default <b>gameId=<%=gameId%></b>, token/userId=<b><%=userToken%></b>.
</p>
<p>
    <a target="_blank" href="<%=reportUrl%>">Open raw bankReleaseReport.jsp output</a>
    |
    <a href="/tools/subCasinoInfo.jsp?subCasinoId=<%=bankInfo.getSubCasinoId()%>">Back to SubCasino banks</a>
</p>

<div class="section">
    <h2>Start Game</h2>
    <div class="line"><span class="label">Guest mode:</span><a target="_blank" href="<%=startGuestUrl%>"><%=startGuestUrl%></a></div>
    <div class="line"><span class="label">Start game:</span><a target="_blank" href="<%=startGameUrl%>"><%=startGameUrl%></a></div>
    <div class="line"><span class="label">History:</span><a target="_blank" href="<%=historyUrl%>"><%=historyUrl%></a></div>
</div>

<% if (!StringUtils.isTrimmedEmpty(bankInfo.getBonusReleaseUrl())) { %>
<div class="section">
    <h2>OCBonus</h2>
    <div class="line"><span class="label">1 BonusAward:</span><a target="_blank" href="<%=bsAwardUrl%>"><%=bsAwardUrl%></a></div>
    <div class="line"><span class="label">2 Not active bonuses:</span><a target="_blank" href="<%=bsHistoryUrl%>"><%=bsHistoryUrl%></a></div>
    <div class="line"><span class="label">3 Active bonuses:</span><a target="_blank" href="<%=bsInfoUrl%>"><%=bsInfoUrl%></a></div>
    <div class="line"><span class="label">4 Bonus info by bonusId:</span><a target="_blank" href="<%=bsInfoByIdUrl%>"><%=bsInfoByIdUrl%></a></div>
    <div class="line"><span class="label">5 Cancel bonus:</span><a target="_blank" href="<%=bsCancelUrl%>"><%=bsCancelUrl%></a></div>
    <div class="line"><span class="label">6 Check bonus:</span><a target="_blank" href="<%=bsCheckUrl%>"><%=bsCheckUrl%></a></div>
    <div class="line"><span class="label">7 Start game in stub mode:</span><a target="_blank" href="<%=bsStartGameUrl%>"><%=bsStartGameUrl%></a></div>
</div>
<% } %>

<% if (!StringUtils.isTrimmedEmpty(bankInfo.getFRBonusWinURL()) || bankInfo.isFRBForCTSupported()) { %>
<div class="section">
    <h2>OFRBonus</h2>
    <div class="line"><span class="label">1 Award OFR bonus:</span><a target="_blank" href="<%=frbAwardUrl%>"><%=frbAwardUrl%></a></div>
    <div class="line"><span class="label">2 Cancel OFR bonus:</span><a target="_blank" href="<%=frbCancelUrl%>"><%=frbCancelUrl%></a></div>
    <div class="line"><span class="label">3 OFR bonus info:</span><a target="_blank" href="<%=frbInfoUrl%>"><%=frbInfoUrl%></a></div>
    <div class="line"><span class="label">4 OFR info by bonusId:</span><a target="_blank" href="<%=frbInfoByIdUrl%>"><%=frbInfoByIdUrl%></a></div>
    <div class="line"><span class="label">5 Check OFR bonus:</span><a target="_blank" href="<%=frbCheckUrl%>"><%=frbCheckUrl%></a></div>
    <div class="line"><span class="label">6 OFR bonus history:</span><a target="_blank" href="<%=frbHistoryUrl%>"><%=frbHistoryUrl%></a></div>
    <div class="line"><span class="label">7 Game list with OFRB:</span><a target="_blank" href="<%=frbGameListUrl%>"><%=frbGameListUrl%></a></div>
</div>
<% } %>

<div class="section">
    <h2>Feeds</h2>
    <div class="line"><span class="label">Jackpot4 feed:</span><a target="_blank" href="<%=jackpot4Url%>"><%=jackpot4Url%></a></div>
    <div class="line"><span class="label">Winners feed:</span><a target="_blank" href="<%=winnersUrl%>"><%=winnersUrl%></a></div>
    <div class="line"><span class="label">Games feed:</span><a target="_blank" href="<%=gamesFeedUrl%>"><%=gamesFeedUrl%></a></div>
</div>
</body>
</html>

<%!
    private String resolveOrigin(javax.servlet.http.HttpServletRequest request) {
        String scheme = request.getScheme();
        String hostHeader = request.getHeader("Host");
        if (!StringUtils.isTrimmedEmpty(hostHeader)) {
            return scheme + "://" + hostHeader;
        }

        String host = request.getServerName();
        int port = request.getServerPort();
        boolean standard = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return standard ? scheme + "://" + host : scheme + "://" + host + ":" + port;
    }

    private String detectProtocol(BankInfo bankInfo) {
        if (!StringUtils.isTrimmedEmpty(bankInfo.getPPClass()) && bankInfo.getPPClass().contains("PTPT")) {
            return "WT";
        }
        if (!StringUtils.isTrimmedEmpty(bankInfo.getPPClass()) && bankInfo.getPPClass().contains("CTPaymentProcessor")) {
            return "CT";
        }
        if (!StringUtils.isTrimmedEmpty(bankInfo.getRefundBetUrl())) {
            return "CW3";
        }
        if (!StringUtils.isTrimmedEmpty(bankInfo.getCWAuthUrl())) {
            return "CW2";
        }
        if (!StringUtils.isTrimmedEmpty(bankInfo.getCWWagerUrl())) {
            return "CW1";
        }
        return "UNKNOWN";
    }

    private String getCwStartGameAction(BankInfo bankInfo) {
        return bankInfo.getSubCasinoId() == 291 ? "btbstartgame.do" : "cwstartgamev2.do";
    }

    private String getEffectiveBankId(BankInfo bankInfo) {
        if (!StringUtils.isTrimmedEmpty(bankInfo.getExternalBankId())) {
            return bankInfo.getExternalBankId();
        }
        return String.valueOf(bankInfo.getId());
    }

    private String resolveUserToken(BankInfo bankInfo) {
        if (bankInfo.getId() == 6274L || bankInfo.getId() == 6275L) {
            return "bav_game_session_001";
        }
        String base = !StringUtils.isTrimmedEmpty(bankInfo.getExternalBankId())
                ? bankInfo.getExternalBankId()
                : String.valueOf(bankInfo.getId());
        return "test_user_" + base;
    }

    private String buildUrl(String origin, String path, String[][] params) {
        StringBuilder sb = new StringBuilder();
        sb.append(origin);
        if (!path.startsWith("/")) {
            sb.append("/");
        }
        sb.append(path);

        if (params != null && params.length > 0) {
            sb.append("?");
            boolean first = true;
            for (String[] pair : params) {
                if (pair == null || pair.length < 2 || pair[0] == null || pair[1] == null) {
                    continue;
                }
                if (!first) {
                    sb.append("&");
                }
                first = false;
                sb.append(urlEncode(pair[0])).append("=").append(urlEncode(pair[1]));
            }
        }
        return sb.toString();
    }

    private String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            return s;
        }
    }

    private String hashWithBonusKey(BankInfo bankInfo, String... params) {
        StringBuilder sb = new StringBuilder();
        if (params != null) {
            for (String p : params) {
                if (p != null) {
                    sb.append(p);
                }
            }
        }
        if (!StringUtils.isTrimmedEmpty(bankInfo.getBonusPassKey())) {
            sb.append(bankInfo.getBonusPassKey());
        }
        return md5(sb.toString());
    }

    private String md5(String source) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(source.getBytes("UTF-8"));
            StringBuilder out = new StringBuilder();
            for (byte b : digest) {
                out.append(String.format("%02x", b & 0xff));
            }
            return out.toString();
        } catch (Exception e) {
            return "";
        }
    }
%>
