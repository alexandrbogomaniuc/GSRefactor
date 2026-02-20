import Payout from './Payout';
import AtlasConfig from '../config/AtlasConfig';
import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class HPValue extends Payout {

	i_updateValue(aValue_int)
	{
		this._updateValue(aValue_int);
	}

	constructor(aOptFontScale_num = 0.2)
	{
		super(aOptFontScale_num);
	}

	_init()
	{
		super._init();

		this._fValue_bt.scale.set(this._fFontScale_num);
	}

	_getLetterSpacing()
	{
		return -12;
	}

	_getValueOffsetY()
	{
		return APP.isMobile ? -3 : 3;
	}

	_getAssetName()
	{
		return "awards/gold_numbers";
	}

	_getAssetAtlas()
	{
		return AtlasConfig.GoldNumbers;
	}

	_updateValue(aValue_int)
	{
		this.value = aValue_int;
	}
}

export default HPValue;