package com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors;

import com.abs.casino.gs.managers.dblink.IDBLink;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.GameError;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.annotations.PostProcessor;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.annotations.PreProcessor;
import com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.command.ICommandRelated;
import com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.command.ILockedCommandProcessor;
import com.abs.casino.gs.singlegames.tools.cbservtools.commands.processors.command.IUnlockedCommandProcessor;
import com.dgphoenix.casino.gs.singlegames.tools.cbservtools.commands.processors.error.IErrorProcessor;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class GameCommandsProcessor {

    private static final Logger LOG = LogManager.getLogger(GameCommandsProcessor.class);

    private final Map<String, IUnlockedCommandProcessor> unlockedCommandProcessors;
    private final List<ILockedProcessor> lockedPreProcessors;
    private final Map<String, ILockedCommandProcessor> lockedCommandProcessors;
    private final List<ILockedProcessor> lockedPostProcessors;
    private final List<IErrorProcessor> errorProcessors;

    public GameCommandsProcessor(Collection<ILockedCommandProcessor> lockedCommandProcessors,
                                 Collection<IUnlockedCommandProcessor> unlockedCommandProcessors,
                                 Collection<ILockedProcessor> lockedProcessors,
                                 Collection<IErrorProcessor> errorProcessors) {
        this.unlockedCommandProcessors = ImmutableMap.copyOf(collectProcessorsByCommand(unlockedCommandProcessors));
        LOG.info("Unlocked command processors: {}", buildStringOfProcessors(this.unlockedCommandProcessors));
        this.lockedCommandProcessors = ImmutableMap.copyOf(collectProcessorsByCommand(lockedCommandProcessors));
        LOG.info("Locked command processors: {}", buildStringOfProcessors(this.lockedCommandProcessors));

        lockedPreProcessors = ImmutableList.copyOf(orderProcessors(findProcessors(lockedProcessors, PreProcessor.class)));
        LOG.info("Locked pre processors: {}", buildStringOfProcessors(this.lockedPreProcessors));
        lockedPostProcessors = ImmutableList.copyOf(orderProcessors(findProcessors(lockedProcessors, PostProcessor.class)));
        LOG.info("Locked post processors: {}", buildStringOfProcessors(this.lockedPostProcessors));

        this.errorProcessors = ImmutableList.copyOf(orderProcessors(errorProcessors.stream()));
        LOG.info("Error processors: {}", buildStringOfProcessors(this.errorProcessors));
    }

    private String buildStringOfProcessors(Map<String, ?> processors) {
        return processors.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue().getClass().getSimpleName())
                .collect(Collectors.joining(", "));
    }

    private String buildStringOfProcessors(Collection<?> processors) {
        return processors.stream()
                .map(object -> object.getClass().getSimpleName())
                .collect(Collectors.joining(", "));
    }

    private <T extends ICommandRelated> Map<String, T> collectProcessorsByCommand(Collection<T> processors) {
        Map<String, T> unlockedCommandProcessorsByCommand = new HashMap<>();
        for (T unlockedCommandProcessor : processors) {
            String command = unlockedCommandProcessor.getCommand();
            T existCommandProcessor = unlockedCommandProcessorsByCommand.putIfAbsent(command, unlockedCommandProcessor);
            checkArgument(existCommandProcessor == null,
                    "Command processor already exists for command %s, exist processor: %s, new processor: %s",
                    command, existCommandProcessor, unlockedCommandProcessor);
        }
        return unlockedCommandProcessorsByCommand;
    }

    private <T> Stream<T> findProcessors(Collection<T> processors, Class<? extends Annotation> processorType) {
        return processors.stream()
                .filter(processor -> processor.getClass().isAnnotationPresent(processorType));
    }

    private <T> Collection<T> orderProcessors(Stream<T> processors) {
        return processors
                .sorted((processor1, processor2) -> {
                    int order1 = getOrderOfProcessor(processor1);
                    int order2 = getOrderOfProcessor(processor2);
                    checkArgument(order1 != order2,
                            "Two processors have the same order, processor1: %s, processor2: %s",
                            processor1, processor2);
                    return order1 - order2;
                })
                .collect(Collectors.toList());
    }

    private int getOrderOfProcessor(Object processor) {
        Class<?> processorClass = processor.getClass();
        Order orderAnnotation = processorClass.getAnnotation(Order.class);
        checkNotNull(orderAnnotation, "Order must be specified for %s",
                processorClass.getCanonicalName());
        return orderAnnotation.value();
    }

    public boolean canBeProcessedUnlocked(String command) {
        return unlockedCommandProcessors.containsKey(command);
    }

    public boolean canBeProcessedLocked(String command) {
        return lockedCommandProcessors.containsKey(command);
    }

    public IUnlockedCommandProcessor getUnlockedCommandProcessor(String command) {
        return unlockedCommandProcessors.get(command);
    }

    public Iterable<ILockedProcessor> getLockedPreProcessors(String command, boolean isNewRoundBet) {
        return lockedPreProcessors.stream()
                .filter(processor -> processor.canProcessCommand(command, isNewRoundBet))
                .collect(Collectors.toList());
    }

    public ILockedCommandProcessor getLockedCommandProcessor(String command) {
        return lockedCommandProcessors.get(command);
    }

    public Iterable<ILockedProcessor> getLockedPostProcessors(String command, boolean isNewRoundBet) {
        return lockedPostProcessors.stream()
                .filter(processor -> processor.canProcessCommand(command, isNewRoundBet))
                .collect(Collectors.toList());
    }

    public Iterable<IErrorProcessor> getErrorProcessors(GameError error, Throwable exception, IDBLink dbLink) {
        return errorProcessors.stream()
                .filter(processor -> processor.canProcessError(error, exception, dbLink))
                .collect(Collectors.toList());
    }
}
