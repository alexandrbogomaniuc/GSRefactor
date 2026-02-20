import SimpleUIInfo from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/uis/SimpleUIInfo';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class BottomPanelInfo extends SimpleUIInfo
{
	constructor()
	{
		super();

		this._init();
	}

	set homeCallback(aValue_str)
	{
		this._fHomeCallbackUrl_str = aValue_str;
	}

	get homeCallback()
	{
		return this._fHomeCallbackUrl_str;
	}

	get isHomeButtonRequired()
	{
		return this._fHomeCallbackUrl_str !== undefined;
	}

	set historyCallback(aValue_str)
	{
		this._fHistoryCallbackUrl_str = aValue_str;
	}

	get historyCallback()
	{
		return this._fHistoryCallbackUrl_str;
	}

	get isHistoryButtonRequired()
	{
		return this._fHistoryCallbackUrl_str !== undefined;
	}

	set timerFrequency(aValue_num)
	{
		this._fTimerFrequency_num = aValue_num;
	}

	get timerFrequency()
	{
		return this._fTimerFrequency_num;
	}

	set timerOffset(aValue_num)
	{
		this._fTimerOffset_num = aValue_num;
	}

	set timeFromServer(aValue_num)
	{
		this._fTimeFromServer_num = aValue_num;
	}

	get timeFromServer()
	{
		return this._fTimeFromServer_num;
	}

	get isTimeIndicatorRequired()
	{
		return APP.appParamsInfo.timerFrequency !== undefined;
	}

	_init ()
	{
		this._fHomeCallbackUrl_str = undefined;
		this._fHistoryCallbackUrl_str = undefined;
		this._fTimerFrequency_num = undefined;
		this._fTimerOffset_num = undefined;
		this._fTimeFromServer_num = undefined;
	}

	destroy()
	{
		super.destroy();
		
		this._fHomeCallbackUrl_str = undefined;
		this._fHistoryCallbackUrl_str = undefined;
		this._fTimerFrequency_num = undefined;
		this._fTimerOffset_num = undefined;
		this._fTimeFromServer_num = undefined;
	}
}

export default BottomPanelInfo