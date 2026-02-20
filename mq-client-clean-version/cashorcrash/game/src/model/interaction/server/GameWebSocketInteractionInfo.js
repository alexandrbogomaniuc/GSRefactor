import { GAME_CLIENT_MESSAGES, WebSocketInteractionInfo, SERVER_MESSAGES} from '../../../../../../common/PIXI/src/dgphoenix/unified/model/interaction/server/WebSocketInteractionInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

SERVER_MESSAGES.ENTER_GAME_RESPONSE = "EnterLobbyResponse";
SERVER_MESSAGES.BALANCE_UPDATED = "BalanceUpdated";
SERVER_MESSAGES.GAME_TIME_UPDATED = "LobbyTimeUpdated";
SERVER_MESSAGES.STATS = "Stats";
SERVER_MESSAGES.CRASH_GAME_INFO = 'CrashGameInfo';
SERVER_MESSAGES.SIT_IN_RESPONSE = 'SitInResponse';
SERVER_MESSAGES.SIT_OUT_RESPONSE = 'SitOutResponse';
SERVER_MESSAGES.GAME_STATE_CHANGED = 'GameStateChanged';
SERVER_MESSAGES.CRASH_STATE_INFO = 'CrashStateInfo';
SERVER_MESSAGES.CRASH_BET_RESPONSE = "CrashBetResponse";
SERVER_MESSAGES.CRASH_CANCEL_BET_RESPONSE = "CrashCancelBetResponse";
SERVER_MESSAGES.ROUND_RESULT = 'RoundResult';
SERVER_MESSAGES.CRASH_CANCEL_AUTOEJECT_RESPONSE = 'CrashCancelAutoEjectResponse';
SERVER_MESSAGES.CRASH_CHANGE_AUTOEJECT_RESPONSE = 'CrashChangeAutoEjectResponse';
SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_REJECTED_RESPONSE = 'CrashAllBetsRejectedDetailedResponse'; // server sends this message when master bets are not accepted during BUY_IN state (technical state between WAIT and PLAY for bets confirmation), actual in BetsInfo.isNoMoreBetsPeriodMode
SERVER_MESSAGES.CRASH_ALL_COPLAYERS_BETS_REJECTED_RESPONSE = 'CrashAllBetsRejectedResponse'; // server sends this message when co-players bets are not accepted during BUY_IN state (technical state between WAIT and PLAY for bets confirmation), actual in BetsInfo.isNoMoreBetsPeriodMode
SERVER_MESSAGES.CRASH_ALL_MASTER_BETS_CONFIRMED_RESPONSE = 'CrashAllBetsResponse'; // server sends this message when master bets are accepted during BUY_IN state (technical state between WAIT and PLAY for bets confirmation), actual in BetsInfo.isNoMoreBetsPeriodMode
SERVER_MESSAGES.CANCEL_BATTLEGROUND_ROUND =	"CancelBattlegroundRound";
SERVER_MESSAGES.LATENCY = "Latency";
SERVER_MESSAGES.GET_START_GAME_URL_RESPONSE = "GetStartGameUrlResponse";


const CLIENT_MESSAGES = GAME_CLIENT_MESSAGES;
CLIENT_MESSAGES.ENTER = "EnterLobby";
CLIENT_MESSAGES.GET_START_GAME_URL = "GetStartGameUrl";
CLIENT_MESSAGES.CRASH_BET = "CrashBet";
CLIENT_MESSAGES.CRASH_BETS = "CrashBets";
CLIENT_MESSAGES.CRASH_CANCEL_BET = "CrashCancelBet";
CLIENT_MESSAGES.CRASH_CANCEL_ALL_BETS = "CrashCancelAllBets";
CLIENT_MESSAGES.CRASH_CANCEL_AUTOEJECT = "CrashCancelAutoEject";
CLIENT_MESSAGES.CRASH_CHANGE_AUTOEJECT = "CrashChangeAutoEject";
CLIENT_MESSAGES.GET_PRIVATE_BATTLEGROUND_START_URL = "GetPrivateBattlegroundStartGameUrl";


class GameWebSocketInteractionInfo extends WebSocketInteractionInfo
{
	constructor()
	{
		super();
	}

	//override
	getRequestTimeLimit(aRequest_str)
	{
		switch (aRequest_str)
		{
			case CLIENT_MESSAGES.CRASH_BET:
			case CLIENT_MESSAGES.CRASH_BETS:
			case CLIENT_MESSAGES.CRASH_CANCEL_BET:
			case CLIENT_MESSAGES.CRASH_CANCEL_ALL_BETS:
			case CLIENT_MESSAGES.CRASH_CANCEL_AUTOEJECT:
				return 50;
		}
		return super.getRequestTimeLimit(aRequest_str);
	}

	get __differentRequestTypesAvailableTimeIntervals()
	{
		if (APP.isBattlegroundGame)
		{
			return [
						{ requests: [
										{ class: CLIENT_MESSAGES.CRASH_CANCEL_BET, param: { placeNewBet: true } },
										{ class: CLIENT_MESSAGES.CRASH_BET } 
									],
							interval: 2500 }
					];
		};

		return super.__differentRequestTypesAvailableTimeIntervals;
	}
}

export { GameWebSocketInteractionInfo, SERVER_MESSAGES, CLIENT_MESSAGES };