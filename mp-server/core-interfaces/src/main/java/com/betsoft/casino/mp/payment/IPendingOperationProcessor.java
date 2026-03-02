package com.betsoft.casino.mp.payment;

import com.betsoft.casino.mp.service.ISocketService;
import com.abs.casino.common.exception.CommonException;
import com.abs.casino.common.exception.ObjectAlreadyExistsException;

/**
 * User: flsh
 * Date: 12.08.2022.
 */
public interface IPendingOperationProcessor<P extends IPendingOperation> {
    //return true if success and operation may be removed
    boolean process(ISocketService socketService, int serverId, P operation) throws CommonException;

    //return false if need silent skip saving newOperation
    boolean mergeNewAndExistingOperation(P newOperation,IPendingOperation oldOperation) throws ObjectAlreadyExistsException;

    default void throwMergeException() throws ObjectAlreadyExistsException {
        throw new ObjectAlreadyExistsException("merge operations not supported");
    }
}
