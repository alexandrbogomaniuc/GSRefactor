package com.abs.casino.cassandra;

import com.dgphoenix.casino.cassandra.persist.engine.ICassandraPersister;
import com.google.common.collect.ImmutableMap;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * @author <a href="mailto:noragami@dgphoenix.com">Alexander Aldokhin</a>
 * @since 08.08.2022
 */
public class PersisterDependencyInjector {

    private static final Logger LOG = LogManager.getLogger(PersisterDependencyInjector.class);

    private final Map<ICassandraPersister, List<Pair<Class<? extends ICassandraPersister>, Method>>> dependencyMap = new HashMap<>();

    public void collect(ICassandraPersister persister) {
        StreamEx.of(persister.getClass().getDeclaredMethods())
                .append(persister.getClass().getSuperclass().getDeclaredMethods())
                .filter(this::isSetter)
                .sorted(Comparator.comparing(Method::getName))
                .forEach(setter -> {
                    Class<? extends ICassandraPersister> dependency = setter.getParameterTypes()[0].asSubclass(ICassandraPersister.class);
                    Pair<Class<? extends ICassandraPersister>, Method> injectionPoint = Pair.of(dependency, setter);
                    dependencyMap.computeIfAbsent(persister, k -> new ArrayList<>()).add(injectionPoint);
                    LOG.debug("Dependency {} for persister {} with injectionPoint {} added to dependencyMap", dependency, persister, injectionPoint);
                });
    }

    public void inject(Function<Class<? extends ICassandraPersister>, ICassandraPersister> dependencyContainer) {
        LOG.debug("Start injecting");
        dependencyMap.forEach((persister, injectionPoints) -> {
            for (Pair<Class<? extends ICassandraPersister>, Method> injectionPoint : injectionPoints) {
                Method setter = injectionPoint.getValue();
                ICassandraPersister dependency = dependencyContainer.apply(injectionPoint.getKey());
                invokeSetter(persister, setter, dependency);
            }
        });
        LOG.debug("End injecting");
    }

    public Map<ICassandraPersister, List<Pair<Class<? extends ICassandraPersister>, Method>>> getDependencyMap() {
        return ImmutableMap.copyOf(dependencyMap);
    }

    private boolean isSetter(Method method) {
        return method.getName().startsWith("set")
                && method.getParameterCount() == 1
                && ICassandraPersister.class.isAssignableFrom(method.getParameterTypes()[0])
                && method.getReturnType().equals(Void.TYPE);
    }

    private void invokeSetter(ICassandraPersister persister, Method setter, ICassandraPersister dependency) {
        try {
            setter.setAccessible(true);
            setter.invoke(persister, dependency);
            LOG.debug("Invoked setter [{}] with dependency {} on persister {}", setter, dependency, persister);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to inject " + dependency + " into " + persister + " with setter [" + setter + "]", e);
        }
    }
}
