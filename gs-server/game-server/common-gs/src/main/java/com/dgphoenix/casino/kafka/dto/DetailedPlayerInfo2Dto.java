package com.abs.casino.kafka.dto;

import java.util.List;
import java.util.Map;

import com.dgphoenix.casino.kafka.dto.BasicKafkaResponse;
import com.abs.casino.kafka.dto.BattlegroundInfoDto;
import com.abs.casino.kafka.dto.CashBonusDto;
import com.abs.casino.kafka.dto.FRBonusDto;
import com.abs.casino.kafka.dto.TournamentInfoDto;

public class DetailedPlayerInfo2Dto extends BasicKafkaResponse {
    private long bankId; 
    private long accountId; 
    private String externalId; 
    private String userName; 
    private long balance; 
    private String currency; 
    private String currencySymbol; 
    private double currencyRateForEUR; 
    private boolean guest; 
    private boolean showRefreshBalanceButton; 
    private FRBonusDto activeFrb; 
    private List<Long> stakes; 
    private Map<String, String> gameSettings; 
    private double lbContribution; 
    private CashBonusDto cashBonus; 
    private TournamentInfoDto tournamentInfo; 
    private List<BattlegroundInfoDto> battlegrounds; 

    public DetailedPlayerInfo2Dto() { }

    public DetailedPlayerInfo2Dto(boolean success,
                                  int errorCode,
                                  String errorDetails) {
        super(success, errorCode, errorDetails);
    }

    public DetailedPlayerInfo2Dto(long bankId,
            long accountId,
            String externalId,
            String userName,
            long balance,
            String currency,
            String currencySymbol,
            double currencyRateForEUR,
            boolean guest,
            boolean showRefreshBalanceButton,
            FRBonusDto activeFrb,
            List<Long> stakes,
            Map<String, String> gameSettings,
            boolean success,
            int errorCode,
            String errorDetails,
            double lbContribution,
            CashBonusDto cashBonus,
            TournamentInfoDto tournamentInfo,
            List<BattlegroundInfoDto> battlegrounds) {
        super(success, errorCode, errorDetails);
        this.bankId = bankId;
        this.accountId = accountId;
        this.externalId = externalId;
        this.userName = userName;
        this.balance = balance;
        this.currency = currency;
        this.currencySymbol = currencySymbol;
        this.currencyRateForEUR = currencyRateForEUR;
        this.guest = guest;
        this.showRefreshBalanceButton = showRefreshBalanceButton;
        this.activeFrb = activeFrb;
        this.stakes = stakes;
        this.gameSettings = gameSettings;
        this.lbContribution = lbContribution;
        this.cashBonus = cashBonus;
        this.tournamentInfo = tournamentInfo;
        this.battlegrounds = battlegrounds;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public double getCurrencyRateForEUR() {
        return currencyRateForEUR;
    }

    public void setCurrencyRateForEUR(double currencyRateForEUR) {
        this.currencyRateForEUR = currencyRateForEUR;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public boolean isShowRefreshBalanceButton() {
        return showRefreshBalanceButton;
    }

    public void setShowRefreshBalanceButton(boolean showRefreshBalanceButton) {
        this.showRefreshBalanceButton = showRefreshBalanceButton;
    }

    public FRBonusDto getActiveFrb() {
        return activeFrb;
    }

    public void setActiveFrb(FRBonusDto activeFrb) {
        this.activeFrb = activeFrb;
    }

    public List<Long> getStakes() {
        return stakes;
    }

    public void setStakes(List<Long> stakes) {
        this.stakes = stakes;
    }

    public Map<String, String> getGameSettings() {
        return gameSettings;
    }

    public void setGameSettings(Map<String, String> gameSettings) {
        this.gameSettings = gameSettings;
    }

    public double getLbContribution() {
        return lbContribution;
    }

    public void setLbContribution(double lbContribution) {
        this.lbContribution = lbContribution;
    }

    public CashBonusDto getCashBonus() {
        return cashBonus;
    }

    public void setCashBonus(CashBonusDto cashBonus) {
        this.cashBonus = cashBonus;
    }

    public TournamentInfoDto getTournamentInfo() {
        return tournamentInfo;
    }

    public void setTournamentInfo(TournamentInfoDto tournamentInfo) {
        this.tournamentInfo = tournamentInfo;
    }

    public List<BattlegroundInfoDto> getBattlegrounds() {
        return battlegrounds;
    }

    public void setBattlegrounds(List<BattlegroundInfoDto> battlegrounds) {
        this.battlegrounds = battlegrounds;
    }
}
