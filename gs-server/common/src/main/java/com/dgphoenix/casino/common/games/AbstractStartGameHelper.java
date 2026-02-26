package com.abs.casino.common.games;

import com.abs.casino.common.games.IDelegatedStartGameHelper;
import com.abs.casino.common.games.ICassandraHostCdnPersister;
import com.abs.casino.common.games.IStartGameHelper;
import com.dgphoenix.casino.common.cache.BankInfoCache;
import com.dgphoenix.casino.common.cache.BaseGameCache;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.CurrencyCache;
import com.dgphoenix.casino.common.cache.data.bank.BankInfo;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.game.BaseGameConstants;
import com.dgphoenix.casino.common.cache.data.game.IBaseGameInfo;
import com.dgphoenix.casino.common.cache.data.session.GameSession;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

import static com.dgphoenix.casino.common.configuration.messages.MessageManager.getLocalizedTitleOrDefault;
import static com.dgphoenix.casino.common.util.string.StringUtils.isTrimmedEmpty;

public abstract class AbstractStartGameHelper implements IStartGameHelper {
    protected final long gameId;
    protected String servletName;
    protected final String title;
    protected String swfPath;
    protected String swfName;
    protected String swfLocation;
    protected final String additionalParams;
    protected final IDelegatedStartGameHelper delegatedHelper;
    protected ICassandraHostCdnPersister hostCdnPersister;
    private static final Logger LOG = Logger.getLogger(AbstractStartGameHelper.class);


    public AbstractStartGameHelper(long gameId, String servletName, String title, String swfLocation,
                                   String additionalParams, IDelegatedStartGameHelper delegatedHelper,
                                   ICassandraHostCdnPersister hostCdnPersister) {
        this.gameId = gameId;
        this.servletName = servletName;
        this.title = title;
        this.swfLocation = swfLocation;
        this.swfPath = "";
        this.swfName = "";
        if (!isTrimmedEmpty(swfLocation)) {
            try {
                this.swfPath = swfLocation.substring(0, swfLocation.lastIndexOf("/"));
                this.swfName = swfLocation.substring(swfLocation.lastIndexOf("/"));
            } catch (Exception ex) {
                LOG.warn("swfLocation is not correct, gameId=" + gameId + " swfLocation=" + swfLocation);
            }
        } else {
            LOG.warn("swfLocation is null or empty, gameId=" + gameId);
        }
        this.additionalParams = additionalParams;
        this.delegatedHelper = delegatedHelper;
        this.hostCdnPersister = hostCdnPersister;
    }

    @Override
    public String getHtml5Location(long bankId, boolean isUnified, boolean isForceHtml5) {
        if (isUnified && !isForceHtml5 && bankId > 0) {
            Currency defaultCurrency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, defaultCurrency);
            String unifiedLocation = gameInfo.getProperty(BaseGameConstants.KEY_UNIFIED_LOCATION);
            if (!isTrimmedEmpty(unifiedLocation)) {
                return unifiedLocation;
            }
        }
        String name = getSwfName(bankId);
        return "/html5/" + name.substring(1, name.length() - 4);
    }

    protected ICassandraHostCdnPersister getHostCdnPersister() {
        return hostCdnPersister;
    }

    @Override
    public long getGameId() {
        return gameId;
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    @Override
    public void setSwfLocation(String swfLocation) {
        this.swfLocation = swfLocation;
    }

    @Override
    public String getServletName(long bankId, String currencyCode) {
        Currency currency = CurrencyCache.getInstance().get(currencyCode);
        IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, currency);
        if (gameInfo != null && !isTrimmedEmpty(gameInfo.getServlet())) {
            return gameInfo.getServlet();
        } else {
            String servlet = BaseGameInfoTemplateCache.getInstance().getServletById(gameId);
            if (!isTrimmedEmpty(servlet)) {
                return servlet;
            }
        }
        return getServletName();
    }

    @Override
    public String getTitle(long bankId, String lang) {
        String localizedTitle = getLocalizedTitleOrDefault(bankId, gameId, lang);
        return isTrimmedEmpty(localizedTitle) ? this.title : localizedTitle;
    }

    @Override
    public String getAdditionalParams() {
        return additionalParams;
    }

    @Override
    public String getSwfPath(long bankId) {
        if (bankId > 0) {
            Currency defaultCurrency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, defaultCurrency);
            String swfLocation = gameInfo.getProperty("swfLocation");
            if (gameInfo.getProperty("swfLocation") != null)
                return swfLocation.substring(0, swfLocation.lastIndexOf("/"));
        }
        return swfPath;
    }

    @Override
    public String getSwfName(long bankId) {
        if (bankId > 0) {
            Currency defaultCurrency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, defaultCurrency);
            String swfLocation = gameInfo.getProperty("swfLocation");
            if (gameInfo.getProperty("swfLocation") != null)
                return swfLocation.substring(swfLocation.lastIndexOf("/"));
        }
        return swfName;
    }

    @Override
    public String getSwfLocation(long bankId) {
        if (bankId > 0) {
            Currency defaultCurrency = BankInfoCache.getInstance().getBankInfo(bankId).getDefaultCurrency();
            IBaseGameInfo gameInfo = BaseGameCache.getInstance().getGameInfoById(bankId, gameId, defaultCurrency);
            if (gameInfo.getProperty("swfLocation") != null)
                return gameInfo.getProperty("swfLocation");
        }
        return swfLocation;
    }

    public static String getStaticContentPath(BankInfo bankInfo, HttpServletRequest req, long serverId) {
        if (bankInfo.isReplaceStartServerName()) {
            String serverName = req.getServerName();
            serverName = serverName.replaceFirst(bankInfo.getReplaceStartServerFrom(),
                    bankInfo.getReplaceStartServerTo() + serverId);
            return req.getScheme() + "://" + serverName;
        }
        return ""; //load from this server
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("GameParams");
        sb.append("[gameId=").append(gameId);
        sb.append(", servletName=").append(servletName);
        sb.append(", title='").append(title).append('\'');
        sb.append(", swfPath='").append(swfPath).append('\'');
        sb.append(", swfName='").append(swfName).append('\'');
        sb.append(", swfLocation='").append(swfLocation).append('\'');
        sb.append(", additionalParams=").append(additionalParams);
        sb.append(']');
        return sb.toString();
    }

    //VIVO Live games
    public static boolean isGameWithoutLastHand(long gameId) {
        return (gameId >= 415 && gameId <= 421) || gameId == 626;
    }

    @Override
    public boolean isRoundFinished(String lasthand, GameSession gameSession) {
        if (isTrimmedEmpty(lasthand)) {
            if (gameSession != null && isGameWithoutLastHand(gameSession.getGameId())) {
                return gameSession.isCreateNewBet();
            }
            return true;
        }
        return delegatedHelper != null && delegatedHelper.isRoundFinished(lasthand);
    }

    @Override
    public boolean isRoundFinished(String lasthand) {
        return isTrimmedEmpty(lasthand) ||
                (delegatedHelper != null && delegatedHelper.isRoundFinished(lasthand));
    }
}
