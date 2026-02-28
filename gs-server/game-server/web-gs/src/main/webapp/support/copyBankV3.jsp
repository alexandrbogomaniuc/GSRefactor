<%@ page import="com.abs.casino.bgm.BaseGameHelper" %>
<%@ page import="com.abs.casino.common.cache.data.game.BaseGameConstants" %>
<%@ page import="com.dgphoenix.casino.common.cache.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.BankInfo" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.Coin" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.Limit" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.bank.SubCasino" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.currency.Currency" %>
<%@ page import="com.dgphoenix.casino.common.exception.CommonException" %>
<%@ page import="com.abs.casino.gs.persistance.remotecall.RemoteCallHelper" %>
<%@ page import="static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty" %>
<%@ page import="com.google.common.base.Function" %>
<%@ page import="com.google.common.base.Splitter" %>
<%@ page import="com.google.common.collect.Iterables" %>
<%@ page import="com.google.common.collect.Lists" %>
<%@ page import="java.util.*" %>
<%@ page import="com.dgphoenix.casino.common.cache.data.game.*" %>
<%--
gs1.sb.xxx.com/support/xpro/copyBankV2.jsp?source=979&target=1071&extId=1071&extDescr=www.mbitcasino.com&defcoin=2&targetCoins=2|5|10|25|50|100&targetCurrency=MBC&targetLimit=1

gs1-gp3.xxx.com/support/xpro/copyBankV2.jsp?source=1074&target=1081&extId=1081&extDescr=gr.sportingbet.com&targetCurrency=EUR
--%>
<%
    try {
        response.getWriter().write("0");
        Long sourceBankId;
        try {
            sourceBankId = Long.parseLong(request.getParameter("source"));
        } catch (NumberFormatException e) {
            throw new CommonException("Source bankId is not numeric", e);
        }
        response.getWriter().write("1");
        BankInfo sourceBank = BankInfoCache.getInstance().getBankInfo(sourceBankId);
        if (sourceBank == null) {
            throw new CommonException("Source BankInfo not found for id: " + sourceBankId);
        }
        response.getWriter().write("2");
        Long target;
        try {
            target = Long.parseLong(request.getParameter("target"));
        } catch (NumberFormatException e) {
            throw new CommonException("Target bankId is not numeric", e);
        }
        response.getWriter().write("3");
        if (BankInfoCache.getInstance().isExist(target)) {
            throw new CommonException("Target bank already exist for id: " + target);
        }
        String externalBankId = request.getParameter("extId");
        if (isTrimmedEmpty(externalBankId)) {
            externalBankId = String.valueOf(target);
        }
        response.getWriter().write("4");
        String extDescr = request.getParameter("extDescr");
        if (isTrimmedEmpty(extDescr)) {
            extDescr = "";
        }
        response.getWriter().write("5");
        String targetCoinsStr = request.getParameter("targetCoins");
        List<Coin> targetCoins;
        try {
            targetCoins = Lists.newArrayList(
                    Iterables.transform(Splitter.on("|").split(targetCoinsStr), new Function<String, Coin>() {
                        public Coin apply(String input) {
                            return CoinsCache.getInstance().getCoinByValue(Long.valueOf(input));
                        }
                    }));
        } catch (Exception e) {
            targetCoins = null;
        }
        response.getWriter().write("6");
        Long defCoin;
        String defcoin = request.getParameter("defcoin");
        try {
            defCoin = Long.parseLong(defcoin);
            if (defCoin < 0 || defCoin > targetCoins.size()) {
                throw new RuntimeException("defCoin < 0 || defCoin > coins.size(): defcoin: " + defcoin + ", coins size: " + targetCoins.size());
            }
        } catch (Exception e) {
            defCoin = null;
        }
        response.getWriter().write("7");
        String targetLimit = request.getParameter("targetLimit");
        Long limitId;
        Limit limit;
        try {
            limitId = Long.parseLong(targetLimit);
            limit = limitsCache.getLimit(limitId);
        } catch (Exception e) {
            limit = null;
        }
        response.getWriter().write("8");
        String curCode = request.getParameter("targetCurrency");
        if (isTrimmedEmpty(curCode)) {
            curCode = null;
        }
        boolean isSaveLimits = false;
        String saveLimits = request.getParameter("saveLimits");
        if (!isTrimmedEmpty(saveLimits) && "1".equals(saveLimits)) {
            isSaveLimits = true;
        }
        boolean isSaveCoins = false;
        String saveCoins = request.getParameter("saveCoins");
        if (!isTrimmedEmpty(saveCoins) && "1".equals(saveCoins)) {
            isSaveCoins = true;
        }
        Long subCasinoId = BankInfoCache.getInstance().getSubCasinoId(sourceBankId);
        Long newSubCasinoId = null;
        String sSubCasinoId = request.getParameter("subCasino");
        if (!isTrimmedEmpty(sSubCasinoId)) {
            try {
                newSubCasinoId = Long.parseLong(sSubCasinoId);
            } catch (Exception e) {
            }
        }
        if (newSubCasinoId != null) {
            subCasinoId = newSubCasinoId;
        }
        Bank bank = new Bank(target, externalBankId, extDescr,
                curCode == null ? sourceBank.getDefaultCurrency().getCode() : curCode,
                targetCoins == null ? sourceBank.getCoins() : targetCoins,
                defCoin == null ? null : defCoin,
                limit == null ? sourceBank.getLimit() : limit);
        response.getWriter().write("9");
        Currency newCurrency = CurrencyCache.getInstance().get(bank.getCurrency());
        BankInfo bankInfo = new BankInfo(bank.getBankId(), bank.getExternalBankId(), bank.getExternalBankDescription(), newCurrency, bank.getLimit(), bank.getCoins());
        BankInfoCache.getInstance().put(bankInfo);
        bankInfo.setSubCasinoId(subCasinoId);
        RemoteCallHelper.getInstance().saveAndSendNotification(bankInfo);
        BankInfo newBankInfo = BankInfoCache.getInstance().getBankInfo(bank.getBankId());
        SubCasinoCache scc = SubCasinoCache.getInstance();
        SubCasino casino = SubCasinoCache.getInstance().get(subCasinoId);
        scc.put(subCasinoId, newBankInfo.getId(), false);
        RemoteCallHelper.getInstance().saveAndSendNotification(casino);
        String minCoin = null;
        try {
            minCoin = Long.toString(getMinCoin(targetCoins).getValue());
        } catch (Exception e) {
            minCoin = null;
        }
        for (String property : sourceBank.getProperties().keySet()) {
            newBankInfo.setProperty(property, sourceBank.getStringProperty(property));
        }
        RemoteCallHelper.getInstance().saveAndSendNotification(newBankInfo);
        Map<Long, IBaseGameInfo> games = BaseGameCache.getInstance().getAllGameInfosAsMap(sourceBank.getId(), sourceBank.getDefaultCurrency());
        for (Map.Entry<Long, IBaseGameInfo> gameInfoEntry : games.entrySet()) {
            Long gameId = gameInfoEntry.getKey();
            IBaseGameInfo game = gameInfoEntry.getValue();
            // clone properties
            Map<String, String> cloneProp = new HashMap<>();
            cloneProp.putAll(game.getPropertiesMap());
            if (game.getVariableType() == GameVariableType.COIN) {
                Long bankDefCoin = bank.getDefCoin();
                cloneProp.put(BaseGameConstants.KEY_DEFAULT_COIN,
                        bankDefCoin == null ? String.valueOf(game.getDefaultCoin()) : String.valueOf(bankDefCoin));
            }
            if (frbGames.contains(game.getId())) {
                cloneProp.put(BaseGameConstants.KEY_FRB_COIN, minCoin == null ? game.getProperty(BaseGameConstants.KEY_FRB_COIN) : minCoin);
            }
            List<Coin> sourceCoins = game.getCoins();
            Double prcp = null;
            Double brcp = null;
            // create game
            BaseGameHelper.createGame(bank.getBankId(), game.getId(), newCurrency, game.getName(),
                    game.getGameType(), game.getGroup(), game.getVariableType(),
                    game.getRmClassName(), game.getGsClassName(),
                    cloneProp, isSaveLimits ? game.getLimit() : null,
                    game.getId() == 249l || game.getId() == 15l || isSaveCoins ? sourceCoins : null,
                    false, prcp, brcp);
            IBaseGameInfo info = BaseGameCache.getInstance().getGameInfoShared(bank.getBankId(), game.getId(), newCurrency);
            if (info != null) {
                info.setLanguages(game.getLanguages());
                RemoteCallHelper.getInstance().saveAndSendNotification(info);
            }
        }
    } catch (Exception e) {
        e.printStackTrace(response.getWriter());
    }

