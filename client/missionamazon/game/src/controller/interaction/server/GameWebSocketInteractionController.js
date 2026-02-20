import WebSocketInteractionController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/server/WebSocketInteractionController';
import {GameWebSocketInteractionInfo, SERVER_MESSAGES, CLIENT_MESSAGES} from '../../../model/interaction/server/GameWebSocketInteractionInfo';
import {DEBUG_WEB_SOCKET_URL} from '../../../config/Constants';
import {WEAPONS} from '../../../../../shared/src/CommonConstants';
import {APP} from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../../../main/GameScreen'
import Game from '../../../Game'
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import GameExternalCommunicator from '../../../external/GameExternalCommunicator';
import {LOBBY_MESSAGES, GAME_MESSAGES} from '../../../external/GameExternalCommunicator';
import GameStateController from '../../state/GameStateController';
import { ROUND_STATE } from '../../../model/state/GameStateInfo';
import TournamentModeController from '../../custom/tournament/TournamentModeController';
import { ERROR_CODE_TYPES } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/interaction/server/WebSocketInteractionInfo';
import GamePendingOperationController from '../../custom/GamePendingOperationController';
import GameCafRoomManagerController from '../../uis/battleground/GameCafRoomManagerController';
import InfoPanelController from '../../uis/info_panel/InfoPanelController';

class GameWebSocketInteractionController extends WebSocketInteractionController
{
	static get EVENT_ON_SERVER_MESSAGE()							{ return WebSocketInteractionController.EVENT_ON_SERVER_MESSAGE };
	static get EVENT_ON_SERVER_CONNECTION_CLOSED()					{ return WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED };
	static get EVENT_ON_SERVER_CONNECTION_OPENED()					{ return WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED };
	static get EVENT_ON_SERVER_ERROR_MESSAGE()						{ return WebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE };
	static get EVENT_ON_SERVER_OK_MESSAGE()							{ return WebSocketInteractionController.EVENT_ON_SERVER_OK_MESSAGE };

