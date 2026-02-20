package com.dgphoenix.casino.services.externallobby.data;

import java.io.Serializable;

/**
 * User: flsh
 * Date: 7/13/11
 */
public class AccountInfoWrapper implements Serializable {
    private long id;
    private String nickName;
    private String externalId;
    private long bankId;
    private long subcasinoId;
    private long balance;
    private long freeBalance;
    private boolean guest;
    private boolean locked;
    private long registerTime;
    private long lastLoginTime;
    private String currency;
    private String md5pa;
    private String cpassword;
    private String backUrl;
    private String prefferedGameMode;

    public AccountInfoWrapper() {
    }

    public AccountInfoWrapper(long id, String nickName, String externalId, long bankId, long subcasinoId, long balance,
                              long freeBalance, boolean guest, boolean locked, long registerTime, long lastLoginTime,
                              String currency, String md5pa, String cpassword, String backUrl, String prefferedGameMode) {
        this.id = id;
        this.nickName = nickName;
        this.externalId = externalId;
        this.bankId = bankId;
        this.subcasinoId = subcasinoId;
        this.balance = balance;
        this.freeBalance = freeBalance;
        this.guest = guest;
        this.locked = locked;
        this.registerTime = registerTime;
        this.lastLoginTime = lastLoginTime;
        this.currency = currency;
        this.md5pa = md5pa;
        this.cpassword = cpassword;
        this.backUrl = backUrl;
        this.prefferedGameMode = prefferedGameMode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
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

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getFreeBalance() {
        return freeBalance;
    }

    public void setFreeBalance(long freeBalance) {
        this.freeBalance = freeBalance;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMd5pa() {
        return md5pa;
    }

    public void setMd5pa(String md5pa) {
        this.md5pa = md5pa;
    }

    public String getCpassword() {
        return cpassword;
    }

    public void setCpassword(String cpassword) {
        this.cpassword = cpassword;
    }

    public String getBackUrl() {
        return backUrl;
    }

    public void setBackUrl(String backUrl) {
        this.backUrl = backUrl;
    }

    public String getPrefferedGameMode() {
        return prefferedGameMode;
    }

    public void setPrefferedGameMode(String prefferedGameMode) {
        this.prefferedGameMode = prefferedGameMode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("AccountInfoWrapper");
        sb.append("[id=").append(id);
        sb.append(", nickName='").append(nickName).append('\'');
        sb.append(", externalId='").append(externalId).append('\'');
        sb.append(", bankId=").append(bankId);
        sb.append(", subcasinoId=").append(subcasinoId);
        sb.append(", balance=").append(balance);
        sb.append(", freeBalance=").append(freeBalance);
        sb.append(", guest=").append(guest);
        sb.append(", locked=").append(locked);
        sb.append(", registerTime=").append(registerTime);
        sb.append(", lastLoginTime=").append(lastLoginTime);
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", md5pa='").append(md5pa).append('\'');
        sb.append(", cpassword='").append(cpassword).append('\'');
        sb.append(", backUrl='").append(backUrl).append('\'');
        sb.append(", prefferedGameMode='").append(prefferedGameMode).append('\'');
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountInfoWrapper)) return false;

        AccountInfoWrapper that = (AccountInfoWrapper) o;

        if (id != that.id) return false;
        if (nickName != null ? !nickName.equals(that.nickName) : that.nickName != null) return false;
        if (externalId != null ? !externalId.equals(that.externalId) : that.externalId != null) return false;
        if (bankId != that.bankId) return false;
        if (md5pa != null ? !md5pa.equals(that.md5pa) : that.md5pa != null) return false;
        if (balance != that.balance) return false;
        if (freeBalance != that.freeBalance) return false;
        if (guest != that.guest) return false;
        if (lastLoginTime != that.lastLoginTime) return false;
        if (locked != that.locked) return false;
        if (registerTime != that.registerTime) return false;
        if (subcasinoId != that.subcasinoId) return false;
        if (backUrl != null ? !backUrl.equals(that.backUrl) : that.backUrl != null) return false;
        if (cpassword != null ? !cpassword.equals(that.cpassword) : that.cpassword != null) return false;
        if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;
        if (prefferedGameMode != null ? !prefferedGameMode.equals(
                that.prefferedGameMode) : that.prefferedGameMode != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ id >>> 32);
        result = 31 * result + (nickName != null ? nickName.hashCode() : 0);
        result = 31 * result + (externalId != null ? externalId.hashCode() : 0);
        result = 31 * result + (int) (bankId ^ bankId >>> 32);
        result = 31 * result + (int) (subcasinoId ^ subcasinoId >>> 32);
        result = 31 * result + (int) (balance ^ balance >>> 32);
        result = 31 * result + (int) (freeBalance ^ freeBalance >>> 32);
        result = 31 * result + (guest ? 1 : 0);
        result = 31 * result + (locked ? 1 : 0);
        result = 31 * result + (int) (registerTime ^ registerTime >>> 32);
        result = 31 * result + (int) (lastLoginTime ^ lastLoginTime >>> 32);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (md5pa != null ? md5pa.hashCode() : 0);
        result = 31 * result + (cpassword != null ? cpassword.hashCode() : 0);
        result = 31 * result + (backUrl != null ? backUrl.hashCode() : 0);
        result = 31 * result + (prefferedGameMode != null ? prefferedGameMode.hashCode() : 0);
        return result;
    }
}
