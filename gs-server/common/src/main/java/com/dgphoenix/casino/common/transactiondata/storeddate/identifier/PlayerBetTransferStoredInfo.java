package com.abs.casino.common.transactiondata.storeddate.identifier;

/**
 * User: Grien
 * Date: 22.12.2014 16:30
 */
public class PlayerBetTransferStoredInfo implements StoredItemInfo<Long> {
    private int maxPlayerBetId;

    public PlayerBetTransferStoredInfo(int maxPlayerBetId) {
        this.maxPlayerBetId = maxPlayerBetId;
    }

    public int getMaxPlayerBetId() {
        return maxPlayerBetId;
    }

    public void setMaxPlayerBetId(int maxPlayerBetId) {
        this.maxPlayerBetId = maxPlayerBetId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("PlayerBetTransferStoredInfo");
        sb.append("[maxPlayerBetId=").append(maxPlayerBetId);
        sb.append(']');
        return sb.toString();
    }
}
