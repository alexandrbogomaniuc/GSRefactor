package com.abs.casino.common.web;

import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.cache.data.game.GameMode;
import com.dgphoenix.casino.common.cache.data.game.Html5PcVersionMode;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.web.MobileDetector;

import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

/**
 * Created by mic on 25.11.14.
 */
public final class ShellDetector {

    private static final BaseGameInfoTemplateCache templates = BaseGameInfoTemplateCache.getInstance();

    private static final String DEFAULT_MOBILE_TEMPLATE_FREE = "/free/mobile/template.jsp";
    private static final String DEFAULT_MOBILE_TEMPLATE_REAL = "/real/mobile/template.jsp";
    private static final String MOBILE_UNJ_SHELL_REAL = "/real/mobile/unj/template.jsp";

    private static final String DEFAULT_HTML5PC_TEMPLATE_FREE = "/free/html5pc/template.jsp";
    private static final String DEFAULT_HTML5PC_TEMPLATE_REAL = "/real/html5pc/template.jsp";

    private ShellDetector() {
    }


    public static boolean isMobileShell(BaseGameInfoTemplate template, boolean singleGID, String userAgent,
                                        String platform, boolean isHtml5Pc) {
        boolean isForceHtml5 = "html5".equalsIgnoreCase(platform);
        boolean isMobileBrowser = MobileDetector.isMobile(userAgent);
        Html5PcVersionMode html5PcMode = template.getDefaultGameInfo().getHtml5PcVersionMode();
        boolean isUnified = html5PcMode.equals(Html5PcVersionMode.UNIFIED);

        String playerDeviceType = templates.getPlayerDeviceType(template.getGameId());

        boolean mobileGame = isMobileBrowser && (!isTrimmedEmpty(playerDeviceType) || singleGID);
        return isUnified || isForceHtml5 || mobileGame || !isHtml5Pc;
    }

    public static String getShellPath(BankInfo bankInfo, GameMode mode, BaseGameInfoTemplate template, String userAgent,
                                      String platform, boolean isUnj) {
        boolean singleGID = bankInfo.isUseSingleGameIdForAllDevices() || template.isSingleGameIdForAllPlatforms();
        Html5PcVersionMode html5PcMode;
        IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankInfo.getId(), template.getGameId(), bankInfo.getDefaultCurrency());
        if (gameInfo != null) {
            html5PcMode = gameInfo.getHtml5PcVersionMode();
        } else {
            html5PcMode = template.getDefaultGameInfo().getHtml5PcVersionMode();
        }
        boolean isHtml5Pc = html5PcMode.equals(Html5PcVersionMode.AVAILABLE)
                || (html5PcMode.equals(Html5PcVersionMode.DEVELOPMENT) && bankInfo.isStubMode())
                || html5PcMode.equals(Html5PcVersionMode.ONLY);

        boolean mobileShell = isMobileShell(template, singleGID, userAgent, platform, isHtml5Pc);
        return getShellPath(mode, mobileShell, isUnj);
    }

    private static String getShellPath(GameMode mode, boolean isMobileShell, boolean isUNJ) {
        if (isMobileShell) {
            return getMobileShellPath(mode, isUNJ);
        }
        return getHtml5PcShellPath(mode);
    }

    private static String getMobileShellPath(GameMode mode, boolean isUNJ) {
        switch (mode) {
            case REAL:
            case BONUS:
                if (isUNJ) {
                    return MOBILE_UNJ_SHELL_REAL;
                } else {
                    return DEFAULT_MOBILE_TEMPLATE_REAL;
                }
            case FREE:
            default:
                return DEFAULT_MOBILE_TEMPLATE_FREE;
        }
    }

    private static String getHtml5PcShellPath(GameMode mode) {
        switch (mode) {
            case REAL:
            case BONUS:
                return DEFAULT_HTML5PC_TEMPLATE_REAL;
            case FREE:
            default:
                return DEFAULT_HTML5PC_TEMPLATE_FREE;
        }
    }
}
