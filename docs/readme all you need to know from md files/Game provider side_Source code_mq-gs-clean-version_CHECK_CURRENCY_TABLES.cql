-- Query Currency Tables in Cassandra
-- Run this to see what's currently configured

-- First, let's see all tables related to currency
-- DESCRIBE TABLES;

-- Check RCasinoKS keyspace for currency-related tables
USE RCasinoKS;

-- Query the main currency rates map table
SELECT * FROM currencyratesmap LIMIT 100;

-- Also check if there's any currency config in other tables
-- Check banksmap for currency info (this might have default currencies)
SELECT key, jcn FROM banksmap WHERE key IN (6274, 6275) ALLOW FILTERING;

-- Alternative: Check if there's a currencies table
-- SELECT * FROM currencies LIMIT 10;

-- Show the schema for currencyratesmap
DESCRIBE TABLE currencyratesmap;
