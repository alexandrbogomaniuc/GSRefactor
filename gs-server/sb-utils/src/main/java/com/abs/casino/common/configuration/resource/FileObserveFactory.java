package com.abs.casino.common.configuration.resource;

import com.abs.casino.common.configuration.resource.observable.IFileObservable;
import com.abs.casino.common.configuration.resource.observable.PropertyObservable;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

public class FileObserveFactory {
    public static final String CASINO_CONFIGURATION_PROPERTIES = "CasinoConfiguration.properties";

    public static final String FACEBOOK_CONFIGURATION_PROPERTIES = "FacebookConfiguration.properties";

    private final Map<String, IFileObservable> observableFileToManager = new HashMap<String, IFileObservable>();

    private final static FileObserveFactory instance = new FileObserveFactory();

    private FileObserveFactory() {
    }

    public static FileObserveFactory getInstance() {
        return instance;
    }

    public void createObservableFileManager(String bundleName) throws CommonException {
        PropertyObservable observableFileManeger = new PropertyObservable(bundleName);
        observableFileToManager.put(bundleName, observableFileManeger);
    }

    public void removeObservableFileManager(String bundleName) {
        IFileObservable observableFileManager = this.observableFileToManager.get(bundleName);
        if (observableFileManager != null) {
            observableFileManager.clear();
            this.observableFileToManager.remove(bundleName);
        }
    }

    public IFileObservable getFileObservableManager(String bundleName) {
        return this.observableFileToManager.get(bundleName);
    }

    public void updateProperty(String bundleName) {
        IFileObservable observableFileManager = observableFileToManager.get(bundleName);
        observableFileManager.fileModified();
    }

    public boolean containsObservableFileManager(String bundleName) {
        return CollectionUtils.isEmpty(observableFileToManager) ? false :
                observableFileToManager.containsKey(bundleName);
    }
}
