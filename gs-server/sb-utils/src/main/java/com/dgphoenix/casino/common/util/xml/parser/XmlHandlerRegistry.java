/**
 * User: val
 * Date: Jan 29, 2003
 * Time: 8:38:06 PM
 */
package com.abs.casino.common.util.xml.parser;

import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.exception.ObjectNotFoundException;
import com.abs.casino.common.util.ReflectionUtils;
import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;

public class XmlHandlerRegistry implements IXmlHandlerRegistry {
    private static final Logger LOG = Logger.getLogger(XmlHandlerRegistry.class);


    protected final static String registryName = "XmlHandlers.properties";
    private final static IXmlHandlerRegistry instance = new XmlHandlerRegistry(registryName);

    private Map <String, IXmlHandler> handlersClass = new HashMap<String, IXmlHandler>();

    private XmlHandlerRegistry(String bundleName) {
        try {
            PropertiesConfiguration configFile = new PropertiesConfiguration(bundleName);
            configFile.load();
            Iterator iterator = configFile.getKeys();
            for (; iterator.hasNext();) {
                String key = (String) iterator.next();
                String newValue = configFile.getString(key);
                handlersClass.put(key, (XmlHandler) ReflectionUtils.forNameWithCompatibilityAliases(newValue).newInstance());
            }

        } catch (Throwable e) {
            LOG.error("XmlHandlerRegistry::constructor error: " + e);
            //throw new RuntimeException(e);
        }
    }

    public static IXmlHandlerRegistry instance() {
        return instance;
    }

    public void register(String name, IXmlHandler handler) {
        if(!handlersClass.containsKey(name)) {
            handlersClass.put(name, handler);
        } else {
            LOG.warn("Already registered, name=" + name + " , handler=" + handler);
        }
    }

    public IXmlHandler getXmlHandler(String name)
            throws ObjectNotFoundException {

        final Object handler = handlersClass.get(name);
        if (handler != null) {
            return (IXmlHandler) handler;
        }
        throw new ObjectNotFoundException("error in getXmlHandler");
    }

}
