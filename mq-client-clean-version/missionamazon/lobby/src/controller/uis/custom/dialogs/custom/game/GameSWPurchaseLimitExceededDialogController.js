import GameBaseDialogController from './GameBaseDialogController';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyExternalCommunicator from '../../../../../../external/LobbyExternalCommunicator';
import PseudoGameWebSocketInteractionController from '../../../../../interaction/server/PseudoGameWebSocketInteractionController';
import {GAME_MESSAGES, LOBBY_MESSAGES} from '../../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import {GAME_CLIENT_MESSAGES} from '../../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import LobbyAPP from '../../../../../../LobbyAPP';

class GameSWPurchaseLimitExceededDialogController extends GameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return GameBaseDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return GameBaseDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);
	}

	__init()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameSWPurchaseLimitExceededDialogView;
	}

	__initModelLevel()
	{
		super.__initModelLevel();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		if (APP.lobbyAppStarted)
		{
			this._startHandleEnvironmentMessages();
		}
		else
		{
			APP.once(LobbyAPP.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		}
	}

	_onLobbyStarted()
	{
		this._startHandleEnvironmentMessages();
	}

	_startHandleEnvironmentMessages()
	{
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		let pseudoGamewebSocketInteractionController = APP.pseudoGamewebSocketInteractionController;
		pseudoGamewebSocketInteractionController.on(PseudoGameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

	}

	_onFRBDialogActivated()
	{
		this.__deactivateDialog();
	}

	//VALIDATION...
	__validateModelLevel()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel()
	{
		let info = this.info;
		if (info.isActive)
		{
			this._configureDialogView();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		super.__deactivateDialog();
	}

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);
		this.__deactivateDialog();

		if (this.info.isGoBackToLobbyNeed)
		{
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_DEACTIVATED, {backToLobby: true});
			this.info.isGoBackToLobbyNeed = false;
		}
	}

	_configureDialogView()
	{
		let view = this.__fView_uo;
		let messageAssetId;

		//buttons configuration...
		view.setOkMode();
		view.okButton.setOKCaption();
		//...buttons configuration

		//message configuration...
		messageAssetId = "TADialogMessageSWPurchaseLimitExceeded";
		view.setMessage(messageAssetId);
		//...message configuration
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let errorCode = serverData.code;
		let lCurrentCode = Number(errorCode);
		let supported_codes = LobbyWebSocketInteractionController.ERROR_CODES;

		switch (lCurrentCode)
		{
			case supported_codes.SW_PURCHASE_LIMIT_EXCEEDED:
				this.__activateDialog();
				break;
		}
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		switch (msgType)
		{
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (LobbyWebSocketInteractionController.isGeneralError(event.data.errorType))
				{
					this._handleGameGeneralError(event.data.errorCode, event.data.requestClass);
				}
				break;
			case GAME_MESSAGES.SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_REQUIRED:
				this.__activateDialog();
				break;
			case GAME_MESSAGES.SERVER_CONNECTION_CLOSED:
			case GAME_MESSAGES.ROOM_CLOSED:
				this.__deactivateDialog();
				break;
		}
	}

	_handleGameGeneralError(errorCode, requestClass)
	{
		let supported_codes = LobbyWebSocketInteractionController.ERROR_CODES;
		let lCurrentCode = Number(errorCode);
		switch (lCurrentCode)
		{
			case supported_codes.SW_PURCHASE_LIMIT_EXCEEDED:
				if (
					APP.isBattlegroundGamePlayMode ||
					requestClass && requestClass == GAME_CLIENT_MESSAGES.BUY_IN
					)
				{
					this.info.isGoBackToLobbyNeed = true;
				}
				this.__activateDialog();
				break;
		}
	}

}

export default GameSWPurchaseLimitExceededDialogController