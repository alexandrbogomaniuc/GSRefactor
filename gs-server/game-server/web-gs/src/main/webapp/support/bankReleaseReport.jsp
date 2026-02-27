<%@ page import="java.io.StringWriter" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.util.string.StringUtils" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="com.dgphoenix.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.config.HostConfiguration" %>
<%@ page contentType="text/html" pageEncoding="utf-8" %>
<%--
  Author: svvedenskiy
  Date: 10/8/18

  The script generates bank release report

  Usage: https://gs1-gp3.xxx.com/support/bankReleaseReport.jsp?bankId=[BANK_ID]

  Update: kis
  Date: 11/12/20

  Bank range added
  Usage: https://gs1-gp3.xxx.com/support/bankReleaseReport.jsp?bankId=[FROM_BANK_ID-TO_BANK_ID]

--%>

<%
    //TODO: report for bank range
    //TODO: custom integration: ACE GAMING, LUMINIS - NG, BETPLAY, BTOBET
    //TODO: assert(COMMON_WALLET_AUTH_PASS == BONUS_PASS_KEY)
    try {
        writer = response.getWriter();
        useRange = false;

        if (request.getParameter("bankId") != null) {
            String bankRange = request.getParameter("bankId");
            if (bankRange.matches("\\d{2,4}-\\d{2,4}"))
                rangeApply(bankRange);
            if (bankRange.matches("\\d{2,4}\\|.+"))
                rangeApply2(bankRange);
        }

        Long bankId;

        try {
            bankId = useRange ? fromToBanks.get(0) : Long.valueOf(request.getParameter("bankId"));
        } catch (NumberFormatException e) {
            println("Please check 'bankId' parameter value");
            return;
        }
        bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bankInfo == null) {
            println("Bank " + bankId + " not found");
            return;
        }
        protocolType = protocolType(bankInfo);

        detectCustomIntegration();
        printHeader();
        println();

        if (useRange)
            printRangeBankInfo();
        else
            printBankInfo();

        println();
        println();
        printUrls();
    } catch (Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        response.getWriter().write(sw.toString());
    }
%>

</body>
</html>

