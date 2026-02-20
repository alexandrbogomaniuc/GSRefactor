import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import CalloutsInfo from '../../../../model/uis/custom/callouts/CalloutsInfo';
import CalloutView from './CalloutView';


class CalloutsView extends SimpleUIView
{
	constructor()
	{
		super();

		this.calloutsViews = null;

		this._initCalloutsView();
	}

	destroy()
	{
		this.calloutsViews = null;

		super.destroy();
	}

	get weaponCarrierCalloutView()
	{
		return this._weaponCarrierCalloutView;
	}

	get timesRunningOutView()
	{
		return this._timesRunningOutView;
	}

	getCalloutView (calloutId)
	{
		return this.__getCalloutView(calloutId);
	}

	_initCalloutsView()
	{
		this.calloutsViews = [];
	}

	__getCalloutView (calloutId)
	{
		return this.calloutsViews[calloutId] || this._initCalloutView(calloutId);
	}

	_initCalloutView (calloutId)
	{
		var calloutView = this.__generateCalloutView(calloutId);

		this.calloutsViews[calloutId] = calloutView;
		this.addChild(calloutView);

		return calloutView;
	}

	__generateCalloutView (calloutId)
	{
		var calloutView;
		switch (calloutId)
		{
			case CalloutsInfo.CALLOUT_ID_WEAPON_CARRIER:
			case CalloutsInfo.CALLOUT_ID_TIMES_RUNNING_OUT:
				calloutView = new CalloutView();
				break;
			default:
				throw new Error (`Unsupported callout id: ${calloutId}`);
		}
		return calloutView;
	}

	get _weaponCarrierCalloutView()
	{
		return this.__getCalloutView(CalloutsInfo.CALLOUT_ID_WEAPON_CARRIER);
	}

	get _timesRunningOutView()
	{
		return this.__getCalloutView(CalloutsInfo.CALLOUT_ID_TIMES_RUNNING_OUT);
	}
}

export default CalloutsView;