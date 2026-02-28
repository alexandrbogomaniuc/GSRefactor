<%@ page import="com.dgphoenix.casino.common.cache.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.Coin" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.Limit" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.*" %>
<%@ page import="com.abs.casino.common.cache.data.game.RoundFinishedHelper" %>
<%@ page import="com.dgphoenix.casino.system.configuration.GameServerConfiguration" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="com.google.common.collect.ComparisonChain" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%!
    int nameWidth = 250;

    private static String resolveDefaultGameServletClassName() {
        String absClassName = "com.abs.casino.singlegames.game.GameServlet";
        try {
            Class.forName(absClassName);
            return absClassName;
        } catch (ClassNotFoundException ignore) {
            return "com.dgphoenix.casino.singlegames.game.GameServlet";
        }
    }
%>


<%
    HashMap<Long, String> arraySubcasinoDescription = new HashMap<Long, String>();

    /*Casinoroom*/
    arraySubcasinoDescription.put(7L, "Нужно ставить конфигурации монет так, чтобы MAX_WIN был <= ~250 000 EUR");
    /*FUN88*/
    arraySubcasinoDescription.put(65L, "У джекпотовых игр на каждой валюте сетится свой WIN лимит");

    /*SPORTSINTERATION*/
    arraySubcasinoDescription.put(19L, "Добавление FRB игр в список банков");
    /*GTBETS*/
    arraySubcasinoDescription.put(40L, "Добавление FRB игр в список банков");

    /*PANBET*/
    arraySubcasinoDescription.put(50L, "Выдача сразу на COPY/LIVE");
    /*IFORIUM*/
    arraySubcasinoDescription.put(36L, "Выдача сразу на COPY/LIVE");
    /*SOFTSWISS*/
    arraySubcasinoDescription.put(178L, "Выдача сразу на COPY/LIVE <br> Настройки игр у дочерних банков всегда должны быть такие же как у мастера для всех валют. <br> В BaseGameInfo есть JACKPOT_WIN_LIMIT");
    /*MBET*/
    arraySubcasinoDescription.put(206L, "Выдача сразу на COPY/LIVE <br> Не выдавать игры на 1125 и 1126 банки");

    /*Uedbet*/
    arraySubcasinoDescription.put(215L, "Выдача сразу на COPY/LIVE");
    /*XPROGAMING*/
    arraySubcasinoDescription.put(56L, "Выдача сразу на COPY/LIVE" + "<br>" + "Не использовать GAME_TESTING" + "<br>" + "Прописывать IMAGE_URL" + "<br>" + "Наше StandAlone лобби" + "<br>" + "Выдавать на XPRO DEMO COPY/LIVE");
    /*XPROGAMING DEMO*/
    arraySubcasinoDescription.put(57L, "Выдача сразу на COPY/LIVE" + "<br>" + "Не использовать GAME_TESTING" + "<br>" + "Прописывать IMAGE_URL" + "<br>" + "Наше StandAlone лобби" + "<br>");
    /*Guts*/
    arraySubcasinoDescription.put(175L, "Выдача сразу на COPY/LIVE");

    /*GAMESCALE*/
    arraySubcasinoDescription.put(64L, "Своя ссылка запуска" + "<br>" + "По протоколу конфирм на 995 банке COPY влечёт выдачу на все LIVE" + "<br>" + "The client does not want JP games." + "<br>" + "The Client doesn't support \"Negative Bet.\" игры Ridem Poker(43) быть не должно ");
    /*HERITAGE*/
    arraySubcasinoDescription.put(69L, "По протоколу конфирм на 312 банке COPY влечёт выдачу на все банки LIVE");
    /*CasinoSaga*/
    arraySubcasinoDescription.put(90L, "Своя ссылка запуска");
    /*GrandCasino*/
    arraySubcasinoDescription.put(16L, "Своя ссылка запуска");
    /*Whitehat*/
    arraySubcasinoDescription.put(62L, "Своя ссылка запуска");

    /*Videoslots*/
    arraySubcasinoDescription.put(77L, "BaseAmount у джекпотовых игр должен быть 0.001");
    /*FINNPLAY*/
    arraySubcasinoDescription.put(72L, "Please remove games from bankId 518 and 849 and do not deliver any new games this bank");

    /*ACE GAMING*/
    arraySubcasinoDescription.put(83L, "Используется external GameId" + "<br>" + "У 919 банка свои external GameId" + "<br>" + "FRB bonus was never implemented on ACE GAMEING.");
    /*RoyalPK*/
    arraySubcasinoDescription.put(28L, "Используется GameName вместо GameID ");
    /*Sportsbook*/
    arraySubcasinoDescription.put(3L, "Flash и ToGo используют разные external GameId, extGameId нужно сетить в Template <br> ID игр должны входить в интервал SBConstants <br> Тестеры конфирмят на 7000, а выдавать на все LIVE <br> Не поддерживает Windows Phone");
    /*STREAMTECH*/
    arraySubcasinoDescription.put(31L, "Используются внешние ID банков" + "<br>" + "Добавлять в ссылку CDN=GLOBAL");

    /*AllWinCity*/
    arraySubcasinoDescription.put(210L, "Наше Stand-alone лобби");
    /*Vietbet*/
    arraySubcasinoDescription.put(203L, "Наше Stand-alone лобби, нужно вставлять широкий баннер" + "<br>" + "RQA конфирмят только на 1 банке 968: \"Client only has one COPY bank.\"");
    /*ITECHGAMING*/
    arraySubcasinoDescription.put(85L, "Наше Stand-alone лобби");
    /*OPERIA*/
    arraySubcasinoDescription.put(45L, "Наше Stand-alone лобби");
    /*DemoCasino*/
    arraySubcasinoDescription.put(171L, "Наше Stand-alone лобби, есть мобильное лобби");
    /*WINWIS*/
    arraySubcasinoDescription.put(244L, "Наше Stand-alone лобби[default.jsp]");
    /*LAZYBUG*/
    arraySubcasinoDescription.put(234L, "Наше Stand-alone лобби");

    /*SLOTSMILLION*/
    arraySubcasinoDescription.put(213L, "Используется SingleGameId");

    /*VIVO INTERACTIVE*/
    arraySubcasinoDescription.put(218L, "Не поддерживает Windows Phone" + "<br>" + "Джекпотовые отчисления в 10 раз меньше");
    /*188BET*/
    arraySubcasinoDescription.put(48L, "Не поддерживает Windows Phone");

    /*Interwetten*/
    arraySubcasinoDescription.put(216L, "Должен быть просечен параметр LGA_SHELL_SUPPORTED у PC+ToGo" + "<br>" + "Interwetten doesn't use FRB functionality.");
    /*Comeon*/
    arraySubcasinoDescription.put(180L, "LGA система, должен быть просечен LGA_APPROVED=TRUE");
    /*MrGreen*/
    arraySubcasinoDescription.put(29L, "Игры должны быть LGA_APPROVED=TRUE" + "<br>" + "строковые GameName PC+ToGo" + "<br>" + "Для LGA игр свой LGA домен");
    /*7RED*/
    arraySubcasinoDescription.put(22L, "Эксклюзивные игры не поддерживают FRB_REINITIALIZE" + "<br>" + "ACS у PC и ToGo может различаться");
    /*PTPT*/
    arraySubcasinoDescription.put(214L, "Джекпотовые отчисления в 10 раз меньше");
    /*W88*/
    arraySubcasinoDescription.put(99L, "Джекпотовые отчисления в 10 раз меньше");

    /*PARLAY_BINGO*/
    arraySubcasinoDescription.put(32L, "Обычно у игр есть FREEBALANCE=25000");
    /*PRODUCT MADNESS*/
    arraySubcasinoDescription.put(27L, "Только PC игры" + "<br>" + "LIVE на отдельном кластере");
    /*SBTECH*/
    arraySubcasinoDescription.put(168L, "Не выдавать новые игры на 916 банк, Please disable Stampede PC/ToGo for Bank ID - 916 and do not enable any new games.");

    GameServerConfiguration conf = ApplicationContextHelper.getApplicationContext()
            .getBean(GameServerConfiguration.class);

%>


