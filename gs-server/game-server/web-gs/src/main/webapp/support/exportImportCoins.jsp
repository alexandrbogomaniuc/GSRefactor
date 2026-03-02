<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.BaseGameCache" %>
<%@ page import="com.abs.casino.common.cache.BaseGameInfoTemplateCache" %>
<%@ page import="com.abs.casino.common.cache.CurrencyCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.cache.data.bank.Coin" %>
<%@ page import="com.abs.casino.common.cache.data.bank.Limit" %>
<%@ page import="com.abs.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameConstants" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.abs.casino.common.cache.data.game.GameVariableType" %>
<%@ page import="com.abs.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="com.abs.casino.common.exception.CommonException" %>
<%@ page import="com.abs.casino.common.util.property.PropertyUtils" %>
<%@ page import="com.abs.casino.common.util.string.StringUtils" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="com.google.common.base.Joiner" %>
<%@ page import="com.google.common.base.Splitter" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.reflect.TypeToken" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="org.apache.commons.fileupload.FileItemFactory" %>
<%@ page import="org.apache.commons.fileupload.disk.DiskFileItemFactory" %>
<%@ page import="org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.lang.reflect.Type" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.stream.Collectors" %>
<%--
  Author: svvedenskiy
  Date: 5/26/20
--%>

<%
    String banksStr = request.getParameter("banks");
    if (!StringUtils.isTrimmedEmpty(banksStr)) {
        //Export:

        //Params
        Splitter splitter = Splitter.on(",").omitEmptyStrings().trimResults();
        List<Long> banks = splitter.splitToList(banksStr).stream().map(s -> Long.parseLong(s)).collect(Collectors.toList());
        List<Long> games = splitter.splitToList(request.getParameter("games")).stream().map(s -> Long.parseLong(s)).collect(Collectors.toList());
        List<String> currencies = splitter.splitToList(request.getParameter("currencies"));

        String fileName = Joiner.on("_").join(banks);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        fileName += "_" + dateTimeFormatter.format(LocalDate.now()) + ".json";
        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        Collection<BankCoins> bankCoinsCollection = exportSettingsForBanks(banks, currencies, games);

        response.getWriter().write(new Gson().toJson(bankCoinsCollection));
        return;
    }


    if (ServletFileUpload.isMultipartContent(request)) {
        //Import:
        response.getWriter().write("Import...<br/>");

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List items = upload.parseRequest(request);
        Iterator iterator = items.iterator();
        while (iterator.hasNext()) {
            FileItem item = (FileItem) iterator.next();
            if (!item.isFormField()) {
                String json = item.getString();
                Type collectionType = new TypeToken<Collection<BankCoins>>() {
                }.getType();
                Collection<BankCoins> bankCoinsCollection = new Gson().fromJson(json, collectionType);
                importBankCoins(bankCoinsCollection, response.getWriter());
            }
        }
        response.getWriter().write("Import successfully finished<br/>");
        return;
    }
%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<head>
    <title>Export/Import coins</title>
    <script type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
    <script type="text/javascript">
        function exportButtonClick() {
            if ($("#banks").val() == "") {
                alert("Please fill 'Banks' field");
                return;
            }
            $("#exportButton").val("Exporting....").prop('disabled', true);
            $("#exportForm").submit();
        }

        function importButtonClick() {
            if ($("#importFileInput").val() == "") {
                alert("No file chosen");
                return;
            }
            $("#importForm").submit();
        }
    </script>
</head>
<body>
<h3>Export</h3>
<form id="exportForm" action="exportImportCoins.jsp" method="GET">
    <table>
        <tr>
            <td>Banks:</td>
            <td>
                <input id="banks" name="banks" style="width: 800px" type="text"/>
            </td>
        </tr>
        <tr>
            <td>Currencies:</td>
            <td>
                <input name="currencies" style="width: 800px" type="text"/>
            </td>
        </tr>
        <tr>
            <td>Games:</td>
            <td>
                <input name="games" style="width: 800px" type="text" title=""/>
            </td>
        </tr>
    </table>
    <input id="exportButton" style="width: 120px" type="button" value="Export" onclick="exportButtonClick()"/>
