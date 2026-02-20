-- Manual Cassandra Update (run each statement separately)

-- First, check current configuration
SELECT bank_id, bank_name, bank_url_authenticate, bank_url_balance, bank_url_bet 
FROM RCasinoKS.banks 
WHERE bank_id = 6274;

SELECT bank_id, bank_name, bank_url_authenticate, bank_url_balance, bank_url_bet 
FROM RCasinoKS.banks 
WHERE bank_id = 6275;

-- Update Bank 6274
UPDATE RCasinoKS.banks 
SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate'
WHERE bank_id = 6274;

UPDATE RCasinoKS.banks 
SET bank_url_balance = 'http://host.docker.internal:8000/bav/balance'
WHERE bank_id = 6274;

UPDATE RCasinoKS.banks 
SET bank_url_bet = 'http://host.docker.internal:8000/bav/betResult'
WHERE bank_id = 6274;

UPDATE RCasinoKS.banks 
SET bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet'
WHERE bank_id = 6274;

-- Update Bank 6275
UPDATE RCasinoKS.banks 
SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate'
WHERE bank_id = 6275;

UPDATE RCasinoKS.banks 
SET bank_url_balance = 'http://host.docker.internal:8000/bav/balance'
WHERE bank_id = 6275;

UPDATE RCasinoKS.banks 
SET bank_url_bet = 'http://host.docker.internal:8000/bav/betResult'
WHERE bank_id = 6275;

UPDATE RCasinoKS.banks 
SET bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet'
WHERE bank_id = 6275;

-- Verify updates
SELECT bank_id, bank_name, bank_url_authenticate, bank_url_balance, bank_url_bet, bank_url_refund_bet 
FROM RCasinoKS.banks 
WHERE bank_id IN (6274, 6275);
