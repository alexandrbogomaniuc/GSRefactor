import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import InputText from '../../../../common/PIXI/src/dgphoenix/unified/view/ui/InputText';
import NumberValueFormat from '../../../../common/PIXI/src/dgphoenix//unified/view/custom/values/NumberValueFormat';

export class DecimalPartValidator
{
	static validate(aChar_str)
	{
		let lRestrictedLength_int = APP.appParamsInfo.restrictCoinFractionLength;
		if (DecimalPartValidator.isCharSeparator(aChar_str) && lRestrictedLength_int !== undefined)
		{
			return lRestrictedLength_int > 0;
		}

		return true;
	}

	static isCharSeparator(aChar_str)
	{
		return aChar_str === NumberValueFormat.DECIMAL_DELIMETER; 
	}

	/**
	 * Cases
	 * $12.34	CurrencyDecimalPrecision = 2 && restrictCoinFractionLength = 1. Returns 1. It stands for $12.3
	 * $12.3	CurrencyDecimalPrecision = 1 && restrictCoinFractionLength = 0. Returns 1. It stands for $12
	 * $12.34	CurrencyDecimalPrecision = 2 && restrictCoinFractionLength = 3. Returns 0. It stands for $12.34
	 * @static
	 * @returns 0 if nothing to trunc else difference or zero (if diff is less than 0)
	 */
	static getAmountOfDecimalsToTrunc()
	{
		let lAllowedByCurrency_num = APP.currencyInfo.i_getCurrencyDecimalPrecision();
		let lRestrictedLength_int = APP.appParamsInfo.restrictCoinFractionLength;
		return Math.max(0, lAllowedByCurrency_num - lRestrictedLength_int);
	}
}

class BetInputField extends InputText 
{
	static get EVENT_ON_BLUR()				{ return InputText.EVENT_ON_BLUR; }
	static get EVENT_ON_FOCUS()				{ return InputText.EVENT_ON_FOCUS; }
	static get EVENT_ON_VALUE_CHANGED()		{ return InputText.EVENT_ON_VALUE_CHANGED; }
	static get EVENT_ON_CLICK()				{ return InputText.EVENT_ON_CLICK; }

	constructor(aInputData_obj)
	{
		super(aInputData_obj);
	}

	isCharacterAcceptable(aChar_str)
	{
		if (!DecimalPartValidator.validate(aChar_str))
		{
			return false;
		}

		return super.isCharacterAcceptable(aChar_str)
	}

	// override in child class to adjust additional filters
	__checkNextCharacterAcceptability(aValue_str, aCurrentText_str)
	{
		// additional filter for Crash Game input values...
		let lDecimalPartIndex_int = aCurrentText_str.indexOf(".");
		if (~lDecimalPartIndex_int)
		{
			// rule 1: no excess 'dots'
			if (aValue_str == ".")
				return false;
			// rule 2: up to allowed number of digits in decimal part (2 by default)
			let lRestrictedLength_int = APP.appParamsInfo.restrictCoinFractionLength !== undefined ? APP.appParamsInfo.restrictCoinFractionLength : 2;
			if (aCurrentText_str.length - lDecimalPartIndex_int > lRestrictedLength_int)
				return false;

		}
		// ...additional filter for Crash Game input values

		return this.isCharacterAcceptable(aValue_str);
	}
}

export default BetInputField;