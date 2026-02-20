package com.dgphoenix.casino.promo.tournaments;

import com.dgphoenix.casino.promo.tournaments.messages.PlaceInfo;

import java.util.List;

public class TournamentLeaderboard {
    private int prizePlaces;
    private List<PlaceInfo> places;
    private PlaceInfo currentPlayer;

    public TournamentLeaderboard(int prizePlaces, List<PlaceInfo> places, PlaceInfo currentPlayer) {
        this.prizePlaces = prizePlaces;
        this.places = places;
        this.currentPlayer = currentPlayer;
    }

    public int getPrizePlaces() {
        return prizePlaces;
    }

    public List<PlaceInfo> getPlaces() {
        return places;
    }

    public PlaceInfo getCurrentPlayer() {
        return currentPlayer;
    }
}
