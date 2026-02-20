import GUGameBaseDialogController from './GUGameBaseDialogController';
import { APP } from '../../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GUSLobbyExternalCommunicator from '../../../../../external/GUSLobbyExternalCommunicator';
import { GAME_MESSAGES } from '../../../../../external/GUSExternalCommunicator';
import GUSLobbyApplication from '../../../../../main/GUSLobbyApplication';
import GULobbyBonusController from '../../../bonus/GUSLobbyBonusController';
import GUFRBDialogController from '../GUFRBDialogController'
import { GAME_CLIENT_MESSAGES } from '../../../../../../model/interaction/server/GUSLobbyWebSocketInteractionInfo';
import { WEAPONS } from '../../../../../../model/weapons/GUSWeaponsInfo';

class GUGameNEMDialogController extends GUGameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED()			{ return GUGameBaseDialogController.EVENT_DIALOG_PRESENTED }
	static get EVENT_PRESENTED_DIALOG_UPDATED()	{ return GUGameBaseDialogController.EVENT_PRESENTED_DIALOG_UPDATED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._lootTypeReason = false;
		this._fBonusController_bc = null;
		this._fTournamentModeInfo_tni = null;

		this._initGameNEMDialogController();
	}

	_initGameNEMDialogController()
	{
	}

	__init()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameNEMDialogView;
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		let modeValue = APP.urlBasedParams.MODE || APP.urlBasedParams.mode || "free";
		this.info.isRealMoneyMode = modeValue.toLowerCase() === "real";

		this._fTournamentModeInfo_tni = APP.tournamentModeController.info;
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
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);

		this._fBonusController_bc = APP.lobbyBonusController;
		this._fBonusController_bc.on(GULobbyBonusController.EVENT_ON_BONUS_STATE_CHANGED, this._onBonusStatusChanged, this);
		this.info.isBonusMode = this._fBonusController_bc.info.isActivated;

		APP.dialogsController.FRBDialogController.on(GUFRBDialogController.EVENT_DIALOG_ACTIVATED, this._onFRBDialogActivated, this);

		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;

		this._fLobbyWeaponsPanelInfo_lwpi = APP.commonPanelController.lobbyWeaponsPanelController.info;
	}

	_onFRBDialogActivated()
	{
		this.__deactivateDialog();
	}

	//VALIDATION...
	__validateModelLevel()
	{
		super.__validateModelLevel();

		let lInfo = this.info;

		lInfo.isBonusModeNEMForRoom = false;
		if (lInfo.isBonusMode && !this._checkEnoughMoneyForOneShot())
		{
			let lBalance_num = this._fPlayerInfo_pi.balance;
			let lUnpresentedWin_num = this._fPlayerInfo_pi.unpresentedWin || 0;
			let lRealAmmo_num = this._realAmmo;
			let lRealAmmoCost_num = lRealAmmo_num === undefined ? 0 : lRealAmmo_num * this._fPlayerInfo_pi.currentStake;

			lBalance_num += lUnpresentedWin_num + lRealAmmoCost_num;

			lInfo.isBonusModeNEMForRoom = (lBalance_num >= this._fPlayerInfo_pi.minRoomsStake)
		}
	}

	__validateViewLevel()
	{
		let info = this.info;
		if (info.isActive)
		{
			if (info.isBonusMode)
			{
				this._configureBonusModeDialogView();
			}
			else if (info.isFreeMoneyMode)
			{
				this._configureFreeModeDialogView();
			}
			else //REAL MONEY MODE
			{
				this._configureRealModeDialogView();
			}
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		this._stopBalanceRefreshingHandling();

		this._lootTypeReason = false;

		super.__deactivateDialog();
	}

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		if (
			this._fTournamentModeInfo_tni.isTournamentMode
			|| !this._isCashierCallNeeded
			|| !APP.buyInFuncDefined
		)
		{
			this.__deactivateDialog();
		}
	}

	__onDialogCustomButtonClicked(event)
	{
		this._startBalanceRefreshingHandling();

		super.__onDialogCustomButtonClicked(event);
	}

	get _isCashierCallNeeded()
	{
		//don't need to call Cashier in Bonus Mode
		return !this.info.isBonusMode;
	}

	//BONUS MODE...
	_onBonusStatusChanged()
	{
		this.info.isBonusMode = this._fBonusController_bc.info.isActivated;
	}

	_configureBonusModeDialogView()
	{
		let info = this.info;
		let view = this.__fView_uo;
		let messageAssetId;

		//buttons configuration...
		view.setOkMode();
		view.okButton.setOKCaption();
		//...buttons configuration

		//message configuration...
		messageAssetId = info.isBonusModeNEMForRoom ? "TADialogMessageTournamentLowBalanceForCurrentRoom" : "TADialogMessageLowBonusBalance";

		if (this._fPlayerInfo_pi.betLevel > 1 && this._checkEnoughMoneyForOneShot())
		{
			messageAssetId = "TADialogMessageLowBonusBalanceBetLevel";
		}
		if (this._fLobbyWeaponsPanelInfo_lwpi.isPaidSpecialWeaponSelected && this._checkEnoughMoneyForOneShot())
		{
			messageAssetId = "TADialogMessageLowBonusBalancePayWeapon";
		}

		view.setMessage(messageAssetId);
		//...message configuration

	}
	//...BONUS MODE

	_configureFreeModeDialogView()
	{
		let view = this.__fView_uo;
		let messageAssetId;

		//buttons configuration...
		view.setOkMode();
		view.okButton.setOKCaption();
		//...buttons configuration

		//message configuration...
		messageAssetId = "TADialogMessageLowBalanceForFree";

		if (this._fPlayerInfo_pi.betLevel > 1 && this._checkEnoughMoneyForOneShot())
		{
			messageAssetId = "TADialogMessageLowBalanceForFreeBetLewel";
		}

		if (this._fLobbyWeaponsPanelInfo_lwpi.isPaidSpecialWeaponSelected && this._checkEnoughMoneyForOneShot())
		{
			messageAssetId = "TADialogMessageLowBalanceForFreePayWeapon";
		}

		view.setMessage(messageAssetId);
		//...message configuration
	}

	_configureRealModeDialogView()
	{
		let view = this.__fView_uo;
		let messageAssetId;

		//buttons configuration...
		if (APP.buyInFuncDefined)
		{
			if (APP.playerController.info.refreshBalanceAvailable && !this._fTournamentModeInfo_tni.isTournamentMode)
			{
				view.setOkCancelCustomMode();
				view.okButton.setBuyInCaption();
				view.customButton.setRefreshCaption();
			}
			else
			{
				view.setOkCancelMode();
				view.okButton.setBuyInCaption();
			}
		}
		else
		{
			if (APP.playerController.info.refreshBalanceAvailable && !this._fTournamentModeInfo_tni.isTournamentMode)
			{
				view.setOkCustomMode();
				view.okButton.setOKCaption();
				view.customButton.setRefreshCaption();
			}
			else
			{
				view.setOkMode();
				view.okButton.setOKCaption();
			}
		}
		//...buttons configuration

		//message configuration...
		if (APP.buyInFuncDefined)
		{
			messageAssetId = "TADialogMessageCashier";
			if (this._fPlayerInfo_pi.betLevel > 1 && this._checkEnoughMoneyForOneShot())
			{
				messageAssetId = "TADialogMessageCashierBetLevel";
			}

			if (this._fLobbyWeaponsPanelInfo_lwpi.isPaidSpecialWeaponSelected && this._checkEnoughMoneyForOneShot())
			{
				messageAssetId = "TADialogMessageCashierPayWeapon";
			}
		}
		else
		{
			messageAssetId = "TADialogMessageLowBalanceForReal";
			if (this._fPlayerInfo_pi.betLevel > 1 && this._checkEnoughMoneyForOneShot())
			{
				messageAssetId = "TADialogMessageLowBalanceForRealBetLevel";
			}

			if (this._fLobbyWeaponsPanelInfo_lwpi.isPaidSpecialWeaponSelected && this._checkEnoughMoneyForOneShot())
			{
				messageAssetId = "TADialogMessageLowBalanceForRealPayWeapon";
			}
		}

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
			case GAME_MESSAGES.NOT_ENOUGH_MONEY_DIALOG_REQUIRED:
				if (!this._fTournamentModeInfo_tni.isTournamentMode)/* in tournament mode NEM dialog should be activated only if we don't have enough real money for rebuy (error code NOT_ENOUGH_MONEY) */
				{
					this._lootTypeReason = !!(event.data.dialogType === "lootCrate");
					this.__activateDialog();
				}
				break;
			case GAME_MESSAGES.SERVER_BALANCE_UPDATED_MESSAGE_RECIEVED:
				if (this._lootTypeReason) return;
				this._balanceRefreshingHandler(event.data);
				break;
			case GAME_MESSAGES.SERVER_CONNECTION_OPENED:
			case GAME_MESSAGES.SERVER_CONNECTION_CLOSED:
			case GAME_MESSAGES.ROOM_CLOSED:
			case GAME_MESSAGES.ROUND_RESULT_ACTIVATED:
				this.__deactivateDialog();
				break;
		}
	}

	_balanceRefreshingHandler(data)
	{
		this._stopBalanceRefreshingHandling();

		if (this._checkEnoughMoneyForOneShotFromCurrentWeapon(data.specBalance) && !data.keepDialog)
		{
			this.__deactivateDialog();
		}
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

	get _areAllConditionsForActivationMet()
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


	_checkEnoughMoneyForOneShotFromCurrentWeapon(aSpecBalance_num)
	{
		let lIsEnoughMoney_bl = false;
		if (!this._fPlayerInfo_pi)
		{
			return lIsEnoughMoney_bl;
		}

		let lBalance_num = aSpecBalance_num === undefined ? this._fPlayerInfo_pi.balance : aSpecBalance_num;
		let lOneShotCost_num = this._fLobbyWeaponsPanelInfo_lwpi.i_getCurrentWeaponShotPrice();
		let ricochetBullets = this._fPlayerInfo_pi.ricochetBullets; // If > 0, don't show NEM dialog
		if ((this._fPlayerInfo_pi.betLevel > 1 || this._fLobbyWeaponsPanelInfo_lwpi.isPaidSpecialWeaponSelected) && this._checkEnoughMoneyForOneShot())
		{
			ricochetBullets = 0; // Ignore if betLevel > 1 or special weapon selected
		}

		if (!this._fLobbyWeaponsPanelInfo_lwpi.isPaidWeaponSelected
			|| lBalance_num >= lOneShotCost_num || ricochetBullets)
		{
			lIsEnoughMoney_bl = true;
		}

		return lIsEnoughMoney_bl;
	}


	_handleGameGeneralError(errorCode, requestClass)
	{
		let supported_codes = GUSLobbyWebSocketInteractionController.ERROR_CODES;
		switch (errorCode)
		{
			case supported_codes.NOT_ENOUGH_MONEY:
				if (this._fTournamentModeInfo_tni.isTournamentMode)
				{
					/*
						in tournament mode we should activate NEM dialog only if NOT_ENOUGH_MONEY error code caused by GAME_CLIENT_MESSAGES.RE_BUY
					*/
					if (requestClass == GAME_CLIENT_MESSAGES.RE_BUY)
					{
						if(this._areAllConditionsForActivationMet)
						{
							this.__activateDialog();
						}
					}
				}
				else if (APP.isBattlegroundRoomMode)
				{
					//PREVENT DEFAULT NEM DIALOG ON BATTLEGROUND RE-BUY...
					if (requestClass !== GAME_CLIENT_MESSAGES.RE_BUY)
					{
						if(this._areAllConditionsForActivationMet)
						{
							this.__activateDialog();
						}
					}
					//...PREVENT DEFAULT NEM DIALOG ON BATTLEGROUND RE-BUY
				}
				else
				{
					if(this._areAllConditionsForActivationMet)
					{
						this.__activateDialog();
					}
				}
				break;
		}
	}

	_onLobbyServerBalanceUpdated()
	{
		this._stopBalanceRefreshingHandling();
	}

	_startBalanceRefreshingHandling()
	{
		this._lootTypeReason = false;

		let view = this.__fView_uo;
		view && view.startBalanceRefreshingHandling();
	}

	_stopBalanceRefreshingHandling()
	{
		let view = this.__fView_uo;
		view && view.stopBalanceRefreshingHandling();
	}

	get _realAmmo()
	{
		let lRealAmmo_num = this._fPlayerInfo_pi.realAmmo;
		if (isNaN(lRealAmmo_num))
		{
			return 0;
		}

		return Number(lRealAmmo_num.toFixed(2));
	}

}

export default GUGameNEMDialogController