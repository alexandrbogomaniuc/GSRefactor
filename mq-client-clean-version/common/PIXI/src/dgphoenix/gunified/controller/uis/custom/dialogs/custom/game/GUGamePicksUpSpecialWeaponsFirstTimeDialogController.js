import GUGameBaseDialogController from './GUGameBaseDialogController';
import { APP } from '../../../../../../../unified/controller/main/globals';
import { GAME_MESSAGES } from '../../../../../external/GUSExternalCommunicator';
import GUSLobbyExternalCommunicator from '../../../../../external/GUSLobbyExternalCommunicator';

class GUGamePicksUpSpecialWeaponsFirstTimeDialogController extends GUGameBaseDialogController
{
	static get ON_CHANGE_STATE() { return "ON_CHANGE_STATE"; }

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().picksUpSpecialWeaponsFirstTimeDialogView;
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);
	}

	__validateViewLevel()
	{
		let info = this.info;
		if (info.isActive)
		{
			let view = this.__fView_uo;

			//message configuration...
			view.setMessage();
			view.setOkCancelMode();
			//...message configuration
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		this.emit(GUGamePicksUpSpecialWeaponsFirstTimeDialogController.ON_CHANGE_STATE, { state: true });

		this.__deactivateDialog();
	}

	__onDialogCancelButtonClicked(event)
	{
		super.__onDialogCancelButtonClicked(event);

		this.emit(GUGamePicksUpSpecialWeaponsFirstTimeDialogController.ON_CHANGE_STATE, { state: false });

		this.__deactivateDialog();
	}

	_onGameMessageReceived(event)
	{
		let msgType = event.type;
		switch (msgType)
		{
			case GAME_MESSAGES.PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME_REQUIRED:
				this._showDialogIfRequired();
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

	_showDialogIfRequired()
	{
		if (!this.info.isActive)
		{
			this.__activateDialog();
		}
	}
}

export default GUGamePicksUpSpecialWeaponsFirstTimeDialogController