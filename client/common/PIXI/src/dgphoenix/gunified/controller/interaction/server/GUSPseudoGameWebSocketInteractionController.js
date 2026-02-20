import WebSocketInteractionController from '../../../../unified/controller/interaction/server/WebSocketInteractionController';
import GUSPseudoGameWebSocketInteractionInfo, { SERVER_MESSAGES, CLIENT_MESSAGES } from '../../../model/interaction/server/GUSPseudoGameWebSocketInteractionInfo';
import GUDialogController from '../../uis/custom/dialogs/GUDialogController';
import { APP } from '../../../../unified/controller/main/globals';
import GUSLobbyTournamentModeController from '../../custom/tournament/GUSLobbyTournamentModeController';
import { ERROR_CODE_TYPES } from '../../../../unified/model/interaction/server/WebSocketInteractionInfo';
import GUSLobbyPendingOperationController from '../../gameplay/GUSLobbyPendingOperationController';

class GUSPseudoGameWebSocketInteractionController extends WebSocketInteractionController
{
	static get EVENT_ON_SERVER_MESSAGE()				{ return WebSocketInteractionController.EVENT_ON_SERVER_MESSAGE }
	static get EVENT_ON_SERVER_CONNECTION_CLOSED()		{ return WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED }
	static get EVENT_ON_SERVER_CONNECTION_OPENED()		{ return WebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED }
	static get EVENT_ON_CONNECTION_RECOVERY_STARTED()	{ return "EVENT_ON_CONNECTION_RECOVERY_STARTED" }
	static get EVENT_ON_CONNECTION_CLOSED()				{ return "EVENT_ON_CONNECTION_CLOSED" }
	static get EVENT_ON_COMPENSATION_REQUIRED()			{ return "EVENT_ON_COMPENSATION_REQUIRED" }

	setParams(aParams_obj)
	{
		this._pseudoGameParams = aParams_obj;
		if (!this.__fInitialized_bl)
		{
			this.__init();
		}
		else
		{
			if (this._isConnectionClosed)
			{
				this._establishConnection();
			}
		}
	}

	constructor(aOptInfo_si)
	{
		super(aOptInfo_si || new GUSPseudoGameWebSocketInteractionInfo());

		this._reconnectingAfterGameUrlUpdatedRequired = false;
		this._isRoomOpened_bl = false;
		this._fAlreadySitInNumber_int = -1;
	}

	get _debugWebSocketUrl()
	{
		return this._webSocketUrl;
	}

	get _webSocketUrl()
	{
		if (this._pseudoGameParams && this._pseudoGameParams.WEB_SOCKET_URL)
		{
			return this._pseudoGameParams.WEB_SOCKET_URL;
		}
		return APP.urlBasedParams.WEB_SOCKET_URL;
	}

	get _isSeater()
	{
		this._fAlreadySitInNumber_int >= 0;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let tournamentModeController = APP.tournamentModeController;
		tournamentModeController.on(GUSLobbyTournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);

		APP.pendingOperationController.on(GUSLobbyPendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompleted, this);
	}

	_onTournamentModeStateChanged()
	{
		let lTournamentModeInfo_tmi = APP.tournamentModeController.info;

		if (lTournamentModeInfo_tmi.isTournamentCompletedOrFailedState)
		{
			this._blockAfterCriticalError();
			this._stopServerMesagesHandling();
			this._closeConnectionIfPossible();
		}
	}

	_onPendingOperationCompleted(aEvent_e)
	{
		if (APP.lobbyScreen.resitOutInProcess && this._isConnectionOpened)
		{
			if (!this._isRoomOpened_bl)
			{
				if (this.hasUnRespondedRequest(CLIENT_MESSAGES.OPEN_ROOM) || this.hasDelayedRequests(CLIENT_MESSAGES.OPEN_ROOM))
				{
					// just wait for response
				}
				else
				{
					this._sendOpenRoom();
				}
			}
			else if (this._isSeater)
			{
				if (this.hasUnRespondedRequest(CLIENT_MESSAGES.SIT_OUT) || this.hasDelayedRequests(CLIENT_MESSAGES.SIT_OUT))
				{
					// just wait for response
				}
				else
				{
					this._sendSitOut();
				}
			}
			else
			{
				if (this.hasUnRespondedRequest(CLIENT_MESSAGES.CLOSE_ROOM) || this.hasDelayedRequests(CLIENT_MESSAGES.CLOSE_ROOM))
				{
					// just wait for response
				}
				else
				{
					this._sendCloseRoom();
				}
			}
		}
	}

