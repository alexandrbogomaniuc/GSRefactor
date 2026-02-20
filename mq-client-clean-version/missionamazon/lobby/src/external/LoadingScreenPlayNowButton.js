import Button from '../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class LoadingScreenPlayNowButton extends Button
{
	constructor(aIsEnabled_bl)
	{
		let lBaseAssetName_str = aIsEnabled_bl ? "preloader/play_now_button_base_enabled" : "preloader/play_now_button_base_disabled";

		super(lBaseAssetName_str, "TAPreloderPlayNow", true, true, 0, undefined);

		if(aIsEnabled_bl)
		{
			this.hitArea = new PIXI.Rectangle(-80, -18.5 - 5, 160, 37);
			this._addBaseAssetActiveBack("preloader/play_now_button_base_selected");
		}
		else
		{
			this.alpha = 0.5;
			this.interactive = false;
		}

	}


	_addBaseAssetActiveBack(aBaseAssetActiveName_str)
	{
		this._fBaseAssetActiveName_spr = this.holder.addChildAt(APP.library.getSprite(aBaseAssetActiveName_str), 1);
		this._fBaseAssetActiveName_spr.visible = false;
	}

	handleOver() 
	{
		if(this._fBaseAssetActiveName_spr)
		{
			this._fBaseAssetActiveName_spr.visible = true;
		}
	}

	handleOut() 
	{
		if(
			this._fBaseAssetActiveName_spr &&
			!this._fHolded_bln
			)
		{
			this._fBaseAssetActiveName_spr.visible = false;
		}
	}

	handleUp(e)
	{
		super.handleUp(e);

		if(this._fBaseAssetActiveName_spr)
		{
			this._fBaseAssetActiveName_spr.visible = false;
		}
	}

	destroy()
	{
		super.destroy();

		this._fBaseAssetActiveName_spr = null;
	}

}

export default LoadingScreenPlayNowButton