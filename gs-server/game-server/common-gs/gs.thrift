#see http://diwakergupta.github.io/thrift-missing-guide/
namespace java com.dgphoenix.casino.thrift

typedef i64 long
typedef i32 int

exception CommonException {
  1: int code,
  2: string message
}

struct InternalServerInfo {
    1: int gameServerId;
    2: bool gameServerStatus;
    3: bool masterServer;
    4: long upTime;
    5: long ntpLastSyncTime;
    6: long ntpOffset;
    7: long currentTime;
}

struct GameDataAccess {
    1: bool bControl;
    2: double GtotalBetOld;
    3: double GtotalWinOld;
    4: double GtotalRestOld;
    5: double GtotalCountOld;
    6: double GsetWinPercent;
    7: double GtotalBet;
    8: double GtotalWin;
    9: double GtotalRest;
    10: double GtotalCount;
    11: double GfutureWin;
    12: double GfutureChn;
    13: double GreserveWin;
    14: double GreserveChn;
    15: double GfalseWin;
    16: double GfalseChn;
    17: double GWinPercentGame;
    18: double GfutureWinAdd;
    19: double GfutureChnAdd;
}

struct Jackpot3Key {
    1: long bankId;
    2: long gameId;
    3: string currencyCode;
    4: string stringKey;
}

enum PromoNotificationType {
    CAMPAIGN_STATUS_CHANGED,
    PRIZE_WON,
    MISSION_OBJECTIVE,
    TOURNAMENT_INFO
}

struct TFRBonus {
    1: long bonusId;
    2: long awardDate;
    3: long startDate;
    4: long expirationDate;
    5: long rounds;
    6: long roundsLeft;
    7: long winSum;
    8: long spinCost;
    9: long maxWinLimit;
}

struct TCashBonus {
    1: long bonusId;
    2: long awardDate;
    3: long expirationDate;
    4: long balance;
    5: long amount;
    6: long amountToRelease;
    7: double rolloverMultiplier;
    8: long betSum;
    9: string status;
    10: long maxWinLimit;
}

struct TTournamentInfo {
    1: long tournamentId;
    2: string name;
    3: string state;
    4: long startDate;
    5: long endDate;
    6: long balance;
    7: long buyInPrice;
    8: long buyInAmount;
    9: bool reBuyAllowed;
    10: long reBuyPrice;
    11: long reBuyAmount;
    12: int reBuyCount;
    13: int reBuyLimit;
    14: bool resetBalanceAfterRebuy;
}

struct TBattlegroundInfo {
    1: long gameId;
    2: string icon;
    3: string rules;
    4: list<long> buyIns;
    5: double rake;
}

struct TDetailedPlayerInfo2 {
    1: long bankId;
    2: long accountId;
    3: string externalId;
    4: string userName;
    5: long balance;
    6: string currency;
    7: string currencySymbol;
    8: double currencyRateForEUR;
    9: bool guest;
    10: bool showRefreshBalanceButton;
    11: TFRBonus activeFrb;
    12: list<long> stakes;
    13: map<string, string> gameSettings;
    14: bool success;
    15: int errorCode;
    16: string errorDetails;
    17: double lbContribution;
    18: TCashBonus cashBonus;
    19: TTournamentInfo tournamentInfo;
    20: list<TBattlegroundInfo> battlegrounds;
}

struct TSitInResult {
    1: long gameSessionId;
    2: long amount;
    3: long balance;
    4: long playerRoundId;
    5: bool success;
    6: int errorCode;
    7: string errorDetails;
}

struct TBuyInResult {
    1: long amount;
    2: long balance;
    3: long playerRoundId;
    4: long gameSessionId;
    5: bool success;
    6: int errorCode;
    7: string errorDetails;
}

struct TStartNewRoundResult {
    1: long playerRoundId;
    2: long gameSessionId;
    3: bool success;
    4: int errorCode;
    5: string errorDetails;
}

struct TRoundPlayer {
    1: long accountId;
    2: string sessionId;
    3: long gameSessionId;
}

