-- Add SubCasino 507 with localhost:8081 domain for local testing
-- This allows Game Provider to recognize localhost:8081 as a valid casino domain

UPDATE RCasinoSCKS.subcasinocf 
SET jcn = '{"id":507,"name":"Local Test Casino","defaultBank":6274,"bankIds":["java.util.ArrayList",[6274,6275]],"domainNames":["java.util.ArrayList",["localhost:8081","default-gp3.local.com"]]}' 
WHERE key = 507;
