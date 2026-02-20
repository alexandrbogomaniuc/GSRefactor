package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 19.05.2020.
 */
public interface IExperience {
    void add(double xp);

    void add(IExperience xp);

    void multiply(double multiplier);

    double getAmount();

    long getLongAmount();

    long getDiff(IExperience other);
}
