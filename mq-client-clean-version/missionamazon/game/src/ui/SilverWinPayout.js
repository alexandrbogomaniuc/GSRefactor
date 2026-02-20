import WinPayout from './WinPayout';
import AtlasConfig from '../config/AtlasConfig';


class SilverWinPayout extends WinPayout {

	//override
	_getLetterSpacing()
	{
		return 0;
	}

	//override
	_getValueOffsetY()
	{
		return 3;
	}

	//override
	_getAssetName()
	{
		return "awards/silver_numbers";
	}

	//override
	_getAssetAtlas()
	{
		return AtlasConfig.SilverNumbers;
	}

	//SIGN SYMBOL...
	//override
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
			this._fDecimicalValue_bt.position.set(this._fValue_bt.position.x + lValueBounds_pt.width, lValueBounds_pt.y - this._fDecimicalValue_bt.getBounds().y - this._fDecimicalValue_bt.y);
			this._fDecimicalValue_bt.visible = true;
		}
		else
		{
			this._fDecimicalValue_bt.visible = false;
			super._writeValue.call(this, aValue_num);
		}
	}

}

export default SilverWinPayout;