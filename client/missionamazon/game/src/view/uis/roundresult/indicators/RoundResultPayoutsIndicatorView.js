import GUIndicatorView from '../../../../../../../common/PIXI/src/dgphoenix/gunified/view/ui/GUIndicatorView';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import NumberValueFormat from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/custom/values/NumberValueFormat';
import TextField from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class RoundResultPayoutsIndicatorView extends GUIndicatorView
{
	//INIT...
	_initIndicatorView()
	{
		this._fValueContainer_sprt.position.set(0, 0);
		this._fQuestsWinValueAsset_ta = this._fValueContainer_sprt.addChild(I18.generateNewCTranslatableAsset("TARoundResultPlusMoneyCaption"));

		super._initIndicatorView();

		this._onValueChanged();
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
		return APP.currencyInfo.i_formatNumber(aValue_num, true);
	}

	_onValueChanged()
	{
		let lTotalWidth_num = 0;

		let lPlusWidth_num = this._fQuestsWinValueAsset_ta.assetContent.textBounds.width;
		let lValueWidth_num = this._fValueView_tf.textBounds.width;

		this._fQuestsWinValueAsset_ta.position.set(lPlusWidth_num/2, 0);
		this._fValueView_tf.position.set(lValueWidth_num/2 + lPlusWidth_num, 0);

		this._fValueContainer_sprt.position.set(-(lValueWidth_num + lPlusWidth_num)/2, 0);

		lTotalWidth_num = lValueWidth_num + lPlusWidth_num;

		if (lTotalWidth_num > 94)
		{
			let lOldX_num = this._fValueContainer_sprt.position.x;
			let lScale_num = 94/lTotalWidth_num;
			this._fValueContainer_sprt.scale.x = lScale_num;
			this._fValueContainer_sprt.position.x = lOldX_num*lScale_num;
		}
	}
	//...VALUE

	destroy()
	{
		super.destroy();

		this._fQuestsWinValueAsset_ta = null;
		this._fValueContainer_sprt = null;
	}
}

export default RoundResultPayoutsIndicatorView;