namespace java com.dgphoenix.casino.thrift

typedef i32 int
typedef i64 long

struct TBotStatus {
    1: int status;
    2: long mmcBalance;
    3: long mqcBalance;
    4: bool success;
    5: int errorCode;
    6: string errorDetails;
}

struct TBotLogInResult {
    1: string sessionId;
    2: long mmcBalance;
    3: long mqcBalance;
    4: bool success;
    5: int errorCode;
    6: string errorDetails;
}

struct TBotLogOutResult {
    1: bool success;
    2: int errorCode;
    3: string errorDetails;
}

enum TBotState {
    idle,
    observing,
    waiting_for_response,
    playing,
    wait_battle_players,
    need_log_out
}

struct TBot {
    1: string id;
    2: string nickname;
    3: long roomId;
    4: int bankId;
    5: long gameId;
    6: int serverId;
    7: string token;
    8: string sid;
    9: string url;
    10: long expiresAt;
    11: TBotState botState;
    12: TBot roomBot;
}

struct TBotsMap {
    1: bool success;
    2: int errorCode;
    3: string errorDetails;
    4: list<TBot> botsMap;
}

service MQBBotService {
    TBotStatus getStatusForNewBot(1: string userName, 2: string password, 3: string botNickName, 4: long bankId, 5: long gameId);

    TBotLogInResult logIn(1: long botId, 2: string userName, 3: string password, 4: long bankId, 5: long gameId, 6: long buyIn,
            7: string botNickname, 8: long roomId, 9: string lang, 10: int gameServerId, 11: string enterLobbyWsUrl, 12: string openRoomWSUrl
            13: long expiresAt, 14: double shootsRate, 15: double bulletsRate);

    TBotStatus getStatus(1: long botId, 2: string sessionId, 3: string botNickName, 4: long roomId);

    TBotStatus confirmNextRoundBuyIn(1: long botId, 2: string sessionId, 3: string botNickName, 4: long roomId, 5: long roundId);

    TBotLogOutResult logOut(1: long botId, 2: string sessionId, 3: string botNickName, 4: long roomId);

    void removeBot(1: long botId, 2: string botNickName, 3: long roomId);

    string getDetailBotInfo(1: long botId, 2: string botNickName);

    TBotsMap getBotsMap();
}