package com.abs.casino.common.cache.data.session;

import com.abs.casino.common.cache.data.bank.Coin;
import com.abs.casino.common.cache.data.bet.PlayerBet;
import com.abs.casino.common.cache.data.bonus.BonusStatus;
import com.abs.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.session.IGameSession;
import com.abs.casino.gamecombos.ComboFeature;
import com.abs.casino.gamecombos.GameSessionCommonStatistics;
import com.abs.casino.unj.api.ContributionResult;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.*;

public class GameSession implements IGameSession, KryoSerializable {
    private static final byte VERSION = 11;
    private long id;
    private long accountId;
    private long bankId;
    private long gameId;
    private long startTime;
    private Long endTime;
    private long income;
    private long payout;
    private long negativeBet;
    private int betsCount;
    private int roundsCount;
    private long lastPlayerBetId;
    private boolean createNewBet = true;
    private boolean realMoney;
    private double pcrSum;
    private double bcrSum;
    private Currency currency;
    private String currencyFraction;

    private Long bonusId;
    private Long frbonusId;
    private BonusStatus bonusStatus;
    private BonusStatus frbonusStatus;
    private String externalSessionId;

    private long startBalance;
    private long startBonusBalance;
    private long endBonusBalance;

    private String lang;
    private ClientType clientType;
    private boolean persistBets = true;

    private long bonusBet;
    private long bonusWin;

    private Long unjId;
    private double unjSummaryContribution;
    private long unjSummaryWin;

    private long enterDate = -1;
    //typically is walletOperationId;
    private Long lastPaymentOperationId;

    private String profileId;

    private HashMap<Long, Double> contributionsJP = new HashMap<>();
    private String inGameDisplayedMessage;
    private List<Long> promoCampaignIds;
    private Long tournamentId;

    private int dblUpRoundsCount;
    private long dblUpIncome;
    private long dblUpPayout;

    private Double model;

    private List<Long> cachedCoins;
    private GameSessionCommonStatistics gameSessionCommonStatistics;

    public GameSession() {
    }

    public GameSession(long id, long accountId, long bankId, long gameId, long startTime, long income, long payout,
                       int betsCount, int roundsCount, boolean createNewBet, boolean realMoney, Currency currency,
                       String externalSessionId, String lang, boolean persistBets, String profileId) {
        this.id = id;
        this.accountId = accountId;
        this.bankId = bankId;
        this.gameId = gameId;
        this.startTime = startTime;
        this.income = income;
        this.payout = payout;
        this.betsCount = betsCount;
        this.roundsCount = roundsCount;
        this.createNewBet = createNewBet;
        this.realMoney = realMoney;
        this.currency = currency;
        this.externalSessionId = externalSessionId;
        this.lang = lang;
        this.persistBets = persistBets;
        this.profileId = profileId;
    }

    public GameSession(long id, long accountId, long bankId, long gameId,
                       long startTime, Long endTime, long income, long payout, int betsCount, int roundsCount,
                       boolean createNewBet, boolean realMoney, Currency currency,
                       String externalSessionId, String lang, Long bonusId, Long frbonusId, long bonusBet, long bonusWin,
                       double unjSumContribution) {
        this.id = id;
        this.accountId = accountId;
        this.bankId = bankId;
        this.gameId = gameId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.income = income;
        this.payout = payout;
        this.betsCount = betsCount;
        this.roundsCount = roundsCount;
        this.createNewBet = createNewBet;
        this.realMoney = realMoney;
        this.currency = currency;
        this.externalSessionId = externalSessionId;
        this.lang = lang;
        this.bonusId = bonusId;
        this.frbonusId = frbonusId;
        this.bonusBet = bonusBet;
        this.bonusWin = bonusWin;
        this.unjSummaryContribution = unjSumContribution;
    }

