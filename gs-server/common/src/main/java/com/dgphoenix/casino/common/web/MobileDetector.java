package com.abs.casino.common.web;

import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.account.PlayerDeviceType;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MobileDetector {

    private final static Logger logger = LogManager.getLogger(MobileDetector.class);

    public static boolean isHtml5Compatible(String userAgent) {
        return MobileDetector.isMobile(userAgent);
    }

    public static boolean isMobile(String userAgent) {
        return isIOSDevice(userAgent) || isAndroidDevice(userAgent) || isWindowsPhoneDevice(userAgent);
    }

    public static boolean isMobile(ClientType clientType) {
        return clientType == ClientType.IOSMOBILE || clientType == ClientType.ANDROID
                || clientType == ClientType.WINDOWSPHONE;
    }

    public static boolean isIOSDevice(String userAgent) {
        if (userAgent == null) {
            return false;
        }
        String s = userAgent.toLowerCase();
        return !s.contains("windows") && (s.contains("iphone") || s.contains("ipad") || s.contains("ipod"));
    }

    public static boolean isAndroidDevice(String userAgent) {
        if (userAgent == null) {
            return false;
        }
        String s = userAgent.toLowerCase();
        return !s.contains("windows") && s.contains("android");
    }

    public static boolean isWindowsPhoneDevice(String userAgent) {
        if (userAgent == null) {
            return false;
        }
        String s = userAgent.toLowerCase();
        return s.contains("windows phone");
    }

    public static PlayerDeviceType getPlayerDeviceType(String userAgent) {
        if (isIOSDevice(userAgent)) {
            return PlayerDeviceType.IOSMOBILE;
        } else if (isAndroidDevice(userAgent)) {
            return PlayerDeviceType.ANDROID;
        } else if (isWindowsPhoneDevice(userAgent)) {
            return PlayerDeviceType.WINDOWSPHONE;
        } else {
            return PlayerDeviceType.PC;
        }
    }

    public static IBaseGameInfo getAlternateGameInfo(IBaseGameInfo gameInfo, String userAgent)
            throws Exception {
        if (BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameInfo.getId()).isSingleGameIdForAllPlatforms()) {
            return gameInfo;
        }

        long bankId = gameInfo.getBankId();
        logger.info("MobileDetector::getAlternateGameInfo: " + " gameInfo.isMobile()=" +
                gameInfo.isMobile() + ", bankId=" + bankId);
        boolean iOS = isIOSDevice(userAgent);
        boolean android = isAndroidDevice(userAgent);
        boolean winPhone = isWindowsPhoneDevice(userAgent);

        String pcDeviceType = gameInfo.getProperty(PlayerDeviceType.PC.name());
        Long pcGameId = pcDeviceType != null ? Long.parseLong(pcDeviceType) : null;
        String iosDeviceType = gameInfo.getProperty(PlayerDeviceType.IOSMOBILE.name());
        Long iOSGameId = iosDeviceType != null ? Long.parseLong(iosDeviceType) : null;
        String androidDeviceType = gameInfo.getProperty(PlayerDeviceType.ANDROID.name());
        Long androidGameId = androidDeviceType != null ? Long.parseLong(androidDeviceType) : null;

        String winPhoneDeviceType = gameInfo.getProperty(PlayerDeviceType.WINDOWSPHONE.name());
        Long winPhoneGameId = winPhoneDeviceType != null ? Long.parseLong(winPhoneDeviceType) : null;


        PlayerDeviceType playerDeviceType = gameInfo.getPlayerDeviceType();
        logger.info(" name() " + PlayerDeviceType.PC.name() + ", pc= " + pcDeviceType + ", iOS = " + iOS +
                ", android = " + android + ", pcGameId= " + pcGameId + " , winPhone = " + winPhone + ", iOSGameId = " + iOSGameId +
                ", androidGameId = " + androidGameId + ", winPhoneGameId =  " + winPhoneGameId + ", gameInfo.getPlayerDeviceType()=" + playerDeviceType);
        if (gameInfo.isMobile()) {
            if (iOS) {
                if (playerDeviceType != null && playerDeviceType.equals(PlayerDeviceType.IOSMOBILE)) {
                    return gameInfo;
                }

                if (playerDeviceType != null && playerDeviceType.equals(PlayerDeviceType.ANDROID)) {
                    if (iOSGameId != null) {
                        IBaseGameInfo alternateGameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, iOSGameId,
                                gameInfo.getCurrency());
                        if (alternateGameInfo == null) {
                            logger.error("getAlternateGameInfo [iOSGameId/android]: alternateGameInfo not found, " +
                                    "bankId=" + bankId + ", iOSGameId=" + iOSGameId + ", currency=" +
                                    gameInfo.getCurrency());
                        }
                        return alternateGameInfo;
                    } else {
                        throw new Exception("iOS player tries to open Android gameId and iOS gameId is not found");
                    }
                }

                if (playerDeviceType != null && playerDeviceType.equals(PlayerDeviceType.WINDOWSPHONE)) {
                    if (iOSGameId != null) {
                        IBaseGameInfo alternateGameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, iOSGameId,
                                gameInfo.getCurrency());
                        if (alternateGameInfo == null) {
                            logger.error("getAlternateGameInfo [iOSGameId/winphone]: alternateGameInfo not found, " +
                                    "bankId=" + bankId + ", iOSGameId=" + iOSGameId + ", currency=" +
                                    gameInfo.getCurrency());
                        }
                        return alternateGameInfo;
                    } else {
                        throw new Exception(
                                "iOS player tries to open Windows Phone gameId and iOS gameId is not found");
                    }
                }

            }
            if (android) {
                if (playerDeviceType != null && playerDeviceType.equals(PlayerDeviceType.ANDROID)) {
                    return gameInfo;
                }

                if (playerDeviceType != null && playerDeviceType.equals(PlayerDeviceType.IOSMOBILE)) {
                    if (androidGameId != null) {
                        IBaseGameInfo alternateGameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, androidGameId,
                                gameInfo.getCurrency());
                        if (alternateGameInfo == null) {
                            logger.error("getAlternateGameInfo [androidGameId/ios]: alternateGameInfo not found, " +
                                    "bankId=" + bankId + ", androidGameId=" + androidGameId + ", currency=" +
                                    gameInfo.getCurrency());
                        }
                        return alternateGameInfo;
                    }
                }

                if (playerDeviceType != null && playerDeviceType.equals(PlayerDeviceType.WINDOWSPHONE)) {
                    if (androidGameId != null) {
                        IBaseGameInfo alternateGameInfo = BaseGameCache.getInstance().getGameInfoById(bankId,
                                androidGameId, gameInfo.getCurrency());
                        if (alternateGameInfo == null) {
                            logger.error("getAlternateGameInfo [androidGameId/winphone]: alternateGameInfo not found, " +
                                    "bankId=" + bankId + ", androidGameId=" + androidGameId + ", currency=" +
                                    gameInfo.getCurrency());
                        }
                        return alternateGameInfo;
                    }
                }

            }

            if (winPhone) {
                if (playerDeviceType != null && playerDeviceType.equals(PlayerDeviceType.WINDOWSPHONE)) {
                    return gameInfo;
                }

                if (playerDeviceType != null && (playerDeviceType.equals(
                        PlayerDeviceType.IOSMOBILE) || playerDeviceType.equals(PlayerDeviceType.ANDROID))) {
                    if (winPhoneGameId != null) {
                        return BaseGameCache.getInstance().getGameInfoById(bankId, winPhoneGameId,
                                gameInfo.getCurrency());
                    }
                } else {
                    throw new Exception(
                            "winPhone player tries to open iOS/Android gameId and WinPhone gameId is not found");
                }
            }


            if (pcGameId != null) {
                IBaseGameInfo alternateGameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, pcGameId,
                        gameInfo.getCurrency());
                if (alternateGameInfo == null) {
                    logger.error("getAlternateGameInfo [pcGameId]: alternateGameInfo not found, " +
                            "bankId=" + bankId + ", pcGameId=" + pcGameId + ", currency=" +
                            gameInfo.getCurrency());
                }
                return alternateGameInfo;
            } else {
                throw new Exception("PC version of game is not found and PC player tries to open mobile game");
            }
        } else {
            if (iOS) {
                if (iOSGameId != null) {
                    return BaseGameCache.getInstance().getGameInfoById(bankId, iOSGameId,
                            gameInfo.getCurrency());
                } else {
                    throw new Exception("iOS player tries to open standard game and iOS gameId is not found");
                }
            }
            if (android) {
                if (androidGameId != null) {
                    return BaseGameCache.getInstance().getGameInfoById(bankId, androidGameId,
                            gameInfo.getCurrency());
                }
            }

            if (winPhone) {
                if (winPhoneGameId != null) {
                    return BaseGameCache.getInstance().getGameInfoById(bankId, winPhoneGameId,
                            gameInfo.getCurrency());
                }
            }

            return gameInfo;
        }
    }

    public static IBaseGameInfo checkGameInfo(IBaseGameInfo gameInfo, ClientType clientType, String userAgent)
            throws Exception {
        long bankId = gameInfo.getBankId();
        BankInfo bankInfo = BankInfoCache.getInstance().getBankInfo(bankId);
        boolean useSingleGameIdForAllDevices = bankInfo.isUseSingleGameIdForAllDevices();
        logger.info("MobileDetector::checkGameInfo clientType=" + clientType + ", gameInfo.isMobile()=" +
                gameInfo.isMobile() + ", bankId=" + bankId + ", useSingleGameIdForAllDevices=" + useSingleGameIdForAllDevices);
        if (useSingleGameIdForAllDevices) {
            return gameInfo;
        }

        return getAlternateGameInfo(gameInfo, userAgent);
    }
}
