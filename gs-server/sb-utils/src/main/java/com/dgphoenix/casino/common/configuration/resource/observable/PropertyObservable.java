package com.dgphoenix.casino.common.configuration.resource.observable;

import com.dgphoenix.casino.common.configuration.resource.event.PropertyChangedEvent;
import com.dgphoenix.casino.common.configuration.resource.listener.IPropertyListener;
import com.dgphoenix.casino.common.exception.CommonException;
import com.dgphoenix.casino.common.util.CollectionUtils;
import org.apache.log4j.Logger;
import com.dgphoenix.casino.common.util.string.StringUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PropertyObservable implements IFileObservable {
    private static final Logger LOG = Logger.getLogger(PropertyObservable.class);

    private String bundleName;
    private PropertiesConfiguration bundle;
    private final List<IPropertyListener> propertyListeners;
    private final Map<String, String> properties;

    private Runnable updaterTask;

    public PropertyObservable(String bundleName) throws CommonException {
        try {
            if (StringUtils.isTrimmedEmpty(bundleName)) {
                throw new CommonException("bundle name is empty");
            }

            this.bundleName = bundleName;

            synchronized (this) {
                bundle = new PropertiesConfiguration(bundleName);
                if (bundle == null) {
                    throw new CommonException("ResourceBundle for bundleName:" + bundleName + " is not found");
                }
                bundle.load();

                this.propertyListeners = new LinkedList<IPropertyListener>();
                this.properties = new ConcurrentHashMap<String, String>();

                this.initProperties();
            }
        } catch (CommonException e) {
            throw e;
        } catch (Throwable e) {
            throw new CommonException("cant create resource bundle", e);
        }
    }

    private void initProperties() {
        Iterator iterator = bundle.getKeys();
        for (; iterator.hasNext();) {
            String key = (String) iterator.next();
            String newValue = bundle.getString(key);
            this.properties.put(key, newValue);
        }
    }

    public void addListener(IPropertyListener listener) {
        propertyListeners.add(listener);
        setupListenerProperties(listener);
        LOG.debug("PropertyObservable[" + bundleName + "]::addListener listener:" + listener);
    }

    private void setupListenerProperties(IPropertyListener listener) {
        synchronized (this) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                listener.propertyChanged(new PropertyChangedEvent(entry.getKey(), null, entry.getValue()));
            }
        }
    }

    public void removeListener(IPropertyListener listener) {
        if (!CollectionUtils.isEmpty(propertyListeners)) {
            propertyListeners.remove(listener);
            LOG.debug("PropertyObservable[" + bundleName + "]::removeListener listener:" + listener);
        }
    }

    public void notifyListeners(String propertyName, String oldValue, String newValue) {
        if (!CollectionUtils.isEmpty(propertyListeners)) {
            LOG.debug("PropertyObservable[" + bundleName + "]::notifyListeners propertyName:" + propertyName
                    + " oldValue:" + oldValue + " newValue:" + newValue);

            PropertyChangedEvent event = new PropertyChangedEvent(propertyName, oldValue, newValue);
            for (IPropertyListener listener : propertyListeners) {
                listener.propertyChanged(event);
            }
        }
    }

    @Override
    public void fileModified() {
        synchronized (this) {
            LOG.info("PropertyObservable[" + bundleName + "]::fileModified called");
            try {
                bundle.clear();
                bundle.load();

                Iterator iterator = bundle.getKeys();
                for (; iterator.hasNext();) {
                    String key = (String) iterator.next();
                    String newValue = bundle.getString(key);
                    String storedValue = properties.get(key);

                    if (StringUtils.isTrimmedEmpty(storedValue) || !storedValue.equals(newValue)) {
                        this.properties.put(key, newValue);
                        this.notifyListeners(key, storedValue, newValue);
                    }
                }
            } catch (Throwable e) {
                LOG.error("PropertyObservable[" + bundleName + "]::fileModified error:", e);
            }
        }
    }

    @Override
    public boolean isUpdated() {
        return updaterTask == null;
    }

    @Override
    public void setUpdater(Runnable task) {
        if (updaterTask == null) {
            this.updaterTask = task;
        }
    }

    @Override
    public void clear() {
        this.properties.clear();
        this.propertyListeners.clear();

        this.bundle = null;
        LOG.info("PropertyObservable[" + bundleName + "]::cleared");
    }
}
