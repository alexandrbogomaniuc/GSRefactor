<%@ page import="com.dgphoenix.casino.bgm.BaseGameHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.IdObject" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankConstants" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.ILimit" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.session.ClientType" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<html>
<head>
    <title></title>
</head>
<body>

<script type="text/javascript">
    function showHide(element_id) {
        if (document.getElementById(element_id)) {
            var obj = document.getElementById(element_id);
            if (obj.style.display != "block") {
                obj.style.display = "block";
            } else obj.style.display = "none";
        } else alert("Элемент с id: " + element_id + " не найден!");
    }
</script>

<%@include file="GameClass.jsp" %>

<%!
    String server_name = null;

    int spoiler_level = 0;

    long[] etalonGames = {};
    ArrayList<Currency> array_currencies = null;

    PrintWriter writer = null;
    boolean isTestMode = true;
    boolean isEtalonCoins = true;
    boolean isEtalonFRB = true;

    boolean isUpdateTemplate = true;
    boolean isUpdateLanguages = true;

    boolean isToGo = false;

    int deliveredCount = 0;
    boolean isAddedPC = false;
    boolean isAddedMobile = false;
    boolean isAddedAndroid = false;
    boolean isAddedWindowsPhone = false;
    String firstGameId = "";

    ArrayList<String> deliveredPCToBanks = new ArrayList<String>();
    ArrayList<String> deliveredIOSToBanks = new ArrayList<String>();
    ArrayList<String> deliveredAndroidToBanks = new ArrayList<String>();
    ArrayList<String> deliveredWindowsPhoneToBanks = new ArrayList<String>();

    String toPlainText(String text) {
        if (text == null) return null;
        return text.replaceAll("\\s", "&nbsp;");
    }

    void writeText(String text) {
        if (writer != null) {
            for (int i = 0; i < spoiler_level; i++)
                writer.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            writer.write(text);
        }
    }

    String[] getParameterPCAndToGo(HttpServletRequest request, String key_pc, String key_mobile, String key_android, String key_windowsphone) throws IOException {
        String[] result = new String[4];
        result[0] = request.getParameter(key_pc);
        result[1] = request.getParameter(key_mobile);
        result[2] = request.getParameter(key_android);
        result[3] = request.getParameter(key_windowsphone);

        return result;
    }

    String getParameter(HttpServletRequest request, String key) throws IOException {
        String result = request.getParameter(key);
        if (result == null || result.equals("null") || result.equals("")) return null;

        return result.trim();
    }

    String[] getArrayParameter(HttpServletRequest request, String key) throws IOException {
        return request.getParameterValues(key);
    }

    String getExternalId(Map<String, IdObject> externalMap, String strGameId) {
        String extId = null;

        for (Map.Entry<String, IdObject> object : externalMap.entrySet()) {
            if (object.getValue().getId() == Long.parseLong(strGameId))
                extId = object.getKey().substring(0, object.getKey().indexOf("+"));
        }

        if (extId == null) extId = "{need external game id}";

        return extId;
    }


    private List<Coin> parseCoins(String strCoins) {
        if (strCoins == null || strCoins.isEmpty() || strCoins.equals("null") || strCoins.contains("(default)"))
            return null;

        ArrayList<Coin> array_coin = new ArrayList<Coin>();

        String[] strCoinsValues = strCoins.split(" ");
        for (String strCoin : strCoinsValues) {
            if (strCoin.isEmpty()) continue;
            long coinValue = (long) (Float.parseFloat(strCoin) * 100);
            Coin coin = CoinsCache.getInstance().getCoinByValue(coinValue);
            array_coin.add(coin);
        }

        return array_coin;
    }

    long[] convertStringArrayToLongArray(String[] array) {
        long[] result = new long[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = Long.parseLong(array[i]);
        }

        return result;
    }


    String coinsToString(List<Coin> coins) {
        if (coins == null) return "null";

        String result = "";

        float[] result_coins = new float[coins.size()];
        for (int i = 0; i < coins.size(); i++) {
            result_coins[i] = coins.get(i).getValue() / 100.0f;
        }

        Arrays.sort(result_coins);

        for (int i = 0; i < result_coins.length; i++) {
            result += String.format("%1.2f", result_coins[i]);
            if (i < result_coins.length - 1) result += ", ";
        }

        return result;
    }

    HashMap<Long, Long> mapUniversalJackpots = new HashMap<Long, Long>();
%>


