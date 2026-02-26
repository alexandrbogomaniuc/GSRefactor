package com.dgphoenix.casino.common.games;

import com.abs.casino.common.games.IHelperCreator;
import com.dgphoenix.casino.common.cache.BaseGameInfoTemplateCache;
import com.dgphoenix.casino.common.cache.data.game.BaseGameInfoTemplate;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: flsh
 * Date: 26.10.11
 */
public class StartGameHelpers {
    private static StartGameHelpers instance = new StartGameHelpers();
    private IHelperCreator helperCreator;
    private static final Logger LOG = LogManager.getLogger(StartGameHelpers.class);
    private static final ConcurrentMap<Long, IStartGameHelper> gameParams = new ConcurrentHashMap<>();

    public void init(IHelperCreator helperCreator) {
        if (this.helperCreator == null) {
            this.helperCreator = helperCreator;
        }
        Collection<BaseGameInfoTemplate> templates = BaseGameInfoTemplateCache.getInstance().getAllObjects().values();
        for (BaseGameInfoTemplate template : templates) {
            TemplateStartGameHelper helper = new TemplateStartGameHelper(template);
            try {
                addNewHelper(template.getGameId(), template.getServlet(),
                        template.getTitle(), template.getSwfLocation(), template.getAdditionalParams(), helper);
            } catch (Exception e) {
                LOG.error("Cannot initialize StartGameHelper: template=" + template, e);
            }
        }
    }

    public static StartGameHelpers getInstance() {
        return instance;
    }

    private StartGameHelpers() {
    }

    public IStartGameHelper getHelper(Long gameId) {
        IStartGameHelper helper = gameParams.get(gameId);
        if (helper == null) {
            BaseGameInfoTemplate template = BaseGameInfoTemplateCache.getInstance().getBaseGameInfoTemplateById(gameId);
            if (template == null) {
                throw new NullPointerException("StartGameHelper: template not found for gameId=" + gameId);
            }
            synchronized (this) {
                try {
                    TemplateStartGameHelper newHelper = new TemplateStartGameHelper(template);
                    addNewHelper(template.getGameId(), template.getServlet(),
                            template.getTitle(), template.getSwfLocation(), template.getAdditionalParams(), newHelper);
                    helper = gameParams.get(gameId);
                } catch (Exception e) {
                    LOG.error("Cannot initialize StartGameHelper: template=" + template, e);
                }
            }
        }
        if (helper == null) {
            throw new NullPointerException("StartGameHelper not found for gameId=" + gameId);
        }
        return helper;
    }

    public Collection<IStartGameHelper> getHelpers() {
        return Collections.unmodifiableCollection(gameParams.values());
    }

    public void addNewHelper(long gameId, String servletName, String title, String swfLocation,
                             String additionalParams, IDelegatedStartGameHelper delegatedHelper) {
        IStartGameHelper helper = helperCreator.create(false, gameId, servletName, title, swfLocation,
                additionalParams, delegatedHelper);
        addHelper(helper);
    }

    public void addHelper(IStartGameHelper helper) {
        if (StringUtils.isTrimmedEmpty(helper.getServletName()) || StringUtils.isTrimmedEmpty(helper.getTitle(-1, null)) ||
                StringUtils.isTrimmedEmpty(helper.getSwfName(-1))) {
            LOG.warn("Missing required parameters, params={}", helper);
        }
        gameParams.putIfAbsent(helper.getGameId(), helper);
    }

    class TemplateStartGameHelper implements IDelegatedStartGameHelper {
        private BaseGameInfoTemplate template;

        TemplateStartGameHelper(BaseGameInfoTemplate template) {
            this.template = template;
        }

        @Override
        public boolean isRoundFinished(String lasthand) {
            //hack for mq tournament
            if (template.getGameId() == 1) {
                return true;
            }
            return template.isRoundFinished(lasthand);
        }
    }
}
