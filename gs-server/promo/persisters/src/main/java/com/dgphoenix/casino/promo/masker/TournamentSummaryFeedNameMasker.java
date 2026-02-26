package com.abs.casino.promo.masker;

import com.dgphoenix.casino.common.promo.feed.tournament.SummaryTournamentFeedEntry;

import java.util.List;
import java.util.stream.Collectors;

public class TournamentSummaryFeedNameMasker extends SummaryFeedNameMasker<SummaryTournamentFeedEntry> {
    private final List<SummaryTournamentFeedEntry> feedEntries;

    public TournamentSummaryFeedNameMasker(List<SummaryTournamentFeedEntry> feedEntries) {
        this.feedEntries = feedEntries;
    }

    @Override
    public List<SummaryTournamentFeedEntry> getFeedEntriesWithMaskedNames() {
        return feedEntries.stream()
                .map(feedEntry -> {
                    String originalName = feedEntry.getNickName();
                    String maskedName = maskName(originalName);
                    feedEntry.setNickName(maskedName);
                    return feedEntry;
                })
                .collect(Collectors.toList());
    }
}