	_handleServerMessage(messageData, requestData)
	{
		super._handleServerMessage(messageData, requestData);

		let msgClass = messageData.class;
		switch (msgClass)
		{
			case SERVER_MESSAGES.OK:
				let requestClass = undefined;
				if (requestData && requestData.rid >= 0)
				{
					requestClass = requestData.class;
					if (requestClass === CLIENT_MESSAGES.CLOSE_ROOM)
					{
						this._closeConnection();
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
					errorCode == WebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION
					&& APP.pendingOperationController.info.isPendingOperationHandlingSupported
				)
		{
			errorCodeSeverity = ERROR_CODE_TYPES.ERROR;
		}
		else
		{
			errorCodeSeverity = super._specifyErrorCodeSeverity(messageData, requestData);
		}

		return errorCodeSeverity;
	}

	_onConnectionOpened()
	{
		super._onConnectionOpened();

		if (!this._pseudoGameParams)
		{
			this._closeConnection();
			return;
		}

		this._sendOpenRoom();
	}

	_onConnectionClosed(event)
	{
		super._onConnectionClosed(event);

		this._isRoomOpened_bl = false;
		this._fAlreadySitInNumber_int = -1;
	}

	_activateReconnectTimeout()
	{
		super._activateReconnectTimeout();
	}

	get recoveringConnectionInProgress()
	{
		return this._reconnectingAfterGameUrlUpdatedRequired
			|| super.recoveringConnectionInProgress;
	}

	_startRecoveringSocketConnection()
	{
		let lTournamentModeInfo_tmi = APP.tournamentModeController.info;
		if (lTournamentModeInfo_tmi.isTournamentCompletedOrFailedState)
		{
			return;
		}

		// reconnect to new start game url, requested when server connection lost
		this._reconnectingAfterGameUrlUpdatedRequired = true;

		this.emit(GUSPseudoGameWebSocketInteractionController.EVENT_ON_CONNECTION_RECOVERY_STARTED);
	}

	_startReconnectingOnConnectionLost()
	{
		let lTournamentModeInfo_tmi = APP.tournamentModeController.info;
		if (lTournamentModeInfo_tmi.isTournamentCompletedOrFailedState)
		{
			return;
		}

		super._startReconnectingOnConnectionLost();
	}

	_specifyEventMessageType(messageData)
	{
		let eventType;
		switch (messageData.class)
		{
			case SERVER_MESSAGES.PENDING_OPERATION_STATUS:
				this._sendRequest(CLIENT_MESSAGES.OPEN_ROOM, { sid: this._pseudoGameParams.SID, serverId: this._pseudoGameParams.serverId, roomId: this._pseudoGameParams.roomId, lang: this._pseudoGameParams.lang });
				break;
			case SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE:
				this._isRoomOpened_bl = true;

				if (messageData.alreadySitInNumber >= 0)
				{
					this._fAlreadySitInNumber_int = messageData.alreadySitInNumber;
					this._sendSitOut();
				}
				else
				{
					this._fAlreadySitInNumber_int = -1;
					this._sendCloseRoom();
				}
				break;
			case SERVER_MESSAGES.SIT_OUT_RESPONSE:
				if (this._fAlreadySitInNumber_int !== messageData.id) // co-player's sitout
				{
					break;
				}

				this._fAlreadySitInNumber_int = -1;

				if (messageData.compensateSpecialWeapons > 0)
				{
					let midRoundCompensateSWExitDialogController = APP.dialogsController.midRoundCompensateSWExitDialogController;
					midRoundCompensateSWExitDialogController.once(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onMidRoundCompensateSWExitDialogRequestConfirmed, this);

					this.emit(GUSPseudoGameWebSocketInteractionController.EVENT_ON_COMPENSATION_REQUIRED, { data: { compensateSpecialWeapons: messageData.compensateSpecialWeapons, roomId: this._pseudoGameParams.roomId } });
				}
				else
				{
					this._sendCloseRoom();
				}
				break;
			default:
				eventType = super._specifyEventMessageType(messageData);
				break;
		}

		return eventType;
	}

	_sendOpenRoom()
	{
		this._sendRequest(CLIENT_MESSAGES.OPEN_ROOM, { sid: this._pseudoGameParams.SID, serverId: this._pseudoGameParams.serverId, roomId: this._pseudoGameParams.roomId, lang: this._pseudoGameParams.lang });
	}

	_sendSitOut()
	{
		this._sendRequest(CLIENT_MESSAGES.SIT_OUT, {});
	}

	_sendCloseRoom()
	{
		this._sendRequest(CLIENT_MESSAGES.CLOSE_ROOM, {roomId: this._pseudoGameParams.roomId});
	}

	_onMidRoundCompensateSWExitDialogRequestConfirmed()
	{
		this._sendCloseRoom();
	}

	_handleGeneralError(errorCode, requestData)
	{
		if (
				errorCode == WebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION
				&& APP.pendingOperationController.info.isPendingOperationHandlingSupported
			)
		{
			// should not close connection when pending operation starts
		}
		else
		{
			switch (errorCode)
			{
				case WebSocketInteractionController.ERROR_CODES.NOT_SEATER:
					this._sendCloseRoom();
					break;

				case WebSocketInteractionController.ERROR_CODES.NEED_SITOUT:
					if (requestData && requestData.rid >= 0 && requestData.class === CLIENT_MESSAGES.CLOSE_ROOM)
					{
						this._sendSitOut();
					}
				break;

				default:
					this._closeConnection();
					break;
			}
		}

		super._handleGeneralError(errorCode, requestData);
	}

	_closeConnection()
	{
		this._closeConnectionIfPossible();

		APP.tournamentModeController.off(GUSLobbyTournamentModeController.EVENT_ON_TOURNAMENT_STATE_CHANGED, this._onTournamentModeStateChanged, this);
		APP.pendingOperationController.off(GUSLobbyPendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompleted, this);

		this.emit(GUSPseudoGameWebSocketInteractionController.EVENT_ON_CONNECTION_CLOSED);
	}

	destroy()
	{
		super.destroy();
	}
}

export default GUSPseudoGameWebSocketInteractionController