    public GameSession copy() {
        GameSession gameSession = new GameSession(id, accountId, bankId, gameId, startTime, endTime, income, payout,
                betsCount, roundsCount, false, realMoney, currency, externalSessionId, lang, bonusId, frbonusId,
                bonusBet, bonusWin, unjSummaryContribution);
        gameSession.setNegativeBet(negativeBet);
        gameSession.setLastPlayerBetId(lastPlayerBetId);
        gameSession.setPcrSum(pcrSum);
        gameSession.setBcrSum(bcrSum);
        gameSession.setBonusStatus(bonusStatus);
        gameSession.setFrbonusStatus(frbonusStatus);
        gameSession.setStartBalance(startBalance);
        gameSession.setStartBonusBalance(startBonusBalance);
        gameSession.setEndBonusBalance(endBonusBalance);
        gameSession.setClientType(clientType);
        gameSession.setUnjId(unjId);
        gameSession.setModel(model);
        return gameSession;
    }

    public boolean isBonusGameSession() {
        return bonusId != null && bonusId != -1;
    }

    public BonusStatus getBonusStatus() {
        return bonusStatus;
    }

    public Long getBonusId() {
        return bonusId;
    }

    public boolean isFRBonusGameSession() {
        return frbonusId != null && frbonusId != -1;
    }

