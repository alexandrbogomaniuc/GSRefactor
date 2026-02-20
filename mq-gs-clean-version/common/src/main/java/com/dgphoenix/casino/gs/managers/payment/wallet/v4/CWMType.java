package com.dgphoenix.casino.gs.managers.payment.wallet.v4;

/**
 * Description of CWMType.
 * <p/>
 * Use every type in common case:
 * SEND_WIN_ONLY:                     need to send win that graiter 0 (winCondition: win>0)
 * SEND_WIN_ACCUMULATED:              need to send accumulated win that graiter 0 and isRoundFinished=true (winCondition: win>0 && isRoundFinished==true)
 * SEND_WIN_ONLY_AND_ISROUNDFINISHED: need to send accumulated win that graiter 0 or isRoundFinished=true (winCondition: win>0 || isRoundFinished==true)
 * SEND_WIN_ACCUMULATED_AND_ISROUNDFINISHED need to send win if isRoundFinished=true (isRoundFinished==true)
 */
public enum CWMType {
    SEND_ALWAYS {
        @Override
        public boolean isCreditCondition(long winAmount, long negativeBetAmount, boolean isRoundFinished) {
            return true;
        }

        @Override
        public boolean isWinAccumulated() {
            return false;
        }

        @Override
        public boolean isRoundFinished() {
            return false;
        }
    },
    SEND_WIN_ONLY {
        @Override
        public boolean isCreditCondition(long winAmount, long negativeBetAmount, boolean isRoundFinished) {
            return (winAmount + negativeBetAmount) > 0;
        }

        @Override
        public boolean isWinAccumulated() {
            return false;
        }

        @Override
        public boolean isRoundFinished() {
            return false;
        }
    },
    SEND_WIN_ACCUMULATED {
        @Override
        public boolean isCreditCondition(long winAmount, long negativeBetAmount, boolean isRoundFinished) {
            return ((winAmount + negativeBetAmount) > 0) && isRoundFinished;
        }

        @Override
        public boolean isWinAccumulated() {
            return true;
        }

        @Override
        public boolean isRoundFinished() {
            return false;
        }
    },
    SEND_WIN_ONLY_AND_ISROUNDFINISHED {
        @Override
        public boolean isCreditCondition(long winAmount, long negativeBetAmount, boolean isRoundFinished) {
            return ((winAmount + negativeBetAmount) > 0) || isRoundFinished;
        }

        @Override
        public boolean isWinAccumulated() {
            return false;
        }

        @Override
        public boolean isRoundFinished() {
            return true;
        }
    },
    SEND_WIN_ACCUMULATED_AND_ISROUNDFINISHED {
        @Override
        public boolean isCreditCondition(long winAmount, long negativeBetAmount, boolean isRoundFinished) {
            return isRoundFinished;
        }

        @Override
        public boolean isWinAccumulated() {
            return true;
        }

        @Override
        public boolean isRoundFinished() {
            return true;
        }
    },
    SEND_WIN_WITHOUT_NB_OR_ISROUNDFINISHED {
        @Override
        public boolean isCreditCondition(long winAmount, long negativeBetAmount, boolean isRoundFinished) {
            return winAmount > 0 || isRoundFinished;
        }

        @Override
        public boolean isWinAccumulated() {
            return false;
        }

        @Override
        public boolean isRoundFinished() {
            return true;
        }
    };

    public static CWMType getCWMTypeByString(String stringCWMType) {
        if (stringCWMType != null) {
            if (stringCWMType.equalsIgnoreCase(SEND_ALWAYS.toString())) {
                return SEND_ALWAYS;
            } else if (stringCWMType.equalsIgnoreCase(SEND_WIN_ONLY.toString())) {
                return SEND_WIN_ONLY;
            } else if (stringCWMType.equalsIgnoreCase(SEND_WIN_ACCUMULATED.toString())) {
                return SEND_WIN_ACCUMULATED;
            } else if (stringCWMType.equalsIgnoreCase(SEND_WIN_ONLY_AND_ISROUNDFINISHED.toString())) {
                return SEND_WIN_ONLY_AND_ISROUNDFINISHED;
            } else if (stringCWMType.equalsIgnoreCase(SEND_WIN_ACCUMULATED_AND_ISROUNDFINISHED.toString())) {
                return SEND_WIN_ACCUMULATED_AND_ISROUNDFINISHED;
            } else if (stringCWMType.equalsIgnoreCase(SEND_WIN_WITHOUT_NB_OR_ISROUNDFINISHED.toString())) {
                return SEND_WIN_WITHOUT_NB_OR_ISROUNDFINISHED;
            }
        }
        return SEND_WIN_ONLY;
    }

    public abstract boolean isCreditCondition(long winAmount, long negativeBetAmount, boolean isRoundFinished);

    public abstract boolean isWinAccumulated();

    public abstract boolean isRoundFinished();
}