<SCRIPT>

    function isDigit(event, input, allow_point) {
        var key, ctrl;

        if (window.event) {
            key = window.event.keyCode;
            ctrl = window.event.ctrlKey;
        } else {
            key = event.which;
            ctrl = event.ctrlKey;
        }

        if ((key == 46) || (key == 8) || (key == 9)) return true; // delete, backspace, TAB
        if ((key >= 48) && (key <= 57)) return true;    // 0..9
        if (key >= 96 && key <= 105) return true;   //Numpad0..Numpad9
        if (((key == 86) || (key == 67) || (key == 88)) && ctrl) return true; // X C V
        if (key == 190 && allow_point) {
            var points = input.value.split('.');
            if (points.length <= 1 && points[0] != '') return true;
        }

        return false;
    }

    function showJackpotInfo(bValue) {
        var elements = document.getElementsByClassName('class_jackpot');

        for (var i = 0; i < elements.length; i++) {
            elements[i].style.visibility = ((bValue == "TRUE") ? "visible" : "hidden");
        }
    }

    function showUNJInfo(handleUnjField) {
        let hideUnjProps = handleUnjField !== "TRUE";
        document.getElementById('unjTableNames').style.display = hideUnjProps ? "none" : "inline";
        document.getElementById('unjTableValues').style.display = hideUnjProps ? "none" : "inline";
        document.getElementById('unjTablePcrp').style.display = hideUnjProps ? "none" : "inline";
        document.getElementById('unjTableBcrp').style.display = hideUnjProps ? "none" : "inline";
        if (handleUnjField === "LINKED") {
            document.getElementById('unjLinkedGame').style.display = "inline";
        } else {
            document.getElementById('unjLinkedGame').style.display = "none";
            document.getElementById('unj_linked_gameid').value = 0;
        }
    }

    function showFRBInfo(bValue) {
        document.getElementById('frbTable').style.visibility = ((bValue == "TRUE") ? "visible" : "hidden");
    }

    function showJackpot3Info(bValue) {
        var elements = document.getElementsByClassName('id_jackpot_3_info');
        if (bValue == "TRUE") {
            for (var i = 0; i < elements.length; i++)
                elements[i].style.visibility = "visible";
        } else {
            for (var i = 0; i < elements.length; i++)
                elements[i].style.visibility = "hidden";
        }
    }

    function addRowToPropertiesTable() {
        var table = document.getElementById('propertiesTable');

        var rowCount = table.rows.length;
        var row = table.insertRow(rowCount - 1);

        row.insertCell(0).innerHTML = '<INPUT style="width: 250px; font-weight: bold;"  type="text"       class="class_prop_key"   name="prop_key_x"                    value=""/>';
        row.insertCell(1).innerHTML = '<INPUT style="width: 150px;"                     type="text"       class="class_prop_value" name="prop_value_x"                  value="" onclick="showRow(this)"/>';
        row.insertCell(2).innerHTML = '<TD> <INPUT                                      type="button"                              name="delbutton"                     value="X" onclick="deleteRow(this)" id="x"> </TD>'
        row.insertCell(3).innerHTML = '<INPUT                                           type="checkbox"   class="class_prop_flash" name="prop_flash_x"> Только PC'

        setNewPropertyIDS();
    }

    function deleteRow(row) {
        var table = document.getElementById('propertiesTable');
        table.deleteRow(row.id);

        setNewPropertyIDS();
    }

    function setNewPropertyIDS() {
        var prop_names = document.getElementsByClassName('class_prop_key');
        var prop_values = document.getElementsByClassName('class_prop_value');
        var buttons = document.getElementsByName('delbutton');
        var prop_flash = document.getElementsByClassName('class_prop_flash');

        for (var i = 0; i < buttons.length; i++) {
            prop_names[i].name = "prop_key_" + i;
            prop_values[i].name = "prop_value_" + i;
            prop_flash[i].name = "prop_flash_" + i;
            buttons[i].id = i;
        }

        var value_prop_count = document.getElementById('id_property_count');
        value_prop_count.value = buttons.length;
    }

    function setBanks(value) {
        var banks = document.getElementsByName('check_banks[]');

        for (var i = 0; i < banks.length; i++) {
            if (banks[i].disabled) continue
            banks[i].checked = value;
        }
    }

    function submitButton(btn) {
        var $result = document.getElementById('id_subcasino').value;

        if ($result == "") {
            alert("ОШИБКА! Необходимо ввести ID субказино");
            return false;
        }

        setNewPropertyIDS();

        return true;
    }

    function AutoParams(row) {
        var name = document.getElementById('id_name');
        var title = document.getElementById('id_title');
        var repository_file = document.getElementById('id_repositoryFile');
        var swf_location = document.getElementById('id_swfLocation');
        var servlet = document.getElementById('id_servlet');
        var game_class = document.getElementById('id_gameClass');

        var nonSpaced_title = title.value.split(" ").join("").split("'").join("");
        var lower_title = nonSpaced_title.toLowerCase();

        name.value = lower_title.toUpperCase();
        repository_file.value = lower_title + "-2.0.jar";
        swf_location.value = "/flash/" + lower_title + "/" + lower_title + ".swf";
        servlet.value = "/" + nonSpaced_title + ".game";
        game_class.value = "com." + "<%=conf.getBrandNameLowCase()%>" + ".casino.singlegames." +
                lower_title + "." + nonSpaced_title + "Servlet";
    }

    function reloadPage(gameId, sourceBankId, destSubcasionId) {
        if (sourceBankId == null) return;
        if (destSubcasionId == null) return;

        location.href = "/support/gameBankConfig/editGameForm.jsp?gamecopy=" + gameId + "&bankcopy=" + sourceBankId + "&to_subcasino=" + destSubcasionId + "";
    }

    <%!
        String getUNJValue(String value, int prefferedIndex, String defaultValue) {
            String[] arrayValue = value.split(",");

            if (arrayValue.length == 2) {
                return arrayValue[prefferedIndex];
            } else {
                return (defaultValue == null) ? arrayValue[0] : defaultValue;
            }
        }

        static Map<Long, List<Long>> etalonsMap = new HashMap<>();
        static final int ETALONS_NUMBER = 5;
        static final List<Long> exclusives = Arrays.asList(
                685L, 684L, 683L, 682L, 636L, 635L, 634L, 633L, 632L, 631L, 630L, 530L, 529L, 528L, 527L,
                511L, 510L, 509L, 508L, 436L, 435L, 434L, 395L, 394L, 393L, 392L, 391L, 349L, 348L, 347L,
                333L, 327L, 326L, 322L, 317L, 316L, 294L, 293L, 273L, 265L, 264L, 261L, 260L, 257L, 255L,
                253L, 252L, 251L, 250L, 246L, 239L, 237L, 235L, 233L, 232L, 230L, 227L, 208L, 188L, 187L,
                186L, 185L, 184L, 183L, 89L
        );

        long maxId = -1;

        String getEtalonSlotsGames(long gameId) {
            if (etalonsMap.containsKey(gameId)) {
                return StringUtils.join(etalonsMap.get(gameId), ", ");
            }
            if (maxId < 0) {
                maxId = BaseGameInfoTemplateCache.getInstance().getAllObjects()
                    .keySet().stream().mapToLong(i -> i).max().getAsLong();
            }
            BaseGameInfoTemplate originGame = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
            if (originGame == null) {
                return etalonGames;
            }
            if (originGame.isDynamicLevelsSupported()) {
                etalonsMap.put(gameId, Collections.EMPTY_LIST);
                return ""; // для игр с GL_SUPPORTED=TRUE эталоны не используются
            }
            //Эталоны ищутся только для слотов!!!
            String possibleLines = originGame.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_POSSIBLE_LINES);
            String defNumLines = originGame.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_DEFAULTNUMLINES);
            List<Long> foundGames = new ArrayList<>();
            for (long id = maxId; id > 1 && foundGames.size() < ETALONS_NUMBER; id--) {
                if (exclusives.contains(id) || id == gameId) {
                    continue;
                }
                BaseGameInfoTemplate game = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(id);
                if (game == null) {
                    continue;
                }
                if (StringUtils.isNotEmpty(possibleLines) &&
                    possibleLines.equals(game.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_POSSIBLE_LINES))) {
                    foundGames.add(id);
                    continue;
                }
                if (StringUtils.isNotEmpty(defNumLines) &&
                    defNumLines.equals(game.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_DEFAULTNUMLINES))) {
                    foundGames.add(id);
                }
            }
            etalonsMap.put(gameId, foundGames);

            return StringUtils.join(etalonsMap.get(gameId), ", ");
        }

        String etalonAAMSGames = "50000, 50004, 50008, 50012, 50016";
        String etalonNGGames   = "30396, 30393";
        String etalonSlotsGames = "816, 812, 803";
        String etalonRouletteGames = "5, 278, 79";
        String etalonGames = etalonSlotsGames;

        BaseGameInfoTemplate templatePC = null;
        BaseGameInfoTemplate templateMobile = null;
        BaseGameInfoTemplate templateAndroid = null;
        BaseGameInfoTemplate templateWindowsPhone = null;

        private boolean keyIsNew(String key)
        {
            if (key.toUpperCase().equals(BaseGameConstants.KEY_DEFAULT_COIN))       return false;
            if (key.toUpperCase().equals(BaseGameConstants.KEY_ISENABLED))          return false;
            if (key.toUpperCase().equals(BaseGameConstants.KEY_PAYOUT_PERCENT))     return false;
            if (key.toUpperCase().equals(BaseGameConstants.KEY_FRB_COIN))           return false;
            if (key.toUpperCase().equals(BaseGameConstants.KEY_GAME_TESTING))       return false;

            return true;
        }

        private String getLanguages(BankInfo bankInfo, String strGameId)
        {
            String result = "";
            if (strGameId != null && !strGameId.isEmpty())
            {
                IBaseGameInfo game = BaseGameCache.getInstance().getGameInfoById(bankInfo.getId(),
                        Long.parseLong(strGameId), bankInfo.getDefaultCurrency());

                if (game != null && game.getLanguages() != null)
                {
                    ArrayList<String> langugages = new ArrayList<String>();
                    langugages.addAll(game.getLanguages());
                    Collections.sort(langugages);

                    result = langugages.toString().replace("[", "").replace("]", "");
                }
            }
            return result;
        }


        private String[] readProperties(String key)
        {
            String[] result = new String[4];

            result[0] = (templatePC           != null) ? templatePC.getDefaultGameInfo().getProperty(key) : null;
            result[1] = (templateMobile       != null) ? templateMobile.getDefaultGameInfo().getProperty(key) : null;
            result[2] = (templateAndroid      != null) ? templateAndroid.getDefaultGameInfo().getProperty(key) : null;
            result[3] = (templateWindowsPhone != null) ? templateWindowsPhone.getDefaultGameInfo().getProperty(key) : null;

            return result;
        }

        private String getCoins(BankInfo bankInfo, String strGameId)
        {
            String result = "";
            float[] result_coins = {};

            if (strGameId != null && !strGameId.isEmpty())
            {
                IBaseGameInfo game = BaseGameCache.getInstance().getGameInfoById(bankInfo.getId(),
                        Long.parseLong(strGameId), bankInfo.getDefaultCurrency());

                if (game != null)
                {
                    if (game.getVariableType() == GameVariableType.COIN)
                    {
                        boolean isDefaultCoins = false;
                        List<Coin> array_coin = game.getCoins();
                        if (array_coin == null || array_coin.isEmpty()) {
                            isDefaultCoins = true;
                            array_coin = bankInfo.getCoins();
                        }

                        result_coins = new float[array_coin.size()];
                        for(int i = 0; i < array_coin.size(); i++) {
                            result_coins[i] = array_coin.get(i).getValue()/100.0f;
                        }

                        Arrays.sort(result_coins);

                        for(int i = 0; i < result_coins.length; i++)
                        {
                            result += result_coins[i];
                            if (i < result_coins.length-1) result += " ";
                        }

                        return result + (isDefaultCoins ? " (default)" : "");
                    }
                    else return "null"; // Game don't support coins;
                }
            }
            return ""; // Game not delivered;
        }
    %>

    <%
        String serverName = request.getServerName();
    %>

