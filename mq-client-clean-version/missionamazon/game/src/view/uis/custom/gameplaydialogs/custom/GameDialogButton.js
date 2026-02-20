import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Button from '../../../../../ui/GameButton';
import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class GameDialogButton extends Button
{
	constructor(aBaseAssetActiveName_str, aBaseAssetDeactiveName_str, aCaptionId_str, noDefaultSound, anchorPoint, buttonType, isNeedButtonStyleUpdateOnHandle = true, isDeactiveOnlyCaptionStyle = false)
	{
		super(aBaseAssetDeactiveName_str, aCaptionId_str, true, noDefaultSound, anchorPoint, buttonType);

		this._fBaseAssetActiveName_spr = null;
		this._addBaseAssetActiveBack(aBaseAssetActiveName_str);	

		this._fIsNeedButtonStyleUpdateOnHandle_bl = isNeedButtonStyleUpdateOnHandle;

		if(isDeactiveOnlyCaptionStyle)
		{
			this.caption.assetContent.textFormat = this._getBtnTextDectiveStyle();
			this.caption.update();
		}
	}

	_addBaseAssetActiveBack(aBaseAssetActiveName_str)
	{
		this._fBaseAssetActiveName_spr = this.holder.addChildAt(APP.library.getSprite(aBaseAssetActiveName_str), 1);
		this._fBaseAssetActiveName_spr.visible = false;
	}
	
	handleOver() 
	{
		this._fBaseAssetActiveName_spr.visible = true;

		if (this.captionId)
		{
			if (this._fIsNeedButtonStyleUpdateOnHandle_bl)
			{
				this.caption.assetContent.textFormat = this._getBtnTextActiveStyle();
				this.caption.update();
			}
		}
	}

	handleOut() 
	{
		if(!this._fHolded_bln)
		{
			this._fBaseAssetActiveName_spr.visible = false;

			if (this.captionId)
			{
				if (this._fIsNeedButtonStyleUpdateOnHandle_bl)
				{
					this.caption.assetContent.textFormat = this._getBtnTextDectiveStyle();
					this.caption.update();
				}
			}
		}
	}

	handleUp(e)
	{
		super.handleUp(e);

		this._fBaseAssetActiveName_spr.visible = false;

		if (this.captionId && this._fIsNeedButtonStyleUpdateOnHandle_bl)
		{
			this.caption.assetContent.textFormat = this._getBtnTextDectiveStyle();
		}
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
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 0.5,
			dropShadowAlpha: 0.5
		}

		return lStyle;
	}

	destroy()
	{
		super.destroy();

		this._fBaseAssetActiveName_spr = null;
		this._fIsNeedButtonStyleUpdateOnHandle_bl = null;
	}
}

export default GameDialogButton