import SimpleUIInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';
import WeaponCarrierCalloutInfo from './custom/WeaponCarrierCalloutInfo';
import TimesRunningOutInfo from './custom/TimesRunningOutInfo';


let calloutIdCounter = 0;

let CALLOUT_ID_WEAPON_CARRIER						= calloutIdCounter++; //0
let CALLOUT_ID_TIMES_RUNNING_OUT					= calloutIdCounter++; //1

let CALLOUTS_AMOUNT = calloutIdCounter;

class CalloutsInfo extends SimpleUIInfo
{
	static get CALLOUT_ID_WEAPON_CARRIER() 			{ return CALLOUT_ID_WEAPON_CARRIER;}
	static get CALLOUT_ID_TIMES_RUNNING_OUT() 		{ return CALLOUT_ID_TIMES_RUNNING_OUT;}

	constructor()
	{
		super();

		this._calloutsInfos = null;
		this._calloutIdForPresentation = undefined;

		this._initCalloutsInfo();
	}

	destroy()
	{
		this._calloutsInfos = null; 

		super.destroy();
	}

	getCalloutInfo (calloutId)
	{
		return this._getCalloutInfo(calloutId);
	}

	get calloutsCount ()
	{
		return CALLOUTS_AMOUNT;
	}

	get calloutIdForPresentation ()
	{
		return this._calloutIdForPresentation;
	}

	set calloutIdForPresentation (calloutId)
	{
		this._calloutIdForPresentation = calloutId;
	}

	hasActiveCalloutWithId(aCalloutId_int)
	{
		var calloutsAmount = this.calloutsCount;
		for (var i = 0; i < calloutsAmount; i++)
		{
			if (
				this._getCalloutInfo(i).isActive &&
				aCalloutId_int === this._getCalloutInfo(i).calloutId
				)
			{
				return true;
			}
		}
		return false;
	}

	get hasActiveCallout ()
	{
		var calloutsAmount = this.calloutsCount;
		for (var i = 0; i < calloutsAmount; i++)
		{
			if (this._getCalloutInfo(i).isActive)
			{
				return true;
			}
		}
		return false;
	}

	_initCalloutsInfo()
	{
		this._calloutsInfos = [];
	}

	_getCalloutInfo (calloutId)
	{
		return this._calloutsInfos[calloutId] || this._initCalloutInfo(calloutId);
	}

	_initCalloutInfo (calloutId)
	{
		var calloutInfo = this.__generateCalloutInfo(calloutId);

		this._calloutsInfos[calloutId] = calloutInfo;

		return calloutInfo;
	}

	__generateCalloutInfo (calloutId)
	{
		var calloutInfo = null;
		switch (calloutId)
		{
			case CalloutsInfo.CALLOUT_ID_WEAPON_CARRIER:
				calloutInfo = new WeaponCarrierCalloutInfo(calloutId, 0);
				break;
			case CalloutsInfo.CALLOUT_ID_TIMES_RUNNING_OUT:
				calloutInfo = new TimesRunningOutInfo(calloutId, 1);
				break;
			default:
				throw new Error (`Unsupported callout id: ${calloutId}`);
		}
		return calloutInfo;
	}
}

export default CalloutsInfo