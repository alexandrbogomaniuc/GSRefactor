-- Fix: Use correct class name StandartRESTCWClient (typo in original code)
UPDATE RCasinoSCKS.bankinfocf 
SET jcn = REPLACE(jcn, 'StandardRESTCWClient', 'StandartRESTCWClient')
WHERE key IN (6274, 6275);
