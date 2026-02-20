import GUGameBaseDialogController from './GUGameBaseDialogController';
import { APP } from '../../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GUPseudoGameWebSocketInteractionController from '../../../../../interaction/server/GUSPseudoGameWebSocketInteractionController';
import GUSLobbyExternalCommunicator from '../../../../../external/GUSLobbyExternalCommunicator';
import {GAME_MESSAGES, LOBBY_MESSAGES} from '../../../../../external/GUSExternalCommunicator';
import {GAME_CLIENT_MESSAGES} from '../../../../../../model/interaction/server/GUSLobbyWebSocketInteractionInfo';
import GUSLobbyApplication from '../../../../../main/GUSLobbyApplication';

class GUGameSWPurchaseLimitExceededDialogController extends GUGameBaseDialogController
{
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
			APP.once(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		}
	}

	_onLobbyStarted()
	{
		this._startHandleEnvironmentMessages();
	}

	_startHandleEnvironmentMessages()
	{
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		let pseudoGamewebSocketInteractionController = APP.pseudoGamewebSocketInteractionController;
		pseudoGamewebSocketInteractionController.on(GUPseudoGameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
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

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		switch (msgType)
		{
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (GUSLobbyWebSocketInteractionController.isGeneralError(event.data.errorType))
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

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let errorCode = serverData.code;
		let lCurrentCode = Number(errorCode);
		let supported_codes = GUSLobbyWebSocketInteractionController.ERROR_CODES;

		switch (lCurrentCode)
		{
			case supported_codes.SW_PURCHASE_LIMIT_EXCEEDED:
				this.__activateDialog();
				break;
		}
	}

	_handleGameGeneralError(errorCode, requestClass)
	{
		let supported_codes = GUSLobbyWebSocketInteractionController.ERROR_CODES;
		let lCurrentCode = Number(errorCode);
		switch (lCurrentCode)
		{
			case supported_codes.SW_PURCHASE_LIMIT_EXCEEDED:
				if (
						APP.isBattlegroundRoomMode
						|| (requestClass && requestClass == GAME_CLIENT_MESSAGES.BUY_IN)
					)
				{
					this.info.isGoBackToLobbyNeed = true;
				}
				this.__activateDialog();
				break;
		}
	}

}

export default GUGameSWPurchaseLimitExceededDialogController