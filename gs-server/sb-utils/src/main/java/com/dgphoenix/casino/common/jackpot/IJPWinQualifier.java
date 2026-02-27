package com.abs.casino.common.jackpot;

public interface IJPWinQualifier {
    boolean isJpCanBeWonForCoin(long coinId);

    boolean isJpCanBeWonForAmount(double amount);
}
