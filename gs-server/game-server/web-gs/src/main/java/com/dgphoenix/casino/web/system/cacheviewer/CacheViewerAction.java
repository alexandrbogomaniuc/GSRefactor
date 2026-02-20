package com.dgphoenix.casino.web.system.cacheviewer;

import com.dgphoenix.casino.cache.CachesHolder;
import com.dgphoenix.casino.common.cache.*;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.dgphoenix.casino.common.cache.data.server.ServerInfo;
import com.dgphoenix.casino.common.util.ApplicationContextHelper;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringUtils;
import com.dgphoenix.casino.common.util.xml.GmtTimeConverter;
import com.google.common.base.Splitter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.converters.reflection.FieldDictionary;
import com.thoughtworks.xstream.converters.reflection.ImmutableFieldKeySorter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.dgphoenix.casino.common.cache.IDistributedCache.ID_DELIMITER;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public class CacheViewerAction extends Action {
    private static final Logger LOG = LogManager.getLogger(CacheViewerAction.class);
    private static final int DEFAULT_NORMAL_SIZE = 250;

    public static final String CMD_VIEW = "view";
    public static final String CMD_VIEW_XML = "viewXML";
    public static final String CMD_VIEW_TRACKING_INFO = "viewTrackingInfo";
    public static final String CMD_VIEW_ADDITIONAL_INFO = "viewAdditionalInfo";
    public static final String CMD_VIEW_SIZE = "viewSize";

    public static final String ATTRIBUTE_RESULT_CACHE = "cache";
    public static final String ATTRIBUTE_RESULT_CACHE_OBJECT = "cacheObject";
    public static final String ATTRIBUTE_RESULT_CACHE_TRACKING_INFO = "cacheTrackingInfo";
    public static final String ATTRIBUTE_RESULT_CACHE_ADDITIONAL_INFO = "cacheAdditionalInfo";
    public static final String ATTRIBUTE_RESULT_CACHE_SIZE = "cacheSize";
    public static final String ATTRIBUTE_RESULT_EMPTY = "emptyResult";
    public static final String ATTRIBUTE_RESULT_RECORDS_COUNT = "recordsCount";
    public static final String ATTRIBUTE_SIZE_WARNING = "sizeWarning";
    public static final String ATTRIBUTE_ERROR = "error";
    public static final String FIELD_UPDATE_TIME = "updateTime";
    public static final String INCORRECT_INPUT = "Incorrect input: objectId=";
    public static final String ERROR_TEMPLATE = "<error>%s</error>";

    public static final List<CacheListItem> cachesList = new ArrayList<>();
    public static final Map<String, IDistributedCache> cacheMap = new HashMap<>();
    private static final XStream xStream = new XStream(new TransientFieldsAllowedProvider());

    static {
        CachesHolder cachesHolder = ApplicationContextHelper.getApplicationContext()
                .getBean("cachesHolder", CachesHolder.class);
        final LinkedList<IDistributedCache> caches = cachesHolder.getCaches();
        for (IDistributedCache cache : caches) {
            put(cache.getClass().getSimpleName(), cache);
        }

        for (Map.Entry entry : getCacheMap().entrySet()) {
            String description;
            CacheKeyInfo annotation = entry.getValue().getClass().getAnnotation(CacheKeyInfo.class);
            if (annotation != null) {
                description = annotation.description();
            } else {
                description = "undefined";
            }
            cachesList.add(new CacheListItem(entry.getKey().toString(), entry.getKey().toString(), description));
        }
        cachesList.sort(Comparator.comparing(CacheListItem::getKey));
        xStream.registerLocalConverter(ServerInfo.class, FIELD_UPDATE_TIME, new GmtTimeConverter());
        xStream.autodetectAnnotations(true);
    }

    private static void put(String klazzName, IDistributedCache cache) {
        if (!cacheMap.containsKey(klazzName)) {
            cacheMap.put(klazzName, cache);
        }
    }

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        CacheViewerForm viewerForm = (CacheViewerForm) form;

        if (CMD_VIEW.equalsIgnoreCase(viewerForm.getCmd())) {
            processCommandView(request, viewerForm);
        } else if (CMD_VIEW_ADDITIONAL_INFO.equalsIgnoreCase(viewerForm.getCmd())) {
            processCommandViewAdditionalInfo(request, viewerForm);
        } else if (CMD_VIEW_SIZE.equalsIgnoreCase(viewerForm.getCmd())) {
            processCommandViewSize(request, viewerForm);
        } else if (CMD_VIEW_TRACKING_INFO.equalsIgnoreCase(viewerForm.getCmd())) {
            processCommandViewTrackingInfo(request, viewerForm);
        } else if (CMD_VIEW_XML.equalsIgnoreCase(viewerForm.getCmd())) {
            processCommandViewXML(response, viewerForm);
            return null;
        } else {
            LOG.error("CacheViewerAction::execute cmd:{} is not defined", viewerForm.getCmd());
        }

        return mapping.findForward("success");
    }

    private void processCommandViewXML(HttpServletResponse response, CacheViewerForm viewerForm) throws IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        String objectId = viewerForm.getObjectId();
        if (StringUtils.isTrimmedEmpty(objectId)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String error = String.format(ERROR_TEMPLATE, "Parameter 'objectId' is not passed.");
            writer.print(error);
        } else {
            try {
                IDistributedCache cache = cacheMap.get(viewerForm.getCache());
                writer.print(xStream.toXML(getCacheValue(objectId, cache)));
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.print(String.format(ERROR_TEMPLATE, e.getMessage()));
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writer.print(String.format(ERROR_TEMPLATE, e.getMessage()));
            }
        }
        writer.flush();
    }

    private void processCommandViewTrackingInfo(HttpServletRequest request, CacheViewerForm viewerForm) {
        String cacheName = viewerForm.getCache();
        IDistributedCache cache = getCacheMap().get(cacheName);
        if (cache instanceof ITrackingCache) {
            request.setAttribute(ATTRIBUTE_RESULT_CACHE_TRACKING_INFO, ((ITrackingCache) cache).getTrackingInfo());
        } else {
            request.setAttribute(ATTRIBUTE_RESULT_CACHE_TRACKING_INFO, IDistributedCache.NO_INFO);
        }
    }

    private void processCommandViewSize(HttpServletRequest request, CacheViewerForm viewerForm) {
        String cacheName = viewerForm.getCache();
        IDistributedCache cache = getCacheMap().get(cacheName);
        request.setAttribute(ATTRIBUTE_RESULT_CACHE_SIZE, cache.size());
    }

    private void processCommandViewAdditionalInfo(HttpServletRequest request, CacheViewerForm viewerForm) {
        String cacheName = viewerForm.getCache();
        IDistributedCache cache = getCacheMap().get(cacheName);
        request.setAttribute(ATTRIBUTE_RESULT_CACHE_ADDITIONAL_INFO, cache.getAdditionalInfo());
    }

    private void processCommandView(HttpServletRequest request, CacheViewerForm form) {
        if (!StringUtils.isTrimmedEmpty(form.getObjectId())) {
            this.processViewCacheRecord(request, form.getCache(), form.getObjectId());
        } else {
            this.processViewAllCacheRecords(request, form.getCache(), form.isBigCacheSizeAllowed());
        }
    }

    private void processViewCacheRecord(HttpServletRequest request, String cacheName, String objectId) {
        IDistributedCache cache = getCacheMap().get(cacheName);
        Object result;
        try {
            Object ob = getCacheValue(objectId, cache);
            if (ob == null) {
                result = "No object with id:" + objectId + " was found";
                request.setAttribute(ATTRIBUTE_RESULT_EMPTY, result);
            } else {
                result = new Pair<Object, Object>(objectId, ob);
                request.setAttribute(ATTRIBUTE_RESULT_CACHE_OBJECT, result);
            }
        } catch (IllegalArgumentException e) {
            LOG.error("objectId: {} is incorrect input", objectId, e);
            request.setAttribute(ATTRIBUTE_ERROR, INCORRECT_INPUT + objectId);
        } catch (Exception e) {
            LOG.error("Unable to get cache record", e);
            request.setAttribute(ATTRIBUTE_ERROR, e.getMessage());
        }
    }

    private Object getCacheValue(String objectId, IDistributedCache cache) {
        String searchParameter = objectId.trim();
        if (cache instanceof BaseGameCache) {
            List<String> parameters = Splitter.on(ID_DELIMITER).splitToList(objectId);
            if (parameters.size() > 2 && parameters.size() < 5) {
                long bankId = Long.parseLong(parameters.get(0));
                long gameId = Long.parseLong(parameters.get(1));
                String currencyCode = parameters.get(2);
                Currency currency = CurrencyCache.getInstance().get(currencyCode);
                if (currency != null) {
                    BaseGameCache baseGameCache = (BaseGameCache) cache;
                    searchParameter = baseGameCache.composeGameKey(bankId, gameId, currency);
                    if (parameters.size() > 3) {
                        String profileId = parameters.get(3);
                        searchParameter = baseGameCache.composeGameKeyProfiled(bankId, gameId, currency, profileId);
                    }
                }
            } else if (parameters.size() == 2) {
                if (!isNumeric(parameters.get(0)) || !isNumeric(parameters.get(1))) {
                    throw new IllegalArgumentException(INCORRECT_INPUT + objectId);
                }
            } else {
                throw new IllegalArgumentException(INCORRECT_INPUT + objectId);
            }
        }
        return cache.getObject(searchParameter);
    }

    private void processViewAllCacheRecords(HttpServletRequest request, String cacheName, boolean bigCacheSizeAllowed) {
        IDistributedCache cache = getCacheMap().get(cacheName);
        int cacheSize = cache.size();
        if (cacheSize > DEFAULT_NORMAL_SIZE && !bigCacheSizeAllowed) {
            request.setAttribute(ATTRIBUTE_SIZE_WARNING, "Cache has:" + cacheSize +
                    " are you sure you want to show it?");
            return;
        }

        Map<Object, Object> cacheMap = cache.getAllObjects();
        if (CollectionUtils.isEmpty(cacheMap)) {
            request.setAttribute(ATTRIBUTE_RESULT_EMPTY, "Cache is empty");
        } else {
            request.setAttribute(ATTRIBUTE_RESULT_CACHE, cacheMap);
            request.setAttribute(ATTRIBUTE_RESULT_RECORDS_COUNT, cacheSize);
        }
    }

    public static Map<String, IDistributedCache> getCacheMap() {
        return cacheMap;
    }

    public static List<CacheListItem> getCachesList() {
        return cachesList;
    }

    static class TransientFieldsAllowedProvider extends PureJavaReflectionProvider {

        public TransientFieldsAllowedProvider() {
            this(new FieldDictionary(new ImmutableFieldKeySorter()));
        }

        public TransientFieldsAllowedProvider(FieldDictionary fieldDictionary) {
            super(fieldDictionary);
        }

        @Override
        protected boolean fieldModifiersSupported(Field field) {
            if (field.getAnnotation(XStreamOmitField.class) != null) {
                return false;
            }
            if (IDistributedCache.class.isAssignableFrom(field.getType())) {
                return false;
            }
            int modifiers = field.getModifiers();
            return !Modifier.isStatic(modifiers);
        }
    }
}