struct GameServerInfo {
    1: int id;
    2: string host;
    3: string domain;
    4: string thriftHost;
    5: int thriftPort;
}

struct TAddWinResult {
    1: bool playerOffline;
    2: long balance;
    3: bool success;
    4: int errorCode;
    5: string errorDetails;
}

struct TAddWinRequest {
    1: string sessionId;
    2: long gameSessionId;
    3: long cents;
    4: long returnedBet;
    5: long accountId;
    6: TRoundInfoResult roundInfo;
    7: map<long, double> contributions;
    8: long gsRoundId;
    9: bool sitOut;
}

struct TSitOutResult {
    1: bool success;
    2: int errorCode;
    3: string errorDetails;
}

struct TRefundResult {
    1: bool success;
    2: int errorCode;
    3: string errorDetails;
}

struct TSitOutCashBonusSessionResult {
    1: bool success;
    2: int errorCode;
    3: string errorDetails;
    4: TCashBonus cashBonus;
    5: long activeFRBonusId;
}

struct TSitOutTournamentSessionResult {
    1: bool success;
    2: int errorCode;
    3: string errorDetails;
    4: TTournamentInfo tournamentSession;
    5: long activeFRBonusId;
}

struct TCurrencyRate {
    1:  string sourceCurrency;
    2:  string destinationCurrency;
    3:  double rate;
    4:  long updateDate;
}

struct TRoundInfoResult {
    1: long accountId;
    2: long time;
    3: double bet;
    4: double payout;
    5: string archiveData;
    6: TBattlegroundRoundInfo battlegroundRoundInfo;
    7: long roundStartTime;
}

struct TBattlegroundRoundInfo {
    1: long buyIn;
    2: long winAmount;
    3: long betsSum;
    4: long winSum;
    5: list<TPlace> places;
    6: string status;
    7: int playersNumber;
    8: string winnerName;
    9: long roundId;
    10: long roundStartDate;
    11: string privateRoomId;
}

struct TPlace {
     1: long accountId;
     2: long win;
     3: int rank;
     4: long betsSum;
     5: long winSum;
     6: long gameSessionId;
     7: long gameScore;
     8: double ejectPoint;
}

struct TCloseFRBonusResult {
    1: long nextFRBonusId;
    2: bool success;
    3: int errorCode;
    4: string errorDetails;
    5: long balance;
    6: long realWinSum
}

struct BankName {
    1: long id;
    2: string name;
}

struct TLeaderboardWin {
    1: long bankId;
    2: long accountId;
    3: int place;
    4: long score;
    5: long win;
}

struct TLeaderboardResult {
    1: long leaderboardId;
    2: long startDate;
    3: long endDate;
    4: string currency;
    5: list<long> banks;
    6: list<long> games;
    7: list<TLeaderboardWin> winners;
}

struct TJackpotNotificationDetails {
    1: long id;
    2: long betId;
    3: long accountId;
    4: long amount;
    5: long time;
    6: set<long> notifiedPlayers;
}

struct TJackpotNotification {
    1: long bankId;
    2: long gameId;
    3: string currencyCode;
    4: map<long, TJackpotNotificationDetails> jackpotDetails;
}

struct TMQTreasureQuestProgress {
    1: int treasureId;
    2: int collect;
    3: int goal;
}

struct TMQuestAmount {
    1: int fromAmount;
    2: int toAmount;
}

struct TMQuestPrize {
    1: TMQuestAmount amount;
    2: int specialWeaponId;
}

struct TMQQuestData {
    1: long id;
    2: int type;
    3: long roomCoin;
    4: bool needReset;
    5: long collectedAmount;
    6: string name;
    7: TMQuestPrize questPrize;
    8: list<TMQTreasureQuestProgress> treasures;
}


