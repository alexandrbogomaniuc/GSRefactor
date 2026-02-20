import Button from '../../../../common/PIXI/src/dgphoenix/unified/view/ui/Button';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class GameButton extends Button
{
	constructor(baseAssetName, captionId, cursorPointer, noDefaultSound, anchorPoint, buttonType, useDefaultGreyscaleForDisabledState)
	{
		super(baseAssetName, captionId, cursorPointer, noDefaultSound, anchorPoint, buttonType);

		this._fSpecifiedSoundName_str = undefined;
		this._fUseDefaultGreyscaleForDisabledState_bl = useDefaultGreyscaleForDisabledState;
	}

	set specifiedSoundName(value)
	{
		this._fSpecifiedSoundName_str = value;
	}

	get specifiedSoundName()
	{
		return this._fSpecifiedSoundName_str;
	}

	get isSpecifiedSoundNameDefined()
	{
		return this._fSpecifiedSoundName_str !== undefined;
	}

	handleDown(e) 
	{
		if(!APP.isMobile && e.data.originalEvent.button != 0) return;
		this._tryPlaySound();
		this.holder.scale.set(0.95);
		this._fHolded_bln = true;

		this.emit(Button.EVENT_ON_HOLDED);
	}

	_updateDisabledView()
	{
		let lCheckVisible_bl = null;

		if (!this.baseView.visible)
		{
			lCheckVisible_bl = false;
			this.baseView.visible = true;
		}
		else 
		{
			lCheckVisible_bl = true;
		}

		super._updateDisabledView()

		if (!lCheckVisible_bl)
		{
			this.baseView.visible = false;
		}
	}

	setEnabled()
	{
		this.baseView.visible = true; 
		super.setEnabled();
	}
	
	setDisabled()
	{
		super.setDisabled();
		this.baseView.visible = false;
	}

	_tryPlaySound()
	{
		if (!this.noDefaultSound && APP.soundsController)
		{
			let soundName = "";

			if (this.isSpecifiedSoundNameDefined)
			{
				soundName = this.specifiedSoundName;
			}
			else
			{
				switch (this._buttonType)
				{
					case Button.BUTTON_TYPE_ACCEPT:
					case Button.BUTTON_TYPE_CANCEL:
					case Button.BUTTON_TYPE_COMMON:
					default:
						soundName = "gui_button_generic";
					break;
				}
			}

			APP.soundsController.play && APP.soundsController.play(soundName);
		}
	}
	
	_initGrayFilter()
	{
		if (this._fUseDefaultGreyscaleForDisabledState_bl)
		{
			return super._initGrayFilter();
		}

		let colorMatrix = [
			//R  G  B  A
			0, 0, 0, 0,
			0, 0, 0, 0,
			0, 0, 0, 0,
			0, 0, 0, 0
		];
		let filter = new PIXI.filters.ColorMatrixFilter();
		filter.matrix = colorMatrix;
		filter.resolution = 20;
		filter.alpha = 0.67;
		filter.blur = 10;
		return filter;
	}
}

export default GameButton