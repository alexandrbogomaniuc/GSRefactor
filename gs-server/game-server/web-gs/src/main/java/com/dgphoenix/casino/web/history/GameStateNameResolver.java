package com.abs.casino.web.history;

import java.util.HashMap;
import java.util.Map;

/**
 * User: flsh
 * Date: 20.04.2009
 */
public class GameStateNameResolver {
    public static final int GS_ENDROUND = 1;  //end of round
    public static final int GS_STARTGAME = 2;  //USER ENTERS THE GAME
    public static final int GS_ENDGAME = 3;  //USER LEAVES THE GAME
    public static final int GS_NOTFINISHED = 4;  //USER INTERRUPTS THE GAME
    public static final int GS_CONTINUATION = 5;  //USER CONTINUES THE GAME
    public static final int GS_HOLDCARDS = 12;  //hold cards in videopokers
    public static final int GS_PLACEBET = 20; //USER PLACES THE BET
    public static final int GS_HIT = 21; //USER CHOOSES HIT (BLACK JACK)
    public static final int GS_STAND = 22; //USER CHOOSES STAND (BLACK JACK)
    public static final int GS_DOUBLE = 23; //USER CHOOSES DOUBLE (BLACK JACK)
    public static final int GS_SPLIT = 24; //USER CHOOSES SPLIT (BLACK JACK)
    public static final int GS_INSURANCE = 25; //USER CHOOSES INSURANCE (BLACK JACK)
    public static final int GS_SURRENDER = 39; //USER CHOOSES SURRENDER (BLACK JACK)
    public static final int GS_BURN = 46; //USER CHOOSES BURN (BLACK JACK)
    public static final int GS_DOUBLEUP = 43; //bonus round
    public static final int GS_BONUS = 45; //bonus round
    public static final int GS_NEXTDRAW = 13;
    public static final int GS_FIRSTSTEP = 40;
    public static final int GS_SECONDSTEP = 41;

    //game state names
    private static Map<Integer, String> gameStatesMap = new HashMap<Integer, String>();

    static {
        gameStatesMap.put(GS_ENDROUND, "Round Complete");
        gameStatesMap.put(GS_STARTGAME, "Game Start");
        gameStatesMap.put(GS_ENDGAME, "Game End");
        gameStatesMap.put(GS_NOTFINISHED, "Game Not Finished");
        gameStatesMap.put(GS_CONTINUATION, "Game Continuation");
        gameStatesMap.put(11, "NOT USED");
        gameStatesMap.put(GS_HOLDCARDS, "Hold cards");
        gameStatesMap.put(GS_NEXTDRAW, "Next Draw");
        gameStatesMap.put(14, "NOT USED");
        gameStatesMap.put(15, "NOT USED");
        gameStatesMap.put(16, "NOT USED");
        gameStatesMap.put(17, "NOT USED");
        gameStatesMap.put(18, "NOT USED");
        gameStatesMap.put(19, "NOT USED");
        gameStatesMap.put(GS_PLACEBET, "Place bet");
        gameStatesMap.put(GS_HIT, "Hit");
        gameStatesMap.put(GS_STAND, "Stand");
        gameStatesMap.put(GS_DOUBLE, "Double");
        gameStatesMap.put(GS_SPLIT, "Split");
        gameStatesMap.put(GS_INSURANCE, "Insurance");
        gameStatesMap.put(26, "NOT USED");
        gameStatesMap.put(27, "NOT USED");
        gameStatesMap.put(28, "NOT USED");
        gameStatesMap.put(29, "NOT USED");
        gameStatesMap.put(GS_SURRENDER, "Surrender");
        gameStatesMap.put(GS_FIRSTSTEP, "First step");
        gameStatesMap.put(GS_SECONDSTEP, "Second step");
        gameStatesMap.put(42, "NOT USED");
        gameStatesMap.put(GS_DOUBLEUP, "DoubleUp");
        gameStatesMap.put(44, "NOT USED");
        gameStatesMap.put(GS_BONUS, "Bonus round");
        gameStatesMap.put(GS_BURN, "Burn");
    }

    public static String getGameStateName(int gameStateId) {
        return gameStatesMap.get(gameStateId);
    }
}
