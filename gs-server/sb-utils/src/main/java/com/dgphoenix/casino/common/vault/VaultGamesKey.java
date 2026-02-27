package com.abs.casino.common.vault;

/**
 * Created by nkurtushin on 31.05.17.
 */
public class VaultGamesKey {
    private final Long bankId;
    private final String currencyCode;
    private final String lang;

    public VaultGamesKey(Long bankId, String currencyCode, String lang) {
        this.bankId = bankId;
        this.currencyCode = currencyCode;
        this.lang = lang;
    }

    public Long getBankId() {
        return bankId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getLang() {
        return lang;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VaultGamesKey that = (VaultGamesKey) o;

        if (bankId != null ? !bankId.equals(that.bankId) : that.bankId != null) return false;
        if (currencyCode != null ? !currencyCode.equals(that.currencyCode) : that.currencyCode != null) return false;
        return lang != null ? lang.equals(that.lang) : that.lang == null;
    }

    @Override
    public int hashCode() {
        int result = bankId != null ? bankId.hashCode() : 0;
        result = 31 * result + (currencyCode != null ? currencyCode.hashCode() : 0);
        result = 31 * result + (lang != null ? lang.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VaultGamesKey{" +
                "bankId=" + bankId +
                ", currencyCode='" + currencyCode + '\'' +
                ", lang='" + lang + '\'' +
                '}';
    }
}
