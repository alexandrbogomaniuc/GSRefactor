package com.dgphoenix.casino.support.configuration;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.ServerConfigsCache;
import com.dgphoenix.casino.common.cache.ServerConfigsTemplateCache;
import com.dgphoenix.casino.common.config.GameServerConfig;
import com.dgphoenix.casino.common.config.GameServerConfigTemplate;
import com.dgphoenix.casino.common.configuration.CasinoSystemType;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.gs.persistance.remotecall.RemoteCallHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

/**
 * User: isirbis
 * Date: 21.08.14
 */

/**
 * Tool for setting and getting server map properties and field properties.
 * Getting and setting field properties from server configuration by using java reflection.
 * Also tool for adding new server configuration for GS and Lobby.
 */
public class ServerConfigurationAction extends Action {
    private static final Logger LOG = LogManager.getLogger(ServerConfigurationAction.class);

    private static final Class[] EMPTY_DECLARED_METHOD = new Class[]{};
    private static final Long DEFAULT_TEMPLATE_SERVER_ID = 0l;

    /**
     * Commands for actions
     */
    public static final String CMD_GET_MAP_PROPERTIES = "getMapProperties";
    public static final String CMD_SET_MAP_PROPERTIES = "setMapProperties";

    public static final String CMD_GET_FIELD_PROPERTIES = "getFieldProperties";
    public static final String CMD_SET_FIELD_PROPERTIES = "setFieldProperties";

    /**
     * Actions
     */
    public static final String ACTION_GET_CONFIGURATION = "getConfiguration";
    public static final String ACTION_SET_CONFIGURATION = "setConfiguration";

    /**
     * Attributes
     */
    private static final String ATTRIBUTE_MAP_PROPERTIES = "mapProperties";
    private static final String ATTRIBUTE_UPDATED_MAP_PROPERTIES = "updatedMapProperties";
    private static final String ATTRIBUTE_UPDATE_MAP_PROPERTIES_IS_OK = "UpdatedMapPropertiesIsOK";

    private static final String ATTRIBUTE_FIELD_PROPERTIES = "fieldProperties";
    private static final String ATTRIBUTE_UPDATED_FIELD_PROPERTIES = "updatedFieldProperties";
    private static final String ATTRIBUTE_UPDATE_FIELD_PROPERTIES_IS_OK = "UpdatedFieldPropertiesIsOK";

    private static final String ATTRIBUTE_IS_NEW_SERVER = "isNewServer";
    private static final String ATTRIBUTE_COMMON_ERROR = "CommonError";

    /**
     * Forwards
     */
    private static final String FORWARD_DEFAULT = "main";

    /**
     * GameServerConfig methods prefix
     */
    private static final String METHOD_SET_PREFIX = "set";
    private static final String METHOD_GET_PREFIX = "get";
    private static final String METHOD_IS_PREFIX = "is";


    private static final String PREFIX_GAME_SERVER = "G";

    private static final List<String> excludedMethodsList = new LinkedList<>();

    private static final Map<Integer, CasinoSystemType> casinoSystemTypesList = new HashMap<>();

    static {
        CasinoSystemType[] types = CasinoSystemType.values();
        for (int i = 0; i < types.length; i++) {
            casinoSystemTypesList.put(i, types[i]);
        }

        // all
        excludedMethodsList.add("setProperty");
        excludedMethodsList.add("setProperties");
        excludedMethodsList.add("setSubnetUtils");

        excludedMethodsList.add("setTrustedIp");
        excludedMethodsList.add("setCasinoSystemType");

        excludedMethodsList.add("setWinnerFeedBanks");
    }