struct TMQData {
    1: long accountId;
    2: long gameId;
    3: string nickname;
    4: double experience;
    5: int rounds;
    6: map<int, long> kills;
    7: map<int, long> treasures;
    8: int borderStyle;
    9: int hero;
    10: int background;
    11: set<int> borders;
    12: set<int> heroes;
    13: set<int> backgrounds;
    14: bool disableTooltips;
    15: set<TMQQuestData> quests;
    16: map<long, map<int, int>> weapons;
}

struct TTimeFrame {
    1: long startTime;
    2: long endTime;
    3: set<int> daysOfWeek;
}

struct TBotConfigInfo {
    1: long id
    2: long bankId
    3: set<long> allowedGames
    4: bool active;
    5: string username;
    6: string password;
    7: string mqNickname;
    8: long mmcBalance;
    9: long mqcBalance;
    10: set<TTimeFrame> timeFrames;
    11: set<long> allowedBankIds
}

struct TMQDataWrapper {
    1: optional TMQData data;
}

struct TJackpotWin {
    1: long id;
    2: long jackpotId;
    3: long accountId;
    4: long gameSessionId;
    5: long gameId;
    6: long systemId;
    7: long bankId;
    8: double contribution;
    9: long amount;
    10: long amountInPlayerCurrency;
    11: long betAmount;
    12: long betAmountInPlayerCurrency;
    13: string baseCurrency;
    14: long baseAmount;
    15: long date;
}

struct MQClassData {
    1: long fileLength;
    2: long lastModified;
    3: string name;
    4: string hash;
}

struct HashAuditInfo {
    1: string gameName;
    2: long admCode;
    3: string fileName;
    4: string hash;
}

struct TCrashGameSetting {
    1: long bankId;
    2: string currencyCode;
    3: int maxRoomPlayers;
    4: int maxMultiplier;
    5: long maxPlayerProfitInRound;
    6: long totalPlayersProfitInRound;
    7: long minStake;
    8: long maxStake;
    9: bool sendRealBetWin;
}

struct TLeaderboardEntry {
    1: long accountId;
    2: long bankId;
    3: string nickname;
    4: long rank;
    5: long rankUpdateTime;
}

struct TRMSPlayer {
    1: int serverId;
    2: string nickname;
    3: bool isOwner;
    4: string sessionId;
    5: int seatNr;
}
struct TRMSRoom {
    1: long roomId;
    2: int serverId;
    3: bool isActive;
    4: bool isBattleground;
    5: bool isPrivate;
    6: long buyInStake;
    7: string currency;
    8: long gameId;
    9: string gameName;
    10: list<TRMSPlayer> players;
}

struct TBGCreateRoomRequest {
    1: string ownerUsername;
    2: int gameId;
    3: long bankId;
    4: long buyIn;
    5: string currency;
    6: string domainUrl;
    7: string ownerExternalId;
}

struct TBGGetPrivateRoomIdRequest {
    1: string ownerUsername;
    2: int gameId;
    3: long bankId;
    4: long buyIn;
    5: string currency;
    6: long ownerAccountId;
    7: string ownerExternalId;
}

struct TBGGetPrivateRoomIdResult {
    1: int code;
    2: string result;
    3: string message;
    4: string privateRoomId;
    5: list<string> activePrivateRooms;
}

struct TBGDeactivateRoomRequest {
    1: string ownerUsername;
    2: long ownerAccountId;
    3: string roomId;
    4: string ownerExternalId;
}

enum TBGStatus {
    invited,
    accepted,
    rejected,
    kicked,
    loading,
    ready,
    waiting,
    playing
}

struct TBGPlayer {
    1: string nickname;
    2: long accountId;
    3: string externalId;
    4: TBGStatus status;
}

struct TBGUpdateRoomRequest {
    1: string privateRoomId;
    2: list<TBGPlayer> players;
    3: int bankId;
}

struct TBGUpdateRoomResult {
    1: int code;
    2: string message;
    3: string privateRoomId;
    4: list<TBGPlayer> players;
}

enum TBGFStatus {
    sent,
    received,
    friend,
    rejected,
    blocked
}

