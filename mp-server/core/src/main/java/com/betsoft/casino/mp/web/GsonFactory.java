package com.betsoft.casino.mp.web;

import com.betsoft.casino.mp.maxcrashgame.model.CrashRoundInfo;
import com.betsoft.casino.mp.transport.Error;
import com.betsoft.casino.mp.transport.*;
import com.betsoft.casino.utils.GsonClassSerializer;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonFactory {

    public static Gson createGson() {
        return createGson(null);
    }

    public static Gson createGson(ExclusionStrategy deserializationExclusionStrategy) {
        GsonClassSerializer typeAdapter = createGsonClassSerializer();

        GsonBuilder builder = new GsonBuilder().registerTypeHierarchyAdapter(Object.class, typeAdapter);
        if (deserializationExclusionStrategy != null) {
            builder.addDeserializationExclusionStrategy(deserializationExclusionStrategy);
        }

        return builder.create();
    }

    private static GsonClassSerializer createGsonClassSerializer() {
        GsonClassSerializer typeAdapter = new GsonClassSerializer();

        typeAdapter.register(Error.class);
        typeAdapter.register(EnterLobby.class);
        typeAdapter.register(EnterLobbyResponse.class);
        typeAdapter.register(GetRoomInfo.class);
        typeAdapter.register(GetRoomInfoResponse.class);
        typeAdapter.register(Seat.class);
        typeAdapter.register(Room.class);
        typeAdapter.register(Weapon.class);
        typeAdapter.register(Enemy.class);
        typeAdapter.register(Ok.class);
        typeAdapter.register(GetStartGameUrl.class);
        typeAdapter.register(GetStartGameUrlResponse.class);
        typeAdapter.register(OpenRoom.class);
        typeAdapter.register(CloseRoom.class);
        typeAdapter.register(SitIn.class);
        typeAdapter.register(SitInResponse.class);
        typeAdapter.register(SitOut.class);
        typeAdapter.register(SitOutResponse.class);
        typeAdapter.register(FullGameInfo.class);
        typeAdapter.register(RoomEnemy.class);
        typeAdapter.register(GameStateChanged.class);
        typeAdapter.register(EnemyMove.class);
        typeAdapter.register(EnemiesMoved.class);
        typeAdapter.register(NewEnemy.class);
        typeAdapter.register(BuyIn.class);
        typeAdapter.register(BuyInResponse.class);
        typeAdapter.register(Shot.class);
        typeAdapter.register(Hit.class);
        typeAdapter.register(Miss.class);
        typeAdapter.register(ShotResponse.class);
        typeAdapter.register(EnemyDestroyed.class);
        typeAdapter.register(GetFullGameInfo.class);
        typeAdapter.register(RoundResult.class);
        typeAdapter.register(SpawnEnemy.class);
        typeAdapter.register(EndRound.class);
        typeAdapter.register(Award.class);
        typeAdapter.register(Awards.class);
        typeAdapter.register(ChangeMap.class);
        typeAdapter.register(BalanceUpdated.class);
        typeAdapter.register(ChangeNickname.class);
        typeAdapter.register(ChangeAvatar.class);
        typeAdapter.register(CheckNicknameAvailability.class);
        typeAdapter.register(PurchaseWeaponLootBox.class);
        typeAdapter.register(WeaponLootBox.class);
        typeAdapter.register(FRBEnded.class);
        typeAdapter.register(RefreshBalance.class);
        typeAdapter.register(SwitchWeapon.class);
        typeAdapter.register(WeaponSwitched.class);
        typeAdapter.register(Weapons.class);
        typeAdapter.register(NewEnemies.class);
        typeAdapter.register(SyncLobby.class);
        typeAdapter.register(ShortRoomInfo.class);
        typeAdapter.register(GetLobbyTime.class);
        typeAdapter.register(UpdateTrajectories.class);
        typeAdapter.register(CloseRoundResults.class);
        typeAdapter.register(Stats.class);
        typeAdapter.register(LevelUp.class);
        typeAdapter.register(CloseRoundResultNotification.class);
        typeAdapter.register(RoundFinishSoon.class);
        typeAdapter.register(ChangeToolTips.class);
        typeAdapter.register(GetQuests.class);
        typeAdapter.register(CollectQuest.class);
        typeAdapter.register(NewQuest.class);
        typeAdapter.register(UpdateQuest.class);
        typeAdapter.register(RemoveQuest.class);
        typeAdapter.register(MineCoordinates.class);
        typeAdapter.register(GetWeapons.class);
        typeAdapter.register(NewTreasure.class);
        typeAdapter.register(MinePlace.class);
        typeAdapter.register(AddFreeShotsToQueue.class);
        typeAdapter.register(SeatWinForQuest.class);
        typeAdapter.register(ReBuy.class);
        typeAdapter.register(BetLevel.class);
        typeAdapter.register(BetLevelResponse.class);
        typeAdapter.register(Bullet.class);
        typeAdapter.register(BulletClear.class);
        typeAdapter.register(BulletResponse.class);
        typeAdapter.register(BulletClearResponse.class);
        typeAdapter.register(ChangeEnemyMode.class);
        typeAdapter.register(JoinBattleground.class);
        typeAdapter.register(JoinBattlegroundResponse.class);
        typeAdapter.register(GetBattlegroundStartGameUrl.class);
        typeAdapter.register(ConfirmBattlegroundBuyIn.class);
        typeAdapter.register(CancelBattlegroundRound.class);
        typeAdapter.register(UpdateWeaponPaidMultiplier.class);
        typeAdapter.register(UpdateWeaponPaidMultiplierResponse.class);
        typeAdapter.register(CrashBet.class);
        typeAdapter.register(CrashBets.class);
        typeAdapter.register(CrashBetResponse.class);
        typeAdapter.register(CrashStateInfo.class);
        typeAdapter.register(CrashCancelBet.class);
        typeAdapter.register(CrashCancelBetResponse.class);
        typeAdapter.register(CrashCancelAllBets.class);
        typeAdapter.register(CrashCancelAutoEject.class);
        typeAdapter.register(CrashCancelAutoEjectResponse.class);
        typeAdapter.register(CrashChangeAutoEject.class);
        typeAdapter.register(CrashChangeAutoEjectResponse.class);
        typeAdapter.register(CrashGameInfo.class);
        typeAdapter.register(CrashRoundInfo.class);
        typeAdapter.register(CrashBetInfo.class);
        typeAdapter.register(BattlegroundScoreBoard.class);
        typeAdapter.register(BattlegroundRoundResult.class);
        typeAdapter.register(BattleScoreInfo.class);
        typeAdapter.register(KingOfHillChanged.class);
        typeAdapter.register(GetPrivateBattlegroundStartGameUrl.class);
        typeAdapter.register(StartBattlegroundPrivateRoom.class);
        typeAdapter.register(RoomManagerChanged.class);
        typeAdapter.register(BuyInConfirmedSeats.class);
        typeAdapter.register(CrashAllBetsResponse.class);
        typeAdapter.register(ReBuyResponse.class);
        typeAdapter.register(CheckPendingOperationStatus.class);
        typeAdapter.register(PendingOperationStatus.class);
        typeAdapter.register(CrashAllBetsRejectedDetailedResponse.class);
        typeAdapter.register(CrashAllBetsRejectedResponse.class);
        typeAdapter.register(Kick.class);
        typeAdapter.register(KickResponse.class);
        typeAdapter.register(CancelKick.class);
        typeAdapter.register(CancelKickResponse.class);
        typeAdapter.register(PrivateRoomInvite.class);
        typeAdapter.register(PrivateRoomInviteResponse.class);
        typeAdapter.register(RoomWasOpened.class);
        typeAdapter.register(Observer.class);
        typeAdapter.register(ObserverRemoved.class);
        typeAdapter.register(Latency.class);
        typeAdapter.register(FinishGameSession.class);
        typeAdapter.register(FinishGameSessionResponse.class);
        return typeAdapter;
    }
}