    public static final String CLASS_FIELD_TRUSTED_IP = "TrustedIp";
    public static final String CLASS_FIELD_CASINO_SYSTEM_TYPE = "CasinoSystemType";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
                                 HttpServletResponse response) throws Exception {

        ServerConfigurationForm configurationForm = (ServerConfigurationForm) form;

        // default forward
        String forward = FORWARD_DEFAULT;

        // if not select any action
        if (configurationForm.getAction() == null) {
            return mapping.findForward(forward);
        }

        try {
            Map<String, Object> attributes = new HashMap<String, Object>();
            Short serverId = configurationForm.getLongServerConfigurationId();
            if (serverId == null) {
                throw new CommonException("Don't chosen game server ID");
            }

            String serverType = configurationForm.getTypeServerConfiguration();

            GameServerConfigTemplate config = null;
            if (configurationForm.getIsNewServer()) {
                config = getConfiguration(serverId, serverType);
                attributes.put(ATTRIBUTE_IS_NEW_SERVER, true);
            } else {
                if (serverType.equalsIgnoreCase(PREFIX_GAME_SERVER)) {
                    config = ServerConfigsTemplateCache.getInstance().getServerConfigTemplate();
                }
            }
            if (config == null) {
                throw new CommonException("Could not found game server");
            }
            LOG.debug("Class ServerConfig name: {}", config.getClass().getSimpleName());
            LOG.debug("COMMANDS: {}", configurationForm.getCmds());
            if (ACTION_GET_CONFIGURATION.equalsIgnoreCase(configurationForm.getAction())) {
                for (String cmd : configurationForm.getCmds()) {
                    if (CMD_GET_MAP_PROPERTIES.equalsIgnoreCase(cmd)) {
                        attributes.put(ATTRIBUTE_MAP_PROPERTIES, new TreeMap<>(getMapProperties(config)));
                        continue;
                    }
                    if (CMD_GET_FIELD_PROPERTIES.equalsIgnoreCase(cmd)) {
                        attributes.put(ATTRIBUTE_FIELD_PROPERTIES, getFieldProperties(config));
                    }
                }

                setAttributes(attributes, request, "Could not get game server configuration");
                return mapping.findForward(forward);
            }

            Set<GameServerConfigTemplate> setServersConfig = null;

            if (ACTION_SET_CONFIGURATION.equalsIgnoreCase(configurationForm.getAction())) {
                for (String cmd : configurationForm.getCmds()) {
                    if (CMD_SET_MAP_PROPERTIES.equalsIgnoreCase(cmd)) {
                        List<String> updatedProperties = null;
                        boolean updateIsOK = true;
                        try {
                            updatedProperties = setMapProperties(config, configurationForm.getMapProperties());
                        } catch (Exception e) {
                            updateIsOK = false;
                            LOG.debug("Could not update map properties", e);
                        }
                        attributes.put(ATTRIBUTE_UPDATE_MAP_PROPERTIES_IS_OK, updateIsOK);
                        attributes.put(ATTRIBUTE_MAP_PROPERTIES, new TreeMap<>(getMapProperties(config)));
                        attributes.put(ATTRIBUTE_UPDATED_MAP_PROPERTIES, updatedProperties);
                        continue;
                    }
                    if (CMD_SET_FIELD_PROPERTIES.equalsIgnoreCase(cmd)) {
                        List<String> updatedFieldProperties = null;
                        Boolean updateIsOK = true;
                        try {
                            updatedFieldProperties = setFieldProperties(config, configurationForm.getFieldProperties());
                        } catch (Exception e) {
                            updateIsOK = false;
                            LOG.debug("Could not update field properties", e);
                        }
                        attributes.put(ATTRIBUTE_UPDATE_FIELD_PROPERTIES_IS_OK, updateIsOK);
                        attributes.put(ATTRIBUTE_FIELD_PROPERTIES, getFieldProperties(config));
                        attributes.put(ATTRIBUTE_UPDATED_FIELD_PROPERTIES, updatedFieldProperties);
                    }
                }

                if (configurationForm.getIsNewServer()) {
                    LOG.debug("Added new server");
                } else {
                    RemoteCallHelper.getInstance().saveAndSendNotification(config);
                }
                setAttributes(attributes, request, "Could not set game server configuration");
                return mapping.findForward(forward);
            }

        } catch (CommonException e) {
            request.setAttribute(ATTRIBUTE_COMMON_ERROR, e.getMessage());
            LOG.warn(e.getMessage(), e);
        }
        return mapping.findForward(forward);
    }

    private void setAttributes(Map<String, Object> attributes, HttpServletRequest request, String msg) throws CommonException {
        try {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            throw new CommonException(msg, e);
        }
    }

    /**
     * Getter for map properties
     *
     * @param config Server configuration
     * @return Map properties for server configuration
     * @throws com.dgphoenix.casino.common.exception.CommonException
     */
    private Map<String, String> getMapProperties(IDistributedCacheEntry config) throws CommonException {
        if (config.getClass().getSimpleName().equalsIgnoreCase(GameServerConfigTemplate.class.getSimpleName())) {
            return getMapProperties((GameServerConfigTemplate) config);
        }

        return null;
    }

    /**
     * Getter for game server map properties
     *
     * @param config Game server configuration
     * @return Value of property
     * @throws com.dgphoenix.casino.common.exception.CommonException
     */
    private Map<String, String> getMapProperties(GameServerConfigTemplate config) throws CommonException {
        return config.getProperties();
    }

    /**
     * Setter for map properties
     *
     * @param config     Server configuration
     * @param properties Map properties
     * @return List of updater properties (added, updated or removed)
     */
    private List<String> setMapProperties(IDistributedCacheEntry config, Map<String, Object> properties) {
        List<String> updatedProperties = new LinkedList<String>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String value = entry.getValue().toString();

            // remove map properties is disabled
            if (value == null || value.equalsIgnoreCase("null")) {
                //value = null;
                continue;
            }

            updatedProperties.add(entry.getKey());

            if (config.getClass().getSimpleName().equalsIgnoreCase(GameServerConfigTemplate.class.getSimpleName())) {
                setProperty((GameServerConfigTemplate) config, entry.getKey(), value);
                continue;
            }
        }

        return updatedProperties;
    }

    /**
     * Setter for game server map properties
     *
     * @param config   Game server configuration
     * @param property Property name in map
     * @param value    Value of property
     */
    private void setProperty(GameServerConfigTemplate config, String property, String value) {
        LOG.debug("SET MAP@ Key: " + property +
                "; OldValue: " + config.getProperty(property) + "; NewValue: " + value);
        config.setProperty(property, value);
    }

    /**
     * Getter for field properties
     *
     * @param config Server configuration
     * @return Field properties
     * @throws com.dgphoenix.casino.common.exception.CommonException
     */
    private Map<String, Pair<String, ServerConfigurationFieldType>> getFieldProperties(IDistributedCacheEntry config) throws CommonException {
        Map<String, Pair<String, ServerConfigurationFieldType>> properties =
                new HashMap<String, Pair<String, ServerConfigurationFieldType>>();
        try {
            String className;
            if (config.getClass().getSimpleName().equalsIgnoreCase(GameServerConfigTemplate.class.getSimpleName())) {
                className = GameServerConfigTemplate.class.getName();
            } else {
                throw new CommonException("Could not get class name");
            }

            Method[] methods = Class.forName(className).getDeclaredMethods();
            List<String> getterMethodsName = new LinkedList<String>();

            // get all fileds which can be changed
            for (Method entry : methods) {
                String methodName = entry.getName();
                if (methodName.startsWith(METHOD_SET_PREFIX) && !excludedMethodsList.contains(methodName)) {
                    getterMethodsName.add(methodName.substring(METHOD_SET_PREFIX.length()));
                }
            }

            // get all methods for fields which can be changed
            for (Method entry : methods) {
                String methodName = entry.getName();
                if (methodName.startsWith(METHOD_GET_PREFIX) &&
                        getterMethodsName.contains(methodName.substring(METHOD_GET_PREFIX.length()))) {
                    properties.put(
                            entry.getName().substring(METHOD_GET_PREFIX.length()),
                            new Pair(
                                    getResultInvokeMethod(entry, config),
                                    entry.getReturnType().equals(Boolean.TYPE) ?
                                            ServerConfigurationFieldType.CHECKBOX :
                                            ServerConfigurationFieldType.TEXT
                            )
                    );
                    continue;
                }
                if (methodName.startsWith(METHOD_IS_PREFIX) &&
                        getterMethodsName.contains(methodName.substring(METHOD_IS_PREFIX.length()))) {
                    properties.put(
                            entry.getName().substring(METHOD_IS_PREFIX.length()),
                            new Pair(
                                    getResultInvokeMethod(entry, config),
                                    entry.getReturnType().equals(Boolean.TYPE) ?
                                            ServerConfigurationFieldType.CHECKBOX :
                                            ServerConfigurationFieldType.TEXT
                            )
                    );
                    continue;
                }

                // Method: getTrustedIp
                if (methodName.compareTo(METHOD_GET_PREFIX + CLASS_FIELD_TRUSTED_IP) == 0) {
                    StringBuilder valueString = new StringBuilder();
                    List<String> list = getTrustedIp(config);
                    if (list != null) {
                        for (String ipEntry : list) {
                            valueString.append(ipEntry).append(";");
                        }
                    }
                    properties.put(
                            entry.getName().substring(METHOD_GET_PREFIX.length()),
                            new Pair(valueString.toString(), ServerConfigurationFieldType.TEXT)
                    );
                    continue;
                }

                // Method: getCasinoSystemType
                if (methodName.compareTo(METHOD_GET_PREFIX + CLASS_FIELD_CASINO_SYSTEM_TYPE) == 0) {
                    CasinoSystemType type = getCasinoSystemType(config);
                    for (Map.Entry<Integer, CasinoSystemType> typeEntry : casinoSystemTypesList.entrySet()) {
                        if (typeEntry.getValue().equals(type)) {
                            properties.put(
                                    entry.getName().substring(METHOD_GET_PREFIX.length()),
                                    new Pair(typeEntry.getKey().toString(), ServerConfigurationFieldType.SELECT)
                            );
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new CommonException("Could not get field properties", e);
        }

        return properties;
    }

    /**
     * Getter for field property "trustedIp" in server configuration
     *
     * @param config Server configuration
     * @return Value for field
     */
    private List<String> getTrustedIp(IDistributedCacheEntry config) {
        if (config.getClass().getSimpleName().equalsIgnoreCase(GameServerConfigTemplate.class.getSimpleName())) {
            return getTrustedIp((GameServerConfigTemplate) config);
        }

        return null;
    }

    /**
     * Getter for field property "trustedIp" in {@link com.dgphoenix.casino.common.config.GameServerConfig}
     *
     * @param config Game server configuration
     * @return Value for field
     */
    private List<String> getTrustedIp(GameServerConfigTemplate config) {
        return config.getTrustedIp();
    }

    /**
     * Getter for field property "casinoSystemType" in server configuration
     *
     * @param config Server configuration
     * @return Value for field
     */
    private CasinoSystemType getCasinoSystemType(IDistributedCacheEntry config) {
        if (config.getClass().getSimpleName().equalsIgnoreCase(GameServerConfigTemplate.class.getSimpleName())) {
            return getCasinoSystemType((GameServerConfigTemplate) config);
        }

        return null;
    }

    /**
     * Getter for field property "casinoSystemType" in {@link com.dgphoenix.casino.common.config.GameServerConfig}
     *
     * @param config Game server configuration
     * @return Value for property
     */
    private CasinoSystemType getCasinoSystemType(GameServerConfigTemplate config) {
        return config.getCasinoSystemType();
    }

    /**
     * Setter for field properties
     *
     * @param config     Server configuration
     * @param properties Field properties
     * @return List of updater field properties (only updated)
     * @throws com.dgphoenix.casino.common.exception.CommonException
     */
    private List<String> setFieldProperties(IDistributedCacheEntry config, Map<String, Object> properties) throws CommonException {
        List<String> updatedFields = new LinkedList<String>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String propertyName = entry.getKey();
            try {
                String className;
                if (config.getClass().getSimpleName().equalsIgnoreCase(GameServerConfigTemplate.class.getSimpleName())) {
                    className = GameServerConfigTemplate.class.getName();
                } else {
                    throw new CommonException("Could not get class name");
                }

                Class<?> type = Class.forName(className).getDeclaredField(
                        StringUtils.uncapitalize(propertyName)).getType();
                Method method = Class.forName(className)
                        .getDeclaredMethod(METHOD_SET_PREFIX + propertyName, new Class[]{type});

                Object propertyValue = entry.getValue();

                // Method: setTrustedIp
                if (method.getName().compareTo(METHOD_SET_PREFIX + CLASS_FIELD_TRUSTED_IP) == 0) {
                    List<String> list = new LinkedList<String>();
                    Collections.addAll(list, ((String) propertyValue).split(";"));
                    propertyValue = list;
                }

                // Method: setCasinoSystemType
                if (method.getName().compareTo(METHOD_SET_PREFIX + CLASS_FIELD_CASINO_SYSTEM_TYPE) == 0) {
                    propertyValue = casinoSystemTypesList.get(Integer.parseInt((String) propertyValue));
                }

                if (type.isPrimitive()) {
                    propertyValue = getPrimitive(type, propertyValue.toString());
                } else {
                    if (propertyValue.toString().equalsIgnoreCase("null")) {
                        propertyValue = null;
                    }
                }

                Method getter;
                try {
                    getter = Class.forName(className)
                            .getDeclaredMethod(METHOD_GET_PREFIX + propertyName, EMPTY_DECLARED_METHOD);
                } catch (NoSuchMethodException e) {
                    try {
                        getter = Class.forName(className)
                                .getDeclaredMethod(METHOD_IS_PREFIX + propertyName, EMPTY_DECLARED_METHOD);
                    } catch (NoSuchMethodException ex) {
                        throw new CommonException("Not found getter method", e);
                    }
                }
                LOG.debug("UPDATE FIELD@ FieldName: " + propertyName
                        + "; OldValue: " + getResultInvokeMethod(getter, config) + "; NewValue: " + propertyValue);
                updatedFields.add(propertyName);
                method.invoke(config, propertyValue);
            } catch (Exception e) {
                throw new CommonException("Could not set field properties", e);
            }
        }
        return updatedFields;
    }

    /**
     * Invoke method for getter and setter for field properties
     *
     * @param method Method for invoke
     * @param config Server configuration
     * @return Result of invoke method
     * @throws java.lang.reflect.InvocationTargetException
     * @throws IllegalAccessException
     */
    private String getResultInvokeMethod(Method method, IDistributedCacheEntry config)
            throws InvocationTargetException, IllegalAccessException {
        Object resultInvoke = method.invoke(config, (Object[]) null);
        String result = "null";
        if (resultInvoke != null) {
            result = String.valueOf(resultInvoke);
        }
        return result;
    }

    /**
     * Parser primitives
     *
     * @param type  Type of primitive
     * @param value Value
     * @return Parsed value
     */
    public static Object getPrimitive(Class type, String value) {
        Object arg = 0;
        if (type.equals(Long.TYPE)) {
            arg = Long.parseLong(value);
        }
        if (type.equals(Integer.TYPE)) {
            arg = Integer.parseInt(value);
        }
        if (type.equals(Boolean.TYPE)) {
            arg = Boolean.TRUE.toString().equalsIgnoreCase(value);
        }
        return arg;
    }

    /**
     * Getter server configuration when adding new server configuration
     *
     * @param serverId   When adding new server configuration by using existing
     *                   this value does not must equal DEFAULT_TEMPLATE_SERVER_ID
     * @param serverType Type of adding server configuration
     * @return Server configuration existing or default
     * @throws com.dgphoenix.casino.common.exception.CommonException
     */
    private GameServerConfigTemplate getConfiguration(Short serverId, String serverType) throws CommonException {
        GameServerConfigTemplate config = null;
        GameServerConfigTemplate newServerConfig = null;

        if (serverType.equalsIgnoreCase(PREFIX_GAME_SERVER)) {
            if (serverId.equals(DEFAULT_TEMPLATE_SERVER_ID)) {
                return getDefaultTemplateGSConfiguration();
            }

            // from exists
            config = ServerConfigsTemplateCache.getInstance().getServerConfigTemplate();
            newServerConfig = new GameServerConfigTemplate();
        }

        Map<String, Object> mapProperties = new HashMap<>();
        for (Map.Entry<String, String> entry : getMapProperties(config).entrySet()) {
            mapProperties.put(entry.getKey(), entry.getValue());
        }

        Map<String, Object> fieldProperties = new HashMap<>();
        for (Map.Entry<String, Pair<String, ServerConfigurationFieldType>> entry : getFieldProperties(config).entrySet()) {
            fieldProperties.put(entry.getKey(), entry.getValue().getKey());
        }

        setMapProperties(newServerConfig, mapProperties);
        setFieldProperties(newServerConfig, fieldProperties);

        return newServerConfig;
    }

    /**
     * Getter default game server configuration
     * Here need adding new map properties for default game server configuration
     *
     * @return Default game server configuration
     */
    private GameServerConfigTemplate getDefaultTemplateGSConfiguration() {
        GameServerConfigTemplate config = new GameServerConfigTemplate();

        // default values
        config.setProperty("SHOW_MESSAGE_IN_TEST_MODE", "false");
        config.setProperty("IS_USE_CASSANDRA_PLAYER_SESSION_PERSISTER", "false");

        return config;
    }

    public static List<ServerStatus> getServersList() {
        List<ServerStatus> serversList = new LinkedList<>();

        // add default servers
        //serversList.add(new Pair<>("DEFAULT_GS_SERVER", PREFIX_GAME_SERVER + String.valueOf(DEFAULT_TEMPLATE_SERVER_ID)));

        // get all game servers
        for (Entry<Integer, GameServerConfig> server : ServerConfigsCache.getInstance()
                .getAllObjects().entrySet()) {
            serversList.add(new ServerStatus("GS" + server.getKey(), server.getValue().isOnline(),
                    server.getValue().isMaster()));
        }

        return serversList;
    }

    public static List<Pair<String, Integer>> getCasinoSystemTypesList() {
        List<Pair<String, Integer>> list = new LinkedList<>();
        for (Map.Entry<Integer, CasinoSystemType> entry : casinoSystemTypesList.entrySet()) {
            list.add(new Pair<>(entry.getValue().name(), entry.getKey()));
        }
        return list;
    }

    public enum ServerConfigurationFieldType {

        CHECKBOX(0),
        SELECT(1),
        TEXT(2);

        private int type;

        private ServerConfigurationFieldType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }
    }
}