struct TBGFriend {
    1: string nickname;
    2: string externalId;
    3: TBGFStatus status;
}

struct TBGUpdateFriendsRequest {
    1: string nickname;
    2: string externalId;
    3: list<TBGFriend> friends;
}

struct TBGUpdateFriendsResult {
    1: int code;
    2: string message;
    3: string nickname;
    4: string externalId;
    5: list<TBGFriend> friends;
}

enum TBGOStatus {
    online,
    offline
}

struct TBGOnlinePlayer {
    1: string nickname;
    2: string externalId;
    3: TBGOStatus status;
}

struct TBGUpdateOnlinePlayersRequest {
    1: list<TBGOnlinePlayer> onlinePlayers;
}

struct TBGUpdateOnlinePlayersResult {
    1: int code;
    2: string message;
    3: list<TBGOnlinePlayer> onlinePlayers;
}

struct TBGDeactivateResult {
    1: int code;
    2: string message;
}

struct TBGPrivateRoomInfoResult {
    1: string currency;
    2: int gameId;
    3: long bankId;
    4: long buyIn;
    5: int serverId;
}

struct TActiveBattlegroundRoundInfoMQ {
    1: long roundId
    2: long gameId
    3: long buyIn
    4: int participants
    5: double rake
}

struct TRoomInfo {
    1: bool privateRoom
    2: string privateRoomId
}

enum JBoolean {
    NullValue,
    TrueValue,
    FalseValue
}

