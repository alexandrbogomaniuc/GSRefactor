import DialogController from '../DialogController';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import BottomPanelController from '../../bottom_panel/BottomPanelController';

class LeaveRoomDialogController extends DialogController
{
	static get EVENT_DIALOG_PRESENTED () { return DialogController.EVENT_DIALOG_PRESENTED };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return DialogController.EVENT_PRESENTED_DIALOG_UPDATED };

	constructor(aOptInfo_usuii, parentController)
	{
		super(aOptInfo_usuii, undefined, parentController);

		this._initLeaveRoomDialogController();
	}

	_initLeaveRoomDialogController()
	{	
	}

	__init ()
	{
		super.__init();
	}

	__getExternalViewForSelfInitialization()
	{
		return this.__getViewLevelSelfInitializationViewProvider().leaveRoomDialogView;
	}

	__initModelLevel()
	{
		super.__initModelLevel();
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		this._fGamePlayersController_gpsc = APP.gameController.gameplayController.gamePlayersController;

		APP.gameController.bottomPanelController.on(BottomPanelController.EVENT_HOME_BUTTON_CLICKED, this._onHomeBtnClicked, this);
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
	}

	__validateViewLevel ()
	{
		var info = this.info;
		if (info.isActive)
		{
			var view = this.__fView_uo;

			view.setMessage("TADialogLeaveRoomActiveBets");
			view.setOkCancelMode();

			view.okButton.setYesCaption();
			view.cancelButton.setNoCaption();
			
		}

		super.__validateViewLevel();
	}
	//...VALIDATION

	__activateDialog ()
	{
		super.__activateDialog();
	}

	__onDialogOkButtonClicked(event)
	{
		super.__onDialogOkButtonClicked(event);

		this.__deactivateDialog();
	}

	_onHomeBtnClicked(event)
	{
		let lGamePlayersInfo_gpsi = this._fGamePlayersController_gpsc.info;
		let lMasterActiveBets_bi_arr = lGamePlayersInfo_gpsi.isMasterSeatDefined ? lGamePlayersInfo_gpsi.masterPlayerInfo.activeBets : null;
		if (lMasterActiveBets_bi_arr && !!lMasterActiveBets_bi_arr.length)
		{
			this.__activateDialog();
		}
	}
}

export default LeaveRoomDialogController