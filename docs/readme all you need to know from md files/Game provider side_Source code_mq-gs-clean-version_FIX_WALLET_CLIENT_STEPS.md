-- Step 1: Read current bank 6274 configuration to modify
SELECT jcn FROM RCasinoSCKS.bankinfocf WHERE key = 6274;

-- Step 2: After getting the JSON above, modify the COMMON_WALLET_REQUEST_CLIENT_CLASS property
-- Change from: "COMMON_WALLET_REQUEST_CLIENT_CLASS":"com.dgphoenix.casino.payment.wallet.client.v4.StandardRESTCWClient"
-- Change to:   "COMMON_WALLET_REQUEST_CLIENT_CLASS":"com.dgphoenix.casino.payment.wallet.client.v4.StandartRESTCWClient"

-- Step 3: Use the FINAL_BAV_BANKS.cql file which has the complete correct JSON
-- Or manually update by replacing the full JSON:

-- For Bank 6274:
-- Copy the entire JSON from FINAL_BAV_BANKS.cql line 9-11 and execute it

-- For Bank 6275:
-- Copy the entire JSON from FINAL_BAV_BANKS.cql line 13-15 and execute it

-- Step 4: Verify the change
SELECT key FROM RCasinoSCKS.bankinfocf WHERE key IN (6274, 6275);
