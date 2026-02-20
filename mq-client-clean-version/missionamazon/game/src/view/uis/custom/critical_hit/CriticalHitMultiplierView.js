import SimpleUIView from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import AtlasSprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import BitmapText from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/BitmapText';
import AtlasConfig from './../../../../config/AtlasConfig';

class CriticalHitMultiplierView extends SimpleUIView
{
	set value(aValue_num)
	{
		this.__setValue(aValue_num);
	}

	get value()
	{
		return this._fValue_str;
	}

	constructor()
	{
		super();

		this.__fContainer_sprt = this.addChild(new Sprite());
		this.__init();
	}

	__init()
	{
		super.__init();

		this._fTextures_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset(this.__getAssetName())], [this.__getAssetAtlas()], "");

		this.__fValue_bt = this._createValueTextField();
		this._fValue_str = undefined;
	}

	__getAssetName()
	{
		return "critical_hit/critical_numbers";
	}

	__getAssetAtlas()
	{
		return AtlasConfig.CriticalNumbers;
	}

	__alignValue()
	{
		this.__fValue_bt.position.x = -this.__fValue_bt.getBounds().width/2;
		this.__fValue_bt.anchor.set(0.5, 1);
	}

	__getLetterSpacing()
	{
		return -2;
	}

	__setValue(aValue_num)
	{
		this._fValue_str = "" + aValue_num;
		this._writeValue();
		this.__alignValue();
	}

	_getValueOffsetY()
	{
		return 0;
	}

	_writeValue()
	{
		this.__fValue_bt.write(this._fValue_str);
		this.__fValue_bt.position.set(0, this._getValueOffsetY());
	}

	_createValueTextField()
	{
		return this.__fContainer_sprt.addChild(new BitmapText(this._fTextures_tx_map, "", this.__getLetterSpacing()));
	}

	destroy()
	{
		super.destroy();

		this.__fContainer_sprt = null;
		this._fTextures_tx_map = null;
		this.__fValue_bt = null;
		this._fValue_str = null;
	}
}

export default CriticalHitMultiplierView