</form>
<br/>
<h3>Import</h3>
<form id="importForm" action="exportImportCoins.jsp" method="POST" enctype="multipart/form-data">
    <input id="importFileInput" type="file" name="file" accept="application/json"/>
    <input id="importButton" style="width: 120px" type="button" value="Import" onclick="importButtonClick()"/>
</form>
</body>
</html>

<%!
    ////////////////////////////////////////// IMPORT

    private void importBankCoins(Collection<BankCoins> bankCoinsCollection, PrintWriter writer) throws CommonException {
        for (BankCoins bankCoins : bankCoinsCollection) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankCoins.bankId);
            if (bankInfo == null || !bankInfo.isEnabled()) {
                continue;
            }
            if (bankCoins.coins != null) {
                bankInfo.setCoins(bankCoins.coins.stream().map(Coin::getByValue).collect(Collectors.toList()));
            }
            if (bankCoins.limitMaxValue != null && bankCoins.limitMinValue != null) {
                bankInfo.setLimit(Limit.valueOf(bankCoins.limitMinValue, bankCoins.limitMaxValue));
            }
            if (bankCoins.glMinBet != null) {
                bankInfo.setProperty(BankInfo.KEY_GL_MIN_BET, String.valueOf(bankCoins.glMinBet));
            } else {
                bankInfo.removeProperty(BankInfo.KEY_GL_MIN_BET);
            }
            if (bankCoins.glMaxBet != null) {
                bankInfo.setProperty(BankInfo.KEY_GL_MAX_BET, String.valueOf(bankCoins.glMaxBet));
            } else {
                bankInfo.removeProperty(BankInfo.KEY_GL_MAX_BET);
            }
            if (bankCoins.glMaxExposure != null) {
                bankInfo.setProperty(BankInfo.KEY_GL_MAX_EXPOSURE, String.valueOf(bankCoins.glMaxExposure));
            } else {
                bankInfo.removeProperty(BankInfo.KEY_GL_MAX_EXPOSURE);
            }
            if (bankCoins.glDefaultBet != null) {
                bankInfo.setProperty(BankInfo.KEY_GL_DEFAULT_BET, String.valueOf(bankCoins.glDefaultBet));
            } else {
                bankInfo.removeProperty(BankInfo.KEY_GL_DEFAULT_BET);
            }
            if (bankCoins.glOfrbBet != null) {
                bankInfo.setProperty(BankInfo.KEY_GL_OFRB_BET, String.valueOf(bankCoins.glOfrbBet));
            } else {
                bankInfo.removeProperty(BankInfo.KEY_GL_OFRB_BET);
            }
            if (bankCoins.glOcbMaxBet != null) {
                bankInfo.setProperty(BankInfo.KEY_GL_OCB_MAX_BET, String.valueOf(bankCoins.glOcbMaxBet));
            } else {
                bankInfo.removeProperty(BankInfo.KEY_GL_OCB_MAX_BET);
            }
            if (bankCoins.glOcbMaxTableLimit != null) {
                bankInfo.setProperty(BankInfo.KEY_GL_OCB_MAX_TABLE_LIMIT, String.valueOf(bankCoins.glOcbMaxTableLimit));
            } else {
                bankInfo.removeProperty(BankInfo.KEY_GL_OCB_MAX_TABLE_LIMIT);
            }
            if (bankCoins.glNumberOfCoins != null) {
                bankInfo.setProperty(BankInfo.KEY_GL_NUMBER_OF_COINS, String.valueOf(bankCoins.glNumberOfCoins));
            } else {
                bankInfo.removeProperty(BankInfo.KEY_GL_NUMBER_OF_COINS);
            }
            if (bankCoins.glNumberOfChips != null) {
                bankInfo.setProperty(BankInfo.KEY_GL_NUMBER_OF_CHIPS, String.valueOf(bankCoins.glNumberOfChips));
            } else {
                bankInfo.removeProperty(BankInfo.KEY_GL_NUMBER_OF_CHIPS);
            }
            if (bankCoins.glUseDefaultCurrency != null) {
                bankInfo.setProperty(BankInfo.KEY_GL_USE_DEFAULT_CURRENCY, String.valueOf(bankCoins.glUseDefaultCurrency));
            } else {
                bankInfo.removeProperty(BankInfo.KEY_GL_USE_DEFAULT_CURRENCY);
            }
            RemoteCallHelper.getInstance().saveAndSendNotification(bankInfo);
            importGameSettings(bankInfo, bankCoins.games, writer);
        }
    }

    private void importGameSettings(BankInfo bankInfo, List<GameSettings> games, PrintWriter writer) throws CommonException {
        if (games == null) {
            return;
        }
        for (GameSettings gameSettings : games) {
            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoShared(bankInfo.getId(),
                    gameSettings.gameId, CurrencyCache.getInstance().get(gameSettings.currency));
            if (gameInfo == null || !(gameInfo instanceof BaseGameInfo)) {
                writer.write("The game #" + gameSettings.gameId + " " + gameSettings.currency
                        + " doesn't exist for bank #" + bankInfo.getId() + "<br/>");
                continue;
            }
            writer.write("Import game #" + gameSettings.gameId + " " + gameSettings.currency + " "
                    + " to the bank #" + bankInfo.getId() + " ");
            if (gameSettings.defCoin != null) {
                gameInfo.setDefaultCoin(gameSettings.defCoin);
            }
            if (gameInfo.getVariableType() == GameVariableType.COIN) {
                if (gameSettings.coins != null) {
                    writer.write("coins: " + gameSettings.coins);
                    gameInfo.setCoins(gameSettings.coins.stream().map(Coin::getByValue).collect(Collectors.toList()));
                }
                if (gameSettings.frbCoin != null) {
                    gameInfo.setProperty(BaseGameConstants.KEY_FRB_COIN, String.valueOf(gameSettings.frbCoin));
                }
                if (gameSettings.glMinBet != null) {
                    gameInfo.setProperty(BaseGameConstants.KEY_GL_MIN_BET, String.valueOf(gameSettings.glMinBet));
                } else {
                    gameInfo.removeProperty(BaseGameConstants.KEY_GL_MIN_BET);
                }
                if (gameSettings.glMaxBet != null) {
                    gameInfo.setProperty(BaseGameConstants.KEY_GL_MAX_BET, String.valueOf(gameSettings.glMaxBet));
                } else {
                    gameInfo.removeProperty(BaseGameConstants.KEY_GL_MAX_BET);
                }
                if (gameSettings.glNumberOfCoins != null) {
                    gameInfo.setProperty(BaseGameConstants.KEY_GL_NUMBER_OF_COINS, String.valueOf(gameSettings.glNumberOfCoins));
                } else {
                    gameInfo.removeProperty(BaseGameConstants.KEY_GL_NUMBER_OF_COINS);
                }
                if (gameSettings.glMaxExposure != null) {
                    gameInfo.setProperty(BaseGameConstants.KEY_GL_MAX_EXPOSURE, String.valueOf(gameSettings.glMaxExposure));
                } else {
                    gameInfo.removeProperty(BaseGameConstants.KEY_GL_MAX_EXPOSURE);
                }
                if (gameSettings.glDefaultBet != null) {
                    gameInfo.setProperty(BaseGameConstants.KEY_GL_DEFAULT_BET, String.valueOf(gameSettings.glDefaultBet));
                } else {
                    gameInfo.removeProperty(BaseGameConstants.KEY_GL_DEFAULT_BET);
                }
            } else {
                writer.write("limit: " + gameSettings.limitMinValue + " - " + gameSettings.limitMaxValue);
                if (gameSettings.limitMinValue != null && gameSettings.limitMaxValue != null) {
                    gameInfo.setLimit(Limit.valueOf(gameSettings.limitMinValue, gameSettings.limitMaxValue));
                }
                if (!StringUtils.isTrimmedEmpty(gameSettings.chipValues)) {
                    gameInfo.setChipValues(gameSettings.chipValues);
                }
                if (gameInfo.getName().toLowerCase().contains("roulet")) {
                    if (gameSettings.rouletteMaxBet1 != null) {
                        gameInfo.setProperty(BaseGameConstants.KEY_MAX_BET_1, String.valueOf(gameSettings.rouletteMaxBet1));
                    }
                    if (gameSettings.rouletteMaxBet2 != null) {
                        gameInfo.setProperty(BaseGameConstants.KEY_MAX_BET_2, String.valueOf(gameSettings.rouletteMaxBet2));
                    }
                    if (gameSettings.rouletteMaxBet3 != null) {
                        gameInfo.setProperty(BaseGameConstants.KEY_MAX_BET_3, String.valueOf(gameSettings.rouletteMaxBet3));
                    }
                    if (gameSettings.rouletteMaxBet4 != null) {
                        gameInfo.setProperty(BaseGameConstants.KEY_MAX_BET_4, String.valueOf(gameSettings.rouletteMaxBet4));
                    }
                    if (gameSettings.rouletteMaxBet5 != null) {
                        gameInfo.setProperty(BaseGameConstants.KEY_MAX_BET_5, String.valueOf(gameSettings.rouletteMaxBet5));
                    }
                    if (gameSettings.rouletteMaxBet6 != null) {
                        gameInfo.setProperty(BaseGameConstants.KEY_MAX_BET_6, String.valueOf(gameSettings.rouletteMaxBet6));
                    }
                    if (gameSettings.rouletteMaxBet12 != null) {
                        gameInfo.setProperty(BaseGameConstants.KEY_MAX_BET_12, String.valueOf(gameSettings.rouletteMaxBet12));
                    }
                    if (gameSettings.rouletteMaxBet18 != null) {
                        gameInfo.setProperty(BaseGameConstants.KEY_MAX_BET_18, String.valueOf(gameSettings.rouletteMaxBet18));
                    }
                }
            }
            writer.write("<br/>");
            RemoteCallHelper.getInstance().saveAndSendNotification(gameInfo);
        }
    }


    ////////////////////////////////////////// EXPORT

    private Collection<BankCoins> exportSettingsForBanks(Collection<Long> banks, Collection<String> currencies, Collection<Long> games) {
        List<BankCoins> bankCoinsList = new ArrayList<>();
        for (Long bankId : banks) {
            BankCoins bankCoins = exportBankCoins(bankId, currencies, games);
            if (bankCoins != null) {
                bankCoinsList.add(bankCoins);
            }
        }
        return bankCoinsList;
    }

    private BankCoins exportBankCoins(long bankId, Collection<String> currencies, Collection<Long> games) {
        BankInfo bank = BankInfoCache.getInstance().getBankInfo(bankId);
        if (bank == null || !bank.isEnabled()) {
            return null;
        }

        BankCoins bankCoins = new BankCoins();
        bankCoins.bankId = bankId;
        if (bank.getCoins() != null) {
            bankCoins.coins = bank.getCoins().stream().map(c -> c.getValue()).collect(Collectors.toList());
        }
        if (bank.getLimit() != null) {
            bankCoins.limitMinValue = bank.getLimit().getMinValue();
            bankCoins.limitMaxValue = bank.getLimit().getMaxValue();
        }
        bankCoins.glMinBet = bank.getMinBet();
        bankCoins.glMaxBet = bank.getMaxBet();
        bankCoins.glMaxExposure = bank.getMaxWin();
        bankCoins.glDefaultBet = bank.getDefaultBet();
        bankCoins.glOfrbBet = PropertyUtils.getLongProperty(bank.getProperties(), BankInfo.KEY_GL_OFRB_BET);
        bankCoins.glOcbMaxBet = bank.getOCBMaxBet();
        bankCoins.glOcbMaxTableLimit = bank.getOCBMaxTableLimit();
        bankCoins.glNumberOfCoins = bank.getCoinsNumber();
        bankCoins.glNumberOfChips = PropertyUtils.getIntProperty(bank.getProperties(), BankInfo.KEY_GL_NUMBER_OF_CHIPS);
        bankCoins.glUseDefaultCurrency = bank.isGLUseDefaultCurrency();
        bankCoins.games = exportGames(bank, currencies, games);
        return bankCoins;
    }

    private List<GameSettings> exportGames(BankInfo bank, Collection<String> currencies, Collection<Long> games) {
        if (currencies == null || currencies.isEmpty()) {
            currencies = bank.getCurrencies().stream().map(c -> c.getCode()).collect(Collectors.toList());
        }
        if (games == null || games.isEmpty()) {
            games = BaseGameCache.getInstance().getAllGamesSet(bank.getId(), bank.getDefaultCurrency());
        }
        List<GameSettings> gameSettingsList = new ArrayList<>();
        for (String currencyCode : currencies) {
            Currency currency = CurrencyCache.getInstance().get(currencyCode);
            for (long gameId : games) {
                IBaseGameInfo game = BaseGameCache.getInstance().getGameInfoShared(bank.getId(), gameId, currency);
                if (game == null || !(game instanceof BaseGameInfo)) {
                    continue;
                }
                GameSettings gameSettings = new GameSettings();
                gameSettings.gameId = gameId;
                gameSettings.currency = currencyCode;
                gameSettings.defCoin = PropertyUtils.getIntProperty(game.getProperties(), BaseGameConstants.KEY_DEFAULT_COIN);
                if (game.getVariableType() == GameVariableType.COIN) {
                    gameSettings.frbCoin = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_FRB_COIN);
                    if (BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId).isDynamicLevelsSupported()) {
                        gameSettings.glMinBet = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_GL_MIN_BET);
                        gameSettings.glMaxBet = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_GL_MAX_BET);
                        gameSettings.glNumberOfCoins = PropertyUtils.getIntProperty(game.getProperties(), BaseGameConstants.KEY_GL_NUMBER_OF_COINS);
                        gameSettings.glMaxExposure = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_GL_MAX_EXPOSURE);
                        gameSettings.glDefaultBet = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_GL_DEFAULT_BET);
                    } else {
                        gameSettings.coins = (List<Long>) game.getCoins().stream().map(c -> ((Coin) c).getValue()).collect(Collectors.toList());
                    }
                } else {
                    gameSettings.limitMinValue = game.getLimit().getMinValue();
                    gameSettings.limitMaxValue = game.getLimit().getMaxValue();
                    gameSettings.chipValues = game.getChipValues();
                    if (game.getName().toLowerCase().contains("roulet")) {
                        gameSettings.rouletteMaxBet1 = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_MAX_BET_1);
                        gameSettings.rouletteMaxBet2 = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_MAX_BET_2);
                        gameSettings.rouletteMaxBet3 = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_MAX_BET_3);
                        gameSettings.rouletteMaxBet4 = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_MAX_BET_4);
                        gameSettings.rouletteMaxBet5 = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_MAX_BET_5);
                        gameSettings.rouletteMaxBet6 = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_MAX_BET_6);
                        gameSettings.rouletteMaxBet12 = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_MAX_BET_12);
                        gameSettings.rouletteMaxBet18 = PropertyUtils.getLongProperty(game.getProperties(), BaseGameConstants.KEY_MAX_BET_18);
                    }
                }
                gameSettingsList.add(gameSettings);
            }
        }
        return gameSettingsList;
    }


    class BankCoins {
        public long bankId;
        public List<Long> coins;
        public Integer limitMinValue;
        public Integer limitMaxValue;

        public Long glMinBet;
        public Long glMaxBet;
        public Long glMaxExposure;
        public Long glDefaultBet;
        public Long glOfrbBet;
        public Long glOcbMaxBet;
        public Long glOcbMaxTableLimit;
        public Integer glNumberOfCoins;
        public Integer glNumberOfChips;
        public Boolean glUseDefaultCurrency;
        public List<GameSettings> games;
    }

    class GameSettings {
        public long gameId;
        public String currency;

        public List<Long> coins;
        public Long frbCoin;
        public Integer defCoin;
        public Long glMinBet;
        public Long glMaxBet;
        public Integer glNumberOfCoins;
        public Long glMaxExposure;
        public Long glDefaultBet;

        public Integer limitMinValue;
        public Integer limitMaxValue;
        public String chipValues;

        public Long rouletteMaxBet1;
        public Long rouletteMaxBet2;
        public Long rouletteMaxBet3;
        public Long rouletteMaxBet4;
        public Long rouletteMaxBet5;
        public Long rouletteMaxBet6;
        public Long rouletteMaxBet12;
        public Long rouletteMaxBet18;
    }
%>