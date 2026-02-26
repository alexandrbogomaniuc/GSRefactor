package com.abs.casino.web.system.cacheviewer;

import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class CacheViewerForm extends ActionForm {
    private String cmd;
    private String cache;
    private String objectId;
    private boolean bigCacheSizeAllowed = false;

    public CacheViewerForm() {
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId != null ? objectId.trim() : null;
    }

    public boolean isBigCacheSizeAllowed() {
        return bigCacheSizeAllowed;
    }

    public void setBigCacheSizeAllowed(boolean bigCacheSizeAllowed) {
        this.bigCacheSizeAllowed = bigCacheSizeAllowed;
    }

    public List<CacheListItem> getCachesList() {
        return CacheViewerAction.getCachesList();
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (StringUtils.isTrimmedEmpty(cmd)) {
            errors.add("cmd_error", new ActionMessage("error.cacheviewer.badCommand", "'empty command'"));
            return errors;
        } else {
            if (!CacheViewerAction.CMD_VIEW.equalsIgnoreCase(cmd) &&
                    !CacheViewerAction.CMD_VIEW_XML.equalsIgnoreCase(cmd) &&
                    !CacheViewerAction.CMD_VIEW_ADDITIONAL_INFO.equalsIgnoreCase(cmd) &&
                    !CacheViewerAction.CMD_VIEW_SIZE.equalsIgnoreCase(cmd) &&
                    !CacheViewerAction.CMD_VIEW_TRACKING_INFO.equalsIgnoreCase(cmd)) {
                errors.add("cmd_error", new ActionMessage("error.cacheviewer.badCommand"));
                return errors;
            }
        }

        if (StringUtils.isTrimmedEmpty(cache)) {
            errors.add("cache_not_found", new ActionMessage("error.cacheviewer.badCache"));
            return errors;
        } else {
            if (!CacheViewerAction.getCacheMap().containsKey(cache)) {
                errors.add("cache_not_found", new ActionMessage("error.cacheviewer.cacheNotFound", cache));
                return errors;
            }
        }
        return errors;
    }
}
