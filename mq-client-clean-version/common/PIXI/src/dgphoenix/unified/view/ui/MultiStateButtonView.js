import SimpleUIView from '../base/SimpleUIView';
import { APP } from '../../controller/main/globals';

class MultiStateButtonView extends SimpleUIView 
{
	getButtonStateView(aStateId_int)
	{
		return this._getButtonStateView(aStateId_int);
	}

	set buttonState(aStateId_int)
	{
		this._buttonState = aStateId_int;
	}

	constructor(aOptDrawSeptum_bln = true, aOptBaseScale_obj = undefined) 
	{
		super();

		this._fDrawSeptum_bln = aOptDrawSeptum_bln;
		this._fBaseScale_obj = aOptBaseScale_obj || this.__defaultBaseScale;

		this._fButtonStateViews_arr = [];

		this._initMultiStateButtonView();
	}

	_initMultiStateButtonView()
	{
	}

	_getButtonStateView(aStateId_int)
	{
		return this._fButtonStateViews_arr[aStateId_int] || (this._fButtonStateViews_arr[aStateId_int] = this._initButtonStateView(aStateId_int));
	}

	_initButtonStateView(aStateId_int)
	{
		throw new Error("Method must be overrided");
	}

	set _buttonState(aStateId_int)
	{
		for (let i = 0; i < this._fButtonStateViews_arr.length; i++) 
		{
			this._fButtonStateViews_arr[i].visible = aStateId_int === i;
		}
	}

	get __defaultBaseScale()
	{
		return (APP.isMobile ? 1.4 : 1);
	}

	destroy()
	{
		this._fButtonStateViews_arr = null;
		
		super.destroy();
	}
}

export default MultiStateButtonView;