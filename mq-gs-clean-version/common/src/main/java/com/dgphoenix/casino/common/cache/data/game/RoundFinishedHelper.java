package com.dgphoenix.casino.common.cache.data.game;

/**
 * User: flsh
 * Date: 22.01.14
 */
public enum RoundFinishedHelper {
    NOTCONTAINS {
        @Override
        public boolean isRoundFinished(String lasthand, String endRoundSignature) {
            return !lasthand.contains(endRoundSignature);
        }
    },
    CONTAINS {
        @Override
        public boolean isRoundFinished(String lasthand, String endRoundSignature) {
            return lasthand.contains(endRoundSignature);
        }
    },
    WHOSPUNIT {
        @Override
        public boolean isRoundFinished(String lasthand, String endRoundSignature) {
            return lasthand.contains("STATE=0") || lasthand.contains("STATE=1")
                    || lasthand.contains("STATE=2") || lasthand.contains("STATE=3");
        }
    },
    GREEDYGOBLINS {
        @Override
        public boolean isRoundFinished(String lasthand, String endRoundSignature) {
            if (lasthand != null && lasthand.contains("SWC") && lasthand.contains("STATE=0")) {
                return false;
            }
            return lasthand.contains("STATE=0");
        }
    },
    GHOULS {
        @Override
        public boolean isRoundFinished(String lasthand, String endRoundSignature) {
            return !lasthand.contains("ENDLASTGAME=1") && !lasthand.contains("ENDLASTGAME=2");
        }
    },
    MOREGOLDDIGGIN {
        @Override
        public boolean isRoundFinished(String lasthand, String endRoundSignature) {
            return lasthand.contains("DUPP") && !lasthand.contains("CARDS");
        }
    },
    VIKINGVOYAGE {
        @Override
        public boolean isRoundFinished(String lasthand, String endRoundSignature) {
            return lasthand.contains("STATE=MAIN") && !lasthand.contains("STATE=MAIN_RESPIN");
        }
    };


    public abstract boolean isRoundFinished(String lasthand, String endRoundSignature);
}