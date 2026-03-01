package com.abs.casino.common.persist;

import com.abs.casino.common.util.Pair;

import java.io.IOException;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 15.01.16
 */
public interface StreamPersister<K, V> {

    void processAll(TableProcessor<Pair<K, V>> tableProcessor) throws IOException;

    void processByCondition(TableProcessor<Pair<K, V>> tableProcessor, String conditionName, Object... conditionValues) throws IOException;
}
