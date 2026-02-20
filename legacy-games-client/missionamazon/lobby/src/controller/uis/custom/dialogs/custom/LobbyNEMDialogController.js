import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyPlayerController from '../../../../custom/LobbyPlayerController';
import PlayerInfo from '../../../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import {CLIENT_MESSAGES} from '../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import LobbyStateController from '../../../../state/LobbyStateController';

class LobbyNEMDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._fTournamentModeInfo_tni = null;
		
		this._initLobbyNEMDialogController();
	}

	_initLobbyNEMDialogController()
	{
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().lobbyNEMDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();

		this._fTournamentModeInfo_tni = APP.tournamentModeController.info;
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.once(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ENTER_LOBBY_MESSAGE, this._onServerEnterLobbyMessage, this);

		// DEBUG...
		// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG
	}

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 107) // +
	// 	{
	// 		this.__activateDialog();
	// 	}
	// }
	//...DEBUG

	_onServerEnterLobbyMessage(event)
	{
		if (this._fTournamentModeInfo_tni.isTournamentMode)
		{
			this._startHandleEnvironmentMessages();
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
		var messageAssetId;

		if (info.isActive)
		{
			var info = this.info;
			var view = this.__fView_uo;
			var messageAssetId;

			//buttons configuration...
		
			view.setOkMode();
			view.okButton.setOKCaption();
			
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
		this.view.okButton.enabled = false; 
		this.view.okButton.alpha = 0.5; 
	}

	_startHandleEnvironmentMessages()
	{
		APP.playerController.on(LobbyPlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);
		webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);

		let lLobbyStateController_lsc = APP.lobbyStateController;
		lLobbyStateController_lsc.on(LobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyVisibilityChanged, this);
		
		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;
	}

	_checkEnoughMoneyForOneShot(aSpecBalance_num)
	{
		let lIsEnoughMoney_bl = false;
		if (!this._fPlayerInfo_pi)
		{
			return lIsEnoughMoney_bl;
		}

		let lBalance_num = aSpecBalance_num === undefined ? this._fPlayerInfo_pi.balance : aSpecBalance_num;
		let lRealAmmo_num = this._realAmmo;

		if (
				!this._fLobbyWeaponsPanelInfo_lwpi.isPaidWeaponSelected
				|| lBalance_num >= this._fPlayerInfo_pi.currentStake
				|| ~~(lRealAmmo_num) > 0
			)
		{
			lIsEnoughMoney_bl = true;
		}

		return lIsEnoughMoney_bl;
	}

	_checkEnoughMoneyForOneShotFromCurrentWeapon(aSpecBalance_num)
	{
		let lIsEnoughMoney_bl = false;
		if (!this._fPlayerInfo_pi)
		{
			return lIsEnoughMoney_bl;
		}

		if(
			APP.currentWindow &&
			APP.currentWindow.weaponsController &&
			APP.currentWindow.weaponsController.i_getInfo().isAnyFreeSpecialWeaponExist
			)
		{
			return true;
		}

		let lBalance_num = aSpecBalance_num === undefined ? this._fPlayerInfo_pi.balance : aSpecBalance_num;
		let lOneShotCost_num = this._fLobbyWeaponsPanelInfo_lwpi.i_getCurrentWeaponShotPrice();
		let ricochetBullets = this._fPlayerInfo_pi.ricochetBullets; // If > 0, don't show NEM dialog
		if ((this._fPlayerInfo_pi.betLevel > 1 || this._fLobbyWeaponsPanelInfo_lwpi.isPaidSpecialWeaponSelected) && this._checkEnoughMoneyForOneShot())
		{
			ricochetBullets = 0; // Ignore if betLevel > 1 or special weapon selected
		}

		if (!this._fLobbyWeaponsPanelInfo_lwpi.isPaidWeaponSelected
			||lBalance_num >= lOneShotCost_num || ricochetBullets)
		{
			lIsEnoughMoney_bl = true;
		}

		return lIsEnoughMoney_bl;
	}

	get _arrAllConditionsForActivationMet()
	{
		const lIsEnoughMoneyOneShotCurrentWeapon_bl =  this._checkEnoughMoneyForOneShotFromCurrentWeapon();
		const lIsEnoughMoneyOneShot_bl =  this._checkEnoughMoneyForOneShot();
		const lisBtg_bl = APP.isBattlegroundGamePlayMode;
		if(lIsEnoughMoneyOneShot_bl || lIsEnoughMoneyOneShotCurrentWeapon_bl)
		{
			console.warng("Game reported not enough money for one shot, but internal balance is enough!");
			return false;
		}
		if(lisBtg_bl)
		{
			console.warng("No balance detected in BTG game !");
			return false;
		}
		return true; 
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
			case LobbyWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY:
				if (requestClass == CLIENT_MESSAGES.RE_BUY)
				{
					if(this._arrAllConditionsForActivationMet)
					{
						this.__activateDialog();
					}
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

	_onLobbyServerConnectionOpened(event)
	{
		this.__deactivateDialog();
	}

	_onLobbyServerConnectionClosed(event)
	{
		this.__deactivateDialog();
	}
}

export default LobbyNEMDialogController