	static get EVENT_ON_SERVER_GET_ROOM_INFO_RESPONSE_MESSAGE()		{ return "EVENT_ON_SERVER_GET_ROOM_INFO_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_FULL_GAME_INFO_MESSAGE()				{ return "EVENT_ON_SERVER_FULL_GAME_INFO_MESSAGE" };
	static get EVENT_ON_SERVER_SIT_IN_RESPONSE_MESSAGE()			{ return "EVENT_ON_SERVER_SIT_IN_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_SIT_OUT_RESPONSE_MESSAGE()			{ return "EVENT_ON_SERVER_SIT_OUT_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_NEW_ENEMY_MESSAGE()					{ return "EVENT_ON_SERVER_NEW_ENEMY_MESSAGE" };
	static get EVENT_ON_SERVER_NEW_ENEMIES_MESSAGE()				{ return "EVENT_ON_SERVER_NEW_ENEMIES_MESSAGE"};
	static get EVENT_ON_SERVER_MISS_MESSAGE()						{ return "EVENT_ON_SERVER_MISS_MESSAGE" };
	static get EVENT_ON_SERVER_HIT_MESSAGE()						{ return "EVENT_ON_SERVER_HIT_MESSAGE" };
	static get EVENT_ON_SERVER_NEW_TREASURE_MESSAGE()				{ return "EVENT_ON_SERVER_NEW_TREASURE_MESSAGE" };
	static get EVENT_ON_SERVER_LEVEL_UP_MESSAGE()					{ return "EVENT_ON_SERVER_LEVEL_UP_MESSAGE" };
	static get EVENT_ON_SERVER_CLIENTS_INFO_MESSAGE()				{ return "EVENT_ON_SERVER_CLIENTS_INFO_MESSAGE" };
	static get EVENT_ON_SERVER_ROUND_RESULT_MESSAGE()				{ return "EVENT_ON_SERVER_ROUND_RESULT_MESSAGE" };
	static get EVENT_ON_SERVER_GAME_STATE_CHANGED_MESSAGE()			{ return "EVENT_ON_SERVER_GAME_STATE_CHANGED_MESSAGE" };
	static get EVENT_ON_SERVER_BUY_IN_RESPONSE_MESSAGE()			{ return "EVENT_ON_SERVER_BUY_IN_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE()			{ return "EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE" };
	static get EVENT_ON_SERVER_ENEMY_DESTROYED_MESSAGE()			{ return "EVENT_ON_SERVER_ENEMY_DESTROYED_MESSAGE" };
	static get EVENT_ON_SERVER_CHANGE_MAP_MESSAGE()					{ return "EVENT_ON_SERVER_CHANGE_MAP_MESSAGE" };
	static get EVENT_ON_SERVER_WEAPONS_MESSAGE()					{ return "EVENT_ON_SERVER_WEAPONS_MESSAGE" };
	static get EVENT_ON_SERVER_WEAPON_SWITCHED_MESSAGE()			{ return "EVENT_ON_SERVER_WEAPON_SWITCHED_MESSAGE" };
	static get EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE()			{ return "EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE" };
	static get EVENT_ON_SERVER_UPDATE_TRAJECTORIES_MESSAGE()		{ return "EVENT_ON_SERVER_UPDATE_TRAJECTORIES_MESSAGE"};
	static get EVENT_ON_SERVER_ROUND_FINISH_SOON()					{ return "EVENT_ON_SERVER_ROUND_FINISH_SOON"};
	static get EVENT_ON_CONNECTION_RECOVERY_STARTED()				{ return "EVENT_ON_CONNECTION_RECOVERY_STARTED" };
	static get EVENT_ON_SERVER_BET_LEVEL_CHANGED()					{ return "EVENT_ON_SERVER_BET_LEVEL_CHANGED" };
	static get EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE() 		{ return "EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE" };
	static get EVENT_ON_SERVER_FRB_ENDED_MESSAGE()					{ return "EVENT_ON_SERVER_FRB_ENDED_MESSAGE" };
	static get EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED() 			{ return "EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED" };
	static get EVENT_ON_SERVER_BULLET_RESPONSE() 					{ return "EVENT_ON_SERVER_BULLET_RESPONSE" };
	static get EVENT_ON_SERVER_BULLET_CLEAR_RESPONSE() 				{ return "EVENT_ON_SERVER_BULLET_CLEAR_RESPONSE" };
	static get EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND() 			{ return "EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND" };
	static get EVENT_BATTLEGROUND_SCORE_BOARD() 					{ return "EVENT_BATTLEGROUND_SCORE_BOARD" };
	static get EVENT_ON_DELAYED_RICOCHET_SHOT_REMOVED() 			{ return "EVENT_ON_DELAYED_RICOCHET_SHOT_REMOVED" };
	static get EVENT_ON_SERVER_BATTLEGROUND_BUY_IN_CONFIRMED_SEATS() { return "EVENT_ON_SERVER_BATTLEGROUND_BUY_IN_CONFIRMED_SEATS" };
	static get EVENT_ON_SERVER_LATENCY_REQUEST() 					{ return "EVENT_ON_SERVER_LATENCY_REQUEST" };


	static get EVENT_ON_REQUEST_WEAPON_UPDATE_SENDED() 				{ return "EVENT_ON_REQUEST_WEAPON_UPDATE_SENDED" };

	static get EVENT_ON_CONNECTION_READY_TO_ROOM_OPEN()			 	{ return "EVENT_ON_CONNECTION_READY_TO_ROOM_OPEN" };

	static get EVENT_ON_GAME_CLIENT_SENT_MESSAGE()					{return WebSocketInteractionController.EVENT_ON_GAME_CLIENT_SENT_MESSAGE; }

	static get EVENT_ON_RESTORE_AFTER_OFFLINE_FINISHED()			{return "EVENT_ON_RESTORE_AFTER_OFFLINE_FINISHED";}

	static get EVENT_ON_SERVER_BATTLEGROUND_CAF_KICK_RESPONSE()		{return "EVENT_ON_SERVER_BATTLEGROUND_CAF_KICK_RESPONSE";}

	constructor()
	{
		super(new GameWebSocketInteractionInfo());

		this._gameScreen = null;
		this._reconnectingAfterGameUrlUpdatedRequired = false;
		this._fLobbyConnectionState_bln = true;
		this._fLastWeapon_int = null;
		this._hitDataUniqId = 0;
		this._fTournamentModeInfo_tmi = null;
		this._requests_shot_list = [];

		this._fOpenRoomSent_bln = false;

		this._fRestoreAfterOffline_bl = false;
	}

	get lastUniqRequestId()
	{
		return this._requestUniqId;
	}

	get _debugWebSocketUrl()
	{
		return this._webSocketUrl;
		return DEBUG_WEB_SOCKET_URL;
	}

	get _webSocketUrl()
	{
		return APP.urlBasedParams.WEB_SOCKET_URL;
	}

	get hasUnrespondedShots()
	{
		return this._hasUnRespondedRequest(CLIENT_MESSAGES.SHOT);
	}

	get hasDelayedShots()
	{
		return super._hasDelayedRequests(CLIENT_MESSAGES.SHOT);
	}

	get delayedRicochetShotsAmount()
	{
		let lHasDelayedShots_bl = this.hasDelayedShots;

		if (!lHasDelayedShots_bl)
		{
			return 0;
		}

		let lDelayedShotRequests = this._delayedShotRequests;
		if (!lDelayedShotRequests || !lDelayedShotRequests.length)
		{
			return 0;
		}

		let lAmount = 0;
		for (let i=0; i<lDelayedShotRequests.length; i++)
		{
			let lCurDelayedShotRequest = lDelayedShotRequests[i];

			if (lCurDelayedShotRequest.data.bulletId !== undefined)
			{
				lAmount += 1;
			}
		}

		return lAmount;
	}

	get _delayedShotRequests()
	{
		if (!this._delayedRequests)
		{
			return null;
		}

		let lRequests = [];

		for (let i=0; i<this._delayedRequests.length; i++)
		{
			let curDelayedRequestInfo = this._delayedRequests[i];
			if (curDelayedRequestInfo.class == CLIENT_MESSAGES.SHOT)
			{
				lRequests.push(curDelayedRequestInfo);
			}
		}

		return lRequests;
	}

	get isSitoutRequestInProgress()
	{
		return this._hasUnRespondedRequest(CLIENT_MESSAGES.SIT_OUT) || this._hasDelayedRequests(CLIENT_MESSAGES.SIT_OUT);
	}

	get isCloseRoomRequestInProgress()
	{
		return this._hasUnRespondedRequest(CLIENT_MESSAGES.CLOSE_ROOM);
	}

	get isRebuyRequestInProgress()
	{
		return this._hasUnRespondedRequest(CLIENT_MESSAGES.RE_BUY);
	}

	get isShotRequestParseInProgress()
	{
		for (let id in this._requests_shot_list)
		{
			if (this._requests_shot_list[id])
			{
				return true;
			}
		}

		return false;
	}

	get isBuyInRequestInProgress()
	{
		return this._hasUnRespondedRequest(CLIENT_MESSAGES.BUY_IN);
	}

	clearShotResponseParsed()
	{
		this._requests_shot_list = [];
	}

	__initControlLevel()
	{
		let tournamentModeController = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = tournamentModeController.info;
		tournamentModeController.on(TournamentModeController.EVENT_ON_TOURNAMENT_CLIENT_STATE_CHANGED, this._onTournamentModeClientStateChanged, this);

		super.__initControlLevel();

		APP.once("onGameStarted", this._onGameStarted, this);

		let gameScreen = this._gameScreen = APP.gameScreen;
		gameScreen.on(GameScreen.EVENT_ON_GAME_SOCKET_URL_UPDATED, this._onGameSocketUrlUpdated, this);
		gameScreen.on(GameScreen.EVENT_ON_FULL_GAME_INFO_REQUIRED, this._onFullGameInfoRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_CLOSE_ROOM_REQUIRED, this._onCloseRoomRequired, this);
		gameScreen.on(GameScreen.EVENT_BATTLEGROUND_CONFIRM_BUY_IN_REQUIRED, this._onBattlegroundConfirmBuyInRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_SIT_IN_REQUIRED, this._onSitInRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_SIT_OUT_REQUIRED, this._onSitOutRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_SHOT_TRIGGERED, this._onShotTriggered, this);
		gameScreen.on(GameScreen.EVENT_ON_BUY_IN_REQUIRED, this._onBuyInRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_RE_BUY_REQUIRED, this._onReBuyRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_CHANGE_STAKE_REQUIRED, this._onChangeStakeRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_WEAPON_UPDATED, this._onWeaponUpdated, this);
		gameScreen.on(GameScreen.EVENT_ON_ROUND_RESULT_SCREEN_DEACTIVATED, this._onRoundResultScreenClosed, this);
		gameScreen.on(GameScreen.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED, this._onRoundResultScreenOpened, this);
		gameScreen.on(GameScreen.EVENT_ON_BACK_TO_LOBBY_BUTTON_CLICKED, this._onGameBackToLobbyInitiated, this);
		gameScreen.on(GameScreen.EVENT_ON_BET_MULTIPLIER_UPDATE_REQUIRED, this._onBetLevelChangeRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_BULLET, this._onBullet, this);
		gameScreen.on(GameScreen.EVENT_ON_BULLET_CLEAR, this._onBulletClear, this);
		gameScreen.on(GameScreen.EVENT_BATTLEGROUND_REQUEST_TIME_TO_START, this._onBattlegroundTimeTostartRequest, this);
		gameScreen.on(GameScreen.EVENT_ON_WEAPON_PAID_MULTIPLIER_UPDATE_REQUIRED, this._onWeaponPaidMultiplierUpdateRequired, this);
		gameScreen.on(GameScreen.EVENT_ON_SHOT_RESPONSE_PARSED, this._onShotResponseParsed, this);
		gameScreen.on(GameScreen.EVENT_ON_NO_EMPTY_SEATS, this._onNoPlaceToSeatHandler, this);
		gameScreen.on(GameScreen.EVENT_ON_BTG_ROUND_OBSERVER_DENIED, this._onBattlegroundRoundObserverDenied, this);
		gameScreen.infoPanelController.on(InfoPanelController.LATENCY_REQUEST, this._onLatencyRequested, this);

		if (gameScreen.isReady)
		{
			gameScreen.gameStateController.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
			gameScreen.on(GameScreen.EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS, this._onTimeToOpenRealRoomAfterBonus, this);
		}
		else
		{
			gameScreen.once(GameScreen.EVENT_ON_READY, () => {
																gameScreen.gameStateController.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
																gameScreen.on(GameScreen.EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS, this._onTimeToOpenRealRoomAfterBonus, this);
															}, this);
		}
		
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyMessageReceived, this);

		APP.on(Game.EVENT_ON_ASSETS_LOADING_ERROR, this._onAssetsLoadingError, this);
		APP.on(Game.EVENT_ON_WEBGL_CONTEXT_LOST, this._onWebglContextLost, this);
		APP.on(Game.EVENT_ON_BONUS_CANCEL_ROOM_RELOAD, this._onBonusCancelRoomReaload, this);
		APP.on(Game.EVENT_ON_OFFLINE, this._onOffline, this)
		APP.on(Game.EVENT_ON_ONLINE_RESTORED, this._onOnlineRestored, this);

		APP.on('onTickerResumed', (e) => this._onTickerResumed(e));

		let l_poc = this._fPendingOperationController_poc = APP.pendingOperationController;
		l_poc.on(GamePendingOperationController.EVENT_ON_PENDING_OPERATION_STARTTED, this._onPendingOperationStarted, this);
		l_poc.on(GamePendingOperationController.EVENT_ON_REFRESH_PENDING_OPERATION_STATUS_REQUIRED, this._onRefreshPendingOperationStatusRequired, this);
		l_poc.on(GamePendingOperationController.EVENT_ON_PENDING_OPERATION_STATUS_TRACKING_TURNED_OFF, this._onPendingOperationStatusTrackingTurnedOff, this);		

		// DEBUG...
		// window.addEventListener(
		// 	"keydown", this.keyDownHandler.bind(this), false
		// );
		// ...DEBUG
	}

	_onGameStarted(event)
	{
		if (APP.isBattlegroundGame && APP.isCAFMode)
		{
			console.log("latency web socket listener inited")
			APP.gameScreen.battlegroundGameController.cafRoomManagerController.on(GameCafRoomManagerController.EVENT_ON_BATTLEGROUND_START_PRIVATE_ROOM, this._onBattlegroundStartPrivateRoom, this);
			APP.gameScreen.battlegroundGameController.cafRoomManagerController.on(GameCafRoomManagerController.EVENT_ON_BATTLEGROUND_PLAYER_KICK_TRIGGERED, this._onBattlegroundPlayerKickTriggered, this);
			APP.gameScreen.battlegroundGameController.cafRoomManagerController.on(GameCafRoomManagerController.EVENT_ON_BATTLEGROUND_PLAYER_REINVITE_TRIGGERED, this._onBattlegroundPlayerReinviteTriggered, this);
			APP.gameScreen.battlegroundGameController.cafRoomManagerController.on(GameCafRoomManagerController.EVENT_ON_BATTLEGROUND_PLAYER_INVITE_TRIGGERED, this._onBattlegroundPlayerInviteTriggered, this);
			
		}
	}

	_onShotResponseParsed(aRequestData)
	{
		if (aRequestData.class == CLIENT_MESSAGES.SHOT && aRequestData.rid >= 0)
			{
				let requestData = this._requests_shot_list[aRequestData.rid];
				if (!!requestData)
				{
					delete this._requests_shot_list[aRequestData.rid];
				}
			}
	}

	//DEBUG...
	keyDownHandler(keyCode)
	{
		/*
		var msg =
		{
			"newKing": Math.round(Math.random()),
			"class":"KingOfHillChanged",
			"rid":9,
		}
		msg.date = APP.gameScreen.currentTime
		this._processServerMessage(msg);
		*/
		// if (keyCode.keyCode == 103)
		// {
		// 	const currentTime =  APP.gameScreen.currentTime;//(new Date()).getTime();
		// 	// const startTime = currentTime-window.ddd;

		// 	// var msg = {"newEnemy":{"id":123456789,"typeId":7,"speed":4.0,"awardedPrizes":"","awardedSum":0.0,"energy":700.0,"fullEnergy":700.0,"skin":1,
		// 	// 				"trajectory":{"speed":4.0,"points":[
		// 	// 														{"invulnerable":true,"x":800.0,"y":191.25,"time":startTime},
		// 	// 														{"x":800.0,"y":191.25,"time":startTime+500},
		// 	// 														{"x":800.0,"y":191.25,"time":startTime+500+1200},
		// 	// 														{"x":250.0,"y":300,"time":startTime+500+1200+1000},
		// 	// 														{"x":250.0,"y":300,"time":startTime+500+1200+1000+1700},
		// 	// 														{"teleport":true,"invulnerable":true, "x":250.0,"y":300,"time":startTime+500+1200+1000+1700},
		// 	// 														{"invulnerable":true, "x":250.0,"y":300,"time":startTime+500+1200+1000+1700+700},
		// 	// 														{"invulnerable":true, "x":250.0,"y":300,"time":startTime+500+1200+1000+1700+700+1500},

		// 	// 														{"invulnerable":true,"x":150.0,"y":191.25,"time":startTime+7500},
		// 	// 														{"x":150.0,"y":191.25,"time":startTime+500+7500},
		// 	// 														{"x":150.0,"y":191.25,"time":startTime+500+1200+7500},
		// 	// 														{"x":600.0,"y":300,"time":startTime+500+1200+1000+7500},
		// 	// 														{"x":600.0,"y":300,"time":startTime+500+1200+1000+1700+7500},
		// 	// 														{"teleport":true,"invulnerable":true, "x":600.0,"y":300,"time":startTime+500+1200+1000+1700+7500},
		// 	// 														{"invulnerable":true, "x":600.0,"y":300,"time":startTime+500+1200+1000+1700+700+7500},
		// 	// 														{"invulnerable":true, "x":600.0,"y":300,"time":startTime+500+1200+1000+1700+700+1500+7500}

		// 	// 													],
		// 	// 							"maxSize":300},"parentEnemyId":-1,"members":[],"swarmId":0,"swarmType":0},
		// 	// 				"date":1622526701978,"rid":-1,"class":"NewEnemy"}

		// 	// for (let point of msg.newEnemy.trajectory.points)
		// 	// {
		// 	// 	point.time = currentTime + point.time - startTime;
		// 	// }
		// 	// msg.date = currentTime;
		// 	// this._processServerMessage(msg);


		// 	var msg = {"enemies":[{"id":4085541213,"typeId":15,"speed":4.0,"awardedPrizes":"","awardedSum":0.0,"energy":3000.0,"fullEnergy":3000.0,"skin":1,"trajectory":{"speed":4.0,"points":[{"x":-80.0,"y":478.125,"time":1623300301373},{"x":220.0,"y":309.375,"time":1623300308873},{"x":600.0,"y":523.125,"time":1623300318373},{"x":860.0,"y":376.875,"time":1623300324873},{"x":520.0,"y":185.625,"time":1623300333373},{"x":50.0,"y":450.0,"time":1623300345123},{"x":110.0,"y":483.75,"time":1623300346623},{"x":770.0,"y":112.5,"time":1623300363123},{"x":840.0,"y":151.875,"time":1623300364873},{"x":260.0,"y":478.125,"time":1623300379373},{"x":60.0,"y":365.625,"time":1623300384373},{"x":340.0,"y":208.125,"time":1623300391373},{"x":860.0,"y":500.625,"time":1623300404373},{"x":940.0,"y":455.625,"time":1623300406373},{"x":450.0,"y":180.0,"time":1623300418623},{"x":30.0,"y":416.25,"time":1623300429123},{"x":150.0,"y":483.75,"time":1623300432123},{"x":30.0,"y":551.25,"time":1623300435123},{"x":-80.0,"y":489.375,"time":1623300437873}],"maxSize":300},"parentEnemyId":-1,"members":[],"swarmId":0,"swarmType":0}],"date":1623300300373,"rid":-1,"class":"NewEnemies"}
		// 	const startTime = msg.enemies[0].trajectory.points[0].time;
		// 	let timedelta = 0;
		// 	for (let i=0; i< msg.enemies[0].trajectory.points.length; i++)
		// 	// for (let point of msg.enemies[0].trajectory.points)
		// 	{
		// 		let point = msg.enemies[0].trajectory.points[i];
		// 		point.origtime = point.time;
		// 		point.time = currentTime + point.time - startTime;

		// 		if (i > 0)
		// 		{
		// 			let pp = msg.enemies[0].trajectory.points[i-1];
		// 			// timedelta += (point.origtime - pp.origtime)*1;
		// 			point.time += timedelta;
		// 		}
		// 	}
		// 	msg.date = currentTime;
		// 	this._processServerMessage(msg);
		// }

		// if (keyCode.keyCode == 16)
		// {
		// 	if (this._webSocket && this._webSocket.readyState === WebSocket.OPEN)
		// 	{
		// 		//close connection...
		// 		this._closeConnectionIfPossible();
		// 		this.emit(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, {wasClean: false});

		// 		setTimeout(this._establishConnection.bind(this), 3000);
		// 		//...close connection

		// 		//network error...
		// 		// this._processServerMessage({"code":2,"msg":"Server shutdown","date":1538637759398,"rid":-1,"class":"Error"});
		// 		// setTimeout(this._closeConnectionIfPossible.bind(this), 2000);
		// 		//...network error

		// 		//error code...
		// 		// this._processServerMessage({"code": 1008,"msg": "ROOM_NOT_FOUND","date": 1496748898812,"class": "Error","rid": 1});
		// 		//...error code
		// 	}
		// }

		// if (keyCode.keyCode == 32)
		// {
		// 	let msgBoss = {"newEnemy":{"id":43292,"typeId":26,"speed":1.0,"awardedPrizes":"","awardedSum":0.0,"energy":1.0,"fullEnergy":1.0,"skin":1,"trajectory":{"speed":1.0,"points":[{"invulnerable":true,"x":653.0,"y":132.625,"time":1626680551845},{"x":653.0,"y":132.625,"time":1626680557845},{"x":653.0,"y":132.625,"time":1626680567845}],"maxSize":300},"parentEnemyId":-1,"members":[],"swarmId":0,"swarmType":0},"date":1626680550845,"rid":-1,"class":"NewEnemy"}

		// 	const startTime = msgBoss.newEnemy.trajectory.points[0].time;
		// 	const currentTime =  (new Date()).getTime();

		// 	for (let point of msgBoss.newEnemy.trajectory.points)
		// 	{
		// 		point.time = currentTime + point.time - startTime;
		// 	}
		// 	msgBoss.date = currentTime;

		// 	this._processServerMessage(msgBoss);
		// }
		// else if (keyCode.keyCode == 65)
		// {
		// 	let msgBossHit = {"seatId":0,"damage":2480.0,"win":20.0,"awardedWeaponId":-1,"usedSpecialWeapon":-1,"remainingSWShots":0,"score":1.0,"enemy":{"id":182,"typeId":21,"speed":3.200000047683716,"awardedPrizes":"","awardedSum":0.0,"energy":20,"fullEnergy":2500.0,"skin":1,"parentEnemyId":-1,"members":[],"swarmId":0,"swarmType":0},"hit":true,"awardedWeaponShots":0,"killed":false,"lastResult":true,"multiplierPay":0,"killBonusPay":0.0,"currentWin":25.0,"hvEnemyId":-1,"x":649.5831,"y":151.7215,"newFreeShots":0,"newFreeShotsSeatId":0,"hitResultBySeats":{"0":[{"id":0,"value":"20.0"}]},"instanceKill":false,"chMult":1,"awardedWeapons":[],"needExplode":false,"isExplode":false,"gems":[],"enemyId":182,"shotEnemyId":182,"enemiesInstantKilled":{},"date":1598847224885,"rid":181,"class":"Hit"}

		// 	msgBossHit.date = (new Date()).getTime();
		// 	this._processServerMessage(msgBossHit);
		// }
	}
	//...DEBUG

	_handleServerMessage(messageData, requestData)
	{
		super._handleServerMessage(messageData, requestData);

		let msgClass = messageData.class;

	    if(msgClass == SERVER_MESSAGES.ERROR)
		{
			let errorCode = messageData.code;
			if(errorCode == WebSocketInteractionController.ERROR_CODES.TOO_MANY_OBSERVERS)
			{
				APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.DO_NOT_COUNT_START_GAME_RECONNECTION_ATTEMPT);
			}
		}

		switch(msgClass)
		{
			case SERVER_MESSAGES.OK:
				let requestClass = undefined;
				if (requestData && requestData.rid >= 0)
				{
					requestClass = requestData.class;
					if (requestClass === CLIENT_MESSAGES.CLOSE_ROOM)
					{
						this.onRoomClosed();
					}
				}
				break;
		}
	}

	_specifyErrorCodeSeverity(messageData, requestData)
	{
		let errorCode = messageData.code;
		let errorCodeSeverity;

		if (
				errorCode == WebSocketInteractionController.ERROR_CODES.BAD_REQUEST
				&& (!!requestData && requestData.class === CLIENT_MESSAGES.SHOT && requestData.bulletId !== undefined)
			)
		{
			// BAD_REQUEST for ricochet shots is not considered as fatal error due to https://jira.dgphoenix.com/browse/DRAG-986
			errorCodeSeverity = ERROR_CODE_TYPES.ERROR;
		}
		else if (
					errorCode == WebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION
					&& this._fPendingOperationController_poc.info.isPendingOperationHandlingSupported
				)
		{
			errorCodeSeverity = ERROR_CODE_TYPES.ERROR;
		}
		else if (
					errorCode == WebSocketInteractionController.ERROR_CODES.ROOM_WAS_DEACTIVATED
					&& APP.isCAFMode
					&& (!requestData || requestData.class != CLIENT_MESSAGES.OPEN_ROOM)
					&& APP.gameScreen.gameField && APP.gameScreen.gameField.isGameplayStarted
					&& !APP.gameScreen.isPaused && !APP.gameScreen.restoreAfterLagsInProgress
					&& APP.gameScreen.gameStateController.info.isQualifyState
					&& APP.gameScreen.gameField.roundResultScreenController.info.roundResultResponseRecieved
				)
		{
			this.info.addDelayedFatalError(errorCode);

			errorCodeSeverity = ERROR_CODE_TYPES.ERROR;
		}
		else
		{
			errorCodeSeverity = super._specifyErrorCodeSeverity(messageData, requestData);
		}

		return errorCodeSeverity;
	}

	_onBonusCancelRoomReaload()
	{
		this._fIsReopenSocketRequired_bl = false;
	}

	_onOffline()
	{
		if (!APP.isBattlegroundGame && !this._oldWebSocket)
		{
			this._requests_list = {};
			this._clearSocketHandlers();
			this._clearDelayedRequests();
			this._oldWebSocket = this._webSocket;
			this._oldWebSocket.onclose = function ()
			{
				this._oldWebSocket = null;
			}
			this._webSocket = null;
		}
		else
		{
			this._closeConnectionIfPossible();
		}
		this.emit(WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, {wasClean: false});
	}

	_onOnlineRestored()
	{
		this._fRestoreAfterOffline_bl = true;
		this._startRecoveringSocketConnection();
	}

	onRoomClosed()
	{
		this._closeConnectionIfPossible();
		this._oldWebSocket && this._oldWebSocket.close();
		if (APP.currentWindow.gameBonusController.info.isRoomRestartRequired)
		{
			//wait for EnterLobby completion, then - establish new connection
			this._fIsReopenSocketRequired_bl = true;
		}
		this._fOpenRoomSent_bln = false;
	}

	_processServerMessage(messageData)
	{
		let msgClass = messageData.class;

		//DEBUG...
		// if (!window.hasEnterLobby && msgClass == "GetRoomInfoResponse")
		// {
		// 	window.hasEnterLobby = true;
		// 	messageData.cashBonusInfo = {
		// 		id: 345345,
		// 		amountToRelease: 0,
		// 		balance: 10000,
		// 		amount: 10000,
		// 		status: "ACTIVE"
		// 	}
		// 	if (messageData.balance)
		// 	{
		// 		messageData.balance = 10000;
		// 	}
		// }
		// if (msgClass == "SitOutResponse" && APP.currentWindow.gameBonusController.info.isActivated)
		// {
		// 	messageData.rid = -1;
		// 	messageData.nextRoomId = "70011";
		// }
		/*if (msgClass == "RoundResult")
		{
			messageData.hasNextFrb = true;
		}*/
		//...DEBUG

		switch(msgClass)
		{
			case SERVER_MESSAGES.HIT:
				messageData.id = this._hitDataUniqId++;
				break;
			case SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE:
				if (this._fRestoreAfterOffline_bl)
				{
					this._fRestoreAfterOffline_bl = false;
					this.emit(GameWebSocketInteractionController.EVENT_ON_RESTORE_AFTER_OFFLINE_FINISHED);
				}
				break;
		}

		//DEBUG...
		// if (msgClass == "Hit" || msgClass == "Miss") return;
		//..DEBUG

		super._processServerMessage(messageData);
	}

	__performActionWithRequestOnGameLevel(aRequestData)
	{
		if (aRequestData.class == CLIENT_MESSAGES.SHOT)
		{
			this._requests_shot_list[aRequestData.rid] = aRequestData;
		}
	}

	_sendRequest(requestClass, requestData)
	{
		if (this._fTournamentModeInfo_tmi.isTournamentOnServerCompletedState)
		{
			return;
		}

		if (
				!this._fOpenRoomSent_bln
				&& requestClass !== CLIENT_MESSAGES.OPEN_ROOM
				&& requestClass !== CLIENT_MESSAGES.CHECK_PENDING_OPERATION_STATUS
			)
		{
			if (requestClass === CLIENT_MESSAGES.SIT_IN)
			{
				APP.gameScreen.clearPendingRequestSitin();
			}

			return;
		}

		super._sendRequest(requestClass, requestData);
	}

	_onConnectionOpened()
	{
		console.log("Game -> _onConnectionOpened");
		this._fPendingOperationController_poc.off(GamePendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompletedForOpenRoom, this, true);

		if (this._fTournamentModeInfo_tmi.isTournamentOnClientCompletedState)
		{
			this._stopReconnecting();
			this._blockAfterCriticalError();
			this._stopServerMesagesHandling();
			this._closeConnectionIfPossible();
		}

		super._onConnectionOpened();

		let gameScreen = this._gameScreen;

		if (gameScreen.isReady)
		{
			this._trySendOpenRoomRequest();
		}
		else
		{
			gameScreen.on(GameScreen.EVENT_ON_READY, this._onGameScreenReady, this);
		}
	}

	_onConnectionClosed(event)
	{
		this._fOpenRoomSent_bln = false;
		this._fPendingOperationController_poc.off(GamePendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompletedForOpenRoom, this, true);
		console.log("Game -> _onConnectionClosed");
		super._onConnectionClosed(event);
	}

	_activateReconnectTimeout()
	{
		console.log("Game -> _activateReconnectTimeout");
		super._activateReconnectTimeout();
	}

	get recoveringConnectionInProgress ()
	{
		return this._reconnectingAfterGameUrlUpdatedRequired
				|| super.recoveringConnectionInProgress;
	}

	_startRecoveringSocketConnection()
	{
		if (this._fTournamentModeInfo_tmi.isTournamentOnServerCompletedState)
		{
			return;
		}

		if (APP.currentWindow && APP.currentWindow.gameFrbController && APP.currentWindow.gameFrbController.info.isFrbEndedAndConnectionLost)
		{
			// Do not request new start game url
			this._startReconnectingOnConnectionLost();
			return;
		}

		// reconnect to new start game url, requested when server connection lost
		this._reconnectingAfterGameUrlUpdatedRequired = true;

		let lRoomId_num = window.GET ? window.GET.roomId : -1;
		this.emit(GameWebSocketInteractionController.EVENT_ON_CONNECTION_RECOVERY_STARTED, {roomId: lRoomId_num});
	}

	_startReconnectingOnConnectionLost()
	{
		if (this._fTournamentModeInfo_tmi.isTournamentOnServerCompletedState)
		{
			return;
		}

		super._startReconnectingOnConnectionLost();
	}

	_establishConnection()
	{
		this._fIsReopenSocketRequired_bl = false;

		if (this._fTournamentModeInfo_tmi.isTournamentOnServerCompletedState)
		{
			this._closeConnectionIfPossible();
			return;
		}

		super._establishConnection();

	}

	_stopRecoveringSocketConnectionIfRequired()
	{
		if (this._reconnectingAfterGameUrlUpdatedRequired)
		{
			this._stopRecoveringSocketConnection();
		}
	}

	_stopRecoveringSocketConnection()
	{
		this._reconnectingAfterGameUrlUpdatedRequired = false;

		this._stopReconnecting();
	}

	_onGameScreenReady(event)
	{
		this._trySendOpenRoomRequest();
	}

	_onTimeToOpenRealRoomAfterBonus()
	{
		this._trySendOpenRoomRequest();
	}

	_specifyEventMessageType(messageData)
	{
		let eventType;
		switch(messageData.class)
		{
			case SERVER_MESSAGES.CANCEL_BATTLEGROUND_ROUND:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND;
				break;
			case SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_GET_ROOM_INFO_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.FULL_GAME_INFO:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_FULL_GAME_INFO_MESSAGE;
				break;
			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_SIT_IN_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.SIT_OUT_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_SIT_OUT_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.NEW_ENEMY:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_NEW_ENEMY_MESSAGE;
				break;
			case SERVER_MESSAGES.NEW_ENEMIES:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_NEW_ENEMIES_MESSAGE;
				break;
			case SERVER_MESSAGES.MISS:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_MISS_MESSAGE;
				break;
			case SERVER_MESSAGES.HIT:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_HIT_MESSAGE;
				break;
			case SERVER_MESSAGES.NEW_TREASURE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_NEW_TREASURE_MESSAGE;
				break;
			case SERVER_MESSAGES.CLIENTS_INFO:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_CLIENTS_INFO_MESSAGE;
				break;
			case SERVER_MESSAGES.ROUND_RESULT:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_ROUND_RESULT_MESSAGE;
				break;
			case SERVER_MESSAGES.GAME_STATE_CHANGED:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_GAME_STATE_CHANGED_MESSAGE;
				break;
			case SERVER_MESSAGES.BUY_IN_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_BUY_IN_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.RE_BUY_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE;
				break;
			case SERVER_MESSAGES.ENEMY_DESTROYED:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_ENEMY_DESTROYED_MESSAGE;
				break;
			case SERVER_MESSAGES.CHANGE_MAP:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_CHANGE_MAP_MESSAGE;
				break;
			case SERVER_MESSAGES.WEAPONS:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_WEAPONS_MESSAGE;
				break;
			case SERVER_MESSAGES.WEAPON_SWITCHED:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_WEAPON_SWITCHED_MESSAGE;
				break;
			case SERVER_MESSAGES.BALANCE_UPDATED:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE;
				break;
			case SERVER_MESSAGES.UPDATE_TRAJECTORIES:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_UPDATE_TRAJECTORIES_MESSAGE;
				break;
			case SERVER_MESSAGES.ROUND_FINISH_SOON:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_ROUND_FINISH_SOON;
				break;
			case SERVER_MESSAGES.BET_LEVEL_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_BET_LEVEL_CHANGED;
				break;
			case SERVER_MESSAGES.BONUS_STATUS_CHANGED:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_BONUS_STATUS_CHANGED_MESSAGE;
				break;
			case SERVER_MESSAGES.FRB_ENDED:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_FRB_ENDED_MESSAGE;
				break;
			case SERVER_MESSAGES.TOURNAMENT_STATE_CHANGED:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED;
				break;
			case SERVER_MESSAGES.BULLET_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_BULLET_RESPONSE;
				break;
			case SERVER_MESSAGES.BULLET_CLEAR_RESPONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_BULLET_CLEAR_RESPONSE;
				break;
			case SERVER_MESSAGES.BATTLEGROUND_SCORE_BOARD:
				eventType = GameWebSocketInteractionController.EVENT_BATTLEGROUND_SCORE_BOARD;
				break;
			case SERVER_MESSAGES.SERVER_BATTLEGROUND_BUY_IN_CONFIRMED_SEATS:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_BATTLEGROUND_BUY_IN_CONFIRMED_SEATS;
				break;
			case SERVER_MESSAGES.BATTLEGROUND_CAF_KICK_REPSONSE:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_BATTLEGROUND_CAF_KICK_RESPONSE;
				break;
			case SERVER_MESSAGES.LATENCY:
				eventType = GameWebSocketInteractionController.EVENT_ON_SERVER_LATENCY_REQUEST;
			default:
				eventType = super._specifyEventMessageType(messageData);
				break;
		}

		return eventType;
	}

	_isServerMessageReceivingAvailable(messageClass)
	{
		let unaffectedResponses = [	SERVER_MESSAGES.FULL_GAME_INFO,
									SERVER_MESSAGES.SIT_OUT_RESPONSE,
									SERVER_MESSAGES.WEAPONS,
									SERVER_MESSAGES.ERROR,
									SERVER_MESSAGES.OK,
									SERVER_MESSAGES.FRB_ENDED,
									SERVER_MESSAGES.TOURNAMENT_STATE_CHANGED,
									SERVER_MESSAGES.LATENCY
								];

		if (this._gameScreen.isPaused || this._gameScreen.restoreAfterLagsInProgress)
		{
			unaffectedResponses.push(SERVER_MESSAGES.BUY_IN_RESPONSE);
			unaffectedResponses.push(SERVER_MESSAGES.RE_BUY_RESPONSE);
			unaffectedResponses.push(SERVER_MESSAGES.ROUND_RESULT);
			unaffectedResponses.push(SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE);
			unaffectedResponses.push(SERVER_MESSAGES.SIT_IN_RESPONSE);
			unaffectedResponses.push(SERVER_MESSAGES.WEAPON_SWITCHED);
			unaffectedResponses.push(SERVER_MESSAGES.BET_LEVEL_RESPONSE);

			unaffectedResponses.push(SERVER_MESSAGES.BALANCE_UPDATED);
			unaffectedResponses.push(SERVER_MESSAGES.HIT);
			unaffectedResponses.push(SERVER_MESSAGES.MISS);
			unaffectedResponses.push(SERVER_MESSAGES.CANCEL_BATTLEGROUND_ROUND);
			unaffectedResponses.push(SERVER_MESSAGES.BATTLEGROUND_SCORE_BOARD);
			unaffectedResponses.push(SERVER_MESSAGES.GAME_STATE_CHANGED);
			unaffectedResponses.push(SERVER_MESSAGES.SERVER_BATTLEGROUND_BUY_IN_CONFIRMED_SEATS);
			unaffectedResponses.push(SERVER_MESSAGES.BATTLEGROUND_CAF_KICK_REPSONSE);
			unaffectedResponses.push(SERVER_MESSAGES.ROOM_WAS_OPENED);
		}

		return (!this._gameScreen.isPaused && !this._gameScreen.restoreAfterUnseasonableRequestInProgress && !this._gameScreen.restoreAfterLagsInProgress) || Boolean(~unaffectedResponses.indexOf(messageClass));
	}

	_handleFatalError(errorCode, requestData)
	{
		this._onBulletClear();

		super._handleFatalError(errorCode, requestData);
	}

	_onBattlegroundTimeTostartRequest()
	{
		this._sendRequest(CLIENT_MESSAGES.GET_FULL_GAME_INFO, {});
	}

	_onWeaponPaidMultiplierUpdateRequired(event)
	{
		this._sendRequest(CLIENT_MESSAGES.WEAPON_PAID_MULTIPLIER_UPDATE_REQUIRED, {roomId: event.roomId});
	}

	_handleGeneralError(errorCode, requestData)
	{
		let supported_codes = WebSocketInteractionController.ERROR_CODES;
		switch(errorCode)
		{
			case supported_codes.ROOM_WAS_DEACTIVATED:
				this._blockAfterCriticalError();
			case supported_codes.TOO_MANY_OBSERVERS:
			case supported_codes.TOO_MANY_PLAYER:
				if (APP.isCAFMode)
				{
					this._blockAfterCriticalError();
				}
			case supported_codes.ROOM_NOT_FOUND:
			case supported_codes.ROOM_NOT_OPEN:
			case supported_codes.ROOM_MOVED:
				this._stopServerMesagesHandling();
				this._closeConnectionIfPossible();
				break;
			case supported_codes.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE:
				if (APP.isBattlegroundMode && requestData && requestData.class === CLIENT_MESSAGES.OPEN_ROOM)
				{
					this._stopServerMesagesHandling();
					this._closeConnectionIfPossible();
				}
				break;
		}

		super._handleGeneralError(errorCode, requestData);
	}

	_onNoPlaceToSeatHandler(event)
	{
		this._stopServerMesagesHandling();
		this._closeConnectionIfPossible();
	}

	_onBattlegroundRoundObserverDenied(event)
	{
		this._stopServerMesagesHandling();
		this._closeConnectionIfPossible();
	}

	_trySendOpenRoomRequest()
	{
		//DEBUG error code...
		//setTimeout(this._processServerMessage.bind(this, {"code": 1012,"msg": "ROOM_NOT_OPEN","date": 1496748898812,"class": "Error","rid": 1}), 10);
		//...DEBUG error code

		if (APP.currentWindow && APP.currentWindow.gameFrbController && APP.currentWindow.gameFrbController.info.isFrbEndedAndConnectionLost)
		{
			APP.currentWindow.gameFrbController.info.isFrbEndedAndConnectionLost = false;
			this._fOpenRoomNotSent_bln = true;

			APP.gameScreen.gameField && APP.gameScreen.gameField.roundResultScreenController.tryToActivateScreen(true);
			APP.gameScreen.tryToProceedPostponedSitOut(true);
			APP.gameScreen.clearPendingRequestSitin();
			return;
		}

		this._fCheckLobbyConnection_bln = true;
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.CHECK_LOBBY_CONNECTION);
	}

