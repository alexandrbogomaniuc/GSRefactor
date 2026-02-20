-- Update SubCasino 507 to add localhost:8081 to domainNames
UPDATE RCasinoSCKS.subcasinocf 
SET jcn = '{"id":507,"name":"MQ TECNOLOGIA","defaultBank":6274,"bankIds":["java.util.ArrayList",[6274,6275]],"domainNames":["java.util.ArrayList",["default.mqbase.com","localhost:8081"]]}' 
WHERE key = 507;
