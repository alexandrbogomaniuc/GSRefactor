package com.abs.casino.support.configuration;

import com.dgphoenix.casino.common.config.GameServerConfigTemplate;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.Pair;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.apache.commons.lang.StringUtils.uncapitalize;

/**
 * User: isirbis
 * Date: 21.08.14
 */
public class ServerConfigurationForm extends ActionForm {
    public static final Logger LOG = LogManager.getLogger(ServerConfigurationForm.class);
    public static final String ERROR_BAD_CMD = "error.serverConfiguration.badCmd";
    public static final String INCORRECT_PROPERTY = "incorrect_property";
    public static final String PROPERTY_CMD_ERROR = "cmd_error";

    private String action;
    private String serverConfigurationId;
    private Boolean isNewServer = false;
    private Boolean allServers = false;
    private final ArrayList<String> cmds = new ArrayList<>();
    private final Map<String, Object> properties = new HashMap<>();
    private final Map<String, Object> fields = new HashMap<>();

    /**
     * Field methods
     */

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setCmd(int index, String value) {
        this.cmds.add(value);
    }

    public String getServerConfigurationId() {
        return this.serverConfigurationId;
    }

    public void setServerConfigurationId(String serverConfigurationId) {
        this.serverConfigurationId = serverConfigurationId;
    }

    public Boolean getIsNewServer() {
        return isNewServer;
    }

    public void setIsNewServer(Boolean isNew) {
        this.isNewServer = isNew;
    }

    public Boolean getAllServers() {
        return allServers;
    }

    public void setAllServers(Boolean allServers) {
        this.allServers = allServers;
    }

    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    public void setProperty(String key, Object value) {
        this.properties.put(key.trim(), value.toString().trim());
    }

    public Object getField(String key) {
        return this.fields.get(key);
    }

    public void setField(String key, Object value) {
        this.fields.put(key, value.toString().trim());
    }

    public List<ServerStatus> getServersList() {
        return ServerConfigurationAction.getServersList();
    }

    public List<Pair<String, String>> getCasinoSystemTypesList() {
        List<Pair<String, String>> list = new LinkedList<>();
        for (Pair<String, Integer> entry : ServerConfigurationAction.getCasinoSystemTypesList()) {
            list.add(new Pair<>(entry.getKey(), entry.getValue().toString()));
        }
        return list;
    }

    /**
     * Common methods
     */

    public List<String> getCmds() {
        return this.cmds;
    }

    public Map<String, Object> getMapProperties() {
        return this.properties;
    }

    public Map<String, Object> getFieldProperties() {
        return this.fields;
    }

    public Short getLongServerConfigurationId() throws CommonException {
        short serverId = -1;
        try {
            serverId = Short.parseShort(this.serverConfigurationId.substring(1));
        } catch (NumberFormatException e) {
            throw new CommonException("Number format exception when parsing serverId. RAW ServerId: " + serverId);
        }
        return serverId;
    }

    public String getTypeServerConfiguration() {
        return this.serverConfigurationId.substring(0, 1);
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (!StringUtils.isTrimmedEmpty(action) &&
                !ServerConfigurationAction.ACTION_GET_CONFIGURATION.equalsIgnoreCase(action) &&
                !ServerConfigurationAction.ACTION_SET_CONFIGURATION.equalsIgnoreCase(action)) {
            errors.add("action_error", new ActionMessage("error.serverConfiguration.badAction", this.action));
        }

        if (!StringUtils.isTrimmedEmpty(action) && cmds.isEmpty()) {
            errors.add(PROPERTY_CMD_ERROR, new ActionMessage("error.serverConfiguration.emptyCmd"));
        } else if (!cmds.isEmpty()) {
            for (String cmd : cmds) {
                if (ServerConfigurationAction.CMD_GET_MAP_PROPERTIES.equalsIgnoreCase(cmd)) {
                    continue;
                }
                if (ServerConfigurationAction.CMD_SET_MAP_PROPERTIES.equalsIgnoreCase(cmd)) {
                    continue;
                }
                if (ServerConfigurationAction.CMD_GET_FIELD_PROPERTIES.equalsIgnoreCase(cmd)) {
                    continue;
                }
                if (ServerConfigurationAction.CMD_SET_FIELD_PROPERTIES.equalsIgnoreCase(cmd)) {
                    continue;
                }
                errors.add(PROPERTY_CMD_ERROR, new ActionMessage(ERROR_BAD_CMD, cmd));
            }
        }

        if (cmds.contains(ServerConfigurationAction.CMD_SET_MAP_PROPERTIES) && properties.isEmpty()) {
            errors.add(PROPERTY_CMD_ERROR, new ActionMessage(ERROR_BAD_CMD, ServerConfigurationAction.CMD_SET_MAP_PROPERTIES));
        }

        if (cmds.contains(ServerConfigurationAction.CMD_SET_FIELD_PROPERTIES) && fields.isEmpty()) {
            errors.add(PROPERTY_CMD_ERROR, new ActionMessage(ERROR_BAD_CMD, ServerConfigurationAction.CMD_SET_FIELD_PROPERTIES));
        }

        if (cmds.contains(ServerConfigurationAction.CMD_SET_FIELD_PROPERTIES) && !fields.isEmpty()) {
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                String propertyName = entry.getKey();
                Class<?> type = null;
                try {
                    String fieldName = uncapitalize(propertyName);
                    type = GameServerConfigTemplate.class.getDeclaredField(fieldName).getType();
                    if (type.isPrimitive()) {
                        Object value = ServerConfigurationAction.getPrimitive(type, entry.getValue().toString());
                        if (fieldName.equals("freeBalance") && (Long) value < 1) {
                            errors.add(INCORRECT_PROPERTY,
                                    new ActionMessage("error.serverConfiguration.lessThenOne", entry.getKey(), value));
                        }
                    }
                } catch (NoSuchFieldException e) {
                    errors.add(INCORRECT_PROPERTY,
                            new ActionMessage("error.serverConfiguration.incorrect.FieldPropertyName", entry.getKey()));
                } catch (Exception e) {
                    errors.add(INCORRECT_PROPERTY,
                            new ActionMessage("error.serverConfiguration.incorrect.FieldProperty", entry.getKey(), type.getCanonicalName()));
                }
            }
        }

        if (getServerConfigurationId() != null) {
            try {
                getLongServerConfigurationId();
            } catch (CommonException e) {
                errors.add("server_configuration_error",
                        new ActionMessage("error.serverConfiguration.badServerConfigurationId"));
            }
        }

        return errors;
    }
}
