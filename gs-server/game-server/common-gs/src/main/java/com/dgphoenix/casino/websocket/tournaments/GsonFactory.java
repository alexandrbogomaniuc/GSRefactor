package com.abs.casino.websocket.tournaments;

import com.abs.casino.promo.tournaments.messages.Error;
import com.abs.casino.promo.tournaments.messages.*;
import com.abs.casino.promo.tournaments.messages.*;
import com.google.gson.ExclusionStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonFactory {

    private GsonFactory() {
    }

    public static Gson createGson() {
        return createGson(null);
    }

    public static Gson createGson(boolean prettyPrinting) {
        return createGson(null, prettyPrinting);
    }

    private static Gson createGson(ExclusionStrategy deserializationExclusionStrategy, boolean prettyPrinting) {
        GsonClassSerializer typeAdapter = createGsonClassSerializer();

        GsonBuilder builder = new GsonBuilder().registerTypeHierarchyAdapter(Object.class, typeAdapter);
        if (deserializationExclusionStrategy != null) {
            builder.addDeserializationExclusionStrategy(deserializationExclusionStrategy);
        }
        if (prettyPrinting) {
            builder.setPrettyPrinting();
        }
        return builder.create();
    }

    public static Gson createGson(ExclusionStrategy deserializationExclusionStrategy) {
        return createGson(deserializationExclusionStrategy, false);
    }

    private static GsonClassSerializer createGsonClassSerializer() {
        GsonClassSerializer typeAdapter = new GsonClassSerializer();
        typeAdapter.register(EnterLobby.class);
        typeAdapter.register(EnterLobbyResponse.class);
        typeAdapter.register(Error.class);
        typeAdapter.register(GetLeaderboard.class);
        typeAdapter.register(GetTournamentDetails.class);
        typeAdapter.register(JoinTournament.class);
        typeAdapter.register(JoinTournamentResponse.class);
        typeAdapter.register(Leaderboard.class);
        typeAdapter.register(NewTournament.class);
        typeAdapter.register(PlaceInfo.class);
        typeAdapter.register(PrizeInfo.class);
        typeAdapter.register(PrizePlaceInfo.class);
        typeAdapter.register(ShortTournamentInfo.class);
        typeAdapter.register(TournamentDetails.class);
        typeAdapter.register(TournamentStateChanged.class);
        typeAdapter.register(NetworkTournament.class);
        typeAdapter.register(JoinBattleground.class);
        typeAdapter.register(GetBattlegroundHistory.class);
        typeAdapter.register(GetBattlegroundHistoryResponse.class);
        return typeAdapter;
    }
}
