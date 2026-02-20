import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import AtlasSprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import BitmapText from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/BitmapText';

class Payout extends Sprite
{
	set value(aValue_num)
	{
		this._setValue(aValue_num);
	}

	get value()
	{
		return this._fValue_num;
	}

	get fontScale()
	{
		return this._fFontScale_num;
	}

	constructor(aOptFontScale_num = 1)
	{
		super();

		this._fContainer_sprt = null;
		this._fValue_bt = null;

		this._fValue_num = undefined;
		this._fTextures_tx_map = AtlasSprite.getMapFrames([APP.library.getAsset(this._getAssetName())], [this._getAssetAtlas()], "");

		this._fFontScale_num = aOptFontScale_num;

		this._init();
	}

	_init()
	{
		this._fContainer_sprt = this.addChild(new Sprite());
		this._fSignContainer_sprt = this._fContainer_sprt.addChild(new Sprite());

		this._fValue_bt = this._createValueTextField();
		
		this._fValue_num = undefined;
	}

	_clear()
	{
		if (this._fValue_bt)
		{
			this._fValue_bt.destroy();
			this._fValue_bt = null;
		}

		if (this._fSignContainer_sprt)
		{
			if (this._fOptSignBaseView_sprt)
			{
				this._fSignContainer_sprt.removeChild(this._fOptSignBaseView_sprt);
				this._fOptSignBaseView_sprt.anchor.set(0.5, 0.5);
				this._fOptSignBaseView_sprt.position.set(0, 0);
			}

			if (this._fOptSignView_sprt)
			{
				this._fSignContainer_sprt.removeChild(this._fOptSignView_sprt);
				this._fOptSignView_sprt.anchor.set(0.5, 0.5);
				this._fOptSignView_sprt.position.set(0, 0);
			}

			this._fSignContainer_sprt.destroy();
			this._fSignContainer_sprt = null;
		}

		if (this._fContainer_sprt)
		{
			this._fContainer_sprt.destroy();
			this._fContainer_sprt = null;
		}

		this._fValue_num = undefined;
	}

	_getLetterSpacing()
	{
		return 0;
	}

	_getValueOffsetY()
	{
		return 0;
	}

	_getAssetName()
	{
		throw new Error(`Method should be overriden`);
	}

	_getAssetAtlas()
	{
		throw new Error(`Method should be overriden`);
	}

	_formatValue(aValue_num)
	{
		return APP.currencyInfo.i_formatNumber(aValue_num, false);
	}

	_setValue(aValue_num)
	{
		this._fValue_num = aValue_num;
		this._writeValue(aValue_num);
		this._alignValue();
	}

	_writeValue(aValue_num)
	{
		this._fValue_bt.write(this._formatValue(aValue_num));
		this._fValue_bt.position.set(0, this._getValueOffsetY());
	}

	_alignValue()
	{
		this._fContainer_sprt.position.x = -this._fContainer_sprt.getBounds().width/2;
	}

	//VALUE TEXTFIELD...
	_createValueTextField()
	{
		return this._fContainer_sprt.addChild(new BitmapText(this._fTextures_tx_map, "", this._getLetterSpacing()));
	}
	//...VALUE TEXTFIELD

	destroy()
	{
		this._fTextures_tx_map = null;

		this._fValue_bt && this._fValue_bt.destroy();
		this._fValue_bt = null;

		this._fContainer_sprt && this._fContainer_sprt.destroy();
		this._fContainer_sprt = null;

		this._fValue_num = undefined;

		super.destroy();
	}
}

export default Payout;