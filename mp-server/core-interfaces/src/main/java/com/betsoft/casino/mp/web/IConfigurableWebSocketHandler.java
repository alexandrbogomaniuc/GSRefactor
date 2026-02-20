package com.betsoft.casino.mp.web;

import io.netty.channel.ChannelHandler;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketDecoderConfig;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import org.apache.logging.log4j.Logger;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * User: flsh
 * Date: 01.12.2022.
 *
 * Patched:
 * - No-ops on Servlet stack (Tomcat/Jetty), where reflective Netty internals don't exist.
 * - On Reactor Netty, finds WebSocket13FrameDecoder in the Netty pipeline and REPLACES it with a new one
 *   that uses a larger max frame size. No private-field mutation.
 * - All reflection is guarded and exceptions are swallowed (logged optionally by implementor).
 */
public interface IConfigurableWebSocketHandler extends WebSocketHandler {

    /**
     * Default max frame size (adjust as needed). Can be overridden via system property:
     * -Dws.maxFrameBytes=4194304
     */
    int MAX_FRAME_BYTES = Integer.getInteger("ws.maxFrameBytes", 4 * 1024 * 1024); //4 MB max frame size

    WebSocketDecoderConfig DEFAULT_CONFIG = WebSocketDecoderConfig.newBuilder()
            .maxFramePayloadLength(MAX_FRAME_BYTES)
            .expectMaskedFrames(true)
            .allowMaskMismatch(false)
            .allowExtensions(false)
            .closeOnProtocolViolation(true)
            .withUTF8Validator(true)
            .build();

    WebSocketDecoderConfig DEFAULT_CONFIG_WITHOUT_MASKED_FRAMES = WebSocketDecoderConfig.newBuilder()
            .maxFramePayloadLength(MAX_FRAME_BYTES)
            .expectMaskedFrames(false)
            .allowMaskMismatch(false)
            .allowExtensions(false)
            .closeOnProtocolViolation(true)
            .withUTF8Validator(true)
            .build();

    /**
     * Returns a WebSocketDecoderConfig based on whether masked frames are expected.
     */
    static WebSocketDecoderConfig getConfig(boolean expectMaskedFrames) {
        return expectMaskedFrames ? DEFAULT_CONFIG : DEFAULT_CONFIG_WITHOUT_MASKED_FRAMES;
    }

    /**
     * Keep default behavior: masked frames expected (browser clients).
     */
    default void changeDefaultConfig(WebSocketSession session, Logger log) throws NoSuchFieldException, IllegalAccessException {
        changeDefaultConfig(session, true, log);
    }

