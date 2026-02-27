package com.abs.casino.common.engine;

import com.dgphoenix.casino.common.exception.CommonException;

/**
 * Created by ANGeL
 * Date: Dec 21, 2007
 * Time: 1:57:20 PM
 */
public interface IControllable {
    boolean startup();
    boolean isStarted();
    boolean isRunning();
    boolean shutdown() throws CommonException;
}
