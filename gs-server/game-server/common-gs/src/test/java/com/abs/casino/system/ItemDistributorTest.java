package com.abs.casino.system;

import com.dgphoenix.casino.system.ItemDistributor;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 03.06.16
 */
public class ItemDistributorTest {

    private static final String UNEXPECTED_DISTRIBUTION = "Unexpected evenly distribution";

    @Test
    public void distributeOneItemBetweenTwoServers() {
        ItemDistributor distributor = new ItemDistributor(Arrays.asList(1, 2), Collections.singletonList(11L));
        Map<Integer, List<Long>> actualDistribution = distributor.distributeEvenly();

        Map<Integer, List<Long>> expectedDistribution = new HashMap<>(2);
        expectedDistribution.put(1, Collections.singletonList(11L));
        expectedDistribution.put(2, Collections.<Long>emptyList());

        assertEquals(UNEXPECTED_DISTRIBUTION, expectedDistribution, actualDistribution);
    }

    @Test
    public void distributeTwoItemsBetweenTwoServers() {
        ItemDistributor distributor = new ItemDistributor(Arrays.asList(1, 2), Arrays.asList(11L, 12L));
        Map<Integer, List<Long>> actualDistribution = distributor.distributeEvenly();

        Map<Integer, List<Long>> expectedDistribution = new HashMap<>(2);
        expectedDistribution.put(1, Collections.singletonList(11L));
        expectedDistribution.put(2, Collections.singletonList(12L));

        assertEquals(UNEXPECTED_DISTRIBUTION, expectedDistribution, actualDistribution);
    }

    @Test
    public void distributeThreeItemsBetweenTwoServers() {
        ItemDistributor distributor = new ItemDistributor(Arrays.asList(1, 2), Arrays.asList(11L, 12L, 13L));
        Map<Integer, List<Long>> actualDistribution = distributor.distributeEvenly();

        Map<Integer, List<Long>> expectedDistribution = new HashMap<>(2);
        expectedDistribution.put(1, Arrays.asList(11L, 13L));
        expectedDistribution.put(2, Collections.singletonList(12L));

        assertEquals(UNEXPECTED_DISTRIBUTION, expectedDistribution, actualDistribution);
    }

    @Test
    public void distributeFiveItemsBetweenThreeServers() {
        ItemDistributor distributor = new ItemDistributor(Arrays.asList(1, 2, 3), Arrays.asList(11L, 12L, 13L, 14L, 15L));
        Map<Integer, List<Long>> actualDistribution = distributor.distributeEvenly();

        Map<Integer, List<Long>> expectedDistribution = new HashMap<>(2);
        expectedDistribution.put(1, Arrays.asList(11L, 14L));
        expectedDistribution.put(2, Arrays.asList(12L, 15L));
        expectedDistribution.put(3, Collections.singletonList(13L));

        assertEquals(UNEXPECTED_DISTRIBUTION, expectedDistribution, actualDistribution);
    }

    @Test
    public void distributeSevenItemsBetweenThreeServers() {
        ItemDistributor distributor = new ItemDistributor(Arrays.asList(1, 2, 3),
                Arrays.asList(11L, 12L, 13L, 14L, 15L, 16L, 17L));
        Map<Integer, List<Long>> actualDistribution = distributor.distributeEvenly();

        Map<Integer, List<Long>> expectedDistribution = new HashMap<>(2);
        expectedDistribution.put(1, Arrays.asList(11L, 12L, 17L));
        expectedDistribution.put(2, Arrays.asList(13L, 14L));
        expectedDistribution.put(3, Arrays.asList(15L, 16L));

        assertEquals(UNEXPECTED_DISTRIBUTION, expectedDistribution, actualDistribution);
    }
}
