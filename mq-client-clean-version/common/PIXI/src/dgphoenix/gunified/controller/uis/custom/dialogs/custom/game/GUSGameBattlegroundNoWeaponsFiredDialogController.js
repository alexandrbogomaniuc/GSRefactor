import GUDialogController from '../../GUDialogController';
import { APP } from '../../../../../../../unified/controller/main/globals';
import GUSLobbyExternalCommunicator, {GAME_MESSAGES, LOBBY_MESSAGES} from '../../../../../external/GUSLobbyExternalCommunicator';
import GUSLobbyWebSocketInteractionController from '../../../../../interaction/server/GUSLobbyWebSocketInteractionController';
import GUSLobbyBattlegroundController from '../../../../../custom/battleground/GUSLobbyBattlegroundController';

class GUSGameBattlegroundNoWeaponsFiredDialogController extends GUDialogController
{
	static get EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED() { return "EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED" };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initGUSGameBattlegroundNoWeaponsFiredDialogController();
	}

	_initGUSGameBattlegroundNoWeaponsFiredDialogController()
	{

	}

	__init ()
	{
		super.__init()
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameGameBattlegroundNoWeaponsFiredDialogView;
	}

	__initModelLevel ()
	{
		super.__initModelLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();
		
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
		
		this._fBattlegroundController_bc = APP.battlegroundController;
		this._fBattlegroundController_bc.on(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NO_WEAPONS_FIRED_REOPENED, this.__activateDialog, this);
		this._fBattlegroundController_bc.on(GUSLobbyBattlegroundController.EVENT_BATTLEGROUND_NO_WEAPONS_FIRED_CLOSED, this.__deactivateDialog, this);
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case GAME_MESSAGES.BATTLEGROUND_ROUND_CANCELED:
				if(!event.data.isWaitState)
				{
					let refund = event.data.refundedAmount || APP.appParamsInfo.battlegroundBuyIn;
					this.info.setRefund(refund);

					this.__activateDialog();
				}
				break;
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
		super.__onDialogOkButtonClicked(event);

		this.emit(GUSGameBattlegroundNoWeaponsFiredDialogController.EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED);
		this.__deactivateDialog();
	}

	__onDialogCancelButtonClicked(event)
	{
		super.__onGameDialogChangeWorldBuyInButtonClicked(event);
	}

	_startHandleEnvironmentMessages()
	{
		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onLobbyServerConnectionOpened, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onLobbyServerConnectionClosed, this);
		webSocketInteractionController.on(GUSLobbyWebSocketInteractionController.EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND, this._onCancelBattlegroundRound, this);
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
			case GUSLobbyWebSocketInteractionController.ERROR_CODES.ROOM_NOT_OPEN:
			case GUSLobbyWebSocketInteractionController.ERROR_CODES.ROOM_NOT_FOUND:
				this.__deactivateDialog();
				break;
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

	_onCancelBattlegroundRound(event)
	{
		this.__deactivateDialog();
	}

	__activateDialog()
	{
		super.__activateDialog();

		if (this.info.getRefund() != APP.appParamsInfo.battlegroundBuyIn)
		{
			this.view.updateRefundIndicator(this.info.getRefund());
			this.view.showRefundView(true);
		}
		else
		{
			this.view.showRefundView(false);
		}
	}
}

export default GUSGameBattlegroundNoWeaponsFiredDialogController