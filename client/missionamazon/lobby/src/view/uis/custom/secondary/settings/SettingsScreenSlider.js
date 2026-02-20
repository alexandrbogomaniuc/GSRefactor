import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class SettingsScreenSlider extends Sprite
{
	static get EVENT_ON_SLIDER_VALUE_CHANGED()			{ return "onLaunchGameBtnClicked"; }
	static get EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE()	{ return "EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE"; }

	constructor(aMin, aMax, aVal)
	{
		super();

		this._fMin_num = aMin;
		this._fMax_num = aMax;
		this._fVal_num = aVal;

		this._fCurrentValue_num = 0;

		APP.on("contextmenu", this.stopMove, this);

		this._fBackValue_spr = this.addChild(APP.library.getSprite("settings/slider_back"));
		this._fBackValue_spr.on("pointerdown", this.startMove, this);
		this._fBackValue_spr.on("pointerupoutside", this.stopMove, this);
		this._fBackValue_spr.on("pointerup", this.stopMove, this);

		this.buttonMode = true;
		this.interactive = true;

		this._fSliderWidth_num = this._fBackValue_spr.getBounds().width;

		this._fBar_spr = this.addChild(APP.library.getSprite("settings/bar"));
		this._fBar_spr.position.set(-this._fSliderWidth_num / 2, 0.5);
		this._fBar_spr.scale.x = this._fVal_num / 100;
		this._fBar_spr.anchor.set(0, 0.5);
	}

	set value(aVal)
	{
		if (aVal >= this._fMin_num && aVal <= this._fMax_num
			&& aVal !== this._fVal_num)
		{
			this._fVal_num = aVal;
			this._fBar_spr.scale.x = this._fVal_num / 100;
			this.emit(SettingsScreenSlider.EVENT_ON_SLIDER_VALUE_CHANGED, { value: this._fVal_num });
		}
	}

	//EVENT LISTENERS...

	updateValByPointer()
	{
		if (this._fCurrentValue_num > this._fSliderWidth_num) this._fCurrentValue_num = this._fSliderWidth_num;
		if (this._fCurrentValue_num < 0) this._fCurrentValue_num = 0;

		this._fVal_num = Math.round(this._fCurrentValue_num / this._fSliderWidth_num * (this._fMax_num - this._fMin_num) + this._fMin_num);
		this._fBar_spr.scale.x = this._fVal_num / 100;

		this.emit(SettingsScreenSlider.EVENT_ON_SLIDER_VALUE_CHANGED, { value: this._fVal_num });
	}

	startMove(e)
	{
		this.emit(SettingsScreenSlider.EVENT_ON_SETTINGS_SCREEN_SLIDER_MOVE);

		this._fCurrentValue_num = e.data.local.x + this._fSliderWidth_num * 0.5;
		this.updateValByPointer();

		this._fBackValue_spr.on("pointermove", this.move, this);
	}

	move(e)
	{
		this._fCurrentValue_num = e.data.local.x + this._fSliderWidth_num * 0.5;
		this.updateValByPointer();
	}

	stopMove()
	{
		this._fBackValue_spr.off("pointermove", this.move, this);
	}
	//...EVENT LISTENERS

	destroy()
	{
		this._fMin_num = null;
		this._fMax_num = null;
		this._fVal_num = null;
		this._fCurrentValue_num = null;
		this._fBackValue_spr = null;
		this._fSliderWidth_num = null
		this._fBar_spr = null;

		this._fBackValue_spr.off("pointermove", this.move, this);
		APP.off("contextmenu", this.stopMove, this);
		super.destroy();
	}
}

export default SettingsScreenSlider;