service GameServerThriftService {
    string ping(1: int gameServerId) throws (1: CommonException e);

    InternalServerInfo syncInternalServerInfo(1: set<InternalServerInfo> infos) throws (1: CommonException e);

    void refreshConfig(1: string configName, 2: string objectId) throws (1: CommonException e);

    void invalidateJackpot3Key(1: Jackpot3Key key, 2: bool needFlush) throws (1: CommonException e);

    void invalidateCombinedJackpot(1: long jackpotId, 2: bool needFlush) throws (1: CommonException e);

    map<int, bool> getServersStatuses() throws (1: CommonException e);

    void changeServersStatuses(1: map<int, bool> serversStatuses) throws (1: CommonException e);

    bool unlock(1: string lockManagerName, 2: string lockId, 3: long lockTime);

    void changeMassAwardStatus(1: long massAwardId, 2: string status) throws (1: CommonException e);

    void deleteMassAward(1: long massAwardId) throws (1: CommonException e);

    void sendCdrChatMessage(1: long messageId, 2: long playerId, 3: long bankId, 4: bool realMode, 5: int roomId,
    6: string messageText) throws (1: CommonException e);

    void sendCdrEnterMessage(1: long playerId, 2: long playerKey, 3: string nickName, 4: long bankId, 5: bool realMode)
    throws (1: CommonException e);

    void processCommonDrawRoundResult(1: long roundId) throws (1: CommonException e);

    void saveGDA(1: GameDataAccess gda, 2: string key) throws (1: CommonException e);

    void invalidateTransactionDataAndDBLinkCaches(1: string lockId, 2: long gameSessionId) throws (1: CommonException e);

    void invalidateDBLinkCache(1: long gameSessionId);

    void invalidatePromoCampaignCache(1: long promoCampaignId) throws (1: CommonException e);

    void notifyPromoCampaignCreated(1: long promoCampaignId) throws (1: CommonException e);

    void notifyPromoCampaignStatusChanged(1: long promoCampaignId, 2: string oldStatus, 3: string newStatus)
    throws (1: CommonException e);

    void sendPromoNotifications(1: string sessionId, 2: long campaignId, 3: set<PromoNotificationType> notificationsTypes)
    throws (1: CommonException e);

    void notifySessionClosed(1: string sessionId) throws (1: CommonException e);

    void initializeFRBProfile(1: long bankId, 2: string currencyCode);

    void initializeFRBProfileForGame(1: long bankId, 2: string currencyCode, 3: long gameId);

    TDetailedPlayerInfo2 getDetailedPlayerInfo(1: string sessionId, 2: long gameId, 3: string mode)
        throws (1: CommonException e);

    TDetailedPlayerInfo2 getDetailedPlayerInfo2(1: string sessionId, 2: long gameId, 3: string mode,
                4: long bonusId, 5: long tournamentId)
            throws (1: CommonException e);

    bool touchSession(1: string sessionId);

    TSitInResult sitIn(1: string sessionId, 2: long gameId, 3: string mode, 4: string lang, 6: long bonusId,
                            7: long oldGameSessionId, 8: long oldRoundId, 9: long roomId, 10: int betNumber,
                            11: long tournamentId, 12: string nickname)
            throws (1: CommonException e);

    map<long, TAddWinResult> addBatchWin(1: long roomId, 2: long roundId, 3: long gameId, 4: set<TAddWinRequest> addWinRequest,
                                         5: long timeoutInMillis);

    string getBatchAddWinStatus(1: long roomId, 2: long roundId);

    //deprecated, use getPaymentOperationStatus. May be removed after deploy MQ release 1.8, GS release 1.88.0 to all clusters
    string getPendingWinStatusForPlayer(1: long accountId, 2: long roomId, 3: long roundId, 4: string sessionId, 5: long gameSessionId,
                                              6: long gameId, 7: long bankId);

    //deprecated, use getPaymentOperationStatus2
    string getPaymentOperationStatus(1: long accountId, 2: long roomId, 3: long roundId, 4: string sessionId, 5: long gameSessionId,
                                              6: long gameId, 7: long bankId);

    string getPaymentOperationStatus2(1: long accountId, 2: long roomId, 3: long roundId, 4: string sessionId, 5: long gameSessionId,
                                              6: long gameId, 7: long bankId, 8: JBoolean isBet, 9: int betNumber);

    TAddWinResult addWinWithSitOut(1: string sessionId, 2: long gameSessionId, 3: long cents, 4: long returnedBet,
                                         5: long roundId, 6: long roomId, 7: long accountId, 8: TRoundInfoResult roundInfo,
                                         9: map<long, double> contributions, 10: bool sitOut);

    #deprecated, replaced to addWinWithSitOut()
    TAddWinResult addWin(1: string sessionId, 2: long gameSessionId, 3: long cents, 4: long returnedBet,
                                         5: long roundId, 6: long roomId, 7: long accountId, 8: TRoundInfoResult roundInfo,
                                         9: map<long, double> contributions);

    #deprecated, replaced to addWinWithSitOut()
    TSitOutResult sitOut(1: string sessionId, 2: long gameSessionId, 3: long cents, 4: long returnedBet, 5: long roundId,
                              6: long roomId, 7: long accountId, 8: TRoundInfoResult roundInfo, 9: map<long, double> contributions);

    TBuyInResult buyIn3(1: string sessionId, 2: long cents, 3: long gameSessionId, 4: long roomId, 5: int betNumber,
                    6: long tournamentId, 7: long currentBalance, 8: long roundId);

    TBuyInResult checkBuyIn(1: string sessionId, 2: long cents, 3: long accountId, 4: long gameSessionId,
                5: long roomId, 6: int betNumber);

    TRefundResult refundBuyIn(1: string sessionId, 2: long cents, 3: long accountId, 4: long gameSessionId,
                5: long roomId, 6: int betNumber);

    TBGUpdateRoomResult updatePlayersStatusInPrivateRoom(1: TBGUpdateRoomRequest request);

    bool invitePlayersToPrivateRoom(1: list<TBGPlayer> players, 2: string privateRoomId);

    list<TBGFriend> getFriends(1: TBGFriend friend);

    list<TBGOnlinePlayer> getOnlineStatus(1: list<TBGOnlinePlayer> onlinePlayers);

    bool finishGameSessionAndMakeSitOut(1: string sid, 2: string privateRoomId);

    bool closeGameSession(1: string sessionId, 2: long accountId, 3: long gameSessionId);

    bool closeGameSessionV2(1: string sessionId, 2: long accountId, 3: long gameSessionId, 4: long buyIn);

    TStartNewRoundResult startNewRound(1: string sessionId, 2: long accountId, 3: long gameSessionId, 4: long roomId);

    TStartNewRoundResult startNewRound2(1: string sessionId, 2: long accountId, 3: long gameSessionId, 4: long roomId,
                5: long roomRoundId, 6: long roundStartDate, 7: bool battlegroundRoom, 8: long stakeOrBuyInAmount);

    map<long, TStartNewRoundResult> startNewRoundForManyPlayers(1: list<TRoundPlayer> roundPlayers, 2: long roomId, 3: long roomRoundId,
                4: long roundStartDate, 5: bool battlegroundRoom, 6: long stakeOrBuyInAmount);

    set<TCurrencyRate> updateCurrencyRates(1: set<TCurrencyRate> unknownRates);

    void leaveMultiPlayerLobby(1: string sessionId);

    long getBalance(1: string sessionId, 2: string mode);

    TCloseFRBonusResult closeFRBonusAndSession(1: long accountId, 2: string sessionId, 3: long gameSessionId,
                4: long gameId, 5: long bonusId, 6: long winSum);

    TCashBonus saveCashBonusRoundResult(1: long accountId, 2: string sessionId, 3: long gameSessionId, 4: long bonusId,
                5: long balance, 6: long betSum, 7: TMQData data, 8: TRoundInfoResult roundInfo, 9: long roundId);

    TSitOutCashBonusSessionResult sitOutCashBonusSession(1: long accountId, 2: string sessionId, 3: long gameSessionId,
                4: long bonusId, 5: long balance, 6: long betSum, 7: TMQData data, 8: TRoundInfoResult roundInfo,
                9: long roundId);

    TTournamentInfo saveTournamentRoundResult(1: long accountId, 2: string sessionId, 3: long gameSessionId,
                4: long tournamentId, 5: long balance, 6: TMQData data, 7: TRoundInfoResult result,
                8: long roundId);

    TSitOutTournamentSessionResult sitOutTournamentSession(1: long accountId, 2: string sessionId, 3: long gameSessionId,
                4: long tournamentId, 5: long balance, 6: TMQData data, 7: TRoundInfoResult roundInfo,
                8: long roundId);

    void registerRemoteStatusListener(1: string thriftHost, 2: int thiftPort);

    void unregisterRemoteStatusListener(1: string thriftHost, 2: int thiftPort);

    void notifyRemoteStatusListener(1: int gameServerId, 2: bool status);

    set<GameServerInfo> getGameServersInfo();

    void invalidateBonusClient(1: long bankId);

    void invalidateFrBonusClient(1: long bankId);

    void invalidateFrBonusWinManager(1: long bankId);

    void invalidateWalletManager(1: long bankId);

    void invalidateLocalBaseGameInfo(1: long bankId, 2: long gameId, 3: string currencyCode) throws (1: CommonException e);

    void invalidateAllBaseGameInfo(1: long bankId);

    void invalidateTournamentPlaceholdersCache(1: long promoId);

    string getPlayerCurrency(1: long accountId);

    bool savePlayerBetForFRB(1: string sessionId, 2: long gameSessionId, 3: long roundId,  4: long accountId,
    5: TRoundInfoResult roundInfo);

    set<BankName> getBankNames(1: set<long> bankIds);

    map<long, string> getExternalAccountIds(1: list<long> accountIds);

    void storeLeaderboardResult(1: TLeaderboardResult result);

    void saveJackpotNotification(1: TJackpotNotification notification);

    void markPlayerNotified(1: long bankId, 2: long gameId, 3: string currencyCode, 4: long betId, 5: long accountId);

    void storeMQData(1: TMQData data);

    TMQDataWrapper getMQData(1: long accountId, 2: long gameId);

    void invalidateJackpotCache(1: string key);

    void addEmulatedResponse(1: string type, 2: string key, 3: string value);

    void removeEmulatedResponse(1: string key);

    void updateStubBalance(1: long accountId, 2: long balance);

    void updateStubBalanceByExternalUserId(1: string externalUserId, 2: long balance);

    void sendUnjWinNotification(1: map<long, TJackpotWin> jackpotWins, 2: map<string, long> unjIds, 3: long mainUnjId);

    void addMQReservedNicknames(1: string region, 2: long owner, 3: set<string> nicknames);

    void removeMQReservedNicknames(1: string region, 2: long owner, 3: set<string> nicknames);

    void sendPlayerTournamentStateChanged(1: string sessionId, 2: long tournamentId, 3: bool cannotJoin, 4: bool joined);

    void sendBalanceUpdated(1: string sessionId, 2: long balance);

    list<HashAuditInfo> getAuditInfo(1: string email, 2: long subcacinoId, 3: string rootPath);

    void addFeatureToLocalDemoStand(1: long sessionId, 2: int gameId, 3: string demoParams);

    set<TCrashGameSetting> getCrashGameSettings(1: set<long> bankIds, 2: int gameId);

    void sendLeaderboardEntry(1: long promoId, 2: TLeaderboardEntry entry);

    void notifyPrivateRoomWasDeactivated(1: string privateRoomId, 2: string reason, 3: long bankId);

    bool pushOnlineRoomsPlayers (1: list<TRMSRoom> roomsPlayers);
}

