package com.betsoft.casino.mp.payment;

import com.betsoft.casino.mp.model.IPlayerBet;
import com.betsoft.casino.mp.model.battleground.IBattlegroundRoundInfo;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * User: flsh
 * Date: 24.08.2022.
 */
public class AddWinPendingOperation extends AbstractPendingOperation {
    private static final byte VERSION = 0;
    private long winAmount;
    private long returnedBet;
    private long roundId;
    private IPlayerBet playerBet;
    private IBattlegroundRoundInfo bgRoundInfo;
    private BuyInPendingOperation innerOperation;

    public AddWinPendingOperation() {
        super();
    }

    public AddWinPendingOperation(long accountId, String sessionId, long gameSessionId, long roomId, long winAmount, long returnedBet,
                                  long roundId, IPlayerBet playerBet, IBattlegroundRoundInfo bgRoundInfo, long gameId, long bankId) {
        super(accountId, sessionId, gameSessionId, roomId, gameId, bankId);
        this.winAmount = winAmount;
        this.returnedBet = returnedBet;
        this.roundId = roundId;
        this.playerBet = playerBet;
        this.bgRoundInfo = bgRoundInfo;
    }

    public long getWinAmount() {
        return winAmount;
    }

    public long getReturnedBet() {
        return returnedBet;
    }

    public long getRoundId() {
        return roundId;
    }

    public IPlayerBet getPlayerBet() {
        return playerBet;
    }

    public IBattlegroundRoundInfo getBgRoundInfo() {
        return bgRoundInfo;
    }

    @Override
    public PendingOperationType getOperationType() {
        return PendingOperationType.ADD_WIN;
    }

    public BuyInPendingOperation getInnerOperation() {
        return innerOperation;
    }

    public void setInnerOperation(BuyInPendingOperation innerOperation) {
        this.innerOperation = innerOperation;
    }

    public String getOperationId() {
        return accountId + "+" + roomId + "+" + roundId;
    }

    @Override
    public void write(Kryo kryo, Output output) {
        super.write(kryo, output);
        output.writeByte(VERSION);
        output.writeLong(winAmount, true);
        output.writeLong(returnedBet, true);
        output.writeLong(roundId, true);
        kryo.writeClassAndObject(output, playerBet);
        kryo.writeClassAndObject(output, bgRoundInfo);
        kryo.writeClassAndObject(output, innerOperation);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        super.read(kryo, input);
        byte version = input.readByte();
        winAmount = input.readLong(true);
        returnedBet = input.readLong(true);
        roundId = input.readLong(true);
        playerBet = (IPlayerBet) kryo.readClassAndObject(input);
        bgRoundInfo = (IBattlegroundRoundInfo) kryo.readClassAndObject(input);
        innerOperation = (BuyInPendingOperation) kryo.readClassAndObject(input);
    }

    @Override
    public String toString() {
        return "AddWinPendingOperation [" + "winAmount=" + winAmount +
                ", returnedBet=" + returnedBet +
                ", roundId=" + roundId +
                ", playerBet=" + playerBet +
                ", bgRoundInfo=" + bgRoundInfo +
                ", innerOperation=" + innerOperation +
                super.toString() +
                ']';
    }
}
