package com.betsoft.casino.mp.dragonstone.model.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class PayTableInstTest {

    @Test
    public void testPayTableCouldBeInstantiated() {
        assertNotNull(PayTableInst.getTable());
    }
}
