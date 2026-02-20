package com.betsoft.casino.mp.exceptions;

import java.util.StringJoiner;

/**
 * User: flsh
 * Date: 21.07.2021.
 */
public class ErrorProcessedException extends Exception {
    @Override
    public String toString() {
        return new StringJoiner(", ", ErrorProcessedException.class.getSimpleName() + "[", "]").toString();
    }
}
