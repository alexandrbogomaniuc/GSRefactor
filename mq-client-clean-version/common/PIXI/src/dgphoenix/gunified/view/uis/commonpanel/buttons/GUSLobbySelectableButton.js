import GUSLobbyCommonPanelButton from './GUSLobbyCommonPanelButton';
import { APP } from '../../../../../unified/controller/main/globals';

class GUSLobbySelectableButton extends GUSLobbyCommonPanelButton
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

	constructor(aBaseAssetName_str, aCaptionId_str, aOptDrawSeptum_bln, aOptScale, aIsBlackAndWhiteButton_bl = false)
	{
		super(aBaseAssetName_str, aCaptionId_str, aOptDrawSeptum_bln, aOptScale, aIsBlackAndWhiteButton_bl);

		if (aCaptionId_str && APP.isMobile)
		{
			let lPos_p = this.__captionPosition;
			this.caption.position.set(lPos_p.x, lPos_p.y);
		}
	}

	get __captionPosition()
	{
		return new PIXI.Point(0, 22);
	}

	_activated()
	{
		this._fSelectedBack_grphc.visible = true;
		if (this.caption) this.caption.assetContent.tint = this.__activatedTintColor;
		this._baseView.tint = this.__activatedTintColor;

		this.interactive = false;
		this.buttonMode = false;
	}

	get __activatedTintColor()
	{
		return 0x000000;
	}

	_deactivated()
	{
		this._fSelectedBack_grphc.visible = false;
		if (this.caption) this.caption.assetContent.tint = this.__deactivatedTintColor;
		this._baseView.tint = this.__deactivatedTintColor;

		if (this._enabled)
		{
			this.interactive = true;
			this.buttonMode = this.cursorPointer;
		}
	}

	get __deactivatedTintColor()
	{
		return 0xffffff;
	}

	_updateBaseView(aBaseAssetName_str)
	{
		let lArea_obj = this.__selectionArea;

		this._fSelectedBack_grphc = this.holder.addChild(new PIXI.Graphics());
		this._fSelectedBack_grphc.beginFill(0xfccc32).drawRect(lArea_obj.x, lArea_obj.y, lArea_obj.width, lArea_obj.height).endFill();
		this._fSelectedBack_grphc.visible = false;

		this.setHitArea(new PIXI.Rectangle(lArea_obj.x, lArea_obj.y, lArea_obj.width, lArea_obj.height));

		super._updateBaseView(aBaseAssetName_str);
	}

	get __selectionArea()
	{
		let lIsMobile_bln = APP.isMobile;
		let lArea_obj = {
			x: lIsMobile_bln ? -43 : -18,
			y: lIsMobile_bln ? -26 : -14,
			width: lIsMobile_bln ? 86 : 36,
			height: lIsMobile_bln ? 60 : 28
		};

		return lArea_obj;
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

export default GUSLobbySelectableButton