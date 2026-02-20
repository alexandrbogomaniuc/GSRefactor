import GUGameBaseDialogController from './GUGameBaseDialogController';
import { APP } from '../../../../../../../unified/controller/main/globals';
import GUSLobbyExternalCommunicator from '../../../../../external/GUSLobbyExternalCommunicator';
import { GAME_MESSAGES } from '../../../../../external/GUSExternalCommunicator';
import GUSLobbyApplication from '../../../../../main/GUSLobbyApplication';
import GUGameRebuyDialogController from './GUGameRebuyDialogController';
import PlayerController from '../../../../../../../unified/controller/custom/PlayerController';
import PlayerInfo from '../../../../../../../unified/model/custom/PlayerInfo';
import GUSLobbyStateController from '../../../../../state/GUSLobbyStateController';
import { WEAPONS } from '../../../../../../model/weapons/GUSWeaponsInfo';

class GUGameNEMForRoomDialogController extends GUGameBaseDialogController
{
	static get EVENT_ON_REBUY_DENIED_WITH_NON_SPENT_BALANCE()	{ return "EVENT_ON_REBUY_DENIED_WITH_NON_SPENT_BALANCE" }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._fNoReturnToLobby_bln = false;
		this._fKeepDialog_bln = false;
		this._fRebuyBalanceUpdateIsExpected_bln = false;

		this._initGameNEMForRoomDialogController();
	}

	_initGameNEMForRoomDialogController()
	{
	}

	__init()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameNEMForRoomDialogView;
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

	_onLobbyStarted(event)
	{
		this._startHandleEnvironmentMessages();
	}

	_startHandleEnvironmentMessages()
	{
		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;
		this._fLobbyWeaponsPanelInfo_lwpi = APP.commonPanelController.lobbyWeaponsPanelController.info;

		APP.on(GUSLobbyApplication.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		this._fPlayerController_pc.on(PlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

		APP.dialogsController.gameRebuyDialogController.on(GUGameRebuyDialogController.EVENT_ON_REBUY_DENIED_WITH_NON_SPENT_BALANCE, this._onRebuyDeniedWithNonSpentBalance, this);

		let lLobbyStateController_lsc = APP.lobbyStateController;
		this._lobbyStateInfo = lLobbyStateController_lsc.info;
		lLobbyStateController_lsc.on(GUSLobbyStateController.EVENT_ON_LOBBY_VISIBILITY_CHANGED, this._onLobbyVisibilityChanged, this);

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
	}

	//VALIDATION...
	__validateModelLevel()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel()
	{
		var info = this.info;
		var view = this.__fView_uo;
		var messageAssetId;

		if (info.isActive)
		{
			messageAssetId = "TADialogMessageTournamentLowBalanceForCurrentRoom";

			if (this._fPlayerInfo_pi.betLevel > 1 && this._checkEnoughMoneyForOneShot())
			{
				messageAssetId = "TADialogMessageTournamentBetLevel";
				this._fNoReturnToLobby_bln = true;
				this._fKeepDialog_bln = true;
			}
			if (
				this._fLobbyWeaponsPanelInfo_lwpi.isPaidSpecialWeaponSelected && 
					(
						this._checkEnoughMoneyForOneShot()||
						this._fRebuyBalanceUpdateIsExpected_bln
					)
				)
			{
				messageAssetId = "TADialogMessageTournamentPayWeapon";
				this._fNoReturnToLobby_bln = true;
				this._fKeepDialog_bln = true;
			}

			view.setMessage(messageAssetId);
			view.setOkMode();
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		super.__deactivateDialog();

		this._fNoReturnToLobby_bln = false;
		this._fKeepDialog_bln = false;
	}

	__onDialogOkButtonClicked(event)
	{
		if (!this._fNoReturnToLobby_bln)
		{
			super.__onDialogOkButtonClicked(event);
		}

		this._fNoReturnToLobby_bln = false;
		this._fKeepDialog_bln = false;
		this.__deactivateDialog();
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.RE_BUY_RESPONSE_RECIEVED:
				this._fRebuyBalanceUpdateIsExpected_bln = true;
				break;
			case GAME_MESSAGES.SERVER_BALANCE_UPDATED_MESSAGE_RECIEVED:
				this._balanceRefreshingHandler(event.data);
				this._fRebuyBalanceUpdateIsExpected_bln = false;
				break;
			case GAME_MESSAGES.SERVER_CONNECTION_OPENED:
			case GAME_MESSAGES.SERVER_CONNECTION_CLOSED:
			case GAME_MESSAGES.ROOM_CLOSED:
				this.__deactivateDialog();
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
					this._fKeepDialog_bln && this._checkEnoughMoneyForCurShot(lUnpresentedWin_num + lRealAmmoCost_num)
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

	_onLobbyVisibilityChanged(event)
	{
		if (event.visible)
		{
			this.__deactivateDialog();
		}
	}

	_balanceRefreshingHandler(data)
	{
		if (
			this._checkEnoughMoneyForOneShot(data.specBalance) && !this._fKeepDialog_bln
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
				lBalance_num >= this._fPlayerInfo_pi.currentStake
				|| ~~(lRealAmmo_num) > 0
			)
		{
			lIsEnoughMoney_bl = true;
		}

		return lIsEnoughMoney_bl;
	}

	_checkEnoughMoneyForCurShot(aSpecBalance_num)
	{
		let lIsEnoughMoney_bl = false;
		if (!this._fPlayerInfo_pi)
		{
			return lIsEnoughMoney_bl;
		}

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

	_onRebuyDeniedWithNonSpentBalance(event)
	{
		if (!this._lobbyStateInfo.lobbyScreenVisible)
		{
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
}

export default GUGameNEMForRoomDialogController