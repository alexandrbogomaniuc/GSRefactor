package com.abs.casino.services.bonus;

import com.dgphoenix.casino.common.cache.SubCasinoCache;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: zhevlakoval
 * Date: 15.06.12
 * Time: 16:14
 * Список игр по которым нельзя создавать бонусы
 */
public class ForbiddenGamesForBonusProvider {

    private static final long ROYAAL_CASINO_BANK_ID = 164L;
    private static final long BETONLINE_NG_SUBCASINO_ID = 289L;

    private static final Set<Long> COMMON_GAMES = ImmutableSet.of(
            209L, //CDROULETTE
            298L, //ATTHECOPASMALL
            299L, //ATTHECOPAMIDDLE
            310L, //ATTHECOPASMALLMOBILE
            311L, //ATTHECOPAMIDDLEMOBILE
            313L, //ATTHECOPASMALLANDROID
            314L, //ATTHECOPAMIDDLEANDROID
            779L  //UNDISCOVERED_EGYPT
    );
    private static final Set<Long> ROYAAL_GAMES = ImmutableSet.<Long>builder()
            .addAll(COMMON_GAMES)
            .add(146L) // 10s or better poker
            .add(103L) // Double Jackpot poker
            .add(142L) // Split Way Royal Poker
            .build();
    private static final Set<Long> BETONLINE_NG_GAMES = ImmutableSet.<Long>builder()
            .addAll(COMMON_GAMES)
            .add(30404L) // THEGOLDENGAMES (= Spring Tails)
            .build();

    private final SubCasinoCache subCasinoCache;

    public ForbiddenGamesForBonusProvider(SubCasinoCache subCasinoCache) {
        this.subCasinoCache = subCasinoCache;
    }

    public Set<Long> getGames(long bankId) {
        if (isBetOnlineNg(bankId)) {
            return BETONLINE_NG_GAMES;
        } else if (bankId == ROYAAL_CASINO_BANK_ID) {
            return ROYAAL_GAMES;
        } else {
            return COMMON_GAMES;
        }
    }

    private boolean isBetOnlineNg(long bankId) {
        return Optional.ofNullable(subCasinoCache.getBankIds(BETONLINE_NG_SUBCASINO_ID))
                .map(bankIds -> bankIds.contains(bankId))
                .orElse(false);
    }
}
