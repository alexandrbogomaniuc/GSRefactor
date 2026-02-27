package com.abs.casino.common.vault;

import java.util.List;

/**
 * Created by nkurtushin on 21.03.17.
 */
public class VaultStartResponse {
    private final Long bankId;
    private final String sessionId;
    private final String cashier;
    private final String help;
    private final String defaultLang;
    private final String defaultCurrencyCode;
    private final String defaultCurrencySymbol;
    private final String playerCurrencyCode;
    private final String playerCurrencySymbol;
    private final String bankCertification;
    private final String balanceCheckUrl;
    private final String frbUrl;
    private final Long initialBalance;
    private final String staticDirectory;
    private final List<String> customPackages;
    private final List<String> mirrors;

    private VaultStartResponse(Builder builder) {
        this.bankId = builder.bankId;
        this.sessionId = builder.sessionId;
        this.cashier = builder.cashier;
        this.help = builder.help;
        this.defaultLang = builder.defaultLang;
        this.defaultCurrencyCode = builder.defaultCurrencyCode;
        this.defaultCurrencySymbol = builder.defaultCurrencySymbol;
        this.balanceCheckUrl = builder.balanceCheckUrl;
        this.frbUrl = builder.frbUrl;
        this.initialBalance = builder.initialBalance;
        this.mirrors = builder.mirrors;
        this.bankCertification = builder.bankCertification;
        this.staticDirectory = builder.staticDirectory;
        this.customPackages = builder.customPackages;
        this.playerCurrencyCode = builder.playerCurrencyCode;
        this.playerCurrencySymbol = builder.playerCurrencySymbol;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Long getBankId() {
        return bankId;
    }

    public String getCashier() {
        return cashier;
    }

    public String getHelp() {
        return help;
    }

    public String getDefaultLang() {
        return defaultLang;
    }

    public String getDefaultCurrencyCode() {
        return defaultCurrencyCode;
    }

    public String getBalanceCheckUrl() {
        return balanceCheckUrl;
    }

    public Long getInitialBalance() {
        return initialBalance;
    }

    public List<String> getMirrors() {
        return mirrors;
    }

    public String getFrbUrl() {
        return frbUrl;
    }

    public String getDefaultCurrencySymbol() {
        return defaultCurrencySymbol;
    }

    public String getBankCertification() {
        return bankCertification;
    }

    public String getStaticDirectory() {
        return staticDirectory;
    }

    public List<String> getCustomPackages() {
        return customPackages;
    }

    public String getPlayerCurrencyCode() {
        return playerCurrencyCode;
    }

    public String getPlayerCurrencySymbol() {
        return playerCurrencySymbol;
    }

    public static class Builder {
        private final Long bankId;
        private String sessionId;
        private String cashier;
        private String help;
        private String defaultLang;
        private String defaultCurrencyCode;
        private String defaultCurrencySymbol;
        private String playerCurrencyCode;
        private String playerCurrencySymbol;
        private String bankCertification;
        private String balanceCheckUrl;
        private String frbUrl;
        private Long initialBalance;
        private String staticDirectory;
        private List<String> customPackages;
        private List<String> mirrors;

        public Builder(Long bankId) {
            this.bankId = bankId;
        }

        public Builder(Long bankId, String sessionId) {
            this.bankId = bankId;
            this.sessionId = sessionId;
        }

        public Builder cashier(String cashier) {
            this.cashier = cashier;
            return this;
        }

        public Builder help(String help) {
            this.help = help;
            return this;
        }

        public Builder defaultLang(String defaultLang) {
            this.defaultLang = defaultLang;
            return this;
        }

        public Builder defaultCurrencyCode(String defaultCurrencyCode) {
            this.defaultCurrencyCode = defaultCurrencyCode;
            return this;
        }

        public Builder defaultCurrencySymbol(String defaultCurrencySymbol) {
            this.defaultCurrencySymbol = defaultCurrencySymbol;
            return this;
        }

        public Builder bankCertification(String bankCertification) {
            this.bankCertification = bankCertification;
            return this;
        }

        public Builder balanceCheckUrl(String balanceCheckUrl) {
            this.balanceCheckUrl = balanceCheckUrl;
            return this;
        }

        public Builder frbUrl(String frbUrl) {
            this.frbUrl = frbUrl;
            return this;
        }

        public Builder initialBalance(Long initialBalance) {
            this.initialBalance = initialBalance;
            return this;
        }

        public Builder staticDirectory(String staticDirectory) {
            this.staticDirectory = staticDirectory;
            return this;
        }

        public Builder mirrors(List<String> mirrors) {
            this.mirrors  = mirrors;
            return this;
        }

        public Builder customPackages(List<String> customPackages) {
            this.customPackages = customPackages;
            return this;
        }

        public Builder playerCurrencySymbol(String playerCurrencySymbol) {
            this.playerCurrencySymbol = playerCurrencySymbol;
            return this;
        }

        public Builder playerCurrencyCode(String playerCurrencyCode) {
            this.playerCurrencyCode = playerCurrencyCode;
            return this;
        }

        public VaultStartResponse build() {
            VaultStartResponse response = new VaultStartResponse(this);
            validate(response);
            return response;
        }

        private void validate(VaultStartResponse response) {
            if (response.bankId == null) {
                throw new IllegalStateException("bankId is not set");
            }
        }
    }
}
