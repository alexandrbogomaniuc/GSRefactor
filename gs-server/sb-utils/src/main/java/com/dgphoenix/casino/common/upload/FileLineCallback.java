package com.dgphoenix.casino.common.upload;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * @author <a href="mailto:zomac@dgphoenix.com">Roman Sorokin</a>
 * @since 9/19/22
 */
public interface FileLineCallback {

    void handle(String line) throws CommonException;

}
