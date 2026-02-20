USE rcasinoscks;

-- Root fix for launch flow:
-- gameId 838 must be MP (gameTypeId=1), otherwise GS goes to /real/en/launch.jsp
-- and sends websocket=null which breaks lobby WS bootstrap.

UPDATE gametinfocf
SET jcn = '{"gameId":838,"gameName":"DRAGONSTONE","jackpotGame":false,"gameVariationIdBase":null,"defaultGameInfo":{"id":838,"externalId":null,"bankId":0,"currency":null,"name":"DRAGONSTONE","gameTypeId":1,"groupId":11,"variableTypeId":1,"rmClassName":null,"gsClassName":"com.dgphoenix.casino.gs.singlegames.tools.cbservtools.SPGameProcessor","limit":null,"coins":["java.util.ArrayList",[]],"languages":["java.util.ArrayList",["en"]],"propertiesMap":{"_class":"java.util.HashMap","LGA_APPROVED":"TRUE","DEVELOPMENT_VERSION":"FALSE","JACKPOT3_GAME":"FALSE","KEY_SINGLE_GAME_ID_FOR_ALL_PLATFORMS":"TRUE","DEFCOIN":"2","ISENABLED":"TRUE","CDN_SUPPORT":"TRUE","KEY_ACS_ENABLED":"FALSE","MAX_WIN":"25000","RTP":"97.5","HTML5PC_VERSION_MODE":"AVAILABLE"},"servlet":null,"isMobile":false,"lastUpdateDate":0},"servlet":"/MQ_Dragonstone.game","gameControllerClass":null,"endRoundSignature":null,"roundFinishedHelper":null,"title":"Max Quest: Dragonstone","swfLocation":"/html5pc/actiongames/dragonstone/","additionalParams":"","oldTranslation":false,"isFrbGame":true}'
WHERE key = 838;

UPDATE gameinfocf
SET jcn = '{"id":838,"externalId":null,"bankId":6274,"currency":{"code":"MMC","symbol":"MMC","basicCurrency":null,"minimalFraction":null},"name":"DRAGONSTONE","gameTypeId":1,"groupId":11,"variableTypeId":1,"rmClassName":null,"gsClassName":"com.dgphoenix.casino.gs.singlegames.tools.cbservtools.SPGameProcessor","limit":null,"coins":["java.util.ArrayList",[{"id":1,"value":10},{"id":15,"value":20},{"id":3,"value":50},{"id":4,"value":100},{"id":5,"value":200}]],"languages":["java.util.ArrayList",["en"]],"propertiesMap":{"_class":"java.util.HashMap","HTML5PC_VERSION_MODE":"AVAILABLE"},"servlet":null,"isMobile":false,"lastUpdateDate":0}'
WHERE key = '6274+838';

UPDATE gameinfocf
SET jcn = '{"id":838,"externalId":null,"bankId":6275,"currency":{"code":"VND","symbol":"\\u20ab","basicCurrency":null,"minimalFraction":null},"name":"DRAGONSTONE","gameTypeId":1,"groupId":11,"variableTypeId":1,"rmClassName":null,"gsClassName":"com.dgphoenix.casino.gs.singlegames.tools.cbservtools.SPGameProcessor","limit":null,"coins":["java.util.ArrayList",[{"id":1,"value":10},{"id":15,"value":20},{"id":3,"value":50},{"id":4,"value":100},{"id":5,"value":200}]],"languages":["java.util.ArrayList",["en"]],"propertiesMap":{"_class":"java.util.HashMap","HTML5PC_VERSION_MODE":"AVAILABLE"},"servlet":null,"isMobile":false,"lastUpdateDate":0}'
WHERE key = '6275+838';
