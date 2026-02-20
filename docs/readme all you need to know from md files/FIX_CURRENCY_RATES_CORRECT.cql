-- CORRECT Currency Rates Table
-- The table is in RCasinoSCKS (Platform), NOT RCasinoKS (Game Engine)!

USE RCasinoSCKS;

-- Check the table structure
DESCRIBE TABLE currencyratescf;

-- Check current rates
SELECT * FROM currencyratescf;

-- If empty or missing USD, insert rates:

-- USD to EUR (base currency is EUR according to Java code)
INSERT INTO currencyratescf (source, dest, crate, update_date) 
VALUES ('USD', 'EUR', 0.92, 1736849400000);

-- EUR to EUR (identity rate)
INSERT INTO currencyratescf (source, dest, crate, update_date) 
VALUES ('EUR', 'EUR', 1.0, 1736849400000);

-- EUR to USD (reverse)
INSERT INTO currencyratescf (source, dest, crate, update_date) 
VALUES ('EUR', 'USD', 1.09, 1736849400000);

-- USD to USD (identity rate)
INSERT INTO currencyratescf (source, dest, crate, update_date) 
VALUES ('USD', 'USD', 1.0, 1736849400000);

-- Verify the data
SELECT source, dest, crate FROM currencyratescf;
