import { Utils } from '../../../model/Utils';

var _FORMATED_VALUE_PARTS_DELIMITERS = [".", ","];
var _DELIMITER_INT_ID_DECIMAL = 0;
var _DELIMITER_INT_ID_THOUSAND = 1;

var _MAX_NUMBER_TYPE_SIGNIFICANT_DIGITS_COUNT = 15;
var _EXTENDED_VALUE_PRECISION_LEVEL = 3; //OPTIMAL DEFAULT VALUE

/**
 * Number value fortatting.
 * @class
 */
class NumberValueFormat 
{
	/**
	 * Symbol that is used as decimal separator.
	 * @type {string}
	 * @static
	 */
	static get DECIMAL_DELIMETER () 
	{
		return _FORMATED_VALUE_PARTS_DELIMITERS[_DELIMITER_INT_ID_DECIMAL];
	}

	/**
	 * Format money value.
	 * @param {number} aValue_num - Source number value.
	 * @param {boolean} [aOptConvertToBaseCurrency_bl=true] - Convert from cents to currency format or not.
	 * @param {number} [aOptDecimalsCount_int=2] - Allowed number of digits to appear after the decimal separator.
	 * @param {boolean} [aOptThrowExceptionIfExtraDigitsCount_bl=true] - If true - exception will be thrown for values with total digits amount more then maximum allowed for proper calculations.
	 * @param {boolean} [aOptCutDecimalNulls_bl=false] - Should decimal nulls (that are outside of allowed decimals amount) be removed or not.
	 * @param {number} [aOptAmountOfAllowedDecimals_int=0] - Allowed number of null digits to appear after the decimal separator. Actual only if removing of decimal nulls is on.
	 * @returns {string}
	 * @static
	 */
	static formatMoney(aValue_num, aOptConvertToBaseCurrency_bl = true, aOptDecimalsCount_int = 2, aOptThrowExceptionIfExtraDigitsCount_bl=true, aOptCutDecimalNulls_bl=false, aOptAmountOfAllowedDecimals_int=0)
	{
		if (aValue_num === undefined)
		{
			throw new Error("Formated value is not available: " + aValue_num);
			return undefined;
		}

		if (aOptConvertToBaseCurrency_bl)
		{
			aValue_num = Math.round(aValue_num/100*100000)/100000;
		}
		
		let lRet_str = NumberValueFormat.format(aValue_num, aOptDecimalsCount_int, aOptThrowExceptionIfExtraDigitsCount_bl);
		if (lRet_str === undefined)
		{
			throw new Error("Formated value is not accessible!");
			return undefined;
		}

		let lPointIndex_int = lRet_str.indexOf(NumberValueFormat.DECIMAL_DELIMETER);

		if(aOptCutDecimalNulls_bl && lPointIndex_int > 0)
		{
			for (let i = lRet_str.length - 1; i > lPointIndex_int + aOptAmountOfAllowedDecimals_int; i--)
			{
				if(	lRet_str[i] === "0" || lRet_str[i] === NumberValueFormat.DECIMAL_DELIMETER )
				{
					lRet_str = lRet_str.slice(0, i);
				}
				else
				{
					break;
				}
			}

			if (lRet_str.endsWith(NumberValueFormat.DECIMAL_DELIMETER))
			{
				lRet_str = lRet_str.slice(0, lPointIndex_int);
			}
		}

		return lRet_str;
	}

