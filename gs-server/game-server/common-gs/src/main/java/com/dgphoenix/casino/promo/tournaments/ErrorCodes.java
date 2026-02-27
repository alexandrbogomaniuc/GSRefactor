package com.abs.casino.promo.tournaments;

public interface ErrorCodes {
    int INTERNAL_ERROR = 1;
    int SERVER_SHUTDOWN = 2;
    int INVALID_SESSION = 3;
    int FOUND_PENDING_OPERATION = 4;
    int BAD_REQUEST = 1001;
    int NOT_LOGGED_IN = 1002;
    int NOT_ENOUGH_MONEY = 1003;
    int TOURNAMENT_NOT_FOUND = 1004;
    int NOT_JOINED = 1005;
    int TOURNAMENT_EXPIRED = 1006;
    int PLAYER_ALIAS_ALREADY_REGISTERED = 1007;
    int PLAYER_ALIAS_NOT_ALLOWED = 1008;
    int GAME_ID_IS_NOT_ENABLED = 1009;
    int TIME_IS_NOT_CORRECT = 1010;
    int LOCK_FAILED = 1011;
}
