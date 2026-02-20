import GUGameBaseDialogController from './GUGameBaseDialogController';
import GUSLobbyExternalCommunicator from '../../../../../external/GUSLobbyExternalCommunicator';
import { APP } from '../../../../../../../unified/controller/main/globals';
import { GAME_MESSAGES } from '../../../../../external/GUSExternalCommunicator';

class GUGameForceSitOutDialogController extends GUGameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED() { return GUGameBaseDialogController.EVENT_DIALOG_PRESENTED }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._initGameForceSitOutDialogController();
	}

	_initGameForceSitOutDialogController()
	{
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameForceSitOutDialogView;
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

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
		let info = this.info;
		if (info.isActive)
		{
			let view = this.__fView_uo;

			//message configuration...
			view.setMessage("TADialogForceSitOut");
			view.setOkMode();
			//...message configuration
		}

		if (APP.FRBController.i_getInfo().isActivated && APP.FRBController.i_getInfo().gameFRBStarted)
		{
			let view = this.__fView_uo;
			view.okButton.visible = false;
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		this.__deactivateDialog();
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		switch (msgType)
		{
			case GAME_MESSAGES.EVENT_ON_FORCE_SIT_OUT_REQUIRED:
				this._showDialogIfRequired(event.data);
				break;
			case GAME_MESSAGES.RESTORED_AFTER_UNREASONABLE_REQUEST:
			case GAME_MESSAGES.BACK_TO_LOBBY:
				if (this.info.isActive)
				{
					this.__deactivateDialog();
				}
				break;
		}
	}

	_showDialogIfRequired(data)
	{
		if (!this.info.isActive)
		{
			this.info.compensateSpecialWeapons = data.compensateSpecialWeapons || 0; //since June 2019, these parameters are not transferred to this dialog
			this.info.compensateSpecialWeapons = data.compensateSpecialWeapons || 0;

			this.__activateDialog();
			this.info.resitOutInProcess = false;
		}
	}
}

export default GUGameForceSitOutDialogController