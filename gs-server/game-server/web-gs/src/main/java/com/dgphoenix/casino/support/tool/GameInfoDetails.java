package com.dgphoenix.casino.support.tool;

import java.util.ArrayList;
import java.util.List;

public class GameInfoDetails {

    private String currencyCode;
    private List<GameDetails> games = new ArrayList<>();

    public String getCurrencyCode() {
        return currencyCode;
    }

    public List<GameDetails> getGames() {
        return games;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public void addGameDetails(GameDetails gameDetails) {
        this.games.add(gameDetails);
    }
}
