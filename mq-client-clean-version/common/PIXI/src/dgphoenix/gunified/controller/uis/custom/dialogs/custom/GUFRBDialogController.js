import { APP } from '../../../../../../unified/controller/main/globals';
import GUSLobbyApplication from '../../../../main/GUSLobbyApplication';
import GUFRBDialogInfo from '../../../../../model/uis/custom/dialogs/custom/GUFRBDialogInfo';
import GUDialogController from '../GUDialogController';
import GUSLobbyFRBController from '../../../../custom/frb/GUSLobbyFRBController';
import NumberValueFormat from '../../../../../../unified/view/custom/values/NumberValueFormat';
import { LOBBY_MESSAGES } from '../../../../external/GUSExternalCommunicator';

class GUFRBDialogController extends GUDialogController
{
	static get EVENT_DIALOG_ACTIVATED() { return GUDialogController.EVENT_DIALOG_ACTIVATED }

	deactivateDialog()
	{
		this.__deactivateDialog();
	}

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, null, parentController);

		this._fIsNeedActiveDialogOnRoomReload_bl = null;

		this._initFRBDialogController();
	}

	_initFRBDialogController()
	{
	}

	__init()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().FRBDialogView;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		if (APP.lobbyAppStarted)
		{
			this._startHandleGameErrors();
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
		this._fFRBController_frbc = APP.FRBController;
		this._fFRBInfo_frbi = this._fFRBController_frbc.i_getInfo();

		this._fFRBController_frbc.on(GUSLobbyFRBController.EVENT_ON_FRB_STATE_CHANGED, this._onFRBStateChanged, this);
		this.__validate();
		this._activateDialogSuspicion();

		APP.on(GUSLobbyApplication.EVENT_ON_LOBBY_SCREEN_SHOWED, this._onLobbyScreenShowed, this);
		APP.on(GUSLobbyApplication.EVENT_ON_ROOM_CLOSED, this._onRoomClosed, this);
	}

	//VALIDATION...
	__validateModelLevel()
	{
		var info = this.info;

		if (this._fFRBInfo_frbi)
		{
			info.messageType = this._fFRBInfo_frbi.messageType;
			info.nextModeFRB = this._fFRBInfo_frbi.nextModeFRB;
		}

		super.__validateModelLevel();

	}

	__validateViewLevel()
	{
		var info = this.info;
		var view = this.__fView_uo;

		let messageAssetId;
		let lValue_num = undefined;
		let lTextValue_str = undefined;
		let lSubString_str = undefined;


		view.setOkMode();
		view.okButton.setOKCaption();

		if (info.isActive)
		{
			switch (info.messageType)
			{
				case GUFRBDialogInfo.MESSAGE_FRB_EXPIRED:
					{
						if (info.nextModeFRB)
						{
							messageAssetId = "TADialogStandardFRBExpired";
						}
						else
						{
							messageAssetId = "TADialogStandardFRBExpiredRealMode";
						}
					}
					break;
				case GUFRBDialogInfo.MESSAGE_FRB_CANCELLED:
					{
						if (info.nextModeFRB)
						{
							messageAssetId = "TADialogStandardFRBCancelled";
						}
						else
						{
							messageAssetId = "TADialogStandardFRBCancelledRealMode";
						}
					}
					break;
				case GUFRBDialogInfo.MESSAGE_FRB_FINISHED:
					{
						lValue_num = this._fFRBInfo_frbi.realWinSum;
						lTextValue_str = NumberValueFormat.formatMoney(lValue_num);

						if (this._fFRBInfo_frbi.isWinLimitExceeded)
						{
							if (this._fFRBInfo_frbi.isRoomRestartPossible)
							{
								messageAssetId = "TADialogStandardFRBFinishedLimitCapRealMode";
								view.setOkCancelMode();
								view.okButton.setYesCaption();
								view.cancelButton.setNoCaption();
							}
							else
							{
								messageAssetId = "TADialogStandardFRBFinishedLimitCap";
							}
						}
						else if (!this._fFRBInfo_frbi.isRoomRestartPossible)
						{
							messageAssetId = "TADialogStandardFRBFinished";
						}
						else
						{
							messageAssetId = "TADialogStandardFRBFinishedRealMode";
							view.setOkCancelMode();
							view.okButton.setYesCaption();
							view.cancelButton.setNoCaption();
						}
					}
					break;
				case GUFRBDialogInfo.MESSAGE_FRB_LOBBY_INTRO:
					{
						messageAssetId = "TADialogFRBLobbyIntro";
					}
					break;
				case GUFRBDialogInfo.MESSAGE_FRB_ROOM_INTRO:
					{
						messageAssetId = "TADialogFRBRoomIntro";
						lValue_num = this._fFRBInfo_frbi.allWeaponsFreeShots;
						lSubString_str = "#shots:"
					}
					break;
				default:
				{
					throw new Error('Unsupported FRB message type: ' + info.messageType);
				}
					break;
			}

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

		// REENTER LOBBY

		this.info.messageType = null;
		this.__deactivateDialog();
	}

	__onDialogCancelButtonClicked(event)
	{
		super.__onDialogCancelButtonClicked(event);

		// REENTER LOBBY

		this.info.messageType = null;
		this.__deactivateDialog();
	}

	_onLobbyScreenShowed(event)
	{
		APP.off(GUSLobbyApplication.EVENT_ON_LOBBY_SCREEN_SHOWED, this._onLobbyScreenShowed, this);
		this._activateDialogSuspicion();
	}

	_onRoomClosed()
	{
		this._activateDialogSuspicion(true);
	}

	_onFRBStateChanged(event)
	{
		if (!this._fFRBInfo_frbi.isActivated)
		{
			if (this.info.isActive)
			{
				this.__deactivateDialog();
				return;
			}
		}

		this._fIsNeedActiveDialogOnRoomReload_bl = event.onRoomReload;

		this.__validate();
		this._activateDialogSuspicion();
	}

	__deactivateDialog()
	{
		super.__deactivateDialog();

		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.FRB_DIALOG_DEACTIVATED);
	}

	__activateDialog()
	{
		super.__activateDialog();

		APP.externalCommunicator.sendExternalMessage(LOBBY_MESSAGES.FRB_DIALOG_ACTIVATED);
	}

	_activateDialogSuspicion(aOnlyForLobbyIntro_bl = null)
	{
		if (!this._fFRBInfo_frbi.isActivated)
		{
			this.__deactivateDialog();//just for sure
			return;
		}
		if (aOnlyForLobbyIntro_bl)
		{
			if (
					this.info.messageType == GUFRBDialogInfo.MESSAGE_FRB_LOBBY_INTRO
					&& (!APP.layout.isGamesLayoutVisible || this._fIsNeedActiveDialogOnRoomReload_bl)
				)
			{
				this.__activateDialog();
			}		
		}
		else
		{
			switch (this.info.messageType)
			{
				case GUFRBDialogInfo.MESSAGE_FRB_ROOM_INTRO:
				case GUFRBDialogInfo.MESSAGE_FRB_EXPIRED:
				case GUFRBDialogInfo.MESSAGE_FRB_CANCELLED:
					this.__activateDialog();
					break;
				case GUFRBDialogInfo.MESSAGE_FRB_FINISHED:
					if (APP.layout.isGamesLayoutVisible)
					{
						this.__activateDialog();
					}
					break;
				case GUFRBDialogInfo.MESSAGE_FRB_LOBBY_INTRO:
					if (!APP.layout.isGamesLayoutVisible || this._fIsNeedActiveDialogOnRoomReload_bl)
					{
						this.__activateDialog();
					}
					break;
				case GUFRBDialogInfo.MESSAGE_FORCE_SIT_OUT:
					break;
			}
		}
	}
}

export default GUFRBDialogController;