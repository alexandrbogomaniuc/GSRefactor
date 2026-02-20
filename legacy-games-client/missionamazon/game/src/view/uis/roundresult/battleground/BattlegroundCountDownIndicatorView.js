import GUIndicatorView from '../../../../../../../common/PIXI/src/dgphoenix/gunified/view/ui/GUIndicatorView';
import * as FEATURES from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/features';

class BattlegroundCountDownIndicatorView extends GUIndicatorView
{
	static get COUNT_DOWN_UNDEFINED_VALUE()							{ return "--:--:--"}

	applyValue(aFormatedValue_str = BattlegroundCountDownIndicatorView.COUNT_DOWN_UNDEFINED_VALUE)
	{
		this._applyFormattedValue(aFormatedValue_str);
	}

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
			fontFamily: "fnt_nm_barlow_bold",
			fontSize: 30,
			fill: 0xffffff,
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 5,
			dropShadowAlpha: 0.5
		};
		return format;
	}

	_getValueMaxWidth()
	{
		return 0;
	}

	get _maxWidth()
	{
		return 245;
	}

	//override
	_applyFormattedValue(aFormatedValue_str = BattlegroundCountDownIndicatorView.COUNT_DOWN_UNDEFINED_VALUE)
	{
		let lFormattedValue_str = aFormatedValue_str;

		if (lFormattedValue_str == null)
		{
			lFormattedValue_str = BattlegroundCountDownIndicatorView.COUNT_DOWN_UNDEFINED_VALUE
		}

		if (this._fValueView_tf.text == lFormattedValue_str)
		{
			return;
		}

		this._fValueView_tf.text = lFormattedValue_str;
		this._onValueChanged();
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

export default BattlegroundCountDownIndicatorView;