    public Long getFrbonusId() {
        return frbonusId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getAccountId() {
        return accountId;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    @Override
    public long getGameId() {
        return gameId;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public Long getEndTime() {
        return endTime;
    }

    public void setBonusId(Long bonusId) {
        this.bonusId = bonusId;
    }

    public void setBonusStatus(BonusStatus bonusStatus) {
        this.bonusStatus = bonusStatus;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public long getIncome() {
        return income;
    }

    public void setIncome(long income) {
        this.income = income;
    }

    public long getNegativeBet() {
        return negativeBet;
    }

    public void setLastPlayerBetId(long lastPlayerBetId) {
        this.lastPlayerBetId = lastPlayerBetId;
    }

    public void setNegativeBet(long negativeBet) {
        this.negativeBet = negativeBet;
    }

    public void updateBonusBetAndWin(long bonusBetChange, long bonusWinChange) {
        this.bonusBet += bonusBetChange;
        this.bonusWin += bonusWinChange;
    }

    public void update(long bet, long win, double pcrChange, double bcrChange,
                       Long unjId, Map<Long, ContributionResult> unjLastContributions, Long lastPaymentOperationId) {
        if (bet != 0) {
            incrementIncome(bet);
            if (bet > 0) {
                incrementBetsCount(1);
            } else {
                incrementNegativeBet(Math.abs(bet));
            }
        }

        if (win > 0) {
            incrementPayout(win);
        }

        if (pcrChange > 0) {
            incrementPCRSum(pcrChange);
        }

        if (bcrChange > 0) {
            incrementBCRSum(bcrChange);
        }
        if (this.unjId == null && unjId != null) {
            this.unjId = unjId;
        }
        incrementUnjSummaryContributions(unjLastContributions);
        this.lastPaymentOperationId = lastPaymentOperationId;
    }

    public void addBet(long bet) {
        betsCount++;
        income += bet;
    }

    public void addWin(long win, long returnedBet) {
        income -= returnedBet;
        payout += win;
    }

    private void incrementNegativeBet(long bet) {
        this.negativeBet += bet;
    }

    private void incrementIncome(long inc) {
        this.income = this.income + inc;
    }

    private void incrementPCRSum(double inc) {
        this.pcrSum = this.pcrSum + inc;
    }

    private void incrementBCRSum(double inc) {
        this.bcrSum = this.bcrSum + inc;
    }

    private void incrementPayout(long inc) {
        this.payout = this.payout + inc;
    }

    public void incrementBetsCount(int inc) {
        this.betsCount += inc;
    }

    public void incrementRoundsCount(long inc) {
        this.roundsCount += inc;
    }

    @Override
    public long getPayout() {
        return payout;
    }

    public void setPayout(long payout) {
        this.payout = payout;
    }

    @Override
    public long getBetsCount() {
        return betsCount;
    }

    @Override
    public long getRoundsCount() {
        return roundsCount;
    }

    public long getLastPlayerBetId() {
        return lastPlayerBetId;
    }

    public void incrementLastPlayerBetId() {
        lastPlayerBetId++;
    }

    //use addPlayerBet, getPlayerBets,getCurrentBet, finish from PlayerBetPersistenceManager
    public void finish(long endTime, PlayerBet bet) {
        if (bet != null) {
            lastPlayerBetId++;
        }
        this.endTime = endTime;
    }

    @Override
    public boolean isCreateNewBet() {
        return createNewBet;
    }

    public void setCreateNewBet(boolean createNewBet, boolean incrementLastPlayerBetId) {
        this.createNewBet = createNewBet;
        if (incrementLastPlayerBetId) {
            lastPlayerBetId++;
        }
    }

    @Override
    public boolean isRealMoney() {
        return realMoney;
    }

    public double getPcrSum() {
        return pcrSum;
    }

    public void setPcrSum(double pcrSum) {
        this.pcrSum = pcrSum;
    }

    public double getBcrSum() {
        return bcrSum;
    }

    public void setBcrSum(double bcrSum) {
        this.bcrSum = bcrSum;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getExternalSessionId() {
        return externalSessionId;
    }

    public void setExternalSessionId(String externalSessionId) {
        this.externalSessionId = externalSessionId;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setFrbonusId(Long frbonusId) {
        this.frbonusId = frbonusId;
    }


    public BonusStatus getFrbonusStatus() {
        return frbonusStatus;
    }

    public void setFrbonusStatus(BonusStatus frbonusStatus) {
        this.frbonusStatus = frbonusStatus;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public boolean isPersistBets() {
        return persistBets;
    }

    public void setPersistBets(boolean persistBets) {
        this.persistBets = persistBets;
    }

    public long getStartBalance() {
        return startBalance;
    }

    public void setStartBalance(long startBalance) {
        this.startBalance = startBalance;
    }

    public long getStartBonusBalance() {
        return startBonusBalance;
    }

    public void setStartBonusBalance(long startBonusBalance) {
        this.startBonusBalance = startBonusBalance;
    }

    public long getEndBonusBalance() {
        return endBonusBalance;
    }

    public void setEndBonusBalance(long endBonusBalance) {
        this.endBonusBalance = endBonusBalance;
    }

    public long getBonusBet() {
        return bonusBet;
    }

    public void setBonusBet(long bonusBet) {
        this.bonusBet = bonusBet;
    }

    public long getBonusWin() {
        return bonusWin;
    }

    public void setBonusWin(long bonusWin) {
        this.bonusWin = bonusWin;
    }

    public Long getUnjId() {
        return unjId;
    }

    public void setUnjId(Long unjId) {
        this.unjId = unjId;
    }

    public double getUnjSummaryContribution() {
        return unjSummaryContribution;
    }

    public void setUnjSummaryContribution(double unjSummaryContribution) {
        this.unjSummaryContribution = unjSummaryContribution;
    }

    public void incrementUnjSummaryContributions(Map<Long, ContributionResult> unjLastContributions) {
        if (unjLastContributions != null) {
            for (Map.Entry<Long, ContributionResult> entry : unjLastContributions.entrySet()) {
                Double contribution = entry.getValue().getSummaryContribution();
                if (contribution > 0) {
                    this.unjSummaryContribution += entry.getValue().getSummaryContributionInEUR();
                    Long unjId = entry.getKey();
                    Double existContrib = contributionsJP.get(unjId);
                    if (existContrib != null) {
                        contribution = contribution + existContrib;
                    }
                    contributionsJP.put(unjId, contribution);
                }
            }
        }
    }

    public long getUnjSummaryWin() {
        return unjSummaryWin;
    }

    public void incrementUnjSummaryWin(long unjWin) {
        unjSummaryWin += unjWin;
    }

    public long getEnterDate() {
        return enterDate;
    }

    public void setEnterDate(long enterDate) {
        this.enterDate = enterDate;
    }

    public boolean isGameEntered() {
        return enterDate > 0;
    }

    public Long getLastPaymentOperationId() {
        return lastPaymentOperationId;
    }

    public void setLastPaymentOperationId(Long lastPaymentOperationId) {
        this.lastPaymentOperationId = lastPaymentOperationId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public void updateContributions(long betId, double pcrChange, double bcrChange) {
        updateJPContributions(betId, pcrChange);
        incrementPCRSum(pcrChange);
        incrementBCRSum(bcrChange);
    }

    public void updateJPContributions(long betId, double pcrChange) {
        if (pcrChange > 0) {
            //LOG.debug("Contribution_ is "+ bcrChange+ " bet = "+betId);
            if (contributionsJP == null) {
                contributionsJP = new HashMap<>();
            }
            Double existContribution = contributionsJP.get(betId);
            if (existContribution != null) {
                contributionsJP.put(betId, existContribution + pcrChange);
            } else {
                contributionsJP.put(betId, pcrChange);
            }
        }
    }

    public HashMap<Long, Double> getContributionsJP() {
        return contributionsJP;
    }

    public void setContributionsJP(HashMap<Long, Double> contributionsJP) {
        this.contributionsJP = contributionsJP;
    }

    public String getInGameDisplayedMessage() {
        return inGameDisplayedMessage;
    }

    public void setInGameDisplayedMessage(String inGameDisplayedMessage) {
        this.inGameDisplayedMessage = inGameDisplayedMessage;
    }

    public List<Long> getPromoCampaignIds() {
        return promoCampaignIds;
    }

    public boolean hasPromoCampaign() {
        return promoCampaignIds != null && !promoCampaignIds.isEmpty();
    }

    public void setPromoCampaignIds(List<Long> promoCampaignIds) {
        this.promoCampaignIds = promoCampaignIds;
    }

    public String getCurrencyFraction() {
        return currencyFraction;
    }

    public void setCurrencyFraction(String currencyFraction) {
        this.currencyFraction = currencyFraction;
    }

    public int getDblUpRoundsCount() {
        return dblUpRoundsCount;
    }

    public void setDblUpRoundsCount(int dblUpRoundsCount) {
        this.dblUpRoundsCount = dblUpRoundsCount;
    }

    public long getDblUpIncome() {
        return dblUpIncome;
    }

    public void setDblUpIncome(long dblUpIncome) {
        this.dblUpIncome = dblUpIncome;
    }

    public long getDblUpPayout() {
        return dblUpPayout;
    }

    public void setDblUpPayout(long dblUpPayout) {
        this.dblUpPayout = dblUpPayout;
    }

    public void setModel(Double model) {
        this.model = model;
    }

    public Double getModel() {
        return model;
    }

    public List<Coin> getCachedDynamicCoins() {
        if (cachedCoins == null || cachedCoins.isEmpty()) {
            return null;
        }
        List<Coin> resultCoins = new ArrayList<>();
        for (Long coinValue : cachedCoins) {
            resultCoins.add(Coin.getByValue(coinValue));
        }
        return resultCoins;
    }

    public void cacheDynamicCoins(List<Coin> coins) {
        this.cachedCoins = new ArrayList<>();
        for (Coin coin : coins) {
            cachedCoins.add(coin.getValue());
        }
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(Long tournamentId) {
        this.tournamentId = tournamentId;
    }

    public GameSessionCommonStatistics getGameSessionCommonStatistics() {
        return gameSessionCommonStatistics;
    }

    public void updateGameSessionCommonStatistics(String sdKey, Collection<ComboFeature> comboFeatureStats) {
        if (gameSessionCommonStatistics == null) {
            gameSessionCommonStatistics = new GameSessionCommonStatistics(sdKey);
        }
        for (ComboFeature comboFeature : comboFeatureStats) {
            gameSessionCommonStatistics.incrementComboFeatureStatistics(comboFeature, 1);
        }
    }

    public void setGameSessionCommonStatistics(GameSessionCommonStatistics gameSessionCommonStatistics) {
        this.gameSessionCommonStatistics = gameSessionCommonStatistics;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GameSession");
        sb.append("[id=").append(id);
        sb.append(", accountId=").append(accountId);
        sb.append(", bankId=").append(bankId);
        sb.append(", gameId=").append(gameId);
        sb.append(", startTime=").append(new Date(startTime));
        sb.append(", endTime=").append(endTime == null ? "null" : new Date(endTime));
        sb.append(", income=").append(income);
        sb.append(", payout=").append(payout);
        sb.append(", negativeBet=").append(negativeBet);
        sb.append(", betsCount=").append(betsCount);
        sb.append(", roundsCount=").append(roundsCount);
        sb.append(", createNewBet=").append(createNewBet);
        sb.append(", realMoney=").append(realMoney);
        sb.append(", pcrSum=").append(pcrSum);
        sb.append(", bcrSum=").append(bcrSum);
        sb.append(", currency=");
        sb.append(getCurrency() == null ? "null" : getCurrency().getCode());
        sb.append(", currencyFraction=").append(currencyFraction);
        sb.append(", bonusId=").append(bonusId);
        sb.append(", frBonusId=").append(frbonusId);
        sb.append(", bonusStatus=").append(bonusStatus);
        sb.append(", lang=").append(lang);
        sb.append(", clientType=").append(clientType);
        sb.append(", startBalance=").append(startBalance);
        sb.append(", startBonusBalance=").append(startBonusBalance);
        sb.append(", endBonusBalance=").append(endBonusBalance);
        sb.append(", bonusBet=").append(bonusBet);
        sb.append(", bonusWin=").append(bonusWin);
        sb.append(", unjId=").append(unjId);
        sb.append(", unjSummaryContribution=").append(unjSummaryContribution);
        sb.append(", unjSummaryWin=").append(unjSummaryWin);
        sb.append(", persistBets=").append(persistBets);
        sb.append(", lastPaymentOperationId=").append(lastPaymentOperationId);
        sb.append(", enterDate=").append(enterDate);
        sb.append(", contributionsJP=");
        sb.append(contributionsJP == null ? "null" : contributionsJP.toString());
        sb.append(", profileId=").append(profileId);
        sb.append(", inGameDisplayedMessage=").append(inGameDisplayedMessage);
        sb.append(", externalSessionId=").append(externalSessionId);
        sb.append(", promoCampaignIds=").append(promoCampaignIds);
        sb.append(", dblUpRoundsCount=").append(dblUpRoundsCount);
        sb.append(", dblUpIncome=").append(dblUpIncome);
        sb.append(", dblUpPayout=").append(dblUpPayout);
        sb.append(", model=").append(model);
        sb.append(", tournamentId=").append(tournamentId);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(accountId, true);
        output.writeLong(bankId, true);
        output.writeLong(gameId, true);
        output.writeLong(startTime, true);
        kryo.writeObjectOrNull(output, endTime, Long.class);
        output.writeLong(income);
        output.writeLong(payout);
        output.writeLong(negativeBet);
        output.writeInt(betsCount);
        output.writeInt(roundsCount);
        output.writeLong(lastPlayerBetId);
        output.writeBoolean(createNewBet);
        output.writeBoolean(realMoney);
        output.writeDouble(pcrSum);
        output.writeDouble(bcrSum);
        kryo.writeObjectOrNull(output, currency, Currency.class);
        kryo.writeObjectOrNull(output, bonusId, Long.class);
        kryo.writeObjectOrNull(output, frbonusId, Long.class);
        kryo.writeObjectOrNull(output, bonusStatus, BonusStatus.class);
        kryo.writeObjectOrNull(output, frbonusStatus, BonusStatus.class);
        output.writeString(externalSessionId);
        output.writeLong(startBalance, true);
        output.writeLong(startBonusBalance, true);
        output.writeLong(endBonusBalance, true);
        output.writeString(lang);
        kryo.writeObjectOrNull(output, clientType, ClientType.class);
        output.writeBoolean(persistBets);
        output.writeLong(bonusBet, true);
        output.writeLong(bonusWin, true);
        kryo.writeObjectOrNull(output, unjId, Long.class);
        output.writeDouble(unjSummaryContribution);
        kryo.writeObjectOrNull(output, lastPaymentOperationId, Long.class);
        output.writeLong(enterDate);
        kryo.writeObjectOrNull(output, contributionsJP, HashMap.class);
        output.writeString(profileId);
        output.writeString(inGameDisplayedMessage);
        kryo.writeObjectOrNull(output, promoCampaignIds, ArrayList.class);
        output.writeString(currencyFraction);
        output.writeInt(dblUpRoundsCount, true);
        output.writeLong(dblUpIncome, true);
        output.writeLong(dblUpPayout, true);
        output.writeLong(unjSummaryWin, true);
        kryo.writeObjectOrNull(output, model, Double.class);
        kryo.writeObjectOrNull(output, cachedCoins, ArrayList.class);
        kryo.writeObjectOrNull(output, tournamentId, Long.class);
        kryo.writeObjectOrNull(output, gameSessionCommonStatistics, GameSessionCommonStatistics.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong(true);
        accountId = input.readLong(true);
        bankId = input.readLong(true);
        gameId = input.readLong(true);
        startTime = input.readLong(true);
        endTime = kryo.readObjectOrNull(input, Long.class);
        income = input.readLong();
        payout = input.readLong();
        negativeBet = input.readLong();
        betsCount = input.readInt();
        roundsCount = input.readInt();
        lastPlayerBetId = input.readLong();
        createNewBet = input.readBoolean();
        realMoney = input.readBoolean();
        pcrSum = input.readDouble();
        bcrSum = input.readDouble();
        currency = kryo.readObjectOrNull(input, Currency.class, Currency.SERIALIZER);
        bonusId = kryo.readObjectOrNull(input, Long.class);
        frbonusId = kryo.readObjectOrNull(input, Long.class);
        bonusStatus = kryo.readObjectOrNull(input, BonusStatus.class);
        frbonusStatus = kryo.readObjectOrNull(input, BonusStatus.class);
        externalSessionId = input.readString();
        startBalance = input.readLong(true);
        startBonusBalance = input.readLong(true);
        endBonusBalance = input.readLong(true);
        lang = input.readString();
        clientType = kryo.readObjectOrNull(input, ClientType.class);
        persistBets = input.readBoolean();
        bonusBet = input.readLong(true);
        bonusWin = input.readLong(true);
        unjId = kryo.readObjectOrNull(input, Long.class);
        unjSummaryContribution = input.readDouble();
        lastPaymentOperationId = kryo.readObjectOrNull(input, Long.class);
        enterDate = input.readLong();
        if (ver > 0) {
            contributionsJP = kryo.readObjectOrNull(input, HashMap.class);
        }
        if (ver > 1) {
            profileId = input.readString();
        }
        if (ver > 2) {
            inGameDisplayedMessage = input.readString();
        }
        if (ver > 3) {
            promoCampaignIds = kryo.readObjectOrNull(input, ArrayList.class);
        }
        if (ver > 4) {
            currencyFraction = input.readString();
        }
        if (ver > 5) {
            dblUpRoundsCount = input.readInt(true);
            dblUpIncome = input.readLong(true);
            dblUpPayout = input.readLong(true);
        }
        if (ver > 6) {
            unjSummaryWin = input.readLong(true);
        }
        if (ver > 7) {
            model = kryo.readObjectOrNull(input, Double.class);
        }
        if (ver > 8) {
            cachedCoins = kryo.readObjectOrNull(input, ArrayList.class);
        }
        if (ver > 9) {
            tournamentId = kryo.readObjectOrNull(input, Long.class);
        }
        if (ver > 10) {
            gameSessionCommonStatistics = kryo.readObjectOrNull(input, GameSessionCommonStatistics.class);
        }
    }
}