	_sendOpenRoomRequest()
	{
		let lRoomId_num = APP.urlBasedParams.roomId;
		let lServerId_num = APP.urlBasedParams.serverId;
		this._onLobbyMessageReceived({type: LOBBY_MESSAGES.GAME_REFRESH_BALANCE_REQUIRED})

		this._fOpenRoomSent_bln = true;
		this._sendRequest(CLIENT_MESSAGES.OPEN_ROOM, {sid: APP.urlBasedParams.SID, serverId: lServerId_num, roomId: lRoomId_num, lang: I18.currentLocale});
	}

	_closeConnectionIfPossible()
	{
		super._closeConnectionIfPossible();
		this._fOpenRoomSent_bln = false;
	}

	_onGameSocketUrlUpdated(event)
	{
		let socketUrl = event.socketUrl;
		if (socketUrl === undefined)
		{
			return;
		}

		this.info.socketUrl = socketUrl;

		if (this._isConnectionOpened)
		{
			this._recoverAfterServerShutdownRequired = false;
			this._closeConnectionIfPossible();
			this._establishConnection();
		}
		else if (this._isConnectionClosed || this._isConnectionClosing)
		{
			if (this._reconnectingAfterGameUrlUpdatedRequired)
			{
				this._reconnectingAfterGameUrlUpdatedRequired = false;
				this._startReconnectingOnConnectionLost();
			}
			else if (!this._reconnectInProgress)
			{
				this._establishConnection();
			}
		}
	}

