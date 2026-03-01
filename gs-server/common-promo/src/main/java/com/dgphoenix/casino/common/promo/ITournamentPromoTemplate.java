package com.abs.casino.common.promo;

import com.abs.casino.common.promo.TournamentObjective;
import com.abs.casino.common.promo.TournamentRankQualifier;

import java.util.Set;

/**
 * User: flsh
 * Date: 28.06.2022.
 */
public interface ITournamentPromoTemplate {
    TournamentObjective getObjective();
    TournamentRankQualifier getRankQualifier();

    Set<TournamentObjective> getAllowedObjectives();
}
