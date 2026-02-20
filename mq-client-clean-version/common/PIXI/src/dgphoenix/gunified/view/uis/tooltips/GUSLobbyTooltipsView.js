import SimpleUIView from '../../../../unified/view/base/SimpleUIView';
import { APP } from '../../../../unified/controller/main/globals';
import GUSTooltipView from './GUSTooltipView';

class GUSLobbyTooltipsView extends SimpleUIView
{
	static get EVENT_ON_TIP_SHOWN()		{ return GUSTooltipView.EVENT_ON_TIP_SHOWN; }
	static get EVENT_ON_TIP_HIDED()		{ return GUSTooltipView.EVENT_ON_TIP_HIDED; }
	static get EVENT_ON_TIPS_ENDED()	{ return "onTipsEnded"; }

	static get ID_SELECT_ROOM()			{ return 0; }
	static get DEFAULT_TIP_DURATION()	{ return 3000; } //milliseconds

	startTooltips()
	{
		this._startTooltips();
	}

	stopTooltips()
	{
		this._stopTooltips();
	}

	//INIT...
	constructor()
	{
		super();

		this._fTips_arr = [];
		this._fCurrentTooltipId_num = null;
		this._fNextTooltipTimer_tmr = null;

		this._init();
	}

	_init()
	{
	}

	_addTooltip(aPos_obj, aTAsset_str, aArrowDir_str, aId_num, aOptDurationMs_num = GUSLobbyTooltipsView.DEFAULT_TIP_DURATION)
	{
		let lTooltip_tv = this.__provideTooltipViewInstance(aTAsset_str, aId_num, aArrowDir_str, aOptDurationMs_num);
		lTooltip_tv.position.set(aPos_obj.x, aPos_obj.y);
		lTooltip_tv.visible = false;

		this.addChild(lTooltip_tv);

		this._fTips_arr.push(lTooltip_tv);

		lTooltip_tv.on(GUSTooltipView.EVENT_ON_TIP_SHOWN, this._onTooltipShown, this);
		lTooltip_tv.on(GUSTooltipView.EVENT_ON_TIP_HIDED, this._onTooltipHided, this);
	}

	__provideTooltipViewInstance(aTAsset_str, aId_num, aArrowDir_str, aOptDurationMs_num)
	{
		return new GUSTooltipView(aTAsset_str, aId_num, aArrowDir_str, aOptDurationMs_num);
	}
	//...INIT

	_startTooltips()
	{
		if (this._fTips_arr && this._fTips_arr.length > 0)
		{
			this._fCurrentTooltipId_num = 0;
			this._fTips_arr[this._fCurrentTooltipId_num].showTip();
		}
	}

	_stopTooltips()
	{
		if (this._fTips_arr && this._fTips_arr.length > 0 && this._fCurrentTooltipId_num !== null)
		{
			this._fTips_arr[this._fCurrentTooltipId_num].hideTip();
			this._fCurrentTooltipId_num = null;
		}
	}

	_onTooltipShown(aEvent_obj)
	{
		let lTipId_num = aEvent_obj.id;

		this.emit(GUSLobbyTooltipsView.EVENT_ON_TIP_SHOWN, { id: lTipId_num });
	}

	_onTooltipHided(aEvent_obj)
	{
		let lTipId_num = aEvent_obj.id;

		this.emit(GUSLobbyTooltipsView.EVENT_ON_TIP_HIDED, { id: lTipId_num });

		this._tryNextTooltip();
	}

	_tryNextTooltip()
	{
		++this._fCurrentTooltipId_num;

		if (this._fTips_arr[this._fCurrentTooltipId_num])
		{
			this._fTips_arr[this._fCurrentTooltipId_num].showTip();
		}
		else
		{
			this._fCurrentTooltipId_num = null;
			this.emit(GUSLobbyTooltipsView.EVENT_ON_TIPS_ENDED);
		}
	}

	destroy()
	{
		for (let i = 0; i < this._fTips_arr.length; ++i)
		{
			this._fTips_arr[i].off(GUSTooltipView.EVENT_ON_TIP_SHOWN, this._onTooltipShown, this);
			this._fTips_arr[i].off(GUSTooltipView.EVENT_ON_TIP_HIDED, this._onTooltipHided, this);
		}

		super.destroy();

		this._fTips_arr = null;
		this._fCurrentTooltipId_num = null;
		this._fNextTooltipTimer_tmr = null;
	}
}

export default GUSLobbyTooltipsView;