package com.dgphoenix.casino.services.transfer;

import com.dgphoenix.casino.common.exception.CommonException;

import javax.xml.ws.WebFault;

/**
 * User: van0ss
 * Date: 01/11/2016
 */
@WebFault
public class ClusterTransferException extends CommonException {

    public ClusterTransferException(String message) {
        super(message);
    }

    public ClusterTransferException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
