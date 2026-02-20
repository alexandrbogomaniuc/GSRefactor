package com.betsoft.casino.bots;

import com.dgphoenix.casino.common.util.string.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgsParser {

    private int botsCount;
    private int botsPerRoom = 1;
    private int bankId = 271;
    private int gameId = 779;
    private long roomId = -1;
    private String mode = "real";
    private String nickname = "Bot";
    private String emailPrefix = "";
    private String emailDomain = "@maxquest.io";
    private String pass = "pass";
    private String url;
    private String tokenGenerationUrl;
    private String googleAuthUrl;
    private String botPrefix = "";
    private String botStrategy = "";
    private int specialPaidWeaponId = -2;
    private int requestedBetLevel = 1;
    private boolean allowedUseDroppedSW = false;
    private long requestedByInAmount;
    private List<Long> customRoomIds = new ArrayList<>();
    private long delayTimeBetweenSeatIn = 200;
    private boolean needTryRandomExitInWaitState = false;
    private int numberRoundsBeforeRestart = 4000;
    private String astroParams = "";
    private String requestedEnemyIds = "";

    public void parse(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String key = arg.substring(2, arg.indexOf("="));
                String value = arg.substring(arg.indexOf("=") + 1);
                System.out.println(key + "=" + value);
                updateArgValue(key, value);
            }
        }
        if (url == null || botsCount == 0) {
            throw new IllegalArgumentException("Missing required params");
        }
    }

    private void updateArgValue(String key, String value) {
        switch (key) {
            case "astroParams":
                astroParams = value;
                break;
            case "numberRoundsBeforeRestart":
                numberRoundsBeforeRestart = Integer.parseInt(value);
                break;
            case "requestedByInAmount":
                requestedByInAmount = Integer.parseInt(value);
                break;
            case "botsCount":
                botsCount = Integer.parseInt(value);
                break;
            case "botsPerRoom":
                botsPerRoom = Integer.parseInt(value);
                if(botsPerRoom < 1 || botsPerRoom > 6) {
                    throw new RuntimeException("Invalid argument: botsPerRoom=" + botsPerRoom);
                }
                break;
            case "bankId":
                bankId = Integer.parseInt(value);
                break;
            case "gameId":
                gameId = Integer.parseInt(value);
                break;
            case "mode":
                mode = value;
                break;
            case "nickname":
                nickname = value;
                break;
            case "emailPrefix":
                emailPrefix = value;
                break;
            case "emailDomain":
                if (value.contains("@")) {
                    emailDomain = value;
                } else{
                    emailDomain = "@" + value;
                }
                break;
            case "url":
                url = value;
                break;
            case "tokenGenerationUrl":
                tokenGenerationUrl = value;
                break;
            case "googleAuthUrl":
                googleAuthUrl = value;
                break;
            case "roomId":
                roomId = Long.parseLong(value);
                break;
            case "pass":
                pass = value;
                break;
            case "botPrefix":
                botPrefix = value;
                break;
            case "botStrategy":
                botStrategy = value;
                break;
            case "specialPaidWeaponId":
                specialPaidWeaponId = Integer.parseInt(value);
                break;
            case "requestedBetLevel":
                requestedBetLevel = Integer.parseInt(value);
                break;
            case "delayTimeBetweenSeatIn":
                delayTimeBetweenSeatIn = Integer.parseInt(value);
                break;
            case "allowedUseDroppedSW" :
                allowedUseDroppedSW = Boolean.parseBoolean(value);
                break;
            case "needTryRandomExitInWaitState" :
                needTryRandomExitInWaitState = Boolean.parseBoolean(value);
                break;
            case "setRequestedRoomIds" :
                if (!StringUtils.isTrimmedEmpty(value)) {
                    customRoomIds = Stream.of(value.split(",")).map(s -> Long.valueOf(s.trim())).collect(Collectors.toList());
                }
                break;
            case "requestedEnemyIds" :
                requestedEnemyIds = value;
                break;
            default:
                break;
        }
    }

    public String getAstroParams() {
        return astroParams;
    }

    public int getNumberRoundsBeforeRestart() {
        return numberRoundsBeforeRestart;
    }

    public long getDelayTimeBetweenSeatIn() {
        return delayTimeBetweenSeatIn;
    }

    public long getRequestedByInAmount() {
        return requestedByInAmount;
    }

    public int getRequestedBetLevel() {
        return requestedBetLevel;
    }

    public void setRequestedBetLevel(int requestedBetLevel) {
        this.requestedBetLevel = requestedBetLevel;
    }

    public int getSpecialPaidWeaponId() {
        return specialPaidWeaponId;
    }

    public void setSpecialPaidWeaponId(int specialPaidWeaponId) {
        this.specialPaidWeaponId = specialPaidWeaponId;
    }

    public String getBotStrategy() {
        return botStrategy;
    }

    public void setBotStrategy(String botStrategy) {
        this.botStrategy = botStrategy;
    }

    public int getBotsCount() {
        return botsCount;
    }

    public int getBankId() {
        return bankId;
    }

    public int getGameId() {
        return gameId;
    }

    public String getMode() {
        return mode;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmailPrefix() {
        return emailPrefix;
    }

    public String getEmailDomain() {
        return emailDomain;
    }

    public String getUrl() {
        return url;
    }

    public String getTokenGenerationUrl() {
        return tokenGenerationUrl;
    }

    public String getGoogleAuthUrl() {
        return googleAuthUrl;
    }

    public long getRoomId() {
        return roomId;
    }

    public String getPass() {
        return pass;
    }

    public int getBotsPerRoom() {
        return botsPerRoom;
    }

    public String getBotPrefix() {
        return botPrefix;
    }

    public boolean isAllowedUseDroppedSW() {
        return allowedUseDroppedSW;
    }

    public List<Long> getCustomRoomIds() {
        return customRoomIds;
    }

    public boolean isNeedTryRandomExitInWaitState() {
        return needTryRandomExitInWaitState;
    }

    public void setNeedTryRandomExitInWaitState(boolean needTryRandomExitInWaitState) {
        this.needTryRandomExitInWaitState = needTryRandomExitInWaitState;
    }

    public String getRequestedEnemyIds() {
        return requestedEnemyIds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ArgsParser [");
        sb.append("botsCount=").append(botsCount);
        sb.append(", botsPerRoom=").append(botsPerRoom);
        sb.append(", bankId=").append(bankId);
        sb.append(", gameId=").append(gameId);
        sb.append(", roomId=").append(roomId);
        sb.append(", mode='").append(mode).append('\'');
        sb.append(", nickname='").append(nickname).append('\'');
        sb.append(", emailPrefix='").append(emailPrefix).append('\'');
        sb.append(", emailDomain='").append(emailDomain).append('\'');
        sb.append(", pass='").append(pass).append('\'');
        sb.append(", botPrefix='").append(botPrefix).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", tokenGenerationUrl='").append(tokenGenerationUrl).append('\'');
        sb.append(", googleAuthUrl='").append(googleAuthUrl).append('\'');
        sb.append(", botStrategy='").append(botStrategy).append('\'');
        sb.append(", allowedUseSW='").append(allowedUseDroppedSW).append('\'');
        sb.append(", requestedByInAmount='").append(requestedByInAmount).append('\'');
        sb.append(", customRoomIds='").append(customRoomIds).append('\'');
        sb.append(", customRoomIds='").append(customRoomIds).append('\'');
        sb.append(", needTryRandomExitInWaitState='").append(needTryRandomExitInWaitState).append('\'');
        sb.append(", delayTimeBetweenSeatIn='").append(delayTimeBetweenSeatIn).append('\'');
        sb.append(", astroParams='").append(astroParams).append('\'');
        sb.append(", numberRoundsBeforeRestart='").append(numberRoundsBeforeRestart).append('\'');
        sb.append(", requestedEnemyIds='").append(requestedEnemyIds).append('\'');
        sb.append(']');
        return sb.toString();
    }
}
