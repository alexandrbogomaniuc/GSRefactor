package com.abs.casino.common.cache.data.account;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 29.10.15
 */
public enum PlayerGameError {

    START_GAME {
        @Override
        public String toString() {
            return "startGame";
        }
    },

    PLACE_BET {
        @Override
        public String toString() {
            return "placeBet";
        }
    },

    LEVEL_UP {
        @Override
        public String toString() {
            return "levelUp";
        }
    },

    MAKE_PURCHASE {
        @Override
        public String toString() {
            return "makePurchase";
        }
    }

}
