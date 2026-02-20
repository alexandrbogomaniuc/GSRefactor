import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import BottomPanelInfo from '../../../../model/uis/custom/bottom_panel/BottomPanelInfo';
import BottomPanelView from '../../../../view/uis/custom/bottom_panel/BottomPanelView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import BalanceController from '../../../main/BalanceController';
import RoundsHistoryController from '../../../gameplay/RoundsHistoryController';
import GameSoundButtonController from '../secondary/GameSoundButtonController';
import CrashAPP from '../../../../CrashAPP';
import DOMLayout from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/DOMLayout';
import RoundDetailsController from '../RoundDetailsController';
import DialogsController from '../../custom/dialogs/DialogsController';
import DialogsInfo from '../../../../model/uis/custom/dialogs/DialogsInfo';

class BottomPanelController extends SimpleUIController
{
	static get EVENT_HOME_BUTTON_CLICKED()			{ return BottomPanelView.EVENT_HOME_BUTTON_CLICKED; }
	static get EVENT_HISTORY_BUTTON_CLICKED()		{ return BottomPanelView.EVENT_HISTORY_BUTTON_CLICKED; }
	static get EVENT_ROUND_HISTORY_ITEM_CLICKED()	{ return BottomPanelView.EVENT_ROUND_HISTORY_ITEM_CLICKED; }
	static get EVENT_SETTINGS_BUTTON_CLICKED()		{ return BottomPanelView.EVENT_SETTINGS_BUTTON_CLICKED; }
	static get EVENT_INFO_BUTTON_CLICKED()			{ return BottomPanelView.EVENT_INFO_BUTTON_CLICKED; }
	static get EVENT_BACK_BUTTON_CLICKED()			{ return BottomPanelView.EVENT_BACK_BUTTON_CLICKED; }

	//INTERFACE...
	get soundButtonController()
	{
		return this._getSoundButtonController();
	}
	//...INTERFACE

	//INIT...
	constructor(aOptInfo_suii, aOptView_suiv)
	{
		super(aOptInfo_suii || new BottomPanelInfo(), aOptView_suiv || new BottomPanelView());

		this._fSoundButtonController_sbc = null;
		this._fBalanceController_bc = null;
		this._fRoundsHistoryController_rshc = null;
		this._fRoundDetailsController_rdsc = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let lInfo_bpi = this.info;
		let lAppParamsInfo_apppi = APP.appParamsInfo;
		if (lAppParamsInfo_apppi.homeFuncName)
		{
			lInfo_bpi.homeCallback = lAppParamsInfo_apppi.homeFuncName;
		}

		if (lAppParamsInfo_apppi.historyFunName)
		{
			lInfo_bpi.historyCallback = lAppParamsInfo_apppi.historyFunName;
		}

		if (lAppParamsInfo_apppi.timerFrequency)
		{
			lInfo_bpi.timerFrequency = lAppParamsInfo_apppi.timerFrequency;
		}

		if (lAppParamsInfo_apppi.timerOffset)
		{
			lInfo_bpi.timerOffset = lAppParamsInfo_apppi.timerOffset;
		}

		this._fBalanceController_bc = APP.gameController.balanceController;
		this._fRoundsHistoryController_rshc = APP.gameController.roundsHistoryController;
		this._fRoundDetailsController_rdsc = APP.gameController.roundDetailsController;
		this._getSoundButtonController().init();
		this._addEventListeners();
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		let lView_bpv = this.view;
		
		lView_bpv.on(BottomPanelView.EVENT_HISTORY_BUTTON_CLICKED, this.emit, this);
		lView_bpv.on(BottomPanelView.EVENT_HOME_BUTTON_CLICKED, this.emit, this);
		lView_bpv.on(BottomPanelView.EVENT_ROUND_HISTORY_ITEM_CLICKED, this.emit, this);
		lView_bpv.on(BottomPanelView.EVENT_SETTINGS_BUTTON_CLICKED, this.emit, this);
		lView_bpv.on(BottomPanelView.EVENT_INFO_BUTTON_CLICKED, this.emit, this);
		lView_bpv.on(BottomPanelView.EVENT_BACK_BUTTON_CLICKED, this.emit, this);
		
		lView_bpv.updateBalance(this._fBalanceController_bc.info.balance);
		this._getSoundButtonController().initView(lView_bpv.soundButtonView);
	}
	//...INIT

