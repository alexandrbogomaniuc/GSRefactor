import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import PlayerController from '../../../../../../unified/controller/custom/PlayerController';
import PlayerInfo from '../../../../../../unified/model/custom/PlayerInfo';
import { GAME_CLIENT_MESSAGES } from '../../../../../../unified/model/interaction/server/WebSocketInteractionInfo';
import GUSLobbyStateController from '../../../../state/GUSLobbyStateController';
import Timer from "../../../../../../unified/controller/time/Timer";

class GULobbyRebuyDialogController extends GUDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return GUDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return GUDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._fTournamentModeInfo_tni = null;
		this._lobbyStateInfo = null;
		this._fWeaponsAlreadyRequested_bl = false;

		this._fShowDialogTimer_tmr = new Timer(this._activateIfPossible.bind(this), 300, true);
		this._fShowDialogTimer_tmr.pause();

		this._initLobbyRebuyDialogController();
	}

	_initLobbyRebuyDialogController()
	{
	}

	__init()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().lobbyRebuyDialogView;
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
		webSocketInteractionController.once(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_WEAPONS_MESSAGE, this._onServerWeaponsMessage, this);
	}

	_onServerEnterLobbyMessage()
	{
		if (this._fTournamentModeInfo_tni.isTournamentMode)
		{
			this._startHandleEnvironmentMessages();
		}
	}

	_onServerWeaponsMessage()
	{
		this._fWeaponsAlreadyRequested_bl = true;
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

		if (info.isActive)
		{
			let lHomeFuncNameDefined_bl = APP.appParamsInfo.homeFuncNameDefined;

			if (this._fTournamentModeInfo_tni.rebuyAllowed)
			{
				if (this._fTournamentModeInfo_tni.isRebuyLimitExceeded)
				{
					view.setMessage("TADialogMessageTournamentRebuyLimitExceeded");
					if (lHomeFuncNameDefined_bl)
					{
						view.setOkMode();
					}
					else
					{
						view.setEmptyMode();
					}
				}
				else
				{
					view.setOkCancelMode();

					let messageId = this._fTournamentModeInfo_tni.isFreerollMode ? "TADialogMessageFreerollTournamentRebuy" : "TADialogMessagePaidTournamentRebuy";

					let lRechargeValue_num = this._fTournamentModeInfo_tni.rebuyAmount;
					if (!this._fTournamentModeInfo_tni.resetBalanceAfterRebuy)
					{
						lRechargeValue_num += this._fPlayerInfo_pi.balance;
					}
					view.setMessage(messageId, this._fTournamentModeInfo_tni.rebuyPrice, lRechargeValue_num);

				}
			}
			else
			{
				view.setMessage("TADialogMessageTournamentRebuyNotAvailable");
				if (lHomeFuncNameDefined_bl)
				{
					view.setOkMode();
				}
				else
				{
					view.setEmptyMode();
				}
			}
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
		this._fShowDialogTimer_tmr.pause();
		this.__deactivateDialog();
	}

	__onDialogCancelButtonClicked(event)
	{
		super.__onDialogCancelButtonClicked(event);
		this._fShowDialogTimer_tmr.pause();
	}

	_startHandleEnvironmentMessages()
	{
		APP.playerController.on(PlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);

		let lLobbyStateController_lsc = APP.lobbyStateController;
		this._lobbyStateInfo = lLobbyStateController_lsc.info;
		lLobbyStateController_lsc.on(GUSLobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyVisibilityChanged, this);

		APP.dialogsController.lobbyNEMDialogController.on(GUDialogController.EVENT_REQUEST_CONFIRMED, this._onLobbyNEMDialogConfirmed, this);

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
				if (requestClass == GAME_CLIENT_MESSAGES.RE_BUY)
				{
					this.__deactivateDialog();
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
			this._fShowDialogTimer_tmr.start();
			this._activateIfPossible();
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

	_onLobbyNEMDialogConfirmed()
	{
		if (APP.buyInFuncDefined)
		{
			this._activateIfPossible();
		}
	}

	_activateIfPossible()
	{
		let lPossible_bl = this._isActivationPossible;
		if (lPossible_bl)
		{
			this.__activateDialog();
		}
	}

	get _isActivationPossible()
	{
		let curBalanceValue = this._fPlayerInfo_pi.balance;
		let lPossible_bl = this._lobbyStateInfo.lobbyScreenVisible
							&& (curBalanceValue < this._fPlayerInfo_pi.minRoomsStake)
							&& !APP.lobbyScreen.isRoomLasthand
							&&
							(
								this._fWeaponsAlreadyRequested_bl ||
								this._fTournamentModeInfo_tni.isTournamentMode
							);

		let ricochetBullets = this._fPlayerInfo_pi.ricochetBullets; // If > 0, don't show Rebuy dialog
		if (ricochetBullets)
		{
			lPossible_bl = false;
		}

		return lPossible_bl;
	}
}

export default GULobbyRebuyDialogController