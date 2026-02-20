import PointerSprite from './PointerSprite';
import Button from './Button';

/**
 * Interactive element with several possible states.
 * Each state has its own view.
 * Only one state can be active (and visible) at the moment.
 * @class
 * @extends PointerSprite
 * @inheritdoc
 */
class MultiCheckBox extends PointerSprite 
{
	static get EVENT_ON_CHECKBOX_STATE_CHANGED()	{ return "onCheckboxStateChanged"; }

	/**
	 * Switch to state.
	 * @param {number} aState_int
	 */
	set checkState(aState_int)
	{
		if (aState_int > this._fStatesLength_int) aState_int = this._fStatesLength_int;

		this._fCheckState_int = aState_int;
		this._onStateChange();
	}

	/**
	 * Apply scale to all states.
	 * @param {number} aScale_num 
	 */
	scaleStateViews(aScale_num)
	{
		for (let lView_sprt of this._fStateViews_arr)
		{
			lView_sprt.scale.set(aScale_num);
		}
	}

	/**
	 * @constructor
	 * @param {PIXI.Texture|PIXI.Graphics|Sprite|String} aBaseAsset_str  - Asset name or view of base.
	 * @param {PIXI.Texture[]|PIXI.Graphics[]|Sprite[]|String[]} aAssets_arr - Assets (or names) for states.
	 * @param {number} aStatesLength_int_opt - Amount of possible states.
	 */
	constructor(aBaseAsset_str, aAssets_arr, aStatesLength_int_opt) 
	{
		super(aBaseAsset_str, true);

		if (aStatesLength_int_opt !== undefined && aStatesLength_int_opt !== null)
			this._fStatesLength_int = aStatesLength_int_opt;
		else
			this._fStatesLength_int = aAssets_arr.length;

		this._fCheckState_int = 1;
		this._fStateViews_arr = [];

		this._init(aAssets_arr);
		this.setEnabled();
	}

	_init(aAssets_arr)
	{
		for (let i = 0; i < aAssets_arr.length; ++i)
		{
			let lStateView_btn = this.addChild(new Button(aAssets_arr[i], null, true));
			this._fStateViews_arr.push(lStateView_btn);
		}

		this.on("pointerclick", this._onClick, this);
		this._onStateChange();
	}

	_onClick()
	{
		if (++this._fCheckState_int > this._fStatesLength_int)
		{
			this._fCheckState_int = 1;
		}

		this._onStateChange();
	}

	_onStateChange()
	{
		for (let i = 0; i < this._fStateViews_arr.length; ++i)
		{
			this._fStateViews_arr[i].visible = false;
		}

		if (this._fStateViews_arr[this._fCheckState_int - 1])
		{
			this._fStateViews_arr[this._fCheckState_int - 1].visible = true;
		}

		this._fireStateChangeEvent();
	}

	_fireStateChangeEvent()
	{
		this.emit(MultiCheckBox.EVENT_ON_CHECKBOX_STATE_CHANGED, {state: this._fCheckState_int});
	}

	/**
	 * Destroy instance.
	 */
	destroy()
	{
		super.destroy();

		this._fStatesLength_int = null;
		this._fCheckState_int = null;
		this._fStateViews_arr = null;
	}
}

export default MultiCheckBox;