	_onFullGameInfoRequired(event)
	{
		if (this._isConnectionOpened)
		{
			this._sendRequest(CLIENT_MESSAGES.GET_FULL_GAME_INFO, {});
		}
		else if (this._isConnectionClosed)
		{
			if (this.recoveringConnectionInProgress || this._blockedAfterCriticalError)
			{
				// no actions required
			}
			else
			{
				this._establishConnection();
			}
		}
	}

	clearOpenRoomSent()
	{
		APP.currentWindow.gameFrbController.info.isFrbEndedAndConnectionLost = false;
		this._fOpenRoomNotSent_bln = false;
	}

	_onCloseRoomRequired(event)
	{
		if (!this.isCloseRoomRequestInProgress)
		{
			if (this._fOpenRoomNotSent_bln)
			{
				this._fOpenRoomNotSent_bln = false;
				this.onRoomClosed();
				APP.gameScreen.onRoomClosed();
				APP.currentWindow.gameFrbController.onRoomClosed();
				return;
			}
			this._sendRequest(CLIENT_MESSAGES.CLOSE_ROOM, {roomId: event.roomId});

		}
	}

	_onBattlegroundConfirmBuyInRequired()
	{
		this._sendRequest(CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN, {});
	}

	_onSitInRequired(event)
	{
		this._sendRequest(CLIENT_MESSAGES.SIT_IN, {stake:event.stake});
	}

