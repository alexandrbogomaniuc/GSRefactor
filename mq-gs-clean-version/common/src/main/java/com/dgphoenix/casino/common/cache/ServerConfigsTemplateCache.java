package com.dgphoenix.casino.common.cache;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Map;

import com.dgphoenix.casino.common.config.GameServerConfigTemplate;
import com.dgphoenix.casino.common.exception.CommonException;

/**
 * User: flsh
 * Date: 10.06.14.
 */

@CacheKeyInfo(description = "gsConfig.id")
public class ServerConfigsTemplateCache extends AbstractExportableCache<GameServerConfigTemplate> {
    private static final ServerConfigsTemplateCache instance = new ServerConfigsTemplateCache();

    private GameServerConfigTemplate template = null;

    private ServerConfigsTemplateCache() {
    }

    public static ServerConfigsTemplateCache getInstance() {
        return instance;
    }

    public GameServerConfigTemplate getServerConfigTemplate() {
        return template;
    }

    @Override
    public void exportEntries(ObjectOutputStream outStream) throws IOException {
        synchronized (this) {
            if (template == null) {
                return;
            }
            outStream.writeObject(new ExportableCacheEntry(String.valueOf(template.getId()), template));
        }
    }

    @Override
    public void importEntry(ExportableCacheEntry entry) {
        synchronized (this) {
            if (entry.getValue() instanceof GameServerConfigTemplate) {
                GameServerConfigTemplate config = (GameServerConfigTemplate) entry.getValue();
                try {
                    put(config);
                } catch (CommonException e) {
                    throw new IllegalStateException("Cannot put entry: " + config, e);
                }
            }
        }
    }

    @Override
    public void put(GameServerConfigTemplate entry) throws CommonException {
        this.template = entry;
    }

    @Override
    public GameServerConfigTemplate getObject(String id) {
        return template;
    }

    @Override
    public Map<Integer, GameServerConfigTemplate> getAllObjects() {
        return Collections.singletonMap(GameServerConfigTemplate.TEMPLATE_ID, template);
    }

    @Override
    public int size() {
        return template == null ? 0 : 1;
    }

    @Override
    public String getAdditionalInfo() {
        return "NONE";
    }

    @Override
    public String printDebug() {
        return getAdditionalInfo();
    }
}

