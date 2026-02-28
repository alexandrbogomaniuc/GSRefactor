package com.abs.casino.common.cache.data.payment.transfer;

import com.dgphoenix.casino.common.cache.data.payment.PaymentMode;
import com.dgphoenix.casino.gs.managers.payment.wallet.CommonWalletOperation;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Date;

/**
 * User: flsh
 * Date: 13.11.13
 */
public class ExternalPaymentTransaction implements KryoSerializable {
    private static final byte VERSION = 0;
    private String extId;
    private String extAccountId;
    private long accountId;
    private long bankId;
    private long amount;
    private Long gameSessionId;
    private Long gameId;
    private long startDate;
    private Long finishDate;
    private PaymentMode paymentMode;
    //for Wallet - walletOperationId, CommonTransfer - PaymentTransaction.id
    private Long internalOperationId;
    private TransactionStatus status;
    private TransactionType type;
    private String description;
    private long roundId;
    private String history;
    private boolean roundFinished;
    //refunded is applicable to TransactionType.DEPOSIT
    private boolean refunded = false;
    private CommonWalletOperation operation;

    public ExternalPaymentTransaction() {
    }

    public ExternalPaymentTransaction(String extId, String extAccountId, long accountId, long bankId, long amount,
                                      Long gameSessionId, Long gameId, long startDate, Long finishDate,
                                      PaymentMode paymentMode, Long internalOperationId,
                                      TransactionStatus status,
                                      TransactionType type, String description, long roundId, String history,
                                      boolean roundFinished) {
        this.extId = extId;
        this.extAccountId = extAccountId;
        this.accountId = accountId;
        this.bankId = bankId;
        this.amount = amount;
        this.gameSessionId = gameSessionId;
        this.gameId = gameId;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.paymentMode = paymentMode;
        this.internalOperationId = internalOperationId;
        this.status = status;
        this.type = type;
        this.description = description;
        this.roundId = roundId;
        this.history = history;
        this.roundFinished = roundFinished;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    public String getExtAccountId() {
        return extAccountId;
    }

    public void setExtAccountId(String extAccountId) {
        this.extAccountId = extAccountId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public Long getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Long finishDate) {
        this.finishDate = finishDate;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Long getInternalOperationId() {
        return internalOperationId;
    }

    public void setInternalOperationId(Long internalOperationId) {
        this.internalOperationId = internalOperationId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getRoundId() {
        return roundId;
    }

    public void setRoundId(long roundId) {
        this.roundId = roundId;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public boolean isRoundFinished() {
        return roundFinished;
    }

    public void setRoundFinished(boolean roundFinished) {
        this.roundFinished = roundFinished;
    }

    public boolean isRefunded() {
        return refunded;
    }

    public void setRefunded(boolean refunded) {
        this.refunded = refunded;
    }

    public CommonWalletOperation getOperation() {
        return operation;
    }

    public void setOperation(CommonWalletOperation operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExternalPaymentTransaction [");
        sb.append("extId='").append(extId).append('\'');
        sb.append(", extAccountId='").append(extAccountId).append('\'');
        sb.append(", accountId=").append(accountId);
        sb.append(", bankId=").append(bankId);
        sb.append(", amount=").append(amount);
        sb.append(", gameSessionId=").append(gameSessionId);
        sb.append(", gameId=").append(gameId);
        sb.append(", startDate=").append(new Date(startDate));
        sb.append(", finishDate=").append(finishDate == null ? "null" : new Date(finishDate));
        sb.append(", paymentMode=").append(paymentMode);
        sb.append(", internalOperationId=").append(internalOperationId);
        sb.append(", status=").append(status);
        sb.append(", type=").append(type);
        sb.append(", description='").append(description).append('\'');
        sb.append(", roundId=").append(roundId);
        sb.append(", history='").append(history).append('\'');
        sb.append(", roundFinished=").append(roundFinished);
        sb.append(", operation=").append(operation);
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeString(extId);
        output.writeString(extAccountId);
        output.writeLong(accountId, true);
        output.writeLong(bankId, true);
        output.writeLong(amount, true);
        kryo.writeObjectOrNull(output, gameSessionId, Long.class);
        kryo.writeObjectOrNull(output, gameId, Long.class);
        output.writeLong(startDate, true);
        kryo.writeObjectOrNull(output, finishDate, Long.class);
        output.writeString(paymentMode.name());
        kryo.writeObjectOrNull(output, internalOperationId, Long.class);
        output.writeString(status.name());
        output.writeString(type.name());
        output.writeString(description);
        output.writeLong(roundId, true);
        output.writeString(history);
        output.writeBoolean(roundFinished);
        output.writeBoolean(refunded);
        kryo.writeObjectOrNull(output, operation, CommonWalletOperation.class);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        byte ver = input.readByte();
        extId = input.readString();
        extAccountId = input.readString();
        accountId = input.readLong(true);
        bankId = input.readLong(true);
        amount = input.readLong(true);
        gameSessionId = kryo.readObjectOrNull(input, Long.class);
        gameId = kryo.readObjectOrNull(input, Long.class);
        startDate = input.readLong(true);
        finishDate = kryo.readObjectOrNull(input, Long.class);
        paymentMode = PaymentMode.valueOf(input.readString());
        internalOperationId = kryo.readObjectOrNull(input, Long.class);
        status = TransactionStatus.valueOf(input.readString());
        type = TransactionType.valueOf(input.readString());
        description = input.readString();
        roundId = input.readLong(true);
        history = input.readString();
        roundFinished = input.readBoolean();
        refunded = input.readBoolean();
        operation = kryo.readObjectOrNull(input, CommonWalletOperation.class);
    }
}