	_onSitOutRequired(event)
	{
		if (!this.isSitoutRequestInProgress)
		{
			this._sendRequest(CLIENT_MESSAGES.SIT_OUT, {});
			APP.logger.i_pushDebug(`GWSIC. SitOut required. ${event}`);
		}
	}

	_onBattlegroundStartPrivateRoom(event)
	{
		this._sendRequest(CLIENT_MESSAGES.BATTLEGROUND_START_PRIVATE_ROOM, {});
	}

	_onBattlegroundPlayerKickTriggered(event)
	{
		this._sendRequest(CLIENT_MESSAGES.BATTLEGROUND_KICK, {nickname: event.nickname});
	}


	_onBattlegroundPlayerReinviteTriggered(event){
		this._sendRequest(CLIENT_MESSAGES.BATTLEGROUND_REINVITE, {nickname: event.nickname});
	}

	_onBattlegroundPlayerInviteTriggered(event){
		console.log("invite request " + event.nicknames);
		this._sendRequest(CLIENT_MESSAGES.BATTLEGROUND_INVITE, {nicknames: event.nicknames});
	}

	_onLatencyRequested(event){
		
		const requestBody = {
			serverTs: event.serverTs,
			serverAckTs: event.serverAckTs,
			clientTs: event.clientTs,
			clientAckTs: event.clientAckTs,
			step: event.step
		}
		this._sendRequest(CLIENT_MESSAGES.SEND_LATENCY, requestBody);
	}

