<%@ page import="com.dgphoenix.casino.common.cache.BankInfoCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.BaseGameCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.CurrencyCache" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.Coin" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.GameVariableType" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo" %>
<%@ page import="com.abs.casino.common.util.StreamUtils" %>
<%@ page import="com.fasterxml.jackson.annotation.JsonInclude" %>
<%@ page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@ page import="com.google.common.base.Splitter" %>
<%@ page import="static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.ImmutableBaseGameInfoWrapper" %>
<%@ page import="com.abs.casino.gs.managers.game.settings.GameSettingsManager" %>
<%@ page import="com.dgphoenix.casino.common.util.ApplicationContextHelper" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.BaseGameConstants" %>
<%@ page import="com.dgphoenix.casino.support.tool.*" %>
<%@ page contentType="text/html;charset=UTF-8" %>

<%
    String value;
    Splitter splitter = Splitter.on("|").trimResults().omitEmptyStrings();

    List<Long> bankIds = null;
    value = request.getParameter("bankId");
    if (value != null) {
        bankIds = StreamUtils.asStream(splitter.split(value))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    List<String> currencies = null;
    value = request.getParameter("currencyCode");
    if (value != null) {
        currencies = splitter.splitToList(value);
    }

    Set<Long> gameIds = null;
    value = request.getParameter("gameId");
    if (value != null) {
        gameIds = StreamUtils.asStream(splitter.split(value))
                .map(Long::parseLong)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    List<String> groups = null;
    value = request.getParameter("group");
    if (value != null) {
        groups = splitter.splitToList(value.toLowerCase());
    }

    Set<String> properties = null;
    value = request.getParameter("prop");
    if (value != null) {
        properties = new HashSet<>(splitter.splitToList(value));
    }

    OutputFormat outputFormat = OutputFormat.HTML;
    value = request.getParameter("format");
    if (value != null) {
        outputFormat = OutputFormat.valueOf(value.toUpperCase());
    }

    if (bankIds != null) {
        List<BankDetails> bankDetailsList = new ArrayList<>();

        for (long bankId : bankIds) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            if (bankInfo != null) {
                BankDetails bankDetails = new BankDetails();
                if (bankInfo.getMasterBankId() != null && bankId != bankInfo.getMasterBankId()) {
                    bankDetails.setMasterBankId(bankInfo.getMasterBankId());
                }
                List<Currency> currencyList = collectCurrencyList(currencies, bankId);
                List<Currency> bankCurrencies = BankInfoCache.getInstance().getBankInfo(bankId).getCurrencies();

                for (Currency currency : currencyList) {
                    GameInfoDetails gameInfoDetails = new GameInfoDetails();
                    if (!bankCurrencies.contains(currency)) {
                        continue;
                    }
                    Set<Long> gameIdSet = collectGameIds(gameIds, bankInfo);

                    for (long gameId : gameIdSet) {
                        IBaseGameInfo bgi = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, currency);
                        if (groups != null && !groups.contains(bgi.getGroup().getGroupName().toLowerCase())) {
                            continue;
                        }
                        GameDetails gameDetails = new GameDetails();
                        if (bgi instanceof ImmutableBaseGameInfoWrapper) {
                            gameDetails.setFromMasterBank(true);
                        }
                        gameDetails.setGameName(bgi.getName());
                        gameDetails.setGameId(gameId);
                        gameDetails.setGroup(bgi.getGroup().getGroupName());
                        gameDetails.setVariableType(bgi.getVariableType());
                        bankDetails.setBankId(bankId);
                        bankDetails.setDescription(bankInfo.getExternalBankIdDescription());
                        gameInfoDetails.setCurrencyCode(currency.getCode());
                        if (!currency.equals(bgi.getCurrency())) {
                            gameDetails.setFromDefaultInfo(true);
                        }

                        if (bgi.getVariableType().equals(GameVariableType.COIN)) {
                            setCoins(currency, bgi, gameDetails);
                        } else {
                            gameDetails.setMinLimit(bgi.getLimit().getMinValue());
                            gameDetails.setMaxLimit(bgi.getLimit().getMaxValue());
                        }

                        if (properties != null) {
                            setProperties(bgi, properties, gameDetails);
                        }

                        gameInfoDetails.addGameDetails(gameDetails);
                    }
                    bankDetails.addGameInfoDetails(gameInfoDetails);
                }
                bankDetailsList.add(bankDetails);
            }
        }
        switch (outputFormat) {
            case HTML:
                response.getWriter().write(toHTML(bankDetailsList));
                break;
            case JSON:
                response.setContentType(APPLICATION_JSON_UTF8_VALUE);
                response.getWriter().write(toJson(bankDetailsList));
                break;
            default:
                throw new IllegalStateException("Unsupported output format");
        }
    }
