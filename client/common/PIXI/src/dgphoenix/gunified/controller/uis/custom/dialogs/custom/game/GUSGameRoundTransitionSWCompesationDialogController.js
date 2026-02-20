import GUGameBaseDialogController from './GUGameBaseDialogController';
import GUSLobbyExternalCommunicator, { GAME_MESSAGES } from '../../../../../external/GUSLobbyExternalCommunicator';
import { APP } from '../../../../../../../unified/controller/main/globals';

class GUSGameRoundTransitionSWCompesationDialogController extends GUGameBaseDialogController
{
	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._initGUSGameRoundTransitionSWCompesationDialogController();
	}

	_initGUSGameRoundTransitionSWCompesationDialogController()
	{
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().gameRoundTransitionSWCompensationDialogView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(GUSLobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

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
			view.setMessage("TADialogCompensateSWInfo");
			view.setOkMode();
			//...message configuration
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
			case GAME_MESSAGES.ROUND_RESULT_RETURN_SW_RESPONSE:
				{
					this._onRoundResultResponse(event.data);
					break;
				}
			case GAME_MESSAGES.ROUND_RESULT_RETURN_SW_DIALOG_SHOW_VALIDATE:
				{
					this._validateDialogShow();
					break;
				}
			case GAME_MESSAGES.RESTORED_AFTER_UNREASONABLE_REQUEST:
			case GAME_MESSAGES.BACK_TO_LOBBY:
			case GAME_MESSAGES.ROUND_RESULT_SCREEN_DEACTIVATED:
				if (this.info.isActive)
				{
					this.__deactivateDialog();
				}
				break;
		}
	}

	_onRoundResultResponse(aData_obj)
	{
		let lDataWeaponsReturned_arr = aData_obj.weaponSurplus;

		if (lDataWeaponsReturned_arr && lDataWeaponsReturned_arr.length > 0)
		{
			let lPayout_num = 0;
			for (let i = 0; i < lDataWeaponsReturned_arr.length; ++i)
			{
				lPayout_num += lDataWeaponsReturned_arr[i].winBonus;
			}
			
			this.info.totalReturnedSpecialWeapons = lPayout_num; 
		}
		else
		{
			this.info.totalReturnedSpecialWeapons = 0; 
		}
	}

	_validateDialogShow()
	{
		if (!this.info.isActive)
		{
			if (this.info.totalReturnedSpecialWeapons && this.info.totalReturnedSpecialWeapons > 0)
			{
				this.__activateDialog();
			}
		}
		else
		{
			if (!this.info.totalReturnedSpecialWeapons || this.info.totalReturnedSpecialWeapons <= 0)
			{
				this.__deactivateDialog();
			}
		}
	}
}

export default GUSGameRoundTransitionSWCompesationDialogController