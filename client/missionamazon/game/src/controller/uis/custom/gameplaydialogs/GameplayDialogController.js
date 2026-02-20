import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayDialogView from '../../../../view/uis/custom/gameplaydialogs/GameplayDialogView';

class GameplayDialogController extends SimpleUIController
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
		
		super.__init();

		this.__validate();
	}

	__initViewLevel ()
	{
		super.__initViewLevel();
		let view = this.__fView_uo;
		
		view.on(GameplayDialogView.EVENT_ON_OK_BTN_CLICKED, this.__onDialogOkButtonClicked, this);
		view.on(GameplayDialogView.EVENT_ON_CANCEL_BTN_CLICKED, this.__onDialogCancelButtonClicked, this);
		view.on(GameplayDialogView.EVENT_ON_CUSTOM_BTN_CLICKED, this.__onDialogCustomButtonClicked, this);
		
		this.__validateViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		this._dialogsController.on(GameplayDialogController.EVENT_DIALOG_ACTIVATED, this._onSomeDialogActivated, this);
		this._dialogsController.on(GameplayDialogController.EVENT_DIALOG_DEACTIVATED, this._onSomeDialogDeactivated, this);
	}

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
		
		this.emit(GameplayDialogController.EVENT_PRESENTED_DIALOG_UPDATED);
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
			
			this.emit(GameplayDialogController.EVENT_DIALOG_PRESENTED);
		}
		else if (info.isActive)
		{
			this.emit(GameplayDialogController.EVENT_DIALOG_DEFERRED);
		}
	}

	__onDialogOkButtonClicked(event)
	{
		this.emit(GameplayDialogController.EVENT_REQUEST_CONFIRMED);
	}

	__onDialogCancelButtonClicked(event)
	{
		this.emit(GameplayDialogController.EVENT_REQUEST_NOT_CONFIRMED);
		this.__deactivateDialog();
	}

	__onDialogCustomButtonClicked(event)
	{	
		this.emit(GameplayDialogController.EVENT_ON_DLG_CUSTOM_BTN_CLICKED);
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
			this.emit(GameplayDialogController.EVENT_DIALOG_ACTIVATED, {dialogId: info.dialogId});
		}
		else
		{
			this.emit(GameplayDialogController.EVENT_DIALOG_DEACTIVATED, {dialogId: info.dialogId});
		}
	}	
	//...DIALOG ACTIVATION
}

export default GameplayDialogController