import { APP } from '../../../../../unified/controller/main/globals';
import Button from '../../GUSLobbyButton';

class GUSLobbyCommonPanelButton extends Button
{
	hideSeptum()
	{
		if (!this._fSeptum_grphc) return;
		this._fSeptum_grphc.visible = false;
	}

	showSeptum()
	{
		if (!this._fSeptum_grphc) return;
		this._fSeptum_grphc.visible = true;
	}

	constructor(baseAssetName, captionId = undefined, aOptDrawSeptum_bln = true, aOptScale, aIsBlackAndWhiteButton_bl = false)
	{
		super(baseAssetName, captionId, true);

		this._addUpHandler();

		if (aOptScale)
		{
			if (aOptScale.x && aOptScale.y)
			{
				this._baseView.scale.x = aOptScale.x;
				this._baseView.scale.y = aOptScale.y;
			}
			else
			{
				this._baseView.scale.set(aOptScale);
			}
		}

		this._fSeptum_grphc = null;
		this._fIsBlackAndWhiteButton_bl = aIsBlackAndWhiteButton_bl;

		if (aOptDrawSeptum_bln)
		{
			this._addSeptum();
		}
	}

	_addSeptum()
	{
		this._fSeptum_grphc = this.addChild(new PIXI.Graphics());

		let lWidth_num = this.__septumWidth;
		let lHeight_num = this.__septumHeight;

		this._fSeptum_grphc.beginFill(this.__septumColor).drawRect(-lWidth_num, -lHeight_num / 2, lWidth_num, lHeight_num).endFill();
		
		let lPos_p = this.__septumPosition;
		this._fSeptum_grphc.position.set(lPos_p.x, lPos_p.y);
	}

	get __septumColor()
	{
		return 0x4e4e4e;
	}

	get __septumWidth()
	{
		return 1;
	}

	get __septumHeight()
	{
		return APP.isMobile ? 22 : 18;
	}

	get __septumPosition()
	{
		return new PIXI.Point(-18, 0);
	}

	_addUpHandler()
	{
		document.body.addEventListener("mouseup", this._onDocumentMouseUp.bind(this));
		document.body.addEventListener("pointerup", this._onDocumentMouseUp.bind(this));
		document.body.addEventListener("touchend", this._onDocumentMouseUp.bind(this));
	}

	_onDocumentMouseUp()
	{
		if (this._fHolded_bln)
		{
			this.handleUp();
		}
	}

	setEnabled()
	{
		this._fIsBlackAndWhiteButton_bl && (this._baseView.tint = 0xffffff);

		super.setEnabled();
	}

	setDisabled()
	{
		this._fIsBlackAndWhiteButton_bl && (this._baseView.tint = 0x5b5b5b);

		super.setDisabled();
	}

	destroy()
	{
		super.destroy();

		this._fSeptum_grphc = null;
		this._fIsBlackAndWhiteButton_bl = null;
	}
}

export default GUSLobbyCommonPanelButton;
