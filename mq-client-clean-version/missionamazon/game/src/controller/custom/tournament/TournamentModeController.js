import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import TournamentModeInfo from '../../../model/custom/tournament/TournamentModeInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameExternalCommunicator from '../../../external/GameExternalCommunicator';
import { LOBBY_MESSAGES, GAME_MESSAGES } from '../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSGameExternalCommunicator';
import GameWebSocketInteractionController from '../../interaction/server/GameWebSocketInteractionController';
import GameStateController from '../../state/GameStateController';

class TournamentModeController extends SimpleController 
{
	static get EVENT_ON_TOURNAMENT_SERVER_STATE_CHANGED() 		{ return 'EVENT_ON_TOURNAMENT_SERVER_STATE_CHANGED'; }
	static get EVENT_ON_TOURNAMENT_CLIENT_STATE_CHANGED() 		{ return 'EVENT_ON_TOURNAMENT_CLIENT_STATE_CHANGED'; }
	
	constructor() 
	{
		super(new TournamentModeInfo());
	}

	__init()
	{
		super.__init();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED, this._onServerTournamentStateChangedMessage, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_GET_ROOM_INFO_RESPONSE_MESSAGE, this._onServerGetRoomInfoResponseMessage, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_FULL_GAME_INFO_MESSAGE, this._onServerFullGameInfoMessage, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE, this._onServerReBuyResponseMessage, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyMessageReceived, this);

		APP.on("onGameStarted", this._onGameStarted, this);
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		if (APP.urlBasedParams.TOURNAMENT_MODE === "true")
		{
			this.info.isTournamentMode = true;
			let lCurLobbyTournamentState = APP.urlBasedParams.TOURNAMENT_STATE;
			this._updateTournamentServerState(lCurLobbyTournamentState, false);
			this._updateTournamentClientState(lCurLobbyTournamentState);
		}
	}

	destroy()
	{
		APP.externalCommunicator.off(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyMessageReceived, this);

		super.destroy();
	}

	_onGameStarted(event)
	{
		let lGameStateController_gsc = APP.gameScreen.gameStateController;
		lGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_ROUND_STATE_CHANGED, this._onGameRoundStateChanged, this);
		lGameStateController_gsc.on(GameStateController.EVENT_ON_PLAYER_SEAT_STATE_CHANGED, this._onGamePlayerStateChanged, this);
	}

	_onServerTournamentStateChangedMessage(event)
	{
		let newTournamentState = event.messageData.newState;
		this._updateTournamentServerState(newTournamentState);

		if (this.info.isTournamentOnServerCompletedState)
		{
			this._tryToCompleteTournamentOnClient();
		}
		else
		{
			this._updateTournamentClientState(newTournamentState);
		}
	}

	_onServerGetRoomInfoResponseMessage(event)
	{
		let tournamentData = event.messageData.tournamentInfo;
		if (!!tournamentData)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.TOURNAMENT_REBUY_COUNT_UPDATED, {rebuyCount: tournamentData.reBuyCount});

			this.info.resetBalanceAfterRebuy = tournamentData.resetBalanceAfterRebuy;

			let newTournamentState = tournamentData.state;
			this._updateTournamentServerState(newTournamentState);
			this._updateTournamentClientState(newTournamentState);
		}
	}

	_onServerFullGameInfoMessage(event)
	{
		let tournamentData = event.messageData.tournamentInfo;
		if (!!tournamentData)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.TOURNAMENT_REBUY_COUNT_UPDATED, {rebuyCount: tournamentData.reBuyCount});
		}
	}

	_onServerReBuyResponseMessage(event)
	{
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.TOURNAMENT_REBUY_COUNT_UPDATED, {rebuyCount: event.messageData.reBuyCount});
	}

	_updateTournamentServerState(newTournamentServerState, aSendStateToLobby_bl=true)
	{
		let prevTournamentServerState = this.info.tournamentServerState;
		if (prevTournamentServerState == newTournamentServerState)
		{
			return;
		}

		this.info.tournamentServerState = newTournamentServerState;
		
		this.emit(TournamentModeController.EVENT_ON_TOURNAMENT_SERVER_STATE_CHANGED);

		if (aSendStateToLobby_bl)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.TOURNAMENT_STATE_CHANGED, {tournamentState: this.info.tournamentServerState});
		}
	}

	_tryToCompleteTournamentOnClient()
	{
		if (
				this.info.isTournamentOnServerCompletedState
				&& !this._gameStateInfo.isGameInProgress
				&& !this._gameStateInfo.isPlayerSitIn
			)
		{
			this._updateTournamentClientState(this.info.tournamentServerState);
		}
	}

	get _gameStateInfo()
	{
		return this._fGameStateInfo_gsi || (this._fGameStateInfo_gsi = APP.currentWindow.gameStateController.info);
	}

	_updateTournamentClientState(newTournamentClientState)
	{
		let prevTournamentClientState = this.info.tournamentClientState;
		if (prevTournamentClientState == newTournamentClientState)
		{
			return;
		}

		this.info.tournamentClientState = newTournamentClientState;
		
		this.emit(TournamentModeController.EVENT_ON_TOURNAMENT_CLIENT_STATE_CHANGED);
	}

	_onLobbyMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case LOBBY_MESSAGES.TOURNAMENT_STATE_CHANGED:
				let lCurLobbyTournamentState = event.data.tournamentState;
				this._updateTournamentServerState(lCurLobbyTournamentState, false);
				this._updateTournamentClientState(lCurLobbyTournamentState);
				break;
		}
	}

	_onGameRoundStateChanged(event)
	{
		let lIsGameRoundInProgress_bln = event.value;

		if (!lIsGameRoundInProgress_bln)
		{
			this._tryToCompleteTournamentOnClient();
		}
	}

	_onGamePlayerStateChanged(event)
	{
		let lIsPlayerSitIn_bln = event.value;

		if (!lIsPlayerSitIn_bln)
		{
			this._tryToCompleteTournamentOnClient();
		}
	}
}

export default TournamentModeController