package com.abs.casino.gs.managers.payment.wallet;

import com.abs.casino.common.cache.data.payment.AbstractWalletOperation;
import com.abs.casino.common.cache.data.payment.WalletOperationStatus;
import com.abs.casino.common.cache.data.payment.WalletOperationType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class CommonWalletOperation extends AbstractWalletOperation implements KryoSerializable {
    private static final byte VERSION = 5;
    private String externalTransactionId;
    private String externalRoundId;
    private String cmd;
    private long realBet = -1;
    private long realWin = -1;
    private long swBet = -1;
    private long swCompensatedWin = -1;
    private long trackTime = 0;
    private int trackAttempt = 0;

    public CommonWalletOperation() {
    }

    public CommonWalletOperation(long id, long accountId, Long gameSessionId, Long roundId, long amount,
                                 WalletOperationType type, WalletOperationStatus externalStatus,
                                 WalletOperationStatus internalStatus, long startTime, long endTime, String description,
                                 String externalTransactionId, String externalRoundId, long negativeBet, String cmd,
                                 long realBet, long realWin) {
        super(id, accountId, gameSessionId, roundId, amount, type, externalStatus, internalStatus,
                description, startTime, endTime, negativeBet);

        this.externalRoundId = externalRoundId;
        this.externalTransactionId = externalTransactionId;
        this.cmd = cmd;
        this.realBet = realBet;
        this.realWin = realWin;
    }

    public CommonWalletOperation(long id, long accountId, Long gameSessionId, Long roundId, long amount,
                                 WalletOperationType type, String description, WalletOperationStatus externalStatus,
                                 WalletOperationStatus internalStatus, long negativeBet) {
        super(id, accountId, gameSessionId, roundId, amount, type, description, externalStatus, internalStatus,
                negativeBet);
    }

    public CommonWalletOperation copy() {
        CommonWalletOperation commonWalletOperation = new CommonWalletOperation(getId(), getAccountId(),
                getGameSessionId(), getRoundId(), getAmount(), getType(), getExternalStatus(), getInternalStatus(),
                getStartTime(), getEndTime(), getDescription(), getExternalTransactionId(), getExternalRoundId(),
                getNegativeBet(), getCmd(), getRealBet(), getRealWin());
        commonWalletOperation.setSwBet(getSwBet());
        commonWalletOperation.setSwCompensatedWin(getSwCompensatedWin());
        return commonWalletOperation;
    }

    @Override
    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    @Override
    public String getExternalRoundId() {
        return externalRoundId;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public synchronized void update(String responseMessage, String externalRoundId, String externalTransactionId) {
        setDescription(responseMessage);
        this.externalRoundId = externalRoundId;
        this.externalTransactionId = externalTransactionId;
    }

    public long getRealBet() {
        return realBet;
    }

    public void setRealBet(long realBet) {
        this.realBet = realBet;
    }

    public long getRealWin() {
        return realWin;
    }

    public void setRealWin(long realWin) {
        this.realWin = realWin;
    }

    public long getSwBet() {
        return swBet;
    }

    public void setSwBet(long swBet) {
        this.swBet = swBet;
    }

    public long getSwCompensatedWin() {
        return swCompensatedWin;
    }

    public void setSwCompensatedWin(long swCompensatedWin) {
        this.swCompensatedWin = swCompensatedWin;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CommonWalletOperation");
        sb.append("[").append(super.toString()).append(", externalTransactionId='").
                append(externalTransactionId).append('\'');
        sb.append(", externalRoundId='").append(externalRoundId).append('\'');
        sb.append(", cmd='").append(cmd).append('\'');
        sb.append(", realBet=").append(realBet);
        sb.append(", realWin=").append(realWin);
        sb.append(", swBet=").append(swBet);
        sb.append(", swCompensatedWin=").append(swCompensatedWin);
        sb.append(", trackTime=").append(trackTime);
        sb.append(", trackAttempt").append(trackAttempt);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id);
        output.writeLong(accountId, true);
        kryo.writeObjectOrNull(output, gameSessionId, Long.class);
        kryo.writeObjectOrNull(output, roundId, Long.class);
        output.writeLong(amount);
        output.writeLong(startTime, true);
        kryo.writeObjectOrNull(output, endTime, Long.class);
        kryo.writeObject(output, type);
        kryo.writeObject(output, externalStatus);
        kryo.writeObject(output, internalStatus);
        output.writeString(description);
        output.writeString(externalSessionId);
        output.writeString(externalTransactionId);
        output.writeString(externalRoundId);
        output.writeLong(negativeBet, true);
        output.writeString(additionalProperties);
        output.writeString(cmd);
        output.writeLong(realBet);
        output.writeLong(realWin);
        output.writeLong(trackTime, true);
        output.writeInt(trackAttempt, true);
        output.writeLong(swBet);
        output.writeLong(swCompensatedWin);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        id = input.readLong();
        accountId = input.readLong(true);
        gameSessionId = kryo.readObjectOrNull(input, Long.class);
        roundId = kryo.readObjectOrNull(input, Long.class);
        amount = input.readLong();
        startTime = input.readLong(true);
        endTime = kryo.readObjectOrNull(input, Long.class);
        type = kryo.readObject(input, WalletOperationType.class);
        externalStatus = kryo.readObject(input, WalletOperationStatus.class);
        internalStatus = kryo.readObject(input, WalletOperationStatus.class);
        description = input.readString();
        externalSessionId = input.readString();
        externalTransactionId = input.readString();
        externalRoundId = input.readString();
        negativeBet = input.readLong(true);
        additionalProperties = input.readString();
        if (ver >= 1) {
            cmd = input.readString();
        }
        if (ver >= 2) {
            realBet = input.readLong();
            realWin = input.readLong();
        }
        if (ver >= 3) {
            trackTime = input.readLong(true);
            trackAttempt = input.readInt(true);
        }
        if (ver >= 4) {
            swBet = input.readLong();
        }
        if (ver >= 5) {
            swCompensatedWin = input.readLong();
        }
    }
}
