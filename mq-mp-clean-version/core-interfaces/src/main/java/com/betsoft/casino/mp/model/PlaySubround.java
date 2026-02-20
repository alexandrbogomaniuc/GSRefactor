package com.betsoft.casino.mp.model;

/**
 * User: flsh
 * Date: 02.04.18.
 */
public enum PlaySubround {
    BASE() {
        @Override
        public PlaySubround getNext() {
            return BOSS;
        }
    },
    BOSS() {
        @Override
        public PlaySubround getNext() {
            return null;
        }
    };

    PlaySubround() {
    }

    public abstract PlaySubround getNext();
}
