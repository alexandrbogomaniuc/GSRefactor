package com.betsoft.casino.mp.payment;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 23.08.2022.
 */
public class BuyInPendingOperation extends AbstractPendingOperation {
    private static final byte VERSION = 0;
    private long amount;
    private int betNumber;
    private Long tournamentId;
    private Long currentBalance;

    public BuyInPendingOperation() {
        super();
    }

    public BuyInPendingOperation(long accountId, String sessionId, long gameSessionId, long roomId, long amount, int betNumber,
                                 Long tournamentId, Long currentBalance, long gameId, long bankId) {
        super(accountId, sessionId, gameSessionId, roomId, gameId, bankId);
        this.amount = amount;
        this.betNumber = betNumber;
        this.tournamentId = tournamentId;
        this.currentBalance = currentBalance;
    }

    public long getAmount() {
        return amount;
    }

    public int getBetNumber() {
        return betNumber;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public Long getCurrentBalance() {
        return currentBalance;
    }

    @Override
    public PendingOperationType getOperationType() {
        return PendingOperationType.BUY_IN;
    }

    public String getOperationId(){
        return accountId + "+" + gameSessionId + "+" + roomId + "+" + betNumber;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeByte(VERSION);
        output.writeLong(amount, true);
        output.writeInt(betNumber, true);
        kryo.writeObjectOrNull(output, tournamentId, Long.class);
        kryo.writeObjectOrNull(output, currentBalance, Long.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        byte version = input.readByte();
        amount = input.readLong(true);
        betNumber = input.readInt(true);
        tournamentId = kryo.readObjectOrNull(input, Long.class);
        currentBalance = kryo.readObjectOrNull(input, Long.class);
    }

    @Override
    public String toString() {
        return "BuyInPendingOperation [amount=" + amount +
                ", betNumber=" + betNumber +
                ", tournamentId=" + tournamentId +
                ", currentBalance=" + currentBalance +
                super.toString() +
                ']';
    }
}
