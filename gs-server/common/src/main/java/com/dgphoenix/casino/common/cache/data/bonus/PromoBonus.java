package com.abs.casino.common.cache.data.bonus;

import com.abs.casino.common.cache.VersionedDistributedCacheEntry;
import com.abs.casino.common.util.ConcurrentHashSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.Set;

/**
 * User: flsh
 * Date: 3/29/12
 */
public class PromoBonus extends VersionedDistributedCacheEntry {
    private static final Logger LOG = LogManager.getLogger(PromoBonus.class);

    protected long id;
    private long bankId;
    private long subcasinoId;
    private long amount;
    private int freeSpinsCount;
    private long startDate;
    private Long endDate;
    private boolean activated;
    private String comment;
    private Set<Long> usedAccounts = new ConcurrentHashSet<Long>();
    private long adminId;

    public PromoBonus() {
    }

    public PromoBonus(long id, long bankId, long subcasinoId, long amount, int freeSpinsCount,
                      long startDate, Long endDate, boolean activated, String comment, long adminId) {
        this.id = id;
        this.bankId = bankId;
        this.subcasinoId = subcasinoId;
        this.amount = amount;
        this.freeSpinsCount = freeSpinsCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.activated = activated;
        this.comment = comment;
        this.adminId = adminId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBankId() {
        return bankId;
    }

    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    public long getSubcasinoId() {
        return subcasinoId;
    }

    public void setSubcasinoId(long subcasinoId) {
        this.subcasinoId = subcasinoId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public boolean isActivated() {
        return activated;
    }

    public boolean isNotExpired() {
        long now = System.currentTimeMillis();
        return now >= startDate && (endDate == null || now <= endDate);
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Set<Long> getUsedAccounts() {
        return usedAccounts;
    }

    public void setUsedAccounts(Set<Long> usedAccounts) {
        this.usedAccounts = usedAccounts;
    }

    public boolean isAccountUsed(long accountId) {
        return usedAccounts.contains(accountId);
    }

    public boolean setAccountUsed(long accountId) {
        if (!isAccountUsed(accountId)) {
            usedAccounts.add(accountId);
            return true;
        }
        return false;
    }

    public long getAdminId() {
        return adminId;
    }

    public void setAdminId(long adminId) {
        this.adminId = adminId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromoBonus that = (PromoBonus) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public String getUsedAccountsAsString() {
        StringBuilder sb = new StringBuilder();
        Iterator<Long> iterator = usedAccounts.iterator();
        while (iterator.hasNext()) {
            Long id = iterator.next();
            sb.append(id);
            if (iterator.hasNext()) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public int getFreeSpinsCount() {
        return freeSpinsCount;
    }

    public void setFreeSpinsCount(int freeSpinsCount) {
        this.freeSpinsCount = freeSpinsCount;
    }

    @Override
    public String toString() {
        return "PromoBonus[" +
                "id=" + id +
                ", bankId=" + bankId +
                ", subcasinoId=" + subcasinoId +
                ", amount=" + amount +
                ", freeSpinsCount=" + freeSpinsCount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", activated=" + activated +
                ", adminId=" + adminId +
                ", comment='" + comment + '\'' +
                ", usedAccounts=" + getUsedAccountsAsString() +
                ']';
    }
}
