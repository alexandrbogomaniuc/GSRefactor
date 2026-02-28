package com.abs.casino.gs.managers.payment.wallet;

import com.dgphoenix.casino.gs.managers.payment.wallet.CWError;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: Jun 29, 2010
 */
public class CommonWalletErrors {
    public static final CWError USER_ALREADY_EXIST = new CWError(101, "User already exists");
    public static final CWError USER_NOT_FOUND = new CWError(100, "User not found");
    public static final CWError INTERNAL_ERROR = new CWError(110, "Internal Error");
    public static final CWError CURRENCY_NOT_FOUND = new CWError(111, "Currency not found");
    public static final CWError BAD_CURRENCY_CODE = new CWError(112, "Bad currency code");
    public static final CWError CURRENCY_MISMATCH = new CWError(113, "Currency mismatch");
    public static final CWError INSUFFICIENT_FUNDS = new CWError(300, "Insufficient funds");
    public static final CWError OPERATION_FAILED = new CWError(301, "Operation failed");
    public static final CWError UNKNOWN_TRANSACTION_ID = new CWError(302, "Unknown transaction id");
    public static final CWError UNKNOWN_USER_ID = new CWError(310, "Unknown user id");
    public static final CWError TRANSACTION_WAGER_LIMIT_REACHED = new CWError(311, "Per transaction limit reached", false, true);
    public static final CWError WEEK_WAGER_LIMIT_REACHED = new CWError(312, "Week limit reached", false, true);
    public static final CWError RESPONSIBLE_LIMIT_REACHED = new CWError(313, "Responsible gaming limit reached");
    public static final CWError DAY_WAGER_LIMIT_REACHED = new CWError(314, "Day limit reached", false, true);
    public static final CWError MONTH_WAGER_LIMIT_REACHED = new CWError(315, "Month limit reached", false, true);
    public static final CWError REALITY_CHECK_REQUIRED = new CWError(316, "Reality check required", false, true);
    public static final CWError SESSION_WAGER_LIMIT_REACHED = new CWError(317, "Session wager limit reached", false, true);
    public static final CWError PLAYER_SELF_EXCLUDED = new CWError(318, "Player is self excluded");
    public static final CWError MAXIMUM_LOGIN_DURATION = new CWError(319, "Maximum log-in duration reached");
    public static final CWError SPIN_IS_CANCELLED = new CWError(320, "Spin is canceled and money are being refunded");
    public static final CWError SPIN_HAS_DELAYED_WIN = new CWError(321, "Prize is being processed");
    public static final CWError MAXIMUM_BONUS_BET = new CWError(322, "Bonus bet limit reached");
    public static final CWError BONUS_WAGER_REQUIREMENT_REACHED = new CWError(323, "Bonus wagering requirement reached");
    public static final CWError SESSION_CLOSED = new CWError(324, "Session is closed");
    public static final CWError PLAY_LIMIT_REACHED = new CWError(325, "One of play limits reached");
    public static final CWError ROUND_CLOSED = new CWError(326, "Round is closed on external side");
    public static final CWError SESSION_LOSS_LIMIT_REACHED = new CWError(330, "Session loss limit reached");
    public static final CWError DAY_LOSS_LIMIT_REACHED = new CWError(331, "Daily loss limit reached");
    public static final CWError WEEK_LOSS_LIMIT_REACHED = new CWError(332, "Weekly loss limit reached");
    public static final CWError MONTH_LOSS_LIMIT_REACHED = new CWError(333, "Monthly loss limit reached");
    public static final CWError WITHDRAW_LIMIT_REACHED = new CWError(334, "Withdraw limit reached");
    public static final CWError EC_INTERNAL_ERROR = new CWError(399, "Internal Error");
    public static final CWError INVALID_TOKEN = new CWError(400, "Invalid token");
    public static final CWError PLAYER_HAS_BETS_IN_CDR = new CWError(499, "Player has bets in CDR");
    public static final CWError INVALID_HASH = new CWError(500, "Invalid hash code");
    public static final CWError REMOTE_SERVICE_NOT_AVAILABLE = new CWError(599, "The service is unavailable right now");

    //EveryMatrix ERROR CODES
    public static final CWError EM_USER_BLOCKED = new CWError(1000, "User blocked");
    public static final CWError EM_USER_NOT_ACTIVE = new CWError(1001, "User not active");
    public static final CWError EM_BETSOFT_ACCOUNT_NOT_FOUND = new CWError(1010, "Betsoft account not found");
    public static final CWError EM_BETSOFT_ACCOUNT_IS_BLOCKED = new CWError(1011, "Betsoft account is blocked");
    public static final CWError EM_BETSOFT_ACCOUNT_IS_NOT_ACTIVE = new CWError(1012, "Betsoft account is not active");
    public static final CWError INVALID_SESSION = new CWError(1020, "Invalid session");

    //PlayTech ERROR CODES
    public static final CWError PLAYER_ACCOUNT_LOCKED = new CWError(26, "Player account locked");
    public static final CWError GAMEPLAY_BLOCKED = new CWError(1016, "Gameplay blocked by wallet");
    public static final CWError MANDATORY_LIMIT_MISSING = new CWError(2010, "Mandatory limit missing");
    public static final CWError GENERAL_USER_ERROR = new CWError(2212, "General user error");

    private static final Map<Integer, CWError> errorsMap = new HashMap<Integer, CWError>();
    static {
        Field[] errors = CommonWalletErrors.class.getDeclaredFields();
        for (Field errorField : errors) {
            try {
                CWError error = (CWError) errorField.get(null);
                errorsMap.put(error.getCode(), error);
            } catch (Exception e) {
                //ignore
            }
        }
    }

    public static CWError getCWErrorByCode(int code) {
        return errorsMap.get(code);
    }
}
