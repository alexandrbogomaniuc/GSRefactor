package com.betsoft.casino.mp.payment;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 24.08.2022.
 */
public class SitOutPendingOperation extends AbstractPendingOperation {
    private IPendingOperation innerOperation;

    public SitOutPendingOperation() {
        super();
    }

    public SitOutPendingOperation(long accountId, String sessionId, long gameSessionId, long roomId, long gameId, long bankId) {
        super(accountId, sessionId, gameSessionId, roomId, gameId, bankId);
    }

    public IPendingOperation getInnerOperation() {
        return innerOperation;
    }

    public void setInnerOperation(IPendingOperation innerOperation) {
        this.innerOperation = innerOperation;
    }

    @Override
    public PendingOperationType getOperationType() {
        return PendingOperationType.SIT_OUT;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        kryo.writeClassAndObject(output, innerOperation);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        innerOperation = (IPendingOperation) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "SitOutPendingOperation [" + "innerOperation=" + innerOperation + super.toString() + ']';
    }
}