	_getSoundButtonController()
	{
		return this._fSoundButtonController_sbc || (this._fSoundButtonController_sbc = new GameSoundButtonController());
	}

	_addEventListeners()
	{
		APP.layout.on(DOMLayout.EVENT_ON_ORIENTATION_CHANGED, this._onAppOrientationChange, this);

		APP.on(CrashAPP.EVENT_ON_CURRENCY_INFO_UPDATED, this._onCurrencyInfoUpdated, this);
		APP.on(CrashAPP.EVENT_ON_TICK_TIME, this._onTickTime, this);

		APP.dialogsController.on(DialogsController.EVENT_DIALOG_ACTIVATED, this._onDialogActivated, this);
		APP.dialogsController.on(DialogsController.EVENT_DIALOG_DEACTIVATED, this._onDialogDeactivated, this);

		this._fBalanceController_bc.on(BalanceController.EVENT_ON_BALANCE_UPDATED, this._onBalanceValueUpdated, this);
		this._fRoundsHistoryController_rshc.on(RoundsHistoryController.EVENT_ON_ROUNDS_HISTORY_UPDATED, this._onRoundsHistoryUpdated, this);
		this._fRoundDetailsController_rdsc.on(RoundDetailsController.EVENT_ON_ROUND_DETAILD_OPENED, this._onRoundDetailsOpened, this);
		this._fRoundDetailsController_rdsc.on(RoundDetailsController.EVENT_ON_ROUND_DETAILD_CLOSED, this._onRoundDetaildClosed, this);
	}

	_onTickTime(event)
	{
		if (
				this.view
				&& (
						!this.view.appliedTimeDefined
						|| APP.appClientServerTime - this.view.appliedTime > 5000
					)
			)
		{
			this.view.syncTime(APP.appClientServerTime);
		}
		
	}

	_onCurrencyInfoUpdated(event)
	{
		this._updateBalanceIndicatorValue();
	}

	_onDialogActivated(event)
	{
		if(APP.isCAFMode && (event.dialogId == DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOOM_MANAGER || DialogsInfo.DIALOG_ID_BATTLEGROUND_CAF_ROOOM_GUEST)) return;
		this.view && this.view.disableWinBlock();
		this.view && this.view.disableInterfaceButtons();
	}

	_onDialogDeactivated(event)
	{
		this.view && this.view.enableWinBlock();
		this.view && this.view.enableInterfaceButtons();
	}

	_onBalanceValueUpdated(event)
	{
		this._updateBalanceIndicatorValue();
	}

	_onRoundsHistoryUpdated(event)
	{
		let lRoundsHistoryInfo_rshi = this._fRoundsHistoryController_rshc.info;
		if (lRoundsHistoryInfo_rshi.isMultHistoryDefined)
		{
			let lMultipliers_num_arr = [];
			for (let i = 0; i < lRoundsHistoryInfo_rshi.multHistorySize; i++)
			{
				lMultipliers_num_arr.push(lRoundsHistoryInfo_rshi.getMultiplierById(i));
			}
			this.view && this.view.updateWinsLog(lMultipliers_num_arr);
			
			this._updateCurrentRoundDetailsId();
		}
	}

	_onRoundDetailsOpened(event)
	{
		this._updateCurrentRoundDetailsId();
	}

	_onRoundDetaildClosed(event)
	{
		this.view && this.view.updateWinsLogAim();
	}

	_updateCurrentRoundDetailsId()
	{
		let lCurrentRoundDetailsId_int = this._fRoundsHistoryController_rshc.info.getIdByRoundId(this._fRoundDetailsController_rdsc.info.currentRoundId);
		if (lCurrentRoundDetailsId_int !== undefined)
		{
			this.view && this.view.updateWinsLogAim(lCurrentRoundDetailsId_int);
		}
	}

	_updateBalanceIndicatorValue()
	{
		this.view && this.view.updateBalance(this._fBalanceController_bc.info.balance);
	}

	//ORIENTATION...
	_onAppOrientationChange(event)
	{
		this.view.updateArea();
	}
	//...ORIENTATION
}

export default BottomPanelController