    /**
     * Safely attempts to replace Netty's WebSocket13FrameDecoder with a new one using the provided config.
     * On Servlet/Tomcat/Jetty sessions (StandardWebSocketSession), this method NO-OPs.
     *
     * @param session            Spring WebSocketSession
     * @param expectMaskedFrames whether to expect masked frames (true for browser clients)
     */
    default void changeDefaultConfig(WebSocketSession session, boolean expectMaskedFrames, Logger log) throws NoSuchFieldException, IllegalAccessException {

        /*try {
            // 1) WS-decoder "aggressive" substitution with maxFramePayloadLength
            boolean applied = applyWebSocketFrameLimit(session, MAX_FRAME_BYTES, log);
            if (applied) {
                if (log != null) log.info("changeDefaultConfig: WS frame limit successfully increased on server side to {} bytes." ,MAX_FRAME_BYTES);
                return;
            }
        } catch (Throwable e) {
            if (log != null) log.error("changeDefaultConfig: Failed to raise WS frame size limit", e);
        }*/

        // 2) Fallback: original method can work in different configurations
        if (log != null) log.warn("changeDefaultConfig: WS frame limit tweak via applyWebSocketFrameLimit() failed; fallback original method.");

        // Fast path: Servlet stack (Tomcat/Jetty) → do nothing; configure via ServletServerContainerFactoryBean.
        final String sessionClassName = session.getClass().getName();
        if (isServletSession(sessionClassName)) {
            if (log != null) log.debug("changeDefaultConfig: isServletSession for sessionClassName={}.", sessionClassName);
            return;
        }

        // Find the "delegate" holder on the base session class (Reactor Netty layout).
        Class<?> base = session.getClass();
        while (base.getSuperclass() != null && base.getSuperclass() != Object.class) {
            base = base.getSuperclass();
        }

        if (log != null) log.debug("changeDefaultConfig: base class={}.", base);
        Field delegateField = base.getDeclaredField("delegate");
        delegateField.setAccessible(true);
        Object delegate = delegateField.get(session);

        // "inbound" exists on Reactor Netty session delegate; if not present → not our layout → bail out.
        Field inboundField = tryGetDeclaredField(delegate.getClass(), "inbound");
        if (inboundField == null) {
            if (log != null) log.warn("changeDefaultConfig: inboundField is null.");
            return;
        }
        inboundField.setAccessible(true);
        Object inboundOps = inboundField.get(delegate);

        // Walk hierarchy to find "channel"
        Field channelField = findFieldInHierarchy(inboundOps.getClass(), "channel");
        if (channelField == null) {
            if (log != null) log.warn("changeDefaultConfig: channelField is null.");
            return;
        }
        channelField.setAccessible(true);
        Object channel = channelField.get(inboundOps);

        // Walk hierarchy to find "pipeline"
        Field pipelineField = findFieldInHierarchy(channel.getClass(), "pipeline");
        if (pipelineField == null) {
            if (log != null) log.warn("changeDefaultConfig: pipelineField is null.");
            return;
        }
        pipelineField.setAccessible(true);
        DefaultChannelPipeline pipeline = (DefaultChannelPipeline) pipelineField.get(channel);

        // Locate existing WebSocket13FrameDecoder
        String targetName = null;
        ChannelHandler targetHandler = null;
        for (Map.Entry<String, ChannelHandler> e : pipeline.toMap().entrySet()) {
            if (e.getValue() instanceof WebSocket13FrameDecoder) {
                targetName = e.getKey();
                targetHandler = e.getValue();
                break;
            }
        }

        if (log != null) log.info("changeDefaultConfig: targetName={}, targetHandler={}", targetName, targetHandler);

        if (targetHandler == null || targetName == null) {
            if (log != null) log.warn("changeDefaultConfig: targetName or targetHandler is null, nothing to replace.");
            return; // nothing to replace
        }

        // Build a new decoder with our config
        WebSocket13FrameDecoder newDecoder;
        WebSocketDecoderConfig cfg = getConfig(expectMaskedFrames);

        try {
            // Netty 4.1+ constructor
            if (log != null) log.debug("changeDefaultConfig: use Netty 4.1+ constructor, new WebSocket13FrameDecoder(cfg)");
            newDecoder = new WebSocket13FrameDecoder(cfg);
        } catch (Throwable ignore) {
            // Older signature fallback
            boolean allowExtensions = false;
            boolean allowMaskMismatch = false;
            int maxLen = cfg.maxFramePayloadLength();
            if (log != null)
                log.debug("changeDefaultConfig: new WebSocket13FrameDecoder({}, {}, {}, {})",
                        expectMaskedFrames, allowExtensions, maxLen, allowMaskMismatch);
            newDecoder = new WebSocket13FrameDecoder(expectMaskedFrames, allowExtensions, maxLen, allowMaskMismatch);
        }

        if (log != null) log.debug("changeDefaultConfig: Replace in-place to newDecoder={}", newDecoder);
        pipeline.replace(targetName, targetName, newDecoder);

        // after decoder replacement — clean HTTP-agregator and set WS-agregator with required limit
        tweakAggregators(pipeline, targetName, MAX_FRAME_BYTES, log);
    }

    /**
     * Detects Servlet-based WebSocket sessions.
     */
    static boolean isServletSession(String sessionClassName) {
        // Typical: org.springframework.web.reactive.socket.adapter.StandardWebSocketSession
        // Any "StandardWebSocketSession" implies Servlet container (Tomcat/Jetty/Undertow).
        return sessionClassName.contains("StandardWebSocketSession");
    }