<%!
    private PrintWriter writer;
    private BankInfo bankInfo;
    private ProtocolType protocolType;
    private boolean useRange = false;
    private List<Long> fromToBanks = new ArrayList<>();
    private static final String DEFAULT_CW_CLIENT_LEGACY =
            "com.abs.casino.payment.wallet.client.v4.StandartRESTCWClient";
    private static final String DEFAULT_CW_CLIENT_ABS =
            "com.abs.casino.payment.wallet.client.v4.StandartRESTCWClient";


    private String getSubcasinoName() {
        return SubCasinoCache.getInstance().get(bankInfo.getSubCasinoId()).getName().toLowerCase().replaceAll(" ", "");
    }

    private String getNameForReport() {
        return useRange ? getSubcasinoName() + "_" + fromToBanks.get(0) + "-" + fromToBanks.get(fromToBanks.size() - 1) + "_" + clusterType()
                : getSubcasinoName() + "_" + bankInfo.getId() + "_" + clusterType();
    }

    void rangeApply(String bankRange) {
        String[] massBanks = bankRange.split("-");
        Long fromBank = Long.valueOf(massBanks[0]);
        Long toBank = Long.valueOf(massBanks[1]);
        BankInfo fromBankInfo = BankInfoCache.getInstance().getBankInfo(fromBank);
        List<Long> banksRange = SubCasinoCache.getInstance().getBankIds(fromBankInfo.getSubCasinoId());
        Collections.sort(banksRange);
        fromToBanks = banksRange.subList(banksRange.indexOf(fromBank), banksRange.indexOf(toBank) + 1);
        useRange = true;
    }
    void rangeApply2(String bankRange)
    {
        String[] massBanks = bankRange.split("\\|");
//        Long fromBank = Long.valueOf(massBanks[0]);
//        Long toBank = Long.valueOf(massBanks[1]);
//        BankInfo fromBankInfo = BankInfoCache.getInstance().getBankInfo(fromBank);
        List<Long> banksRange = new ArrayList<>(); //SubCasinoCache.getInstance().getBankIds(fromBankInfo.getSubCasinoId());
        for (String m : massBanks)
        {
            banksRange.add(Long.valueOf(m));
        }
        Collections.sort(banksRange);
        fromToBanks.addAll(banksRange); //= banksRange.subList(banksRange.indexOf(fromBank), banksRange.indexOf(toBank)+1);
        useRange = true;
    }

    void println() {
        print("<br/>");
    }

    void println(String str) {
        print(str + "<br/>");
    }

    void print(String str) {
        writer.write(str);
    }

    void detectCustomIntegration() {
        final String cwClientClass = bankInfo.getCWRequestClientClass();
        if (!StringUtils.isTrimmedEmpty(cwClientClass) && !isDefaultWalletClient(cwClientClass)) {
            println("WARNING! Custom integration detected!<br/>COMMON_WALLET_REQUEST_CLIENT_CLASS = " + cwClientClass);
            println();
        }
    }

    boolean isDefaultWalletClient(String cwClientClass) {
        return DEFAULT_CW_CLIENT_LEGACY.equals(cwClientClass) || DEFAULT_CW_CLIENT_ABS.equals(cwClientClass);
    }

    String clusterType() {
        final String COPY = "Copy";
        final String LIVE = "Live";
        HostConfiguration hostConfiguration = ApplicationContextHelper.getBean(HostConfiguration.class);
        if (hostConfiguration.isProductionCluster()) {
            return LIVE;
        } else {
            return COPY;
        }
    }

    void printHeader() {
        print("<html><head><title>" + getNameForReport() + "</title></head><body>");
        println("Configured on " + clusterType());
    }

    void printBankInfo() {

        String externalBankId = bankInfo.getExternalBankId();
        long bank_Id = bankInfo.getId();
        String currency = bankInfo.getDefaultCurrency().getCode();
        String bankName = bankInfo.getExternalBankIdDescription();

        boolean check = externalBankId == null || externalBankId.equals("") || externalBankId.equals(String.valueOf(bank_Id));

        String BSGNG = "BSGBankId";

        if (bankDomain().contains("-ng"))
            BSGNG = "NGBankId";

        String trTitle = check ? "<tr><td>BankId</td><td>Site</td><td>Currency</td></tr>"
                : "<tr><td>BankId</td><td>" + BSGNG + "</td><td>Currency</td></tr>";
        String tdText = check ? "<tr><td>" + bank_Id + "</td><td>" + bankName + "</td><td>" + currency + "</td></tr>"
                : "<tr><td>" + externalBankId + "</td><td>" + bank_Id + "</td><td>" + currency + "</td></tr>";

        print("<table>");
        print(trTitle);
        print(tdText);
        print("</table>");
        println();
        println("API: " + protocolType.description());
        println("PASS KEY: " + bankInfo.getAuthPassword());
        println("API Endpoint: " + protocolType.apiEndpoint(bankInfo));
        printMasterInfo();
    }

    void printRangeBankInfo() {
        String externalBankId = bankInfo.getExternalBankId();
        Long bank_Id = bankInfo.getId();

        Set<String> apiUrls = new HashSet<>();

        for (Long bankId : fromToBanks) {
            BankInfo bi = BankInfoCache.getInstance().getBankInfo(bankId);
            ProtocolType protType = protocolType(bi);
            apiUrls.add(protType.apiEndpoint(bi));
        }

        int urlsCount = apiUrls.size();

        String dop = urlsCount > 1 ? "<td>API endpoint</td>" : "";
        String APIEndpoint = urlsCount <= 1 ? "API Endpoint: " + protocolType.apiEndpoint(bankInfo) + "<BR>" : "";

        boolean check = externalBankId == null || externalBankId.equals("") || externalBankId.equals(String.valueOf(bank_Id));

        String BSGNG = "BSGBankId";

        if (bankDomain().contains("-ng"))
            BSGNG = "NGBankId";

        String trTitle = check ? "<tr><td>BankId</td><td>Site</td><td>Currency</td>" + dop + "</tr>"
                : "<tr><td>BankId</td><td>" + BSGNG+"</td><td>Currency</td>" + dop + "</tr>";

        print("<table>");
        print(trTitle);

        for (Long bankId : fromToBanks) {
            BankInfo bi = BankInfoCache.getInstance().getBankInfo(bankId);
            ProtocolType pt = protocolType(bi);

            String col1 = check ? "" + bi.getId() : bi.getExternalBankId();
            String col2 = check ? bi.getExternalBankIdDescription() : "" + bi.getId();
            String col3 = bi.getDefaultCurrency().getCode();
            String col4 = urlsCount > 1 ? "<td>" + pt.apiEndpoint(bi) + "</td>" : "";
            String tdText = "<tr><td>" + col1 + "</td><td>" + col2 + "</td><td>" + col3 + "</td>" + col4 + "</tr>";

            print(tdText);
        }

        print("</table>");

        println();
        println("API: " + protocolType.description());
        println("PASS KEY: " + bankInfo.getAuthPassword());
        print(APIEndpoint);
        printMasterInfoForRange(apiUrls);
    }


    void printMasterInfo() {
        Long masterBankId = bankInfo.getMasterBankId();
        if (masterBankId == null || masterBankId == bankInfo.getId()) {
            return;
        }
        BankInfo masterInfo = BankInfoCache.getInstance().getBankInfo(masterBankId);
        print("MBS: ");

        ProtocolType masterBankProtocolType = protocolType(masterInfo);

        if (protocolType.apiEndpoint(bankInfo).equals(masterBankProtocolType.apiEndpoint(masterInfo))) {
            print("Full;");
        } else {
            print("Partial (different API Endpoint);");
        }
        if (masterInfo.getExternalBankId() == null || masterInfo.getExternalBankId().equals("")
                || masterInfo.getExternalBankId().equals(String.valueOf(masterInfo.getId()))) {
            println(" Master reference: " + masterInfo.getId() + "-" + masterInfo.getExternalBankIdDescription());
        } else {
            println(" Master reference: " + masterInfo.getId()
                    + "-" + masterInfo.getExternalBankId()
                    + "-" + masterInfo.getExternalBankIdDescription());
        }
    }

    void printMasterInfoForRange(Set<String> apiUrls) {
        Long lastBankId = fromToBanks.get(fromToBanks.size() - 1);
        Long masterBankId = BankInfoCache.getInstance().getBankInfo(lastBankId).getMasterBankId();
        if (masterBankId == null) {
            return;
        }

        BankInfo masterInfo = BankInfoCache.getInstance().getBankInfo(masterBankId);
        print("MBS: ");

        if (apiUrls.size() > 1)
            print("Partial (different API Endpoint);");

        else {
            ProtocolType masterBankProtocolType = protocolType(masterInfo);
            if (apiUrls.contains(masterBankProtocolType.apiEndpoint(masterInfo))) {
                print("Full;");
            } else {
                print("Partial (different API Endpoint);");
            }
        }

        if (masterInfo.getExternalBankId() == null || masterInfo.getExternalBankId().equals("")
                || masterInfo.getExternalBankId().equals(String.valueOf(masterInfo.getId()))) {
            println(" Master reference: " + masterInfo.getId() + "-" + masterInfo.getExternalBankIdDescription());
        } else {
            println(" Master reference: " + masterInfo.getId()
                    + "-" + masterInfo.getExternalBankId()
                    + "-" + masterInfo.getExternalBankIdDescription());
        }
    }

    enum ProtocolType {
        //Note: CWv3 == CWv4
        CW3 {
            String description() {
                return "CW v3.07c";
            }

            String authUrl(BankInfo bankInfo) {
                return bankInfo.getCWAuthUrl();
            }

            String historyUrl() {
                return "/cwstarthistory.do?bankId=[BANKID]&token=[VALID_CW_TOKEN]";
            }

            String apiUrls(String domain, BankInfo bankInfo) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append("Common Wallet URLs:").append("<br/>").append("<br/>");
                strBldr.append("Start in guest mode:").append("<br/>");
                strBldr.append(domain).append("/cwguestlogin.do?bankId=[BANK_ID]&gameId=[GAME_ID]&lang=en")
                        .append("<br/>").append("<br/>");
                strBldr.append("Start game:").append("<br/>");
                strBldr.append(domain).append("/" + getCwStartGameAction(bankInfo) + "?bankId=[BANK_ID]&gameId=[GAME_ID]&mode=real&token=[VALID_CW_TOKEN]&lang=en")
                        .append("<br/>").append("<br/>");
                return strBldr.toString();
            }
        },
        CW2 {//bank #2252

            String description() {
                return "CW v2";
            }

            String authUrl(BankInfo bankInfo) {
                return bankInfo.getCWAuthUrl();
            }

            String historyUrl() {
                return "/cwstarthistory.do?bankId=[BANKID]&token=[VALID_CW_TOKEN]";
            }

            String apiUrls(String domain, BankInfo bankInfo) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append("Common Wallet URLs:").append("<br/>").append("<br/>");
                strBldr.append("Start in guest mode:").append("<br/>");
                strBldr.append(domain).append("/cwguestlogin.do?bankId=[BANK_ID]&gameId=[GAME_ID]&lang=en")
                        .append("<br/>").append("<br/>");
                strBldr.append("Start game:").append("<br/>");
                strBldr.append(domain).append("/" + getCwStartGameAction(bankInfo) + "?bankId=[BANK_ID]&gameId=[GAME_ID]&mode=real&token=[VALID_CW_TOKEN]&lang=en")
                        .append("<br/>").append("<br/>");
                return strBldr.toString();
            }
        },
        //CW1 bankId: 76
        CW1 {
            String description() {
                return "CW v1";
            }

            String authUrl(BankInfo bankInfo) {
                return bankInfo.getCWWagerUrl(); //auth url is null
            }

            String historyUrl() {
                return "/cwstarthistory.do?bankId=[BANKID]&token=[VALID_CW_TOKEN]";
            }

            String apiUrls(String domain, BankInfo bankInfo) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append("Common Wallet URLs:").append("<br/>").append("<br/>");
                strBldr.append("Register:").append("<br/>");
                strBldr.append(domain).append("/cwregister.do?bankId=[BANK_ID]&firstName=BonusFirst&lastName=BonusLast&userId=Brandi&username=Brandi&currencyCode=EUR")
                        .append("<br/>").append("<br/>");
                strBldr.append("Login:").append("<br/>");
                strBldr.append(domain).append("/cwlogin.do?bankId=[BANK_ID]&userid=Brandi&balance=100000.00")
                        .append("<br/>").append("<br/>");
                strBldr.append("Start game:").append("<br/>");
                strBldr.append(domain).append("/cwstartgame.do?sessionId=[sessionId]&gameId=[gameId]&mode=[free|real]")
                        .append("<br/>").append("<br/>");
                strBldr.append("Guest mode:").append("<br/>");
                strBldr.append(domain).append("/cwguestlogin.do?bankId=[BANK_ID]&gameId=[gameId]")
                        .append("<br/>").append("<br/>");
                return strBldr.toString();
            }
        },
        CT {
            String description() {
                return "CBT v1.05";
            }

            String authUrl(BankInfo bankInfo) {
                return bankInfo.getCTRESTAuthURL();
            }

            String historyUrl() {
                return "/ctstarthistory.do?bankId=[BANK_ID]&token=[VALID_CT_TOKEN]";
            }

            String apiUrls(String domain, BankInfo bankInfo) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append("Common Transfer URLs:").append("<br/>").append("<br/>");
                strBldr.append("Start in guest mode:").append("<br/>");
                strBldr.append(domain).append("/guestmode.do?bankId=[BANK_ID]&gameId=[GAME_ID]&lang=en")
                        .append("<br/>").append("<br/>");
                strBldr.append("Start game:").append("<br/>");
                strBldr.append(domain).append("/ctenter.do?bankId=[BANK_ID]&gameId=[GAME_ID]&mode=real&token=[TOKEN]&balance=[BALANCE_IN_CENTS]&lang=en")
                        .append("<br/>").append("<br/>");
                return strBldr.toString();
            }
        },
        //WT protocol aka PTPT. bankId: 3341
        WT {
            String description() {
                return "WT Schema";
            }

            String authUrl(BankInfo bankInfo) {
                return bankInfo.getCWAuthUrl();
            }

            String historyUrl() {
                return "/wtstarthistory.do?bankId=[BANK_ID]&token=[VALID_CT_TOKEN]";
            }

            String apiUrls(String domain, BankInfo bankInfo) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append("Launch URLs:").append("<br/>").append("<br/>");
                strBldr.append("Start in guest mode:").append("<br/>");
                strBldr.append(domain).append("/guestmode.do?bankId=[BANK_ID]&gameId=[GAME_ID]&lang=en")
                        .append("<br/>").append("<br/>");
                strBldr.append("Start game:").append("<br/>");
                strBldr.append(domain).append("/wtstartgame.do?token=[TOKEN]&gameId=[GAME_ID]&lang=[LANG]&bankId=[BANK_ID]&mode=real")
                        .append("<br/>").append("<br/>");
                strBldr.append("WT API URLs:<br/>").append(domain).append("/wt/*")
                        .append("<br/>").append("<br/>");
                return strBldr.toString();
            }
        },
        UNKNOWN {
            String description() {
                return "UNKNOWN!";
            }

            String authUrl(BankInfo bankInfo) {
                return "UNKNOWN!";
            }

            String historyUrl() {
                return "/cwstarthistory.do?bankId=[BANKID]&token=[VALID_CW_TOKEN]";
            }

            String apiUrls(String domain, BankInfo bankInfo) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append("Common Wallet URLs:").append("<br/>").append("<br/>");
                strBldr.append("Start in guest mode:").append("<br/>");
                strBldr.append(domain).append("/cwguestlogin.do?bankId=[BANK_ID]&gameId=[GAME_ID]&lang=en")
                        .append("<br/>").append("<br/>");
                strBldr.append("Start game:").append("<br/>");
                strBldr.append(domain).append("/" + getCwStartGameAction(bankInfo) + "?bankId=[BANK_ID]&gameId=[GAME_ID]&mode=real&token=[VALID_CW_TOKEN]&lang=en")
                        .append("<br/>").append("<br/>");
                return strBldr.toString();
            }
        };

        abstract String description();

        abstract String authUrl(BankInfo bankInfo);

        abstract String historyUrl();

        abstract String apiUrls(String domain, BankInfo bankInfo);


        String apiEndpoint(BankInfo bankInfo) {
            if (bankInfo.isStubMode()) {
                return "Stub";
            }
            String authUrl = authUrl(bankInfo);
            int lastIdx;
            lastIdx = authUrl.lastIndexOf("="); //"=" bank 3574
            if (lastIdx >= 0) {
                return authUrl.substring(0, Math.min(authUrl.length() - 1, lastIdx + 1));
            }
            lastIdx = authUrl.lastIndexOf("/");
            if (lastIdx < 0) {
                return authUrl;
            }
            if (lastIdx == authUrl.length() - 1) {
                authUrl = authUrl.substring(0, authUrl.length() - 1);
            }
            return authUrl.substring(0, authUrl.lastIndexOf("/") + 1);
        }

        static String getCwStartGameAction(BankInfo bankInfo) {
            if (bankInfo.getSubCasinoId() == 291) { //BTOBET
                return "btbstartgame.do";
            }
            return "cwstartgamev2.do";
        }
    }

    ProtocolType protocolType(BankInfo bankInfo) {
        //Note: CW3 == CW4

        if (!StringUtils.isTrimmedEmpty(bankInfo.getPPClass()) && bankInfo.getPPClass().contains("PTPT")) {
            return ProtocolType.WT;
        }

        if (!StringUtils.isTrimmedEmpty(bankInfo.getPPClass()) && bankInfo.getPPClass().contains("CTPaymentProcessor")) {
            return ProtocolType.CT;
        }

        if (!StringUtils.isTrimmedEmpty(bankInfo.getRefundBetUrl())) {
            return ProtocolType.CW3;
        }

        if (!StringUtils.isTrimmedEmpty(bankInfo.getCWAuthUrl())) {
            return ProtocolType.CW2;
        } else if (!StringUtils.isTrimmedEmpty(bankInfo.getCWWagerUrl())) {
            return ProtocolType.CW1;
        }

        if (!StringUtils.isTrimmedEmpty(bankInfo.getCTRESTAuthURL())) {
            return ProtocolType.CT;
        }

        return ProtocolType.UNKNOWN;
    }


    void printUrls() {
        println("URLS:");
        println();
        String domain = "https://" + bankDomain();
        printStartGameUrls(domain);
        printBonusUrls(domain);
        printFrbUrls(domain);
        printFeeds(domain);
    }

    void printStartGameUrls(String domain) {
        println(protocolType.apiUrls(domain, bankInfo));
        println();
    }

    void printBonusUrls(String domain) {
        if (StringUtils.isTrimmedEmpty(bankInfo.getBonusReleaseUrl())) {
            return;
        }
        println("OCBonus URLs:");
        println();
        println("1 BonusAward:");
        println(domain + "/bsaward.do?bankId=[BANK_ID]&amount=10000&games=all&hash=235546&extBonusId=1&gameIds=null&userId=12346&expDate=28.04.2012&type=Deposit&multiplier=2");
        println();
        println("2 List of not active bonuses:");
        println(domain + "/bshistory.do?bankId=[BANK_ID]&userId=12346&hash=4324");
        println();
        println("3 List of active bonuses:");
        println(domain + "/bsinfo.do?bankId=[BANK_ID]&userId=12346&hash=4324");
        println();
        println("4 Cancel Bonus:");
        println(domain + "/bscancel.do?bankId=[BANK_ID]&bonusId=40643137&hash=4324");
        println();
        println("5 Check Bonus:");
        println(domain + "/bscheck.do?bankId=[BANK_ID]&extBonusId=1&hash=4324");
        println();
        println("6 Start game in stub mode:");
        println(domain + "/bsstartgame.do?bankId=[BANK_ID]&gameId=210&mode=bonus&bonusId=654647323&token=12346&lang=en");
        println();
        println();
    }

    void printFrbUrls(String domain) {
        if (StringUtils.isTrimmedEmpty(bankInfo.getFRBonusWinURL())) {
            return;
        }
        println("OFRBonus URLs:");
        println();
        println("1 Award OFR Bonus:");
        println(domain + "/frbaward.do?bankId=[BANK_ID]&userId=12346789&rounds=5&games=210|221&extBonusId=001&hash=12345");
        println();
        println("2 Cancel OFR Bonus:");
        println(domain + "/frbcancel.do?bankId=[BANK_ID]&bonusId=50727361&hash=122345");
        println();
        println("3 Get OFR Bonus Info:");
        println(domain + "/frbinfo.do?bankId=[BANK_ID]&userId=12346789&hash=12345");
        println();
        println("4 Check OFR Bonus:");
        println(domain + "/frbcheck.do?bankId=[BANK_ID]&extBonusId=001&hash=1");
        println();
        println("5 Get OFR Bonus History:");
        println(domain + "/frbhistory.do?bankId=[BANK_ID]&userId=12346789&hash=kj");
        println();
        println("6 Get game list with OFRB:");
        println(domain + "/frbgamelist.do?bankId=[BANK_ID]");
        println();
        println();
    }

    void printFeeds(String domain) {
        if (!domain.contains("-aams."))
            printJackPotFeeds(domain);
        println("Winners feed:");
        println(domain + "/winners/winners_[BANKID].xml");
        println();
        println("Games feed:");
        println(domain + "/gamelist.do?bankId=[BANKID]");
        println();
        println("History:");
        println(domain + protocolType.historyUrl());
    }

    void printJackPotFeeds(String domain) {
        println("Jackpot feed:");
        println(domain + "/jackpots/jackpots_[BANKID].xml");
        println();
        println("Jackpot3 feed");
        println(domain + "/jackpots/jackpot3_[BANKID].xml");
        println();
        println("Jackpot4 feed");
        println(domain + "/jackpots/jackpot4_[BANKID].xml");
        println();
    }

    String bankDomain() {
        if (!StringUtils.isTrimmedEmpty(bankInfo.getStartGameDomain())) {
            return bankInfo.getStartGameDomain();
        }
        SubCasino subCasino = SubCasinoCache.getInstance().get(bankInfo.getSubCasinoId());
        return subCasino.getDomainNames().get(0);
    }
%>
