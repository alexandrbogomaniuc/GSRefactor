-- Quick fix: Update only the COMMON_WALLET_REQUEST_CLIENT_CLASS property
-- This is a workaround since updating the full JSON from FINAL_BAV_BANKS.cql didn't work

-- First, let's see what we have now
SELECT key FROM RCasinoSCKS.bankinfocf WHERE key = 6274;

-- The issue: You need to copy the ENTIRE JSON from lines 9-11 of FINAL_BAV_BANKS.cql
-- and paste it as ONE SINGLE LINE in cqlsh

-- STEPS TO FIX:
-- 1. Open FINAL_BAV_BANKS.cql in a text editor
-- 2. Copy the UPDATE statement for bank 6274 (lines 9-11)
-- 3. Make sure it's all on ONE line (remove line breaks in the JSON)
-- 4. Paste into cqlsh and execute

-- The JSON is very long because it contains ALL bank properties
-- You CANNOT update just one property - you must replace the entire JSON

-- After update, verify:
SELECT key FROM RCasinoSCKS.bankinfocf WHERE key = 6274;
-- Then search the output for "COMMON_WALLET_REQUEST_CLIENT_CLASS"
-- It MUST say "Stand art RESTCWClient" (with the typo)
