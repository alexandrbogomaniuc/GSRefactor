import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import DialogView from '../../../../view/uis/custom/dialogs/DialogView';
import DOMLayout from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/DOMLayout';
import hotkeys from '../../../../../../../common/PIXI/src/dgphoenix/unified/lib/hotkeys';

class DialogController extends SimpleUIController
{
	static get EVENT_DIALOG_ACTIVATED () { return 'onDialogActivated' };
	static get EVENT_DIALOG_DEACTIVATED () { return 'onDialogDeactivated' };
	static get EVENT_DIALOG_PRESENTED () { return 'onDialogPresented' };
	static get EVENT_PRESENTED_DIALOG_UPDATED () { return 'onDialogUpdated' };
	static get EVENT_DIALOG_DEFERRED () { return 'onDialogDeferred' };

	static get EVENT_REQUEST_CONFIRMED () { return 'onRequestConfirmed' };
	static get EVENT_REQUEST_NOT_CONFIRMED () { return 'onRequestNotConfirmed' };
	static get EVENT_ON_DLG_CUSTOM_BTN_CLICKED () { return 'onDialogCustomButtonClocked' };
	
	constructor(aOptInfo_usuii, aOptView_uo, aOptParentController_usc)
	{
		super(aOptInfo_usuii, aOptView_uo, aOptParentController_usc);

		this._dialogsController = null;
		this._dialogsInfo = null;

		this._initDialogController();
	}

	_initDialogController()
	{
		
	}

	__init ()
	{
		this._dialogsController = this.__getParentController();
		this._dialogsInfo = this._dialogsController.info;

		this._fCurDebugMessage_str = undefined;
		
		super.__init();

		this.__validate();
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
		let view = this.__fView_uo;
		
		view.on(DialogView.EVENT_ON_OK_BTN_CLICKED, this.__onDialogOkButtonClicked, this);
		view.on(DialogView.EVENT_ON_CANCEL_BTN_CLICKED, this.__onDialogCancelButtonClicked, this);
		view.on(DialogView.EVENT_ON_CUSTOM_BTN_CLICKED, this.__onDialogCustomButtonClicked, this);
		
		this.__validateViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		APP.layout.on(DOMLayout.EVENT_ON_ORIENTATION_CHANGED, this._onAppOrientationChange, this);

		this._dialogsController.on(DialogController.EVENT_DIALOG_ACTIVATED, this._onSomeDialogActivated, this);
		this._dialogsController.on(DialogController.EVENT_DIALOG_DEACTIVATED, this._onSomeDialogDeactivated, this);
	}

	//DEBUG...
	debugDeactivateDialog()
	{
		this.__deactivateDialog();
	}

	debugActivateDialog()
	{
		if (this.debugMessages && this.debugMessages.length)
		{
			let lCurDebugMessageIndex_int = 0;
			if (this.curDebugMessage !== undefined)
			{
				lCurDebugMessageIndex_int = this.debugMessages.indexOf(this.curDebugMessage);

				lCurDebugMessageIndex_int ++;
				if (lCurDebugMessageIndex_int >= this.debugMessages.length)
				{
					lCurDebugMessageIndex_int = this.debugMessages.length-1;
				}
			}

			this._fCurDebugMessage_str = this.debugMessages[lCurDebugMessageIndex_int];
		}

		this.__setAdditionalDenugActivateParams();
		
		this.__activateDialog();
	}

	get isDebugMessageRequired()
	{
		return true;
	}

	__setAdditionalDenugActivateParams()
	{

	}

	get debugMessages()
	{
		return [];
	}

	get curDebugMessage()
	{
		return this._fCurDebugMessage_str;
	}

	resetDebugMessage()
	{
		this._fCurDebugMessage_str = undefined;
	}

	get isDlgDebugCycleCompleted()
	{
		return !this.debugMessages || !this.debugMessages.length || this.debugMessages[this.debugMessages.length-1] === this.curDebugMessage;
	}

	//...DEBUG

	__initViewLevelSelfInitialization ()
	{

	}

	__isViewLevelSelfInitializationAllowed ()
	{
		return true;
	}

	//VALIDATION...
	__validateModelLevel ()
	{
		super.__validateModelLevel();
		
		this._validatePresentationState();

		var info = this.info;
		info.visible = info.isPresented;
	}

	__validateViewLevel ()
	{
		super.__validateViewLevel();
		
		this.emit(DialogController.EVENT_PRESENTED_DIALOG_UPDATED);
	}

	__validate()
	{
		super.__validate();
	}
	//...VALIDATION

	__onViewLevelSelfInitializationViewProviderAccessible()
	{
		super.__onViewLevelSelfInitializationViewProviderAccessible();
		if (this.info.isPresented)
		{
			this.__forceViewLevelSelfInitialization();
		}
	}

	_validatePresentationState ()
	{
		var info = this.info;
		this._updateDialogPresentedState(info.isActive && this._dialogsInfo.dialogIdForPresentation === info.dialogId);
	}

	_updateDialogPresentedState (aPresented_bl)
	{
		aPresented_bl = Boolean(aPresented_bl);
		var info = this.info;
		if (info.isPresented === aPresented_bl)
		{
			return;
		}

		info.isPresented = aPresented_bl;
		if (aPresented_bl)
		{
			if (!this.__fView_uo && this.__isViewLevelSelfInitializationViewProviderAccessible())
			{
				this.__forceViewLevelSelfInitialization();
			}
			
			this.emit(DialogController.EVENT_DIALOG_PRESENTED);
		}
		else if (info.isActive)
		{
			this.emit(DialogController.EVENT_DIALOG_DEFERRED);
		}
	}

	__onDialogOkButtonClicked(event)
	{
		this.emit(DialogController.EVENT_REQUEST_CONFIRMED);
	}

	__onDialogCancelButtonClicked(event)
	{
		this.emit(DialogController.EVENT_REQUEST_NOT_CONFIRMED);
		this.__deactivateDialog();
	}

	__onDialogCustomButtonClicked(event)
	{	
		this.emit(DialogController.EVENT_ON_DLG_CUSTOM_BTN_CLICKED);
	}

	__onGameDialogChangeWorldBuyInButtonClicked(event)
	{	
		this.__deactivateDialog();
	}

	//DIALOG ACTIVATION...
	_onSomeDialogActivated (aEvent_ue)
	{
		if (this.info.dialogId != aEvent_ue.dialogId)
		{
			this.__validate();
		}
	}

	_onSomeDialogDeactivated (aEvent_ue)
	{
		if (this.info.dialogId != aEvent_ue.dialogId)
		{
			this.__validate();
		}
	}

	__activateDialog ()
	{
		this.__setActiveState(true);
		this.__validate();
	}

	__deactivateDialog ()
	{
		this.__setActiveState(false);
		this.__validate();
	}

	__setActiveState (aActive_bl)
	{
		aActive_bl = Boolean(aActive_bl);

		var info = this.info;
		if (info.isActive === aActive_bl)
		{
			return;
		}

		info.isActive = aActive_bl;

		if (aActive_bl)
		{
			info.activationTime = new Date().getTime();
			this.emit(DialogController.EVENT_DIALOG_ACTIVATED, {dialogId: info.dialogId});
		}
		else
		{
			this.emit(DialogController.EVENT_DIALOG_DEACTIVATED, {dialogId: info.dialogId});
		}
	}	
	//...DIALOG ACTIVATION

	//ORIENTATION...
	_onAppOrientationChange(event)
	{
		if(!this.info.isDisabled && APP.gameAppStarted && this.view)  this.view.updateArea();
	}
	//...ORIENTATION
}

export default DialogController