-- Cassandra CQL Script: Update Banks 6274 & 6275 for BAV Integration
-- Run this from within the gp3-gs-1 Docker container

-- View current configuration
SELECT bank_id, bank_name, bank_url_authenticate, bank_url_balance, bank_url_bet, bank_url_refund_bet 
FROM RCasinoKS.banks 
WHERE bank_id IN (6274, 6275);

-- Update Bank 6274 (MaxCoins → USD)
UPDATE RCasinoKS.banks 
SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate',
    bank_url_balance = 'http://host.docker.internal:8000/bav/balance',
    bank_url_bet = 'http://host.docker.internal:8000/bav/betResult',
    bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet'
WHERE bank_id = 6274;

-- Update Bank 6275 (QuestCoins → VND)
UPDATE RCasinoKS.banks 
SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate',
    bank_url_balance = 'http://host.docker.internal:8000/bav/balance',
    bank_url_bet = 'http://host.docker.internal:8000/bav/betResult',
    bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet'
WHERE bank_id = 6275;

-- Verify changes
SELECT bank_id, bank_name, bank_url_authenticate, bank_url_balance, bank_url_bet, bank_url_refund_bet 
FROM RCasinoKS.banks 
WHERE bank_id IN (6274, 6275);
