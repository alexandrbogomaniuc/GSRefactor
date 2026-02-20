import GUIndicatorView from '../../../../../../../common/PIXI/src/dgphoenix/gunified/view/ui/GUIndicatorView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import * as FEATURES from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/features';
import NumberValueFormat from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';

class LobbyBulletCostIndicatorView extends GUIndicatorView
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
			this._fValueContainer_sprt.pivot.set(1, -2);
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
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 18,
			fill: 0x000000,
			dropShadow: true,
			dropShadowColor: 0xffffff,
			dropShadowAngle: Math.PI / 2,
			dropShadowDistance: 1,
			dropShadowAlpha: 0.5
		};
		return format;
	}

	_getValueMaxWidth()
	{
		return 0;
	}

	_formatValue(aValue_num)
	{
		let lFormattedValue_str = APP.currencyInfo.i_formatNumber(aValue_num, true);
		let lDecimalDelimeterIndex_int = lFormattedValue_str.indexOf(NumberValueFormat.DECIMAL_DELIMETER);

		if (lDecimalDelimeterIndex_int >= 0 && APP.currencyInfo.i_getCurrencyId() === "MQC")
		{
			lFormattedValue_str = lFormattedValue_str.substring(0, lDecimalDelimeterIndex_int);
		}

		return lFormattedValue_str;
	}

	get _maxWidth()
	{
		return 50;
	}

	_onValueChanged()
	{
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

export default LobbyBulletCostIndicatorView;