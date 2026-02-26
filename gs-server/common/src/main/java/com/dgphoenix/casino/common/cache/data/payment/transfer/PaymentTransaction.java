package com.dgphoenix.casino.common.cache.data.payment.transfer;

import com.dgphoenix.casino.common.cache.IDistributedCacheEntry;
import com.dgphoenix.casino.common.cache.data.currency.Currency;
import com.abs.casino.common.cache.data.payment.transfer.paymentmean.PaymentMeanId;
import com.abs.casino.common.cache.data.payment.transfer.paymentmean.PaymentMeanType;
import com.dgphoenix.casino.common.cache.data.session.ClientType;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Date;

/**
 * User: flsh
 * Date: 11.08.2009
 */
public class PaymentTransaction implements IDistributedCacheEntry, KryoSerializable {
    private static final byte VERSION = 0;

    private long id;
    private long accountId;
    private long amount;
    private Long gameSessionId;
    private Long gameId;
    private boolean isRealMoney;
    private long startDate;
    private Long finishDate;
    private TransactionStatus status;
    private TransactionType type;
    private PaymentSystemType paymentSystemType;
    private PaymentMeanType paymentMeanType;
    private PaymentMeanId paymentMeanId;
    private String externalTransactionId;
    private String description;
    private long subCasinoId;
    private ClientType clientType = ClientType.FLASH;
    private String comment;
    private Currency currency;
    private Long cost = null;
    private Long adjusterId = null;

    private Long specialId;
    private String specialType;

    public PaymentTransaction() {
    }

    public PaymentTransaction(long id, long accountId, long amount, Long cost, Long gameSessionId, long startDate, TransactionStatus status,
                              TransactionType type, String externalTransactionId, PaymentSystemType paymentSystemType,
                              PaymentMeanType paymentMeanType, PaymentMeanId paymentMeanId, long subCasinoId,
                              ClientType clientType, Currency currency, Long adjusterId, String comment, Long specialId,
                              String specialType) {
        this(id, accountId, amount, gameSessionId, startDate, status, type, externalTransactionId, paymentSystemType,
                paymentMeanType, paymentMeanId, true, null, subCasinoId, clientType, currency, comment);
        this.cost = cost;
        this.adjusterId = adjusterId;
        this.specialId = specialId;
        this.specialType = specialType;
    }

    public PaymentTransaction(long id, long accountId, long amount, Long gameSessionId, long startDate,
                              TransactionStatus status, TransactionType type, String externalTransactionId,
                              PaymentSystemType paymentSystemType, PaymentMeanType paymentMeanType,
                              PaymentMeanId paymentMeanId,
                              boolean realMoney, Long gameId, long subCasinoId, ClientType clientType,
                              Currency currency, String comment) {
        super();
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.gameSessionId = gameSessionId;
        this.startDate = startDate;
        this.status = status;
        this.type = type;
        this.externalTransactionId = externalTransactionId;
        this.paymentSystemType = paymentSystemType;
        this.paymentMeanType = paymentMeanType;
        this.paymentMeanId = paymentMeanId;
        this.isRealMoney = realMoney;
        this.gameId = gameId;
        this.subCasinoId = subCasinoId;
        this.clientType = clientType;
        this.currency = currency;
        this.comment = comment;
    }

    //copy constructor

    private PaymentTransaction(long id, long accountId, long amount, Long cost, Long gameSessionId, Long gameId,
                               boolean isRealMoney, long startDate,
                               Long finishDate, TransactionStatus status, TransactionType type,
                               PaymentSystemType paymentSystemType,
                               PaymentMeanType paymentMeanType, PaymentMeanId paymentMeanId,
                               String externalTransactionId,
                               String description, long subCasinoId, ClientType clientType, Currency currency,
                               Long adjusterId, String comment) {
        super();
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.cost = cost;
        this.gameSessionId = gameSessionId;
        this.gameId = gameId;
        this.isRealMoney = isRealMoney;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.status = status;
        this.type = type;
        this.paymentSystemType = paymentSystemType;
        this.paymentMeanType = paymentMeanType;
        this.paymentMeanId = paymentMeanId;
        this.externalTransactionId = externalTransactionId;
        this.description = description;
        this.subCasinoId = subCasinoId;
        this.clientType = clientType;
        this.currency = currency;
        this.adjusterId = adjusterId;
        this.comment = comment;
    }

