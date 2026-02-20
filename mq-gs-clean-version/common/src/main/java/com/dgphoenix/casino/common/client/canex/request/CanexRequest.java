package com.dgphoenix.casino.common.client.canex.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

import static com.dgphoenix.casino.gs.managers.payment.wallet.CCommonWallet.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CanexRequest {
    @JsonProperty("HASH")
    @SerializedName("HASH")
    private String hash;

    @JsonProperty("TOKEN")
    @SerializedName("TOKEN")
    private String token;

    @JsonProperty("BANKID")
    @SerializedName("BANKID")
    private Long bankId;

    @JsonProperty("USERID")
    @SerializedName("USERID")
    private String userId;

    @JsonProperty("GAMEID")
    @SerializedName("GAMEID")
    private Long gameId;

    @JsonProperty("CLIENTTYPE")
    @SerializedName("CLIENTTYPE")
    private String clientType;

    @JsonProperty("GAMESESSIONID")
    @SerializedName("GAMESESSIONID")
    private String gameSessionId;

    @JsonProperty("ROUNDID")
    @SerializedName("ROUNDID")
    private Long roundId;

    @JsonProperty("REALGAMEROUNDID")
    @SerializedName("REALGAMEROUNDID")
    private Long realGameRoundId;

    @JsonProperty("ISROUNDFINISHED")
    @SerializedName("ISROUNDFINISHED")
    private Boolean isRoundFinished;

    @JsonProperty("CASINOTRANSACTIONID")
    @SerializedName("CASINOTRANSACTIONID")
    private String casinoTransactionId;

    @JsonProperty("WIN")
    @SerializedName("WIN")
    private String win;

    @JsonProperty("REALWIN")
    @SerializedName("REALWIN")
    private Long realWin;

    @JsonProperty("BET")
    @SerializedName("BET")
    private String bet;

    @JsonProperty("REALBET")
    @SerializedName("REALBET")
    private Long realBet;

    @JsonProperty("DEBITTYPE")
    @SerializedName("DEBITTYPE")
    private String debitType;

    @JsonProperty("privateRoomId")
    @SerializedName("privateRoomId")
    private String privateRoomId;

    @JsonProperty("nickname")
    @SerializedName("nickname")
    private String nickname;

    @JsonProperty("externalId")
    @SerializedName("externalId")
    private String externalId;

    @JsonProperty("status")
    @SerializedName("status")
    private String status;

    public CanexRequest(RequestType requestType, Map<String, String> params) {
        hash = params.get("hash");
        switch (requestType) {
            case PLAYER_STATUS:
                privateRoomId = params.get(PARAM_PRIVATE_ROOM_ID);
                nickname = params.get(PARAM_NICKNAME);
                externalId = params.get(PARAM_EXTERNAL_ID);
                status = params.get(PARAM_STATUS);
                break;
            case AUTH:
                token = params.get("token");
                bankId = parseLong(params.get(PARAM_BANKID));
                gameId = parseLong(params.get(PARAM_GAMEID));
                break;
            case BALANCE:
                userId = params.get(PARAM_USERID);
                bankId = parseLong(params.get(PARAM_BANKID));
                break;
            case WAGER:
                userId = params.get(PARAM_USERID);
                bankId = parseLong(params.get(PARAM_BANKID));
                gameId = parseLong(params.get(PARAM_GAMEID));
                gameSessionId = params.get(PARAM_GAMESESSIONID);
                roundId = parseLong(params.get(PARAM_ROUNDID));
                realGameRoundId = parseLong(params.get(PARAM_REALGAMEROUNDID));
                isRoundFinished = parseBoolean(params.get(PARAM_ROUND_FINISHED));
                clientType = params.get("clientType");
                bet = params.get(PARAM_BET);
                realBet = parseLong(params.get(PARAM_REAL_BET));
                win = params.get(PARAM_WIN);
                realWin = parseLong(params.get(PARAM_REAL_WIN));
                debitType = params.get("debitType");
                break;
            case REFUND:
                userId = params.get(PARAM_USERID);
                bankId = parseLong(params.get(PARAM_BANKID));
                casinoTransactionId = params.get(PARAM_CASINOTRANSACTIONID);
                break;
            default:
                throw new IllegalStateException("Unknown request type");
        }
    }


    private Long parseLong(String value) {
        return value != null ? Long.valueOf(value) : null;
    }

    private Boolean parseBoolean(String value) {
        return value != null ? Boolean.valueOf(value) : null;
    }

    public String getHash() {
        return hash;
    }

    public String getToken() {
        return token;
    }

    public Long getBankId() {
        return bankId;
    }

    public String getUserId() {
        return userId;
    }

    public Long getGameId() {
        return gameId;
    }

    public String getClientType() {
        return clientType;
    }

    public String getGameSessionId() {
        return gameSessionId;
    }

    public Long getRoundId() {
        return roundId;
    }

    public Long getRealGameRoundId() {
        return realGameRoundId;
    }

    public Boolean getRoundFinished() {
        return isRoundFinished;
    }

    public String getCasinoTransactionId() {
        return casinoTransactionId;
    }

    public String getWin() {
        return win;
    }

    public Long getRealWin() {
        return realWin;
    }

    public String getBet() {
        return bet;
    }

    public Long getRealBet() {
        return realBet;
    }

    public String getDebitType() {
        return debitType;
    }

    public String getPrivateRoomId() {
        return privateRoomId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getStatus() {
        return status;
    }
}
