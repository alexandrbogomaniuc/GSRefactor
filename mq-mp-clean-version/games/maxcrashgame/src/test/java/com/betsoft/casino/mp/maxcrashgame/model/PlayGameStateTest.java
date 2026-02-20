package com.betsoft.casino.mp.maxcrashgame.model;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlayGameStateTest extends TestCase {

    @Mock
    AbstractCrashGameRoom room;

    PlayGameState playGameState;

    @Before
    public void setUp() {
        playGameState = new PlayGameState(room);
    }

    @Test
    public void testCalculateWin() {
        long actual = playGameState.calculateWin(1.01, 200).toCents();
        assertEquals(202, actual);

        long actual2 = playGameState.calculateWin(1.01, 401).toCents();
        assertEquals(405, actual2);

        long actual3 = playGameState.calculateWin(1.99, 200).toCents();
        assertEquals(398, actual3);

        long actual4 = playGameState.calculateWin(11.25, 130).toCents();
        assertEquals(1462, actual4);

        long actual5 = playGameState.calculateWin(11.99, 121).toCents();
        assertEquals(1450, actual5);
    }
}