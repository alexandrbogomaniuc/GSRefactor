import GUIndicatorView from '../../../../../../../../common/PIXI/src/dgphoenix/gunified/view/ui/GUIndicatorView';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import NumberValueFormat from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import TextField from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import * as FEATURES from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/layout/features';
import I18 from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import MTimeLine from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';

class BattlegroundCountDownView extends GUIndicatorView
{
	applyValue(aFormatedValue_str = "00:59:00")
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

		//BLINKING TIMELINE...
		let l_mtl = new MTimeLine();
		l_mtl.addAnimation(
			this._fValueView_tf,
			MTimeLine.SET_ALPHA,
			1,
			[
				[0, 3],
				5,
				[1, 3],
				5,
			]);

		this._fBlinkingTimerTimeline_mtl = l_mtl;
		//...BLINKING TIMELINE
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
			fontSize: 60,
			fill: 0xffffff,
			dropShadow: true,
			dropShadowColor: 0x000000,
			dropShadowAngle: Math.PI/2,
			dropShadowDistance: 5,
			dropShadowAlpha: 0.6
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
	_applyFormattedValue(aFormatedValue_str = "00:59:00")
	{
		this._fValueView_tf.text = aFormatedValue_str;
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

export default BattlegroundCountDownView;