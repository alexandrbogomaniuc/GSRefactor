package com.dgphoenix.casino.common;

import com.dgphoenix.casino.common.exception.CommonException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GameSessionExtendedPropertiesTest {

//    @Test(expected = CommonException.class)
    public void shouldNotOverrideContributions() throws CommonException {
        GameSessionExtendedProperties properties = new GameSessionExtendedProperties();
        Map<Long, Double> first = new HashMap<Long, Double>();
        first.put(100L, 12.3);
        first.put(101L, 24.6);
        properties.addLeaderboardContributions(1L, first);
        Map<Long, Double> second = new HashMap<Long, Double>();
        second.put(101L, 32d);
        properties.addLeaderboardContributions(1L, second);
    }

    @Test
    public void shouldNotRemoveExistingContributions() throws CommonException {
        GameSessionExtendedProperties properties = new GameSessionExtendedProperties();
        Map<Long, Double> first = new HashMap<Long, Double>();
        first.put(100L, 12.3);
        first.put(101L, 24.6);
        properties.addLeaderboardContributions(1L, first);
        properties.addLeaderboardContributions(1L, null);
        properties.addLeaderboardContributions(1L, new HashMap<Long, Double>());

        Map<Long, Map<Long, Double>> expected = new HashMap<Long, Map<Long, Double>>();
        expected.put(1L, first);
        assertEquals(expected, properties.getLeaderboardContributions());
    }

    @Test
    public void shouldSummarizeLeaderboardContributions() throws CommonException {
        GameSessionExtendedProperties properties = new GameSessionExtendedProperties();

        Map<Long, Double> firstRound = new HashMap<Long, Double>();
        firstRound.put(1L, 10.5);
        firstRound.put(2L, 12.0);
        properties.addLeaderboardContributions(10L, firstRound);

        Map<Long, Double> secondRound = new HashMap<Long, Double>();
        secondRound.put(1L, 1.234);
        secondRound.put(3L, 23.0);
        properties.addLeaderboardContributions(20L, secondRound);

        Map<Long, Double> expected = new HashMap<Long, Double>();
        expected.put(1L, 11.734);
        expected.put(2L, 12.0);
        expected.put(3L, 23.0);

        assertEquals(expected, properties.getSummarizedContributions());
    }

    @Test
    public void shouldIgnoreRepeatedRecords() throws CommonException {
        GameSessionExtendedProperties properties = new GameSessionExtendedProperties();

        Map<Long, Double> firstRound = new HashMap<Long, Double>();
        firstRound.put(1L, 10.5);
        firstRound.put(2L, 12.0);
        properties.addLeaderboardContributions(10L, firstRound);

        Map<Long, Double> secondRound = new HashMap<Long, Double>();
        secondRound.put(2L, 12.0);
        secondRound.put(1L, 10.5);
        properties.addLeaderboardContributions(10L, secondRound);

        assertEquals(firstRound, properties.getSummarizedContributions());
    }
}