    /**
     * Utility: try get declared field, return null if missing.
     */
    static Field tryGetDeclaredField(Class<?> type, String fieldName) {
        try {
            return type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * Utility: find a declared field by name walking up the class hierarchy.
     */
    static Field findFieldInHierarchy(Class<?> type, String fieldName) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    /**
     * Removes all HttpObjectAggregator and sets/replaces WebSocketFrameAggregator(maxLen)
     * straight after specified decoder (decoderName).
     */
    static void tweakAggregators(DefaultChannelPipeline pipeline, String decoderName, int maxLen, Logger log) {
        if (pipeline == null || decoderName == null) return;

        // 1) remove All HttpObjectAggregator (usually these raise TooLongFrameException: 65536)
        int removed = 0;
        for (Map.Entry<String, ChannelHandler> e : pipeline.toMap().entrySet()) {
            if (e.getValue() instanceof HttpObjectAggregator) {
                try {
                    pipeline.remove(e.getKey());
                    removed++;
                    if (log != null) log.info("WS tweak: removed HttpObjectAggregator handler '{}'", e.getKey());
                } catch (Throwable t) {
                    if (log != null) log.warn("WS tweak: failed remove HttpObjectAggregator '{}': {}", e.getKey(), t.toString());
                }
            }
        }
        if (log != null && removed == 0) {
            log.debug("WS tweak: no HttpObjectAggregator found");
        }

        // 2) replace/add WebSocketFrameAggregator(maxLen)
        String aggName = null;
        for (Map.Entry<String, ChannelHandler> e : pipeline.toMap().entrySet()) {
            if (e.getValue() instanceof WebSocketFrameAggregator) {
                aggName = e.getKey();
                break;
            }
        }

        WebSocketFrameAggregator newAgg = new WebSocketFrameAggregator(maxLen);

        try {
            if (aggName != null) {
                pipeline.replace(aggName, aggName, newAgg);
                if (log != null) log.info("WS tweak: replaced WebSocketFrameAggregator '{}' with maxLen={}", aggName, maxLen);
            } else {
                // new handler name
                String newName = uniqueAfterName(pipeline, decoderName, "ws-frame-agg-" + maxLen);
                pipeline.addAfter(decoderName, newName, newAgg);
                if (log != null) log.info("WS tweak: added WebSocketFrameAggregator '{}' after '{}', maxLen={}", newName, decoderName, maxLen);
            }
        } catch (Throwable t) {
            if (log != null) log.warn("WS tweak: failed to install WebSocketFrameAggregator: {}", t.toString());
        }
    }

    /** Generates unique name for addAfter(...) */
    static String uniqueAfterName(DefaultChannelPipeline pipeline, String after, String base) {
        String name = base;
        int i = 1;
        Map<String, ChannelHandler> map = pipeline.toMap();
        while (map.containsKey(name)) {
            name = base + "-" + (i++);
        }
        return name;
    }


    /**
     * Increases the WebSocket frame size limit (maxFramePayloadLength) on the
     * Spring 5.0.x + Reactor Netty 0.7.x (Netty 4.1.x) stack, without modifying dependencies.
     *
     * Algorithm:
     * 1) Access the underlying Netty Channel from the WebSocketSession (via internal fields).
     * 2) Locate the decoder in the pipeline (usually named "ws-decoder" or "websocket-decoder").
     * 3) Instead of trying to modify private fields, create a NEW WebSocket13FrameDecoder
     *    with the same flags but an increased maxLen, and replace the handler using pipeline.replace(...).
     */
    default boolean applyWebSocketFrameLimit(WebSocketSession session,  int maxLen, Logger log) {
        // 3 tries if a small back-off
        int attempts = 3;
        long backoffMs = 20L;
        Throwable last = null;
        for (int i = 1; i <= attempts; i++) {
            try {
                doApply(session, maxLen, log);
                if (log != null) log.info("apply: WS decoder replaced with maxFramePayloadLength={}", maxLen);
                return true;
            } catch (Throwable t) {
                last = t;
                if (log != null) log.warn("apply: WS frame limit tweak attempt {}/{} failed: {}", i, attempts, t.toString());
                sleepQuiet(backoffMs * i);
            }
        }
        if (log != null && last != null) {
            log.warn("apply: WS frame limit tweak failed after retries: {}", last.toString());
            dumpPipelineSafe(session, log);
        }
        return false;
    }

    // ---- internal ----

    static void doApply(WebSocketSession session, int maxLen, Logger log) throws Exception {
        Object target = session;

        // different names for internal fields in different implementations.
        for (String f : new String[]{"delegate", "session", "reactorSession", "nativeSession"}) {
            Object v = tryGetField(target, f);
            if (v != null) { target = v; break; }
        }

        Object inbound = tryGetField(target, "inbound");
        if (inbound == null) inbound = tryGetField(target, "in");

        Object ch = (inbound != null) ? tryInvoke(inbound, "channel") : tryInvoke(target, "channel");
        if (ch == null) throw new IllegalStateException("Netty channel not found");

        Object pipeline = tryInvoke(ch, "pipeline");
        if (pipeline == null) throw new IllegalStateException("Channel pipeline is null");

        // --- helpers via reflection ---
        Class<?> pipelineClass = pipeline.getClass();
        Class<?> chHandlerClass = Class.forName("io.netty.channel.ChannelHandler");

        java.lang.reflect.Method namesM   = pipelineClass.getMethod("names");
        java.lang.reflect.Method getM     = pipelineClass.getMethod("get", String.class);
        java.lang.reflect.Method removeM  = pipelineClass.getMethod("remove", String.class);
        java.lang.reflect.Method replaceM = pipelineClass.getMethod("replace", String.class, String.class, chHandlerClass);
        java.lang.reflect.Method addAfterM= pipelineClass.getMethod("addAfter", String.class, String.class, chHandlerClass);

        // --- locate decoder handler name ---
        String foundDecoderName = null;
        Object decoder = null;

        Iterable<?> names = (Iterable<?>) namesM.invoke(pipeline);
        for (Object nameObj : names) {
            String n = String.valueOf(nameObj);
            Object h = getM.invoke(pipeline, n);
            if (h != null) {
                String cn = h.getClass().getName();
                if (cn.endsWith("WebSocket13FrameDecoder")) {
                    foundDecoderName = n;
                    decoder = h;
                    break;
                }
            }
        }
        if (decoder == null || foundDecoderName == null)
            throw new IllegalStateException("WebSocket decoder handler not found in pipeline");

        // --- build replacement decoder (with existing flags, but maxLen) ---
        boolean expectMaskedFrames = getBooleanField(decoder, "expectMaskedFrames", false);
        boolean allowExtensions    = getBooleanField(decoder, "allowExtensions",   true);
        boolean allowMaskMismatch  = getBooleanField(decoder, "allowMaskMismatch", false);

        Class<?> ws13 = Class.forName("io.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder");
        Object newDecoder;
        try {
            // Netty 4.1+ cfg-constructor
            Class<?> cfgClz = Class.forName("io.netty.handler.codec.http.websocketx.WebSocketDecoderConfig");
            // build config using Fabric getConfig(...) to not doblicate the logic
            WebSocketDecoderConfig cfg = getConfig(expectMaskedFrames).toBuilder()
                    .maxFramePayloadLength(maxLen)
                    .build();
            java.lang.reflect.Constructor<?> ctor = ws13.getConstructor(cfgClz);
            newDecoder = ctor.newInstance(cfg);
        } catch (Throwable ignore) {
            // Fallback old original constructor
            java.lang.reflect.Constructor<?> ctor = ws13.getConstructor(boolean.class, boolean.class, int.class, boolean.class);
            newDecoder = ctor.newInstance(expectMaskedFrames, allowExtensions, maxLen, allowMaskMismatch);
        }

        replaceM.invoke(pipeline, foundDecoderName, foundDecoderName, newDecoder);
        if (log != null) log.info("apply: WS decoder '{}' replaced with maxFramePayloadLength={}", foundDecoderName, maxLen);

        // --- 1) remove HttpObjectAggregator (if remained) ---
        Class<?> httpAggClz = Class.forName("io.netty.handler.codec.http.HttpObjectAggregator");
        String httpAggName = null;
        names = (Iterable<?>) namesM.invoke(pipeline); // reread names for the fields after decoder replacement.
        for (Object nameObj : names) {
            String n = String.valueOf(nameObj);
            Object h = getM.invoke(pipeline, n);
            if (h != null && httpAggClz.isInstance(h)) {
                httpAggName = n; break;
            }
        }
        if (httpAggName != null) {
            removeM.invoke(pipeline, httpAggName);
            if (log != null) log.info("apply: removed HttpObjectAggregator handler '{}'", httpAggName);
        }

        // --- 2) replace/add WebSocketFrameAggregator(maxLen) ---
        Class<?> wsAggClz = Class.forName("io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator");
        String wsAggName = null;
        Object wsAggObj = null;
        names = (Iterable<?>) namesM.invoke(pipeline);
        for (Object nameObj : names) {
            String n = String.valueOf(nameObj);
            Object h = getM.invoke(pipeline, n);
            if (h != null && wsAggClz.isInstance(h)) {
                wsAggName = n; wsAggObj = h; break;
            }
        }

        java.lang.reflect.Constructor<?> wsAggCtor = wsAggClz.getConstructor(int.class);
        Object newAgg = wsAggCtor.newInstance(maxLen);

        if (wsAggObj != null) {
            replaceM.invoke(pipeline, wsAggName, wsAggName, newAgg);
            if (log != null) log.info("apply: replaced WebSocketFrameAggregator '{}' with maxLen={}", wsAggName, maxLen);
        } else {
            // добавим ПОсЛЕ декодера
            String newName = "ws-frame-agg-" + maxLen;
            addAfterM.invoke(pipeline, foundDecoderName, newName, newAgg);
            if (log != null) log.info("apply: added WebSocketFrameAggregator '{}' after '{}', maxLen={}", newName, foundDecoderName, maxLen);
        }
    }

    static boolean getBooleanField(Object obj, String name, boolean def) {
        if (obj == null) return def;
        Class<?> c = obj.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.getBoolean(obj);
            } catch (NoSuchFieldException e) { c = c.getSuperclass(); }
            catch (Exception e) { return def; }
        }
        return def;
    }

