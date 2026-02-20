import Sprite from '../../../unified/view/base/display/Sprite';
import TextField from '../../../unified/view/base/display/TextField';
import { Utils } from '../../../unified/model/Utils';
import NumberValueFormat from '../../../unified/view/custom/values/NumberValueFormat';
import I18 from '../../../unified/controller/translations/I18';

var _EMPTY_VALUE = "";

class GUIndicatorView extends Sprite 
{
	//INTERFACE...
	set indicatorValue(aValue_num)
	{
		this._indicatorValue = aValue_num;
	}

	get indicatorValue()
	{
		return this._fIndicatorValue_num;
	}
	//...INTERFACE

	//INIT...
	constructor(aOptTranslatableCaption_str) 
	{
		super();

		this._fValueView_tf = null;
		this._fIndicatorValue_num = undefined;

		this.addChild(this._fBackContainer_sprt = new Sprite());
		this.addChild(this._fCaptionContainer_sprt = new Sprite());
		this.addChild(this._fValueContainer_sprt = new Sprite());

		this._initIndicatorView(aOptTranslatableCaption_str);
	}

	_initIndicatorView(aOptTranslatableCaption_str)
	{
		this._initCaptionView(aOptTranslatableCaption_str);
		this._initValueView();

		this._forceIndicatorUpdate();
	}

	//...INIT

	//CAPTION...
	_initCaptionView(aOptTranslatableCaption_str = undefined)
	{
		let lTranslatableAssetId_str = aOptTranslatableCaption_str === undefined ? this._getDefaultCaptionAssetId() : aOptTranslatableCaption_str;
		if (lTranslatableAssetId_str != undefined)
		{
			this._fCaptionContainer_sprt.addChild((I18.generateNewCTranslatableAsset(lTranslatableAssetId_str)));
		}
	}

	_getDefaultCaptionAssetId()
	{
		return undefined;
	}
	//...CAPTION

	//VALUE...
	_initValueView()
	{
		this._fValueView_tf = new TextField(this._getValueTextFormat());
		this._fValueView_tf.maxWidth = this._getValueMaxWidth();
		this._fValueContainer_sprt.addChild(this._fValueView_tf);
	}

	_getValueTextFormat()
	{
		return null;
	}

	_getValueMaxWidth()
	{
		return 0;
	}

	set _indicatorValue(aValue_num)
	{
		if (!Utils.isNumber(aValue_num) || aValue_num === undefined)
		{
			throw new Error("Invalid indicator value: " + aValue_num);
			return;
		}

		if (this._fIndicatorValue_num === aValue_num)
		{
			return;
		}
		this._fIndicatorValue_num = aValue_num;

		this._forceIndicatorUpdate(aValue_num);
	}

	_forceIndicatorUpdate(aOptValue_num = undefined)
	{
		let lFormattedValue_str = aOptValue_num === undefined ? undefined : this._formatValue(aOptValue_num);
		this._applyFormattedValue(lFormattedValue_str);
	}

	_applyFormattedValue(aFormatedValue_str)
	{
		let lValueView_tf = this._fValueView_tf;
		let lNewValue_str = aFormatedValue_str != undefined ? aFormatedValue_str : _EMPTY_VALUE;

		lValueView_tf.text = lNewValue_str;
		this._onValueChanged();
	}

	_onValueChanged()
	{
	}

	_formatValue(aValue_num)
	{
		let lResult_str;
		if (aValue_num === undefined)
		{
			lResult_str = undefined;;
		}
		else
		{
			lResult_str = APP.currencyInfo.i_formatNumber(aValue_num, true);
		}
		return lResult_str;
	}

	_getFormatedValueDecimalsCount()
	{
		return undefined;
	}
	//...VALUE

	destroy()
	{
		this._fValueView_tf = null;
		this._fIndicatorValue_num = undefined;

		this._fBackContainer_sprt = null;
		this._fCaptionContainer_sprt = null;
		this._fValueContainer_sprt = null;

		super.destroy();
	}
}

export default GUIndicatorView;