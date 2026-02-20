package com.betsoft.casino.mp.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class RatSwarmTrajectoryGeneratorTest {

    @Test
    public void shouldLimitBaseAngle() {
        assertEquals(0, RatSwarmTrajectoryGenerator.getBaseAngle(0));
        assertEquals(0, RatSwarmTrajectoryGenerator.getBaseAngle(22));
        assertEquals(45, RatSwarmTrajectoryGenerator.getBaseAngle(23));
        assertEquals(45, RatSwarmTrajectoryGenerator.getBaseAngle(24));
        assertEquals(225, RatSwarmTrajectoryGenerator.getBaseAngle(210));
        assertEquals(225, RatSwarmTrajectoryGenerator.getBaseAngle(225));
        assertEquals(225, RatSwarmTrajectoryGenerator.getBaseAngle(245));
    }



}
