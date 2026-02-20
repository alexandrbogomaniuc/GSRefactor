import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import CommonButton from '../../../../../ui/CommonButton';

class SelectableButton extends CommonButton
{
	set activated(aVal_bln)
	{
		if (aVal_bln)
		{
			this._activated();
		}
		else
		{
			this._deactivated();
		}
	}

	constructor(aBaseAssetName_str, aCaptionId_str, aOptDrawSeptum_bln, aOptScale, aIsBlackAndWhiteButton_bl)
	{
		super(aBaseAssetName_str, aCaptionId_str, aOptDrawSeptum_bln, aOptScale, aIsBlackAndWhiteButton_bl);

		if (aCaptionId_str && APP.isMobile)
		{
			this.caption.position.set(0, 22);
		}
	}

	_activated()
	{
		this._fSelectedBack_grphc.visible = true;
		if (this.caption) this.caption.assetContent.tint = 0x000000;
		this._baseView.tint = 0x000000;

		this.interactive = false;
		this.buttonMode = false;
	}

	_deactivated()
	{
		this._fSelectedBack_grphc.visible = false;
		if (this.caption) this.caption.assetContent.tint = 0xffffff;
		this._baseView.tint = 0xffffff;

		if (this._enabled)
		{
			this.interactive = true;
			this.buttonMode = this.cursorPointer;
		}
	}

	_updateBaseView(aBaseAssetName_str)
	{
		let lIsMobile_bln = APP.isMobile;
		let lArea_obj = {
			x:		lIsMobile_bln ? -43 : -18,
			y:		lIsMobile_bln ? -26 : -14,
			width:	lIsMobile_bln ?  86 :  36,
			height:	lIsMobile_bln ?  60 :  28
		};

		this._fSelectedBack_grphc = this.holder.addChild(new PIXI.Graphics());
		this._fSelectedBack_grphc.beginFill(0xfccc32).drawRect(lArea_obj.x, lArea_obj.y, lArea_obj.width, lArea_obj.height).endFill();
		this._fSelectedBack_grphc.visible = false;

		this.setHitArea(new PIXI.Rectangle(lArea_obj.x, lArea_obj.y, lArea_obj.width, lArea_obj.height));

		super._updateBaseView(aBaseAssetName_str);
	}

	handleDown()
	{
		this._tryPlaySound();
	}

	destroy()
	{
		super.destroy();

		this._fSelectedBack_grphc = null;
	}
}

export default SelectableButton