</SCRIPT>


<HTML>
<HEAD>
    <TITLE> Game Bank Config </TITLE>
</HEAD>
<BODY>
<%
    try {
        ArrayList<Long> disabledBanks = new ArrayList<Long>();
        disabledBanks.add(211L);
        disabledBanks.add(212L);
        disabledBanks.add(213L);
        disabledBanks.add(214L);
        disabledBanks.add(215L);
        disabledBanks.add(354L);
        disabledBanks.add(452L);
        disabledBanks.add(472L);
        disabledBanks.add(514L);
        disabledBanks.add(689L);
        disabledBanks.add(128L);

        disabledBanks.add(518L);
        disabledBanks.add(849L);

        String strGameID = request.getParameter("gamecopy");
        String strCurrency = request.getParameter("currency");
        String strBankID = request.getParameter("bankcopy");
        String strSubcasinoID = request.getParameter("to_subcasino");

        if (strGameID == null) strGameID = "79";
        else strGameID = strGameID.trim(); // Hamburg Roulette
        if (strCurrency == null) strCurrency = "";
        else strCurrency = strCurrency.trim(); // Default
        if (strBankID == null) strBankID = "271";
        else strBankID = strBankID.trim(); // Default
        if (strSubcasinoID == null) strSubcasinoID = "58";
        else strSubcasinoID = strSubcasinoID.trim(); // Default

        List<Long> banks = new ArrayList<Long>();

        SubCasino subCasino = SubCasinoCache.getInstance().get(Long.parseLong(strSubcasinoID));
        Collection<SubCasino> availableSubCasinos = SubCasinoCache.getInstance().getAllObjects().values().stream().
                sorted((sc1,sc2) -> ComparisonChain.start().compare(sc1.getId(), sc2.getId()).result())
                .collect(Collectors.toList());
        Long prev_bank = null, next_bank = null;
        Long prev_game = null, next_game = null;

        long gameID = Long.parseLong(strGameID);
        Long bankId = Long.parseLong(strBankID);
        BankInfo destBank = BankInfoCache.getInstance().getBankInfo(bankId);
        BaseGameInfoTemplate templateGame = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameID);

        String gameName = "NULL";
        String strID_PC = null;
        String strID_Mobile = null;
        String strID_Android = null;
        String strID_WindowsPhone = null;

        ArrayList<Long> games = new ArrayList<Long>();

        if (templateGame != null) {
            gameName = templateGame.getGameName();

            strID_PC = templateGame.getDefaultGameInfo().getProperty("PC");
            strID_Mobile = templateGame.getDefaultGameInfo().getProperty("IOSMOBILE");
            strID_Android = templateGame.getDefaultGameInfo().getProperty("ANDROID");
            strID_WindowsPhone = templateGame.getDefaultGameInfo().getProperty("WINDOWSPHONE");

            if (gameName.contains("MOBILE")) strID_Mobile = strGameID;
            else if (gameName.contains("ANDROID")) strID_Android = strGameID;
            else if (gameName.contains("WINDOWSPHONE")) strID_WindowsPhone = strGameID;
            else strID_PC = strGameID;

            Long idPC = (strID_PC != null ? Long.parseLong(strID_PC) : null);
            Long idMobile = (strID_Mobile != null ? Long.parseLong(strID_Mobile) : null);
            Long idAndroid = (strID_Android != null ? Long.parseLong(strID_Android) : null);
            Long idWindowsPhone = (strID_WindowsPhone != null ? Long.parseLong(strID_WindowsPhone) : null);

            if (idPC != null) {
                games.add(idPC);
                templatePC = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(idPC);
            }
            if (idMobile != null) {
                games.add(idMobile);
                templateMobile = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(idMobile);
            }
            if (idAndroid != null) {
                games.add(idAndroid);
                templateAndroid = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(idAndroid);
            }
            if (idWindowsPhone != null) {
                games.add(idWindowsPhone);
                templateWindowsPhone = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(idWindowsPhone);
            }

            int game_index = games.indexOf(gameID);

            if (strID_Mobile != null && strID_Android != null && strID_WindowsPhone != null &&
                    !strID_Mobile.isEmpty() && !strID_Android.isEmpty() && !strID_WindowsPhone.isEmpty()) {
                prev_game = (game_index > 0 ? games.get(games.indexOf(gameID) - 1) : gameID);
                next_game = (game_index < games.size() - 1 ? games.get(games.indexOf(gameID) + 1) : gameID);
            } else {
                prev_game = gameID;
                next_game = gameID;
            }
        }

        if (destBank != null) {
            response.getWriter().write("Source bank's SubCasino: " + destBank.getSubCasinoId() + "<br>");
        } else
            response.getWriter().write("Source bank is NULL<br>");

        Currency testCurrency = null;

        if (subCasino != null) {
            banks = subCasino.getBankIds();

            if (destBank != null) {
                int bank_index = banks.indexOf(bankId);

                prev_bank = (bank_index > 0 ? banks.get(banks.indexOf(bankId) - 1) : bankId);
                next_bank = (bank_index < banks.size() - 1 ? banks.get(banks.indexOf(bankId) + 1) : bankId);

                if (strCurrency != null && !strCurrency.isEmpty()) {
                    testCurrency = CurrencyCache.getInstance().get(strCurrency);
                    if (!destBank.getCurrencies().contains(testCurrency))
                        testCurrency = null;
                }
            }
            response.getWriter().write("Dest SubCasino has banks: " + Arrays.toString(banks.toArray()) + "<br>");
        } else
            response.getWriter().write("Dest SubCasino is NULL<br>");
