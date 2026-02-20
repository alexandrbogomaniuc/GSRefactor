-- Cassandra CQL: Production-Ready Wallet URLs for BAV Integration
-- Supports multiple deployment scenarios

-- ============================================
-- OPTION 1: LOCAL TESTING (Current Setup)
-- ============================================
-- Use this when both Casino Side and Game Provider are on same machine
-- Game Provider (Docker) → Casino Side (Docker)

UPDATE RCasinoKS.banks 
SET bank_url_authenticate = 'http://host.docker.internal:8000/bav/authenticate',
    bank_url_balance = 'http://host.docker.internal:8000/bav/balance',
    bank_url_bet = 'http://host.docker.internal:8000/bav/betResult',
    bank_url_refund_bet = 'http://host.docker.internal:8000/bav/refundBet'
WHERE bank_id IN (6274, 6275);


-- ============================================
-- OPTION 2: SEPARATE MACHINES (IP Address)
-- ============================================
-- Use this when Casino Side is on a different machine
-- Replace 192.168.1.100 with actual IP of Casino Side server

UPDATE RCasinoKS.banks 
SET bank_url_authenticate = 'http://192.168.1.100:8000/bav/authenticate',
    bank_url_balance = 'http://192.168.1.100:8000/bav/balance',
    bank_url_bet = 'http://192.168.1.100:8000/bav/betResult',
    bank_url_refund_bet = 'http://192.168.1.100:8000/bav/refundBet'
WHERE bank_id IN (6274, 6275);


-- ============================================
-- OPTION 3: PRODUCTION (Domain Name + HTTPS)
-- ============================================
-- Use this for production deployment with SSL/TLS
-- Replace casino-api.yourdomain.com with your actual domain

UPDATE RCasinoKS.banks 
SET bank_url_authenticate = 'https://casino-api.yourdomain.com/bav/authenticate',
    bank_url_balance = 'https://casino-api.yourdomain.com/bav/balance',
    bank_url_bet = 'https://casino-api.yourdomain.com/bav/betResult',
    bank_url_refund_bet = 'https://casino-api.yourdomain.com/bav/refundBet'
WHERE bank_id IN (6274, 6275);


-- ============================================
-- OPTION 4: CLOUD DEPLOYMENT (Load Balancer)
-- ============================================
-- Use this when Casino Side is behind a load balancer
-- Example: AWS ALB, Google Cloud Load Balancer, etc.

UPDATE RCasinoKS.banks 
SET bank_url_authenticate = 'https://casino-lb.yourdomain.com/bav/authenticate',
    bank_url_balance = 'https://casino-lb.yourdomain.com/bav/balance',
    bank_url_bet = 'https://casino-lb.yourdomain.com/bav/betResult',
    bank_url_refund_bet = 'https://casino-lb.yourdomain.com/bav/refundBet'
WHERE bank_id IN (6274, 6275);


-- ============================================
-- VERIFY CONFIGURATION
-- ============================================
SELECT bank_id, bank_name, 
       bank_url_authenticate, 
       bank_url_balance, 
       bank_url_bet, 
       bank_url_refund_bet 
FROM RCasinoKS.banks 
WHERE bank_id IN (6274, 6275);


-- ============================================
-- NOTES
-- ============================================
-- 1. For local testing: Use host.docker.internal (OPTION 1)
-- 2. For LAN deployment: Use private IP like 192.168.x.x or 10.0.x.x (OPTION 2)
-- 3. For internet deployment: Use public IP or domain name (OPTION 3)
-- 4. For production: Always use HTTPS with valid SSL certificate (OPTION 3/4)
-- 5. Firewall: Ensure port 8000 is open on Casino Side server
-- 6. Network: Ensure Game Provider can reach Casino Side (test with curl/ping)
