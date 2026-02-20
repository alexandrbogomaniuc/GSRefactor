import GameButton from '../../../../ui/GameButton';
import { GAME_VIEW_SETTINGS } from '../../../main/GameBaseView';

class BottomPanelButton extends GameButton
{
	constructor(baseAssetName, captionId = undefined, aOptScale, aIsBlackAndWhiteButton_bl = false)
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

		this._fIsBlackAndWhiteButton_bl = aIsBlackAndWhiteButton_bl;
	}

	_updateBaseView(aBaseAssetName_str)
	{
		let lBarRowHeight_num = GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT;
		let lAreaWidth_num = lBarRowHeight_num-2;
		let lAreaHeight_num = lBarRowHeight_num-2;

		this.setHitArea(new PIXI.Rectangle(-lAreaWidth_num/2, -lAreaHeight_num/2, lAreaWidth_num, lAreaHeight_num));

		super._updateBaseView(aBaseAssetName_str);
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

	handleDown()
	{
		this._tryPlaySound();
	}

	destroy()
	{
		super.destroy();

		this._fIsBlackAndWhiteButton_bl = null;
	}
}

export default BottomPanelButton;
