package com.abs.casino.common.remotecall;

import com.dgphoenix.casino.common.exception.CommonException;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 02.04.13
 */
public interface IRemoteCall extends Serializable {
    void call() throws CommonException;
}
