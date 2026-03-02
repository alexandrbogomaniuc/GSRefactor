package com.abs.casino.common.promo;

import com.abs.casino.common.promo.AwardedPrize;
import com.abs.casino.common.promo.PromoCampaignMember;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static junit.framework.Assert.*;

public class PromoCampaignMemberTest {
    private PromoCampaignMember member;

    @Before
    public void setUp() {
        member = new PromoCampaignMember(123L, 777L, "Tester", 555L, 0);
    }

    @Test
    public void testGetLastAwardedPrizeWhenEmpty() {
        member.setAwardedPrizes(Collections.<AwardedPrize>emptyList());
        AwardedPrize lastAwardedPrize = member.getLastAwardedPrize();
        assertNull(lastAwardedPrize);
    }

    @Test
    public void testGetLastAwardedPrizeWhenSinglePrize() {
        AwardedPrize awardedPrize = new AwardedPrize(1L, 333L, System.currentTimeMillis());
        member.setAwardedPrizes(Collections.singletonList(awardedPrize));

        AwardedPrize lastAwardedPrize = member.getLastAwardedPrize();

        assertNotNull(lastAwardedPrize);
        assertEquals(awardedPrize, lastAwardedPrize);
    }

    @Test
    public void testGetLastAwardedPrize() {
        AwardedPrize firstPrize = new AwardedPrize(1L, 333L, System.currentTimeMillis());
        AwardedPrize secondPrize = new AwardedPrize(2L, 35L, System.currentTimeMillis());
        AwardedPrize thirdPrize = new AwardedPrize(3L, 444L, System.currentTimeMillis());
        member.setAwardedPrizes(Arrays.asList(firstPrize, secondPrize, thirdPrize));
        AwardedPrize lastAwardedPrize = member.getLastAwardedPrize();
        assertEquals(thirdPrize, lastAwardedPrize);
    }

    @Test
    public void testAwardedPrizesLimitOverflow() {
        long awardDate = System.currentTimeMillis();
        for (int i = 0; i < PromoCampaignMember.AWARDED_PRIZES_SIZE_LIMIT + 2; i++) {
            member.addAwardedPrize(new AwardedPrize(i, 1000 + i, awardDate));
        }
        System.out.println(member.getAwardedPrizes());
        assertEquals(member.getAwardedPrizes().size(), PromoCampaignMember.AWARDED_PRIZES_SIZE_LIMIT);
        assertFalse(member.getAwardedPrizes().contains(new AwardedPrize(0, 1000, awardDate)));
        assertFalse(member.getAwardedPrizes().contains(new AwardedPrize(1, 1001, awardDate)));
        assertTrue(member.getAwardedPrizes().contains(new AwardedPrize(2, 1002, awardDate)));
        assertTrue(member.getAwardedPrizes().contains(new AwardedPrize(11, 1011, awardDate)));
    }

    @Test(expected = NullPointerException.class)
    public void testAddingNullToAwardedPrizes() {
        member.addAwardedPrize(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDirectModifyAwardedPrizes() {
        member.getAwardedPrizes().add(new AwardedPrize(1L, 333L, System.currentTimeMillis()));
    }
}
