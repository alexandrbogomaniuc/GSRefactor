package com.dgphoenix.casino.promo.masker;

import com.dgphoenix.casino.common.promo.feed.tournament.RoundCountRecord;
import com.dgphoenix.casino.common.promo.feed.tournament.SummaryTournamentFeedEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

public class TournamentSummaryFeedNameMaskerTest {
    private final List<SummaryTournamentFeedEntry> testEntries = Arrays.asList(
            new SummaryTournamentFeedEntry("first",
                    new RoundCountRecord("1", "longName", null, 10)),
            new SummaryTournamentFeedEntry("second",
                    new RoundCountRecord("1", "small", null, 10)),
            new SummaryTournamentFeedEntry("third",
                    new RoundCountRecord("1", "s", null, 10)),
            new SummaryTournamentFeedEntry("fourth",
                    new RoundCountRecord("1", "longName@hotmail.com", null, 10)),
            new SummaryTournamentFeedEntry("fifth",
                    new RoundCountRecord("1", "small@mail.ru", null, 10)),
            new SummaryTournamentFeedEntry("sixth",
                    new RoundCountRecord("1", "s@gmail.com", null, 10)),
            new SummaryTournamentFeedEntry("seventh",
                    new RoundCountRecord("1", null, null, 10)),
            new SummaryTournamentFeedEntry("eighth",
                    new RoundCountRecord("1", "", null, 10))
    );

    private List<SummaryTournamentFeedEntry> entriesWithMaskedNames;

    @Before
    public void setUp() {
        SummaryFeedNameMasker<SummaryTournamentFeedEntry> masker = new TournamentSummaryFeedNameMasker(testEntries);
        entriesWithMaskedNames = masker.getFeedEntriesWithMaskedNames();
    }

    @Test
    public void testLongName() {
        assertEquals("longN***", getNameForBank("first"));
    }

    @Test
    public void testShortName() {
        assertEquals("smal*", getNameForBank("second"));
    }

    @Test
    public void testVeryShortName() {
        assertEquals("s*", getNameForBank("third"));
    }

    @Test
    public void testLongNameWithEmail() {
        assertEquals("longN***", getNameForBank("fourth"));
    }

    @Test
    public void testShortNameWithEmail() {
        assertEquals("smal*", getNameForBank("fifth"));
    }

    @Test
    public void testVeryShortNameWithEmail() {
        assertEquals("s*", getNameForBank("sixth"));
    }

    @Test
    public void testEmptyName() {
        assertEquals("", getNameForBank("eighth"));
    }

    @Test
    public void testNullName() {
        assertNull(getNameForBank("seventh"));
    }

    private String getNameForBank(String bankName) {
        return entriesWithMaskedNames.stream()
                .filter(raffleFeedEntry -> raffleFeedEntry.getBankName().equals(bankName))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new).getNickName();
    }
}