%>
<%!
    private void setCoins(Currency currency, IBaseGameInfo bgi, GameDetails gameDetails) {
        GameSettingsManager gameSettingsManager = ApplicationContextHelper.getBean(GameSettingsManager.class);
        List<Coin> dynamicCoins = gameSettingsManager.getCoins(bgi, currency.getCode(), false);
        List<Long> coins = dynamicCoins.stream()
                .map(Coin::getValue)
                .sorted()
                .collect(Collectors.toList());
        gameDetails.setCoins(coins);

        Integer defaultCoin = gameSettingsManager.getDefaultCoin(bgi, currency.getCode(), dynamicCoins);
        if (defaultCoin != null) {
            gameDetails.setDefCoin(String.valueOf(defaultCoin));
        }
    }

    private void setProperties(IBaseGameInfo bgi, Set<String> properties, GameDetails gameDetails) {
        for (String prop : properties) {
            if (prop.equals(BaseGameConstants.KEY_FRB_COIN)) {
                GameSettingsManager gameSettingsManager = ApplicationContextHelper.getBean(GameSettingsManager.class);
                BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bgi.getBankId());
                String strFrbCoin = gameSettingsManager.getFRBCoin(bankInfo, bgi, bgi.getCurrency());
                gameDetails.addProperties(prop, strFrbCoin);
            } else if (bgi.getProperty(prop) != null) {
                if (prop.equals("ISENABLED")) {
                    gameDetails.addProperties(prop, String.valueOf(bgi.isEnabled()));
                } else {
                    gameDetails.addProperties(prop, bgi.getProperty(prop));
                }
                if (prop.equals(BaseGameConstants.KEY_CURRENT_MODEL) || prop.equals(BaseGameConstants.KEY_POSSIBLE_MODELS)) {
                    prop = "DEFAULT_RTP_MODEL";
                    gameDetails.addProperties(prop, String.valueOf(bgi.getDefaultRtp()));
                }
            }
        }
    }

    private String toJson(List<BankDetails> bankDetailsList) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(bankDetailsList);
    }

    private String toHTML(List<BankDetails> bankDetailsList) {
        StringBuilder htmlMsg = new StringBuilder();
        for (BankDetails bankDetails : bankDetailsList) {
            htmlMsg.append("-----------------------processing bank: ").append(bankDetails.getBankId())
                    .append(" name: ").append(bankDetails.getDescription()).append("<br>");

            for (GameInfoDetails gameInfoDetail : bankDetails.getGameInfoDetails()) {
                htmlMsg.append("<table>").append("games details bank=").append(bankDetails.getBankId()).append(", Currency=")
                        .append(gameInfoDetail.getCurrencyCode()).append(" found:").append("<br>");

                for (GameDetails gameDetails : gameInfoDetail.getGames()) {
                    if (gameDetails.getFromDefaultInfo()) {
                        htmlMsg.append("<tr><td>config from Default currency</td>");
                    }
                    gameDetails.setGameName(String.format("<a href=\"/support/loadgameinfo.do?bankId=%s&curCode=%s&gameId=%s\">%s/%s</a><style=\"word-wrap: break-word;\">",
                            bankDetails.getBankId(),
                            gameInfoDetail.getCurrencyCode(),
                            gameDetails.getGameId(),
                            gameDetails.getGameId(),
                            gameDetails.getGameName()));
                    if (gameDetails.getMaxLimit() == null) {
                        htmlMsg.append("<tr><td>").append(gameDetails.getGameName()).append("</td><td>")
                                .append("coins: ").append(gameDetails.getCoins()).append("</td><td>");
                        if (gameDetails.getDefCoin() != null) {
                            htmlMsg.append(" defCoin: ").append(gameDetails.getDefCoin());
                        }
                        htmlMsg.append("</td><td>");
                    } else {
                        htmlMsg.append("<tr><td>").append(gameDetails.getGameName()).append("</td><td>")
                                .append("limit: ").append(gameDetails.getMinLimit()).append("-")
                                .append(gameDetails.getMaxLimit()).append("</td><td></td><td>");
                    }
                    htmlMsg.append(gameDetails.getProperties()).append("</td></tr>");
                }
                htmlMsg.append("</table>").append("<br>");
            }
        }
        return htmlMsg.toString();
    }

    private Set<Long> collectGameIds(Set<Long> gameIds, BankInfo bankInfo) {
        if (gameIds != null) {
            Set<Long> availableGames = BaseGameCache.getInstance().getAllGamesSet(bankInfo.getId(), bankInfo.getDefaultCurrency());
            return gameIds.stream()
                    .filter(availableGames::contains)
                    .collect(Collectors.toSet());
        }
        return new TreeSet<>(BaseGameCache.getInstance().getAllGamesSet(bankInfo.getId(), bankInfo.getDefaultCurrency()));
    }

    private List<Currency> collectCurrencyList(List<String> currencies, long bankId) {
        if (currencies != null) {
            if (currencies.get(0).equalsIgnoreCase("all")) {
                return BankInfoCache.getInstance().getBankInfo(bankId).getCurrencies();
            } else {
                return currencies.stream()
                        .map(code -> CurrencyCache.getInstance().get(code))
                        .collect(Collectors.toList());
            }
        }
        return Collections.singletonList(BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency());
    }
%>
