package com.abs.casino.common.cache.data.account;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 29.10.15
 */
public enum PlayerAction {

    BET {
        @Override
        public String toString() {
            return "bet";
        }
    },

    LEVEL_UP {
        @Override
        public String toString() {
            return "levelUp";
        }
    },

    START_GAME {
        @Override
        public String toString() {
            return "startGame";
        }
    }

}
