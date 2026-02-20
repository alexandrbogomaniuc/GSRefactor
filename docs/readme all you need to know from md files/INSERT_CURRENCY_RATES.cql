-- Insert Currency Rates into RCasinoSCKS.currencyratescf
-- Default currency in the system is EUR (from CurrencyRatesManager.java)
-- Timestamp: 1736849400000 = Jan 14, 2026 10:30:00 GMT

-- First, check what's currently in the table
SELECT * FROM currencyratescf;

-- ============================================
-- REQUIRED INSERTS
-- ============================================

-- 1. USD to EUR (primary conversion needed)
INSERT INTO currencyratescf (source, dest, crate, update_date) 
VALUES ('USD', 'EUR', 0.92, 1736849400000);

-- 2. EUR to EUR (identity - base currency)
INSERT INTO currencyratescf (source, dest, crate, update_date) 
VALUES ('EUR', 'EUR', 1.0, 1736849400000);

-- 3. USD to USD (identity)
INSERT INTO currencyratescf (source, dest, crate, update_date) 
VALUES ('USD', 'USD', 1.0, 1736849400000);

-- 4. EUR to USD (reverse conversion)
INSERT INTO currencyratescf (source, dest, crate, update_date) 
VALUES ('EUR', 'USD', 1.09, 1736849400000);

-- ============================================
-- OPTIONAL: Other common currencies
-- ============================================

-- GBP to EUR
INSERT INTO currencyratescf (source, dest, crate, update_date) 
VALUES ('GBP', 'EUR', 1.17, 1736849400000);

-- EUR to GBP
INSERT INTO currencyratescf (source, dest, crate, update_date) 
VALUES ('EUR', 'GBP', 0.85, 1736849400000);

-- ============================================
-- VERIFY
-- ============================================
SELECT source, dest, crate FROM currencyratescf ORDER BY source, dest;