%>

<FORM ACTION="editGameForm.jsp" METHOD="GET">
    Считать информацию об игре:
    <INPUT type="button" name="prevgame" value="<" title="Предыдущая платформа игры"
           onclick="reloadPage(<%=prev_game%>, <%=bankId%>, <%=Integer.parseInt(strSubcasinoID)%>)">
    <INPUT style="text-align: left;  width: 80px" type="text" id="id_game" name="gamecopy"
           onKeyDown="return isDigit(event, this, false);" value="<%=strGameID%>"/>
    <INPUT type="button" name="nextgame" value=">" title="Следующая платформа игры"
           onclick="reloadPage(<%=next_game%>, <%=bankId%>, <%=Integer.parseInt(strSubcasinoID)%>)">

    из банка:
    <INPUT type="button" name="prevbank" value="<" title="Предыдущий банк"
           onclick="reloadPage(<%=gameID%>, <%=prev_bank%>, <%=Integer.parseInt(strSubcasinoID)%>)">
    <INPUT style="text-align: left;  width: 80px" type="text" id="id_bank" name="bankcopy"
           onKeyDown="return isDigit(event, this, false);" value="<%=strBankID%>"/>
    <INPUT type="button" name="nextbank" value=">" title="Следующий банк"
           onclick="reloadPage(<%=gameID%>, <%=next_bank%>, <%=Integer.parseInt(strSubcasinoID)%>)">
    валюта:
    <INPUT style="text-align: left;  width: 80px; <%if (!strCurrency.isEmpty() && testCurrency == null) { %> background-color: red <%}%>"
           type="text" id="id_currency" name="currency" value="<%=strCurrency%>"/>
    <label for="id_subcasino">для добавления в субказино:</label>
    <select name="to_subcasino" id="id_subcasino">
        <% for (SubCasino sc : availableSubCasinos) { %>
        <option <%=subCasino.getId() == sc.getId() ? "selected" : ""%> value="<%=sc.getId()%>"><%=sc.getId()%>: <%=sc.getName()%></option>
        <% } %>
    </select>
    <INPUT style="width: 80px" type="submit" value="ОК"/>
</FORM>

<%
    if ((request.getParameter("bankcopy") != null) && (request.getParameter("gamecopy") != null)) { // If form submited
        if ((strBankID.toUpperCase().equals("") || strGameID.toUpperCase().equals(""))) { // If parameters not correct
%>
<DIV style="font-weight: bold; color: #ff0000;">ОШИБКА! Необходимо ввести ID банка и ID игры</DIV>
<%
} else {
    long bankID = Long.parseLong(strBankID);
    BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankID);

    if (bankInfo == null) {
%>
<DIV style="font-weight: bold; color: #ff0000;">ОШИБКА! Банк <%=bankID%> не существует</DIV>
<% } else {
    if (strSubcasinoID.toUpperCase().equals("")) strSubcasinoID = String.valueOf(bankInfo.getSubCasinoId());
%>
<script>
    document.getElementById("id_subcasino").value = <%=strSubcasinoID%>;
</script>
<%
    Currency currency = destBank.getDefaultCurrency();
    if (strCurrency != null && !strCurrency.isEmpty())
        currency = CurrencyCache.getInstance().get(strCurrency);

    IBaseGameInfo destGame = BaseGameCache.getInstance().getGameInfoById(bankId, gameID, currency);
    if (destGame == null) {
%>
<DIV style="font-weight: bold; color: #ff0000;">ОШИБКА! Эта игра <%=gameID%> отсутствует в банке: <%=bankID%>,
    валюта: <%=!strCurrency.isEmpty() ? strCurrency : destBank.getDefaultCurrency().getCode()%>
</DIV>
<%
    if (templateGame == null) {
%>
<DIV style="font-weight: bold; color: #ff0000;">ОШИБКА! Игра <%=gameID%> отсутствует в темплейте</DIV>
<%
        } else {
            destGame = templateGame.getDefaultGameInfo();
        }
    }

    if (destGame != null) {
        String gameProperties = destGame.getPropertiesMap().toString();

        String tempDefCoin = destGame.getProperty(BaseGameConstants.KEY_DEFAULT_COIN);
        String gameDefCoin = null;
        if (tempDefCoin != null && !tempDefCoin.equals("null")) gameDefCoin = destGame.getDefaultCoin().toString();
        String gameIsEnabled = destGame.isEnabled() ? "TRUE" : "FALSE";

        String gamePayoutPercent = (destGame.getProperty(BaseGameConstants.KEY_PAYOUT_PERCENT) != null) ? String.valueOf(destGame.getPayoutPercent()) : null;

        String cdnSupport = templateGame.getDefaultGameInfo().getProperty("CDN_SUPPORT");
        String lgaApproved = templateGame.getDefaultGameInfo().getProperty("LGA_APPROVED");

        String pcrDef = templateGame.getDefaultGameInfo().getProperty("PCR_DEF");

        String[] sideJPIds = readProperties("SIDE_JP_GAME_IDS");
        String[] fakeIdFor = readProperties("FAKE_ID_FOR");

        String jackpotName = templateGame.getDefaultGameInfo().getProperty("JACKPOT_NAME");
        String jackpotFreqency = templateGame.getDefaultGameInfo().getProperty("JACKPOT_HIT_FREQUENCY");

        String gameDoubleUp = templateGame.getDefaultGameInfo().getProperty("GAME_WITH_DOUBLE_UP");
        String gameProgress = templateGame.getDefaultGameInfo().getProperty("GAME_WITH_PROGRESS");

        if (jackpotName != null)
            jackpotName = jackpotName.replace("Mobile ", "").replace("Android ", "").replace("Windows Phone ", "");


        float[] jackpot3_percent_values = {-1, -1, -1};
        long[] jackpot3_min_values = {-1, -1, -1};
        long[] jackpot3_max_values = {-1, -1, -1};

        Limit limit = (Limit) destGame.getLimit();

        boolean isFRB = BaseGameInfoTemplateCache.getInstance().getFrbGames().contains(gameID);

        RoundFinishedHelper roundFinishedHelper = null;

        gameName = gameName.replace("MOBILE", "").replace("ANDROID", "").replace("WINDOWSPHONE", "");

        GameType gameType = destGame.getGameType();
        GameGroup gameGroup = destGame.getGroup();
        GameVariableType varType = destGame.getVariableType();

        if (strID_PC == null) strID_PC = "";
        if (strID_Mobile == null) strID_Mobile = "";
        if (strID_Android == null) strID_Android = "";
        if (strID_WindowsPhone == null) strID_WindowsPhone = "";

        String repository_file = "";
        String development_key = "";
        String endRoundSignature = "null";
        String title = "null";
        String frbCoin = (String) destGame.getPropertiesMap().get(BaseGameConstants.KEY_FRB_COIN);
        String gameTesting = (String) destGame.getPropertiesMap().get(BaseGameConstants.KEY_GAME_TESTING);

        String swfLocation = "/flash/game/game.swf";
        String gameControllerClass = resolveDefaultGameServletClassName();
        String servlet = "Game.game";
        String maxWin = "null";
        String RTP = "null";
        String possibleLines = "null";
        String possibleBetPerLines = "null";

        String linesCount = "null";
        String defaultNumLines = "null";
        String defaultBetPerLine = "null";

        Html5PcVersionMode html5PcVersionMode = Html5PcVersionMode.NOT_AVAILABLE;
        String unifiedLocation = "null";

        String pdfRulesName = "null";
        String additionalFlashVars = "null";

        String languagesPC = getLanguages(bankInfo, strID_PC);
        String languagesMobile = getLanguages(bankInfo, strID_Mobile);
        String languagesAndroid = getLanguages(bankInfo, strID_Android);
        String languagesWindowsPhone = getLanguages(bankInfo, strID_WindowsPhone);

        String coinsPC = getCoins(bankInfo, strID_PC);
        String coinsMobile = getCoins(bankInfo, strID_Mobile);
        String coinsAndroid = getCoins(bankInfo, strID_Android);
        String coinsWindowsPhone = getCoins(bankInfo, strID_WindowsPhone);


        if (frbCoin == null) frbCoin = "";

        if (templateGame != null) {
            swfLocation = templateGame.getSwfLocation();
            gameControllerClass = templateGame.getGameControllerClass();
            servlet = templateGame.getServlet();

            maxWin = templateGame.getDefaultGameInfo().getProperty("MAX_WIN");
            RTP = templateGame.getDefaultGameInfo().getProperty("RTP");
            possibleLines = templateGame.getDefaultGameInfo().getProperty("POSSIBLE_LINES");
            possibleBetPerLines = templateGame.getDefaultGameInfo().getProperty("POSSIBLE_BETPERLINES");

            linesCount = templateGame.getDefaultGameInfo().getProperty("LINES_COUNT");
            defaultNumLines = templateGame.getDefaultGameInfo().getProperty("DEFAULTNUMLINES");
            defaultBetPerLine = templateGame.getDefaultGameInfo().getProperty("DEFAULTBETPERLINE");

            repository_file = templateGame.getDefaultGameInfo().getRepositoryFile();
            development_key = templateGame.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_DEVELOPMENT_VERSION);
            endRoundSignature = templateGame.getEndRoundSignature();

            title = templateGame.getTitle();
            if (title != null)
                title = title.replace(" Mobile", "").replace(" Android", "").replace(" Windows Phone", "");
            else title = "null";

            if (servlet != null)
                servlet = servlet.replace("Mobile", "").replace("Android", "").replace("WindowsPhone", "");
            else servlet = "null";

            roundFinishedHelper = templateGame.getRoundFinishedHelper();

            String strHtml5Mode = templateGame.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_HTML5PC_VERSION_MODE);
            html5PcVersionMode = strHtml5Mode != null ? Html5PcVersionMode.valueOf(strHtml5Mode) : null;
            unifiedLocation = templateGame.getDefaultGameInfo().getProperty("UNIFIED_LOCATION");
            pdfRulesName = templateGame.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_PDF_RULES_NAME);
            additionalFlashVars = templateGame.getDefaultGameInfo().getProperty(BaseGameConstants.KEY_ADDITIONAL_FLASHVARS);

            if (repository_file == null) repository_file = "null";
            if (endRoundSignature == null) endRoundSignature = "null";
            if (development_key == null) development_key = "NULL";
        }

        etalonGames = getEtalonSlotsGames(gameID);
        if (destGame.getName().contains("ROULETTE")) {
            etalonGames = etalonRouletteGames;
        } else if (destGame.getName().contains("AAMS_")) {
            etalonGames = etalonAAMSGames;
        } else if (destGame.getId() >= 30000 && destGame.getId() < 40000) {
            etalonGames = etalonNGGames;
        }

