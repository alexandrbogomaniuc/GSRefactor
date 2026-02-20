import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Button from '../../../../ui/GameButton';

class BattlegroundResultButton extends Button
{
	constructor(aBaseAssetName_str, aCaptionId_str, noDefaultSound, anchorPoint, buttonType, aIsConstantlyActive_bl)
	{
		super(aBaseAssetName_str, aCaptionId_str, true, noDefaultSound, anchorPoint, buttonType);

		this._fIsConstatntntlyActive_bl = aIsConstantlyActive_bl;
		this._fBaseAssetActiveName_spr = null;

		this._addBaseAssetActiveBack(aBaseAssetName_str);
		this.handleUp();
	}

	getCaption()
	{
		return super.captionTf._text;
	}

	changeVisible(aCaptionVisible_bl)
	{
		super.captionTf.visible = aCaptionVisible_bl;
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
		if(!this._fIsConstatntntlyActive_bl)
		{
			return this._getBtnTextDectiveStyle();
		}

		return this.getBlackCaptionStyle();
	}

	getBlackCaptionStyle()
	{
		let lStyle =

		{
			fontFamily:"fnt_nm_barlow_bold",
			fontSize: 15,
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
		if(this._fIsConstatntntlyActive_bl)
		{
			return this._getBtnTextActiveStyle();
		}

		let lStyle =
		{
			fontFamily:"fnt_nm_barlow_semibold",
			fill: 0xffffff,
			dropShadow: false
		}

		return lStyle;
	}

	_updateBaseView(baseAssetName)
	{
		if (!baseAssetName)
		{
			return;
		}

		const sprite = APP.library.getSprite(baseAssetName)
		
		if (this._baseView)
		{
			this._baseView = sprite;
		}
		else
		{
			this._baseView = this.holder.addChild(sprite);
		}

		this._updateHitArea();
	}

	destroy()
	{
		super.destroy();

		this._fBaseAssetActiveName_spr = null;
	}
}

export default BattlegroundResultButton