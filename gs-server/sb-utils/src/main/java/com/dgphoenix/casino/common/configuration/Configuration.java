package com.abs.casino.common.configuration;

import com.abs.casino.common.configuration.resource.FileObserveFactory;
import com.abs.casino.common.configuration.resource.event.PropertyChangedEvent;
import com.abs.casino.common.configuration.resource.listener.IPropertyListener;
import com.abs.casino.common.configuration.resource.observable.IFileObservable;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.exception.ObjectNotFoundException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import com.dgphoenix.casino.common.util.ConcurrentBidirectionalMap;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import com.dgphoenix.casino.common.util.string.StringUtils;

import java.util.*;

public class Configuration implements IPropertyListener {
    private static final Logger LOG = Logger.getLogger(Configuration.class);
    private String bundleName;
    private PropertiesConfiguration bundle;
    protected final ConcurrentBidirectionalMap<String, String> properties;

    public Configuration(String bundleName) {
        this.properties = new ConcurrentBidirectionalMap<String, String>();
        if (StringUtils.isTrimmedEmpty(bundleName)) {
            throw new RuntimeException("bundle name is empty");
        }

        this.bundleName = bundleName;
        try {
            bundle = new PropertiesConfiguration(this.bundleName);
            bundle.load();
            initProperties();
        } catch (ConfigurationException e) {
            throw new RuntimeException("Cannot init bundle", e);
        }
/*
        try {
            registerListener(bundleName);
        } catch (Throwable e) {
            LOG.error("Configuration::constructor error:", e);
            throw new RuntimeException(e);
        }
*/
    }

    private void initProperties() {
        Iterator iterator = bundle.getKeys();
        for (; iterator.hasNext();) {
            String key = (String) iterator.next();
            String newValue = bundle.getString(key);
            this.properties.put(key, newValue);
        }
    }

    public int getIntProperty(String name) throws CommonException {
        try {
            return Integer.parseInt(getProperty(name));
        } catch (Exception e) {
            throw new CommonException("No such property: " + name + " or type mismatch", e);
        }
    }

    public long getLongProperty(String name) throws CommonException {
        try {
            return Long.valueOf(getProperty(name));
        } catch (Exception e) {
            throw new CommonException("No such property: " + name + " or type mismatch", e);
        }
    }

    public String getStringPropertySilent(String name) {
        return getPropertySilent(name);
    }

    public String getStringProperty(String name) throws CommonException {
        return getProperty(name);
    }

    public boolean getBooleanProperty(String name) {
        String result = properties.get(name);
        return result != null && (Boolean.TRUE.toString().equalsIgnoreCase(result) || "1".equals(result) );
    }

    public double getDoubleProperty(String name) throws CommonException {
        double result;
        try {
            result = Double.parseDouble(getProperty(name));
        } catch (Exception e) {
            throw new CommonException("No such property: " + name + " or type mismatch", e);
        }
        return result;
    }

    public float getFloatProperty(String name) throws CommonException {
        float result;
        try {
            result = Float.parseFloat(getProperty(name));
        } catch (Exception e) {
            throw new CommonException("No such property: " + name + " or type mismatch", e);
        }
        return result;
    }

    public List<Long> getLongsList(String key, String delim) throws CommonException {
        List<String> strings = getStringsList(key, delim);
        if (CollectionUtils.isEmpty(strings)) {
            return Collections.emptyList();
        }
        List<Long> longs = new ArrayList<Long>(strings.size());
        try {
            for (String string : strings) {
                longs.add(Long.parseLong(string));
            }
            return longs;
        } catch (NumberFormatException e) {
            throw new CommonException(e);
        }
    }

    public List<String> getStringsList(String key, String delim) throws CommonException {
        String str = getStringPropertySilent(key);
        if(StringUtils.isTrimmedEmpty(str)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(str, delim);
        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken());
        }

        return result;
    }

    public String getKey(String value) throws CommonException {
        String result = properties.getKeyForValue(value);
        if (StringUtils.isTrimmedEmpty(result)) {
            throw new ObjectNotFoundException("Value:" + value + " not found");
        }
        return result.trim();
    }

    private String getProperty(String name) throws CommonException {
        String result = properties.get(name);
        if (StringUtils.isTrimmedEmpty(result)) {
            throw new ObjectNotFoundException("Property:" + name + " not found");
        }
        
        return result.trim();
    }

    private String getPropertySilent(String name) {
        String result = properties.get(name);
        if (StringUtils.isTrimmedEmpty(result)) {
            return null;
        }
        return result.trim();
    }


    @Override
    public void propertyChanged(PropertyChangedEvent event) {
        properties.put(event.getPropertyName(), event.getNewValue());
    }

    @Override
    @Deprecated
    public void registerListener(String bundleName) {
        IFileObservable casinoConfObservable = FileObserveFactory.getInstance().getFileObservableManager(bundleName);
        if(casinoConfObservable == null) {
            throw new RuntimeException("Cannot find FileObservableManager for bundle: " + bundleName);
        }
        casinoConfObservable.addListener(this);
    }
}
