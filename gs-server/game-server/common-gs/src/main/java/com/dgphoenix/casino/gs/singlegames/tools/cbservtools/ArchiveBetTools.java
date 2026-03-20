package com.abs.casino.gs.singlegames.tools.cbservtools;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class ArchiveBetTools {
    private static final Logger LOG = Logger.getLogger(ArchiveBetTools.class);
    public static byte[] testArray = new byte[759];

    static {
        byte t = -6;
        for (int i = 0; i < testArray.length; ++i) {
            t++;
            if (t > 5)
                t = -5;
            testArray[i] = t;
        }
    }

    //constants
    //delimeters
    public static final String DELIMETER = "~";
    //game states
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

    //parameters names
    public static final String PARAM_PLAYERSCARD = "PCARD"; //player's cards
    public static final String PARAM_PLAYERSCARD1 = "PCARD1"; //player's cards (first hand or only hand)
    public static final String PARAM_PLAYERSCARD2 = "PCARD2"; //player's cards (second hand)
    public static final String PARAM_PLAYERSCARD3 = "PCARD3"; //player's cards (third hand)
    public static final String PARAM_PLAYERSCARD4 = "PCARD4"; //player's cards (4 hand)
    public static final String PARAM_PLAYERSCARD5 = "PCARD5"; //player's cards (5 hand)
    public static final String PARAM_DEALERSSCARD1 = "DCARD1"; //dealer's cards (first hand or only hand)
    public static final String PARAM_SPLIT = "SPLIT";  //split flag in black jack 0 - no split, 1 - was split
    public static final String PARAM_INSURANCE = "INSURANCE";  //insurance flag in black jack 0 - no ins., 1 - was ins.
    public static final String PARAM_DOUBLELEFT = "DOUBLELEFT"; //double flag for left hand in black jack 0 - no double., 1 - was double
    public static final String PARAM_DOUBLERIGHT = "DOUBLERIGHT"; //double flag for right hand in black jack 0 - no double., 1 - was double
    public static final String PARAM_PAYOUTKIND = "PAYOUTKIND"; //payout type for pokers: -1 - nothing, 8 - jacks or better (or one pair), 0 - royal flush, 1 - straight flush,
    //2- four of a kind, 3 - full house, 4 - flush, 5 - straight, 6 - three of a kind, 7 - two pairs,
    //9 - Wild Royal Flush, 10  - 4 deuces, 11 - five of a kind
    //for carpoker: -1 - nothing, 0 - push, 1 - player wins, 2 - dealer wins, 3 - not qualify
    //for baccatar: 0 - push, 1 - player, 2 - banker
    public static final String PARAM_PAYOUT = "PAYOUT";
    public static final String PARAM_STOPREEL = "STOP_REEL"; //stop position for slots games
    public static final String PARAM_BONUSPOINTS = "BONUSPOINTS"; //number of bonus points (monkey money)
    public static final String PARAM_CHOICE = "CHOICE"; //choice of player in carribean poker 0 - surrender, 1 - bet
    public static final String PARAM_BETID = "BETID"; //bet id
    public static final String PARAM_BETSCOUNT = "BETCOUNT"; //bets count, rounds count for keno
    public static final String PARAM_ACTIVELINES = "ACTLINE"; //active lines count
    public static final String PARAM_NUMBERS = "NUMBERS"; //choised numbers for keno
    public static final String PARAM_ROUND = "R"; //round results for keno. first round - R1, second - R2 etc.
    public static final String PARAM_DOUBLEUPSCOUNT = "DBUPC"; //round results for keno. first round - R1, second - R2 etc.
    public static final String PARAM_DICE = "DICE"; //dice in craps: "first_result second_result"
    public static final String PARAM_ON_OFF_CHIP = "POOC"; //craps
    public static final String PARAM_CHANGE_ON_OFF_CHIP = "PCOOC"; //craps
    public static final String PARAM_BONUSRESULT = "BONRES";
    public static final String PARAM_BONUS = "BONUS";
    public static final String PARAM_SIDEICONS = "SIDE_ICONS"; // single-line slots (triplecrown, etc.)

    //game states
    public static final byte GMD_UNKNOWN = 0;
    public static final byte GMD_PLACEBET = 1;
    public static final byte GMD_HOLDICONS = 2;
    public static final byte GMD_BONUS = 3;
    public static final byte GMD_STARTROUND = 4;
    public static final byte GMD_HIT = 5;
    public static final byte GMD_STAND = 6;
    public static final byte GMD_DOUBLE = 7;
    public static final byte GMD_INSURANCE = 8;
    public static final byte GMD_SPLIT = 9;
    public static final byte GMD_ENDROUND = 10;
    public static final byte GMD_CONTINUEROUND = 11;
    public static final byte GMD_HOLDCARDS = 12;
    public static final byte GMD_DOUBLEUPACCEPTED = 13;
    public static final byte GMD_DOUBLEUPDECLINED = 14;
    public static final byte GMD_BREAKROUND = 15;
    public static final byte GMD_HIT_LEFT = 16;
    public static final byte GMD_STAND_LEFT = 17;
    public static final byte GMD_DOUBLE_LEFT = 18;
    public static final byte GMD_HIT_RIGHT = 19;
    public static final byte GMD_STAND_RIGHT = 20;
    public static final byte GMD_DOUBLE_RIGHT = 21;
    public static final byte GMD_GAME5_FIRST = 22;
    public static final byte GMD_GAME7_FIRST = 23;

    public static final byte GMD_BURN_FIRST_HAND = 24;
    public static final byte GMD_SURRENDER_FIRST_HAND = 25;
    public static final byte GMD_SPLIT_FIRST_HAND = 26;
    public static final byte GMD_INSURANCE_FIRST_HAND = 27;
    public static final byte GMD_HIT_FIRST_HAND = 28;
    public static final byte GMD_STAND_FIRST_HAND = 29;
    public static final byte GMD_DOUBLE_FIRST_HAND = 30;
    public static final byte GMD_HIT_FIRST_HAND_LEFT = 31;
    public static final byte GMD_STAND_FIRST_HAND_LEFT = 32;
    public static final byte GMD_DOUBLE_FIRST_HAND_LEFT = 33;
    public static final byte GMD_HIT_FIRST_HAND_RIGHT = 34;
    public static final byte GMD_STAND_FIRST_HAND_RIGHT = 35;
    public static final byte GMD_DOUBLE_FIRST_HAND_RIGHT = 36;

    public static final byte GMD_BURN_SECOND_HAND = 37;
    public static final byte GMD_SURRENDER_SECOND_HAND = 38;
    public static final byte GMD_SPLIT_SECOND_HAND = 39;
    public static final byte GMD_INSURANCE_SECOND_HAND = 40;
    public static final byte GMD_HIT_SECOND_HAND = 41;
    public static final byte GMD_STAND_SECOND_HAND = 42;
    public static final byte GMD_DOUBLE_SECOND_HAND = 43;
    public static final byte GMD_HIT_SECOND_HAND_LEFT = 44;
    public static final byte GMD_STAND_SECOND_HAND_LEFT = 45;
    public static final byte GMD_DOUBLE_SECOND_HAND_LEFT = 46;
    public static final byte GMD_HIT_SECOND_HAND_RIGHT = 47;
    public static final byte GMD_STAND_SECOND_HAND_RIGHT = 48;
    public static final byte GMD_DOUBLE_SECOND_HAND_RIGHT = 49;

    public static final byte GMD_BURN_THIRD_HAND = 50;
    public static final byte GMD_SURRENDER_THIRD_HAND = 51;
    public static final byte GMD_SPLIT_THIRD_HAND = 52;
    public static final byte GMD_INSURANCE_THIRD_HAND = 53;
    public static final byte GMD_HIT_THIRD_HAND = 54;
    public static final byte GMD_STAND_THIRD_HAND = 55;
    public static final byte GMD_DOUBLE_THIRD_HAND = 56;
    public static final byte GMD_HIT_THIRD_HAND_LEFT = 57;
    public static final byte GMD_STAND_THIRD_HAND_LEFT = 58;
    public static final byte GMD_DOUBLE_THIRD_HAND_LEFT = 59;
    public static final byte GMD_HIT_THIRD_HAND_RIGHT = 60;
    public static final byte GMD_STAND_THIRD_HAND_RIGHT = 61;
    public static final byte GMD_DOUBLE_THIRD_HAND_RIGHT = 62;

    public static final byte GMD_SECONDSTEP = 63;
    public static final byte GMD_WAR_FIRST = 64;

    public static final byte GMD_OPENFIRSTCARD_FIRST = 65;
    public static final byte GMD_OPENSECONDCARD_FIRST = 66;

    public static final byte GMD_CASHOUT = 67;
    public static final byte GMD_HIGHER = 68;
    public static final byte GMD_LOWER = 69;

    public static final byte GMD_PLACEBET_FIRST_HAND = 70;
    public static final byte GMD_PLACEBET_SECOND_HAND = 71;
    public static final byte GMD_PLACEBET_THIRD_HAND = 72;
    public static final byte GMD_SUPER7BET_FIRST_HAND = 73;
    public static final byte GMD_SUPER7BET_SECOND_HAND = 74;
    public static final byte GMD_SUPER7BET_THIRD_HAND = 75;
    public static final byte GMD_WIN_FIRST_HAND = 76;
    public static final byte GMD_WIN_SECOND_HAND = 77;
    public static final byte GMD_WIN_THIRD_HAND = 78;
    public static final byte GMD_WINSUPER_FIRST_HAND = 79;
    public static final byte GMD_WINSUPER_SECOND_HAND = 80;
    public static final byte GMD_WINSUPER_THIRD_HAND = 81;
    public static final byte GMD_WIN_FIRST_HAND_LEFT = 82;
    public static final byte GMD_WIN_SECOND_HAND_LEFT = 83;
    public static final byte GMD_WIN_THIRD_HAND_LEFT = 84;
    public static final byte GMD_WIN_FIRST_HAND_RIGHT = 85;
    public static final byte GMD_WIN_SECOND_HAND_RIGHT = 86;
    public static final byte GMD_WIN_THIRD_HAND_RIGHT = 87;

    public static final byte GMD_WAR_SECOND = 88;
    public static final byte GMD_WAR_THIRD = 89;

    public static final byte GMD_GAME5_SECOND = 90;
    public static final byte GMD_GAME5_THIRD = 91;
    public static final byte GMD_GAME7_SECOND = 92;
    public static final byte GMD_GAME7_THIRD = 93;

    public static final byte GMD_OPENFIRSTCARD_SECOND = 94;
    public static final byte GMD_OPENFIRSTCARD_THIRD = 95;
    public static final byte GMD_OPENSECONDCARD_SECOND = 96;
    public static final byte GMD_OPENSECONDCARD_THIRD = 97;

    public static final byte GMD_BANK_HALF = 98;
    public static final byte GMD_DRAW_AGAIN = 99;

    public static final byte GMD_CALL = 100;
    public static final byte GMD_RAISE = 101;

    public static final byte GMD_FOLD_FIRST_HAND = 102;
    public static final byte GMD_FOLD_SECOND_HAND = 103;
    public static final byte GMD_FOLD_THIRD_HAND = 104;
    public static final byte GMD_RAISE_FIRST_HAND = 105;
    public static final byte GMD_RAISE_SECOND_HAND = 106;
    public static final byte GMD_RAISE_THIRD_HAND = 107;

    public static final String[] NAMES_GMD = {"", //0
            "Place Bet", //1
            "Hold Icons", //2
            "Bonus Round", //3
            "Round Start", //4
            "Hit", //5
            "Stand", //6
            "Double", //7
            "Insurance", //8
            "Split", //9
            "Round End", //10
            "Round Continue", //11
            "Hold Cards", //12
            "Double Up Accepted", //13
            "Double Up Declined", //14
            "Round Break", //15
            "SL - Hit", //16
            "SL - Stand", //17
            "SL - Double", //18
            "SR - Hit", //19
            "SR - Stand", //20
            "SR - Double", //21
            "Game 5 on first hand", //22
            "Game 7 on first hand", //23
            "Burn on first hand", //24
            "Surrender on first hand", //25
            "Split on first hand", //26
            "Insurance on first hand", //27
            "Hit on first hand", //28
            "Stand on first hand", //29
            "Double on first hand", //30
            "SL - Hit on first hand", //31
            "SL - Stand on first hand", //32
            "SL - Double on first hand", //33
            "SR - Hit on first hand", //34
            "SR - Stand on first hand", //35
            "SR - Double on first hand", //36
            "Burn on second hand", //37
            "Surrender on second hand", //38
            "Split on second hand", //39
            "Insurance on second hand", //40
            "Hit on second hand", //41
            "Stand on second hand", //42
            "Double on second hand", //43
            "SL - Hit on second hand", //44
            "SL - Stand on second hand", //45
            "SL - Double on second hand", //46
            "SR - Hit on second hand", //47
            "SR - Stand on second hand", //48
            "SR - Double on second hand", //49
            "Burn on third hand", //50
            "Surrender on third hand", //51
            "Split on third hand", //52
            "Insurance on third hand", //53
            "Hit on third hand", //54
            "Stand on third hand", //55
            "Double on third hand", //56
            "SL - Hit on third hand", //57
            "SL - Stand on third hand", //58
            "SL - Double on third hand", //59
            "SR - Hit on third hand", //60
            "SR - Stand on third hand", //61
            "SR - Double on third hand", //62
            "Second step", //63
            "War on first hand", //64
            "Open First Dealer Card first hand", //65
            "Open Second Dealer Card first hand", //66
            "Cash Out", //67
            "Higher", //68
            "Lower", //69
            "Place Bet on first hand", //70
            "Place Bet on second hand", //71
            "Place Bet on third hand", //72
            "Place Bet on bonus bet first hand", //73
            "Place Bet on bonus bet second hand", //74
            "Place Bet on bonus bet third hand", //75
            "Win on first hand", //76
            "Win on second hand", //77
            "Win on third hand", //78
            "Win on bonus bet first hand", //79
            "Win on bonus bet second hand", //80
            "Win on bonus bet third hand", //81
            "Win on first left hand", //82
            "Win on second left hand", //83
            "Win on third left hand", //84
            "Win on first right hand", //85
            "Win on second right hand", //86
            "Win on third right hand", //87
            "War on second hand", //88
            "War on third hand", //89
            "Game 5 on second hand", //90
            "Game 5 on third hand", //91
            "Game 7 on second hand", //92
            "Game 7 on third hand", //93
            "Open First Dealer Card second hand", //94
            "Open First Dealer Card third hand", //95
            "Open Second Dealer Card second hand", //96
            "Open Second Dealer Card third hand",//97
            "Bank half",//98
            "Draw againg", //99
            "Call",
            "Raise", //101
            "Fold on first hand", //102
            "Fold on second hand", //103
            "Fold on third hand", //104
            "Raise on first hand", //105
            "Raise on second hand", //106
            "Raise on third hand" //107
    };

    public static final int CHUNK_LENGTH = 8 + 1 + 4 + 4 + 4; //21
    public static final String ROUND_ID = "ROUND_ID";

    public static String getGameStateName(int gameStateId) {
        return gameStatesMap.get(gameStateId);
    }

    public static String getGmdName (byte index) {
        try {
            return NAMES_GMD[index];
        } catch (Exception ex) {
            return NAMES_GMD[GMD_UNKNOWN];
        }
    }

    public static byte[] addData (byte[] ar1, Object ob2) {
        if (ob2 == null)
            return ar1;
        byte[] ar2 = (byte[]) ob2;
        int totalSize = (ar1 == null ? 0 : ar1.length) + ar2.length - 1;
        byte[] res = new byte[totalSize];
        if (ar1 != null)
            System.arraycopy (ar1, 0, res, 0, ar1.length);
        System.arraycopy (ar2, 0, res, totalSize - ar2.length + 1, ar2.length - 1);
        return res;
    }

    //pack functions
    public static Vector unpackGameMoves (Object obj1, Object obj2, Object obj3) {
        try {
            byte[] data = addData (null, obj1);
            data = addData (data, obj2);
            data = addData (data, obj3);
            LOG.debug ("ORG: Unpacking data: " + data);
            if (data != null)
                LOG.debug ("ORG: " + Tools.packArray (data));

            if ((data == null) || (data.length < 2) || (data.length % CHUNK_LENGTH != 0))
                return null;

            Vector result = new Vector ();
            for (int i = 0; i < (data.length / CHUNK_LENGTH); ++i) {
                int offset = i * CHUNK_LENGTH;
                //get game state
                byte gameState = data[offset];
                //get time
                byte[] tmp = new byte[8];
                for (int j = 0; j < tmp.length; ++j)
                    tmp[j] = data[offset + 1 + j];
                long lTmp = bytesToLong (tmp);
                String date = CM2Date.parse (new Date (lTmp));
                //get balance
                tmp = new byte[4];
                for (int j = 0; j < tmp.length; ++j)
                    tmp[j] = data[offset + 9 + j];
                double balance = bytesToDouble (tmp);
                //get bet
                for (int j = 0; j < tmp.length; ++j)
                    tmp[j] = data[offset + 13 + j];
                double bet = bytesToDouble (tmp);
                //get win
                for (int j = 0; j < tmp.length; ++j)
                    tmp[j] = data[offset + 17 + j];
                double win = bytesToDouble (tmp);
                result.add (new GameMove (gameState, bet, win, balance, date));
            }
            if (result.size () != 0) {
                GameMove move = (GameMove) result.get (0);
                if (move.getGameState () != ArchiveBetTools.GMD_STARTROUND)
                    result.insertElementAt (new GameMove (GMD_CONTINUEROUND, 0, 0, move.getBalance (), move.getDate ()), 0);
                move = (GameMove) result.get (result.size () - 1);
                if (move.getGameState () != GMD_ENDROUND)
                    result.add (new GameMove (GMD_BREAKROUND, 0, 0, move.getBalance (), move.getDate ()));
            }
            return result;
        } catch (Exception ex) {
            return null;
        }
    }

    public static byte[] addGameMoveDescription (byte[] descr, byte gameState, double bet, double win, double oldBalance, long timeDelta) throws CBGameException {
        try {
            double balance = oldBalance - bet + win;
            int length = descr == null ? CHUNK_LENGTH : descr.length + CHUNK_LENGTH;
            byte[] res = new byte[length];
            if (descr != null)
                System.arraycopy (descr, 0, res, 0, descr.length);
            //write game state
            res[length - CHUNK_LENGTH] = gameState;
            //write time
            byte[] tmp = longToBytes (System.currentTimeMillis () + timeDelta);
            System.arraycopy (tmp, 0, res, length - CHUNK_LENGTH + 1, tmp.length);
            //write balance
            tmp = doubleToBytes (balance);
            System.arraycopy (tmp, 0, res, length - CHUNK_LENGTH + 9, tmp.length);
            //write bet
            tmp = doubleToBytes (bet);
            System.arraycopy (tmp, 0, res, length - CHUNK_LENGTH + 13, tmp.length);
            //write win
            tmp = doubleToBytes (win);
            System.arraycopy (tmp, 0, res, length - CHUNK_LENGTH + 17, tmp.length);

            return res;
        } catch (Exception ex) {
            throw new CBGameException ("Cannot addGameMoveDescription", ex.getMessage ());
        }
    }

    public static long bytesToLong (byte[] b) throws Exception {
        return ((b[0] >= 0 ? b[0] : 256 + b[0]) * 0x1L + (b[1] >= 0 ? b[1] : 256 + b[1]) * 0x100L + (b[2] >= 0 ? b[2] : 256 + b[2]) * 0x10000L + (b[3] >= 0 ? b[3] : 256 + b[3]) * 0x1000000L + (b[4] >= 0 ? b[4] : 256 + b[4]) * 0x100000000L + (b[5] >= 0 ? b[5] : 256 + b[5]) * 0x10000000000L + (b[6] >= 0 ? b[6] : 256 + b[6]) * 0x1000000000000L + (b[7] >= 0 ? b[7] : 256 + b[7]) * 0x100000000000000L);
    }

    public static byte[] longToBytes (long l) throws Exception {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; ++i)
            bytes[i] = (byte) (l >> (i * 8));
        return bytes;
    }

    public static double bytesToDouble (byte[] b) throws Exception {
        int i = ((b[0] >= 0 ? b[0] : 256 + b[0]) * 0x1 + (b[1] >= 0 ? b[1] : 256 + b[1]) * 0x100 + (b[2] >= 0 ? b[2] : 256 + b[2]) * 0x10000 + (b[3] >= 0 ? b[3] : 256 + b[3]) * 0x1000000);
        return 1.0 * i / 100;
    }

    public static byte[] doubleToBytes (double f) throws Exception {
        byte[] bytes = new byte[4];
        int i = (int) (f * 100.0);
        for (int j = 0; j < 4; ++j)
            bytes[j] = (byte) (i >> (j * 8));
        return bytes;
    }

    public static int getRoundNumberByBetID (String gameID, String userID, String betID, String startTime, String endTime) {
        int result = 1;
        try {
//        	com.abs.casino.gs.singlegames.tools.db.IResultList list = com.abs.casino.gs.singlegames.tools.db.DBLink.getInstance ().executeSQL ("pl_GetRoundNo @GameId=" + gameID + ", @AccountId=" + userID + ", @BetArchiveId=" + betID + (startTime == null ? "" : ", @StartTime='" + startTime + "'") + (endTime == null ? "" : ", @EndTime='" + endTime + "'"), com.abs.casino.gs.singlegames.tools.db.CDatabasePool.PLAYERDB_ID);
//            if (list.nextRecord ())
//                result = list.getInteger ("RoundNo");
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return result;
    }
}

/*
cards representation:
99 - joker
0 - ace of spades   13 - ace of hearts  26 - ace of clubs(+) 39 - ace of diamonds
1 - 2    		    14 - 2              27 - 2               40 - 2
2 - 3               15 - 3              28 - 3               41 - 3
3 - 4               16 - 4              29 - 4               42 - 4
4 - 5               17 - 5              30 - 5               43 - 5
5 - 6               18 - 6              31 - 6               44 - 6
6 - 7               19 - 7              32 - 7               45 - 7
7 - 8               20 - 8              33 - 8               46 - 8
8 - 9               21 - 9              34 - 9               47 - 9
9 - 10              22 - 10             35 - 10              48 - 10
10 - J              23 - J              36 - J               49 - J
11 - Q              24 - Q              37 - Q               50 - Q
12 - K              25 - K              38 - K               51 - K
*/
