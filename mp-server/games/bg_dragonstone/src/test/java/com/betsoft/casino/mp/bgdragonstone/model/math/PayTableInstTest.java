package com.betsoft.casino.mp.bgdragonstone.model.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class PayTableInstTest {

    @Test
    public void testPayTableCouldBeInstantiated() {
        assertNotNull(PayTableInst.getTable());
    }
}
