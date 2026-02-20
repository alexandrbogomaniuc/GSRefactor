package com.dgphoenix.casino.common.promo;

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
