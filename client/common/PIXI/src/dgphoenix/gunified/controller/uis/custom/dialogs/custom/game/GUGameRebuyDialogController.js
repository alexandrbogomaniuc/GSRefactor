import GUGameBaseDialogController from './GUGameBaseDialogController';
import { APP } from '../../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GUSLobbyExternalCommunicator from '../../../../../external/GUSLobbyExternalCommunicator';
import { GAME_MESSAGES } from '../../../../../external/GUSExternalCommunicator';
import GUSLobbyApplication from '../../../../../main/GUSLobbyApplication';
import { GAME_CLIENT_MESSAGES } from '../../../../../../model/interaction/server/GUSLobbyWebSocketInteractionInfo';
import PlayerController from '../../../../../../../unified/controller/custom/PlayerController';
import PlayerInfo from '../../../../../../../unified/model/custom/PlayerInfo';

class GUGameRebuyDialogController extends GUGameBaseDialogController
{
	static get EVENT_ON_REBUY_DENIED_WITH_NON_SPENT_BALANCE ()	{ return "EVENT_ON_REBUY_DENIED_WITH_NON_SPENT_BALANCE" };
	static get EVENT_ON_REBUY_DIALOG_EXIT_CLICKED () 			{ return "EVENT_ON_REBUY_DIALOG_EXIT_CLICKED" };

