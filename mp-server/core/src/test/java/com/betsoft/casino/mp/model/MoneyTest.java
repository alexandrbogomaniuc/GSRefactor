package com.betsoft.casino.mp.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class MoneyTest {

    @Test
    public void testFromToCents() {
        assertEquals(5, Money.fromCents(5).toCents());
    }

    @Test
    public void testFromToCentsExact() {
        assertEquals(5, Money.fromCents(5).toCentsExact());
    }

    @Test
    public void testToCentsFloat() {
        assertEquals(5.0, Money.fromCents(5).toFloatCents(), 0);
    }

    @Test
    public void testToCentsDouble() {
        assertEquals(5d, Money.fromCents(5).toDoubleCents(), 0);
    }

    @Test
    public void testFloor() {
        assertEquals(new Money(1000000), new Money(1000001).floor());
        assertEquals(new Money(1000000), new Money(1500000).floor());
        assertEquals(new Money(1000000), new Money(1999999).floor());
        assertEquals(new Money(2000000), new Money(2000000).floor());
    }

    @Test
    public void shouldNotLoosePrecision() {
        assertEquals(Money.fromCents(100), Money.fromFloatCents(63.644350f).getWithMultiplier(1/0.6364435));
//        assertEquals(Money.fromFloatCents(1.2f), Money.fromCents(6).getWithMultiplier(0.2));
    }

    @Test
    public void testMultiply() {
        assertEquals(0.999998, Money.fromFloatCents(0.499999f).multiply(2).toDoubleCents(), 0.0000000001);
        assertEquals(0.056088, Money.fromFloatCents(0.123f).multiply(0.456).toDoubleCents(), 0.0000000001);
    }

    @Test
    public void testMultiplyToMoney() {
        Money first = Money.fromCents(5);
        Money second = Money.fromCents(2);
        Money result = first.multiply(second);
        assertEquals(10, result.toCents());
    }

    @Test
    public void testAdd() {
        Money first = Money.fromCents(5);
        Money second = Money.fromCents(2);
        Money result = first.add(second);
        assertEquals(7, result.toCents());
    }

    @Test
    public void testSubtract() {
        Money first = Money.fromCents(5);
        Money second = Money.fromCents(2);
        Money result = first.subtract(second);
        assertEquals(3, result.toCents());
    }

    @Test
    public void testDivideToLong() {
        Money first = Money.fromCents(8);
        Money result = first.divideBy(2);
        assertEquals(4, result.toCents());
    }

    @Test
    public void testDivideToMoney() {
        Money first = Money.fromCents(80);
        Money second = Money.fromCents(2);
        long result = first.divideBy(second);
        assertEquals(40, result);
    }


}