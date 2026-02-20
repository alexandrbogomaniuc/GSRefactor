import MultiCheckBox from './MultiCheckBox';

/**
 * Interactive view with 2 possible states: checked or not checked.
 * Only one state is active (and visible) at the moment.
 * @class
 * @extends MultiCheckBox
 * @inheritdoc
 */
class CheckBox extends MultiCheckBox 
{
	/**
	 * Update checked state.
	 * @param {boolean} aState_bln
	 */
	set checkState(aState_bln)
	{
		this._fCheckState_int = aState_bln ? 1 : 2;
		this._onStateChange();
	}

	/** Set checked. */
	check()
	{
		this._check();
	}

	/** Set unchecked. */
	uncheck()
	{
		this._uncheck();
	}

	/**
	 * @constructor
	 * @param {PIXI.Texture|PIXI.Graphics|Sprite|String} aBaseAsset_str - Asset name or view of base.
	 * @param {PIXI.Texture|PIXI.Graphics|Sprite|String} aCheckAsset_str - Asset name or view of checked state.
	 * @param {PIXI.Texture|PIXI.Graphics|Sprite|String} aUncheckAsset_str_opt  - Asset name or view of unchecked state.
	 */
	constructor(aBaseAsset_str, aCheckAsset_str, aUncheckAsset_str_opt) 
	{
		super(aBaseAsset_str, aUncheckAsset_str_opt ? [aCheckAsset_str, aUncheckAsset_str_opt] : [aCheckAsset_str], 2);
	}

	_check()
	{
		this.checkState = 1;
	}

	_uncheck()
	{
		this.checkState = 2;
	}

	_fireStateChangeEvent()
	{
		let lState_bln = !Boolean(this._fCheckState_int - 1);

		this.emit(MultiCheckBox.EVENT_ON_CHECKBOX_STATE_CHANGED, {checked: lState_bln});
	}
}

export default CheckBox;