    public PaymentTransaction copy() {
        return new PaymentTransaction(id, accountId, amount, cost, gameSessionId, gameId, isRealMoney,
                startDate, finishDate, status, type, paymentSystemType, paymentMeanType,
                paymentMeanId, externalTransactionId, description, subCasinoId, clientType,
                currency, adjusterId, comment);
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatusWithDescription(TransactionStatus status, String description) {
        this.status = status;
        this.description = description;
        if (status == TransactionStatus.APPROVED || status == TransactionStatus.FAILED) {
            setFinishDate(System.currentTimeMillis());
        }
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

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAmount() {
        return amount;
    }

    public Long getGameSessionId() {
        return gameSessionId;
    }

    public void setGameSessionId(Long gameSessionId) {
        this.gameSessionId = gameSessionId;
    }

    public boolean isRealMoney() {
        return isRealMoney;
    }

    public void setRealMoney(boolean isRealMoney) {
        this.isRealMoney = isRealMoney;
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

    public Long getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(long time) {
        this.finishDate = time;
    }

    public PaymentSystemType getPaymentSystemType() {
        return paymentSystemType;
    }

    public PaymentMeanType getPaymentMeanType() {
        return paymentMeanType;
    }

    public PaymentMeanId getPaymentMeanId() {
        return paymentMeanId;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void finish(TransactionStatus status, String externalTransactionId, String description) {
        this.finishDate = System.currentTimeMillis();
        this.status = status;
        this.externalTransactionId = externalTransactionId;
        this.description = description;
    }

    public long getSubCasinoId() {
        return subCasinoId;
    }

    public void setSubCasinoId(long subCasinoId) {
        this.subCasinoId = subCasinoId;
    }

    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Long getCost() {
        return cost;
    }

    public Long getAdjusterId() {
        return adjusterId;
    }

    public Long getSpecialId() {
        return specialId;
    }

    public void setSpecialId(Long specialId) {
        this.specialId = specialId;
    }

    public String getSpecialType() {
        return specialType;
    }

    public void setSpecialType(String specialType) {
        this.specialType = specialType;
    }

    @Override
    public int hashCode() {
        return (int) (getId() ^ (getId() >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PaymentTransaction other = (PaymentTransaction) obj;
        return getId() == other.getId();
    }

    public String toString() {
        return "PaymentTransaction [" +
                "id=" + this.id +
                ", accountId=" + this.accountId +
                ", amount=" + this.amount +
                ", cost=" + this.cost +
                ", gameSessionId=" + this.gameSessionId +
                ", gameId=" + this.gameId +
                ", isRealMoney=" + this.isRealMoney +
                ", startDate=" + new Date(startDate) +
                ", finishDate=" + (finishDate == null ? "" : new Date(finishDate)) +
                ", status=" + this.status +
                ", type=" + this.type +
                ", paymentSystemType=" + this.paymentSystemType +
                ", paymentMeanType=" + this.paymentMeanType +
                ", paymentMeanId=" + this.paymentMeanId +
                ", externalTransactionId=" + this.externalTransactionId +
                ", description=" + this.description +
                ", subCasinoId=" + this.subCasinoId +
                ", clientType=" + this.clientType +
                ", currency=" + this.currency +
                ", adjusterId=" + this.adjusterId +
                ", comment=" + this.comment +
                ", specialId=" + this.specialId +
                ", specialType=" + this.specialType +
                "]";
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeByte(VERSION);
        output.writeLong(id, true);
        output.writeLong(accountId, true);
        output.writeLong(amount, true);
        kryo.writeObjectOrNull(output, gameSessionId, Long.class);
        kryo.writeObjectOrNull(output, gameId, Long.class);
        output.writeBoolean(isRealMoney);
        output.writeLong(startDate, true);
        kryo.writeObjectOrNull(output, finishDate, Long.class);
        kryo.writeObject(output, status);
        kryo.writeObject(output, type);
        kryo.writeObject(output, paymentSystemType);
        kryo.writeObject(output, paymentMeanType);
        kryo.writeObjectOrNull(output, paymentMeanId, PaymentMeanId.class);
        output.writeString(externalTransactionId);
        output.writeString(description);
        output.writeLong(subCasinoId, true);
        kryo.writeObject(output, clientType);
        output.writeString(comment);
        kryo.writeObject(output, currency);
        kryo.writeObjectOrNull(output, cost, Long.class);
        kryo.writeObjectOrNull(output, adjusterId, Long.class);
        kryo.writeObjectOrNull(output, specialId, Long.class);
        output.writeString(specialType);
    }

    @Override
    public void read(Kryo kryo, Input input) {
        @SuppressWarnings("UnusedDeclaration")
        byte ver = input.readByte();
        id = input.readLong(true);
        accountId = input.readLong(true);
        amount = input.readLong(true);
        gameSessionId = kryo.readObjectOrNull(input, Long.class);
        gameId = kryo.readObjectOrNull(input, Long.class);
        isRealMoney = input.readBoolean();
        startDate = input.readLong(true);
        finishDate = kryo.readObjectOrNull(input, Long.class);
        status = kryo.readObject(input, TransactionStatus.class);
        type = kryo.readObject(input, TransactionType.class);
        paymentSystemType = kryo.readObject(input, PaymentSystemType.class);
        paymentMeanType = kryo.readObject(input, PaymentMeanType.class);
        paymentMeanId = kryo.readObjectOrNull(input, PaymentMeanId.class);
        externalTransactionId = input.readString();
        description = input.readString();
        subCasinoId = input.readLong(true);
        clientType = kryo.readObject(input, ClientType.class);
        comment = input.readString();
        currency = kryo.readObject(input, Currency.class, Currency.SERIALIZER);
        cost = kryo.readObjectOrNull(input, Long.class);
        adjusterId = kryo.readObjectOrNull(input, Long.class);
        specialId = kryo.readObjectOrNull(input, Long.class);
        specialType = input.readString();
    }
}
