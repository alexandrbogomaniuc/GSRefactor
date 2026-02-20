import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import LobbyAPP from '../../../../../LobbyAPP';
import LobbyBonusController from '../../bonus/LobbyBonusController';
import BonusDialogInfo from '../../../../../model/uis/custom/dialogs/custom/BonusDialogInfo';
import NumberValueFormat from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import DialogController from '../../dialogs/DialogController';
import {LOBBY_MESSAGES} from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';

class BonusDialogController extends DialogController
{
	static get EVENT_DIALOG_ACTIVATED()		{return DialogController.EVENT_DIALOG_ACTIVATED};

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, null, parentController);
		this._initBonusDialogController();
	}

	_initBonusDialogController()
	{

	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().bonusDialogView;
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		if (APP.lobbyAppStarted)
		{
			this._startHandleGameErrors();
		}
		else
		{
			APP.once(LobbyAPP.EVENT_ON_LOBBY_STARTED, this._onLobbyStarted, this);
		}

	}

	_onLobbyStarted(event)
	{
		this._startHandleEnvironmentMessages();
	}

	_startHandleEnvironmentMessages()
	{
		this._fLobbyBonusController_fbc = APP.lobbyBonusController;
		this._fLobbyBonusInfo_lbi = this._fLobbyBonusController_fbc.i_getInfo();

		this._fLobbyBonusController_fbc.on(LobbyBonusController.EVENT_ON_BONUS_STATE_CHANGED, this._onBonusStateChanged, this);
		this.__validate();
		this._activateDialogSuspicion();

		APP.on(LobbyAPP.EVENT_ON_LOBBY_SCREEN_SHOWED, this._onLobbyScreenShowed, this);
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		var info = this.info;

		if (this._fLobbyBonusInfo_lbi)
		{
			info.messageType = this._fLobbyBonusInfo_lbi.messageType;
			info.nextModeFRB = this._fLobbyBonusInfo_lbi.nextModeFRB;
		}

		super.__validateModelLevel();

	}

	__validateViewLevel ()
	{
		var info = this.info;
		var view = this.__fView_uo;

		let messageAssetId;
		let lValue_num = undefined;
		let lTextValue_str = undefined;
		let lSubString_str = undefined;

		if (info.isActive)
		{
			switch (info.messageType)
			{
				case BonusDialogInfo.MESSAGE_BONUS_EXPIRED:
					{
						if (info.nextModeFRB)
						{
							messageAssetId = "TADialogStandardBonusExpired";
						}
						else
						{
							messageAssetId = "TADialogStandardBonusExpiredRealMode";
						}
					}
					break;
				case BonusDialogInfo.MESSAGE_BONUS_CANCELLED:
					{
						if (info.nextModeFRB)
						{
							messageAssetId = "TADialogStandardBonusCancelled";
						}
						else
						{
							messageAssetId = "TADialogStandardBonusCancelledRealMode";
						}
					}
					break;
				case BonusDialogInfo.MESSAGE_BONUS_RELEASED:
					{
						lValue_num = this._fLobbyBonusInfo_lbi.realWinSum || 0;
						let lNotRealValue_num = this._fLobbyBonusInfo_lbi.winSum || 0;
						lTextValue_str = NumberValueFormat.formatMoney(lValue_num);
						if (lNotRealValue_num != lValue_num)
						{
							if (this._fLobbyBonusInfo_lbi.isRoomRestartPossible)
							{
								messageAssetId = "TADialogStandardBonusFinishedLimitCapRealMode";
							}
							else
							{
								messageAssetId = "TADialogStandardBonusFinishedLimitCap";
							}
						}
						else if (this._fLobbyBonusInfo_lbi.isRoomRestartPossible)
						{
							messageAssetId = "TADialogStandardBonusReleasedRealMode";
						}
						else
						{
							messageAssetId = "TADialogStandardBonusReleased";
						}
					}
					break;
				case BonusDialogInfo.MESSAGE_BONUS_LOST:
					{
						if (info.nextModeFRB)
						{
							messageAssetId = "TADialogStandardBonusLost";
						}
						else
						{
							messageAssetId = "TADialogStandardBonusLostRealMode";
						}
					}
					break;
				case BonusDialogInfo.MESSAGE_BONUS_LOBBY_INTRO:
					{
						messageAssetId = "TADialogBonusLobbyIntro";
						//DEBUG...
						//messageAssetId = "TADialogStandardBonusExpired"
						//messageAssetId = "TADialogStandardBonusExpiredRealMode"
						//messageAssetId = "TADialogStandardBonusCancelled"
						//messageAssetId = "TADialogStandardBonusCancelledRealMode"
						//messageAssetId = "TADialogStandardBonusReleased"
						//messageAssetId = "TADialogStandardBonusReleasedRealMode"
						//messageAssetId = "TADialogStandardBonusLost"
						//messageAssetId = "TADialogStandardBonusLostRealMode"
						//messageAssetId = "TADialogMessageLowBonusBalance"
						//...DEBUG
					}
					break;
				case BonusDialogInfo.MESSAGE_BONUS_ROOM_INTRO:
					{
						messageAssetId = "TADialogBonusRoomIntro";
					}
					break;
				default:
					{
						throw new Error('Unsupported BONUS message type: ' + info.messageType);
					}
					break;
			}

			//buttons configuration...
			if (
					info.messageType === BonusDialogInfo.MESSAGE_BONUS_RELEASED
					&& this._fLobbyBonusInfo_lbi.isRoomRestartPossible
				)
			{
				view.setOkCancelMode();
				view.okButton.setYesCaption();
				view.cancelButton.setNoCaption();
			}
			else
			{
				view.setOkMode();
				view.okButton.setOKCaption();
			}
			//...buttons configuration

			//message configuration...
			view.setMessage(messageAssetId, lValue_num, lTextValue_str, lSubString_str);
			//...message configuration
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		var info = this.info;

		/*switch (info.messageType)
		{
			case BonusDialogInfo.MESSAGE_BONUS_EXPIRED:
			case BonusDialogInfo.MESSAGE_BONUS_CANCELLED:
			case BonusDialogInfo.MESSAGE_BONUS_RELEASED:

				break;
		}*/
		//[Y] if messageType was CANCELLED, FINISHED or EXPIRED - then go to the lobby

		info.messageType = null;
		this.__deactivateDialog();
	}

	__deactivateDialog()
	{
		super.__deactivateDialog();

		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BONUS_DIALOG_DEACTIVATED);
	}

	__activateDialog()
	{
		super.__activateDialog();

		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.BONUS_DIALOG_ACTIVATED);
	}

	_onLobbyScreenShowed(event)
	{
		APP.off(LobbyAPP.EVENT_ON_LOBBY_SCREEN_SHOWED, this._onLobbyScreenShowed, this);
		this._activateDialogSuspicion();
	}

	_onBonusStateChanged(event)
	{
		if (!this._fLobbyBonusInfo_lbi.isActivated)
		{
			if (this.info.isActive)
			{
				this.__deactivateDialog();
				return;
			}
		}

		this.__validate();
		this._activateDialogSuspicion();
	}

	_activateDialogSuspicion()
	{
		if (!this._fLobbyBonusInfo_lbi.isActivated)
		{
			return;
		}

		switch (this.info.messageType)
		{
			case BonusDialogInfo.MESSAGE_BONUS_ROOM_INTRO:
			case BonusDialogInfo.MESSAGE_BONUS_EXPIRED:
			case BonusDialogInfo.MESSAGE_BONUS_CANCELLED:
			case BonusDialogInfo.MESSAGE_BONUS_RELEASED:
			case BonusDialogInfo.MESSAGE_BONUS_LOST:
				this.__activateDialog();
				break;
			case BonusDialogInfo.MESSAGE_BONUS_LOBBY_INTRO:
				if (APP.lobbyScreen.visible)
				{
					this.__activateDialog();
				}
				break;
			case BonusDialogInfo.MESSAGE_FORCE_SIT_OUT:
				break;
		}
	}
}

export default BonusDialogController