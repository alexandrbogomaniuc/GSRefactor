package com.dgphoenix.casino.cassandra;

import com.dgphoenix.casino.cassandra.config.ColumnFamilyConfig;
import com.dgphoenix.casino.cassandra.persist.engine.ICassandraPersister;
import com.dgphoenix.casino.common.util.ReflectionUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.09.16
 */
public class PersistersFactory {

    private static final Logger LOG = LogManager.getLogger(PersistersFactory.class);

    private List<ICassandraPersister> persisters;
    private Map<Class<? extends ICassandraPersister>, ICassandraPersister> persistersMap;
    private Map<Class<?>, List<Class<? extends ICassandraPersister>>> persistersInterfaceMap;
    private final PersisterDependencyInjector persisterDependencyInjector;

    public PersistersFactory(PersisterDependencyInjector persisterDependencyInjector) {
        this.persisterDependencyInjector = persisterDependencyInjector;
    }

    public void initializePersisters(List<ColumnFamilyConfig> configs,
                                     com.datastax.driver.core.ConsistencyLevel defaultReadConsistency,
                                     com.datastax.driver.core.ConsistencyLevel defaultWriteConsistency,
                                     com.datastax.driver.core.ConsistencyLevel defaultSerialConsistency) {
        persisters = new ArrayList<>(configs.size());
        persistersMap = new HashMap<>(configs.size());
        persistersInterfaceMap = new HashMap<>();
        for (ColumnFamilyConfig persisterConfig : configs) {
            LOG.trace("Start initialize persister: {}", persisterConfig.getClassName());
            Class<ICassandraPersister> persisterClass = getPersisterClass(persisterConfig.getClassName());
            ICassandraPersister persister = createPersister(persisterClass);
            persister.init();
            persister.setTtl(persisterConfig.getTtl());
            persister.setConsistencyLevels(defaultReadConsistency, defaultWriteConsistency, defaultSerialConsistency);
            persisters.add(persister);
            persistersMap.put(persisterClass, persister);
            updatePersistersInterfaceMap(persisterClass);
            persisterDependencyInjector.collect(persister);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<ICassandraPersister> getPersisterClass(String className) {
        try {
            Class<?> klass = ReflectionUtils.forNameWithCompatibilityAliases(className);
            checkArgument(ICassandraPersister.class.isAssignableFrom(klass), "Persister must implement ICassandraPersister");
            return (Class<ICassandraPersister>) klass;
        } catch (ClassNotFoundException e) {
            LOG.error("Wrong persister class name: {}", className, e);
            throw new IllegalArgumentException("Wrong persister class name: " + className, e);
        }
    }

    private ICassandraPersister createPersister(Class<ICassandraPersister> klass) {
        try {
            Constructor<ICassandraPersister> constructor = klass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            LOG.error("Cannot instantiate persister: {}", klass, e);
            throw new IllegalStateException("Cannot instantiate persister", e);
        }
    }

    private void updatePersistersInterfaceMap(Class<ICassandraPersister> persisterClass) {
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(persisterClass);
        for (Class<?> anInterface : interfaces) {
            if (!ICassandraPersister.class.isAssignableFrom(anInterface)) {
                List<Class<? extends ICassandraPersister>> persisterClassList = persistersInterfaceMap.get(anInterface);
                if (persisterClassList == null) {
                    persisterClassList = new ArrayList<>();
                }
                persisterClassList.add(persisterClass);
                persistersInterfaceMap.put(anInterface, persisterClassList);
            }
        }
    }

    public void populateSession(com.datastax.driver.core.Session session) {
        persisters.forEach(persister -> persister.initSession(session));
    }

    public List<ICassandraPersister> getAllPersisters() {
        return persisters;
    }

    @SuppressWarnings("unchecked")
    public <P extends ICassandraPersister> P getPersister(Class<P> persisterClass) {
        return (P) persistersMap.get(persisterClass);
    }

    @SuppressWarnings("unchecked")
    public <P> List<P> getPersistersByInterface(Class<P> persisterInterface) {
        List<Class<? extends ICassandraPersister>> persisterClassList = persistersInterfaceMap.get(persisterInterface);
        if (persisterClassList != null) {
            return persisterClassList.stream()
                    .map(persisterClass -> (P) persistersMap.get(persisterClass))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public void shutdownPersisters() {
        persisters.forEach(ICassandraPersister::shutdown);
    }
}
