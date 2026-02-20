package com.dgphoenix.casino.common.util;

/**
 * User: flsh
 * Date: 22.03.13
 */
public class LongIdGeneratorFactory {
    private static LongIdGeneratorFactory instance = new LongIdGeneratorFactory();
    private ILongIdGenerator generator;


    public static LongIdGeneratorFactory getInstance() {
        return instance;
    }

    private LongIdGeneratorFactory() {
    }

    public void addGenerator(ILongIdGenerator generator) {
        this.generator = generator;
    }

    public ILongIdGenerator getGenerator() {
        return generator;
    }
}