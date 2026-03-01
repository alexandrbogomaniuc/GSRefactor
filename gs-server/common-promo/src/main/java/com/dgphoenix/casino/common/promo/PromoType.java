package com.abs.casino.common.promo;

/**
 * Created by vladislav on 3/21/17.
 */
public enum PromoType {
    TOURNAMENT("Tournament", true, false),
    MAX_BALANCE_TOURNAMENT("MaxBalanceTournament", true, true),
    TOTAL_WAGER_TOURNAMENT("TotalWagerTournament", true, true);

    private final String stringRepresentation;
    private final boolean tournamentLogic;
    private final boolean scoreCounting;
    private final boolean singleWinPerMultiplePromosAtOnce;
    private final String merchantName;

    PromoType(String stringRepresentation, boolean tournamentLogic, boolean scoreCounting, boolean singleWinPerMultiplePromosAtOnce, String merchantName) {
        this.stringRepresentation = stringRepresentation;
        this.tournamentLogic = tournamentLogic;
        this.scoreCounting = scoreCounting;
        this.singleWinPerMultiplePromosAtOnce = singleWinPerMultiplePromosAtOnce;
        this.merchantName = merchantName;
    }

    PromoType(String stringRepresentation, boolean tournamentLogic, boolean scoreCounting) {
        this.stringRepresentation = stringRepresentation;
        this.tournamentLogic = tournamentLogic;
        this.scoreCounting = scoreCounting;
        this.merchantName = stringRepresentation;
        this.singleWinPerMultiplePromosAtOnce = false;
    }

    public String getStringRepresentation() {
        return stringRepresentation;
    }

    public boolean isTournamentLogic() {
        return tournamentLogic;
    }

    public boolean isScoreCounting() {
        return scoreCounting;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public boolean isSingleWinPerMultiplePromosAtOnce() {
        return singleWinPerMultiplePromosAtOnce;
    }
}
