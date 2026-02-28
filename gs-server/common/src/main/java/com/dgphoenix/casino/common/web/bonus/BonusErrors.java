package com.abs.casino.common.web.bonus;

import com.dgphoenix.casino.common.web.bonus.BonusError;

/**
 * User: ktd
 * Date: 29.03.11
 */
public class BonusErrors {

    private BonusErrors() {
    }

    public static final BonusError INVALID_OR_EMPTY_AMOUNT = new BonusError(602, "Invalid or empty amount");
    public static final BonusError INVALID_PARAMETERS = new BonusError(610, "Invalid parameters");
    public static final BonusError INVALID_AUTO_RELEASE_PARAM = new BonusError(611, "Invalid autoRelease parameter");
    public static final BonusError INVALID_HASH = new BonusError(620, "Invalid hash");
    public static final BonusError USER_NOT_FOUND = new BonusError(631, "User not found");
    public static final BonusError TOKEN_NOT_FOUND = new BonusError(632, "Token not found");
    public static final BonusError INTERNAL_ERROR = new BonusError(699, "Internal error");
    public static final BonusError OPERATION_NOT_FOUND = new BonusError(630, "Operation not found");
    public static final BonusError OPERATION_ALREADY_EXIST = new BonusError(641, "Bonus with such extBonusId already exists");
    public static final BonusError BONUS_NOT_FOUND = new BonusError(630, "Bonus not found");

    public static final BonusError INVALID_BONUS_TYPE = new BonusError(601, "Invalid or empty bonus type");
    public static final BonusError INVALID_MULTIPLIER = new BonusError(603, "Invalid or empty multiplier");
    public static final BonusError INVALID_ROLLOVER_AMOUNT = new BonusError(608, "Invalid rollover amount");
    public static final BonusError INVALID_GAMES_MODE = new BonusError(604, "Invalid or empty game modes");
    public static final BonusError INVALID_GAMES_ID = new BonusError(605, "Invalid or empty game ID");
    public static final BonusError INVALID_EXP_DATE = new BonusError(606, "Invalid or empty exp Date");
    public static final BonusError INVALID_ROUNDS = new BonusError(607, "Invalid rounds");
    public static final BonusError EXPIRED = new BonusError(608, "Wrong exp Date: already expired");
    public static final BonusError INVALID_BONUS_ID = new BonusError(640, "Invalid or empty bonus ID");
    public static final BonusError INVALID_DURATION = new BonusError(642, "Invalid Duration");
    public static final BonusError INVALID_START_TIME = new BonusError(643, "Invalid or empty startTime");
    public static final BonusError INVALID_EXP_TIME = new BonusError(644, "Invalid or empty expirationTime");
    public static final BonusError INVALID_DATES_COMBINATION = new BonusError(645, "Expiration Time less or equals Start Time");
    public static final BonusError INVALID_DATES_DURATION_COMB = new BonusError(646, "Duration is greater than selected time period");
    public static final BonusError INVALID_EXP_HOURS = new BonusError(647, "Invalid or empty expirationHours");
    public static final BonusError INVALID_TABLE_CHIPS = new BonusError(648, "Invalid table chips value");
    public static final BonusError INVALID_FRB_PROFILE = new BonusError(649, "Invalid profile name");
    public static final BonusError INVALID_MAX_WIN = new BonusError(650, "Invalid maxWin value");
    public static final BonusError INVALID_MAX_WIN_MULTIPLIER = new BonusError(652, "Invalid maxWinMultiplier value");

    public static final BonusError FORBIDDEN_GAME_ID = new BonusError(651, "Forbidden game ID");
}