	/**
	 * Format number value to string.
	 * @param {number} aValue_num - Source number value.
	 * @param {number} aOptDecimalsCount_int - Allowed number of digits to appear after the decimal separator.
	 * @param {boolean} [aOptThrowExceptionIfExtraDigitsCount_bl=true] - If true - exception will be thrown for values with total digits amount more then maximum allowed for proper calculations.
	 * @returns {string}
	 * @static
	 */
	static format(aValue_num, aOptDecimalsCount_int = undefined, aOptThrowExceptionIfExtraDigitsCount_bl=true)
	{
		if (aValue_num === undefined)
		{
			throw new Error("Formated value is not available: " + aValue_num);
			return undefined;
		}

		let lNegativeValue_bl = aValue_num < 0;
		let lAbsValue_num = lNegativeValue_bl ? -aValue_num : aValue_num;
		let lDecimalsCountDefined_bl = aOptDecimalsCount_int !== undefined;
		if (lDecimalsCountDefined_bl)
		{
			if (
					!Utils.isInt(aOptDecimalsCount_int)
					|| aOptDecimalsCount_int < 0
				)
			{
				throw new Error("Invalid DC argument value: " + aOptDecimalsCount_int);
				return undefined;
			}
			let lPrecision_num = 1 / (Math.pow(10, aOptDecimalsCount_int + _EXTENDED_VALUE_PRECISION_LEVEL));

			lAbsValue_num = lAbsValue_num + lPrecision_num; //precision is used to eliminate external calculations falts caused by Number operations precision restrictions; e.g. this allows 0.999999767 to be formated as 1.00 etc
		}

		//int part processing...
		let lAbsIntPart_int = Math.floor(lAbsValue_num); //optimization lAbsValue_num | 0 cannot be used due to one restricts possible value to int32 limits;
		let lRoughFormatedAbsIntPart_str = lAbsIntPart_int.toFixed(0);
		let lAbsIntPart_str = NumberValueFormat.formatAbsIntString(lRoughFormatedAbsIntPart_str);
		let lAbsIntPartSignificantDigitsCount_int = lAbsIntPart_int > 0 ? lRoughFormatedAbsIntPart_str.length : 0;
		//...int part processing

		//precision loss check...
		if ((lAbsIntPartSignificantDigitsCount_int + (lDecimalsCountDefined_bl ? aOptDecimalsCount_int : 0)) > _MAX_NUMBER_TYPE_SIGNIFICANT_DIGITS_COUNT)
		{
			if (aOptThrowExceptionIfExtraDigitsCount_bl)
			{
				throw new Error ("Current value " + aValue_num + " formating with " + aOptDecimalsCount_int + " decimals is not safe (significant precision loss is possible)");
				return undefined;
			}
			else
			{
				console.error("Current value " + aValue_num + " formating with " + aOptDecimalsCount_int + " decimals is not safe (significant precision loss is possible)");

				if (lRoughFormatedAbsIntPart_str.indexOf("+") > 0)
				{
					return lRoughFormatedAbsIntPart_str;
				}

				
				let lAbsValue_str = "" + lAbsValue_num;
				let lDecPart_str = lAbsValue_str.substring(lAbsIntPartSignificantDigitsCount_int+1);
				let lDecPartRequiredLength_int = lDecimalsCountDefined_bl ? aOptDecimalsCount_int : lDecPart_str.length;
				if (lDecPartRequiredLength_int <= lDecPart_str.length)
				{
					lDecPart_str = lDecPart_str.substring(0, lDecPartRequiredLength_int);
				}
				else
				{
					let lSourceDecPart_str = lDecPart_str;
					lDecPart_str = "";
					for (let i=0; i<lDecPartRequiredLength_int; i++)
					{
						lDecPart_str += lSourceDecPart_str[i] || "0";
					}
				}

				let lRet_str = (lNegativeValue_bl ? "-" : "") + lAbsIntPart_str + ((lDecPart_str !== undefined) ? _FORMATED_VALUE_PARTS_DELIMITERS[_DELIMITER_INT_ID_DECIMAL] + lDecPart_str : "");
				return lRet_str;
			}
		}
		//...precision loss check

		//dec part processing...
		let lMaxDecPartSignificantDigitsCount_int = _MAX_NUMBER_TYPE_SIGNIFICANT_DIGITS_COUNT - lAbsIntPartSignificantDigitsCount_int;
		let lPower_int = Math.pow(10, lMaxDecPartSignificantDigitsCount_int);
		let lPoweredAbsIntPart_int = lAbsIntPart_int * lPower_int;
		let lPoweredAbsValue_int = Math.floor(lAbsValue_num * lPower_int);
		let lPoweredDecPart_int = lPoweredAbsValue_int - lPoweredAbsIntPart_int;
		let lUnprefixedRoughFormatedDecPart_str = lPoweredDecPart_int.toFixed(0);
		let lUnprefixedRoughFormatedDecPartLength_int = lUnprefixedRoughFormatedDecPart_str.length;
		let lRoughFormatedDecPart_str = lUnprefixedRoughFormatedDecPart_str;
		for (let i = lUnprefixedRoughFormatedDecPartLength_int; i < lMaxDecPartSignificantDigitsCount_int; i++)
		{
			lRoughFormatedDecPart_str = "0" + lRoughFormatedDecPart_str;
		}

		let lRoughFormatedDecPartLength_int = lRoughFormatedDecPart_str.length;
		let lRoughFormatedDecPartNonSignificantDecimalsCount_int = 0;
		for (let i = lRoughFormatedDecPartLength_int - 1; i >= 0; i--)
		{
			if (lRoughFormatedDecPart_str[i] === "0")
			{
				++lRoughFormatedDecPartNonSignificantDecimalsCount_int;
			}
			else
			{
				break; //for i
			}
		}
		let lRoughFormatedDecPartSignificantDecimalsCount_int = lRoughFormatedDecPartLength_int - lRoughFormatedDecPartNonSignificantDecimalsCount_int;

		let lDecimalsCount_int;
		if (lDecimalsCountDefined_bl)
		{
			lDecimalsCount_int = aOptDecimalsCount_int;
		}
		else
		{
			lDecimalsCount_int = lRoughFormatedDecPartSignificantDecimalsCount_int;
		}

		for (let i = lRoughFormatedDecPartLength_int; i < lDecimalsCount_int; i++)
		{
			lRoughFormatedDecPart_str += "0";
		}

		let lDecPart_str = (lDecimalsCount_int === 0) ? undefined : lRoughFormatedDecPart_str.substr(0, lDecimalsCount_int);
		//...dec part processing

		let lRet_str = (lNegativeValue_bl ? "-" : "") + lAbsIntPart_str + ((lDecPart_str !== undefined) ? _FORMATED_VALUE_PARTS_DELIMITERS[_DELIMITER_INT_ID_DECIMAL] + lDecPart_str : "");
		
		return lRet_str;
	}

