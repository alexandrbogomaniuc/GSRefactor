package com.abs.casino.common.configuration;

import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.logkit.ThreadLog;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.configuration.ConfigurationUtils;

import java.io.File;
import java.io.FileReader;
import java.net.URL;

/**
 * User: flsh
 * Date: 9/15/11
 */
public class ConfigHelper {
    private static final int BUFFER_SIZE = 1024;
    private static ConfigHelper instance = new ConfigHelper();
    private final XStream xstream;

    public static ConfigHelper getInstance() {
        return instance;
    }

    private ConfigHelper() {
        xstream = new XStream(); //use XPP3 xml parser
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypesByWildcard(new String[] {"com.dgphoenix.casino.**"});
        xstream.autodetectAnnotations(true);
    }

    public void registerAlias(Class klazz) {
        xstream.processAnnotations(klazz);
    }

    public String getPlainConfig(String fileName) {
        try {
            File f = ConfigurationUtils.fileFromURL(ConfigurationUtils.locate(fileName));
            return asString(f);
        } catch (CommonException e) {
            ThreadLog.error("Error getting config:" + fileName, e);
        }

        return null;
    }

    public String getFilePath(String fileName) {
        URL url = ConfigurationUtils.locate(fileName);
        return url == null ? null : url.getPath();
    }

    public IXmlConfig getConfig(String fileName) {
        try {
            File f = ConfigurationUtils.fileFromURL(ConfigurationUtils.locate(fileName));
            return (IXmlConfig) xstream.fromXML(asString(f));
        } catch (CommonException e) {
            ThreadLog.error("Error getting config:" + fileName, e);
        }

        return null;
    }

    public <T extends IXmlConfig> T getConfig(Class<T> klass) {
        String name = klass.getSimpleName() + ".xml";
        try {
            registerAlias(klass);
            File f = ConfigurationUtils.fileFromURL(ConfigurationUtils.locate(name));
            return (T) xstream.fromXML(asString(f));
        } catch (CommonException e) {
            ThreadLog.error("Error getting config:" + name, e);
        }

        return null;
    }

    public IXmlConfig getConfigFromPlainXml(String xml) {
        return (IXmlConfig) xstream.fromXML(xml);
    }

    public String toString(IXmlConfig config) {
        return xstream.toXML(config);
    }

    public static String asString(File file)
            throws CommonException {
        StringBuilder sb = new StringBuilder();
        try {
            FileReader is = new FileReader(file);
            char[] buffer = new char[BUFFER_SIZE];
            for (int n = is.read(buffer); n >= 0; n = is.read(buffer)) {
                sb.append(buffer, 0, n);
            }
            is.close();
        } catch (Exception e) {
            throw new CommonException("Error reading file " + file, e);
        }
        return sb.toString();
    }


}