	_onShotTriggered(event)
	{

		let sendData = {enemyId:event.enemyId, weaponId:event.weaponId, x: event.x, y: event.y, isPaidSpecialShot: event.isPaidSWShot, weaponPrice: event.weaponPrice};
		if (event.bulletId)
		{
			sendData.bulletId = event.bulletId;
			sendData.excludeParams = sendData.excludeParams || {};
			sendData.excludeParams.betLevel = APP.playerController.info.betLevel;
		}

		this._sendRequest(CLIENT_MESSAGES.SHOT, sendData);


		//DEBUG...
		// let msgWrongWeapon = {"code":1015,"msg":"Wrong weapon","date":1599034648281,"rid":40,"class":"Error"};
		// msgWrongWeapon.rid = this._requestUniqId;
		// setTimeout(() => this._processServerMessage(msgWrongWeapon), 1000);
		//...DEBUG
	}

	_onBullet(event)
	{
		this._sendRequest(CLIENT_MESSAGES.BULLET, {bulletTime: event.bulletTime, bulletAngle: event.bulletAngle,
			bulletId: event.bulletId, weaponId: event.weaponId,
			startPointX: event.startPointX, startPointY: event.startPointY, endPointX: event.endPointX, endPointY: event.endPointY});
	}

	_onBulletClear()
	{
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.BULLET);

