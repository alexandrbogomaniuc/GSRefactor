package com.abs.casino.common.transactiondata.storeddate;

import com.abs.casino.common.transactiondata.storeddate.identifier.StoredItemInfo;

/**
 * User: Grien
 * Date: 22.12.2014 12:27
 */
public class StoredItem<T, I extends StoredItemInfo<T>> {
    private T item;
    private I identifier;

    public StoredItem(T item, I identifier) {
        this.item = item;
        this.identifier = identifier;
    }

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public I getIdentifier() {
        return identifier;
    }

    public void setIdentifier(I identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StoredItem");
        sb.append("[item=").append(item);
        sb.append(", identifier=").append(identifier);
        sb.append(']');
        return sb.toString();
    }
}