<%
    mapUniversalJackpots.put(792L, 798L);
    mapUniversalJackpots.put(798L, 792L);       // Reels Of Wealth <-> Faerie Spells

    server_name = request.getServerName();
    spoiler_level = 0;
    array_currencies = null;

    writer = response.getWriter();

    deliveredPCToBanks.clear();
    deliveredIOSToBanks.clear();
    deliveredAndroidToBanks.clear();
    deliveredWindowsPhoneToBanks.clear();

    if (getParameter(request, "game_name") != null) {
        String reqCurrency = getParameter(request, "currency");
        String reqGameName = getParameter(request, "game_name");
        String[] reqCheckedBanks = getArrayParameter(request, "check_banks[]");

        String reqIdPC = getParameter(request, "pc_id");
        String reqIdMobile = getParameter(request, "mobile_id");
        String reqIdAndroid = getParameter(request, "android_id");
        String reqIdWindowsPhone = getParameter(request, "wphone_id");

        String reqGameType = getParameter(request, "game_type");
        String reqGroupType = getParameter(request, "game_grouptype");
        String reqVarType = getParameter(request, "game_vartype");

        String reqDefCoin = getParameter(request, "defcoin");
        String reqIsKeyAcs = getParameter(request, "is_key_acs");
        String reqIsFRB = getParameter(request, "is_frb");
        String reqIsEnabled = getParameter(request, "is_enabled");

        String reqIsJackpot = getParameter(request, "is_jackpot");
        String reqJPMultiplier = getParameter(request, "value_jpmultiplier");
        String reqJPPCRP = getParameter(request, "value_jppcrp");
        String reqJPBCRP = getParameter(request, "value_jpbcrp");

        String reqIsJackpot3 = getParameter(request, "is_jackpot_3");

        String reqPayoutPercent = getParameter(request, "payout_percent");


        String reqIsUnj = getParameter(request, "handle_unj_win");
        String reqUnjLinkedGameId = getParameter(request, "unj_linked_gameid");
        String reqUnjTotalContribution = getParameter(request, "value_unj_totalcontribution");

        String reqUnjName_1 = getParameter(request, "name_jackpot_1");
        String reqUnjName_2 = getParameter(request, "name_jackpot_2");
        String reqUnjName_3 = getParameter(request, "name_jackpot_3");
        String reqUnjName_4 = getParameter(request, "name_jackpot_4");

        String reqUnjBase_1 = getParameter(request, "value_jackpot_1");
        String reqUnjBase_2 = getParameter(request, "value_jackpot_2");
        String reqUnjBase_3 = getParameter(request, "value_jackpot_3");
        String reqUnjBase_4 = getParameter(request, "value_jackpot_4");

        String reqUnjPcrp_1 = getParameter(request, "pcrp_jackpot_1");
        String reqUnjPcrp_2 = getParameter(request, "pcrp_jackpot_2");
        String reqUnjPcrp_3 = getParameter(request, "pcrp_jackpot_3");
        String reqUnjPcrp_4 = getParameter(request, "pcrp_jackpot_4");

        String reqUnjBcrp_1 = getParameter(request, "bcrp_jackpot_1");
        String reqUnjBcrp_2 = getParameter(request, "bcrp_jackpot_2");
        String reqUnjBcrp_3 = getParameter(request, "bcrp_jackpot_3");
        String reqUnjBcrp_4 = getParameter(request, "bcrp_jackpot_4");

        String reqPropCount = getParameter(request, "property_count");

        String reqLimitMin = getParameter(request, "limit_min");
        String reqLimitMax = getParameter(request, "limit_max");
        String reqSwfLocation = getParameter(request, "swfLocation");
        String reqGameControllerClass = getParameter(request, "gameControllerClass");
        String reqServlet = getParameter(request, "servlet");

        String reqMaxWin = getParameter(request, "maxWin");
        String reqRTP = getParameter(request, "RTP");
        String reqPossibleLines = getParameter(request, "possibleLines");
        String reqBetPerLines = getParameter(request, "possibleBetPerLines");

        String reqLinesCount = getParameter(request, "linesCount");
        String reqDefaultNumLines = getParameter(request, "defaultNumLines");
        String reqDefaultBetPerLine = getParameter(request, "defaultBetPerLine");

        String[] reqCheckIDs = {getParameter(request, "check_pc"),
                getParameter(request, "check_mobile"),
                getParameter(request, "check_android"),
                getParameter(request, "check_wphone")};

        String reqRepositoryFile = getParameter(request, "repository_file");
        String reqIsDevelopment = getParameter(request, "is_development");
        String reqEndRoundSignature = getParameter(request, "endRoundSignature");
        String reqTitle = getParameter(request, "title");

        String reqFRBCoin = getParameter(request, "frb_coin");

        String reqTest = getParameter(request, "check_test");
        String reqRoundFinishedHelper = getParameter(request, "roundFinishedHelper");
        String reqJackpot3WinChecker = getParameter(request, "jackpot_3_win_checker");

        String reqCheckTemplate = getParameter(request, "check_template");
        String reqCheckLanguages = getParameter(request, "check_languages");
        String[] reqLanguages = getParameterPCAndToGo(request, "languages_pc", "languages_mobile", "languages_android", "languages_windowsphone");
        String reqCoinsPC = getParameter(request, "coins_pc");
        String reqGameTesting = getParameter(request, "game_testing");

        String reqHtml5VersionMode = getParameter(request, "html5PcVersionMode");
        String reqUnifiedLocation = getParameter(request, "unifiedLocation");

        String reqAdditionalFlashVars = getParameter(request, "additionalFlashVars");
        String reqPdfRulesName = getParameter(request, "pdfRulesName");


        String reqEtalonGames = getParameter(request, "etalon_games");
        if (reqEtalonGames != null && !reqEtalonGames.isEmpty()) {
            etalonGames = convertStringArrayToLongArray(reqEtalonGames.split(", "));
        }

        String reqCurrencies = getParameter(request, "currencies");
        if (reqCurrencies != null && !reqCurrencies.isEmpty()) {
            String[] strArrayCurrencies = reqCurrencies.split(", ");

            if (strArrayCurrencies.length > 0 && !strArrayCurrencies[0].isEmpty()) {
                array_currencies = new ArrayList<Currency>();
                for (int i = 0; i < strArrayCurrencies.length; i++) {
                    Currency currency = CurrencyCache.getInstance().getObject(strArrayCurrencies[i]);
                    if (currency != null)
                        array_currencies.add(currency);
                }
            }

        }

        String reqEtalonJackpot = getParameter(request, "check_etalon_jackpot");
        String reqEtalonCoins = getParameter(request, "check_etalon_coins");
        String reqEtalonFRB = getParameter(request, "check_etalon_frb");
        String reqEtalonACS = getParameter(request, "check_etalon_acs");

        isEtalonCoins = (reqEtalonCoins != null && reqEtalonCoins.equals("on"));
        isEtalonFRB = (reqEtalonFRB != null && reqEtalonFRB.equals("on"));

        isUpdateTemplate = (reqCheckTemplate != null && reqCheckTemplate.equals("on"));
        isUpdateLanguages = (reqCheckLanguages != null && reqCheckLanguages.equals("on"));

        String[] reqArrayJp3persent = null;
        String[] reqArrayJp3startMin = null;
        String[] reqArrayJp3startMax = null;

        if (reqIsJackpot3.equals("TRUE")) {
            reqArrayJp3persent = getArrayParameter(request, "value_jp3persent[]");
            reqArrayJp3startMin = getArrayParameter(request, "value_jp3min[]");
            reqArrayJp3startMax = getArrayParameter(request, "value_jp3max[]");
        }

        isTestMode = (reqTest != null && reqTest.equals("on"));

        int property_count = (reqPropCount != null && !reqPropCount.isEmpty()) ? Integer.parseInt(reqPropCount) : 0;

        String reqCDNSupport = getParameter(request, "cdn_support");
        String reqLGA_Approved = getParameter(request, "lga_approved");

        String reqGameWithProgress = getParameter(request, "game_progress");
        String reqGameWithDoubleUp = getParameter(request, "game_dblup");

        String[] reqSideJPidsPC = getParameterPCAndToGo(request, "side_jp_ids_pc", "side_jp_ids_mobile", "side_jp_ids_android", "side_jp_ids_windowsphone");
        String[] reqFakeIdForPC = getParameterPCAndToGo(request, "fake_id_for_pc", "fake_id_for_mobile", "fake_id_for_android", "fake_id_for_windowsphone");

        String reqTemplateJPMultiplier = getParameter(request, "jp_multiplier");
        String reqPCRDef = getParameter(request, "pcr_def");

        String reqJPFrequency = getParameter(request, "jp_frequency");
        String reqJPName = getParameter(request, "jp_name");
        //////////////////////////////////////////////////////////////////////////////////////////////////

        if (reqCheckedBanks == null) reqCheckedBanks = new String[]{};
        long[] banks = convertStringArrayToLongArray(reqCheckedBanks);

        Game game = new Game(reqGameName);
        game.setCoins(parseCoins(reqCoinsPC));
        game.setIds(reqIdPC, reqIdMobile, reqIdAndroid, reqIdWindowsPhone);
        game.setType(reqGameType, reqGroupType, reqVarType);
        game.setDefCoin(reqDefCoin);
        game.setPayoutPercent(reqPayoutPercent);
        game.setFlags(reqIsKeyAcs, reqIsEnabled, reqIsJackpot);
        game.setJackPotInfo(reqJPMultiplier, reqJPPCRP, reqJPBCRP, null);

        game.setUnjInfo(reqIsUnj, reqUnjTotalContribution,
                reqUnjName_1, reqUnjName_2, reqUnjName_3, reqUnjName_4,
                reqUnjBase_1, reqUnjBase_2, reqUnjBase_3, reqUnjBase_4,
                reqUnjPcrp_1, reqUnjPcrp_2, reqUnjPcrp_3, reqUnjPcrp_4,
                reqUnjBcrp_1, reqUnjBcrp_2, reqUnjBcrp_3, reqUnjBcrp_4);
        game.setUnjLinkedGame(reqUnjLinkedGameId);


        game.setLimit(reqLimitMin, reqLimitMax);
        game.setCheckIDs(reqCheckIDs);
        game.setFRB(reqIsFRB, reqFRBCoin);
        game.setGameTesting(reqGameTesting);

        game.setTitle(reqTitle);
        game.setRepositoryFile(reqRepositoryFile);
        game.setSwfLocation(reqSwfLocation);
        game.setGameControllerClass(reqGameControllerClass);
        game.setServletName(reqServlet);
        game.setDevelopmentKey(reqIsDevelopment);
        game.setRoundFinishedHelper(reqRoundFinishedHelper);
        game.setEndRoundSignature(reqEndRoundSignature);
        game.setMaxWin(reqMaxWin);
        game.setRTP(reqRTP);
        game.setDefaultBetPerLine(reqDefaultBetPerLine);
        game.setDefaultNumLines(reqDefaultNumLines);
        game.setLinesCount(reqLinesCount);
        game.setPossibleBetPerLines(reqBetPerLines);
        game.setPossibleLines(reqPossibleLines);

        game.setHtml5VersionMode(reqHtml5VersionMode, reqUnifiedLocation);

        game.setPdfRulesName(reqPdfRulesName);
        game.setAdditionalFlashVars(reqAdditionalFlashVars);


        game.setCDNSupport(reqCDNSupport);
        game.setLGA_Approved(reqLGA_Approved);
        game.setGameWithProgress(reqGameWithProgress);
        game.setGameWithDoubleUp(reqGameWithDoubleUp);

        if (game.isJackPot) {
            game.setTemplateJPMultiplier(reqTemplateJPMultiplier);
            game.setPCRDef(reqPCRDef);
            game.setSideJPids(reqSideJPidsPC);
            game.setFakeIdFor(reqFakeIdForPC);
            game.setJPFrequency(reqJPFrequency);
            game.setJPName(reqJPName);
        }

        game.setLanguages(reqLanguages);

        for (int i = 0; i < property_count; i++) {
            String tempKey = request.getParameter("prop_key_" + i);
            String tempValue = request.getParameter("prop_value_" + i);
            String tempOnlyFlash = request.getParameter("prop_flash_" + i);

            game.addToProperties(tempKey, tempValue, tempOnlyFlash);
        }


        if (isUpdateTemplate)
            applyTemplate(game);

        applyFRB(game);

        isToGo = false;
        for (long bankId : banks) {
            startSpoiler(writer, "block_" + bankId, "Выдача на банк " + bankId);

            if (bankId == banks[0]) {
                deliveredCount = 0;

                Currency defCurrency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
                if (reqCurrency != null)
                    defCurrency = CurrencyCache.getInstance().get(reqCurrency);

                IBaseGameInfo destPC = null;
                IBaseGameInfo destMobile = null;
                IBaseGameInfo destAndroid = null;
                IBaseGameInfo destWP = null;

                if (game.isChecked(0)) destPC = BaseGameCache.getInstance().getGameInfoShared(bankId, game.gameIds[0], defCurrency);
                if (game.isChecked(1)) destMobile = BaseGameCache.getInstance().getGameInfoShared(bankId, game.gameIds[1], defCurrency);
                if (game.isChecked(2)) destAndroid = BaseGameCache.getInstance().getGameInfoShared(bankId, game.gameIds[2], defCurrency);
                if (game.isChecked(3)) destWP = BaseGameCache.getInstance().getGameInfoShared(bankId, game.gameIds[3], defCurrency);

                if ((destPC != null || !game.isChecked(0)) && (
                        (destMobile == null && (game.isChecked(1))) &&
                                (destAndroid == null && (game.isChecked(2))) &&
                                (destWP == null && (game.isChecked(3))))) {
                    isToGo = true;
                }

                isAddedPC = false;
                isAddedMobile = false;
                isAddedAndroid = false;
                isAddedWindowsPhone = false;

                IBaseGameInfo baseGamePC = (game.gameIds[0] == -1) ? null : BaseGameCache.getInstance().getGameInfoById(bankId, game.gameIds[0], defCurrency);
                IBaseGameInfo baseGameIOS = (game.gameIds[1] == -1) ? null : BaseGameCache.getInstance().getGameInfoById(bankId, game.gameIds[1], defCurrency);
                IBaseGameInfo baseGameAND = (game.gameIds[2] == -1) ? null : BaseGameCache.getInstance().getGameInfoById(bankId, game.gameIds[2], defCurrency);
                IBaseGameInfo baseGameWP = (game.gameIds[3] == -1) ? null : BaseGameCache.getInstance().getGameInfoById(bankId, game.gameIds[3], defCurrency);

                if (baseGamePC == null && game.isChecked(0)) {
                    isAddedPC = true;
                    deliveredCount += 1;
                }
                if (baseGameIOS == null && game.isChecked(1)) {
                    isAddedMobile = true;
                    deliveredCount += 1;
                }
                if (baseGameAND == null && game.isChecked(2)) {
                    isAddedAndroid = true;
                    deliveredCount += 1;
                }
                if (baseGameWP == null && game.isChecked(3)) {
                    isAddedWindowsPhone = true;
                    deliveredCount += 1;
                }

                // Nothing new delivered ///////////////////////////////////////////////////////////////////////////////
                if ((!isAddedPC) && (!isAddedMobile) && (!isAddedAndroid) && (!isAddedWindowsPhone)) {
                    isAddedPC = true;
                    isAddedMobile = true;
                    isAddedAndroid = true;
                    isAddedWindowsPhone = true;
                    deliveredCount = 4;
                }
                ////////////////////////////////////////////////////////////////////////////////////////////////////////

                if (isAddedPC) firstGameId = reqIdPC;
                else if (isAddedMobile) firstGameId = reqIdMobile;
                else if (isAddedAndroid) firstGameId = reqIdAndroid;
                else if (isAddedWindowsPhone) firstGameId = reqIdWindowsPhone;
            }

            boolean canAddGame = true;
            BankInfo destBank = BankInfoCache.getInstance().getBankInfo(bankId);

            if (game.isFRB && destBank.getSubCasinoId() == 40 || destBank.getSubCasinoId() == 19) // GTBETS || SPORTSINTERATION
                addToBankFRB(game.gameIds, bankId);

            for (Currency currency : ((array_currencies != null && array_currencies.size() > 0) ? array_currencies : destBank.getCurrencies())) {
                boolean isCanAddToWallet = false;

                if (etalonGames.length == 0)
                    isCanAddToWallet = true;

                for (long etalonID : etalonGames) {
                    IBaseGameInfo etalonGame = BaseGameCache.getInstance().getGameInfoShared(bankId, etalonID, currency);
                    if (etalonGame != null) {
                        isCanAddToWallet = true;
                    }
                }

                if (isCanAddToWallet || destBank.getDefaultCurrency().getCode().equals(currency.getCode())) {
                    startSpoiler(writer, "currency_" + bankId + "_" + currency.getCode(), "Валюта: " + currency.getCode());

                    game.setCoins(parseCoins(reqCoinsPC));
                    game.setLimit(reqLimitMin, reqLimitMax);
                    game.setDefCoin(reqDefCoin);
                    game.setPayoutPercent(reqPayoutPercent);
                    game.setFRB(reqIsFRB, reqFRBCoin);
                    game.setFlags(reqIsKeyAcs, reqIsEnabled, reqIsJackpot);

                    for (long etalonID : etalonGames) {
                        IBaseGameInfo etalonGame = BaseGameCache.getInstance().getGameInfoShared(bankId, etalonID, currency);
                        BaseGameInfoTemplate etalonTemplate = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(etalonID);

                        if (etalonGame != null) { // Set etalon parameters
                            writeText("<b>ETALON Game:</b> " + etalonGame.getName() + "<br>");

                            if (etalonGame.getProperty("CDN_URL") != null) {
                                writeText("<b>ETALON CDN_URL:</b> " + etalonGame.getProperty("CDN_URL") + "<br>");
                                game.setCDN_URL(etalonGame.getProperty("CDN_URL"));
                            }

                            if (isEtalonCoins) {
                                if (game.varType == GameVariableType.COIN) {
                                    List<Coin> etalon_coins = etalonGame.getCoins();
                                    String etalon_gameDefCoin = (etalonGame.getDefaultCoin() != null ? String.valueOf(etalonGame.getDefaultCoin()) : null);

                                    writeText("<b>ETALON coins:</b> " + coinsToString(etalon_coins) + "<br>");
                                    writeText("<b>ETALON defCoin:</b> " + etalon_gameDefCoin + "<br>");

                                    game.setCoins(etalon_coins);
                                    game.setDefCoin(etalon_gameDefCoin);
                                } else {
                                    ILimit etalon_Limit = etalonGame.getLimit();
                                    game.setLimit((Limit) etalon_Limit);

                                    writeText("<b>ETALON limit:</b> " + (etalon_Limit != null ? etalon_Limit.toString() : null) + "<br>");

                                    game.setRouletteLimits(null);
                                    if (etalonGame.getName().contains("ROULETTE")) {
                                        game.setRouletteLimits((BaseGameInfo) etalonGame);
                                        writeText("<b>ETALON ROULETTE limits:</b> " + Arrays.toString(game.rouletteLimits) + "<br>");
                                    }

                                    game.setChipValues(null);
                                    String chipValues = etalonGame.getProperty("CHIPVALUES");
                                    if (chipValues != null) {
                                        game.setChipValues(chipValues);
                                        writeText("<b>ETALON CHIPVALUES:</b> " + chipValues + "<br>");
                                    }
                                }
                            }

                            if (game.isFRB && isEtalonFRB) {
                                boolean etalon_isFRB = BaseGameInfoTemplateCache.getInstance().getFrbGames().contains(etalonID);
                                String etalon_frbCoin = etalonGame.getProperty(BaseGameConstants.KEY_FRB_COIN);

                                if (etalon_frbCoin == null) etalon_frbCoin = "";

                                game.setFRB((etalon_isFRB ? "TRUE" : "FALSE"), etalon_frbCoin);

                                writeText("<b>ETALON isFRB:</b> " + etalon_isFRB + "<br>");
                                writeText("<b>ETALON frbCoin:</b> " + etalon_frbCoin + "<br>");
                            }
                            break;
                        }
                    }

                    // Check EtalonToGo Parameters /////////////////////////////////////////////////////////////////////
                    game.setCDN_URL_ToGo(null);
                    String toGoCDN_URL = null;

                    for (long etalonID : etalonGames) {
                        BaseGameInfoTemplate etalonTemplate = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(etalonID);

                        if (etalonTemplate != null) {
                            String strEtalonIdToGo = etalonTemplate.getDefaultGameInfo().getProperty("IOSMOBILE");

                            if (strEtalonIdToGo != null) {
                                IBaseGameInfo etalonGameToGo = BaseGameCache.getInstance().getGameInfoShared(bankId, Long.parseLong(strEtalonIdToGo), currency);

                                if (etalonGameToGo != null) {
                                    toGoCDN_URL = etalonGameToGo.getProperty("CDN_URL");
                                    game.setCDN_URL_ToGo(toGoCDN_URL);
                                    writeText("<b>ETALON CDN_URL ToGo:</b> " + toGoCDN_URL + "<br>");
                                    break;
                                }
                            }

                        }
                    }
                    ////////////////////////////////////////////////////////////////////////////////////////////////////


                    for (int i = 0; i < game.gameIds.length; i++) {
                        if (game.isChecked(i)) {
                            long gameId = game.gameIds[i];
                            IBaseGameInfo destGame = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, currency);
                            if (destGame == null) continue;

                            if (!destGame.getName().contains(reqGameName)) {
                                canAddGame = false;
                                writeText("<b>ОШИБКА! Невозможно перезаписать ID(" + gameId + ") игры, так как он уже занят другой игрой: \"" + destGame.getName() + "\", и не соответствует: \"" + reqGameName + "\"! </b><br>");
                                break;
                            }
                        }
                    }

                    if (destBank.getMasterBankId() != null && destBank.getMasterBankId() != bankId) // Slave
                        canAddGame = false;

                    if (canAddGame) {
                        applyBankAndCurrency(game, bankId, currency);
                    }

                    endSpoiler();
                }
            }

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            endSpoiler();
        }

        if (reqCheckedBanks.length > 0) {
%>
<HR>
<H2>Отчет:</H2> <%
    int deliveredIndex = 0;

    String[] showBanks = new String[reqCheckedBanks.length];
    for (int i = 0; i < reqCheckedBanks.length; i++) {
        long bankId = Long.parseLong(reqCheckedBanks[i]);
        showBanks[i] = BankInfoCache.getInstance().getExternalBankId(bankId);
        if (showBanks[i].isEmpty()) showBanks[i] = String.valueOf(bankId);
    }

    String strBanks = Arrays.toString(showBanks);
    strBanks = strBanks.substring(1, strBanks.length() - 1);

    long subId = BankInfoCache.getInstance().getBankInfo(banks[0]).getSubCasinoId();
    String subCasinoName = SubCasinoCache.getInstance().get(subId).getName();
    if (subCasinoName == null)
        subCasinoName = SubCasinoCache.getInstance().get(subId).getStaticDirectoryName();

    String gameType = game.getTitle(0);
    String delimeter = ", ";

    if (BankInfoCache.getInstance().getBankInfo(Long.parseLong(reqCheckedBanks[0])).isUseSingleGameIdForAllDevices() || game.isSingleGameId()) {
        delimeter = "|";

        if (game.isSingleGameId()) {
            isAddedPC = true;
            isAddedMobile = true;
            isAddedAndroid = true;
            isAddedWindowsPhone = true;
            deliveredCount = 4;
        }
    }

    if (!game.isSingleGameId()) {
        if (isAddedPC && (isAddedMobile || isAddedAndroid || isAddedWindowsPhone)) gameType += " PC/ToGo";
        else if (!isAddedPC && (isAddedMobile || isAddedAndroid || isAddedWindowsPhone)) gameType += " ToGo";
    }
%>
<b>Company Name:</b> <%=subCasinoName.toUpperCase()%><br>
<b>Bank(s):</b> <%=strBanks%><br>
<b>Game Type:</b> <%=gameType%><br>
<b>Platform(s):</b>
<% if (isAddedPC) { %>PC<% if (++deliveredIndex < deliveredCount) { %><%=delimeter%><%
        }
    }
%>
<% if (isAddedMobile) { %>Mobile<% if (++deliveredIndex < deliveredCount) { %><%=delimeter%><%
        }
    }
%>
<% if (isAddedAndroid) { %>Android<% if (++deliveredIndex < deliveredCount) { %><%=delimeter%><%
        }
    }
%>
<% if (isAddedWindowsPhone) { %>WinPhone<% if (deliveredIndex < deliveredCount) { %>  <%
        }
    }
%>
<br>

<% deliveredIndex = 0; %>

<b>GameID(s):</b>
<% if (!game.isSingleGameId()) { %>
<% if (isAddedPC && game.gameIds[0] != -1) { %> <%=game.gameIds[0]%> <%if (++deliveredIndex < deliveredCount) { %>, <%
        }
    }
%>
<% if (isAddedMobile && game.gameIds[1] != -1) { %> <%=game.gameIds[1]%> <%if (++deliveredIndex < deliveredCount) { %>, <%
        }
    }
%>
<% if (isAddedAndroid && game.gameIds[2] != -1) { %> <%=game.gameIds[2]%> <%if (++deliveredIndex < deliveredCount) { %>, <%
        }
    }
%>
<% if (isAddedWindowsPhone && game.gameIds[3] != -1) { %> <%=game.gameIds[3]%> <%if (deliveredIndex < deliveredCount) { %>  <%
        }
    }
%>
<%} else {%>
<%=game.gameIds[0]%>
<%}%>
<br>

<%
    String lang = "en";
    boolean isShowLanguagesPC = (isAddedPC && game.langugages[0] != null && game.langugages[0].size() > 1);

    if (reqLanguages[0].equals(reqLanguages[1]) || game.isSingleGameId()) {
        lang = game.langugages[0].get(0); %>
<b>Language(s): </b> <%=reqLanguages[0]%> <br>
<%
} else if (isShowLanguagesPC) {
    lang = game.langugages[0].get(0);
%>
<b>Language(s) PC: </b> <%=reqLanguages[0]%> <br>
<%}%>
<%
    boolean isShowLanguagesToGo = (isAddedMobile || isAddedAndroid || isAddedWindowsPhone) && (game.langugages[1] != null && game.langugages[1].size() > 1);

    if (reqLanguages[0].equals(reqLanguages[1])) {
%>
<%
} else if (isShowLanguagesToGo) {
    if (!isShowLanguagesPC) {
        lang = game.langugages[1].get(0);
    } %>
<b>Language(s) ToGo: </b> <%=reqLanguages[1]%> <br>
<%}%>

<b>Link Example(s):</b><br>
<%
    String bankDomainName = null;

    List<String> addDomains = SubCasinoCache.getInstance().get(subId).getDomainNames();
    for (String domain : addDomains) {
        if (domain.contains("-")) {
            bankDomainName = domain.intern();
            break;
        }
    }

    String link = "https://" + bankDomainName + "/cwguestlogin.do?bankId=" + showBanks[0] + "&gameId=" + firstGameId + "&lang=" + lang;

    if (subCasinoName != null) {
        if (subId == 64L || subId == 90L) {// "GAMESCALE" || "CasinoSaga")
%>
<div><a href=<%=link%>><%=link%>
</a></div>
<%

    link = "https://" + bankDomainName + "/cwguestlogin.do?bankId=" + showBanks[0] + "&gameId=" + firstGameId + "&lang=" + lang;
%>
<div><a href=<%=link%>><%=link%>
</a></div>
<%
} else if (subId == 85L || subId == 203L || subId == 210L || subId == 45L || subId == 234L) { // "ITECHGAMING" || "Vietbet" || "AllWinCity" || "OPERIA" || "LazyBug")
    link = "https://" + bankDomainName + "/cwguestlogin.do?bankId=" + showBanks[0] + "&gameId=" + firstGameId + "&lang=" + lang;
    String lobbyLink = "https://" + bankDomainName + "/cwgueststlobby.do?bankId=" + showBanks[0] + "&lang=" + (subCasinoName.equals("ITECHGAMING") ? "zh-cn" : lang);
%>
<div><a href=<%=link%>><%=link%>
</a></div>
<div><a href=<%=lobbyLink%>><%=lobbyLink%>
</a></div>
<%
} else if (subId == 171L) { // "DemoCasino"
    link = "https://" + bankDomainName + "/cwguestlogin.do?bankId=" + showBanks[0] + "&gameId=" + firstGameId + "&lang=" + lang;
    String lobbyLink = "https://" + bankDomainName + "/demolobby.do";
    String mobileLobbyLink = "https://" + bankDomainName + "/demomobile.do";
%>
<div><a href=<%=link%>><%=link%>
</a></div>
<div><a href=<%=lobbyLink%>><%=lobbyLink%>
</a></div>
<div><a href=<%=mobileLobbyLink%>><%=mobileLobbyLink%>
</a></div>
<%
} else if (subId == 3L) { // SPORTSBOOK
    Integer extId = BaseGameInfoTemplateCache.getInstance().getGameVariationId(Long.parseLong(firstGameId), ClientType.FLASH);
    link = "https://" + bankDomainName + "/login.do?siteID=" + showBanks[0] + "&game_mode=free&game=" + (extId != null ? extId : "{Need_External_GameId}") + "&lang=en";
%>
<div><a href=<%=link%>><%=link%>
</a></div>
<%
} else if (subId == 29L) { //"MrGreen"
    link = "https://" + bankDomainName + "/cwguestlogin.do?bankId=" + showBanks[0] + "&gameId=" + "{Need_Set_External_GameName_As_Text}" + "&lang=" + lang;
%>
<div><a href=<%=link%>><%=link%>
</a></div>
<%
} else if (subId == 28L) { // "RoyalPK"
    link = "https://" + bankDomainName + "/launchGameFree.servlet?ln=en&gameName={external_gamename}";
%>
<div><a href=<%=link%>><%=link%>
</a></div>
<%
} else if (subId == 31L) { // "STREAMTECH"
    link = "https://" + bankDomainName + "/cwguestlogin.do?bankId=" + showBanks[0] + "&gameId=" + firstGameId + "&CDN=GLOBAL&lang=" + lang;
%>
<div><a href=<%=link%>><%=link%>
</a></div>
<%
} else if (subId == 83L) { // "ACE GAMING"
    IdObject externalObject = ExternalGameIdsCache.getInstance().get(String.valueOf(banks[0]));
    String extId = String.valueOf(externalObject.getId());
    link = "https://" + bankDomainName + "/cwguestlogin.do?bankId=" + showBanks[0] + "&gameId=" + extId + "&lang=" + lang;
%>
<div><a href=<%=link%>><%=link%>
</a></div>
<%

    for (String bank : showBanks) {
        if (bank.equals("919")) {
            extId = String.valueOf(ExternalGameIdsCache.getInstance().get("919").getId());

            link = "https://" + bankDomainName + "/cwguestlogin.do?bankId=919&gameId=" + extId + "&lang=" + lang;
%>
<div><a href=<%=link%>><%=link%>
</a></div>
<%
        }
    }
} else {
%>
<div><a href=<%=link%>><%=link%>
</a></div>
<%
        }
    }

    String strDeliveredBanksPC = deliveredPCToBanks.toString().replace("[", "").replace("]", "");
    String strDeliveredBanksIOS = deliveredIOSToBanks.toString().replace("[", "").replace("]", "");
    String strDeliveredBanksAndroid = deliveredAndroidToBanks.toString().replace("[", "").replace("]", "");
    String strDeliveredBanksWindowsPhone = deliveredWindowsPhoneToBanks.toString().replace("[", "").replace("]", "");
%>
<HR>
<b>Банки, на которые были выданы игры:</b> <BR>
<% if (game.isChecked(0)) { %><label <% if (!strBanks.equals(strDeliveredBanksPC          )){ %>style="font-weight: bold; color: #ff0000;" <%}%>><b>Bank
    &nbsp;PC:</b> <%=strDeliveredBanksPC          %>
</label><br> <%}%>
<% if (game.isChecked(1)) { %><label <% if (!strBanks.equals(strDeliveredBanksIOS         )){ %>style="font-weight: bold; color: #ff0000;" <%}%>><b>Bank
    IOS:</b> <%=strDeliveredBanksIOS         %>
</label><br> <%}%>
<% if (game.isChecked(2)) { %><label <% if (!strBanks.equals(strDeliveredBanksAndroid     )){ %>style="font-weight: bold; color: #ff0000;" <%}%>><b>Bank
    AND:</b> <%=strDeliveredBanksAndroid     %>
</label><br> <%}%>
<% if (game.isChecked(3)) { %><label <% if (!strBanks.equals(strDeliveredBanksWindowsPhone)){ %>style="font-weight: bold; color: #ff0000;" <%}%>><b>Bank
    &nbsp;WP:</b> <%=strDeliveredBanksWindowsPhone%>
</label><br> <%}%>

<HR>
<b>Ссылки на игры в банках:</b> <BR>
<%
    for (long bankId : banks) {
%>
<div><a href="/support/loadgameinfo.do?bankId=<%=bankId%>&curCode=default&gameId=<%=reqIdPC%>"> Game <%=reqIdPC%> in bank <%=bankId%>
</a></div>
<%
    }

    String strScheme = request.getScheme();
    String strServerName = request.getServerName();
%>
<HR>
<b>Ссылки для проверки выдачи:</b> <BR>
<a href="<%=strScheme%>://<%=strServerName%>/support/showGamesDetailsProp3.jsp?bankId=<%=getStringBankList(reqCheckedBanks, "|")%>&gameId=<%=getStringGameList(game)+"180|468|469|470|178|406|407|408|277|318|319|368"%>&prop=KEY_ACS_ENABLED|FRB_COIN|CDN_URL">
    showGamesDetailsProp3 </a> <BR>
<a href="<%=strScheme%>://<%=strServerName%>/support/GameConfig/getGamesConfigByBanks.jsp?banks=<%=getStringBankList(reqCheckedBanks, ",")%>&editmode=true">
    getGamesConfigByBanks </a> <BR>

<% if (game.isJackPot) { %>
<a href="<%=strScheme%>://<%=strServerName%>/support/games/listJackPotGamesParamsv2.jsp?bankId=<%=reqCheckedBanks[0]%>">Jackpot Percents</a> <BR>
<%
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
%>


<%!
    String getStringBankList(String[] strCheckedBanks, String separator) {
        String result = "";
        for (int i = 0; i < strCheckedBanks.length; i++) {
            result += strCheckedBanks[i];
            if (i < strCheckedBanks.length - 1 && i < 50 - 1) result += separator;
            else break;
        }
        return result;
    }

    String getStringGameList(Game game) {
        String result = "";

        for (int i = 0; i < game.gameIds.length; i++) {
            if (game.gameIds[i] != -1)
                result += game.gameIds[i] + "|";
        }

        if (result.endsWith("\\|")) result = result.substring(0, result.length() - 1);

        return result;
    }


    void applyTemplate(Game game) throws CommonException {
        for (int i = 0; i < 4; i++) {
            if (game.gameIds[i] == -1 || !game.isChecked(i)) continue;

            BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(game.gameIds[i]);

            startSpoiler(writer, "template_" + game.gameIds[i], ((template == null) ? "Создание темплейта: " : "Обновление темплейта: ") + game.gameIds[i]);

            HashMap<String, String> properties = buildTemplateProperties(game, template, i);

            String repFile = properties.get("REPOSITORY_FILE");

            writeText("<b>Name:                     </b>" + game.getFullName(i) + "<br>");
            writeText("<b>Title:                    </b>" + toPlainText(game.getTitle(i)) + "<br>");
            writeText("<b>servlet:                  </b>" + game.getServlet(i) + "<br>");
            writeText("<b>repositoryFile:           </b>" + ((repFile != null || template == null) ? repFile : template.getDefaultGameInfo().getRepositoryFile()) + "<br>");
            writeText("<b>isDevelopmentKey:         </b>" + game.isDevelopmentKey + "<br>");
            writeText("<b>swfLocation:              </b>" + game.getSWFLocation() + "<br>");
            writeText("<b>gameControllerClass:      </b>" + game.getGameControllerClass() + "<br>");
            writeText("<b>roundFinishedHelper:      </b>" + game.roundFinishedHelper + "<br>");
            writeText("<b>endRoundSignature:        </b>" + game.endRoundSignature + "<br>");
            writeText("<b>html5VersionMode:         </b>" + game.html5VersionMode + "<br>");
            writeText("<b>unifiedLocation:          </b>" + game.unifiedLocation + "<br>");
            writeText("<b>additionalFlashVars:      </b>" + game.additionalFlashVars + "<br>");
            writeText("<b>pdfRulesName:             </b>" + game.pdfRulesName + "<br>");

            writeText("<br>");

            for (String key : properties.keySet()) {
                String value = properties.get(key);
                writeText("<b>" + key + ":      </b>" + value + "<br>");
            }

            if (!isTestMode) {
                if (template == null) {
                    createGameInTemplate(game, properties, i);
                } else {
                    updateGameInTemplate(game, template, properties, i);
                }
            }

            endSpoiler();
        }
        writeText("<br>");
    }


    void applyFRB(Game game) throws CommonException {
        if (game.title != null && !game.title.equals("")) {
            if (game.isFRB)
                addToGlobalFRB(game);
            else
                removeFromGlobalFRB(game);
        }
    }

    void addToGlobalFRB(Game game) throws CommonException {
        Set<Long> listFRB = new HashSet<Long>();
        listFRB.addAll(BaseGameInfoTemplateCache.getInstance().getFrbGames());

        String strAddFRBGames = exportMissedGames(game, listFRB);
        if (strAddFRBGames.isEmpty()) return;

        startSpoiler(writer, "frb", "Добавление игр в список FRB: " + strAddFRBGames);

        writeText("<b>Список FRB до изменений:</b><br>" + (new TreeSet(listFRB)) + "<br>");
        writeText("<br>");

        for (int i = 0; i < game.gameIds.length; i++) {
            if (!listFRB.contains(game.gameIds[i]) && game.isChecked(i)) {
                writeText("<b>Добавлен в FRB:</b> " + game.gameIds[i] + "<br>");
                listFRB.add(game.gameIds[i]);
            } else continue;
        }

        writeText("<br>");
        writeText("<b>Список FRB после изменений:</b><br>" + (new TreeSet(listFRB)) + "<br>");

        if (!isTestMode) {
            BaseGameInfoTemplateCache.getInstance().setFrbGames(listFRB);
        }
        for (int i = 0; i < game.gameIds.length; i++) {
            if (game.isChecked(i)) {
                BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(game.gameIds[i]);
                template.setFrbGame(true);
                RemoteCallHelper.getInstance().saveAndSendNotification(template);
            }
        }

        endSpoiler();
        writeText("<br>");
    }

    void removeFromGlobalFRB(Game game) throws CommonException {
        Set<Long> listFRB = new HashSet<Long>();
        listFRB.addAll(BaseGameInfoTemplateCache.getInstance().getFrbGames());

        String strRemoveFRBGames = exportContainingGames(game, listFRB);
        if (strRemoveFRBGames.isEmpty()) return;

        startSpoiler(writer, "frb", "Удаление из списка FRB: " + strRemoveFRBGames);

        writeText("<b>Список FRB до изменений:</b><br>" + (new TreeSet(listFRB)) + "<br>");
        writeText("<br>");

        for (int i = 0; i < game.gameIds.length; i++) {
            if (listFRB.contains(game.gameIds[i]) && game.isChecked(i)) {
                listFRB.remove(game.gameIds[i]);
                writeText("<b>Удален из FRB:</b> " + game.gameIds[i] + "<br>");
            } else {
                continue;
            }
        }

        writeText("<br>");
        writeText("<b>Список FRB после изменений:</b><br>" + (new TreeSet(listFRB)) + "<br>");

        if (!isTestMode)
            BaseGameInfoTemplateCache.getInstance().setFrbGames(listFRB);


        for (int i = 0; i < game.gameIds.length; i++) {
            if (game.isChecked(i)) {
                BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(game.gameIds[i]);
                template.setFrbGame(false);
                RemoteCallHelper.getInstance().saveAndSendNotification(template);
            }
        }

        endSpoiler();
        writeText("<br>");
    }

    void addToBankFRB(long[] games, long bankId) throws CommonException {
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        String gameListDisable = bankInfo.getProperties().get(BankInfo.KEY_FRB_GAMES_DISABLE);
        if (!isTrimmedEmpty(gameListDisable)) {
            throw new CommonException("Only one of KEY_FRB_GAMES_ENABLE or KEY_FRB_GAMES_DISABLE can be specified, bankId=" + bankId);
        }

        String gameList = bankInfo.getProperties().get(BankInfo.KEY_FRB_GAMES_ENABLE);
        if (gameList != null && gameList.length() > 0) {
            List<String> currentListFRB = Arrays.asList(gameList.split("\\|"));
            List<String> newListFRB = new ArrayList<String>(currentListFRB);

            startSpoiler(writer, "frb_" + bankId, "Добавление в банковский FRB: " + Arrays.toString(games));

            writeText("<b>BANK listFRB before: </b>" + currentListFRB + "<br>");

            for (long gameId : games) {
                if ((gameId != -1) && (!newListFRB.contains(String.valueOf(gameId)))) {
                    writeText(gameId + " added to frb<br>");
                    newListFRB.add(String.valueOf(gameId));
                    gameList += "|" + String.valueOf(gameId);
                } else continue;
            }

            writeText("<b>BANK listFRB after: </b>" + newListFRB + "<br>");
            endSpoiler();

            bankInfo.setProperty(BankInfo.KEY_FRB_GAMES_ENABLE, gameList);
            RemoteCallHelper.getInstance().saveAndSendNotification(bankInfo);
        }
    }

    void removeFromBankFRB(long[] games, long bankId) {

    }

    void applyBankAndCurrency(Game game, long bankId, Currency currency) throws CommonException {

        for (int i = 0; i < game.gameIds.length; i++) {
            if (game.isChecked(i)) {
                BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(game.gameIds[i]);
                IBaseGameInfo destGame = BaseGameCache.getInstance().getGameInfoShared(bankId, game.gameIds[i], currency);

                if (destGame != null && !(destGame instanceof ImmutableBaseGameInfoWrapper)) {
                    updateGameInBankAndCurrency(destGame, game, i, bankId, currency, template);

                    if (destGame.isEnabled() != game.isEnabled) {
                        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                        if (currency == bankInfo.getDefaultCurrency()) {
                            String extBankId = BankInfoCache.getInstance().getExternalBankId(bankId);
                            if (extBankId.isEmpty()) extBankId = String.valueOf(bankId);
                            if (i == 0) deliveredPCToBanks.add(extBankId);
                            if (i == 1) deliveredIOSToBanks.add(extBankId);
                            if (i == 2) deliveredAndroidToBanks.add(extBankId);
                            if (i == 3) deliveredWindowsPhoneToBanks.add(extBankId);
                        }
                    }
                } else {
                    createGameInBankAndCurrency(game, i, bankId, currency, template);

                    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
                    if (currency == bankInfo.getDefaultCurrency()) {
                        String extBankId = BankInfoCache.getInstance().getExternalBankId(bankId);
                        if (extBankId.isEmpty()) extBankId = String.valueOf(bankId);
                        if (i == 0) deliveredPCToBanks.add(extBankId);
                        if (i == 1) deliveredIOSToBanks.add(extBankId);
                        if (i == 2) deliveredAndroidToBanks.add(extBankId);
                        if (i == 3) deliveredWindowsPhoneToBanks.add(extBankId);
                    }
                }
            }
        }
    }

    void updateGameInBankAndCurrency(IBaseGameInfo destGame, Game game, int game_index, long bankId,
                                     Currency currency, BaseGameInfoTemplate template) throws CommonException {
        String fullGameName = game.name + platformNames[game_index];
        long gameId = game.gameIds[game_index];

        startSpoiler(writer, "updGameBank_" + bankId + "_" + gameId + "_" + currency.getCode(), "Обновление игры: " + gameId + "(" + fullGameName + ")");

        Map properties = game.getProperties(game_index, currency.isDefault(bankId));
        if (!isTestMode) {
            if (template.isDynamicLevelsSupported()) {
                properties.remove(BaseGameConstants.KEY_DEFAULT_COIN);
                properties.remove(BaseGameConstants.KEY_FRB_COIN);
            }
            destGame.setProperties(properties);
        }

        if (game.getModels() != null) {
            writeText("<b>Model Index: </b>" + game.getCurrentModel() + " from " + game.getModels() + "<br>");
        }

        if (game.varType == GameVariableType.COIN) {
            if (!isTestMode) {
                destGame.setCoins(game.coins);
            }
            writeText("<b>Coins: </b>" + coinsToString(game.coins) + "<br>");
        } else {
            if (!isTestMode) {
                destGame.setLimit(game.limit);
            }
            writeText("<b>Limit: </b>" + game.limit + "<br>");
        }

        if (!isTestMode) {
            destGame.setName(fullGameName);
        }

        writeText("<b>Properties: </b>" + properties.toString() + "<br>");
        if (isUpdateLanguages) {
            if (game.langugages[game_index] != null) {
                if (!isTestMode) {
                    destGame.setLanguages(game.langugages[game_index]);
                }
                writeText("<b>Langugages " + deviceTypes[game_index] + ": </b>" + game.langugages[game_index] + "<br>");
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        if (!isTestMode) {
            try {
                RemoteCallHelper.getInstance().saveAndSendNotification(destGame);
                BaseGameCache.getInstance().invalidate(composeGameKey(bankId, gameId, null));
                BaseGameCache.getInstance().invalidate(composeGameKey(bankId, gameId, currency));
            } catch (CommonException e) {
                e.printStackTrace();
            }
        }

        endSpoiler();
    }

    private String composeGameKey(long bankId, long gameId, Currency currency) {
        String defaultKey = bankId + "+" + gameId;
        return currency == null || currency.isDefault(bankId) ? defaultKey : defaultKey + "+" + currency.getCode();
    }

    void createGameInBankAndCurrency(Game game, int game_index, long bankId, Currency currency, BaseGameInfoTemplate template) {
        if (template.isDynamicLevelsSupported()) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (!bankInfo.getDefaultCurrency().getCode().equals(currency.getCode())) {
                return;
            }
        }
        String fullGameName = game.name + platformNames[game_index];
        long gameId = game.gameIds[game_index];

        startSpoiler(writer, "crtGameBank_" + bankId + "_" + gameId + "_" + currency.getCode(), "Создание игры: " + gameId + "(" + fullGameName + ")");

        Map properties = game.getProperties(game_index, currency.isDefault(bankId));
        if (template.isDynamicLevelsSupported()) {
            properties.remove(BaseGameConstants.KEY_DEFAULT_COIN);
            properties.remove(BaseGameConstants.KEY_FRB_COIN);
        }

        if (!isTestMode) {
            try {
                BaseGameHelper.createGame(bankId, gameId, currency, fullGameName, game.type, game.group,
                        game.varType, null, game.spGameProcessor, properties, game.limit,
                        game.coins,
                        game.isJackPot, game.pcrp, game.bcrp);
            } catch (CommonException e) {
                e.printStackTrace();
            }
        }
        if (game.getModels() != null) {
            writeText("<b>Model Index: </b>" + game.getCurrentModel() + " from " + game.getModels() + "<br>");
        }
        writeText("<b>Type: </b>" + game.type + "<br>");
        writeText("<b>Group: </b>" + game.group + "<br>");
        writeText("<b>VarType: </b>" + game.varType + "<br>");
        writeText("<b>Coins: </b>" + coinsToString(game.coins) + "<br>");
        writeText("<b>Limit: </b>" + game.limit + "<br>");
        writeText("<b>Properties: </b>" + properties.toString() + "<br>");
        writeText("<b>isJackPot: </b>" + game.isJackPot + "<br>");
        if (game.isJackPot) {
            writeText("<b>pcrp: </b>" + game.pcrp + "<br>");
            writeText("<b>bcrp: </b>" + game.bcrp + "<br>");
        }

        IBaseGameInfo newGame = BaseGameCache.getInstance().getGameInfoShared(bankId, gameId, currency);
        if (newGame != null && !(newGame instanceof ImmutableBaseGameInfoWrapper)) {
            if (game.langugages[game_index] != null) {
                if (!isTestMode) newGame.setLanguages(game.langugages[game_index]);
                writeText("<b>Langugages " + deviceTypes[game_index] + ": </b>" + game.langugages[game_index] + "<br>");
            }

            if (!isTestMode) {
                try {
                    RemoteCallHelper.getInstance().saveAndSendNotification(newGame);
                } catch (CommonException e) {
                    e.printStackTrace();
                }
            }
        }

        endSpoiler();
    }

    String getSystemName() {
        String systemName = null;
        String shortServerName = server_name.replace("gaming", "");

        if (shortServerName.contains("sb.")) systemName = "SB";
        else if (shortServerName.contains("gp3.")) systemName = "GP3";
        else if (shortServerName.contains("c2ss.")) systemName = "C2SS";
        else if (shortServerName.contains("c2lga.")) systemName = "C2LGA";
        else if (shortServerName.contains("c2lga2.")) systemName = "C2LGA2";
        else if (shortServerName.contains("aams.")) systemName = "AAMS";
        else if (shortServerName.contains("c2188bet.")) systemName = "C2188BET";
        else if (shortServerName.contains("188bet.")) systemName = "188BET";
        else if (shortServerName.contains("laptop.")) systemName = "LAPTOP";
        else if (shortServerName.contains("ng.")) systemName = "NG";
        else if (shortServerName.contains("ng-copy.")) systemName = "NG";
        else if (shortServerName.contains("democluster.")) systemName = "DEMO";
        else if (shortServerName.contains("expo2.")) systemName = "DEMO";
        else if (shortServerName.contains("beta.")) systemName = "BETA";
        else if (shortServerName.contains("stress.")) systemName = "STRESS";
        else if (shortServerName.contains("stc.")) systemName = "STC";
        else if (shortServerName.contains("stc2.")) systemName = "STC2";

        return systemName;
    }

    void setOrRemoveTemplateProperty(BaseGameInfoTemplate template, String property, String value) {
        if (isCorrectValue(value))
            template.getDefaultGameInfo().setProperty(property, value);
        else
            template.getDefaultGameInfo().removeProperty(property);
    }

    boolean isCorrectValue(String value) {
        return (value != null && !value.toUpperCase().equals("NULL") && !value.isEmpty());
    }

    void startSpoiler(PrintWriter writer, String id, String title) {
        writeText("<a href=\"javascript:void(0)\" style=\"font-weight: bold;\" onclick=\"showHide('" + id + "')\">" + title +
                "</a><br>" + "<div id=\"" + id + "\" style=\"display: none;\">");
        spoiler_level += 1;
    }

    void endSpoiler() {
        writeText("</div>");
        spoiler_level -= 1;
    }

    HashMap<String, String> buildTemplateProperties(Game game, BaseGameInfoTemplate template, int platform_index) {
        HashMap<String, String> properties = new HashMap<String, String>();

        properties.put(BaseGameConstants.KEY_REPOSITORY_FILE, game.repositoryFile);
        properties.put(BaseGameConstants.KEY_PAYOUT_PERCENT, game.getPayoutPercent());
        if (template != null && !template.isDynamicLevelsSupported()) {
            properties.put(BaseGameConstants.KEY_DEFAULT_COIN, game.getDefCoin());
        }
        //properties.put(BaseGameConstants.KEY_ACS_ENABLED,          game.getAcs()); //do not set ACS any more
        properties.put(BaseGameConstants.KEY_ISENABLED, game.getEnabled());
        properties.put(BaseGameConstants.KEY_RTP, game.rtp);
        properties.put(BaseGameConstants.KEY_MAX_WIN, game.maxWin);
        properties.put(BaseGameConstants.KEY_POSSIBLE_LINES, game.possibleLines);
        properties.put(BaseGameConstants.KEY_POSSIBLE_BETPERLINES, game.possibleBetPerLines);
        properties.put(BaseGameConstants.KEY_LINES_COUNT, game.linesCount);
        properties.put(BaseGameConstants.KEY_DEFAULTNUMLINES, game.defaultNumLines);
        properties.put(BaseGameConstants.KEY_DEFAULTBETPERLINE, game.defaultBetPerLine);
        properties.put(BaseGameConstants.KEY_CDN_SUPPORT, game.cdn_support);
        properties.put(BaseGameConstants.KEY_LGA_APPROVED, game.lga_approved);
        properties.put(BaseGameConstants.KEY_HTML5PC_VERSION_MODE, (game.html5VersionMode != null ? game.html5VersionMode.name() : "NULL"));
        properties.put(BaseGameConstants.KEY_UNIFIED_LOCATION, game.unifiedLocation);
        properties.put(BaseGameConstants.KEY_PDF_RULES_NAME, game.pdfRulesName);
        properties.put(BaseGameConstants.KEY_ADDITIONAL_FLASHVARS, game.additionalFlashVars);

        properties.put(BaseGameConstants.KEY_DEVELOPMENT_VERSION, game.isDevelopmentKey);

        if (platform_index != 0) {
            properties.put(BaseGameConstants.KEY_PLAYER_DEVICE_TYPE, deviceTypes[platform_index]);
        }

        for (int j = 0; j < game.gameIds.length; j++) {
            if (!game.isChecked(j)) continue;
            if ((game.gameIds[j] != -1) && (game.gameIds[j] != game.gameIds[platform_index])) {
                properties.put(deviceTypes[j], String.valueOf(game.gameIds[j]));
            }
        }

        return properties;
    }

    void createGameInTemplate(Game game, HashMap<String, String> properties, int platform_index) throws CommonException {
        String fullName = game.getFullName(platform_index);
        String title = game.getTitle(platform_index);
        String servlet = game.getServlet(platform_index);

        List<String> langs = Arrays.asList("en");

        BaseGameInfo gameInfo = new BaseGameInfo(game.gameIds[platform_index], BankConstants.DEFAULT_BANK_ID, fullName, game.type, game.group,
                game.varType, null, game.spGameProcessor, null, null, properties, null, langs);

        BaseGameInfoTemplateCache baseGameInfoTemplateCache = BaseGameInfoTemplateCache.getInstance();

        boolean hasJackpot = (game.isJackPot || game.isJackPot3);
        BaseGameInfoTemplate template = new BaseGameInfoTemplate(game.gameIds[platform_index], fullName, null, gameInfo, hasJackpot, servlet);
        template.setTitle(title);
        template.setSwfLocation(game.swfLocation);
        template.setAdditionalParams("");
        template.setOldTranslation(false);
        template.setGameControllerClass(game.gameControllerClass);
        template.setRoundFinishedHelper(game.roundFinishedHelper);
        template.setEndRoundSignature(game.endRoundSignature);
        template.setServlet(servlet);

        HashMap<String, String> copyProperties = new HashMap<String, String>(properties);
        for (String key : copyProperties.keySet()) {
            String value = copyProperties.get(key);
            setOrRemoveTemplateProperty(template, key, value);
        }

        if (!isTestMode) {
            baseGameInfoTemplateCache.put(template);
            RemoteCallHelper.getInstance().saveAndSendNotification(template);
        }
    }

    void updateGameInTemplate(Game game, BaseGameInfoTemplate template, HashMap<String, String> properties, int platform_index) throws CommonException {
        template.setTitle(game.getTitle(platform_index));
        template.setSwfLocation(game.getSWFLocation());
        template.setAdditionalParams("");
        template.setOldTranslation(false);
        template.setGameControllerClass(game.getGameControllerClass());
        template.setRoundFinishedHelper(game.roundFinishedHelper);
        template.setEndRoundSignature(game.getEndRoundSignature());
        template.setServlet(game.getServlet(platform_index));

        for (String key : properties.keySet()) {
            String value = properties.get(key);
            setOrRemoveTemplateProperty(template, key, value);
        }

        if (!isTestMode) {
            RemoteCallHelper.getInstance().saveAndSendNotification(template);
        }
    }

    String exportMissedGames(Game game, Set<Long> listFRB) {
        String result = "";

        for (int i = 0; i < game.gameIds.length; i++) {
            if (game.isChecked(i) && !listFRB.contains(game.gameIds[i]))
                result += game.gameIds[i] + ", ";
        }

        if (!result.isEmpty())
            result = result.substring(0, result.length() - 2);

        return result;
    }

    String exportContainingGames(Game game, Set<Long> listFRB) {
        String result = "";
        for (int i = 0; i < game.gameIds.length; i++) {
            if (game.isChecked(i) && listFRB.contains(game.gameIds[i]))
                result += game.gameIds[i] + ", ";
        }

        if (!result.isEmpty())
            result = result.substring(0, result.length() - 2);

        return result;
    }
%>

</body>
</html>