%>

<%!
    private class Bank {
        private Long bankId;
        private String externalBankId;
        private String externalBankDescription;
        private String currency;
        private List<Coin> coins;
        private Long defCoin;
        private Limit limit;

        private Bank(Long bankId, String externalBankId, String extDescr, String currency, List<Coin> coins,
                     Long defCoin, Limit limit) {
            this.bankId = bankId;
            this.externalBankId = externalBankId;
            externalBankDescription = extDescr;
            this.currency = currency;
            this.coins = coins;
            this.defCoin = defCoin;
            this.limit = limit;
        }

        public String getExternalBankDescription() {
            return externalBankDescription;
        }

        public Long getBankId() {
            return bankId;
        }

        public void setBankId(Long bankId) {
            this.bankId = bankId;
        }

        public String getExternalBankId() {
            return externalBankId;
        }

        public void setExternalBankId(String externalBankId) {
            this.externalBankId = externalBankId;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public List<Coin> getCoins() {
            return coins;
        }

        public void setCoins(List<Coin> coins) {
            this.coins = coins;
        }

        public Long getDefCoin() {
            return defCoin;
        }

        public void setDefCoin(Long defCoin) {
            this.defCoin = defCoin;
        }

        public Limit getLimit() {
            return limit;
        }

        public void setLimit(Limit limit) {
            this.limit = limit;
        }
    }%>

<%!
    private Set<Long> frbGames = BaseGameInfoTemplateCache.getInstance().getFrbGames();
    private LimitsCache limitsCache = LimitsCache.getInstance();
    CoinsCache cache = CoinsCache.getInstance();
    private Coin getMinCoin(List<Coin> coins) {
        Coin resultCoin = null;
        for (Coin coin : coins) {
            if (resultCoin == null) {
                resultCoin = coin;
                continue;
            }
            if (resultCoin.getValue() > coin.getValue()) {
                resultCoin = coin;
            }
        }
        return resultCoin;
    }
%>
