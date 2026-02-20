import DialogController from '../../DialogController';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../../interaction/server/LobbyWebSocketInteractionController';
import LobbyStateController from '../../../../../state/LobbyStateController';
import {GAME_MESSAGES, LOBBY_MESSAGES} from '../../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';
import LobbyExternalCommunicator from '../../../../../../external/LobbyExternalCommunicator';
import LobbyApp from '../../../../../../LobbyAPP';

class GameBattlegroundNoWeaponsFiredDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };
	static get EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED() { return "EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED" };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initGameBattlegroundNoWeaponsFiredDialogController();
	}

	_initGameBattlegroundNoWeaponsFiredDialogController()
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

		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.info;
		
		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

		if (APP.isCAFMode)
		{
			APP.on(LobbyApp.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		}else
		{
			APP.on(LobbyApp.EVENT_ON_OBSERVER_MODE_ACTIVATED, this._onObserverModeActivated, this);
		}
	}
	
	_onObserverModeActivated()
	{
		this.__deactivateDialog();
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

		//message configuration...
		//view.setMessage("TABattlegroundToJoinThisGame");
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

		this.emit(GameBattlegroundNoWeaponsFiredDialogController.EVENT_BATTLEGROUND_PLAY_AGAIN_CLICKED);
		this.__deactivateDialog();
	}

	__onDialogCancelButtonClicked(event)
	{
		super.__onGameDialogChangeWorldBuyInButtonClicked(event);
	}

	_onPlayerInfoUpdated(event)
	{
		if (event.data.isKicked !== undefined)
		{
			if (this._fPlayerInfo_pi.isKicked)
			{
				if (this.info.isActive)
				{
					this.__deactivateDialog();
				}
			}
		}
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

export default GameBattlegroundNoWeaponsFiredDialogController