	get keepDlg()
	{
		return this._fKeepDialog_bln;
	}

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._lootTypeReason = false;
		this._fTournamentModeInfo_tni = null;
		this._fKeepDialog_bln = false;
	}

	__init()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameRebuyDialogView;
	}

	__initModelLevel()
	{
		super.__initModelLevel();

		this._fTournamentModeInfo_tni = APP.tournamentModeController.info;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._fPlayerInfo_pi = APP.playerController.info;
		this._fLobbyWeaponsPanelInfo_lwpi = APP.commonPanelController.lobbyWeaponsPanelController.info;

		if (APP.lobbyAppStarted)
		{
			this._startHandleEnvironmentMessages();
		}
		else
		{
			APP.once(GUSLobbyApplication.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		}
	}

	_onLobbyStarted(event)
	{
		this._startHandleEnvironmentMessages();
	}

	_startHandleEnvironmentMessages()
	{
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;

		APP.on(GUSLobbyApplication.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		this._fPlayerController_pc.on(PlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
	}

	//VALIDATION...
	__validateModelLevel()
	{
		this._fKeepDialog_bln = this._checkEnoughMoneyForOneShot() && (this._fLobbyWeaponsPanelInfo_lwpi.isPaidSpecialWeaponSelected || this._fPlayerInfo_pi.betLevel > 1);

		super.__validateModelLevel();
	}

	__validateViewLevel()
	{
		var info = this.info;
		var view = this.__fView_uo;

		if (info.isActive)
		{
			let lHomeFuncNameDefined_bl = APP.appParamsInfo.homeFuncNameDefined;

			if (
					APP.isBattlegroundGame 
					&& APP.battlegroundController.info.isBattlegroundGameStarted
				)
			{
				view.setMessage("TADialogMessageTournamentRebuyNotAvailable");
				view.setOkCustomMode();
				view.okButton.setRetryCaption();
				view.customButton.setExitCaption();
			}
			else if (
						this._fLobbyWeaponsPanelInfo_lwpi.isPaidSpecialWeaponSelected
						&& this._checkEnoughMoneyForOneShot()
					)
			{
				view.setMessage("TADialogMessageTournamentPayWeapon");
				view.setOkMode();
			}
			else if (
						this._fPlayerInfo_pi.betLevel > 1
						&& this._checkEnoughMoneyForOneShot()
					)
			{
				view.setMessage("TADialogMessageTournamentBetLevel");
				view.setOkMode();
			}
			else if (this._fTournamentModeInfo_tni.rebuyAllowed)
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
						lRechargeValue_num += this._calcTotalBalance();
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
		this._lootTypeReason = false;
		this._fKeepDialog_bln = false;

		super.__deactivateDialog();
	}

	__onDialogOkButtonClicked(event)
	{
		if (
				APP.isBattlegroundGame 
				&& APP.battlegroundController.info.isBattlegroundGameStarted
			)
		{
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_NEED_RETRY_REBUY);
			return;
		}

		//dialog pretends to be NEM if minimal stake is affordable  https://jira.dgphoenix.com/browse/MQRR-159
		//rebuy action is prevented in this case  https://jira.dgphoenix.com/browse/MQRR-197
		if (!this._checkEnoughMoneyForOneShot())
		{
			super.__onDialogOkButtonClicked(event);
		}

		this._fKeepDialog_bln = false;

		this.__deactivateDialog();
	}

	__onDialogCustomButtonClicked(event)
	{	
		if (
				APP.isBattlegroundGame 
				&& APP.battlegroundController.info.isBattlegroundGameStarted
			)
		{
			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BACK_TO_LOBBY);
			this.emit(GUGameRebuyDialogController.EVENT_ON_REBUY_DIALOG_EXIT_CLICKED);
			this.__deactivateDialog();
		}
		else 
		{
			super.__onDialogCustomButtonClicked(event);
		}
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (
						APP.isBattlegroundGame
						&& event.data.requestClass == GAME_CLIENT_MESSAGES.RE_BUY
						&& event.data.errorCode == GUSLobbyWebSocketInteractionController.ERROR_CODES.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE
					)
				{
					if (!APP.battlegroundController.info.isBattlegroundGameStarted)
					{
						this.__activateDialog();
					}
				}
				else if (GUSLobbyWebSocketInteractionController.isGeneralError(event.data.errorType))
				{
					this._handleGameGeneralError(event.data.errorCode, event.data.requestClass);
				}
				break;

			case GAME_MESSAGES.NOT_ENOUGH_MONEY_DIALOG_REQUIRED:
				if (this._fTournamentModeInfo_tni.isTournamentMode)
				{
					let specBalance = event.data.specBalance;
					let lHasUnrespondedShots_bl = event.data.hasUnrespondedShots;
					let lHasDelayedShots_bl = event.data.hasDelayedShots;
					let lHasUnparsedShotResponse_bl = event.data.hasUnparsedShotResponse;
					let lHasAwardedFreeSW_bl = event.data.hasAwardedFreeSW;

					let ricochetBullets = this._fPlayerInfo_pi.ricochetBullets; // If > 0, don't Rebuy
					if (!this._checkEnoughMoneyForOneShot(specBalance) && !ricochetBullets)
					{
						this._lootTypeReason = !!(event.data.dialogType === "lootCrate");
						this._activateOrDenyRebuy(specBalance, lHasUnrespondedShots_bl, lHasDelayedShots_bl, lHasUnparsedShotResponse_bl, lHasAwardedFreeSW_bl);
					}
					else if (this._fPlayerInfo_pi.betLevel > 1 || this._fLobbyWeaponsPanelInfo_lwpi.isPaidSpecialWeaponSelected)
					{
						if (!!lHasUnrespondedShots_bl || !!lHasDelayedShots_bl || !!lHasUnparsedShotResponse_bl || !!lHasAwardedFreeSW_bl)
						{
							// skip dialog appearing due to we don't still know shot results (win occured or not)
						}
						else
						{
							this.__activateDialog();
						}
					}
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
			case GAME_MESSAGES.RE_BUY_RESPONSE_RECIEVED:
					if (
							APP.isBattlegroundGame 
							&& APP.battlegroundController.info.isBattlegroundGameStarted
						)
					{
						this.__deactivateDialog();
					}
				break;
		}
	}

	_onPlayerInfoUpdated(event)
	{
		if (
				event.data[PlayerInfo.KEY_REAL_AMMO]
				|| event.data[PlayerInfo.KEY_UNPRESENTED_WIN]
			)
		{
			let lRealAmmo_num = this._realAmmo;
			let lShotCost_num = this._fPlayerInfo_pi.currentStake;
			let lRealAmmoCost_num = lRealAmmo_num * lShotCost_num;
			let lUnpresentedWin_num = this._fPlayerInfo_pi.unpresentedWin || 0;

			if (
					(this._fKeepDialog_bln && this._checkEnoughMoneyForCurShot(lUnpresentedWin_num + lRealAmmoCost_num))
					|| (
						!this._fKeepDialog_bln
						&& (~~(lRealAmmo_num) > 0 || (lUnpresentedWin_num + lRealAmmoCost_num) >= lShotCost_num)
					)
				)
			{
				this.__deactivateDialog();
			}
		}
	}

	_balanceRefreshingHandler(data)
	{
		if (
				(this._checkEnoughMoneyForOneShot(data.specBalance) && !data.keepDialog && !this._fKeepDialog_bln)
				|| (this._fKeepDialog_bln && this._checkEnoughMoneyForCurShot(data.specBalance))
			)
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

	_checkEnoughMoneyForCurShot(aSpecBalance_num)
	{
		if (!this._fPlayerInfo_pi)
		{
			return false;
		}

		let lIsEnoughMoney_bl = false;

		let lBalance_num = aSpecBalance_num === undefined ? this._fPlayerInfo_pi.balance : aSpecBalance_num;
		let lCurrentShotCost_num = this._fLobbyWeaponsPanelInfo_lwpi.i_getCurrentWeaponShotPrice();
		
		if (
				!this._fLobbyWeaponsPanelInfo_lwpi.isPaidWeaponSelected
				|| lBalance_num >= lCurrentShotCost_num
			)
		{
			lIsEnoughMoney_bl = true;
		}

		return lIsEnoughMoney_bl;
	}

	_handleGameGeneralError(errorCode, requestClass)
	{
		if (!this._fTournamentModeInfo_tni.isTournamentMode)
		{
			return;
		}

		let supported_codes = GUSLobbyWebSocketInteractionController.ERROR_CODES;
		switch (errorCode)
		{
			case supported_codes.SW_PURCHASE_LIMIT_EXCEEDED:
				this._fKeepDialog_bln = false;
				this.__deactivateDialog();
				break;

			case supported_codes.NOT_ENOUGH_MONEY:
				if (requestClass == GAME_CLIENT_MESSAGES.RE_BUY)
				{
					this.__deactivateDialog();
				}
				break;
			case supported_codes.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE:
				this.__deactivateDialog();
				break;
		}
	}

	_activateOrDenyRebuy(aSpecBalance_num = undefined, aHasUnrespondedShots_bl=false, aHasDelayedShots_bl=false, aHasUnparsedShotResponse_bl=false, aHasAwardedFreeSW_bl=false)
	{
		let lBalance_num = this._fPlayerInfo_pi.balance;
		if (aSpecBalance_num !== undefined)
		{
			lBalance_num = aSpecBalance_num;
		}
		else
		{
			let lUnpresentedWin_num = this._fPlayerInfo_pi.unpresentedWin || 0;
			let lRealAmmo_num = this._realAmmo;
			let lRealAmmoCost_num = lRealAmmo_num === undefined ? 0 : lRealAmmo_num * this._fPlayerInfo_pi.currentStake;

			lBalance_num += lUnpresentedWin_num + lRealAmmoCost_num;
		}

		let ricochetBullets = this._fPlayerInfo_pi.ricochetBullets; // If > 0, don't Rebuy

		if (
				(lBalance_num >= this._fPlayerInfo_pi.minRoomsStake)
				|| ricochetBullets
			)
		{
			this.emit(GUGameRebuyDialogController.EVENT_ON_REBUY_DENIED_WITH_NON_SPENT_BALANCE);
		}
		else
		{
			if (
					!this._checkEnoughMoneyForOneShot(lBalance_num)
					&& (!!aHasUnrespondedShots_bl || !!aHasDelayedShots_bl || !!aHasUnparsedShotResponse_bl || !!aHasAwardedFreeSW_bl)
				)
			{
				return;
			}

			this.__activateDialog();
		}
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

	_calcTotalBalance()
	{
		let lBalance_num = this._fPlayerInfo_pi.balance;

		let lUnpresentedWin_num = this._fPlayerInfo_pi.unpresentedWin || 0;
		let lRealAmmo_num = this._realAmmo;
		let lRealAmmoCost_num = lRealAmmo_num === undefined ? 0 : lRealAmmo_num * this._fPlayerInfo_pi.currentStake;

		lBalance_num += lUnpresentedWin_num + lRealAmmoCost_num;

		return lBalance_num;
	}

}

export default GUGameRebuyDialogController