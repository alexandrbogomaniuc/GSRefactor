import GUDialogController from '../GUDialogController';
import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyWebSocketInteractionController from '../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GUSLobbyBattlegroundController from '../../../../custom/battleground/GUSLobbyBattlegroundController';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES, LOBBY_MESSAGES } from '../../../../external/GUSLobbyExternalCommunicator';
import GUSGameBattlegroundNoWeaponsFiredDialogController from './game/GUSGameBattlegroundNoWeaponsFiredDialogController';
import GUSBattlegroundBuyInConfirmationDialogInfo from '../../../../../model/uis/custom/dialogs/custom/GUSBattlegroundBuyInConfirmationDialogInfo';
import { GAME_CLIENT_MESSAGES } from '../../../../../model/interaction/server/GUSLobbyWebSocketInteractionInfo';
import { BTN_TYPE } from '../../../../../view/uis/dialogs/custom/GUSBattlegroundBuyInConfirmationDialogView';

class GUSBattlegroundBuyInConfirmationDialogController extends GUDialogController
{
	static get EVENT_BATTLEGROUND_BUY_IN_CONFIRMED () { return "EVENT_BATTLEGROUND_BUY_IN_CONFIRMED" };
	static get EVENT_BATTLEGROUND_RE_BUY_CONFIRMED () { return "EVENT_BATTLEGROUND_RE_BUY_CONFIRMED" };
	static get EVENT_BATTLEGROUND_BUY_IN_CANCELED () { return "EVENT_BATTLEGROUND_BUY_IN_CANCELED" };
	static get EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED () { return "EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED" };
	static get EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS() { return "EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS" };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._fBattlegroundController_bc = null;

		this._fCurrentBalance = null;

		this._initBattlegroundBuyInConfirmationDialogController();

