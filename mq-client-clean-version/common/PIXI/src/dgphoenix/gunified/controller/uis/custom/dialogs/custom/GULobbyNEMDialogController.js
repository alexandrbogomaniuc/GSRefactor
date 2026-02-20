import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import PlayerController from '../../../../../../unified/controller/custom/PlayerController';
import PlayerInfo from '../../../../../../unified/model/custom/PlayerInfo';
import { CLIENT_MESSAGES } from '../../../../../model/interaction/server/GUSLobbyWebSocketInteractionInfo';
import LobbyStateController from '../../../../state/GUSLobbyStateController';

class GULobbyNEMDialogController extends GUDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return GUDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._fTournamentModeInfo_tni = null;

		this._initLobbyNEMDialogController();
	}

	_initLobbyNEMDialogController()
	{
	}

	__init()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().lobbyNEMDialogView;
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		this._fTournamentModeInfo_tni = APP.tournamentModeController.info;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.once(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);
	}

	_onServerEnterLobbyMessage()
	{
		if (this._fTournamentModeInfo_tni.isTournamentMode)
		{
			this._startHandleEnvironmentMessages();
		}
	}

	//VALIDATION...
	__validateModelLevel()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel()
	{
		let info = this.info;
		let view = this.__fView_uo;
		let messageAssetId;

		if (info.isActive)
		{
			//buttons configuration...
			if (APP.buyInFuncDefined)
			{
				view.setOkCancelMode();
				view.okButton.setBuyInCaption();
			}
			else
			{
				view.setOkMode();
				view.okButton.setOKCaption();
			}
			//...buttons configuration

			//message configuration...
			if (APP.buyInFuncDefined)
			{
				messageAssetId = "TADialogMessageCashier";
			}
			else
			{
				messageAssetId = "TADialogMessageLowBalanceForReal";
			}

			view.setMessage(messageAssetId);
			//...message configuration
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
	}

	_startHandleEnvironmentMessages()
	{
		APP.playerController.on(PlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);

		let lLobbyStateController_lsc = APP.lobbyStateController;
		lLobbyStateController_lsc.on(LobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyVisibilityChanged, this);

		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let requestClass = undefined;
		if (requestData && requestData.rid >= 0)
		{
			requestClass = requestData.class;
		}

		switch (serverData.code) 
		{
			case GUSLobbyWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY:
				if (requestClass == CLIENT_MESSAGES.RE_BUY)
				{
					this.__activateDialog();
				}
				break;
		}
	}

	_onPlayerInfoUpdated(event)
	{
		if (event.data[PlayerInfo.KEY_BALANCE])
		{
			let curBalanceValue = event.data[PlayerInfo.KEY_BALANCE].value;
			if (curBalanceValue >= this._fPlayerInfo_pi.minRoomsStake)
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

	_onLobbyServerConnectionOpened()
	{
		this.__deactivateDialog();
	}

	_onLobbyServerConnectionClosed()
	{
		this.__deactivateDialog();
	}
}

export default GULobbyNEMDialogController