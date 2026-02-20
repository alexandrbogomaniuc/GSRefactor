import AtlasConfig from '../config/AtlasConfig';
import Payout from './Payout';

const DECIMAL_PART_SCALE = 0.63

class WinPayout extends Payout
{
	constructor(aOptFontScale_num)
	{
		super(aOptFontScale_num);
	}

	_init()
	{
		super._init();

		this._fValue_bt.scale.set(this._fFontScale_num);

		this._fDecimicalValue_bt = this._createValueTextField();
		this._fDecimicalValue_bt.scale.set(DECIMAL_PART_SCALE*this._fFontScale_num);
	}

	_clear()
	{
		if (this._fDecimicalValue_bt)
		{
			this._fDecimicalValue_bt.destroy();
			this._fDecimicalValue_bt = null;
		}

		super._clear();
	}

	_setValue(aValue_num)
	{
		if (this._fValue_num === aValue_num) return;

		this._clear();
		this._init();

		super._setValue(aValue_num);
	}

	_getLetterSpacing()
	{
		return 0;
	}

	_getValueOffsetY()
	{
		return 3;
	}

	_getAssetName()
	{
		return "awards/gold_numbers";
	}

	_getAssetAtlas()
	{
		return AtlasConfig.GoldNumbers;
	}

	_writeValue(aValue_num)
	{
		let lFormattedValue_str = this._formatValue(aValue_num);
		let lDelimIndex_int = lFormattedValue_str.indexOf('.');
		if (~lDelimIndex_int)
		{
			this._fValue_bt.write(lFormattedValue_str.substring(0, lDelimIndex_int));
			this._fValue_bt.position.set(0, this._getValueOffsetY());

			let lValueBounds_pt = this._fValue_bt.getBounds();
			let lDemicals_str = lFormattedValue_str.substring(lDelimIndex_int).substr(0, 3);
			this._fDecimicalValue_bt.write(lDemicals_str);
			this._fDecimicalValue_bt.position.set(this._fValue_bt.position.x + lValueBounds_pt.width, this._getValueOffsetY() - lValueBounds_pt.height*(1-DECIMAL_PART_SCALE)/2);
			this._fDecimicalValue_bt.visible = true;
		}
		else
		{
			this._fDecimicalValue_bt.visible = false;
			super._writeValue.call(this, aValue_num);
		}
	}

	destroy()
	{
		this._fDecimicalValue_bt && this._fDecimicalValue_bt.destroy();
		this._fDecimicalValue_bt = null;

		super.destroy();
	}
}

export default WinPayout;