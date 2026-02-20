import GameBaseDialogController from './GameBaseDialogController';
import LobbyExternalCommunicator from '../../../../../../external/LobbyExternalCommunicator';
import LobbyAPP from '../../../../../../LobbyAPP';
import LobbyScreen from '../../../../../../main/LobbyScreen';
import { APP } from '../../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import {GAME_MESSAGES} from '../../../../../../../../../common/PIXI/src/dgphoenix/gunified/controller/external/GUSLobbyExternalCommunicator';

class GameRoundTransitionSWCompesationDialogController extends GameBaseDialogController
{
	static get EVENT_DIALOG_PRESENTED() 			{return GameBaseDialogController.EVENT_DIALOG_PRESENTED};
	static get EVENT_PRESENTED_DIALOG_UPDATED() 	{return GameBaseDialogController.EVENT_PRESENTED_DIALOG_UPDATED};

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, parentController);

		this._initGameRoundTransitionSWCompesationDialogController();
	}

	_initGameRoundTransitionSWCompesationDialogController()
	{
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().roundTransitionSWCompensationDialogView;
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		let externalCommunicator = APP.externalCommunicator;
		externalCommunicator.on(LobbyExternalCommunicator.GAME_MESSAGE_RECEIVED, this._onGameMessageReceived, this);

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

	__activateDialog()
	{
		this._activisionTime = Date.now();
		super.__activateDialog();
	}

	__deactivateDialog()
	{
		if (!this.info.isActive) return;
		const now = Date.now();
		const difference = now - this._activisionTime;
		if(difference < 3000)
		{
			const waitingPeriod = 3000 - difference;
			setTimeout(()=>{
				super.__deactivateDialog();
			}, waitingPeriod);
		}else{
			super.__deactivateDialog();
		}
	}
}

export default GameRoundTransitionSWCompesationDialogController