service MQThriftService {
//    void ping(1: string message);

//    void sendBonusStatus(1: long bonusId, 2: string status, 3: long accountId);

    void sendTournamentEnded(1: long tournamentId, 2: string oldState, 3: string newState);

//    set<MQClassData> getCheckSums(1: set<long> gameIds);

//    void sitOut(1: long accountId, 2: long gameSessionId);

//    string getBGPrivateRoomUrl(1: TBGCreateRoomRequest request);

//    TBGPrivateRoomInfoResult getPrivateRoomInfo(1: string privateRoomId);

//    TBGGetPrivateRoomIdResult getPrivateRoomId(1: TBGGetPrivateRoomIdRequest request);

//    TBGDeactivateResult deactivate(1: TBGDeactivateRoomRequest request);

//    TBGUpdateRoomResult updatePlayersStatusInPrivateRoom(1: TBGUpdateRoomRequest request);

//    list<TActiveBattlegroundRoundInfoMQ> getActiveBattlegroundRoundsInfo(1: long bankId);
//
//    TBGUpdateRoomResult updatePlayersStatusInPrivateRoomStatusTransitionLimited(1: TBGUpdateRoomRequest request);

//    TBGUpdateFriendsResult updateFriends(1: TBGUpdateFriendsRequest request);

//    TBGUpdateOnlinePlayersResult updateOnlinePlayers(1: TBGUpdateOnlinePlayersRequest request);

//    list<TActiveBattlegroundRoundInfoMQ> getActiveBattlegroundRoundsInfoByGameId(1: long bankId, 2: long gameId);

//    set<long> getParticipantAccountIdsInRound(1: long accountId, 2: long gameSession);

//    TRoomInfo loadCurrentBattlegroundRoomInfoForPlayer(1: long accountId, 2: long gameSession);

    set<long> getParticipantAccountIdsInRound(1: long accountId, 2: long gameSession);

    TRoomInfo loadCurrentBattlegroundRoomInfoForPlayer(1: long accountId, 2: long gameSession);

    void enableBotService(1: bool enable);

    bool isBotServiceEnabled();

    list<TBotConfigInfo> getAllBotConfigInfos();

    TBotConfigInfo getBotConfigInfo(1: long botId);

    TBotConfigInfo getBotConfigInfoByUserName(1: string username);

    TBotConfigInfo getBotConfigInfoByMqNickName(1: string mqNickname);

    list<TBotConfigInfo> upsertBotConfigInfo(1: list<TBotConfigInfo> botConfigInfo);

    list<TBotConfigInfo> removeBotConfigInfo(1: list<long> botId);
}


