import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GULobbyStateController from '../../../../state/GUSLobbyStateController';
import GUSLobbyExternalCommunicator from '../../../../external/GUSLobbyExternalCommunicator';
import { GAME_MESSAGES } from '../../../../external/GUSExternalCommunicator';
import GUSBattlegroundBuyInConfirmationDialogController from './GUSBattlegroundBuyInConfirmationDialogController';
import GUSLobbyPlayerController from '../../../../custom/GUSLobbyPlayerController';
import PlayerInfo from '../../../../../../unified/model/custom/PlayerInfo';

class GULobbyInsufficientFundsDialogController extends GUDialogController
{
	static get EVENT_DIALOG_PRESENTED () { return GUDialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);
	
		this._initLobbyInsufficientFundsDialogController();
	}

	_initLobbyInsufficientFundsDialogController()
	{
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().insufficientFundsDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.once(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		APP.dialogsController.battlegroundBuyInConfirmationDialogController.on(GUSBattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED, this._onBattlegroundBuyInConfirmationDialogActivationRequired, this);
	}

	_onServerEnterLobbyMessage(event)
	{
		this._startHandleEnvironmentMessages();
	}

	_onGameMessageReceived(event)
	{
		switch (event.type)
		{
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (event.data.errorCode == GUSLobbyWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS)
				{
					this.info.setOkMode();
					this.__activateDialog();
				}
				else if (GUSLobbyWebSocketInteractionController.isGeneralError(event.data.errorType))
				{
					this._handleGameGeneralError(event.data.errorCode, event.data.requestClass);
				}
				break;

		}
	}

	_handleGameGeneralError(errorCode, requestClass)
	{
		let supported_codes = GUSLobbyWebSocketInteractionController.ERROR_CODES;
		switch(errorCode)
		{
			case supported_codes.NOT_ENOUGH_MONEY:
			{
				if (APP.isBattlegroundRoomMode)
				{
					this.info.setCustomMode();
					this.__activateDialog();//show on battleground re-buy NEM
				}
			}
		}
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		var info = this.info;
		var view = this.__fView_uo;

		//buttons configuration...
		view.setOkMode();
		//...buttons configuration

		//message configuration...
		view.setMessage("TADialogMessageErrorInsufficientFunds");
		//...message configuration

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
	}

	__onDialogCustomButtonClicked(event)
	{
		super.__onDialogCustomButtonClicked(event);

		this.__deactivateDialog();
	}

	_startHandleEnvironmentMessages()
	{	
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);

		let lLobbyStateController_lsc = APP.lobbyStateController;
		lLobbyStateController_lsc.on(GULobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyVisibilityChanged, this);

		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;

		this._fPlayerController_pc.on(GUSLobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		switch (serverData.code) 
		{
			case GUSLobbyWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS:
					this.info.setOkMode();
					this.__activateDialog();
				break;
		}
	}

	_onBattlegroundBuyInConfirmationDialogActivationRequired()
	{
		this.info.setOkMode();
		this.__activateDialog();
	}

	_onPlayerInfoUpdated(event)
	{
		if (event.data[PlayerInfo.KEY_BALANCE])
		{
			let curBalanceValue = event.data[PlayerInfo.KEY_BALANCE].value;

			if (curBalanceValue >= APP.battlegroundController.info.getSelectedBuyInCost())
			{
				this.__deactivateDialog();
			}
		}
	}

	_onLobbyVisibilityChanged(event)
	{
		if (event.visible)
		{
		}
		else
		{
			this.__deactivateDialog();
		}
	}

	_onLobbyServerConnectionOpened(event)
	{
		this.__deactivateDialog();
	}

	_onLobbyServerConnectionClosed(event)
	{
		this.__deactivateDialog();
	}
}

export default GULobbyInsufficientFundsDialogController