		this._removeDelayedRicochetShots();

		this._sendRequest(CLIENT_MESSAGES.BULLET_CLEAR, {});
	}

	_removeDelayedRicochetShots()
	{
		let lDelayedRequests = this._delayedRequests;

		if (!lDelayedRequests || !lDelayedRequests.length)
		{
			return;
		}

		for (let i=0; i<lDelayedRequests.length; i++)
		{
			let lCurDelayedRequest = lDelayedRequests[i];
			let lRequestData_obj = lCurDelayedRequest.data;
			let lBulletId = lRequestData_obj.bulletId;
			let lWeaponId = lRequestData_obj.weaponId;
			let lBetLevel = lRequestData_obj.excludeParams ? lRequestData_obj.excludeParams.betLevel : lRequestData_obj.betLevel;

			if (lCurDelayedRequest.class == CLIENT_MESSAGES.SHOT && lBulletId !== undefined)
			{
				lCurDelayedRequest.timer && lCurDelayedRequest.timer.destructor();
				lDelayedRequests.splice(i, 1);
				i--;

				this.emit(GameWebSocketInteractionController.EVENT_ON_DELAYED_RICOCHET_SHOT_REMOVED, {weaponId: lWeaponId, betLevel: lBetLevel})
			}
		}
	}

	_onBuyInRequired(event)
	{
		if (this.isBuyInRequestInProgress)
		{
			return;
		}

		this._sendRequest(CLIENT_MESSAGES.BUY_IN, {});
	}

	_onReBuyRequired(event)
	{
		if (this.isRebuyRequestInProgress)
		{
			return;
		}

		this._sendRequest(CLIENT_MESSAGES.RE_BUY, {});
	}

	_onChangeStakeRequired(event)
	{
		this._sendRequest(CLIENT_MESSAGES.CHANGE_STAKE, {stake: event.stake});
	}

	_onWeaponUpdated(event)
	{
		if (
			APP.currentWindow.gameFrbController.info.frbEnded
			|| !APP.currentWindow.gameStateController.info.isPlayerSitIn
			)
		{
			return;
		}

		this._removeDelayedSwitchWeaponRequests(false);

		let requestData = {weaponId: event.weaponId};
		this._sendRequest(CLIENT_MESSAGES.SWITCH_WEAPON, requestData);
		this.emit(GameWebSocketInteractionController.EVENT_ON_REQUEST_WEAPON_UPDATE_SENDED, {weaponId: event.weaponId});
	}

	_onGameStateChanged(event)
	{
		let lNewState_str = event.value;

		if (
				APP.currentWindow.isPaused
				&& lNewState_str == ROUND_STATE.QUALIFY
				&& !APP.currentWindow.isKeepSWModeActive
			)
		{
			this._removeDelayedSwitchWeaponRequests(true);
		}
	}

	_removeDelayedSwitchWeaponRequests(aKeepDelayedDefaultWeapon_bl)
	{
		if (this._delayedRequests && this._delayedRequests.length)
		{
			for (let i=0; i<this._delayedRequests.length; i++)
			{
				let curDelayedRequestInfo = this._delayedRequests[i];
				if (curDelayedRequestInfo.class === CLIENT_MESSAGES.SWITCH_WEAPON)
				{
					let curDelayedWeaponId = curDelayedRequestInfo.data.weaponId;
					if (curDelayedWeaponId == WEAPONS.DEFAULT && aKeepDelayedDefaultWeapon_bl)
					{
						continue;
					}
					curDelayedRequestInfo.timer && curDelayedRequestInfo.timer.destructor();

					this._delayedRequests.splice(i, 1);
					i--;
				}
			}
		}
	}

	_onRoundResultScreenClosed(event)
	{
		if (!APP.currentWindow.gameStateController.info.isPlayerSitIn || APP.currentWindow.gameFrbController.info.frbEnded)
		{
			return;
		}

		this._sendRequest(CLIENT_MESSAGES.CLOSE_ROUND_RESULTS, {});
		this._onRefreshBalance();
	}

	_onRoundResultScreenOpened(event)
	{
		this._sendRequest(CLIENT_MESSAGES.GET_FULL_GAME_INFO, {});
	}

	_onGameBackToLobbyInitiated(event)
	{
		this._stopRecoveringSocketConnectionIfRequired();
		this._closeConnectionIfPossible();
	}

	_onAssetsLoadingError(event)
	{
		this._blockAfterCriticalError();
		this._stopServerMesagesHandling();
		this._closeConnectionIfPossible();
	}

	_onWebglContextLost(event)
	{
		this._onBulletClear();
		this._blockAfterCriticalError();
		this._stopServerMesagesHandling();
		this._closeConnectionIfPossible();
	}

	_onBetLevelChangeRequired(event)
	{
		this._sendRequest(CLIENT_MESSAGES.BET_LEVEL, {betLevel: event.multiplier});
	}

	_onTournamentModeClientStateChanged(event)
	{
		let lTournamentModeInfo_tmi = this._fTournamentModeInfo_tmi;
		if (lTournamentModeInfo_tmi.isTournamentOnClientCompletedState)
		{
			this._stopRecoveringSocketConnection();
			this._blockAfterCriticalError();
			this._stopServerMesagesHandling();
			this._closeConnectionIfPossible();
		}
	}

	_onLobbyMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case LOBBY_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (GameWebSocketInteractionController.isFatalError(event.data.errorType))
				{
					this._onBulletClear();
					this._blockAfterCriticalError();
					this._stopServerMesagesHandling();
					this._closeConnectionIfPossible();
				}
				else if (GameWebSocketInteractionController.isGeneralError(event.data.errorType))
				{
					this._handleLobbyGeneralError(event.data.errorCode);
				}
				break;
			case LOBBY_MESSAGES.GAME_REFRESH_BALANCE_REQUIRED:
				if (this._gameScreen.roundFinishSoon) return;
				this._onRefreshBalance();
				break;
			case LOBBY_MESSAGES.LOBBY_CONNECTION_STATE:
				this._fLobbyConnectionState_bln = event.data.state;

				if (this._fLobbyConnectionState_bln)
				{
					if (this._fCheckLobbyConnection_bln)
					{
						this._fCheckLobbyConnection_bln = false;
						this.emit(GameWebSocketInteractionController.EVENT_ON_CONNECTION_READY_TO_ROOM_OPEN);

						if (!this._fPendingOperationController_poc.info.isPendingOperationProgressStatusDefined || this._fPendingOperationController_poc.info.isPendingOperationInProgress)
						{
							this._fPendingOperationController_poc.once(GamePendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompletedForOpenRoom, this);
						}
						else
						{
							this._sendOpenRoomRequest();
						}
					}
				}
				else
				{
					if (this._reconnectInProgress)
					{
						this._deactivateReconnectTimeout();

						if (this._isConnectionClosed || this._isConnectionClosing)
						{
							this._reconnectingAfterGameUrlUpdatedRequired = false;
							this._startReconnectingOnConnectionLost();
						}
					}
				}
				break;
			case LOBBY_MESSAGES.LOBBY_LOADING_ERROR:
			case LOBBY_MESSAGES.WEBGL_CONTEXT_LOST:
				this._onBulletClear();
				this._blockAfterCriticalError();
				this._stopServerMesagesHandling();
				this._closeConnectionIfPossible();
				break;
			case LOBBY_MESSAGES.ENTER_LOBBY_MESSAGE_RECEIVED:
				if (this._fIsReopenSocketRequired_bl)
				{
					this._establishConnection();
				}
				break;
		}
	}

	_handleLobbyGeneralError(errorCode)
	{
		let supported_codes = GameWebSocketInteractionController.ERROR_CODES;
		switch(errorCode)
		{
			case supported_codes.ROOM_NOT_FOUND:
				this._recoverAfterServerShutdownRequired = false;
				this._reconnectingAfterGameUrlUpdatedRequired = false;
				break;
		}
	}

	_onRefreshBalance()
	{
		let msg = {};
		//add ammo params for debug...
		let playerInfo = APP.playerController.info;
		msg.clientAmmo = APP.currentWindow.weaponsController.info.ammo;

		let lPendingAmmo_num = playerInfo.pendingAmmo;
		let lRealAmmo_num = APP.currentWindow.weaponsController.info.realAmmo;
		msg.clientPendingAmmo = Number(lPendingAmmo_num.toFixed(2));

		let lResAmmo_num = lRealAmmo_num+lPendingAmmo_num;
		if (APP.currentWindow.gameFrbController.info.frbMode || APP.isBattlegroundGame)
		{
			msg.clientPendingAmmo = 0;
			lResAmmo_num = APP.currentWindow.weaponsController.info.realAmmo;
		}

		lResAmmo_num = Number(lResAmmo_num.toFixed(2));
		msg.clientResultAmmo = Math.floor(lResAmmo_num);
		//...add ammo params for debug
		this._sendRequest(CLIENT_MESSAGES.REFRESH_BALANCE, msg);
	}

	_onTickerResumed(event)
	{
		this._forceDelayedRequests();
	}

	destroy()
	{
		let gameScreen = this._gameScreen = APP.gameScreen;
		if (gameScreen)
		{
			gameScreen.off(GameScreen.EVENT_ON_GAME_SOCKET_URL_UPDATED, this._onGameSocketUrlUpdated, this);
			gameScreen.off(GameScreen.EVENT_ON_FULL_GAME_INFO_REQUIRED, this._onFullGameInfoRequired, this);
			gameScreen.off(GameScreen.EVENT_ON_CLOSE_ROOM_REQUIRED, this._onCloseRoomRequired, this);
			gameScreen.off(GameScreen.EVENT_BATTLEGROUND_CONFIRM_BUY_IN_REQUIRED, this._onBattlegroundConfirmBuyInRequired, this);
			gameScreen.off(GameScreen.EVENT_ON_SIT_IN_REQUIRED, this._onSitInRequired, this);
			gameScreen.off(GameScreen.EVENT_ON_SIT_OUT_REQUIRED, this._onSitOutRequired, this);
			gameScreen.off(GameScreen.EVENT_ON_SHOT_TRIGGERED, this._onShotTriggered, this);
			gameScreen.off(GameScreen.EVENT_ON_BUY_IN_REQUIRED, this._onBuyInRequired, this);
			gameScreen.off(GameScreen.EVENT_ON_RE_BUY_REQUIRED, this._onReBuyRequired, this);
			gameScreen.off(GameScreen.EVENT_ON_CHANGE_STAKE_REQUIRED, this._onChangeStakeRequired, this);
			gameScreen.off(GameScreen.EVENT_ON_WEAPON_UPDATED, this._onWeaponUpdated, this);
			gameScreen.off(GameScreen.EVENT_ON_ROUND_RESULT_SCREEN_DEACTIVATED, this._onRoundResultScreenClosed, this);
			gameScreen.off(GameScreen.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED, this._onRoundResultScreenOpened, this);
			gameScreen.off(GameScreen.EVENT_ON_READY, this._onGameScreenReady, this);
			gameScreen.off(GameScreen.EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS, this._onTimeToOpenRealRoomAfterBonus, this);
		}

		this._fLastWeapon_int = null;


		let externalCommunicator = APP.externalCommunicator;
		if (externalCommunicator)
		{
			externalCommunicator.off(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyMessageReceived, this);
		}

		super.destroy();
	}

	//PENDING_OPERATION...
	_onPendingOperationStarted(event)
	{
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.BUY_IN);
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.RE_BUY);
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.SIT_IN);
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN);
	}

	_onPendingOperationCompletedForOpenRoom(event)
	{
		this._sendOpenRoomRequest();	
	}

	_onRefreshPendingOperationStatusRequired(event)
	{
		let lParams_obj = {};
		lParams_obj.sid = APP.urlBasedParams.SID;
		
		this._sendRequest(CLIENT_MESSAGES.CHECK_PENDING_OPERATION_STATUS, lParams_obj);
	}

	_onPendingOperationStatusTrackingTurnedOff(event)
	{
		this._removeSpecificDelayedRequests(CLIENT_MESSAGES.CHECK_PENDING_OPERATION_STATUS);
	}
	//...PENDING_OPERATION
}

export default GameWebSocketInteractionController