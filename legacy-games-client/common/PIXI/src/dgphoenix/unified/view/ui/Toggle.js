import { APP } from '../../controller/main/globals';
import I18 from '../../controller/translations/I18';
import Sprite from '../base/display/Sprite';
import Button from './Button';

class Toggle extends Sprite 
{
	static get EVENT_BUTTON_CLICKED() 		{return "onToggleButtonCLicked";}
	static get EVENT_STATE_CHANGED() 		{return "onToggleStateChanged";}

	set state(aState_int)
	{
		this._setState(aState_int);
	}

	get state()
	{
		return this._fState_int;
	}

	set enabled(aEnable_bl)
	{
		this._setEnabled(aEnable_bl);
	}

	get enabled()
	{
		return this._fEnabled_bl;
	}

	//INIT...
	constructor(aAssetNames_obj = {}, aLabels_obj = {}) 
	{
		super();

		this._fButtons_btn_arr = [];
		this._fLabels_cta_arr = [];
		this._fState_int = undefined;
		this._fEnabled_bl = false;

		if (aAssetNames_obj)
		{
			let bg = aAssetNames_obj.bgAsset;
			if (bg)
			{
				this._fBgView_sprt = this._initBg(bg);
			}

			let btns = aAssetNames_obj.buttonAssets;
			if (btns && btns.length)
			{
				for (let i = 0; i < btns.length; i++) 
				{
					let btn = this._initButton(btns[i], i);
					this._fButtons_btn_arr.push(btn);
				}
			}
		}

		if (aLabels_obj)
		{
			let caption = aLabels_obj.captionAsset;
			if (caption)
			{
				this._fCaption_ta = this._initCaption(caption);
			}

			let labels = aLabels_obj.labelAssets;
			if (labels && labels.length)
			{
				for (let i = 0; i < labels.length; i++) 
				{
					this._fLabels_cta_arr.push(this._initButtonLabel(labels[i], i));
				}
			}
		}

		this.enabled = true;
	}

	_initBg(aAssetName_str)
	{
		let bg = this.addChild(APP.library.getSprite(aAssetName_str));
		return bg;
	}

	_initButton(aAssetName_str, aBtnIndex_int)
	{
		let btn = this.addChild(new Button(aAssetName_str, undefined, true));
		btn.position.x = btn.getBounds().width * (aBtnIndex_int ? 1 : -1);
		btn.on("pointerclick", this._onButtonClicked, this);
		return btn;
	}

	_initCaption(aAssetName_str)
	{
		let ta = this.addChild(I18.generateNewCTranslatableAsset(aAssetName_str));
		ta.position.y = -20;
		return ta;
	}

	_initButtonLabel(aAssetName_str, aLabelIndex_int)
	{
		let ta = this.addChild(I18.generateNewCTranslatableAsset(aAssetName_str));
		ta.position.set(this._fButtons_btn_arr[aLabelIndex_int].x, 10);
		return ta;
	}
	//...INIT

	_onButtonClicked(event)
	{
		let index = this._fButtons_btn_arr.indexOf(event.target);
		this.emit(Toggle.EVENT_BUTTON_CLICKED, {index: index});

		this._setState(index);
	}

	_setState(aState_int)
	{
		if (aState_int === undefined 
			|| aState_int === this._fState_int
			|| aState_int < 0
			|| aState_int >= this._fButtons_btn_arr.length
			)
		{
			return;
		}

		this._fState_int = aState_int;
		this.emit(Toggle.EVENT_STATE_CHANGED, {state: this._fState_int});

		this._updateState();
	}

	_updateState()
	{
		for (var i = 0; i < this._fButtons_btn_arr.length; i++) 
		{
			this._fButtons_btn_arr[i].holder.visible = (i === this._fState_int);
		}
	}

	_setEnabled(aEnable_bl)
	{
		aEnable_bl = !!aEnable_bl;
		if (aEnable_bl === this._fEnabled_bl)
		{
			return;
		}

		this._fEnabled_bl = aEnable_bl;
		this._updateEnable();
	}

	_updateEnable()
	{
		for (var i = 0; i < this._fButtons_btn_arr.length; i++) 
		{
			this._fButtons_btn_arr[i].enabled = this._fEnabled_bl;
		}
	}

	destroy()
	{
		while(this._fButtons_btn_arr.length)
		{
			let btn = this._fButtons_btn_arr.pop();
			btn.destroy();
		}

		while(this._fLabels_cta_arr.length)
		{
			let btn = this._fLabels_cta_arr.pop();
			btn.destroy();
		}

		if (this._fBgView_sprt)
		{
			this._fBgView_sprt.destroy();
			this._fBgView_sprt = null;
		}

		if (this._fCaption_ta)
		{
			this._fCaption_ta.destroy();
			this._fCaption_ta = null;
		}

		super.destroy();
	}
}

export default Toggle;