import GUIndicatorView from '../../../../../../../common/PIXI/src/dgphoenix/gunified/view/ui/GUIndicatorView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import NumberValueFormat from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';

class BossModeGemsTotalWinIndicator extends GUIndicatorView
{
	centringIndicator()
	{
		let lCurrencyWidth_num = this._fCurrencySymbol_tf ? this._fCurrencySymbol_tf.textBounds.width : 0;
		let lValueWidth_num = this._fValueView_tf.textBounds.width;
		this.position.x += (lCurrencyWidth_num + lValueWidth_num)/2;
	}

	//INIT...
	_initIndicatorView()
	{
		this._fCurrencySymbol_tf = null;
		this._fValueContainer_sprt.position.set(0, 0);

		super._initIndicatorView();

		this._onValueChanged();
	}
	//...INIT

	//VALUE...
	_initValueView()
	{
		super._initValueView();
		this._fValueView_tf.anchor.set(0.5, 0.5);

		this._initCurrencySymbolViewIfRequired();
	}

	_getValueTextFormat()
	{
		let format = {
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 13,
			fill: 0xfb8e0c,
			align: "center",
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5
		};
		return format;
	}

	_getValueMaxWidth()
	{
		return 80;
	}
	
	_formatValue(aValue_num)
	{
		return NumberValueFormat.formatMoney(aValue_num);
	}

	_onValueChanged()
	{
		if (this._fCurrencySymbol_tf)
		{
			let lCurrencyWidth_num = this._fCurrencySymbol_tf.textBounds.width;

			let lValueWidth_num = this._fValueView_tf.textBounds.width;
			if(lValueWidth_num > this._getValueMaxWidth())
			{
				lValueWidth_num = this._getValueMaxWidth();
			}

			this._fCurrencySymbol_tf.position.x =  - (lCurrencyWidth_num + lValueWidth_num)/2;
		}

		super._onValueChanged();
	}
	//...VALUE

	//CURRENCY SYMBOL...
	_initCurrencySymbolViewIfRequired()
	{
		if (!APP.playerController.info.currencySymbol)
		{
			return;
		}

		let lIsFontIncludesCurrencySymbol_bl = APP.fonts.isGlyphsSupported("fnt_nm_barlow_semibold", APP.playerController.info.currencySymbol);
		let l_tf = new TextField(lIsFontIncludesCurrencySymbol_bl ? this._getValueTextFormat() : this._getCurrencySymbolTextFormat());

		l_tf.anchor.set(0.5, 0.5);
		l_tf.text = APP.playerController.info.currencySymbol;
		this._fCurrencySymbol_tf = this._fValueContainer_sprt.addChild(l_tf);
		this._fValueContainer_sprt.position.y = 0.5;

		return l_tf;
	}

	_getCurrencySymbolTextFormat()
	{
		let format = {
			fontFamily: "fnt_nm_arial_currency",
			fontSize: 13,
			align: "center",
			fill: 0xfb8e0c
		};
		return format;
	}
	//...CURRENCY SYMBOL

	destroy()
	{
		super.destroy();

		this._fCurrencySymbol_tf = null;
		this._fValueContainer_sprt = null;
	}
}

export default BossModeGemsTotalWinIndicator;