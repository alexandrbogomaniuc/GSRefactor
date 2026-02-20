package com.betsoft.casino.mp.utils;

public interface ErrorCodes {
    int OK = 0;
    int INTERNAL_ERROR = 1;
    int SERVER_SHUTDOWN = 2;
    int INVALID_SESSION = 3;
    int DEPRECATED_REQUEST = 4;
    int FOUND_PENDING_OPERATION = 5;
    int CLOSE_FRB_IN_PROGRESS = 6;
    int UNKNOWN_GAME_ID = 7;
    int FOUND_OPEN_LOBBY_SESSION = 8;
    int CANNOT_OBTAIN_LOCK = 9;
    int ROOM_WAS_DEACTIVATED = 10;
    int ILLEGAL_NICKNAME = 1000;
    int BAD_REQUEST = 1001;                         //this is fatal error on client side
    int NOT_LOGGED_IN = 1002;
    int ROOM_NOT_FOUND = 1003;
    int TOO_MANY_OBSERVERS = 1004;
    int ROOM_MOVED = 1005;
    int TOO_MANY_PLAYER = 1006;
    int NOT_SEATER = 1007;
    int NOT_ENOUGH_MONEY = 1008;
    int BAD_STAKE = 1009;                           //this is fatal error on client side
    int BAD_BUYIN = 1010;                           //this is fatal error on client side
    int CHANGE_STAKE_NOT_ALLOWED = 1011;
    int ROOM_NOT_OPEN = 1012;
    int ROUND_NOT_STARTED = 1013;
    int NEED_SITOUT = 1014;
    int WRONG_WEAPON = 1015;
    int NICKNAME_NOT_AVAILABLE = 1016;
    int AVATAR_PART_NOT_AVAILABLE = 1017;          //this is fatal error on client side
    int NOT_ENOUGH_BULLETS = 1018;
    int REQUEST_FREQ_LIMIT_EXCEEDED = 1019;
    int QUEST_IS_NOT_COMPLETED = 1020;
    int BUYIN_NOT_ALLOWED = 1021;
    int BUYIN_NOT_ALLOWED_ALREADY = 1043;
    int QUEST_ALREADY_COLLECTED = 1022;
    int WRONG_MINE_COORDINATES = 1023;
    int BOSS_ROUND_STARTED = 1024;
    int WRONG_UNPLAYED_FREE_SHOTS = 1025;
    int NOT_FATAL_BAD_BUYIN = 1026;
    int NOT_ALLOWED_CHANGE_BET_LEVEL = 1027;
    int NOT_ALLOWED_CHANGE_NICKNAME = 1028;
    int NOT_ALLOWED_PLACE_BULLET = 1029;
    int NOT_ALLOWED_SITIN = 1030;
    int BET_NOT_FOUND = 1031;
    int BAD_MULTIPLIER = 1032;
    int CANCEL_BET_NOT_ALLOWED = 1033;
    int FOUND_TEMPORARY_PENDING_OPERATION = 1034;
    int NOT_ALLOWED_START_ROUND = 1035;

    int BG_ROUND_ALREADY_STARTED = 1036;

    int BG_ROUND_ALREADY_FINISHED = 1037;
    int NOT_ALLOWED_SIT_IN_FOR_BOT = 1038;
    int MQ_SERVER_REBOOT = 1039;
    int NOT_ALLOWED_KICK = 1040;
    int OBSERVER_DOESNT_EXIST = 1041;
    int SIT_OUT_NOT_ALLOWED = 1042;

    //errorCodes after 100000 reserved for GS error codes
    int BASE_GS_ERROR_CODE = 100000;

    int WRONG_STEP = 1044;

    static int translateGameServerErrorCode(int gsErrorCode) {
        return BASE_GS_ERROR_CODE + gsErrorCode;
    }
}
