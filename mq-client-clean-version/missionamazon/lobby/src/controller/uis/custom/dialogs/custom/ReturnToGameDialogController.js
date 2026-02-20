import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyWebSocketInteractionController from '../../../../interaction/server/LobbyWebSocketInteractionController';
import { SERVER_MESSAGES } from '../../../../../model/interaction/server/LobbyWebSocketInteractionInfo';
import FRBController from '../../../../custom/frb/FRBController';
import LobbyApp from '../../../../../LobbyAPP';

class ReturnToGameDialogController extends DialogController
{
	// static get EVENT_DIALOG_PRESENTED() 			{return DialogController.EVENT_DIALOG_PRESENTED};
	// static get EVENT_PRESENTED_DIALOG_UPDATED() 	{return DialogController.EVENT_PRESENTED_DIALOG_UPDATED};

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, null, parentController);

		this._initReturnToGameDialogController();
	}

	_initReturnToGameDialogController()
	{
		this._isFirstEnterHandled = false;
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().returnToGameDialogView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		APP.webSocketInteractionController.on(LobbyWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		APP.FRBController.on(FRBController.EVENT_ON_FRB_STATE_CHANGED, this._onFRBStateChanged, this);

		APP.on(LobbyApp.EVENT_ON_LOBBY_UI_SHOWN, this._onLobbyUIShown, this);
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		let info = this.info;
		if (info.isActive)
		{
			let view = this.__fView_uo;
			//message configuration...
			view.setMessage("TADialogRTGResumeRoom", info.roomId, info.alreadySitInStake);

			view.setOkCancelCustomMode();
			//...message configuration
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		this.info.resetLasthandInfo();
		this.__deactivateDialog();
	}

	_onServerMessage(event)
	{
		let data = event.messageData;

		switch(data.class)
		{
			case SERVER_MESSAGES.ENTER_LOBBY_RESPONSE:
				if (
					!(
						data.battleground &&
						data.battleground.alreadySeatRoomId
					)
					&& data.roomId && data.roomId != -1
					)
				{
					this.info.roomId = data.roomId;
					this.info.alreadySitInStake = data.alreadySitInStake;

					if (APP.lobbyScreen.visible || !this._isFirstEnterHandled)
					{
						this.__activateDialog();
					}
				}
				else
				{
					this.info.resetLasthandInfo();

					if (this.info.isActive)
					{
						this.__deactivateDialog();
					}
				}
				
				this._isFirstEnterHandled = true;
				break;
		}
	}

	_onFRBStateChanged(event)
	{
		if (APP.FRBController.info.isActivated)
		{
			this.info.resetLasthandInfo();

			if (this.info.isActive)
			{
				this.__deactivateDialog();
			}
		}
	}

	_onLobbyUIShown(event)
	{
		if (APP.lobbyScreen.isRoomLasthand && this.info.isLasthandInfoDefined && !this.info.isActive)
		{
			this.__activateDialog();
		}
	}

	__deactivateDialog()
	{
		this.info.resetLasthandInfo();

		super.__deactivateDialog();
	}
}

export default ReturnToGameDialogController