%>
<HR>
<%
    String subcasino_name = "NULL";

    if (subCasino != null) {
        subcasino_name = subCasino.getName();
        if (subcasino_name == null)
            subcasino_name = subCasino.getStaticDirectoryName();
    }
%>

<b>Информация о лицензиате "<%=subcasino_name%>":</b><br>
<%
    String description = arraySubcasinoDescription.get(subCasino.getId());
    if (description == null) { %> Обычный лицензиат, выдача игр без дополнительных правил.<%} else { %> <%=description%> <%}%>

<HR>
<FORM id="mainForm" ACTION="applyGame.jsp" METHOD="POST">
    <INPUT style="width: 200px;margin-bottom: 4px" type="submit" onclick="return submitButton(this);" value="Принять"/>
    <TABLE id="mainTable" border="1">
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">NAME</TD>
            <TD>
                <INPUT id="id_name" style="width: 100%" type="text" name="game_name" value="<%=gameName%>"/></TD>

            <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">Title</TD>
            <TD>
                <INPUT class="class_template" style="width: 260px" type="text" id="id_title" name="title"
                       value="<%=title%>"/>
                <INPUT class="class_template" type="button" name="autoparams" value="Автоподстройка"
                       title="Автоматически построить параметры для текстовых полей, зависящих от названия игры, например, REPOSITORY_FILE, servlet..."
                       onclick="AutoParams()">
            </TD>
        </TR>

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">GAME_IDS</TD>
            <TD>
                <INPUT type="checkbox" name="check_pc"      <%if (!strID_PC.toUpperCase().equals("")) { %> checked <%}%>>
                <INPUT style="width: 50px" title="ID игры для Desktop" type="text" name="pc_id" onKeyDown="return isDigit(event, this, false);" value="<%=strID_PC%>"/>
                &nbsp;&nbsp;

                <INPUT type="checkbox" name="check_mobile"  <%if (!strID_Mobile.toUpperCase().equals("")) { %> checked <%}%>>
                <INPUT style="width: 50px" title="ID игры для Mobile" type="text" name="mobile_id" onKeyDown="return isDigit(event, this, false);" value="<%=strID_Mobile%>"/>
                &nbsp;&nbsp;

                <INPUT type="checkbox" name="check_android" <%if (!strID_Android.toUpperCase().equals("")) { %> checked <%}%>>
                <INPUT style="width: 50px" title="ID игры для Android" type="text" name="android_id" onKeyDown="return isDigit(event, this, false);" value="<%=strID_Android%>"/>
                &nbsp;&nbsp;

                <INPUT type="checkbox" name="check_wphone"  <%if (!strID_WindowsPhone.toUpperCase().equals("")) { %> checked <%}%>>
                <INPUT style="width: 50px" title="ID игры для Windows Phone" type="text" name="wphone_id" onKeyDown="return isDigit(event, this, false);" value="<%=strID_WindowsPhone%>"/>
                &nbsp;&nbsp;
            </TD>

            <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">REPOSITORY_FILE</TD>
            <TD>
                <INPUT class="class_template" style="width: 100%" type="text" id="id_repositoryFile"
                       name="repository_file" value="<%=repository_file%>"/>
            </TD>
        </TR>

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">GAME TYPE</TD>
            <TD>
                <SELECT style="width: 50px" name="game_type">
                    <OPTION value="SP" <% if (gameType == GameType.SP) { %> selected="selected" <% } %> >SP</OPTION>
                    <OPTION value="MP" <% if (gameType == GameType.MP) { %> selected="selected" <% } %> >MP</OPTION>
                </SELECT>&nbsp&nbsp

                <SELECT style="width: 80px" name="game_grouptype">
                    <OPTION value="SLOTS"            <% if (gameGroup == GameGroup.SLOTS) { %> selected="selected" <% } %> >SLOTS</OPTION>
                    <OPTION value="TABLE"            <% if (gameGroup == GameGroup.TABLE) { %> selected="selected" <% } %> >TABLE</OPTION>
                    <OPTION value="KENO"             <% if (gameGroup == GameGroup.KENO) { %> selected="selected" <% } %> >KENO</OPTION>
                    <OPTION value="LIVE"             <% if (gameGroup == GameGroup.LIVE) { %> selected="selected" <% } %> >LIVE</OPTION>
                    <OPTION value="SOFT_GAME_ARCADE" <% if (gameGroup == GameGroup.SOFT_GAME_ARCADE) { %> selected="selected" <% } %> >
                        SOFT_GAME_ARCADE
                    </OPTION>
                    <OPTION value="MULTIHAND_POKER"  <% if (gameGroup == GameGroup.MULTIHAND_POKER) { %> selected="selected" <% } %> >
                        MULTIHAND_POKER
                    </OPTION>
                    <OPTION value="MULTISTACK_POKER" <% if (gameGroup == GameGroup.MULTISTACK_POKER) { %> selected="selected" <% } %> >
                        MULTISTACK_POKER
                    </OPTION>
                    <OPTION value="PYRAMID_POKER"    <% if (gameGroup == GameGroup.PYRAMID_POKER) { %> selected="selected" <% } %> >PYRAMID_POKER
                    </OPTION>
                    <OPTION value="RUSH_THE_ROYAL"   <% if (gameGroup == GameGroup.RUSH_THE_ROYAL) { %> selected="selected" <% } %> >RUSH_THE_ROYAL
                    </OPTION>
                    <OPTION value="SOFT_GAMES"       <% if (gameGroup == GameGroup.SOFT_GAMES) { %> selected="selected" <% } %> >SOFT_GAMES</OPTION>
                    <OPTION value="VIDEOPOKER"       <% if (gameGroup == GameGroup.VIDEOPOKER) { %> selected="selected" <% } %> >VIDEOPOKER</OPTION>
                    <OPTION value="ACTION_GAMES"     <% if (gameGroup == GameGroup.ACTION_GAMES) { %> selected="selected" <% } %> >ACTION_GAMES
                    </OPTION>
                </SELECT>&nbsp&nbsp

                <SELECT style="width: 80px" name="game_vartype">
                    <OPTION value="COIN"  <% if (varType == GameVariableType.COIN) { %> selected="selected" <% } %> >COIN</OPTION>
                    <OPTION value="LIMIT" <% if (varType == GameVariableType.LIMIT) { %> selected="selected" <% } %> >LIMIT</OPTION>
                </SELECT>
            </TD>

            <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">swfLocation</TD>
            <TD><INPUT class="class_template" style="width: 100%" type="text" id="id_swfLocation" name="swfLocation"
                       value="<%=swfLocation%>"/></TD>
        </TR>


        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">DEFAULT COIN</TD>
            <TD>
                <INPUT style="width: 50px" type="text" name="defcoin" value="<%=gameDefCoin%>"/>
            </TD>

            <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">gameControllerClass</TD>
            <TD><INPUT class="class_template" style="width: 100%" type="text" id="id_gameClass"
                       name="gameControllerClass" value="<%=gameControllerClass%>"/></TD>
        </TR>

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">PAYOUT PERCENT</TD>
            <TD>
                <% if (templateGame.isDynamicLevelsSupported()) { %>
                <INPUT style="width: 50px" type="text" name="payout_percent" value="" disabled="disabled"/>
                <% } else { %>
                <INPUT style="width: 50px" type="text" name="payout_percent" value="<%=gamePayoutPercent%>"/>
                <% } %>
            </TD>

            <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">servlet</TD>
            <TD><INPUT class="class_template" style="width: 100%" type="text" id="id_servlet" name="servlet"
                       value="<%=servlet%>"/></TD>
        </TR>

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">GAME_TESTING</TD>
            <TD>
                <SELECT style="width: 100px" name="game_testing">
                    <OPTION value="NULL" selected="selected">
                        NULL
                    </OPTION>
                    <OPTION value="TRUE">
                        TRUE
                    </OPTION>
                </SELECT>
            </TD>

            <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">DEVELOPMENT_VERSION</TD>
            <TD>
                <SELECT class="class_template" style="width: 150px" name="is_development">
                    <OPTION value="TRUE" <% if (development_key.toUpperCase().equals("TRUE")) { %> selected="selected" <% } %> >TRUE</OPTION>
                    <OPTION value="FALSE"<% if (development_key.toUpperCase().equals("FALSE")) { %> selected="selected" <% } %> >FALSE</OPTION>
                    <OPTION value="NULL" <% if (development_key.toUpperCase().equals("NULL")) { %> selected="selected" <% } %> >NULL</OPTION>
                </SELECT>
            </TD>
        </TR>

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">LIMIT</TD>
            <TD>
                Min: <INPUT style="width: 25%;" type="text" name="limit_min"
                            value="<%=(limit == null) ? null : limit.getMinValue()%>"/>
                Max: <INPUT style="width: 25%;" type="text" name="limit_max"
                            value="<%=(limit == null) ? null : limit.getMaxValue()%>"/>
            </TD>

            <TD colspan="4">
                <TABLE>
                    <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">Round Finished Helper</TD>
                    <TD>
                        <SELECT class="class_template" style="width: 160px" name="roundFinishedHelper">
                            <OPTION value="NULL" <% if (roundFinishedHelper == null) { %> selected="selected" <% } %> >NULL</OPTION>
                            <%
                                for (RoundFinishedHelper helper : RoundFinishedHelper.values()) {
                            %>
                            <OPTION value="<%=helper.name()%>" <% if (roundFinishedHelper == helper) { %> selected="selected" <% } %> ><%=helper.name()%>
                            </OPTION>
                            <%
                                }
                            %>
                        </SELECT>
                    </TD>

                    <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">End Round Signature</TD>
                    <TD><INPUT class="class_template" style="width: 320px" type="text" name="endRoundSignature"
                               value="<%=endRoundSignature%>"/></TD>
                </TABLE>
            </TD>

        </TR>
        <input type=hidden id="id_limit_index" name="limit_index" value="<%if (limit != null) { %><%=limit.getId()%><%} else { %>0<%}%>">

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">KEY_ACS_ENABLED</TD>
            <TD>
                <SELECT style="width: 80px" name="is_key_acs" disabled>
                    <OPTION value="FALSE" selected="selected">FALSE</OPTION>
                </SELECT>
            </TD>

            <TD colspan="4">
                <TABLE>
                    <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">MAX_WIN</TD>
                    <TD><INPUT class="class_template" style="width: 160px" type="text" name="maxWin"
                               value="<%=maxWin%>"/></TD>

                    <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">RTP</TD>
                    <TD><INPUT class="class_template" style="width: 320px" type="text" name="RTP" value="<%=RTP%>"/>
                    </TD>
                </TABLE>
            </TD>
        </TR>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">IS_ENABLED</TD>
            <TD>
                <SELECT style="width:80px;" name="is_enabled">
                    <OPTION value="TRUE" <% if (gameIsEnabled.toUpperCase().equals("TRUE")) { %>
                            selected="selected" <% } %> >TRUE
                    </OPTION>
                    <OPTION value="FALSE"<% if (gameIsEnabled.toUpperCase().equals("FALSE")) { %>
                            selected="selected" <% } %> >FALSE
                    </OPTION>
                </SELECT>
            </TD>

            <TD colspan="6">
                <TABLE>
                    <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">DEFAULTBETPERLINE</TD>
                    <TD><INPUT class="class_template" style="width: 160px" type="text" name="defaultBetPerLine" value="<%=defaultBetPerLine%>"/></TD>

                    <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">DEFAULTNUMLINES</TD>
                    <TD><INPUT class="class_template" style="width: 80px" type="text" name="defaultNumLines" value="<%=defaultNumLines%>"/></TD>

                    <TD style="font-weight: bold; background-color: #bbbbbb; width: 150px">LINES_COUNT</TD>
                    <TD><INPUT class="class_template" style="width: 80px" type="text" name="linesCount" value="<%=linesCount%>"/></TD>
                </TABLE>
            </TD>
        </TR>

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">FRB</TD>
            <TD>
                <SELECT style="width: 80px" name="is_frb" onchange="showFRBInfo(this.value)">
                    <OPTION value="TRUE" <% if (isFRB) { %> selected="selected" <% } %> >TRUE</OPTION>
                    <OPTION value="FALSE"<% if (!isFRB) { %> selected="selected" <% } %> >FALSE</OPTION>
                </SELECT>
                <span id="frbTable" <%if (!isFRB) { %> style="visibility: hidden;" <% } %>>
                    coin:
                        <INPUT style="width: 80px" type="text" name="frb_coin" value="<%=frbCoin%>"/>
                </span>
            </TD>

            <TD colspan="4">
                <TABLE>
                    <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">POSSIBLE_BETPERLINES</TD>
                    <TD><INPUT class="class_template" style="width: 160px" type="text" name="possibleBetPerLines"
                               value="<%=possibleBetPerLines%>"/></TD>

                    <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">POSSIBLE_LINES</TD>
                    <TD><INPUT class="class_template" style="width: 320px" type="text" name="possibleLines"
                               value="<%=possibleLines%>"/></TD>
                </TABLE>
            </TD>
        </TR>

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">HANDLE_UNJ_WIN</TD>
            <TD>
            </TD>

            <TD colspan="4">
                <TABLE>
                    <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">HTML5PC_VERSION_MODE</TD>
                    <TD><SELECT class="class_template" style="width: 160px" name="html5PcVersionMode">
                        <OPTION value="NULL" <% if (html5PcVersionMode == null) { %> selected="selected" <% } %> >NULL</OPTION>
                        <%
                            for (Html5PcVersionMode html5Mode : Html5PcVersionMode.values()) {
                        %>
                        <OPTION value="<%=html5Mode.name()%>" <% if (html5Mode == html5PcVersionMode) { %> selected="selected" <% } %> ><%=html5Mode.name()%>
                        </OPTION>
                        <%
                            }
                        %>
                    </SELECT></TD>

                    <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">UNIFIED_LOCATION</TD>
                    <TD><INPUT class="class_template" style="width: 320px" type="text" name="unifiedLocation"
                               value="<%=unifiedLocation%>"/></TD>
                </TABLE>
            </TD>

        </TR>

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">UNJ VALUES</TD>
            <TD>
            </TD>

            <TD colspan="4">
                <TABLE>
                    <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">PDF_RULES_NAME</TD>
                    <TD><INPUT class="class_template" style="width: 160px" type="text" name="pdfRulesName"
                               value="<%=pdfRulesName%>"/></TD>

                    <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">ADDIT.._FLASHVARS</TD>
                    <TD><INPUT class="class_template" style="width: 320px" type="text" name="additionalFlashVars"
                               value="<%=additionalFlashVars%>"/></TD>
                </TABLE>
            </TD>

        </TR>

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">UNJ PCRP VALUES</TD>
            <TD>
            </TD>

            <TD colspan="4">
                <TABLE>
                    <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">EMPTY</TD>
                    <TD><INPUT class="class_empty" style="width: 160px" type="text" name="empty" value=""/></TD>

                    <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">EMPTY</TD>
                    <TD><INPUT class="class_empty" style="width: 320px" type="text" name="empty" value=""/></TD>
                </TABLE>
            </TD>
        </TR>

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">UNJ BCRP VALUES</TD>
            <TD>
            </TD>

            <TD colspan="4">
                <TABLE>
                    <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">EMPTY</TD>
                    <TD><INPUT class="class_empty" style="width: 160px" type="text" name="empty" value=""/></TD>

                    <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">EMPTY</TD>
                    <TD><INPUT class="class_empty" style="width: 320px" type="text" name="empty" value=""/></TD>
                </TABLE>
            </TD>
        </TR>


        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">IS_JACKPOT</TD>
            <TD>
            </TD>

            <TD colspan="4">
                <TABLE>
                    <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">CDN_SUPPORT</TD>
                    <TD>
                        <SELECT class="class_template" style="width: 160px" name="cdn_support">
                            <OPTION value="NULL" <% if (cdnSupport == null) { %> selected="selected" <% } %> >NULL</OPTION>
                            <OPTION value="TRUE" <% if (cdnSupport != null && cdnSupport.toUpperCase().equals("TRUE")) { %> selected="selected" <% } %> >
                                TRUE
                            </OPTION>
                            <OPTION value="FALSE"<% if (cdnSupport != null && cdnSupport.toUpperCase().equals("FALSE")) { %> selected="selected" <% } %> >
                                FALSE
                            </OPTION>
                        </SELECT>
                    </TD>

                    <TD style="font-weight: bold; background-color: #bbbbbb; width: 200px">LGA_APPROVED</TD>
                    <TD>
                        <SELECT class="class_template" style="width: 160px" name="lga_approved">
                            <OPTION value="NULL" <% if (lgaApproved == null) { %> selected="selected" <% } %> >NULL</OPTION>
                            <OPTION value="TRUE" <% if (lgaApproved != null && lgaApproved.toUpperCase().equals("TRUE")) { %> selected="selected" <% } %> >
                                TRUE
                            </OPTION>
                            <OPTION value="FALSE"<% if (lgaApproved != null && lgaApproved.toUpperCase().equals("FALSE")) { %> selected="selected" <% } %> >
                                FALSE
                            </OPTION>
                        </SELECT>
                    </TD>
                </TABLE>
            </TD>

        </TR>

        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb">IS_JACKPOT_3</TD>
            <TD>
            </TD>

            <TD colspan="4">
                <TABLE>
                    <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">GAME_WITH_DOUBLE_UP</TD>
                    <TD>
                        <SELECT class="class_template" style="width: 160px" name="game_dblup">
                            <OPTION value="NULL" <% if (gameDoubleUp == null) { %> selected="selected" <% } %> >NULL
                            </OPTION>
                            <OPTION value="TRUE" <% if (gameDoubleUp != null && gameDoubleUp.toUpperCase().equals("TRUE")) { %>
                                    selected="selected" <% } %> >TRUE
                            </OPTION>
                            <OPTION value="FALSE"<% if (gameDoubleUp != null && gameDoubleUp.toUpperCase().equals("FALSE")) { %>
                                    selected="selected" <% } %> >FALSE
                            </OPTION>
                        </SELECT>
                    </TD>

                    <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">GAME_WITH_PROGRESS</TD>
                    <TD>
                        <SELECT class="class_template" style="width: 160px" name="game_progress">
                            <OPTION value="NULL" <% if (gameProgress == null) { %> selected="selected" <% } %> >NULL</OPTION>
                            <OPTION value="TRUE" <% if (gameProgress != null && gameProgress.toUpperCase().equals("TRUE")) { %>
                                    selected="selected" <% } %> >TRUE
                            </OPTION>
                            <OPTION value="FALSE"<% if (gameProgress != null && gameProgress.toUpperCase().equals("FALSE")) { %>
                                    selected="selected" <% } %> >FALSE
                            </OPTION>
                        </SELECT>
                    </TD>
                </TABLE>
            </TD>
        </TR>


        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">LANGUAGES PC</TD>
            <TD colspan="4"><INPUT style="width: 100%" type="text" name="languages_pc" value="<%=languagesPC%>"/></TD>
        </TR>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">LANGUAGES MOBILE</TD>
            <TD colspan="4"><INPUT style="width: 100%" type="text" name="languages_mobile" value="<%=languagesMobile%>"/></TD>
        </TR>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">LANGUAGES ANDROID</TD>
            <TD colspan="4"><INPUT style="width: 100%" type="text" name="languages_android" value="<%=languagesAndroid%>"/></TD>
        </TR>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">LANGUAGES WINPHONE</TD>
            <TD colspan="4"><INPUT style="width: 100%" type="text" name="languages_windowsphone" value="<%=languagesWindowsPhone%>"/></TD>
        </TR>

        <% if (templateGame.isDynamicLevelsSupported()) { %>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">COINS PC</TD>
            <TD colspan="4"><INPUT style="width: 100%;" type="text" name="coins_pc" value="" disabled="disabled"/></TD>
        </TR>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">COINS MOBILE</TD>
            <TD colspan="4"><INPUT style="width: 100%;" type="text" name="coins_mobile" readonly value="" disabled="disabled"/></TD>
        </TR>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">COINS ANDROID</TD>
            <TD colspan="4"><INPUT style="width: 100%;" type="text" name="coins_android" readonly value="" disabled="disabled"/></TD>
        </TR>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">COINS WINDOWS PHONE</TD>
            <TD colspan="4"><INPUT style="width: 100%;" type="text" name="coins_windowsphone" readonly value="" disabled="disabled"/></TD>
        </TR>
        <% } else { %>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">COINS PC</TD>
            <TD colspan="4"><INPUT style="width: 100%;" type="text" name="coins_pc" value="<%=coinsPC%>"/></TD>
        </TR>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">COINS MOBILE</TD>
            <TD colspan="4"><INPUT style="width: 100%;" type="text" name="coins_mobile" readonly value="<%=coinsMobile%>"/></TD>
        </TR>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">COINS ANDROID</TD>
            <TD colspan="4"><INPUT style="width: 100%;" type="text" name="coins_android" readonly value="<%=coinsAndroid%>"/></TD>
        </TR>
        <TR>
            <TD style="font-weight: bold; background-color: #bbbbbb; width: <%=nameWidth%>px">COINS WINDOWS PHONE</TD>
            <TD colspan="4"><INPUT style="width: 100%;" type="text" name="coins_windowsphone" readonly value="<%=coinsWindowsPhone%>"/></TD>
        </TR>
        <% } %>
    </TABLE>

    <BR><BR>
    <span style="font-weight: bold;">ADDITIONAL PROPERTIES BASEGAMEINFO:</span><BR>
    <TABLE id="propertiesTable" border="1">
        <%
            Map<String, String> properties = destGame.getPropertiesMap();

            if (subCasino.getId() == 56L || subCasino.getId() == 57L) { // XPRO
                String gameImageUrl = properties.get(BaseGameConstants.KEY_GAME_IMAGE_URL);

                if (gameImageUrl == null) {
                    gameImageUrl = serverName.substring(serverName.indexOf(".") + 1);
                    String url = gameImageUrl.substring(gameImageUrl.indexOf(".") + 1);

                    gameImageUrl = "http://lobby-xprogaming." + url + "/stlobbies/islandluck/black/photos/game_" + strID_PC + ".png";
                }

                String freeBalance = properties.get(BaseGameConstants.KEY_FREEBALANCE);
                if (freeBalance == null) freeBalance = "50000";

                properties.put(BaseGameConstants.KEY_GAME_IMAGE_URL, gameImageUrl);
                properties.put(BaseGameConstants.KEY_FREEBALANCE, "50000");
            } else if (subCasino.getId() == 32L) { // PARLAY_BINGO
                String freeBalance = properties.get(BaseGameConstants.KEY_FREEBALANCE);
                if (freeBalance == null) freeBalance = "25000";

                properties.put(BaseGameConstants.KEY_FREEBALANCE, "25000");
            }


            Set<String> propKeys = properties.keySet();

            String[] keys = propKeys.toArray(new String[propKeys.size()]);

            int index = 0;
            for (int i = 0; i < propKeys.size(); i++) {
                String key = keys[i];
                String value = (String) destGame.getPropertiesMap().get(key);

                String templateValue = templateGame.getDefaultGameInfo().getProperty(key);

                if ((!key.startsWith("MAX_BET_") || key.startsWith("MAX_BET_IN_CREDITS")) && (!keyIsNew(key) || templateValue == value || value.equals(templateValue))) {
                    continue;
                }
        %>
        <TR>
            <TD><INPUT style="width: 250px; font-weight: bold;" type="text" class="class_prop_key" name="prop_key_<%=index%>" value="<%=key%>"/></TD>
            <TD><INPUT style="width: 150px;" type="text" class="class_prop_value" name="prop_value_<%=index%>" value="<%=value%>"/></TD>
            <TD><INPUT type="button" name="delbutton" value="X" onclick="deleteRow(this)" id="<%=index%>"></TD>
            <TD><INPUT type="checkbox" class="class_prop_flash" name="prop_flash_x"> Только PC'
        </TR>
        <%
                index += 1;
            }
        %>

        <TR>
            <TD colspan="3"><INPUT style="width: 450px; font-weight: bold;" type="button" name="prop_key_new"
                                   value="add property" onclick="addRowToPropertiesTable()"/></TD>
        </TR>
    </TABLE>

    <input type=hidden id="id_property_count" name="property_count" value="0">


    <HR>
    Добавить/обновить игру в банках:
    <INPUT style="width: 100px;" type="button" name="select_all_banks" value="Select All" onclick="setBanks(true)"/>
    <INPUT style="width: 100px;" type="button" name="unselect_all_banks" value="Unselect All"
           onclick="setBanks(false)"/>
    <INPUT type="checkbox" name="check_test" title="Имитирует работу, отображая только отчёт" hidden/> <span hidden>Тестовый режим</span>
    <INPUT type="checkbox" name="check_template"
           title="Будут ли обновляться параметры в темплейтах, иначе простая выдача на банки"> Обновлять темплейты
    <INPUT type="checkbox" name="check_languages" title="Будут ли обновляться языки, если игра уже была выдана">
    Обновлять языки
    <BR>
    <%
        try {
            int bank_index = 0;
            for (Long bank : banks) {
                boolean isDisabled = false;
                boolean isDelivered = true;
                boolean isSlave = false;


                BankInfo deliverBankInfo = BankInfoCache.getInstance().getBankInfo(bank);

                if (deliverBankInfo == null) { %> <b>BANK <%=bank%> IS NULL </b><% continue;
}

    isSlave = deliverBankInfo.getMasterBankId() != null && !deliverBankInfo.getMasterBankId().equals(bank);

    IBaseGameInfo gameInfoPC = (strID_PC.equals("") ? null : BaseGameCache.getInstance().getGameInfoById(bank, Long.parseLong(strID_PC), deliverBankInfo.getDefaultCurrency()));
    IBaseGameInfo gameInfoIOS = (strID_Mobile.equals("") ? null : BaseGameCache.getInstance().getGameInfoById(bank, Long.parseLong(strID_Mobile), deliverBankInfo.getDefaultCurrency()));
    IBaseGameInfo gameInfoAND = (strID_Android.equals("") ? null : BaseGameCache.getInstance().getGameInfoById(bank, Long.parseLong(strID_Android), deliverBankInfo.getDefaultCurrency()));
    IBaseGameInfo gameInfoWP = (strID_WindowsPhone.equals("") ? null : BaseGameCache.getInstance().getGameInfoById(bank, Long.parseLong(strID_WindowsPhone), deliverBankInfo.getDefaultCurrency()));

    boolean isDeliveredPC = strID_PC.equals("") || (gameInfoPC != null && gameInfoPC.isEnabled());
    boolean isDeliveredMobile = strID_Mobile.equals("") || (gameInfoIOS != null && gameInfoIOS.isEnabled());
    boolean isDeliveredAndroid = strID_Android.equals("") || (gameInfoAND != null && gameInfoAND.isEnabled());
    boolean isDeliveredWindowsPhone = strID_WindowsPhone.equals("") || (gameInfoWP != null && gameInfoWP.isEnabled());

    if (disabledBanks.contains(bank) || !deliverBankInfo.isEnabled()) {
        isDisabled = true;
    } else {
        IBaseGameInfo gameInfoAAMSBank_1 = BaseGameCache.getInstance().getGameInfoById(bank, 50000L, null);
        IBaseGameInfo gameInfoAAMSBank_2 = BaseGameCache.getInstance().getGameInfoById(bank, 50028L, null);
        IBaseGameInfo gameInfoAAMSBank_3 = BaseGameCache.getInstance().getGameInfoById(bank, 50149L, null);
        IBaseGameInfo gameInfoAAMSBank_4 = BaseGameCache.getInstance().getGameInfoById(bank, 50177L, null);

        //if ((gameInfoAAMSBank_1 != null || gameInfoAAMSBank_2 != null ||
        //     gameInfoAAMSBank_3 != null || gameInfoAAMSBank_4 != null) &&
        //     Long.parseLong(strID_PC) < 50000) {
        //    isDisabled = true;
        //}
    }


    if (!isDeliveredPC || !isDeliveredMobile || !isDeliveredAndroid || !isDeliveredWindowsPhone)
        isDelivered = false;

    if ((bank_index > 0) && (bank_index % 5 == 0)) { %> <br> <% }
    bank_index += 1;
%>

    <INPUT type="checkbox" name="check_banks[]"
           <%if (!isDelivered && !isDisabled && !isSlave) { %>checked <% } %>
           <%if (isDisabled && bank != 1488) { %>disabled <% } %>
           value="<%=bank%>"><%=bank%> &nbsp;&nbsp;
    <%
        }
    %>
    <INPUT type="checkbox" name="check_banks[]" value="833">833
    <%
        } catch (Exception ex) {
            if (ex != null && response != null)
                response.getWriter().write("EXCEPTION: " + ex.toString());
        }
    %>
    <BR>
    Валюты: <INPUT style="width: <%=nameWidth%>px;"
                   title="Выполнить только для указнных валют, если пусто, то для всех. Пример: 'EUR, USD'" type="text"
                   name="currencies" value="">
    <BR>
    Эталоны:
    <% if (templateGame.isDynamicLevelsSupported()) { %>
    <INPUT style="width: 234px;" title="Эталоны не используются для игр с динамическими монетами" type="text" name="etalon_games"
           value="" disabled="disabled">
    <% } else { %>
    <INPUT style="width: 234px;" title="На какие игры следует ориентрироваться, для копирования пропертей и на какие валюты выдавать, если пусто, то игра будет выдана на все указанные валюты" type="text" name="etalon_games"
           value="<%=strID_PC%><%=(etalonGames == null || "".equals(etalonGames) ? "" : ", " + etalonGames)%>">
    <% } %>
    &nbsp::::&nbsp<INPUT type="checkbox" name="check_etalon_coins" checked> Монеты/лимиты/чипы
    &nbsp::::&nbsp<INPUT type="checkbox" name="check_etalon_frb"   <%if (title != null && !title.equals("null")) {%>
                         checked <%}%>> ФРБ
    &nbsp::::&nbsp<INPUT type="checkbox" name="check_etalon_acs"   <%if (title != null && !title.equals("null")) {%>
                         disabled <%}%>> АКС
    <BR><BR>
    <INPUT style="width: 200px;" type="submit" id="id_button_save" onclick="return submitButton(this);" value="Принять"/>
</FORM>
<%
                    }
                }
            }
        }
    } catch (NullPointerException ex) {

    }
%>
</BODY>
</HTML>
