import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import CalloutView from '../../../../view/uis/custom/callouts/CalloutView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../../../../main/GameScreen';

class CalloutController extends SimpleUIController
{
	static get EVENT_CALLOUT_ACTIVATED () { return 'onCalloutActivated' };
	static get EVENT_CALLOUT_DEACTIVATED () { return 'onCalloutDeactivated' };
	
	constructor(aOptInfo_usuii, aOptView_uo, aOptParentController_usc)
	{
		super(aOptInfo_usuii, aOptView_uo, aOptParentController_usc);

		this._calloutsController = null;
		this._calloutsInfo = null;
	}

	__init ()
	{
		this._calloutsController = this.__getParentController();
		this._calloutsInfo = this._calloutsController.info;
		
		super.__init();

		this.__validate();
	}

	__initViewLevel ()
	{
		super.__initViewLevel();

		let view = this.__fView_uo;
		view.on(CalloutView.EVENT_ON_PRESENTATION_ENDED, this._onPresentationEnded, this);

		this.__validateViewLevel();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		this._calloutsController.on(CalloutController.EVENT_CALLOUT_ACTIVATED, this._onSomeCalloutActivated, this);
		this._calloutsController.on(CalloutController.EVENT_CALLOUT_DEACTIVATED, this._onSomeCalloutDeactivated, this);

		APP.gameScreen.on(GameScreen.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomFieldCleared, this);
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

	_validatePresentationState()
	{
		var info = this.info;
		this._updateCalloutPresentedState(info.isActive && this._calloutsInfo.calloutIdForPresentation === info.calloutId);
	}

	_updateCalloutPresentedState(aPresented_bl)
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
			
			APP.soundsController.play("mq_dragonstone_mini_notification_banner");
			this._startCallout();
		}
	}

	_onPresentationEnded ()
	{
		this.__deactivateCallout();
	}

	//CALLOUT ACTIVATION...
	_onSomeCalloutActivated (aEvent_ue)
	{
		if (this.info.calloutId != aEvent_ue.calloutId)
		{
			this.__validate();
		}
	}

	_onSomeCalloutDeactivated (aEvent_ue)
	{
		if (this.info.calloutId != aEvent_ue.calloutId)
		{
			this.__validate();
		}
	}

	__activateCallout ()
	{
		if (APP.gameScreen.isPaused)
		{
			return;
		}

		this.__setActiveState(true);
		this.__validate();
	}

	__deactivateCallout ()
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
			this.emit(CalloutController.EVENT_CALLOUT_ACTIVATED, {calloutId: info.calloutId});
		}
		else
		{
			this.emit(CalloutController.EVENT_CALLOUT_DEACTIVATED, {calloutId: info.calloutId});
		}
	}	
	//...CALLOUT ACTIVATION

	_onRoomFieldCleared()
	{
		this.__deactivateCallout();
	}
}

export default CalloutController