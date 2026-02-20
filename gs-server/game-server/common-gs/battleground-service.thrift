namespace java com.dgphoenix.casino.thrift.managers.btgconfig

typedef i64 long
typedef i32 int

struct TBattlegroundGame {
    1: long gameId
    2: bool enabled
    3: list<long> selectedBuyIns
    4: double rake
    5: int maxNumberPlayers
}

struct TBattlegroundConfiguration {
    1: long gameId
    2: bool enabled
    3: list<long> availableBuyIns
    4: list<long> selectedBuyIns
    5: double rake
}

struct TActiveBattlegroundRoundInfo {
    1: long roundId
    2: long gameId
    3: long buyIn
    4: int participants
    5: double rake
}


service BattlegroundService {
    list<TBattlegroundGame> getBattlegroundGames(1: long bankId);

    TBattlegroundConfiguration getBattlegroundConfiguration(1: long bankId, 2: long gameId);

    void saveBattlegroundConfiguration(1: long bankId, 2: TBattlegroundConfiguration battlegroundGame);

    list<TActiveBattlegroundRoundInfo> getActiveBattlegroundRoundsInfo(1: long bankId);

    list<TActiveBattlegroundRoundInfo> getActiveBattlegroundRoundsInfoByGameId(1: long bankId, 2: long gameId);
}