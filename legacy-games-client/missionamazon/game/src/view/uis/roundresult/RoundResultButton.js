import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Button from '../../../ui/GameButton';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class RoundResultButton extends Button
{
	constructor(aBaseAssetActiveName_str, aBaseAssetDeactiveName_str, aCaptionId_str, noDefaultSound, anchorPoint, buttonType)
	{
		super(aBaseAssetDeactiveName_str, aCaptionId_str, true, noDefaultSound, anchorPoint, buttonType);

		this._fBaseAssetActiveName_spr = null;
		this._addBaseAssetActiveBack(aBaseAssetActiveName_str);	
	}

	_addBaseAssetActiveBack(aBaseAssetActiveName_str)
	{
		this._fBaseAssetActiveName_spr = this.holder.addChildAt(APP.library.getSprite(aBaseAssetActiveName_str), 1);
		this._fBaseAssetActiveName_spr.visible = false;
	}

	handleOver() 
	{
		this._fBaseAssetActiveName_spr.visible = true;

		this.caption.assetContent.textFormat = this._getBtnTextActiveStyle();
	}

	handleOut() 
	{
		if(!this._fHolded_bln)
		{
			this._fBaseAssetActiveName_spr.visible = false;

			this.caption.assetContent.textFormat = this._getBtnTextDectiveStyle();
		}
	}

	handleUp(e)
	{
		super.handleUp(e);

		this._fBaseAssetActiveName_spr.visible = false;
		this.caption.assetContent.textFormat = this._getBtnTextDectiveStyle();
	}

	_getBtnTextActiveStyle()
	{
		let lStyle = 
		{
			fill: 0x000000,
			dropShadow: true,
			dropShadowColor: 0xffffff,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 0.5,
			dropShadowAlpha: 0.5
		}

		return lStyle;
	}

	_getBtnTextDectiveStyle()
	{
		let lStyle = 
		{
			fill: 0xffffff,
			dropShadow: false
		}

		return lStyle;
	}

	destroy()
	{
		super.destroy();

		this._fBaseAssetActiveName_spr = null;
	}
}

export default RoundResultButton