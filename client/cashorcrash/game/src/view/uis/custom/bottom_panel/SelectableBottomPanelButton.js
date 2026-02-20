import BottomPanelButton from './BottomPanelButton';

class SelectableBottomPanelButton extends BottomPanelButton
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

	constructor(aBaseAssetName_str, aCaptionId_str, aOptScale, aOptIsBlackAndWhiteButton_bl)
	{
		super(aBaseAssetName_str, aCaptionId_str, aOptScale, aOptIsBlackAndWhiteButton_bl);

		// this.activated = true;
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
		this._fSelectedBack_grphc = this.holder.addChild(new PIXI.Graphics());
		this._fSelectedBack_grphc.visible = false;
		
		super._updateBaseView(aBaseAssetName_str);
	}

	setHitArea(aHitArea_obj)
	{
		super.setHitArea(aHitArea_obj);

		this._redrawSelectedBack();
	}

	_redrawSelectedBack()
	{
		let l_gr = this._fSelectedBack_grphc;
		let lArea_obj = this.getHitArea();

		l_gr.clear();
		l_gr.beginFill(0xfccc32).drawRect(lArea_obj.x, lArea_obj.y, lArea_obj.width, lArea_obj.height).endFill();
	}

	destroy()
	{
		super.destroy();

		this._fSelectedBack_grphc = null;
	}
}

export default SelectableBottomPanelButton