	/**
	 * Add thousand separators.
	 * @param {string} aValue_str - Source string.
	 * @returns {string} - Formatted string.
	 * @static
	 */
	static formatAbsIntString(aValue_str)
	{
		let lValue_str = aValue_str;
		let lDigitsCount_int = lValue_str.length;
		let lRet_str = "";
		for (let i = 0; i < lDigitsCount_int; i++)
		{
			lRet_str = lValue_str[lDigitsCount_int - i - 1] + lRet_str;
			if (
					((i + 1) % 3 === 0)
					&& (i !== lDigitsCount_int - 1)
				)
			{
				lRet_str = _FORMATED_VALUE_PARTS_DELIMITERS[_DELIMITER_INT_ID_THOUSAND] + lRet_str;
			}
		}
		return lRet_str;
	}

	/**
	 * Amount of non-zero digits after the decimal separator.
	 * @param {number} num - Source number.
	 * @returns {number}
	 * @static
	 */
	static decimalPlaces(num)
	{
		let match = (''+num).match(/(?:\.(\d+))?(?:[eE]([+-]?\d+))?$/);
		if (!match) { return 0; }
		return Math.max(
			0,
			// Number of digits right of decimal point.
			(match[1] ? match[1].length : 0)
			// Adjust for scientific notation.
			- (match[2] ? +match[2] : 0));
	}
}

export default NumberValueFormat;