		this._fIsPlayerSitOutRequired_bl = false;
	}

	_initBattlegroundBuyInConfirmationDialogController()
	{
	}

	__init ()
	{
		super.__init()
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().battlegroundBuyInConfirmationDialogView;
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

		this._fBattlegroundController_bc = APP.battlegroundController;
		this._fBattlegroundController_bc.on(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_BUY_IN_REOPENED, this._reopenBuyInDialog, this);
		this._fBattlegroundController_bc.on(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_BUY_IN_CLOSED, this._closeBuyInDialog, this);
		
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		APP.dialogsController.gameGameBattlegroundNoWeaponsFiredDialogController.on(
			GUSGameBattlegroundNoWeaponsFiredDialogController.EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED, this._noWeaponsFiredPlayAgainClicked, this);
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	_reopenBuyInDialog()
	{
		let l_bi = APP.battlegroundController.info;
		this.info.setMode(GUSBattlegroundBuyInConfirmationDialogInfo.MODE_ID_RE_BUY);
		this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);

		this.__activateDialog();
		this.emit(GUSBattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_CANCELED);
	}

	_closeBuyInDialog()
	{
		this.__deactivateDialog();
	}

	_noWeaponsFiredPlayAgainClicked()
	{
		let l_bi = APP.battlegroundController.info;
		this.info.setMode(GUSBattlegroundBuyInConfirmationDialogInfo.MODE_ID_RE_BUY);
		this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);

		this.__activateDialog();
	}

	_onServerEnterLobbyMessage(event)
	{
		this._fCurrentBalance = event.messageData.balance;
		
		this._startHandleEnvironmentMessages();
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		let msgData = event.data;
		let info = this.info;

		switch (msgType)
		{
			case GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST:
				let l_bi = APP.battlegroundController.info;
				
				this.info.setMode(GUSBattlegroundBuyInConfirmationDialogInfo.MODE_ID_RE_BUY);
				this.info.setBuyInCost(l_bi.getConfirmedBuyInCost() || APP.appParamsInfo.battlegroundBuyIn);
				
				if (msgData && msgData.isRRActive)
				{
					this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_SCOREBOARD);
				}
				else
				{
					this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_LOBBY);
				}
				
				this.__activateDialog();

				if (this._fIsPlayerSitOutRequired_bl && msgData.alreadySitInNumber !== undefined)
				{
					this._fIsPlayerSitOutRequired_bl = msgData.alreadySitInNumber !== -1;
					this._validateOkButton();
				}
				break;

			case GAME_MESSAGES.BATTLEGROUND_BUY_IN_REOPENING_REQUIRED:
				this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_LOBBY);
				this._reopenBuyInDialog();
				break;

			case GAME_MESSAGES.BATTLEGROUND_ROUND_CANCELED:
				this.view.changeButtons(BTN_TYPE.BTN_TYPE_RETURN_TO_LOBBY);
				break;

			case GAME_MESSAGES.ON_SIT_OUT_REQUIRED:
				this._fIsPlayerSitOutRequired_bl = true;
				this._validateOkButton();
				break;

			case GAME_MESSAGES.ON_SIT_OUT_RESPONSE:
				if(msgData.rid != -1)
				{
					this._fIsPlayerSitOutRequired_bl = false;
					this._validateOkButton();
				}
				break;

			case GAME_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (msgData.requestClass === GAME_CLIENT_MESSAGES.SIT_OUT)
				{
					this._fIsPlayerSitOutRequired_bl = false;
					this._validateOkButton();
				}
				break;
		}
	}

	_validateOkButton()
	{
		if (this._fIsPlayerSitOutRequired_bl)
		{
			this.view && this.view.deactivateOkButton();
		}
		else
		{
			this.view && this.view.activateOkButton();
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

		//buttons configuration...
		view.setOkCancelMode();
		//...buttons configuration

		super.__validateViewLevel();
	}
	//...VALIDATION

	__deactivateDialog()
	{
		super.__deactivateDialog();

		this.view.hideWaitLayer();
	}

	__onDialogOkButtonClicked(event)
	{
		if (this._fIsPlayerSitOutRequired_bl) return;
		
		let l_bi = APP.battlegroundController.info;
		if (this._fCurrentBalance >= l_bi.getConfirmedBuyInCost())
		{
			super.__onDialogOkButtonClicked(event);

			switch (this.info.getModeId())
			{
				case GUSBattlegroundBuyInConfirmationDialogInfo.MODE_ID_BUY_IN:
					this.emit(GUSBattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_CONFIRMED, { buyIn: this.info.getBuyInCost()} );
					this.view.showWaitLayer();
					break;
				case GUSBattlegroundBuyInConfirmationDialogInfo.MODE_ID_RE_BUY:
					APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_CONFIRMED);
					this.emit(GUSBattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_RE_BUY_CONFIRMED, { buyIn: this.info.getBuyInCost()} );

					APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_RE_BUY_CONFIRMED);
					this.__deactivateDialog();
					break;
			}

			APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_BUTTON_CLICKED);
		}
		else
		{
			this.emit(GUSBattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_INSUFFICIENT_FUNDS_DIALOG_REQUIRED);
		}
	}

	__onDialogCancelButtonClicked(event)
	{
		if (this._fBattlegroundController_bc.isRoundResultWasActivated())
		{
			super.__onDialogCancelButtonClicked(event);
			this.__deactivateDialog();

			this.emit(GUSBattlegroundBuyInConfirmationDialogController.EVENT_BATTLEGROUND_BUY_IN_BACK_TO_RESULTS);
		}
		else
		{
			super.__onGameDialogChangeWorldBuyInButtonClicked(event);
		}
	}

	_startHandleEnvironmentMessages()
	{
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onLobbyServerBalanceUpdated, this);

		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;
	}

	_onLobbyServerBalanceUpdated(event)
	{
		this._fCurrentBalance = event.messageData.balance;
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

		if (GUSLobbyWebSocketInteractionController.isFatalError(event.errorType))
		{
			this.info.errorCode = serverData.code;
			this.info.errorTime = serverData.date;
			this.info.rid = serverData.rid;

			this.__deactivateDialog();
		}
	}

	__activateDialog()
	{
		if (
				!APP.isBattlegroundGame
				|| APP.dialogsController.lobbyBattlegroundNotEnoughPlayersDialogController.info.isActive
				|| APP.secondaryScreenController.paytableScreenController.isActiveScreen
			)
		{
			return;
		}

		super.__activateDialog();

		this._validateOkButton();
		this.view.updateBuyInCostIndicator(this.info.getBuyInCost());

	}
	
}

export default GUSBattlegroundBuyInConfirmationDialogController