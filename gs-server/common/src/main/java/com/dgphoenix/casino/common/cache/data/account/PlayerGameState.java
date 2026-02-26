package com.abs.casino.common.cache.data.account;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 11.02.16
 */
public enum PlayerGameState {

    NOT_SPECIFIED {
        @Override
        public String toString() {
            return "notSpecified";
        }
    },

    OUT_OF_COINS {
        @Override
        public String toString() {
            return "outOfCoins";
        }
    },

    PROMO {
        @Override
        public String toString() {
            return "promo";
        }
    }
}
