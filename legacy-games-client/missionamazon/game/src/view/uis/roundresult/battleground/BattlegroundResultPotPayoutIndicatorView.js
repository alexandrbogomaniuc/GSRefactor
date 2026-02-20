import GUIndicatorView from '../../../../../../../common/PIXI/src/dgphoenix/gunified/view/ui/GUIndicatorView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import NumberValueFormat from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';

const MAX_WIDTH = 115;

class BattlegroundResultPotPayoutIndicatorView extends GUIndicatorView
{
	//INIT...
	_initIndicatorView()
	{
		this._fValueContainer_sprt.position.set(0, 0);

		super._initIndicatorView();

		this._onValueChanged();

		let l_s = this.addChild(APP.library.getSpriteFromAtlas("round_result/btg/gradient"));
		l_s.position.set(MAX_WIDTH / 2 + l_s.width / 2 + 3, 0);

		l_s = this.addChild(APP.library.getSpriteFromAtlas("round_result/btg/gradient"));
		l_s.scale.set(-1, 1);
		l_s.position.set( -MAX_WIDTH / 2 - l_s.width / 2 - 3, 0);
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
			fontFamily: "fnt_nm_barlow_semibold",
			fontSize: 20,
			align: "center",
			fill: 0xfccc32
		};
		return format;
	}

	_getValueMaxWidth()
	{
		return undefined;
	}
	
	_formatValue(aValue_num)
	{
		return APP.currencyInfo.i_formatNumber(aValue_num);
	}

	_applyFormattedValue(aFormatedValue_str)
	{
		let lValueView_tf = this._fValueView_tf;
		let lNewValue_str = aFormatedValue_str !== undefined ? aFormatedValue_str : "";
		lNewValue_str = APP.currencyInfo.i_formatString(lNewValue_str)

		lValueView_tf.text = lNewValue_str;
		this._onValueChanged();
	}

	_onValueChanged()
	{
		let lTotalWidth_num = 0;

		
		let lValueWidth_num = this._fValueView_tf.textBounds.width;

		this._fValueView_tf.position.set(lValueWidth_num/2, 0);

		this._fValueContainer_sprt.position.set(-(lValueWidth_num)/2, 0);

		lTotalWidth_num = lValueWidth_num;

		if (lTotalWidth_num > MAX_WIDTH)
		{
			let lOldX_num = this._fValueContainer_sprt.position.x;
			let lScale_num = MAX_WIDTH/lTotalWidth_num;
			this._fValueContainer_sprt.scale.x = lScale_num;
			this._fValueContainer_sprt.position.x = lOldX_num*lScale_num;
		}
	}
	//...VALUE

	destroy()
	{
		super.destroy();

		this._fValueContainer_sprt = null;
	}
}

export default BattlegroundResultPotPayoutIndicatorView;