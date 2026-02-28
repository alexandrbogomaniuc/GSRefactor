package com.abs.casino.common.persist;

import java.io.IOException;

/**
 * @author <a href="mailto:fateyev@dgphoenix.com">Anton Fateyev</a>
 * @since 14.01.16
 */
public interface TableProcessor<T> {

    void process(T entity) throws IOException;

}
