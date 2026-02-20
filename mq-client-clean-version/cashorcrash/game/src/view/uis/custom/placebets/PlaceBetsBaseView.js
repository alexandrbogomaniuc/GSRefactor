import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';

class PlaceBetsBaseView extends SimpleUIView
{
	static get EVENT_ON_PLACE_BETS ()					{ return "EVENT_ON_PLACE_BETS"; }
	static get EVENT_ON_CANCEL_BET ()					{ return "EVENT_ON_CANCEL_BET"; }
		
	updateLayout(aLayout_rt, aIsPortraitMode_bl)
	{
		this._fContentX_num = aLayout_rt.x;
		this._fContentY_num = aLayout_rt.y;

		this._updateViewPosition(aIsPortraitMode_bl);

		if (this._fContentWidth_num !== aLayout_rt.width || this._fContentHeight_num !== aLayout_rt.height || this._fIsPortraitMode_bl !== aIsPortraitMode_bl)
		{
			this._fContentWidth_num = aLayout_rt.width;
			this._fContentHeight_num = aLayout_rt.height;
			this._fIsPortraitMode_bl = aIsPortraitMode_bl;

			if (this.uiInfo) // view is already initialized
			{
				this._updateLayoutSettings();
			}
		}
	}

	adjustLayoutSettings()
    {
        this._updateViewPosition(this._fIsPortraitMode_bl);

        this._updateLayoutSettings();
    }

	_updateViewPosition(aIsPortraitMode_bl)
    {
        let lX_num = this._fContentX_num || 0;
        let lY_num = this._fContentY_num || 0;

        this.position.set(lX_num, lY_num);
    }

	//INIT...
	constructor()
	{
		super();

		this._fContentWidth_num = undefined;
		this._fContentHeight_num = undefined;
		this._fContentX_num = undefined;
		this._fContentY_num = undefined;
		this._fIsPortraitMode_bl = false;
	}
	
	__init()
	{
		super.__init();
	}
	//...INIT

	_updateLayoutSettings()
	{
	}

	_setCurrencySymbols()
	{
		
	}

	validate()
	{
	}
}

export default PlaceBetsBaseView;