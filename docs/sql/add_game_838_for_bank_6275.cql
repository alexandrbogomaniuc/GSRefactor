USE rcasinoscks;

UPDATE gameinfocf
SET bankidx = '6275',
    bankandcuridx = '6275_VND',
    jcn = '{"id":838,"externalId":null,"bankId":6275,"currency":{"code":"VND","symbol":"\u20ab","basicCurrency":null,"minimalFraction":null},"name":"DRAGONSTONE","gameTypeId":0,"groupId":11,"variableTypeId":1,"rmClassName":null,"gsClassName":"com.dgphoenix.casino.gs.singlegames.tools.cbservtools.SPGameProcessor","limit":null,"coins":["java.util.ArrayList",[{"id":1,"value":10},{"id":15,"value":20},{"id":3,"value":50},{"id":4,"value":100},{"id":5,"value":200}]],"languages":["java.util.ArrayList",["en"]],"propertiesMap":{"_class":"java.util.HashMap","HTML5PC_VERSION_MODE":"AVAILABLE"},"servlet":null,"isMobile":false,"lastUpdateDate":0}'
WHERE key = '6275+838';
