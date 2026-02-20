import Button from '../../../../ui/GameButton';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

class GameplayDialogButton extends Button
{
	static get BUTTON_ID_OK() {return "btn_ok"}
	static get BUTTON_ID_CANCEL() {return "btn_cancel"}
	static get BUTTON_ID_CUSTOM() {return "btn_custom"}

	constructor()
	{
		super("preloader/dialogs/btn_base", undefined, true);

		this._isDown = false;
		this._glow = null;
		this._buttonId = undefined;
		this._baseColour = 0x000000;

		this.applyRoundedCornersHitArea();
	}

	set buttonId (value)
	{
		this._buttonId = value;

		switch (this._buttonId)
		{
			case GameplayDialogButton.BUTTON_ID_OK:
				{
					this.setOKCaption();
					break;
				}
			case GameplayDialogButton.BUTTON_ID_CANCEL:
				{
					this.setCancelCaption();
					break;
				}
			case GameplayDialogButton.BUTTON_ID_CUSTOM:
				{
					// no default caption
					break;
				}
			default:
				{
					throw new Error(`Unsupported button ID: ${value}`);
				}
		}
	}

	get buttonId ()
	{
		return this._buttonId;
	}

	get isOkButton ()
	{
		return this._buttonId == GameplayDialogButton.BUTTON_ID_OK;
	}

	get isCancelButton ()
	{
		return this._buttonId == GameplayDialogButton.BUTTON_ID_CANCEL;
	}

	get isCustomButton ()
	{
		return this._buttonId == GameplayDialogButton.BUTTON_ID_CUSTOM;
	}

	setOKCaption()
	{
		this.captionId = "TADialogButtonOK";
		this.captionTf.tint = this._baseColour;
	}

	setCancelCaption()
	{
		this.captionId = "TADialogButtonCancel";
		this.captionTf.tint = this._baseColour;
	}

	setBuyInCaption()
	{
		this.captionId = "TADialogButtonBuyIn";
		this.captionTf.tint = this._baseColour;
	}

	setRefreshCaption()
	{
		this.captionId = "TADialogButtonRefresh";
		this.captionTf.tint = this._baseColour;
	}

	setCopyCaption()
	{
		this.captionId = "TADialogButtonCopy";
		this.captionTf.tint = this._baseColour;
	}

	setRetryCaption()
	{
		this.captionId = "TADialogButtonRetry";
		this.captionTf.tint = this._baseColour;
	}

	setExitCaption()
	{
		this.captionId = "TADialogButtonExit";
		this.captionTf.tint = this._baseColour;
	}

	setYesCaption()
	{
		this.captionId = "TADialogButtonYes";
		this.captionTf.tint = this._baseColour;
	}

	setNoCaption()
	{
		this.captionId = "TADialogButtonNo";
		this.captionTf.tint = this._baseColour;
	}

	handleDown(e)
	{
		super.handleDown(e);

		this._isDown = true;

		//this.updateBase('preloader/dialogs/btn_down_base');
		this._updateCaptionColor(0x000000);
		this._hideGlow();
	}

	handleUpOutside(e)
	{
		super.handleUpOutside(e);
		this._updateCaptionColor(0x000000);
	}

	handleUp(e)
	{
		super.handleUp(e);

		this._isDown = false;

		this.updateBase('preloader/dialogs/btn_base');
		this._updateCaptionColor(0x000000);
	}

	handleOver(e)
	{
		super.handleOver(e);

		if (!this._isDown)
		{
			this._updateCaptionColor(0x000000);

			this._showGlow();
		}
	}

	handleOut(e)
	{
		super.handleOut(e);

		if (!this._isDown)
		{
			this._updateCaptionColor(0x000000);
		}
	}

	_updateCaptionColor(color)
	{
		if (!this.captionTf)
		{
			return;
		}
		this.captionTf.tint = color;
	}

	_showGlow()
	{
		if (!this._glow)
		{
			this._glow = new Sprite();

			let glowGraphics = new PIXI.Graphics();
			glowGraphics.beginFill(0xffffcc, 0.25);
			glowGraphics.drawRoundedRect(-37, -12, 74, 23, 100);
			glowGraphics.endFill();
			this._glow.addChild(glowGraphics);

			this.addChildAt(this._glow, 0);
		}

		this._glow.scale.set(1);
		this._glow.alpha = 1;

		this._glow.scaleXTo(1.3, 200);
		this._glow.scaleYTo(1.6, 200);
		this._glow.fadeTo(0, 500);
	}

	_hideGlow()
	{
		if (!this._glow)
		{
			return;
		}

		this._glow.removeTweens();
		this._glow.scale.set(1);
		this._glow.alpha = 1;
	}
}

export default GameplayDialogButton