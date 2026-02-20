import SimpleUIController from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import CalloutController from './CalloutController';
import CalloutsInfo from '../../../../model/uis/custom/callouts/CalloutsInfo';
import CalloutsView from '../../../../view/uis/custom/callouts/CalloutsView';
import WeaponCarrierCalloutController from './custom/WeaponCarrierCalloutController';
import TimesRunningOutController from './custom/TimesRunningOutController';


class CalloutsController extends SimpleUIController
{
	static get EVENT_CALLOUT_ACTIVATED() {return CalloutController.EVENT_CALLOUT_ACTIVATED};
	static get EVENT_CALLOUT_DEACTIVATED() {return CalloutController.EVENT_CALLOUT_DEACTIVATED};

	static _sortCalloutsByPresentationPriority (callout1, callout2)
	{
		var firstCalloutInfo = callout1.info;
		var secondCalloutInfo = callout2.info;

		var firstCalloutPriority = firstCalloutInfo.priority;
		var secondCalloutPriority = secondCalloutInfo.priority;
		var lRet_num = secondCalloutPriority - firstCalloutPriority;
		if (!lRet_num)
		{
			var firstCalloutActivationTime = firstCalloutInfo.activationTime;
			var secondCalloutActivationTime = secondCalloutInfo.activationTime;

			lRet_num = firstCalloutActivationTime - secondCalloutActivationTime;
		}
		return lRet_num;
	}

	constructor(optInfo)
	{
		super(new CalloutsInfo());

		this._calloutsControllers = null;
		this._fViewContainer_sprt = null;

		this._initCalloutsController();
	}

	initView(viewContainer)
	{
		this._fViewContainer_sprt = viewContainer;

		let view = new CalloutsView();
		this._fViewContainer_sprt.addChild(view);

		super.initView(view);
	}
	
	get viewContainer()
	{
		return this._fViewContainer_sprt;
	}

	get timesRunningOutController()
	{
		return this._timesRunningOutController;
	}

	destroy()
	{
		this._calloutsControllers = null;

		super.destroy();
	}

	_initCalloutsController()
	{
		this._calloutsControllers = [];
	}

	__init ()
	{
		super.__init();
	}

	__initControlLevel ()
	{
		super.__initControlLevel();

		var info = this.info;
		var calloutsAmount = info.calloutsCount;
		for (var i = 0; i < calloutsAmount; i++)
		{
			var calloutController = this.__getCalloutController(i);
			calloutController.init();
		}
	}

	__initViewLevel ()
	{
		super.__initViewLevel();

		var view = this.__fView_uo;
		var calloutsAmount = this.info.calloutsCount;
		for (var i = 0; i < calloutsAmount; i++)
		{
			var calloutController = this.__getCalloutController(i);
			if (calloutController.isViewLevelSelfInitializationMode)
			{
				if (!calloutController.hasView)
				{
					calloutController.initViewLevelSelfInitializationViewProvider(view);
				}
			}
			else
			{
				calloutController.initView(view.getCalloutView(i));
			}
		}
	}

	__getCalloutController (calloutId)
	{
		return this._calloutsControllers[calloutId] || this._initCalloutController(calloutId);
	}

	_initCalloutController (calloutId)
	{
		var calloutController = this.__generateCalloutController(this.info.getCalloutInfo(calloutId));
		this._calloutsControllers[calloutId] = calloutController;

		calloutController.on(CalloutController.EVENT_CALLOUT_ACTIVATED, this._onCalloutActivated, this);
		calloutController.on(CalloutController.EVENT_CALLOUT_DEACTIVATED, this._onCalloutDeactivated, this);

		return calloutController;
	}

	__generateCalloutController (calloutInfo)
	{
		var calloutController;
		var calloutId = calloutInfo.calloutId;
		var calloutInfo = calloutInfo;

		switch (calloutId)
		{
			case CalloutsInfo.CALLOUT_ID_WEAPON_CARRIER:
				calloutController = new WeaponCarrierCalloutController(calloutInfo, this);
				break;
			case CalloutsInfo.CALLOUT_ID_TIMES_RUNNING_OUT:
				calloutController = new TimesRunningOutController(calloutInfo, this);
				break;
			default:
				throw new Error(`Unsupported callout id: ${calloutId}`);
		}

		return calloutController;
	}

	_onCalloutActivated (aEvent_ue)
	{
		if(this.info.calloutIdForPresentation == undefined)
		{
			this._updateCalloutForPresentationSettings();
		}
		this.emit(aEvent_ue);
	}

	_onCalloutDeactivated (aEvent_ue)
	{
		this._updateCalloutForPresentationSettings();
		this.emit(aEvent_ue);
	}

	_updateCalloutForPresentationSettings ()
	{
		var sortedActiveCallouts = this._getActiveCalloutsWithPresentationPrioritySorting();
		var info = this.info;

		if (!sortedActiveCallouts)
		{
			info.calloutIdForPresentation = undefined;
		}
		else
		{
			info.calloutIdForPresentation = sortedActiveCallouts[0].info.calloutId;
		}
	}

	_getActiveCalloutsWithPresentationPrioritySorting ()
	{
		var activeCallouts = null;
		var calloutsAmount = this.info.calloutsCount;
		for (var i = 0; i < calloutsAmount; i++)
		{
			var calloutController = this.__getCalloutController(i);
			if (calloutController.info.isActive)
			{
				activeCallouts = activeCallouts || [];
				activeCallouts.push(calloutController);
			}
		}
		if (activeCallouts)
		{
			activeCallouts.sort(CalloutsController._sortCalloutsByPresentationPriority);
		}

		return activeCallouts;
	}

	get _timesRunningOutController()
	{
		return this.__getCalloutController(CalloutsInfo.CALLOUT_ID_TIMES_RUNNING_OUT);
	}
}

export default CalloutsController