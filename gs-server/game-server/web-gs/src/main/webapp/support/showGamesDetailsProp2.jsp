<%@ page import="com.abs.casino.common.cache.BankInfoCache" %>
<%@ page import="com.abs.casino.common.cache.BaseGameCache" %>
<%@ page import="com.abs.casino.common.cache.CurrencyCache" %>
<%@ page import="com.abs.casino.common.cache.SubCasinoCache" %>
<%@ page import="com.abs.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.abs.casino.common.cache.data.bank.Coin" %>
<%@ page import="com.abs.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameInfo" %>
<%@ page import="com.abs.casino.common.cache.data.game.GameVariableType" %>
<%@ page import="java.util.*" %>
<%@ page import="com.abs.casino.common.cache.data.game.IBaseGameInfo" %>
<%
    String value;
    List<Long> bankIds = null;
    List<String> currencies = null;
    Set<Long> gameIds = null;
    List<String> typess = null;
    Set<String> prop = null;
    Set<String> nameContains = null;
    Set<String> nameNotContains = null;
    value = request.getParameter("bankId");
    if (value != null) {
        StringTokenizer banks = new StringTokenizer(value, ",.;:|-+");
        bankIds = new ArrayList<>();
        while (banks.hasMoreTokens()) {
            bankIds.add(Long.parseLong(banks.nextToken()));
        }
    }

    value = request.getParameter("currencyCode");
    if (value != null) {
        StringTokenizer codes = new StringTokenizer(value, ",.;:|-+");
        currencies = new ArrayList<>();
        while (codes.hasMoreTokens()) {
            currencies.add(codes.nextToken());
        }
    }

    value = request.getParameter("gameId");
    if (value != null) {
        StringTokenizer games = new StringTokenizer(value, ",.;:|-+");
        gameIds = new TreeSet<>();
        while (games.hasMoreTokens()) {
            gameIds.add(Long.parseLong(games.nextToken()));
        }
    }

    value = request.getParameter("group");
    if (value != null) {
        StringTokenizer types = new StringTokenizer(value, ",.;:|-+");
        typess = new ArrayList<>();
        while (types.hasMoreTokens()) {
            typess.add(types.nextToken().toLowerCase());
        }
    }

    value = request.getParameter("prop");
    if (value != null) {
        StringTokenizer types = new StringTokenizer(value, ",.;:|-+");
        prop = new HashSet<>();
        while (types.hasMoreTokens()) {
            prop.add(types.nextToken());
        }
    }

    value = request.getParameter("nameContains");
    if (value != null) {
        StringTokenizer types = new StringTokenizer(value, ",.;:|-+");
        nameContains = new HashSet<>();
        while (types.hasMoreTokens()) {
            nameContains.add(types.nextToken());
        }
    }

    value = request.getParameter("nameNotContains");
    if (value != null) {
        StringTokenizer types = new StringTokenizer(value, ",.;:|-+");
        nameNotContains = new HashSet<>();
        while (types.hasMoreTokens()) {
            nameNotContains.add(types.nextToken());
        }
    }

    value = request.getParameter("subCasinoId");
    if (value != null) {
        StringTokenizer types = new StringTokenizer(value, ",.;:|-+");
        if (bankIds == null) {
            bankIds = new ArrayList<>();
        }
        while (types.hasMoreTokens()) {
            bankIds.addAll(SubCasinoCache.getInstance().getBankIds(Long.parseLong(types.nextToken())));
        }
    }


    if (bankIds != null) {
        for (long bankId : bankIds) {
            BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
            response.getWriter().write("<br><br>-----------------------processing bank: " + bankId + " name: " + bankInfo.getExternalBankIdDescription() + "<br>");

            Currency defaultCurrency = bankInfo.getDefaultCurrency();
            List<Currency> currencyList = new ArrayList<>();
            if (currencies != null) {
                if (currencies.get(0).toLowerCase().equals("all")) {
                    currencyList.addAll(BankInfoCache.getInstance().getBankInfo(bankId).getCurrencies());
                } else {
                    for (String code : currencies) {
                        currencyList.add(CurrencyCache.getInstance().get(code));
                    }
                }
            } else {
                currencyList.add(BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency());
            }

            List<Currency> curr = BankInfoCache.getInstance().getBankInfo(bankId).getCurrencies();
            for (Currency currency_ : currencyList) {
                String isFound = " not found";
                if (curr.contains(currency_)) {
                    isFound = " found";
                }
                response.getWriter().print("games details bank=" + bankId + ", Currency=" + currency_.getCode() + isFound + ": <br>");
                response.getWriter().print("<table>");
                Set<Long> gamesIdss = new TreeSet<>();
                if (gameIds != null) {
                    for (Long gameId : gameIds) {
                        if (BaseGameCache.getInstance().getAllGamesSet(bankId, defaultCurrency).contains(gameId)) {
                            gamesIdss.add(gameId);
                        }
                    }
                } else {
                    gamesIdss = new TreeSet<>(BaseGameCache.getInstance().getAllGamesSet(bankId, bankInfo.getDefaultCurrency()));
                }

                if (isFound.equals(" not found"))
                    continue;

                for (long gameId : gamesIdss) {
                    IBaseGameInfo bgi = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, currency_);
                    String gameName = bgi.getName();
                    boolean flag = false;
                    if (nameContains != null && !nameContains.isEmpty()) {
                        for (String s : nameContains) {
                            flag |= gameName.contains(s);
                        }
                        if (!flag) {
                            continue;
                        }
                    }
                    if (nameNotContains != null && !nameNotContains.isEmpty()) {
                        flag = false;
                        for (String s : nameNotContains) {
                            flag |= !gameName.contains(s);
                        }
                        if (!flag) {
                            continue;
                        }
                    }

                    //DMIFacade.getInstance().initializeLocalGameInfo(bankId, gameId, currency_);
                    boolean fromDefaultInfo = false;

                    if (!currency_.equals(bgi.getCurrency())) {
//                        bgi = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, defaultCurrency);
//                        response.getWriter().write(" ---------------------------------------------------  currency_: " + currency_ + " bgi: " + bgi);
                        fromDefaultInfo = true;
                    }
                    if (bgi != null) {
                        if (typess != null && !typess.contains(bgi.getGroup().getGroupName().toLowerCase()))
                            continue;
                        String coinsByArray = "";
                        String defCoin = "not used ";
                        String frbCoin = "not used ";

                        if (bgi.getVariableType().equals(GameVariableType.COIN)) {
                            List<Coin> vjc = bgi.getCoins();

                            long[] coins = new long[vjc.size()];
                            for (int i = 0; i < vjc.size(); i++) {
                                coins[i] = vjc.get(i).getValue();
                            }
                            for (int i = 0; i < coins.length - 1; i++) {
                                for (int j = 0; j < coins.length - 1; j++) {
                                    if (coins[j] > coins[j + 1]) {
                                        long buf = coins[j];
                                        coins[j] = coins[j + 1];
                                        coins[j + 1] = buf;
                                    }

                                }
                            }
                            for (long coin : coins) {
                                coinsByArray += coin + ", ";
                            }
                            if (bgi.getDefaultCoin() != null) {
                                defCoin = String.valueOf(bgi.getDefaultCoin());
                            }

                            if (bgi.getProperty("FRB_COIN") != null) {
                                frbCoin = String.valueOf(bgi.getProperty("FRB_COIN"));
                            }

                        }
                        response.getWriter().write(
                                String.format("<tr><td><a href=\"/support/loadgameinfo.do?bankId=%s&curCode=%s&gameId=%s\">%s/%s</a></t><td>",
                                        bankId,
                                        currency_.getCode(),
                                        gameId,
                                        gameId,
                                        bgi.getName()
                                )/*"" + gameId + "/" + bgi.getName() + "" */ +
                                        (bgi.getVariableType().equals(
                                                GameVariableType.COIN) ? " coins: " + coinsByArray + " frbCoin: "
                                                + frbCoin + ", defCoin: " + defCoin : " limit: " +
                                                bgi.getLimit().getMinValue() + "-" + bgi.getLimit().getMaxValue())
                        );


                        if (prop != null)
                            for (String s : prop) {
                                if (bgi.getProperty(s) != null) {
                                    response.getWriter().write("</br>     " + s + " : " + bgi.getProperty(s));/* + "<br>")*/
                                    ;
                                }

                            }

                        if (fromDefaultInfo) {
                            response.getWriter().write(" config from Default currency  ");
                        }

                        response.getWriter().write("</t></tr>");
                    }
                }
                response.getWriter().write("</table>" + "<br>");
            }
        }
    }
%>
