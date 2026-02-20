import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import TournamentModeInfo from '../../../model/custom/tournament/TournamentModeInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyExternalCommunicator from '../../../external/LobbyExternalCommunicator';
import { LOBBY_MESSAGES, GAME_MESSAGES } from '../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import LobbyWebSocketInteractionController from '../../interaction/server/LobbyWebSocketInteractionController';
import { SERVER_MESSAGES } from '../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import DialogController from '../../uis/custom/dialogs/DialogController';
import {GAME_CLIENT_MESSAGES, CLIENT_MESSAGES} from '../../../model/interaction/server/LobbyWebSocketInteractionInfo';

class TournamentModeController extends SimpleController 
{

	static get EVENT_ON_TOURNAMENT_STATE_CHANGED() 		{ return 'EVENT_ON_TOURNAMENT_STATE_CHANGED'; }
	static get EVENT_ON_TOURNAMENT_ENTER_ROOM_REQUIRED()	{ return 'EVENT_ON_TOURNAMENT_ENTER_ROOM_REQUIRED';}
	
	constructor() 
	{
		super(new TournamentModeInfo());
	}

	__init()
	{
		super.__init();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED, this._onServerTournamentStateChangedMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE, this._onServerReBuyResponseMessage, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		// DEBUG...
		// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 107) // +
	// 	{
	// 		this.info.tournamentState = "FINISHED";
	// 		this.emit(TournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED);
	// 	}
	// }
	//...DEBUG

	destroy()
	{
		webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		webSocketInteractionController.off(LobbyWebSocketInteractionController.EVENT_ON_SERVER_TOURNAMENT_STATE_CHANGED, this._onServerTournamentStateChangedMessage, this);
		
		APP.externalCommunicator.off(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		super.destroy();
	}

	_onServerMessage(event)
	{
		let messageData = event.messageData;
		switch(messageData.class)
		{
			case SERVER_MESSAGES.ENTER_LOBBY_RESPONSE:
				// debug...
				// messageData.tournamentInfo = {
				// 	balance: 1000,
				// 	buyInAmount: 200,
				// 	buyInPrice: 10,
				// 	endDate: Date.now()+1000000,
				// 	id: 5447682,
				// 	name: "Test 1058",
				// 	reBuyAllowed: true,
				// 	reBuyAmount: 200,
				// 	reBuyCount: 0,
				// 	reBuyLimit: 10,
				// 	reBuyPrice: 10,
				// 	startDate: 1597224177049,
				// 	state: "STARTED"
				// }
				// ...debug
				let tournamentInfo = messageData.tournamentInfo;
				if (tournamentInfo)
				{
					let newTournamentState = tournamentInfo.state;

					this.info.lastStateUpdateTime = messageData.date;
					this.info.lastStateUpdateRID = messageData.rid;

					this.info.rebuyAllowed = tournamentInfo.reBuyAllowed;
					this.info.rebuyPrice = tournamentInfo.reBuyPrice;
					this.info.rebuyCount = tournamentInfo.reBuyCount;
					this.info.rebuyLimit = tournamentInfo.reBuyLimit;
					this.info.rebuyAmount = tournamentInfo.reBuyAmount;
					this.info.resetBalanceAfterRebuy = tournamentInfo.resetBalanceAfterRebuy;

					this._updateTournamentState(newTournamentState);
				}
			break;
		}
	}

	_onServerTournamentStateChangedMessage(event)
	{
		let commonPanelInfo = APP.commonPanelController.info;
		if (commonPanelInfo.gameUIVisible)
		{
			// TournamentStateChanged message will be handled via game socket
			return;
		}

		let newTournamentState = event.messageData.newState;
		this._updateTournamentState(newTournamentState);		
	}

	_onServerReBuyResponseMessage(event)
	{
		this.info.rebuyCount = event.messageData.reBuyCount;
	}

	_updateTournamentState(newTournamentState, aSendStateToGame_bl=true)
	{
		let prevTournamentState = this.info.tournamentState;
		if (prevTournamentState == newTournamentState)
		{
			return;
		}

		this.info.tournamentState = newTournamentState;
		
		this.emit(TournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED);

		if (aSendStateToGame_bl)
		{
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.TOURNAMENT_STATE_CHANGED, {tournamentState: this.info.tournamentState});
		}		
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.TOURNAMENT_STATE_CHANGED:
				let lCurGameTournamentState = event.data.tournamentState;
				this._updateTournamentState(lCurGameTournamentState, false);
				break;
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (LobbyWebSocketInteractionController.isGeneralError(event.data.errorType))
				{
					this._handleGameGeneralError(event.data.errorCode, event.data.requestClass);
				}
				break;
			case GAME_MESSAGES.TOURNAMENT_REBUY_COUNT_UPDATED:
				this.info.rebuyCount = event.data.rebuyCount;
				break;
		}
	}

	_handleGameGeneralError(errorCode, requestClass)
	{
		let supported_codes = LobbyWebSocketInteractionController.ERROR_CODES;
		switch(errorCode)
		{	
			case supported_codes.TOO_MANY_OBSERVERS:
				if (this.info.isTournamentMode)
				{
					this.emit(TournamentModeController.EVENT_ON_TOURNAMENT_ENTER_ROOM_REQUIRED);
				}
				break;
		}
	}
}

export default TournamentModeController