    static Object tryGetField(Object owner, String field) {
        if (owner == null) return null;
        Class<?> c = owner.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(field);
                f.setAccessible(true);
                return f.get(owner);
            } catch (NoSuchFieldException | IllegalAccessException ignored) { }
            c = c.getSuperclass();
        }
        return null;
    }

    static Object tryInvoke(Object target, String method, Class<?>[] types, Object[] args) {
        if (target == null) return null;
        Class<?> c = target.getClass();
        while (c != null) {
            try {
                Method m = c.getMethod(method, types);
                m.setAccessible(true);
                return m.invoke(target, args);
            } catch (NoSuchMethodException e) { c = c.getSuperclass(); }
            catch (Exception e) { return null; }
        }
        return null;
    }

    static Object tryInvoke(Object target, String method) {
        return tryInvoke(target, method, new Class<?>[0], new Object[0]);
    }

    static void dumpPipelineSafe(WebSocketSession session, Logger log) {
        try {
            Object target = session;
            for (String f : new String[]{"delegate", "session", "reactorSession", "nativeSession"}) {
                Object v = tryGetField(target, f);
                if (v != null) { target = v; break; }
            }
            Object inbound = tryGetField(target, "inbound");
            if (inbound == null) inbound = tryGetField(target, "in");
            Object ch = (inbound != null) ? tryInvoke(inbound, "channel") : tryInvoke(target, "channel");
            Object pipeline = tryInvoke(ch, "pipeline");
            if (pipeline != null) dumpPipeline(pipeline, log);
        } catch (Throwable ignored) {}
    }

    static void dumpPipeline(Object pipeline, Logger log) {
        try {
            Object namesObj = tryInvoke(pipeline, "names");
            if (namesObj instanceof Iterable) {
                StringBuilder sb = new StringBuilder("WS pipeline handlers: ");
                for (Object n : (Iterable<?>) namesObj) {
                    Object h = tryInvoke(pipeline, "get", new Class[]{String.class}, new Object[]{String.valueOf(n)});
                    sb.append('[').append(n).append(" -> ").append(h != null ? h.getClass().getName() : "null").append("] ");
                }
                if (log != null) log.warn(sb.toString());
            }
        } catch (Throwable ignored) {}
    }

    static void sleepQuiet(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
