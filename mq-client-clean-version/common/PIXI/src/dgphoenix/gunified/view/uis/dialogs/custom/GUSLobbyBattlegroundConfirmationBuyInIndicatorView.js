import GUIndicatorView from '../../../ui/GUIndicatorView';
import * as FEATURES from '../../../../../unified/view/layout/features';
import { APP } from '../../../../../unified/controller/main/globals';

class GUSLobbyBattlegroundConfirmationBuyInIndicatorView extends GUIndicatorView
{
	getBounds()
	{
		return this._fValueContainer_sprt.getBounds();
	}

	//INIT...
	_initIndicatorView()
	{
		this._fValueContainer_sprt.position.set(0, -4);

		super._initIndicatorView();

		if (FEATURES.IE)
		{
			this._fValueContainer_sprt.pivot.set(-4, -2);
		}
	}
	//...INIT

	//VALUE...
	_initValueView()
	{
		super._initValueView();
		this._fValueView_tf.anchor.set(0.5, 0.5);
	}

	_getValueTextFormat()
	{
		let format = {
			fontFamily: "fnt_nm_cmn_barlow_bold",
			fontSize: 33,
			fill: 0xfccc32,
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 5,
			dropShadowAlpha: 0.8
		};
		return format;
	}

	_getValueMaxWidth()
	{
		return 0;
	}
	
	_formatValue(aValue_num)
	{
		return APP.currencyInfo.i_formatNumber(aValue_num, false, false, 2);
	}

	get _maxWidth()
	{
		return 136;
	}

	applyValue(aValue_num)
	{
		this._applyFormattedValue(this._formatValue(aValue_num));
	}

	//override
	_applyFormattedValue(aFormatedValue_str)
	{
		let lValueView_tf = this._fValueView_tf;
		let lNewValue_str = aFormatedValue_str !== undefined ? APP.currencyInfo.i_formatString(aFormatedValue_str) : "";		
		lValueView_tf.text = " " + lNewValue_str;

		this._onValueChanged();
	}

	_onValueChanged()
	{
		this._fValueView_tf.position.x = 0;

		this._fValueContainer_sprt.scale.x = 1;

		this._fValueContainer_sprt.scale.x = 1;
		let lWidth_num = this._fValueContainer_sprt.getBounds().width;

		if (lWidth_num > this._maxWidth)
		{
			this._fValueContainer_sprt.scale.x = this._maxWidth / lWidth_num;
		}
		
		super._onValueChanged();

	}
	//...VALUE
}

export default GUSLobbyBattlegroundConfirmationBuyInIndicatorView;