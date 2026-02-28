package com.abs.casino.common.exception;

/**
 * User: plastical
 * Date: 11.05.2010
 */
public class ForeignServerException extends Exception {
    private int ownerServerId;
    private long accountId;

    public ForeignServerException(int ownerServerId, long accountId, String message) {
        super(message);
        this.ownerServerId = ownerServerId;
        this.accountId = accountId;
    }

    public int getOwnerServerId() {
        return ownerServerId;
    }

    public long getAccountId() {
        return accountId;
    }

    @Override
    public String toString() {
        return "ForeignServerException[" +
                "ownerServerId=" + ownerServerId +
                ", accountId=" + accountId +
                ", message='" + getMessage() + "'" +
                ']';
    }
}
