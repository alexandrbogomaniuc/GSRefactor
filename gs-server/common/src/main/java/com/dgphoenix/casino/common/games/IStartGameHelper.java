package com.abs.casino.common.games;

import com.abs.casino.common.games.SwfLocationInfo;
import com.abs.casino.common.cache.data.session.GameSession;

import javax.servlet.http.HttpServletRequest;

/**
 * User: flsh
 * Date: 26.10.11
 */
public interface IStartGameHelper {
    long getGameId();

    String getServletName();

    void setServletName(String servletName);

    void setSwfLocation(String swfLocation);

    String getServletName(long bankId, String currencyCode);

    String getTitle(long bankId, String lang);

    String getAdditionalParams();

    String getSwfPath(long bankId);

    String getSwfName(long bankId);

    String getSwfLocation(long bankId);

    String getHtml5Location(long bankId, boolean isUnified, boolean isForceHtml5);

    SwfLocationInfo getSwfBase(long bankId, String lang, boolean realMode, HttpServletRequest req, long serverId);

    boolean isRoundFinished(String lasthand);

    boolean isRoundFinished(String lasthand, GameSession gameSession);
}
