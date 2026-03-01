package com.abs.casino.common.promo;

import com.abs.casino.common.promo.ITournamentRankQualifier;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * User: flsh
 * Date: 18.01.17.
 */
public enum TournamentRankQualifier implements ITournamentRankQualifier {
    SCORE_AND_ENTER_TIME {
        @Override
        public int compare(TournamentMemberRank o1, TournamentMemberRank o2) {
            if (o1.getScore() == o2.getScore()) {
                return o1.getPlayerEnterTime() < o2.getPlayerEnterTime() ? -1 :
                        (o1.getPlayerEnterTime() == o2.getPlayerEnterTime() ? 0 : 1);
            }
            return defaultCompare(o1, o2);
        }

        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return Collections.emptySet();
        }
    },
    SCORE_AND_ACHIEVEMENT_TIME {
        @Override
        public int compare(TournamentMemberRank o1, TournamentMemberRank o2) {
            if (o1.getScore() == o2.getScore()) {
                return o1.getSaveTime() < o2.getSaveTime() ? -1 : (o1.getSaveTime() == o2.getSaveTime() ? 0 : 1);
            }
            return defaultCompare(o1, o2);
        }

        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return Collections.emptySet();
        }
    },
    SCORE_AND_WIN_SUM {
        @Override
        public int compare(TournamentMemberRank o1, TournamentMemberRank o2) {
            if (o1.getScore() == o2.getScore()) {
                return o1.getWinSum() > o2.getWinSum() ? -1 : (o1.getWinSum() == o2.getWinSum() ? 0 : 1);
            }
            return defaultCompare(o1, o2);
        }

        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return Collections.singleton(SignificantEventType.WIN);
        }
    },
    SCORE_AND_BETS_SUM {
        @Override
        public int compare(TournamentMemberRank o1, TournamentMemberRank o2) {
            if (o1.getScore() == o2.getScore()) {
                return o1.getBetSum() > o2.getBetSum() ? -1 : (o1.getBetSum() == o2.getBetSum() ? 0 : 1);
            }
            return defaultCompare(o1, o2);
        }

        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return Collections.singleton(SignificantEventType.BET);
        }
    },
    SCORE_AND_REVENUE {
        @Override
        public int compare(TournamentMemberRank o1, TournamentMemberRank o2) {
            if (o1.getScore() == o2.getScore()) {
                long revenue1 = o1.getBetSum() - o1.getWinSum();
                long revenue2 = o2.getBetSum() - o2.getWinSum();
                return revenue1 > revenue2 ? -1 : (revenue1 == revenue2 ? 0 : 1);
            }
            return defaultCompare(o1, o2);
        }

        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return EnumSet.of(SignificantEventType.BET, SignificantEventType.WIN);
        }
    },
    SCORE_ONLY {
        @Override
        public int compare(TournamentMemberRank o1, TournamentMemberRank o2) {
            if (o1.getScore() == o2.getScore()) {
                return 0;
            }
            return defaultCompare(o1, o2);
        }

        @Override
        public Set<SignificantEventType> getSignificantEvents() {
            return Collections.emptySet();
        }
    };

    int defaultCompare(TournamentMemberRank o1, TournamentMemberRank o2) {
        return o1.getScore() < o2.getScore() ? 1 : -1;
    }

    public abstract Set<SignificantEventType> getSignificantEvents();
}
